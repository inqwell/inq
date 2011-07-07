/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnySpinner.java $
 * $Author: sanderst $
 * $Revision: 1.7 $
 * $Date: 2011-04-19 07:00:16 $
 */

package com.inqwell.any.client;

import java.awt.Container;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.AbstractVisitor;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyTimeZone;
import com.inqwell.any.Array;
import com.inqwell.any.ByteI;
import com.inqwell.any.Call;
import com.inqwell.any.CharI;
import com.inqwell.any.ConstString;
import com.inqwell.any.DateI;
import com.inqwell.any.Decimal;
import com.inqwell.any.DoubleI;
import com.inqwell.any.Event;
import com.inqwell.any.FloatI;
import com.inqwell.any.Func;
import com.inqwell.any.IntI;
import com.inqwell.any.LongI;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.ShortI;
import com.inqwell.any.Transaction;
import com.inqwell.any.client.swing.JPanel;

public class AnySpinner extends AnyDocView
{
  // Modes of operation for the Inq<-->Spinner visitor
  static private short CREATE_MODEL = 0;
  static private short GET_VALUE    = 1;
  static private short SET_VALUE    = 2;
  static private short SET_MAXIMUM  = 3;
  static private short SET_MINIMUM  = 4;
  
  private static String    today__    = "Today";
  private static KeyStroke keyToday__;
  private static Border    editorBorder__ = new EmptyBorder(3, 3, 3, 3); 

  private static DateI globalToday__;
  private static AnyTimeZone globalTimeZone__;

  private JSpinner         s_;
	private JComponent       borderee_;
  
  private SpinConverter    sc_;
  
  private boolean          guard_;
  
  private SpinFocusAdapter focusBinding_;
  
  //private SpinTextField   tf_;
  
	//private AnySpinnerModel model_;

  //AnyString v_ = new AnyString();
    
	private static Set      spinnerProperties__;
	private static Any      columns__        = new ConstString("columns");
//	private static Any      prevValue__      = new ConstString("prevValue");
//	private static Any      nextValue__      = new ConstString("nextValue");
  //public  static Array    spinEventTypes__ = AbstractComposite.array();

	static
	{
    spinnerProperties__ = AbstractComposite.set();
    spinnerProperties__.add(columns__);
    spinnerProperties__.add(AbstractValue.flyweightString("globalToday"));
    spinnerProperties__.add(AbstractValue.flyweightString("globalTimeZone"));
    spinnerProperties__.add(AbstractValue.flyweightString("minimum"));
    spinnerProperties__.add(AbstractValue.flyweightString("maximum"));
    spinnerProperties__.add(AbstractValue.flyweightString("todaySet"));
    spinnerProperties__.add(AbstractValue.flyweightString("editorBackground"));
    spinnerProperties__.add(AbstractValue.flyweightString("editorForeground"));
    
//    spinnerProperties__.add(prevValue__);
//    spinnerProperties__.add(nextValue__);

//		spinEventTypes__.add(EventConstants.SPINEDIT_ACTION);
//		spinEventTypes__.add(EventConstants.SPINEDIT_FOCUS);
    
    keyToday__ = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                                        ActionEvent.CTRL_MASK,
                                        false);
  }

	public void setObject(Object o)
	{
		if (!(o instanceof JSpinner))
			throw new IllegalArgumentException
									("AnySpinner wraps javax.swing.JSpinner and sub-classes");

    s_ = (JSpinner)o;

    // Create the converter visitor
    sc_ = new SpinConverter();
    
    // Match L&F text field border so spinners are (approx) the same height as
    // text fields. Otherwise the SpinnerUI removes the border on the
    // editor and the spinner is not as high. This is done here so the
    // spinner's preferred height is correct during layout. The default
    // editor is changed when the data type is known but if the geometry
    // has since fixed the height the result is that the spinner is cut off
    // at the bottom. Yes, spinners are a bit tricky...
    Insets i = UIManager.getLookAndFeelDefaults().getInsets("TextField.margin");
    Border b;
    if (i == null)
      b = editorBorder__;
    else
      b = new EmptyBorder(i);
    getTextComponent().setBorder(b);
    
    // Create the SpinFocusAdapter to reflect focus events. The editor
    // can change so we need to put something in the way to maintain
    // any listeners on it.
    focusBinding_ = new SpinFocusAdapter();
    
    // We will change the editor in effect to either a number editor
    // or a date editor, those being the Java implementations, but
    // the underlying component is a JFormattedTextField, so event sets
    // are always valid.
    setupEventSet(((JSpinner.DefaultEditor)s_.getEditor()).getTextField().getDocument());
    setupEventSet(focusBinding_);
    
    addAdaptedEventListener(new TextFocusListener(focusEventTypes__));

    // A tad messy but we need to keep track of when the editor
    // changes and keep the SpinFocusAdapter on it
    s_.addPropertyChangeListener(new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent e)
      {
        String propertyName = e.getPropertyName();
        if ("editor".equals(propertyName))
        {
          JComponent oldEditor = (JComponent)e.getOldValue();
          JComponent newEditor = (JComponent)e.getNewValue();
          if (oldEditor instanceof JSpinner.DefaultEditor)
          {
            JTextField tf = ((JSpinner.DefaultEditor)oldEditor).getTextField();
            if (tf != null)
              tf.removeFocusListener(focusBinding_);
          }
          if (newEditor instanceof JSpinner.DefaultEditor)
          {
            JTextField tf = ((JSpinner.DefaultEditor)newEditor).getTextField();
            if (tf != null)
              tf.addFocusListener(focusBinding_);
          }
        }
      }
    });
    
    borderee_ = new JPanel();
    borderee_.add(s_);

    /*
    tf_.addFocusListener(new FocusListener()
                         {
                           public void focusGained(FocusEvent e)
                           {
                           }
                           
                           public void focusLost(FocusEvent e) 
                           {
                             updateFromEditor();
                           }
                         });
  
    tf_.addActionListener(new ActionListener()
                          {
                            public void actionPerformed(ActionEvent e) 
                            {
                              updateFromEditor();
                            }
                          });
     */
    
    // A default width
		setColumns(5);
    
		super.setObject(s_);
    
    
	}
	
  public Container getComponent()
  {
    return s_;
  }

	public Object getAddee()
	{
		return getBorderee();
	}

	public JComponent getBorderee()
	{
		return borderee_;
	}

	protected Object getAttachee(Any eventType)
	{
		if (eventType.equals(ListenerConstants.FOCUS))
			return focusBinding_;
		else
			return super.getAttachee(eventType);
	}

	public void setColumns(int columns)
	{
    JTextField t = ((JSpinner.DefaultEditor)s_.getEditor()).getTextField();
    t.setColumns(columns);
	}
	
  protected void focusGained(boolean isTemporary)
  {
    // Just override the base implementations for now while we
    // decide what to do...
    
    if (getSelectOnFocus())
    {
      selectText();
    }
 
    Any val = getRenderedValue();
    setupFocusGainedVal(val);
    
  }
  
  protected void focusLost(boolean isTemporary)
  {
  }
  
	public int getColumns()
	{
    JTextField t = ((JSpinner.DefaultEditor)s_.getEditor()).getTextField();
    return t.getColumns();
	}
	
	public void evaluateContext()
	{
    super.evaluateContext();
	}

	/**
   */
	public void setRenderInfo(RenderInfo r)
	{
    // Readjust to any specified width as this method is often called
    // prior to layout
    if (r != null)
    {
      setColumns(r.getWidth());
      super.setRenderInfo(r);
    }
	}
	
  public void setNextValue(Any nextF)
  {
    Call nextFunc = AnyComponent.verifyCall(nextF);
  }

  public void setPrevValue(Any prevF)
  {
    Call prevFunc = AnyComponent.verifyCall(prevF);
  }
  
  public Any getNextValue()
  {
    return null;
  }

  public Any getPrevValue()
  {
    return null;
  }
  
  public void setMinimum(Any value)
  {
    sc_.setMinimum(value);
  }
  
  public void setMaximum(Any value)
  {
    sc_.setMaximum(value);
  }
  
  public void setGlobalToday(DateI date)
  {
    globalToday__ = date;
  }
  
  public DateI getGlobalToday()
  {
    return (DateI)globalToday__.bestowConstness();
  }
  
  public void setGlobalTimeZone(AnyTimeZone timeZone)
  {
    globalTimeZone__ = timeZone;
  }
  
  public void setEditorForeground(AnyColor c)
  {
    this.getTextComponent().setForeground(c.getColor());
  }
  
  public void setEditorBackground(AnyColor c)
  {
    this.getTextComponent().setBackground(c.getColor());
  }
  
  public AnyColor getEditorForeground()
  {
    return new AnyColor(this.getTextComponent().getForeground());
  }
  
  public AnyColor getEditorBackground()
  {
    return new AnyColor(this.getTextComponent().getBackground());
  }
  
  public boolean isTodaySet()
  {
    SpinnerModel m = s_.getModel();
    if (m instanceof SpinnerDateModel)
    {
      SpinnerDateModel sdm = (SpinnerDateModel)m;
      return sdm.isTodaySet();
    }
    
    return false;
  }
  
  public void setTodaySet(boolean todaySet)
  {
    SpinnerModel m = s_.getModel();
    if (m instanceof SpinnerDateModel)
    {
      SpinnerDateModel sdm = (SpinnerDateModel)m;

      if (todaySet)
      {
        setTodaysDate(sdm);
      }
      else
        sdm.setTodaySet(false);
    }
  }
  
  protected void setValueToComponent(Any v)
  {
    sc_.setValueToSpinner(v);

    JTextField t = ((JSpinner.DefaultEditor)s_.getEditor()).getTextField();
    if (getSelectOnFocus())
      t.selectAll();
  }

	protected JTextComponent getTextComponent()
	{
    return ((JSpinner.DefaultEditor)s_.getEditor()).getTextField();
  }

	/**
	 * Perform any processing required to update the model data
	 * this component is viewing.
	 */
	protected void initUpdateModel()
	{
    addAdaptedEventListener(new SpinnerChangedListener(changeEventType__));
	}

	protected Object getPropertyOwner(Any property)
	{
		if (spinnerProperties__.contains(property))
		  return this;
		
		return super.getPropertyOwner(property);
	}
	  
  public void updateModel()
  {
    // The button got clicked - update the model.
  	try
  	{
      if (getRenderInfo() != null)
      {
        Any dataNode = getRenderInfo().resolveResponsibleData(getContextNode());
        // Set the value into the model. (Spinner already done)
        if (dataNode != null)
        {
          sc_.getValueFromSpinner(dataNode);
        }
      }
      if (!guard_)
        setTodaySet(false);
  	}
  	catch (AnyException e)
  	{
  		throw new RuntimeContainedException(e);
  	}
  }
  
  protected void setMargin(Insets i)
  {
    getTextComponent().setMargin(i);
  }
  
  private void selectText()
  {
    // Weird spinnerisms. Completely weird
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        getTextComponent().selectAll();
      }
    });
  }
  
  private void setupTodayAction()
  {
    getTextComponent().getInputMap().put(keyToday__, today__);
    getTextComponent().getActionMap().put
      (today__,
       new AbstractAction()
       {
         public void actionPerformed(ActionEvent e)
         {
           if (s_.getModel().getClass() == SpinnerDateModel.class)
             setTodaysDate((SpinnerDateModel)s_.getModel());
         }
       });

  }

  private void setTodaysDate(SpinnerDateModel m)
  {
    guard_ = true;
    // Find the current time to 1 second
    Calendar c = Calendar.getInstance();
    if (globalTimeZone__ != null)
      c.setTimeZone(globalTimeZone__.getTimeZone());
    
    c.setTimeInMillis(System.currentTimeMillis());
    int hour   = c.get(Calendar.HOUR_OF_DAY);
    int minute = c.get(Calendar.MINUTE);
    int second = c.get(Calendar.SECOND);
    
    if (globalToday__ != null)
    {
      // Take the current global date and set the time component
      // to now
      
      c.setTimeInMillis(globalToday__.getTime());
      
      c.set(Calendar.HOUR_OF_DAY, hour);
      c.set(Calendar.MINUTE, minute);
      c.set(Calendar.SECOND, second);
      
      m.setValue(c.getTime(), true);
    }
    else
      m.setValue(new Date(), true);
    
    // When here the model will have fired the state change, so we
    // can set the flag saying that we've set today's date
    //m.setTodaySet(true);
    guard_ = false;
  }

  private class SpinnerChangedListener extends EventBinding
  {
    public SpinnerChangedListener(Array eventTypes)
    {
      super(eventTypes, false, false);
    }

    protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
    {
      //if (!nodeEvent_)
      updateModel();
        
        if (getSelectOnFocus())
          selectText();
      return null;
    }
  }

  private class SpinConverter extends AbstractVisitor
  {
    private boolean modelCreated_ = false;
    private short   mode_;
    
    // Check if the current Java model installed in the
    // JSpinner is suitable for the given value. If so, leave
    // it alone. Otherwise set the spinner's model to a
    // Number or Date variant.
    private void setModelFor(Any value)
    {
      mode_ = CREATE_MODEL;
      value.accept(this);
    }
    
    // Get the current model value and assign to the given any
    private void getValueFromSpinner(Any value)
    {
      mode_ = GET_VALUE;
      value.accept(this);
    }
    
    // Get the current model value and assign to the given any
    private void setValueToSpinner(Any value)
    {
      if (value != null)
      {
        if (!modelCreated_)
        {
          setModelFor(value);
          modelCreated_ = true;
        }
        mode_ = SET_VALUE;
        value.accept(this);
      }
    }
    
    private void setMaximum(Any value)
    {
      if (value != null)
      {
        if (!modelCreated_)
        {
          setModelFor(value);
          modelCreated_ = true;
        }
        mode_ = SET_MAXIMUM;
        value.accept(this);
      }
    }
    
    private void setMinimum(Any value)
    {
      if (value != null)
      {
        if (!modelCreated_)
        {
          setModelFor(value);
          modelCreated_ = true;
        }
        mode_ = SET_MINIMUM;
        value.accept(this);
      }
    }
    
    public void visitAnyByte (ByteI b)
    {
      if (mode_ == CREATE_MODEL)
        numericModel(new Byte((byte)0));
      else if (mode_ == GET_VALUE)
      {
        Byte jb = (Byte)((SpinnerNumberModel)s_.getModel()).getValue();
        b.setValue(jb.byteValue());
      }
      else if (mode_ == SET_VALUE)
      {
        if (b.isNull())
          ((SpinnerNumberModel)s_.getModel()).setValue(new Byte((byte)0));
        else
          ((SpinnerNumberModel)s_.getModel()).setValue(new Byte(b.getValue()));
      }
      else if (mode_ == SET_MAXIMUM)
      {
        if (b.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerNumberModel)s_.getModel()).setMaximum(new Byte(b.getValue()));
      }
      else if (mode_ == SET_MINIMUM)
      {
        if (b.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerNumberModel)s_.getModel()).setMinimum(new Byte(b.getValue()));
      }
    }
    
    public void visitAnyChar (CharI c)
    {
      if (mode_ == CREATE_MODEL)
        numericModel(new Byte((byte)0));
      else if (mode_ == GET_VALUE)
      {
        Byte jb = (Byte)((SpinnerNumberModel)s_.getModel()).getValue();
        c.setValue((char)jb.byteValue());
      }
      else if (mode_ == SET_VALUE)
      {
        if (c.isNull())
          ((SpinnerNumberModel)s_.getModel()).setValue(new Byte((byte)0));
        else
          ((SpinnerNumberModel)s_.getModel()).setValue(new Byte((byte)c.getValue()));
      }
      else if (mode_ == SET_MAXIMUM)
      {
        if (c.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerNumberModel)s_.getModel()).setMaximum(new Byte((byte)c.getValue()));
      }
      else if (mode_ == SET_MINIMUM)
      {
        if (c.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerNumberModel)s_.getModel()).setMinimum(new Byte((byte)c.getValue()));
      }
    }
    
    public void visitAnyInt (IntI i)
    {
      if (mode_ == CREATE_MODEL)
        numericModel(new Integer(0));
      else if (mode_ == GET_VALUE)
      {
        Integer ji = (Integer)((SpinnerNumberModel)s_.getModel()).getValue();
        i.setValue(ji.intValue());
      }
      else if (mode_ == SET_VALUE)
      {
        if (i.isNull())
          ((SpinnerNumberModel)s_.getModel()).setValue(new Integer(0));
        else
          ((SpinnerNumberModel)s_.getModel()).setValue(new Integer(i.getValue()));
      }
      else if (mode_ == SET_MAXIMUM)
      {
        if (i.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerNumberModel)s_.getModel()).setMaximum(new Integer(i.getValue()));
      }
      else if (mode_ == SET_MINIMUM)
      {
        if (i.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerNumberModel)s_.getModel()).setMinimum(new Integer(i.getValue()));
      }
    }
    
    public void visitAnyShort (ShortI s)
    {
      if (mode_ == CREATE_MODEL)
        numericModel(new Short((short)0));
      else if (mode_ == GET_VALUE)
      {
        Short js = (Short)((SpinnerNumberModel)s_.getModel()).getValue();
        s.setValue(js.shortValue());
      }
      else if (mode_ == SET_VALUE)
      {
        if (s.isNull())
          ((SpinnerNumberModel)s_.getModel()).setValue(new Short((short)0));
        else
          ((SpinnerNumberModel)s_.getModel()).setValue(new Short(s.getValue()));
      }
      else if (mode_ == SET_MAXIMUM)
      {
        if (s.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerNumberModel)s_.getModel()).setMaximum(new Short(s.getValue()));
      }
      else if (mode_ == SET_MINIMUM)
      {
        if (s.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerNumberModel)s_.getModel()).setMinimum(new Short(s.getValue()));
      }
    }
    
    public void visitAnyLong (LongI l)
    {
      if (mode_ == CREATE_MODEL)
        numericModel(new Long(0));
      else if (mode_ == GET_VALUE)
      {
        Long jl = (Long)((SpinnerNumberModel)s_.getModel()).getValue();
        l.setValue(jl.longValue());
      }
      else if (mode_ == SET_VALUE)
      {
        if (l.isNull())
          ((SpinnerNumberModel)s_.getModel()).setValue(new Long(0));
        else
          ((SpinnerNumberModel)s_.getModel()).setValue(new Long(l.getValue()));
      }
      else if (mode_ == SET_MAXIMUM)
      {
        if (l.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerNumberModel)s_.getModel()).setMaximum(new Long(l.getValue()));
      }
      else if (mode_ == SET_MINIMUM)
      {
        if (l.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerNumberModel)s_.getModel()).setMinimum(new Long(l.getValue()));
      }
    }
    
    public void visitAnyFloat (FloatI f)
    {
      if (mode_ == CREATE_MODEL)
        numericModel(new Float(0));
      else if (mode_ == GET_VALUE)
      {
        Float jf = (Float)((SpinnerNumberModel)s_.getModel()).getValue();
        f.setValue(jf.floatValue());
      }
      else if (mode_ == SET_VALUE)
      {
        if (f.isNull())
          ((SpinnerNumberModel)s_.getModel()).setValue(new Float(0));
        else
          ((SpinnerNumberModel)s_.getModel()).setValue(new Float(f.getValue()));
      }
      else if (mode_ == SET_MAXIMUM)
      {
        if (f.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerNumberModel)s_.getModel()).setMaximum(new Float(f.getValue()));
      }
      else if (mode_ == SET_MINIMUM)
      {
        if (f.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerNumberModel)s_.getModel()).setMinimum(new Float(f.getValue()));
      }
    }
    
    public void visitAnyDouble (DoubleI d)
    {
      if (mode_ == CREATE_MODEL)
        numericModel(new Double(0));
      else if (mode_ == GET_VALUE)
      {
        Double jd = (Double)((SpinnerNumberModel)s_.getModel()).getValue();
        d.setValue(jd.doubleValue());
      }
      else if (mode_ == SET_VALUE)
      {
        if (d.isNull())
          ((SpinnerNumberModel)s_.getModel()).setValue(new Double(0));
        else
          ((SpinnerNumberModel)s_.getModel()).setValue(new Double(d.getValue()));
      }
      else if (mode_ == SET_MAXIMUM)
      {
        if (d.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerNumberModel)s_.getModel()).setMaximum(new Double(d.getValue()));
      }
      else if (mode_ == SET_MINIMUM)
      {
        if (d.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerNumberModel)s_.getModel()).setMinimum(new Double(d.getValue()));
      }
    }
    
    public void visitAnyDate (DateI d)
    {
      if (mode_ == CREATE_MODEL)
      {
        s_.setModel(new SpinnerDateModel());
        
        // Put the renderinfo's format pattern in, if there is one
        String fmtStr = null;
        RenderInfo r;
        if ((r = getRenderInfo()) != null)
        {
          fmtStr = r.getFormatString();
          
          // Keep the columns as reqd
          setColumns(r.getWidth());
        }
        
        if (fmtStr != null)
          ((JSpinner.DateEditor)s_.getEditor()).getFormat().applyPattern(fmtStr);
        
        // Set the editor's font to the textfield default for the L&F
        getTextComponent().setFont(UIManager.getLookAndFeelDefaults().getFont("TextField.font"));
        Insets i = UIManager.getLookAndFeelDefaults().getInsets("TextField.margin");
        Border b;
        if (i == null)
          b = editorBorder__;
        else
          b = new EmptyBorder(i);
        getTextComponent().setBorder(b);
        
        setupTodayAction();
      }
      else if (mode_ == GET_VALUE)
      {
        SpinnerDateModel m = (SpinnerDateModel)s_.getModel();
        Date date = m.getDate();
        d.setValue(date);
      }
      else if (mode_ == SET_VALUE)
      {
        SpinnerDateModel m = (SpinnerDateModel)s_.getModel();
        int p = getTextComponent().getCaretPosition();
        //System.out.println("Was: " + p);
        // Null is not supported for spinners
        if (d.isNull())
          m.setValue(new Date());
        else
          m.setValue(d.getValue());

        if (!guard_)
          m.setTodaySet(false);
        
        getTextComponent().setCaretPosition(p);
      }
      else if (mode_ == SET_MAXIMUM)
      {
        if (d.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerDateModel)s_.getModel()).setEnd(d.getValue());
      }
      else if (mode_ == SET_MINIMUM)
      {
        if (d.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerDateModel)s_.getModel()).setStart(d.getValue());
      }
    }
    
    public void visitDecimal(Decimal d)
    {
      // Looks like SpinnerNumberModel can accept a BigInteger but then
      // the increment can only be integer values. We'll map to a double
      // in the Java model and do something else when converting back out
      // again. Hmmm.
      if (mode_ == CREATE_MODEL)
        numericModel(new Double(0));
      else if (mode_ == GET_VALUE)
      {
        Double jd = (Double)((SpinnerNumberModel)s_.getModel()).getValue();
        // Best effort convert Double to BigDecimal
        d.setValue(BigDecimal.valueOf(jd.doubleValue()));
      }
      else if (mode_ == SET_VALUE)
      {
        if (d.isNull())
          ((SpinnerNumberModel)s_.getModel()).setValue(new Double(0));
        else
          ((SpinnerNumberModel)s_.getModel()).setValue(new Double(d.doubleValue()));
      }
      else if (mode_ == SET_MAXIMUM)
      {
        if (d.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerNumberModel)s_.getModel()).setMaximum(new Double(d.doubleValue()));
      }
      else if (mode_ == SET_MINIMUM)
      {
        if (d.isNull())
          throw new AnyRuntimeException("value cannot be null");
        else
          ((SpinnerNumberModel)s_.getModel()).setMinimum(new Double(d.doubleValue()));
      }
    }

    protected void unsupportedOperation (Any o)
    {
      throw new AnyRuntimeException("Can't put any spin on " + o.getClass());
    }
    
    private void numericModel(Number initVal)
    {
      s_.setModel(new SpinnerNumberModel(initVal, null, null, new Integer(1)));

      // Put the renderinfo's format pattern in, if there is one
      String fmtStr = null;
      RenderInfo r;
      if ((r = getRenderInfo()) != null)
      {
        fmtStr = r.getFormatString();
        
        // Keep the columns as reqd
        setColumns(r.getWidth());
      }
      
      if (fmtStr != null)
        ((JSpinner.NumberEditor)s_.getEditor()).getFormat().applyPattern(fmtStr);
      
      // Set the editor's font to the textfield default for the L&F
      getTextComponent().setFont(UIManager.getLookAndFeelDefaults().getFont("TextField.font"));
      Insets i = UIManager.getLookAndFeelDefaults().getInsets("TextField.margin");
      Border b;
      if (i == null)
        b = editorBorder__;
      else
        b = new EmptyBorder(i);
      getTextComponent().setBorder(b);
    }
  }
  
  private class SpinFocusAdapter implements FocusListener
  {
    private ArrayList listeners_ = new ArrayList();
    
    public void addFocusListener(FocusListener l)
    {
      listeners_.add(l);
    }
    
    public void removeFocusListener(FocusListener l)
    {
      listeners_.remove(l);
    }

    public void focusGained(FocusEvent e)
    {
      // Pass on to our listeners
      for (int i = 0; i < listeners_.size(); i++)
      {
        FocusListener fl = (FocusListener)listeners_.get(i);
        fl.focusGained(e);
      }
    }

    public void focusLost(FocusEvent e)
    {
      for (int i = 0; i < listeners_.size(); i++)
      {
        FocusListener fl = (FocusListener)listeners_.get(i);
        fl.focusLost(e);
      }
    }
  }
  
  private class SpinnerDateModel extends javax.swing.SpinnerDateModel
  {
    private boolean todaySet_ = false;
    
    public void setValue(Object value, boolean todaySet)
    {
      todaySet_ = todaySet;
      int p = getTextComponent().getCaretPosition();
      setValue(value);
      getTextComponent().setCaretPosition(p);
    }
    
    private boolean isTodaySet()
    {
      return todaySet_;
    }

    private void setTodaySet(boolean todaySet)
    {
      todaySet_ = todaySet;
    }
  }
}

