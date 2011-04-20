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
import java.util.Hashtable;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import com.mxgraph.util.mxConstants;

import nl.biggrid.ct.ui.*;

public class RoundedComponentDrawer implements IComponentDrawer {
    static final int ARTIFACT_WIDTH  = 80;
    static final int ARTIFACT_HEIGHT = 60;
    static final int PROCESS_WIDTH   = 260;
    static final int PROCESS_HEIGHT  = 200;

    mxGraph graph = null;
    mxGraphComponent graphComponent = null;
    Vector<TomoEntity> entities = new Vector<TomoEntity>();
    JInternalFrame currentEditor = null;
    JFrame container = null;
    RoundedComponentDrawer(Vector<TomoEntity> entities, final JFrame container) {
        this.entities = entities;
        this.container = container;
    }

    private void initStyle() {
        mxStylesheet stylesheet = graph.getStylesheet();
        // Style for processes
        Hashtable<String, Object> styleProcess = new Hashtable<String, Object>();
        styleProcess.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        styleProcess.put(mxConstants.STYLE_OPACITY, 50);
        styleProcess.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        styleProcess.put(mxConstants.STYLE_FILLCOLOR, "#ffbbcc");
        styleProcess.put(mxConstants.STYLE_GRADIENTCOLOR, "#ff0000");
        styleProcess.put(mxConstants.STYLE_GRADIENT_DIRECTION, mxConstants.DIRECTION_NORTH);
        styleProcess.put(mxConstants.STYLE_SHADOW, true);
        mxConstants.SHADOW_OFFSETX = 4;
        mxConstants.SHADOW_OFFSETY = 5;
        styleProcess.put(mxConstants.STYLE_ROUNDED, true);
        mxConstants.RECTANGLE_ROUNDING_FACTOR = 0.5;
        styleProcess.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_RECTANGLE);
        stylesheet.putCellStyle("PROCESS", styleProcess);
        // Style for edges
        Hashtable<String, Object> styleEdge = new Hashtable<String, Object>();
        styleEdge.put(mxConstants.STYLE_STARTARROW, mxConstants.ARROW_DIAMOND);
        styleEdge.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
        styleEdge.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ELBOW);
        styleEdge.put(mxConstants.STYLE_ROUNDED, true);
        stylesheet.putCellStyle("EDGE", styleEdge);
        // Style for file parameters using image
        Hashtable<String, Object> styleParamFile = new Hashtable<String, Object>();
        styleParamFile.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_LABEL);
        styleParamFile.put(mxConstants.STYLE_IMAGE, "/nl/tomo/resources/images/ascii.png");
        styleParamFile.put(mxConstants.STYLE_IMAGE_WIDTH, 48);
        styleParamFile.put(mxConstants.STYLE_IMAGE_HEIGHT, 48);
        styleParamFile.put(mxConstants.STYLE_OPACITY, 50);
        styleParamFile.put(mxConstants.STYLE_SHADOW, true);
        mxConstants.SHADOW_OFFSETX = 4;
        mxConstants.SHADOW_OFFSETY = 5;
        styleParamFile.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        styleParamFile.put(mxConstants.STYLE_FILLCOLOR, "#bbffcc");
        styleParamFile.put(mxConstants.STYLE_GRADIENTCOLOR, "#00ff00");
        styleParamFile.put(mxConstants.STYLE_GRADIENT_DIRECTION, mxConstants.DIRECTION_NORTH);
        styleParamFile.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_RECTANGLE);
        stylesheet.putCellStyle("FILE_IMAGE", styleParamFile);
        // Style for scalar parameters using icon
        Hashtable<String, Object> styleParamScalar = new Hashtable<String, Object>();
        styleParamScalar.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_LABEL);
        styleParamScalar.put(mxConstants.STYLE_IMAGE, "/nl/tomo/resources/images/db_add.png");
        styleParamScalar.put(mxConstants.STYLE_IMAGE_WIDTH, 48);
        styleParamScalar.put(mxConstants.STYLE_IMAGE_HEIGHT, 48);
        styleParamScalar.put(mxConstants.STYLE_LABEL_POSITION, mxConstants.ALIGN_TOP);
        styleParamScalar.put(mxConstants.STYLE_OPACITY, 50);
        styleParamScalar.put(mxConstants.STYLE_SHADOW, true);
        mxConstants.SHADOW_OFFSETX = 4;
        mxConstants.SHADOW_OFFSETY = 5;

        styleParamScalar.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        styleParamScalar.put(mxConstants.STYLE_FILLCOLOR, "#32ffff");
        styleParamScalar.put(mxConstants.STYLE_GRADIENTCOLOR, "#00ffff");
        styleParamScalar.put(mxConstants.STYLE_GRADIENT_DIRECTION, mxConstants.DIRECTION_NORTH);
        styleParamScalar.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_RECTANGLE);
        stylesheet.putCellStyle("PARAMETER_IMAGE", styleParamScalar);
    }

    public void initializeGraph() {
        graph = new CustomGraph(entities);
        initStyle();
        graph.setEnabled(true);
        graph.setMultigraph(true);
        graph.setCellsMovable(false);
        graph.setCellsResizable(false);
        graph.setCellsEditable(false);
        
        graph.getModel().beginUpdate();
        initializeGraphComponent();
    }

    public void finalizeGraph() {
        graph.getModel().endUpdate();
    }

  	public boolean isEditEvent(MouseEvent e)
	{
		return (e != null) ? e.getClickCount() == 2 : false;
	}

    String getIdInHtmlLabel(final String label) {
        String id = "";
        final String START_TAG = "<center>";
        final String END_TAG = "<br>";
        final int HTML_TAG_LENGTH = START_TAG.length();
        int start = label.indexOf(START_TAG);
        int end = label.indexOf(END_TAG);
        if (start != -1) {
            id = label.substring(start+HTML_TAG_LENGTH, end);
        }
        return id;
    }

    void initializeGraphComponent() {
        try {
            graphComponent = new CustomGraphComponent(graph, entities);
        } finally {
			this.finalizeGraph();
		}
        if (graphComponent != null) {
            graphComponent.getGraphControl().addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseReleased(MouseEvent e)
                {
                   if (!e.isConsumed() && isEditEvent(e)) {
                        int x = e.getX();
                        int y = e.getY();
                        Object cell = graphComponent.getCellAt(x, y);
                        if (cell != null)
                        {
                            String label = graph.getLabel(cell);
                            String id = getIdInHtmlLabel(label);
                            TomoEntity entity = findEntity(id);
                            //showEntity(e.getXOnScreen(), e.getYOnScreen(), entity, cell);
                            showEntity(x, y, entity, cell);
                        }
                   }
                }
            });
        }
    }

    public JComponent getGraph() {
        return graphComponent;
    }


    public Object addProcess(final String label, final int x, final int y, final String path) {
        return graph.insertVertex(graph.getDefaultParent(), null, label, x, y, PROCESS_WIDTH, PROCESS_HEIGHT, "PROCESS");
    }

    public Object addParameterFile(final String label, final int x, final int y, final String fileURL) {
        return graph.insertVertex(graph.getDefaultParent(), null, label, x, y, ARTIFACT_WIDTH, ARTIFACT_HEIGHT, "FILE_IMAGE");
    }

    public Object addParameterData(final String label, final int x, final int y, final Vector<String> data, ParamEntity.ValueType valueType) {
        return graph.insertVertex(graph.getDefaultParent(), null, label, x, y, ARTIFACT_WIDTH, ARTIFACT_HEIGHT, "PARAMETER_IMAGE");
    }
   
    public void connect(final Object source, final Object target, final String label) {
        graph.insertEdge(graph.getDefaultParent(), null, label, source, target, "EDGE");
    }

    public void setEntities(final Vector<TomoEntity> vEntities) {
        entities = vEntities;
    }

    TomoEntity findEntity(final String label) {
        TomoEntity result = null;
        for (TomoEntity entity : entities) {
            if (label.equalsIgnoreCase(entity.getId())) {
                result = entity;
                break;
            }
        }
        return result;
    }

    void setComponent(final JInternalFrame entityEditor) {
        JLayeredPane containerLayeredPane = container.getLayeredPane();
        containerLayeredPane.add(entityEditor);
        JLayeredPane.putLayer(entityEditor, JLayeredPane.MODAL_LAYER);
        containerLayeredPane.moveToFront(entityEditor);
        entityEditor.show();
        try {
            entityEditor.setSelected(true);
        } catch (java.beans.PropertyVetoException pve) {
            pve.printStackTrace();
        }
        currentEditor = entityEditor;
    }

    void showEntity(final int x, final int y, final TomoEntity entity, final Object cell) {
        if (currentEditor != null) {
            currentEditor.setVisible(false);
            currentEditor.dispose();
        }
        if (entity instanceof ParamDataEntity) {
            ParamDataEntity dentity = (ParamDataEntity)entity;
            if (dentity.getValues().size() > 1) {
                ParamDataEditor paramEditor = new ParamDataEditor(graphComponent, cell, x, y, dentity);
                setComponent(paramEditor);
            } else {
                ParamDataEditorScalar paramEditor = new ParamDataEditorScalar((CustomGraphComponent)graphComponent, cell, x, y, dentity);
                setComponent(paramEditor);
            }
        } else if (entity instanceof ParamFileEntity) {
            ParamFileEditor paramEditor = new ParamFileEditor(graphComponent, cell, x, y, (ParamFileEntity)entity);
            setComponent(paramEditor);
        } else if (entity instanceof ProcessEntity) {
            ProcessEditor processEditor = new ProcessEditor(graphComponent, cell, x, y, (ProcessEntity)entity);
            setComponent(processEditor);
        }
    }

}
