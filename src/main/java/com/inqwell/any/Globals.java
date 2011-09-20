/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Globals.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-12 13:53:58 $
 */

package com.inqwell.any;

import com.inqwell.any.Process;
import com.inqwell.any.client.UIDefaultsMap;
import com.inqwell.any.io.PrintStream;
import java.util.HashMap;

/**
 * Global variables set up by JVM startup.
 */
public class Globals
{      
	static public java.util.Map channelOutputReplacements__;
	static public java.util.Map channelInputReplacements__;

	static public java.util.Map streamOutputReplacements__;
	static public java.util.Map streamInputReplacements__;

	static public java.util.Map xmlStreamOutputReplacements__;
	static public java.util.Map xmlStreamInputReplacements__;

  // Only on-null in a server
	static public SessionList sessionList__ = null;
  
  static public LockManager lockManager__ = null;
	
	// Only non-null/relevant on a client
	static public Process      process__      = null;
	static public volatile boolean      inqActive__    = false;
	static public Interpreter  interpreter__  = null;
  static public volatile boolean awtSync__  = true;
  static public volatile AnyTimeZone serverTimeZone__ = null;
  static public boolean interactive__ = false;
  static public boolean awtActive__   = false;
  
  // A map of Java Thread objects to Inq Process objects.
  // Made available so that the process (and associated
  // transaction, context etc) can be retrieved for the
  // currently running thread.
  static private HashMap<Thread, Process> threadMap__ = new HashMap<Thread, Process>();
	
	static
	{
    Map s = AbstractComposite.map();
    s.add (new ConstString("out"), new PrintStream(System.out));
    s.add (new ConstString("err"), new PrintStream(System.err));
    s.add (new ConstString("in"),  new PrintStream(System.in));
    s.add (new ConstString("vm"),  new MemoryStats());
    Catalog.instance().getCatalog().add(new ConstString ("system"), s);
  }
  
  public static boolean isServer()
  {
    return process__ == null;
  }
  
  public static boolean isInteractive()
  {
    return interactive__;
  }
  
  public static Process getProcessForCurrentThread()
  {
    return getProcessForThread(Thread.currentThread(), true);
  }
  
  public static Process getProcessForCurrentThread(boolean mandatory)
  {
    return getProcessForThread(Thread.currentThread(), mandatory);
  }
  
  public static boolean haveProcessForThread()
  {
    return haveProcessForThread(Thread.currentThread());
  }
  
  public static Process getProcessForThread(Thread t)
  {
    return getProcessForThread(t, true);
  }
  
  public static Process getProcessForThread(Thread t, boolean mandatory)
  {
    synchronized(Globals.class)
    {
      Process p = null;

      if (((p = threadMap__.get(t)) == null) && mandatory)
      {
        throw new AnyRuntimeException("No process for thread!");
      }
      
      return p;
    }
  }
  
  public static boolean haveProcessForThread(Thread t)
  {
    synchronized(Globals.class)
    {
      if (!threadMap__.containsKey(t))
      {
        return false;
      }
      
      return true;
    }
  }
  
  public static Map getUIDefaults()
  {
    if (isServer())
      throw new UnsupportedOperationException("Not a client environment");
    
    return UIDefaultsMap.getUIDefaults();
  }
  
  public static void setProcessForThread(Thread t, Process p)
  {
    if (p == null)
      throw new IllegalArgumentException("Process cannot be null");
    
    synchronized(Globals.class)
    {
      if (threadMap__.containsKey(t))
        throw new AnyRuntimeException("Already have a process for " + t);
      
      threadMap__.put(t, p);
    }
  }
  
  public static void removeProcessForThread(Thread t, Process p)
  {
    synchronized(Globals.class)
    {
      if (!threadMap__.containsKey(t))
        throw new AnyRuntimeException("No process for thread!");
      
      threadMap__.remove(t);
    }
  }
}

