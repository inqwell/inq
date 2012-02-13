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

import java.awt.Container;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.ConstString;
import com.inqwell.any.Set;

/**
 * An active Inq GUI component that may also act as a label for another
 * component.
 * <p>
 * The label is initialised when the labelFor property is set (typically
 * during parsing a layout). If the renderInfo property is never set then
 * the label cannot be updated from rendered data. This would be wasteful
 * unless the text (or other rendered properties) is required.
 *  
 * @author Tom
 * @deprecated
 */
public class AnyLabelFor extends AnyLabel
{
  private JLabel         l_;
  
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
    }
  }
  
  public Any getLabelFor()
  {
    // readonly
    return null;
  }
}
