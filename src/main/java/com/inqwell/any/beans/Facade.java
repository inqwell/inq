/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/beans/Facade.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.beans;
import com.inqwell.any.*;
import com.inqwell.any.Process;

/**
 * Allow wrapping of external sub-system objects into the <code>Any</code>
 * framework.  The external sub-system is assumed to support hierarchies
 * of objects in some generic way (i.e. Swing)
 */
public interface Facade extends Any
{
  public void   setObject(Object o);
  public Object getObject();

//  public void setContext(Any     context,
//												 Process p,
//												 boolean adjustExecutionContext);

	public Any  getContext();

	public Any  getContextNode();
  
  public Facade getParentComponent();
  
  public boolean isDisposed();

}

