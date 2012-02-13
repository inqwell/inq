/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.beans;
import com.inqwell.any.Any;

/**
 * Allow wrapping of external sub-system objects (also
 * called <code>components</code>)
 * into the <code>Any</code> client-side framework.
 * <p/>
 * The external sub-system is assumed to support hierarchies
 * of objects in some generic way (i.e. Swing) although in the
 * Any framework these hierarchies may be different to their
 * native counterparts as well as differing in their
 * function and requirements.
 * <p/>
 * Implementations must support a <code>context</code>, which
 * is a node of significance in <code>Any</code> hierarchy. This
 * node is typically the root of some application subdivision,
 * such as a window, self-standing dialog or tab child.  
 */
public interface Facade extends Any
{
  /**
   * Set the external sub-system object this is wrapping
   * @param o
   */
  public void   setObject(Object o);
  
  /**
   * Return the external sub-system object this is wrapping
   * @return
   */
  public Object getObject();

  /**
   * Return the <strong>path</strong> that represents the
   * context for this component. 
   * @return
   */
	public Any  getContext();

	/**
	 * Return the <strong>node</strong> that represents the
	 * context for this component. 
	 * @return
	 */
	public Any  getContextNode();
  
	/**
	 * Find this component's parent in the <code>Any</code> hierarchy
	 * @return the parent component or <code>null</code> if this
	 * component is at the top level.
	 */
  public Facade getParentComponent();

}

