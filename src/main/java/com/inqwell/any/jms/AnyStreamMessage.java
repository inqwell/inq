/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/**
 * 
 */
package com.inqwell.any.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.StreamMessage;

import com.inqwell.any.AbstractVisitor;
import com.inqwell.any.Any;
import com.inqwell.any.AnyByteArray;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.ByteI;
import com.inqwell.any.DateI;
import com.inqwell.any.Decimal;
import com.inqwell.any.DoubleI;
import com.inqwell.any.FloatI;
import com.inqwell.any.IntI;
import com.inqwell.any.LongI;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.ShortI;
import com.inqwell.any.StringI;
import com.inqwell.any.Visitor;

/**
 * @author tom
 */
public class AnyStreamMessage extends    AnyMessage
                              implements StreamMessageI,
                                         BytesMessageI
{
  private ContentConverter cc_;
  
  public AnyStreamMessage(StreamMessage m)
  {
    super(m);
  }

  public AnyStreamMessage(Message m)
  {
    super(m);
    
    if (!(m instanceof StreamMessage))
      throw new IllegalArgumentException("Not a StreamMessage");
  }

  public Any read(Any type)
  {
    if (cc_ == null)
      cc_ = new ContentConverter();
    
    cc_.setOp(Visitor.READ);
    type.accept(cc_);
    
    return type;
  }

  public void reset()
  {
    try
    {
      getMessage().reset();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void write(Any value)
  {
    if (cc_ == null)
      cc_ = new ContentConverter();
    
    cc_.setOp(Visitor.WRITE);
    value.accept(cc_);
    
  }


  @Override
  public Any getBodyLength()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Any readUnsignedByte()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Any readUnsignedShort()
  {
    throw new UnsupportedOperationException();
  }
  
  public Any getMessageType()
  {
    return stream__;
  }

  private StreamMessage getMessage()
  {
    return (StreamMessage)msg_;
  }

  private class ContentConverter extends AbstractVisitor
  {
    private Any op_;
    
    private void setOp(Any op)
    {
      op_   = op;
    }
    
    public void visitAnyBoolean(BooleanI b)
    {
      try
      {
        if (op_ == Visitor.READ)
          b.setValue(getMessage().readBoolean());
        else
          getMessage().writeBoolean(b.getValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyByte(ByteI b)
    {
      try
      {
        if (op_ == Visitor.READ)
          b.setValue(getMessage().readByte());
        else
          getMessage().writeByte(b.getValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyDate(DateI d)
    {
      try
      {
        // Perhaps a slightly dubious conversion to Date from long...
        if (op_ == Visitor.READ)
          d.setTime(getMessage().readLong());
        else
          getMessage().writeLong(d.getTime());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyDouble(DoubleI d)
    {
      try
      {
        if (op_ == Visitor.READ)
          d.setValue(getMessage().readDouble());
        else
          getMessage().writeDouble(d.getValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyFloat(FloatI f)
    {
      try
      {
        if (op_ == Visitor.READ)
          f.setValue(getMessage().readFloat());
        else
          getMessage().writeFloat(f.getValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyInt(IntI i)
    {
      try
      {
        if (op_ == Visitor.READ)
          i.setValue(getMessage().readInt());
        else
          getMessage().writeInt(i.getValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyLong(LongI l)
    {
      try
      {
        if (op_ == Visitor.READ)
          l.setValue(getMessage().readLong());
        else
          getMessage().writeLong(l.getValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyShort(ShortI s)
    {
      try
      {
        if (op_ == Visitor.READ)
          s.setValue(getMessage().readShort());
        else
          getMessage().writeShort(s.getValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyString(StringI s)
    {
      try
      {
        if (op_ == Visitor.READ)
          s.setValue(getMessage().readString());
        else
          getMessage().writeString(s.getValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitDecimal(Decimal d)
    {
      try
      {
        if (op_ == Visitor.READ)
          d.fromString(String.valueOf(getMessage().readDouble()));
        else
          getMessage().writeDouble(d.doubleValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitArray(Array a)
    {
      // Must be a AnyByteArray
      if (!(a instanceof AnyByteArray))
        throw new IllegalArgumentException("not a bytearray");
      
      AnyByteArray b = (AnyByteArray)a;

      if (op_ == READ)
      {
        try
        {
          int bytesRead = getMessage().readBytes(b.getValue());
          if (bytesRead < b.getValue().length)
          {
            byte[] bb = new byte[bytesRead];
            System.arraycopy(b.getValue(), 0, bb, 0, bytesRead);
            b.setValue(bb);
          }
        }
        catch(JMSException e)
        {
          throw new RuntimeContainedException(e);
        }
      }
      else
      {
        try
        {
          getMessage().writeBytes(b.getValue());
        }
        catch(JMSException e)
        {
          throw new RuntimeContainedException(e);
        }
      }
    }
  }
}
