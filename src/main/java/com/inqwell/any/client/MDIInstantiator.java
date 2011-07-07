/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/MDIInstantiator.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;
//import com.inqwell.any.beans.Facade;

/**
 * 
 */
public class MDIInstantiator extends    GuiInstantiator
                             implements Cloneable
{
	public MDIInstantiator (Any awtClass, Any wrapperClass)
	{
		super(wrapperClass.toString(), awtClass.toString());
	}

	public MDIInstantiator (String awtClass)
	{
    super(awtClass, null);
	}

	public MDIInstantiator (String awtClass, String wrapperClass)
	{
    super(awtClass, wrapperClass);
	}

	/**
	 * 
	 */
	protected Object makeAwtObject(String awtClass) throws Exception
	{
    // Accordingly from Inq.jj the object made will be a
    // com.inqwell.any.client.swing.JFrame.  All we want
    // to do is set the root pane container
    Object o = Class.forName(awtClass).newInstance();
    javax.swing.JFrame f = (javax.swing.JFrame)o;
    f.setContentPane(new javax.swing.JDesktopPane());
    return o;
	}

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
}
