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

import javax.swing.JToolBar;

public class AnyToolBar extends AnyComponent
{
  private static final long serialVersionUID = 1L;

  private JToolBar b_;

  public void setObject(Object o)
  {

    if (!(o instanceof JToolBar))
      throw new IllegalArgumentException
                  ("AnyToolBar wraps javax.swing.JToolBar and sub-classes");


    b_ = (JToolBar)o;      

    super.setObject(b_);
  }
  
  public Container getComponent()
  {
    return b_;
  }


}
