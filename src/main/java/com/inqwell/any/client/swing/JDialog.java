/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JDialog.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.Icon;

import com.inqwell.any.Any;
import com.inqwell.any.AnyNull;
import com.inqwell.any.IntI;
import com.inqwell.any.client.AnyComponent;

/**
 * An extension of JDialog simply to make some things easier from BML:
 * the setting the layout manager.  A BoxLayout object is used
 * exclusively as the layout manager.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class JDialog extends    javax.swing.JDialog
                     implements InqWindow
{
	private NotifyDialog notifyDialog_;
	
	private boolean      hideOnCancel_ = true;
	private boolean      hideOnOk_     = true;
	
  /**
   * Create a new JDialog with a Y axis BoxLayout and no parent
   */
  public JDialog()
  {
  	setLayout (JFrame.Y_AXIS);
  	initGlassPane();
  }

  /**
   * Create a new JDialog with a Y axis BoxLayout and specified parent
   */
  public JDialog(Frame parent)
  {
  	super(parent);
  	setLayout (JFrame.Y_AXIS);
    initGlassPane();
  }

  /**
   * Create a new JDialog with a Y axis BoxLayout and specified parent
   */
  public JDialog(JFrame parent)
  {
  	super(parent);
  	setLayout (JFrame.Y_AXIS);
    initGlassPane();
  }

  /**
   * Create a new JDialog with a Y axis BoxLayout and specified parent
   */
  public JDialog(Dialog parent)
  {
  	super(parent);
  	setLayout (JFrame.Y_AXIS);
    initGlassPane();
  }

  /**
   * Create a new JDialog with the specified axis BoxLayout, parent
   * component and modality.
   */
  public JDialog(int axis, Any parent, boolean modal)
  {
		super((Frame)(((AnyComponent)parent).getComponent()), modal);
  	setLayout (axis);
    initGlassPane();
  }

  /**
   * Create a new JDialog with the specified axis BoxLayout and
   * modality with no parent.
   */
  public JDialog(int axis, boolean modal)
  {
		super();
		setModal(modal);
  	setLayout (axis);
    initGlassPane();
  }
  
  public void setAxis(Any axis)
  {
  	IntI iAxis = (IntI)axis;
  	setLayout(iAxis.getValue());
  }
  
  public Any getAxis()
  {
  	return null;
  }
  
  public void setLayout(int axis)
  {
		Container c = getContentPane();
		c.setLayout(new BoxLayout(c, axis));
	}
	
	public NotifyDialog getDialogGenerator()
	{
		if (notifyDialog_ == null)
			notifyDialog_ = new NotifyDialog();
			
		return notifyDialog_;
	}
	
	public void setHideOnOk(boolean hide)
	{
    hideOnOk_ = hide;
	}

	public void setHideOnCancel(boolean hide)
	{
    hideOnCancel_ = hide;
	}

	public boolean getHideOnOk()
	{
		return hideOnOk_;
	}
	
	public boolean getHideOnCancel()
	{
		return hideOnCancel_;
	}
	
	public void setDisabledText(Any text)
	{
	  DisabledGlassPane g = (DisabledGlassPane)this.getRootPane().getGlassPane();
	  
	  if (AnyNull.isNull(text))
	    g.deactivate();
	  else
	    g.activate(text.toString());
	}
	
  public void setClosed(boolean b) throws PropertyVetoException {}
  public void setIcon(boolean b) throws PropertyVetoException {}
  public void setSelected(boolean isSelected) throws PropertyVetoException {}

  public void setFrameIcon(Icon icon) {}
  public void setState(int state) {}
  public void revalidate() {}

  private void initGlassPane()
  {
    this.getRootPane().setGlassPane(new DisabledGlassPane());
  }

  public static class NotifyDialog
	{
		ArrayList listeners_ = new ArrayList();
		
		public void addDialogListener(DialogListener l)
		{
			listeners_.add(l);
		}

		public void removeDialogListener(DialogListener l)
		{
			int i = -1;
			if ((i = listeners_.indexOf(l)) >= 0)
				listeners_.remove(i);
		}
		
		public void fireDialogOk(DialogEvent e)
		{
			Iterator i = listeners_.iterator();
			while (i.hasNext())
			{
				DialogListener l = (DialogListener)i.next();
				l.dialogOk(e);
			}
		}
		
		public void fireDialogCancel(DialogEvent e)
		{
			Iterator i = listeners_.iterator();
			while (i.hasNext())
			{
				DialogListener l = (DialogListener)i.next();
				l.dialogCancel(e);
			}
		}
	}
}
