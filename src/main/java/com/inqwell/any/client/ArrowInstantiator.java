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

import javax.swing.SwingConstants;

public class ArrowInstantiator extends GuiInstantiator
{
  public ArrowInstantiator(String awtClass, String wrapperClass)
  {
    super(awtClass, wrapperClass);
  }

  protected Object makeAwtObject(String awtClass) throws Exception
  {
    // Well we just ignore the class name in any case.
    Object o = new javax.swing.plaf.basic.BasicArrowButton(SwingConstants.SOUTH);
    return o;
  }
}
