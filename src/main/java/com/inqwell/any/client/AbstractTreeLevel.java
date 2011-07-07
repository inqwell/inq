/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AbstractTreeLevel.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.Array;
import com.inqwell.any.client.swing.TableModel;

public abstract class AbstractTreeLevel extends CommonTreeLevel
{
  private AnyTreeModel m_;
  
	// Each level has its own renderer for tree nodes
	// represented by Inq children...
  protected AnyCellRenderer renderer_;
  
  // Only non-null if we are being used as a TreeTable and
  // specific columns have been configured at this level.
  // [Note that colInfo[0] will be the same as renderer_ above
  // unless overridden]
  private Array colInfo_;

  // Like the above, but the editors
  private Array colEditors_;
  
  // If we have a name then this is set
  private Any name_;
  
  // Equals expression
  private Any equals_;

  public void setClosedIcon(Any icon)
  {
    renderer_.setClosedIcon(icon);
  }

  public void setLeafIcon(Any icon)
  {
    renderer_.setLeafIcon(icon);
  }
  
  public void setOpenIcon(Any icon)
  {
    renderer_.setOpenIcon(icon);
  }

  public void setColumns(Array columns, TableModel tableModel)
  {
    // The tableModel offers up the renderinfos and corresponding
    // cell renderers. We are given an array of renderinfos the elements
    // of which are 1) the same as the tableModel when there is no
    // override or 2) are different when there is. Work out which and
    // create new cell renderers as appropriate
    
    if (colInfo_ == null)
      colInfo_ = AbstractComposite.array();
    else
      colInfo_.empty();
    
    // Column zero is the tree
    colInfo_.add(columns.get(0));
    
    for (int i = 1; i < columns.entries(); i++)
    {
      RenderInfo override  = (RenderInfo)columns.get(i);
      RenderInfo fromTable = tableModel.getRenderInfo(i);
      if (override == fromTable)
      {
        // inherit the renderer from the table
        colInfo_.add((AnyCellRenderer)tableModel.getCellRenderer(-1, i));
      }
      else
      {
        // Its an override so make a new one
        colInfo_.add(new AnyCellRenderer(override));
      }
    }
  }
  
  public RenderInfo getColumnRenderInfo(int col)
  {
    RenderInfo r = null;

    if (col == 0)
      r = getRenderInfo();
    else if (colInfo_ != null)
    {
      AnyCellRenderer renderer = (AnyCellRenderer)colInfo_.get(col);
      r = renderer.getRenderInfo();
    }
    
    return r;
  }

  public void setBranchOnly(Any branchOnly)
  {
    throw new UnsupportedOperationException("setBranchOnly");
  }

  public void setModel(AnyTreeModel m)
  {
    TreeLevel nextLevel = getNextTreeLevel();
    
    if (nextLevel != this && nextLevel != TerminalTreeLevel.terminalLevel__)
      nextLevel.setModel(m);
    
    // Put the model into any expansions
    int exp = getExpansionCount();
    for (int i = 0; i < exp; i++)
    {
      TreeNodeExpansion e = getExpansion(i);
      e.setModel(m);
    }
    
    // Save here of course
    m_ = m;
  }

  public AnyComponentEditor getEditor(int column)
  {
    if (column == 0)
      return getEditor();
    
    if (colEditors_ == null)
      return null;
    
    return (AnyComponentEditor)colEditors_.get(column);
  }
  
  public AnyCellRenderer getRenderer(int column)
  {
    if (colInfo_ == null)
      return null;
    
    return (AnyCellRenderer)colInfo_.get(column);
  }
  
  public AnyCellRenderer getRenderer()
  {
    return renderer_;
  }
  
  public void setEditor(AnyComponentEditor ace, int column)
  {
    if (column == 0)
    {
      setEditor(ace);
    }
    else
    {
      // If no array then initialise to number of columns and clear
      // to null.
      if (colEditors_ == null)
      {
        int cc = this.getModel().getTable().getModel().getColumnCount();
        colEditors_ = AbstractComposite.array(cc);
        for (int i = 0; i < cc; i++)
          colEditors_.add(null);
      }
      
      colEditors_.replaceItem(column, ace);
    }
  }
  
  public AnyTreeModel getModel()
  {
    return m_;
  }
  
  public void setName(Any name)
  {
    name_ = name;
  }
  
  public Any getName()
  {
    return name_;
  }

  public void setEquals(Any equals)
  {
    equals_ = equals;
  }

  public Any getEquals()
  {
    return equals_;
  }
}
