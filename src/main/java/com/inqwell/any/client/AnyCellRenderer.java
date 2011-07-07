/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyCellRenderer.java $
 * $Author: sanderst $
 * $Revision: 1.9 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.BooleanI;
import com.inqwell.any.Call;
import com.inqwell.any.ConstString;
import com.inqwell.any.Descriptor;
import com.inqwell.any.EventConstants;
import com.inqwell.any.Globals;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.PropertyAccessMap;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Transaction;
import com.inqwell.any.client.swing.TableModel;

public class AnyCellRenderer extends    PropertyAccessMap
                             implements ListCellRenderer,
                                        TableCellRenderer
{
  public static ConstString null__     = new ConstString("");
  public static Border      noFocusBorder = new EmptyBorder(1, 1, 1, 1); 
  public static Border      editBorder    = LineBorder.createBlackLineBorder(); 

  private AnyComponent  renderer_;
  
  // Although an AnyComponent does hold a RenderInfo we cannot reuse this
  // in the context of cell rendering because then we could not share
  // renderers across table columns or indeed different components.
  // Keep it here instead.
  private RenderInfo    renderInfo_;

  private Map           propertyMap_;

  // A style function can be supplied via property access
  private Call          styleFunc_;
  
  // The component to set the rendered value into. May be the same
  // as renderer_
  private AnyComponent  setValueToComponent_;
  
  static private Map    fnArgs__;
  static private AnyInt row__;
  static private AnyInt col__;
  static private AnyBoolean isLeaf__;
  static private AnyBoolean expanded__;
  static private AnyBoolean mouseCell__;
  
  static public Any callCellFunc(Call         cellFunc,
                                 AnyComponent parentComponent,
                                 AnyComponent cellComponent,
                                 RenderInfo   renderInfo,
                                 Any          contextNode,
                                 Any          rowRoot,
                                 Any          oldValue,
                                 Any          newValue,
                                 int          row,
                                 Any          rowKey,
                                 int          col,
                                 Any          colName,
                                 boolean      mouseCell,
                                 BooleanI     isUser,
                                 BooleanI     after,
                                 TreeLevel    level,
                                 BooleanI     isLeaf,
                                 BooleanI     expanded)
  {
    Any ret = null;

    if (fnArgs__ == null)
    {
      fnArgs__    = AbstractComposite.simpleMap();
      row__       = new AnyInt();
      col__       = new AnyInt();
      mouseCell__ = new AnyBoolean();
    }

    fnArgs__.add(EventConstants.EVENT_PARENT, parentComponent);
    fnArgs__.add(AnyTable.rowRoot__, rowRoot);
    row__.setValue(row);
    fnArgs__.add(AnyTable.row__, row__);

    if (col > -2)
    {
      col__.setValue(col);
      fnArgs__.add(AnyTable.column__, col__);
    }

    if (colName != null)
      fnArgs__.add(AnyTable.columnName__, colName);

    if (rowKey != null)
      fnArgs__.add(AnyTable.rowKey__, rowKey);

    if (isLeaf != null)
    {
      fnArgs__.add(AnyTree.isLeaf__, isLeaf);
      fnArgs__.add(AnyTree.expanded__, expanded);
    }
    
    if (level != null)
      fnArgs__.add(AnyTree.levelArg__, level);
    
    mouseCell__.setValue(mouseCell);
    fnArgs__.add(AnyTable.mouseCell__, mouseCell__);
    
    if (isUser != null)
    {
      fnArgs__.add(AnyTable.isUser__, isUser);
      fnArgs__.add(AnyTable.after__, after);
    }

    if (oldValue != null && newValue == null)
    {
      // Protect with const if not in the context of stopping editing
      oldValue = oldValue.bestowConstness();
    }
    fnArgs__.add(AnyDocView.val__, oldValue);
    
    if (newValue != null)
      fnArgs__.add(AnyDocView.newVal__, newValue);

    Transaction t = Globals.process__.getTransaction();

    Map context = Globals.process__.getContext();
    Any contextPath = Globals.process__.getContextPath();
    try
    {
      fnArgs__.add(AnyComponent.component__, cellComponent);
      if (renderInfo != null)
        fnArgs__.add(AnyDocView.fmt__, renderInfo.getFormat(oldValue));

      cellFunc.setArgs(fnArgs__);
      cellFunc.setTransaction(t);
      Globals.process__.setContext((Map)parentComponent.getContextNode());
      Globals.process__.setContextPath(parentComponent.getContext());
      ret = cellFunc.exec(contextNode);
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
    finally
    {
      cellFunc.setArgs(null);
      fnArgs__.empty();
      cellFunc.setTransaction(null);
      Globals.process__.setContext(context);
      Globals.process__.setContextPath(contextPath);
    }

    return ret;
  }


  public AnyCellRenderer(RenderInfo r)
  {
    // Make a place-holder AnyComponent whose purpose is to hold the
    // renderinfo until the proper one is created on first rendering
    // Note that setting the AnyComponent.setRenderInfo doesn't
    // do anything else because the component has no context.
    renderer_ = new AnyComponent();
    renderer_.setRenderer(true);
    setValueToComponent_ = renderer_;
    renderInfo_ = r;
  }
  
  public AnyCellRenderer(RenderInfo r, DefaultTreeCellRenderer tcr)
  {
    // For tree cell rendering we pre-install a Java DefaultTreeCellRenderer
    // label extension. This is because there is too much functionality
    // contained within it to safely do the work ourselves (from the point
    // of view of maintenance against new Java versions). This does not
    // preclude the use of alternative components installed by scripts
    // although that would likely also mean the use of a style function
    // to set things up properly.
    renderer_ = new AnyLabel();
    renderer_.setObject(tcr);
    setValueToComponent_ = renderer_;
    renderInfo_ = r;
  }

  public Component getListCellRendererComponent(JList   list,
                                                Object  value,
                                                int     index,
                                                boolean isSelected,
                                                boolean cellHasFocus)
  {
    Any a = (Any)value;

    return renderListCell(list, a, index, isSelected, cellHasFocus);
  }

  public Component getTableCellRendererComponent(JTable  table,
                                                 Object  value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int     row,
                                                 int     column)
  {
    Any a = (Any)value;
    
    com.inqwell.any.client.swing.JTable aTable = (com.inqwell.any.client.swing.JTable)table;
    
    int modelColumn = table.convertColumnIndexToModel(column);
    
    Component c = renderTableCell(aTable, a, isSelected, hasFocus, row, column, modelColumn);

    aTable.getAnyTable().setMaxRenderedWidth(column, c.getPreferredSize().width);

    return c;
  }

  // PropertyAccessMap
  public Any get (Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }

      return propertyMap_;
    }
    else
    {
      handleNotExist(key); // throws
      return null;
    }
  }

  public Any getIfContains(Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }

      return propertyMap_;
    }
    else
    {
      return null;
    }
  }

  public boolean contains (Any key)
  {
    if (properties__.equals(key))
      return true;

    return false;
  }

  public boolean isEmpty() { return false; }

  public void setStyle(Any styleFuncF)
  {
    Call styleFunc = AnyComponent.verifyCall(styleFuncF);
    styleFunc_ = styleFunc;
  }

  public Any getStyle()
  {
    return styleFunc_;
  }

  public void setComponent(Any renderer)
  {
    if (!(renderer instanceof AnyComponent))
      throw new IllegalArgumentException("Not an AnyComponent");

    AnyComponent ac = (AnyComponent)renderer;
    
    ac.getBorderee().setBorder(null);
    renderer_ = ac;
    setValueToComponent_ = renderer_;
  }

  public Any getComponent()
  {
    return renderer_;
  }
  
  public void setSetValueToComponent(Any setValueToComopnent)
  {
    if (AnyNull.isNull(setValueToComopnent))
      setValueToComponent_ = null;
    else
    {
      if (!(setValueToComopnent instanceof AnyComponent))
        throw new IllegalArgumentException("Not an AnyComponent");
  
      setValueToComponent_ = (AnyComponent)setValueToComopnent;
    }
  }
  
  public Any getSetValueToComponent()
  {
    return setValueToComponent_;
  }
  
//  public void setPath(Any path)
//  {
//    if (!(path instanceof NodeSpecification))
//      throw new AnyRuntimeException("Not a path");
//    
//    renderInfo_.setRenderPath((NodeSpecification)path);
//  }
  
  public Any getPath()
  {
    return renderInfo_.getRenderPath();
  }
  
  public RenderInfo getRenderInfo()
  {
    return renderInfo_;
  }
  
  public void setRenderInfo(Any renderInfo)
  {
    if (!(renderInfo instanceof RenderInfo))
      throw new AnyRuntimeException("Not a RenderInfo");
    
    renderInfo_ = (RenderInfo)renderInfo;
  }
  
  public boolean isEditable()
  {
    return renderInfo_.isEditable();
  }
  
  public Component renderTreeCell(JTree       tree,
                                  AnyTreeNode value,
                                  boolean     sel,
                                  boolean     expanded,
                                  boolean     leaf,
                                  int         row,
                                  boolean     hasFocus)
  {
    Any a = (value.getParent() == null) ? value.getContext()
                                        : value.getAny();
    
    // In the tree case the node we are given is the child of
    // the root node of a TreeLevel, and not yet the actual
    // value we want to render.  We must do the final bit
    // of value fetching here.
    try
    {
      // Always re-evaluate the rendering value because the
      // same renderer is used for all the individual level
      // children.
      a = renderInfo_.resolveDataNode(value.getAny(), true);
      if (a == null)
        a = null__;
    }
    catch (AnyException e)
    {
      throw new RuntimeContainedException(e);
    }


    // In the tree case the value we are passed is the node from which the
    // rendered text will be resolved by the renderinfo within the
    // component wrapper. Note that this is the reverse of table/list
    // rendering where we are given the data node and all that remains is
    // formatting. In that case, when calling the style function we go
    // back to the model to get the appropriate row root. Here, that node
    // is the passed-in value. Efficiency-wise there's not much to choose
    // as the list/table models cache their root.
    
    
    AnyAttributeSet style    = null;
    AnyAttributeSet rowStyle = null;
    
    Component c = renderer_.getComponent();
    
    com.inqwell.any.client.swing.JTree iTree =
      (com.inqwell.any.client.swing.JTree)tree;
    
    // Check for the special case of rendering the root
    // and the root not being visible.
    boolean nodeVisible = (value.getParent() != null || tree.isRootVisible());
    
    // Slightly experimental. When being used as a *table* cell renderer
    // the focus and selection state is driven by the table. In particular,
    // the tree never has the focus
    if (iTree.isTreeTableRenderer())
    {
      hasFocus = iTree.hasCellFocus();
      sel      = iTree.isCellSelected();
    }
    
    if (c instanceof DefaultTreeCellRenderer)
    {
      // Everything except the text is delegated to the DefaultTreeCellRenderer.
      // It does try to set the text as well by calling JTree.convertValueToText
      // but this is too crude for us and we use the wrapper's support
      // for that (AnyLabel.setValueToComponent()). Therefore set the text
      // after calling DefaultTreeCellRenderer.getTreeCellRendererComponent
      DefaultTreeCellRenderer dtcr = (DefaultTreeCellRenderer)c;
      
      if (iTree.isTreeTableRenderer())
      {
        if (sel)
        {
          if (hasFocus)
            dtcr.setBackgroundSelectionColor(iTree.getJTable().getSelectionBackground());
          else
            dtcr.setBackgroundSelectionColor(Color.LIGHT_GRAY);
        }
      }
      
      dtcr.getTreeCellRendererComponent(tree, a, sel, expanded, leaf, row, hasFocus);
      
      
      if (iTree.isTreeTableRenderer())
      {
        if (sel)
        {
          if (hasFocus)
          {
            c.setForeground(iTree.getJTable().getSelectionForeground());
            c.setBackground(iTree.getJTable().getSelectionBackground());
            dtcr.setBackgroundSelectionColor(iTree.getJTable().getSelectionBackground());
          }
          else
          {
            c.setBackground(Color.LIGHT_GRAY);
            c.setForeground(iTree.getJTable().getForeground());
            dtcr.setBackgroundSelectionColor(Color.LIGHT_GRAY);
          }
        }
      }
      
      // NB dtcr doesn't reset the font
      c.setFont(tree.getFont());
      
      if (setValueToComponent_ != null)
      {
        setValueToComponent_.setRenderInfo(renderInfo_);
        setValueToComponent_.setValueToComponent(a);
      }
      
      if (iTree.getRowStyle() != null && nodeVisible)
        rowStyle = callTreeStyleFunc((Call)iTree.getRowStyle(),
                                     iTree,
                                     value.getContext(),
                                     value.getAny(),
                                     a,
                                     row,
                                     value.getTreeLevel(),
                                     expanded,
                                     leaf);
      
      if (styleFunc_ != null && nodeVisible)
        style = callTreeStyleFunc(styleFunc_,
                                  iTree,
                                  value.getContext(),
                                  value.getAny(),
                                  a,
                                  row,
                                  value.getTreeLevel(),
                                  expanded,
                                  leaf);

      // the dtcr does its own default style stuff
      if (rowStyle != null)
      {
        AnyDocument.applyStyle((Container)c, rowStyle.getAttributeSet());
        
        // When not selected (in which case things are already done) we
        // require special handling for background colour.
        
        if (!sel)
        {
          // If there is a background then for treetables to have the whole
          // cell filled in we set the tree's background colour
          Color bg = (Color) rowStyle.getAttributeSet().getAttribute(StyleConstants.Background);
  
          if (bg != null)
          {
            dtcr.setBackgroundNonSelectionColor(bg);
          }
        }
      }
      
      if (style != null)
      {
        AnyDocument.applyStyle((Container)c, style.getAttributeSet());
        // When not selected (in which case things are already done) we
        // require special handling for background colour.
        
        if (!sel)
        {
          // If there is a background then for treetables to have the whole
          // cell filled in we set the tree's background colour
          Color bg = (Color) style.getAttributeSet().getAttribute(StyleConstants.Background);
  
          if (bg != null)
          {
            // Ineffective - too late!
//            if (iTree.isTreeTableRenderer())
//              iTree.setBackground(bg);
            
            dtcr.setBackgroundNonSelectionColor(bg);
          }
        }
      }
    }
    else
    {
      if (setValueToComponent_ != null)
      {
        setValueToComponent_.setRenderInfo(renderInfo_);
        setValueToComponent_.setValueToComponent(a);
      }
      
      if (iTree.getRowStyle() != null && nodeVisible)
        rowStyle = callTreeStyleFunc((Call)iTree.getRowStyle(),
                                     iTree,
                                     value.getContext(),
                                     value.getAny(),
                                     a,
                                     row,
                                     value.getTreeLevel(),
                                     expanded,
                                     leaf);
      
      if (styleFunc_ != null && nodeVisible)
        style = callTreeStyleFunc(styleFunc_,
                                  iTree,
                                  value.getContext(),
                                  value.getAny(),
                                  a,
                                  row,
                                  value.getTreeLevel(),
                                  expanded,
                                  leaf);

      defaultStyle(c, tree);

      if (rowStyle != null)
      {
        AnyDocument.applyStyle((Container)c, rowStyle.getAttributeSet());

        if (!sel && iTree.isTreeTableRenderer())
        {
          // If there is a background then for treetables to have the whole
          // cell filled in we set the tree's background colour
          Color bg = (Color) rowStyle.getAttributeSet().getAttribute(StyleConstants.Background);
  
          if (bg != null)
            iTree.setBackground(bg);
        }
      }

      if (style != null)
      {
        AnyDocument.applyStyle((Container)c, style.getAttributeSet());

        if (!sel && iTree.isTreeTableRenderer())
        {
          // If there is a background then for treetables to have the whole
          // cell filled in we set the tree's background colour
          Color bg = (Color) style.getAttributeSet().getAttribute(StyleConstants.Background);
  
          if (bg != null)
          {
            iTree.setBackground(bg);
          }
        }
      }
    }

    if (hasFocus)
    {
      boolean editable = value.getTreeLevel().isEditable(value);
      ((JComponent)c).setBorder(editable ? editBorder : UIManager.getBorder("Table.focusCellHighlightBorder"));
    }
    else
    {
      ((JComponent)c).setBorder(noFocusBorder);
    }
    
    return c;
  }

  public void setClosedIcon(Any icon)
  {
    if (setValueToComponent_ != null)
    {
      AnyIcon i = (AnyIcon)icon;
      
      setValueToComponent_.setClosedIcon(i.getIcon());
    }
  }

  public void setLeafIcon(Any icon)
  {
    if (setValueToComponent_ != null)
    {
      AnyIcon i = (AnyIcon)icon;
      
      setValueToComponent_.setLeafIcon(i.getIcon());
    }
  }
  
  public void setOpenIcon(Any icon)
  {
    if (setValueToComponent_ != null)
    {
      AnyIcon i = (AnyIcon)icon;
      
      setValueToComponent_.setOpenIcon(i.getIcon());
    }
  }

  private Component renderListCell(JList   list,
                                   Any     value,
                                   int     index,
                                   boolean isSelected,
                                   boolean cellHasFocus)
  {
    // Extract list value
    Any a = value;
    if (value instanceof Map)
    {
      Map m = (Map)value;
      a = m.getIfContains(ListRenderInfo.external__);
      if (a == null)
        a = m.getIfContains(ListRenderInfo.internal__);
    }
    
    if (a == null)
      a = null__;

    // Create a default component if one was not supplied from script 
    if (renderer_.isRenderer())
      initListRenderer(a);
    
    AnyAttributeSet style = null;
    if (styleFunc_ != null)
      style = callListStyleFunc(list, a, index);

    Component c = renderer_.getComponent();
    
    if (setValueToComponent_ != null)
    {
      setValueToComponent_.setRenderInfo(renderInfo_);
      setValueToComponent_.setValueToComponent(a);
    }
    
    // Return the renderer properties to suitable defaults
    defaultStyle(c, list);
    
    if (style != null)
      AnyDocument.applyStyle((Container)c, style.getAttributeSet());

    // Apply selection hints etc. These would override any style settings - is
    // that really what we want?
    if (isSelected)
    {
      c.setBackground(list.getSelectionBackground());
      c.setForeground(list.getSelectionForeground());
    }
    else
    {
      c.setBackground(list.getBackground());
      c.setForeground(list.getForeground());
    }
    
    c.setEnabled(list.isEnabled());
    ((JComponent)c).setBorder((cellHasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
    if (cellHasFocus)
      ((JComponent)c).setBorder(UIManager.getBorder("List.focusCellHighlightBorder"));

    ((JComponent)c).setOpaque(true);

    return c;
  }

  private Component renderTableCell
                         (com.inqwell.any.client.swing.JTable  table,
                          Any                                  a,
                          boolean                              isSelected,
                          boolean                              hasFocus,
                          int                                  row,
                          int                                  column,
                          int                                  modelColumn)
  {
    if (a == null)
      a = AnyNull.instance();
    else
    {
      a = translateEnum(a);
    }

    // Create a default component if one was not supplied from script 
    if (renderer_.isRenderer())
      initTableRenderer(a);

    AnyAttributeSet rowStyle = null;
    if (table.getRowStyle() != null)
    {
      rowStyle = callTableStyleFunc((Call)table.getRowStyle(), table, a, row, modelColumn);
    }
    
    AnyAttributeSet style = null;
    if (styleFunc_ != null)
      style = callTableStyleFunc(styleFunc_, table, a, row, modelColumn);

    Component c = renderer_.getComponent();
    
    if (setValueToComponent_ != null)
    {
      if (rowStyle != null && rowStyle.contains(NodeSpecification.atFormat__))
        renderInfo_.setFormat(rowStyle.getAttributeSet().getAttribute(NodeSpecification.atFormat__).toString());
      
      if (style != null && style.contains(NodeSpecification.atFormat__))
        renderInfo_.setFormat(style.getAttributeSet().getAttribute(NodeSpecification.atFormat__).toString());
      
      setValueToComponent_.setRenderInfo(renderInfo_);
      setValueToComponent_.setValueToComponent(a);
    }
    
    // Return the renderer properties to suitable defaults
    defaultStyle(c, table);
    
    if (rowStyle != null)
      AnyDocument.applyStyle((Container)c, rowStyle.getAttributeSet());

    if (style != null)
      AnyDocument.applyStyle((Container)c, style.getAttributeSet());


    if (isSelected)
    {
      if (hasFocus)
      {
        if (style != null)
        {
          if (StyleConstants.getBackground(style.getAttributeSet()) != null)
            c.setBackground(c.getBackground().darker());
          c.setForeground(table.getSelectionForeground());
          
        }
        else
        {
          c.setForeground(table.getSelectionForeground());
          c.setBackground(table.getSelectionBackground());
        }
      }
      else
      {
        if (style != null)
        {
          if (StyleConstants.getBackground(style.getAttributeSet()) != null)
            c.setBackground(c.getBackground().darker());
          c.setForeground(table.getSelectionForeground());
        }
        else
        {
          c.setBackground(Color.LIGHT_GRAY);
          c.setForeground(table.getForeground());
        }
      }
    }

    if (hasFocus)
    {
      boolean editable = table.isCellEditable(row, column);
      ((JComponent)c).setBorder(editable ? editBorder : UIManager.getBorder("Table.focusCellHighlightBorder"));
      //if (table.isCellEditable(row, column))
      //{
        //ret.setForeground(UIManager.getColor("Table.focusCellForeground"));
        //ret.setBackground(UIManager.getColor("Table.focusCellBackground"));
      //}
    }
    else
    {
      ((JComponent)c).setBorder(noFocusBorder);
    }

    if (setValueToComponent_ != null)
    {
      if (c.getPreferredSize().width > table.getCellRect(row, column, false).width)
        renderer_.setTooltip(a);
      else
        renderer_.setTooltip(null);
    }
    
    return c;
  }
  
  private void initListRenderer(Any v)
  {
    initRenderer(v, MakeComponent.LIST);
  }

  private void initTableRenderer(Any v)
  {
    initRenderer(v, MakeComponent.TABLE);
  }

  // Make a component and suitable wrapper
  private void initRenderer(Any v, Any renderType)
  {
    Container c = MakeComponent.makeRenderer(v,
                                             renderer_.getComponent(),
                                             renderType);
    if (c != renderer_.getComponent())
    {
      // The type of component required changed (or first use)
      RenderInfo r = renderer_.getRenderInfo();
      renderer_ = MakeComponent.makeWrapper(c);
      
      // This method is only called when the default rendering is in effect.
      // If it has been explicitly switched off then leave it that way 
      if (setValueToComponent_ != null)
        setValueToComponent_ = renderer_;
    }
  }

  private void defaultStyle(Component to, Component from)
  {
    to.setFont(from.getFont());
    to.setBackground(from.getBackground());
    to.setForeground(from.getForeground());
    // Hmmm
    ((JComponent)to).setToolTipText(null);
  }

  private AnyAttributeSet callListStyleFunc(JList list,
                                            Any   value,
                                            int   row)
  {
    AnyList l = ((com.inqwell.any.client.swing.JList)list).getAnyList();
    AnyListModel m = (AnyListModel)l.getListModel();
    Any rowRoot = m.getItemAt(row);
    Any rowKey  = m.getRowKey(row);

    if (rowRoot == null)
      return null;
    
    Any contextNode = m.getContext();
    
    return callStyleFunc(styleFunc_,
                         l,
                         contextNode,
                         rowRoot,
                         rowKey,
                         value,
                         row,
                         -2,
                         null,
                         false,   // mouse cell is TODO
                         null,
                         null,
                         null);
  }
  
  private AnyAttributeSet callTableStyleFunc
                             (Call                                styleFunc,
                              com.inqwell.any.client.swing.JTable table,
                              Any                                 a,
                              int                                 row,
                              int                                 column)
  {
    TableModel m = table.getAnyTable().getModel();
    
    Any rowRoot = m.getRowAt(row);
    Any rowKey  = m.getRowKey(row);
    if (rowRoot == null)
      return null;
    
    Any contextNode     = m.getContext();
    Any colName         = m.getNameOfColumn(column);
    boolean isMouseCell = table.isMouseCell(row, column);
    TreeLevel level = null;
    if (m.isTreeTable())
      level = m.getTreeLevel(row);
    
    return callStyleFunc(styleFunc,
                         table.getAnyTable(),
                         contextNode,
                         rowRoot,
                         rowKey,
                         a,
                         row,
                         column,
                         colName,
                         isMouseCell,
                         level,
                         null,
                         null);
  }
  
  private AnyAttributeSet callTreeStyleFunc(Call     styleFunc,
                                            com.inqwell.any.client.swing.JTree iTree,
                                            Any      contextNode,
                                            Any      rowRoot,
                                            Any      a,
                                            int      row,
                                            TreeLevel level,
                                            boolean  expanded,
                                            boolean  isLeaf)
  {
    // NOTE: The given contextNode argument is the context specific
    // to the tree level. This is less useful than the GUI context so
    // in fact we pass that. If we need the "levelContext" then we'll
    // need to pass that as a separate argument.  (yawn...)
    
    
    AnyComponent t = iTree.getAnyTree();
    if (t == null)
      t = iTree.getAnyTable(); // we must be a treetable if we are not a tree
    
    contextNode = t.getContextNode();
    
    if (isLeaf__ == null)
    {
      isLeaf__   = new AnyBoolean();
      expanded__ = new AnyBoolean();
    }
    
    isLeaf__.setValue(isLeaf);
    expanded__.setValue(expanded);
    
    return callStyleFunc(styleFunc,
                         t,
                         contextNode,
                         rowRoot,
                         null, // TODO: is there a rowKey?
                         a,
                         row,
                         -2,
                         null,
                         false,   // mouse cell is TODO
                         level,
                         isLeaf__,
                         expanded__);
  }
  
  private AnyAttributeSet callStyleFunc(Call         styleFunc,
                                        AnyComponent parentComponent,
                                        Any          contextNode,
                                        Any          rowRoot,
                                        Any          rowKey,
                                        Any          value,
                                        int          row,
                                        int          col,
                                        Any          colName,
                                        boolean      isMouseCell,
                                        TreeLevel    level,
                                        BooleanI     isLeaf,
                                        BooleanI     expanded)
  {
    Any ret = callCellFunc(styleFunc,
                           parentComponent,
                           renderer_,
                           renderInfo_,
                           contextNode,
                           rowRoot,
                           value,
                           null,  // No new value when rendering
                           row,
                           rowKey,
                           col,
                           colName,
                           isMouseCell,
                           null,  // No isUser when rendering
                           null,  // No after when rendering
                           level,
                           isLeaf,
                           expanded);

    if (ret == null || AnyNull.isNullInstance(ret))
      return null;

    if (!(ret instanceof AnyAttributeSet))
      throw new IllegalArgumentException("Style function must return a style or null");

    return (AnyAttributeSet)ret;
  }

  private Any translateEnum(Any v)
  {
    RenderInfo r = renderInfo_;
    
    Descriptor d = r.getDescriptor();
    if (d != Descriptor.degenerateDescriptor__ && d != null)
    {
      Any        f = r.getField();
      
      if (d.isEnum(f))
      {
        Map enums = d.getEnums();
        enums = (Map)enums.get(f);
        
        if (enums.contains(v))
          v = enums.get(v);
      }
    }
    return v;
  }
}
