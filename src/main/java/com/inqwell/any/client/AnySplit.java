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

import javax.swing.JSplitPane;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyDouble;
import com.inqwell.any.AnyInt;
import com.inqwell.any.Set;

public class AnySplit extends AnyComponent
{
  private static final long serialVersionUID = 1L;

  private JSplitPane s_;

  private static Set      splitProperties__;

  static
  {
    splitProperties__ = AbstractComposite.set();
    splitProperties__.add(AbstractValue.flyweightString("dividerLocation"));
    splitProperties__.add(AbstractValue.flyweightString("proportionalLocation"));
  }
  
  public void setObject(Object o)
  {

    if (!(o instanceof JSplitPane))
      throw new IllegalArgumentException
                  ("AnySplit wraps javax.swing.JSplitPane and sub-classes");


    s_ = (JSplitPane)o;      

    super.setObject(s_);
  }
  
  public Container getComponent()
  {
    return s_;
  }
  
  public void setDividerLocation(Any l)
  {
    AnyInt i = new AnyInt(l);
    s_.setDividerLocation(i.getValue());
  }

  public void setProportionalLocation(Any l)
  {
    AnyDouble d = new AnyDouble(l);
    s_.setDividerLocation(d.getValue());
  }

  protected Object getPropertyOwner(Any property)
  {
    if (splitProperties__.contains(property))
      return this;
    
    return super.getPropertyOwner(property);
  }
  

}
