/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.biggrid.ct.ui;

/**
 *
 * @author Souley
 */
import com.jgoodies.looks.LookUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import nl.biggrid.ct.jgoodies.Factory;
import nl.biggrid.ct.run.*;

public class LogViewer extends javax.swing.JDialog {

    protected static final Dimension PREFERRED_SIZE =
            LookUtils.IS_LOW_RESOLUTION
            ? new Dimension(400, 300)
            : new Dimension(400, 300);
    String jobPath;
    boolean isNormalLog;
    RunManager runManager = RunManager.instance();

    public LogViewer(final String aJobPath, final int jobId, final boolean normalLog) {
        jobPath = aJobPath;
        isNormalLog = normalLog;
        String title = "";
        if (runManager.isJobMode()) {
            title += "Job " + jobId + (isNormalLog ? " Output" : " Errors");
        }
        setTitle(title);
        initComponents();
        buildGUI();
        setSize(PREFERRED_SIZE);
        showLog();
    }

    void showLog() {
        String outputFile = "";
        IRunner runner = runManager.getRunner();
        RemoteRunner jobRunner = null;
        if (runner instanceof RemotePBSJobRunner) {
            jobRunner = (RemotePBSJobRunner) runner;
        } else if (runner instanceof RemoteSGEJobRunner) {
            jobRunner = (RemoteSGEJobRunner) runner;
        } else if (runner instanceof RemoteLSFJobRunner) {
            jobRunner = (RemoteLSFJobRunner) runner;
        }
        if (jobRunner != null) {
            if (isNormalLog) {
                outputFile = jobPath + "/log/" + runManager.getLogFile();
            } else {
                outputFile = jobPath + "/log/" + runManager.getErrorFile();
            }
            jtxaView.setText(jobRunner.runCmd("cat " + outputFile, false, false));
        }
    }

    private void initComponents() {
        jtxaView = new javax.swing.JTextArea();
        jtxaView.setColumns(20);
        jtxaView.setRows(5);
    }

    private void buildGUI() {
        setContentPane(buildContentPane());
        setIconImage(readImageIcon("bulb.gif").getImage());
    }

    private JComponent buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(Factory.createStrippedScrollPane(jtxaView), BorderLayout.CENTER);
        return panel;
    }

    protected static ImageIcon readImageIcon(String filename) {
        URL url = LogViewer.class.getResource("/resources/images/" + filename);
        return new ImageIcon(url);
    }
    private javax.swing.JTextArea jtxaView;
}
