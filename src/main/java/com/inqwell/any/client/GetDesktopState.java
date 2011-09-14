/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Map;

/**
 * Ask the desktop for its current state
 */
public class GetDesktopState extends    AbstractFunc
                             implements Cloneable
{
  public GetDesktopState ()
  {
  }

  public Any exec(Any a) throws AnyException
  {
    Map m = AbstractComposite.simpleMap();
    AnyWindow.saveDesktop(m);
    return m;
  }
  
  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
}
