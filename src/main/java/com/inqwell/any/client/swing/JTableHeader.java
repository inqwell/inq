/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JTableHeader.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.ChangeEvent;

public class JTableHeader extends javax.swing.table.JTableHeader
{
  public JTableHeader()
  {
  }
  
  public JTableHeader(TableColumnModel cm)
  {
    super(cm);
  }

  public void columnMarginChanged(ChangeEvent e)
  {
    //System.out.println("columnMarginChanged " + e.getSource());
    super.columnMarginChanged(e);
  }

  public void columnMoved(TableColumnModelEvent e)
  {
    //System.out.println("columnMoved");
    super.columnMoved(e);
  }

/*
  public TableColumn getDraggedColumn()
  {
    TableColumn c = super.getDraggedColumn();
    System.out.println("getDraggedColumn 1 " + c);
    System.out.println("getDraggedColumn 2 " + draggedColumn);
    System.out.println("getDraggedColumn 3 " + draggedDistance);
    return c;
  }
*/
}
