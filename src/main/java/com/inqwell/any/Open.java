/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Open.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import com.inqwell.any.io.PhysicalIO;

/**
 * Open an I/O stream.
 * Opens the specified stream to the given target with the given mode.
 * The I/O stream is an implementation of <code>PhysicalIO</code> and
 * that implementation defines the format that will be generated/consumed,
 * for example native serialization, XML or flat files.
 * <p>
 * The actual
 * source/sink of the stream (what the stream is connected to) is defined
 * by the <code>toOpen</code> argument.  This is identified by a URL and
 * thus may be any device, host, protocol etc that can be expressed in
 * a URL that
 * the <code>&lt;inq&gt;</code><sup><font size=-2>TM</font></sup> environment
 * can understand.
 * <p>
 * The stream can be opened either for read or write - specify one of
 * <code>PhysicalIO.read__</code>, <code>PhysicalIO.write__</code>
 * or <code>PhysicalIO.append__</code>
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Open extends    AbstractFunc
									implements Cloneable
{
	private Any     io_;
	private Any     toOpen_;
	private Any     mode_;

	private AnyURL  baseURL_;

	public Open (Any io, Any toOpen, Any mode)
	{
		io_     = io;
		toOpen_ = toOpen;
		mode_   = mode;
	}

	public Open (Any io, Any toOpen)
	{
		this(io, toOpen, null);
	}

  public Any exec (Any a) throws AnyException
  {
		PhysicalIO io    = (PhysicalIO)EvalExpr.evalFunc(getTransaction(),
																				 a,
																				 io_,
																				 PhysicalIO.class);

		if (io == null)
		  nullOperand(io_);

		Any toOpen       = EvalExpr.evalFunc(getTransaction(),
																				 a,
																				 toOpen_);

		if (toOpen == null)
		  nullOperand(toOpen_);

		IntI mode        = (IntI)EvalExpr.evalFunc(getTransaction(),
																				 a,
																				 mode_,
																				 IntI.class);

		if (mode == null)
		  nullOperand(mode_);

		BooleanI ret = new AnyBoolean();

		ret.setValue(io.open(getTransaction().getProcess(),
                         toOpen,
                         mode));

		return ret;
  }

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(io_);
  	a.add(toOpen_);
  	a.add(mode_);
  	return a.createIterator();
  }

	public void setBaseURL(String url)
	{
		baseURL_ = new AnyURL(url);
	}

  public Object clone () throws CloneNotSupportedException
  {
    Open o = (Open)super.clone();

    o.io_     = io_.cloneAny();
    o.toOpen_ = toOpen_.cloneAny();
    o.mode_   = AbstractAny.cloneOrNull(mode_);

    return o;
  }
}
