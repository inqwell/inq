/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyTabStop.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;
import javax.swing.text.TabStop;

public class AnyTabStop extends    AnyObject
                        implements Cloneable
{
	public static AnyTabStop null__ = new AnyTabStop((TabStop)null);

	/**
	 * Construct to wrap a pre-loaded Tab Stop.
	 */
	public AnyTabStop(TabStop t)
	{
		super(t);
	}

  /**
   * Construct around tab stop attributes in the given Map.
   */
	public AnyTabStop(Map m)
	{
    fromMap(m);
	}

	public TabStop getTabStop()
	{
		return (TabStop)getValue();
	}

  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
			if (!(a instanceof AnyTabStop))
				throw new IllegalArgumentException("AnyTabStop.copyFrom()");

			AnyTabStop s = (AnyTabStop)a;
			this.setValue(s.getValue());
		}
    return this;
  }

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
  
  private void fromMap(Map m)
  {
    float pos    = 0;
    int   align  = TabStop.ALIGN_LEFT;
    int   leader = TabStop.LEAD_NONE;
    
    IntI     i = new AnyInt();
    FloatI   f = new AnyFloat();
    
    tabAttr(f, NodeSpecification.tabPos__, m);
    pos = f.getValue();
    
    i.setValue(TabStop.ALIGN_LEFT);
    tabAttr(i, NodeSpecification.tabAlign__, m);
    align = i.getValue();
    
    i.setValue(TabStop.LEAD_NONE);
    tabAttr(i, NodeSpecification.tabLead__, m);
    leader = i.getValue();
    
    setValue(new TabStop(pos, align, leader));
  }
  
  private void tabAttr(Any out, Any attr, Map attrs)
  {
    if (attrs.contains(attr))
    {
      Any a = attrs.get(attr);
      out.copyFrom(a);
    }
  }
}
