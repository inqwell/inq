/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/tools/AnyClient.java $
 * $Author: sanderst $
 * $Revision: 1.14 $
 * $Date: 2011-04-18 21:46:04 $
 */
package com.inqwell.any.tools;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFuncHolder;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyTimeZone;
import com.inqwell.any.AnyURL;
import com.inqwell.any.BasicProcess;
import com.inqwell.any.BuildNodeMap;
import com.inqwell.any.Call;
import com.inqwell.any.Catalog;
import com.inqwell.any.Composite;
import com.inqwell.any.ConstString;
import com.inqwell.any.DefaultPropertyAccessMap;
import com.inqwell.any.DepthFirstIter;
import com.inqwell.any.EventConstants;
import com.inqwell.any.EventDispatcher;
import com.inqwell.any.ExceptionHandler;
import com.inqwell.any.ExceptionToService;
import com.inqwell.any.ExecInq;
import com.inqwell.any.Globals;
import com.inqwell.any.InqInterpreter;
import com.inqwell.any.InvokeService;
import com.inqwell.any.Iter;
import com.inqwell.any.LockManager;
import com.inqwell.any.Map;
import com.inqwell.any.NullService;
import com.inqwell.any.RunInq;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.ServerConstants;
import com.inqwell.any.Set;
import com.inqwell.any.StringI;
import com.inqwell.any.Transaction;
import com.inqwell.any.UserProcess;
import com.inqwell.any.beans.WindowF;
import com.inqwell.any.channel.AnyChannel;
import com.inqwell.any.channel.ChannelConstants;
import com.inqwell.any.channel.FIFO;
import com.inqwell.any.client.ClientTransaction;
import com.inqwell.any.client.UpdateEventProcessor;
import com.inqwell.any.client.swing.SwingInvoker;
import com.inqwell.any.net.InqStreamHandlerFactory;
import com.inqwell.any.net.NetUtil;
import com.inqwell.any.server.LoginException;
import com.inqwell.any.util.CommandArgs;

public class AnyClient extends DefaultPropertyAccessMap
{
  public static StringI clientPath__ = new ConstString("$catalog.inq.system.Client");

  private static AnyClient theClient__ = null;
  
  private AnyFuncHolder.FuncHolder serverLostF_;

  public static AnyClient instance()
  {
    if (theClient__ == null)
    {
      synchronized (AnyClient.class)
      {
        if (theClient__ == null)
          theClient__ = new AnyClient();
      }
    }
    return theClient__;
  }
  
  private static String findLAFName(String lafClass)
  {
    UIManager.LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();
    for (int i = 0; i < lafInfo.length; i++)
    {
      UIManager.LookAndFeelInfo lafI = lafInfo[i];
      if (lafI.getClassName().equals(lafClass))
        return lafI.getName();
    }
    return null;
  }

  public static void main (String args[]) throws LoginException,
                                                 AnyException
  {
		// Set up globals
		Globals.channelOutputReplacements__ = com.inqwell.any.client.Replacements.clientToServer__;
		Globals.channelInputReplacements__  = com.inqwell.any.client.Replacements.clientFromServer__;
		Globals.streamOutputReplacements__  = com.inqwell.any.client.Replacements.clientToNativeStream__;
		Globals.streamInputReplacements__   = com.inqwell.any.client.Replacements.clientFromNativeStream__;
		
		// XML Replacements
		Globals.xmlStreamOutputReplacements__ = com.inqwell.any.client.Replacements.clientToXMLStream__;
		Globals.xmlStreamInputReplacements__  = com.inqwell.any.client.Replacements.clientFromXMLStream__;

		Globals.interpreter__               = new InqInterpreter();
    Globals.lockManager__               = new LockManager();

    System.out.println("Inq Client");
    System.out.println("Copyright (c) InqWell Ltd 2002-2011");
    System.out.println("Java Compiler Compiler Version 3.2 Copyright (c) 2003 Sun Microsystems, Inc. All  Rights Reserved.");
    System.out.println("JDateChooser Copyright (c) Kai Toedter 1999 - 2009");
    System.out.println("TableLayout Copyright (c) 2001 Daniel Barbalace. All rights reserved.");

//    if (args.length == 0)
//      usage();

    // Process command line arguments and make them available
    CommandArgs cArgs = new CommandArgs(args);
    Map m = cArgs.toMap();
    Map catalog = Catalog.instance().getCatalog();
    catalog.add(CommandArgs.commandLine__, m);
    catalog.add(AnyTimeZone.timezone__, new AnyTimeZone());

	  new NullService();

    // set up an authenticator
    Authenticator.setDefault(new MyAuth());

    // Initialise custom url protocols
    InqStreamHandlerFactory.install();
		
    // Catalog ourselves before the startup script is run - it sets some
    // of our properties.
    BuildNodeMap bn = new BuildNodeMap();
    bn.build(clientPath__, AnyClient.instance(), Catalog.instance().getCatalog());

    // Set up process environment and start
		AnyChannel    ich = new AnyChannel(new FIFO(0, ChannelConstants.REFERENCE));
		AnyChannel    och = null;

		EventDispatcher  connectedEd    = new EventDispatcher();
		EventDispatcher  disconnectedEd = new EventDispatcher();
    ExceptionHandler eh             = new ExceptionToService(new ConstString("system.client.services.handleException"),
                                                             UserProcess.loginContext__);
    //eh = new ExceptionToStream(System.out);
    BasicProcess.RootMap root           = new BasicProcess.RootMap();
		Transaction          t              = new ClientTransaction();
		connectedEd.addEventListener(InvokeService.makeInvokeService
		                                   (EventConstants.INVOKE_SVC,
		                                    t,
		                                    root));

    ClassLoader cl = AnyClient.class.getClassLoader();

    URL anyClientInq = cl.getResource("com/inqwell/any/tools/AnyClient.inq");
    anyClientInq = AnyURL.fixJarURL(anyClientInq);
    
    //System.out.println(anyClientInq);

//    Globals.process__ = new UserProcess(Thread.currentThread(), null);
//    Globals.process__.setTransaction(t);
//    Globals.awtSync__ = false;
    UserProcess p = new ClientUserProcess(eh,
                                          t,
                                          root,
                                          connectedEd,
                                          disconnectedEd,
                                          null);//anyClientInq);

    root.setProcess(p);
    
    Globals.setProcessForThread(Thread.currentThread(), p);

    Call.setupCommandLineArgs(anyClientInq.toString(),
                              args,
                              m,
                              cArgs,
                              t);

    synchronized(Globals.process__)
    {
      SwingInvoker.initSwing();
      
      Globals.inqActive__ = true;
      p.setContext(p.getRoot());
      p.setContextPath(ServerConstants.NSROOT);
      ExecInq execInq = new ExecInq(new AnyURL(anyClientInq));
      execInq.setTransaction(p.getTransaction());
      execInq.exec(p.getRoot());
      Globals.inqActive__ = false;
    }
    
    Globals.removeProcessForThread(Thread.currentThread(), p);
    
    p.startThread();

	}

  static void usage ()
  {
    System.out.println ("AnyClient: Bad usage");
    //System.out.println ("      -url <initial source>");
    //System.out.println ("      -user <user>");
    //System.out.println ("      -passwd <passwd>");
    //System.out.println ("      -package <package>");
    //System.out.println ("      -hostname <server host>");
    //System.out.println ("     [-resolve] by default url is opened by server");
		System.exit(1);
  }

  public void setServerLostHandler(Any serverLost)
  {
    serverLostF_ = (AnyFuncHolder.FuncHolder)serverLost;
  }
  
  public Any getServerLostHandler()
  {
    return serverLostF_;
  }

  private static class MyAuth extends Authenticator
  {
		private static final String httpProxyUser__    = "http.proxyUser";
		private static final String proxyUser__        = "proxyUser";
		private static final String httpProxyPasswd__  = "http.proxyPasswd";
		private static final String proxyPasswd__      = "proxyPasswd";

    protected PasswordAuthentication getPasswordAuthentication()
    {
      System.out.println ("Authenticator called!");
/*
  		String proxyUser = System.getProperty(proxyUser__);
		  if (proxyUser == null)
		    proxyUser = System.getProperty(httpProxyUser__);

  		String proxyPasswd = System.getProperty(proxyPasswd__);
		  if (proxyPasswd == null)
		    proxyPasswd = System.getProperty(httpProxyPasswd__);
*/
//AbstractAny.stackTrace();
      Map m = NetUtil.getProxyServer();
  		String proxyPasswd = NetUtil.getProxyPassword(m);
  		String proxyUser = NetUtil.getProxyUser(m);
      return new PasswordAuthentication(proxyUser, proxyPasswd.toCharArray());
    }
  }

  public static class ClientUserProcess extends UserProcess
  {
    static private Any login__ = new ConstString("login");

    ClientUserProcess(ExceptionHandler exceptionHandler,
                      Transaction      transaction,
                      Map              root,
                      EventDispatcher  connectedDispatcher,
                      EventDispatcher  disconnectedDispatcher,
                      URL              initInq) throws AnyException
    {
      super(exceptionHandler,
            transaction,
            root,
            connectedDispatcher,
            disconnectedDispatcher,
            initInq);
    }

  	protected void initClient(EventDispatcher connectedDispatcher) throws AnyException
  	{
      super.initClient(connectedDispatcher);
      UpdateEventProcessor.setupNodeEventDispatcher(connectedDispatcher,
                                                    getTransaction(),
                                                    getRoot());

      // Set <code>Globals.process__ as a means
      // of synchronisation with the awt thread.
      Globals.process__ = this;
      Globals.awtSync__ = true;
      new RunInq();
  	}
  	
  	protected Any getStartEventType()
  	{
      return EventConstants.START_USERPROCESS;
  	}
  	
    protected void serverLost() throws AnyException
    {
      this.reset();
      
      // Iterate to find any windows and dispose of them
      //System.out.println("ClientUserProcess.ServerLost");
      if (SwingUtilities.isEventDispatchThread())
      {
        emptyWindows();
      }
      else
      {
        SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            try
            {
              emptyWindows();
            }
            catch(Exception ee)
            {
              throw new RuntimeContainedException(ee);
            }
          }
        });
      }
    }
    
    private void emptyWindows() throws AnyException
    {
      Map root = getRoot();
      DepthFirstIter d = new DepthFirstIter();
      d.setSkipNodeSetChildren(true);
      d.setVisitRoot(true);
      d.setCyclicSafe(true);
      Set s = AbstractComposite.set();

      Iter i = root.createKeysIterator();

      while (i.hasNext())
      {
        Any k = i.next();

        if (k.equals(login__))
          continue;

        d.reset(root.get(k));
        while (d.hasNext())
        {
          Any node = d.next();
          //System.out.println("Iter : " + node);
          if (node instanceof WindowF)
          {
            //System.out.println("Found : " + node);
            // Yuk - DepthFirstIter still not right when structure is cyclic
            if (!s.contains(node))
              s.add(node);
          }
        }
      }

      //System.out.println("1 Dispose : " + s);
      Iter w = s.createIterator();
      while (w.hasNext())
      {
        Composite c = (Composite)w.next();
        c.removeInParent();
        //System.out.println("2 Dispose : " + c);
      }
      //System.out.println("Dispose Done");
      
      // Invoke the serverLost scripted function
      Transaction t = this.getTransaction();
      try
      {
        AnyFuncHolder.FuncHolder f =
          (AnyFuncHolder.FuncHolder)AnyClient.instance().getServerLostHandler();

        // The handlers execute at $root (unless the func defines
        // a context)
        this.setContextPath(ServerConstants.NSROOT);
        if (f != null)
        {
          f.doFunc(t, null, this.getRoot());
          t.commit();
        }
      }
      catch(AnyException aex)
      {
        t.abort();
        //throw(aex);
      }
      catch(AnyRuntimeException rex)
      {
        t.abort();
        //throw(rex);
      }
      catch(Exception ex)
      {
        t.abort();
        //throw new RuntimeContainedException(ex);
      }
      finally
      {
        this.setContextPath(null);
        this.setContext(null);
      }
    }
  }
}
