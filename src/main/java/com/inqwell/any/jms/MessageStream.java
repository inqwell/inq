/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-05-02 20:34:15 $
 */
package com.inqwell.any.jms;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyNull;
import com.inqwell.any.Array;
import com.inqwell.any.IntI;
import com.inqwell.any.Map;
import com.inqwell.any.Process;
import com.inqwell.any.Transaction;
import com.inqwell.any.io.AbstractStream;

/**
 * A stream to read a StreamMessageI or BytesMessageI as a byte array
 */
public class MessageStream extends AbstractStream
{
  private BytesMessageI msg_;
  private Any           readVar_;

  public MessageStream()
  {
  }
  
  @Override
  protected void acceptStreams() throws AnyException
  {
    // There are no streams as such, the base class streams are
    // set to dummies
    
    MqURLConnection.MessageInputStream  i = (MqURLConnection.MessageInputStream)istream_;
    MqURLConnection.MessageOutputStream o = (MqURLConnection.MessageOutputStream)ostream_;
    if (i != null)
    {
      msg_ = i.getMessage();
      msg_.reset();
    }
    else if (o != null)
    {
      msg_ = o.getMessage();
      msg_.clearBody();
    }
  }

  @Override
  protected boolean doCloseRead()
  {
    msg_.reset();
    return true;
  }

  @Override
  protected boolean doCloseWrite()
  {
    msg_.reset();
    return true;
  }
  
  protected void doFlush()
  {
  }
  
  public void setReadTo(Any a)
  {
    if (AnyNull.isNull(a))
      readVar_ = null;
    else
      readVar_ = a;
  }

  @Override
  public boolean delete(Map ioKey, Map outputItem, Transaction t)
      throws AnyException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean delete(Map outputItem, Transaction t) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean open(Process p, Any toOpen, IntI mode) throws AnyException
  {
    boolean ret = makeStreams(p, toOpen, mode);
    
    acceptStreams();
    
    return ret;
  }

  @Override
  public Any read() throws AnyException
  {
    if (readVar_ == null)
      throw new java.lang.IllegalStateException("read variable not set");
    
    msg_.read(readVar_);
    
    return readVar_;
  }

  @Override
  public int read(Map ioKey, Map outputProto, Array outputComposite, int maxCount) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map read(Map ioKey, Map outputProto) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean write(Any outputItem, Transaction t) throws AnyException
  {
    msg_.write(outputItem);
    return true;
  }

  @Override
  public boolean write(Map ioKey, Map outputItem, Transaction t) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean writeln(Any outputItem, Transaction t) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

}
