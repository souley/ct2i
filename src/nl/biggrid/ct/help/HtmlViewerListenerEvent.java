package nl.biggrid.ct.help;
/*
 * Copyright (C) 2003 -2006 Colin Bell
 * colbell@users.sourceforge.net
 *
 */
import java.util.EventObject;
/**
 * This class is an event fired for changes in the HtmlViewerPanel.
 *
 * @author  <A HREF="mailto:colbell@users.sourceforge.net">Colin Bell</A>
 */
public class HtmlViewerListenerEvent extends EventObject
{
	/** The <CODE>HtmlViewerPanel</CODE> involved. */
	private HtmlViewer _pnl;
	/**
	 * Ctor.
	 *
	 * @param	source	The <CODE>HtmlViewerPanel</CODE> that change has
	 *					happened to.
	 * 
	 * @throws	IllegalArgumentException
	 * 			Thrown if <TT>null</TT>HtmlViewerPanel/TT> passed.
	 */
	HtmlViewerListenerEvent(HtmlViewer source)
	{
		super(checkParams(source));
		_pnl = source;
	}
	/**
	 * Return the <CODE>HtmlViewerPanel</CODE>.
	 */
	public HtmlViewer getHtmlViewerPanel()
	{
		return _pnl;
	}
	private static HtmlViewer checkParams(HtmlViewer source)
	{
		if (source == null)
		{
			throw new IllegalArgumentException("HtmlViewer == null");
		}
		return source;
	}
}