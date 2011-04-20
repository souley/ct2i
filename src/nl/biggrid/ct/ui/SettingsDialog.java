/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.biggrid.ct.ui;

/**
 *
 * @author Souley
 */
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.Formatter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashMap;
import nl.biggrid.ct.run.*;
import nl.biggrid.ct.util.MessageBox;

public class SettingsDialog extends JDialog
        implements ActionListener, ItemListener {

    final String CONFIG_DIR = File.separator + "config" + File.separator;
    final String CONFIG_FILE = "defaultsettings.xml";
    final String TWO_DIGIT_FORMAT = "%02d";
    public final String WMS_PBS = "PBS";
    public final String WMS_SGE = "SGE";
    public final String WMS_LSF = "LSF";
    final String[] WMS = new String[]{WMS_PBS, WMS_SGE, WMS_LSF};
    final String CURRENT_DIR = System.getProperty("user.dir");
    final String USER_CT2I = CURRENT_DIR + File.separator + ".ct2i";
    final String DEFAULT_BUFFER = USER_CT2I + File.separator + "data";
    final String DEFAULT_OUTBUFFER = DEFAULT_BUFFER + File.separator + "outbound";
    final String DEFAULT_INBUFFER = DEFAULT_BUFFER + File.separator + "inbound";
    final String SCRIPT_PATH = CURRENT_DIR + CONFIG_DIR + "scripts" + File.separator;
    final String BIN_PATH = CURRENT_DIR + CONFIG_DIR + "bin";

    private boolean checkingPassed;
    private ConfigChecker checker = null;
    private String runSubDir = "";
    private String wallTime = "";
    private String numberOfNodes = "";
    private String cpuPerNode = "";
    private MainWindow appFrame = null;
    private String logFile = "";
    private String errFile = "";
    private ConfigManager configManager = ConfigManager.instance();

    public SettingsDialog(final MainWindow parent) {
        super(parent, true);
        appFrame = parent;
        buildGUI();
        pack();
        jpflPwd.requestFocusInWindow();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(appFrame);
        jbOk.addActionListener(this);
        jbCancel.addActionListener(this);
        jpflPwd.addActionListener(this);
        checkingPassed = false;
        //ct2iKnown = (new File(USER_CT2I)).isDirectory();
        initSettings();
    }

    public boolean isCheckingPassed() {
        return checkingPassed;
    }

    public String getRemoteHost() {
        return jtflHost.getText();
    }

    public String getRemoteLogin() {
        return jtflLogin.getText();
    }

    public String getRemotePass() {
        return new String(jpflPwd.getPassword());
    }

    public String getToolPath() {
        return jtflTool.getText();
    }

    public String getInputPath() {
        return jtflIndir.getText();
    }

    public String getOutputPath() {
        return jtflOutdir.getText();
    }

    public String getOutBufferPath() {
        return jtflOutBuffer.getText();
    }

    public String getInBufferPath() {
        return jtflInBuffer.getText();
    }

    public String getWallTime() {
        return wallTime;
    }

    public void setWallTime(final String aWallTime) {
        wallTime = aWallTime;
    }

    public String getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(final String aNumberOfNodes) {
        numberOfNodes = aNumberOfNodes;
    }

    public String getCPUPerNode() {
        return cpuPerNode;
    }

    public void setCPUPerNode(final String aCPUPerNode) {
        cpuPerNode = aCPUPerNode;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(final String aFileName) {
        logFile = aFileName;
    }

    public String getErrorFile() {
        return errFile;
    }

    public void setErrorFile(final String aFileName) {
        errFile = aFileName;
    }

    public String getJobPath() {
        String jobPath = jtflOmniHome.getText();
        String selectedWMS = (String) jcbWMSChoices.getSelectedItem();
        if (selectedWMS.equalsIgnoreCase(WMS_PBS)) {
            jobPath += "/" + RunManager.PBS_JOB_SCRIPT;
        } else if (selectedWMS.equalsIgnoreCase(WMS_SGE)) {
            jobPath += "/" + RunManager.SGE_JOB_SCRIPT;
        } else {
            jobPath += "/" + RunManager.LSF_JOB_SCRIPT;
        }
        return jobPath;
    }

    void closeWindow() {
        setVisible(false);
        dispose();
    }

    void createDirectory(final String dp) {
        if (checker != null && !checker.createDirectory(dp).isEmpty()) {
            MessageBox.showErrorMsg(appFrame, "Cannot create directory '" + dp + "'");
        }
    }

    boolean moveFile(final String from, final String to) {
        return checker.moveFileTo(from, to);
    }

    boolean moveBinariesTo(final String dp) {
        File[] files = new File(BIN_PATH).listFiles();
        String[] filePaths = new String[files.length];
        for (int i=0; i<files.length; i++) {
            filePaths[i] = files[i].getAbsolutePath();
        }
        return checker.moveFilesTo(filePaths, dp);
    }

    String checkBufferPath(final String bp) {
        if (!(new File(bp)).isDirectory()) {
            return "Cannot find directory '" + bp + "'";
        }
        return "";
    }

    String createLocalDirectory(final String bp) {
        if (!(new File(bp)).mkdirs()) {
            return "Cannot create directory '" + bp + "'";
        }
        return "";
    }

    boolean checkSettings() {
        String host = jtflHost.getText();
        String login = jtflLogin.getText();
        String pwd = new String(jpflPwd.getPassword());
        String toolPath = jtflTool.getText();
        String inputPath = jtflIndir.getText();
        String outputPath = jtflOutdir.getText();
        String outBufferPath = jtflOutBuffer.getText();
        String inBufferPath = jtflInBuffer.getText();
        //String jobPath = jtflOmniHome.getText();
        //String jobPath = "";
        //String remoteJobFile = "";
        String jobPath = getJobPath();
//        String selectedWMS = (String) jcbWMSChoices.getSelectedItem();
//        String defaultJobFile = SCRIPT_PATH;
//        if (selectedWMS.equalsIgnoreCase(WMS_PBS)) {
//            jobPath += "/" + RunManager.PBS_JOB_SCRIPT;
//            defaultJobFile += RunManager.PBS_JOB_SCRIPT;
//        } else if (selectedWMS.equalsIgnoreCase(WMS_SGE)) {
//            jobPath += "/" + RunManager.SGE_JOB_SCRIPT;
//            defaultJobFile += RunManager.SGE_JOB_SCRIPT;
//        } else {
//            jobPath += "/" + RunManager.LSF_JOB_SCRIPT;
//            defaultJobFile += RunManager.LSF_JOB_SCRIPT;
//        }

        
        checker = new ConfigChecker(host, login, pwd, toolPath, inputPath, outputPath, jobPath, outBufferPath, inBufferPath);
        String checkMsg = checker.checkToolPath();
        if (!checkMsg.isEmpty()) {
            String createMsg = checker.createDirectory(toolPath);
            if (!createMsg.isEmpty()) {
                MessageBox.showErrorMsg(appFrame, checkMsg+createMsg);
                return false;
            }
            moveBinariesTo(toolPath);
        }
        checkMsg = checker.checkInputPath();
        if (!checkMsg.isEmpty()) {
            String createMsg = checker.createDirectory(inputPath);
            if (!createMsg.isEmpty()) {
                MessageBox.showErrorMsg(appFrame, checkMsg+createMsg);
                return false;
            }
        }
        checkMsg = checker.checkOutputPath();
        if (!checkMsg.isEmpty()) {
            String createMsg = checker.createDirectory(outputPath);
            if (!createMsg.isEmpty()) {
                MessageBox.showErrorMsg(appFrame, checkMsg+createMsg);
                return false;
            }
        }        
        checkMsg = checker.checkJobScript(jobPath);
        if (!checkMsg.isEmpty()) {
            String defaultJobFile = SCRIPT_PATH + jobPath.substring(jobPath.lastIndexOf("/")+1);
            if (!moveFile(defaultJobFile, jobPath)) {
                MessageBox.showErrorMsg(appFrame, checkMsg);
                return false;
            }
        }
        checkMsg = checkBufferPath(outBufferPath);
        if (!checkMsg.isEmpty()) {
            String createMsg = createLocalDirectory(outBufferPath);
            if (!createMsg.isEmpty()) {
                MessageBox.showErrorMsg(appFrame, checkMsg+" <br> "+createMsg);
                return false;
            }
        }
        checkMsg = checkBufferPath(inBufferPath);
        if (!checkMsg.isEmpty()) {
            String createMsg = createLocalDirectory(inBufferPath);
            if (!createMsg.isEmpty()) {
                MessageBox.showErrorMsg(appFrame, checkMsg+" <br> "+createMsg);
                return false;
            }
        }
        // check all OmniMatch file args
        checkMsg = checker.checkFileArgs();
        if (!checkMsg.isEmpty()) {
            MessageBox.showErrorMsg(appFrame, checkMsg);
            return false;
         }

        checkingPassed = true;
        return true;
    }

    void initSettings() {
        jtflHost.setText(configManager.getString("cluster.hostname"));
        jtflLogin.setText(configManager.getString("cluster.user"));
        jtflTool.setText(configManager.getString("cluster.binarypath"));
        jtflIndir.setText(configManager.getString("cluster.inputpath"));
        jtflOutdir.setText(configManager.getString("cluster.outputpath"));
        jtflOutBuffer.setText(configManager.getString("application.outbuffer"));
        if (jtflOutBuffer.getText().isEmpty()) {
            jtflOutBuffer.setText(DEFAULT_OUTBUFFER);
        }
        jtflInBuffer.setText(configManager.getString("application.inbuffer"));
        if (jtflInBuffer.getText().isEmpty()) {
            jtflInBuffer.setText(DEFAULT_INBUFFER);
        }
        jtflOmniHome.setText("/home/"+jtflLogin.getText()+"/omnimatch");
        wallTime = configManager.getString("cluster.walltime");
        numberOfNodes = configManager.getString("cluster.nodes");
        cpuPerNode = configManager.getString("cluster.gpusorcpus");
        logFile = configManager.getString("cluster.logfile");
        errFile = configManager.getString("cluster.errorfile");
    }

    public void saveSettings() {
        configManager.setProperty("cluster.hostname", jtflHost.getText());
        configManager.setProperty("cluster.user", jtflLogin.getText());
        configManager.setProperty("cluster.binarypath", jtflTool.getText());
        configManager.setProperty("cluster.inputpath", jtflIndir.getText());
        configManager.setProperty("cluster.outputpath", jtflOutdir.getText());
        configManager.setProperty("application.outbuffer", jtflOutBuffer.getText());
        configManager.setProperty("application.inbuffer", jtflInBuffer.getText());
        configManager.setProperty("cluster.walltime", wallTime);
        configManager.setProperty("cluster.nodes", numberOfNodes);
        configManager.setProperty("cluster.gpusorcpus", cpuPerNode);
        configManager.setProperty("cluster.logfile", logFile);
        configManager.setProperty("cluster.errorfile", errFile);
    }

    String format(final int number) {
        StringBuilder formattedNumber = new StringBuilder();
        Formatter formatter = new Formatter(formattedNumber);
        formatter.format(TWO_DIGIT_FORMAT, number);
        return formattedNumber.toString();
    }

    public String buildRunSubDir(final String outname) {
        SimpleTimeZone tz = new SimpleTimeZone(3600000,
                "Europe/Amsterdam",
                Calendar.MARCH, -1, Calendar.SUNDAY,
                3600000, SimpleTimeZone.UTC_TIME,
                Calendar.OCTOBER, -1, Calendar.SUNDAY,
                3600000, SimpleTimeZone.UTC_TIME,
                3600000);

        Calendar calendar = new GregorianCalendar(tz);
        runSubDir = Integer.toString(calendar.get(Calendar.YEAR));
        runSubDir += format(calendar.get(Calendar.MONTH) + 1);
        runSubDir += format(calendar.get(Calendar.DAY_OF_MONTH));
        runSubDir += "_" + outname + "_";
        runSubDir += format(calendar.get(Calendar.HOUR_OF_DAY));
        runSubDir += format(calendar.get(Calendar.MINUTE));
        runSubDir += format(calendar.get(Calendar.SECOND));

        return runSubDir;
    }

    public void createRunSubDir() {
        createDirectory(jtflOutdir.getText() + "/" + runSubDir);
    }

    void goAhead() {
        if (checkSettings()) {
            RunManager.instance().invalidateRunner();
            closeWindow();
            appFrame.initWindow();
        }
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source.equals(jbCancel)) {
            closeWindow();
            appFrame.shutdown();
        } else if (source.equals(jbOk) || source.equals(jpflPwd)) {
            goAhead();
        }
    }

    public void itemStateChanged(ItemEvent e) {
//        if (jhbUseDefaults.equals(e.getItem())) {
//            if (jhbUseDefaults.isSelected()) {
//                String duuh = "/home/" + jtflLogin.getText() + "/omnimatch/";
//                jtflTool.setText(duuh + "bin");
//                jtflIndir.setText(duuh + "input");
//                jtflOutdir.setText(duuh + "output");
//                jtflOutBuffer.setText(DEFAULT_OUTBUFFER);
//                jtflInBuffer.setText(DEFAULT_INBUFFER);
//                jtflTool.setEnabled(false);
//                jtflIndir.setEnabled(false);
//                jtflOutdir.setEnabled(false);
//                jtflOutBuffer.setEnabled(false);
//                jtflInBuffer.setEnabled(false);
//            } else {
//                jtflTool.setText(configManager.getString("cluster.binarypath"));
//                jtflIndir.setText(configManager.getString("cluster.inputpath"));
//                jtflOutdir.setText(configManager.getString("cluster.outputpath"));
//                jtflOutBuffer.setText(configManager.getString("application.outbuffer"));
//                jtflInBuffer.setText(configManager.getString("application.inbuffer"));
//                jtflTool.setEnabled(true);
//                jtflIndir.setEnabled(true);
//                jtflOutdir.setEnabled(true);
//                jtflOutBuffer.setEnabled(true);
//                jtflInBuffer.setEnabled(true);
//            }
//        }
    }

    private void buildGUI() {
        jtflHost = new javax.swing.JTextField(15);
        jtflLogin = new javax.swing.JTextField(15);
        jpflPwd = new javax.swing.JPasswordField(15);
        jtflTool = new javax.swing.JTextField(50);
        jtflIndir = new javax.swing.JTextField(50);
        jtflOutdir = new javax.swing.JTextField(50);
        jtflOutBuffer = new javax.swing.JTextField(50);
        jtflInBuffer = new javax.swing.JTextField(50);
        jbOk = new javax.swing.JButton("OK");
        jbCancel = new javax.swing.JButton("Cancel");
        jcbWMSChoices = new javax.swing.JComboBox(WMS);
        //jhbUseDefaults = new javax.swing.JCheckBox("Use Defaults", false);
        //jhbUseDefaults.addItemListener(this);
        jtflOmniHome = new javax.swing.JTextField();

        setContentPane(buildContentPane());
        setTitle("");
        setIconImage(readImageIcon("settings.gif").getImage());
    }

    private JComponent buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildMainPanel(), BorderLayout.CENTER);
        panel.add(buildButtonsPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private Component buildMainPanel() {
        FormLayout layout = new FormLayout(
                "fill:60dlu:grow, 5dlu, pref, 10dlu, pref," +
                "$lcgap, pref, $lcgap, pref",
                "p, 5dlu, p, 5dlu, p, 10dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 10dlu, p, 5dlu, p, 5dlu, p");
        PanelBuilder builder = new PanelBuilder(layout);

        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();

        builder.addSeparator("Remote Host and Credentials", cc.xyw(1, 1, 7));
        builder.addLabel("Hostname", cc.xy(1, 3));
        builder.add(jtflHost, cc.xy(3, 3));
        builder.addLabel("Login", cc.xy(1, 5));
        builder.add(jtflLogin, cc.xy(3, 5));
        builder.addLabel("Password", cc.xy(5, 5));
        builder.add(jpflPwd, cc.xy(7, 5));

        builder.addSeparator("Remote Execution Environment", cc.xyw(1, 7, 7));
        builder.addLabel("WMS", cc.xy(1, 9));
        builder.add(jcbWMSChoices, cc.xy(3, 9));
        //builder.add(jhbUseDefaults, cc.xy(7, 9));
        builder.addLabel("Install Path", cc.xy(5, 9));
        builder.add(jtflOmniHome, cc.xy(7, 9));
        builder.addLabel("Binary Directory", cc.xy(1, 11));
        builder.add(jtflTool, cc.xyw(3, 11, 5));
        builder.addLabel("Input Directory", cc.xy(1, 13));
        builder.add(jtflIndir, cc.xyw(3, 13, 5));
        builder.addLabel("Output Directory", cc.xy(1, 15));
        builder.add(jtflOutdir, cc.xyw(3, 15, 5));

        builder.addSeparator("Local Settings", cc.xyw(1, 17, 7));
        builder.addLabel("Outgoing buffer", cc.xy(1, 19));
        builder.add(jtflOutBuffer, cc.xyw(3, 19, 5));
        builder.addLabel("Incoming buffer", cc.xy(1, 21));
        builder.add(jtflInBuffer, cc.xyw(3, 21, 5));

        JPanel panel = builder.getPanel();
        panel.setOpaque(false);

        return panel;
    }

    private Component buildButtonsPanel() {
        ButtonBarBuilder2 builder = new ButtonBarBuilder2();
        builder.setOpaque(false);

        builder.addGlue();
        builder.addButton(jbOk);
        builder.addRelatedGap();
        builder.addButton(jbCancel);
        builder.addGlue();

        JPanel panel = builder.getPanel();
        panel.setOpaque(false);

        return panel;
    }

    protected static ImageIcon readImageIcon(String filename) {
        URL url = SettingsDialog.class.getResource("/resources/images/" + filename);
        return new ImageIcon(url);
    }

    public String checkFileArgs(final HashMap<String, TomoEntity> entityMap) {
        return checker.checkFileArgs(entityMap);
    }
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbOk;
    private javax.swing.JPasswordField jpflPwd;
    private javax.swing.JTextField jtflHost;
    private javax.swing.JTextField jtflIndir;
    private javax.swing.JTextField jtflOutBuffer;
    private javax.swing.JTextField jtflInBuffer;
    private javax.swing.JTextField jtflLogin;
    private javax.swing.JTextField jtflOutdir;
    private javax.swing.JTextField jtflTool;
    private javax.swing.JComboBox jcbWMSChoices;
    private javax.swing.JCheckBox jhbUseDefaults;
    private javax.swing.JTextField jtflOmniHome;
}
