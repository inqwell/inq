/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import com.inqwell.any.Any;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.Array;

public interface MapMessageI extends MessageI
{
  /**
   * Indicates whether an item exists in this MapMessage object.
   * @param name
   * @return <code>true</code> if the item exists
   * @throws AnyRuntimeException
   * Any exception that occurs in the underlying JMS provider
   * implementation is thrown as a {@link AnyRuntimeException}
   */
  public boolean itemExists(Any name);
  
  /**
   * Get the named item according to the supplied type. The
   * <code>type</code> argument is used to resolve the underlying type
   * and takes its value from the appropriate method on
   * <code>javax.jms.MapMessage</code>, for example
   * <code>getDouble(String name)</code>.
   * 
   * @param name
   *          the name of the item
   * @param type
   *          the value to which the item is assigned. This value is also
   *          returned.
   * @return the <code>type</code>argument.
   * @throws AnyRuntimeException
   * Any exception that occurs in the underlying JMS provider
   * implementation is thrown as a {@link AnyRuntimeException}
   */
  public Any get(Any name, Any type);
  
  /**
   * Set a named item according to the supplied type. The
   * <code>type</code> argument is used to resolve the underlying property type
   * to determine the method to call on
   * <code>javax.jms.MapMessage</code>, for example
   * <code>setDouble(String name, double value)</code>.
   * 
   * @param name
   *          the name of the item
   * @param type
   *          the value to which the item is set.
   * @throws AnyRuntimeException
   * Any exception that occurs in the underlying JMS provider
   * implementation is thrown as a {@link AnyRuntimeException}
   */
  public void set(Any name, Any type);
  
  /**
   * Returns an Array containing all the names in the underlying MapMessage
   * object. 
   * @return the array of names - zero length should the message contain
   * no items
   * @throws AnyRuntimeException
   * Any exception that occurs in the underlying JMS provider
   * implementation is thrown as a {@link AnyRuntimeException}
   */
  public Array getNames();
}
