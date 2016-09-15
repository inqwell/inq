/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/WriteStream.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import com.inqwell.any.io.PhysicalIO;

/**
 * Write to an I/O stream previously opened with <code>Open</code>
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */ 
public class WriteStream extends    AbstractFunc
												 implements Cloneable
{
	private Any     io_;
	private Any     toWrite_;
	
	private boolean isLn_;
	
	public WriteStream (Any io, Any toWrite)
	{
		io_      = io;
		toWrite_ = toWrite;
	}
	
  public Any exec (Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
		PhysicalIO io    = (PhysicalIO)EvalExpr.evalFunc(t,
																				 a,
																				 io_,
																				 PhysicalIO.class);
		if (io == null)
		  nullOperand(io_);
			
		Any   toWrite    = EvalExpr.evalFunc(t,
																				 a,
																				 toWrite_);
//    if (toWrite == null)
//      nullOperand(toWrite_);
      
		
		BooleanI ret = new AnyBoolean();
    
    if (isLn_)
      ret.setValue(io.writeln(toWrite, t));
    else
      ret.setValue(io.write(toWrite, t));

    /*        
		if (toWrite instanceof Vectored)
		{
      Vectored v = (Vectored)toWrite;
			for (int i = 0; i < v.entries(); i++)
			{
        Any item = v.getByVector(i);
        if (isLn_)
          ret.setValue(io.writeln(item));
        else
          ret.setValue(io.write(item));
			}
		}
    else
    {
      if (isLn_)
        ret.setValue(io.writeln(toWrite));
      else
        ret.setValue(io.write(toWrite));
    }
		*/
    
		return ret;
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(io_);
  	a.add(toWrite_);
  	return a.createIterator();
  }
  
  public void setLn(boolean isLn)
  {
  	isLn_ = isLn;
  }
  
  public boolean isLn()
  {
  	return isLn_;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    WriteStream w = (WriteStream)super.clone();
    
    w.io_      = io_.cloneAny();
    w.toWrite_ = toWrite_.cloneAny();
    
    return w;
  }
}
