/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyButton.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import java.awt.Container;

import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;

import com.inqwell.any.Any;
import com.inqwell.any.AnyInt;
import com.inqwell.any.IntI;

public class AnyArrowButton extends AnyComponent
{
  private static final long serialVersionUID = 1L;

  private BasicArrowButton b_;
  private IntI             direction_ = new AnyInt();

  public void setObject(Object o)
  {

    if (!(o instanceof BasicArrowButton))
      throw new IllegalArgumentException
                  ("AnyButton wraps javax.swing.plaf.basic.BasicArrowButton and sub-classes");


    b_ = (BasicArrowButton)o;    
    direction_.setValue(b_.getDirection());

    super.setObject(b_);
  }
  
  public Container getComponent()
  {
    return b_;
  }
  
  public void initAsCellEditor()
  {
    b_.setOpaque(true);
  }

  protected void setValueToComponent(Any v)
  {
    // For an arrow button we assume the direction property as the default
    // rendered value.
    direction_.copyFrom(v);
    if (validDirection(direction_.getValue()))
      b_.setDirection(direction_.getValue());
  }
  
  private boolean validDirection(int d)
  {
    return (d == SwingConstants.NORTH ||
            d == SwingConstants.SOUTH ||
            d == SwingConstants.EAST  ||
            d == SwingConstants.WEST);
  }
}
