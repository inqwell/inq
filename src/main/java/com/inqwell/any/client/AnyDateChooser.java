/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyDateChooser.java $
 * $Author: sanderst $
 * $Revision: 1.8 $
 * $Date: 2011-05-04 12:31:11 $
 */

package com.inqwell.any.client;

import java.awt.Container;
import java.util.Calendar;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyCalendar;
import com.inqwell.any.AnyDate;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFormat;
import com.inqwell.any.AnyFuncHolder;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyTimeZone;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.Call;
import com.inqwell.any.DateI;
import com.inqwell.any.Event;
import com.inqwell.any.Func;
import com.inqwell.any.Globals;
import com.inqwell.any.Map;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.Transaction;
import com.inqwell.any.client.swing.JDateChooser;
import com.inqwell.any.client.swing.JPanel;
import com.toedter.calendar.DateVerifier;

public class AnyDateChooser extends AnyDocView
{
  private JDateChooser    d_;
  
  // Working date for Any interfacing
  private AnyDate         date_ = new AnyDate();
  
  private JComponent      borderee_;
  
  private AnyFuncHolder.FuncHolder dateVerifier_;

  private static Set      chooserProperties__;
  
  static private Any  globalToday__  = AbstractValue.flyweightString("globalToday");
  static private Any  dateVerifier__ = AbstractValue.flyweightString("dateVerifier");
  static private Any  globalDateVerifier__ = AbstractValue.flyweightString("globalDateVerifier");
  static private Any  vDate__        = AbstractValue.flyweightString("vDate");
  static private Any  vCal__         = AbstractValue.flyweightString("vCalendar");

  static private AnyFuncHolder.FuncHolder globalDateVerifierF__;

  static private Any  minSelectableDate__  = AbstractValue.flyweightString("minSelectableDate");
  static private Any  maxSelectableDate__  = AbstractValue.flyweightString("maxSelectableDate");

  static
	{
    chooserProperties__ = AbstractComposite.set();
    chooserProperties__.add(globalToday__);
    chooserProperties__.add(dateVerifier__);
    chooserProperties__.add(minSelectableDate__);
    chooserProperties__.add(maxSelectableDate__);
    chooserProperties__.add(globalDateVerifier__);
  }

	public void setObject(Object o)
	{
		if (!(o instanceof JDateChooser))
			throw new IllegalArgumentException
									("Not a JDateChooser");

    d_ = (JDateChooser)o;

    borderee_ = new JPanel();
    borderee_.add(d_);
    

    super.setObject(d_);
		setupEventSet(getTextComponent());
		addAdaptedEventListener(new TextFocusListener(focusEventTypes__));
    d_.setDateVerifier(new VerifyDate());
	}
	
  public Container getComponent()
  {
    return d_;
  }

	public JComponent getBorderee()
	{
		return borderee_;
	}

	public Object getAddee()
	{
		return getBorderee();
	}

	protected Object getAttachee(Any eventType)
	{
    // Hmmm... for the focus event we want the text component, so
    // it actually works (JDateChooser is a compound component, how
    // better to deal with this?)
		if (eventType.equals(ListenerConstants.FOCUS))
    {
			return getTextComponent();
    }
		else
			return super.getAttachee(eventType);
	}

  protected void setValueToComponent(Any v)
  {
    setDate(v);
  }

	/**
   */
	public void setRenderInfo(RenderInfo r)
	{
    if (r != null)
    {
      String s = r.getFormatString();
      
      // If none specified use the component's default
      if (s != null)
        d_.setDateFormatString(s);
      else
        r.setFormat(d_.getDateFormatString());
      
      ((JTextField)getTextComponent()).setColumns(r.getWidth());
    }
    
    super.setRenderInfo(r);
	}

	// Actually not all that useful - you can still alter the date
	// using the popup even if you can't via the text editor
	public void setEditable(boolean editable)
	{
	  d_.setEditable(editable);
    super.setEditable(editable);
	}

  public void setGlobalToday(AnyDate date)
  {
    d_.setGlobalToday(date);
  }
  
  public void setDateVerifier(Any verifier)
  {
    if (!AnyNull.isNull(verifier))
    {
      AnyComponent.verifyCallFuncHolder(verifier);
      dateVerifier_ = (AnyFuncHolder.FuncHolder)verifier.cloneAny();
    }
    else
      dateVerifier_ = null;
  }

  public Any getDateVerifier()
  {
    return dateVerifier_;
  }

  public void setGlobalDateVerifier(Any verifier)
  {
    if (!AnyNull.isNull(verifier))
    {
      AnyComponent.verifyCallFuncHolder(verifier);
      globalDateVerifierF__ = (AnyFuncHolder.FuncHolder)verifier.cloneAny();
    }
    else
      globalDateVerifierF__ = null;
  }

  public Any getGlobalDateVerifier()
  {
    return globalDateVerifierF__;
  }

  public void setMinSelectableDate(Any date)
  {
    if (date == AnyNull.instance() || date == null)
      d_.setMinSelectableDate(null);
    else
    {
      if (!(date instanceof DateI))
        throw new IllegalArgumentException(date.getClass().toString());
      DateI d = (DateI)date;
      d_.setMinSelectableDate(d.isNull() ? null : d.getValue());
    }
  }
  
  public Any getMinSelectableDate()
  {
    return new AnyDate(d_.getMinSelectableDate());
  }

  public void setMaxSelectableDate(Any date)
  {
    if (date == AnyNull.instance() || date == null)
      d_.setMaxSelectableDate(null);
    else
    {
      if (!(date instanceof DateI))
        throw new IllegalArgumentException(date.getClass().toString());
      DateI d = (DateI)date;
      d_.setMaxSelectableDate(d.isNull() ? null : d.getValue());
    }
  }
  
  public Any getMaxSelectableDate()
  {
    return new AnyDate(d_.getMaxSelectableDate());
  }

  public void updateModel() throws AnyException
	{
    // The date was changed by the user - update the model
	  RenderInfo r = getRenderInfo();
	  
	  if (r != null)
	  {
      Any dataItem = r.resolveDataNode(getContextNode(), false);
  
      if (dataItem != null)
      {
        AnyFormat f = r.getFormat(dataItem);
        
        d_.getDateFormat().setTimeZone(((AnyTimeZone)f.getTimeZone()).getTimeZone());
        date_.setValue(d_.getDate());
        if (!date_.isNull())
        {
          // Flatten according to the prevailing format:
          f.parseAny(f.format(date_), date_, false);
        }
        dataItem.copyFrom(date_);
      }
	  }
	}
  
	protected JTextComponent getTextComponent()
	{
	  return d_.getTextComponent();
  }
	
	protected void focusGained(boolean isTemporary)
	{
	  super.focusGained(isTemporary);
    if (getSelectOnFocus())
    {
      JTextComponent t = getTextComponent();
      t.setText(t.getText());
      t.selectAll();
    }
	}

	/**
	 * Perform any processing required to update the model data
	 * this component is viewing.
	 */
	protected void initUpdateModel()
	{
    // Establish an event listener on JDateChooser (or one of its
    // subcomponents) that allows us to update the model when the
    // date is changed via the GUI.
		addAdaptedEventListener(new DateChangedListener(changeEventType__));
	}

	protected Object getPropertyOwner(Any property)
	{
		if (chooserProperties__.contains(property))
		  return this;
		
		return super.getPropertyOwner(property);
	}

  private void setDate(Any date)
  {
    date_.copyFrom(date);

    if (date_.isNull())
      d_.setDate(null);
    else
      d_.setDate(date_.getValue());
  }
  
  class DateChangedListener extends EventBinding
  {
    public DateChangedListener(Array eventTypes)
    {
      super(eventTypes, false);
    }

		protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
		{
			updateModel();
			return null;
		}
  }

  protected class VerifyDate implements DateVerifier
  {
    private BooleanI     res_      = new AnyBoolean();
    private DateI        date_     = new AnyDate();
    private AnyCalendar  calendar_ = new AnyCalendar(Calendar.getInstance());
    
    public boolean valid(JComponent input, Calendar calendar)
    {
      // Ignore if the context is not set yet
      if (getContextNode() == null)
        return true;

      Call dateVerifier = null;
      if (dateVerifier_  != null)
        dateVerifier = (Call)dateVerifier_.getFunc();
      else if (globalDateVerifierF__ != null)
        dateVerifier = (Call)globalDateVerifierF__.getFunc();

      if (dateVerifier == null)
        return true;

      Map context = Globals.process__.getContext();
      Any contextPath = Globals.process__.getContextPath();
      
      try
      {
        Globals.process__.setContext((Map)getContextNode());
        Globals.process__.setContextPath(getContext());
        
        date_.setTime(calendar.getTimeInMillis());
        calendar_.setTime(calendar.getTimeInMillis());
        
        Map docArgs = dateVerifier.getArgs();
        
        if (docArgs == null)
          docArgs = AbstractComposite.simpleMap();

        docArgs.replaceItem(vDate__, date_);
        docArgs.replaceItem(vCal__,  calendar_);
        
        Any       val    = null;
        AnyFormat fmt    = null;
        
        RenderInfo r = AnyDateChooser.this.getRenderInfo();
        if (r != null)
        {
          val = r.resolveResponsibleData(context);
          if (val != null)
          {
            fmt    = r.getFormat(val);
            // Or would it be better to make const?
            //val    = val.cloneAny();
          }
        }

        docArgs.replaceItem(val__, val);
        docArgs.replaceItem(fmt__, fmt);
        
        // Put the component in as well
        docArgs.replaceItem(AnyView.component__, AnyDateChooser.this);
        
        dateVerifier.setArgs(docArgs);

        Any ret = Call.call(dateVerifier, null);
        
        res_.copyFrom(ret);
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
      catch(Exception e)
      {
        throw new RuntimeContainedException(e);
      }
      finally
      {
        Globals.process__.setContext(context);
        Globals.process__.setContextPath(contextPath);
      }

      return res_.getValue();
    }
  }
}
