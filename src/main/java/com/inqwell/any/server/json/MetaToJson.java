/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.server.json;

import java.io.ByteArrayOutputStream;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.BreadthFirstIter;
import com.inqwell.any.BuildNodeMap;
import com.inqwell.any.Catalog;
import com.inqwell.any.ConstInt;
import com.inqwell.any.ConstString;
import com.inqwell.any.Descriptor;
import com.inqwell.any.Iter;
import com.inqwell.any.KeyDef;
import com.inqwell.any.Map;
import com.inqwell.any.Service;
import com.inqwell.any.Set;
import com.inqwell.any.Transaction;
import com.inqwell.any.io.XMLXStream;

/**
 * Traverse the <code>$catalog</code> and generate an inq structure
 * representing the currently loaded <code>typedef</code>s. Callers
 * may then convert this into their required format, for example
 * XML or JSON.
 *  
 * @author tom
 */
public class MetaToJson extends Service
{
  private static Any servicePath__ = new ConstString("system.server.services.metaToJson");

  public MetaToJson() throws AnyException
  {
    init();
  }

  private void init() throws AnyException
  {
		setExpr(new MetaToJsonExec());

		Catalog.catalog(this, servicePath__.toString(), Transaction.NULL_TRANSACTION);
		setBaseURL(new ConstString("internal://MetaToJson"));
		setFQName(new ConstString("system:MetaToJson"));
	}
	
  static private class MetaToJsonExec extends    AbstractFunc
                                      implements Cloneable
  {
  	private static final Any INQ_PROTO = new ConstString("inqProto"); 
  	private static final Any KEYS      = new ConstString("keys"); 
  	private static final Any ISNUMERIC = new ConstString("isNumeric"); 
  	private static final Any FORMAT    = new ConstString("format"); 
  	private static final Any LABEL     = new ConstString("label"); 
  	private static final Any DEFVAL    = new ConstString("defVal");
  	private static final Any WIDTH     = new ConstString("width");
  	private static final Any ISUNIQUE  = new ConstString("isUnique"); 
  	
  	private BuildNodeMap         bn_;
  	private XMLXStream.IsNumeric isNumeric_;
  
  	@Override
  	public Any exec(Any a) throws AnyException
  	{
  		Set s = AbstractComposite.set();
  
  		BreadthFirstIter i = new BreadthFirstIter(Catalog.instance().getCatalog());
      i.setCyclicSafe(true);
  
  		while (i.hasNext())
  		{
  			Any any = i.next();
  
  			if (any instanceof Descriptor)
  			  s.add(any);
  		}
  		
    	bn_ = new BuildNodeMap();
    	isNumeric_ = new XMLXStream.IsNumeric(); 

  		// Root of resulting structure
  		Map root = AbstractComposite.simpleMap();
  		Iter ii = s.createIterator();
  		while (ii.hasNext())
  			typedef(root, (Descriptor)ii.next());
  		
  		// Now convert to Json (actually callers do this (or whatever
  		// they want)
//  		XMLXStream strm = new XMLXStream();
//  		strm.setJsonOutput(true);
//  		strm.setFormatOutput(false);
//  
//  		ByteArrayOutputStream bs = new ByteArrayOutputStream();
//  		strm.setStreams(null, bs);
//  		strm.write(root, null);
//  		strm.close();
//  		ConstString ret = new ConstString(bs.toString());
  		
  		return root;
  	}
  	
  	private void typedef(Map root, Descriptor d) throws AnyException
  	{
  		// Remove the "types" name space that the parser puts in. We don't
  		// need this in JSON because we are never going to send functions
  		// or services there....
  		String fqName = d.getFQName().toString().replace(".types", "");
  		
  		// Create the node representing this typedef
  		Map thisType = AbstractComposite.simpleMap();
  		
  		// Add the prototype
  		proto(thisType, d);
  		keys(thisType, d);
  		
  	  // Create the structure to the meta data using the typedef's
  		// package
  		bn_.build(fqName, thisType, root);
  	}
  	
  	/**
  	 * Convert the typedef's prototype into a structure suitable for the
  	 * chosen representation in JSON. This is of the form:
  	 * <pre>
  	 *   inqProto
  	 *     |
  	 *     -----[FieldName]
  	 *              |
  	 *              -----format : [format-string]
  	 *              |
  	 *              -----width : [width-hint]
  	 *              |
  	 *              -----label : [label-string]
  	 *              |
  	 *              -----defVal : [ default value or undefined if null ]
  	 *              |
  	 *              -----isNumeric : [ true | false ]
  	 * 
  	 * </pre>
  	 * @param thisType Map to place inqProto in
  	 * @param d the typedef
  	 */
  	private void proto(Map thisType, Descriptor d)
  	{
  		// Create the node representing this typedef
  		Map protoMeta = AbstractComposite.simpleMap();
  		
  		Map proto = d.getProto();
  		
  		Iter i = proto.createKeysIterator();
  		while (i.hasNext())
  		{
  			Any fieldName = i.next();
  			
  			field(protoMeta, fieldName, proto.get(fieldName), d.getFormat(fieldName),
              d.getWidth(fieldName), d.getTitle(fieldName));
  		}
  		
  		thisType.add(INQ_PROTO, protoMeta);
  	}
  	
  	private void field(Map protoMeta, Any name, Any value, String formatStr, int width, Any label)
  	{
  		Map fieldMeta = AbstractComposite.simpleMap();
  		
  		fieldMeta.add(FORMAT, new ConstString(formatStr));
  		fieldMeta.add(LABEL, label);
  		fieldMeta.add(DEFVAL, value);
  		fieldMeta.add(WIDTH, new ConstInt(width));
  		value.accept(isNumeric_);
  		fieldMeta.add(ISNUMERIC, isNumeric_.isNumeric() ? AnyBoolean.TRUE : AnyBoolean.FALSE);
  		
  		protoMeta.add(name, fieldMeta);
  	}
  	
  	/**
  	 * 
  	 * @param thisType Map to place keys metadata in
  	 * @param d the typedef
  	 */
  	private void keys(Map thisType, Descriptor d)
  	{
  		Map allKeys = d.getAllKeys();
  		if (allKeys != null)
  		{
    		Map keys = AbstractComposite.simpleMap();
    		
    		KeyDef pkey = d.getPrimaryKey();
    		key(keys, KeyDef.primaryKey__, pkey);
    
    		Iter i = allKeys.createKeysIterator();
    		while (i.hasNext())
    		{
    			Any keyName = i.next();
    			KeyDef key = (KeyDef)allKeys.get(keyName);
    			if (key == pkey)
    				continue;
    			key(keys, keyName, key);
    		}
    		
    		thisType.add(KEYS, keys);
  		}
  	}
  	
  	private void key(Map keys, Any keyName, KeyDef key)
  	{
  		Map keyMeta = AbstractComposite.simpleMap();
  		
  		keyProto(keyMeta, key);
  		
  		keys.add(keyName, keyMeta);
  	}
  	
  	private void keyProto(Map keyMeta, KeyDef key)
  	{
  		Map proto = key.getKeyProto();
  		
  		keyMeta.add(INQ_PROTO, proto);
  		
  		keyMeta.add(ISUNIQUE, key.isUnique() ? AnyBoolean.TRUE : AnyBoolean.FALSE);
  	}

		public Object clone() throws CloneNotSupportedException
		{
			return super.clone();
		}
  }
}
