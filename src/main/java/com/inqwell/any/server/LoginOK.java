/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/LoginOK.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.server;

import com.inqwell.any.AnyTimeZone;
import com.inqwell.any.Exec;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Map;
import com.inqwell.any.UserProcess;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.AnyException;
import com.inqwell.any.Catalog;
import com.inqwell.any.Any;
import com.inqwell.any.Process;
import com.inqwell.any.ConstString;
import com.inqwell.any.ConstShort;
import com.inqwell.any.ShortI;
import com.inqwell.any.AnyShort;
import com.inqwell.any.AnyURL;
import com.inqwell.any.Map;
import com.inqwell.any.Func;
import com.inqwell.any.Transaction;
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
 * @version $Revision: 1.4 $
 */
public class LoginOK extends Exec
{

  private static Any functionPath__ = new ConstString("system.exprs.LoginOK");
  public  static Any ochannel__     = new ConstString("ochannel");

  public LoginOK() throws AnyException
  {
    init();
  }

  private void init() throws AnyException
  {
    setExpr(new LocalExec());
		Catalog.catalog(this, functionPath__.toString(), Transaction.NULL_TRANSACTION);
    // By leaving baseURL_ blank we leave the caller's URL in the
    // transaction. This means we can use it to resolve relative URLs
    // passed to us.
		//setBaseURL(new ConstString("internal://LoginOK"));
		setFQName(new ConstString("system:LoginOK"));
		this.addParam(UserProcess.privLevel__, new AnyShort(Process.DEFAULT_PRIVILEGE));
		this.addParam(UserProcess.url__, null);
		this.addParam(UserProcess.expired__, null);
		this.addParam(UserProcess.expiresIn__, null);
	}

	static private class LocalExec extends AbstractFunc implements Cloneable
	{
    public Any exec (Any a) throws AnyException
    {
      Map stackFrame = getTransaction().getCurrentStackFrame();
      Map serverResp = AbstractComposite.simpleMap();

      ShortI privLevel = (ShortI)stackFrame.get(UserProcess.privLevel__);
      privLevel = new ConstShort(privLevel);

      boolean b = stackFrame.contains(UserProcess.url__);

      if (stackFrame.contains(UserProcess.expired__))
      {
        b = false;
        serverResp.add(UserProcess.expired__, stackFrame.get(UserProcess.expired__));
      }

      if (stackFrame.contains(UserProcess.expiresIn__))
      {
        b = false;
        serverResp.add(UserProcess.expiresIn__, stackFrame.get(UserProcess.expiresIn__));
      }

      getTransaction().getProcess().setRealPrivilegeLevel(privLevel.getValue());
      getTransaction().getProcess().setEffectivePrivilegeLevel(privLevel.getValue());

      serverResp.add(UserProcess.privLevel__, privLevel);
      if (b)
        serverResp.add(UserProcess.url__, AnyBoolean.TRUE);

      Process p = getTransaction().getProcess();
      
      serverResp.add(AnyTimeZone.timezone__, Catalog.instance().getCatalog().get(AnyTimeZone.timezone__));
      serverResp.add(UserProcess.loginName__, p.get(UserProcess.loginName__));
      serverResp.add(UserProcess.package__, p.get(UserProcess.package__));
      
      Event e = new SimpleEvent(EventConstants.LOGIN_OK);
      e.setContext(serverResp);

      Map root = getTransaction().getProcess().getRoot();

      OutputChannel oc = (OutputChannel)root.get(ochannel__);

      oc.write(e);
      oc.flushOutput();

      // If there was a URL to send it means that the server has the
      // responsibility of initialising the client rather tham
      // the client doing it itself. Send necessary service sequence.
      if (b)
      {
        // Send all descriptors
        Func f = new LoadDescriptors.FindDescriptors();
        f.setTransaction(getTransaction());
        f.exec(a);

        // Send initial script
        Any u = getTransaction().getCurrentStackFrame().get(UserProcess.url__);
        CompileInq.sendSource(new AnyURL(u),
                              new AnyURL(getTransaction().getExecURL()),
                              null,
                              a,
                              getTransaction(),
                              false);
      }

      return e;
    }
    public Object clone () throws CloneNotSupportedException
    {
      return super.clone();
    }
	}
}


