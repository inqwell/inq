/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/PropertiesStream.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-05-02 20:06:45 $
 */
 

package com.inqwell.any.io;

import java.io.IOException;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Array;
import com.inqwell.any.IntI;
import com.inqwell.any.Map;
import com.inqwell.any.Process;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.SystemProperties;
import com.inqwell.any.Transaction;

/**
 * Reads a stream assuming it to be properties. Only read is supported. You
 * cannot write to a properties stream yet.
 * 
 */

public class PropertiesStream extends AbstractStream
{
  public boolean open(Process p, Any toOpen, IntI mode) throws AnyException
  {
    boolean ret = makeStreams(p, toOpen, mode);
    acceptStreams();
    return ret;
  }

  public Any read () throws AnyException
  {
    if (istream_ == null)
      throw new AnyException("Not opened for read");
    
    Map ret = AbstractComposite.simpleMap();

    SystemProperties.loadProperties(istream_, ret);

    return ret;
  }

  public int read (Map ioKey,
                   Map       outputProto,
                   Array     outputComposite,
                   int       maxCount) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

  public Map read (Map ioKey,
                   Map outputProto) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

  public boolean write (Any outputItem, Transaction t) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

  public boolean write (Map ioKey,
                        Map outputItem,
                        Transaction t) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
                        
  public boolean writeln (Any outputItem, Transaction t) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

  public boolean delete (Map ioKey,
                         Map outputItem,
                         Transaction t) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean delete (Map outputItem, Transaction t) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
  
  protected boolean doCloseRead()
  {
    return true;
  }
  
  protected boolean doCloseWrite()
  {
    return true;
  }

  public Object clone() throws CloneNotSupportedException
  {
    PropertiesStream s = (PropertiesStream)super.clone();
    return s;
  }

  protected void doFlush()
  {
  }
  
  protected void acceptStreams() throws AnyException
  {
    if (ostream_ != null)
      throw new AnyException("OPEN_WRITE is not supported");
  }
}
