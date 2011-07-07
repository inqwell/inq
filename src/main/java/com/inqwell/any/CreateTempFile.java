/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/CreateTempFile.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 * @version $Revision: 1.2 $
 */

package com.inqwell.any;


/**
 * Creates a new empty file using the given
 * prefix and suffix strings to generate its name. If the optional directory
 * parameter is specified the file will be created in this directory.
 * <p/>
 * The prefix must be at least three characters long. The suffix may be value
 * null, in which case <code>.tmp</code> is assumed.
 * <p/>
 * Returns the file that was created.
 */
public class  CreateTempFile extends    AbstractFunc
                             implements Cloneable
{
  private Any     prefix_; 
  private Any     suffix_; 
  private Any     dir_;
  

  /**
   * 
   */
  public CreateTempFile(Any prefix, Any suffix, Any dir)
  {
    prefix_ = prefix;
    suffix_ = suffix;
    dir_    = dir;
  }

  public Any exec(Any a) throws AnyException
  {
    Any prefix  = EvalExpr.evalFunc(getTransaction(),
                                    a,
                                    prefix_);

    if (prefix == null && prefix_ != null)
      nullOperand(prefix_);
    
    Any suffix  = EvalExpr.evalFunc(getTransaction(),
                                    a,
                                    suffix_);

    if (suffix == null && suffix_ != null)
      nullOperand(suffix_);
    if (AnyNull.isNullInstance(suffix))
      suffix = null;
    
    Any dir  = EvalExpr.evalFunc(getTransaction(),
                                 a,
                                 dir_);

    if (dir == null && dir_ != null)
      nullOperand(dir_);
    if (AnyNull.isNullInstance(dir))
      dir = null;
    
    AnyFile fDir = AnyFile.toFile(dir);
    
    return AnyFile.createTempFile(prefix, suffix, fDir);
  }

  public Object clone () throws CloneNotSupportedException
  {
    CreateTempFile ct = (CreateTempFile)super.clone();
    
    ct.prefix_  = prefix_.cloneAny();
    ct.suffix_  = AbstractAny.cloneOrNull(suffix_);
    ct.dir_     = AbstractAny.cloneOrNull(dir_);
    
    return ct;
  }
}
