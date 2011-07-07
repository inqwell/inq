/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

/**
 * Convenience location for the various visitors which support copying
 * between Any fundamental types.
 */
public abstract class AssignerVisitor extends AbstractVisitor
{
  // Helper method when data truncation occurs during assignment
  static protected void rangeError (Any    sourceType,
                             String val,
                             Any    targetType)
  {
    throw (new AnyRuntimeException
      ("Truncation occurred assigning " +
       sourceType +
       " value " +
       val +
       " to " +
       targetType));
  }
  
  static protected void rangeError (Any sourceType,
                             Any val,
                             Any targetType)
  {
    throw (new AnyRuntimeException
      ("Truncation occurred assigning " +
       sourceType +
       " value " +
       val +
       " to " +
       targetType));
  }
  
  //public abstract void fromString (String s);
  protected abstract void copy (Any from);
}
