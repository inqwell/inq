/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/TableEditEvent.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import com.inqwell.any.Any;
import java.util.EventObject;

/**
 *
 */
public class TableEditEvent extends EventObject
{
  private Any rowRoot_;
  private Any rowKey_;
  private int column_ = -1;
  private int row_    = -1;
  private Any vOld_;
  private Any vNew_;
  private boolean isUI_;
  private boolean isBefore_;
  
	public TableEditEvent(Object source)
	{
    super(source);
	}
  
	public TableEditEvent(Object  source,
                        Any     rowRoot,
                        Any     rowKey,
                        int     row,
                        int     column,
                        Any     vOld,
                        Any     vNew,
                        boolean isUI,
                        boolean isBefore)
	{
		super(source);
		rowRoot_  = rowRoot;
		rowKey_   = rowKey;
    row_      = row;
    column_   = column;
    vOld_     = vOld;
    vNew_     = vNew;
    isUI_     = isUI;
    isBefore_ = isBefore;
	}
	
	public Any getRowRoot()
	{
    return rowRoot_;
  }
  
	public Any getRowKey()
	{
    return rowKey_;
  }
  
  public Any getOldValue()
  {
    return vOld_;
  }
  
  public Any getNewValue()
  {
    return vNew_;
  }
  
  public int getColumn()
  {
    return column_;
  }
  
  public int getRow()
  {
    return row_;
  }
  
  public boolean isBefore()
  {
    return isBefore_;
  }
  
  public boolean isUI()
  {
    return isUI_;
  }
}
