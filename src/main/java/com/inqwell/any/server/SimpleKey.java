/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/SimpleKey.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.server;

import com.inqwell.any.Descriptor;
import com.inqwell.any.KeyDef;
import com.inqwell.any.Any;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.Map;
import com.inqwell.any.AnyException;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractKeyDef;
import com.inqwell.any.Transaction;
import com.inqwell.any.io.PhysicalIO;
import com.inqwell.any.io.AnyIOException;
import com.inqwell.any.client.ClientKey;
import java.io.ObjectStreamException;

/**
 * A simple key implementation.  Instances are
 * not <i>managed</i> and, as such cannot be handled
 * by <code>Transaction</code> implementations
 * like <code>TwoPhaseTransaction</code>.
 */
public class SimpleKey extends    IoKey
											 implements KeyDef
{
	/**
	 * Make a new Simple Key definition.  There is no relationship between
	 * all keys for a given item and the primary key
	 * where <code>SimpleKey</code> is concerned as this class
	 * only serves as a bridge to the I/O system and does no
	 * caching nor any referential object integrity.  This constructor
	 * exists only to satisfy the XSL scripts used to create
	 * system configurations.
	 */
  public SimpleKey(String name, Any primary, boolean unique)
  {
    super(name, unique);
  }
  
  /**
	 * There is no primary, see comments on other constructors.
	 */
  public SimpleKey(String name)
  {
		this(name, null, true);
  }
  
  public Any read (Map keyVal, int maxCount) throws AnyException
  {

    Any ret = null;
		
    if (isUnique())
    {
      // instances are not managed.
      ret = io_.read (keyVal,
                      (Map)getDescriptor().newInstance());
    }
    else
    {
      Array a = AbstractComposite.array();
      int i = io_.read(keyVal,
											 (Map)getDescriptor().newInstance(),
											 a,
                       maxCount);
      if (i != 0)
        ret = a;
    }
    
    return ret;
  }
	
	public void manage (Map instanceVal)
	{
	  // no-op
	}
	
	public void unmanage (Map instanceVal)
	{
	  // no-op
	}
	
	public void resync (Transaction t, Map m) throws AnyException
  {
	  // no-op
  }

  public Any getFromPrimary(Any key)
  {
	  // no-op
    return null;
  }
  
  public boolean isPrimary()
  {
    return false;
  }
  
  public void setIO (PhysicalIO io, Descriptor d)
  {
    io_ = io;
    io_.setAuxInfo(auxInfo_, null);
    System.out.println ("SimpleKey.setIO " + io_);
  }

  public boolean isValid()
  {
    return true;
  }   
}
