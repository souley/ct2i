/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * HelpViewer.java
 *
 * Created on 31-mei-2010, 15:00:40
 */

package nl.biggrid.ct.help;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author Souley
 */
public class HelpViewer extends javax.swing.JFrame {
	/** Home URL. */
	private URL _homeURL;

	/** Panel that displays the help document. */
	private HtmlViewer _detailPnl;

	/** Collection of the nodes in the tree keyed by the URL.toString(). */
	private final Map<String, DefaultMutableTreeNode> _nodes = new HashMap<String, DefaultMutableTreeNode>();

    /** Creates new form HelpViewer */
    public HelpViewer() {
        initComponents();
        jsppAll.add(createDetailsPanel(), javax.swing.JSplitPane.RIGHT);
        initOtherThings();
        try {
            createTreeContents();
        } catch (IOException ex) {
            System.err.println("Error generating Contents file");
		}
        setIconImage(readImageIcon("help.gif").getImage());
    }

    protected static ImageIcon readImageIcon(String filename) {
        URL url = HelpViewer.class.getResource("/resources/images/" + filename);
        return new ImageIcon(url);
    }

    private void initOtherThings() {

        SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				_detailPnl.setHomeURL(_homeURL);
				jtTOC.expandRow(0);
 				jtTOC.expandRow(2);
                jtTOC.setSelectionRow(1);
				jtTOC.setRootVisible(false);
			}
		});

        _detailPnl.addListener(new IHtmlViewerListener()
		{
			public void currentURLHasChanged(HtmlViewerListenerEvent evt)
			{
				selectTreeNodeForURL(evt.getHtmlViewerPanel().getURL());
			}
			public void homeURLHasChanged(HtmlViewerListenerEvent evt)
			{
				// Nothing to do.
			}
		});
    }

	private void selectTreeNodeForURL(URL url)
	{
		// Strip local part of URL.
		String key = url.toString();
		final int idx = key.lastIndexOf('#');
		if ( idx > -1)
		{
			key = key.substring(0, idx);
		}
		DefaultMutableTreeNode node = _nodes.get(key);
		if (node != null) // && node != _tree.getLastSelectedPathComponent())
		{
			DefaultTreeModel model = (DefaultTreeModel)jtTOC.getModel();
			TreePath path = new TreePath(model.getPathToRoot(node));
			if (path != null)
			{
				jtTOC.expandPath(path);
				jtTOC.scrollPathToVisible(path);
				jtTOC.setSelectionPath(path);
			}
		}
	}

    /**
	 * Set the Document displayed to that defined by the passed URL.
	 *
	 * @param	url		URL of document to be displayed.
	 */
	private void setSelectedDocument(URL url)
	{
        try {
            _detailPnl.gotoURL(url);
		}
		catch (IOException ex) {
            System.err.println("Error displaying document");
            ex.printStackTrace();
		}
	}

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        URL imgURL = HelpViewer.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("HelpViewer.createImageIcon() Couldn't find file: " + path);
            return null;
        }
    }

    private void createTreeContents() throws IOException {
		final FolderNode root = new FolderNode("Help");
        jtTOC.setModel(new DefaultTreeModel(root));
		jtTOC.addTreeSelectionListener(new ObjectTreeSelectionListener());

		// Renderer for tree.
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(createImageIcon("/resources/images/topic.gif"));
		renderer.setOpenIcon(createImageIcon("/resources/images/toc_open.gif"));
		renderer.setClosedIcon(createImageIcon("/resources/images/toc_closed.gif"));
		jtTOC.setCellRenderer(renderer);

		final FolderNode helpRoot = new FolderNode("Help");
		root.add(helpRoot);
		_nodes.put(helpRoot.getURL().toString(), helpRoot);

		File file = new File("help.html");
		try
		{
			DocumentNode dn = new DocumentNode("ctii", file);
			helpRoot.add(dn);
			_homeURL = dn.getURL();
			_nodes.put(_homeURL.toString(), dn);
		}
		catch (MalformedURLException ex)
		{
            System.err.println("Error retrieving Help from URL "+file.getAbsolutePath());
		}

    }

  	HtmlViewer createDetailsPanel()
	{
		_detailPnl = new HtmlViewer(null);
		return _detailPnl;
	}

	private class DocumentNode extends DefaultMutableTreeNode
	{
        private static final long serialVersionUID = 1L;

        private URL _url;

		DocumentNode(String title, File file) throws MalformedURLException
		{
			super(title, false);
			setFile(file);
		}

		DocumentNode(String title, boolean allowsChildren)
		{
			super(title, allowsChildren);
		}

		URL getURL()
		{
			return _url;
		}

		void setFile(File file) throws MalformedURLException
		{
			_url = file.toURI().toURL();
		}
	}

	private class FolderNode extends DocumentNode
	{
        private static final long serialVersionUID = 1L;
        private final List<String> _docTitles = new ArrayList<String>();
		private final List<URL> _docURLs = new ArrayList<URL>();
		private final File _contentsFile;

		FolderNode(String title) throws IOException
		{
			super(title, true);
			_contentsFile = File.createTempFile("ctiihelp", "html");
			_contentsFile.deleteOnExit();
			setFile(_contentsFile);
		}

        @Override
		public void add(MutableTreeNode node)
		{
			super.add(node);
			if (node instanceof DocumentNode)
			{
				final DocumentNode dn = (DocumentNode)node;
				final URL docURL = dn.getURL();
				if (docURL != null)
				{
					String docTitle = dn.toString();
					if (docTitle == null || docTitle.length() == 0)
					{
						docTitle = docURL.toExternalForm();
					}
					_docTitles.add(docTitle);
					_docURLs.add(docURL);
				}
			}
		}

		synchronized void generateContentsFile()
		{
			try
			{
				final PrintWriter pw = new PrintWriter(new FileWriter(_contentsFile));
				try
				{
					StringBuffer buf = new StringBuffer(50);
					buf.append("<HTML><BODY><H1>")
						.append(toString())
						.append("</H1>");
					pw.println(buf.toString());
					for (int i = 0, limit = _docTitles.size(); i < limit; ++i)
					{
						final URL docUrl = _docURLs.get(i);
						buf = new StringBuffer(50);
						buf.append("<A HREF=\"")
							.append(docUrl)
							.append("\">")
							.append(_docTitles.get(i))
							.append("</A><BR>");
						pw.println(buf.toString());
					}
					pw.println("</BODY></HTML");
				}
				finally
				{
					pw.close();
				}
			}
			catch (IOException ex)
			{
				System.err.println("Error generating Contents file");
			}
		}
	}

	/**
	 * This class listens for changes in the node selected in the tree
	 * and displays the appropriate help document for the node.
	 */
	private final class ObjectTreeSelectionListener
		implements TreeSelectionListener
	{
		public void valueChanged(TreeSelectionEvent evt)
		{
			final TreePath path = evt.getNewLeadSelectionPath();
			if (path != null)
			{
				Object lastComp = path.getLastPathComponent();
				if (lastComp instanceof DocumentNode)
				{
					setSelectedDocument(((DocumentNode)lastComp).getURL());
				}
			}
		}
	}
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jsppAll = new javax.swing.JSplitPane();
        jscpTree = new javax.swing.JScrollPane();
        jtTOC = new javax.swing.JTree();

        setTitle("CTII Help");
        setAlwaysOnTop(true);

        jsppAll.setDividerLocation(120);
        jsppAll.setContinuousLayout(true);
        jsppAll.setOneTouchExpandable(true);

        jtTOC.setModel(null);
        jtTOC.setShowsRootHandles(true);
        jscpTree.setViewportView(jtTOC);

        jsppAll.setLeftComponent(jscpTree);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 690, Short.MAX_VALUE)
            .addGap(0, 690, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jsppAll, javax.swing.GroupLayout.DEFAULT_SIZE, 690, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 470, Short.MAX_VALUE)
            .addGap(0, 470, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jsppAll, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jscpTree;
    private javax.swing.JSplitPane jsppAll;
    private javax.swing.JTree jtTOC;
    // End of variables declaration//GEN-END:variables

}
