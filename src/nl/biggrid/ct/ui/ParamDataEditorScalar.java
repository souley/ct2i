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
import javax.swing.JComponent;
import javax.swing.JPanel;
import nl.biggrid.ct.graph.CustomGraphComponent;

public class ParamDataEditorScalar extends javax.swing.JInternalFrame {

    private ParamDataEntity entity;
    CustomGraphComponent graphComponent = null;
    Object cell = null;

    /** Creates new form ParamDataEditorScalar */
    public ParamDataEditorScalar(final CustomGraphComponent graphComponent, final Object cell, int x, int y, ParamDataEntity entity) {
        initComponents();
        initOtherComponents();
        this.entity = entity;
        this.graphComponent = graphComponent;
        this.cell = cell;

        setContentPane(buildContentPane());

        fillFields();
        setLocation(x, y - getHeight() / 2);
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
        jtflValue.setText(entity.getValues().elementAt(0));
    }

    void setEntityFields() {
        if (!entity.getId().equalsIgnoreCase(jtflValue.getText())) {
            entity.getValues().set(0, jtflValue.getText());
            changeOuputParams();
        }
    }

    void changeOuputParams() {
        if ("output".equalsIgnoreCase(entity.getId())) {
            TomoEntity tentity = graphComponent.getEntityMap().get("ccf");
            if ((tentity != null) && (tentity instanceof ParamFileEntity)) {
                ParamFileEntity ccfEntity = (ParamFileEntity) tentity;
                ccfEntity.setURL(jtflValue.getText() + ".ccf");
            }
            tentity = graphComponent.getEntityMap().get("angles");
            if ((tentity != null) && (tentity instanceof ParamFileEntity)) {
                ParamFileEntity ccfEntity = (ParamFileEntity) tentity;
                ccfEntity.setURL(jtflValue.getText() + ".ang");
            }
        }
    }

    void updateOutputLabels() {
        Object[] cells = graphComponent.getCells(graphComponent.getGraph().getGraphBounds().getRectangle());
        for (int i = 0; i < cells.length; i++) {
            String label = graphComponent.getGraph().getLabel(cells[i]);
            if (!label.isEmpty()) {
                boolean isCCF = (label.indexOf("ccf") != -1);
                boolean isAngles = (label.indexOf("angles") != -1);
                if (isCCF || isAngles) {
                    String newLabel = label.replaceFirst("<i>.*</i>", "<i>" + jtflValue.getText() + "." + (isCCF ? "ccf" : "ang") + "</i>");
                    graphComponent.getGraph().cellLabelChanged(cells[i], newLabel, false);
                }
            }
        }
    }

    void refreshGraphIfNeeded() {
        if (cell != null) {
            String label = graphComponent.getGraph().getLabel(cell);
            String oldVal = label.substring(label.indexOf("<i>") + 3, label.indexOf("</i>"));
            String newVal = jtflValue.getText();
            if (!oldVal.equalsIgnoreCase(newVal)) {
                String newLabel = "<html><center>" + entity.getId();
                newLabel += ("<br><small><i>" + newVal + "</i></small></center></html>");
                graphComponent.getGraph().cellLabelChanged(cell, newLabel, false);
                if ("output".equalsIgnoreCase(entity.getId())) {
                    updateOutputLabels();
                }
            }
        }
    }

    private void jtflValueActionPerformed(java.awt.event.ActionEvent evt) {
        jbtnOKActionPerformed(evt);
    }

    private void jbtnOKActionPerformed(java.awt.event.ActionEvent evt) {
        setEntityFields();
        refreshGraphIfNeeded();
        closeFrame();
    }

    void initComponents() {
        jtflValue = new javax.swing.JTextField(15);
        jbtnOK = new javax.swing.JButton("OK");
        jbtnCancel = new javax.swing.JButton("Cancel");

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setTitle("Parameter");
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/category.gif")));

        jtflValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtflValueActionPerformed(evt);
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
        builder.addLabel("Value", cc.xy(1, 3));
        builder.add(jtflValue, cc.xy(3, 3));
        builder.add(jbtnCancel, cc.xy(5, 3));

        JPanel panel = builder.getPanel();
        panel.setOpaque(false);

        return panel;
    }

    private javax.swing.JButton jbtnCancel;
    private javax.swing.JButton jbtnOK;
    private javax.swing.JTextField jtflValue;

}
