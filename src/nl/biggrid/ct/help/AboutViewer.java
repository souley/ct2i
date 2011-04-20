/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.help;

/**
 *
 * @author Souley
 */
import com.jgoodies.looks.LookUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.URL;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import nl.biggrid.ct.jgoodies.Factory;
import nl.biggrid.ct.ui.LogViewer;

public class AboutViewer extends javax.swing.JDialog {
    protected static final Dimension PREFERRED_SIZE =
            LookUtils.IS_LOW_RESOLUTION
            ? new Dimension(524, 350)
            : new Dimension(524, 350);

    public AboutViewer() {
        initComponents();
        buildGUI();
        setTitle("About CT2I");
        setAlwaysOnTop(true);
        setSize(PREFERRED_SIZE);
        initContent();
    }

    void initContent() {
        URL aboutURL = AboutViewer.class.getResource("about.html");
        if (aboutURL != null) {
            try {
                jepContent.setPage(aboutURL);
            } catch (IOException e) {
                System.err.println("Attempted to read a bad URL: " + aboutURL);
            }
        } else {
            System.err.println("Couldn't find file: about.html");
        }

    }

    private void initComponents() {
        jepContent = new javax.swing.JEditorPane();
        jepContent.setBorder(null);
        jepContent.setEditable(false);
    }

    private void buildGUI() {
        setContentPane(buildContentPane());
        setIconImage(readImageIcon("omnimatch.jpg").getImage());
    }

    private JComponent buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(Factory.createStrippedScrollPane(jepContent), BorderLayout.CENTER);
        return panel;
    }

    protected static ImageIcon readImageIcon(String filename) {
        URL url = LogViewer.class.getResource("/resources/images/" + filename);
        return new ImageIcon(url);
    }

    private javax.swing.JEditorPane jepContent;
}
