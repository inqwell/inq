/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.connect;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyURL;
import com.inqwell.any.ConstString;
import com.inqwell.any.InqInterpreter;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.client.StackTransaction;

/**
 * Environment manager for the connect package.
 * @author tom
 * TODO: Speculative
 *
 */
public class Environment extends AbstractAny
{
  private static Environment theEnv__;
  
  static
  {
    theEnv__ = new Environment();
    theEnv__.init();
  }
  
  public static Environment getEnvironment()
  {
    return theEnv__;
  }
  
  private void init()
  {
    try
    {
      String inqHome = System.getProperty("inq.home");
      if (inqHome == null)
        throw new AnyRuntimeException("inq.home property is undefined");
      
      File f = new File(inqHome, "serverconnect.properties");
      
      Properties props = new Properties();
      FileInputStream fis = new FileInputStream(f);
      props.load(fis);
      
      String initInq = props.getProperty("initinq");
      if (initInq == null)
        throw new AnyRuntimeException("initinq property is undefined");
      
      AnyURL u = new AnyURL(initInq);
      URL url = u.getURL(new ConstString(new File(inqHome).toURI().toURL().toString()));
      u.setValue(url);
      
      InqInterpreter intr = new InqInterpreter();
      intr.run(AbstractComposite.simpleMap(),
               u,
               new StackTransaction(), url.openStream());
    }
    catch(IOException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
}
