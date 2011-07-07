/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/GetBlob.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Gets the Any value from the specified Blob.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class GetBlob extends    AbstractFunc
                     implements Cloneable
{
	
	private Any blob_;
	
	public GetBlob(Any blob)
	{
		blob_ = blob;
	}
	
	public Any exec(Any a) throws AnyException
	{
		AnyBlob blob = (AnyBlob)EvalExpr.evalFunc(getTransaction(),
                                              a,
                                              blob_,
                                              AnyBlob.class);
                                       
    return (Any)blob.getValue();
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		GetBlob g = (GetBlob)super.clone();
		g.blob_ = blob_.cloneAny();
		return g;
  }
}
