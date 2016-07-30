/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.server.servlet;

import java.io.ByteArrayOutputStream;

import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.ConstString;
import com.inqwell.any.io.XMLXStream;

/**
 * A function that transforms the Inq structure into an externalised
 * format, according to the implementation, and returns
 * the result.
 * 
 * @author tom
 *
 */
public abstract class Transformer extends AbstractFunc
{
	protected boolean writeMeta_;
	protected boolean formatOutput_;

	public Transformer()
	{
		this(false, false);
	}
	
	public Transformer(boolean writeMeta, boolean formatOutput)
	{
		writeMeta_    = writeMeta;
		formatOutput_ = formatOutput;
	}
	
	public abstract void setupStream(XMLXStream strm);
	
	@Override
	public Any exec(Any a) throws AnyException
	{
		XMLXStream s = new XMLXStream();
		
		setupStream(s);
		s.setWriteMeta(writeMeta_);
		s.setFormatOutput(formatOutput_);

		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		s.setStreams(null, bs);
		s.write(a, null);
		s.close();

		// we're all good.
		String op = bs.toString();
		
		return new ConstString(op);
	}
	
	public static class JsonTransformer extends Transformer
	{
		public JsonTransformer()
		{
			super();
		}
		
		public JsonTransformer(boolean writeMeta, boolean formatOutput)
		{
			super(writeMeta, formatOutput);
		}
		
		public void setupStream(XMLXStream strm)
		{
  		strm.setJsonOutput(true);
		}		
	}
	
	public static class XMLTransfomer extends Transformer
	{
		public XMLTransfomer()
		{
			super();
		}
		
		public XMLTransfomer(boolean writeMeta, boolean formatOutput)
		{
			super(writeMeta, formatOutput);
		}
		
		public void setupStream(XMLXStream strm)
		{
  		strm.setXmlOutput(true);
		}		
	}
}
