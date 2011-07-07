/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyFileFilter.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import java.io.File;

import com.inqwell.any.client.AnyComponent;


/**
 * Wrap up the Inq consolidation of the various JDK file filtering
 * classes into an Any.
 * <p>
 * @see InqFileFilter
 */
public class AnyFileFilter extends    PropertyAccessMap
                           implements Cloneable
{
  private InqFileFilter filter_;
  
  private Map           propertyMap_;

  public AnyFileFilter()
	{
		filter_ = new InqFileFilter();
	}
	
  /**
   * Override.  If the key is <code>"properties"</code> then
   * return true.
   */
  public boolean contains(Any key)
  {
    if (properties__.equals(key))
      return true;
      
    return false;
  }

  /**
   * Override.  If the key is <code>"properties"</code> then (make and)
   * return a property binding object.
   */
  public Any get(Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }
        
      return propertyMap_;
    }
    else
    {
      handleNotExist(key); // throws
      return null;
    }
  }
  
  public Any getIfContains(Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }

      return propertyMap_;
    }
    else
    {
      return null;
    }
  }

  public Any copyFrom (Any a)
  {
    if (!(a instanceof StringI))
      throw new IllegalArgumentException("Not a string (for a matcher)");
    
    AnyMatcher m = new AnyMatcher();
    m.setPattern(a);
    this.setMatcher(m);
    
    return this;
  }

  public void setPropertyBean(Object bean)
  {
    if (!(bean instanceof InqFileFilter))
      throw new IllegalArgumentException(bean.getClass().toString());
      
    filter_ = (InqFileFilter)bean;
  }

  public Object getPropertyBean()
  {
    return filter_;
  }

	public InqFileFilter inqFileFilter()
	{
		return filter_;
	}
	
	public void setDescription(Any descr)
	{
		filter_.setDescriptionString(descr);
	}
	
	public Any getDescription()
	{
		return filter_.getDescriptionString();
	}

	public void setAccept(Any acceptF)
	{
	  if (AnyNull.isNullInstance(acceptF))
	    filter_.setAccept(null);
	  else
	  {
      Call c = AbstractFunc.verifyCall(acceptF);
      Map args = c.getArgs();
      args.replaceItem(InqFileFilter.fileArg__, new LocateNode("$stack.selection"));
      
      AnyFuncHolder.FuncHolder af = (AnyFuncHolder.FuncHolder)acceptF;
      
      filter_.setAccept(af);
	  }
	}
	
	public boolean accept(File f)
	{
		return filter_.accept(f);
	}
	
	public void setMatcher(Any matcher)
	{
    if (matcher == null || AnyNull.isNullInstance(matcher))
    	filter_.setMatcher(null);
    else
    {
    	if(!(matcher instanceof AnyMatcher))
    		throw new IllegalArgumentException(matcher.getClass().toString() +
                                           " not a matcher");
    	
		  filter_.setMatcher((AnyMatcher)matcher);
    }
	}
	
	public void setAcceptDirs(boolean acceptDirs)
	{
		filter_.setAcceptDirs(acceptDirs);
	}
	
	public boolean getAcceptDirs()
	{
		return filter_.getAcceptDirs();
	}

	public Object clone() throws CloneNotSupportedException
	{
		AnyFileFilter a = (AnyFileFilter)super.clone();
		
		a.filter_      = (InqFileFilter)filter_.clone();
		a.propertyMap_ = null;
		
		return a;
	}
	  
}
