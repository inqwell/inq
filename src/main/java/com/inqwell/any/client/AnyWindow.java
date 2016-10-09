/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


/*
 * $Archive: /src/com/inqwell/any/client/AnyWindow.java $
 * $Author: sanderst $
 * $Revision: 1.10 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;

import javax.swing.JButton;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.Array;
import com.inqwell.any.Composite;
import com.inqwell.any.ConstInt;
import com.inqwell.any.ContainedException;
import com.inqwell.any.DepthFirstIter;
import com.inqwell.any.EventListener;
import com.inqwell.any.ExceptionHandler;
import com.inqwell.any.Exit;
import com.inqwell.any.Globals;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.Map;
import com.inqwell.any.Process;
import com.inqwell.any.Set;
import com.inqwell.any.Transaction;
import com.inqwell.any.beans.Facade;
import com.inqwell.any.beans.WindowF;
import com.inqwell.any.client.dock.AnyCControl;
import com.inqwell.any.client.dock.AnyCDockable;
import com.inqwell.any.client.dock.AnyMultipleCDockable;
import com.inqwell.any.client.swing.InqWindow;
import com.inqwell.any.client.swing.SwingInvoker;


public class AnyWindow extends    AnyComponent
											 implements WindowF
{
	static public IntI HIDE_ON_CLOSE       = new ConstInt(WindowConstants.HIDE_ON_CLOSE);
	static public IntI DO_NOTHING_ON_CLOSE = new ConstInt(WindowConstants.DO_NOTHING_ON_CLOSE);
	static public IntI DISPOSE_ON_CLOSE    = new ConstInt(WindowConstants.DISPOSE_ON_CLOSE);
	static public IntI EXIT_ON_CLOSE       = new ConstInt(WindowConstants.EXIT_ON_CLOSE);

	public     static Any   menuBar__              = AbstractValue.flyweightString("menuBar");
	public     static Any   toolBar__              = AbstractValue.flyweightString("toolBar");
	public     static Any   defaultButton__        = AbstractValue.flyweightString("defaultButton");
	public     static Any   defaultableButtons__   = AbstractValue.flyweightString("defaultableButtons");
  protected  static Any   defaultableCloseOp__   = AbstractValue.flyweightString("defaultCloseOperation");
  protected  static Any   active__               = AbstractValue.flyweightString("active");
  protected  static Any   focused__              = AbstractValue.flyweightString("focused");
  protected  static Any   toolBarChild__         = AbstractValue.flyweightString("toolBar__");
  public     static Any   disabledText__         = AbstractValue.flyweightString("disabledText");

  private    static Any   desktop__              = AbstractValue.flyweightString("desktop");

  private    static Set   preferredListenerTypes__;
	private    static Set   windowProperties__;

  private    static Array allWindows__      = AbstractComposite.array();
  private    static Map   glassPaneCursor__ = AbstractComposite.simpleMap();

	private InqWindow       w_;

  private AnyComponent    defaultButton_;
  private Set             dbs_;

  private FocusAdapter    defaultButtonListener_;

	private boolean         packed_ = false;

  private IntI            closeOperation_ = new AnyInt(HIDE_ON_CLOSE);

  static
  {
    windowProperties__ = AbstractComposite.set();
    windowProperties__.add(defaultButton__);
    windowProperties__.add(defaultableButtons__);
    windowProperties__.add(defaultableCloseOp__);
    windowProperties__.add(focused__);
    windowProperties__.add(active__);
    windowProperties__.add(disabledText__);

    preferredListenerTypes__ = AbstractComposite.set();
    preferredListenerTypes__.add(ListenerConstants.WINDOW);
    preferredListenerTypes__.add(ListenerConstants.CONTEXT);
  }

	public static WindowF getParentWindow(Facade f)
	{
		do
		{
			f = f.getParentComponent();
		}
		while((f != null) && !(f instanceof WindowF));

		return (WindowF)f;
	}

  public static void setBusyCursor()
  {
    for (int i = 0; i < allWindows__.entries(); i++)
    {
      WindowF w = (WindowF)allWindows__.get(i);
      Component glassPane = w.getGlassPane();
      Cursor currCursor = glassPane.getCursor();
      AnyCursor c = (AnyCursor)glassPaneCursor__.get(w);
      c.setValue(currCursor);
      glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      glassPane.setVisible(true);
    }
  }

  public static void unsetBusyCursor()
  {
    for (int i = 0; i < allWindows__.entries(); i++)
    {
      WindowF w = (WindowF)allWindows__.get(i);
      Component glassPane = w.getGlassPane();
      AnyCursor c = (AnyCursor)glassPaneCursor__.get(w);
      glassPane.setCursor(c.getCursor());
      glassPane.setVisible(false);
    }
  }

  public static void saveDesktop(Map m)
  {
    Map desktop = AbstractComposite.simpleMap();
    Iter i = allWindows__.createIterator();
    while (i.hasNext())
    {
      AnyWindow w = (AnyWindow)i.next();
      w.saveState(desktop);
    }
    m.add(desktop__, desktop);
  }
  
  public static void restoreDesktop(Map m)
  {
    Map desktop = (Map)m.getIfContains(desktop__);
    Iter i = allWindows__.createIterator();
    while (i.hasNext())
    {
      AnyWindow w = (AnyWindow)i.next();
      w.restoreState(desktop);
    }
  }
  
//  public static Map getDesktopData(Any from) throws AnyException
//  {
////    AnyFile f = new AnyFile(new File(System.getProperty("user.home") +
////                                     System.getProperty("file.separator") +
////                                     ".inqDesktop"));
//    
//    XMLXStream xs = new XMLXStream();
//    Map ret = null;
//    
//    try
//    {
//      if (xs.open(Globals.getProcessForThread(Thread.currentThread()),
//          from,
//          PhysicalIO.read__))
//      {
//        ret = (Map)xs.read();
//      }
//    }
//    finally
//    {
//      xs.close();
//    }
//    return ret;
//  }
  
  public static void inqEventQueue()
  {
    new EventProcessor();
  }

	public void setObject(Object w)
	{
		if (!(w instanceof InqWindow))
			throw new IllegalArgumentException
									("AnyWindow wraps java.awt.Window or JInternalFrame and sub-classes");

		w_ = (InqWindow)w;
		super.setObject(w);
    allWindows__.add(this);
    glassPaneCursor__.add(this, new AnyCursor(null));

    // At the Java level, all windows do nothing on close
    w_.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    // Monitor closing events to handle closing actions ourselves
    w_.addWindowListener(new ClosingMonitor());
	}

	public void show(boolean withResize, AnyComponent relativeTo)
	{
    Show show = new Show(withResize, relativeTo);
		show.maybeSync();
	}

	public void hide()
	{
		new Hide().maybeAsync(false);
	}
	
	public void toFront()
	{
	  // May be the window is in the process of being shown, so give it a chance
	  // to be so.
    SwingInvoker ss = new SwingInvoker()
    {
      protected void doSwing()
      {
        w_.toFront();
      }
    };

    ss.maybeAsync(false);
	}

  /**
   * Provided for a terminating condition when implementing our
   * own dialog modality. Dialogs can have a parent window
   * but windows cannot.
   */
	public WindowF getParentFrame()
	{
	  return null;
	}

  public boolean isBypassModality()
  {
    // For the moment, only Frames do bypassModality
    return false;
  }

  public void inqDispose()
  {
    System.out.println("inqDispose");
    this.removeInParent();
  }

  public void inqHide()
  {
    hide();
  }
  
  public void inqExit()
  {
    Exit exit = new Exit(new ConstInt(0), AnyBoolean.FALSE);
    Process p = Globals.getProcessForCurrentThread();
    exit.setTransaction(p.getTransaction());
    try
    {
      exit.exec(p.getContext());
    }
    catch(Exception e) {}
    finally
    {
      System.exit(0);
    }
  }

	public void dispose(boolean disposeChildren)
	{
    if (disposeChildren)
      disposeAllChildren();
    
    int indx = allWindows__.indexOf(this);
    if (indx >= 0)  // TODO: Why can we get the same window twice sometimes?
      allWindows__.remove(indx);
    
    if (glassPaneCursor__.contains(this))
      glassPaneCursor__.remove(this);

    SwingInvoker ss = new SwingInvoker()
    {
      protected void doSwing()
      {
        // If we harbour a AnyCControl then destroy it
        AnyCControl.destroyCControl(AnyWindow.this);
        w_.dispose();
      }
    };
    
    ss.maybeSync();
	}

  public void removeEventListener (EventListener l)
  {
    // If we are removed from our parent then dispose of the
    // window.
    super.removeEventListener(l);

    if (l == getParentAny())
    {
      dispose(true);
    }
  }

  public void setDefaultCloseOperation(IntI closeOperation)
  {
    closeOperation_.copyFrom(closeOperation);
  }

  public IntI getDefaultCloseOperation()
  {
    return closeOperation_;
  }

  public void setPrefSize(Array size)
  {
    IntI wI = (IntI)size.get(0);
    IntI hI = (IntI)size.get(1);
    int w = wI.getValue();
    int h = hI.getValue();
    Dimension d = new Dimension(w, h);
    w_.setPreferredSize(d);
    //validate();
  }

  public void setMinSize(Array size)
  {
    IntI wI = (IntI)size.get(0);
    IntI hI = (IntI)size.get(1);
    int w = wI.getValue();
    int h = hI.getValue();
    Dimension d = new Dimension(w, h);
    w_.setMinimumSize(d);
    //validate();
  }
  
  public void setDisabledText(Any text)
  {
    w_.setDisabledText(text);
  }

  /**
   * Set the button that should be the default button when no
   * button that is in the default list has the focus.
   * If the <code>setDefaultableButtons</code> property has
   * been set then this property should be set also.  If
   * this is not the case then the behaviour on the default
   * input event is undefined when none of the defaultable
   * buttons has the focus.
   */
  public void setDefaultButton(AnyComponent defaultButton)
  {
    if (defaultButton_ != null)
    {
      removeDefaultListener(defaultButton_);
      if (dbs_ != null && dbs_.contains(defaultButton_))
        dbs_.remove(defaultButton_);
    }

    if (defaultButtonListener_ == null)
      defaultButtonListener_ = new DefaultButtonListener();

    addDefaultListener(defaultButton);

    defaultButton_ = defaultButton;

    if (dbs_ != null && defaultButton_ != null && !dbs_.contains(defaultButton_))
      dbs_.add(defaultButton_);

    defaultButtonListener_.focusLost(null);
    
    defaultButton.requestFocus();
  }

  /**
   * Get the button that should be the default button when no
   * button that is in the default list has the focus.
   */
  public AnyComponent getDefaultButton()
  {
    return defaultButton_;
  }

  /**
   * Set the list of buttons that will be accepted as default
   * buttons for this window. When any of the buttons in the
   * given list gets the focus this button will be the default
   * button and thus activated when the default input event
   * is received in this window.
   */
  public void setDefaultableButtons(Set dbs)
  {
    if (defaultButtonListener_ == null)
      defaultButtonListener_ = new DefaultButtonListener();

    if (dbs_ != null)
      removeDefaultListener(dbs_);

    if (dbs_ == null && defaultButton_ != null)
      removeDefaultListener(defaultButton_);

    if (defaultButton_ != null && !dbs.contains(defaultButton_))
      dbs.add(defaultButton_);

    dbs_ = dbs;

    addDefaultListener(dbs_);
  }

  public Set getDefaultableButtons()
  {
    return dbs_;
  }

	protected Object getPropertyOwner(Any property)
	{
		if (windowProperties__.contains(property))
		  return this;

		return super.getPropertyOwner(property);
	}

  protected Composite getPreferredListenerTypes()
  {
    return AnyWindow.preferredListenerTypes__;
  }

  public boolean isFocused()
  {
    return w_.isFocused();
  }
  
  public boolean isActive()
  {
    return w_.isActive();
  }
  
  public Component getGlassPane()
  {
    return w_.getGlassPane();
  }

  public void saveState(Map m)
  {
  }

  public void restoreState(Map m)
  {
  }
  
  private void addDefaultListener(AnyComponent c)
  {
    c.getComponent().addFocusListener(defaultButtonListener_);
  }

  private void removeDefaultListener(AnyComponent c)
  {
    c.getComponent().removeFocusListener(defaultButtonListener_);
  }

  private void addDefaultListener(Set s)
  {
    Iter i = s.createIterator();
    while (i.hasNext())
    {
      AnyComponent c = (AnyComponent)i.next();
      addDefaultListener(c);
    }
  }

  private void removeDefaultListener(Set s)
  {
    Iter i = s.createIterator();
    while (i.hasNext())
    {
      AnyComponent c = (AnyComponent)i.next();
      removeDefaultListener(c);
    }
  }

	/**
	 * Forces an initial update of the model representing the window
	 * title, thus initialising any such model with the default title
	protected void initUpdateModel()
	{
		try
		{
			updateModel();
		}
		catch (AnyException e)
		{
			e.printStackTrace();
		}
	}
	 */

  private boolean disposeOnClose()
  {
    return closeOperation_.equals(DISPOSE_ON_CLOSE);
  }

  private boolean hideOnClose()
  {
    return closeOperation_.equals(HIDE_ON_CLOSE);
  }
  
  private boolean exitOnClose()
  {
    return closeOperation_.equals(EXIT_ON_CLOSE);
  }
  
  private void disposeAllChildren()
  {
    DepthFirstIter d = new DepthFirstIter(this, true, true);
    Set s = AbstractComposite.set();

    while (d.hasNext())
    {
      Any node = d.next();
      
      // Protect against finding the same node twice, in case the node
      // space contains cyclic paths. The iterator won't blow up but
      // we may find the same children more than once...
      if (node instanceof WindowF && !s.contains(node))
      {
        // Only process the window if its parent is the same as the
        // iterator parent. That way we only dispose of windows beneath
        // ourselves that we are actually responsible for.
        Composite w = (Composite)node;
        
        if (isChild(w))
          s.add(node);
      }
    }
    
    Iter i = s.createIterator();
    while (i.hasNext())
    {
      WindowF w = (WindowF)i.next();
      w.dispose(false);
    }
    AnyMultipleCDockable.clearFactory();
  }
  
  private boolean isChild(Composite w)
  {
    // returns true if w is beneath this in the Inq hierarchy
    do
    {
      Composite c = w.getParentAny();
      if (c == this)
        return true;
      w = c;
    }
    while(w != null);
    
    return false;
  }

  private class DefaultButtonListener extends FocusAdapter
  {
    public void focusGained(FocusEvent e)
    {
      JButton button = (JButton)e.getSource();
      ((RootPaneContainer)w_).getRootPane().setDefaultButton(button);
    }

    public void focusLost(FocusEvent e)
    {
      if (defaultButton_ == null)
      {
        ((RootPaneContainer)w_).getRootPane().setDefaultButton(null);
      }
      else
      {
        JButton button = (JButton)defaultButton_.getComponent();
        ((RootPaneContainer)w_).getRootPane().setDefaultButton(button);
      }
    }
  }

  private class Show extends SwingInvoker
  {
	  private boolean      resize_ = false;
	  private AnyComponent relativeTo_;
    
    private Show(boolean resize, AnyComponent relativeTo)
    {
      resize_     = resize;
      relativeTo_ = relativeTo;
    }

		protected void doSwing()
		{
			if (!packed_)
			{
				// Always pack the first time
				w_.pack();
        ensureTitleVisible();
				packed_ = true;

        WindowF w = AnyWindow.getParentWindow(AnyWindow.this);
        while (w instanceof AnyCDockable)
        	w = AnyWindow.getParentWindow(w);

        if (relativeTo_ == null && !(AnyWindow.this instanceof AnyInternalFrame))
        {
          // Position window on first show
          Dimension d = null; // size of what we're positioning against
          Point     p = null;

          if (w != null)
          {
            d = w.getComponent().getSize();
            p = w.getComponent().getLocation();
          }
          else
          {
            d = Toolkit.getDefaultToolkit().getScreenSize();
            p = new Point();
          }

          double centreX = p.getX() + d.getWidth()  / 2;
          double centreY = p.getY() + d.getHeight() / 2;

          AnyWindow.this.getComponent().getSize(d);
          p.setLocation(centreX - d.getWidth() / 2,
                        centreY - d.getHeight() / 2);
          if (p.getX() < 0)
            p.setLocation(0, p.getY());

          if (p.getY() < 0)
            p.setLocation(p.getX(), 0);

          AnyWindow.this.getComponent().setLocation(p);
        }
			}

      if (resize_)
      {
			  w_.pack();
        ensureTitleVisible();
      }
      
      if (relativeTo_ != null)
        AnyWindow.this.w_.setLocationRelativeTo(relativeTo_.getComponent());
        
			w_.show();
			w_.setVisible(true);
      w_.setState(Frame.NORMAL);
      try
      {
        //w_.setClosed(false);
        w_.setIcon(false);
        w_.setSelected(true);
      }
      catch(PropertyVetoException e) {}
		}

    // Make the frame large enough to accommodate its title.
    private void ensureTitleVisible()
    {
      if (w_.getTitle() != null)
      {
        // Make the frame always wide enough to display the title
        // without chopping.
        int w = w_.getWidth();
        int mw = 100 + w_.getFontMetrics(w_.getFont()).stringWidth(w_.getTitle());
        //System.out.println("AnyWindow w " + w);
        //System.out.println("AnyWindow mw " + mw);
        if (w < mw)
          w_.setSize(mw, w_.getHeight());
      }
    }
	}

  private class Hide extends SwingInvoker
  {
		protected void doSwing()
		{
			//w_.hide();
      w_.setVisible(false);
      //w_.revalidate();

//			try
//      {
//        w_.setClosed(true);
//      }
//      catch(PropertyVetoException e) {}
		}
	}

	private class ClosingMonitor extends java.awt.event.WindowAdapter
	{
	  public void windowClosing(WindowEvent e)
	  {
  	  if (disposeOnClose())
  	  {
        // invoke later so that all Inq event handers for this event have
        // already run before doing any custom disposal
  	    SwingUtilities.invokeLater(new Runnable()
  	      {
  	        public void run()
  	        {
  	          // custom disposal - remove us from our parent
              inqDispose();
  	        }
  	      }
  	    );
  	  }
  	  else if (hideOnClose())
        inqHide();
  	  else if (exitOnClose())
  	    inqExit();
	  }
	}

	static private class EventProcessor extends EventQueue
	{
    int eventCount__ = 0;

    public EventProcessor()
	  {
      Toolkit.getDefaultToolkit().
      getSystemEventQueue().push(this);
    }

    protected void dispatchEvent(AWTEvent e)
	  {
      //System.out.println(e);
      if (Globals.process__ != null && Globals.awtSync__)
      {
        synchronized(Globals.process__)
        {
          if (!isModalityEvent(e))
          {
            if (Globals.inqActive__)
            {
                super.dispatchEvent(e);
            }
            else
            {
              Transaction      t           = Globals.process__.getTransaction();
              ExceptionHandler eh          = Globals.process__.getExceptionHandler();
              Map              context     = Globals.process__.getContext();
              Any              contextPath = Globals.process__.getContextPath();
              // We should only get an exception here if the stack did NOT
              // pass through an Inq EventBinding, otherwise it would have
              // been handled there.  Examples include the repaint manager.
              int eventCount = eventCount__++;
              try
              {
                super.dispatchEvent(e);
              }
              catch (Throwable err)
              {
                //err.printStackTrace();
                if (err instanceof AnyException)
                {
                  // Normal exceptions from the Any framework
                  AnyException ex = (AnyException)err;
                  ex.fillInCallStack(t);
                  eh.handleException(ex, t);
                  t.getCallStack().empty();
                }
                else if (err instanceof AnyRuntimeException)
                {
                  // Runtime exceptions from the Any framework
                  AnyRuntimeException ex = (AnyRuntimeException)err;
                  ex.fillInCallStack(t);
                  eh.handleException(ex, t);
                  t.getCallStack().empty();
                }
                else if (err instanceof Exception)
                {
                  // Runtime exceptions from Java
                  AnyException ex = new ContainedException(err);
                  ex.fillInCallStack(t);
                  eh.handleException(ex, t);
                  t.getCallStack().empty();
                }
                else
                {
                  // Nasty things
                  AnyException ex = new ContainedException(err);
                  ex.topOfStack(t);
                  eh.handleException(ex, t);
                  t.getCallStack().empty();
                }
              }
              finally
              {
                eventCount__ = eventCount;
                if (eventCount__ == 0)
                {
                  t.getCallStack().empty();
                  Globals.process__.setContext(context);
                  Globals.process__.setContextPath(contextPath);
                }
              }
            }
          }
        }
      }
      else
      {
        if (!isModalityEvent(e))
          super.dispatchEvent(e);
      }
    }

    private boolean isModalityEvent(AWTEvent e)
    {
	    //System.out.println(e.toString());
		  boolean consumed = false;

      int id = e.getID();

      if((id == MouseEvent.MOUSE_PRESSED) ||
         (id == MouseEvent.MOUSE_RELEASED) ||
         (id == MouseEvent.MOUSE_CLICKED) ||
         (id == MouseEvent.MOUSE_DRAGGED) ||
         (id == KeyEvent.KEY_PRESSED) ||
         (id == KeyEvent.KEY_RELEASED) ||
         (id == KeyEvent.KEY_TYPED))
      {
	      //System.out.println("Intercept " + e);
	      //System.out.println("Source    " + e.getSource());
        consumed = AnyDialog.raiseActiveDialog(e);
      }

      return consumed;
    }
  }
}
