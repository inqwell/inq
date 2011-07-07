/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/CommonTreeLevel.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.client.swing.TableModel;
import javax.swing.table.AbstractTableModel;

public abstract class CommonTableModel extends    AbstractTableModel
                                       implements TableModel
{
  // Non-null if we were able to remember any columns by their
  // names in the referring AnyTable
	private Vectored columns_;
  
  // A function that will be called on a row basis should events
  // be identified as dispatching to it
  private NodeFunction rowFunc_;

  public void setColumnProperties(Vectored columns)
  {
    columns_ = columns;
  }
  
  public Vectored getColumnProperties()
  {
    return columns_;
  }
  
  public Any getNameOfColumn(int col)
  {
    Any ret = null;
    
    if (columns_ != null)
    {
      AnyTable.ColumnProperty column = (AnyTable.ColumnProperty)columns_.getByVector(col);
      ret = column.getColumnName();
    }
    
    return ret;
  }
  
  public void setRowFunction(Any rowFuncF)
  {
    Call rowFunc = AnyComponent.verifyCall(rowFuncF);
    rowFunc_ = new NodeFunction(rowFunc);
    rowFunc_.resolveNodeRefs(getContext());
  }

  protected Call getRowFunc()
  {
    Call ret = null;
    if (rowFunc_ != null)
      ret = (Call)rowFunc_.getDataNode();

    return ret;
  }
  
  protected NodeFunction getRowNodeFunc()
  {
    return rowFunc_;
  }
  
  protected boolean isRowFuncDispatching(Any field)
  {
    if (rowFunc_ != null)
      return rowFunc_.isDispatching(field);
    else
      return false;
  }
}
