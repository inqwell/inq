/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/InqFileFilter.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import com.inqwell.any.client.AnyComponent;
import java.io.File;
//import java.io.FileFilter;
//import java.io.FilenameFilter;


/**
 * Wrap up the JDK <code>java.io.FileFilter</code>
 * and <code>java.io.FilenameFilter</code> interfaces and
 * the <code>javax.swing.filechooser.FileFilter</code> class.
 * <p>
 * 
 */
public class InqFileFilter extends    javax.swing.filechooser.FileFilter
													 implements java.io.FileFilter,
																			java.io.FilenameFilter,
																			Cloneable
{
	// Properties
	private boolean    acceptDirs_ = true;
	private Any        descr_;
	
	// One or the other of these
	private AnyMatcher matcher_;
	private AnyFuncHolder.FuncHolder filterExpr_;
	
	// Working variables
	private AnyBoolean b_;
	private AnyFile    file_;
  private Map        fnArgs_;
  private AnyString  s_;
  
  static public Any  allFiles__ = new ConstString("All Files");
  
  static public Any fileArg__ = AbstractValue.flyweightString("selection");
	
	public InqFileFilter()
	{
		descr_      = allFiles__;
		acceptDirs_ = true;
	}
	
	public boolean accept(File f)
	{
		if (f.isDirectory())
			return acceptDirs_;
		
		boolean ret = true;
		
		if (filterExpr_ != null)
      ret = callAcceptFunc(filterExpr_, f);
		else if (matcher_ != null)
		{
			if (s_ == null)
				s_ = new AnyString(f.getName());
			else
				s_.setValue(f.getName());
			
			matcher_.setSequence(s_);
			ret = matcher_.find();
		}
		
		return ret;
	}

  public void setAccept(AnyFuncHolder.FuncHolder acceptF)
  {
    if (acceptF != null && acceptF.getContextPath() == null)
    {
      throw new AnyRuntimeException("func required, cfunc provided");
    }
    
    filterExpr_ = acceptF;
  }

  public void setMatcher(AnyMatcher matcher)
  {
  	matcher_ = matcher;
  }

	public String getDescription()
	{
		return descr_.toString();
	}
	
	public void setDescriptionString(Any descr)
	{
		descr_ = descr;
	}
	
	public Any getDescriptionString()
	{
		return descr_;
	}

	public void setAcceptDirs(boolean acceptDirs)
	{
		acceptDirs_ = acceptDirs;
	}
	
	public boolean getAcceptDirs()
	{
		return acceptDirs_;
	}

  public boolean accept(File   dir,
												String name)
	{
		File f = new File(dir, name);
		return accept(f);
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		InqFileFilter a = (InqFileFilter)super.clone();
		
		a.descr_        = AbstractAny.cloneOrNull(descr_);
		//a.filterExpr_   = (Call)AbstractAny.cloneOrNull(filterExpr_);
		a.matcher_      = (AnyMatcher)AbstractAny.cloneOrNull(matcher_);
		
		a.b_            = null;
		a.fnArgs_       = null;
		a.file_         = null;
		a.s_            = null;
    
		return a;
	}
	  
  private boolean callAcceptFunc(AnyFuncHolder.FuncHolder f, File file)
  {
    Any     ret = null;
    
    Transaction t = Globals.getProcessForThread(Thread.currentThread()).getTransaction();

    try
    {
    	if (fnArgs_ == null)
    	{
        fnArgs_ = AbstractComposite.simpleMap();
    		file_   = new AnyFile();
    		b_      = new AnyBoolean(true);
    	}
    	
    	b_.setValue(true);
    	
    	file_.setFile(file);

      fnArgs_.add(fileArg__, file_);
      ret = f.doFunc(t, fnArgs_, null);  // must be a func so has own context
//      f.setArgs(fnArgs_);
//      f.setTransaction(t);
//      ret = f.exec(t.getContext());
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
    finally
    {
      //f.setArgs(null);
      fnArgs_.remove(fileArg__);
      file_.setFile(null);
      //f.setTransaction(null);
    }

    b_.copyFrom(ret);
      
    return b_.getValue();
  }
}
