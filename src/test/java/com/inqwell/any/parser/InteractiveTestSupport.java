/**
 * Copyright (C) 2012 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.parser;

import org.junit.After;

import com.inqwell.any.Any;
import com.inqwell.any.AnyURL;
import com.inqwell.any.ExecInq;
import com.inqwell.any.Globals;
import com.inqwell.any.Process;
import com.inqwell.any.ServerConstants;
import com.inqwell.any.client.swing.SwingInvoker;
import com.inqwell.any.util.CommandArgs;

/**
 * Test support for interactive scripts
 * @author tom
 *
 */
public class InteractiveTestSupport
{
	private Process p_;
	private Inq     parser_;
  CommandArgs     cArgs_;
	
  protected void setUp(String args[])
  {
    Inq.initInteractive();
    cArgs_ = new CommandArgs(args);
    Inq.initCommandArgs(cArgs_);
    p_ = Inq.initProcess();
    Globals.setProcessForThread(Thread.currentThread(), p_);
    parser_ = Inq.initParser(cArgs_, args);
  }
  
  protected Any run(String args[])
  {
  	setUp(args);
    synchronized(Globals.process__)
    {
      Globals.inqActive__ = true;
      SwingInvoker.initSwing();
      Any ret = Inq.parseLoop(parser_, cArgs_);
      Globals.inqActive__ = false;
      return ret;
    }
  }
  
  @After
  public void tearDown()
  {
    Globals.inqActive__ = false;
    Globals.removeProcessForThread(Thread.currentThread(), p_);
  }
  
//  private static void loadTestSupport()
//  {
//  	assert(Globals.process__ != null);
//  	Process p = Globals.process__;
//  	
//		p.setContext(getRoot());
//		p.setContextPath(ServerConstants.NSROOT);
//    ExecInq execInq = new ExecInq(new AnyURL("cp://inq.test.assert.inq"));
//    execInq.setTransaction(getTransaction());
//
//    execInq.exec(getRoot());
//
//  }
}
