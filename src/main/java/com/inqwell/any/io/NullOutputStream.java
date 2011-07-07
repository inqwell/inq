/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/NullOutputStream.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that junks all write operations. You can call it
 * the "byte bucket"
 * 
 * @author Tom
 *
 */
public class NullOutputStream extends OutputStream
{

  public void write(int b) throws IOException
  {
    // Does nothing
  }

}
