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

import com.mxgraph.view.mxGraph;

import nl.biggrid.ct.ui.TomoEntity;

public class CustomGraph extends mxGraph {
    Vector<TomoEntity> entities = new Vector<TomoEntity>();

    CustomGraph(Vector<TomoEntity> entities) {
        this.entities = entities;
        setHtmlLabels(true);
    }

    String getEntityId(final String label) {
        final String START_PATTERN = "<center>";
        final int START_PATTERN_LENGTH = START_PATTERN.length();
        String id = "";
        int tagIndex = label.indexOf(START_PATTERN);
        if (tagIndex != -1) {
            id = label.substring(tagIndex+START_PATTERN_LENGTH, label.indexOf("<br>"));
        }
        return id;
    }
    TomoEntity findEntity(final String label) {
        TomoEntity result = null;
        for (TomoEntity entity : entities) {
            if (entity.getId().equalsIgnoreCase(getEntityId(label))) {
                result = entity;
                break;
            }
        }

        return result;
    }

    /**
     * Prints out some useful information about the cell in the tooltip.
     */
    @Override
    public String getToolTipForCell(Object cell)
    {
        String tip = "<html>";

        if (getModel().isEdge(cell))
        {
            tip += "connector";
        } else {
            String label = getLabel(cell);
            TomoEntity entity = findEntity(label);
            if (entity != null) {
                tip += entity.getId();
            }
        }
        tip += "</html>";
        return tip;
    }

}
