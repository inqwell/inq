/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyComboBoxEditor.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-18 21:45:00 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;
import javax.swing.ComboBoxEditor;
import com.inqwell.any.client.swing.JTextField;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionListener;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import javax.swing.event.DocumentEvent;
import java.util.ArrayList;

/**
 * The Inq combo box editor.  All JComboBoxes automatically install
 * an instance of this class as their editor (irrespective of whether
 * they are editable or not) and lazily create a JTextField when
 * required. The supporting RenderInfo object is used to provide
 * for formatting.
 * <p>
 * For the moment, there is no control available to Inq script
 * over the editing component used or its configuration. This
 * could be provided by an alternative ComboBoxEditor implementation.
 */
public class AnyComboBoxEditor extends    AbstractAny
															 implements ComboBoxEditor,
															            ActionListener,
															            DocumentListener
{
  private static Border editorBorder__ = new EmptyBorder(3, 3, 3, 3); 

  private RenderInfo r_;
  
	private JTextField t_;
	
	private ArrayList actionListeners_   = new ArrayList();
	private ArrayList documentListeners_ = new ArrayList();
	
  public AnyComboBoxEditor(RenderInfo r)
  {
    r_ = r;
  }
	
  public void addActionListener(ActionListener l)
  {
  	// We take over the maintenance of the action listeners
  	// registered (in fact only the combo box) as the
  	// underlying editor component is allowed to change
  	actionListeners_.add(l);
  }

  public Component getEditorComponent()
  {
		if (t_ == null)
      setComponent(new JTextField());
      
  	return t_;
  }
  
  public Object getItem()
  {
  	// Always returns a new object
  	return new ConstString(t_.getText());
  }

  public void removeActionListener(ActionListener l)
  {
  	int indx;
  	
  	if ((indx = actionListeners_.indexOf(l)) >= 0)
	  	actionListeners_.remove(indx);
  }

  public void selectAll()
  {
  	t_.selectAll();
  }
  
  public void setItem(Object anObject)
  {
		//t_.setText(anObject.toString());
    // ----------
  	Any a = (Any)anObject;
  	
  	// From the list model we must look into the int/ext
  	// map etc.
		if (anObject instanceof Map)
		{
			Map m = (Map)anObject;
			
			if (m.hasKeys(AnyListModel.listKeys__))
			{
				if ((a = m.get(ListRenderInfo.external__)) == null)
					a = m.get(ListRenderInfo.internal__);

			}
		}
		
    if (a == null)
      a = AnyCellRenderer.null__;
    
    
		if (t_ == null)
		{
      setComponent(new JTextField());
      // MakeComponent does alignment via a visitor but leave for now
			//ec_ = MakeComponent.makeComboEditor(a, r_, null);
		}

    AnyFormat f = r_.getFormat(a);
		
		//System.out.println("AnyComboboxEditor.setItem " + a);
		
		t_.setText(f.format(a));
    
    t_.selectAll();
  }
  
  private void setComponent(Container c)
  {
  	if (t_ != null)
  	{
  	  t_.removeActionListener(this);
  	  t_.getDocument().removeDocumentListener(this);
    }
  	
  	t_ = (JTextField)c;
    t_.setBorder(editorBorder__);
  	t_.addActionListener(this);
    t_.getDocument().addDocumentListener(this);
  }
  
  public void actionPerformed(ActionEvent e)
  {
  	for (int i = 0; i < actionListeners_.size(); i++)
  	{
  		ActionListener al = (ActionListener)actionListeners_.get(i);
  		al.actionPerformed(e);
  	}
  }

  public void changedUpdate(DocumentEvent e)
  {
  	for (int i = 0; i < documentListeners_.size(); i++)
  	{
  		DocumentListener dl = (DocumentListener)documentListeners_.get(i);
  		dl.changedUpdate(e);
  	}
  }
  
  public void insertUpdate(DocumentEvent e) 
  {
  	for (int i = 0; i < documentListeners_.size(); i++)
  	{
  		DocumentListener dl = (DocumentListener)documentListeners_.get(i);
  		dl.insertUpdate(e);
  	}
  }
  
  public void removeUpdate(DocumentEvent e) 
  {
  	for (int i = 0; i < documentListeners_.size(); i++)
  	{
  		DocumentListener dl = (DocumentListener)documentListeners_.get(i);
  		dl.removeUpdate(e);
  	}
  }

  public void addDocumentListener(DocumentListener l)
  {
  	documentListeners_.add(l);
  }

  public void removeDocumentListener(DocumentListener l)
  {
  	int indx;
  	
  	if ((indx = documentListeners_.indexOf(l)) >= 0)
	  	documentListeners_.remove(indx);
  }
}
