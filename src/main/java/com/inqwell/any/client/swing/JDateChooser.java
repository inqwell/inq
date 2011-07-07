/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client.swing;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

import com.inqwell.any.AnyDate;
import com.toedter.calendar.JSpinnerDateEditor;
import com.toedter.calendar.JTextFieldDateEditor;

public class JDateChooser extends com.toedter.calendar.JDateChooser
{
  private ArrayList<ChangeListener> listeners_ = new ArrayList<ChangeListener>();

  private static AnyDate globalToday__;

  public JDateChooser()
  {
    super(new JSpinnerDateEditor());
    init();
  }
  
  public void addChangeListener(ChangeListener l)
  {
    listeners_.add(l);
  }
  
  public void removeChangeListener(ChangeListener l)
  {
    int i = -1;
    if ((i = listeners_.indexOf(l)) >= 0)
      listeners_.remove(i);
  }

  public void setGlobalToday(AnyDate date)
  {
    globalToday__ = date;
  }
  
  public JTextComponent getTextComponent()
  {
    return getDateEditor().getTextComponent();
  }

  public void setEditable(boolean editable)
  {
    getTextComponent().setEditable(editable);
    calendarButton.setEnabled(editable);
  }
  
  private  void fireStateChanged(ChangeEvent e)
  {
    for (int i = 0; i < listeners_.size(); i++)
    {
      ChangeListener c = (ChangeListener)listeners_.get(i);
      c.stateChanged(e);
    }
  }
  
  private void setTodaysDate()
  {
    if (globalToday__ != null)
      setDate(globalToday__.getValue());
    else
      setDate(new Date());
  }
  
  private void init()
  {
    // Add an action for setting the date to "today" - ctrl-t
    KeyStroke nullDate = KeyStroke.getKeyStroke(KeyEvent.VK_T,
        ActionEvent.CTRL_MASK, false);
    getDateEditor().getTextComponent().getInputMap().put(nullDate, "Today");
    getDateEditor().getTextComponent().getActionMap().put("Today", new AbstractAction()
    {
      private static final long serialVersionUID = -1913767779079949632L;
      public void actionPerformed(ActionEvent e)
      {
        if (!JDateChooser.this.isEnabled())
          return;
        setTodaysDate();
      }
    });

    this.addPropertyChangeListener("date", new DateListener());
  }
  
  private class DateListener implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent evt)
    {
      if (evt.getPropertyName().equals("date"))
        fireStateChanged(new ChangeEvent(JDateChooser.this));
    }
  }
}
