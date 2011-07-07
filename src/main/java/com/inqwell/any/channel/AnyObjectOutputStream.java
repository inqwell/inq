/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/channel/AnyObjectOutputStream.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import java.io.ObjectOutputStream;
import java.io.IOException;
import com.inqwell.any.Composite;

/**
 * Puts the desired functionality on top of standard JDK
 * serialization to ensure Any structures are handled properly.
 */
public class AnyObjectOutputStream implements ObjectOutput
{
	// Our delegate stream
	private ObjectOutputStream stream_;
	
	private boolean seenRootParentable_ = false;

	public AnyObjectOutputStream(ObjectOutputStream s)
	{
		stream_ = s;
	}
	
	/**
	 * Write an object to the underlying storage or stream.  The
	 * class that implements this interface defines how the object is
	 * written.
	 * <p>
	 * What we do here is check
	 * to see if this object is the first parentable composite
	 * we are trying to serialize in this session and if so,
	 * temporarily set its parent to null.  This avoids the
	 * inadvertent serialization of the entire universe reachable
	 * through the parent link, but we want these links preserved
	 * in the children.
	 * <p>
	 * Author's note - same code is in channel drivers!
	 *
	 * @exception IOException Any of the usual Input/Output related exceptions.
	 */
	public void writeObject(Object obj) throws IOException
	{
		if (obj instanceof Composite)
		{
			// If we are sending a composite structure then ensure
			// the root has no parent while we are sending, or
			// we will send the entire universe referencable from
			// this root!
			Composite c = (Composite)obj;
			if (c.isParentable())
			{
				synchronized(obj)
				{
					Composite p = c.getParentAny();
					c.setParent(null);
					stream_.writeObject(obj);
					c.setParent(p);
				}
			}
			else
			{
				// Not parentable so just send
				stream_.writeObject(obj);
			}
		}
		else
		{
			stream_.writeObject(obj);
		}
	}

	/**
	 * Writes a byte. This method will block until the byte is actually
	 * written.
	 * @param b	the byte
	 * @exception IOException If an I/O error has occurred.
	 */
	public void write(int b) throws IOException
	{
		stream_.write(b);
	}

	/**
	 * Writes an array of bytes. This method will block until the bytes
	 * are actually written.
	 * @param b	the data to be written
	 * @exception IOException If an I/O error has occurred.
	 */
	public void write(byte b[]) throws IOException
	{
		stream_.write(b);
	}

	/**
	 * Writes a sub array of bytes.
	 * @param b	the data to be written
	 * @param off	the start offset in the data
	 * @param len	the number of bytes that are written
	 * @exception IOException If an I/O error has occurred.
	 */
	public void write(byte b[], int off, int len) throws IOException
	{
		stream_.write(b, off, len);
	}

	/**
	 * Flushes the stream. This will write any buffered
	 * output bytes.
	 * @exception IOException If an I/O error has occurred.
	 */
	public void flush() throws IOException
	{
		stream_.flush();
	}

	/**
	 * Closes the stream. This method must be called
	 * to release any resources associated with the
	 * stream.
	 * @exception IOException If an I/O error has occurred.
	 */
	public void close() throws IOException
	{
		stream_.close();
	}

	public void reset() throws IOException
	{
		stream_.reset();
	}

	public void writeBoolean(boolean v) throws IOException
	{
		stream_.writeBoolean(v);
	}

	public void writeByte(int v) throws IOException
	{
		stream_.writeByte(v);
	}

	public void writeShort(int v) throws IOException
	{
		stream_.writeShort(v);
	}

	public void writeChar(int v) throws IOException
	{
		stream_.writeChar(v);
	}

	public void writeInt(int v) throws IOException
	{
		stream_.writeInt(v);
	}

	public void writeLong(long v) throws IOException
	{
		stream_.writeLong(v);
	}

	public void writeFloat(float v) throws IOException
	{
		stream_.writeFloat(v);
	}

	public void writeDouble(double v) throws IOException
	{
		stream_.writeDouble(v);
	}

	public void writeBytes(String s) throws IOException
	{
		stream_.writeBytes(s);
	}

	public void writeChars(String s) throws IOException
	{
		stream_.writeChars(s);
	}

	public void writeUTF(String str) throws IOException
	{
		stream_.writeUTF(str);
	}
}
