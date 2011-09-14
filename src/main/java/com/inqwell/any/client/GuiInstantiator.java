/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/GuiInstantiator.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.beans.Facade;
import com.inqwell.any.client.swing.SwingInvoker;

/**
 * A function which creates a Container derived and
 * wraps it inside the specified AnyComponent derived.
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class GuiInstantiator extends    AbstractFunc
                             implements Cloneable
{
  static public String defaultWrapper__ = "com.inqwell.any.client.AnyComponent";
  
	private String wrapperClass_;
	private String awtClass_;
	
	private Object o_;
	
	public GuiInstantiator (String awtClass, String wrapperClass)
	{
		wrapperClass_ = wrapperClass;
		awtClass_     = awtClass;
	}

	/**
	 * 
	 */
	public Any exec(Any a) throws AnyException
	{
    Facade any = null;
    
    String wrapperClass = (wrapperClass_ != null) ? wrapperClass_
                                                  : defaultWrapper__;
    
    String awtClass = awtClass_;
    
    try
    {
    	Object o = guiObject(awtClass);
      any = (Facade)Class.forName(wrapperClass).newInstance();

      if (o != null)
        any.setObject(o);
    }
    catch(Exception e)
    {
      throw new ContainedException(e);
    }
    
	  return any;
	}
	
	protected Object makeAwtObject(String awtClass) throws Exception
	{
    Object o = Class.forName(awtClass).newInstance();
    return o;
	}

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
  
  private Object guiObject(final String awtClass) throws Exception
  {
    // When creating certain types of dockable, the external object
    // cannot be created at the same time as its wrapper
    if (awtClass == null)
      return null;
    
    SwingInvoker ss = new SwingInvoker()
    {
      protected void doSwing()
      {
        try
        {
          o_ = makeAwtObject(awtClass);
        }
        catch (Exception e)
        {
          throw new RuntimeContainedException(e);
        }
      }
    };

    ss.maybeSync();
    //return makeAwtObject(awtClass);
    return o_;
  }
}
