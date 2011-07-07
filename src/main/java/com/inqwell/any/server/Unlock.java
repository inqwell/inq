/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/Unlock.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see 
 */

package com.inqwell.any.server;

import com.inqwell.any.*;

/**
 * An explicit attempt by the current process to unlock the given object.
 * 
 */
public class Unlock extends    AbstractFunc
                    implements Cloneable
{
  private Any toUnlock_;

  /**
   * 
   */
  public Unlock(Any toUnlock)
  {
    toUnlock_   = toUnlock;
  }

  public Any exec(Any a) throws AnyException
  {
		Any toUnlock  = EvalExpr.evalFunc(getTransaction(),
                                      a,
                                      toUnlock_);
		
    if (toUnlock == null)
      nullOperand(toUnlock_);

    getTransaction().unlock(toUnlock);
      		
		return null;
  }

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(toUnlock_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    Unlock u = (Unlock)super.clone();
    
    u.toUnlock_     = toUnlock_.cloneAny();
    return u;
  }
}
