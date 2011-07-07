/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 * @version $Revision: 1.3 $
 */

package com.inqwell.any;

import java.io.File;

/**
 * Make directory(s) specified by a <code>file</code> variable.
 * <p>
 * Returns <code>true</code>.
 */
public class  MakeDirs extends    AbstractFunc
                       implements Cloneable
{
	
  private Any     file_;
  private boolean dirs_;

	/**
	 * 
	 */
  public MakeDirs(Any file, boolean dirs)
  {
    file_ = file;
    dirs_ = dirs;
  }

  public Any exec(Any a) throws AnyException
  {
		AnyFile file  = AnyFile.toFile(EvalExpr.evalFunc(getTransaction(),
                                               a,
                                               file_));
		
    if (file == null)
      nullOperand(file_);
    
    //File f = file.getFile();
    
    Any ret;
    
    if (dirs_)
      ret = file.mkdirs();
    else
      ret = file.mkdir();
      
		return ret;
  }

  public Iter createIterator()
  {
  	Array a = AbstractComposite.array();
  	a.add(file_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    MakeDirs md = (MakeDirs)super.clone();
    
    md.file_     = file_.cloneAny();
    
    return md;
  }
}
