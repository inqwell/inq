/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;


/**
 * The exit function.  Behaviour differs on client and server:
 * <ul><li>On a client, closes our output channel and exits the VM.
 * The server receives the close notification and terminates the
 * associated user process. If there is an exit status the value is
 * passed to <code>System.exit()</code>, otherwise exits with a status
 * of zero.</li>
 * <li>On a server, places zero or supplied exit status
 * at <code>$process.status</code> and terminates the process by
 * closing its input channel. If the process was started
 * with <code>spawn</code> and there is an <code>end</code> function
 * then this will be called prior to termination</li></ul>.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class SetEventMask extends    AbstractFunc
                  implements Cloneable
{
  private static final long serialVersionUID = 1L;
  
  private Any node_;
  private Any eventTypes_;

  public SetEventMask(Any node, Any eventTypes)
  {
    node_       = node;
    eventTypes_ = eventTypes;
  }

  public Any exec(Any a) throws AnyException
  {
    InstanceHierarchyMap node  = 
        (InstanceHierarchyMap)EvalExpr.evalFunc(getTransaction(),
                                                a,
                                                node_,
                                                InstanceHierarchyMap.class);
    
    if (node == null)
      nullOperand(node_);

    Any eventTypes  = EvalExpr.evalFunc(getTransaction(),
                                        a,
                                        eventTypes_);

    if (eventTypes == null)
      nullOperand(eventTypes_);

    if (AnyNull.isNullInstance(eventTypes))
    {
      node.setEventMask(null);
      
      // When clearing the event mask raise an event on
      // the node to cause MVC to refresh
      Event e = node.makeEvent(EventConstants.NODE_REPLACED);
      node.fireEvent(e);
    }
    else
    {
      if (!(eventTypes instanceof Array))
        throw new AnyException("Not an array");
      
      node.setEventMask((Array)eventTypes);
    }
    
    return node;
  }

  public Object clone () throws CloneNotSupportedException
  {
    SetEventMask m = (SetEventMask)super.clone();

    m.node_       = AbstractAny.cloneOrNull(node_);
    m.eventTypes_ = AbstractAny.cloneOrNull(eventTypes_);

    return m;
  }
}
