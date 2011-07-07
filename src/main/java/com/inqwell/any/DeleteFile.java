/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/DeleteFile.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Delete the specified file.  Operand may be an AnyFile or a string that
 * will be converted to an AnyFile.  Returns boolean true if the file was
 * successfully deleted.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class DeleteFile extends    AbstractFunc
                        implements Cloneable
{
	private Any toDeleteFile_;

	public DeleteFile(Any toDeleteFile)
	{
		toDeleteFile_ = toDeleteFile;
	}

	public Any exec(Any a) throws AnyException
	{
		AnyFile toDeleteFile = AnyFile.toFile(EvalExpr.evalFunc(getTransaction(),
                                                        a,
                                                        toDeleteFile_));
		if (toDeleteFile == null)
		  nullOperand(toDeleteFile_);

		return toDeleteFile.delete();
	}

  public Object clone () throws CloneNotSupportedException
  {
    DeleteFile c = (DeleteFile)super.clone();

    c.toDeleteFile_   = toDeleteFile_.cloneAny();

    return c;
  }

}
