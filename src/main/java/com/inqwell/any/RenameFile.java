/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/RenameFile.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 * @version $Revision: 1.2 $
 */

package com.inqwell.any;

import java.io.File;

/**
 * Make directory(s) specified by a <code>file</code> variable.
 * <p>
 * Returns <code>true</code>.
 */
public class  RenameFile extends    AbstractFunc
                            implements Cloneable
{
	
  private Any     file_;
  private Any     toFile_;

	/**
	 * 
	 */
  public RenameFile(Any file, Any toFile)
  {
    file_   = file;
    toFile_ = toFile;
  }

  public Any exec(Any a) throws AnyException
  {
		AnyFile file  = AnyFile.toFile(EvalExpr.evalFunc(getTransaction(),
                                               a,
                                               file_));
		
    if (file == null)
      nullOperand(file_);
    
		AnyFile toFile  = AnyFile.toFile(EvalExpr.evalFunc(getTransaction(),
                                               a,
                                               toFile_));
		
    if (toFile == null)
      nullOperand(toFile_);
    
    File f = file.getFile();
    
    boolean ret = f.renameTo(toFile.getFile());
    
		return new AnyBoolean(ret);
  }

  public Iter createIterator()
  {
  	Array a = AbstractComposite.array();
  	a.add(file_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    RenameFile md = (RenameFile)super.clone();
    
    md.file_     = file_.cloneAny();
    
    return md;
  }
}
