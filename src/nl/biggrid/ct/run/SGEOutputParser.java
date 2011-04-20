/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.run;

/**
 *
 * @author Souley
 */
public class SGEOutputParser implements IOutputParser {
    public JobInfo[] parseAll(final int[] jobIds, final String cmdOutput) {
        JobInfo[] jobInfos = new JobInfo[jobIds.length];
        String[] lines = cmdOutput.split("\n");
        for (int i = 0; i < jobIds.length; i++) {
            jobInfos[i] = new JobInfo(jobIds[i]);
            int lineCount = 0;
            while (lineCount < lines.length && lines[lineCount].indexOf(Integer.toString(jobIds[i])) == -1) {
                lineCount++;
            }
            if (lineCount < lines.length) {
                parse(jobInfos[i], lines[lineCount].trim());
            } else {
                jobInfos[i].setStatus(RunUtil.RUN_DONE_MSG);
            }
        }
        return jobInfos;
    }

    String getStatusName(final String ssn) {
        String stateChar = ssn.substring(0, 1);
        if (ssn.length() > 1) {
            stateChar = ssn.substring(ssn.length()-1, ssn.length());
        }
        if ("d".equalsIgnoreCase(stateChar)) {
            return RunUtil.DELETE_MSG;
        } else if ("h".equalsIgnoreCase(stateChar)) {
            return RunUtil.HOLD_MSG;
        } else if ("r".equalsIgnoreCase(stateChar)) {
            return RunUtil.RUNNING_MSG;
        } else if ("s".equalsIgnoreCase(stateChar)) {
            return RunUtil.SUSPENDED_MSG;
        } else if ("t".equalsIgnoreCase(stateChar)) {
            return RunUtil.MOVING_MSG;
        } else if ("E".equalsIgnoreCase(stateChar)) {
            return RunUtil.ERROR_MSG;
        } else if ("w".equalsIgnoreCase(stateChar)) {
            return RunUtil.QUEUEING_MSG;
        }
        return RunUtil.UNKNOWN_MSG;
    }

    String getQueueNameFromOutput(final String queueWord) {
        int atPos = queueWord.indexOf("@");
        if (atPos != -1) {
            return queueWord.substring(0, atPos);
        }
        return queueWord;
    }

    String getHostListFromOutput(final String queueWord) {
        int atPos = queueWord.indexOf("@");
        if (atPos != -1) {
            String hostList = "";
            String[] hosts = queueWord.substring(atPos+1).split(",");
            for (String host : hosts) {
                hostList += (host.substring(0, host.indexOf(".")) + ",");
            }
            return hostList.substring(0, hostList.length()-1);
        }
        return queueWord;
    }

    public void parse(JobInfo jobInfo, final String output) {
        final int STATE_COLUMN = 4;
        final int QUEUE_COLUMN = 7;
        final int FULL_LENGTH = 9;
        if (!output.isEmpty()) {
            String[] words = output.split("\\s+");
            if (words.length > STATE_COLUMN) {
                jobInfo.setStatus(getStatusName(words[STATE_COLUMN]));
            }
            if (words.length > QUEUE_COLUMN && words.length == FULL_LENGTH) {
                jobInfo.setQueue(getQueueNameFromOutput(words[QUEUE_COLUMN]));
                jobInfo.setHostList(getHostListFromOutput(words[QUEUE_COLUMN]));
            }
        } else {
            jobInfo.setStatus(RunUtil.UNKNOWN_MSG);
        }
    }
}
