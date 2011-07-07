/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/StyledEditorKit.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.geom.Rectangle2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Container;
import java.awt.FontMetrics;
import javax.swing.SizeRequirements;
import javax.swing.event.DocumentEvent;
import javax.swing.text.View;
import javax.swing.text.BoxView;
import javax.swing.text.FlowView;
import javax.swing.text.LabelView;
import javax.swing.text.Document;
import javax.swing.text.ViewFactory;
import javax.swing.text.Element;
import javax.swing.text.AttributeSet;
import javax.swing.text.AbstractDocument;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.Position;
import javax.swing.text.BadLocationException;
import java.awt.Rectangle;
import java.util.HashSet;

import com.inqwell.any.AbstractAny;
/**
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class StyledEditorKit extends javax.swing.text.StyledEditorKit
{
  private static HashSet elementTypes__;
  
  // ViewFactory types
  public final static String TABLE     = "table";
  public final static String ROW       = "row";
  public final static String CELL      = "cell";
  public final static String PAGEBREAK = "pagebreak";
  
  // Style keys of our own making
  public final static String BorderWidth = "borderWidth";
  public final static String CellPadding = "cellPadding";
  public final static String CellAlign   = "cellAlign";
  public final static String CellWidth   = "cellWidth";
  public final static String CellFill    = "cellFill";
  
  private ViewFactory viewFactory_ = new ExtendedViewFactory();
  private ViewFactory origVf_;
  
  private static float defaultCellAlign__ = 0.5f;
  
  static
  {
    elementTypes__ = new HashSet();
    elementTypes__.add(AbstractDocument.ParagraphElementName);
    elementTypes__.add(AbstractDocument.ContentElementName);
    elementTypes__.add(TABLE);
    elementTypes__.add(ROW);
    elementTypes__.add(CELL);
    elementTypes__.add(PAGEBREAK);
  }
  
  // If there is an absolute character width then convert it
  // to a pixel size via the metrics of our font.  An absolute
  // width of zero is interpreted as fixed size and no
  // attribute present means size to available width.
  static private int getCharacterWidth(AttributeSet s, FontMetrics f)
  {
    //System.out.println(s);
    Object o = s.getAttribute(CellWidth);
    if (o != null)
    {
      Integer i = (Integer)o;
      int ii = i.intValue();
      if (ii == 0)
        return 0;
        
      return ii * f.charWidth('n');
    }
    return -1;
  }

  public StyledEditorKit(ViewFactory vf)
  {
    origVf_ = vf;
  }
  
  public Document createDefaultDocument()
  {
    return new StyledDocument();
  }

  public ViewFactory getViewFactory()
  {
    return viewFactory_;
  }
  
  private class ExtendedViewFactory implements ViewFactory
  {
    public View create(Element elem)
    {
	    String kind = elem.getName();
      //System.out.println("ExtendedViewFactory.create " + kind);
	    if (elementTypes__.contains(kind))
	    {
        if (kind.equals(AbstractDocument.ParagraphElementName))
        {
          return new AdvancedParagraphView(elem);
          //return new ParagraphView(elem);
        }
        else if (kind.equals(AbstractDocument.ContentElementName))
        {
          return new FixableLabelView(elem);
        }
        else if (kind.equals(TABLE))
        {
          //System.out.println("ExtendedViewFactory.create TABLE!");
          return new TableView(elem);
        }
        else if (kind.equals(ROW))
        {
          // If this gets called its a problem because TableRow is a
          // member-inner class of TableView and we don't have the
          // TableView to create it with.  TBD.
          //System.out.println("ExtendedViewFactory.create ROW!");
          throw new IllegalStateException("Can't create row view out of table context");
        }
        else if (kind.equals(CELL))
        {
          //System.out.println("ExtendedViewFactory.create CELL!");
          //return new ParagraphView(elem);
          TableCell tc = new TableCell(elem);
          return tc;
        }
        else if (kind.equals(PAGEBREAK))
        {
          //System.out.println("ExtendedViewFactory.create PAGEBREAK!");
          PageBreakView pb = new PageBreakView(elem);
          return pb;
        }
        else
          return origVf_.create(elem); // Defer to swing's ViewFactory
	    }
	    else
	    {
        // Defer to swing's ViewFactory
        return origVf_.create(elem);
	    }
    }
  }
  
  static private class TableView extends javax.swing.text.TableView
  {
    private View parent_;

    private BasicStroke stroke_;
    private short       cellPadding_;
    private float       cellAlign_;
    private short       borderWidth_;

    TableView (Element elem)
    {
      super(elem);
      setPropertiesFromAttributes();
    }
    
    // Override to protect our children from having their
    // parent view set to null as well.  See View.setParent()
    public void setParent(View parent)
    {
      parent_ = parent;
        
      if (parent != null)
        super.setParent(parent);
    }
    
    public View getParent()
    {
      return parent_;
    }
    
//    public int getBreakWeight(int axis,
//                              float pos,
//                              float len)
//    {
//      if (axis == X_AXIS)
//        return BadBreakWeight;
//      else
//        return super.getBreakWeight(axis, pos, len);
//    }
    
    public void setSize(float width, float height)
    {
      super.setSize(width, height);
      //System.out.println("TableView.setSize          " + hashCode() + " " + width + " " + height);
    }
    
    public float getPreferredSpan(int axis)
    {
      float f = super.getPreferredSpan(axis);
      //if (axis == View.Y_AXIS)
        //System.out.println("TableView.getPreferredSpan " + hashCode() + " " + axis + " " + f);
      return f;
    }

//    public int getResizeWeight(int axis)
//    {
//      System.out.println("TableView.getResizeWeight");
//      if (axis == View.X_AXIS)
//        return 1;
//      return super.getResizeWeight(axis);
//    }

    
    public float getMinimumSpan(int axis)
    {
      float f = super.getMinimumSpan(axis);
      //if (axis == View.Y_AXIS)
        //System.out.println("TableView.getMinimumSpan   " + hashCode() + " " + axis + " " + f);
      return f;
    }
    
    // Does super.getPreferredSpan(X) and super.getMaximumSpan(Y)
    // which has the effect of preventing the table growing into
    // the available space in the X direction.
    // The Y direction is not constrained by this method.  See
    // instead TableRow.calculateMinorAxisRequirements for Y axis
    // handling.
    public float getMaximumSpan(int axis)
    {
      //float f = super.getMaximumSpan(axis);
      float f ;
      if (axis == View.X_AXIS)
        f = super.getPreferredSpan(axis);  // No stretch.  Could make configurable...
      else
        f = super.getMaximumSpan(axis);
        
      //if (axis == View.Y_AXIS)
      //System.out.println("TableView.getMaximumSpan   " + hashCode() + " " + axis + " " + f);
      return f;
    }

/*
    protected void layoutMinorAxis(int   targetSpan,
                                   int   axis,
                                   int[] offsets,
                                   int[] spans)
    {
//      Container c = getContainer();
//      int w;
//      if (c != null && (w = c.getWidth()) != 0)
//        targetSpan = w;

      //AbstractAny.stackTrace();
      System.out.println("1 TableView.layoutMinorAxis");
      System.out.println("1 axis " + axis);
      System.out.println("1 targetSpan " + targetSpan);
      System.out.print("1 offsets");
      for (int i = 0; i < offsets.length; i++)
        System.out.print(" " + offsets[i]);
      System.out.println("");
      System.out.print("1 spans");
      for (int i = 0; i < spans.length; i++)
        System.out.print(" " + spans[i]);
      System.out.println("");
        
      
      super.layoutMinorAxis(targetSpan, axis, offsets, spans);
      
      System.out.println("2 TableView.layoutMinorAxis");
      System.out.println("2 targetSpan " + targetSpan);
      System.out.print("2 offsets");
      for (int i = 0; i < offsets.length; i++)
        System.out.print(" " + offsets[i]);
      System.out.println("");
      System.out.print("2 spans");
      for (int i = 0; i < spans.length; i++)
        System.out.print(" " + spans[i]);
      System.out.println("");
        
      System.out.println("*******************");
    }
    
    protected void baselineLayout(int targetSpan,
                                  int axis,
                                  int[] offsets,
                                  int[] spans)
    {
      //Container c = getContainer();
      //int w;
      //if (c != null && (w = c.getWidth()) != 0)
        //targetSpan = w;

      //AbstractAny.stackTrace();
      System.out.println("1 TableView.baselineLayout");
      System.out.println("1 axis " + axis);
      System.out.println("1 targetSpan " + targetSpan);
      System.out.print("1 offsets");
      for (int i = 0; i < offsets.length; i++)
        System.out.print(" " + offsets[i]);
      System.out.println("");
      System.out.print("1 spans");
      for (int i = 0; i < spans.length; i++)
        System.out.print(" " + spans[i]);
      System.out.println("");
        
      
      super.baselineLayout(targetSpan, axis, offsets, spans);
      
      System.out.println("2 TableView.baselineLayout");
      System.out.println("2 targetSpan " + targetSpan);
      System.out.print("2 offsets");
      for (int i = 0; i < offsets.length; i++)
        System.out.print(" " + offsets[i]);
      System.out.println("");
      System.out.print("2 spans");
      for (int i = 0; i < spans.length; i++)
        System.out.print(" " + spans[i]);
      System.out.println("");
        
      System.out.println("*******************");
    }
    
    protected void layoutColumns(int                targetSpan,
                                 int[]              offsets,
                                 int[]              spans, 
                                 SizeRequirements[] reqs)
    {
//      Container c = getContainer();
//      int w;
//      if (c != null && (w = c.getWidth()) != 0)
//        targetSpan = w;

      //AbstractAny.stackTrace();
      System.out.println("1 TableView.layoutColumns");
      System.out.println("1 targetSpan " + targetSpan);
      System.out.print("1 offsets");
      for (int i = 0; i < offsets.length; i++)
        System.out.print(" " + offsets[i]);
      System.out.println("");
      System.out.print("1 spans");
      for (int i = 0; i < spans.length; i++)
        System.out.print(" " + spans[i]);
      System.out.println("");
        
      System.out.println("1 reqs " + reqs);
      
      super.layoutColumns(targetSpan, offsets, spans, reqs);
      
      System.out.println("2 TableView.layoutColumns");
      System.out.println("2 targetSpan " + targetSpan);
      System.out.print("2 offsets");
      for (int i = 0; i < offsets.length; i++)
        System.out.print(" " + offsets[i]);
      System.out.println("");
      System.out.print("2 spans");
      for (int i = 0; i < spans.length; i++)
        System.out.print(" " + spans[i]);
      System.out.println("");
        
      System.out.println("2 reqs " + reqs);
      System.out.println("*******************");
      
    }
*/
    
    protected void loadChildren(ViewFactory f)
    {
      if (f == null)
      {
        // No factory. This most likely indicates the parent view
        // has changed out from under us, bail!
        System.out.println("No factory!!");
        return;
      }
      
      //System.out.println("TableView.loadChildren()");
      Element e = getElement();
      int n = e.getElementCount();
      if (n > 0)
      {
        View[] added = new View[n];
        for (int i = 0; i < n; i++)
        {
          TableRow r = new TableRow(e.getElement(i));
          r.setCellPadding(cellPadding_);
          r.setBorderWidth(borderWidth_);
          r.setCellAlign(cellAlign_);
          added[i] = r;
          //System.out.println("row");
        }
        replace(0, getViewCount(), added);
      }
    }
    
    // This is the same as the base class implememtation
    // with the exception that it dows not use the ViewFactory.
    // TableRow objects can only be created in the context of
    // a TableView.  See TableView.loadChildren() also.
    protected boolean updateChildren(DocumentEvent.ElementChange ec,
                                     DocumentEvent e,
                                     ViewFactory f)
    {
      //System.out.println("updateChildren " + e);
      //System.out.println("updateChildren " + ec);
      
      //System.out.println("added " + dumpElemArray(ec.getChildrenAdded()));
      //System.out.println("removed " + dumpElemArray(ec.getChildrenRemoved()));
      //System.out.println("element " + ec.getElement() + ec.getElement().hashCode());
      //System.out.println("index " + ec.getIndex());

      Element[] removedElems = ec.getChildrenRemoved();
      Element[] addedElems   = ec.getChildrenAdded();
      View[] added = null;
      if (addedElems != null)
      {
        added = new View[addedElems.length];
        for (int i = 0; i < addedElems.length; i++)
        {
          TableRow r = new TableRow(addedElems[i]);
          r.setCellPadding(cellPadding_);
          r.setBorderWidth(borderWidth_);
          r.setCellAlign(cellAlign_);
          added[i] = r;
        }
      }
      int nremoved = 0;
      int index = ec.getIndex();
      if (removedElems != null)
      {
        nremoved = removedElems.length;
      }
      replace(index, nremoved, added);
      return true;
    }
    
    private String dumpElemArray(Element e[])
    {
      StringBuffer sb = new StringBuffer();
      
      for (int i = 0; i < e.length; i++)
      {
        sb.append(e[i].toString());
        sb.append(" " + e[i].hashCode() + "\n");
      }
      return sb.toString();
    }
    
    
    protected void setPropertiesFromAttributes()
    {
      AttributeSet attr = getAttributes();
      if (attr != null)
      {
        setBorderWidth(attr);
        setCellPadding(attr);
      }
      setCellAlign(attr);
    }
    
    private void setBorderWidth(AttributeSet attr)
    {
      Object o = null;
      float f = 1.0f; // default border thickness of 1
      
      if ((o = attr.getAttribute(BorderWidth)) != null)
      {
        Float F = (Float)o;
        f = F.floatValue();
      }
 
      if (f != 0)
        stroke_ = new BasicStroke(f);
      else
        stroke_ = null;
      
      borderWidth_ = (short)f;
    }

    private void setCellPadding(AttributeSet attr)
    {
      Object o = null;
      short  s = 1;
      
      if ((o = attr.getAttribute(CellPadding)) != null)
      {
        Short S = (Short)o;
        s = S.shortValue();
      }
      
      cellPadding_ = s;
    }

    private void setCellAlign(AttributeSet attr)
    {
      Object o = null;
      float  f = defaultCellAlign__;
      
      if (attr != null &&
          (o = attr.getAttribute(CellAlign)) != null)
      {
        Float F = (Float)o;
        f = F.shortValue();
      }
      
      cellAlign_ = f;
    }

    public class TableRow extends javax.swing.text.TableView.TableRow
    {
      private short       cellPadding_;
      private short       borderWidth_;
      private float       cellAlign_;
      private BasicStroke stroke_;
      
      TableRow(Element elem)
      {
        super(elem);
      }

      public void paint(Graphics g,
                        Shape    allocation)
      {
        drawCellBorder(g, allocation);
        super.paint(g, allocation);
        //drawCellFill(g, allocation);
        //System.out.println("TableRow.paint");
      }
    
      public void setSize(float width, float height)
      {
        super.setSize(width, height);
        //System.out.println("TableRow.setSize           " + hashCode() + " " + width + " " + height);
      }
      
      public float getPreferredSpan(int axis)
      {
        float f = super.getPreferredSpan(axis);
        //if (axis == View.Y_AXIS)
          //System.out.println("TableRow.getPreferredSpan  " + hashCode() + " " + axis + " " + f);
        return f;
      }
      
      public float getMinimumSpan(int axis)
      {
        float f = super.getMinimumSpan(axis);
        //if (axis == View.Y_AXIS)
          //System.out.println("TableRow.getMinimumSpan    " + hashCode() + " " + axis + " " + f);
        return f;
      }
      
      public float getMaximumSpan(int axis)
      {
        float f;
        if (axis == View.Y_AXIS)
          f = super.getPreferredSpan(axis);
        else
          f = super.getMaximumSpan(axis);
        //System.out.println("TableRow.getMaximumSpan    " + hashCode() + " " + axis + " " + f);
        return f;
      }

/*      
      protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans)
      {
//        Container c = getContainer();
//        int w;
//        if (c != null && (w = c.getWidth()) != 0)
//          targetSpan = w;
        System.out.println("1 TableRow.layoutMajorAxis");
        System.out.println("1 axis " + axis);
        System.out.println("1 targetSpan " + targetSpan);
        System.out.print("1 offsets");
        for (int i = 0; i < offsets.length; i++)
          System.out.print(" " + offsets[i]);
        System.out.println("");
        System.out.print("1 spans");
        for (int i = 0; i < spans.length; i++)
          System.out.print(" " + spans[i]);
        System.out.println("");
          
        
        super.layoutMajorAxis(targetSpan, axis, offsets, spans);
        
        System.out.println("2 TableRow.layoutMajorAxis");
        System.out.println("2 targetSpan " + targetSpan);
        System.out.print("2 offsets");
        for (int i = 0; i < offsets.length; i++)
          System.out.print(" " + offsets[i]);
        System.out.println("");
        System.out.print("2 spans");
        for (int i = 0; i < spans.length; i++)
          System.out.print(" " + spans[i]);
        System.out.println("");
          
        System.out.println("*******************");
      }
*/
      
      //public int getResizeWeight(int axis)
      //{
      //  System.out.println("TableRow.getResizeWeight");
      //  return 0;
      //}

/*  
      protected void baselineLayout(int targetSpan,
                                    int axis,
                                    int[] offsets,
                                    int[] spans)
      {
        //Container c = getContainer();
        //int w;
        //if (c != null && (w = c.getWidth()) != 0)
          //targetSpan = w;
  
        //AbstractAny.stackTrace();
        System.out.println("1 TableRow.baselineLayout");
        System.out.println("1 axis " + axis);
        System.out.println("1 targetSpan " + targetSpan);
        System.out.print("1 offsets");
        for (int i = 0; i < offsets.length; i++)
          System.out.print(" " + offsets[i]);
        System.out.println("");
        System.out.print("1 spans");
        for (int i = 0; i < spans.length; i++)
          System.out.print(" " + spans[i]);
        System.out.println("");
          
        
        super.baselineLayout(targetSpan, axis, offsets, spans);
        
        System.out.println("2 TableRow.baselineLayout");
        System.out.println("2 targetSpan " + targetSpan);
        System.out.print("2 offsets");
        for (int i = 0; i < offsets.length; i++)
          System.out.print(" " + offsets[i]);
        System.out.println("");
        System.out.print("2 spans");
        for (int i = 0; i < spans.length; i++)
          System.out.print(" " + spans[i]);
        System.out.println("");
          
        System.out.println("*******************");
      }
*/
      
      void setCellAlign(float cellAlign)
      {
        cellAlign_ = cellAlign;
      }
      
      void setCellPadding(short padding)
      {
        cellPadding_ = padding;
      }
      
      void setBorderWidth(short borderWidth)
      {
        borderWidth_ = borderWidth;
        if ((borderWidth_ != TableView.this.borderWidth_) &&
            (borderWidth_ != 0))
        {
          // per row bordering style is different to parent
          // table.  Create local stroke object
          float f = borderWidth_;
          stroke_ = new BasicStroke(f);
        }
        else
          stroke_ = null;
      }
      
      short getCellPadding()
      {
        return cellPadding_;
      }
      
      short getBorderWidth()
      {
        return borderWidth_;
      }
      
      float getCellAlign()
      {
        return cellAlign_;
      }
      
      // The only difference between this method and the base class
      // is that the max size is initialised to zero.  Then the
      // row is only as tall as it needs to be.  Otherwise it
      // will expand (ultimately) to the size of the container.
      // This has the effect of making the table only as tall as
      // it needs to be.  See also TableView.getMaximumSpan(int axis)
//      protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r)
//      {
//        int min   = 0;
//        long pref = 0;
//        int max   = 0;
//        //int max   = Integer.MAX_VALUE;
//
//        int n = getViewCount();
//        for (int i = 0; i < n; i++)
//        {
//          View v = getView(i);
//          min = Math.max((int) v.getMinimumSpan(axis), min);
//          pref = Math.max((int) v.getPreferredSpan(axis), pref);
//          max = Math.max((int) v.getMaximumSpan(axis), max);
//        }
//
//        if (r == null)
//        {
//          r = new SizeRequirements();
//          r.alignment = 0.5f;
//        }
//        r.preferred = (int) pref;
//        r.minimum = min;
//        r.maximum = max;
//        return r;
//      }

      protected void drawCellFill(Graphics g,
                                  Shape    allocation)
      {
        AttributeSet attr = getAttributes();
        Color bg = (Color)attr.getAttribute(CellFill);
        if (bg != null)
        {
          g.setColor(bg);
          Rectangle r = allocation.getBounds();
          g.fillRect(r.x, r.y, r.width, r.height);
        }
        
      }
      
      protected void drawCellBorder(Graphics g,
                                    Shape    allocation)
      {
//        if (borderWidth_ == 0)
//          return;
          
        // Set up the drawing
        Graphics2D g2 = (Graphics2D)g;
        BasicStroke stroke = (this.stroke_ != null) ? this.stroke_
                                                    : TableView.this.stroke_;
        // Get the overall bounding rectangle of the row
        Rectangle2D rowBounds = allocation.getBounds2D();
        
        int children = getViewCount();
        
        //System.out.println("# cells " + children);
        //System.out.println("alloc " + rowBounds);
        
        // draw each cell border/fill
        for (int i = 0; i < children; i++)
        {
          //int width = getSpan(View.X_AXIS, i);
          Shape s = getChildAllocation(i, allocation);
          //System.out.println("child " + s);
          Rectangle2D r = s.getBounds2D();
          r.setRect(r.getX(),
                    Math.min(rowBounds.getY(), r.getY()),
                    r.getWidth(),
                    Math.max(rowBounds.getHeight(), r.getHeight()));
          //r.setRect(r.getX(), r.getY(), width, r.getHeight());
          //System.out.println("draw " + r);
          
          AttributeSet attr = getView(i).getAttributes();
          Color bg = (Color)attr.getAttribute(CellFill);
          if (bg != null)
          {
            g.setColor(bg);
            g2.fill(r);
          }
          
          if (stroke != null)
          {
            g2.setStroke(stroke);
            g2.setColor(Color.BLACK);
            g2.draw(r);
          }
          
          // Move the rectangle origin on to the next cell starting point
          //r.setRect(r.getX() + width, r.getY(), width, r.getHeight());
        }
      }
    }
  }
  
  static private class TableCell extends BoxView
  {
    private short borderWidth_  = 0;
    private short cellPadding_  = 0;
    private float cellAlign_    = 0;
    
    public TableCell(Element elem)
    {
      super(elem, View.Y_AXIS);
    }
    
    public void setBorderWidth(short width)
    {
      if (width < 0)
        return;
        
      short oldBw  = borderWidth_;
      borderWidth_ = width;
      setInsets((short)(getTopInset()    - oldBw - cellPadding_),
                (short)(getLeftInset()   - oldBw - cellPadding_),
                (short)(getBottomInset() - oldBw - cellPadding_),
                (short)(getRightInset()  - oldBw - cellPadding_));
                
    }
    
    void setCellPadding(short padding)
    {
      short oldCp  = cellPadding_;
      cellPadding_ = padding;
      setInsets((short)(getTopInset()    - oldCp - borderWidth_),
                (short)(getLeftInset()   - oldCp - borderWidth_),
                (short)(getBottomInset() - oldCp - borderWidth_),
                (short)(getRightInset()  - oldCp - borderWidth_));
                
    }
    
    void setCellAlign(float cellAlign)
    {
      cellAlign_ = cellAlign;
    }
    
    // override to pick up border and padding sizes for insets
    public void setParent(View parent)
    {
      if (parent != null)
      {
        TableView.TableRow r = (TableView.TableRow)parent;
        setCellPadding(r.getCellPadding());
        setBorderWidth(r.getBorderWidth());
        setCellAlign(r.getCellAlign());
      }
      super.setParent(parent);
    }

//    public int getResizeWeight(int axis)
//    {
//      System.out.println("TableCell.getResizeWeight");
//      return 1;
//    }

    // Not necessarily used because the width attribute is not
    // normally on the cell elements
    public float getPreferredSpan(int axis)
    {
      float offset = 2 * borderWidth_ + 2 * cellPadding_;
      
      if (axis == View.Y_AXIS)
      {
      //System.out.println("TableCell.getPreferredSpan  " + hashCode() + " " + axis + " " + f);
        return super.getPreferredSpan(axis) + offset;
      }
      
      // Check if a width has been specified
      int characterWidth = getCharacterWidth(getAttributes(),
                                             getGraphics().getFontMetrics());
      
      if (characterWidth <= 0)
        return super.getPreferredSpan(axis) + offset;
      
      //System.out.println("TableCell.getPreferredSpan  " + hashCode() + " " + axis + " " + characterWidth + offset);
      return characterWidth + offset;
      
      //float f = super.getPreferredSpan(axis) + offset;
      //System.out.println("TableCell.getPreferredSpan  " + hashCode() + " " + axis + " " + f);
      //return f;

    }
    
    // See above
    public float getMinimumSpan(int axis)
    {
      float offset = 2 * borderWidth_ + 2 * cellPadding_;
      
      if (axis == View.Y_AXIS)
      {
        return super.getMinimumSpan(axis) + offset;
      }
      
      // Check if a width has been specified
      int characterWidth = getCharacterWidth(getAttributes(),
                                             getGraphics().getFontMetrics());
      
      if (characterWidth < 0)
        return super.getMinimumSpan(axis) + offset;
      
      if (characterWidth == 0)
        return super.getPreferredSpan(axis) + offset;

      //System.out.println("TableCell.getMinimumSpan  " + hashCode() + " " + axis + " " + characterWidth + offset);
      return characterWidth + offset;
    }
    
    // See above
    public float getMaximumSpan(int axis)
    {
      float offset = 2 * borderWidth_ + 2 * cellPadding_;
      
      if (axis == View.Y_AXIS)
      {
        return super.getMaximumSpan(axis) + offset;
      }
      
      // Check if a width has been specified
      int characterWidth = getCharacterWidth(getAttributes(),
                                             getGraphics().getFontMetrics());
      
      if (characterWidth < 0)
        return super.getMaximumSpan(axis) + offset;
      
      if (characterWidth == 0)
        return super.getPreferredSpan(axis) + offset;

      //System.out.println("TableCell.getMaximumSpan  " + hashCode() + " " + axis + " " + characterWidth + offset);

      return characterWidth + offset;
      //float f = super.getMaximumSpan(axis) + 2 * borderWidth_ + 2 * cellPadding_;
      //if (axis == View.Y_AXIS)
        //System.out.println("TableCell.getMaximumSpan   " + hashCode() + " " + axis + " " + f);
      //return f;
    }

    public float getAlignment(int axis)
    {
      switch (axis)
      {
        case Y_AXIS: return cellAlign_;
        case X_AXIS: return super.getAlignment(axis);
      }
      return super.getAlignment(axis);
    }
    
    //public int getResizeWeight(int axis)
    //{
    //  System.out.println("TableCell.getResizeWeight");
    //  return 0;
    //}
  
    public void setSize(float width, float height)
    {
      super.setSize(width, height);
      //System.out.println("TableCell.setSize          " + hashCode() + " " + width + " " + height);
    }

    protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r)
    {
      SizeRequirements s = super.calculateMajorAxisRequirements(axis, r);
      //System.out.println("TableCell.calculateMajorAxisRequirements   " + hashCode() + axis + " " + " " + s);
      return s;
    }

    protected void setInsets(short top,
                             short left,
                             short bottom,
                             short right)
    {
      short ntop    = (short)(top    + borderWidth_ + cellPadding_);
      short nleft   = (short)(left   + borderWidth_ + cellPadding_);
      short nbottom = (short)(bottom + borderWidth_ + cellPadding_);
      short nright  = (short)(right  + borderWidth_ + cellPadding_);
      
      super.setInsets(ntop,
                      nleft,
                      nbottom,
                      nright);
    }

    protected void setParagraphInsets(AttributeSet attr)
    {
      super.setParagraphInsets(attr);
      setInsets(getTopInset(),
                getLeftInset(),
                getBottomInset(),
                getRightInset());
    }
    
    public void paint(Graphics g,
                      Shape    allocation)
    {
//      AttributeSet attr = getAttributes();
//      Color bg = (Color)attr.getAttribute(CellFill);
//      if (bg != null)
//      {
//        g.setColor(bg);
//        Rectangle r = allocation.getBounds();
//        g.fillRect(r.x, r.y, r.width, r.height);
//      }
      super.paint(g, allocation);
    }
  }
  
  // A LabelView that respects the cellWidth attribute.  If present
  // and equal to zero this view will not break.  In addition, if
  // the cell width is > 0 the value is taken as the character
  // width and reported by getPreferredSpan.  Supports fixed-size
  // cells in tables.
  static private class FixableLabelView extends LabelView
  {
    FixableLabelView(Element elem)
    {
      super(elem);
    }
    
    public float getPreferredSpan(int axis)
    {
      if (axis == View.Y_AXIS)
        return super.getPreferredSpan(axis);
      
      Graphics g = getGraphics();
      if (g != null)
      {
        int characterWidth = getCharacterWidth(getAttributes(),
                                               getGraphics().getFontMetrics());
        //System.out.println("11111111 width is " + characterWidth);
        if (characterWidth <= 0)
          return super.getPreferredSpan(axis);
        
        return characterWidth;
      }
      
      return super.getPreferredSpan(axis);
    }

    public int getBreakWeight(int axis,
                              float pos,
                              float len)
    {
      if (axis == View.Y_AXIS)
        return super.getBreakWeight(axis, pos, len);
        
      Graphics g = getGraphics();
      if (g != null)
      {
        int characterWidth = getCharacterWidth(getAttributes(),
                                               getGraphics().getFontMetrics());
  
        //System.out.println("2222222 width is " + characterWidth);
        if (characterWidth < 0)
          return super.getBreakWeight(axis, pos, len);
          
        //System.out.println("BadBreakWeight");
        
        return BadBreakWeight;
      }
      return super.getBreakWeight(axis, pos, len);
    }
  }
  
  // A view that renders to a dashed line on screen graphics
  // and nothing at all to a printer.  Causes a page throw when
  // printed.s
  static private class PageBreakView extends View
  {
    static private Stroke dash__;
    
    static
    {
      float dash[] = { 1.0f, 3.0f };
      dash__ = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash, 0);
    }
    
    PageBreakView(Element elem)
    {
      super(elem);
    }

    public float getPreferredSpan(int axis)
    {
      if (axis == View.X_AXIS)
      {
        Container c = getContainer();
        if (c != null)
          return c.getWidth();
        
        return 0;
      }
      else
        return 1; // 1 pixel high for rendering on the screen
    }

    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException
    {
      int p0 = getStartOffset();
      int p1 = getEndOffset();
      if ((pos >= p0) && (pos <= p1))
      {
        Rectangle r = a.getBounds();
        if (pos == p1)
        {
          r.x += r.width;
        }
        r.width = 0;
        return r;
      }
      throw new BadLocationException(pos + " not in range " + p0 + "," + p1, pos);
    }
    
    public void paint(Graphics g, Shape allocation)
    {
      // Only paint if we are not rendering to a printer
      Graphics2D g2 = (Graphics2D)g;
      if (g2.getDeviceConfiguration().getDevice().getType() != GraphicsDevice.TYPE_PRINTER)
      {
  
        // Get the overall bounding rectangle of the row
        Rectangle r = allocation.getBounds();
  
        // Set up the drawing
        g2.setStroke(dash__);
        g2.setColor(Color.BLACK);
  
        g2.drawLine(r.x, r.y, r.x+r.width, r.y);
      }
    }
    public int viewToModel(float x, float y, Shape a, Position.Bias[] bias)
    {
      Rectangle alloc = (Rectangle) a;
      if (x < alloc.x + alloc.width)
      {
        bias[0] = Position.Bias.Forward;
        return getStartOffset();
      }
      bias[0] = Position.Bias.Backward;
      return getEndOffset();
    }
  }
  
  // StanislavL's code from the forums to fix the JavaSoft stuff!
  
  static private class AdvancedParagraphView extends ParagraphView {
      public AdvancedParagraphView(Element elem) {
          super(elem);
          strategy = new AdvancedFlowStrategy();
      }

      protected View createRow()
      {

          Element elem = getElement();

          return new AdvancedRow(elem);
      }

      protected static int getSpaceCount(String content) {

          int result = 0;
          int index = content.indexOf(' ');

          while (index >= 0) {
              result++;
              index = content.indexOf(' ', index + 1);
          }

          return result;
      }

      protected static int[] getSpaceIndexes(String content, int shift) {

          int cnt = getSpaceCount(content);
          int[] result = new int[cnt];
          int counter = 0;
          int index = content.indexOf(' ');

          while (index >= 0) {
              result[counter] = index + shift;
              counter++;
              index = content.indexOf(' ', index + 1);
          }

          return result;
      }

      public int getFlowSpan(int index) {

          int span = super.getFlowSpan(index);

          if (index == 0) {

              int firstLineIdent = (int)
                  StyleConstants.getFirstLineIndent(this.getAttributes());
              span -= firstLineIdent;
          }

          return span;
      }

      protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets,
          int[] spans) {
          super.layoutMinorAxis(targetSpan, axis, offsets, spans);

          int firstLineIdent = (int)
              StyleConstants.getFirstLineIndent(this.getAttributes());
          offsets[0] += firstLineIdent;
      }

      protected static boolean isContainSpace(View v) {

          int startOffset = v.getStartOffset();
          int len = v.getEndOffset() - startOffset;

          try {

              String text = v.getDocument().getText(startOffset, len);

              if (text.indexOf(' ') >= 0)

                  return true;
              else

                  return false;
          } catch (Exception ex) {

              return false;
          }
      }

      static private class AdvancedFlowStrategy extends FlowStrategy {
          public void layout(FlowView fv) {
              super.layout(fv);

              AttributeSet attr = fv.getElement().getAttributes();
              float lineSpacing = StyleConstants.getLineSpacing(attr);
              boolean justifiedAlignment = (StyleConstants.getAlignment(attr) ==
                      StyleConstants.ALIGN_JUSTIFIED);

              if (!(justifiedAlignment || (lineSpacing > 1))) {

                  return;
              }

              int cnt = fv.getViewCount();

              for (int i = 0; i < (cnt - 1); i++) {

                  AdvancedRow row = (AdvancedRow) fv.getView(i);

                  if (lineSpacing > 1) {

                      float height = row.getMinimumSpan(View.Y_AXIS);
                      float addition = (height * lineSpacing) - height;

                      if (addition > 0) {
                          row.setInsets(row.getTopInset(), row.getLeftInset(),
                              (short) addition, row.getRightInset());
                      }
                  }

                  if (justifiedAlignment) {
                      restructureRow(row, i);
                      row.setRowNumber(i + 1);
                  }
              }
          }

          protected void restructureRow(View row, int rowNum) {

              int rowStartOffset = row.getStartOffset();
              int rowEndOffset = row.getEndOffset();
              String rowContent = "";

              try {
                  rowContent = row.getDocument().getText(rowStartOffset,
                          rowEndOffset - rowStartOffset);

                  if (rowNum == 0) {

                      int index = 0;

                      while (rowContent.charAt(0) == ' ') {
                          rowContent = rowContent.substring(1);

                          if (rowContent.length() == 0)

                              break;
                      }
                  }
              } catch (Exception e) {
                  e.printStackTrace();
              }

              int rowSpaceCount = getSpaceCount(rowContent);

              if (rowSpaceCount < 1)

                  return;

              int[] rowSpaceIndexes = getSpaceIndexes(rowContent,
                      row.getStartOffset());
              int currentSpaceIndex = 0;

              for (int i = 0; i < row.getViewCount(); i++) {

                  View child = row.getView(i);

                  if ((child.getStartOffset() <
                              rowSpaceIndexes[currentSpaceIndex]) &&
                          (child.getEndOffset() >
                              rowSpaceIndexes[currentSpaceIndex])) {

  //split view
                      View first = child.createFragment(child.getStartOffset(),
                              rowSpaceIndexes[currentSpaceIndex]);
                      View second = child.createFragment(
                              rowSpaceIndexes[currentSpaceIndex],
                              child.getEndOffset());
                      View[] repl = new View[2];
                      repl[0] = first;
                      repl[1] = second;
                      row.replace(i, 1, repl);
                      currentSpaceIndex++;

                      if (currentSpaceIndex >= rowSpaceIndexes.length)

                          break;
                  }
              }

              int childCnt = row.getViewCount();
          }
      }

      private class AdvancedRow extends BoxView {

          private int rowNumber = 0;

          AdvancedRow(Element elem) {
              super(elem, View.X_AXIS);
          }

          protected void loadChildren(ViewFactory f) {
          }

          public AttributeSet getAttributes() {

              View p = getParent();

              return (p != null) ? p.getAttributes() : null;
          }

          public float getAlignment(int axis) {

              if (axis == View.X_AXIS) {

                  AttributeSet attr = getAttributes();
                  int justification = StyleConstants.getAlignment(attr);

                  switch (justification) {

                  case StyleConstants.ALIGN_LEFT:
                  case StyleConstants.ALIGN_JUSTIFIED:
                      return 0;

                  case StyleConstants.ALIGN_RIGHT:
                      return 1;

                  case StyleConstants.ALIGN_CENTER:
                      return 0.5f;
                  }
              }

              return super.getAlignment(axis);
          }

          public Shape modelToView(int pos, Shape a, Position.Bias b)
              throws BadLocationException {

              Rectangle r = a.getBounds();
              View v = getViewAtPosition(pos, r);

              if ((v != null) && (!v.getElement().isLeaf())) { // Don't adjust the height if the view represents a branch.

                  return super.modelToView(pos, a, b);
              }

              r = a.getBounds();

              int height = r.height;
              int y = r.y;
              Shape loc = super.modelToView(pos, a, b);
              r = loc.getBounds();
              r.height = height;
              r.y = y;

              return r;
          }

          public int getStartOffset() {

              int offs = Integer.MAX_VALUE;
              int n = getViewCount();

              for (int i = 0; i < n; i++) {

                  View v = getView(i);
                  offs = Math.min(offs, v.getStartOffset());
              }

              return offs;
          }

          public int getEndOffset() {

              int offs = 0;
              int n = getViewCount();

              for (int i = 0; i < n; i++) {

                  View v = getView(i);
                  offs = Math.max(offs, v.getEndOffset());
              }

              return offs;
          }

          protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets,
              int[] spans) {
              baselineLayout(targetSpan, axis, offsets, spans);
          }

          protected SizeRequirements calculateMinorAxisRequirements(int axis,
              SizeRequirements r) {

              return baselineRequirements(axis, r);
          }

          protected int getViewIndexAtPosition(int pos) { // This is expensive, but are views are not necessarily layed
  // out in model order.

              if ((pos < getStartOffset()) || (pos >= getEndOffset()))

                  return -1;

              for (int counter = getViewCount() - 1; counter >= 0; counter--) {

                  View v = getView(counter);

                  if ((pos >= v.getStartOffset()) && (pos < v.getEndOffset())) {

                      return counter;
                  }
              }

              return -1;
          }

          public short getTopInset() {

              return super.getTopInset();
          }

          public short getLeftInset() {

              return super.getLeftInset();
          }

          public short getRightInset() {

              return super.getRightInset();
          }

          public void setInsets(short topInset, short leftInset,
              short bottomInset, short rightInset) {
              super.setInsets(topInset, leftInset, bottomInset, rightInset);
          }

          protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets,
              int[] spans) {
              super.layoutMajorAxis(targetSpan, axis, offsets, spans);

              AttributeSet attr = getAttributes();

              if ((StyleConstants.getAlignment(attr) !=
                          StyleConstants.ALIGN_JUSTIFIED) &&
                      (axis != View.X_AXIS)) {

                  return;
              }

              int cnt = offsets.length;
              int span = 0;

              for (int i = 0; i < cnt; i++) {
                  span += spans[i];
              }

              if (getRowNumber() == 0)

                  return;

              int startOffset = getStartOffset();
              int len = getEndOffset() - startOffset;
              String context = "";

              try {
                  context = getElement().getDocument().getText(startOffset, len);
              } catch (Exception e) {
                  e.printStackTrace();
              }

              int spaceCount = getSpaceCount(context) - 1;
              int pixelsToAdd = targetSpan - span;

              if (this.getRowNumber() == 1) {

                  int firstLineIndent = (int)
                      StyleConstants.getFirstLineIndent(getAttributes());
                  pixelsToAdd -= firstLineIndent;
              }

              int[] spaces = getSpaces(pixelsToAdd, spaceCount);
              int j = 0;
              int shift = 0;

              for (int i = 1; i < cnt; i++) {

                  LabelView v = (LabelView) getView(i);
                  offsets[i] += shift;

                  if ((isContainSpace(v)) && (i != (cnt - 1))) {
                      offsets[i] += spaces[j];
                      spans[i - 1] += spaces[j];
                      shift += spaces[j];
                      j++;
                  }
              }
          }

          protected int[] getSpaces(int space, int cnt) {

              int[] result = new int[cnt];

              if (cnt == 0)

                  return result;

              int base = space / cnt;
              int rst = space % cnt;

              for (int i = 0; i < cnt; i++) {
                  result[i] = base;

                  if (rst > 0) {
                      result[i]++;
                      rst--;
                  }
              }

              return result;
          }

          public float getMinimumSpan(int axis) {

              if (axis == View.X_AXIS) {

                  AttributeSet attr = getAttributes();

                  if (StyleConstants.getAlignment(attr) !=
                          StyleConstants.ALIGN_JUSTIFIED) {

                      return super.getMinimumSpan(axis);
                  } else {

                      return this.getParent().getMinimumSpan(axis);
                  }
              } else {

                  return super.getMinimumSpan(axis);
              }
          }

          public float getMaximumSpan(int axis) {

              if (axis == View.X_AXIS) {

                  AttributeSet attr = getAttributes();

                  if (StyleConstants.getAlignment(attr) !=
                          StyleConstants.ALIGN_JUSTIFIED) {

                      return super.getMaximumSpan(axis);
                  } else {

                      return this.getParent().getMaximumSpan(axis);
                  }
              } else {

                  return super.getMaximumSpan(axis);
              }
          }

          public float getPreferredSpan(int axis) {

              if (axis == View.X_AXIS) {

                  AttributeSet attr = getAttributes();

                  if (StyleConstants.getAlignment(attr) !=
                          StyleConstants.ALIGN_JUSTIFIED) {

                      return super.getPreferredSpan(axis);
                  } else {

                      return this.getParent().getPreferredSpan(axis);
                  }
              } else {

                  return super.getPreferredSpan(axis);
              }
          }

          public void setRowNumber(int value) {
              rowNumber = value;
          }

          public int getRowNumber() {

              return rowNumber;
          }
      }
  }
}
