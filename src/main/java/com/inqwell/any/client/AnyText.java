/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyText.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;
import java.awt.Container;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.text.Format;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFormat;
import com.inqwell.any.Array;
import com.inqwell.any.ConstString;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.Func;
import com.inqwell.any.Map;
import com.inqwell.any.Set;
import com.inqwell.any.Transaction;
import com.inqwell.any.client.swing.JPanel;
import com.inqwell.any.client.swing.JTextArea;
import com.inqwell.any.client.swing.JTextPane;

public class AnyText extends AnyDocView
{
	private JTextComponent  t_;
  private JScrollPane     s_;
  private JComponent      borderee_;

  // Whether document events will be delivered to scripted event
  // handlers when the component is not editable/enabled
  private boolean docEventsWhenNotEditable_ = true;
  
	private static Set     textProperties__;
	private static Any     scrollable__    = new ConstString("scrollable");
  private static Any     document__      = new ConstString("document");
  private static Any     docEventsWhenNotEditable__  = new ConstString("docEventsWhenNotEditable");


	static
	{
    textProperties__ = AbstractComposite.set();
    textProperties__.add(scrollable__);
    textProperties__.add(document__);
    textProperties__.add(docEventsWhenNotEditable__);
	}

	public void setObject(Object o)
	{
		if (o instanceof JPanel)
		{
			JComponent borderee = (JComponent)o;
			borderee_ = borderee;
			setObject(borderee.getComponent(0));
			return;
		}

		//System.out.println ("AnyText.setObject " + o.getClass());

		if ((!(o instanceof JTextComponent)) && (!(o instanceof JScrollPane)))
			throw new IllegalArgumentException
									("AnyText wraps javax.swing.text.JTextComponent/JScrollPane and sub-classes");


		if (o instanceof JTextComponent)
		{
			t_ = (JTextComponent)o;
		}
		else
		{
			s_ = (JScrollPane)o;
			t_ = (JTextComponent)s_.getViewport().getView();
		}

		super.setObject(t_);
		// setupEventSet(t_); done by super.setObject
		// setupEventSet(t_.getDocument()); see initUpdateModel
    addAdaptedEventListener(new TextFocusListener(focusEventTypes__));

		if (t_ instanceof JTextArea)
		{
			setScrollable(true);
		}
		else
		{
		  if (t_ instanceof JTextPane)
      {
			  setScrollable(true);
        DefaultHighlighter h = (DefaultHighlighter)t_.getHighlighter();
        h.setDrawsLayeredHighlights(false);
      }
			else
			  setScrollable(false);
		}

		t_.setAutoscrolls(true);
		setColumns(10);
	}

  /**
   * Sets the <i>scrollable</i> property.  This is a
   * <i>synthetic property</i> provided by <code>inq</code>
   * that can only be set prior to layout. Setting this
   * property once the table has been placed in the
   * <code>awt</code> component hierarchy will have
   * undefined results.
   */
  public void setScrollable(boolean scrollable)
  {
  	if (scrollable)
  	{
  		if (borderee_ != null && borderee_ != t_)
  		  borderee_.remove(t_);
  		
  		//s_ == null &&   
  	  s_ = new JScrollPane(t_);
  	  borderee_ = s_;
  	}
  	else
  	{
  		if (s_ != null)
  		  s_.setViewportView(null);
  		  
  	  s_ = null;
  	  borderee_ = new JPanel();
  	  borderee_.add(t_);
  	}
  	//borderee_.validate();
  }
  
  public boolean getScrollable()
  {
  	return s_ != null;
  }
  
  public boolean isDocEventsWhenNotEditable()
  {
    return docEventsWhenNotEditable_;
  }
  
  public void setDocEventsWhenNotEditable(boolean docEventsWhenNotEditable)
  {
    docEventsWhenNotEditable_ = docEventsWhenNotEditable;
  }
  
	public JComponent getBorderee()
	{
		return borderee_;
	}

  public void setDocument(Any doc)
  {
    if (!(doc instanceof AnyDocument))
      throw new IllegalArgumentException ("Not an AnyDocument");
    
    AnyDocument d = (AnyDocument)doc;
    t_.setDocument(d.getDocument());
  }
  
  public Any getDocument()
  {
    return new AnyDocument(t_.getDocument());
  }
  
	public void setRenderInfo(RenderInfo r)
	{
    if (r != null)
    {
  		t_.setEditable(r.isEditable());
  		setColumns(r.getWidth());
    }
		super.setRenderInfo(r);
	}

	public void setEditable(boolean editable)
	{
    super.setEditable(editable);

		t_.setEditable(editable);
	}
  
  public void dump()
  {
    View root = t_.getUI().getRootView(t_);
    dumpView(root, 0);
    dumpDoc(t_.getDocument().getRootElements()[0],0);
  }
  
  private void dumpView(View root, int level)
  {
    for (int i = 0; i < level*2; i++)
      System.out.print(' ');
    System.out.print(root);
    Element elem = root.getElement();
    System.out.println(elem.getName() + "[" + elem.getStartOffset() + ", " + elem.getEndOffset() + "] " + elem.hashCode());
    if (root.getViewCount() != 0)
      for (int j = 0; j < root.getViewCount(); j++)
        dumpView(root.getView(j), level+1);
  }

  private void dumpDoc(Element elem, int level)
  {
    for (int i = 0; i < level*2; i++)
      System.out.print(' ');
    System.out.println(elem.getName() + "[" + elem.getStartOffset() + ", " + elem.getEndOffset() + "] " + elem.hashCode());
    if (!elem.isLeaf() && elem.getElementCount() != 0)
      for (int j = 0; j < elem.getElementCount(); j++)
        dumpDoc(elem.getElement(j), level+1);
  }

	public boolean isEditable()
	{
		return t_.isEditable();
	}
	
	public Object getAddee()
	{
		return getBorderee();
	}

	public Container getComponent()
	{
    /*
		if (s_ != null)
			return s_;
		else
			return t_;
    */
    
    return t_;
	}
	
	protected JTextComponent getTextComponent()
	{
    return t_;
  }

  public void initAsCellEditor()
  {
    if (t_ instanceof JTextField)
    {
      t_.setBorder(null);
      t_.setEditable(true);
    }
  }

  public boolean forwardKeyBinding(KeyStroke ks,
                                   KeyEvent  e,
                                   int       condition,
                                   boolean   pressed)
  {
    boolean ret = false;
    if (t_ instanceof JTextField)
    {
      com.inqwell.any.client.swing.JTextField t = (com.inqwell.any.client.swing.JTextField)t_;
      ret = t.processKeyBinding(ks, e, condition, pressed);
    }
    return ret;
  }

  protected Object getScroller()
  {
    return s_;
  }
  
	public void setReplaceParagraphAttributes(Map attribs)
	{
		if (t_ instanceof javax.swing.JTextPane)
		{
			AnyAttributeSet s = new AnyAttributeSet(attribs);
			javax.swing.JTextPane t = (javax.swing.JTextPane)t_;
		  t.setParagraphAttributes(s.getAttributeSet(), true);
		}
	}

	public void setMergeParagraphAttributes(Map attribs)
	{
		if (t_ instanceof javax.swing.JTextPane)
		{
			AnyAttributeSet s = new AnyAttributeSet(attribs);
			javax.swing.JTextPane t = (javax.swing.JTextPane)t_;
		  t.setParagraphAttributes(s.getAttributeSet(), false);
		}
	}

	public Map getMergeParagraphAttributes()
	{
    return getReplaceParagraphAttributes();
  }

	public Map getReplaceParagraphAttributes()
	{
//		if (t_ instanceof javax.swing.JTextPane)
//		{
//			javax.swing.JTextPane t = (javax.swing.JTextPane)t_;
//			return t.getParagraphAttributes();
//		}
//		else
//		{
//			return null;
//		}
		return null;
	}

	public void setRows(int rows)
	{
		if (t_ instanceof JTextArea)
		{
			((JTextArea)t_).setRows(rows);
      if (s_ != null)
        s_.setPreferredSize(t_.getPreferredSize());
		}
	}

	public void requestFocus()
	{
		super.requestFocus();
		setCaretVisible(true);
	}

	public void setLineWrap(boolean wrap)
	{
		if (t_ instanceof JTextArea)
		{
			JTextArea t = (JTextArea)t_;
			t.setLineWrap(wrap);
		}
	}

	public void setWrapStyleWord(boolean wrap)
	{
		if (t_ instanceof JTextArea)
		{
			JTextArea t = (JTextArea)t_;
			t.setWrapStyleWord(wrap);
      if (wrap)
        t.setLineWrap(wrap);
		}
	}

	public void setColumns(int columns)
	{
		if (t_ instanceof JTextField)
		{
			((JTextField)t_).setColumns(columns);
		}
		if (t_ instanceof JTextArea)
		{
			((JTextArea)t_).setColumns(columns);
		}
	}

	public void setScrollable(JScrollPane o)
	{
		//System.out.println("AnyText.setScrollable: " + o);
		if (!(o instanceof JScrollPane))
			throw new IllegalArgumentException
									("setScrollable not a javax.swing.JScrollPane");


		s_ = (JScrollPane)o;

		if (t_ != null)
		{
			s_.setViewportView(t_);
		}
	}

  public void updateModel() throws AnyException
  {
    // If the renderer says we are an editor update our
    // data node.
    if (getRenderInfo() != null)
    {
      Any a = getRenderInfo().resolveResponsibleData(getContextNode());
      if (a != null)
      {
        AnyFormat f = getRenderInfo().getFormat(a);
        f.parseAny(t_.getText(), a, true);
      }
    }
  }

  protected void setValueToComponent(Any v)
  {
    RenderInfo r = getRenderInfo();
    if (r != null)
    {
      Format f = r.getFormat(v);
      t_.setText(f.format(v));
    }
  }

  public boolean contains (Any key)
  {
    if (key.equals(AnyComponent.modelKey__))
    {
      return true;
    }

    return super.contains(key);
  }
  
	protected boolean beforeAdd(Any key, Any value)
	{
	  if (key.equals(AnyComponent.modelKey__))
	  {
		  if (!(value instanceof AnyDocument))
	      throw new IllegalArgumentException("Cannot add non-Document as 'model' to text components");

      AnyDocument d = (AnyDocument)value;
		  t_.setDocument(d.getDocument());
		  return false;
	  }

		return super.beforeAdd(key, value);
	}

	protected Object getPropertyOwner(Any property)
	{
		if (textProperties__.contains(property))
		  return this;
		
		return super.getPropertyOwner(property);
	}

  protected void setMargin(Insets i)
  {
    t_.setMargin(i);
  }
  
  protected boolean handleBoundEvent(Event e)
  {
    boolean ret = true;
    Any id = e.getId();
    if (id.equals(EventConstants.D_INSERT) ||
        id.equals(EventConstants.D_CHANGE) ||
        id.equals(EventConstants.D_REMOVE))
    {
      ret = docEventsWhenNotEditable_ || (t_.isEnabled() && t_.isEditable());
    }
    return ret && super.handleBoundEvent(e);
  }

  /**
	 * Solicit the document events to update our model.
	 */
	protected void initUpdateModel()
	{
    setupEventSet(t_.getDocument());
    addAdaptedEventListener(new ModelUpdateListener(documentEventTypes__));
	}

  private class ModelUpdateListener extends EventBinding
  {
    public ModelUpdateListener(Array eventTypes)
    {
      super(eventTypes, false);
    }

		protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
		{
      // When the control cannot accept input then we don't bother
      // with document events updating the model. The only problem
      // with this is if script sets the text property and expects
      // the model to be updated.
      if (!modelGuard_ && t_.isEditable() && t_.isEnabled())
			  updateModel();
			return null;
		}
  }
}
