/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyLabel.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.Any;
import com.inqwell.any.AnyNull;
import com.inqwell.any.Set;
import com.inqwell.any.ConstString;
import com.inqwell.any.AbstractComposite;

import javax.swing.Icon;
import javax.swing.JLabel;

import java.awt.Container;
import java.awt.Insets;
import java.text.Format;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * A label wrapper whose primary purpose is to act as a labeling
 * item for some other component.
 */
public class AnyLabel extends AnyComponent
{
  protected JLabel       l_;
  
  private static Set     labelProperties__;
  private static Any     labelFor__ = new ConstString("labelFor");

  static
  {
    labelProperties__ = AbstractComposite.set();
    labelProperties__.add(labelFor__);
  }
  
  public void setLabelFor(Any component)
  {
    if (component instanceof AnyComponent)
    {
      AnyComponent labelFor = (AnyComponent)component;
      l_.setText(labelFor.getRenderInfo().getLabel());
      l_.setLabelFor(labelFor.getComponent());
      labelFor.setLabelledBy(this);
    }
  }
  
  public Any getLabelFor()
  {
    // readonly
    return null;
  }
  
	public void setObject(Object o)
	{
		if (!(o instanceof JLabel))
			throw new IllegalArgumentException
									("AnyLabel wraps javax.swing.JLabel and sub-classes");


    l_ = (JLabel)o;
    
		super.setObject(o);
	}
		
  public Container getComponent()
  {
    return l_;
  }
  
//  public void setRenderInfo(RenderInfo r)
//  {
//    if (r != null)
//      l_.setText(getSpacesForWidth(r).toString());
//    
//    super.setRenderInfo(r);
//  }
  
  protected void setMargin(Insets i)
  {
    // Support for the Inq "margin" property on labels
    Border margin = new EmptyBorder(i);
    
    Border current = l_.getBorder();
    if (current != null)
      margin = BorderFactory.createCompoundBorder(margin, current);
    
    l_.setBorder(margin);
    
  }
  
  public void setClosedIcon(Icon icon)
  {
    ((DefaultTreeCellRenderer)l_).setClosedIcon(icon);
  }

  public void setOpenIcon(Icon icon)
  {
    ((DefaultTreeCellRenderer)l_).setOpenIcon(icon);
  }
  
  public void setLeafIcon(Icon icon)
  {
    ((DefaultTreeCellRenderer)l_).setLeafIcon(icon);
  }
  
  protected void setValueToComponent(Any v)
  {
    // For a label we assume the text property as the default
    // rendered value.
    if (v == AnyNull.instance())
      l_.setText(null);
    else
    {
      RenderInfo r = getRenderInfo();
      if (r != null)
      {
        Format f = getRenderInfo().getFormat(v);
        l_.setText(f.format(v));
      }
      else
      {
        if (v == null)
          l_.setText(null);
        else
          l_.setText(v.toString());
      }
    }
  }
}

