/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/tools/Inqs.java $
 * $Author: sanderst $
 * $Revision: 1.10 $
 * $Date: 2011-04-09 20:16:04 $
 */
package com.inqwell.any.tools;

import java.io.IOException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import javax.net.ssl.SSLHandshakeException;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyString;
import com.inqwell.any.AnyURL;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.ConstString;
import com.inqwell.any.ContainedException;
import com.inqwell.any.Crypt;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.EventDispatcher;
import com.inqwell.any.EventListener;
import com.inqwell.any.Globals;
import com.inqwell.any.InqInterpreter;
import com.inqwell.any.InvokeService;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.RunInq;
import com.inqwell.any.SendRequest;
import com.inqwell.any.SimpleEvent;
import com.inqwell.any.StartProcessEvent;
import com.inqwell.any.SystemProperties;
import com.inqwell.any.UserProcess;
import com.inqwell.any.channel.AnyChannel;
import com.inqwell.any.channel.FIFO;
import com.inqwell.any.channel.OutputChannel;
import com.inqwell.any.channel.Socket;
import com.inqwell.any.client.ClientTransaction;
import com.inqwell.any.net.AnyCertificate;
import com.inqwell.any.net.InqStreamHandlerFactory;
import com.inqwell.any.server.LoginException;
import com.inqwell.any.util.CommandArgs;

public class Inqs
{

  private static Any url__        = new ConstString("url");
  private static Any in__         = new ConstString("in");
  private static Any u__          = new ConstString("u");
  private static Any p__          = new ConstString("p");
  private static Any serverHost__ = new ConstString("serverHost");
  private static Any server__     = new ConstString("server");

  public static void main (String args[]) throws LoginException,
																								 AnyException,
																								 MalformedURLException,
																								 IOException
  {
		// Set up globals
		Globals.channelOutputReplacements__ = com.inqwell.any.client.Replacements.clientToServer__;
		Globals.channelInputReplacements__  = com.inqwell.any.client.Replacements.clientFromServer__;
		Globals.streamOutputReplacements__  = com.inqwell.any.client.Replacements.clientToNativeStream__;
		Globals.streamInputReplacements__   = com.inqwell.any.client.Replacements.clientFromNativeStream__;
		Globals.interpreter__               = new InqInterpreter();

    if (args.length == 0)
      usage();

    CommandArgs cArgs = new CommandArgs(args);
    Map m = cArgs.toMap();

    // Source text inq script URL or local script must be supplied
    if (!m.contains(url__) && !m.contains(in__))
      usage();

    Any sourceText = null;
    if (m.contains(url__))
      sourceText = m.get(url__);

    // User login must be supplied
    if (!m.contains(u__))
      usage();
    Any userName = m.get(u__);

    // User passwd must be supplied
    if (!m.contains(p__))
      usage();
    Any pwd = m.get(p__);

    // User server host must be supplied
    if (!m.contains(serverHost__) && !m.contains(server__))
      usage();
    Any hostName = m.getIfContains(server__);
    if (hostName == null)
      hostName = m.getIfContains(serverHost__);

    InqStreamHandlerFactory.install();

    //execInq.exec(null);

    // set up an authenticator
    Authenticator.setDefault(new MyAuth());

		AnyChannel    ich = new AnyChannel(new FIFO());
		OutputChannel och = null;
		BooleanI      requestSession = null;
		Map           cert = null; // in case its an SSL connection
		Socket        cd = new Socket();
		try
		{
      cd.openURL(new URL(hostName.toString()), null);   //, certChain);
    }
    catch (ContainedException e)
    {
      if (e.getThrowable() instanceof SSLHandshakeException)
      {
        cert = (Map)cd.getInqURLConnection().getTrusted();
        System.out.println("Untrusted certificate received from " + hostName);
        System.out.println("Certificate: " + cert);
        System.out.print("Trust [y]es, [n]o, [a]lways ? ");
        System.out.flush();
        int c = System.in.read();
        c = Character.toLowerCase((char)c);
        if (c == 'a')
          cert.add(AnyCertificate.kPermanent__, AnyBoolean.TRUE);

        if (c == 'n')
          System.exit(0);

        cd.openURL(new URL(hostName.toString()), cert);
      }
      else
        throw e;
    }

	  //Socket        cd = new Socket(new URL(hostName.toString()));
		och = new AnyChannel(cd);

    if (cd.isKeepOpen())
    {
      requestSession = AnyBoolean.TRUE;
    }

    och.write(new StartProcessEvent(EventConstants.START_USERPROCESS,
                                    requestSession,
                                    userName));


    Map login = AbstractComposite.simpleMap();
    login.add(NodeSpecification.user__, userName);
    login.add(UserProcess.passwd__, Crypt.crypt(pwd));
    login.add(UserProcess.package__, RunInq.system__);
    login.add(SystemProperties.localhostname,
              SystemProperties.instance().getSystemProperties().get(SystemProperties.localhostname));
    och.write(new SimpleEvent(EventConstants.LOGIN_REQUEST, login));
    och.flushOutput();
		AnyChannel readInput = new AnyChannel(cd, ich);

    EventDispatcher ed = new EventDispatcher();
    ed.addEventListener(new LoginOK());
    ed.addEventListener(new ServiceInvoked());
    ed.addEventListener(new LoginDenied());
    ed.addEventListener(new ServerLost());

    // Login response
    Event resp = (Event)ich.read();
    ed.processEvent(resp);


    Map svcargs = AbstractComposite.map();
    AnyURL u = new AnyURL(sourceText);
    svcargs.add (new AnyString("source"), u);
    //svcargs.add(AnyURL.baseURLKey__, u);
    svcargs.add (new AnyString("close"), AnyBoolean.TRUE);
		SendRequest sr = new SendRequest(new AnyString("system.services.RunInq"),
																	   null,  // no context
																	   svcargs,
																	   och);
		sr.setPropagateContext(false);
    sr.setTransaction(new ClientTransaction());
		sr.exec(null);
    och.flushOutput();
    ((AnyChannel)och).startKeepAliveTimer(new UserProcess(Thread.currentThread(), AbstractComposite.simpleMap()));

//    if (cd.isClient() && cd.isKeepOpen())
//      och.startKeepOpenProbe();

    //


    try
    {

      // The server terminates the connection, so we get a server-lost
      // here.
      while(true)
      {
        resp = (Event)ich.read();
        ed.processEvent(resp);
      }
    }
    catch(Exception e)
    {
      // have to do this as the readInput channel starts its own
      // thread (or make that thread a daemon thread? )
			e.printStackTrace();
			System.exit(1);
    }
    System.exit(0);
	}

  static void usage ()
  {
    System.err.println ("Inqs: Usage");
    System.err.println ("      -url <Inq Server URL>");
    System.err.println ("      -u <user>");
    System.err.println ("      -p <passwd>");
    System.err.println ("      -server <server host URL>");
		System.exit(1);
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

  		String proxyUser = System.getProperty(proxyUser__);
		  if (proxyUser == null)
		    proxyUser = System.getProperty(httpProxyUser__);

  		String proxyPasswd = System.getProperty(proxyPasswd__);
		  if (proxyPasswd == null)
		    proxyPasswd = System.getProperty(httpProxyPasswd__);

      return new PasswordAuthentication(proxyUser, proxyPasswd.toCharArray());
    }
  }

	private static class ServiceInvoked extends    AbstractAny
                                      implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return InvokeService.eventTypes__;
    }
  }

	private static class LoginOK extends    AbstractAny
                               implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return UserProcess.loginOKEventTypes__;
    }
  }

	private static class LoginDenied extends    AbstractAny
                                   implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      System.err.println("Login denied: " + e.getContext());
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return UserProcess.loginDeniedEventTypes__;
    }
  }

	private static class ServerLost extends    AbstractAny
                                  implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      System.exit(0);
      return true; // not reached
    }

    public Array getDesiredEventTypes()
    {
      return UserProcess.serverLostEventTypes__;
    }
  }
}
