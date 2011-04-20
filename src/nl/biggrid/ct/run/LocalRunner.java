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
public class LocalRunner implements IRunner {
    class LocalRunInfo extends RunInfo {
        private String cmdOutput;
        LocalRunInfo(final String c) {
            cmdOutput = c;
        }
        public String getName() {
            return cmdOutput;
        }
    }

    public String run(final String cmd, final String script, final String runPath, final RunManager.RunType runType, final HashMap<String, String> extraParamMap) {
        return "";
    }

    public String stop(final int cmdId) {
        return "";
    }

    public RunInfo monitor(final int cmdId) {
        return new LocalRunInfo("");
    }

    public RunInfo[] monitor(final int[] jobIds) {
        LocalRunInfo[] runInfos = new LocalRunInfo[jobIds.length];
        return runInfos;
    }

    public void createSummaryFile(final String where, final String what) {
    }

    public String run(final String cmd) {
        return "";
    }
}
