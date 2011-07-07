/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/beans/Listener.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.beans;
import com.inqwell.any.*;

/**
 * The Listener Configuration interface.
 * This tells the client object the default Listener type for 
 * an object that can generate multiple events.
 */
public interface Listener extends Any
{
  public Any getDefaultEventType();
  public boolean hasDefaultEventType();
}

