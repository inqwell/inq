/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import com.inqwell.any.Any;
import com.inqwell.any.AnyString;
import com.inqwell.any.RuntimeContainedException;

public class AnyTextMessage extends AnyMessage implements TextMessageI
{

  public AnyTextMessage(TextMessage m)
  {
    super(m);
  }

  public AnyTextMessage(Message m)
  {
    super(m);
    
    if (!(m instanceof TextMessage))
      throw new IllegalArgumentException("Not a TextMessage");
  }

  public Any getText()
  {
    try
    {
      return new AnyString(getMessage().getText());
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void setText(Any a)
  {
    try
    {
      getMessage().setText(a.toString());
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public Any getMessageType()
  {
    return text__;
  }

  private TextMessage getMessage()
  {
    return (TextMessage)msg_;
  }

}
