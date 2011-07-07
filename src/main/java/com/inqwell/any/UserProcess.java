/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/UserProcess.java $
 * $Author: sanderst $
 * $Revision: 1.12 $
 * $Date: 2011-04-09 18:17:05 $
 */

package com.inqwell.any;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;

import javax.net.ssl.SSLHandshakeException;

import com.inqwell.any.channel.AnyChannel;
import com.inqwell.any.channel.ChannelClosedException;
import com.inqwell.any.channel.ChannelConstants;
import com.inqwell.any.channel.FIFO;
import com.inqwell.any.channel.InputChannel;
import com.inqwell.any.channel.OutputChannel;
import com.inqwell.any.channel.Socket;
import com.inqwell.any.channel.WriteCopy;
import com.inqwell.any.client.FetchDescriptors;
import com.inqwell.any.client.InvokeEventProcessor;
import com.inqwell.any.client.LoadClient;
import com.inqwell.any.client.SessionDefunct;
import com.inqwell.any.client.SessionReconnect;
import com.inqwell.any.net.NetUtil;
import com.inqwell.any.tools.SessionID;

/**
 * Start  a new thread
 * and wait at an InputChannel
 */
public class UserProcess extends    BasicProcess
										     implements Process,
										                Runnable
{
  private static final long serialVersionUID = 1L;

  public  static Any loginName__   = AbstractValue.flyweightString("loginName");
  public  static Any package__     = AbstractValue.flyweightString("package");
  public  static Any address__     = AbstractValue.flyweightString("address");
	public  static Any passwd__      = AbstractValue.flyweightString("passwd");
	public  static Any url__         = AbstractValue.flyweightString("url");
	public  static Any host__        = AbstractValue.flyweightString("host");
	public  static Any cert__        = AbstractValue.flyweightString("cert");
  public  static Any exit__        = AbstractValue.flyweightString("exit");
  public  static Any privLevel__   = AbstractValue.flyweightString("privLevel");
  public  static Any ID            = AbstractValue.flyweightString("id");
	public  static Any expired__     = AbstractValue.flyweightString("expired");   // pwd expired
	public  static Any expiresIn__   = AbstractValue.flyweightString("expiresIn"); // pwd expiring
	public  static Any suspended__   = AbstractValue.flyweightString("suspended"); // acc suspended
	public  static Any denied__      = AbstractValue.flyweightString("denied");    // login denied (no special reason)
	public  static Any badpwd__      = AbstractValue.flyweightString("badpwd");    // login denied (pwd incorrect)
	public  static Any reason__      = AbstractValue.flyweightString("reason");    // login denied (pwd incorrect)
  public  static Any ignoreExpiring__  = AbstractValue.flyweightString("ignoreExpiring"); // ignore pwd expiring
  public  static Any keepAlivePeriod__ = AbstractValue.flyweightString("keepAlivePeriod"); // useful for internet sockets
  public  static Any lastFromClient__  = AbstractValue.flyweightString("lastFromClient");

  private static Any postLoginsvc__ = AbstractValue.flyweightString("system.client.services.PostLogin");

  public  static Any loginContext__ = AbstractValue.flyweightString("$root.login");
  private static Any loginWindow__  = AbstractValue.flyweightString("$root.login.login");

  public  static Array     loginOKEventTypes__;
  public  static Array     loginDeniedEventTypes__;
  public  static Array     serverLostEventTypes__;

  private static Array     loginDetailsEventTypes__;
  private static Array     loginRequestEventTypes__;
  private static Array     loginServiceEventTypes__;

	private Thread           thread_;
  
	private InputChannel     ic_;   // from remote
	private OutputChannel    lic_;  // from local
	private OutputChannel    oc_;   // to remote

	private boolean          isClient_; // these only set if we are a client
	private Any              initURL_;

	private EventDispatcher  connectedEd_;
	private EventDispatcher  disconnectedEd_;

	private EventDispatcher  ed_;      // current dispatcher for incoming
	                                   // events on the channel

	private EventDispatcher  rootEd_;  // for events emanating from getRoot()
																		 // (only set if we are in a server)

	private volatile boolean deadlockVictim_;
	private volatile boolean killed_;

	private ExceptionHandler eh_;

	private boolean          isSupervisor_;

	private Array            childProcesses_;
	private Process          parentProcess_;
  
	private Call             callOnEnd_;

	private Any              catalogPath_;

	private AnyTimer         timer_;

  private Any              waitingObject_;
  private long             waitingTimeout_;
  private Any              lockWaitObject_;
  private long             lockWaitTimeout_;
  
  private Map              propertyMap_;
  
  private Any              processName_;

  static
  {
    serverLostEventTypes__ = AbstractComposite.array();
    serverLostEventTypes__.add(EventConstants.SERVER_LOST);

    loginDetailsEventTypes__ = AbstractComposite.array();
    loginDetailsEventTypes__.add(EventConstants.LOGIN_DETAILS);

    loginOKEventTypes__ = AbstractComposite.array();
    loginOKEventTypes__.add(EventConstants.LOGIN_OK);

    loginDeniedEventTypes__ = AbstractComposite.array();
    loginDeniedEventTypes__.add(EventConstants.LOGIN_DENIED);

    loginRequestEventTypes__ = AbstractComposite.array();
    loginRequestEventTypes__.add(EventConstants.LOGIN_REQUEST);

    loginServiceEventTypes__ = AbstractComposite.array();
    loginServiceEventTypes__.add(EventConstants.INVOKE_LOGINSVC);
  }

	/**
   * Starts a process as a client process that will connect
   * to a server JVM.
   */
	public UserProcess(ExceptionHandler exceptionHandler,
                     Transaction      transaction,
                     Map              root,
                     EventDispatcher  connectedDispatcher,
                     EventDispatcher  disconnectedDispatcher,
                     URL              initInq) throws AnyException
	{
		setTransaction(transaction);
		isClient_       = true;
    exceptionHandler.setProcess(this);
		eh_             = exceptionHandler;
		setRoot(root);
		connectedEd_    = connectedDispatcher;
		disconnectedEd_ = disconnectedDispatcher;

		initClient(connectedDispatcher);
		initCommon();

	  eh_.setInputChannel(ic_);

		// initially we are disconnected so establish the disconnected
		// event dispatcher.
		doServerLost();

	  runInitInq(initInq);

		//startThread();
	}

	/**
	 * Starts a server-side process that is the peer of a
	 * connected client.
	 */
	public UserProcess(InputChannel     ic,
                     OutputChannel    oc,
                     ExceptionHandler exceptionHandler,
                     Transaction      transaction,
                     Map              root,
                     EventDispatcher  ed,
                     EventListener    rootListener) throws AnyException
	{
		setTransaction(transaction);
		setRoot(root);
    ed_ = ed;
		ic_ = ic;
		oc_ = oc;
    exceptionHandler.setProcess(this);
		eh_ = exceptionHandler;
		initServer(rootListener, ed);
		initCommon();
		//startThread();
	}

	/**
	 * Start a spawned process in the server.
	 */
	public UserProcess(Any                      name,
                     InputChannel             ic,
                     OutputChannel            oc,
                     ExceptionHandler         exceptionHandler,
                     Transaction              transaction,
                     Map                      root,
                     EventDispatcher          ed,
                     Process                  parent,
                     Call                     callOnEnd) throws AnyException
	{
		setTransaction(transaction);
		setRoot(root);
    ed_ = ed;
		ic_ = ic;
		oc_ = oc;
    exceptionHandler.setProcess(this);
		eh_ = exceptionHandler;
		parentProcess_  = parent;
		callOnEnd_      = callOnEnd;
		this.add(processName__, name.cloneAny());
	  eh_.setInputChannel(ic_);
    eh_.setServerConnected(true);  // there's no connection really.
		initCommon();
    //debugDecorate();
	}

	/**
	 * Client side (This is effectively defunct.  To be removed when other
	 * code allows.
	public UserProcess(InputChannel     ic,
                     OutputChannel    oc,
                     ExceptionHandler exceptionHandler,
                     Transaction      transaction,
                     Func             initialAction,
                     EventDispatcher  ed,
                     ChannelListener  channelListener,
                     EventListener    rootListener) throws AnyException
	{
		ic_ = ic;
		oc_ = oc;
    exceptionHandler.setProcess(this);
		eh_ = exceptionHandler;
		ed_ = ed;
		//initialAction_ = initialAction;
		setTransaction(transaction);
		//rootListener_  = rootListener;
		//init(channelListener);
	}
	 */

	/**
	 * Wrap a process around a given thread.  This constructor is only provided
	 * for the purposes of utility programs which aren't multi-threaded
	 * and who don't therefore have any transactional issues.  It allows quick and
	 * dirty programs using the Any framework to be built and is used
	 * only by the <code>NullTransaction</code> to supply a default process.
	 */
	public UserProcess (Thread t, Map root)
	{
		thread_ = t;
		setRoot(root);
		initMembers();
	}

	public void run()
	{
		// make sure our members are not garbage collected!
		Process p = this;

		//System.out.println ("User Process Started");
		boolean channelClosed = false;

		initInThread();

    Event e = null;
	  while (!channelClosed && !killed_)
	  {
  	  // Wait at our input channel and process input
  	  Any a = null;
  	  e     = null;
  	  try
  	  {

  	    a = ic_.read(); // wait, can therefore be interrupted

//				System.out.println ("Received: " + a);

				// Everything received through the input channel must be
				// an event.
        e = (Event)a;
			}

			catch (ChannelClosedException cce)
			{
				//System.out.println ("Client went away!");
				//cce.printStackTrace();
				channelClosed = true;
		  }

			catch (AnyException ae)  // Exception received through channel
			{
				//ae.printStackTrace();
        eh_.handleException (ae, getTransaction());
		  }

			catch (AnyRuntimeException ae)  // Exception received through channel
			{
				//ae.printStackTrace();
        eh_.handleException (ae, getTransaction());
		  }

		  if (!channelClosed && e != null)
		  {
        if (Globals.process__ == this && Globals.awtSync__)
        {
          synchronized(Globals.process__)
          {
            Globals.inqActive__ = true;
            dispatchEvent(e);
            Globals.inqActive__ = false;
          }
        }
        else
        {
          if (sync_ == null)
            dispatchEvent(e);
          else
          {
            synchronized(sync_)
            {
              Transaction t = getTransaction();
              if (t.isActive())
              {
                try
                {
                  sync_.wait();
                }
                catch(InterruptedException ie) {}
                
                dispatchEvent(e);
              }
              else
                dispatchEvent(e);
            }
          }
        }
      }
  	}

    if (sync_ == null)
      terminateProcess();
    else
    {
      synchronized(sync_)
      {
        Transaction t = getTransaction();
        if (t.isActive())
        {
          try
          {
            sync_.wait();
          }
          catch(InterruptedException ie) {}
          
          terminateProcess();
        }
        else
          terminateProcess();
      }
    }
    
    // Just to shut eclipse up
    if (p != null)
      p = null;
    
    threadDeath();
	}

  protected void threadDeath() {}

  private void dispatchEvent(Event event)
  {
    boolean abort = false;
    try
    {
      ed_.processEvent(event);
    }
    catch (AnyException e)
    {
      // Ordinary exceptions from the Any framework
      // (includes ContainedException)
      // Note that this path includes the InterruptedException
      // (wrapped in a ContainedException)
      //e.printStackTrace();
      e.fillInCallStack(getTransaction());
      getTransaction().getCallStack().empty();
      eh_.handleException (e, getTransaction());
      eh_.setHandlerProcess(null);
      abort = true;
    }

    catch (AnyRuntimeException e)
    {
      // Runtime exceptions from the Any framework
      //e.printStackTrace();
      e.fillInCallStack(getTransaction());
      getTransaction().getCallStack().empty();
      eh_.handleException(e, getTransaction());
      eh_.setHandlerProcess(null);
      abort = true;
    }

    // Handle uncaught JDK exceptions
    catch (Exception e)
    {
      //e.printStackTrace();
      AnyException ce = new ContainedException(e);
      ce.fillInCallStack(getTransaction());
      getTransaction().getCallStack().empty();
      eh_.handleException(ce, getTransaction());
      eh_.setHandlerProcess(null);
      abort = true;
    }

    // Handle uncaught JDK exceptions
    catch (LinkageError e)
    {
      //e.printStackTrace();
      AnyException ce = new ContainedException(e);
      ce.fillInCallStack(getTransaction());
      getTransaction().getCallStack().empty();
      eh_.handleException(ce, getTransaction());
      eh_.setHandlerProcess(null);
      abort = true;
    }

    catch (StackOverflowError e)
    {
      //e.printStackTrace();
      AnyException ce = new ContainedException(e);
      ce.topOfStack(getTransaction());
      getTransaction().getCallStack().empty();
      eh_.handleException(ce, getTransaction());
      eh_.setHandlerProcess(null);
      abort = true;
    }

    // Serious errors, will cause thread to terminate
    catch (Error e)
    {
      unrecoverableError(e);
      abort = true;
    }

    finally
    {
      try
      {
        getTransaction().getCallStack().empty();
        // Check if we've been un-deadlocked, killed or whatever
        if (killed_ || deadlockVictim_ || abort)
        {
          // Unwind any transaction that may be in progress
          if (deadlockVictim_)
          {
            System.out.print("Undeadlocked ");
            deadlockVictim_ = false;
          }
          //System.out.println ("transaction aborting...");
          getTransaction().abort();
          setContext(null);
          setContextPath(null);
        }
        else
        {
          // All OK - commit transaction
          //System.out.println ("transaction commiting...");
          try
          {
            getTransaction().commit();
            setContext(null);
            setContextPath(null);
          }
          catch(Exception e)
          {
            System.out.println ("exception during commit");
            ContainedException ce = new ContainedException(e);
            ce.fillInCallStack(getTransaction());
            eh_.handleException(ce, getTransaction());
            eh_.setHandlerProcess(null);
            //System.out.println ("transaction aborting...");
            getTransaction().abort();
            setContext(null);
            setContextPath(null);
          }
        }
      }
      catch (AnyException e)
      {
        eh_.handleException(e, getTransaction());
        eh_.setHandlerProcess(null);
      }
      catch (AnyRuntimeException e)
      {
        eh_.handleException(e, getTransaction());
        eh_.setHandlerProcess(null);
      }
      eh_.setHandlerProcess(null);
    }
  }

  private void terminateProcess()
  {
    if (timer_ != null)
    {
      timer_.cancel();
      timer_ = null;
    }

    // No point in processing node events after the client
    // has gone.
    if (!isClient_ && rootEd_ != null)
    {
      //System.out.println ("Removing ClientPropagator");
      EventGenerator eg = (EventGenerator)getRoot();
      eg.removeEventListener(rootEd_);
    }

    if (parentProcess_ != null)
    {
      parentProcess_.removeChildProcess(this, getTransaction());
    }
    else
    {
      // remove us from $catalog.processes
      //Map catalog   = Catalog.instance().getCatalog();
      //Map processes = (Map)catalog.get(Process.PROCESSES);
      //processes.remove (IdentityOf.identityOf(this));
      RemoveFrom removeFrom = new RemoveFrom(this);
      removeFrom.setTransaction(getTransaction());
      try
      {
        // We know it is the catalog we'll be removing from.
        synchronized(Catalog.class)
        {
          removeFrom.exec(this);
        }
      }
      catch(AnyException ex)
      {
        ex.printStackTrace();
      }
    }

    boolean abort = false;
    try
    {
      if (!isClient_)
      {
        // kill off any children
        killChildProcesses();
        if (callOnEnd_ != null)
        {
          // We didn't clone it until now
          Call callOnEnd = (Call)callOnEnd_.cloneAny();
          callOnEnd.setTransaction(getTransaction());
          callOnEnd.exec(getRoot());
        }
        else
        {
          if (this.contains(package__))
          {
            logLogin("Logout ", this);
            ed_.processEvent(
                  SendRequest.makeRequestEvent(new ConstString(this.get(package__).toString() +
                                                             "." +
                                                             "services.Logout"),
                                               null,
                                               null,
                                               null,
                                               null));
          }
        }
      }
      getTransaction().commit();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      abort = true;
    }
    finally
    {
      try
      {
        ic_.close();
        if(oc_ != null)
          oc_.close();
        //System.out.println("Channel closed");
        if (abort)
          getTransaction().abort();
        
        getTransaction().setMqSession(null);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
      finally
      {
        Globals.removeProcessForThread(thread_, this);
      }
    }
  }

  /**
	 * Send data to this process.  The data will be read by the
	 * process's thread and actioned.  Of course, this method is intended
	 * to be called by other process threads so is thread-safe
	 */
	public void send (Any a) throws AnyException
	{
		lic_.write(a);
	}

  /**
	 * In case we are asked to instantiate ourselves in this way, just
	 * return our underlying <code>Map</code> implementation.  The
	 * assumption is that this is for the purposes of tree-building
	 * and, anyway, AnyComponents are only created for real from XML
	 */
  public Any buildNew(Any a)
  {
		return AbstractComposite.managedMap();
  }

	/**
	 * Called by another process (say, the LockManager) to bump this
	 * process out of a <code>wait()</code> in which it is deadlocked
	 * with one or more competing processes.
	 */
	public void deadlockVictim()
	{
		deadlockVictim_ = true;
		thread_.interrupt();
	}

	/**
	 * Called by another process <code>p</code> (say, the LockManager) to bump this
	 * process out of a <code>wait()</code> in which it is deadlocked
	 * with one or more competing processes.
	 */
	public void kill(Process p) throws AnyException
	{
//		if (p != this && !p.isSupervisor() && !isAncestor(p))
//			throw new PermissionException("Can't kill if not supervisor or parent");

    // Commented out just for xylinq at the moment. User Privs
//    if (p != this &&
//        p.getEffectivePrivilegeLevel() > this.getRealPrivilegeLevel() &&
//        !isAncestor(p))
//      throw new PermissionException("Insufficient privilege or not parent");

    // When we are a server we wait to drain the o/p channel
    // before dying
    if (!isClient_ && oc_ != null && oc_.getSessionId() != null)
    {
    	Any sessionId = oc_.getSessionId();
      // We are a server-side process.  Close our o/p channel
      // and enter the zombie state.
    	oc_.close();
    	Globals.sessionList__.zombieSession(sessionId);
      if (!oc_.hasPendingOutput())
      {
      	// If all o/p has drained we can be officially killed.
      	killed_ = true;
      	Globals.sessionList__.deleteSession(sessionId);
      }
    }
    else
    {
  		killed_ = true;
    }
    
		if (p != this)
		  thread_.interrupt();

    // If we are killing the tread then close the i/p channel
    // Caters for the case where there are child processes
    // sharing the i/p channel but the one being killed is not
    // the one at the channel head.  Hopefully there are no
    // other conditions where a thread would not be bumped
    // out of a wait condition
    if (killed_)
      lic_.close();

    // kill off any children
    //killChildProcesses();
	}
  
  public void interrupt()
  {
    try
    {
      getTransaction().interrupt();
      thread_.interrupt();
    }
    catch (Exception e) {}
  }

	public boolean isSupervisor ()
	{
		return isSupervisor_;
	}

  public boolean isAlive()
  {
    if (thread_ == null)
      return false;
    
    return thread_.isAlive();
  }
  
	public void setSupervisor (boolean b)
	{
		isSupervisor_ = b;
	}

	public AnyTimer getTimer()
	{
    // Lazily create timer.  No point in starting the thread for it
    // if timers are never used by this process. Give it a name that
    // associates it with this process.
    if (timer_ == null)
    {
      String s = "<noname>_";
      if (thread_ != null)
        s = thread_.getName() + "_";
      timer_ = new AnyTimer(new Timer(s + "timer", true));
    }

    return timer_;
	}

	public Any getCatalogPath()
	{
		return catalogPath_;
	}

	public boolean isAncestor(Process p)
	{
		if (p == this)
		  return true;

		if (parentProcess_ == null)
		  return false;

    return parentProcess_.isAncestor(p);
	}

	public synchronized void addChildProcess(Process p, Transaction t)
	{
    if (childProcesses_ == null)
      childProcesses_ = AbstractComposite.array();

    if (childProcesses_.indexOf(p) < 0)
      childProcesses_.add(p);

    try
    {
      NodeSpecification n = new NodeSpecification(IdentityOf.identityOf(p).toString());
	    AddTo addTo = new AddTo(p, n);
      addTo.setTransaction(t);
	    addTo.exec(this);
    }
    catch(AnyException e)
    {
    	throw new RuntimeContainedException(e);
    }
	}

	public synchronized void removeChildProcess(Process p, Transaction t)
	{
		int indx = -1;
    if ((indx = childProcesses_.indexOf(p)) >= 0)
    {
      childProcesses_.remove(indx);
      RemoveFrom removeFrom = new RemoveFrom(p);
      removeFrom.setTransaction(t);
      try
      {
        removeFrom.exec(this);
      }
      catch(AnyException e)
      {
      	e.printStackTrace();
      }
    }
	}

  public void setWaitingObject(Any a, long timeout)
  {
    if (waitingObject_ != null && a != null)
      throw new IllegalArgumentException("Can't wait on " + a + " when already waiting on " + waitingObject_);

    waitingObject_  = a;
    waitingTimeout_ = timeout;
  }

  public Any  getWaitingObject()
  {
    return waitingObject_;
  }

  public long getWaitingTimeout()
  {
    if (waitingObject_ != null)
      return waitingTimeout_;
    else
      return 0;
  }
  
  public void setName(Any name)
  {
    if (AnyNull.isNull(name))
      throw new IllegalArgumentException("Cannot set process name to null");
    
    if (!(name instanceof StringI))
      throw new IllegalArgumentException("process name must be a string");
    
    this.replaceItem(processName__, name);
    thread_.setName(name.toString());
  }

  public void setLockWaitObject(Any a, long timeout)
  {
    if (lockWaitObject_ != null && a != null)
      throw new IllegalArgumentException("Can't lockWait on " + a + " when already waiting on " + lockWaitObject_);

    lockWaitObject_  = a;
    lockWaitTimeout_ = timeout;
  }

  public Any  getLockWaitObject()
  {
    return lockWaitObject_;
  }

  public long getLockWaitTimeout()
  {
    if (lockWaitObject_ != null)
      return lockWaitTimeout_;
    else
      return 0;
  }

  public void notifyUnlock(Any a)
  {
    // No operation where there is a thread to notify.
  }

  public Any getId()
  {
    return new ConstInt(hashCode());
  }
  
  // --- Start PropertyAccessMap ---

  public Any get (Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }

      return propertyMap_;
    }
    else
    {
      return super.get(key);
    }
  }

  public Any getIfContains(Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }

      return propertyMap_;
    }
    else
    {
      return super.getIfContains(key);
    }
  }

  public boolean contains (Any key)
  {
    if (properties__.equals(key))
      return true;

    return super.contains(key);
  }

  public boolean isEmpty() { return false; }
  
  // --- End PropertyAccessMap ---

  public ExceptionHandler getExceptionHandler()
  {
    return eh_;
  }

  public void startThread()
  {
    this.startThread(false);
  }

  public void startThread(boolean isDaemon)
  {
		// off we go
    if (thread_ == null)
    {
      thread_ = new Thread(this);
      thread_.setDaemon(isDaemon);
      if (this.contains(processName__))
        thread_.setName(this.get(processName__).toString());
      thread_.start();
      //Globals.setProcessForThread(thread_, this);
    }
  }

  public void join()
  {
    join(0);
  }

  public void join(long waitTime)
  {
    try
    {
      thread_.join(waitTime);
    }
    catch(InterruptedException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

//  protected  void finalize() throws Throwable
//  {
//    System.out.println("***** Finalised " + getClass() + " " + get(processName__)  + " " + System.identityHashCode(getRoot()));
//    super.finalize();
//  }
  
  protected void serverLost() throws AnyException
	{
	}

	/**
   * Handle a <code>java.lang.Error</code> derived from which we
   * can recover.  Examples include <code>StackOverflowError</code>.
   */
	protected void recoverableError(Error e)
	{
	}

  protected void writeOutput(Any a) throws AnyException
  {
    oc_.write(a);
    oc_.flushOutput();
  }

	/**
   * Handle a <code>java.lang.Error</code> derived from which we
   * cannot recover.  Examples include <code>OutOfMemoryError</code>.
   * Process will terminate.
   */
	protected void unrecoverableError(Error e)
	{
    try
    {
      e.printStackTrace();
      //kill(this);
    }
    catch(Exception ex) {}
    // force death
    //killed_ = true;

    if (e instanceof ThreadDeath)
    {
      killed_ = true;
      throw e;
    }
	}

/*
	private void init(ChannelListener channelListener) throws AnyException
	{
		// Default to the null transaction
		//setTransaction(new NullTransaction (this));
		stackFrames_ = AbstractComposite.queue();

		// Set up local input channel
		lic_ = new AnyChannel(new WriteCopy((OutputChannel)ic_));

		BuildNodeMap bn = new BuildNodeMap();
		bn.setTransaction(getTransaction());

		// Set up our map representing the universe as we know it.
		root_ = AbstractComposite.managedMap();

		// Put the channels in at well-known paths
		if (oc_ != null)
			bn.build(ServerConstants.ROCHANNEL, oc_, root_);
		bn.build(ServerConstants.RICHANNEL, lic_, root_);
		bn.build(ServerConstants.PICHANNEL, lic_, root_);
		bn.build(ServerConstants.OWNPROCESS, this, root_);

		// Set up an event dispatcher to handle all received events at
		// our input channel
		if (ed_ == null)
		  ed_ = new EventDispatcher();

		channelListener.setRoot(root_);
		ed_.addEventListener(channelListener);
		if (rootListener_ instanceof UpdateEventProcessor)
		{
			// client
			System.out.println ("Setting dispatch to UpdateEventProcessor");
			UpdateEventProcessor n = (UpdateEventProcessor)rootListener_;
			n.setRoot(root_);
			n.setTransaction(transaction_);
			ed_.addEventListener(rootListener_);

			ReplaceEventProcessor re = new ReplaceEventProcessor
				(EventIdMap.makeNodeEventType(EventConstants.NODE_REPLACED,
																			AnyAlwaysEquals.instance(),
																			AnyAlwaysEquals.instance()),
				 root_);

      re.addEventType(EventIdMap.makeNodeEventType(EventConstants.NODE_ADDED,
																			AnyAlwaysEquals.instance(),
																			AnyAlwaysEquals.instance()));
			re.setTransaction(transaction_);
			ed_.addEventListener(re);

			RemoveEventProcessor rm = new RemoveEventProcessor
				(EventIdMap.makeNodeEventType(EventConstants.NODE_REMOVED,
																			AnyAlwaysEquals.instance(),
																			AnyAlwaysEquals.instance()),
				 root_);

			rm.setTransaction(transaction_);
			ed_.addEventListener(rm);
		}
		if (rootListener_ instanceof ClientPropagator)
		{
			if (root_ instanceof EventGenerator)
			{
				ClientPropagator cp = (ClientPropagator)rootListener_;
				cp.addEventType(EventIdMap.makeNodeEventType
													(EventConstants.BOT_DELETE,
													 AnyAlwaysEquals.instance(),
													 AnyAlwaysEquals.instance()));
				cp.addEventType(EventIdMap.makeNodeEventType
													(EventConstants.NODE_REPLACED,
													 AnyAlwaysEquals.instance(),
													 AnyAlwaysEquals.instance()));
				cp.addEventType(EventIdMap.makeNodeEventType
													(EventConstants.NODE_REMOVED,
													 AnyAlwaysEquals.instance(),
													 AnyAlwaysEquals.instance()));
				cp.addEventType(EventIdMap.makeNodeEventType
													(EventConstants.NODE_ADDED,
													 AnyAlwaysEquals.instance(),
													 AnyAlwaysEquals.instance()));
				rootEd_ = new EventDispatcher();
				//System.out.println ("Setting forward to ClientPropagator");
				EventGenerator eg = (EventGenerator)root_;
				eg.addEventListener(rootEd_);
				rootEd_.addEventListener(rootListener_);
			}
		}

		deadlockVictim_ = false;
		killed_         = false;

		isSupervisor_   = false;

		// off we go
	  thread_ = new Thread(this);
	  thread_.start();
	}
*/

 	private synchronized void killChildProcesses() throws AnyException
 	{
    if (childProcesses_ != null)
    {
      for (int i = 0; i < childProcesses_.entries(); i++)
      {
        Process c = (Process)childProcesses_.get(i);
        c.kill(this);
      }
    }
 	}

 	// Initialisation that should only be carried out if the
 	// thread starts, and in the new thread.
 	private void initInThread()
 	{
    try
    {
      Globals.setProcessForThread(thread_, this);

      // Probably better to use a meta-data typedef to manage the processes.
      // The catalog is a bit of a bodge, perhaps. TODO.
      synchronized(Catalog.class)
      {
        this.add(Process.STARTED, new ConstDate());
        if (parentProcess_ != null)
        {
          //System.out.println("I'm a child process of " + parentProcess_);
          catalogPath_ = new ConstString(parentProcess_.getCatalogPath().toString() +
                                         "." +
                                         IdentityOf.identityOf(this).toString());
          parentProcess_.addChildProcess(this, getTransaction());
          //System.out.println("at " + catalogPath_);
        }
        else
        {
          // top level process - put in at $catalog.processes.<identity>
          catalogPath_ = new ConstString(NodeSpecification.catalog__ +
                                       ".processes." +
                                       IdentityOf.identityOf(this).toString());
          NodeSpecification n = new NodeSpecification(catalogPath_.toString());

          AddTo addTo = new AddTo(this, n, AnyBoolean.FALSE);
          addTo.setTransaction(getTransaction());
          addTo.exec(getRoot());
        }
      }
    }
    catch (Exception e)
    {
      // bit weak but unlikely to happen!
      e.printStackTrace();
    }
 	}

	private void initCommon() throws AnyException
	{
    initMembers();

		BuildNodeMap bn = new BuildNodeMap();
		bn.setTransaction(getTransaction());

		// Set up local input channel
		if (lic_ == null)
		{
      lic_ = new AnyChannel(new WriteCopy((OutputChannel)ic_));
      bn.build(ServerConstants.RICHANNEL, lic_, getRoot());
      bn.build(ServerConstants.PICHANNEL, lic_, getRoot());
    }

		if (oc_ != null)
		{
			bn.build(ServerConstants.ROCHANNEL, oc_, getRoot());
      bn.build(ServerConstants.POCHANNEL, oc_, getRoot());
		}

    if (!contains(ID))
      add(ID, getId());
    
		//bn.build(ServerConstants.OWNPROCESS, this, root_);

		deadlockVictim_ = false;
		killed_         = false;

		isSupervisor_   = false;
	}

	protected void initClient(EventDispatcher connectedDispatcher) throws AnyException
	{
		ic_ = new AnyChannel(new FIFO(0, ChannelConstants.REFERENCE));

		// Add the those event listeners here that the caller
		// hasn't done.
		LoginOK lok;

		connectedDispatcher.addEventListener(new ServerLost());
		connectedDispatcher.addEventListener(new DispatchListener(getRoot(), getTransaction()));
		connectedDispatcher.addEventListener(new ReLoginDetails());
		connectedDispatcher.addEventListener(lok = new LoginOK());

		disconnectedEd_.addEventListener(new LoginDetails());
		disconnectedEd_.addEventListener(lok);
		disconnectedEd_.addEventListener(new LoginDenied());
		disconnectedEd_.addEventListener(new InvokeEventProcessor());
		disconnectedEd_.addEventListener(new DispatchListener(getRoot(), getTransaction()));
		disconnectedEd_.addEventListener(InvokeService.makeInvokeService
		                                   (EventConstants.INVOKE_LOGINSVC,
		                                    getTransaction(),
		                                    getRoot()));

	}

	protected void initServer(EventListener   rootListener,
                            EventDispatcher ed)
	{
		if (rootListener != null)
		{
	    rootEd_ = new EventDispatcher();
	    //System.out.println ("Setting forward to ClientPropagator");
	    EventGenerator eg = (EventGenerator)getRoot();
	    eg.addEventListener(rootEd_);
	    rootEd_.addEventListener(rootListener);
		}
    ed.addEventListener(new LoginRequest());
    ed.addEventListener(new LoginService());
		ed.addEventListener(new DispatchListener(getRoot(), getTransaction()));
	}

  protected Any getStartEventType()
  {
    return EventConstants.START_USERPROCESS;
  }

  protected void connectError(AnyException e)
  {
  }

	private void eventServerLost() throws AnyException
	{
	  doServerLost();
		serverLost();
	}

	private void doServerLost() throws AnyException
	{
    if (oc_ != null)
      ((AnyChannel)oc_).stopKeepAliveTimer();
    
    if (timer_ != null)
    {
      timer_.cancel();
      timer_ = null;
    }

		ed_ = disconnectedEd_;
		eh_.setServerConnected(false);
	}

	private void runInitInq(URL initInq) throws AnyException
	{
		if (initInq != null)
		{
			setContext(getRoot());
			setContextPath(ServerConstants.NSROOT);
	    ExecInq execInq = new ExecInq(new AnyURL(initInq));
	    execInq.setTransaction(getTransaction());

	    execInq.exec(getRoot());
		}
	}

	private void loginOK(Event e) throws AnyException
	{
    Map serverResp = (Map)e.getContext();
    
    Any serverTimeZone = serverResp.get(AnyTimeZone.timezone__);
    Globals.serverTimeZone__ = (AnyTimeZone)serverTimeZone;

    this.replaceItem(loginName__, serverResp.get(loginName__));
    this.replaceItem(package__, serverResp.get(package__));
    
    Any expired   = null;
    Any expiresIn = null;

    if (serverResp.contains(expired__))
      expired = serverResp.get(expired__);
    if (serverResp.contains(expiresIn__))
      expiresIn = serverResp.get(expiresIn__);

		// Run the post-login service
		OutputChannel o = (OutputChannel)ic_;

		Map args = null;
		if (expired != null || expiresIn != null)
		{
		  args = AbstractComposite.simpleMap();
		  args.add(expired__, expired);
		  args.add(expiresIn__, expiresIn);
		}

		o.write(SendRequest.makeRequestEvent(postLoginsvc__,
                                         loginWindow__,
                                         null,
                                         args,
                                         null));

    // Check the server's response, which includes the privilege
    // level assigned to us and whether the server will shortly
    // be sending us a script.
    ShortI privLevel = (ShortI)serverResp.get(privLevel__);
    boolean scriptArriving = serverResp.contains(url__);


    // If there's no script arriving then the server will not
    // be sending us anything else and we have the privilege of
    // doing it ourselves.
    if(!scriptArriving && expired == null && expiresIn == null)
    {
      if (initURL_ == null)
      {
        // The client didn't supply an initial URL and the server
        // didn't send one either.
        throw new AnyException("Neither client or server specified an initial script URL");
      }

      //Set up and execute the initial action.  This comprises a
      // download of the server's descriptors (may be we should
      // refine this to just get the package we want?) and a
      // load of the initial URL.
      Array seq = AbstractComposite.array();

      seq.add(new FetchDescriptors());
      LoadClient lc = new LoadClient(initURL_);
      lc.setBaseURL(initURL_.toString());
      //ExecInq lc = new ExecInq(new AnyURL(initURL_));
      seq.add(lc);
      Sequence init = new Sequence(seq);
      setContextPath(new AnyString("."));
      setContext(getRoot());
      init.setTransaction(getTransaction());
      init.exec(getRoot());
    }

		ed_ = connectedEd_;
		eh_.setServerConnected(true);

    this.setRealPrivilegeLevel(privLevel.getValue());
    this.setEffectivePrivilegeLevel(privLevel.getValue());
	}

	private void loginDenied(Map m) throws AnyException
	{
    // Leave the disconnected event dispatcher in effect,
    // just throw an exception
    LocateNode l = new LocateNode("$catalog.guiFuncs.loginDenied");
    //System.out.println("Context ID " + System.identityHashCode(getContext()));
    AnyFuncHolder.FuncHolder handler = (AnyFuncHolder.FuncHolder)
                  EvalExpr.evalFunc(getTransaction(),
                                    Catalog.instance().getCatalog(),
                                    l,
                                    AnyFuncHolder.FuncHolder.class);
    throw new AnyException(m.get(reason__).toString(),
                           m,
                           handler,
                           false);
	}

  private void login(Map loginSpec) throws AnyException
  {
  	Any hostName = loginSpec.get(host__);
  	boolean exit = false;
  	if (loginSpec.contains(exit__))
      exit = ((BooleanI)loginSpec.get(exit__)).getValue();

    System.out.println ("AnyClient: Connecting by socket to " + hostName);

    AnyInt keepAliveTimeout = new AnyInt(0);
    
    Socket cd = new Socket();
    try
    {
      try
      {
        // Check if there is a user-accepted cert chain in the
        // login spec
        Any certChain = null;

        if (loginSpec.contains(cert__))
          certChain = loginSpec.get(cert__);
        
        URL u = new URL(hostName.toString());
        
        // See if the url query args specified a keepalive t/o
        Map qArgs = NetUtil.parseURLQuery(u.getQuery(), null);
        if (qArgs != null && qArgs.contains(keepAlivePeriod__))
        {
          keepAliveTimeout.copyFrom(qArgs.get(keepAlivePeriod__));
          
          // Min period is hard-coded at 30s
          if (keepAliveTimeout.getValue() < 30000)
            keepAliveTimeout.setValue(30000);

          cd.setProbeTimeout(keepAliveTimeout.getValue());
        }

        cd.openURL(u, certChain);
      }
      catch(MalformedURLException e)
      {
        throw new ContainedException(e);
      }
      catch (ContainedException e)
      {
        //System.out.println("****************** EE " + e.getMessage());
        //System.out.println(e.getThrowable().getClass());

        //e.printStackTrace();
        if (e.getThrowable() instanceof SSLHandshakeException)
        {
          // This is what comes out when the TrustManager throws

          // Rethrow in whatever way we should according to what
          // the exception contains and so that we get to the
          // exception handler for certificate problems.

          // Externalise the certificate and put it into the loginSpec
          Any cert = cd.getInqURLConnection().getTrusted();
          //System.out.println("CERT IS " + cert);
          loginSpec.add(cert__, cert);

          Throwable t = e.getThrowable();

          // Locate the handler
          LocateNode l = new LocateNode("$catalog.guiFuncs.certificateError");
          //System.out.println("Context ID " + System.identityHashCode(getContext()));
          AnyFuncHolder.FuncHolder handler = (AnyFuncHolder.FuncHolder)
                        EvalExpr.evalFunc(getTransaction(),
                                          Catalog.instance().getCatalog(),
                                          l,
                                          AnyFuncHolder.FuncHolder.class);

          //System.out.println("HANDLER IS " + handler);
          throw new RuntimeContainedException(t,
                                              t.getMessage(),
                                              loginSpec,
                                              handler,
                                              false);
        }
        else
          throw e;
      }

      if (oc_ == null)
      {
        oc_ = new AnyChannel(cd);
        disconnectedEd_.addEventListener(new SessionID(oc_));
        initCommon();
      }
      else
      {
        oc_.resetOutput(cd);
      }

      BooleanI requestSession = null;
      if (cd.isKeepOpen())
        requestSession = AnyBoolean.TRUE;

      // Send the login request to the server. Include any keepAlive timeout
      // period. If specified the server will also send probes at that
      // interval.
      oc_.write(new StartProcessEvent(getStartEventType(),
                                      requestSession,
                                      loginSpec.get(NodeSpecification.user__),
                                      keepAliveTimeout.getValue()));

      oc_.write(new SimpleEvent(EventConstants.LOGIN_REQUEST, loginSpec));
      oc_.flushOutput();
      if (cd.isKeepOpen())
        ((AnyChannel)oc_).startKeepOpenProbe();

      thread_.setName(loginSpec.get(NodeSpecification.user__).toString());

      // Start sending keepalives if there was a timeout value set
      ((AnyChannel)oc_).startKeepAliveTimer(this);

      // Even if we have a network input channel already (because
      // we are reconnecting) create a new one, because the read
      // thread is defunct in the old one.  But don't create
      // it until the initial output has been sent above, in case we
      // are dealing with a stateless connection
      AnyChannel readInput = new AnyChannel(cd,
                                            (OutputChannel)ic_,
                                            this,
                                            loginSpec.get(NodeSpecification.user__).toString() + "_Socket");

      // Since we may come through here more than once remove
      // any existing
      SessionReconnect sr = new SessionReconnect(cd, readInput);
      SessionDefunct   sd = new SessionDefunct(readInput);
      connectedEd_.removeEventListener(sr);
      connectedEd_.removeEventListener(sd);
      connectedEd_.addEventListener(sr);
      connectedEd_.addEventListener(sd);
      disconnectedEd_.removeEventListener(sr);
      disconnectedEd_.removeEventListener(sd);
      disconnectedEd_.addEventListener(sr);
      disconnectedEd_.addEventListener(sd);

      // Save the initial URL for when we get the LoginOK event
      if (loginSpec.contains(url__))
        initURL_ = loginSpec.get(url__);

      // No specific use but add username and package to
      // the process itself.
      if (!contains(NodeSpecification.user__))
      {
        add(NodeSpecification.user__, loginSpec.get(NodeSpecification.user__));
        add(UserProcess.package__, loginSpec.get(UserProcess.package__));
      }
      else
      {
        replaceItem(NodeSpecification.user__, loginSpec.get(NodeSpecification.user__));
        replaceItem(UserProcess.package__, loginSpec.get(UserProcess.package__));
      }
    }
    catch (AnyException e)
    {
      connectError(e);

      if (exit)
        kill(this);

      throw e;
    }
  }

  private void reLogin(Map loginSpec) throws AnyException
  {
    reset();

    // Send a login event to the server in the connected state
    oc_.write(new SimpleEvent(EventConstants.LOGIN_REQUEST, loginSpec));
  }

  private void loginRequest(Map loginSpec) throws AnyException
  {
    // server - handle login request
    reset();

    // Put the login name and package into the process map and
    // send ourselves the login service invocation for this package.
    if (!contains(NodeSpecification.user__))
      add(NodeSpecification.user__, loginSpec.get(NodeSpecification.user__));
    if (!contains(UserProcess.loginName__))
      add(UserProcess.loginName__, loginSpec.get(NodeSpecification.user__));
    if (!contains(package__))
      add(package__,   loginSpec.get(package__));
    if (!contains(SystemProperties.localhostname))
      add(SystemProperties.localhostname, loginSpec.get(SystemProperties.localhostname ));
    if (!contains(address__))
    {
      java.net.Socket s = ((AnyChannel)oc_).getSocket();
      add(address__,
          new AnyInetSocketAddress((InetSocketAddress)s.getRemoteSocketAddress()));
    }
    
    if (!contains(processName__))
    {
      if (loginSpec.contains(processName__))
      {
        add(processName__, loginSpec.get(processName__));
        
        processName_ = loginSpec.get(processName__);
      }
      else
      {
        add(processName__, loginSpec.get(NodeSpecification.user__));
        
        processName_ = loginSpec.get(NodeSpecification.user__);
      }
    }

    thread_.setName(get(processName__).toString());
    
    // Start sending keepalives if there was a timeout received in the i/c
    // startprocess event.
    ((AnyChannel)oc_).startKeepAliveTimer(this);

    //add(passwd__,    loginSpec.get(passwd__));

    Map args = AbstractComposite.simpleMap();
    args.add(UserProcess.loginName__, loginSpec.get(NodeSpecification.user__));
    args.add(passwd__, loginSpec.get(passwd__));
    args.add(package__,   loginSpec.get(package__));
    if (loginSpec.contains(ignoreExpiring__))
      args.add(ignoreExpiring__, loginSpec.get(ignoreExpiring__));

    OutputChannel o = (OutputChannel)ic_;
    o.write(
      SendRequest.makeRequestEvent(EventConstants.INVOKE_LOGINSVC,
                                   new ConstString(loginSpec.get(package__).toString() +
                                                 "." +
                                                 "services.Login"),
                                   null,
                                   null,
                                   args,
                                   null));
    
    logLogin("Login ", loginSpec);
  }
  
  synchronized private void logLogin(String msg, Map loginSpec)
  {
    if (oc_ == null)
      return;
    
    String inqhome = System.getProperty("inq.home");
    if (inqhome == null)
      throw new AnyRuntimeException("inq.home is undefined");
    
    String separator = System.getProperty("file.separator");
    
    try
    {
      File log = new File(inqhome + separator + "log" + separator + "server.log");
      
      File logdir = log.getParentFile();
      logdir.mkdirs();
      
      if (!log.exists())
        log.createNewFile();
      
      PrintStream p = new PrintStream(new FileOutputStream(log, true));
      
      java.net.Socket s = ((AnyChannel)oc_).getSocket();
      
      p.println(msg + " from " + s.getRemoteSocketAddress().toString());
      p.println(loginSpec);
      p.println("");
      p.close();
    }
    catch(IOException ioe)
    {
    }
  }

	private void runLoginService(Event e) throws AnyException
	{
		// Handle the login service specially.  Run it as normal but
		// if there's an exception (typically caused by the absence
		// of the service) catch it and send the login denied event
		// as well as the exception.
		Event is = new SimpleEvent(EventConstants.INVOKE_SVRLOGIN,
		                           e.getContext());

		AnyException        aex  = null;
		AnyRuntimeException arex = null;

    try
    {
		  ed_.processEvent(is);
    }
    catch(AnyException ae)
    {
    	aex = ae;
    }
    catch(AnyRuntimeException are)
    {
    	arex = are;
    }
    catch(Exception je)
    {
    	aex = new ContainedException(je);
    }

    // explicitly write to output channel as we know we are a
    // server and we don't want to flush the output at this
    // point in the primordial phase.
    if (aex != null)
    {
      aex.fillInCallStack(getTransaction());
    	oc_.write(aex);
      kill(this);
    }
    else if (arex != null)
    {
      arex.fillInCallStack(getTransaction());
    	oc_.write(arex);
      kill(this);
    }
	}

	private class ServerLost extends    AbstractAny
                           implements EventListener
  {
    private static final long serialVersionUID = 1L;

    public boolean processEvent(Event e) throws AnyException
    {
      System.out.println("UserProcess$ServerLost.processEvent");
      eventServerLost();
      throw new AnyException("Lost contact with server");
    }

    public Array getDesiredEventTypes()
    {
      return serverLostEventTypes__;
    }
  }

  // We have received the login details in a local event
  // so try to connect to the host and login with the
  // specified user/passwd/package
	private class LoginDetails extends    AbstractAny
                             implements EventListener
  {
    private static final long serialVersionUID = 1L;

    public boolean processEvent(Event e) throws AnyException
    {
      //System.out.println("UserProcess$LoginDetails.processEvent");

      // Retrieve the login details from the event and attempt
      // to connect to the server specified therein
      login((Map)e.getContext());
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return loginDetailsEventTypes__;
    }
  }

	private class ReLoginDetails extends    AbstractAny
                               implements EventListener
  {
    private static final long serialVersionUID = 1L;

    public boolean processEvent(Event e) throws AnyException
    {
      System.out.println("UserProcess$ReLoginDetails.processEvent");

      // Retrieve the login details from the event and attempt
      // to connect to the server specified therein
      reLogin((Map)e.getContext());
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return loginDetailsEventTypes__;
    }
  }

	private class LoginRequest extends    AbstractAny
                             implements EventListener
  {
    private static final long serialVersionUID = 1L;

    public boolean processEvent(Event e) throws AnyException
    {
      //System.out.println("UserProcess$LoginRequest.processEvent");

      // Server - retrieve the login details from the event
      // and create an invocation event for the login service
      // for the specified package.
      loginRequest((Map)e.getContext());
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return loginRequestEventTypes__;
    }
  }

	private class LoginOK extends    AbstractAny
                        implements EventListener
  {
    private static final long serialVersionUID = 1L;

    public boolean processEvent(Event e) throws AnyException
    {
      //System.out.println("UserProcess$LoginOK.processEvent");
      loginOK(e);
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return loginOKEventTypes__;
    }
  }

	private class LoginDenied extends    AbstractAny
                            implements EventListener
  {
    private static final long serialVersionUID = 1L;

    public boolean processEvent(Event e) throws AnyException
    {
      System.out.println("UserProcess$LoginDenied.processEvent");
      loginDenied((Map)e.getContext());
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return loginDeniedEventTypes__;
    }
  }

	private class LoginService extends    AbstractAny
                             implements EventListener
  {
    private static final long serialVersionUID = 1L;

    public boolean processEvent(Event e) throws AnyException
    {
      //System.out.println("UserProcess$LoginService.processEvent");
      runLoginService(e);
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return loginServiceEventTypes__;
    }
  }
  // Pops up a window for the user to input the proxy server
  // parameters while the user process is in a primordial
  // state.
  /*
  private class GUIAuth extends Authenticator
  {
  	private static PasswordAuthentication auth__;

    protected PasswordAuthentication getPasswordAuthentication()
    {
    	if (auth__ != null)
    	  return auth__;

    	LocateNode ln = new LocateNode("$root.proxyDialog");

    	ClassLoader cl = this.getClass().getClassLoader();

    	URL proxyDialog = cl.getResource("com/inqwell/any/client/proxyDialog.bml");
      System.out.println ("Authenticator called!");

  		String proxyUser = System.getProperty(proxyUser__);
		  if (proxyUser == null)
		    proxyUser = System.getProperty(httpProxyUser__);

  		String proxyPasswd = System.getProperty(proxyPasswd__);
		  if (proxyPasswd == null)
		    proxyPasswd = System.getProperty(httpProxyPasswd__);

      return new PasswordAuthentication(proxyUser, proxyPasswd.toCharArray());
    }
  }
  */
}
