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

import nl.biggrid.ct.ui.TomoEntity;
import nl.biggrid.ct.ui.ParamEntity;

public interface IComponentDrawer {
    public void initializeGraph();
    public void finalizeGraph();
    public JComponent getGraph();
    public Object addProcess(final String label, final int x, final int y, final String path);
    public Object addParameterFile(final String label, final int x, final int y, final String fileURL);
    public Object addParameterData(final String label, final int x, final int y, final Vector<String> data, ParamEntity.ValueType valueType);
    public void connect(final Object source, final Object target, final String label);
    public void setEntities(final Vector<TomoEntity> entities);
}
