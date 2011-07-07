/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/print/PrintConstants.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:23 $
 */

package com.inqwell.any.print;

import com.inqwell.any.*;
import javax.print.attribute.standard.*;
/**
 */
public interface PrintConstants
{
  public final static Any PRINTER_NAME = new ConstString("printerName");
  public final static Any ACCEPTING    = new ConstString("acceptingJobs");
  public final static Any JOB_COUNT    = new ConstString("jobCount");
  
  public final static Any COPIES       = new ConstString("@copies");
  
  public final static Any ORIENTATION       = new ConstString("@orientation");
  public final static Any PORTRAIT          = new AnyObject(OrientationRequested.PORTRAIT);
  public final static Any LANDSCAPE         = new AnyObject(OrientationRequested.LANDSCAPE);
  public final static Any REVERSE_PORTRAIT  = new AnyObject(OrientationRequested.REVERSE_PORTRAIT);
  public final static Any REVERSE_LANDSCAPE = new AnyObject(OrientationRequested.REVERSE_LANDSCAPE);
  
  public final static Any MEDIA_SIZE          = new ConstString("@mediaSize");
  public final static Any MEDIA_PRINTABLEAREA = new ConstString("@mediaPrintableArea");

  public final static Any PRINT_FILE   = new ConstString("printFile");
}
