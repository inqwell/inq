/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client.dock;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.AbstractInputFunc;
import com.inqwell.any.AbstractVisitor;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.BooleanI;
import com.inqwell.any.ByteI;
import com.inqwell.any.Call;
import com.inqwell.any.CharI;
import com.inqwell.any.DateI;
import com.inqwell.any.Decimal;
import com.inqwell.any.DoubleI;
import com.inqwell.any.FloatI;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.LongI;
import com.inqwell.any.Map;
import com.inqwell.any.ShortI;
import com.inqwell.any.StringI;

public class DockInstantiator extends    AbstractFunc
                              implements Cloneable
{
  static private CheckArg checkArg__ = new CheckArg();
  
  private Call buildGui_;
  
  public DockInstantiator(Call buildGui)
  {
    buildGui_ = buildGui;
  }
  
  public Any exec(Any a) throws AnyException
  {
    if (buildGui_ == null)
      return new AnySingleCDockable();
    else
    {
      // Put the transaction in
      buildGui_.setTransaction(getTransaction());

      // Resolve the call arguments ready for persisting the layout
      AbstractInputFunc calledFunc = buildGui_.resolveFunc(a, getTransaction());
      if (calledFunc == null)
        throw new AnyException("Could not resolve function " + buildGui_);

      Map args = buildGui_.resolveArgs(a, calledFunc, getTransaction());
      Iter i = args.createKeysIterator();
      while (i.hasNext())
      {
        Any k = i.next();
        Any v = args.get(k);
        
        if (k.equals(AnyMultipleCDockable.PARENT))
        {
          i.remove();
          continue;
        }
        
        if (k == null)
          throw new AnyException("Argument " + k + " did not resolve");

        // Check the argument type is acceptable for saving as
        // part of the desktop state
        // via AnyMultipleCDockable.MetaData.writeXML()
        k.accept(checkArg__);
        
        // TODO: Check args have resolved to things that dockingframes
        // is capable of saving (scalars)?
      }
      buildGui_.setArgs(args);

      return new AnyMultipleCDockable(getTransaction().getContext(), buildGui_);
    }
  }
  
  public Object clone() throws CloneNotSupportedException
  {
    DockInstantiator d = (DockInstantiator)super.clone();
    
    d.buildGui_ = (Call)AbstractAny.cloneOrNull(buildGui_);
    
    return d;
  }
  
  private static class CheckArg extends AbstractVisitor
  {
    protected void unsupportedOperation (Any o)
    {
      throw new IllegalArgumentException
        ("Restore function argument of type " +
          o.getClass().getName() +
          " is not a scalar");
    }
    
    // Just a load of no-ops for the types that are OK 
    public void visitAnyBoolean (BooleanI b)
    {
    }

    public void visitAnyByte (ByteI b)
    {
    }

    public void visitAnyChar (CharI c)
    {
    }

    public void visitAnyInt (IntI i)
    {
    }

    public void visitAnyShort (ShortI s)
    {
    }

    public void visitAnyLong (LongI l)
    {
    }

    public void visitAnyFloat (FloatI f)
    {
    }

    public void visitAnyDouble (DoubleI d)
    {
    }

    public void visitDecimal (Decimal d)
    {
    }

    public void visitAnyString (StringI s)
    {
    }

    public void visitAnyDate (DateI d)
    {
    }
  }
}
