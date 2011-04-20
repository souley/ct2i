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
import java.awt.Color;
import java.util.HashMap;

import javax.swing.JFrame;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import nl.biggrid.ct.ui.TomoEntity;

public class CustomGraphComponent extends mxGraphComponent {
    Vector<TomoEntity> entities; 
    HashMap<String, TomoEntity> entityMap = new HashMap<String, TomoEntity>();
    JFrame currentEditor = null;

    public CustomGraphComponent(final mxGraph graph, Vector<TomoEntity> entities) {
        super(graph);
        setToolTips(true);
        setConnectable(false);
      	getViewport().setOpaque(false);
		setBackground(Color.WHITE);

        this.entities = entities;
         for (TomoEntity entity : entities) {
            entityMap.put(entity.getId(), entity);
        }
    }

    public CustomGraphComponent(final CustomGraphComponent rhs) {
        super(rhs.getGraph());
        setToolTips(true);
        setConnectable(false);
      	getViewport().setOpaque(false);
		setBackground(Color.WHITE);

        entities = rhs.getEntities();
        for (TomoEntity entity : entities) {
            entityMap.put(entity.getId(), entity);
        }
    }

    public Vector<TomoEntity> getEntities() {
        return entities;
    }

    public HashMap<String, TomoEntity> getEntityMap() {
        for (TomoEntity entity : entities) {
            entityMap.put(entity.getId(), entity);
        }
        return entityMap;
    }

}
