/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import java.io.InputStream;

public class ConstBlobDecor extends    ConstObjectDecor
                            implements BlobI
{
  private static final long serialVersionUID = 1L;

  ConstBlobDecor(BlobI b)
  {
    super(b);
  }

  public void fillBlob(InputStream is, long length)
  {
    constViolation(reason__);
  }
  
  public InputStream sinkBlobStream()
  {
    BlobI b = (BlobI)delegate_;
    return b.sinkBlobStream();
  }
  
  public String toString()
  {
    return delegate_.toString();
  }
}
