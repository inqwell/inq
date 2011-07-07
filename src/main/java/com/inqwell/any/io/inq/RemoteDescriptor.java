/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/inq/RemoteDescriptor.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:21 $
 */
 
package com.inqwell.any.io.inq;

import com.inqwell.any.*;
import com.inqwell.any.Process;
import com.inqwell.any.io.PhysicalIO;
import com.inqwell.any.io.AnyIOException;
import com.inqwell.any.server.BOTDescriptor;

/**
 * A remote descriptor encloses a <code>Descriptor</code> delegate
 * to which its method calls are forwarded once the delegate
 * has been successfully downloaded from the server in which
 * the BOT is hosted.
 * <p>
 * A download attempt is made whenever 
 * @author $Author: sanderst $
 * @version $Revision: 1.5 $
 * @see com.inqwell.any.Any
 */ 
public final class RemoteDescriptor extends    AbstractAny
                                    implements Descriptor,
                                               EventGenerator
{
	private Any        fQName_;
	private Any        resourceId_;
  
  private PhysicalIO io_;

  private Descriptor delegate_;

	private NodeEventPropagator ourListeners_;

  private WaitReady  waitReady_;

	private Array      eventTypes_;

	public RemoteDescriptor(Any fQName, Any resourceId)
	{
		fQName_     = fQName;
    resourceId_ = resourceId;
    init();
	}
	
  public Any newInstance()
  {
    fetchDescriptor();
      
		Map m = (Map)delegate_.newInstance();
		m.setDescriptor(null);
		m.setDescriptor(this);
		return m;
  }
  
	public void construct(Map m, Transaction t) throws AnyException
	{
    startUse();
    try
    {
      abortNoDelegate();
      delegate_.construct(m, t);
    }
    finally
    {
      endUse();
    }
	}

	public void mutate(Map newVal,
                     Map oldVal,
                     Map context,
                     Transaction t) throws AnyException
	{
    startUse();
    try
    {
      abortNoDelegate();
      delegate_.mutate(newVal, oldVal, context, t);
    }
    finally
    {
      endUse();
    }
	}

	public void destroy(Map m, Transaction t) throws AnyException
	{
    startUse();
    try
    {
      abortNoDelegate();
      delegate_.destroy(m, t);
    }
    finally
    {
      endUse();
    }
	}

  public void expire(Map m, Transaction t) throws AnyException
  {
    startUse();
    try
    {
      abortNoDelegate();
      delegate_.expire(m, t);
    }
    finally
    {
      endUse();
    }
  }

  public Any getName()
  {
    abortNoDelegate();
		return delegate_.getName();
	}
	
  public Map getListenerData()
  {
    abortNoDelegate();
    return delegate_.getListenerData();
  }
  
  public Any getPackage()
  {
    abortNoDelegate();
		return delegate_.getPackage();
  }

	public Any getDefaultAlias()
	{
    abortNoDelegate();
		return delegate_.getDefaultAlias();
	}
	
	public Any getFQName()
	{
		return fQName_;
	}

  public Map getUniqueKey(Map m)
  {
    fetchDescriptor();
		return delegate_.getUniqueKey(m);
  }

	public Map getProto()
	{
    fetchDescriptor();
		return delegate_.getProto();
	}
	
  public KeyDef locateRetrievalKey (Map keyVal) throws AnyException
  {
    fetchDescriptor();
		return delegate_.locateRetrievalKey(keyVal);
  }
  
  public KeyDef locateRelationshipKey (Descriptor from) throws AnyException
  {
    fetchDescriptor();
		return delegate_.locateRelationshipKey(from);
  }
  
	public Any getDataField(Any fieldName, boolean mustResolve)
	{
    fetchDescriptor();
		return delegate_.getDataField(fieldName, mustResolve);
	}
	
  public Map getUniqueKeys()
  {
    fetchDescriptor();
		return delegate_.getUniqueKeys();
  }
  
  public Map getAllKeys()
  {
    fetchDescriptor();
		return delegate_.getAllKeys();
  }
  
	public KeyDef getKey(Any keyName)
	{
    fetchDescriptor();
		return delegate_.getKey(keyName);
	}

  public void joinForeign(Any fieldName, Transaction t)
	{
    fetchDescriptor();
		delegate_.joinForeign(fieldName, t);
	}

	public KeyDef getPrimaryKey()
	{
    startUse();
    try
    {
      fetchDescriptor();
      return delegate_.getPrimaryKey();
    }
    finally
    {
      endUse();
    }
	}

  public Any read(Process p, Map keyVal, int maxCount) throws AnyException
  {
    fetchDescriptor();
    KeyDef kd = locateRetrievalKey (keyVal);
		return delegate_.read(p, kd, keyVal, maxCount);
  }

	public Any  read   (Process p, Any keyName, Map keyVal, int maxCount) throws AnyException
	{
    startUse();
    try
    {
      fetchDescriptor();
      return delegate_.read(p, keyName, keyVal, maxCount);
    }
    finally
    {
      endUse();
    }
	}

	public Any  read   (Process p, KeyDef keyDef, Map keyVal, int maxCount) throws AnyException
	{
    startUse();
    try
    {
      fetchDescriptor();
      return delegate_.read(p, keyDef, keyVal, maxCount);
    }
    finally
    {
      endUse();
    }
	}

  public void write(Process p, Map m) throws AnyException
  {
    startUse();
    try
    {
      fetchDescriptor();
      delegate_.write(p, m);
    }
    finally
    {
      endUse();
    }
  }
	
	public Map manage(Process p, Map m)
  {
    startUse();
    try
    {
      fetchDescriptor();
      return delegate_.manage(p, m);
    }
    finally
    {
      endUse();
    }
  }

	public void unmanage(Process p, Map m)
  {
    startUse();
    try
    {
      abortNoDelegate();
      delegate_.unmanage(p, m);
    }
    finally
    {
      endUse();
    }
  }

	public void resync (Process p, Map m) throws AnyException
  {
    startUse();
    try
    {
      fetchDescriptor();
      delegate_.resync(p, m);
    }
    finally
    {
      endUse();
    }
  }

	public void expire(Transaction t) throws AnyException
	{
    try
    {
      waitIdle();
      fetchDescriptor();
      delegate_.expire(t);
    }
    finally
    {
      signalReady();
    }
	}

	public void destroy()
	{
    try
    {
      waitIdle();
      fetchDescriptor();
      delegate_.destroy();
      delegate_ = null;
    }
    finally
    {
      signalReady();
    }
	}

	public void delete(Process p, Map keyVal, Map instanceVal) throws AnyException
	{
    startUse();
    try
    {
      fetchDescriptor();
      delegate_.delete(p, keyVal, instanceVal);
    }
    finally
    {
      endUse();
    }
	}

	public void delete(Process p, Map instanceVal) throws AnyException
	{
    startUse();
    try
    {
      fetchDescriptor();
      delegate_.delete(p, instanceVal);
    }
    finally
    {
      endUse();
    }
	}

  public void setProto(Map proto)
  {
    // Leave undefined.  We should receive the proto in the
    // downloaded delegate.
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " setProto(proto)"));
	}

  public void addDataField(Any key, Any field)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " addDataField()"));
	}

  public void addEnumValue(Any key, Any symbol, Any value, Any extValue)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " addEnumValue()"));
	}

	public Map getEnums()
	{
    startUse();
    try
    {
      fetchDescriptor();
      return delegate_.getEnums();
    }
    finally
    {
      endUse();
    }
	}

  public Map getEnumSymbols()
  {
    startUse();
    try
    {
      fetchDescriptor();
      return delegate_.getEnumSymbols();
    }
    finally
    {
      endUse();
    }
  }
  
  public boolean isEnum(Any key)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " isEnum()"));
  }
  
  public boolean isResolved(Descriptor d)
  {
		return delegate_ != null;
  }
  
  public Set reportUnresolved()
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean isDefunct()
  {
		throw new UnsupportedOperationException();
  }
  
  public boolean isKeyField(Any f)
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean isUniqueKeyField(Any f)
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean isNonKeyField(Any f)
  {
    throw new UnsupportedOperationException();
  }
  public void setFormat(Any key, Any formatString)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " setFormat()"));
  }
  
  public void setTitle(Any key, Any formatString)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " setTitle()"));
  }
  
  public void setWidth(Any key, Any width)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " setWidth()"));
  }

  public void setPrivilege(Any key, Map privileges)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " setPrivilege()"));
  }

  public String getRenderer(Any key)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " getRenderer()"));
  }
  
  public String getEditor(Any key)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " getEditor()"));
  }
  
  public Any getFromPrimary(Any key)
  {
    throw new UnsupportedOperationException("getFromPrimary()");
  }

	public DomainAgent getDomainAgent()
	{
		return InqIoManager.instance().getDomainAgent(resourceId_);
  }

	public void setDescriptorsInKeys()
  {
    // Iterate over the keys (fetched from the delegate)
    Iter i = getAllKeys().createIterator();
		while (i.hasNext())
		{
			KeyDef k = (KeyDef)i.next();
			k.setDescriptor(this);
			k.buildProto();
		}
  }
  
  public String getFormat(Any key)
  {
    abortNoDelegate();
    return delegate_.getFormat(key);
  }
  
  public Any getTitle(Any key)
  {
    abortNoDelegate();
    return delegate_.getTitle(key);
  }
  
  public int getWidth(Any key)
  {
    abortNoDelegate();
    return delegate_.getWidth(key);
  }
  
	public void checkPrivilege(Process p, Any access, Any key)
	{
    abortNoDelegate();
    delegate_.checkPrivilege(p, access, key);
	}
	
	public void checkPrivilege(Process p, Any access, Map keys)
	{
    abortNoDelegate();
    delegate_.checkPrivilege(p, access, keys);
	}
	
  public void addDataFieldReference(Any field,
                                    Any fQName,
                                    Any alias,
                                    Any initOverride,
                                    Any labelOverride,
                                    Any target)
  {
		throw new UnsupportedOperationException("addDataFieldReference " + getClass().toString());
  }
  
  public int resolveReferences(Descriptor d)
  {
    return BOTDescriptor.RESOLVE_NOTHING;
  }
  
  public void resetResolved(Descriptor d)
  {
  }
  
  public boolean equals(Any o)
  {
		if (AnyAlwaysEquals.isAlwaysEquals(o))
			return true;

		if (o instanceof Descriptor)
		{
      Descriptor d = (Descriptor)o;
			return d.getFQName().equals(fQName_);
		}
		else
		{
			return false;
		}
  }
  
  public int hashCode()
  {
		return fQName_.hashCode();
  }
  
  public boolean isTransient(Any key)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " isTransient()"));
  }
  
  public void setIO (PhysicalIO io)
  {
    // Always an instance of SimpleInqIo (should we check for this?)
    io_ = io;
  }
  
  public void addEventListener (EventListener l, Any eventParam)
  {
		ourListeners_.addEventListener(l, eventParam);
  }

  public void addEventListener (EventListener l)
  {
		ourListeners_.addEventListener(l);
  }

  public void fireEvent (Event e) throws AnyException
  {
		ourListeners_.fireEvent(e);
	}

  public void removeEventListener (EventListener l)
  {
		ourListeners_.removeEventListener(l);
  }

  public void removeAllListeners ()
  {
		ourListeners_.removeAllListeners();
  }

  public Array getGeneratedEventTypes()
  {
		return eventTypes_;
  }

  public Event makeEvent(Any eventType)
  {
		Event ret = null;

		if (eventType.equals(EventConstants.BOT_CREATE))
		{
			return new SimpleEvent(makeEventType(EventConstants.BOT_CREATE));
		}

		if (eventType.equals(EventConstants.BOT_DELETE))
		{
			return new NodeEvent(makeEventType(EventConstants.BOT_DELETE));
		}

		if (eventType.equals(EventConstants.BOT_UPDATE))
		{
			return new NodeEvent(makeEventType(EventConstants.BOT_UPDATE));
		}

		if (eventType.equals(EventConstants.BOT_EXPIRE))
		{
			return new SimpleEvent(makeEventType(EventConstants.BOT_EXPIRE));
		}

		if (eventType.equals(EventConstants.BOT_CATALOGED))
		{
			return new SimpleEvent(makeEventType(EventConstants.BOT_EXPIRE));
		}

		return ret;
	}

  public boolean raiseAgainstChildren(Event e)
  {
    return false;
  }

	public void waitIdle()
	{
    if (waitReady_ != null)
      waitReady_.waitIdle();
  }
  
  public void signalReady()
  {
    if (waitReady_ != null)
      waitReady_.signalReady();
  }

  public synchronized Descriptor getDescriptor()
  {
    return delegate_;
  }
  
	private void startUse()
	{
    if (waitReady_ != null)
      waitReady_.startUse();
	}
	
	private void endUse()
	{
    if (waitReady_ != null)
      waitReady_.endUse();
	}
	
	// Wait for the BOT to be ready
	private void waitReady()
	{
    if (waitReady_ != null)
      waitReady_.waitReady();
	}
  
  private void setDefunct()
  {
    if (waitReady_ != null)
      waitReady_.setDefunct();
  }
	
  private synchronized Descriptor fetchDescriptor()
  {
    if (delegate_ != null)
      return delegate_;
      
    InqIo inqIo = null;
    
    try
    {
      inqIo = (InqIo)InqIoManager.instance().acquire(resourceId_);
      BOTDescriptor bd = (BOTDescriptor)inqIo.fetchDescriptor(fQName_);
      // Patch up descriptor
      bd.setIO(io_);
      delegate_ = bd;
      setDescriptorsInKeys();
    }
    
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
    
    finally
    {
      if (inqIo != null)
      {
        try
        {
          InqIoManager.instance().release(resourceId_, inqIo, null, null);
        }
        catch(AnyException e)
        {
          throw new RuntimeContainedException(e);
        }
      }
    }
    return delegate_;
  }

  private void abortNoDelegate() throws AnyRuntimeException
  {
    if (delegate_ == null)
      throw new AnyRuntimeException("No delegate in remote object " + getFQName());
  }
  
  private void init()
  {
    initEvents();
    waitReady_           = new WaitReady(getFQName());
	}
	
	private void initEvents()
	{
		eventTypes_  = AbstractComposite.array();
		eventTypes_.add(makeEventType(EventConstants.BOT_CREATE));
		eventTypes_.add(makeEventType(EventConstants.BOT_DELETE));
		eventTypes_.add(makeEventType(EventConstants.BOT_UPDATE));
		eventTypes_.add(makeEventType(EventConstants.BOT_EXPIRE));
		eventTypes_.add(makeEventType(EventConstants.BOT_CATALOGED));

		ourListeners_  = new NodeEventPropagator();
	}
  
	private Map makeEventType(Any type)
	{
		Map ret = AbstractComposite.eventIdMap();

		ret.add (Descriptor.descriptor__, this);
		ret.add (EventConstants.EVENT_TYPE, type);
    if (type.equals(EventConstants.BOT_CREATE))
      ret.add(EventConstants.EVENT_CREATE, AnyAlwaysEquals.instance());

		return ret;
	}
}
