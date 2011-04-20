/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.run;

/**
 *
 * @author Souley
 */
public class JobInfo extends RunInfo {
    private int id = 0;
    private String status = "";
    private String walltimeUsed = "";
    private String memoryUsed = "";
    private String hostList = "";
    private String queue = "";

    public JobInfo(final int jid) {
        id = jid;
    }

    public String getName() {
        return Integer.toString(id);
    }

    public int getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getWalltimeUsed() {
        return walltimeUsed;
    }

    public String getMemoryUsed() {
        return memoryUsed;
    }

    public String getHostList() {
        return hostList;
    }

    public String getQueue() {
        return queue;
    }

    public void setId(final int anId) {
        id = anId;
    }

    public void setStatus(final String aStatus) {
        status = aStatus;
    }

    public void setWalltimeUsed(final String aWT) {
        walltimeUsed = aWT;
    }

    public void setMemoryUsed(final String anMU) {
        memoryUsed = anMU;
    }

    public void setHostList(final String anHL) {
        hostList = anHL;
    }

    public void setQueue(final String aQ) {
        queue = aQ;
    }
}
