/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JComboBox.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;

import com.inqwell.any.Any;
import com.inqwell.any.ConstString;
import com.inqwell.any.LocateNode;
import com.inqwell.any.Map;
import com.inqwell.any.client.AnyCellRenderer;
import com.inqwell.any.client.AnyComboBoxEditor;
import com.inqwell.any.client.AnyListModel;
import com.inqwell.any.client.ListRenderInfo;
import com.inqwell.any.client.RenderInfo;

public class JComboBox extends javax.swing.JComboBox
{
  private static Any proto__ = new ConstString("nnnnnnnnnn"); // 10 chars
  
  private boolean itemFired_;
  
	public JComboBox()
	{
		super(new AnyListModel(LocateNode.null__));
		init((AnyListModel)getModel());

    //KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
    //this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enter, "ENTER");
    //this.getActionMap().put("ENTER", enterAction__);
	}

	public JComboBox(AnyListModel m)
	{
		super(m);
		init(m);
	}

/*
  public JComboBox(RenderInfo r)
  {
	  RenderInfo lr = new ListRenderInfo(r.getDescriptor(), r.getField());

		setModel(new AnyListModel(lr));
		setRenderer(new AnyRenderer(r));
  }
*/

  public void setSelectedItem(Object anObject)
  {
    if ((!(anObject instanceof Map)) && anObject != null)
    {
      Map m = new ListRenderInfo.ListItemMap();
      m.add(ListRenderInfo.internal__, (Any)anObject);
      m.add(ListRenderInfo.external__, null);
      anObject = m;
    }
    //System.out.println("JComboBox.setSelectedItem " + anObject);
    super.setSelectedItem(anObject);
  }

  public void actionPerformed(ActionEvent e)
  {
    if (!isEditable() || !isPopupVisible())
    {
      //System.out.println("actionPerformed");
      super.actionPerformed(e);
    }
  }
  
  public boolean didFireItemStateChanged()
  {
    boolean ret = itemFired_;
    itemFired_ = false;
    return ret;
  }
  
  public void fireReselection(Any a)
  {
    fireItemStateChanged(new ItemEvent(this,
                                       ItemEvent.ITEM_STATE_CHANGED,
                                       a,
                                       ItemEvent.SELECTED));
  }

  protected void fireItemStateChanged(ItemEvent e)
  {
    super.fireItemStateChanged(e);
    itemFired_ = true;
  }

  public void fireActionEvent()
  {
    //System.out.println("popupVisible " + this.isPopupVisible());
    super.fireActionEvent();
  }
  
  public boolean processKeyBinding(KeyStroke ks,
                                   KeyEvent  e,
                                   int       condition,
                                   boolean   pressed)
  {
    //if true event consumed
    boolean ret = super.processKeyBinding(ks,e,condition,pressed);
//    System.out.println("JComboBox.processKeyBinding " + ret);
//    System.out.println("JComboBox.processKeyBinding " + condition);
//    System.out.println("JComboBox.processKeyBinding " + ks.getKeyCode());
    
    if (ret &&
        condition == JComponent.WHEN_FOCUSED &&
        ks.getKeyCode() == KeyEvent.VK_ENTER)
    {
      //System.out.println("JComboBox.processKeyBinding 1");
      if (isPopupVisible())
      {
        //System.out.println("JComboBox.processKeyBinding 1.1");
        setPopupVisible(false);
        ret = true;
      }
      else
      {
        //System.out.println("JComboBox.processKeyBinding 1.2");
        ret = false;
      }
    }

    // For table editors
    if (ret &&
        condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT &&
        ks.getKeyCode() == KeyEvent.VK_ENTER &&
        !isPopupVisible())
    {
      Boolean inTable = (Boolean)this.getClientProperty("JComboBox.isTableCellEditor");
      if (inTable != null && inTable.equals(Boolean.TRUE))
      {
        String oldCommand = this.getActionCommand();
        this.setActionCommand("comboBoxEdited");
        this.fireActionEvent();
        this.setActionCommand(oldCommand);
      }
    }
    
    if (ret &&
        condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT &&
        ks.getKeyCode() == KeyEvent.VK_ESCAPE)
    {
      //System.out.println("JComboBox.processKeyBinding 2");
      if (isPopupVisible())
      {
        //System.out.println("JComboBox.processKeyBinding 2.1");
        setPopupVisible(false);
        ret = true;
      }
      else
      {
        //System.out.println("JComboBox.processKeyBinding 2.2");
        ret = false;
      }
    }
    return ret;
  } 

  private void init(AnyListModel m)
	{
		RenderInfo  r  = m.getRenderInfo();
    //AnyRenderer ar = new AnyRenderer(r);
    AnyCellRenderer ar = new AnyCellRenderer(r);
    
    // Combo box pending rewrite - sorry!
    ListCellRenderer lr = this.getRenderer();
    if (lr instanceof ComboRenderer)
    {
      ComboRenderer cr = (ComboRenderer)lr;
      cr.rList_ = ar;
      cr.rSel_ = ar;
    }
    else
		  this.setRenderer(new ComboRenderer(ar, ar));

    r = m.getItemRenderInfo();
    if (r != null)// && !r.isEnum())
    {
			//AnyComponentEditor ac = new AnyComponentEditor(r);
			AnyComboBoxEditor ac = new AnyComboBoxEditor(r);
			this.setEditor(ac);
    }
    
    Any a = m.getPrototypeDisplayValue();
    if (a == null)
      a = proto__;
    
    //System.out.println("*** PROTO " + a);
    setPrototypeDisplayValue(a);
	}

  /**
   * The original can be improved upon!  If the combo box is
   * editable then focus to the editor component, otherwise
   * just focus to the combo box.  Makes for a richer user
   * experience.
   */
  public void requestFocus()
  {
    if (isEditable())
    {
      getEditor().getEditorComponent().requestFocus();
    }
    else
    {
      super.requestFocus();
    }
  }

  public void reinit(AnyListModel m)
  {
    setModel(m);
    init(m);
  }
  
  static private class ComboRenderer implements ListCellRenderer
  {
    ListCellRenderer rSel_;
    ListCellRenderer rList_;
    
    private ComboRenderer(ListCellRenderer rSel, ListCellRenderer rList)
    {
      rSel_ = rSel;
      rList_ = rList;
    }
    
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
      if (index < 0)
        return rSel_.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      
      return rList_.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
  }
}
