/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.graph;

/**
 *
 * @author Souley
 */

import java.util.Vector;
import javax.swing.JComponent;
import java.util.Hashtable;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import com.mxgraph.util.mxConstants;

import nl.biggrid.ct.ui.ParamEntity;
import nl.biggrid.ct.ui.TomoEntity;

public class DefaultComponentDrawer implements IComponentDrawer {
    static final int ARTIFACT_WIDTH = 100;
    static final int ARTIFACT_HEIGHT = 50;
    static final int PROCESS_WIDTH = 260;
    static final int PROCESS_HEIGHT = 200;

    mxGraph graph = null;

    private void initStyle() {
        mxStylesheet stylesheet = graph.getStylesheet();
        // Style for processes
        Hashtable<String, Object> styleProcess = new Hashtable<String, Object>();
        styleProcess.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        styleProcess.put(mxConstants.STYLE_OPACITY, 50);
        styleProcess.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        styleProcess.put(mxConstants.STYLE_FILLCOLOR, "#bbccff");
        styleProcess.put(mxConstants.STYLE_GRADIENTCOLOR, "#ff0000");
        styleProcess.put(mxConstants.STYLE_GRADIENT_DIRECTION, mxConstants.DIRECTION_NORTH);
        styleProcess.put(mxConstants.STYLE_ROUNDED, true);
        styleProcess.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_RECTANGLE);
        stylesheet.putCellStyle("PROCESS", styleProcess);
        // Style for edges
        Hashtable<String, Object> styleEdge = new Hashtable<String, Object>();
        styleEdge.put(mxConstants.STYLE_STARTARROW, mxConstants.ARROW_DIAMOND);
        styleEdge.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
        styleEdge.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ELBOW);
        styleEdge.put(mxConstants.STYLE_ROUTING_CENTER_Y, -0.5);
        stylesheet.putCellStyle("EDGE", styleEdge);
        // Style for file parameters
        Hashtable<String, Object> styleParamFile = new Hashtable<String, Object>();
        styleParamFile.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
        styleParamFile.put(mxConstants.STYLE_OPACITY, 50);
        styleParamFile.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        styleParamFile.put(mxConstants.STYLE_FILLCOLOR, "#bbccff");
        styleParamFile.put(mxConstants.STYLE_GRADIENTCOLOR, "#00ff00");
        styleParamFile.put(mxConstants.STYLE_GRADIENT_DIRECTION, mxConstants.DIRECTION_NORTH);
        styleParamFile.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_ELLIPSE);
        stylesheet.putCellStyle("FILE", styleParamFile);
        // Style for scalar parameters
        Hashtable<String, Object> styleParamScalar = new Hashtable<String, Object>();
        styleParamScalar.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
        styleParamScalar.put(mxConstants.STYLE_OPACITY, 50);
        styleParamScalar.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        styleParamScalar.put(mxConstants.STYLE_FILLCOLOR, "#bbccff");
        styleParamScalar.put(mxConstants.STYLE_GRADIENTCOLOR, "#0000ff");
        styleParamScalar.put(mxConstants.STYLE_GRADIENT_DIRECTION, mxConstants.DIRECTION_NORTH);
        styleParamScalar.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_RHOMBUS);
        styleEdge.put(mxConstants.STYLE_ROUTING_CENTER_Y, -0.5);
        stylesheet.putCellStyle("PARAMETER", styleParamScalar);
    }

    public void initializeGraph() {
        graph = new mxGraph();
        initStyle();
        graph.setResetEdgesOnResize(true);
        graph.getModel().beginUpdate();
    }

    public void finalizeGraph() {
        graph.getModel().endUpdate();
    }

    public JComponent getGraph() {
        JComponent result = null;
        try {
             result = new mxGraphComponent(graph);
        }
		finally
		{
			this.finalizeGraph();
            return result;
		}
    }

    public Object addProcess(final String label, final int x, final int y, final String path) {
        return graph.insertVertex(graph.getDefaultParent(), null, label, x, y, PROCESS_WIDTH, PROCESS_HEIGHT, "PROCESS");
    }

    public Object addParameterFile(final String label, final int x, final int y, final String fileURL) {
        return graph.insertVertex(graph.getDefaultParent(), null, label, x, y, ARTIFACT_WIDTH, ARTIFACT_HEIGHT, "FILE");
    }

    public Object addParameterData(final String label, final int x, final int y, final Vector<String> data, ParamEntity.ValueType valueType) {
        return graph.insertVertex(graph.getDefaultParent(), null, label, x, y, ARTIFACT_WIDTH, ARTIFACT_HEIGHT, "PARAMETER");
    }

    public void connect(final Object source, final Object target, final String label) {
        graph.insertEdge(graph.getDefaultParent(), null, label, source, target, "EDGE");
    }

    public void setEntities(final Vector<TomoEntity> vEntities) {
    }

}
