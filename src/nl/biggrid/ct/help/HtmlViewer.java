package nl.biggrid.ct.help;
/*
 * Copyright (C) 2003 Colin Bell
 * colbell@users.sourceforge.net
 *
 */
import java.awt.BorderLayout;
import java.awt.Point;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.EventListenerList;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 * This panel shows the contents of a HTML file.
 *
 * @author  <A HREF="mailto:colbell@users.sourceforge.net">Colin Bell</A>
 */
public class HtmlViewer extends JPanel
{
	/** Text area containing the HTML. */
	private final JEditorPane _contentsTxt = new JEditorPane();

	/** <TT>JScrollPane</TT> for <TT>_contentsText</TT>. */
	private JScrollPane _contentsScrollPane;

	/** Original URL (home). */
	private URL _homeURL;

	/** Current URL. */
	private URL _currentURL;

	/** History of links. */
	private final List<URL> _history = new LinkedList<URL>();

	/** Current index into <TT>_history</TT>. */
	private int _historyIndex = -1;

	/**
	 * Collection of listeners for events in this object.
	 */
	private EventListenerList _listenerList = new EventListenerList();

	/**
	 * Ctor.
	 * 
	 * @param	url	URL to home document.
	 */
	public HtmlViewer(URL url)
	{
		super();
		createGUI();
		setHomeURL(url);
		setURL(url);
	}

	/**
	 * Retrieve the current URL.
	 * 
	 * @return	The current URL.
	 */
	public URL getURL()
	{
		return _currentURL;
	}

	/**
	 * Retrieve the home URL.
	 * 
	 * @return	The home URL.
	 */
	public URL getHomeURL()
	{
		return _homeURL;
	}

	/**
	 * Specify the URL that is to the consider the &quot;Home&quot; URL. THis
	 * does <EM>not</EM> change the current URL for this viewer.
	 * 
	 * @param	homeURL		The new home URL.
	 */
	public void setHomeURL(URL homeURL)
	{
		_homeURL = homeURL;
		fireHomeURLChanged();
	}

	/**
	 * Adds a listener to this object.
	 *
	 * @param	lis		Listener to be added.
	 */
	public void addListener(IHtmlViewerListener lis)
	{
		_listenerList.add(IHtmlViewerListener.class, lis);
	}

	/**
	 * Removes a listener from this object.
	 *
	 * @param	lis	Listener to be removed.
	 */
	void removeListener(IHtmlViewerListener lis)
	{
		_listenerList.remove(IHtmlViewerListener.class, lis);
	}

	public synchronized void gotoURL(URL url) throws IOException
	{
		if (url == null)
		{
			throw new IllegalArgumentException("URL == null");
		}
		if (!url.equals(_currentURL))
		{
			ListIterator<URL> it = _history.listIterator(_historyIndex + 1);
			while (it.hasNext())
			{
				it.next();
				it.remove();
			}
			_history.add(url);
			_historyIndex = _history.size() - 1;
			_contentsTxt.setPage(url);
			_currentURL = url;
			fireURLChanged();
		}
	}

	public synchronized void goBack()
	{
		if (_historyIndex > 0 && _historyIndex < _history.size())
		{
			displayURL(_history.get(--_historyIndex));
		}
	}

	public synchronized void goForward()
	{
		if (_historyIndex > -1 && _historyIndex < _history.size() - 1)
		{
			displayURL(_history.get(++_historyIndex));
		}
	}

	public synchronized void goHome()
	{
		_historyIndex = 0;
		displayURL(_homeURL);
	}

	public void refreshPage()
	{
		final Point pos = _contentsScrollPane.getViewport().getViewPosition();
		displayURL(_currentURL);
		_contentsScrollPane.getViewport().setViewPosition(pos);
	}

	/**
	 * Displayed the passed URL in this panel.
	 * 
	 * @param	url		URL to be displayed.
	 */
	private synchronized void setURL(URL url)
	{
		if (url != null)
		{
            displayURL(url);
            _history.add(url);
            _historyIndex = 0;
		}
	}

	/**
	 * Display the passed URL. This method does not affect the URL history, it
	 * merely displays the URL.
	 * 
	 * @param	url	The URL to display.
	 */
	private void displayURL(URL url)
	{
		if (url != null)
		{
			try
			{
				_contentsTxt.setPage(url);
				_currentURL = url;
				fireURLChanged();
			}
			catch (Exception ex)
			{
                System.err.println("Error displaying URL.");
                ex.printStackTrace();
			}
		}
	}

	/**
	 * Fire a "URL changed" event to all listeners.
	 */
	private void fireURLChanged()
	{
		// Guaranteed to be non-null.
		Object[] listeners = _listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event.
		HtmlViewerListenerEvent evt = null;
		for (int i = listeners.length - 2; i >= 0; i-=2 )
		{
			if (listeners[i] == IHtmlViewerListener.class)
			{
				// Lazily create the event.
				if (evt == null)
				{
					evt = new HtmlViewerListenerEvent(this);
				}
				((IHtmlViewerListener)listeners[i + 1]).currentURLHasChanged(evt);
			}
		}
	}

	/**
	 * Fire a "Home URL changed" event to all listeners.
	 */
	private void fireHomeURLChanged()
	{
		// Guaranteed to be non-null.
		Object[] listeners = _listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event.
		HtmlViewerListenerEvent evt = null;
		for (int i = listeners.length - 2; i >= 0; i-=2 )
		{
			if (listeners[i] == IHtmlViewerListener.class)
			{
				// Lazily create the event.
				if (evt == null)
				{
					evt = new HtmlViewerListenerEvent(this);
				}
				((IHtmlViewerListener)listeners[i + 1]).homeURLHasChanged(evt);
			}
		}
	}

	/**
	 * Create user interface.
	 */
	private void createGUI()
	{
		setLayout(new BorderLayout());
		add(createMainPanel(), BorderLayout.CENTER);
	}

	/**
	 * Create the main panel.
	 */
	private JPanel createMainPanel()
	{
		_contentsTxt.setEditable(false);
		_contentsTxt.setContentType("text/html");

		final JPanel pnl = new JPanel(new BorderLayout());
		_contentsTxt.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
		_contentsTxt.addHyperlinkListener(createHyperLinkListener());
		_contentsScrollPane = new JScrollPane(_contentsTxt,
									JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
									JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		pnl.add(_contentsScrollPane, BorderLayout.CENTER);

		return pnl;
	}

	private HyperlinkListener createHyperLinkListener()
	{
		return new HyperlinkListener()
		{
			public void hyperlinkUpdate(HyperlinkEvent e)
			{
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
				{
					if (e instanceof HTMLFrameHyperlinkEvent)
					{
						((HTMLDocument)_contentsTxt.getDocument()).processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent)e);
					}
					else
					{
						try
						{
							gotoURL(e.getURL());
						}
						catch (IOException ex)
						{
                            System.err.println("Error processing hyperlink.");
                            ex.printStackTrace();
						}
					}
				}
			}
		};
	}
}
