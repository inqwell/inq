/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwellx;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.Call;
import com.inqwell.any.LocateNode;
import com.inqwell.any.Map;

/**
 * This class is an example configurator for systems requiring host database
 * credentials and integration plugins. It is also the default when no
 * override is provided with the <code>-configurator</code> command line
 * option to the server.
 * <p>
 * A "configurator" is a class containing static methods to provide
 * information about the environment to an Inq application. The static
 * methods herein may be called from Inq script as and when the
 * information is required.
 * <p>
 * A system may require such things as database credentials to be stored
 * securely. Integrators may wish to implement this class to silently
 * decrypt and return passwords, for example. 
 * @author tom
 *
 */
public class Configurator
{
  static private Any which__      = AbstractValue.flyweightString("which");

  static private Any xylinq__     = AbstractValue.flyweightString("xylinq");
  static private Any inq__        = AbstractValue.flyweightString("inq");

  static private Any xyuser__     = AbstractValue.flyweightString("xy1");
  static private Any xypassword__ = AbstractValue.flyweightString("xy1");
  static private Any xyurl__      = AbstractValue.flyweightString("jdbc:mysql://localhost/xydev?verifyServerCertificate=false&useSSL=false&requireSSL=false");
  
  static private Any inquser__     = AbstractValue.flyweightString("inq");
  static private Any inqpassword__ = AbstractValue.flyweightString("inq123");
  static private Any inqurl__      = AbstractValue.flyweightString("jdbc:mysql://localhost/inq?verifyServerCertificate=false&useSSL=false&requireSSL=false");
  
  static private Map mXylinqDb__;
  static private Map mInqDb__;
  
  static
  {
    mXylinqDb__ = Helpers.makeInqMap();
    mXylinqDb__.add(Helpers.USER, xyuser__);
    mXylinqDb__.add(Helpers.PASSWORD, xypassword__);
    mXylinqDb__.add(Helpers.URL, xyurl__);
    
    mInqDb__ = Helpers.makeInqMap();
    mInqDb__.add(Helpers.USER, inquser__);
    mInqDb__.add(Helpers.PASSWORD, inqpassword__);
    mInqDb__.add(Helpers.URL, inqurl__);
  }

  /**
   * Returns a map whose keys are plugin names and values are
   * the fully-qualified class names of {@link com.inqwellx.plugin.AbstractPlugin} extensions.
   * The return type must be a {@link com.inqwell.any.Map} and
   * classes may use {@link com.inqwellx.Helpers#makeInqMap()}
   * and {@link com.inqwellx.Helpers#convertToInqMap(java.util.Map)} as appropriate.
   * @param argsMap Inq environment's command line arguments.
   * @return a {@link com.inqwell.any.Map} describing the plugins
   * or null if there are no plugins configured.
   */
  static public Any getPlugins(Map argsMap)
  {
    return null;
  }
  
  /**
   * Returns a map describing a database connection, consisting of
   * a user name, password and url. The map keys must
   * be {@link Helpers#USER}, {@link Helpers#PASSWORD}
   * and {@link Helpers#URL}. The values are strings to be used
   * in the JDBC connection.
   * @param argsMap
   * Inq environment's command line arguments.
   * @param id
   * the identifier of the database connection being requested,
   * for example, <code>"xylinq"</code>.
   * @return
   */
  static public Any getDatabaseLogin(Map argsMap, Any id)
  {
//    if (id.equals(xylinq__))
//      return mXylinqDb__;
//    else if (id.equals(inq__))
//      return mInqDb__;
    
    return getDbLogin(id);
  }
  
  /**
   * Returns the database administrative password. This method need
   * not be implemented if there is no need to provide privileged
   * database access
   */
  static public Any getDbPwd()
  {
  	return getPwd();
  }
    
  static private Any getPwd()
  {
    Call fetchDbPwd = new Call(new LocateNode("$catalog.xy.test.exprs.fetchDbPwd"));

		Any ret = Call.call(fetchDbPwd, null);

		return ret;
	}

  static private Any getDbLogin(Any which)
  {
    Call fetchLogins = new Call(new LocateNode("$catalog.xy.test.exprs.fetchLogins"));

		Map args = AbstractComposite.simpleMap();
		args.add(which__, which);

		Any ret = Call.call(fetchLogins, args);

		return ret;
	}

  /**
   * Returns a JMS Connection Factory object. This (default) implementation
   * uses the Sun GlassFish Message Queue 4.4 specific connection factory
   * and does not require JNDI.
   * @param args
   * @return
  static public Any getJMSConnectionFactory(Map args)
  {
    return new AnyConnectionFactory(new com.sun.messaging.ConnectionFactory());
  }
   */
}
