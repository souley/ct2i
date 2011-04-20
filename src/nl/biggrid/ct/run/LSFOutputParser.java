/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.biggrid.ct.run;

/**
 *
 * @author Souley
 */
public class LSFOutputParser implements IOutputParser {

    public JobInfo[] parseAll(final int[] jobIds, final String cmdOutput) {
        JobInfo[] jobInfos = new JobInfo[jobIds.length];
        String[] lines = cmdOutput.split("\n");
        for (int i = 0; i < jobIds.length; i++) {
            jobInfos[i] = new JobInfo(jobIds[i]);
            String jobOutput = "";
            int lineCount = 0;
            while (lineCount < lines.length && lines[lineCount].indexOf("Unknown Job Id " + jobIds[i]) == -1 && lines[lineCount].indexOf("Job <" + jobIds[i] + ">") == -1) {
                lineCount++;
            }
            if (lineCount < lines.length) {
                if (lines[lineCount].indexOf("Unknown Job Id " + jobIds[i]) != -1) {
                    jobInfos[i].setStatus(RunUtil.RUN_DONE_MSG);
                } else if (lines[lineCount].indexOf("Job <" + jobIds[i] + ">") != -1) {
                    jobOutput += (lines[lineCount] + "\n");
                    lineCount++;
                    while (lineCount < lines.length && !lines[lineCount].isEmpty() && lines[lineCount].indexOf("Job <") == -1) {
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
        if ("DONE".equalsIgnoreCase(ssn)) {
            return RunUtil.RUN_DONE_MSG;
        } else if ("SSUSP".equalsIgnoreCase(ssn) || "PSUSP".equalsIgnoreCase(ssn) || "USUSP".equalsIgnoreCase(ssn)) {
            return RunUtil.SUSPENDED_MSG;
        } else if ("EXITING".equalsIgnoreCase(ssn)) {
            return RunUtil.EXITING_MSG;
        } else if ("PEND".equalsIgnoreCase(ssn)) {
            return RunUtil.QUEUEING_MSG;
        } else if ("RUN".equalsIgnoreCase(ssn)) {
            return RunUtil.RUNNING_MSG;
        } else if ("ZOMBI".equalsIgnoreCase(ssn)) {
            return RunUtil.ZOMBI_MSG;
        }
        return RunUtil.UNKNOWN_MSG;
    }

    String getHostListFromOutput(final String nodeLine) {
        String hosts = "";
        String[] nodes = nodeLine.split("/");
        for (int i = 0; i < nodes.length; i++) {
            String node = nodes[i].substring(nodes[i].indexOf("+") + 1);
            if (!node.equals("0") && hosts.indexOf(node) == -1) {
                hosts += (node + ", ");
            }
        }
        int endList = hosts.lastIndexOf(",");
        if (endList == -1) {
            endList = hosts.length() - 1;
        }
        return hosts.substring(0, endList);
    }

    public void parse(JobInfo jobInfo, final String output) {
        final String HOSTLIST_KEY = "slurm_alloc";
        if (!output.isEmpty()) {
            String[] lines = output.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                //System.out.println("LINE: " + line);
                if (line.indexOf("Status") != -1) {
                    String[] avPairs = line.split(",");
                    System.out.println("Pairs: " + avPairs.length);
                    if (avPairs.length > 4) {
                        String[] status = avPairs[3].split("\\s+");
                        if (status.length > 2) {
                            jobInfo.setStatus(getStatusName(status[2].substring(1, status[2].length() - 1)));
                        }
                        String[] queue = avPairs[4].split("\\s+");
                        if (queue.length > 2) {
                            jobInfo.setQueue(queue[2].substring(1, queue[2].length() - 1));
                        }
                    }
                } else if (line.indexOf(HOSTLIST_KEY) != -1) {
                    String remLine = line.substring(line.indexOf(HOSTLIST_KEY) + HOSTLIST_KEY.length());
                    jobInfo.setHostList(remLine.substring(1, remLine.indexOf(";")));
                }
            }
        } else {
            jobInfo.setStatus(RunUtil.UNKNOWN_MSG);
        }
    }
}
