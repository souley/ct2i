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
import java.awt.Point;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.JFrame;

import nl.biggrid.ct.run.ConfigManager;
import nl.biggrid.ct.ui.*;

public class GraphDrawer {

    private IComponentDrawer componentDrawer;
    private Vector<TomoEntity> entities;
    private ConfigManager configManager = ConfigManager.instance();

    public GraphDrawer(final JFrame container) {
        entities = new Vector<TomoEntity>();
        parseConfig();
        componentDrawer = new RoundedComponentDrawer(entities, container);
    }

    JComponent draw() {
        componentDrawer.initializeGraph();
        Vector<Object> processI = new Vector<Object>();
        Vector<Object> processO = new Vector<Object>();
        Object process = null;
        for (TomoEntity entity : entities) {
            if (entity instanceof ProcessEntity) {
                ProcessEntity processEntity = (ProcessEntity) entity;
                Point p = processEntity.getGeometry();
                String label = "<html><center>"+processEntity.getId();
                label += ("<br><small><i>"+processEntity.getCommand()+"</i></small></center></html>");
                process = componentDrawer.addProcess(label, p.x, p.y, processEntity.getCommand());
            } else if (entity instanceof ParamFileEntity) {
                ParamFileEntity fileEntity = (ParamFileEntity) entity;
                Point p = fileEntity.getGeometry();
                String label = "<html><center>"+fileEntity.getId();
                label += ("<br><small><i>"+fileEntity.getURL()+"</i></center></html>");
                if (fileEntity.isInput()) {
                    processI.add(componentDrawer.addParameterFile(label, p.x, p.y, fileEntity.getURL()));
                } else {
                    processO.add(componentDrawer.addParameterFile(label, p.x, p.y, fileEntity.getURL()));
                }
            } else if (entity instanceof ParamDataEntity) {
                ParamDataEntity dataEntity = (ParamDataEntity) entity;
                Point p = dataEntity.getGeometry();
                String label = "<html><center>"+dataEntity.getId();
                if (dataEntity.getValues().size() <= 1) {
                    label += ("<br><small><i>"+dataEntity.getValues().elementAt(0)+"</i></small></center></html>");
                } else if (dataEntity.getValues().size() >= 3) {
                    label += ("<br><small><i>s="+dataEntity.getValues().elementAt(0)+"</i></small>");
                    label += ("<br><small><i>e="+dataEntity.getValues().elementAt(1)+"</i></small>");
                    label += ("<br><small><i>i="+dataEntity.getValues().elementAt(2)+"</i></small></center></html>");
                }
                if ("output".equalsIgnoreCase(dataEntity.getId())) {
                    processI.add(componentDrawer.addParameterData(label, p.x, p.y, dataEntity.getValues(),ParamEntity.ValueType.INTEGER));
                } else {
                    processI.add(componentDrawer.addParameterData(label, p.x, p.y, dataEntity.getValues(),ParamEntity.ValueType.INTEGER));
                }
            }
        }
        for (Object inParam : processI) {
            componentDrawer.connect(inParam, process, "");
        }
        for (Object outParam : processO) {
            componentDrawer.connect(process, outParam, "");
        }
        return componentDrawer.getGraph();
    }

    public JComponent drawGraph() {
        return draw();
    }

    public JComponent drawGraph(final Vector<TomoEntity> vEntities) {
        entities = vEntities;
        componentDrawer.setEntities(vEntities);
        return draw();
    }


    public Vector<TomoEntity> getEntities() {
        return entities;
    }

    Point getGeometry (final String key) {
        return new Point(configManager.getInt(key+".geomx"),
                configManager.getInt(key+".geomy"));
    }

    private ProcessEntity getProcess() {
        final String key = "omnimatch.binary";
        String id = "omnimatch";
		String command = configManager.getString(key+"[@file]");
        Point geom = getGeometry(key);
		ProcessEntity e = new ProcessEntity(id, TomoEntity.Type.PROCESS, command);
        e.setGeometry(geom);
		return e;
	}

	private ParamFileEntity getFileInput(final String key) {
        Point geom = getGeometry(key);
        String id = key.substring(key.indexOf(".")+1);
		ParamFileEntity e = new ParamFileEntity(id, configManager.getString(key+"[@file]"));
        e.setGeometry(geom);
        e.setIsInput(true);
        return e;
	}

	private ParamDataEntity getDataInput(final String key) {
        String id = key.substring(key.indexOf(".")+1);
        ParamDataEntity e = null;
        if (id.equalsIgnoreCase("fourier")) {
            Vector<String> items = new Vector<String>(
                    Arrays.asList(Integer.toString(configManager.getInt(key+".value"))));
            e = new ParamDataEntity(id, items, ParamEntity.ValueType.INTEGER);
        } else if (id.equalsIgnoreCase("output")){
            Vector<String> items = new Vector<String>(
                    Arrays.asList(configManager.getString(key+".value")));
            e = new ParamDataEntity(id, items, ParamEntity.ValueType.STRING);
        } else {
            Vector<String> items = new Vector<String>(Arrays.asList(
                    Integer.toString(configManager.getInt(key+".start")),
                    Integer.toString(configManager.getInt(key+".end")),
                    Integer.toString(configManager.getInt(key+".inc"))));
            e = new ParamDataEntity(id, items, ParamEntity.ValueType.INTEGER);
        }
        Point geom = getGeometry(key);
        e.setGeometry(geom);
		return e;
	}

	private ParamFileEntity getFileOutput(final String key) {
        Point geom = getGeometry(key);
        String id = key.substring(key.indexOf(".")+1);
        String filename = configManager.getString("omnimatch.output.value");
		ParamFileEntity e = new ParamFileEntity(id,  filename + "." + (id.equalsIgnoreCase("ccf") ? id : "ang"));
        e.setGeometry(geom);
        e.setIsInput(false);
        return e;
	}

    private void parseConfig() {
        entities.add(getProcess());
        
        entities.add(getFileInput("omnimatch.tomogram"));
        entities.add(getFileInput("omnimatch.template"));
        entities.add(getFileInput("omnimatch.mask"));
        entities.add(getFileInput("omnimatch.psf"));
        
        entities.add(getDataInput("omnimatch.fourier"));
        entities.add(getDataInput("omnimatch.output"));
        entities.add(getDataInput("omnimatch.phi"));
        entities.add(getDataInput("omnimatch.psi"));
        entities.add(getDataInput("omnimatch.theta"));
        
        entities.add(getFileOutput("omnimatch.ccf"));
        entities.add(getFileOutput("omnimatch.angles"));
    }
}
