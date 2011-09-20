/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/server/ServerListener.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.server;

import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.inqwell.any.AbstractProcess;
import com.inqwell.any.AddTo;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyTimer;
import com.inqwell.any.ConstDate;
import com.inqwell.any.ConstInt;
import com.inqwell.any.ConstString;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.PermissionException;
import com.inqwell.any.Process;
import com.inqwell.any.RemoveFrom;
import com.inqwell.any.StackUnderflowException;
import com.inqwell.any.Transaction;

/**
 * Start a new thread
 * and wait at a specified server socket port.  Execute the given
 * function 
 */
public class ServerListener extends    AbstractProcess
										        implements Process,
										                   Runnable
{
	public static final Any PORT = new ConstString("port");
	
	private static Logger logger = Logger.getLogger("inq");
	
	private Thread           thread_;
	
	private int              port_;
	
	private boolean          killed_ = false;
	
	private ProtocolHandler  ph_;
	
	private Any              catalogPath_;
	
	public ServerListener (int              port,
												 ProtocolHandler  ph)
	{
		port_     = port;
		ph_       = ph;
		init(ph_.getProtocolName());
	}
	
	public void run()
	{
		Process p = this;
		ServerSocket ss = null;
		try
		{
      ss = ph_.getServerSocket(port_);
		}
		catch (Exception sse)
		{
      logger.log(Level.SEVERE, "Socket Listener cannot get server socket - terminating......", sse);
			return;
		}
			
    logger.log(Level.INFO, "Socket Listener started on port " + port_);

		initInThread();
		
	  while (!killed_)
	  {
  	  // Wait at requested port
  	  java.net.Socket s = null;
  	  
  	  try
  	  {
  	    s = ss.accept();
				System.out.println (ph_.getProtocolName() +
				                    " Socket Listener accepted on socket " +
				                    s);
  	    
  	    try
  	    {
          // Do any specific protocol handling required by this listener
          ph_.handleConnect(s);
        }
        catch (AnyException ae)
        {
					try
					{
						s.close();
					}
					catch (Exception e)
					{
					  logger.log(Level.SEVERE, "Cannot handle new connection", e);
					}
          logger.log(Level.SEVERE, "Cannot handle new connection (close)", ae);
				}
        catch (AnyRuntimeException ae)
        {
          try
          {
            s.close();
          }
          catch (Exception e)
          {
            logger.log(Level.SEVERE, "Cannot handle new connection", e);
          }
          logger.log(Level.SEVERE, "Cannot handle new connection (close)", ae);
        }
			}
			
			// Handle uncaught JDK exceptions
			catch (Exception e) // ss.accept()
			{
        logger.log(Level.SEVERE, "accept()", e);
				try
				{
          if (s != null)
            s.close();
				}
				catch (Exception e1)
				{
	        logger.log(Level.SEVERE, "close()", e);
				}
				break;
			}
  	}

    logger.log(Level.INFO, "Socket Listener started on port " + port_ + " terminated");

    RemoveFrom removeFrom = new RemoveFrom(this);
    removeFrom.setTransaction(getTransaction());
    try
    {
      removeFrom.exec(this);
    }
    catch(AnyException e)
    {
    	e.printStackTrace();
    }

//		try
//		{
//		  ss.close();
//		}
//		catch (Exception e)
//		{
//			eh_.handleException (new ContainedException(e), getTransaction());
//		}
//  	System.out.println ("Socket Listener terminating......");
	}

	/**
	 * Send data to this process.  No-operation
	 */
	public void send (Any a) throws AnyException
	{
	}
	
	public void setTransaction(Transaction t)
	{
	}
	
	public Transaction getTransaction()
	{
		return Transaction.NULL_TRANSACTION;
	}
	
	/**
	 * No-operation
	 */
	public void deadlockVictim()
	{
	}
	
	/**
	 * Called by another process <code>p</code> (say, the LockManager) to bump this
	 * process out of a <code>wait()</code> in which it is deadlocked
	 * with one or more competing processes.
	 */
	public void kill(Process p) throws AnyException
	{
		if (!p.isSupervisor())
			throw new PermissionException("Can't kill if not supervisor");

		killed_ = true;
		thread_.interrupt();
	}
	
  public void interrupt()
  {
    throw new UnsupportedOperationException();
  }

	public boolean isSupervisor ()
	{
		return false;
	}
	
  public boolean isAlive()
  {
    if (thread_ == null)
      return false;
    
    return thread_.isAlive();
  }
  
	public void setSupervisor (boolean b) {}
	
	public Map getRoot()
	{
		return null;
	}
	
	public AnyTimer getTimer()
	{
    throw new UnsupportedOperationException("getTimer()");
	}
	
	public boolean isAncestor(Process p)
	{
		return false;
	}
	
	public void addChildProcess(Process p, Transaction t)
	{
    throw new UnsupportedOperationException("addChildProcess(p)");
	}
	
	public void removeChildProcess(Process p, Transaction t)
	{
    throw new UnsupportedOperationException("removeChildProcess(p)");
	}
	
	public Map getContext()
	{
		return null;
	}
	
	public Any getContextPath()
	{
		return null;
	}
	
	public Any getCatalogPath()
	{
		return catalogPath_;
	}
	
	public void setContext(Map context)
	{
	}
	
	public void setContextPath(Any contextPath)
	{
	}
	
	public Map getCurrentStackFrame() throws StackUnderflowException
	{
    throw new UnsupportedOperationException("getCurrentStackFrame()");
	}

	public Map pushStackFrame()
	{
    throw new UnsupportedOperationException("pushStackFrame()");
	}
	
	public Map popStackFrame() throws StackUnderflowException
	{
    throw new UnsupportedOperationException("popStackFrame()");
	}
	
	public void emptyStack()
	{
    throw new UnsupportedOperationException("emptyStack()");
	}
	
  public void startThread()
  {
    if (thread_ == null)
    {
      // off we go
      thread_ = new Thread(this);
      if (this.contains(Process.processName__))
        thread_.setName(this.get(Process.processName__).toString());
      thread_.start();
    }
  }
  
	protected void init(String protocolName) //throws AnyException
	{
		killed_ = false;
    this.add(Process.processName__, new ConstString(protocolName));
    startThread();
	}	

 	private void initInThread()
 	{
    try
    {
      this.add(Process.STARTED, new ConstDate());
      this.add(PORT, new ConstInt(port_));
      catalogPath_ = new ConstString(NodeSpecification.catalog__ +
                                   ".processes.ServerListener" +
                                   port_);
      NodeSpecification n = new NodeSpecification(catalogPath_.toString());

      AddTo addTo = new AddTo(this, n);
      addTo.exec(null);
    }
    catch (Exception e)
    {
      // bit weak but inlikely to happen!
      e.printStackTrace();
    }
 	}
}
