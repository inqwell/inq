/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/BOTDescriptor.java $
 * $Author: sanderst $
 * $Revision: 1.7 $
 * $Date: 2011-05-25 15:23:32 $
 */

package com.inqwell.any.server;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

//import com.inqwell.any.io.AnyIOException;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractDescriptor;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyAlwaysEquals;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFuncHolder;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.BreadthFirstIter;
import com.inqwell.any.Call;
import com.inqwell.any.Catalog;
import com.inqwell.any.ConstDate;
import com.inqwell.any.ConstInt;
import com.inqwell.any.ConstString;
import com.inqwell.any.Descriptor;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.EventGenerator;
import com.inqwell.any.EventListener;
import com.inqwell.any.FloatI;
import com.inqwell.any.Func;
import com.inqwell.any.Globals;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.KeyDef;
import com.inqwell.any.LocateNode;
import com.inqwell.any.Map;
import com.inqwell.any.NodeEvent;
import com.inqwell.any.NodeEventPropagator;
import com.inqwell.any.Process;
import com.inqwell.any.Queue;
import com.inqwell.any.Set;
import com.inqwell.any.SimpleEvent;
import com.inqwell.any.StringI;
import com.inqwell.any.Transaction;
import com.inqwell.any.Value;
import com.inqwell.any.WaitReady;
import com.inqwell.any.io.AnyIOException;
import com.inqwell.any.io.PhysicalIO;

public final class BOTDescriptor extends    AbstractDescriptor
                                 implements Descriptor,
                                            EventGenerator,
                                            Cloneable
{
  public static Any botTarget__ = new ConstInt(-1);
  
  public static final IntI REPLACE = new ConstInt(0);
  public static final IntI THIS    = new ConstInt(1);
  public static final IntI PROXY   = new ConstInt(2);
  
  // Arguments to Inq function to set up the metadata
  public static final Any Typedef     = AbstractValue.flyweightString("Typedef");
  public static final Any Field       = AbstractValue.flyweightString("Field");
  public static final Any Key         = AbstractValue.flyweightString("Key");
  public static final Any KeyField    = AbstractValue.flyweightString("KeyField");
  public static final Any Package     = AbstractValue.flyweightString("Package");
  public static final Any FQName      = AbstractValue.flyweightString("FQName");
  public static final Any IsNative    = AbstractValue.flyweightString("IsNative");
  public static final Any Parsed      = AbstractValue.flyweightString("Parsed");
  public static final Any Fields      = AbstractValue.flyweightString("Fields");
  public static final Any Keys        = AbstractValue.flyweightString("Keys");
  public static final Any KeyFields   = AbstractValue.flyweightString("KeyFields");
  public static final Any Enums       = AbstractValue.flyweightString("Enums");
  public static final Any EnumSymbols = AbstractValue.flyweightString("EnumSymbols");
  public static final Any Data        = AbstractValue.flyweightString("Data");
  public static final Any Width       = AbstractValue.flyweightString("Width");
  public static final Any Label       = AbstractValue.flyweightString("Label");
  public static final Any Format      = AbstractValue.flyweightString("Format");
  public static final Any Unique      = AbstractValue.flyweightString("Unique");
  public static final Any Cached      = AbstractValue.flyweightString("Cached");
  public static final Any HitRate     = AbstractValue.flyweightString("HitRate");
  public static final Any Loaded      = AbstractValue.flyweightString("Loaded");
  
  public static final String metaTypes__ = "inq.meta.types";
  
  // Flag - we only try to locate the meta data types once.
  private static boolean metaLookup__;
  private static Descriptor Typedef__;
  private static Descriptor Field__;
  private static Descriptor Key__;
  private static Descriptor KeyField__;

  private static     LogManager lm = LogManager.getLogManager();
  private static     Logger     l  = lm.getLogger("inq");
  
  // Some optimisations for checking whether we should allow
  // certain updates to BOT instance fields
  private Set   uniqueKeyFields_;
  private Set   nonUniqueKeyFields_;
  
  // Fields listed here will be removed in the
  // serialized form
  private Set   transients_;

  // must be unique and native
  private KeyDef primary_;

	// This is the event types we can generate
	private transient Array eventTypes_;

	private transient NodeEventPropagator ourListeners_;

  private Func constructor_;
  private Func join_;
  private Func mutator_;
  private Func destroyer_;
  private Func expirer_;

  private Any  baseURL_;
  
  private transient WaitReady waitReady_;
  
  // The references to fields imported from other descriptors.
  // Maps the other descriptor's fQName to the (Set of) fields.
  private transient Map     references_;
  
  private transient Map     targets_;   // where the reference is destined for (here or a key)
  
  // Those descriptors we have successfully resolved against
  private transient Set     resolveSuccess_;
  
  // Whether we are fully resolved
  private boolean resolved_ = false;
  
  // Map the other descriptor's field name to the local field
  // name, when different.
  private transient Map     referenceAlias_;
  
  // Any initial value overrides for references
  private transient Map     referenceInit_;
  
  // Any label overrides for references
  private transient Map     referenceLabel_;
  
  // Two separate structures are required to capture
  // foreign key references from cached keys within other
  // typedefs:
  // 1) A map relating fields within this typedef that,
  //    when mutated, invalidate a cached key in another,
  //    referring typedef. Maps local field name to
  //    the referring typedef and set of key names
  //    within that typedef.
  // 2) A map relating the fQName of the referring typedef
  //    so that, when the referring typedef is reparsed,
  //    the defunct typedef in 1) can be removed for all fields.
  //    Maps referring fQName to its typedef object and set
  //    of local field names it refers to.
  // The structures are named after what they map from
  private transient Map     fkFields_;
  private transient Map     fkTypes_;
  // And one further structure is required to note the other
  // typedefs a typedef imports key fields from (so the
  // reverse direction to the above structures). Then, when
  // a referring typedef is reparsed it can remove itself
  // from the above structures in all the typedefs it imports
  // from.
  private transient Set     fkRefersTo_;
  
  // Keep the order of the fields for use by IO systems
  private transient Array   fieldOrder_;
    
  /**
   * Enter a descriptor into the catalog at the given path. If there
   * is already a descriptor at the specified path it will be
   * expired before being replaced with the new one.  If there are
   * references remaining to the original descriptor and methods
   * subsequently called on it, an exception will be thrown.
   */
  public static void catalogDescriptor(Descriptor d, String path, Transaction t) throws AnyException
  {
    // Pass in the transaction from the parser so we can allow
    // PRIVILEGE checking

    // This doesn't stop other threads looking things up in the
    // catalog, it just prevents two threads cataloging (and
    // thereafter walking the catalog if there references) at the
    // same time.
    synchronized(Catalog.class)
    {
      //System.out.println ("BOTDescriptor.catalogDescriptor(): " + d + " at " + path);
      LocateNode l       = new LocateNode(path);
      Catalog    c       = Catalog.instance();
      Map        catalog = c.getCatalog();
      
      // Look for any existing descriptor by this name and expire/destroy
      // if found.
      Descriptor od = (Descriptor)EvalExpr.evalFunc(t,
                                                    catalog,
                                                    l,
                                                    Descriptor.class);
      if (od != null)
      {
        if (od instanceof BOTDescriptor)
        {
          BOTDescriptor bd = (BOTDescriptor)od;
          bd.setDefunct();
        }
        od.expire(t);
        od.destroy();
      }
      
      // Scanning the catalog is expensive, so do it once to build the
      // current set of descriptors.
      BreadthFirstIter i = new BreadthFirstIter(catalog);
      i.setCyclicSafe(true);
      Queue descriptors = AbstractComposite.queue();
      Set   refersMe    = AbstractComposite.set();
      while (i.hasNext())
      {
        Any any = i.next();
        
        if (any instanceof Descriptor)
        {
          // No point in resolving candidate descriptor against
          // any existing one we are about to overwrite.
          if (d.equals(any))
            continue;
          
          // Reset the resolved status of the descriptors that have
          // a dependency on the new descriptor.
          Descriptor cd = (Descriptor)any;
          cd.resetResolved(d);
          
          // When building the list of existing descriptors we will
          // resolve the new descriptor against, put any we know the
          // new descriptor has references on first. The rest are
          // just there so we can reverse-resolve them
          if (d instanceof BOTDescriptor)
          {
            BOTDescriptor bd = (BOTDescriptor)d;
            if (bd.references_ != null &&
                bd.references_.contains(cd.getFQName()))
            {
              descriptors.addFirst(any);
              refersMe.add(any);
            }
            else
              descriptors.add(any);
          }
          else
            descriptors.add(any);
        }
      }
      
      resolveDescriptor(d, descriptors, null, t);
      
      // 2nd pass
      resolveDescriptor(d, refersMe, null, t);
      
      d.resolveReferences(d);


      // pass on the transaction PRIVILEGE
      c.catalog(d, l.getNodePath(), t);
      
      // Raise a BOT_CATALOGED event when a new
      // catalog entry is fully resolved.
      if (d.isResolved(null))
      {
        d.setDescriptorsInKeys();
        raiseResolved(d, t);
      }
    }
  }
  
  // Resolve Descriptor d against Descriptors descriptors
  // skipping any given in skip.
  private static void resolveDescriptor(Descriptor  d,
                                        Any         descriptors,
                                        Descriptor  skip,
                                        Transaction t) throws AnyException
  {
    // Resolve new descriptor against existing, and vice versa
		Iter i = descriptors.createIterator();
		
		while (i.hasNext())
		{
			Any any = i.next();
			
			if (any == skip)
        continue;
			
      Descriptor cd = (Descriptor)any;
      d.resolveReferences(cd);
      d.setDescriptorsInKeys();
      // Note that d is the descriptor being cataloged and if it
      // is fully resolved an event will be raised by the top-level
      // call to catalogDescriptor
      
      int didResolve = cd.resolveReferences(d);
      cd.setDescriptorsInKeys();
      if (didResolve == RESOLVE_DONE)
      {
        // We managed to reverse-resolve the current descriptor.
        // It may not be fully resolved but the fact that it
        // has improved means that it is worth running it by the
        // others to see if it can now be used to resolve them.
        resolveDescriptor(cd, descriptors, d, t);

        // Raise a BOT_CATALOGED event when an existing
        // catalog entry is fully resolved.
        raiseResolved(cd, t);
      }
		}
  }
  
  static private void raiseResolved(Descriptor  d,
                                    Transaction t) throws AnyException
  {
    if (d.isResolved(null))
    {
      if (d instanceof EventGenerator)
      {
        EventGenerator eg = (EventGenerator)d;
        Event e = eg.makeEvent(EventConstants.BOT_CATALOGED);
        KeyDef kd = d.getPrimaryKey();
        Array fields = kd.getFields();
        if (!d.getProto().containsAll(fields))
          throw new AnyRuntimeException("Missing native key field amongst " +
                                        fields +
                                        " in typedef " +
                                        d.toString());
        eg.fireEvent(e);
      }
      
      // Do the metadata stuff. Only for types that are
      // not part of the meta data system itself
      if (!d.getFQName().toString().startsWith(metaTypes__))
        setupMetaData(d, t);
    }
  }
  
  // Set up the meta data for the given descriptor (provided
  // its not one of the system meta types itself)
  private static void setupMetaData(Descriptor d,
                                    Transaction t) throws AnyException
  {
    if (!metaLookup__)
    {
      // We haven't tried to find the metadata descriptors yet.
      LocateNode l = new LocateNode(metaTypes__);
      l.setTransaction(t);
      Map m = (Map)l.exec(Catalog.instance().getCatalog());
      if (m != null)
      {
        Typedef__ = (Descriptor)m.get(Typedef);
        Field__   = (Descriptor)m.get(Field);
        Key__     = (Descriptor)m.get(Key);
        KeyField__   = (Descriptor)m.get(KeyField);
      }
      metaLookup__ = true;
    }
    
    // Just in case there are no meta types
    if (Typedef__ != null)
    {
      Call c = (Call)Server.instance().getCreateMeta().cloneAny();
      Map inqArgs = c.getArgs();
      Map args = inqArgs; // put originals back afterwards.
      inqArgs = (Map)args.cloneAny();
      
      // Set up the argument values
      inqArgs.replaceItem(Typedef,   d.getName());
      inqArgs.replaceItem(Package,   d.getPackage());
      inqArgs.replaceItem(FQName,    d.getFQName());
      inqArgs.replaceItem(IsNative,  (d.getAllKeys() == null) ? AnyBoolean.TRUE
                                                              : AnyBoolean.FALSE);
      inqArgs.replaceItem(Parsed,    new ConstDate());
      
      // For Fields/Keys we need a collection of instances
      inqArgs.replaceItem(Fields, makeFields(d, Field__));
      
      // Keys only for complex types
      if (d.getAllKeys() !=  null)
      {
        Map keys;
        inqArgs.replaceItem(Keys,   (keys = makeKeys(d, Key__)));
        
        // For KeyFields there is a collection per Key
        Map kfs = AbstractComposite.simpleMap();
        Iter i = keys.createKeysIterator();
        while (i.hasNext())
        {
          Any k = i.next();
          makeKeyFields(d, k, KeyField__, kfs);
        }
        
        inqArgs.replaceItem(KeyFields, kfs);
      }
      
      // Enums and EnumSymbols
      Any enums = d.getEnums();
      inqArgs.replaceItem(Enums, enums);
      Any enumSymbols = d.getEnumSymbols();
      inqArgs.replaceItem(EnumSymbols, enumSymbols);
      
      // Call the inq script.  The script returns a map of the
      // managed Key instances for each key in the new typedef.
      // The actual field values for the cache statistics are then
      // passed to the keys themselves for use during runtime.
      Map newKeys;
      c.setArgs(inqArgs);

      try
      {
        newKeys = (Map)EvalExpr.evalFunc(t,
                                         t.getContext(),
                                         c);
        
        // We know from the script that the key values in newKeys
        // are the unique keys of the Key instances.  This is a map
        // that includes the key name, which we use to correlate
        // the managed instance with the key itself.
        Iter i = newKeys.createKeysIterator();
        while (i.hasNext())
        {
          Map km = (Map)i.next();
          Any kn = km.get(Key);
          KeyDef kd = d.getKey(kn);
          // Standard inq structure
          Map newKey = (Map)((Map)newKeys.get(km)).get(Key);
          //System.out.println("newKey " + newKey);
          kd.setStaticticsVariables((IntI)newKey.get(Loaded),
                                    (FloatI)newKey.get(HitRate));
        }
      }
      finally
      {
        c.setArgs(args);
      }
    }
  }
  
  static private Map makeFields(Descriptor d, Descriptor field)
  {
    Map m = AbstractComposite.simpleMap();
    Map proto;
    Iter i = (proto = d.getProto()).createKeysIterator();
    while (i.hasNext())
    {
      Any k = i.next();
      Any v = proto.get(k);
      v = v.cloneAny();
      Map newField = (Map)field.newInstance();
      
      // Put the data item in and initialise other fields
      newField.replaceItem(Data, v);
      newField.get(Field).copyFrom(k);
      ((IntI)newField.get(Width)).setValue(d.getWidth(k));
      newField.get(Label).copyFrom(d.getTitle(k));
      ((StringI)newField.get(Format)).setValue(d.getFormat(k));
      
      // Add to the return collection
      m.add(k, newField);
    }
    return m;
  }
      
  static private Map makeKeys(Descriptor d, Descriptor key)
  {
    Map m = AbstractComposite.simpleMap();
    Map keys = d.getAllKeys();
    if (keys == null)
      return null;
      
    Iter i = keys.createKeysIterator();
    while (i.hasNext())
    {
      Any    kn     = i.next();
      KeyDef kd     = (KeyDef)keys.get(kn);
      Map    newKey = (Map)key.newInstance();
      
      // Initialise the fields
      newKey.get(Key).copyFrom(kn);
      ((BooleanI)newKey.get(Unique)).setValue(kd.isUnique());
      ((BooleanI)newKey.get(Cached)).setValue(kd.shouldCache());
      
      // Add to the return collection
      m.add(kn, newKey);
    }
    return m;
  }
    
  // Make a map of KeyField instances for the named key of the
  // given descriptor placing the result in kfs.
  static private void makeKeyFields(Descriptor d,
                                    Any        key,
                                    Descriptor keyField,
                                    Map        kfs)
  {
    KeyDef kd = d.getKey(key);
    Map  m = AbstractComposite.simpleMap();
    Iter i = kd.getAllFields().createIterator();
    while (i.hasNext())
    {
      Any kf          = i.next();
      Map newKeyField = (Map)keyField.newInstance();
      newKeyField.get(Key).copyFrom(key);
      newKeyField.get(Field).copyFrom(kf);
      m.add(kf, newKeyField);
    }
    kfs.add(key, m);
  }
  
    /*
  public static final Any Typedef   = new AnyString("Typedef");
  public static final Any Package   = new AnyString("Package");
  public static final Any FQName    = new AnyString("FQName");
  public static final Any Parsed    = new AnyString("Parsed");
  public static final Any Fields    = new AnyString("Fields");
  public static final Any Keys      = new AnyString("Keys");
  public static final Any KeyFields = new AnyString("KeyFields");
  */
  
  static void predicateBadDescriptor(Descriptor d1, Descriptor d2)
  {
		if (!d1.equals(d2))
			throw new IllegalArgumentException ("Map and Descriptor are incompatible!!");
	}

  public BOTDescriptor (Any name, Any alias, Any fQName, Any inqPackage)
  {
		super(name, alias, fQName, inqPackage);
    init();
  }

  public BOTDescriptor (String name,
                        String alias,
                        String fQName,
                        String inqPackage)
  {
    this(AbstractValue.flyweightString(name),
    		 AbstractValue.flyweightString(alias),
    		 AbstractValue.flyweightString(fQName),
    		 AbstractValue.flyweightString(inqPackage));
  }

  public Any read (Process p, Map keyVal, int maxCount) throws AnyException
  {
    KeyDef kd = locateRetrievalKey (keyVal);
    return read(p, kd, keyVal, maxCount);
	}

  public Any read (Process p, Any keyName, Map keyVal, int maxCount) throws AnyException
  {
    startUse();
      
    try
    {
      KeyDef kd = null;
      if (keyName != null)
      {
        kd = getKey(keyName);

        return read(p, kd, keyVal, maxCount);
      }
      else
      {
        return read(p, keyVal, maxCount);
      }
    }
    finally
    {
      endUse();
    }
	}

	public Any read (Process p, KeyDef kd, Map keyVal, int maxCount) throws AnyException
	{
    startUse();
      
    try
    {
      // Having located a suitable key definition make a copy initialised to the
      // input values.  This is important as the Map implementation is then
      // under the control of the key definition implementation, and it may
      // contain 'system' fields also
      Map m = kd.makeKeyVal(keyVal);

      // Synchronisation issues - we will lock the key value here
      // which may block if
      // another thread is processing the same key value.
      Server.instance().lock(p, m);
      //System.out.println ("Descriptor.read() " + kd);
      Any item;
      try
      {
        item = kd.read(m, maxCount);
      }
      finally
      {
        Server.instance().unlock(p, m);
      }
      return item;
    }
    finally
    {
      endUse();
    }
  }

	public void write (Process p, Map m) throws AnyException
	{
    startUse();
      
    try
    {
      predicateBadDescriptor(this, m.getDescriptor());
      primary_.write(m, p.getTransaction());
    }
    finally
    {
      endUse();
    }
	}

/*
  public void write (Process p, Map keyVal, Map instanceVal) throws AnyException
  {
    startUse();
      
    try
    {
      predicateBadDescriptor(this, instanceVal.getDescriptor());
      primary_.write(instanceVal);
    }
    finally
    {
      endUse();
    }
  }
*/

	public void delete(Process p, Map keyVal, Map instanceVal) throws AnyException
	{
    startUse();
      
    try
    {
      predicateBadDescriptor(this, instanceVal.getDescriptor());
      primary_.delete(keyVal, instanceVal, p.getTransaction());
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
      predicateBadDescriptor(this, instanceVal.getDescriptor());
      primary_.delete(primary_.makeKeyVal(instanceVal),
                      instanceVal,
                      p.getTransaction());
    }
    finally
    {
      endUse();
    }
	}

	public Map manage (Process p, Map m)
	{
    startUse();
      
    try
    {
      // When we get here the object m should already be guaranteed unique
      // by the fact that all its unique keys are locked by the calling
      // process p.  We simply have to put the identity semantics on and add
      // it to all the caches.
      predicateBadDescriptor(this, m.getDescriptor());

      if (m.hasIdentity())
      {
        // objects which have yet to be created should not have
        // identity semantics!  This should already have been checked in
        // in Transaction.createIntent()
        throw new IllegalArgumentException("Object is already created! " + m);
      }

      m.setTransactional(true);
      m = m.bestowIdentity();

      Iter i = getAllKeys().createIterator();
      while (i.hasNext())
      {
        KeyDef kd = (KeyDef)i.next();
        kd.manage(m);
      }
      return m;
    }
    finally
    {
      endUse();
    }
	}

	public void unmanage (Process p, Map m)
	{
    startUse();
      
    try
    {
      predicateBadDescriptor(this, m.getDescriptor());

      if (!m.hasIdentity())
      {
        throw new IllegalArgumentException("Object is not managed! " + m);
      }

      Iter i = getAllKeys().createIterator();
      while (i.hasNext())
      {
        KeyDef kd = (KeyDef)i.next();
        kd.unmanage(m);
      }
      m.setTransactional(false);
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
      primary_.resync(p.getTransaction(), m);
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
      
      if (expirer_ != null)
        expirer_.setTransaction(t);
        
      Iter i = getAllKeys().createIterator();
      while (i.hasNext())
      {
        KeyDef kd = (KeyDef)i.next();
        kd.expire(t);
      }
      Event e = makeEvent(EventConstants.BOT_EXPIRE);
      fireEvent(e);
    }
    finally
    {
      if (expirer_ != null)
        expirer_.setTransaction(Transaction.NULL_TRANSACTION);
        
      signalReady();
    }
	}

	public void destroy()
	{
    try
    {
      waitIdle();
      
      Iter i = getAllKeys().createIterator();
      while (i.hasNext())
      {
        KeyDef kd = (KeyDef)i.next();
        kd.destroy();
      }
    }
    finally
    {
      signalReady();
    }
	}

	public void construct(Map m, Transaction t) throws AnyException
	{
    startUse();
      
    try
    {
      predicateBadDescriptor(this, m.getDescriptor());
      if (constructor_ != null)
      {
        Func f = (Func)constructor_.cloneAny();
        int curLine = t.getLineNumber();
        f.setTransaction(t);
        Call.CallStackEntry se = (Call.CallStackEntry)t.getCallStack().peek();
        se.setLineNumber(curLine);
        t.getCallStack().push(new Call.CallStackEntry(this.getBaseURL(), this.construct__));
        f.execFunc(m);
        if (Call.isLogged(getFQName(), contructf__))
  	    	l.log(Level.INFO, "Constructed {0} ", m);

        t.getCallStack().pop();
        t.setLineNumber(curLine);
      }
      else
      {
        if (Call.isLogged(getFQName(), contructf__))
  	    	l.log(Level.INFO, "Constructed {0} ", m);
      }
      boolean exists = primary_.checkExists(m, t);
      if (!exists)
        throw new AnyException("Your newly constructed instance does not exist!");
    }
    finally
    {
      endUse();
    }
	}

  public void join(Map m, Transaction t) throws AnyException
  {
    startUse();
      
    try
    {
      predicateBadDescriptor(this, m.getDescriptor());
      if (join_ != null)
      {
        Func f = (Func)join_.cloneAny();
        int curLine = t.getLineNumber();
        f.setTransaction(t);
        Call.CallStackEntry se = (Call.CallStackEntry)t.getCallStack().peek();
        se.setLineNumber(curLine);
        t.getCallStack().push(new Call.CallStackEntry(this.getBaseURL(), this.join__));
        f.execFunc(m);
        if (Call.isLogged(getFQName(), joinf__))
  	    	l.log(Level.INFO, "Joined {0} ", m);
        t.getCallStack().pop();
        t.setLineNumber(curLine);
      }
      else
      {
        if (Call.isLogged(getFQName(), joinf__))
  	    	l.log(Level.INFO, "Joined {0} ", m);
      }
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
      predicateBadDescriptor(this, oldVal.getDescriptor());
      if (mutator_ != null)
      {
        context.replaceItem(Descriptor.new__, newVal);
        context.replaceItem(Descriptor.old__, oldVal);
        Func f = (Func)mutator_.cloneAny();

        f.setTransaction(t);
        Call.CallStackEntry se = (Call.CallStackEntry)t.getCallStack().peek();
        int curLine = se.getLineNumber();
        se.setLineNumber(t.getLineNumber());
			  t.getCallStack().push(new Call.CallStackEntry(this.getBaseURL(), Descriptor.mutate__));
        f.execFunc(context);
        if (Call.isLogged(getFQName(), mutatef__))
  	    	l.log(Level.INFO, "Mutated {0} ", context);
        t.getCallStack().pop();
        se.setLineNumber(curLine);
      }
      else
      {
        if (Call.isLogged(getFQName(), mutatef__))
        {
          context.replaceItem(Descriptor.new__, newVal);
          context.replaceItem(Descriptor.old__, oldVal);
  	    	l.log(Level.INFO, "Mutated {0} ", context);
        }
      }
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
      predicateBadDescriptor(this, m.getDescriptor());
      if (destroyer_ != null)
      {
        Func f = (Func)destroyer_.cloneAny();
        int curLine = t.getLineNumber();
        f.setTransaction(t);
        Call.CallStackEntry se = (Call.CallStackEntry)t.getCallStack().peek();
        se.setLineNumber(curLine);
			  t.getCallStack().push(new Call.CallStackEntry(this.getBaseURL(), this.destroy__));
        f.execFunc(m);
        if (Call.isLogged(getFQName(), destroyf__))
  	    	l.log(Level.INFO, "Destroyed {0} ", m);
        t.getCallStack().pop();
        se.setLineNumber(curLine);
      }
      else
      {
        if (Call.isLogged(getFQName(), destroyf__))
  	    	l.log(Level.INFO, "Destroyed {0} ", m);
      }
    }
    finally
    {
      endUse();
    }
	}

  public void expire(Map m, Transaction t) throws AnyException
	{
    // No need for the startUse etc stuff as we have been
    // called back from expire(Transaction).
    if (expirer_ != null)
    {
      predicateBadDescriptor(this, m.getDescriptor());
      // Don't need to clone function as we are protected whilst
      // from within expire(Transaction t) and the txn is already
      // in from there
      expirer_.execFunc(m);
    }
	}

  public Any getFromPrimary(Any key)
  {
    return primary_.getFromPrimary(key);
  }
  
  public Map getUniqueKey(Map m)
  {
    startUse();
      
    try
    {
      predicateBadDescriptor(this, m.getDescriptor());

      return primary_.makeKeyVal(m);
    }
    finally
    {
      endUse();
    }
	}

	public KeyDef getPrimaryKey()
	{
    startUse();
      
    try
    {
      return primary_;
    }
    finally
    {
      endUse();
    }
	}
	
  /**
   * Finds the best KeyDef object which matches the given key.
   * @exception AnyIOException if no key can be found.
   */
  public KeyDef locateRetrievalKey (Map keyVal) throws AnyException
  {
    startUse();
      
    try
    {
      KeyDef foundKey   = null;
      int    lastFields = 0;
      int    thisFields = 0;
      
      Iter iter = getAllKeys().createIterator();
      while (iter.hasNext())
      {
        KeyDef k = (KeyDef)iter.next();
        if (k.isNative() && ((thisFields = k.satisfies(keyVal)) != 0))
        {
          if (thisFields > lastFields)
          {
            foundKey = k;
            lastFields = thisFields;
          }
        }
      }
      if (foundKey == null)
        throw new AnyException ("No key found matching " + keyVal);
      
      return foundKey;
    }
    finally
    {
      endUse();
    }
  }

  public void addKey (KeyDef k)
  {
		//System.out.println ("addKey: " + k + " as " + k.getName());
    getAllKeys().add (k.getName(), k);
    if (k.isNative())
		{
			if (k.isUnique())
				uniqueKeyFields_.addAll(k.getFields(), true);
			else
				nonUniqueKeyFields_.addAll(k.getFields(), true);
		}
		//System.out.println ("addKey: keys are: " + getAllKeys());
  }

  public void addDataField(Any key, Any field)
  {
  	getProto().add(key, field);
    fieldOrder_.add(key);
  }

  public boolean isKeyField(Any f)
  {
    return uniqueKeyFields_.contains(f) || nonUniqueKeyFields_.contains(f);
  }
  
  public boolean isUniqueKeyField(Any f)
  {
    return uniqueKeyFields_.contains(f);
  }
  
  public boolean isNonKeyField(Any f)
  {
    return nonUniqueKeyFields_.contains(f);
  }
  
  // Add a reference for the specified field in the descriptor
  // specified by fQName.  Until the reference
  // is resolved there will be no entry in the prototype.
  // alias - the name the field is known as in this type (or key
  // of this type) or null if the original name is kept
  // initOverride - a different initial value than any applying to
  // the original, or null.
  // labelOverride - a different label than any applying to
  // the original, or null.
  // target - botTarget__ if the field is being imported into
  // the typedef or a key name specifying that the field will be
  // imported to that key.
  public void addDataFieldReference(Any field,
                                    Any fQName,
                                    Any alias,
                                    Any initOverride,
                                    Any labelOverride,
                                    Any target)
  {
    //System.out.println("addDataFieldReference " + target);
  	// alias may be null, in which case the name of the field
  	// will be the referenced field
  	
  	if (references_ == null)
  	{
  	  references_     = AbstractComposite.simpleMap();
  	  targets_        = AbstractComposite.simpleMap();
  	  resolveSuccess_ = AbstractComposite.set();
    }
      	
  	Set fields = null;
  	if (!references_.contains(fQName))
  	{
      fields = AbstractComposite.set();
      references_.add(fQName, fields);
    }
    else
    {
      fields = (Set)references_.get(fQName);
    }
    if (!fields.contains(field))
      fields.add(field);
  	
  	AnyString targetKey = new AnyString(fQName.toString() + field.toString());
  	
  	if (alias == null)
      alias = field;
      
    if (referenceAlias_ == null)
      referenceAlias_ = AbstractComposite.simpleMap();


    Any fieldAlias = new ConstString(fQName.toString() + field.toString());
    Set fieldAliases = null;
    if (!referenceAlias_.contains(fieldAlias))
    {
      fieldAliases = AbstractComposite.set();
      referenceAlias_.add(fieldAlias, fieldAliases);
    }
    else
    {
      fieldAliases = (Set)referenceAlias_.get(fieldAlias);
    }
    
    if (!fieldAliases.contains(alias))
    {
      fieldAliases.add(alias);
      if (target == botTarget__)
        fieldOrder_.add(alias);
    }

    targetKey.setValue(targetKey.toString() + alias.toString());
    
    Any initKey = null;
    if (initOverride != null || labelOverride != null)
      initKey = new ConstString(targetKey.toString() + target.toString());

    if (initOverride != null)
    {
      if (referenceInit_ == null)
        referenceInit_ = AbstractComposite.simpleMap();
      
      referenceInit_.add(initKey, initOverride);
    }
  	
    if (labelOverride != null)
    {
      if (referenceLabel_ == null)
        referenceLabel_ = AbstractComposite.simpleMap();
      
      referenceLabel_.add(initKey, labelOverride);
    }
    
  	Set targets = null;
  	if (!targets_.contains(targetKey))
  	{
      targets = AbstractComposite.set();
      targets_.add(targetKey, targets);
    }
    else
    {
      targets = (Set)targets_.get(targetKey);
    }
    if (!targets.contains(target))
      targets.add(target);
  }
  
  // Resolve any references this may have on the given descriptor
  public int resolveReferences(Descriptor d)
  {
  	Any fQName = d.getFQName();
  	int ret    = RESOLVE_NOTHING;
  	if (references_ != null && !isResolved(d) && references_.contains(fQName))
  	{
      ret = RESOLVE_DONE;  // but only against the given descriptor
      
  		AnyString fieldAlias = new AnyString();
  		AnyString targetKey  = new AnyString();
  		Set fields = (Set)references_.get(fQName);
  		Iter i = fields.createIterator();
  		while (i.hasNext())
  		{
  			Any thisField = i.next();
  			
  			// get the target field, if available
  			Any dataField = null;
        dataField = d.getDataField(thisField, false);
        if (dataField == null)
        {
          // If the referred-to descriptor is, itself, fully resolved
          // then to not find a field there is an error
          if (d.isResolved(null))
            throw new AnyRuntimeException("Non-existent field reference " +
                                          thisField +
                                          " to type " + d +
                                          " from " + this);
          return RESOLVE_UNABLE;
        }
  			// Assume this field is not aliased, then check if it is.
  			Any    fieldName = thisField;
  			
  			fieldAlias.setValue(fQName.toString() + thisField.toString());
  			targetKey.setValue(fieldAlias.getValue());
  			
  			if (referenceAlias_ != null &&
  			    referenceAlias_.contains(fieldAlias))
  			{
  				Any fieldNames = referenceAlias_.get(fieldAlias);
  				Iter ii = fieldNames.createIterator();
  				while (ii.hasNext())
  				{
            fieldName = ii.next();
            targetKey.setValue(targetKey.toString() + fieldName.toString());
            doResolvedReference(thisField, fieldName, dataField, d, targetKey);
            targetKey.setValue(fieldAlias.getValue());
          }
  			}
  			else
  			{
          doResolvedReference(thisField, fieldName, dataField, d, targetKey);
  			}
  		}
  		
  		// When here all the referenced fields of the target typedef
  		// were successfully resolved.
      if (!resolveSuccess_.contains(fQName))
        resolveSuccess_.add(fQName);
  	}
  	
  	return ret;
  }
  
  public boolean isResolved(Descriptor d)
  {
    if (d != null)
      return resolveSuccess_.contains(d.getFQName());
      
    if (resolved_)
      return true;
    
    //if (getName().toString().equals("Instrument"))
    //  System.out.println("isResolved " + getFQName() + " " + resolveSuccess_ + " " + references_);
    return (resolved_ = (resolveSuccess_ == null || resolveSuccess_.containsAll(references_)));
  }
  
  public Set reportUnresolved()
  {
    Set ret = null;
    
    if (references_ != null)
    {
      ret = AbstractComposite.set();
      Iter i = references_.createKeysIterator();
      while (i.hasNext())
      {
        Any d = i.next();
        if (!resolveSuccess_.contains(d))
          ret.add(d);
      }
      if (ret.entries() == 0)
        ret = null;
    }
    
    return ret;
  }
  
  public void resetResolved(Descriptor d)
  {
    //System.out.println("resetResolved on " + getFQName() + "  with  " + d.getFQName());
    resolved_ = false;
    if (d != null)
    {
      if (references_ != null &&
          references_.contains(d.getFQName()) &&
          resolveSuccess_.contains(d.getFQName()))
        resolveSuccess_.remove(d.getFQName());
    }
    else
    {
      if (resolveSuccess_ != null)
        resolveSuccess_.empty();
    }
  }
  
	public void setPrimaryKey(Any k)
	{
		primary_ = (KeyDef)k;
		addKey((KeyDef)k);
	}

  public void setConstructor(Func f)
  {
    constructor_ = f;
  }
  
  public void setJoin(Func f)
  {
    join_ = f;
  }
  
	public void setMutator(Func f)
	{
		mutator_ = f;
	}
	
	public void setDestroyer(Func f)
	{
		destroyer_ = f;
	}
	
	public void setExpirer(Func f)
	{
		expirer_ = f;
	}
	
	public void setTransient(Any key, boolean isTransient)
	{
		if (isTransient)
		{
			if (!transients_.contains(key))
			  transients_.add(key);
		}
		else
		{
			if (transients_.contains(key))
			  transients_.remove(key);
		}
	}

  public boolean isTransient(Any key)
  {
  	return transients_.contains(key);
  }
  
  public Any getBaseURL()
  {
	  return baseURL_;
	}

	public void setBaseURL(Any url)
	{
		baseURL_ = url;
	}

  public Array getFieldOrder()
  {
    return fieldOrder_;
  }
  
  /**
   * A property that places the supplied function into the
   * executing process's transaction so that a test for identity
   * other than this type's primary key is available for the
   * duration of the transaction.
   */
  public void setIdentity(AnyFuncHolder.FuncHolder idFunc)
  {
    //Call identityFunc = AbstractFunc.verifyCall(idFunc);
    // Use the magical way to get the process & transaction
    Process p = Globals.getProcessForThread(Thread.currentThread());
    Transaction t = p.getTransaction();
    t.setIdentity(this, (Func)idFunc.getFunc().cloneAny());
  }

  public void setNotifyOld(Descriptor d)
  {
    Process p = Globals.getProcessForThread(Thread.currentThread());
    Transaction t = p.getTransaction();
    t.setNotifyOld(d, true);
  }
  
  public void setForgetOld(Descriptor d)
  {
    Process p = Globals.getProcessForThread(Thread.currentThread());
    Transaction t = p.getTransaction();
    t.setNotifyOld(d, false);
  }
  
  /**
   * Join any keys whose cache integrity is dependent on
   * the field being mutated
   */
  public void joinForeign(Any fieldName, Transaction t)
  {
    // NOTE - it would be nice to know if we've already joined the
    // keys dependent on the given field before we go looking for
    // them
    startUse();
    
    try
    {
      // Check if we have any references to the field we are changing
      // from keys in other typedefs.
      if (fkFields_ != null && fkFields_.contains(fieldName))
      {
        Map m = (Map)fkFields_.get(fieldName);
        
        // We map a Descriptor as the key to a set of key names as
        // the value.
        
        Iter i = m.createKeysIterator();
        while (i.hasNext())
        {
          Descriptor d = (Descriptor)i.next();
          
          // This shouldn't happen because we keep the resources
          // aligned with each other, but thread windows will exist
          // and you never know.
          if (d.isDefunct())
            throw new AnyRuntimeException("A referring FK has a defunct typedef");
          
          Set keyNames = (Set)m.get(d);
          
          Iter ik = keyNames.createIterator();
          while (ik.hasNext())
          {
            Any keyName = ik.next();
            
            KeyDef kd = d.getKey(keyName);
            
            // Join the key into the transaction
            //t.purgeKey(kd);
          }
        }
      }
    }
    finally
    {
      endUse();
    }
  }
  
  public void setIO (PhysicalIO io)
  {
    //System.out.println ("BOTDescriptor.setIO " + io);
    Iter keys = getAllKeys().createIterator();
    while (keys.hasNext())
    {
			KeyDef k = (KeyDef)keys.next();
			PhysicalIO nio = (PhysicalIO)io.cloneAny();
			k.setIO(nio, this);
    }
    
    initEvents();  // for serialized-in instances
  }

  /**
   * Attempt to locate the key by the given name.
   * @exception AnyIOException if the key cannot be found
   */
//  public KeyInfo getKey (AnyString keyName) throws AnyIOException
//  {
//    try
//    {
//      KeyInfo k = (KeyInfo)_keys.get(keyName);
//      return k;
//    }
//    catch (FieldNotFoundException e)
//    {
//      throw (new AnyIOException ("Key not found: " + keyName));
//    }
//  }

  public KeyDef locateRelationshipKey (Descriptor from) throws AnyException
  {
    KeyDef ret        = null;
    startUse();
      
    try
    {
      // look for a foreign key in from which most satisfies as a retrieval
      // key in this.
      Iter fromKeys = from.getAllKeys().createIterator();
      int    lastFields = 0;
      int    thisFields = 0;
      while (fromKeys.hasNext())
      {
        KeyDef kFrom = (KeyDef)fromKeys.next();
        if (kFrom.isForeign())
        {
          Iter toKeys = getAllKeys().createIterator();
          while (toKeys.hasNext())
          {
            KeyDef kTo = (KeyDef)toKeys.next();
            if (kTo.isNative())
            {
            	Map kv = kFrom.makeKeyVal(from.getProto());
            	kv.remove(Descriptor.descriptor__);
            	if ((thisFields = kTo.satisfies(kv)) != 0)
            	{
            		if (thisFields > lastFields)
            		  ret = kTo;
            	}
            }
          }
        }
      }
      if (ret == null)
        throw new AnyException ("No key found relating " + from + " to " + this);
    }
    finally
    {
      endUse();
    }
    return ret;
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
			return new SimpleEvent(makeEventType(EventConstants.BOT_CATALOGED));
		}

		return ret;
	}

  public boolean raiseAgainstChildren(Event e)
  {
    return true;
  }

  /**
   * Cloning is supported in the sense that no exception will be thrown but, as
   * descriptors are global configuration objects we just return ourselves.  It
   * is necessary to support cloning as a descriptor can be an operand to a
   * function (for example com.inqwell.any.Read, com.inqwell.any.Aggregate) and such functions
   * are typically cloned before use as they themselves are not thread safe.
   */
  public Object clone () throws CloneNotSupportedException
  {
    return this;
  }

//	protected Object writeReplace() throws ObjectStreamException
//	{
//		ClientDescriptor cd = new ClientDescriptor(getName(),
//																							 getDefaultAlias(),
//																							 getFQName(),
//																							 getProto(),
//																							 getAllKeys(),
//																							 getFormatStrings(),
//																							 getTitleStrings(),
//																							 getEnums());
//
//
//		return cd;
//	}

  /**
   * Wait for this BOT to become idle and then clear its ready status
   */
	private void waitIdle()
	{
    if (waitReady_ != null)
      waitReady_.waitIdle();
  }
  
  private void signalReady()
  {
    if (waitReady_ != null)
      waitReady_.signalReady();
  }

  private void init()
  {
    initEvents();
		uniqueKeyFields_     = AbstractComposite.set();
		nonUniqueKeyFields_  = AbstractComposite.set();
    waitReady_           = new WaitReady(getFQName());
    fieldOrder_          = AbstractComposite.array();
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
    
    removeFromAllReferred();
  }
	
  public boolean isDefunct()
  {
    if (waitReady_ != null)
      return waitReady_.isDefunct();
    return true;   // Er, waitReady_ is never null.... fix this
  }
	
  // Process the resolved field dataField known as thisField in d as
  // fieldName in this in all this's targets (typedef and any keys).
  private void doResolvedReference(Any thisField, Any fieldName, Any dataField, Descriptor d, Any targetKey)
  {
    Set targets = (Set)targets_.get(targetKey);
    Iter i = targets.createIterator();
    while (i.hasNext())
    {
      Any target = i.next();
      
      Any initKey = new AnyString(targetKey.toString() + target.toString());
    
      Any f = dataField.cloneAny();

      if (referenceInit_ != null && referenceInit_.contains(initKey))
        f.copyFrom(referenceInit_.get(initKey));
      
        
      if (target.equals(botTarget__))
      {
        // plug in the data to the prototype
        getProto().replaceItem(fieldName, f);

        // then do format, width, title (ie label)  etc
        String format    = d.getFormat(thisField);
        if (format != null)
          this.setFormat(fieldName, new ConstString(format));

        // Width and title are always set because they are
        // returned defaults from the given descriptor.  
        // Problem? Yes....well the default title should be the fieldName
        // not the one in the given descriptor.
        // ie.  title = d.getTitle(thisField));
        int    width     = d.getWidth(thisField);
        this.setWidth(fieldName, new ConstInt(width));

        // Use a supplied label else just use the name of the field.
        Any title = null;        
        if (referenceLabel_ != null && referenceLabel_.contains(initKey))
          title = referenceLabel_.get(initKey);

	    	if (title == null)
	    	{
	    		if (fieldName.equals(thisField))
	    		{
	    			// grab the label from the given descriptor
	    			title = d.getTitle(thisField);
	    		}
	    		else
	    		{
	    			// no specific label given for the fieldName or on the underlying type
	    			// so just use the fieldName as the label
	    	    // clone just in case we ever change the label somehow.
	    	    title = fieldName.cloneAny();  
	    		}
	    	} 

        this.setTitle(fieldName, title);

        if (d.isEnum(thisField))
        {
          Any enumVals = d.getEnums().get(thisField);
          Map myEnums  = getEnums();
          myEnums.replaceItem(fieldName, enumVals);

          Any enumSyms = d.getEnumSymbols().get(thisField);
          Map mySyms   = getEnumSymbols();
          mySyms.replaceItem(fieldName, enumSyms);
        }
      }
      else
      {
        // Assume target represents a key name
        // Note key field values always default to null.
        if (f instanceof Value)
          ((Value)f).setNull();
          
        KeyDef kd = (KeyDef)getKey(target);
        kd.addResolvedReference(fieldName, f);
        
        // If this key is cached then we must place it into
        // the list inside the referred-to typedef so that
        // field changes in that typedef will purge the cache
        // of this key.
        if (kd.shouldCache() && d instanceof BOTDescriptor)
        {
          BOTDescriptor bd = (BOTDescriptor)d;
          bd.addForeignKey(this, fieldName, target);
          
          if (fkRefersTo_ == null)
            fkRefersTo_ = AbstractComposite.set();
            
          if (fkRefersTo_.contains(bd))
            fkRefersTo_.remove(bd);  // in case we are reparsing bd
          fkRefersTo_.add(bd);
        }
      }
    }
  }

  // Add the dependency that keyName in d has on fieldName in this
  private void addForeignKey(Descriptor d, Any fieldName, Any keyName)
  {
    // First fkFields_
    if (fkFields_ == null)
    {
      fkFields_ = AbstractComposite.simpleMap();
      fkTypes_  = AbstractComposite.simpleMap();
    }
    
    Map descriptors;
    
    if (fkFields_.contains(fieldName))
    {
      descriptors = (Map)fkFields_.get(fieldName);
    }
    else
    {
      descriptors = AbstractComposite.simpleMap();
      fkFields_.add(fieldName, descriptors);
    }
    
    Set keys;
    if (descriptors.contains(d))
    {
      keys = (Set)descriptors.get(d);
    }
    else
    {
      keys = AbstractComposite.set();
      descriptors.add(d, keys);
    }
    if (!keys.contains(keyName))
      keys.add(keyName);
    
    
    // Now do fkTypes_.  We could have mapped the descriptor to
    // itself, but we'll use fQName instead.
    Any fQName = d.getFQName();
    if (fkTypes_.contains(fQName))
    {
      descriptors = (Map)fkTypes_.get(fQName);
    }
    else
    {
      descriptors = AbstractComposite.simpleMap();
      fkTypes_.add(fQName, descriptors);
    }
    
    // The "descriptors" map contains two children with
    // well-known keys:
    //  "field"   : a set of field names that fQName depends on
    //  "typedef" : the descriptor typedef that refers to us
    // If its a new map then it will be empty - fill in both.
    // Otherwise add to the set of fields (if not already there)
    if (descriptors.entries() == 0)
    {
      Set fields = AbstractComposite.set();
      fields.add(fieldName);
      descriptors.add(field__, fields);
      descriptors.add(typedef__, d);
    }
    else
    {
      Set fields = (Set)descriptors.get(field__);
      if (!fields.contains(fieldName))
        fields.add(fieldName);
    }
  }
  
  // If this is reparsed, tear down the data structures that
  // are maintained for this in those typedefs that this
  // imports key fields from.
  private void removeFromAllReferred()
  {
    if (fkRefersTo_ != null)
    {
      try
      {
        waitIdle();
  
        Iter i = fkRefersTo_.createIterator();
        while (i.hasNext())
        {
          BOTDescriptor d = (BOTDescriptor)i.next();
          d.removeForeignKeys(this.getFQName());
          i.remove();
        }
      }
      finally
      {          
        signalReady();
      }
    }
  }
  
  // Remove the referring typedef from the fkFields_ and fkTypes_
  // structures. Called when a prior incarnation of the referring
  // typedef is reparsed, passing that typedef's fQName
  private void removeForeignKeys(Any fQName)
  {
    if (fkTypes_.contains(fQName))
    {
      Map descriptors = (Map)fkTypes_.get(fQName);
      Set fields = (Set)descriptors.get(field__);
      Descriptor d = (Descriptor)descriptors.get(typedef__);
      
      Iter i = fields.createIterator();
      while (i.hasNext())
      {
        Any field = i.next();
        removeForeignKey(field, d);
      }
      
      fkTypes_.remove(fQName);
    }
  }
  
  private void removeForeignKey(Any field, Descriptor d)
  {
    if (fkFields_.contains(field))
    {
      Map descriptors = (Map)fkFields_.get(field);
      descriptors.remove(d);
      if (descriptors.entries() == 0)
      fkFields_.remove(field);
    }
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
