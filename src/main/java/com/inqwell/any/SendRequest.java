/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/SendRequest.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import com.inqwell.any.channel.OutputChannel;

/**
 * Forms a service request with an optional response specification
 * and sends it to the specified channel.
 * <p>
 * This class may be used in part of an expression or as an event listener.
 * When acting as an event listener all references to nodes in the
 * in the 'owning' structure must be already resolved when the
 * event is processed since it may be being received from another
 * thread.  The method <code>eventReady()</code> should
 * be called before attaching to a generator to do this.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.6 $
 */
public class SendRequest extends    AbstractFunc
												 implements EventListener,
																		Cloneable
{
  private Any serviceName_;
  private Any context_;
  private Any args_;
  private Any outputChannel_;
  
  // if true (default) and there's no context supplied
  // then propagate the one we are running in
  private boolean propagateContext_ = true;
  
  // For events
  // not null when set up ready for use in preparation for
  // event processing
	private Array         eventTypes_;
  private Map           argsMap_;
  private OutputChannel eventChannel_;
	private boolean       consume_ = false;
	
	public static Event makeRequestEvent(Any serviceName,
																			 Any context,
																			 Any saveAt,
																			 Map args,
																			 Any response)
	{
    return makeRequestEvent(EventConstants.INVOKE_SVC,
                            serviceName,
                            context,
                            saveAt,
                            args,
                            response);
	}
  
	public static Event makeRequestEvent(Any eventType,
                                       Any serviceName,
																			 Any context,
																			 Any saveAt,
																			 Map args,
																			 Any response)
	{

    Map toSend = AbstractComposite.map();
    
    if (serviceName != null)
			toSend.add(ServerConstants.SVCEXEC, serviceName);

		if (args != null)
		{
			toSend.add(ServerConstants.SVCINAR, args);
		}

		if (context != null)
			toSend.add(ServerConstants.SVCCTXT, context);
    
		return new SimpleEvent(eventType, toSend);
	}
	
  public SendRequest(Any serviceName,
										 Any context,
										 Any args,
										 Any outputChannel)
  {
    serviceName_        = serviceName;
    context_            = context;
    args_               = args;
    outputChannel_      = outputChannel;
  }

  public SendRequest(Any serviceName,
                     Any args,
                     Any outputChannel)
  {
    this(serviceName, null, args, outputChannel);
  }
  
  /**
   * Create the service request structure and send it to the channel
   * operand.  Return original structure
   */
  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
		StringI serviceName       = (StringI)EvalExpr.evalFunc
																									(t,
		                                               a,
		                                               serviceName_,
		                                               StringI.class);

		Any        context          = EvalExpr.evalFunc
																									(t,
		                                               a,
		                                               context_);
    if (context == null && context_ != null)
      nullOperand(context_);
    
		if (context == null && propagateContext_)
			context = t.getContextPath();

		Map        args             = (Map)EvalExpr.evalFunc
																									(t,
		                                               a,
		                                               args_,
		                                               Map.class);
		//System.out.println ("args = " + args);

		//System.out.println ("OutputChannel_: " + outputChannel_);
		//System.out.println ("Evaluating against: " + a);
		//System.out.println ("XAction : " + getTransaction());
		
		OutputChannel outputChannel = (OutputChannel)EvalExpr.evalFunc
																									(t,
		                                               a,
		                                               outputChannel_,
		                                               OutputChannel.class);
		                                               
		//System.out.println ("OutputChannel: " + outputChannel);
		if (outputChannel == null)
		  nullOperand(outputChannel_);
		  
		

    outputChannel.write(makeRequestEvent(serviceName,
																				 context,
																				 null, //saveAt,
																				 resolveArgs(args, a, t),
																				 null));
		outputChannel.flushOutput();
    
		return null;
  }
  
  /**
	 * Send the service request represented by this object as a result of
	 * event processing.
	 * <p>
	 * Events may propagate from one thread A to another B when both threads
	 * are observers of the same object.  When thread A raises an event
	 * on a shared object 
	 */
	public boolean processEvent(Event e) throws AnyException
  {
    if (eventChannel_.isClosed())
      return false;
    
		Map argsMap = argsMap_;
		
		if (argsMap == null)
			argsMap = AbstractComposite.simpleMap();
    else
      argsMap = (Map)argsMap.cloneAny();

		//System.out.println ("SendRequest eventTypes " + eventTypes_);
		//System.out.println ("SendRequest.processEvent() " + serviceName_ + " " + argsMap);

		argsMap.add (EventConstants.EVENT_ID, e.getId());
		argsMap.add (EventConstants.EVENT, e);

		Any ec = e.getContext();
		if (ec != null)
		{
			// assume that the event context is a map and make this the argsMap_
			argsMap.add(EventConstants.EVENT_CONTEXT, ec);
		}
    
    Event request = makeRequestEvent(serviceName_,
                                     context_,
                                     null,  // saveAt_,
                                     argsMap,
                                     null);

    //System.out.println ("SendRequest.processEvent() " + serviceName_ + " " + argsMap);
    //request.add(ServerConstants.SVCEVNT, e);
    eventChannel_.write(request);
    eventChannel_.flushOutput();

    //argsMap.remove(EventConstants.EVENT_ID);
    
//		if (ec != null)
//			argsMap.remove(EventConstants.EVENT_CONTEXT);
      
    if (consume_)
      e.consume();

    return true;
  }
  
  public void setArgs(Any args)
  {
  	args_ = args;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
		SendRequest sr = (SendRequest)super.clone();
		
    sr.serviceName_   = serviceName_.cloneAny();
    sr.context_       = AbstractAny.cloneOrNull(context_);
    sr.args_          = AbstractAny.cloneOrNull(args_);
    sr.outputChannel_ = outputChannel_.cloneAny();
    
    return sr;
  }

  public Array getDesiredEventTypes()
  {
		return eventTypes_;
  }
  
  // Resolve the arguments being sent
  public Map resolveArgs(Map args, Any root, Transaction t) throws AnyException
  {
    Map ret = null;
		if (args != null)
		{
      ret = AbstractComposite.simpleMap();
      
			Iter i = args.createKeysIterator();
			while (i.hasNext())
			{
				Any argkey = i.next();
				Any argval = args.get(argkey);
				Any newArgval = EvalExpr.evalFunc (getTransaction(),
                                           root,
                                           argval);
        
        newArgval = AbstractAny.ripSafe(newArgval, getTransaction());
        
        newArgval = t.readProperty(newArgval);
        
        ret.add(argkey, newArgval);
			}
		}
		//System.out.println ("args = " + args);
		return ret;
  }
  
  /**
   * Resolve all operands in advance of calling the <code>exec</code>
   * method so that when the <code>exec</code> is called it is
   * thread-safe.  Note that once this method has been called all
   * original operands which were <code>LocateNode</code> references
   * become normal references so the instance can never be re-used
   * at a different context structure.
   */
  public void resolveOperands(Any a) throws AnyException
  {
		serviceName_      = (StringI)EvalExpr.evalFunc
																							 (getTransaction(),
																								a,
																								serviceName_,
																								StringI.class);

		context_          =            EvalExpr.evalFunc
																							 (getTransaction(),
																								a,
																								context_);
		if (context_ == null && propagateContext_)
			context_ = getTransaction().getContextPath();


		// If this instance is going to be used in the local JVM then
		// we are better not to resolve the individual arguments as this
		// can be done on the receiving end, to get the currently prevailing
		// values from the context.
		args_             = (Map)EvalExpr.evalFunc (getTransaction(),
																								a,
																								args_,
																								Map.class);

	  outputChannel_    = (OutputChannel)EvalExpr.evalFunc
																									(getTransaction(),
		                                               a,
		                                               outputChannel_,
		                                               OutputChannel.class);
  }
  
  /**
	 * Set things up so that we are ready to process events with
	 * thread safety.  This method should be called prior to
	 * connecting to any event generators.
	 */
  public void eventReady(Process p) throws AnyException
  {
	  setTransaction(p.getTransaction());
		resolveOperands(p.getContext());
		
		eventChannel_ = (OutputChannel)outputChannel_;
		argsMap_      = (Map)args_;
		argsMap_ = resolveArgs(argsMap_, p.getContext(), p.getTransaction());
    args_    = null; // gc
		setTransaction(Transaction.NULL_TRANSACTION);
  }
  
  /**
	 * Provided for <code>AnyComponents</code> to set their context into
	 * this object as an <code>EventListener</code>
	 */
  public void setContext(Any context)
  {
		context_ = context;
	}
  
  public void setPropagateContext(boolean propagateContext)
  {
		propagateContext_ = propagateContext;
	}
  
  public void setConsume(boolean consume)
  {
		consume_ = consume;
	}
  
  public void addEventType(Any eventType)
  {
		//System.out.println ("SendRequest.addEventType " + eventType);
		
		if (eventType == EventConstants.DEFAULT_TYPE)
			eventTypes_ = EventConstants.DEFAULT_TYPE;
		else
		{
			if (eventTypes_ == null)
				eventTypes_ = AbstractComposite.array();
			eventTypes_.add(eventType);
		}
  }
  
  public void cloneEventTypes()
  {
    eventTypes_ = (Array)eventTypes_.cloneAny();
  }
  
  public String toString()
  {
		return getClass().toString() + super.toString() + eventTypes_;
  }
}
