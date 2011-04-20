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
public interface IRunner {
    public String    run(final String cmd, final String scriptPath, final String runPath, final RunManager.RunType runType, final HashMap<String, String> extraParamMap);
    public String    stop(final int jobId);
    public RunInfo   monitor(final int jobId);
    public RunInfo[] monitor(final int[] jobIds);
    public void      createSummaryFile(final String where, final String what);
    public String    run(final String cmd);
}
