/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/print/PrintUtil.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:23 $
 */
 
package com.inqwell.any.print;

import java.awt.print.Printable;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyObject;
import com.inqwell.any.AnyPrintable;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.ConstInt;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.Map;
import com.inqwell.any.ObjectI;
import com.inqwell.any.RuntimeContainedException;

/**
 * A set of static routines relating to printing.
 */
public class PrintUtil implements PrintConstants
{
  static Map attrFuncs__ = AbstractComposite.map();
  
  static
  {
    attrFuncs__.add(COPIES,               new NumCopies());
    attrFuncs__.add(ORIENTATION,          new Orientation());
    attrFuncs__.add(MEDIA_SIZE,           new MediaSize());
    attrFuncs__.add(MEDIA_PRINTABLEAREA,  new MediaPrintableArea());
  }
  
  public static void print(AnyPrintable printable, Map jobAttrs) throws AnyException
  {
    Printable p = printable.getPrintable();
    if (p == null)
      throw new AnyRuntimeException("No printable object");

    DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;

    // convert the jobAttrs
    PrintRequestAttributeSet aset = null;
    if (jobAttrs != null)
    {
      Iter i = jobAttrs.createKeysIterator();
      synchronized(attrFuncs__) // for thread safety as everything static
      {
        JobAttrs lastFunc = null;
        while (i.hasNext())
        {
          Any k = i.next();
          if (attrFuncs__.contains(k))
          {
            JobAttrs f = (JobAttrs)attrFuncs__.get(k);
            Any  v = jobAttrs.get(k);
            f.execFunc(v);
            lastFunc = f;
          }
        }
        if (lastFunc != null)
          aset = lastFunc.getAttributeSet();
      }
    }

    System.out.println("ASET : " + aset);
    
    Any printerName = null;
    if (jobAttrs != null && jobAttrs.contains(PRINTER_NAME))
      printerName = jobAttrs.get(PRINTER_NAME);
      
//    if (aset == null)
//    {
//      aset = new HashPrintRequestAttributeSet();
//    }
//    aset.add(MediaSizeName.ISO_A4);
    PrintService ps = null;

    // If we are given a printer name then look up the services
    // that are available and that satisfy the request attributes
    // (currently none).
    if (printerName != null)
    {
      PrintService[] service =
         PrintServiceLookup.lookupPrintServices(flavor, null); //aset);
      for (int i = 0; i < service.length; i++)
      {
        if (printerName.toString().equals(service[i].getName()) ||
            printerName.toString().endsWith(service[i].getName()))
        {
          ps = service[i];
          break;
        }
      }
      if (ps == null)
        throw new AnyRuntimeException
          ("Printer " + printerName +
           " does not exist or does not support the specified attributes");
    }
    else
    {
      ps = PrintServiceLookup.lookupDefaultPrintService();
      if (ps == null)
        throw new AnyRuntimeException
          ("Unable to locate default print service");
    }

    // By now we have a print service.  Set up the remainder
    // of the print job.
    final DocPrintJob pj = ps.createPrintJob();
    
    Doc doc = new SimpleDoc(p, flavor, null);
    
    try
    {
      pj.print(doc, aset);
    }
    catch(PrintException pex)
    {
      throw new RuntimeContainedException(pex);
    }
  }
  
  // Conversion of print job attributes from Inq to jps
  static abstract class JobAttrs extends AbstractFunc
  {
    static protected PrintRequestAttributeSet attrSet_;
    
    PrintRequestAttributeSet getAttributeSet()
    {
      PrintRequestAttributeSet ret = attrSet_;
      attrSet_ = null;
      return ret;
    }
    
    protected void ensureAttributes()
    {
      if (attrSet_ == null)
        attrSet_ = new HashPrintRequestAttributeSet();
    }
  }
  
  static class NumCopies extends JobAttrs
  {
    public Any exec(Any a) throws AnyException
    {
      ensureAttributes();
      
      IntI iCopies = new ConstInt(a);
      Copies jCopies = new Copies(iCopies.getValue());
      attrSet_.add(jCopies);

      return a;
    }
  }

  static class Orientation extends JobAttrs
  {
    public Any exec(Any a) throws AnyException
    {
      ensureAttributes();
      
      ObjectI orientation = (ObjectI)a;
      attrSet_.add((OrientationRequested)orientation.getValue());

      return a;
    }
  }

  static class MediaSize extends JobAttrs
  {
    public Any exec(Any a) throws AnyException
    {
      ensureAttributes();
      
      ObjectI mediaSize = (ObjectI)a;
      System.out.println("MediaSize added " + mediaSize.getValue());
      attrSet_.add((MediaSizeName)mediaSize.getValue());

      return a;
    }
  }
  
  static class MediaPrintableArea extends JobAttrs
  {
    public Any exec(Any a) throws AnyException
    {
      ensureAttributes();
      
      AnyPrintAttribute mediaPrintableArea = (AnyPrintAttribute)a;
      System.out.println("MediaPrintableArea added " + mediaPrintableArea.getAttribute());
      attrSet_.add(mediaPrintableArea.getAttribute());

      return a;
    }
  }
  
  // Add more attribute conversion functions here
}
