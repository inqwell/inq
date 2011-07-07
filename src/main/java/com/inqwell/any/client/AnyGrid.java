/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyGrid.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JScrollPane;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.AbstractMap;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyDouble;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.ConstDouble;
import com.inqwell.any.DegenerateIter;
import com.inqwell.any.DoubleI;
import com.inqwell.any.Globals;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.Map;
import com.inqwell.any.Set;
import com.inqwell.any.Transaction;
import com.inqwell.any.client.swing.JPanel;

/**
 * Inq wrapper for a JPanel when using TableLayout
 */
public class AnyGrid extends AnyLayoutContainer
{
  public  static Any   hGap__       = AbstractValue.flyweightString("hGap");
  public  static Any   vGap__       = AbstractValue.flyweightString("vGap");

  public static DoubleI GRID_FILL      = new ConstDouble(TableLayoutConstants.FILL);
  public static DoubleI GRID_MINIMUM   = new ConstDouble(TableLayoutConstants.MINIMUM);
  public static DoubleI GRID_PREFERRED = new ConstDouble(TableLayoutConstants.PREFERRED);
  
  private static Set     gridProperties__;
  
  private JPanel grid_;
  
  // Maps of identifiers (defined in the layout syntax) to integer
  // axis coordinate.
  private Map xAxis_;  // aka columns
  private Map yAxis_;  // aka rows
  
  private Validate validate_;
  
  private Axis axis_;

  static
  {
    gridProperties__ = AbstractComposite.set();
    
    gridProperties__.add(columns__);
    gridProperties__.add(rows__);
    gridProperties__.add(hGap__);
    gridProperties__.add(vGap__);
  }
  
  public AnyGrid()
  {
  }
  
  public AnyGrid(JPanel p)
  {
    setObject(p);
  }
  
  public AnyGrid(double columns[], double rows[])
  {
    JPanel p = new JPanel(new TableLayout(columns, rows));
    setObject(p);
  }
  
  public void setObject(Object o)
  {
    if (!(o instanceof JPanel))
      throw new IllegalArgumentException
                  ("AnyGrid wraps com.inqwell.any.client.swing.JPanel and sub-classes");
    
    grid_ = (JPanel)o;
    
    if (!(grid_.getLayout() instanceof TableLayout))
      throw new AnyRuntimeException("AnyGrid must use a TableLayout, got " + grid_.getLayout().getClass());
  
    super.setObject(o);
  }
  
  public Container getComponent()
  {
    return grid_;
  }
  
  public void add(Component c, TableLayoutConstraints tlc)
  {
    grid_.add(c, tlc);
  }
  
  public int getNumRow()
  {
    return getTableLayout().getNumRow();
  }

  public int getNumColumn()
  {
    return getTableLayout().getNumColumn();
  }
  
  public int getHGap()
  {
    return getTableLayout().getHGap();
  }
  
  public void setHGap(int hGap)
  {
    getTableLayout().setHGap(hGap);
  }
  
  public int getVGap()
  {
    return getTableLayout().getVGap();
  }
  
  public void setVGap(int vGap)
  {
    getTableLayout().setVGap(vGap);
  }
  
  public Map getRows()
  {
    if (yAxis_ == null)
      throw new IllegalStateException("No rows are available");
    
    if (axis_ == null)
      axis_ = new Axis();
    
    axis_.setAxis(yAxis_);
    
    return axis_;
  }

  public Map getColumns()
  {
    if (xAxis_ == null)
      throw new IllegalStateException("No columns are available");
    
    if (axis_ == null)
      axis_ = new Axis();
    
    axis_.setAxis(xAxis_);
    
    return axis_;
  }
  
  public void setColumnNames(Map colNames)
  {
    xAxis_ = colNames;
  }

  public void setRowNames(Map rowNames)
  {
    yAxis_ = rowNames;
  }

  protected Object getPropertyOwner(Any property)
  {
    if (gridProperties__.contains(property))
      return this;
    
    return super.getPropertyOwner(property);
  }
  
  protected TableLayout getTableLayout()
  {
    return (TableLayout)grid_.getLayout();
  }
  
  protected Object getScroller()
  {
    // We don't keep the scroll pane ourselves.
    Container c = grid_.getParent();
    if (c != null)
      c = c.getParent();
    
    if (c instanceof JScrollPane)
      return c;
    
    return null;
  }

  private class Axis extends AbstractMap
  {
    private Map axis_;
          
    private void setAxis(Map axis)
    {
      axis_       = axis;
    }
    
    protected boolean beforeAdd(Any key, Any value) { return true; }
    protected void afterAdd(Any key, Any value) {}
    protected void beforeRemove(Any key) {}
    protected void afterRemove(Any key, Any value) {}
    protected void emptying() {}
    public Iter createIterator () {return DegenerateIter.i__;}
    
    public boolean isEmpty() { return axis_.isEmpty(); }

    public Any get(Any key)
    {
      Any ret = getWithKey(key);
      if (ret == null)
        handleNotExist(key);  // throws

      return ret;
    }

    public Any getIfContains(Any key)
    {
      Any ret = getWithKey(key);
      return ret;
    }

    public boolean contains (Any key)
    {
      return (axis_.contains(key));
    }

    private Any getWithKey(Any key)
    {
      Any a = axis_.getIfContains(key);
      
      if (a != null)
      {
        IntI i = (IntI)a;
        
        TableLayout l = getTableLayout();
        
        Size s;

        if (axis_ == xAxis_)
          s = new Size(l.getColumn(i.getValue()));
        else
          s = new Size(l.getRow(i.getValue()));

        s.setAxis(axis_);
        s.setCoordinate(i.getValue());
        a = s; 
      }

      return a;
    }
  }

  private class Size extends AnyDouble
  {
    private Map axis_;
    private int coordinate_;
    
    private Size(double d)
    {
      super(d);
    }
    
    private void setCoordinate(int coordinate)
    {
      coordinate_ = coordinate;
    }
    
    private void setAxis(Map axis)
    {
      axis_ = axis;
    }
    
    public Any copyFrom(Any a)
    {
      if (!a.equals(this))
      {
        super.copyFrom(a);
        
        TableLayout l = getTableLayout();
        
        if (axis_ == xAxis_)
          l.setColumn(coordinate_, this.getValue());
        else
          l.setRow(coordinate_, this.getValue());
        
        if (validate_ == null)
          validate_ = AnyGrid.this.new Validate();
        
        Globals.getProcessForCurrentThread().getTransaction().addAction(validate_,
                                                              Transaction.AFTER_EVENTS);
      }
      
      return this;
    }
  }
  
  private class Validate extends AbstractFunc
  {
    public Any exec(Any a) throws AnyException
    {
      grid_.invalidate();
      grid_.validate(); 
      return null;
    }
  }
}
