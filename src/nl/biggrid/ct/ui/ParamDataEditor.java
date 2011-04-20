/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.biggrid.ct.ui;

/**
 *
 * @author Souley
 */
import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URL;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.FormLayout;

import com.mxgraph.swing.mxGraphComponent;

public class ParamDataEditor extends JInternalFrame
        implements ActionListener {

    enum AngleItem {

        START, INC, END
    };
    ParamDataEntity entity = null;
    mxGraphComponent graphComponent = null;
    Object cell = null;

    public ParamDataEditor(final mxGraphComponent aGraphComponent, final Object aCell, int x, int y, ParamDataEntity anEntity) {
        super();
        entity = anEntity;
        graphComponent = aGraphComponent;
        cell = aCell;
        buildGUI();
        fillFields();
        pack();
        setLocation(x, y);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        jbtnCancel.addActionListener(this);
        jbtnOK.addActionListener(this);
    }

    void closeFrame() {
        setVisible(false);
        dispose();
    }

    void fillFields() {
        jtflStart.setText(entity.getValues().elementAt(0));
        if (entity.getValues().size() >= 3) {
            jtflEnd.setText(entity.getValues().elementAt(1));
            jtflInc.setText(entity.getValues().elementAt(2));
        }
    }

    void setEntityFields() {
        entity.getValues().set(0, jtflStart.getText());
        if (entity.getValues().size() >= 3) {
            entity.getValues().set(1, jtflEnd.getText());
            entity.getValues().set(2, jtflInc.getText());
        }
    }

    String getNewLabel(AngleItem item, final String value) {
        String label = "<html><center>" + entity.getId();
        label += ("<br><small><i>s=" + ((item == AngleItem.START) ? value : entity.getValues().elementAt(0)) + "</i></small>");
        label += ("<br><small><i>e=" + ((item == AngleItem.END) ? value : entity.getValues().elementAt(1)) + "</i></small>");
        label += ("<br><small><i>I=" + ((item == AngleItem.INC) ? value : entity.getValues().elementAt(2)) + "</i></small></center></html>");
        return label;
    }

    void refreshGraphIfNeeded() {
        if (cell != null) {
            String label = graphComponent.getGraph().getLabel(cell);
            int istart = label.indexOf("<i>s=") + 5;
            String labelNoStart = label.substring(istart);
            String oldStart = label.substring(istart, istart + labelNoStart.indexOf("</i>"));
            String newStart = jtflStart.getText();
            if (!oldStart.equalsIgnoreCase(newStart)) {
                String newLabel = getNewLabel(AngleItem.START, newStart);
                graphComponent.getGraph().cellLabelChanged(cell, newLabel, false);
            }
            int iEnd = label.indexOf("<i>e=") + 5;
            String labelNoEnd = label.substring(iEnd);
            String oldEnd = label.substring(iEnd, iEnd + labelNoEnd.indexOf("</i>"));
            String newEnd = jtflEnd.getText();
            if (!oldEnd.equalsIgnoreCase(newEnd)) {
                String newLabel = getNewLabel(AngleItem.END, newEnd);
                graphComponent.getGraph().cellLabelChanged(cell, newLabel, false);
            }
            int iInc = label.indexOf("<i>s=") + 5;
            String labelNoInc = label.substring(iInc);
            String oldInc = label.substring(iInc, iInc + labelNoInc.indexOf("</i>"));
            String newInc = jtflInc.getText();
            if (!oldInc.equalsIgnoreCase(newInc)) {
                String newLabel = getNewLabel(AngleItem.INC, newInc);
                graphComponent.getGraph().cellLabelChanged(cell, newLabel, false);
            }
        }
    }

    private void buildGUI() {
        jtflStart = new javax.swing.JTextField(10);
        jtflEnd = new javax.swing.JTextField(10);
        jtflInc = new javax.swing.JTextField(10);
        jbtnOK = new javax.swing.JButton("OK");
        jbtnCancel = new javax.swing.JButton("Cancel");
        
        setContentPane(buildContentPane());
        setTitle("Parameter");
        setFrameIcon(readImageIcon("category.gif"));
        setClosable(true);
    }

    private JComponent buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildMainPanel(), BorderLayout.CENTER);
        return panel;
    }

    private Component buildMainPanel() {
        FormLayout layout = new FormLayout(
                "pref, $lcgap, pref, 15dlu, p",
                "p, 5dlu, p, 5dlu, p, 5dlu, p");
        PanelBuilder builder = new PanelBuilder(layout);

        builder.setDefaultDialogBorder();
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();

        builder.addSeparator(entity.getId(), cc.xyw(1, 1, 3));
        builder.addLabel("Start", cc.xy(1, 3));
        builder.add(jtflStart, cc.xy(3, 3));
        builder.addLabel("End", cc.xy(1, 5));
        builder.add(jtflEnd, cc.xy(3, 5));
        builder.addLabel("Increment", cc.xy(1, 7));
        builder.add(jtflInc, cc.xy(3, 7));

        builder.add(jbtnOK, cc.xy(5, 1));
        builder.add(jbtnCancel, cc.xy(5, 3));

        JPanel panel = builder.getPanel();
        panel.setOpaque(false);

        return panel;
    }

    protected static ImageIcon readImageIcon(String filename) {
        URL url = ParamDataEditor.class.getResource("/resources/images/" + filename);
        return new ImageIcon(url);
    }

    public void actionPerformed(ActionEvent event) {
        Object emitter = event.getSource();
        if (emitter instanceof JButton) {
            if (emitter.equals(jbtnCancel)) {
                closeFrame();
            } else if (emitter.equals(jbtnOK)) {
                setEntityFields();
                refreshGraphIfNeeded();
                closeFrame();
            }
        }
    }
    private javax.swing.JButton jbtnCancel;
    private javax.swing.JButton jbtnOK;
    private javax.swing.JTextField jtflEnd;
    private javax.swing.JTextField jtflInc;
    private javax.swing.JTextField jtflStart;
}
