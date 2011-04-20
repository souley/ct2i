/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.run;

/**
 *
 * @author Souley
 */
public class PBSOutputParser implements IOutputParser {
    public JobInfo[] parseAll(final int[] jobIds, final String cmdOutput) {
        JobInfo[] jobInfos = new JobInfo[jobIds.length];
        String[] lines = cmdOutput.split("\n");
        for (int i = 0; i < jobIds.length; i++) {
            jobInfos[i] = new JobInfo(jobIds[i]);
            String jobOutput = "";
            int lineCount = 0;
            while (lineCount < lines.length && lines[lineCount].indexOf("Unknown Job Id " + jobIds[i]) == -1 && lines[lineCount].indexOf("Job Id: " + jobIds[i]) == -1) {
                lineCount++;
            }
            if (lineCount < lines.length) {
                if (lines[lineCount].indexOf("Unknown Job Id " + jobIds[i]) != -1) {
                    jobInfos[i].setStatus(RunUtil.RUN_DONE_MSG);
                } else if (lines[lineCount].indexOf("Job Id: " + jobIds[i]) != -1) {
                    lineCount++;
                    while (lineCount < lines.length && !lines[lineCount].isEmpty() && lines[lineCount].indexOf("Job Id: ") == -1) {
                        jobOutput += (lines[lineCount] + "\n");
                        lineCount++;
                    }
                    parse(jobInfos[i], jobOutput);
                } else {
                    jobInfos[i].setStatus(RunUtil.UNKNOWN_MSG);
                }
            }
        }
        return jobInfos;
    }

    String getStatusName(final String ssn) {
        if ("C".equalsIgnoreCase(ssn)) {
            return RunUtil.RUN_DONE_MSG;
        } else if ("H".equalsIgnoreCase(ssn)) {
            return RunUtil.HOLD_MSG;
        } else if ("E".equalsIgnoreCase(ssn)) {
            return RunUtil.EXITING_MSG;
        } else if ("Q".equalsIgnoreCase(ssn)) {
            return RunUtil.QUEUEING_MSG;
        } else if ("R".equalsIgnoreCase(ssn)) {
            return RunUtil.RUNNING_MSG;
        } else if ("T".equalsIgnoreCase(ssn)) {
            return RunUtil.MOVING_MSG;
        } else if ("W".equalsIgnoreCase(ssn)) {
            return RunUtil.WAITING_MSG;
        }
        return RunUtil.UNKNOWN_MSG;
    }

    String getHostListFromOutput(final String nodeLine) {
        String hosts = "";
        String[] nodes = nodeLine.split("/");
        for (int i=0; i<nodes.length; i++) {
            String node = nodes[i].substring(nodes[i].indexOf("+")+1);
            if (!node.equals("0") && hosts.indexOf(node) == -1) {
                hosts += (node + ", ");
            }
        }
        int endList = hosts.lastIndexOf(",");
        if (endList == -1) {
            endList = hosts.length()-1;
        }
        return hosts.substring(0, endList);
    }

    public void parse(JobInfo jobInfo, final String output) {
        if (!output.isEmpty()) {
            String[] lines = output.split("\n");
            for (int i=0; i<lines.length; i++) {
                String line = lines[i].trim();
                if (line.indexOf("job_state") != -1) {
                    String[] words = line.split("\\s+");
                    if (words.length > 2) {
                        jobInfo.setStatus(getStatusName(words[2]));
                    }
                } else if (line.indexOf("resources_used.walltime") != -1) {
                    String[] words = line.split("\\s+");
                    if (words.length > 2) {
                        jobInfo.setWalltimeUsed(words[2]);
                    }
                } else if (line.indexOf("resources_used.mem") != -1) {
                    String[] words = line.split("\\s+");
                    if (words.length > 2) {
                        jobInfo.setMemoryUsed(words[2]);
                    }
                } else if (line.indexOf("exec_host") != -1) {
                    String[] words = line.split("\\s+");
                    if (words.length > 2) {
                        jobInfo.setHostList(getHostListFromOutput(words[2]));
                    }
                } else if (line.indexOf("queue") != -1) {
                    String[] words = line.split("\\s+");
                    if (words.length > 2) {
                        jobInfo.setQueue(words[2]);
                    }
                }
            }
        } else {
            jobInfo.setStatus(RunUtil.UNKNOWN_MSG);
        }
    }
}
