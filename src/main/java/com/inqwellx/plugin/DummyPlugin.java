/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwellx.plugin;

import java.util.Map;

import com.inqwell.any.Any;
import com.inqwell.any.Plugin;
import com.inqwell.any.util.CommandArgs;

/**
 * A dummy implementation of the Plugin interface that can be used
 * as the base class for concrete implementations during testing.
 * All methods are overridden to do nothing (apart from write to
 * stout).
 * <p>
 * This class extends AbstractPlugin only so that the helper
 * method get(com.inqwell.any.Map map, String key) is accessible.
 */
public class DummyPlugin extends AbstractPlugin implements Plugin
{

  private int counter_ = 1;
  
  /**
   * Convert the given (command line) String array into a
   * map argument names to values
   */
  public static com.inqwell.any.Map commandArgs(String args[])
  {
    CommandArgs          cArgs   = new CommandArgs(args);
    com.inqwell.any.Map  argsMap = cArgs.toMap();
    return argsMap;
  }
  
  /**
   * Mimics the constructor from AbstractPlugin. Does nothing.
   * @param process
   * @param createF
   * @param modifyF
   */
  public DummyPlugin(Any process, Any createF, Any modifyF)
  {
    super();
  }
  
  /**
   * Default constructor. Does nothing.
   */
  public DummyPlugin()
  {
  }

  public void start(com.inqwell.any.Map argsMap)
  {
    System.out.println("DummyPlugin.start() called argsMap:" + argsMap);
  }

  public void stop()
  {
    System.out.println("DummyPlugin.stop() called");
  }
}
