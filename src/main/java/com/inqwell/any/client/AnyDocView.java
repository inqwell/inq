/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyDocView.java $
 * $Author: sanderst $
 * $Revision: 1.13 $
 * $Date: 2011-05-07 22:15:12 $
 */

package com.inqwell.any.client;

import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.StyledDocument;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFormat;
import com.inqwell.any.AnyFuncHolder;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.Call;
import com.inqwell.any.ConstString;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.Func;
import com.inqwell.any.Globals;
import com.inqwell.any.Map;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.StringI;
import com.inqwell.any.Transaction;
import com.inqwell.any.beans.AnyEvent;
import com.inqwell.any.beans.TextF;

public abstract class AnyDocView extends    AnySimpleComponent
                                 implements TextF
{
	private AnyDocument document_;
	
	// Optional document mutation and verification expressions
	private AnyFuncHolder.FuncHolder insertExpr_;
	private AnyFuncHolder.FuncHolder inputVerifier_;
  
	private   boolean     selectOnFocus_  = false;
  protected boolean     modelGuard_;
  private   boolean     modelFiring_    = false;
  
  // Parameter names to the verifier function
	static private Any  str__    = AbstractValue.flyweightString("text");
  static public  Any  val__    = AbstractValue.flyweightString("value");
  static public  Any  newVal__ = AbstractValue.flyweightString("newValue");
	static public  Any  fmt__    = AbstractValue.flyweightString("formatter");

	public  static Set  documentEvents__;
	private static Set  docViewProperties__;
	
  public  static Array documentEventTypes__    = AbstractComposite.array();

  protected  static Array focusEventTypes__ = AbstractComposite.array();

  private static Any  validateInsert__       = new ConstString("validateInsert");
	private static Any  inputVerifier__        = new ConstString("inputVerifier");
	//private static Any  verificationPattern__  = new ConstString("verificationPattern");
	private static Any  selectOnFocus__        = new ConstString("selectOnFocus");
  private static Any  caretPosition__        = new ConstString("caretPosition");
  
  private static AnyAttributeSet attrSet__ = new AnyAttributeSet();

  static public  Any  attrSet___ = AbstractValue.flyweightString("style");

  static
	{
		documentEvents__ = AbstractComposite.set(2);
		documentEvents__.add(ListenerConstants.DOCUMENT);
		documentEvents__.add(ListenerConstants.UNDOABLEEDIT);

    docViewProperties__ = AbstractComposite.set();
    docViewProperties__.add(validateInsert__);
    docViewProperties__.add(inputVerifier__);
    docViewProperties__.add(selectOnFocus__);
    docViewProperties__.add(caretPosition__);
    //docViewProperties__.add(verificationPattern__);

		focusEventTypes__.add(EventConstants.E_FOCUSGAINED);
		focusEventTypes__.add(EventConstants.E_FOCUSLOST);
    
    documentEventTypes__.add(EventConstants.D_CHANGE);
    documentEventTypes__.add(EventConstants.D_INSERT);
    documentEventTypes__.add(EventConstants.D_REMOVE);
  }
  
  public void setValidateInsert(Any insert)
  {
    AnyFuncHolder.FuncHolder insertExpr =
      AnyComponent.verifyCallFuncHolder(insert);
    
    JTextComponent t = getTextComponent();
    if (insertExpr != null)
    {
      insertExpr_ = (AnyFuncHolder.FuncHolder)insertExpr.cloneAny();
      t.setDocument(new DelegatingDocument(t.getDocument()));
      setupEventSet(t.getDocument());
    }
    else
    {
      if (insertExpr_ != null)
      {
        // Removing a previously installed insert validator
        // Fetch the DelegatingDocument and reinsall the
        // component's original document it contains
        DelegatingDocument dd = (DelegatingDocument)t.getDocument();
        t.setDocument(dd.getUnderlyingDocument());
      }
      insertExpr_ = insertExpr;
    }
  }

  public void setInputVerifier(Any verifier)
  {
    if (!AnyNull.isNull(verifier))
    {
      AnyComponent.verifyCallFuncHolder(verifier);
      inputVerifier_ = (AnyFuncHolder.FuncHolder)verifier.cloneAny();
      getTextComponent().setInputVerifier(new DocVerifier());
    }
    else
      getTextComponent().setInputVerifier(null);
  }
  
  public void setVerificationPattern(StringI pattern)
  {
    //verificationPattern_ = pattern;
  }

  public Any getValidateInsert() { return insertExpr_;    }
  public Any getInputVerifier()  { return inputVerifier_; }
  
	public void setCaretVisible(boolean visible)
	{
		Caret c = getTextComponent().getCaret();
		c.setVisible(visible);
	}

  public void setCaretPosition(int position)
  {
    if (position < 0)
    {
      Document d = getTextComponent().getDocument();
      position = d.getLength();
    }
    getTextComponent().setCaretPosition(position);
  }
  
  public int getCaretPosition()
  {
    return getTextComponent().getCaretPosition();
  }
  
  public void setObject(Object o)
  {
    // Set up a place holder for the old value during focus in/out
    this.add(val__, AnyNull.instance());
    super.setObject(o);
  }
  
	public void setSelectOnFocus(boolean selectOnFocus)
	{
    selectOnFocus_ = selectOnFocus;
	}
	
	public boolean getSelectOnFocus()
	{
    return selectOnFocus_;
	}
	
  public Any get (Any key)
  {
    if (key.equals(AnyComponent.modelKey__))
    {
      if (document_ == null)
        document_ = new AnyDocument(getTextComponent().getDocument());
      else
        document_.setDocument(getTextComponent().getDocument());
      
      return document_;
    }

    return super.get(key);
  }

  public Any getIfContains(Any key)
  {
    if (key.equals(AnyComponent.modelKey__))
    {
      if (document_ == null)
        document_ = new AnyDocument(getTextComponent().getDocument());
      else
        document_.setDocument(getTextComponent().getDocument());
      
      return document_;
    }
    return super.getIfContains(key);
  }
  
  protected void componentProcessEvent(Event e) throws AnyException
  {
    modelGuard_ = true;

    if (!modelFiring_)
    {
      super.componentProcessEvent(e);
    }
    modelFiring_ = false;
    
    modelGuard_ = false;
  }
  
	protected Object getAttachee(Any eventType)
	{
		if (documentEvents__.contains(eventType))
			return getTextComponent().getDocument();
		else
			return super.getAttachee(eventType);
	}

	protected Object getPropertyOwner(Any property)
	{
		if (docViewProperties__.contains(property))
		  return this;
		
		return super.getPropertyOwner(property);
	}
	
	protected void focusLost(boolean isTemporary)
	{
    // If we have an input verifier (leave focus) or an input
    // validator (document change) then set any formatter to
    // use separators when the component loses the focus and
    // re-render the current value
    if (!isTemporary && (inputVerifier_ != null || insertExpr_ != null))
    {
      RenderInfo r;
      if ((r = getRenderInfo()) != null)
      {
        Any val = null;
        try
        {
          val = r.resolveResponsibleData(getContextNode());
          //System.out.println("focusLost " + val);
          AnyFormat f = r.getFormat(val);
          if (f.hasGrouping())
          {
            f.setGroupingUsed(true);
            f.setTrailingZeros(true);
            val = getRenderInfo().resolveDataNode(getContextNode(), false);
            modelGuard_ = true;
            setValueToComponent(val);
            modelGuard_ = false;
          }
        }
        catch(AnyException e)
        {
          throw new RuntimeContainedException(e);
        }
      }
    }
	}

  protected void focusGained(boolean isTemporary)
  {
    // If we have an input verifier (leave focus) or an input
    // validator (document change) then set any formatter not
    // to use separators while the focus is on the component.
    if (inputVerifier_ != null || insertExpr_ != null)
    {
      RenderInfo r;
      if ((r = getRenderInfo()) != null)
      {
        Any val = null;
        try
        {
          val = r.resolveResponsibleData(getContextNode());
          if (val != null)
          {
            //System.out.println("focusGained " + val);
            AnyFormat f = r.getFormat(val);
            if (f.hasGrouping())
            {
              f.setGroupingUsed(false);
              f.setTrailingZeros(false);
              val = getRenderInfo().resolveDataNode(getContextNode(), false);
              modelGuard_ = true;
              setValueToComponent(val);
              modelGuard_ = false;
            }
            setupFocusGainedVal(val);
          }
        }
        catch(AnyException e)
        {
          throw new RuntimeContainedException(e);
        }
  
      }
    }
    if (selectOnFocus_)
      getTextComponent().selectAll();
      
  }
  
  protected void setupFocusGainedVal(Any val)
  {
    Any oldVal = this.get(val__);
    if (oldVal.getClass() != val.getClass())
      this.replaceItem(val__, val.buildNew(val));
    else
      oldVal.copyFrom(val);
  }
  
	protected abstract JTextComponent getTextComponent();

  protected boolean handleBoundEvent(Event e)
  {
    if (e.getUnderlyingEvent() instanceof MouseEvent)
    {
      // Add the component mouse coordinates
      MouseEvent me = (MouseEvent)e.getUnderlyingEvent();

      iX__.setValue(me.getX());
      iY__.setValue(me.getY());
      e.add(x__, iX__);
      e.add(y__, iY__);

      JTextComponent jt = getTextComponent();
      int pos = jt.viewToModel(me.getPoint());

      if (pos >= 0)
      {
        Document d = jt.getDocument();
        if (d instanceof StyledDocument)
        {
          StyledDocument sd = (StyledDocument)jt.getDocument();
          Element el = sd.getCharacterElement(pos);
          AttributeSet as = el.getAttributes();
          attrSet__.setAttributeSet((MutableAttributeSet)as);
          e.add(attrSet___, attrSet__);
        }
      }
    }
    
    // When focus is lost put the old value (set up in the focusGained()
    // method) into the event for the benefit of Inq handlers. They can
    // already get at the rendered value with @component.renderedValue.
    // Guard against coming through here mode than once - the Inq runtime
    // as well as user script may use focus event handlers.
    if (e.getId().equals(EventConstants.E_FOCUSLOST) && !e.contains(val__))
    {
      e.add(val__, this.get(val__));
    }

    return true;
  }

  public EventBinding makeEventBinding(Func expr, Array eventTypes, boolean consume, boolean busy, boolean modelFires)
  {
    if (eventTypes.containsAny(documentEventTypes__))
      return new DocumentEventBinding(expr, eventTypes, consume, busy, modelFires);
    
    return super.makeEventBinding(expr, eventTypes, consume, busy, modelFires);
  }

  protected class DocumentEventBinding extends EventBinding
  {
    public DocumentEventBinding(Func expr, Array eventTypes, boolean consume, boolean busy, boolean modelFires)
    {
      super(expr, eventTypes, consume, busy, modelFires);
    }

    protected boolean doFireModel(Transaction t, Event e) throws AnyException
    {
      if (super.doFireModel(t, e))
      {
        modelFiring_ = true;
        modelFireGuard();
        return true;
      }
      return false;
    }
  }
  
  protected class TextFocusListener extends EventBinding
  {
    public TextFocusListener(Array eventTypes)
    {
      super(eventTypes, false);
    }

		protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
		{
      Any id = e.getId();
      
      BooleanI isTemporary = (BooleanI)e.get(ListenerAdapterFactory.isTemporary__);
      
      if (id.equals(EventConstants.E_FOCUSLOST))
        focusLost(isTemporary.getValue());
      else
        focusGained(isTemporary.getValue());
      
      return null;
		}
  }

  protected class DocVerifier extends InputVerifier
  {
    private EventBinding b_;  // not really but require txn semantics.
    private BooleanI     res_ = new AnyBoolean();
    private StringI      str_ = new AnyString();;
    
    protected DocVerifier()
    {
      final Call inputVerifier = (Call)inputVerifier_.getFunc();
      
      b_ = new EventBinding(inputVerifier, null, true, false)
               {
                 protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
                 {
                   JTextComponent tf = getTextComponent();
                   String str = tf.getText();
                   
                   /*
                   if (docArgs_ == null)
                   {
                     docArgs_ = inputVerifier_.getArgs();
                     if (docArgs_ == null)
                       docArgs_ = AbstractComposite.simpleMap();
                     else
                       docArgs_ = (Map)docArgs_.cloneAny();
                   }
                   */
                   
                   Map docArgs = inputVerifier.getArgs();
                   Map args = docArgs; // put originals back afterwards.
                   
                   if (docArgs == null)
                     docArgs = AbstractComposite.simpleMap();
                   else
                     docArgs = (Map)docArgs.cloneAny();

                   str_.setValue(str);
                   docArgs.add(str__, str_);
                   
                   // Try to resolve the Any we might be associated with
                   // to pass a clone of it to the verification function
                   Any       val    = null;
                   AnyFormat fmt    = null;
                   
                   RenderInfo r = AnyDocView.this.getRenderInfo();
                   if (r != null)
                   {
                     val = r.resolveResponsibleData(context);
                     if (val != null)
                     {
                       fmt    = r.getFormat(val);
                       val    = val.cloneAny();
                     }
                   }

                   docArgs.add(val__, val);
                   docArgs.add(fmt__, fmt);
                   
                   // Put the component in as well
                   docArgs.add(AnyView.component__, AnyDocView.this);
                   
                   inputVerifier.setArgs(docArgs);

                   Any ret = null;
                   try
                   {
                     ret = super.execExpr(t, context, expr, e);
                   }
                   finally
                   {
                     //docArgs_.empty();
                     inputVerifier.setArgs(args);
                   }

                   res_.copyFrom(ret);
                   
                   // If the value is acceptable then check if the
                   // verifier changed the string and if so put it
                   // back into the text field.  Then update the model.
                   if (res_.getValue() &&
                       str_.toString() != str)
                   {
                     tf.setText(str_.toString());
                   }
                   return res_; 
                 }
               };
    }
    
    public boolean verify(JComponent input)
    {
      // Ignore if the context is not set yet
      if (getContextNode() == null)
        return true;
      
      try
      {
        b_.processEvent(new AnyEvent(new FocusEvent(input, FocusEvent.FOCUS_LOST),
                                     EventConstants.E_FOCUSLOST));
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
      if (!res_.getValue())
        Toolkit.getDefaultToolkit().beep();

      return res_.getValue();
    }
  }
  
  /**
   * Intercepts the method calls defined by javax.swing.text.Document
   * so that specific functionality can be imposed.  The insertString
   * and remove methods are currently the only ones implemented and
   * call the Inq expressions installed with setInsertExpr and
   * setRemoveExpr
   */
  protected class DelegatingDocument implements Document
  {
    private Document d_;

    DelegatingDocument(Document d)
    {
      d_ = d;
    }
    
    public void addDocumentListener(DocumentListener listener)
    {
      d_.addDocumentListener(listener);
    }

    public void addUndoableEditListener(UndoableEditListener listener)
    {
      d_.addUndoableEditListener(listener);
    }

    public Position createPosition(int offs) throws BadLocationException
    {
      return d_.createPosition(offs);
    }

    public Element getDefaultRootElement()
    {
      return d_.getDefaultRootElement();
    }

    public Position getEndPosition()
    {
      return d_.getEndPosition();
    }

    public int getLength()
    {
      return d_.getLength();
    }

    public Object getProperty(Object key)
    {
      return d_.getProperty(key);
    }

    public Element[] getRootElements()
    {
      return d_.getRootElements();
    }

    public Position getStartPosition()
    {
      return d_.getStartPosition();
    }

    public String getText(int offset, int length) throws BadLocationException
    {
      return d_.getText(offset, length);
    }

    public void getText(int offset, int length, Segment txt)
                                         throws BadLocationException
    {
      d_.getText(offset, length, txt);
    }

    public void insertString(int offset, String str, AttributeSet a)
                                                   throws BadLocationException
    {
      if (insertExpr_ != null && str.length() != 0)
      {
        /*
        if (docArgs_ == null)
          docArgs_ = AbstractComposite.simpleMap();
        */
        
        Call insertExpr = (Call)insertExpr_.getFunc();
        Map docArgs = insertExpr.getArgs();
        Map args = docArgs; // put originals back afterwards.
        if (docArgs == null)
          docArgs = AbstractComposite.simpleMap();
        else
          docArgs = (Map)docArgs.cloneAny();
          
        docArgs.add(str__, new AnyString(str));
        
        // Try to resolve the Any we might be associated with
        // to pass a clone of it to the verification function
        Any       val = null;
        AnyFormat fmt = null;
        RenderInfo r = AnyDocView.this.getRenderInfo();
        if (r != null)
        {
          try
          {
            val = r.resolveResponsibleData(getContextNode());
            fmt = r.getFormat(val);
            if (val != null)
              val = val.cloneAny();
          }
          catch(AnyException e)
          {
            throw new RuntimeContainedException(e);
          }
        }

        docArgs.add(val__, val);
        docArgs.add(fmt__, fmt);
        docArgs.add(AnyView.component__, AnyDocView.this);
        
        Transaction t = Globals.process__.getTransaction();
        insertExpr.setTransaction(t);
        insertExpr.setArgs(docArgs);

        Any ret = null;
        try
        {
          ret = insertExpr.exec(getContextNode());
        }
        catch (AnyException e)
        {
          throw new RuntimeContainedException(e);
        }
        finally
        {
          //docArgs_.empty();
          insertExpr.setArgs(args);
          insertExpr.setTransaction(Transaction.NULL_TRANSACTION);
        }
        if (ret != null && ret != AnyNull.instance())  // only latter really
          str = ret.toString();
        else
        {
          Toolkit.getDefaultToolkit().beep();
          return;
        }
      }
      d_.insertString(offset, str, a);
    }

    public void putProperty(Object key, Object value)
    {
      d_.putProperty(key, value);
    }

    public void remove(int offs, int len) throws BadLocationException
    {
      d_.remove(offs, len);
    }

    public void removeDocumentListener(DocumentListener listener)
    {
      d_.removeDocumentListener(listener);
    }

    public void removeUndoableEditListener(UndoableEditListener listener)
    {
      d_.removeUndoableEditListener(listener);
    }

    public void render(Runnable r)
    {
      d_.render(r);
    }
    
    private Document getUnderlyingDocument()
    {
      return d_;
    }
  }

}
           
