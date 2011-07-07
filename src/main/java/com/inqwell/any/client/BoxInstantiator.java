/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.client.swing.JPanel;

public class BoxInstantiator extends GuiInstantiator
{
  private int axis_;
  
  public BoxInstantiator(String awtClass, String wrapperClass, int axis)
  {
    super(awtClass, wrapperClass);
    axis_ = axis;
  }

  protected Object makeAwtObject(String awtClass) throws Exception
  {
    // Well we just ignore the class name in any case.
    Object o = new JPanel(axis_);
    return o;
  }
}
