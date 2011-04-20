/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import nl.biggrid.ct.jgoodies.Factory;
import nl.biggrid.ct.jgoodies.Settings;
import nl.biggrid.ct.jgoodies.UIFSplitPane;

/**
 *
 * @author Souley
 */
public class ProcessingTab extends JPanel {
    protected static final Dimension PANEL_SIZE = new Dimension(900, 500);
    protected static final Dimension OMNIMATCH_SIZE = new Dimension(760, 500);
    protected static final Dimension STATUS_SIZE = new Dimension(130, 500);

    private ProcessingPane processingPane = null;
    private JobStatusTable statusTable = null;

    public ProcessingTab(final Settings settings, final MainWindow appWin) {
        statusTable = new JobStatusTable(this);
        processingPane = new ProcessingPane(settings, appWin, statusTable);
        setLayout(new BorderLayout());
        add(buildGUI(), BorderLayout.CENTER);
        setPreferredSize(PANEL_SIZE);
    }

    public ProcessingPane getProcessingPane() {
        return processingPane;
    }

    private JComponent buildGUI() {
        JComponent left = Factory.createStrippedScrollPane(processingPane);
        left.setPreferredSize(OMNIMATCH_SIZE);

        JComponent right = Factory.createStrippedScrollPane(statusTable);
        right.setPreferredSize(STATUS_SIZE);

        JSplitPane jspContent = UIFSplitPane.createStrippedSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            left,
            right);
        jspContent.setOpaque(false);
        jspContent.setDividerLocation(OMNIMATCH_SIZE.width);
        jspContent.setResizeWeight(1);

        return jspContent;
    }

}
