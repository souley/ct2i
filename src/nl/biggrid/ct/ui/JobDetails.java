/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.biggrid.ct.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import nl.biggrid.ct.jgoodies.Factory;
import nl.biggrid.ct.run.JobInfo;
import nl.biggrid.ct.util.JobComparator;

/**
 *
 * @author Souley
 */
public class JobDetails extends javax.swing.JDialog {
    protected static final Dimension PREFERRED_SIZE = new Dimension(465, 200);
    private static final String[] COLUMN_NAMES = new String[]{"Id", "Status", "Wall time", "Memory", "Hosts", "Queue"};

    class JobTableModel extends AbstractTableModel {

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        public int getRowCount() {
            return jobInfos.length;
        }

        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public Class getColumnClass(int column) {
            return String.class;
        }

        public String getValueAt(int row, int col) {
            if (row < jobInfos.length) {
                JobInfo jobInfo = jobInfos[row];
                switch (col) {
                    case 0:
                        return Integer.valueOf(jobInfo.getId()).toString();
                    case 1:
                        return jobInfo.getStatus();
                    case 2:
                        return jobInfo.getWalltimeUsed();
                    case 3:
                        return jobInfo.getMemoryUsed();
                    case 4:
                        return jobInfo.getHostList();
                    case 5:
                        return jobInfo.getQueue();
                }
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    class TextCellRenderer extends JLabel
            implements TableCellRenderer {

        public TextCellRenderer() {
            setHorizontalTextPosition(SwingConstants.CENTER);
            setHorizontalAlignment(SwingConstants.CENTER);
            //setForeground(new Color(153, 0, 0));
        }

        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isCellSelected,
                boolean cellHasFocus,
                int row,
                int col) {
            setText(value.toString());
            return this;
        }
    }

    class JobRowSorter extends TableRowSorter<JobTableModel> {

        public JobRowSorter(final JobTableModel jobTableModel) {
            super(jobTableModel);
            setComparator(1, new JobComparator());
        }
    }

    private JTable jobList = null;
    private JobInfo[] jobInfos = null;

    public JobDetails(final MainWindow appWin, final JobInfo[] someJobInfos, final String prefixTitle) {
        super(appWin, true);
        jobInfos = new JobInfo[someJobInfos.length];
        for (int i=0; i<someJobInfos.length; i++) {
            jobInfos[i] = someJobInfos[i];
        }
        initComponents();
        buildGUI(prefixTitle);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(PREFERRED_SIZE);
        pack();
        setLocationRelativeTo(appWin);
    }

    private void initComponents() {
        jobList = new JTable(new JobTableModel()) {

            @Override
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {

                    @Override
                    public Font getFont() {
                        return new Font(Font.MONOSPACED, Font.BOLD, 11);
                    }
                };
            }
        };
        //jobList.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i=0; i<jobList.getColumnCount(); i++) {
            jobList.getColumnModel().getColumn(i).setCellRenderer(new TextCellRenderer());
        }
        jobList.setRowSorter(new JobRowSorter(new JobTableModel())); // test
    }

    private void buildGUI(final String prefixTitle) {
        setContentPane(buildContentPane());
        setTitle(prefixTitle + " Details");
        setIconImage(readImageIcon("bulb.gif").getImage());
    }

    private JComponent buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(Factory.createStrippedScrollPane(jobList), BorderLayout.CENTER);
        return panel;
    }

    protected static ImageIcon readImageIcon(String filename) {
        URL url = JobDetails.class.getResource("/resources/images/" + filename);
        return new ImageIcon(url);
    }
}
