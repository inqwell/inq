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
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import java.awt.Desktop;

import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFile;
import com.inqwell.any.ContainedException;
import com.inqwell.any.EvalExpr;

/**
 * Print a file using the Desktop print facility
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class SysPrint extends    AbstractFunc
                      implements Cloneable
{
  private Any file_;
  
  public SysPrint(Any file)
  {
    file_ = file;
  }
  
  public Any exec(Any a) throws AnyException
  {
    Any f = EvalExpr.evalFunc(getTransaction(),
                              a,
                              file_);

    if (f == null)
      nullOperand(file_);
    
    AnyFile file;
    if (f instanceof AnyFile)
      file = (AnyFile)f;
    else
      file = new AnyFile(f);
    
    try
    {
      Desktop d = Desktop.getDesktop();
      d.print(file.getFile());
    }
    catch(Exception e)
    {
      throw new ContainedException(e);
    }
    
    return file;
  }

  public Object clone() throws CloneNotSupportedException
  {
    SysPrint s = (SysPrint)super.clone();
    
    s.file_   = file_.cloneAny();
    
    return s;
  }
}
