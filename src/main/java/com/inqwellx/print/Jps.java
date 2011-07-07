/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwellx/print/Jps.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:23 $
 * @version $Revision: 1.2 $
 */

package com.inqwellx.print;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.Attribute;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.IntegerSyntax;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.Size2DSyntax;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import javax.print.attribute.standard.PrinterName;
import javax.print.attribute.standard.QueuedJobCount;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.FloatI;
import com.inqwell.any.Map;
import com.inqwell.any.print.AnyEnumSyntax;
import com.inqwell.any.print.AnyPrintAttribute;
import com.inqwell.any.print.PrintConstants;

/**
 * Routines related to the Inq interface to the Java print service
 */
public class Jps implements PrintConstants
{
  static Any printer__ = new AnyString("printer");

  /**
   * Return a well-behaved Inq structure describing this platform's
   * available printers
   */
  public static Map getPrinterList()
  {
    Map ret = AbstractComposite.managedMap();
    
    PrintRequestAttributeSet aset   = null; //new HashPrintRequestAttributeSet();
    DocFlavor                flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;

    PrintService[] service =
       PrintServiceLookup.lookupPrintServices(flavor, aset);
       
    for (int i = 0; i < service.length; i++)
    {
      PrintService ps = service[i];
      
      Map printer = AbstractComposite.managedMap();
      Map attribs = AbstractComposite.managedMap();
      
      Any name = new AnyString(ps.getName());
      
      attribs.add(PRINTER_NAME, name);
      convertPrinterAttributes(ps, attribs);
      printer.add(printer__, attribs);
      ret.add(name, printer);
    }
    ret.setNodeSet(printer__);
    return ret;
  }
  
  /**
   * Return the default print service or null if none found.
   * @return A map describing the default print service or null if
   * none is determined by jps.
   */
  public static Map getDefaultPrinter()
  {
    Map ret = null;
    
    PrintService ps = PrintServiceLookup.lookupDefaultPrintService();
    if (ps == null)
      return ret;
    
    ret = AbstractComposite.managedMap();
    Any name = new AnyString(ps.getName());

    ret.add(PRINTER_NAME, name);
    convertPrinterAttributes(ps, ret);
    
    return ret;
  }
  
  /**
   * Process (some of) the PrintService attributes into an Inq
   * structure
   */
  public static void convertPrinterAttributes(PrintService ps, Map attribs)
  {
    Attribute[] pAttrs = ps.getAttributes().toArray();
    for (int i = 0; i < pAttrs.length; i++)
    {
      Attribute pAttr = pAttrs[i];
      
      Class category = pAttr.getCategory();
      if (category == PrinterName.class)
        continue; // name already done
        
      if (category == PrinterIsAcceptingJobs.class)
      {
        AnyBoolean b = new AnyBoolean(((EnumSyntax)pAttr).getValue());
        attribs.add(ACCEPTING, b);
        continue;
      }

      if (category == QueuedJobCount.class)
      {
        AnyInt jc = new AnyInt(((IntegerSyntax)pAttr).getValue());
        attribs.add(JOB_COUNT, jc);
        continue;
      }
    }
  }
  
  /**
   * Determine the printable area given the media size name and the
   * margins.
   * @param mediaSize an AnyEnumSyntax object containing the media size
   * name
   * @param an array containing AnyFloat values for the margins in the
   * order left, right, top, bottom in units of millimetres
   */
  public static Any getPrintableArea(Any mediaSize, Any margins)
  {
    AnyEnumSyntax a = (AnyEnumSyntax)mediaSize;
    Array         m = (Array)margins;
    
    MediaSizeName ms   = (MediaSizeName)a.getEnumSyntax();
    MediaSize     size = MediaSize.getMediaSizeForName(ms);
    
    float width  = size.getX(Size2DSyntax.MM);
    float height = size.getY(Size2DSyntax.MM);
    
    FloatI al = (FloatI)m.get(0);
    FloatI ar = (FloatI)m.get(1);
    FloatI at = (FloatI)m.get(2);
    FloatI ab = (FloatI)m.get(3);
    
    MediaPrintableArea mpa = new MediaPrintableArea(al.getValue(),
                                                    at.getValue(),
                                                    width  - al.getValue() - ar.getValue(),
                                                    height - at.getValue() - ab.getValue(),
                                                    MediaPrintableArea.MM);
    
    return new AnyPrintAttribute(mpa);
  }
}
