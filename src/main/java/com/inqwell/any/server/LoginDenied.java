/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/LoginDenied.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.server;

import com.inqwell.any.Exec;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AnyException;
import com.inqwell.any.Catalog;
import com.inqwell.any.Any;
import com.inqwell.any.Locate;
import com.inqwell.any.LocateNode;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyString;
import com.inqwell.any.ConstString;
import com.inqwell.any.UserProcess;
import com.inqwell.any.Map;
import com.inqwell.any.Transaction;
import com.inqwell.any.Process;
import com.inqwell.any.UserProcess;
import com.inqwell.any.EventConstants;
import com.inqwell.any.Event;
import com.inqwell.any.SimpleEvent;
import com.inqwell.any.channel.OutputChannel;

/**
 * A The system level login service on the INQ server.
 * <p>
 * This service is executed on the server when any user logs in
 * as the <code>system</code> package.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class LoginDenied extends Exec
{

  private static Any functionPath__ = new ConstString("system.exprs.LoginDenied");

  public LoginDenied() throws AnyException
  {
    init();
  }

  private void init() throws AnyException
  {
    this.addParam(UserProcess.reason__, new AnyString());
		this.addParam(UserProcess.suspended__, null);  // account is suspended
		this.addParam(UserProcess.denied__, null);  // denied (no specific reason given)
		this.addParam(UserProcess.badpwd__, null);  // account is suspended
    setExpr(new LocalExec ());

		Catalog.catalog(this, functionPath__.toString(), Transaction.NULL_TRANSACTION);
		setBaseURL(new ConstString("internal://LoginDenied"));
		setFQName(new ConstString("system:LoginDenied"));
	}

	static private class LocalExec extends AbstractFunc implements Cloneable
	{
    public Any exec (Any a) throws AnyException
    {
      Map stackFrame = getTransaction().getCurrentStackFrame();
      Map m          = AbstractComposite.simpleMap();

      Any reason = stackFrame.get(UserProcess.reason__);
      m.add(UserProcess.reason__, reason);

      if (stackFrame.contains(UserProcess.badpwd__))
        m.add(UserProcess.badpwd__, AnyBoolean.TRUE);

      if (stackFrame.contains(UserProcess.denied__))
        m.add(UserProcess.denied__, AnyBoolean.TRUE);

      if (stackFrame.contains(UserProcess.suspended__))
        m.add(UserProcess.suspended__, AnyBoolean.TRUE);

      Event e = new SimpleEvent(EventConstants.LOGIN_DENIED, m);

      Map root = getTransaction().getProcess().getRoot();

      OutputChannel oc = (OutputChannel)root.get(LoginOK.ochannel__);

      oc.write(e);
      oc.flushOutput();

      // Under these curcumstances (we are going to kill the process)
      // commit the txn here in case the login handling actually did
      // something (like updating fail attempts counter, for example).
			getTransaction().commit();

      Process p = getTransaction().getProcess();
      p.kill(p);

      return e;
    }
    public Object clone () throws CloneNotSupportedException
    {
      return super.clone();
    }
	}
}


