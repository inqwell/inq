/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/ListenerFactory.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;
import com.inqwell.any.*;
import com.inqwell.any.beans.*;

/**
 * This class maintains a register of the Listeners we can handle
 * keyed on class name.
 */
public class ListenerFactory extends AbstractAny
{
  private static ListenerFactory myFactory__;
  private static ClassMap        factoryMap__;

  static
  {
    factoryMap__ = new ClassMap();
    factoryMap__.add(java.lang.Object.class, new DegenerateListener());
    factoryMap__.add(javax.swing.text.JTextComponent.class, new JTextComponentListener());
    factoryMap__.add(javax.swing.AbstractButton.class, new AbstractButtonListener());
    factoryMap__.add(javax.swing.JComboBox.class, new JComboBoxListener());
    factoryMap__.add(javax.swing.JTabbedPane.class, new JTabbedPaneListener());
    factoryMap__.add(com.inqwell.any.client.swing.JDateChooser.class, new JDateChooserListener());
    factoryMap__.add(javax.swing.JSlider.class, new JSliderListener());
    factoryMap__.add(javax.swing.JSpinner.class, new JSpinnerListener());
    factoryMap__.add(javax.swing.JMenu.class, new JMenuListener());
    factoryMap__.add(javax.swing.ButtonGroup.class, new AbstractButtonListener());
  }


  private ListenerFactory()
  {
  }

  public static ListenerFactory getHandle()
  {
    if (myFactory__ == null)
      myFactory__ = new ListenerFactory();
    return myFactory__;
  }

	public Listener getListener(Object o)
	{
	  return (Listener)factoryMap__.get(o);
	}
}

/**
 * The Degenerate Listener
 */
class DegenerateListener extends AbstractAny implements Listener
{
  public Any getDefaultEventType()
  {
    //System.out.println("DegenerateListener: degenerate getDefaultListenerType() called");
    throw new AnyRuntimeException("No default event type available");
    //return ListenerConstants.DEGENERATE;
  }
  
  public boolean hasDefaultEventType()
  {
    return false;
  }
}

abstract class AbstractListener extends    AbstractAny
                                implements Listener
{
  public boolean hasDefaultEventType()
  {
    return true;
  }
}

/**
 * The JTextComponentListener
 */
class JTextComponentListener extends AbstractListener implements Listener
{
  public Any getDefaultEventType()
  {
    return EventConstants.E_ACTION;
  }
}

/**
 * The AbstractButtonListener
 */
class AbstractButtonListener extends AbstractListener implements Listener
{
  public Any getDefaultEventType()
  {
    return EventConstants.E_ACTION;
  }
}

/**
 * The JComboBoxListener
 */
class JComboBoxListener extends AbstractListener implements Listener
{
  public Any getDefaultEventType()
  {
    return EventConstants.E_ACTION;
  }
}

class JTabbedPaneListener extends AbstractListener implements Listener
{
  public Any getDefaultEventType()
  {
    return EventConstants.E_CHANGE;
  }
}

class JDateChooserListener extends AbstractListener implements Listener
{
  public Any getDefaultEventType()
  {
    return EventConstants.E_CHANGE;
  }
}

class JMenuListener extends AbstractListener implements Listener
{
  public Any getDefaultEventType()
  {
    return EventConstants.MENU_SELECTED;
  }
}

class JSliderListener extends AbstractListener implements Listener
{
  public Any getDefaultEventType()
  {
    return EventConstants.E_CHANGE;
  }
}

class JSpinnerListener extends AbstractListener implements Listener
{
  public Any getDefaultEventType()
  {
    return EventConstants.E_CHANGE;
  }
}

