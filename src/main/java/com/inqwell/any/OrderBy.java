/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/OrderBy.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Sort an <code>Orderable</code>. This function accepts a node
 * to sort and an optional list of child nodes to sort it by.
 * The underlying <code>AnyComparator</code> examines each
 * immediate child of the sort node and applies each expression
 * in the list to the child nodes.
 * <p>
 * The sort node must implement the <code>Orderable</code> interface.
 * The expressions can be any valid Inq expression and this can
 * be used to affect the ordering.
 * For example, <code>&lt;node
 * order ascending by <code>x</code>
 * while <code>&lt;negate select="x"/&gt;</code> will order
 * descending by <code>x</code>.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class OrderBy extends    AbstractFunc
									   implements Cloneable
{
  private static final long serialVersionUID = 1L;
  
  public static final IntI NULL_HIGH = new ConstInt(OrderComparator.NULL_HIGH);
  public static final IntI NULL_LOW  = new ConstInt(OrderComparator.NULL_LOW);

  private Any orderBy_;    // the items we will order by; resolves to an Array
	private Any orderNode_;  // the root from which we will order; Orderable
	private Any descending_ = AnyBoolean.FALSE;
	private Any ignorecase_ = AnyBoolean.FALSE;
  private Any nullMode_   = NULL_HIGH;

	public OrderBy(Any orderNode)
	{
	  this(orderNode, null);
	}

	public OrderBy(Any orderNode, Any orderBy)
	{
		orderNode_ = orderNode;
		orderBy_   = orderBy;
		init();
	}

	public Any exec(Any a) throws AnyException
	{
		Array orderBy = (Array)EvalExpr.evalFunc(getTransaction(),
																						 a,
																						 orderBy_,
																						 Array.class);
		if (orderBy == null)
		  nullOperand(orderBy_);
                                             
		Orderable orderNode = (Orderable)EvalExpr.evalFunc
																						(getTransaction(),
																						 a,
																						 orderNode_,
																						 Orderable.class);

		if (orderNode == null)
			nullOperand(orderNode_);
		
		// Performing "sort(node, null)" removes any comparator cached in
		// the node (though not any dynamic order-backing support at the moment)
		if (orderBy.entries() == 1 && AnyNull.isNullInstance(orderBy.get(0)))
			orderBy = null;
		
    OrderComparator orderComparator = makeComparator(a,
                                                     orderNode,
                                                     getTransaction());
		orderNode.sort(orderBy, orderComparator);

		return orderNode;
	}

  public Array getOrderBy()
  {
    // Slightly yucky method called by AnyTable et al so that
    // expressions (rather than just paths) can be specified when
    // setting up modelSort by passing an instance of OrderBy to
    // those classes.  The parser ensures that the orderBy_ member
    // must be a an array and the general method of expression
    // execution ensures that this instance will have been cloned,
    // so its OK to a) do the cast and b) return the member !!
    return (Array)orderBy_;
  }
  
  public void setDescending(Any descending)
  {
  	descending_ = descending;
  }
  
  public void setIgnoreCase(Any ignorecase)
  {
  	ignorecase_ = ignorecase;
  }
  
  public void setNullMode(Any nullMode)
  {
    nullMode_ = nullMode;
  }
  
  public OrderComparator makeComparator(Any         a,
                                        Orderable   orderNode,
                                        Transaction t) throws AnyException
  {
    boolean d  = false;
    boolean ic = false;

    Any descending = EvalExpr.evalFunc(getTransaction(),
                                       a,
                                       descending_);
    if (descending == null && descending_ != null)
      nullOperand(descending_);
    
    Any ignorecase = EvalExpr.evalFunc(getTransaction(),
                                       a,
                                       ignorecase_);
    if (ignorecase == null && ignorecase_ != null)
      nullOperand(ignorecase_);

    IntI nullMode = (IntI)EvalExpr.evalFunc(getTransaction(),
                                            a,
                                            nullMode_,
                                            IntI.class);

    if (nullMode == null && nullMode_ != null)
      nullOperand(nullMode_);
    
    AnyBoolean ab = new AnyBoolean(descending);
    d = ab.getValue();
    
    ab.copyFrom(ignorecase);
    ic = ab.getValue();

    OrderComparator orderComparator = new AnyComparator();

    if (orderNode instanceof Map)
    {
      orderComparator.setToOrder((Map)orderNode);
    }

    orderComparator.setDescending(d);
    orderComparator.setIgnoreCase(ic);
    orderComparator.setNullMode(nullMode.getValue());
    orderComparator.setTransaction(t);

    return orderComparator;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
		OrderBy o = (OrderBy)super.clone();

    o.orderNode_  = orderNode_.cloneAny();
    o.orderBy_    = AbstractAny.cloneOrNull(orderBy_);
    o.descending_ = descending_.cloneAny();
    o.ignorecase_ = ignorecase_.cloneAny();
    o.nullMode_   = nullMode_.cloneAny();

    o.init();

    return o;
  }

  private void init()
  {
  }
}
