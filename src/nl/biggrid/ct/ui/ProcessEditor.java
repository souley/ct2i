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
import com.mxgraph.swing.mxGraphComponent;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class ProcessEditor extends javax.swing.JInternalFrame {

    ProcessEntity entity = null;
    mxGraphComponent graphComponent = null;
    Object cell = null;

    /** Creates new form ProcessEditor */
    public ProcessEditor(final mxGraphComponent graphComponent, final Object cell, final int x, final int y, ProcessEntity entity) {
        initComponents();
        initOtherComponents();
        this.entity = entity;
        this.graphComponent = graphComponent;
        this.cell = cell;

        setContentPane(buildContentPane());

        fillFields();
        setLocation(x, y);
        pack();
    }

    void closeFrame() {
        setVisible(false);
        dispose();
    }

    void initOtherComponents() {
        jbtnCancel.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeFrame();
            }
        });
    }

    void fillFields() {
        jtflExecutor.setText(entity.getCommand());
    }

    void setEntityFields() {
        entity.setCommand(jtflExecutor.getText());
    }

    void refreshGraphIfNeeded() {
        if (cell != null) {
            String label = graphComponent.getGraph().getLabel(cell);
            String oldVal = label.substring(label.indexOf("<i>") + 3, label.indexOf("</i>"));
            String newVal = jtflExecutor.getText();
            if (!oldVal.equalsIgnoreCase(newVal)) {
                String newLabel = "<html><center>" + entity.getId();
                newLabel += ("<br><small><i>" + newVal + "</i></small></center></html>");
                graphComponent.getGraph().cellLabelChanged(cell, newLabel, false);
            }
        }
    }

    private void jtflExecutorActionPerformed(java.awt.event.ActionEvent evt) {
        jbtnOKActionPerformed(evt);
    }

    private void jbtnOKActionPerformed(java.awt.event.ActionEvent evt) {
        setEntityFields();
        refreshGraphIfNeeded();
        closeFrame();
    }

    private void initComponents() {
        jtflExecutor = new javax.swing.JTextField(15);
        jbtnOK = new javax.swing.JButton("OK");
        jbtnCancel = new javax.swing.JButton("Cancel");

        setClosable(true);
        setTitle("Process");
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/binary.gif"))); // NOI18N

        jtflExecutor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtflExecutorActionPerformed(evt);
            }
        });

        jbtnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnOKActionPerformed(evt);
            }
        });
    }

    private JComponent buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildMainPanel(), BorderLayout.CENTER);
        return panel;
    }

    private Component buildMainPanel() {
        FormLayout layout = new FormLayout(
                "pref, $lcgap, pref, 15dlu, p",
                "p, 5dlu, p");
        PanelBuilder builder = new PanelBuilder(layout);

        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();

        builder.addSeparator(entity.getId(), cc.xyw(1, 1, 3));
        builder.add(jbtnOK, cc.xy(5, 1));
        builder.addLabel("Binary", cc.xy(1, 3));
        builder.add(jtflExecutor, cc.xy(3, 3));
        builder.add(jbtnCancel, cc.xy(5, 3));

        JPanel panel = builder.getPanel();
        panel.setOpaque(false);

        return panel;
    }

    private javax.swing.JButton jbtnCancel;
    private javax.swing.JButton jbtnOK;
    private javax.swing.JTextField jtflExecutor;
}
