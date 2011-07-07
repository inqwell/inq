/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ExceptionContainer.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Support the manipulation of exception containers in the Any framework.
 * The Any framework supports various exception classes based on
 * <code>AnyException</code> and <code>AnyRuntimeException</code>.  In
 * order to embrace exceptions from third-party libraries without
 * making the <code>throws</code> clause of any method (and hence its
 * callers) cumbersome we define <code>ContainedException</code> and
 * <code>RuntimeContainedException</code> which simply wrap up third-party
 * exceptions allowing them to pass through without explicit throws
 * declarations.
 * <p>
 * If these objects never leave the JVM in which they originated then
 * all will be well but if we serialize them to another environment
 * where the contained exception class is unavailable there will be an
 * unmarshalling exception.
 * <p>
 * This interface acts as a marker and provides a method to create a
 * new exception type where the destination of the exception might
 * not have the necessary classes to de-serialize that exception.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */ 
public interface ExceptionContainer
{
  public Any collapseException();
  
  public Throwable getThrowable();
  public void setThrowable(Throwable t);
}
