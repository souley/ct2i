/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.run;

/**
 *
 * @author Souley
 */
public interface IOutputParser {
    public void parse(JobInfo jobInfo, final String output);
    public JobInfo[] parseAll(final int[] jobIds, final String cmdOutput);
}
