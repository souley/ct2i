/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.biggrid.ct.ui;

import com.jgoodies.looks.LookUtils;
import java.awt.Color;
import java.awt.Component;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
//import javax.swing.SwingConstants;
//import javax.swing.UIManager;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import nl.biggrid.ct.run.FileTransferManager;
import nl.biggrid.ct.run.JobInfo;
import nl.biggrid.ct.run.RunInfo;
import nl.biggrid.ct.run.RunManager;
import nl.biggrid.ct.run.RunUtil;
import nl.biggrid.ct.util.JobComparator;

/**
 *
 * @author Souley
 */
public class JobStatusTable extends JTable {

    static final int STATUS_COLUMN = 1;
    static final int STATUS_COLUMN_WIDTH = 80;
    static final int ID_COLUMN_WIDTH = 50;
    private static final int REFRESH_TIME = 5000;
    private static final String[] COLUMN_NAMES = new String[]{"Job", "Status"};
    private static final Color TABLE_FOREGROUND_COLOR = new Color(153, 0, 0);

    class JSTableModel extends AbstractTableModel {

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        public int getRowCount() {
            return jsMap.size();
        }

        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public Class getColumnClass(int column) {
            return String.class;
        }

        public String getValueAt(int row, int col) {
            String[] jobs = jsMap.keySet().toArray(new String[0]);
            switch (col) {
                case 0:
                    return jobs[row];
                case 1:
                    return jsMap.get(jobs[row]);
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
            //setHorizontalTextPosition(JLabel.TRAILING);
            //setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
            setForeground(TABLE_FOREGROUND_COLOR);
        }

        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isCellSelected,
                boolean cellHasFocus,
                int row,
                int col) {
            setText(value.toString());
            if (col == STATUS_COLUMN) {
                setIcon(imageMap.get(value.toString()));
            }
            if (isCellSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            return this;
        }
    }

    class JobRowSorter extends TableRowSorter<JSTableModel> {

        public JobRowSorter(final JSTableModel jobTableModel) {
            super(jobTableModel);
            setComparator(1, new JobComparator());
        }
    }

    class JobSelectionHandler implements ListSelectionListener {

        JTable tableSource = null;

        JobSelectionHandler(final JTable source) {
            tableSource = source;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting() == false) {
                ListSelectionModel source = (ListSelectionModel) e.getSource();
                if (!(source == null || source.isSelectionEmpty())) {
                    String jobId = (String) getValueAt(getSelectedRow(), 0);
                    if (jobId != null) {
                        parentTab.getProcessingPane().showSpecificJob(Integer.parseInt(jobId));
                    }
                }
            }
        }
    }

    class JobStatusTracker extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(REFRESH_TIME);
                } catch (InterruptedException ie) {
                }
                refreshJobStatus();
            }
        }
    }

    void parseOutput(final JobInfo[] jobInfos) {
        if (jobInfos != null) {
            for (int i = 0; i < jobInfos.length; i++) {
                if (runManager.getJobIds().contains(Integer.valueOf(jobInfos[i].getId()))) {
                    int jobid = jobInfos[i].getId();
                    jsMap.put(Integer.toString(jobid), jobInfos[i].getStatus());
                    if (RunUtil.RUN_DONE_MSG.equalsIgnoreCase(jobInfos[i].getStatus()) && runManager.isDownloadResults()) {
                        boolean jobTransferred = trasferDone.contains(Integer.valueOf(jobInfos[i].getId()));
                        if (!jobTransferred) {
                            boolean isTransferred = FileTransferManager.instance().fetchResults(runManager.getJobPath(jobid));
                            if (isTransferred) {
                                trasferDone.add(Integer.valueOf(jobInfos[i].getId()));
                            }
                        }
                    }
                }
            }
        }
        ((JSTableModel) getModel()).fireTableDataChanged();
    }

    public void refreshJobStatus() {

        Vector<Integer> intObjs = runManager.getJobIds();
        int jobs = intObjs.size();
        if (jobs > 0) {
            int[] jobIds = new int[jobs];
            for (int i = 0; i < jobs; i++) {
                jobIds[i] = intObjs.elementAt(i).intValue();
            }
            RunInfo[] runInfos = runManager.monitor(jobIds);
            parseOutput((JobInfo[]) runInfos);
        }
    }

    public void startAutoRefresh() {
        new JobStatusTracker().start();
        autoRefresh = true;
    }

    public void removeCompletedJob(final String jobToRemove) {
        jsMap.remove(jobToRemove);
        ((JSTableModel) getModel()).fireTableDataChanged();
    }

    public boolean autoRefreshStarted() {
        return autoRefresh;
    }

    public void setSelectedJob(final int jobId) {
        JSTableModel model = (JSTableModel) getModel();
        int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            if (jobId == Integer.parseInt(model.getValueAt(i, 0))) {
                getSelectionModel().setSelectionInterval(i, i);
                break;
            }
        }
    }
    private HashMap<String, String> jsMap = new HashMap<String, String>();
    private RunManager runManager = RunManager.instance();
    private boolean autoRefresh = false;
    private HashMap<String, ImageIcon> imageMap;
    private ProcessingTab parentTab = null;
    private Vector<Integer> trasferDone = new Vector<Integer>();

    private ImageIcon readImageIcon(String filename) {
        URL url = ProcessingPane.class.getResource("/resources/images/" + filename);
        return new ImageIcon(url);
    }

    private void loadStatusImages() {
        imageMap = new HashMap<String, ImageIcon>();
        imageMap.put(RunUtil.RUN_DONE_MSG, readImageIcon("done.gif"));
        imageMap.put(RunUtil.QUEUEING_MSG, readImageIcon("waiting.gif"));
        imageMap.put(RunUtil.WAITING_MSG, readImageIcon("waiting.gif"));
        imageMap.put(RunUtil.FAILED_MSG, readImageIcon("failed.gif"));
        imageMap.put(RunUtil.RUNNING_MSG, readImageIcon("running.gif"));
        imageMap.put(RunUtil.ABORT_MSG, readImageIcon("trash.gif"));

        imageMap.put(RunUtil.EXITING_MSG, readImageIcon("exit.png"));
        imageMap.put(RunUtil.HOLD_MSG, readImageIcon("waiting.gif"));
        imageMap.put(RunUtil.MOVING_MSG, readImageIcon("synced.gif"));
    }

    public JobStatusTable(final ProcessingTab aParent) {
        super();
        parentTab = aParent;
        setModel(new JSTableModel());
        setForeground(TABLE_FOREGROUND_COLOR);
        //setBackground(UIManager.getColor("Panel.background"));
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(false);
        setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        loadStatusImages();
        getColumnModel().getColumn(0).setCellRenderer(new TextCellRenderer());
        getColumnModel().getColumn(0).setPreferredWidth(ID_COLUMN_WIDTH);
        getColumnModel().getColumn(1).setCellRenderer(new TextCellRenderer());
        getColumnModel().getColumn(1).setPreferredWidth(STATUS_COLUMN_WIDTH);
        int tableFontSize = getFont().getSize();
        int minimumRowHeight = tableFontSize + 6;
        int defaultRowHeight = LookUtils.IS_LOW_RESOLUTION ? 17 : 18;
        setRowHeight(Math.max(minimumRowHeight, defaultRowHeight));
        setRowSorter(new JobRowSorter(new JSTableModel()));
        getSelectionModel().addListSelectionListener(new JobSelectionHandler(this));
    }
}
