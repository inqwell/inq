/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/SwingInvoker.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.client.swing;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.InvocationEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyInt;
import com.inqwell.any.Catalog;
import com.inqwell.any.ConstString;
import com.inqwell.any.Globals;
import com.inqwell.any.Map;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Transaction;
import com.inqwell.any.client.AnyWindow;

/**
 * Provides basic support for performing Swing/AWT related tasks from
 * either the AWT thread or some other thread.
 */
public abstract class SwingInvoker implements Runnable
{
  public  static Any   lookandfeel__          = AbstractValue.flyweightString("lookandfeel");
  public  static Any   fontadjust__           = AbstractValue.flyweightString("fontadjust");
  public  static Any   nolookandfeel__        = AbstractValue.flyweightString("none");

  static public void initSwing()
  {
    AnyWindow.inqEventQueue();
    
    SwingInvoker ss = new SwingInvoker()
    {
      protected void doSwing()
      {
        // Establish the awt thread in the thread map for the client process
        Globals.setProcessForThread(Thread.currentThread(), Globals.process__);
        
        Map argsMap = Catalog.instance().getCommandArgs();
        
        setupLAF(argsMap);
        
        adjustFonts(argsMap);
      }
    };

    ss.maybeSync();
  }
  
  private static void adjustFonts(Map argsMap)
  {
    try
    {
      if (argsMap.contains(fontadjust__))
      {
        AnyInt adjust = new AnyInt(argsMap.get(fontadjust__));
        adjustUIFonts(adjust.getValue());
      }
    }
    catch (Exception e)
    {
      // Limited error handling possibilities here!
      System.err.println("Cannot adjust font sizes");
    }
  }
  
  private static void adjustUIFonts (int sizeAdjust)
  {
    //
    // See notes in setUIFont
    //
    
    java.util.Enumeration keys = UIManager.getDefaults().keys();
    while (keys.hasMoreElements())
    {
      Object key = keys.nextElement();
      Object value = UIManager.get(key);
      if (value instanceof javax.swing.plaf.FontUIResource)
      {
        Font f1 = (Font)value;
        javax.swing.plaf.FontUIResource f2 =
          new javax.swing.plaf.FontUIResource(f1.deriveFont(f1.getSize2D() + sizeAdjust));
        UIManager.put(key, f2);
      }
    }
  }
  
  private static void setUIFont (javax.swing.plaf.FontUIResource f)
  {
    //
    // sets the default font for all Swing components.
    // ex.
    // setUIFont (new javax.swing.plaf.FontUIResource
    // ("Serif",Font.ITALIC,12));
    //
    // From: http://www.java-forums.org/java-tips/6522-swing-changing-component-default-font.html
    
    java.util.Enumeration keys = UIManager.getDefaults().keys();
    while (keys.hasMoreElements())
    {
      Object key = keys.nextElement();
      Object value = UIManager.get(key);
      if (value instanceof javax.swing.plaf.FontUIResource)
        UIManager.put(key, f);
    }
  }
  
  private static void setupLAF(Map argsMap)
  {
    Any lAndFClass = null;
    try
    {
      if (argsMap.contains(lookandfeel__))
      {
        lAndFClass = argsMap.get(lookandfeel__);
        if (!lAndFClass.equals(nolookandfeel__))
        {
          if (lAndFClass.toString().indexOf('.') > 0)
          {
            // Assume its a class name and try to load
            UIManager.setLookAndFeel(lAndFClass.toString());
          }
          else
          {
            // Assume its a name - see if it is in the supported list
            UIManager.LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();
            for (int i = 0; i < lafInfo.length; i++)
            {
              if (lAndFClass.toString().equalsIgnoreCase(lafInfo[i].getName()))
              {
                lAndFClass = new ConstString(lafInfo[i].getClassName());
                UIManager.setLookAndFeel(lafInfo[i].getClassName());
                return;
              }
            }
            System.err.println("Cannot find specified L&F " + lAndFClass + ", using default");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          }
        }
      }
      else
      {
        // Nothing specified - load platform default
        lAndFClass = new ConstString(UIManager.getSystemLookAndFeelClassName());
        UIManager.setLookAndFeel(lAndFClass.toString());
        //System.out.println(UIManager.getSystemLookAndFeelClassName());
      }
//        UIDefaults d = UIManager.getLookAndFeelDefaults();
//        Object o[] = (Object[])d.get("MenuBar.windowBindings");
//        for (int i = 0; i < o.length; i++)
//          System.out.println(o[i].toString());
    }
    catch (Exception e)
    {
      // Limited error handling possibilities here!
      if (lAndFClass == null)
        System.err.println("Cannot load platform-default L&F");
      else
        System.err.println("Cannot load L&F: " + lAndFClass);
    }
  }
	/**
	 * Just invokes <code>doSwing</code>
	 */
	public void run()
	{
		doSwing();
	}

	/**
	 * Ensure a task runs on the event dispatch thread.
	 * <p/>
	 * If we are running in the dispatch thread then just call doSwing;
	 * if we are not running in the dispatch thread then call
	 * <code>SwingInvoker.invokeLater(this)<code>
	 * <p>
	 * @param force If <code>true</code> then always post to the
	 * event queue even if execution is already on it. Used to
	 * schedule a task to the back of the event queue.  
	 */
	public void maybeAsync(boolean force)
	{
		if ((SwingUtilities.isEventDispatchThread() || !Globals.awtSync__) && !force)
		{
			doSwing();
		}
		else
		{
//			SwingUtilities.invokeLater(this);
			//maybeSync();
      SwingInvoker.invokeLater(this);
		}
	}

	public void serviceAsync(Transaction t)
	{
    SwingInvoker.invokeServiceLater(this, t);
	}
  
	/**
	 * If we are running in the dispatch thread then just call doSwing;
	 * if we are not running in the dispatch thread then call
	 * <code>SwingUtilities.invokeAndWait(this)<code>
	 * <p>
	 * This method is appropriate when data is being fetched from a
	 * GUI component, for example <code>TextField.getText()</code>
	 */
	public void maybeSync()
	{
		if (SwingUtilities.isEventDispatchThread() || !Globals.awtSync__)
		{
      doSwing();
		}
		else
		{
			try
			{
				SwingInvoker.invokeAndWait(this);
			}
			catch (InterruptedException e)
			{
				throw (new RuntimeContainedException(e));
			}
			catch (InvocationTargetException e)
			{
				throw (new RuntimeContainedException(e.getTargetException()));
			}
		}
	}

	/**
	 * Clients must subclass and implement this method.  <code>doSwing</code>
	 * will then be called either directly (in dispatch thread) or via
	 * the <code>SwingUtilities</code> methods.
	 */
	protected abstract void doSwing();

  public static void invokeAndWait(Runnable runnable) throws InterruptedException,
 	                                                           InvocationTargetException
 	{
    if (SwingUtilities.isEventDispatchThread())
    {
      throw new Error("Cannot call invokeAndWait from the event dispatcher thread");
    }

    InvocationEvent event = new InvocationEvent(Toolkit.getDefaultToolkit(),
										                            runnable,
										                            Globals.process__,
												                        true);

    synchronized(Globals.process__)
    {
      Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(event);
      Globals.process__.wait();
      // When we get here we are out of the swing thread and back in
      // the invoking thread.
    }

    Exception eventException = event.getException();

    if (eventException != null)
        throw new InvocationTargetException(eventException);
  }

  public static void invokeLater(Runnable runnable)
 	{
//    if (SwingUtilities.isEventDispatchThread())
//    {
//      throw new Error("Cannot call invokeLater from the event dispatcher thread");
//    }

    InvocationEvent event = new InvocationEvent(Toolkit.getDefaultToolkit(),
										                            runnable);

    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(event);

  }

  public static void invokeServiceLater(Runnable runnable, Transaction t)
 	{
    ServiceInvocationEvent event = new ServiceInvocationEvent
                                         (t,
                                          Toolkit.getDefaultToolkit(),
                                          runnable);

    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(event);

  }
}
