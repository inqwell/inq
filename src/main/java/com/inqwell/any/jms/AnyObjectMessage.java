/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import com.inqwell.any.Any;
import com.inqwell.any.AnyObject;
import com.inqwell.any.RuntimeContainedException;

public class AnyObjectMessage extends    AnyMessage
                                 implements ObjectMessageI
{

  public AnyObjectMessage(ObjectMessage m)
  {
    super(m);
  }

  public AnyObjectMessage(Message m)
  {
    super(m);
    
    if (!(m instanceof ObjectMessage))
      throw new IllegalArgumentException("Not a ObjectMessage");
  }
  
  public Any getAny()
  {
    return getObject();
  }

  public Any getObject()
  {
    try
    {
      Object o = getMessage().getObject();
      Any ret;
      
      if (o instanceof Any)
        ret = (Any)o;
      else
        ret = new AnyObject(o);
      
      return ret;
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public void setObject(Any a)
  {
    try
    {
      getMessage().setObject(a);
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  public Any getMessageType()
  {
    return object__;
  }

  public void setAny(Any a)
  {
    setObject(a);
  }
  

  private ObjectMessage getMessage()
  {
    return (ObjectMessage)msg_;
  }
}
