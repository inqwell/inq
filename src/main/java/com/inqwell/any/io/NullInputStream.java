/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/NullInputStream.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream that always returns eof
 * 
 * @author Tom
 *
 */
public class NullInputStream extends InputStream
{

  public int read() throws IOException
  {
    return -1;
  }

}
