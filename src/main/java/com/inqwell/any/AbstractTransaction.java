/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AbstractTransaction.java $
 * $Author: sanderst $
 * $Revision: 1.8 $
 * $Date: 2011-05-07 16:53:31 $
 */
 
package com.inqwell.any;

import com.inqwell.any.client.AnyAttributeSet;
import com.inqwell.any.client.AnyColor;
import com.inqwell.any.client.AnyFont;
import com.inqwell.any.client.AnyIcon;
import com.inqwell.any.jms.SessionI;

/**
 * Common transaction class functionality.
 */
public abstract class AbstractTransaction extends    AbstractAny
                                          implements Transaction
{
	Iter currentIter_ = null;

  private int       resolving_;
  
  // The transactional container most recently referenced.
  private Map       tMap_;
	
  // The field of the transactional container most recently referenced.
  private Any       tField_;
	
	private Set       beforeEvents_;
	private Set       afterEvents_;
  
  private Any       loop_;
  
  // We may have a messaging session being managed by
  // transaction. Only set into the root transaction.
  private SessionI  session_;
  private boolean   mqDirty_;
  
  // Whether this transaction commits the mq session. If no
  // nested transaction commits the session then the root
  // one will
  private boolean   mqcommit_;
  
  // The typedefs we are supplying the old values of
  // during event notification
  private Map       notifyOld_;
  
  // Our temporary variables. This list should match those
  // that bean property types can be translated between by
  // PropertySet. In particular, the various class types
  // such as AnyColor, that are all Maps to Inq, are tested
  // for in GetTemporary.visitMap.
  private BooleanI        boolean_ = new AnyBoolean();
  private ByteI           byte_    = new AnyByte();
  private CharI           char_    = new AnyChar();
  private IntI            int_     = new AnyInt();
  private ShortI          short_   = new AnyShort();
  private LongI           long_    = new AnyLong();
  private FloatI          float_   = new AnyFloat();
  private DoubleI         double_  = new AnyDouble();
  private Decimal         decimal_ = new AnyBigDecimal();
  private StringI         string_  = new AnyString();
  private DateI           date_    = new AnyDate();
  private AnyIcon         icon_    = new AnyIcon();
  private AnyColor        color_   = new AnyColor();
  private AnyFont         font_    = new AnyFont();
  private AnyAttributeSet attrSet_ = new AnyAttributeSet();
  
  // And how to get them
  private GetTemporary getTemporary_;
  
  // Something to perform read operations on PropertyBindings
  private ReadProperty readProperty_ = new ReadProperty();

  public AbstractTransaction()
  {
    init();
  }
	
	public Iter setIter(Iter i)
	{
		Iter ret = currentIter_;
		currentIter_ = i;
		return ret;
	}
	
	public Iter getIter()
	{
		return currentIter_;
	}
  
  public Any setLoop(Any loop)
  {
    Any ret = loop_;
    loop_ = loop;
    return ret;
  }

  public Any getLoop()
  {
    return loop_;
  }
  
  public void setMqSession(SessionI session)
  {
    // Only the root transaction has a session
    Transaction parent = getParent();
    
    if (parent != null)
      parent.setMqSession(session);
    else
    {
      if (session_ != null && session != null)
        throw new IllegalStateException("A messaging session is already established");
      
      if (session_ != null && session == null)
        session_.close();
    
      session_ = session;
      mqDirty(false);
    }
  }
  
  public SessionI getMqSession()
  {
    // Only the root transaction has a session
    Transaction parent = getParent();
    
    if (parent != null)
      return parent.getMqSession();
    else
    {
      return session_;
    }
  }
  
  public void setMqCommit(boolean commit)
  {
    mqcommit_ = commit;
  }
  
  public void mqDirty(boolean dirty)
  {
    Transaction t = getRootTransaction();
    
    if (t == this)
      mqDirty_ = dirty;
    else
      t.mqDirty(dirty);
  }
  
  public boolean isMqDirty()
  {
    Transaction t = getRootTransaction();
    
    if (t == this)
      return mqDirty_;
    else
      return t.isMqDirty();
  }
  
  public Any getTemporary(Any a)
  {
    if (getTemporary_ == null)
      getTemporary_ = new GetTemporary();
        
    return getTemporary_.getTemporary(a);
  }
  
  public Any readProperty(Any a)
  {
    if (a != null)
    {
      a.accept(readProperty_);
      return readProperty_.getAny();
    }
    return null;
  }

  public void addAction(Func f, int when)
	{
    // NB - we only add an action once (obviously, qv what equals means
    // for any particular action).
		if (when == Transaction.BEFORE_EVENTS)
    {
      if (!beforeEvents_.contains(f))
		    beforeEvents_.add(f);
    }
		else
    {
      if (!afterEvents_.contains(f))
        afterEvents_.add(f);
    }
	}
	
	public boolean canCommit() throws AnyException { return true; }
	
  public boolean isAutoCommit() { return true; }

	public void copyOnWrite (Map m) throws AnyException {}

	public void createIntent (Map m, Any eventData) throws AnyException {}

	public void export(Map m, Transaction t) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
  
  public void fieldChanging(Map m, Any f, Any info) {}
  
  public boolean isActive()
  {
    throw new UnsupportedOperationException();
  }
  
  public void purgeKey(KeyDef kd) {}

  
  public Transaction getParent()
  {
    throw new UnsupportedOperationException();
  }
  
  public void setParent(Transaction t)
  {
    throw new UnsupportedOperationException();
  }
  
  public void setChild(Transaction t)
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean lock(Any a, long timeout) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
  
  public void exportEvents(Array eventBudle, Transaction t)
  {
    throw new UnsupportedOperationException();
  }
  
  public void unlock(Any a) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

  public boolean isDeleteMarked(Map m)
  {
    throw new UnsupportedOperationException();
  }
  
  public Map isCreateMarked(Map m) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
  
  public Map getCreateList(Descriptor d)
  {
    throw new UnsupportedOperationException();
  }
  
  public Map getModifyList(Descriptor d)
  {
    throw new UnsupportedOperationException();
  }
  
  public Map getDeleteList(Descriptor d)
  {
    throw new UnsupportedOperationException();
  }
  
  public Any acquireResource(Any               spec,
                             ResourceAllocator allocator,
                             long              timeout) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
  
  public void setLineNumber(int line)
  {
    Process p = getProcess();
    if (p != null)
    {
    	//System.out.println("setLineMumber " + line);
    	//AbstractAny.stackTrace();
      p.setLineNumber(line);
    }
  }
  
  public void setIdentity(Descriptor d, Func f)
  {
    throw new UnsupportedOperationException();
  }
  
  public void setColumn(int col)
  {
    Process p = getProcess();
    if (p != null)
      p.setColumn(col);
  }

  public int  getLineNumber()
  {
    Process p = getProcess();
    if (p != null)
    {
    	//System.out.println("getLineMumber " + p.getLineNumber());
    	//AbstractAny.stackTrace();
      return p.getLineNumber();
    }
    else
      return -1;
  }
  
  public int  getColumn()
  {
    Process p = getProcess();
    if (p != null)
      return p.getColumn();
    else
      return -1;
  }
  
  public Any  getExecURL()
  {
    Process p = getProcess();
    if (p != null)
      return p.getExecURL();
    else
      return null;
  }
  
  public void setExecURL(Any execUrl)
  {
    Process p = getProcess();
    if (p != null)
      p.setExecURL(execUrl);
  }
  
  public void setResolving(int resolving)
  {
    resolving_ = resolving;
  }
  
  public int getResolving()
  {
    return resolving_;
  }
  
  public void setLastTMap(Map m)
  {
    tMap_ = m;
  }
  
  public void setNotifyOld(Descriptor d, boolean notify)
  {
    if (notify && notifyOld_ == null)
      notifyOld_ = AbstractComposite.simpleMap();
    
    if (notify)
    {
      if (!notifyOld_.contains(d))
        notifyOld_.add(d, AbstractComposite.simpleMap());
    }
    else
    {
      if (notifyOld_ != null)
        notifyOld_.remove(d);
    }
  }
  
  public void addEvent(Event e)
  {
    throw new UnsupportedOperationException();
  }
  
  public void setGatherEvents(boolean gather)
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean isGatheringEvents()
  {
    return false;
  }
  
  public void setLastTField(Any a)
  {
    tField_ = a;
  }
  
  public Map getLastTMap()
  {
    return tMap_;
  }
  
  public Any getLastTField()
  {
    return tField_;
  }
  
  public void resetResolving()
  {
    tMap_      = null;
    tField_    = null;
    resolving_ = R_NOTHING;
  }
  
  public Stack getCallStack()
  {
    Process p = getProcess();
    if (p != null)
      return p.getCallStack();
    else
      return null;
  }

  public void checkPrivilege(Any access, Map node, Any key)
  {
    if (node == null)
      return;
    
    // If there is no process in the transaction then assume
    // OK.  This can happen when a StackTransaction is used
    // in such contexts as eligibility and existance expressions
    Process p = getProcess();
    if (p == null)
      return;
    
    Descriptor d = node.getDescriptor();
    if (d != Descriptor.degenerateDescriptor__)
    {
      // Refer to descriptor when there is one
      d.checkPrivilege(p, access, key);
      return;
    }
    
    short nodePrivilege = node.getPrivilegeLevel(access, key);
    short procPrivilege = p.getEffectivePrivilegeLevel();
    
    if (procPrivilege > nodePrivilege)
      throw new AnyRuntimeException("Insufficient privilege for requested operation");
  }

  protected void execBeforeActions() throws AnyException
  {
		execActions(beforeEvents_);
  }
  
  protected void execAfterActions() throws AnyException
  {
		execActions(afterEvents_);
  }
  
	private void execActions(Set funcs) throws AnyException
	{
    try
    {
      Iter i = funcs.createIterator();
      while (i.hasNext())
      {
        Func f = (Func)i.next();
        f.setTransaction(this);
        //System.out.println (getContext());
        f.exec(getContext());
      }
    }
    finally
    {
      funcs.empty();
    }
	}
	
	protected final void reset()
	{
    doReset();
    doResetRememberOld();
  }
	
  protected void doReset()
  {
	  beforeEvents_.empty();
	  afterEvents_.empty();
    setResolving(R_NOTHING);
    setLastTMap(null);
    setLastTField(null);
    //setLoop(null);
    mqDirty(false);
  }
  
  protected void mqCommit()
  {
    // A message session is only set into the root transaction (in the
    // present implementation at least) so only commit it when the root
    // transaction commits. Similarly rollback.
    Transaction t = getRootTransaction();

    if ((t == this || mqcommit_) && t.isMqDirty())
    {
      SessionI session = t.getMqSession();
      
      if (session != null)
      {
        // Unfortunately there is no way to check if a session is still
        // viable before calling session.commit();
        // We're relying here on the implementation using a RuntimeException
        // and *not* a JMS exception in this case and we just dump the
        // session we are managing. Of course, Inq maps JMS exceptions to
        // a (subclass of) RuntimeException anyway, so the result is
        // everything comes through here.
        try
        {
          // TODO: test root dirty flag
          session.commit();
          mqDirty(false);
        }
        catch(RuntimeException e)
        {
          t.setMqSession(null);
        }
      }
    }
  }
  
  protected void mqRollback()
  {
    Transaction t = getRootTransaction();

    if (t == this || mqcommit_)
    {
      SessionI session = t.getMqSession();
      
      if (session != null)
      {
        try
        {
          // See comments in mqCommit...
          session.rollback();
        }
        catch(RuntimeException e)
        {
          t.setMqSession(null);
        }
      }
    }
  }
  
  /**
   * Returns the events that were generated from the instance creations,
   * mutations and destroys in this transaction. Can only sensibly be called
   * after events have been raised, so is useful for post-event actions.
   */
  protected Array getEventBundle()
  {
    throw new UnsupportedOperationException();
  }
  
  protected void rememberOld(Map i)
  {
    if (notifyOld_ != null)
    {
      Descriptor d = i.getDescriptor();
      Map m;
      if ((m = (Map)notifyOld_.getIfContains(d)) != null)
      {
        Any ii = m.getIfContains(i);
        if (ii == null)
        {
          ii = i.buildNew(i);
          m.add(i, ii);
        }
      }
    }
  }
  
  protected void doResetRememberOld()
  {
    resetRememberOld(true);
  }
  
  protected void resetRememberOld(boolean discardTypes)
  {
    if (notifyOld_ != null)
    {
      if (discardTypes)
        notifyOld_.empty();
      else
      {
        if (notifyOld_.entries() != 0)
        {
          Iter i = notifyOld_.createIterator();
          while (i.hasNext())
          {
            Map m = (Map)i.next();
            m.empty();
          }
        }
      }
    }
  }
  
  protected Any getRememberedOld(Any i)
  {
    Any ret = null;
    
    if (notifyOld_ != null)
    {
      Map ii = (Map)i;
      Map m = (Map)notifyOld_.getIfContains(ii.getDescriptor());
      
      // If we are remembering things then this should always return
      // non-null really...
      ret = m.getIfContains(i);
    }
    
    return ret;
  }

  private void init()
  {
		beforeEvents_ = AbstractComposite.orderedSet();
		afterEvents_  = AbstractComposite.orderedSet();
  }
  
  private Transaction getRootTransaction()
  {
    Transaction t = this;
    Transaction parent;
    do
    {
      parent = t.getParent();
      if (parent != null)
        t = parent;
    }
    while (parent != null);
    
    return t;
  }

  // A function added as an action to the transaction that raises the
  // EXEC_COMPLETE event for a scripted service or function. Intended
  // to be submitted as a post-event action within the transaction.
  public class RaiseCompletedEvent extends AbstractFunc
  {
    private static final long serialVersionUID = 1L;

    private EventGenerator f_;
    private Array          eventTypes_;
    
    /**
     * 
     * @param f
     */
    public RaiseCompletedEvent(EventGenerator f, Array eventTypes)
    {
      f_          = f;
      eventTypes_ = eventTypes;
    }
    
    public Any exec(Any a) throws AnyException
    {
      // If specific event types are requested, only fire the event if the
      // bundle contains them, removing any that are not required.
      Array events = getEventBundle();
      
      if (events != null)
      {
        boolean filter = false;
        if (!eventTypes_.equals(EventConstants.ALL_TYPES))
        {
          filter = true;
          events = events.shallowCopy();
          Iter i = events.createIterator();
          while(i.hasNext())
          {
            Event ev = (Event)i.next();
            if (!eventTypes_.contains(ev.getId()))
              i.remove();
          }
        }
  
        // Only fire an event if the bundle is not empty *and* there
        // was a filter in place
        if (events.entries() != 0 || !filter)
        {
          Event e = f_.makeEvent(EventConstants.EXEC_COMPLETE);
          e.setContext(events);
          
          f_.fireEvent(e);
        }
      }
      
      return null;
    }
    
    public boolean equals(Any a)
    {
      if (!(a instanceof RaiseCompletedEvent))
        return false;
      
      RaiseCompletedEvent r = (RaiseCompletedEvent)a;
      return f_.equals(r.f_);
    }
  }
  
  private class GetTemporary extends AbstractVisitor
  {
    private Any    a_;
    
    public Any getTemporary(Any a)
    {
      a.accept(this);

      return a_;
    }
    
    public void visitAnyBoolean (BooleanI b)
    {
      a_ = boolean_;
    }

    public void visitAnyByte (ByteI b)
    {
      a_ = byte_;
    }

    public void visitAnyChar (CharI c)
    {
      a_ = char_;
    }

    public void visitAnyInt (IntI i)
    {
      a_ = int_;
    }

    public void visitAnyShort (ShortI s)
    {
      a_ = short_;
    }

    public void visitAnyLong (LongI l)
    {
      a_ = long_;
    }

    public void visitAnyFloat (FloatI f)
    {
      a_ = float_;
    }

    public void visitAnyDouble (DoubleI d)
    {
      a_ = double_;
    }

    public void visitDecimal (Decimal d)
    {
      decimal_.setScale(d.scale());
      a_ = decimal_;
    }

    public void visitAnyString (StringI s)
    {
      a_ = string_;
    }

    public void visitAnyDate (DateI d)
    {
      a_ = date_;
    }
    
    public void visitMap(Map m)
    {
      if (m instanceof AnyIcon)
        a_ = icon_;
      else if (m instanceof AnyColor)
        a_ = color_;
      else if (m instanceof AnyFont)
        a_ = font_;
      else if (m instanceof AnyAttributeSet)
        a_ = attrSet_;
      else
        throw new AnyRuntimeException("Unknown tmeporary type " + m.getClass());
      
    }
  }

  static public class ReadProperty extends AbstractVisitor
  {
    private static final long serialVersionUID = 1L;
    
    private Any a_;
    
    public Any getAny()
    {
      Any ret = a_;
      a_ = null;
      return ret;
    }
    
    public void visitAnyBoolean (BooleanI b)
    {
      a_ = b;
    }

    public void visitAnyByte (ByteI b)
    {
      a_ = b;
    }

    public void visitAnyChar (CharI c)
    {
      a_ = c;
    }

    public void visitAnyInt (IntI i)
    {
      a_ = i;
    }

    public void visitAnyShort (ShortI s)
    {
      a_ = s;
    }

    public void visitAnyLong (LongI l)
    {
      a_ = l;
    }

    public void visitAnyFloat (FloatI f)
    {
      a_ = f;
    }

    public void visitAnyDouble (DoubleI d)
    {
      a_ = d;
    }

    public void visitDecimal (Decimal d)
    {
      a_ = d;
    }

    public void visitAnyString (StringI s)
    {
      a_ = s;
    }

    public void visitAnyDate (DateI d)
    {
      a_ = d;
    }

    public void visitMap (Map m)
    {
      a_ = m;
    }

    public void visitArray (Array a)
    {
      a_ = a;
    }

    public void visitSet (Set s)
    {
      a_ = s;
    }

    //public void visitFunc (Func f)
    // There shouldn't be any funcs applied to this visitor. Allow it
    // to croak just to make sure

    public void visitAnyObject (ObjectI o)
    {
      a_ = o;
    }
    
    public void visitUnknown(Any o)
    {
      a_ = o;
    }
  }
}
