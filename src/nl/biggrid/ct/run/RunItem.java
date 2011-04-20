/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.run;

import java.util.HashMap;
import nl.biggrid.ct.graph.CustomGraphComponent;
/**
 *
 * @author Souley
 */
public class RunItem {
    private int id;
    private String name;
    private String directory;
    private RunManager.RunType runType;
    private HashMap<String, String> extraParamMap;
    private CustomGraphComponent graph;

    public RunItem(String aDirectory, RunManager.RunType aRunType, HashMap<String, String> aParamMap) {
        directory = aDirectory;
        runType = aRunType;
        extraParamMap = aParamMap;
    }

    public void setId(final int anId) {
        id = anId;
    }

    public void setName(final String aName) {
        name = aName;
    }

    public void setGraph(final CustomGraphComponent aGraph) {
        graph = aGraph;
    }

    public String getDirectory() {
        return directory;
    }

    public RunManager.RunType getRunType() {
        return runType;
    }

    public HashMap<String, String> getExtraParamMap() {
        return extraParamMap;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CustomGraphComponent getGraph() {
        return graph;
    }
}
