/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.client.swing;

import com.inqwell.any.Any;
import com.inqwell.any.BooleanI;
import com.inqwell.any.client.AnyComponent;
import com.inqwell.any.client.TreeLevel;

/**
 * An additional interface implemented by Inq cell editors that provides
 * additional methods through which the GUI and Inq script cooperate.
 * <p>
 * In addition, some native cell editor methods are repeated to allow
 * them to be accessed in the context of an InqEditor reference.
 * @author Tom
 *
 */
public interface InqEditor
{
  /**
   * Determine whether editing should be started in the specified cell.
   * @param parentComponent the parent tree, table or list
   * @param contextNode the current context
   * @param rowRoot root node from which cell value is referenced
   * @param value value of cell
   * @param row row number
   * @param col column in model coordinates (table and tree table only)
   * @param colName column name, if available (table and tree table only)
   * @param isLeaf if a tree editor, whether the node is a leaf. Null if
   * not a tree
   * @param expanded if a tree editor, whether the node is expanded. Null if
   * not a tree
   * @return true if editing can be started, false if it cannot
   */
  public boolean canStartEditing(AnyComponent parentComponent,
                                 Any          contextNode,
                                 Any          rowRoot,
                                 Any          value,
                                 int          row,
                                 Any          rowKey,
                                 int          col,
                                 Any          colName,
                                 TreeLevel    level,
                                 BooleanI     isLeaf,
                                 BooleanI     expanded);

  /**
   * Whether this editor has a scripted function that will be called
   * when editing stops.
   * @return true if we have a function, false otherwise.
   */
  public boolean hasStopEdit();
  
  public void onStopEditing(AnyComponent parentComponent,
                            Any          contextNode,
                            Any          rowRoot,
                            Any          oldValue,
                            Any          newValue,
                            int          row,
                            Any          rowKey,
                            int          col,
                            Any          colName,
                            BooleanI     isUser,
                            BooleanI     after,
                            TreeLevel    level,
                            BooleanI     isLeaf,
                            BooleanI     expanded);
  
  /**
   * Repeated from {@link javax.swing.CellEditor}
   */
  public Object getCellEditorValue();

}
