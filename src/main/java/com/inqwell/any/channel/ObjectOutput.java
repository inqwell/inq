/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/channel/ObjectOutput.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import java.io.IOException;

/**
 * Just extends the standard JDK <code>ObjectOutput</code> with
 * the <code>reset()</code> method which, considering its
 * usefulness and simarility to <code>flush()</code>, seems to
 * have been forgotten.
 * <p>
 * Also, by defining our own interface we can define concrete
 * implementations which delegate to the
 * JDK <code>ObjectOutputStream</code> class while putting some
 * of our own functionality on top.  It is easier and more extensible
 * to do this than just deriving from <code>ObjectOutputStream</code>.
 */
public interface ObjectOutput extends java.io.ObjectOutput
{
	public void reset() throws IOException;
}
