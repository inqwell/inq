/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyDialog.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.beans.*;
import com.inqwell.any.client.swing.SwingInvoker;
import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.AWTEvent;
import java.awt.Frame;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JWindow;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.swing.Action;
import javax.swing.AbstractAction;
import java.awt.event.KeyEvent;
import com.inqwell.any.client.swing.JDialog;
import com.inqwell.any.client.swing.DialogEvent;
import java.awt.event.ActionEvent;
import java.awt.Window;
import java.awt.event.WindowEvent;

public class AnyDialog extends    AnyWindow
											 implements DialogF
{
	private JDialog     d_;

  // If we were created with a parent frame then this is it
  private AnyWindow   parent_;

	private boolean     modal_;  // TODO: retire
	private Any modality_;

  private boolean fireCancelOnClose_ = true;

  private static Array dialogActiveEvents__   = AbstractComposite.array();
  private static Array dialogInactiveEvents__ = AbstractComposite.array();

  private static Array activeDialogs__;

	private static Any   modal__             = new ConstString("modal");
	private static Any   modality__          = new ConstString("modality");
	public  static Any   modalNone__         = new ConstInt(0);
	public  static Any   modalParent__       = new ConstInt(1);
	public  static Any   modalAll__          = new ConstInt(2);
	private static Any   fireCancelOnClose__ = new ConstString("fireCancelOnClose");
	private static Set   dialogProperties__;
	private static Map   modalities__;
  private static Set   preferredListenerTypes__;

  static
  {
    preferredListenerTypes__ = AbstractComposite.set();
    preferredListenerTypes__.add(ListenerConstants.DIALOG);
    preferredListenerTypes__.add(ListenerConstants.WINDOW);
    preferredListenerTypes__.add(ListenerConstants.CONTEXT);

    modalities__ = AbstractComposite.simpleMap();
    modalities__.add(modalNone__, modalNone__);
    modalities__.add(modalParent__, modalParent__);
    modalities__.add(modalAll__, modalAll__);

    dialogProperties__ = AbstractComposite.set();
    dialogProperties__.add(menuBar__);
    dialogProperties__.add(modal__);
    dialogProperties__.add(modality__);
    dialogProperties__.add(fireCancelOnClose__);

		dialogActiveEvents__.add(EventConstants.W_ACTIVATED);
		dialogActiveEvents__.add(EventConstants.W_OPENED);
		dialogActiveEvents__.add(EventConstants.W_DEICONIFIED);

		dialogInactiveEvents__.add(EventConstants.W_CLOSED);
		dialogInactiveEvents__.add(EventConstants.W_CLOSING);
		//dialogInactiveEvents__.add(EventConstants.W_DEACTIVATED);
		dialogInactiveEvents__.add(EventConstants.W_ICONIFIED);

		activeDialogs__ = AbstractComposite.array();
  }

	public static DialogF getParentDialog(Facade f)
	{
		while((f != null) && !(f instanceof WindowF))
		{
			f = f.getParentComponent();
		}

		return (f instanceof DialogF) ? (DialogF)f : null;
	}

/*
	public static boolean raiseActiveDialog(AWTEvent e)
	{
		boolean ret = false;

		if (activeDialogs__.entries() != 0)
		{
			DialogF d = (DialogF)activeDialogs__.get(activeDialogs__.entries() - 1);
			Container dc = d.getComponent();

			Object source = e.getSource();
			Window wSource = (source instanceof JWindow) ? (JWindow)source : null;
			//System.out.println("1 " + source);
      while (wSource != null && wSource != dc)
      {
        wSource = wSource.getOwner();
        //System.out.println("2 " + wSource);
      }

			if (dc == source ||
          dc == wSource ||
			    (wSource != null && dc == wSource.getOwner()))
			  return false;

//      Component comp = (Component)e.getSource();
//      Frame f = JOptionPane.getFrameForComponent(comp);
//      System.out.println ("f: " + f);
//      System.out.println ("dc: " + JOptionPane.getFrameForComponent(dc));
//      if (f == JOptionPane.getFrameForComponent(dc))
//        return false;

      if (!d.isModal())
        return false;

			d.show(false);
			ret = true;
		}
		return ret;
	}
*/

	public static boolean raiseActiveDialog(AWTEvent e)
	{
	  boolean ret = false;

    int count = activeDialogs__.entries();
		if (count != 0)
		{
		  // Process currently active dialogs from the most recently
		  // raised backwards
		  while (--count >= 0)
		  {
		    // Get the dialog wrapper and component
  			DialogF d = (DialogF)activeDialogs__.get(count);
  			Container dc = d.getComponent();

        // Check the source of the event. Make sure the source is tied
        // back to the appropriate frame in case of (eg) heavyweight
        // dropdowns from combo boxes
  			Object source = e.getSource();
  			Window wSource = (source instanceof JWindow) ? (JWindow)source : null;
  			//System.out.println("1 " + source);
        while (wSource != null && wSource != dc)
        {
          wSource = wSource.getOwner();
          //System.out.println("2 " + wSource);
        }

        // If event is for current dialog then don't "consume".
        // Caller will dispatch as normal.
  			if (dc == source  ||
            dc == wSource ||
  			    (wSource != null && dc == wSource.getOwner()))
  			  return false;

        // If the current dialog is modalAll then consume and show
        // dialog
        if (d.getModality().equals(modalAll__))
        {
    			d.show(false, null);
          return true;
        }

        if (d.getModality().equals(modalParent__))
        {
          // For modalParent dialogs, establish whether the event was
          // destined for any parent dialog or frame
          WindowF parent = d;
          while ((parent = parent.getParentFrame()) != null)
          {
      			if (parent.getComponent() == source  ||
                parent.getComponent() == wSource ||
      			    (wSource != null && parent.getComponent() == wSource.getOwner()))
      			{
      			  d.show(false, null);
              return true;
      			}
          }
        }

        // Non-modal dialog - proceed to next showing
		  }
		}

		return false;
	}

  public void hideAllActiveDialogs()
  {
    SwingInvoker ss = new SwingInvoker()
    {
      protected void doSwing()
      {
        int i;
        while((i = activeDialogs__.entries()) > 0)
          ((AnyDialog)activeDialogs__.get(i)).hide();
      }
    };

    ss.maybeSync();
  }

  public AnyDialog() {}

  public AnyDialog(JDialog d)
  {
		super(d);
	}

	public void setObject(Object d)
	{
		//System.out.println ("AnyDialog.setObject " + d);

		if (!(d instanceof JDialog))
			throw new IllegalArgumentException
									("AnyDialog wraps com.inqwell.any.client.swing.JDialog and sub-classes");

		super.setObject(d);

		d_ = (JDialog)d;

		setupEscapeAction();

		// Tell base class about our spoof dialog events generator
		setupEventSet(d_.getDialogGenerator());

		// Set up to listen for window events so we can
		// cooperate with the event dispacher for preventing
		// hidden dialogs
		//addAdaptedEventListener(new DialogActive(dialogActiveEvents__));
		//addAdaptedEventListener(new DialogInactive(dialogInactiveEvents__));

    d_.addWindowListener(new DialogInactiveMonitor());

		// Intercept modality and handle it ourselves.  As far as
		// swing is concerned, all dialogs are non-modal
		setModal(true);
		d_.setModal(false);
	}

  public Container getComponent()
  {
    return d_;
  }

	public AnyComponent getMenuBar()
	{
		return null;
	}


	public void setMenuBar(AnyComponent menuBar)
	{
		JMenuBar m = (JMenuBar)menuBar.getComponent();
		d_.setJMenuBar(m);
	}

	public void setSize(int width, int height)
	{
		//System.out.println ("AnyDialog.setSize " + width + " " + height + "------");
		JComponent c = (JComponent)d_.getContentPane();
		c.setPreferredSize(new Dimension(width, height));
	}

	/**
	 * The OK action has occurred.  This method may be called by listeners
	 * attached to one or more child components.  A DIALOG_OK event is
	 * then generated
	 */
	public void fireOk()
	{
		if (d_.getHideOnOk())
      super.inqHide();

		//System.out.println ("AnyDialog.fireOk");
		JDialog.NotifyDialog n = d_.getDialogGenerator();
		n.fireDialogOk(new DialogEvent(n,
																	 ActionEvent.ACTION_PERFORMED,
																	 "OK"));

    if (getDefaultCloseOperation().equals(DISPOSE_ON_CLOSE))
      super.inqDispose();

    /*
    if (!getDefaultCloseOperation().equals(DO_NOTHING_ON_CLOSE))
    {
      if (getDefaultCloseOperation().equals(HIDE_ON_CLOSE))
        super.inqHide();
      else if (getDefaultCloseOperation().equals(DISPOSE_ON_CLOSE))
        super.inqDispose();
    }
    */
	}

	public void hide()
	{
		super.hide();
	  dialogInactive();
	}

	public void show(boolean withResize, AnyComponent relativeTo)
	{
		super.show(withResize, relativeTo);
	  dialogActive();
	}

	public boolean isModal()
	{
		return modal_;
	}

	public void setModal(boolean modal)
	{
		modal_ = modal;

		// Assume modalAll
		if (modal)
  		modality_ = modalAll__;
    else
      modality_ = modalNone__;
	}

	public void setModality(Any modality)
	{
	  // validate and throw if necessary
	  if (!modalities__.contains(modality))
	    throw new IllegalArgumentException("Unknown modality value");

	  if (modality.equals(modalParent__) && parent_ == null)
	    throw new IllegalArgumentException("Dialog cannot be parent modal without a parent");

	  modality_ = modalities__.get(modality);
	}

  public Any getModality()
  {
    return modality_;
  }

	public void setParentFrame(AnyWindow f)
	{
	  parent_ = f;
	}

	public WindowF getParentFrame()
	{
	  return parent_;
	}

  public void setFireCancelOnClose(boolean fireCancelOnClose)
  {
    fireCancelOnClose_ = fireCancelOnClose;
  }

  public boolean isFireCancelOnClose()
  {
    return fireCancelOnClose_;
  }

	/**
	 * The Cancel action has occurred.  This method may be called by listeners
	 * attached to one or more child components.  A DIALOG_CANCEL event is
	 * then generated
	 */
	public void fireCancel()
	{
    dialogInactive();
    
		JDialog.NotifyDialog n = d_.getDialogGenerator();
		n.fireDialogCancel(new DialogEvent(n,
																			 ActionEvent.ACTION_PERFORMED,
																			 "CANCEL"));

    if (!getDefaultCloseOperation().equals(DO_NOTHING_ON_CLOSE))
    {
      if (getDefaultCloseOperation().equals(HIDE_ON_CLOSE))
        super.inqHide();
      else if (getDefaultCloseOperation().equals(DISPOSE_ON_CLOSE))
        super.inqDispose();
    }

	}

  public void inqDispose()
  {
    if (fireCancelOnClose_)
      fireCancel();

    super.inqDispose();
  }

  public void inqHide()
  {
    if (fireCancelOnClose_)
      fireCancel();

    super.inqHide();
  }

	public Object getAddee()
	{
    return d_.getContentPane();
	}

	public Object getAddIn()
  {
    return d_.getContentPane();
  }

	protected Object getAttachee(Any eventType)
	{
		if (eventType.equals(ListenerConstants.DIALOG))
			return d_.getDialogGenerator();
		else
			return super.getAttachee(eventType);
	}

  protected Composite getPreferredListenerTypes()
  {
    return AnyDialog.preferredListenerTypes__;
  }

	protected Object getPropertyOwner(Any property)
	{
		if (dialogProperties__.contains(property))
		  return this;

		return super.getPropertyOwner(property);
	}

//	public Object     getAddee()
//	{
//    return d_.getContentPane();
//	}
//
  private void dialogActive()
  {
	  //System.out.println("Dialog Active");
	  if (!activeDialogs__.contains(this))
	    activeDialogs__.add(this);
	}

  private void dialogInactive()
  {
	  //System.out.println("Dialog Inactive");
	  int index;
	  if ((index = activeDialogs__.indexOf(this)) >= 0)
	    activeDialogs__.remove(index);
	}

	private void setupEscapeAction()
	{
    KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);

    Action escapeAction = new AbstractAction()
                              {
                                public void actionPerformed(ActionEvent e)
                                {
                                  fireCancel();
                                }
                              };

    d_.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
    d_.getRootPane().getActionMap().put("ESCAPE", escapeAction);
  }

/*
  class DialogActive extends EventBinding
  {
    public DialogActive(Array eventTypes)
    {
      super(eventTypes, false);
    }

		protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
		{
			dialogActive();
			return null;
		}
  }

  class DialogInactive extends EventBinding
  {
    public DialogInactive(Array eventTypes)
    {
      super(eventTypes, false);
    }

		protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
		{
			dialogInactive();
			return null;
		}
  }
*/

  private class DialogInactiveMonitor extends java.awt.event.WindowAdapter
	{
	  public void windowClosing(WindowEvent e)
	  {
			dialogInactive();
	  }
	  public void windowClosed(WindowEvent e)
	  {
			dialogInactive();
	  }
	  public void windowIconified(WindowEvent e)
	  {
			dialogInactive();
	  }
	}

}

