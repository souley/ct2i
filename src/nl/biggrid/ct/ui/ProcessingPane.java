/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.biggrid.ct.ui;

/**
 *
 * @author Souley
 */
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.windows.WindowsLookAndFeel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import nl.biggrid.ct.jgoodies.Settings;

import nl.biggrid.ct.graph.*;
import nl.biggrid.ct.jgoodies.Factory;
import nl.biggrid.ct.run.*;
import nl.biggrid.ct.util.*;

public class ProcessingPane extends JPanel
        implements ActionListener {

    protected static final Dimension PREFERRED_SIZE =
            LookUtils.IS_LOW_RESOLUTION
            ? new Dimension(740, 500)
            : new Dimension(740, 500);
    private final String DEFAULT_SEQ_EXEC = "omnimatch.seq";
    private final String DEFAULT_PAR_EXEC = "omnimatch.par";
    private final String DEFAULT_GPU_EXEC = "omnimatch.gpu";
    private final String DEFAULT_WF_NAME = "OmniMatch";
    private final Color RUNNING_WF_BG = new Color(204, 204, 204);
    private final Settings settings;
    private int initialWidth = PREFERRED_SIZE.width,  initialHeight = PREFERRED_SIZE.height;
    private int currentWidth,  currentHeight;
    private CustomGraphComponent currentGraphComponent = null;
    private JPanel container = null;
    private MainWindow appFrame = null;
    private String execHost = "";
    private String execLogin = "";
    private String execPwd = "";
    private String toolPath = "";
    private String inputPath = "";
    private String outputPath = "";
    private String runSubPath = "";
    private String jobPath = "";
    private String cmdAndArgs;
    private LinkedHashMap<String, TomoEntity> sortedMap = new LinkedHashMap<String, TomoEntity>();
    private int currentRunIndex = -1;
    private RunManager runManager = RunManager.instance();
    private RunManager.RunType runType = RunManager.RunType.SEQ;
    private LinkedHashMap<Integer, RunItem> runMap = new LinkedHashMap<Integer, RunItem>();
    private int currentJobId = 0;
    private String currentJobOutputPath = "";
    private ExperimentLogger experimentLogger = null;
    private LogViewer currentLogViwer = null;
    private JobStatusTable jobStatusTable = null;
    ComponentAdapter myResizeListener = new ComponentAdapter() {

        @Override
        public void componentResized(ComponentEvent e) {
            currentWidth = container.getWidth();
            currentHeight = container.getHeight();
            if (initialWidth != currentWidth || initialHeight != currentHeight) {
                double widthRatio = Math.round(currentWidth / initialWidth);
                double heightRatio = Math.round(currentHeight / initialHeight);
                double scale = Math.max(widthRatio, heightRatio);
                currentGraphComponent.zoomTo((scale >= 1 ? scale : 1), true);
                initialWidth = currentWidth;
                initialHeight = currentHeight;
            }
        }
    };

    public ProcessingPane(Settings aSettings, MainWindow aFrame, JobStatusTable aJobStatusTable) {
        super();
        settings = aSettings;
        appFrame = aFrame;
        jobStatusTable = aJobStatusTable;

        buildGUI();

        jbtnRun.addActionListener(this);
        jbtnStop.addActionListener(this);
        jbtnMon.addActionListener(this);

        jbtnDel.addActionListener(this);
        jbtnJO.addActionListener(this);
        jbtnJE.addActionListener(this);
//        jbtnPrevRun.addActionListener(this);
//        jbtnNxtRun.addActionListener(this);

        experimentLogger = new ExperimentLogger();
        container = this;
        container.addComponentListener(myResizeListener);
        runManager.setAppFrame(appFrame);
    }

    public void setRunType(final RunManager.RunType aRunType) {
        runType = aRunType;
        //if (currentRunIndex >= 0) {
        syncWorkflowEditor();
    //}
    }

    public CustomGraphComponent getGraphComponent() {
        return currentGraphComponent;
    }

    void runWorkflow() {
        if (appFrame.getSettingsWindow() != null && appFrame.getSettingsWindow().isCheckingPassed()) {
            SettingsDialog settingsWin = appFrame.getSettingsWindow();
            execHost = settingsWin.getRemoteHost();
            execLogin = settingsWin.getRemoteLogin();
            execPwd = settingsWin.getRemotePass();

            toolPath = settingsWin.getToolPath();
            inputPath = settingsWin.getInputPath();
            outputPath = settingsWin.getOutputPath();
            jobPath = settingsWin.getJobPath();
            HashMap<String, TomoEntity> entityMap = currentGraphComponent.getEntityMap();
            runSubPath = settingsWin.buildRunSubDir(((ParamDataEntity) entityMap.get("output")).getValues().elementAt(0));
            cmdAndArgs = sortParameters(entityMap);
            String fileCheckOutput = settingsWin.checkFileArgs(entityMap);
            if (fileCheckOutput.isEmpty()) {
                String userMsg;
                String[] items = cmdAndArgs.split(" ");
                userMsg = "<html><b><font color=\"#0000ff\">About to run</b>:<br>";
                for (int i = 0; i < items.length; i++) {
                    if (i > 3 && (i <= 12)) {
                        userMsg += (items[i] + " ");
                        if (i == 12) {
                            userMsg += "<br>";
                        }
                    } else {
                        userMsg += (items[i] + "<br>");
                    }
                }
                userMsg += "</html>";
                if (MessageBox.askUser(appFrame, "Run command ...", userMsg) == JOptionPane.OK_OPTION) {
                    currentRunIndex = runMap.size();
                    settingsWin.createRunSubDir();
                    if (null == runManager.getRunner()) {
                        runManager.initRunner(execHost, execLogin, execPwd, jobPath);
                    }
                    HashMap<String, String> extraParamMap = new HashMap<String, String>();
                    extraParamMap.put("walltime", appFrame.getWalltime());
                    extraParamMap.put("nodes", appFrame.getNumberOfNodes());
                    extraParamMap.put("cpus", appFrame.getCPUPerNode());
                    extraParamMap.put("logfile", appFrame.getLogFile());
                    extraParamMap.put("errfile", appFrame.getErrorFile());
                    currentJobOutputPath = outputPath + "/" + runSubPath;
                    String runMsg = runManager.run(cmdAndArgs, jobPath, currentJobOutputPath, runType, extraParamMap);
                    if (runMsg.equalsIgnoreCase(RunUtil.RUN_STARTED_MSG)) {
                        int jobId = runManager.getJobId(currentJobOutputPath);
                        if (jobId != 0) {
                            currentJobId = jobId;
                            RunItem aRun = new RunItem(currentJobOutputPath, runType, extraParamMap);
                            String runName = "Run_" + currentRunIndex + "_" + jobId;
                            runMap.put(currentRunIndex, aRun);
                            aRun.setGraph(currentGraphComponent);
                            aRun.setId(jobId);
                            aRun.setName(runName);
                            MessageBox.showInfoMsg(appFrame, "<html><b><font color=\"#0000ff\">" + runMsg + "</b></html>");
                            showPreviousRun();
                            experimentLogger.addExperiment(appFrame, execHost, execLogin, toolPath, inputPath, outputPath, jobPath, runSubPath);
                        }
                    }
                    if (!jobStatusTable.autoRefreshStarted()) {
                        jobStatusTable.startAutoRefresh();
                    }
                    String summary = makeSummary();
                    runManager.createSummaryFile(currentJobOutputPath, summary);
                }
            } else { // Handle cmd args error
                MessageBox.showErrorMsg(appFrame, fileCheckOutput);
            }
        } else {
            MessageBox.showErrorMsg(appFrame, "Execution environment not configured.");
        }
    }

    void monitorWorkflow(int[] jobIds, boolean isSingle) {
        int jobs;
        if (jobIds == null) {
            Vector<Integer> jobIIds = runManager.getJobIds();
            jobs = jobIIds.size();
            jobIds = new int[jobs];
            for (int i = 0; i < jobs; i++) {
                jobIds[i] = jobIIds.elementAt(i);
            }
        } else {
            jobs = jobIds.length;
        }
        if (jobs > 0) {
            RunInfo[] runInfos = runManager.monitor(jobIds);
            JobInfo[] jobInfos = (JobInfo[]) runInfos;
            JobDetails jobInfoWin = new JobDetails(appFrame, jobInfos, (isSingle ? "Job" : "Jobs"));
            jobInfoWin.setVisible(true);
        } else {
            MessageBox.showInfoMsg(appFrame, "Sorry! There is no job!");
        }
    }

    void stopWorkflow() {
        String status = runManager.stop(currentJobId);
        MessageBox.showInfoMsg(appFrame, "<html><b><font color=\"#0000ff\">" + status + "</b></html>");
    }

    void syncWorkflowEditor() {
        HashMap<String, TomoEntity> entityMap = currentGraphComponent.getEntityMap();
        TomoEntity process = entityMap.get("omnimatch");
        if (process != null && process instanceof ProcessEntity) {
            ProcessEntity processEntity = (ProcessEntity) process;
            String oldCmd = processEntity.getCommand();
            switch (runType) {
                case SEQ:
                    processEntity.setCommand(DEFAULT_SEQ_EXEC);
                    break;
                case PAR:
                    processEntity.setCommand(DEFAULT_PAR_EXEC);
                    break;
                case GPU:
                    processEntity.setCommand(DEFAULT_GPU_EXEC);
            }
            Object[] cells = currentGraphComponent.getCells(currentGraphComponent.getGraph().getGraphBounds().getRectangle());
            for (int i = 0; i < cells.length; i++) {
                String label = currentGraphComponent.getGraph().getLabel(cells[i]);
                if (label.indexOf(oldCmd) != -1) {
                    String newLabel = label.replaceFirst(oldCmd, processEntity.getCommand());
                    currentGraphComponent.getGraph().cellLabelChanged(cells[i], newLabel, false);
                    break;
                }
            }
        }
    }

    String makeSummary() {
        String summary = "";
        XMLWriter writer = new XMLWriter(new Vector(sortedMap.values()));
        summary = writer.dom2String(false, appFrame.getWalltime());
        return summary;
    }

    String sortParameters(HashMap<String, TomoEntity> entityMap) {
        String result = "";
        TomoEntity process = entityMap.get("omnimatch");
        if (process != null) {
            result += (toolPath + "/" + ((ProcessEntity) process).getCommand());
            result += " ";
        } else {
            return "***Cannot find tool path, aborting execution***";
        }
        sortedMap.put("omnimatch", process);
        TomoEntity tomo = entityMap.get("tomogram");
        if (tomo != null) {
            result += (inputPath + "/" + ((ParamFileEntity) tomo).getURL());
            result += " ";
        } else {
            return "***Volume file not specified, aborting execution***";
        }
        sortedMap.put("tomogram", tomo);
        TomoEntity template = entityMap.get("template");
        if (template != null) {
            result += (inputPath + "/" + ((ParamFileEntity) template).getURL());
            result += " ";
        } else {
            return "***Template file not specified, aborting execution***";
        }
        sortedMap.put("template", template);
        TomoEntity output = entityMap.get("output");
        Vector theValues = new Vector();
        if (output != null) {
            Vector<String> values = ((ParamDataEntity) output).getValues();
            for (int i = 0; i < Math.min(1, values.size()); i++) {
                String runDir = (outputPath + "/" + runSubPath + "/" + values.elementAt(i));
                result += runDir;
                theValues.add(i, runDir);
                result += " ";
            }
        } else {
            return "***No output file name specified, aborting execution***";
        }
        sortedMap.put("output", output);
        TomoEntity phi = entityMap.get("phi");
        if (phi != null) {
            Vector<String> values = ((ParamDataEntity) phi).getValues();
            for (int i = 0; i < Math.min(3, values.size()); i++) {
                result += values.elementAt(i);
                result += " ";
            }
        } else {
            return "***Phi angles not specified, aborting execution***";
        }
        sortedMap.put("phi", phi);
        TomoEntity psi = entityMap.get("psi");
        if (psi != null) {
            Vector<String> values = ((ParamDataEntity) psi).getValues();
            for (int i = 0; i < Math.min(3, values.size()); i++) {
                result += values.elementAt(i);
                result += " ";
            }
        } else {
            return "***Psi angles not specified, aborting execution***";
        }
        sortedMap.put("psi", psi);
        TomoEntity theta = entityMap.get("theta");
        if (theta != null) {
            Vector<String> values = ((ParamDataEntity) theta).getValues();
            for (int i = 0; i < Math.min(3, values.size()); i++) {
                result += values.elementAt(i);
                result += " ";
            }
        } else {
            return "***Theta angles not specified, aborting execution***";
        }
        sortedMap.put("theta", theta);
        TomoEntity function = entityMap.get("psf");
        String fqURL = "";
        if (function != null) {
            fqURL = (inputPath + "/" + ((ParamFileEntity) function).getURL());
            result += fqURL;
            result += " ";
        } else {
            return "***No ps function file specified, aborting execution***";
        }
        sortedMap.put("psf", function);
        TomoEntity mask = entityMap.get("mask");
        if (mask != null) {
            fqURL = (inputPath + "/" + ((ParamFileEntity) mask).getURL());
            result += fqURL;
            result += " ";
        } else {
            return "***No mask file specified, aborting execution***";
        }
        sortedMap.put("mask", mask);
        TomoEntity fourier = entityMap.get("fourier");
        if (fourier != null) {
            Vector<String> values = ((ParamDataEntity) fourier).getValues();
            theValues = values;
            for (int i = 0; i < Math.min(1, values.size()); i++) {
                result += values.elementAt(i);
            }
        } else {
            return "***No Fourier size specified, aborting execution***";
        }
        sortedMap.put("fourier", fourier);
        return result;
    }

    void syncGUI(final boolean enablePrev, final boolean enableNext, final boolean enableRun, final boolean enableMon, final HashMap<String, String> eParams, final RunManager.RunType runType) {
        appFrame.syncGUI(enablePrev, enableNext, enableRun, enableMon, eParams, runType);
        jbtnRun.setEnabled(enableRun);
        jbtnStop.setEnabled(enableMon);
        jbtnMon.setEnabled(enableMon);
        jbtnJO.setEnabled(enableMon);
        jbtnJE.setEnabled(enableMon);
        jbtnDel.setEnabled(enableMon);
        if (enableRun) {
            jlblNavCount.setText("0/" + runMap.size());
        } else {
            jlblNavCount.setText((currentRunIndex + 1) + "/" + runMap.size());
        }
    }

    void decRunIndex() {
        currentRunIndex--;//test
    }

    void incRunIndex() {
        currentRunIndex++;//test
    }

    void showPreviousRun() {
//        currentRunIndex--;//test
        if (currentRunIndex >= 0 && currentRunIndex < runMap.size()) {
            RunItem prevRun = runMap.get(Integer.valueOf(currentRunIndex));
            if (prevRun != null) {
                currentGraphComponent = prevRun.getGraph();
                currentGraphComponent.setBackground(RUNNING_WF_BG);
                graphContainer.setViewportView(currentGraphComponent);
                jlblRunName.setText(prevRun.getName());
                currentJobId = prevRun.getId();
                currentJobOutputPath = prevRun.getDirectory();
                //boolean enableNext = !jbtnNxtRun.isEnabled();
                boolean enableNext = !appFrame.isNextEnabled();
                boolean enablePrev = (currentRunIndex != 0);
                syncGUI(enablePrev, enableNext, false, true, prevRun.getExtraParamMap(), prevRun.getRunType());
                jobStatusTable.setSelectedJob(currentJobId);
//                if (!jbtnNxtRun.isEnabled()) {
//                    jbtnNxtRun.setEnabled(true);
//                }
//                if (currentRunIndex == 0) {
//                    jbtnPrevRun.setEnabled(false);
//                }
            }
        }
    }

    void showNextRun() {
//        currentRunIndex++;//test
//        if (currentRunIndex > 0 && !jbtnPrevRun.isEnabled()) {
//            jbtnPrevRun.setEnabled(true);
//        }
        if (currentRunIndex < runMap.size()) {
            RunItem nextRun = runMap.get(Integer.valueOf(currentRunIndex));
            if (nextRun != null) {
                currentGraphComponent = nextRun.getGraph();
                currentGraphComponent.setBackground(RUNNING_WF_BG);
                graphContainer.setViewportView(currentGraphComponent);
                jlblRunName.setText(nextRun.getName());
                currentJobId = nextRun.getId();
                currentJobOutputPath = nextRun.getDirectory();
                boolean enablePrev = (currentRunIndex > 0);
                syncGUI(enablePrev, true, false, true, nextRun.getExtraParamMap(), nextRun.getRunType());
                jobStatusTable.setSelectedJob(currentJobId);
            }
        } else {
            currentRunIndex = runMap.size();
            showDefaultState();
        }
    }

    void showDefaultState() {
        //jbtnNxtRun.setEnabled(false);
        //GraphDrawer drawer = new GraphDrawer(DEFAULT_CONFIG_FILE, appFrame);
        GraphDrawer drawer = new GraphDrawer(appFrame);
        currentGraphComponent = (CustomGraphComponent) drawer.drawGraph();
        graphContainer.setViewportView(currentGraphComponent);
        jlblRunName.setText(DEFAULT_WF_NAME);
        currentJobId = 0;
        syncGUI((runMap.size() > 0), false, true, false, null, RunManager.RunType.SEQ);
        setRunType(RunManager.RunType.SEQ);
        jobStatusTable.clearSelection();
    }

    void showSpecificJob(final int jobId) {
        for (Map.Entry<Integer, RunItem> keyVal : runMap.entrySet()) {
            if (keyVal.getValue().getId() == jobId) {
                RunItem theRun = keyVal.getValue();
                currentJobId = jobId;
                currentGraphComponent = theRun.getGraph();
                currentGraphComponent.setBackground(RUNNING_WF_BG);
                graphContainer.setViewportView(currentGraphComponent);
                jlblRunName.setText(theRun.getName());
                currentJobOutputPath = theRun.getDirectory();
                currentRunIndex = keyVal.getKey().intValue();
                syncGUI((currentRunIndex > 0), true, false, true, theRun.getExtraParamMap(), theRun.getRunType());
                break;
            }
        }
    }

    private void removeCurrentJob() {
        runMap.remove(Integer.valueOf(currentRunIndex));
        runManager.removeJob(currentJobId);
        jobStatusTable.removeCompletedJob(Integer.toString(currentJobId));
        LinkedHashMap<Integer, RunItem> runMapCopy = new LinkedHashMap<Integer, RunItem>();
        Set<Map.Entry<Integer, RunItem>> jobs = runMap.entrySet();
        for (Iterator<Map.Entry<Integer, RunItem>> iterator = jobs.iterator(); iterator.hasNext();) {
            Map.Entry<Integer, RunItem> entry = iterator.next();
            Integer key = entry.getKey();
            int intKey = key.intValue();
            if (intKey > currentRunIndex) {
                intKey--;
                RunItem runItem = entry.getValue();
                String runName = "Run_" + intKey + "_" + runItem.getId();
                runItem.setName(runName);
                runMapCopy.put(Integer.valueOf(intKey), runItem);
            } else {
                runMapCopy.put(key, entry.getValue());
            }
        }
        runMap = runMapCopy;
        showNextRun();
    }

    private void buildGUI() {
        setLayout(new BorderLayout());
        add(buildContentPane(), BorderLayout.CENTER);
        initialWidth = 740;
        initialHeight = 500;
    }

    private JComponent buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildToolBar(), BorderLayout.NORTH);
        panel.add(buildGraphPanel(), BorderLayout.CENTER);
        return panel;
    }

    private Component buildGraphPanel() {
        //GraphDrawer drawer = new GraphDrawer(DEFAULT_CONFIG_FILE, appFrame);
        GraphDrawer drawer = new GraphDrawer(appFrame);
        currentGraphComponent = (CustomGraphComponent) drawer.drawGraph();
        graphContainer = Factory.createStrippedScrollPane(currentGraphComponent);
//        initialWidth = graphContainer.getWidth();
//        initialHeight = graphContainer.getHeight();
        return graphContainer;
    }

    private Component buildToolBar() {
        jtButtons = new JToolBar();
        jtButtons.setFloatable(true);
        jtButtons.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        jtButtons.putClientProperty(
                Options.HEADER_STYLE_KEY,
                settings.getToolBarHeaderStyle());
        jtButtons.putClientProperty(
                PlasticLookAndFeel.BORDER_STYLE_KEY,
                settings.getToolBarPlasticBorderStyle());
        jtButtons.putClientProperty(
                WindowsLookAndFeel.BORDER_STYLE_KEY,
                settings.getToolBarWindowsBorderStyle());
        jtButtons.putClientProperty(
                PlasticLookAndFeel.IS_3D_KEY,
                settings.getToolBar3DHint());

        jbtnRun = createToolBarButton("run.gif", "Run job");
        jbtnStop = createToolBarButton("stop.gif", "Stop job");
        jbtnMon = createToolBarButton("console.gif", "Monitor job");

        jbtnDel = createToolBarButton("delete.gif", "Close job window");
        jbtnJE = createToolBarButton("error.png", "Show job errors");
        jbtnJO = createToolBarButton("eye.png", "Show job output");
        //jbtnPrevRun = createToolBarButton("back.gif", "Go to previous run");
        //jbtnNxtRun = createToolBarButton("forward.gif", "Go to next run");

        jlblRunName = new javax.swing.JLabel("OmniMatch");
        jlblRunName.setFont(new java.awt.Font("Tahoma", 1, 11));
        jlblRunName.setForeground(new java.awt.Color(153, 0, 0));
        jlblNavCount = new javax.swing.JLabel("0/0");
        jlblNavCount.setFont(new java.awt.Font("Tahoma", 1, 11));
        jlblNavCount.setForeground(new java.awt.Color(153, 0, 0));

        //jtButtons.add(jbtnPrevRun);
        //jtButtons.addSeparator();
        //jtButtons.add(jbtnNxtRun);
        //jtButtons.add(Box.createHorizontalGlue());
        jtButtons.add(jlblNavCount);
        jtButtons.addSeparator();
        jtButtons.add(jlblRunName);
        jtButtons.add(Box.createHorizontalGlue());
        jtButtons.add(jbtnRun);
        jtButtons.addSeparator();
        jtButtons.add(jbtnStop);
        jtButtons.addSeparator();
        jtButtons.add(jbtnMon);
        jtButtons.addSeparator();
        //jtButtons.add(Box.createHorizontalGlue());
        jtButtons.add(jbtnJO);
        jtButtons.addSeparator();
        jtButtons.add(jbtnJE);
        jtButtons.addSeparator();
        jtButtons.add(jbtnDel);

        jbtnStop.setEnabled(false);
        jbtnMon.setEnabled(false);
//        jbtnPrevRun.setEnabled(false);
//        jbtnNxtRun.setEnabled(false);

        jbtnJO.setEnabled(false);
        jbtnJE.setEnabled(false);
        jbtnDel.setEnabled(false);

        return jtButtons;
    }

    // Helper Code **********************************************************************
    /**
     * Looks up and returns an icon for the specified filename suffix.
     */
    protected static ImageIcon readImageIcon(String filename) {
        URL url = ProcessingPane.class.getResource("/resources/images/" + filename);
        return new ImageIcon(url);
    }

    protected JButton createToolBarButton(String iconName, String toolTipText) {
        JButton button = new JButton(readImageIcon(iconName));
        button.setToolTipText(toolTipText);
        button.setFocusable(false);
        return button;
    }

    void setLogViewer(final LogViewer aLogViewer) {
        if (currentLogViwer != null) {
            currentLogViwer.setVisible(false);
            currentLogViwer.dispose();
        }
        currentLogViwer = aLogViewer;
        currentLogViwer.setLocationRelativeTo(this);
        currentLogViwer.setVisible(true);
    }

    //Handle action events from all the buttons.
    public void actionPerformed(ActionEvent e) {
        Object emitter = e.getSource();
        if (emitter instanceof JButton) {
            if (emitter.equals(jbtnJO)) {
                setLogViewer(new LogViewer(currentJobOutputPath, currentJobId, true));
            } else if (emitter.equals(jbtnJE)) {
                setLogViewer(new LogViewer(currentJobOutputPath, currentJobId, false));
//            } else if (emitter.equals(jbtnPrevRun)) {
//                currentRunIndex--;
//                showPreviousRun();
//            } else if (emitter.equals(jbtnNxtRun)) {
//                currentRunIndex++;
//                showNextRun();
            } else if (emitter.equals(jbtnDel)) {
                removeCurrentJob();
            } else if (emitter.equals(jbtnRun)) {
                if (appFrame.readyToRun()) {
                    runWorkflow();
                }
            } else if (emitter.equals(jbtnMon)) {
                int[] jobid = new int[1];
                Arrays.fill(jobid, currentJobId);
                monitorWorkflow(jobid, true);
            } else if (emitter.equals(jbtnStop)) {
                stopWorkflow();
            }
        }
    }
    private javax.swing.JButton jbtnDel;
    private javax.swing.JButton jbtnJE;
    private javax.swing.JButton jbtnJO;
    //private javax.swing.JButton jbtnNxtRun;
    //private javax.swing.JButton jbtnPrevRun;
    private javax.swing.JButton jbtnRun;
    private javax.swing.JButton jbtnStop;
    private javax.swing.JButton jbtnMon;
    private javax.swing.JLabel jlblNavCount;
    private javax.swing.JLabel jlblRunName;
    private JScrollPane graphContainer = null;
    private JToolBar jtButtons = null;
}
