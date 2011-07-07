/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JComboBox.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.inqwell.any.Any;
import com.inqwell.any.ConstString;
import com.inqwell.any.LocateNode;
import com.inqwell.any.client.AnyCellRenderer;
import com.inqwell.any.client.AnyComboBoxEditor;
import com.inqwell.any.client.AnyNewListModel;
import com.inqwell.any.client.RenderInfo;

public class AnyNewJComboBox extends javax.swing.JComboBox
{
  private static Any proto__ = new ConstString("nnnnnnnnnn"); // 10 chars
  
  private boolean itemFired_;
  
  public AnyNewJComboBox()
  {
    super(new AnyNewListModel(LocateNode.null__));
    init((AnyNewListModel)getModel(), null);

    //KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
    //this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enter, "ENTER");
    //this.getActionMap().put("ENTER", enterAction__);
  }

  public AnyNewJComboBox(AnyNewListModel m)
  {
    super(m);
    init(m, null);
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
    // TODO: Remove
    super.setSelectedItem(anObject);
  }

  public void actionPerformed(ActionEvent e)
  {
    if (!isEditable() || !isPopupVisible())
    {
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

  protected boolean processKeyBinding(KeyStroke ks,
                                      KeyEvent  e,
                                      int       condition,
                                      boolean pressed)
  {
    //System.out.println("JComboBox.processKeyBinding");
    
    //if true event consumed
    boolean ret = super.processKeyBinding(ks,e,condition,pressed);
    
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

  private void init(AnyNewListModel m, RenderInfo itemRenderInfo)
  {
    //AnyRenderer ar = new AnyRenderer(r);
    AnyCellRenderer ar = new AnyCellRenderer(itemRenderInfo);
    this.setRenderer(ar);

    if (itemRenderInfo != null)// && !r.isEnum())
    {
      //AnyComponentEditor ac = new AnyComponentEditor(r);
      AnyComboBoxEditor ac = new AnyComboBoxEditor(itemRenderInfo);
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

  public void reinit(AnyNewListModel m,
                     RenderInfo      itemRenderInfo)
  {
    setModel(m);
    init(m, itemRenderInfo);
  }
}
