
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.biggrid.ct.run;

/**
 *
 * @author Souley
 */
import java.io.File;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import javax.swing.JFrame;
import nl.biggrid.ct.util.MessageBox;

public class RunManager {
    public static final int CMD_FAILED = -1;
    public static final String PBS_JOB_SCRIPT = "pbs_jobscript.sh";
    public static final String SGE_JOB_SCRIPT = "sge_jobscript.sh";
    public static final String LSF_JOB_SCRIPT = "lsf_jobscript.sh";
    public static final String PBS_TEST_CMD   = "pbsnodes -l all";
    public static final String SGE_TEST_CMD   = "qconf -sql";
    public static final String LSF_TEST_CMD   = "/opt/hptc/bin/sinfo";//TODO: LUMC-specific
    public static final String CMD_NOT_FOUND  = "command not found";

    public enum RunType {

        SEQ, PAR, GPU
    };

    public static enum JobAction {

        MONITOR, STOP, OUTPUT, ERROR
    };

    public static enum RunMode {

        USER, AUTO
    };

    static private RunManager _instance = null;

    private IRunner runner = null;
    private Vector<Integer> jobs = new Vector<Integer>();
    private HashMap<Integer, String> jobPaths = new HashMap<Integer, String>();
    private IOutputParser outputParser = null;
    private String userLogin = "";
    private JFrame appWin = null;
    private Vector<Integer> abortedJobs = new Vector<Integer>(); 
    private String logFile = "";
    private String errFile = "";
    private boolean downloadResults = false;

    protected RunManager() {
    }

    static public RunManager instance() {
        if (null == _instance) {
            _instance = new RunManager();
        }
        return _instance;
    }

    public void setAppFrame(final JFrame aFrame) {
        appWin = aFrame;
    }

    public void initRunner(final String host, final String login, final String pwd, final String jobPath) {
        userLogin = login;
        if ("localhost".equalsIgnoreCase(host)) {
            runner = new LocalRunner();
        } else {
            String jobName = (new File(jobPath)).getName();
            if (jobName.regionMatches(true, 0, PBS_JOB_SCRIPT, 0, 6)) {
                RemotePBSJobRunner pbsRunner = new RemotePBSJobRunner(host, login, pwd);
                String tcOut = pbsRunner.runCmd(PBS_TEST_CMD, false, true);
                if (tcOut.indexOf(CMD_NOT_FOUND) == -1) {
                    runner = pbsRunner;
                    outputParser = (new PBSOutputParserFactory()).getOutputParser();
                } else {
                    MessageBox.showErrorMsg(appWin, "PBS: job script or scheduler unknown!");
                }
            } else if (jobName.regionMatches(true, 0, SGE_JOB_SCRIPT, 0, 6)) {
                RemoteSGEJobRunner sgeRunner = new RemoteSGEJobRunner(host, login, pwd);
                String tcOut = sgeRunner.runCmd(SGE_TEST_CMD, false, true);
                if (tcOut.indexOf(CMD_NOT_FOUND) == -1) {
                    runner = sgeRunner;
                    outputParser = (new SGEOutputParserFactory()).getOutputParser();
                } else {
                    MessageBox.showErrorMsg(appWin, "SGE: job script or scheduler unknown!");
                }
            } else if (jobName.regionMatches(true, 0, LSF_JOB_SCRIPT, 0, 6)) {
                RemoteLSFJobRunner lsfRunner = new RemoteLSFJobRunner(host, login, pwd);
                String tcOut = lsfRunner.runCmd(LSF_TEST_CMD, false, true);
                if (tcOut.indexOf(CMD_NOT_FOUND) == -1) {
                    runner = lsfRunner;
                    outputParser = (new LSFOutputParserFactory()).getOutputParser();
                } else {
                    MessageBox.showErrorMsg(appWin, "LSF: job script or scheduler unknown!");
                }
            }
        }
        downloadResults = ("yes".equalsIgnoreCase(ConfigManager.instance().getString("application.downloadresults")));
    }

    public boolean isDownloadResults() {
        return downloadResults;
    }

    public IRunner getRunner() {
        return runner;
    }

    public IOutputParser getOutputParser() {
        return outputParser;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public String getLogFile() {
        return logFile;
    }

    public String getErrorFile() {
        return errFile;
    }

    public String run(final String cmd, final String scriptPath, final String runPath, final RunType runType, final HashMap<String, String> extraParamMap) {
        logFile = extraParamMap.get("logfile");
        errFile = extraParamMap.get("errfile");
        return runner.run(cmd, scriptPath, runPath, runType, extraParamMap);
    }

    public void addJob(final int jobId) {
        jobs.add(jobId);
    }

    public synchronized void removeJob(final int jobId) {
        jobs.remove(Integer.valueOf(jobId));
        abortedJobs.remove(Integer.valueOf(jobId));
    }

    public void addJobPath(final int jobId, final String jobPath) {
        jobPaths.put(jobId, jobPath);
    }

    public String getJobPath(final int jobId) {
        return jobPaths.get(jobId);
    }

    public RunInfo monitor(final int cmdId) {
        return runner.monitor(cmdId);
    }

    public synchronized RunInfo[] monitor(final int[] cmdIds) {
        JobInfo[] jobInfos = (JobInfo[]) runner.monitor(cmdIds);
        if (jobInfos != null) {
            for (JobInfo jobInfo : jobInfos) {
                if (RunUtil.RUN_DONE_MSG.equalsIgnoreCase(jobInfo.getStatus())) {
                    jobInfo.setStatus(checkJobDoneReason(jobInfo.getId()));
                }
            }
        } else {
            return checkJobDoneReasons(cmdIds);
        }
        return jobInfos;
    }

    public String stop(final int cmdId) {
        String cmdOutput = runner.stop(cmdId);
        if (cmdOutput.equalsIgnoreCase(RunUtil.STOP_OK_MSG)) {
            abortedJobs.add(Integer.valueOf(cmdId));
        }
        return cmdOutput;
    }

    public void createSummaryFile(final String where, final String what) {
        runner.createSummaryFile(where, what);
    }

    public boolean isJobMode() {
        return runner instanceof RemotePBSJobRunner;
    }

    public Vector<Integer> getJobIds() {
        return jobs;
    }

    public int getJobId(final String runPath) {
        int jobId = 0;
        if (jobPaths.containsValue(runPath)) {
            Set<Map.Entry<Integer, String>> paths = jobPaths.entrySet();
            for (Iterator<Map.Entry<Integer, String>> iterator = paths.iterator(); iterator.hasNext();) {
                Map.Entry<Integer, String> entry = iterator.next();
                if (entry.getValue().equalsIgnoreCase(runPath)) {
                    return entry.getKey().intValue();
                }
            }
        }
        return jobId;
    }

    public String checkJobDoneReason(final int jobId) {
        if (abortedJobs.contains(Integer.valueOf(jobId))) {
            return RunUtil.ABORT_MSG;
        }
        String jobPath = jobPaths.get(jobId);
        String logFilePath = jobPath + "/log/" + logFile;
        String errFilePath = jobPath + "/log/" + errFile;
        String normLog = runner.run("cat " + logFilePath);
        String errLog = runner.run("cat " + errFilePath);

        if (normLog.isEmpty() && errLog.isEmpty()) {
            return RunUtil.UNKNOWN_MSG;
        } else if (!errLog.isEmpty()) {
            return RunUtil.FAILED_MSG;
        }
        return RunUtil.RUN_DONE_MSG;
    }

    public RunInfo[] checkJobDoneReasons(final int[] cmdIds) {
        JobInfo[] jobInfos = new JobInfo[cmdIds.length];
        for (int i = 0; i < cmdIds.length; i++) {
            jobInfos[i] = new JobInfo(cmdIds[i]);
            jobInfos[i].setStatus(checkJobDoneReason(cmdIds[i]));
        }
        return jobInfos;
    }

    public void invalidateRunner() {
        runner = null;
        outputParser = null;
    }
}
