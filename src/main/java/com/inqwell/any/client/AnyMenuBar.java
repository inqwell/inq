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

import javax.swing.JToolBar;

import com.inqwell.any.Any;
import com.inqwell.any.client.swing.JMenuBar;

public class AnyMenuBar extends AnyComponent
{
  private static final long serialVersionUID = 1L;

  private JMenuBar b_;

  public void setObject(Object o)
  {

    if (!(o instanceof JMenuBar))
      throw new IllegalArgumentException
                  ("AnyMenuBar wraps JMenuBar and sub-classes");


    b_ = (JMenuBar)o;      

    super.setObject(b_);
  }
  
  public Container getComponent()
  {
    return b_;
  }
}
