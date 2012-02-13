/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyComponentEditor.java $
 * $Author: sanderst $
 * $Revision: 1.9 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyNull;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.Call;
import com.inqwell.any.ConstByte;
import com.inqwell.any.ConstString;
import com.inqwell.any.DegenerateIter;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.EventListener;
import com.inqwell.any.Func;
import com.inqwell.any.Globals;
import com.inqwell.any.Iter;
import com.inqwell.any.Map;
import com.inqwell.any.PropertyAccessMap;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.beans.ClassMap;
import com.inqwell.any.client.swing.InqEditor;
import com.inqwell.any.client.swing.TableModel;

/**
 * An all purpose editor, handling table cells and tree cells
 * <p>
 * Acts as a container for an underlying editor which does
 * all the real work.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.9 $
 */ 
public class AnyComponentEditor extends    PropertyAccessMap
															  implements TableCellEditor,
																					 TreeCellEditor,
                                           InqEditor,
                                           EventListener
{
  private static final long serialVersionUID = 1L;
  
  private static final AnyBoolean retVal__ = new AnyBoolean();
  
	// The underlying Table/Tree cell editor
	//private   CellEditor      cellEditor_;
  
  private ArrayList cellEditorListeners_ = new ArrayList();

  // The editor component ( provided via copyFrom() )
  private   AnyComponent extEditor_;
  
  // The event binding on the editor component.  (Not used at
  // present but keep it in case we want to subsequently remove
  // from extEditor_)
  private AnyView.EventBinding eb_;

  // This relates to the data rendered by the complex
  // component (Table or Tree) and not the data rendered by
  // the extEditor_ component.
	private   RenderInfo  r_;

  // Whether this editor actually yields a value.  It may not if,
  // say, the 'editor' is just a button.
  //private boolean hasValue_ = true;
  
  private Map     propertyMap_;
  
  private short   clickCountToStart_ = 2;
  
  private Call    canStartFunc_;
  private Call    onStopFunc_;
  
  // Action command from a combo box
  private static Any comboBoxEdited__ = new ConstString("comboBoxEdited");
  
  // A map of functions that will be used to handle events according to
  // the class of the underlying component.
  private static ClassMap  funcMap__;
  
  public static Any noValue__ = new ConstByte();
  
  static
  {
    funcMap__ = new ClassMap();
    funcMap__.add(java.lang.Object.class, new AlwaysFireStop());
    funcMap__.add(javax.swing.JTextField.class, new AlwaysFireStop());      
    funcMap__.add(javax.swing.JComboBox.class, new FireOnEdited());      
    funcMap__.add(javax.swing.AbstractButton.class, new AlwaysFireStop());      
    funcMap__.add(com.inqwell.any.client.swing.JDateChooser.class, new NeverFireStop());      
  }
  
	public AnyComponentEditor(RenderInfo r)
	{
	  r_ = r;
	}
	
  public Any copyFrom(Any a)
  {
    // Well, a cheap implementation that avoids having to do
    // a PropertyAccessMap job.  Instead we check out the class
    // of the argument and do something appropriate.  Yuk.
    // Used by complex components, like tables, that have
    // embedded renderers/editors
    
    if (a == AnyNull.instance())
      setComponent(null);
    else if (a instanceof BooleanI)
    {
      // The editable property.  With this we can vary the editable
      // state of a table column.
      BooleanI b = (BooleanI)a;
      r_.setEditable(b.getValue());
      
    }
    else if (a instanceof AnyComponent)
    {
      // Direct access to the editor component.  There is no need
      // to use this in the general case as the complex component will
      // create a component and manage the updating of the relevant
      // model data.  However, when a specific component with scripted
      // functionality is required this can be useful (in fact, this is
      // now the more common case)
      initEditor((AnyComponent)a);
      
      // If there is an editor then default the cell to editable
      r_.setEditable(true);
    }
    return this;
  }
  
  public void clearEditingValue()
  {
    if (getEditingComponent() instanceof JTextField)
    {
      try
      {
        extEditor_.setRenderedValue(null);
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  }
  
  public void setClickCountToStart(int clickCountToStart)
  {
    clickCountToStart_ = (short)clickCountToStart;
  }
  
  public int getClickCountToStart()
  {
    return clickCountToStart_;
  }
  
  public Any getComponent()
  {
    return extEditor_;
  }
  
  /**
   * Return whether this editor has a value. If the editor component
   * yields either hard or value-null then it does not have a value.
   * This is usually either because it has no renderinfo property
   * or renders value-null by script configuration.
   * @return
   */
  public boolean hasValue()
  {
    boolean ret = false;
    if (extEditor_ != null)
      ret = extEditor_.getRenderInfo() != null;
    
    return ret;
  }
  
  // Of dubious quality now cell edit event redirection is available
  public void setEventBinding(Any binding)
  {
    if (!(binding instanceof Array))
      throw new IllegalArgumentException("Not an array (of event types)");
    
    if (eb_ == null)
      throw new IllegalStateException("No editor to bind to");

    Func f = eb_.removeBinding();
      
    eb_ = extEditor_.makeEventBinding(f,
                                      (Array)binding,
                                      false,     // consume
                                      false,     // busy
                                      false);    // modelFires

    extEditor_.addAdaptedEventListener(eb_);
  }
  
  public Any getEventBinding()
  {
    return null;
  }
  
  public void setRenderInfo(RenderInfo r)
  {
    r_ = r;
  }
  
  public void setComponent(Any component)
  {
    AnyComponent ac = (AnyComponent)component;
    initEditor(ac);
  }
  
  public void setCanStartEdit(Any canStartF)
  {
    Call f = AnyComponent.verifyCall(canStartF);
    canStartFunc_ = f;
  }

  public Any getCanStartEdit()
  {
    return canStartFunc_;
  }

  public void setOnStopEdit(Any canStopF)
  {
    Call f = AnyComponent.verifyCall(canStopF);
    onStopFunc_ = f;
  }

  public Any getOnStopEdit()
  {
    return onStopFunc_;
  }
  
  public boolean hasStopEdit()
  {
    return onStopFunc_ != null;
  }

  public Array getDesiredEventTypes()
  {
    return EventConstants.ALL_TYPES;
  }

  public boolean processEvent(Event e) throws AnyException
  {
    if (e.getId().equals(EventConstants.CELLEDITOR_STOPPED))
    {
      // Tell the event handler the reason for stopping editing
      // is an event on the component, not because of the UI
      // of the editor's host component (table or tree)
      fireEditingStopped(new ChangeEvent(extEditor_));
      
      // Consume the event to prevent its pointless propagation
      // up through the Inq hierarchy
      e.consume();
    }

    return true;
  }
  
	public Component getTableCellEditorComponent(JTable table,
																							 Object value,
																							 boolean isSelected,
																							 int row,
																							 int column)
	{
    if (value == null)
      return null;
      
    try
    {
      com.inqwell.any.client.swing.JTable t =
        (com.inqwell.any.client.swing.JTable)table;
      AnyTable at = t.getAnyTable();
      
      // Only start editing of there are real rows
      if (at.getRealRowCount() <= 0)
        return null;

      // If the editor is not in the Inq hierarchy then it will
      // have no context. In this case add it to the AnyTable. This
      // means that an editor could float around components in
      // different contexts. May not be that likely to happen but
      // it does mean that ugly script to force the component into
      // the Inq hierarchy other than by layout is unnecessary.
      if (extEditor_.getParentAny() == null)
      {
        // put current editor in replacing any other
        at.replaceItem(AnyTable.editor__, extEditor_);
        
        // Add ourselves as a listener to pick up event types scripted
        // as editstop events
        extEditor_.addEventListener(this);
      }
      else if (extEditor_.getParentAny() != at)
      {
        // this editor is currently elsewhere so move it here
        extEditor_.removeInParent();
        at.replaceItem(AnyTable.editor__, extEditor_);
        extEditor_.addEventListener(this);
      }

      column = table.convertColumnIndexToModel(column);
      TableModel  m = (TableModel)table.getModel();
      Any a = m.getResponsibleValueAt(row, column);
      
      // If we have a scripted function to check for starting to edit
      // then call it now. If it returns false then return null, which
      // even at this late stage is enough to put JTable off.
      if (canStartFunc_ != null)
      {
        Any rowRoot     = m.getRowAt(row);
        Any rowKey      = m.getRowKey(row);
        Any contextNode = m.getContext();
        Any colName     = m.getNameOfColumn(column);
        
        boolean canEdit = canStartEditing(at,
                                          contextNode,
                                          rowRoot,
                                          a,
                                          row,
                                          rowKey,
                                          column,
                                          colName,
                                          null,
                                          null,
                                          null);
        if (!canEdit)
          return null;
      }
      
      if (hasValue())
      {
        extEditor_.setRenderedValue(a);
      }
    }
    catch (AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
    
    // Do we still need to do this, or is it the script's responsibility?
    
		//Component c = editTableCell(a, m.getContext());
    Component c = extEditor_.getComponent();
		
		c.setFont(table.getFont());
    //((JComponent)c).revalidate();
		return c;		
	}
	
	public Component getTreeCellEditorComponent(JTree   tree,
																							Object  value,
																							boolean isSelected,
																							boolean expanded,
																							boolean leaf,
																							int     row)
	{
    // the argument "value" is already the responsible
    // value for the editor - see AnyTreeModel.getTreeCellEditorComponent
    Any a  = (Any)value;
		
		// Have to repeat some code from AnyTeeeModel to get the context.
		// Oh well, its only to enter editing mode.
	  TreePath       tp = tree.getPathForRow(row);
	  AnyTreeModel   m  = (AnyTreeModel)tree.getModel();
    TreeLevel      l  = m.getTreeLevelForPath(tp);

    // TBD Clear up unwanted code above.
    
		//Component c = editTreeCell(a, l.getContext());
    Component c = extEditor_.getComponent();
		c.setFont(tree.getFont());
		return c;
	}
	
	// CellEditor interface methods
  public void addCellEditorListener(CellEditorListener l)
  {
  	// We take over the maintenance of the action listeners
  	// registered (in fact only the combo box) as the
  	// underlying editor component is allowed to change
  	cellEditorListeners_.add(l);
  }

	public void cancelCellEditing() 
	{
		//System.out.println ("AnyComponentEditor.cancelCellEditing()");
    fireEditingCancelled();
	}
	
	public Object getCellEditorValue() 
	{
		//System.out.println ("AnyComponentEditor.getCellEditorValue()");
    if (!hasValue())
      return noValue__;
    
    Any ret =  extEditor_.getRenderedValue();
    if (ret == AnyNull.instance())
      ret = null;
    
    return ret;
	}
	
	public boolean isCellEditable(EventObject anEvent) 
	{
    // anEvent == null means programatic or [F2] action
    boolean ret = r_.isEditable() && (extEditor_ != null);
    
		// System.out.println ("isCellEditable " + anEvent);
    
    if (ret && anEvent instanceof MouseEvent)
    {
      MouseEvent me = (MouseEvent)anEvent;
      int id = me.getID();
      if (id == MouseEvent.MOUSE_DRAGGED ||
          id == MouseEvent.MOUSE_MOVED   ||
          id == MouseEvent.MOUSE_ENTERED   ||
          id == MouseEvent.MOUSE_EXITED)
        return false;
        
      return me.getClickCount() >= clickCountToStart_;
    }

		return ret;
	}
	
  public void removeCellEditorListener(CellEditorListener l) 
	{
  	int indx;
  	
  	if ((indx = cellEditorListeners_.indexOf(l)) >= 0)
	  	cellEditorListeners_.remove(indx);
	}
	
	public boolean shouldSelectCell(EventObject anEvent) 
	{
		//System.out.println ("AnyComponentEditor.shouldSelectCell() " + anEvent);
    if (anEvent instanceof MouseEvent)
    {
      MouseEvent e = (MouseEvent)anEvent;
      return e.getID() != MouseEvent.MOUSE_DRAGGED;
    }
    return true;
	}
  
  public JComponent getEditingComponent()
  {
    JComponent ret = null;
    if (extEditor_ instanceof AnyCard)   // will do for now
    {
      AnyCard ac = (AnyCard)extEditor_;
      AnyComponent comp = ac.getVisibleCard();
      if (comp != null)
        ret = (JComponent)comp.getComponent();
    }
    else
      ret = (JComponent)extEditor_.getComponent();
    
    return ret;
  }
	
	public boolean stopCellEditing() 
	{
		//System.out.println ("AnyComponentEditor.stopCellEditing()");

    // See AnyTableModel.getValueAt() for an explanation.  In this
    // case its the TableUI navigation handling and editable cells.
    if (Globals.process__.getContext() == null)
      Globals.process__.setContext((Map)extEditor_.getContextNode());

    //AbstractAny.stackTrace();
    fireEditingStopped();
    return true;
	}
  
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
                                 BooleanI     expanded)
  {
    boolean ret = true;
    
    if (canStartFunc_ != null)
    {
      // In case the function does anything to the editor that makes it
      // generate events to which bindings are attached, guard them
      // from emptying the stack etc
      int c = AnyView.syncGuiStart();
      Any a;
      try
      {
        a = AnyCellRenderer.callCellFunc(canStartFunc_,
                                         parentComponent,
                                         extEditor_,
                                         r_,
                                         contextNode,
                                         rowRoot,
                                         value,
                                         null,  // no new value until editing has stopped
                                         row,
                                         rowKey,
                                         col,
                                         colName,
                                         false, // mouseCell
                                         null,
                                         null,
                                         level,
                                         isLeaf,
                                         expanded);

        retVal__.copyFrom(a);
        ret = retVal__.getValue();
      }
      finally
      {
        AnyView.syncGuiEnd(c);
      }
    }
    return ret;
  }

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
                            BooleanI     expanded)
  {
    if (onStopFunc_ != null)
      AnyCellRenderer.callCellFunc(onStopFunc_,
                                   parentComponent,
                                   extEditor_,
                                   r_,
                                   contextNode,
                                   rowRoot,
                                   oldValue,
                                   newValue,
                                   row,
                                   rowKey,
                                   col,
                                   colName,
                                   false,    // mouseCell
                                   isUser,
                                   after,
                                   level,
                                   isLeaf,
                                   expanded);
  }

	//
	// Map interface stuff
	//
  
  /**
   * Override.  If the key is <code>"properties"</code> then (make and)
   * return a property binding object.
   */
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

	public Iter createIterator () {return DegenerateIter.i__;}

  protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	protected void emptying() {}

  private void initEditor(AnyComponent ac)
  {
    // No change? Then do nothing
    if (ac == extEditor_)
      return;
      
    if (extEditor_ != null)
    {
      // remove any old editor
      //   1) remove event binding if we created a default one
      if (eb_ != null)
        eb_.removeBinding();
      
      eb_ = null;
      
      //   2) Remove component from any context it may be in
      //      Note this also throws away any Inq event listeners
      //      so scripted edit stop events are undone by this.
      extEditor_.removeInParent();
      
      extEditor_ = null;
    }
    
    if (ac != null)
    {
      RenderInfo r = ac.getRenderInfo();
//      if (r != null)
//        r.setEditable(true);
// Dangerous at least until combo box is rewritten      
      extEditor_ = ac;
      
      extEditor_.initAsCellEditor();
      
      // Edit Stop Events
      
      // If the component supports one, put the default event type on it.
      // If there is a default event type we assume that it is the correct
      // event to cause editing to stop.
      if (extEditor_.hasDefaultEventType())
      {
        StopFunc f = (StopFunc)funcMap__.get(ac.getComponent()).cloneAny();
        f.setEditor(this);
        eb_ = ac.makeEventBinding(f,
                                  EventConstants.DEFAULT_TYPE,
                                  false,     // consume
                                  false,     // busy
                                  false);    // modelFires
        
        ac.addAdaptedEventListener(eb_);
      }
      
      // Add ourselves as a listener to pick up event types scripted
      // as editstop events
      //ac.addEventListener(this);
    }
  }

  private void fireEditingStopped()
  {
    ChangeEvent ce = null;

  	for (int i = 0; i < cellEditorListeners_.size(); i++)
  	{
      if (ce == null)
		    ce = new ChangeEvent(this);
        
  		CellEditorListener cl = (CellEditorListener)cellEditorListeners_.get(i);
  		cl.editingStopped(ce);
  	}
  }
  
  private void fireEditingStopped(ChangeEvent ce)
  {
    // An alternative fire method to our listeners that uses the
    // event supplied. Why? Well editing can stop because of
    // UI navigation OR because of an event received from the
    // underlying component. It is nice to differentiate between
    // these cases when execution reaches CellEditorListener.editingStopped
    // so we know whether the user navigated away from the cell
    // or hit return (say) in a text field. This is particularly
    // important with button editors as we may not want
    // to invoke the action just because the editor was active
    // but later stopped because it lost focus.
    // If editing was stopped by the UI then the event source is
    // the AnyComponentEditor instance. If it was stopped by an
    // event from the underlying component the source is the
    // component. We pass this as a flag to the cellEditDone
    // Inq event handler.
    for (int i = 0; i < cellEditorListeners_.size(); i++)
    {
      CellEditorListener cl = (CellEditorListener)cellEditorListeners_.get(i);
      cl.editingStopped(ce);
    }
  }
	
  private void fireEditingCancelled()
  {
    ChangeEvent ce = null;

  	for (int i = 0; i < cellEditorListeners_.size(); i++)
  	{
      if (ce == null)
		    ce = new ChangeEvent(this);
        
  		CellEditorListener cl = (CellEditorListener)cellEditorListeners_.get(i);
  		cl.editingCanceled(ce);
  	}
  }
  
  static private abstract class StopFunc extends    AbstractFunc
                                         implements Cloneable
  {
    protected Event              e_;
    protected AnyComponentEditor ace_;
    
    // "Synthesise" inner class reference...
    private void setEditor(AnyComponentEditor ace)
    {
      ace_ = ace;
    }
    
    // Make event available from EventBinding.execExpr
    public void setParam(Any a)
    {
      e_ = (Event)a;
    }
  }
  
  static private class NeverFireStop extends StopFunc
  {
    public Any exec(Any a) throws AnyException
    {
      //System.out.println("NeverFireStop.exec " + e_);
      return null;
    }
  }
  
  static private class AlwaysFireStop extends StopFunc
  {
    public Any exec(Any a) throws AnyException
    {
      //System.out.println("AlwaysFireStop.exec " + e_);
      //ace_.stopCellEditing();
      // Tell the event handler the reason for stopping editing
      // is an event on the component, not because of the UI
      // of the editor's host component (table or tree)
      ace_.fireEditingStopped(new ChangeEvent(ace_.getComponent()));
      return null;
    }
  }
  
  static private class FireOnEdited extends StopFunc
  {
    public Any exec(Any a) throws AnyException
    {
      // Hitting enter results in an actioncommand "comboBoxEdited"
      //System.out.println(e_.get(ListenerAdapterFactory.actionCommand__));
      if(e_.get(ListenerAdapterFactory.actionCommand__).equals(comboBoxEdited__))
      {
        //System.out.println("FireOnEdited.exec " + e_);
        ace_.stopCellEditing();
      }
      return null;
    }
  }
}

