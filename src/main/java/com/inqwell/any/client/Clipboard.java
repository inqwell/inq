/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/Clipboard.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.util.Util;
import java.util.StringTokenizer;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;

/**
 * Provide support for importing tabular data from the system
 * clipboard.
 * <p>
 * An ordered map which is initialised to the clipboard contents as a
 * collection of ordered maps representing the clipboard rows.  The row
 * keys are <code>AnyInt</code> values starting from zero.  The column
 * keys are the strings "A", "B", "C", ... "Z", "AA", "AB" .... as required
 * by the size of the clipboard data.
 * <P>
 * The clipboard data must be of the DataFlavor String, as placed there
 * by Excel, for example.
 */
public class Clipboard extends AnyOrderedMap
{
	private StringBuffer s_;
	private Array        columnKeys_ = AbstractComposite.array();
	
	public Clipboard()
	{
		grabClipboard();
	}
	
	public void grabClipboard()
	{
    java.awt.datatransfer.Clipboard clipboard = 
			Toolkit.getDefaultToolkit().getSystemClipboard();
		
		Map lastRow = null;
		
		try
		{		
			String clipData = (String)(clipboard.getContents(this).getTransferData
																				(DataFlavor.stringFlavor));
			StringTokenizer rowTokens =
				new StringTokenizer(clipData, Util.lineSeparator());
			
			int    rowCount  = 0;
			
			while (rowTokens.hasMoreTokens())
			{
				String rowString = rowTokens.nextToken();
				
				StringTokenizer cellTokens = new StringTokenizer(rowString, "\t");
				
				int columnCount = 0;
				
				Map row = AbstractComposite.orderedMap();
				
				while (cellTokens.hasMoreTokens())
				{
				  Any columnKey = nextColumnKey(columnCount++, columnKeys_);

					String cell = cellTokens.nextToken();
					
					row.add(columnKey, new ConstString(cell));
				}
				IntI rowKey    = new ConstInt(rowCount++);
				add(rowKey, row);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeContainedException(e);
		}
	}
	
	private Any nextColumnKey(int columnCount, Array keys)
	{
		if (columnCount < keys.entries())
		{
			return keys.get(columnCount);
		}
		else
		{
			StringBuffer s = new StringBuffer();
			while (columnCount >= 0)
			{
				s.append('A' + columnCount % 26);
				columnCount -= 26;
			}
			Any key = new ConstString(s.toString());
			keys.add(key);
			return key;
		}
	}
}
