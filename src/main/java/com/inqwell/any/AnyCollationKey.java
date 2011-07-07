/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyCollationKey.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.text.CollationKey;

/**
 * A wrapper for the CollationKey class. Not exposed to the scripting language,
 * rather for internal use by {@link AnyComparator}.
 * @author Tom
 *
 */
public class AnyCollationKey extends AnyObject
{
	private boolean negate_;
	
	public AnyCollationKey(CollationKey c)
	{
		super(c);
	}

	public CollationKey getCollationKey()
	{
		return (CollationKey)getValue();
	}
	
	public int compare(AnyCollationKey target)
	{
		int ret =  getCollationKey().compareTo(target.getCollationKey());
		
		return negate_ ? -ret : ret;
	}
	
  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
    	if (!(a instanceof AnyCollationKey))
				throw new IllegalArgumentException(a.getClass().toString());
    	
    	AnyCollationKey i = (AnyCollationKey)a;
			this.setValue(i.getValue());

    }
    return this;
  }
  
  public void negate()
  {
  	negate_ = true;
  }
}
