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
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Return a AnyInetAddress of the local host
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class GetLocalHost extends    AbstractFunc
                          implements Cloneable
{
  public GetLocalHost()
  {
  }
  
  public Any exec(Any a) throws AnyException
  {
    return AnyInetAddress.getLocalHost();
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    return super.clone();
  }
}
