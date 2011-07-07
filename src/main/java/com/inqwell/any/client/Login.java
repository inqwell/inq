/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/Login.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.channel.OutputChannel;

/**
 * Send a login details event to the local process.
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class Login extends    AbstractFunc
                   implements Cloneable
{
	private Any usr_;
	private Any pwd_;
	private Any pkg_;
	private Any url_;
	private Any srv_;
	private Any cert_;
	private Any ignoreExp_;
	private Any exit_;

	public Login (Any usr,
                Any pwd,
                Any pkg,
                Any url,
                Any srv,
                Any cert,
                Any ignoreExp,
                Any exit)
	{
		usr_        = usr;
		pwd_        = pwd;
		pkg_        = pkg;
		url_        = url;
		srv_        = srv;
		cert_       = cert;
		ignoreExp_  = ignoreExp;
		exit_       = exit;
	}

	/**
	 *
	 */
	public Any exec(Any a) throws AnyException
	{
		StringI usr  = (StringI)EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   usr_,
		                                   StringI.class);

		StringI pwd  = (StringI)EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   pwd_,
		                                   StringI.class);

		StringI pkg  = (StringI)EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   pkg_,
		                                   StringI.class);

		StringI url  = (StringI)EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   url_,
		                                   StringI.class);

		StringI srv  = (StringI)EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   srv_,
		                                   StringI.class);

		Map     cert = (Map)EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   cert_,
		                                   Map.class);

		BooleanI ignoreExp = (BooleanI)EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   ignoreExp_,
		                                   BooleanI.class);

		BooleanI exit = (BooleanI)EvalExpr.evalFunc
																		(getTransaction(),
		                                   a,
		                                   exit_,
		                                   BooleanI.class);

    LocateNode l = new LocateNode(ServerConstants.RICHANNEL);

		OutputChannel och  = (OutputChannel)EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   l,
		                                   OutputChannel.class);

    if (usr == null)
      throw new AnyException("User name not specified");

    if (pwd == null)
      throw new AnyException("Password not specified");

    if (pkg == null)
      throw new AnyException("Package not specified");

    //if (url == null)
      //throw new AnyException("Script URL not specified");

    if (srv == null)
      throw new AnyException("Server host not specified");

    if (och == null)
      throw new AnyException("Could not resolve Input Channel");

    Map m = AbstractComposite.simpleMap();
    m.add(NodeSpecification.user__, new ConstString(usr.toString()));
    m.add(UserProcess.passwd__, new ConstString(pwd.toString()));
    m.add(UserProcess.package__, new ConstString(pkg.toString()));
    if (url != null)
      m.add(UserProcess.url__, new ConstString(url.toString()));
    m.add(UserProcess.host__, new ConstString(srv.toString()));
    if (cert != null)
      m.add(UserProcess.cert__, cert);
    if (ignoreExp != null)
      m.add(UserProcess.ignoreExpiring__, ignoreExp);

    m.add(SystemProperties.localhostname,
          SystemProperties.instance().getSystemProperties().get(SystemProperties.localhostname));

    m.add(UserProcess.exit__, exit);

    Event e = new SimpleEvent(EventConstants.LOGIN_DETAILS,
                              m);
    och.write(e);

	  return null;
	}

  public Object clone() throws CloneNotSupportedException
  {
  	Login l = (Login)super.clone();

  	l.usr_  = usr_.cloneAny();
		l.pwd_  = pwd_.cloneAny();
		l.pkg_  = pkg_.cloneAny();
		l.url_  = url_.cloneAny();
		l.srv_  = srv_.cloneAny();

    return l;
  }
}
