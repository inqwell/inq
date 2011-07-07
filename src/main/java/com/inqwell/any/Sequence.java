/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Sequence.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * This class executes a sequence of Funcs.  The operand
 * is resolved and its iterator used to return each
 * immediate child.  Returns the result of the last func
 * in the sequence.
 */
public class Sequence extends    AbstractFunc
                      implements Cloneable
{
  private static final long serialVersionUID = 1L;

  private Any sequence_;
 

	/**
	 * We're expecting an Array or a Func evaluating to one.
	 */
  public Sequence(Any sequence)
  {
    sequence_    = sequence;
  }

  public Any exec(Any root) throws AnyException
  {
		Array sequence = (Array)EvalExpr.evalFunc(getTransaction(),
																		 root,
																		 sequence_,
                                     Array.class);

		Any ret = null;
		//System.out.println ("Sequence.exec sequence: " + sequence);
    int count = sequence.entries();
		for (int i = 0; i < count; i++)
		{
			Any func = sequence.get(i);        

			//System.out.println ("Sequence.exec sequence doing: " + func);
			// execute this func using the passed in root
			// ignore the return values since we don't know what we'd
			// do with them anyway.
			ret = EvalExpr.evalFunc(getTransaction(),
															root,
															func);
		}
    
	  return ret;
  }
  
  public Iter createIterator ()
  {
  	return sequence_.createIterator();
//  	Array a = AbstractComposite.array();
//  	a.add(sequence_);
//  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    Sequence a = (Sequence)super.clone();
    a.sequence_   = sequence_.cloneAny();        
    return a;
  }
  
  protected Any afterExecute(Any ret, Any a)
  {
    // Leave anything left in the transaction by the operand for the
    // outer function to pick up.
    return ret;
  }
}
