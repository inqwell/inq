/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/RunInq.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-09 18:18:26 $
 */
package com.inqwell.any;

/**
 * A pre-canned service for a client or a server which executes an
 * Inq script.
 * <p>
 * This service wraps up the <code>ExecInq</code> class providing
 * the means by which clients can run source texts of Inq script.
 * <p>
 * Hard-coded to run as syncgui so that context-established events are
 * synchronous.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class RunInq extends SyncGraphicsService
{

  public static Any servicePath__   = new ConstString("system.services.RunInq");
  public static Any source__        = new ConstString("source");
  public static Any closeComplete__ = new ConstString("close");
  public static Any level__         = new ConstString("level");
  
  public static Any system__        = AbstractValue.flyweightString("system.server");

  public RunInq() throws AnyException
  {
    init();
  }

  private void init() throws AnyException
  {
    addParam (source__.toString(), new AnyURL());
    addParam (AnyURL.baseURLKey__, new AnyURL());
    addParam (closeComplete__,     new AnyBoolean());

    Any expr = new ExecInq(new LocateNode
        (NodeSpecification.stack__.toString() +
            "." +
            source__.toString()),
         new LocateNode
           (NodeSpecification.stack__.toString() +
            "." +
            AnyURL.baseURLKey__.toString()),
         new LocateNode
           (NodeSpecification.stack__.toString() +
            "." +
            closeComplete__.toString()),
         new LocateNode
           (NodeSpecification.stack__.toString() +
            "." +
            level__.toString()));
    
    if (Globals.isServer())
    {
      Array a = AbstractComposite.array();
      a.add(new CheckAdmin());
      a.add(expr);
      
      Sequence s = new Sequence(a);
      
      expr = s;
    }
    
		setExpr(expr);

		Catalog.catalog(this, servicePath__.toString(), Transaction.NULL_TRANSACTION);
		setBaseURL(new ConstString("internal://RunInq"));
		setFQName(Interpreter.parser__);
	}
  
  static private class CheckAdmin extends    AbstractFunc
                                  implements Cloneable
  {
    public Any exec(Any a) throws AnyException
    {
      Process p = getTransaction().getProcess();
      Any pkg = p.getIfContains(UserProcess.package__);
      if (pkg == null || !pkg.equals(system__))
        throw new AnyException("Not admin");

      return null;
    }
  }
}
