/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Declare.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Variable declaration.
 * Creates the inq variable at the specified node.  Similar
 * to <code>AddTo</code> except that the path must exist to
 * the point where the variable will be placed and must
 * be referenced only by a <code>Locate</code>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */ 
public class Declare extends    AbstractFunc
										 implements Cloneable
{
  private static final long serialVersionUID = 1L;

  protected Locate at_;  // bit messy - just for createIterator in deriveds
	protected Any    var_;
	
	public Declare (Locate at, Any var)
	{
		at_  = at;
		var_ = var;
	}
	
  public Any exec (Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    Any var = evaluateVar(a, t);
    
    placeVar(var, a, t);
    
		return var;
  }
  
  public Any getVariableName()
  {
  	return at_.getNodePath().getLast();
  }
  
  public Any getVariable()
  {
  	return var_;
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(at_);
  	a.add(var_);
  	return a.createIterator();
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    Declare d = (Declare)super.clone();
    
    // See exec() above for why we don't clone at_
    // Indeed, its all done lazily (for loops)
    //d.at_  = (Locate)at_.cloneAny();
    //d.var_ = var_.cloneAny();
    //d.compositeMembers_ = AbstractAny.cloneOrNull(compositeMembers_);
    return d;
    
  }

  protected Any evaluateVar(Any a, Transaction t) throws AnyException
  {
    Any var = EvalExpr.evalFunc(t,
                                a,
                                var_.cloneAny());

    // Special handling of txn map private instances - see
    // LocalTransaction.copyOnWrite()
    //if (var instanceof Map)
    //{
    //Map m = (Map)var;
    //Any tm = m.getUniqueKey();
    //if (tm != null && tm.isTransactional())
    //var = tm;
    //}
    
    var = AbstractAny.ripSafe(var, t);

    return var;
  }
  
  protected void placeVar(Any         var,
                          Any         a,
                          Transaction t) throws AnyException
  {
    // at's node spec is what we actually want.  It is read-only
    // unless it contains indirections.  Thus, we don't clone at_.
    NodeSpecification nsat = at_.getNodePath();
    if (nsat.mustShallowCopy())
      nsat = (NodeSpecification)nsat.shallowCopy();

    BuildNodeMap b = new BuildNodeMap(nsat,
                                      var);

    b.setTransaction(t);
    b.exec(a);
    

  }
  
  static public  Any forcePropertyRead(Any a, ReadProperty v)
  {
    if (a != null)
    {
      a.accept(v);
      return v.getAny();
    }
    return null;
  }

  /**
   * Simply records the last Any visited on. In so doing, PropertyBinding
   * objects are read and their underlying value returned. This visitor
   * thus ensures that anonymous declarations involving property reads
   * are resolved to the value rather than the property binding object
   * itself.
   * <p>
   * 
   */
  static public class ReadProperty extends AbstractVisitor
  {
    private static final long serialVersionUID = 1L;
    
    private Any a_;
    
    public Any getAny()
    {
      Any ret = a_;
      a_ = null;
      return ret;
    }
    
    public void visitAnyBoolean (BooleanI b)
    {
      a_ = b;
    }

    public void visitAnyByte (ByteI b)
    {
      a_ = b;
    }

    public void visitAnyChar (CharI c)
    {
      a_ = c;
    }

    public void visitAnyInt (IntI i)
    {
      a_ = i;
    }

    public void visitAnyShort (ShortI s)
    {
      a_ = s;
    }

    public void visitAnyLong (LongI l)
    {
      a_ = l;
    }

    public void visitAnyFloat (FloatI f)
    {
      a_ = f;
    }

    public void visitAnyDouble (DoubleI d)
    {
      a_ = d;
    }

    public void visitDecimal (Decimal d)
    {
      a_ = d;
    }

    public void visitAnyString (StringI s)
    {
      a_ = s;
    }

    public void visitAnyDate (DateI d)
    {
      a_ = d;
    }

    public void visitMap (Map m)
    {
      a_ = m;
    }

    public void visitArray (Array a)
    {
      a_ = a;
    }

    public void visitSet (Set s)
    {
      a_ = s;
    }

    //public void visitFunc (Func f)
    // There shouldn't be any funcs applied to this visitor. Allow it
    // to croak just to make sure

    public void visitAnyObject (ObjectI o)
    {
      a_ = o;
    }
    
    public void visitUnknown(Any o)
    {
      a_ = o;
    }
  }
}
