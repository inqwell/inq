/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/CreateNewFile.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 * @version $Revision: 1.3 $
 */

package com.inqwell.any;

import java.io.File;

/**
 * Atomically creates a new, empty file named by the specified file if and
 * only if a file with this name does not yet exist.
 * <p>
 * Returns <code>true</code> if the file was created, <code>false> otherwise.
 */
public class  CreateNewFile extends    AbstractFunc
                            implements Cloneable
{
	
  private Any     file_;

	/**
	 * 
	 */
  public CreateNewFile(Any file)
  {
    file_ = file;
  }

  public Any exec(Any a) throws AnyException
  {
		AnyFile file  = AnyFile.toFile(EvalExpr.evalFunc(getTransaction(),
                                               a,
                                               file_));
		
    if (file == null)
      nullOperand(file_);
    
    return file.createNewFile();
  }

  public Iter createIterator()
  {
  	Array a = AbstractComposite.array();
  	a.add(file_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    CreateNewFile md = (CreateNewFile)super.clone();
    
    md.file_     = file_.cloneAny();
    
    return md;
  }
}
