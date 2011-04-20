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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.net.URL;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;


import javax.swing.*;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.windows.WindowsLookAndFeel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import nl.biggrid.ct.jgoodies.*;

import com.mxgraph.util.mxConstants;

import java.awt.Font;
import javax.swing.text.MaskFormatter;
import nl.biggrid.ct.run.*;
import nl.biggrid.ct.util.*;
import nl.biggrid.ct.help.AboutViewer;
import nl.biggrid.ct.help.HelpViewer;

public class MainWindow extends JFrame
        implements ActionListener, ItemListener, ChangeListener {

    static final Dimension PREFERRED_SIZE =
            LookUtils.IS_LOW_RESOLUTION
            ? new Dimension(760, 650)
            : new Dimension(760, 650);
    static final String TAB_TITLES[] = new String[]{"Preprocessing", "OmniMatch", "Postprocessing"};
    static final Font BOLD_FONT = new Font("Tahoma", Font.BOLD, 11);
    static final Font PLAIN_FONT = new Font("Tahoma", Font.PLAIN, 11);
    WindowAdapter ExitListener = new WindowAdapter() {

        @Override
        public void windowClosing(WindowEvent e) {
            if (settingsWin != null) {
                settingsWin.setWallTime(jftfWT.getText());
                settingsWin.setNumberOfNodes(jftfNoN.getText());
                settingsWin.setCPUPerNode(jftfCPN.getText());
                settingsWin.setLogFile(jftfLogFile.getText());
                settingsWin.setErrorFile(jftfErrFile.getText());
            }
            if (processingTab != null) {
                if (MessageBox.askUser(mainFrame, "Save ...", "Save settings?") == JOptionPane.OK_OPTION) {
                    configManager.save(settingsWin, processingTab.getProcessingPane().getGraphComponent().getEntities());
                }
            }
        }
    };
    
    private final Settings settings;
    private SettingsDialog settingsWin = null;
    private MainWindow mainFrame = null;
    private RunManager.RunType runType = RunManager.RunType.SEQ;
    private HashMap<String, String> savedExtraParam = new HashMap<String, String>();
    private HelpViewer helpViewer = null;
    private AboutViewer aboutViewer = null;
    private ProcessingTab processingTab = null;
    private JPanel preprocessingPane = null;
    private JPanel postprocessingPane = null;
    private int lastSelectedTab = -1;
    private ConfigManager configManager = ConfigManager.instance();

    protected MainWindow(Settings givenSettings, final String configFile) {
        settings = givenSettings;
        configureUI();

        configManager.init(configFile);

        mainFrame = this;
        setupRemoteEnv();

        initComponents();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        addWindowListener(ExitListener);
    }

    public void shutdown() {
        setVisible(false);
        dispose();
        System.exit(0);
    }

    public void initWindow() {
        setVisible(true);
        setupListeners();
    }

    void setupListeners() {
        jbNew.addActionListener(this);
        jbNext.addActionListener(this);
        jbPrev.addActionListener(this);

        //jbRun.addActionListener(this);
        jbMonitor.addActionListener(this);
        //jbStop.addActionListener(this);
        jbSettings.addActionListener(this);
        jrbSeq.addItemListener(this);
        jrbPar.addItemListener(this);
        jrbGPU.addItemListener(this);
        jmiContent.addActionListener(this);
        jmiAbout.addActionListener(this);

        if (jrbSeq.isSelected()) {
            runType = RunManager.RunType.SEQ;
        } else if (jrbPar.isSelected()) {
            runType = RunManager.RunType.PAR;
        } else if (jrbGPU.isSelected()) {
            runType = RunManager.RunType.GPU;
        }

        jftfWT.addActionListener(this);
        jftfNoN.addActionListener(this);
        jftfCPN.addActionListener(this);
        jftfLogFile.addActionListener(this);
        jftfErrFile.addActionListener(this);

        setDefaultFieldValues();
    }

    public SettingsDialog getSettingsWindow() {
        return settingsWin;
    }

    public String getWalltime() {
        return jftfWT.getText();
    }

    public String getNumberOfNodes() {
        return jftfNoN.getText();
    }

    public String getCPUPerNode() {
        return jftfCPN.getText();
    }

    public String getLogFile() {
        return jftfLogFile.getText();
    }

    public String getErrorFile() {
        return jftfErrFile.getText();
    }

    public JComponent getTabbedPane() {
        return tabContainer;
    }

    public boolean isNextEnabled() {
        return jbNext.isEnabled();
    }

    public void syncGUI(final boolean enablePrev, final boolean enableNext, final boolean enableRun, final boolean enableMon, final HashMap<String, String> eParams, final RunManager.RunType runType) {
        if (eParams != null) {
            jftfWT.setText(eParams.get("walltime"));
            jftfNoN.setText(eParams.get("nodes"));
            jftfCPN.setText(eParams.get("cpus"));
            jftfLogFile.setText(eParams.get("logfile"));
            jftfErrFile.setText(eParams.get("errfile"));
        } else {
            jftfWT.setText(savedExtraParam.get("walltime"));
            jftfNoN.setText(savedExtraParam.get("nodes"));
            jftfCPN.setText(savedExtraParam.get("cpus"));
            if (jftfLogFile.getText().isEmpty()) {
                jftfLogFile.setText(savedExtraParam.get("logfile"));
            }
            if (jftfErrFile.getText().isEmpty()) {
                jftfErrFile.setText(savedExtraParam.get("errfile"));
            }
        }
        jbPrev.setEnabled(enablePrev);
        jbNext.setEnabled(enableNext);
        //jbRun.setEnabled(enableRun);
        //jbMonitor.setEnabled(enableMon);
        //jbStop.setEnabled(enableMon);
        syncRadioButtons(runType);
    }

    public boolean readyToRun() {
        if (jftfLogFile.getText().equalsIgnoreCase(jftfErrFile.getText())) {
            MessageBox.showErrorMsg(this, "Log and error files should not have the same name!");
            return false;
        }
        return true;
    }

    void syncRadioButtons(final RunManager.RunType runType) {
        switch (runType) {
            case SEQ:
                jrbSeq.setSelected(true);
                break;
            case PAR:
                jrbPar.setSelected(true);
                break;
            case GPU:
                jrbGPU.setSelected(true);
        }
    }

    public void setDefaultFieldValues() {
        jftfWT.setText(settingsWin.getWallTime());
        savedExtraParam.put("walltime", settingsWin.getWallTime());
        jftfNoN.setText(settingsWin.getNumberOfNodes());
        savedExtraParam.put("nodes", settingsWin.getNumberOfNodes());
        jftfCPN.setText(settingsWin.getCPUPerNode());
        savedExtraParam.put("cpus", settingsWin.getCPUPerNode());
        jftfLogFile.setText(settingsWin.getLogFile());
        savedExtraParam.put("logfile", settingsWin.getLogFile());
        jftfErrFile.setText(settingsWin.getErrorFile());
        savedExtraParam.put("errfile", settingsWin.getErrorFile());
    }

    void setupRemoteEnv() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (settingsWin == null) {
                    settingsWin = new SettingsDialog(mainFrame);
                }
                settingsWin.setLocationRelativeTo(mainFrame);
                settingsWin.setVisible(true);
            }
        });
    }

    /**
     * Locates the given component on the screen's center.
     */
    protected void locateOnScreen(Component component) {
        Dimension paneSize = component.getSize();
        Dimension screenSize = component.getToolkit().getScreenSize();
        component.setLocation(
                (screenSize.width - paneSize.width) / 2,
                (screenSize.height - paneSize.height) / 2);
    }

    private void configureUI() {
        Options.setDefaultIconSize(new Dimension(18, 18));
        Options.setUseNarrowButtons(settings.isUseNarrowButtons());

        // Global options
        Options.setTabIconsEnabled(settings.isTabIconsEnabled());
        UIManager.put(Options.POPUP_DROP_SHADOW_ENABLED_KEY,
                settings.isPopupDropShadowEnabled());

        // Swing Settings
        LookAndFeel selectedLaf = settings.getSelectedLookAndFeel();
        if (selectedLaf instanceof PlasticLookAndFeel) {
            PlasticLookAndFeel.setPlasticTheme(settings.getSelectedTheme());
            PlasticLookAndFeel.setTabStyle(settings.getPlasticTabStyle());
            PlasticLookAndFeel.setHighContrastFocusColorsEnabled(
                    settings.isPlasticHighContrastFocusEnabled());
        } else if (selectedLaf.getClass() == MetalLookAndFeel.class) {
            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
        }

        // Work around caching in MetalRadioButtonUI
        JRadioButton radio = new JRadioButton();
        radio.getUI().uninstallUI(radio);
        JCheckBox checkBox = new JCheckBox();
        checkBox.getUI().uninstallUI(checkBox);

        try {
            UIManager.setLookAndFeel(selectedLaf);
        } catch (Exception e) {
            System.out.println("Can't change L&F: " + e);
        }
    }

    protected MaskFormatter createFormatter(String s) {
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter(s);
        } catch (java.text.ParseException exc) {
            System.err.println("formatter is bad: " + exc.getMessage());
            return null;
        }
        return formatter;
    }

    void initComponents() {
        buttonGroup1 = new javax.swing.ButtonGroup();
        jtActions = new javax.swing.JToolBar();

        jtCertificate = createToolBarButton("lock.gif", "Certificate", "Get certificate");

        jbNew = createToolBarButton("add.gif", "New", "Create new job");
        jbNext = createToolBarButton("forward.gif", "Next", "Go to next job");
        jbPrev = createToolBarButton("back.gif", "Previous", "Back to previous job");

        //jbRun = createToolBarButton("run.gif", "Run", "Run workflow");
        //jbStop = createToolBarButton("stop.gif", "Stop", "Stop workflow");
        jbMonitor = createToolBarButton("console.gif", "Jobs", "Monitor all jobs");
        jbSettings = createToolBarButton("settings.gif", "Settings", "Show settings window");

        jftfWT = new javax.swing.JFormattedTextField(createFormatter("##:##:##"));
        jrbSeq = new javax.swing.JRadioButton();
        jrbPar = new javax.swing.JRadioButton();
        jrbGPU = new javax.swing.JRadioButton();
        jftfNoN = new javax.swing.JFormattedTextField();
        jftfCPN = new javax.swing.JFormattedTextField();

        jftfLogFile = new javax.swing.JTextField(8);
        jftfErrFile = new javax.swing.JTextField(8);

        jlblNoN = new javax.swing.JLabel("Number of nodes");
        jlblNoN.setEnabled(false);
        jlblCPN = new javax.swing.JLabel("CPUs per node");
        jlblCPN.setEnabled(false);

        jftfWT.setToolTipText("hh:mm:ss");
        jftfWT.setColumns(6);
        jftfWT.setHorizontalAlignment(JTextField.CENTER);

        buttonGroup1.add(jrbSeq);
        jrbSeq.setSelected(true);

        jrbSeq.setText("sequential");
        jrbSeq.setFocusable(false);
        jrbSeq.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        buttonGroup1.add(jrbPar);
        jrbPar.setText("parallel");
        jrbPar.setFocusable(false);
        jrbPar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        buttonGroup1.add(jrbGPU);
        jrbGPU.setText("gpu");
        jrbGPU.setFocusable(false);
        jrbGPU.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        jftfNoN.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jftfNoN.setEnabled(false);
        jftfNoN.setColumns(3);
        jftfNoN.setHorizontalAlignment(JTextField.TRAILING);

        jftfCPN.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jftfCPN.setEnabled(false);
        jftfCPN.setColumns(3);
        jftfCPN.setHorizontalAlignment(JTextField.TRAILING);

        buildGUI();
    }

    private void buildGUI() {
        setContentPane(buildContentPane());
        setTitle(getWindowTitle());
        setJMenuBar(buildMenuBar());
        setIconImage(readImageIcon("omnimatch.jpg").getImage());
    }

    private JComponent buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildToolBar(), BorderLayout.NORTH);
        panel.add(buildMainPanel(), BorderLayout.CENTER);
        return panel;
    }

    protected String getWindowTitle() {
        return "CellTom User Interface";
    }

    private JComponent buildToolBar() {
        jtActions.setFloatable(true);
        jtActions.putClientProperty("JToolBar.isRollover", Boolean.TRUE);

        jtActions.putClientProperty(
                Options.HEADER_STYLE_KEY,
                settings.getToolBarHeaderStyle());
        jtActions.putClientProperty(
                PlasticLookAndFeel.BORDER_STYLE_KEY,
                settings.getToolBarPlasticBorderStyle());
        jtActions.putClientProperty(
                WindowsLookAndFeel.BORDER_STYLE_KEY,
                settings.getToolBarWindowsBorderStyle());
        jtActions.putClientProperty(
                PlasticLookAndFeel.IS_3D_KEY,
                settings.getToolBar3DHint());

        jtActions.add(jtCertificate);
        jtActions.addSeparator();
        jtActions.add(jbNew);
        jtActions.add(jbMonitor);
        jtActions.addSeparator();

        jtActions.add(jbPrev);
        jtActions.add(jbNext);
        jtActions.addSeparator();

        jtActions.add(jbSettings);

        //jbStop.setEnabled(false);
        //jbMonitor.setEnabled(false);
        jbPrev.setEnabled(false);
        jbNext.setEnabled(false);

        return jtActions;
    }

    JMenuBar buildMenuBar() {
        jmbMenus = new javax.swing.JMenuBar();
        jmbMenus.putClientProperty(Options.HEADER_STYLE_KEY,
                settings.getMenuBarHeaderStyle());
        jmbMenus.putClientProperty(PlasticLookAndFeel.BORDER_STYLE_KEY,
                settings.getMenuBarPlasticBorderStyle());
        jmbMenus.putClientProperty(WindowsLookAndFeel.BORDER_STYLE_KEY,
                settings.getMenuBarWindowsBorderStyle());
        jmbMenus.putClientProperty(PlasticLookAndFeel.IS_3D_KEY,
                settings.getMenuBar3DHint());

        jmbMenus.add(buildHelpMenu());
        return jmbMenus;
    }

    private Component buildMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildControlPanel(), BorderLayout.NORTH);
        panel.add(buildTabbedPane(), BorderLayout.CENTER);
        return panel;
    }

    private Component buildControlPanel() {
        FormLayout layout = new FormLayout(
                "center:pref, $lcgap, pref, 15dlu, pref," +
                "$lcgap, pref, $lcgap, pref, 15dlu, pref, $lcgap," +
                "pref, 5dlu, pref, $lcgap, pref, 15dlu, pref, $lcgap," +
                "pref, 5dlu, pref, $lcgap, pref",
                "p");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();

        builder.addLabel("Wall time", cc.xy(1, 1));
        builder.add(jftfWT, cc.xy(3, 1, CellConstraints.LEFT, CellConstraints.BOTTOM));

        builder.add(jrbSeq, cc.xy(5, 1));
        builder.add(jrbPar, cc.xy(7, 1));
        builder.add(jrbGPU, cc.xy(9, 1));

        builder.add(jlblNoN, cc.xy(11, 1));
        builder.add(jftfNoN, cc.xy(13, 1));
        builder.add(jlblCPN, cc.xy(15, 1));
        builder.add(jftfCPN, cc.xy(17, 1, CellConstraints.RIGHT, CellConstraints.BOTTOM));

        builder.addLabel("Log file", cc.xy(19, 1));
        builder.add(jftfLogFile, cc.xy(21, 1, CellConstraints.LEFT, CellConstraints.BOTTOM));
        builder.addLabel("Error file", cc.xy(23, 1));
        builder.add(jftfErrFile, cc.xy(25, 1, CellConstraints.LEFT, CellConstraints.BOTTOM));

        JPanel panel = builder.getPanel();
        panel.setOpaque(false);

        return panel;
    }

    private Component buildTabbedPane() {
        jtpTabs = new JTabbedPane(SwingConstants.BOTTOM) {

            @Override
            public String getTitleAt(int index) {
                if (index == lastSelectedTab) {
                    jtpTabs.setFont(BOLD_FONT);
                    return TAB_TITLES[index];
                }
                jtpTabs.setFont(PLAIN_FONT);
                return TAB_TITLES[index];
            }
        };
        tabContainer = new SimpleInternalFrame("<html><b>Workflow</b></html>");
        preprocessingPane = new JPanel(); //TODO: write preprocessing panel when info available
        processingTab = new ProcessingTab(settings, this);
        processingTab.getProcessingPane().setRunType(runType);
        postprocessingPane = new JPanel(); //TODO: write postprocessing panel when info available
        jtpTabs.putClientProperty(Options.EMBEDDED_TABS_KEY, Boolean.TRUE);
        jtpTabs.addTab(TAB_TITLES[0], preprocessingPane);
        jtpTabs.addTab(TAB_TITLES[1], processingTab);
        jtpTabs.addTab(TAB_TITLES[2], postprocessingPane);
        jtpTabs.setSelectedComponent(processingTab);
        lastSelectedTab = 1;
        jtpTabs.addChangeListener(this);

        tabContainer.add(jtpTabs);
        return tabContainer;
    }

    protected static ImageIcon readImageIcon(String filename) {
        URL url = MainWindow.class.getResource("/resources/images/" + filename);
        return new ImageIcon(url);
    }

    protected JMenu createMenu(String text, char mnemonic) {
        JMenu menu = new JMenu(text);
        menu.setMnemonic(mnemonic);
        return menu;
    }

    protected JMenuItem createMenuItem(String text, Icon icon, char mnemonic) {
        JMenuItem menuItem = new JMenuItem(text, icon);
        menuItem.setMnemonic(mnemonic);
        return menuItem;
    }

    protected JMenuItem createMenuItem(String text, char mnemonic) {
        return new JMenuItem(text, mnemonic);
    }

    protected JButton createToolBarButton(String iconName, String text, String toolTipText) {
        JButton button = new JButton(text, readImageIcon(iconName));
        button.setToolTipText(toolTipText);
        button.setFocusable(false);
        button.setHorizontalTextPosition(javax.swing.SwingConstants.TRAILING);
        return button;
    }

    private JMenu buildHelpMenu() {
        jmHelp = createMenu("Help", 'H');

        jmiContent = createMenuItem("Help Contents", readImageIcon("help.gif"), 'H');
        jmHelp.add(jmiContent);

        jmHelp.addSeparator();

        jmiAbout = createMenuItem("About", 'a');
        jmHelp.add(jmiAbout);

        return jmHelp;
    }

//    void runWorkflow() {
//        if (jftfLogFile.getText().equalsIgnoreCase(jftfErrFile.getText())) {
//            MessageBox.showErrorMsg(this, "Log and error files should not have the same name!");
//            return;
//        }
//        processingTab.getProcessingPane().runWorkflow();
//    }

    void monitorWorkflow() {
        processingTab.getProcessingPane().monitorWorkflow(null, false);
    }

//    void stopWorkflow() {
//        processingTab.getProcessingPane().stopWorkflow();
//    }

    void showHelp() {
        if (helpViewer == null) {
            helpViewer = new HelpViewer();
        }
        if (!helpViewer.isVisible()) {
            if (aboutViewer != null) {
                aboutViewer.setVisible(false);
                aboutViewer.dispose();
            }
            helpViewer.setLocationRelativeTo(this);
            helpViewer.setVisible(true);
        }
    }

    void showAbout() {
        if (aboutViewer == null) {
            aboutViewer = new AboutViewer();
        }
        if (!aboutViewer.isVisible()) {
            if (helpViewer != null) {
                helpViewer.setVisible(false);
                helpViewer.dispose();
            }
            aboutViewer.setLocationRelativeTo(this);
            aboutViewer.setVisible(true);
        }
    }

    //Handle action events from all the buttons.
    public void actionPerformed(ActionEvent e) {
        Object emitter = e.getSource();
        if (emitter instanceof JButton) {
            if (emitter.equals(jbPrev)) {
                processingTab.getProcessingPane().decRunIndex();
                processingTab.getProcessingPane().showPreviousRun();
            } else if (emitter.equals(jbNext)) {
                processingTab.getProcessingPane().incRunIndex();
                processingTab.getProcessingPane().showNextRun();
//            } else if (emitter.equals(jbRun)) {
//                runWorkflow();
            } else if (emitter.equals(jbMonitor)) {
                monitorWorkflow();
//            } else if (emitter.equals(jbStop)) {
//                stopWorkflow();
            } else if (emitter.equals(jbNew)) {
                processingTab.getProcessingPane().incRunIndex();
                processingTab.getProcessingPane().showDefaultState();
            } else if (emitter.equals(jbSettings)) {
                if (settingsWin != null && !settingsWin.isVisible()) {
                    settingsWin.setLocationRelativeTo(mainFrame);
                    settingsWin.setVisible(true);
                }
            }
        } else if (emitter instanceof JFormattedTextField) {
            if (emitter.equals(jftfWT)) {
                savedExtraParam.put("walltime", jftfWT.getText());
            } else if (emitter.equals(jftfNoN)) {
                savedExtraParam.put("nodes", jftfNoN.getText());
            } else if (emitter.equals(jftfCPN)) {
                savedExtraParam.put("cpus", jftfCPN.getText());
            } else if (emitter.equals(jftfLogFile)) {
                savedExtraParam.put("logfile", jftfLogFile.getText());
            } else if (emitter.equals(jftfErrFile)) {
                savedExtraParam.put("errfile", jftfErrFile.getText());
            }
        } else if (emitter instanceof JMenuItem) {
            if (emitter.equals(jmiContent)) {
                showHelp();
            } else if (emitter.equals(jmiAbout)) {
                showAbout();
            }
        }
    }

    void enableParExec() {
        jlblNoN.setEnabled(true);
        jftfNoN.setEnabled(true);
        jlblCPN.setEnabled(true);
        jftfCPN.setEnabled(true);
    }

    void disableParExec() {
        jlblNoN.setEnabled(false);
        jftfNoN.setEnabled(false);
        jlblCPN.setEnabled(false);
        jftfCPN.setEnabled(false);
    }

    public void itemStateChanged(ItemEvent event) {
        Object source = event.getItem();
        if (source instanceof JRadioButton) {
            JRadioButton rb = (JRadioButton) source;
            if (event.getStateChange() == ItemEvent.SELECTED) {
                if (rb.equals(jrbPar)) {
                    enableParExec();
                    runType = RunManager.RunType.PAR;
                } else if (rb.equals(jrbSeq)) {
                    runType = RunManager.RunType.SEQ;
                } else if (rb.equals(jrbGPU)) {
                    enableParExec();
                    runType = RunManager.RunType.GPU;
                }
                processingTab.getProcessingPane().setRunType(runType);
            } else {
                if (rb.equals(jrbPar) || rb.equals(jrbGPU)) {
                    disableParExec();
                }
            }
        }
    }

    public void stateChanged(ChangeEvent event) {
        lastSelectedTab = jtpTabs.getSelectedIndex();
    }

    private static Settings createDefaultSettings() {
        Settings settings = Settings.createDefault();
        return settings;
    }

    public static void main(String[] args) {
        Settings settings = createDefaultSettings();
        String lafClassName = Options.PLASTICXP_NAME;
        settings.setSelectedLookAndFeel(lafClassName);

        BasicStroke stroke = new BasicStroke(3);
        mxConstants.VERTEX_SELECTION_COLOR = Color.magenta;
        mxConstants.VERTEX_SELECTION_STROKE = stroke;
        mxConstants.EDGE_SELECTION_COLOR = Color.magenta;
        mxConstants.EDGE_SELECTION_STROKE = stroke;

        MainWindow instance = null;
        if (args.length > 0) {
            instance = new MainWindow(settings, args[0]);
        } else {
            instance = new MainWindow(settings, null);
        }
        instance.pack();
        instance.locateOnScreen(instance);
    }
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jbNew;
    private javax.swing.JButton jbNext;
    private javax.swing.JButton jbPrev;
    private javax.swing.JButton jbMonitor;
    //private javax.swing.JButton jbRun;
    private javax.swing.JButton jbSettings;
    //private javax.swing.JButton jbStop;
    private javax.swing.JLabel jlblCPN;
    private javax.swing.JLabel jlblNoN;
    private javax.swing.JFormattedTextField jftfCPN;
    private javax.swing.JFormattedTextField jftfNoN;
    private javax.swing.JFormattedTextField jftfWT;
    private javax.swing.JMenu jmHelp;
    private javax.swing.JMenuBar jmbMenus;
    private javax.swing.JMenuItem jmiAbout;
    private javax.swing.JMenuItem jmiContent;
    private javax.swing.JRadioButton jrbGPU;
    private javax.swing.JRadioButton jrbPar;
    private javax.swing.JRadioButton jrbSeq;
    private javax.swing.JToolBar jtActions;
    private javax.swing.JButton jtCertificate;
    private javax.swing.JTabbedPane jtpTabs;
    private javax.swing.JComponent tabContainer;
    private javax.swing.JTextField jftfLogFile;
    private javax.swing.JTextField jftfErrFile;
}
