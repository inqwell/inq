/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AbstractKeyDef.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

//import com.inqwell.any.util.Util;
import com.inqwell.any.io.PhysicalIO;

public abstract class AbstractKeyDef extends    AbstractAny
																		 implements KeyDef
{
  // The list of field names comprising this key.  This list
  // only holds fields that are native to this typedef
  private   Array     fields_;
  
  private   Array     allFields_;
  
  // This map will be empty if there are no references in this
  // key to fields of other types.  Otherwise it will contain the
  // resolved references and prototypical values.
	protected Map       proto_;
  
  // Maps key field name to an initial value when an explicit
  // override has been provided in the key definition.
  private   Map       initialValues_;

  private Descriptor  descriptor_;
  
  private boolean     isForeign_;
  private boolean     isNative_;
  private boolean     isVolatile_;
  private boolean     isUnique_;
  private boolean     shouldCache_ = true;
  private int         maxCount_    = 0;

	private Any         name_;

  public AbstractKeyDef()
  {
    init();
  }
  
  protected AbstractKeyDef(Any        name,
													 Descriptor descriptor,
													 Array      fields,
                           Map        proto,
													 boolean    isUnique,
													 boolean    isNative,
													 boolean    isForeign,
                           int        maxCount)
  {
		name_       = name;
		descriptor_ = descriptor;
		fields_     = fields;
    proto_      = proto;
		isUnique_   = isUnique;
		isNative_   = isNative;
		isForeign_  = isForeign;
    maxCount_   = maxCount;
  }
  
  public void write (Map instanceVal, Transaction t) throws AnyException
  {
    throw new java.lang.UnsupportedOperationException();
  }
  
  /**
   * Check if the supplied key value looks like this key definition.
   * @return true if keyVal is a map and has the same number of entries with
   * the same keys as the fields in this.
   */
  public int satisfies(Map keyVal)
  {
    System.out.println("AbstractKeyDef.satisfies " + keyVal + " " + fields_);
		if (keyVal.containsAll(fields_))
      return fields_.entries();
    else
      return 0;
  }

  public void setNative (boolean isNative)
  {
    isNative_ = isNative;
  }

  public void setVolatile (boolean isVolatile)
  {
    isVolatile_ = isVolatile;
  }

  public void setForeign (boolean isForeign)
  {
    isForeign_ = isForeign;
  }

  public void setUnique (boolean isUnique)
  {
    isUnique_ = isUnique;
  }
  
  public void setMaxCount(Any maxCount)
  {
    if (maxCount != null)
    {
      IntI i = new ConstInt(maxCount);
      maxCount_ = i.getValue();
    }
  }
  
  public int getMaxCount()
  {
    return maxCount_;
  }

  public boolean isNative ()
  {
    return isNative_;
  }

  public boolean isForeign ()
  {
    return isForeign_;
  }

  public boolean isUnique ()
  {
    return isUnique_;
  }

  public boolean isPersistent()
  {
    return false;
  }

  public boolean isVolatile()
  {
    return isVolatile_;
  }

	public void expire(Transaction t) throws AnyException
	{
	}

	public void destroy()
	{
	}

  public void purge()
  {
  }
  
  public void setName (Any name)
  {
    name_ = name;
  }
  
  public void setDescriptor (Descriptor d)
  {
    //System.out.println("AbstractKeyDef.setDescriptor " + d);
    descriptor_ = d;
  }
  
  public void setFields (Array fields)
  {
    if (proto_.containsAny(fields))
      throw new AnyRuntimeException("Duplicate key field name in " + fields);
      
    fields_ = fields;
  }

  public void setStaticticsVariables(IntI loaded, FloatI hitRate)
  {
    throw new UnsupportedOperationException();
  }
  
  public void setIO (PhysicalIO io, Descriptor d)
  {
    throw new UnsupportedOperationException();
  }

	public void setAuxInfo(Any a)
	{
    throw new UnsupportedOperationException();
	}

	public Any getAuxInfo()
	{
    throw new UnsupportedOperationException();
	}

  public boolean checkExists(Any instanceVal, Transaction t)
  {
    throw new UnsupportedOperationException();
  }
  
  public Array getFields ()
  {
    return fields_;
  }
  
  public Array getAllFields ()
  {
    return allFields_;
  }

  public void addKeyFieldName(Any name)
  {
    if (allFields_.contains(name))
      throw new AnyRuntimeException("Duplicate key field name " + name);

    allFields_.add(name);
  }

  public Any getName ()
  {
		return name_;
  }
  
  public Descriptor getDescriptor()
  {
		return descriptor_;
	}
	
  /**
   * Establish the prototype key value according to the supplied
   * fields and any additional references that have been resolved.
   */
	public void buildProto()
	{
    // Any (resolved) references are already in proto_
    // Add the fields that are part of our Descriptor proto
    if (fields_ != null && fields_.entries() != 0)
    {
      Map dProto = descriptor_.getProto();
      
      Iter i = fields_.createIterator();
      while (i.hasNext())
      {
        Any k = i.next();
        Any v;
        try
        {
          v = dProto.get(k).cloneAny();
          if (v instanceof Value)
            ((Value)v).setNull();
        }
        catch (FieldNotFoundException e)
        {
          continue;
        }
        
        if (initialValues_ != null)
        {
          Any init = initialValues_.getIfContains(k);
          if (init != null)
            v.copyFrom(init);
        }
        
        proto_.replaceItem(k, v);
      }
    }
	}
	
	public boolean shouldCache()
	{
    return shouldCache_;
	}
	
	public void addResolvedReference(Any key, Any value)
	{
    //System.out.println("addResolvedReference " + key  + " " + value);
    if (isPrimary())
      throw new AnyRuntimeException("Primary Key cannot have references");
      
    //if (fields_ != null && fields_.contains(key))
    //  throw new AnyRuntimeException("Duplicate key field name " + key);
    
    proto_.replaceItem(key, value);
    
    // Don't bother to add resolved references to fields_ - it only
    // needs to hold native key fields.
    //if (fields_.indexOf(key) < 0)
      //fields_.add(key);
      
    // Now done in parser
    //shouldCache_ = false;
	}
	
  public void addInitialiser(Any key, Any value)
  {
    if (initialValues_ == null)
      initialValues_ = AbstractComposite.simpleMap();
    
    initialValues_.add(key, value);
  }
  
	public Map makeKeyVal (Map instanceVal)
	{
		Map ret = makeKeyMap(instanceVal);
		
		// Add the descriptor to the key value to make it characteristic
		// to the type the key belongs to
		ret.add(Descriptor.descriptor__, descriptor_);
    ret.add(KeyDef.key__, name_);
		
		return ret;
	}
	
	public Map getKeyProto()
	{
		Map ret = makeKeyMap(proto_);
		return ret;
	}
	
  public void setShouldCache(boolean shouldCache)
  {
    shouldCache_ = shouldCache;
  }
  
  protected int getMaxCount(int maxCount)
  {
    if (maxCount < 0)
      return maxCount_;
    else
      return maxCount;
  }
  
	private Map makeKeyMap(Map m)
	{
		Map ret = AbstractComposite.keyMap();
		Iter i = proto_.createKeysIterator();
		
		while (i.hasNext())
		{
			Any k = i.next();
			Any v = proto_.get(k).cloneAny();
      if (m.contains(k))
			  ret.add(k, v.copyFrom(m.get(k)));
      else
        ret.add(k, v);
		}
		return ret;
	}
	
	public String toString()
	{
    return name_.toString();
		//return "KeyDef " + name_ + " " + fields_;
	}
//  public boolean isValid()
//  {
//    if (getName() == null)
//      return false;
//      
//    // Note - its OK to have a key with no fields.  Consider
//    // the Sql case like "select * from BOTCatalog"
//    
//    if (!isForeign() && !isNative() && !isUpdate() && !isUnique())
//      return false;
//
//    if (isUpdate() && !isNative())
//     return false;
//    
//    return true;
//  }
  
  public void init()
  {
    isNative_  = false;
    isForeign_ = false;
    isUnique_  = false;
    
    // in case there are no fields
    fields_    = AbstractComposite.array();
    allFields_ = AbstractComposite.array();
    proto_     = AbstractComposite.simpleMap();
  }

//  public String toString()
//  {
//    StringBuffer s = new StringBuffer();
//    
//    s.append("Name: ").append
//                       ((getName() == null) ? "null"
//                                            : getName().toString()).append
//                                                        (Util.lineSeparator());
//    s.append ("Fields: ").append  (getFields().toString()).append
//                                                        (Util.lineSeparator());
//    s.append ("Native: ").append  (_isNative).append
//                                                (Util.lineSeparator());
//    s.append ("Foreign: ").append (_isForeign).append
//                                                (Util.lineSeparator());
//    s.append ("Update: ").append  (_isUpdate).append
//                                                (Util.lineSeparator());
//    s.append ("Unique: ").append  (_isUnique).append
//                                                (Util.lineSeparator());
//
//    return s.toString();
//  }   
}
