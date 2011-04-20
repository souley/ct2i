/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.biggrid.ct.run;

/**
 *
 * @author Souley
 */
import java.util.HashMap;

public class RemoteSGEJobRunner extends RemoteRunner {

    final static String WMS_SUB = "qsub";
    final static String WMS_STAT = "qstat";
    final static String WMS_DEL = "qdel";
    final static String MPIRUN_PATH = "/usr/local/Cluster-Apps/openmpi/gcc/64/1.1/bin/mpirun";

    public RemoteSGEJobRunner(final String host,
            final String login,
            final String pwd) {
        super(host, login, pwd);
    }

    int getIdFromOutput(final String cmdOutput) {
        String[] items = cmdOutput.split("\\s+");
        if (items.length > 2) {
            return Integer.parseInt(items[2]);
        }
        return RunManager.CMD_FAILED;
    }

    public String run(final String cmd, final String scriptPath, final String runPath, final RunManager.RunType runType, final HashMap<String, String> extraParamMap) {
        String formattedCmd = cmd.replaceAll("/", "\\\\/");

        String pattern = "";
        String wtPattern = "'s/#$ -l h_rt=.*/#$ -l h_rt=" + extraParamMap.get("walltime") + "/'";
        String logPattern = "'s/#$ -o .*/#$ -o " + extraParamMap.get("logfile") + "/'";
        String errPattern = "'s/#$ -e .*/#$ -e " + extraParamMap.get("errfile") + "/'";
        String nodePattern = "";
        String script_create = "";
        if (runType == RunManager.RunType.SEQ) {
            pattern = "'/exit/ i\\" + formattedCmd + "'";
            script_create = "cat " + scriptPath + " | sed -e " + wtPattern + " -e " + logPattern + " -e " + errPattern + " -e " + pattern + " > " + runPath + "/" + RunManager.SGE_JOB_SCRIPT;
        } else if (runType == RunManager.RunType.PAR) {
            int nbProcs = Integer.parseInt(extraParamMap.get("cpus"));
            int nbNodes = Integer.parseInt(extraParamMap.get("nodes"));
            int procMax = nbProcs * nbNodes;
            //nodePattern = "'/echo Start now/ i\\#$ -pe prun " + extraParamMap.get("cpus") + "-" + procMax + "'";
            nodePattern = "'/echo starting OMNIMATCH/ i\\#$ -pe prun " + extraParamMap.get("cpus") + "-" + procMax + "'";
            pattern = "'/exit/ i\\" + MPIRUN_PATH + " -np $NSLOTS " + formattedCmd + "'";
            script_create = "cat " + scriptPath + " | sed -e " + nodePattern + " -e " + wtPattern + " -e " + logPattern + " -e " + errPattern + " -e " + pattern + " > " + runPath + "/" + RunManager.SGE_JOB_SCRIPT;
        } else if (runType == RunManager.RunType.GPU) {
            //nodePattern = "'s/#PBS -lnodes.*/#PBS -q q_gpu -lnodes=" + extraParamMap.get("nodes") + ":ppn=" + extraParamMap.get("cpus") + "/'";
            //pattern = "'/exit/ i\\mpirun -np $nprocs " + formattedCmd + "'";
            //script_create = "cat " + scriptPath + " | sed -e " + nodePattern + " -e " + wtPattern + " -e " + pattern + " > " + runPath + "/" + RunManager.JOB_SCRIPT;
        }
        String runLogDir = runPath + "/log";
        String wmsCmd = "mkdir -p " + runLogDir + ";cd " + runLogDir + ";" + WMS_SUB + " ../" + RunManager.SGE_JOB_SCRIPT;
        if (runCmd(script_create, false, false).isEmpty()) {
            int jobId = getIdFromOutput(runCmd(wmsCmd, false, false));
            if (jobId != RunManager.CMD_FAILED) {
                RunManager.instance().addJob(jobId);
                RunManager.instance().addJobPath(jobId, runPath);
                return RunUtil.RUN_STARTED_MSG;
            } else {
                runCmd("rm -rf "+runPath, false, false);
            }
        }
        return RunUtil.RUN_NOT_STARTED_MSG;
    }

    @Override
    public RunInfo monitor(final int jobId) {
        JobInfo jobInfo = new JobInfo(jobId);
        String wmsCmd = WMS_STAT + " -j " + jobId;
        RunManager.instance().getOutputParser().parse(jobInfo, runCmd(wmsCmd, false, false));
        return jobInfo;
    }

    public RunInfo[] monitor(final int[] jobIds) {
        String wmsCmd = WMS_STAT + " -u " + RunManager.instance().getUserLogin();
        String wmsCmdOutput = runCmd(wmsCmd, false, true);
        if (wmsCmdOutput.isEmpty()) {
            return null;
        }
        return RunManager.instance().getOutputParser().parseAll(jobIds, wmsCmdOutput);
    }

    String getStopStatus(final int jobId, final String cmdOutput) {
        if (cmdOutput.indexOf("registered the job " + jobId + " for deletion") != -1 ||
            cmdOutput.indexOf("has deleted job " + jobId) != -1) {
            return RunUtil.STOP_OK_MSG;
        }
        return RunUtil.STOP_KO_MSG;
    }

    @Override
    public String stop(final int jobId) {
        String wmsCmd = WMS_DEL + " " + jobId;
        return getStopStatus(jobId, runCmd(wmsCmd, false, true));
    }
}
