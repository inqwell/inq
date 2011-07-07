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

import javax.swing.tree.TreePath;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractMap;
import com.inqwell.any.Any;
import com.inqwell.any.AnyAlwaysEquals;
import com.inqwell.any.AnyComparator;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFuncHolder;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.DegenerateIter;
import com.inqwell.any.Iter;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Vectored;
import com.inqwell.any.client.AnyTreeModel.AnyTreeModelEvent;

public abstract class CommonTreeLevel extends    AbstractMap
                                      implements TreeLevel
{
  public int getExpansionCount()
  {
    return 0;
  }

  public TreeNodeExpansion getExpansion(int index)
  {
    throw new UnsupportedOperationException();
  }

  public int getExpansionIndex(NodeSpecification path)
  {
    throw new UnsupportedOperationException();
  }

  public Vectored getChildVector(AnyTreeNode n) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

  public void sort(AnyTreeNode n, boolean depthSort)
  {
    throw new UnsupportedOperationException();
  }

  public Any getEquals()
  {
    throw new UnsupportedOperationException();
  }

  public void setEquals(Any equals)
  {
    throw new UnsupportedOperationException();
  }

  public Any get(Any key)
  {
    Any ret = getWithKey(key);
    if (ret == null)
      handleNotExist(key); // throws

    return ret;
  }

  public Any getIfContains(Any key)
  {
    return getWithKey(key);
  }

  public boolean contains (Any key)
  {
    return (key.equals(AnyTable.editor__) ||
            key.equals(AnyTable.renderer__) ||
            key.equals(AnySelection.modelSort__) ||
            key.equals(AnyTree.name__) ||
            key.equals(AnyAlwaysEquals.equals__) ||
            key.equals(AnyComponent.columns__));
  }

  public boolean isEmpty() { return false; }

  // Tree level protected interface

  // Map protected interface implementations
  protected boolean beforeAdd(Any key, Any value) { return true; }
  protected void afterAdd(Any key, Any value) {}
  protected void beforeRemove(Any key) {}
  protected void afterRemove(Any key, Any value) {}
  protected void emptying() {}
  public Iter createIterator () {return DegenerateIter.i__;}

  public boolean containsValue (Any value)
  {
    throw new UnsupportedOperationException();
  }

  private Any getWithKey(Any key)
  {
    // The editor for the tree itself (as opposed to any other
    // columns when a TreeTable).
    if (key.equals(AnyTable.editor__))
    {
      // When fetching through property access, if there's no
      // editor available, create one.  See also
      // the AnyTable.ColumnProperty class.
      AnyComponentEditor ace = getEditor();
      if (ace == null)
      {
        RenderInfo r = getRenderInfo();
        ace = new AnyComponentEditor(r);
        r.setEditable(true);
        setEditor(ace);
      }
      return ace;
    }
    else if (key.equals(AnyTable.renderer__))
    {
      AnyCellRenderer r = getRenderer();
      return r;
    }
    else if (key.equals(AnySelection.modelSort__))
    {
      return new ModelSort();
    }
    else if (key.equals(AnyTree.name__))
    {
      return getName();
    }
    else if (key.equals(AnyComponent.columns__))
    {
      return new ColumnAccess();
    }
    else if (key.equals(AnyAlwaysEquals.equals__))
    {
      return new LevelEquals();
    }

    return null;
  }

  // Support the Map interface to access the various features
  // of the configuration of the TreeLevels within a TreeTable.
  // Instances of these objects are used twice in a path like
  //  myTreeTable.properties.levels.SwapTrade.columns.Price.editor
  // once for "Price" and once for "editor"
  private class ColumnAccess extends AbstractMap
  {
    private static final long serialVersionUID = 1L;

    // Once we have been queried for a column name this will be
    // set.  -1 means column does not exist
    private int column_ = -2;

    public Any get(Any key)
    {
      Any ret = getWithKey(key);
      if (ret == null)
        handleNotExist(key); // throws

      return ret;
    }

    public Any getIfContains(Any key)
    {
      return getWithKey(key);
    }

    public boolean contains (Any key)
    {
      // If we are initialised as a known column then we can have
      // specific children.
      if (column_ >= 0 && (key.equals(AnyTable.editor__) ||
                           key.equals(AnyTable.renderer__)))
        return true;

      // Otherwise assume the child is a column key.  If its known
      // to the table (so must be a treetable) then associate us
      // with that column.
      AnyTable t = getModel().getTable();

      int i = t.getColumnIndex(key);

      if (i >= 0)
        return true;

      return false;
    }

    public boolean isEmpty() { return false; }

    protected boolean beforeAdd(Any key, Any value) { return true; }
    protected void afterAdd(Any key, Any value) {}
    protected void beforeRemove(Any key) {}
    protected void afterRemove(Any key, Any value) {}
    protected void emptying() {}
    public Iter createIterator () {return DegenerateIter.i__;}

    public boolean containsValue (Any value)
    {
      throw new UnsupportedOperationException();
    }

    private Any getWithKey(Any key)
    {
      if (column_ >= 0)
      {
        // returning child of specific column.  The only supported
        // children at present are "renderer" and "editor"
        if (key.equals(AnyTable.editor__))
        {
          // When fetching through property access, if there's no
          // editor available, create one.  See also
          // the AnyTable.ColumnProperty class.
          AnyComponentEditor ace = getEditor(column_);
          if (ace == null)
          {
            //RenderInfo r = getModel().getTable().getColumnRenderInfo(column_);
            RenderInfo r = CommonTreeLevel.this.getColumnRenderInfo(column_);
            ace = new AnyComponentEditor(r);
            r.setEditable(true);
            setEditor(ace, column_);
          }

          return ace;
        }
        else if (key.equals(AnyTable.renderer__))
        {
          AnyCellRenderer r = null;
          if (column_ == 0)
            r = getRenderer();
          else
            r = getRenderer(column_);

          return r;
        }
        return null;
      }
      else
      {
        // Identifying us as a specific column
        AnyTable t = getModel().getTable();

        int i = t.getColumnIndex(key);

        if (i >= 0)
        {
          column_ = i;
          return this;
        }

        return null;
      }
    }
  }

  // One of these is returned in anticipation of writing the modelSort
  // property for the TreeLevel. As a reminder, the path is of the form
  //    myTree.properties.levels.myLevel.modelSort = sortVal;
  private class ModelSort extends AbstractAny
  {
    public Any copyFrom(Any a)
    {
      AnyTreeModel model = getModel();

      AnyComparator ac = AnySelection.makeComparator(a, // paths
                                                     model.getContext());

      setComparator(ac);

      // When a new sort is applied to a level then traverse the
      // tree node structure and apply the sort to the node(s) whose
      // children have that level.
      AnyTreeNode parent = (AnyTreeNode)model.getRoot();
      if (parent != null)
      {
        TreeLevel level = parent.getTreeLevel();
        if (level == CommonTreeLevel.this)
        {
          // The root level - only one vector to sort.
          sort(parent, false);

          model.resetRoot();

          TreePath treePath = parent.makeTreePath();
          AnyTreeModelEvent tme = new AnyTreeModelEvent(this, treePath);
          tme.setExpandPaths(true);
          model.fireStructureEvent(tme);
        }
        else
        {
          try
          {
            // Somewhere under the root (first) level children.
            orderChildLevel(model, parent);
          }
          catch (Exception e)
          {
            throw new RuntimeContainedException(e);
          }
        }
      }
      return this;
    }

    private void orderChildLevel(AnyTreeModel model,
                                 AnyTreeNode  parent)
    {
      int count = model.getChildCount(parent);
      while(count-- > 0)
      {
        AnyTreeNode child = (AnyTreeNode)model.getChild(parent, count);
        TreeLevel l = child.getTreeLevel();
        if (l == CommonTreeLevel.this)
        {
          // Found the tree level to which the comparator is being applied.
          // Sort the parent
          parent.getTreeLevel().sort(parent, false);

          parent.setStale();

          TreePath treePath = parent.makeTreePath();
          AnyTreeModelEvent tme = new AnyTreeModelEvent(this, treePath);
          tme.setExpandPaths(true);
          model.fireStructureEvent(tme);

          // No need to find any more children beneath this parent, all
          // that was necessary was to find the appropriate tree level.
          break;
        }
        else
        {
          // Not the right tree level yet. Recurse depth-wise
          orderChildLevel(model, child);
        }
      }
    }
  }

  // One of these is returned in anticipation of writing the "equals"
  // property for the TreeLevel. As a reminder, the path is of the form
  //    myTree.properties.levels.myLevel.equals = equalityExpr;
  private class LevelEquals extends AbstractAny
  {
    public Any copyFrom(Any a)
    {
      if (!(a instanceof AnyFuncHolder.FuncHolder))
        throw new AnyRuntimeException("Not a function");
      
      AnyFuncHolder.FuncHolder f = (AnyFuncHolder.FuncHolder)a;
      setEquals(f);
      
      return this;
    }
  }
}
