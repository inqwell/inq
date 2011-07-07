/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/TreeLevel.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.client.swing.TableModel;

import javax.swing.JTree;
import java.awt.Component;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;

/**
 * An explicit tree node that may appear at a particular tree level.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public interface TreeNodeExpansion extends Any
{
  public boolean isLeaf();

  public boolean isEditable();
  
  public TreeLevel getTreeLevel();
  
  public Any resolveStructureNode(Any root) throws AnyException;
  
  public Any getResponsibleFor(Any a) throws AnyException;
  
  public Any getValueFor(Any a) throws AnyException;

  public void setColumns(Array columns, TableModel tableModel);

  public RenderInfo getColumnRenderInfo(int col);

  // The renderer for the specified column.  Only valid for TreeTables.
  public AnyCellRenderer getRenderer(int column);

  public AnyCellRenderer getRenderer();
  
  public void setModel(AnyTreeModel m);

  public AnyTreeModel getModel();

  public boolean matchesPath(NodeSpecification path);

  public boolean navigableBetween(Map start, Any end);
  
  public Component getTreeCellRendererComponent(JTree       tree,
                                                AnyTreeNode value,
                                                boolean     selected,
                                                boolean     expanded,
                                                boolean     leaf,
                                                int         row,
                                                boolean     hasFocus);

  //public DefaultTreeCellRenderer getDefaultTreeCellRenderer();
  
  public TreeCellEditor getEditor();
  
  public AnyComponentEditor getEditor(int column);

  public void setClosedIcon(Any icon);

  public void setLeafIcon(Any icon);
  
  public void setOpenIcon(Any icon);
}
