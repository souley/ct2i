/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.ui;

/**
 *
 * @author Souley
 */
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import nl.biggrid.ct.run.JobInfo;

public class JobInfoDialog extends javax.swing.JDialog {

    public JobInfoDialog(final JobInfo jobInfo) {
        initComponents();
        buildGUI();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setFields(jobInfo);
        pack();
    }

    void setFields(final JobInfo jobInfo) {
        jtflId.setText(Integer.toString(jobInfo.getId()));
        jtflStatus.setText(jobInfo.getStatus());
        jtflWTU.setText(jobInfo.getWalltimeUsed());
        jtflMU.setText(jobInfo.getMemoryUsed());
        jtflEH.setText(jobInfo.getHostList());
        jtflQ.setText(jobInfo.getQueue());
    }

    protected static ImageIcon readImageIcon(String filename) {
        URL url = JobInfoDialog.class.getResource("/resources/images/" + filename);
        return new ImageIcon(url);
    }

    private void initComponents() {
        jtflId = new javax.swing.JTextField(20);
        jtflStatus = new javax.swing.JTextField(20);
        jtflWTU = new javax.swing.JTextField(20);
        jtflMU = new javax.swing.JTextField(20);
        jtflQ = new javax.swing.JTextField(20);
        jtflEH = new javax.swing.JTextField(20);

        setTitle("Job Status");
        setIconImage(readImageIcon("bulb.gif").getImage());
        setVisible(true);

        jtflId.setEditable(false);
        jtflId.setFont(new java.awt.Font("Tahoma", 1, 11));
        jtflId.setForeground(new java.awt.Color(0, 0, 255));

        jtflStatus.setEditable(false);
        jtflStatus.setFont(new java.awt.Font("Tahoma", 1, 11));
        jtflStatus.setForeground(new java.awt.Color(0, 0, 255));

        jtflWTU.setEditable(false);
        jtflWTU.setFont(new java.awt.Font("Tahoma", 1, 11));
        jtflWTU.setForeground(new java.awt.Color(0, 0, 255));

        jtflMU.setEditable(false);
        jtflMU.setFont(new java.awt.Font("Tahoma", 1, 11));
        jtflMU.setForeground(new java.awt.Color(0, 0, 255));

        jtflQ.setEditable(false);
        jtflQ.setFont(new java.awt.Font("Tahoma", 1, 11));
        jtflQ.setForeground(new java.awt.Color(0, 0, 255));

        jtflEH.setEditable(false);
        jtflEH.setFont(new java.awt.Font("Tahoma", 1, 11));
        jtflEH.setForeground(new java.awt.Color(0, 0, 255));
    }

    private void buildGUI() {
        setContentPane(buildContentPane());
    }

    private JComponent buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildMainPanel(), BorderLayout.CENTER);
        return panel;
    }

    private Component buildMainPanel() {
        FormLayout layout = new FormLayout(
                "pref, $lcgap, pref",
                "p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p");
        PanelBuilder builder = new PanelBuilder(layout);

        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();

        builder.addLabel("Id", cc.xy(1, 1));
        builder.add(jtflId, cc.xy(3, 1));
        builder.addLabel("Status", cc.xy(1, 3));
        builder.add(jtflStatus, cc.xy(3, 3));
        builder.addLabel("Wall time", cc.xy(1, 5));
        builder.add(jtflWTU, cc.xy(3, 5));

        builder.addLabel("Memory", cc.xy(1, 7));
        builder.add(jtflMU, cc.xy(3, 7));
        builder.addLabel("Hosts", cc.xy(1, 9));
        builder.add(jtflEH, cc.xy(3, 9));
        builder.addLabel("Queue", cc.xy(1, 11));
        builder.add(jtflQ, cc.xy(3, 11));

        JPanel panel = builder.getPanel();
        panel.setOpaque(false);

        return panel;
    }

    private javax.swing.JTextField jtflEH;
    private javax.swing.JTextField jtflId;
    private javax.swing.JTextField jtflMU;
    private javax.swing.JTextField jtflQ;
    private javax.swing.JTextField jtflStatus;
    private javax.swing.JTextField jtflWTU;
}
