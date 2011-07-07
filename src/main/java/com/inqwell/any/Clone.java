/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Clone.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import com.inqwell.any.identity.HasIdentity;

/**
 * Create a clone of the given object.  If the object has a
 * Descriptor that is not degenerate then it is used to
 * create the new instance, into which the argument is
 * copied, thus ensuring the correct implementations of
 * interfaces are used.  Otherwise a straightforward clone
 * is taken (leaving the exact policy to the object,
 * therefore).
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class Clone extends    AbstractFunc
                   implements Cloneable
{
	private Any any_;
	private Any copyFrom_;
	
	public Clone(Any a)
	{
		this(a, null);	
	}
	
	public Clone(Any a, Any copyFrom)
	{
		any_      = a;
    copyFrom_ = copyFrom;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Any any = EvalExpr.evalFunc(getTransaction(),
																a,
																any_);

    if (any == null)
      nullOperand(any_);
    
		Any copyFrom = EvalExpr.evalFunc(getTransaction(),
                                 a,
                                 copyFrom_);
    
    if (copyFrom_ != null && copyFrom == null)
      nullOperand(copyFrom_);
    
    if (copyFrom != null)
      throw new AnyException("clone() copyFrom not supported (TODO)");
    
    Any ret;
    
    /*
    if (any instanceof Map)
    {
      Map m = (Map)any;
      ret = cloneMap(m, copyFrom);
    }
    else
    {
      ret = any.cloneAny();
      if (copyFrom != null)
        ret.copyFrom(copyFrom);
    }
    */
    
    CompositeClone cClone = new CompositeClone();
    cClone.setTransaction(getTransaction());
    ret = cClone.cloneItem(any);
		return ret;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    Clone c = (Clone)super.clone();
    
    c.any_      = any_.cloneAny();
    c.copyFrom_ = AbstractAny.cloneOrNull(copyFrom_);
    
    return c;
  }
  
  public Iter createIterator()
  {
    Array a = AbstractComposite.array();
    a.add(any_);
    if (copyFrom_ != null)
      a.add(copyFrom_);
    return a.createIterator();
  }

  /*
  private Map cloneMap(Map m, Any copyFrom)
  {
    Map ret;
    
    // Take care for managed objects - they typically don't do any
    // cloning at all, they just return themselves, so use explicit
    // new instance.
    Descriptor d = m.getDescriptor();
    if (d == Descriptor.degenerateDescriptor__)
    {
      ret = (Map)m.cloneAny();
      if (copyFrom != null)
        ret.copyFrom(copyFrom);
    }
    else
    {
      ret = (Map)d.newInstance();
      
      if (copyFrom != null)
        ret.copyFrom(copyFrom);
      else
        ret.copyFrom(m);

      ret.setUniqueKey(AbstractAny.cloneOrNull(m.getUniqueKey()));
      
      // When in the client, copy over the state of the transaction flag.
    }
    return ret;
  }
  */
  
  static private class CompositeClone extends AbstractVisitor
  {
    private Any cloned_;
    
    private Any cloneItem(Any item)
    {
      item.accept(this);
      
      Any ret = cloned_;
      cloned_ = null;
      return ret;
      
    }
    public void visitAnyBoolean(BooleanI b)
    {
      cloned_ = b.cloneAny();
    }

    public void visitAnyByte(ByteI b)
    {
      cloned_ = b.cloneAny();
    }

    public void visitAnyChar(CharI c)
    {
      cloned_ = c.cloneAny();
    }

    public void visitAnyDate(DateI d)
    {
      cloned_ = d.cloneAny();
    }

    public void visitAnyDouble(DoubleI d)
    {
      cloned_ = d.cloneAny();
    }

    public void visitAnyFloat(FloatI f)
    {
      cloned_ = f.cloneAny();
    }

    public void visitAnyInt(IntI i)
    {
      cloned_ = i.cloneAny();
    }

    public void visitAnyLong(LongI l)
    {
      cloned_ = l.cloneAny();
    }

    public void visitAnyObject(ObjectI o)
    {
      cloned_ = o.cloneAny();
    }

    public void visitAnyShort(ShortI s)
    {
      cloned_ = s.cloneAny();
    }

    public void visitAnyString(StringI s)
    {
      cloned_ = s.cloneAny();
    }

    public void visitArray(Array a)
    {
      // Make a new Array like the one given
      Array aC = (Array)a.buildNew(null);
      
      // Iterate over the current array adding cloned values
      Iter i = a.createIterator();
      while (i.hasNext())
      {
        Any v = i.next();
        aC.add(cloneItem(v));
      }
      
      // Set the member after traversing as a way to return the value
      cloned_ = aC;
    }

    public void visitDecimal(Decimal d)
    {
      cloned_ = d.cloneAny();
    }

    public void visitFunc(Func f)
    {
      cloned_ = f.cloneAny();
    }

    public void visitMap(Map m)
    {
      // Make a new map of the same type as the one given.
      // (Note - if m is a decorated map then the result
      // will be decorated also, which is OK in the client
      // but not in the server, so check for that).
      if (Globals.isServer() && m.hasIdentity())
      {
        HasIdentity h = (HasIdentity)m;
        m = h.getInstance();
      }
      
      Map mC = (Map)m.buildNew(null);
      
      // Some things don't support buildNew(), Slightly messy way of
      // stopping the clone descent for these types of Map implementation.
      if (mC != m)
      {
        
        // Copy over any Descriptor
        Descriptor d = m.getDescriptor();
        if (d != Descriptor.degenerateDescriptor__)
          mC.setDescriptor(d);
        
        // Iterate over the current set adding cloned values
        Iter i = m.createKeysIterator();
        while (i.hasNext())
        {
          Any k = i.next();
          Any v = m.get(k);
          if (v == null)
            mC.add(k, null);
          else
          {
            mC.add(k, cloneItem(v));
          }
        }
        
        // Handle the additional things carried in a map: 
        // 1) Unique Key. The safest thing to so is clone this. This is
        //    probably unnecessary but then we cannot damage someone
        //    else's.
        Any uk = m.getUniqueKey();
        if (uk != null)
          mC.setUniqueKey(cloneItem(uk));
        
        // 2. Transactional state. Only preserve in a client
        if (!Globals.isServer())
          mC.setTransactional(m.isTransactional());
        
        // 3. Node set flag. This is typically r/o so just transfer
        //    reference
        mC.setNodeSet(m.getNodeSet());
      }
      
      cloned_ = mC;
    }

    public void visitSet(Set s)
    {
      // Make a new Set like the one given
      Array sC = (Array)s.buildNew(null);
      
      // Iterate over the current set adding cloned values
      Iter i = s.createIterator();
      while (i.hasNext())
      {
        Any v = i.next();
        sC.add(cloneItem(v));
      }
      
      // Set the member after traversing as a way to return the value
      cloned_ = sC;
    }

    public void visitUnknown(Any o)
    {
      cloned_ = o.cloneAny();
    }
  }
}
