/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.beans;
import com.inqwell.any.Any;

/**
 * Allow wrapping of external sub-system objects into
 * the <code>Any</code> client-side framework.  The external
 * sub-system is assumed to support hierarchies
 * of objects in some generic way (i.e. Swing) although in the
 * framework these hierarchies may be different in their
 * function and requirements.
 */
public interface Facade extends Any
{
  public void   setObject(Object o);
  public Object getObject();

	public Any  getContext();

	public Any  getContextNode();
  
  public Facade getParentComponent();

}

