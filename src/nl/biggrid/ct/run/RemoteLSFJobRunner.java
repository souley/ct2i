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

public class RemoteLSFJobRunner extends RemoteRunner {

    final static String WMS_SUB = "bash -l -c 'bsub -e ./errlog -o ./normlog ";
    final static String WMS_STAT = "bash -l -c 'bjobs";
    final static String WMS_DEL = "bash -l -c 'bkill"; 
    final static String MPIRUN_PATH = "mpirun ";

    public RemoteLSFJobRunner(final String host,
            final String login,
            final String pwd) {
        super(host, login, pwd);
    }

    int getIdFromOutput(final String cmdOutput) {
        String[] items = cmdOutput.split("\\s+");
        if (items.length > 1) {
            return Integer.parseInt(items[1].substring(1, items[1].length()-1));
        }
        return RunManager.CMD_FAILED;
    }

    public String run(final String cmd, final String scriptPath, final String runPath, final RunManager.RunType runType, final HashMap<String, String> extraParamMap) {
        String formattedCmd = cmd.replaceAll("/", "\\\\/");
        String pattern = "";
        String wtOption = "";
        String nodeOption = "";
        String script_create = "";
        if (runType == RunManager.RunType.SEQ) {
            pattern = "'/exit/ i\\" + formattedCmd + "'";
            script_create = "cat " + scriptPath + " | sed -e " + pattern + " > " + runPath + "/" + RunManager.LSF_JOB_SCRIPT;
        } else if (runType == RunManager.RunType.PAR) {
            int nbProcs = Integer.parseInt(extraParamMap.get("cpus"));
            int nbNodes = Integer.parseInt(extraParamMap.get("nodes"));
            int procMax = nbProcs * nbNodes;
            nodeOption = "-n" + procMax;
            pattern = "'/exit/ i\\srun " + formattedCmd + "'";
            script_create = "cat " + scriptPath + " | sed -e " + pattern + " > " + runPath + "/" + RunManager.LSF_JOB_SCRIPT;
        } else if (runType == RunManager.RunType.GPU) {
            // TODO: put appropriate code when required
        }
        String runLogDir = runPath + "/log";
        String wmsCmd = "mkdir -p " + runLogDir + ";cd " + runLogDir + ";" + "chmod u+x ../" + RunManager.LSF_JOB_SCRIPT + ";" + WMS_SUB + wtOption + nodeOption + " ../" + RunManager.LSF_JOB_SCRIPT + "'";
        if (runCmd(script_create, false, false).isEmpty()) {
            String cmdOutput = runCmd(wmsCmd, false, false);
            int jobId = getIdFromOutput(cmdOutput);
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
        String wmsCmd = WMS_STAT + " -l " + jobId;
        RunManager.instance().getOutputParser().parse(jobInfo, runCmd(wmsCmd, false, false));
        return jobInfo;
    }

    public RunInfo[] monitor(final int[] jobIds) {
        String strJobIds = "";
        for (int i = 0; i < jobIds.length; i++) {
            strJobIds += (jobIds[i] + " ");
        }
        strJobIds = strJobIds.trim();
        String wmsCmd = WMS_STAT+ " -l " + strJobIds + "'";
        String wmsCmdOutput = runCmd(wmsCmd, false, true);
        if (wmsCmdOutput.isEmpty()) {
            return null;
        }
        return RunManager.instance().getOutputParser().parseAll(jobIds, wmsCmdOutput);
    }

    String getStopStatus(final int jobId, final String cmdOutput) {
        if (cmdOutput.indexOf("Job <" + jobId + "> is being terminated") != -1) {
            return RunUtil.STOP_OK_MSG;
        }
        return RunUtil.STOP_KO_MSG;
    }

    @Override
    public String stop(final int jobId) {
        String wmsCmd = WMS_DEL + " " + jobId + "'";
        return getStopStatus(jobId, runCmd(wmsCmd, false, true));
    }
}
