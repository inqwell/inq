/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ReadStream.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-05-02 20:04:58 $
 */
package com.inqwell.any;

import com.inqwell.any.io.PhysicalIO;

/**
 * Read from an I/O stream previously opened with <code>Open</code>
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */ 
public class ReadStream extends    AbstractFunc
												implements Cloneable
{
	Any    io_;
	
	public ReadStream (Any io)
	{
		io_     = io;
	}
	
  public Any exec (Any a) throws AnyException
  {
		PhysicalIO io    = (PhysicalIO)EvalExpr.evalFunc(getTransaction(),
																				 a,
																				 io_,
																				 PhysicalIO.class);
		
		if (io == null)
		  nullOperand(io_);
		
		// Flush any pending o/p for pipes/sockets
		io.flush();
		
		Any ret = io.read();
		return ret;
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(io_);
  	return a.createIterator();
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    ReadStream r = (ReadStream)super.clone();
    
    r.io_     = io_.cloneAny();
    
    return r;
  }
}
