/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/IoKey.java $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */


package com.inqwell.any.server;

import com.inqwell.any.Descriptor;
import com.inqwell.any.KeyDef;
import com.inqwell.any.Any;
import com.inqwell.any.ConstString;
import com.inqwell.any.Array;
import com.inqwell.any.Map;
import com.inqwell.any.Iter;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractKeyDef;
import com.inqwell.any.Transaction;
import com.inqwell.any.server.cache.*;
import com.inqwell.any.io.PhysicalIO;
import com.inqwell.any.io.NullIO;
import com.inqwell.any.io.csv.CsvIO;
import com.inqwell.any.io.AnyIOException;
import com.inqwell.any.client.ClientKey;
import com.inqwell.any.client.StackTransaction;
import java.io.ObjectStreamException;

/**
 * A key definition that will use an I/O mechanism to retrieve instances.
 */
public abstract class IoKey extends    AbstractKeyDef
                            implements KeyDef
{
  protected transient  PhysicalIO io_;    // primary io source/sink
  
  protected boolean isPersistent_;
  
	protected Map       auxInfo_;

	/**
	 *
   */
  public IoKey(String name, boolean unique)
  {
    setUnique (unique);
		setName (new ConstString(name));
  }
  
  public IoKey(String name)
  {
		this(name, true);
  }
  
  public void write (Map instanceVal, Transaction t) throws AnyException
  {
    if (isUnique())
      io_.write (instanceVal, t);
    else
      throw new AnyIOException("Attempt to write with non-unique key");
  }

  public void write (Map keyVal, Map instanceVal, Transaction t) throws AnyException
  {
    if (isUnique())
      io_.write (keyVal, instanceVal, t);
    else
      throw new AnyIOException("Attempt to write with non-unique key");
  }

  public void delete (Map instanceVal, Transaction t) throws AnyException
  {
    if (isUnique())
      io_.delete (instanceVal, t);
    else
      throw new AnyIOException("Attempt to delete with non-unique key");
  }

  public void delete (Map keyVal, Map instanceVal, Transaction t) throws AnyException
  {
    if (isUnique())
      io_.delete (keyVal, instanceVal, t);
    else
      throw new AnyIOException("Attempt to delete with non-unique key");
  }

  public PhysicalIO getIO ()
  {
    return io_;
  }

  public boolean isPersistent()
  {
    return isPersistent_;
  }
  
	public void setAuxInfo(Any a)
	{
    auxInfo_ = (Map)a;
    
    // Get the aux info from the primary key.  If the descriptor
    // is null then we are the primary key (chicken/egg during parsing)
    Descriptor d = getDescriptor();
    Any pax = (d != null) ? d.getPrimaryKey().getAuxInfo()
                          : a;
    
    if (io_ != null)
      io_.setAuxInfo((Map)a, (Map)pax);
	}

	public Any getAuxInfo()
  {
    return auxInfo_;
  }
}
