/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import java.awt.Container;
import java.text.Format;

import javax.swing.JMenu;

import com.inqwell.any.Any;

public class AnyMenu extends AnyComponent
{
  private static final long serialVersionUID = 1L;

  private JMenu m_;

  public void setObject(Object o)
  {

    if (!(o instanceof JMenu))
      throw new IllegalArgumentException
                  ("AnyMenu wraps javax.swing.JMenu and sub-classes");


    m_ = (JMenu)o;      

    super.setObject(m_);
  }
  
  public Container getComponent()
  {
    return m_;
  }

  protected void setValueToComponent(Any v)
  {
    // For a menu we assume the text property as the default
    // rendered value.
    // TODO: remove if not required
    super.setValueToComponent(v);
//    Format f = getRenderInfo().getFormat(v);
//    m_.setText(f.format(v));
  }
}
