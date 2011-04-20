/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.biggrid.ct.run;

import java.util.HashMap;

/**
 *
 * @author Souley
 */
public class RemotePBSJobRunner extends RemoteRunner {

    final static String WMS_SUB = "qsub";
    final static String WMS_STAT = "qstat";
    final static String WMS_DEL = "qdel";

    public RemotePBSJobRunner(final String host,
            final String login,
            final String pwd) {
        super(host, login, pwd);
    }

    int getIdFromOutput(final String cmdOutput) {
        int firstDotPos = cmdOutput.indexOf(".");
        if (firstDotPos != -1) {
            return Integer.parseInt(cmdOutput.substring(0, firstDotPos));
        }
        return RunManager.CMD_FAILED;
    }

    public String run(final String cmd, final String scriptPath, final String runPath, final RunManager.RunType runType, final HashMap<String, String> extraParamMap) {
        String formattedCmd = cmd.replaceAll("/", "\\\\/");

        String pattern = "";
        String wtPattern = "'s/#PBS -lwalltime.*/#PBS -lwalltime=" + extraParamMap.get("walltime") + "/'";
        String logPattern = "'s/#PBS -o .*/#PBS -o " + extraParamMap.get("logfile") + "/'";
        String errPattern = "'s/#PBS -e .*/#PBS -e " + extraParamMap.get("errfile") + "/'";
        String nodePattern = "";
        String script_create = "";
        if (runType == RunManager.RunType.SEQ) {
            pattern = "'/exit/ i\\" + formattedCmd + "'";
            nodePattern = "'s/#PBS -lnodes/# #PBS -lnodes/'";
            script_create = "cat " + scriptPath + " | sed -e " + nodePattern + " -e " + wtPattern + " -e " + logPattern + " -e " + errPattern + " -e " + pattern + " > " + runPath + "/" + RunManager.PBS_JOB_SCRIPT;
        } else if (runType == RunManager.RunType.PAR) {
            nodePattern = "'s/#PBS -lnodes.*/#PBS -lnodes=" + extraParamMap.get("nodes") + ":ppn=" + extraParamMap.get("cpus") + "/'";
            pattern = "'/exit/ i\\mpirun -np $nprocs " + formattedCmd + "'";
            script_create = "cat " + scriptPath + " | sed -e " + nodePattern + " -e " + wtPattern + " -e " + logPattern + " -e " + errPattern + " -e " + pattern + " > " + runPath + "/" + RunManager.PBS_JOB_SCRIPT;
        } else if (runType == RunManager.RunType.GPU) {
            nodePattern = "'s/#PBS -lnodes.*/#PBS -q q_gpu -lnodes=" + extraParamMap.get("nodes") + ":ppn=" + extraParamMap.get("cpus") + "/'";
            pattern = "'/exit/ i\\mpirun -np $nprocs " + formattedCmd + "'";
            script_create = "cat " + scriptPath + " | sed -e " + nodePattern + " -e " + wtPattern + " -e " + logPattern + " -e " + errPattern + " -e " + pattern + " > " + runPath + "/" + RunManager.PBS_JOB_SCRIPT;
        }
        String runLogDir = runPath + "/log";
        String wmsCmd = "mkdir -p " + runLogDir + ";cd " + runLogDir + ";" + WMS_SUB + " ../" + RunManager.PBS_JOB_SCRIPT;
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
        String wmsCmd = WMS_STAT + " -f " + jobId;
        RunManager.instance().getOutputParser().parse(jobInfo, runCmd(wmsCmd, false, false));
        return jobInfo;
    }

    public RunInfo[] monitor(final int[] jobIds) {
        String strJobIds = "";
        for (int i = 0; i < jobIds.length; i++) {
            strJobIds += (jobIds[i] + " ");
        }
        strJobIds.trim();
        String wmsCmd = WMS_STAT + " -f " + strJobIds;
        String wmsCmdOutput = runCmd(wmsCmd, false, true);
        //System.out.println("### RJR:m cmdout -> "+wmsCmdOutput);
        if (wmsCmdOutput.isEmpty()) {
            return null;
        }
        return RunManager.instance().getOutputParser().parseAll(jobIds, wmsCmdOutput);
    }

    String getStopStatus(final String cmdOutput) {
        if (cmdOutput.isEmpty()) {
            return RunUtil.STOP_OK_MSG;
        }
        return RunUtil.STOP_KO_MSG;
    }

    @Override
    public String stop(final int jobId) {
        String wmsCmd = WMS_DEL + " " + jobId;
        return getStopStatus(runCmd(wmsCmd, false, true));
    }
}
