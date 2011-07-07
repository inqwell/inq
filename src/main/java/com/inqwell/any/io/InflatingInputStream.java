/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/InflatingInputStream.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
 
package com.inqwell.any.io;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.zip.Inflater;
import java.util.zip.DataFormatException;
import java.io.FileOutputStream;

/**
 * The class implements an inflating input stream. An instance
 * of java.util.zip.Inflater is used to inflate data previously
 * written by a <code>CompressingOutputStream</code>.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class InflatingInputStream extends FilterInputStream
{
  /**
   * The internal buffer where the decompressed data is stored.
   * This may change its size as the data is decompressed.
   */
  protected byte buf_[];
  
    /**
     * The index one greater than the index of the last valid byte in 
     * the buffer. 
     * This value is always
     * in the range <code>0</code> through <code>buf.length</code>;
     * elements <code>buf[0]</code>  through <code>buf[count-1]
     * </code>contain buffered input data obtained
     * from the underlying input stream.
     */
  protected int count_;

   /**
     * The current position in the buffer. This is the index of the
     * next character to be read from the <code>buf_</code> array. 
     * <p>
     * This value is always in the range <code>0</code>
     * through <code>count_</code>. If it is less
     * than <code>count_</code>, then  <code>buf_[pos_]</code>
     * is the next byte to be supplied as input;
     * if it is equal to <code>count</code>, then
     * the  next <code>read</code> or <code>skip</code>
     * operation will require more bytes to be
     * read from the contained  input stream.
     */
  protected int pos_;

  private Inflater inflater_;
  private byte     compressed_[];
  private byte     len_[];
    
  public InflatingInputStream(InputStream in)
  {
    this(in, 512);
  }
  
  public InflatingInputStream(InputStream in, int size)
  {
    super(in);
    buf_        = new byte[size];
    compressed_ = new byte[size];
    len_        = new byte[4];
    inflater_   = new Inflater();
    //System.out.println("CompressingOutputStream buf size is " + size);
  }
  
  /**
   * Returns the number of bytes that can be read (or skipped over)
   * from this input stream without blocking by the next caller of
   * a method for this input stream.
   */
  public synchronized int available() throws IOException
  {
    ensureOpen();
    return (count_ - pos_) + in.available();
  }

  /**
   * Closes this input stream and releases any system resources 
   * associated with the stream. 
   *
   * @exception  IOException  if an I/O error occurs.
   */
  public synchronized void close() throws IOException
  {
    if (in == null)
      return;
      
    in.close();
    in   = null;
    buf_ = null;
  }
  
  /**
   * Marks the current position in this input stream.
   */
  public void mark(int readlimit)
  {
  }
          
  /**
   * Tests if this input stream supports the mark and reset methods. 
   */
  public boolean markSupported() 
  {
    return false;
  }
  
  /**
   * Reads the next byte of data from the input stream. 
   */
  public synchronized int read() throws IOException
  {
    ensureOpen();
    if (pos_ >= count_)
    {
	    fill();
	    if (pos_ >= count_)
        return -1;
    }
    //System.out.println("read 1 pos_ is " + pos_);
    return buf_[pos_++] & 0xff;
  }

  /**
   * Reads some number of bytes from the input stream and stores
   * them into the buffer array b. 
   */
  public synchronized int read(byte[] b) throws IOException
  {
    return this.read(b, 0, b.length);
  }
  
  /**
   * Reads up to len bytes of data from the input stream into an array of bytes. 
   */
  public synchronized int read(byte[] b, int off, int len) throws IOException
  {
    ensureOpen();
    if ((off | len | (off + len) | (b.length - (off + len))) < 0)
    {
	    throw new IndexOutOfBoundsException();
    }
    else if (len == 0)
    {
	    return 0;
    }

    int ret = -1;
    //System.out.println("read 2 pos_ is " + pos_);
    //System.out.println("read 2 len is " + len);
    //System.out.println("read 2 count_ is " + count_);
    if (count_ - pos_ >= len)
    {
      // There are enough bytes in the buffer
      System.arraycopy(buf_, pos_, b, off, len);
      pos_ += len;
      ret = len;
    }
    else
    {
      // Use anything left and refill until satisfied
      ret = count_ - pos_;
      if (ret > 0)
      {
        //System.out.println("buf_.length " + buf_.length);
        //System.out.println("pos_ " + pos_);
        //System.out.println("b.length " + b.length);
        //System.out.println("off " + off);
        //System.out.println("ret " + ret);
        System.arraycopy(buf_, pos_, b, off, ret);
        len -= ret;
        off += ret;
      }
      while (len > 0)
      {
        fill();
        if (pos_ >= count_)
        {
          // If we haven't yet read anything and there's nothing from
          // the underlying stream then return -1
          if (ret == 0)
            ret = -1;
          break;
        }
          
        // pos_ always zero after successful fill()
        if (len < count_)
        {
          System.arraycopy(buf_, pos_, b, off, len);
          pos_ += len;
          ret += len;
          len = 0;
        }
        else
        {
          System.arraycopy(buf_, pos_, b, off, count_);
          len -= count_;
          pos_ = count_;
          off += count_;
          ret += count_;
        }
      }
    }
    return ret;
  }
  
  /**
   * Repositions this stream to the position at the time the mark method was last called on this input stream. 
   */
  public void reset() throws IOException
  {
    throw new IOException("reset() not supported");
  }

  /**
   * Skips over and discards n bytes of data from this input stream. 
   */
  public synchronized long skip(long n) throws IOException
  {
    ensureOpen();
    
    if (n == 0)
      return 0;
      
    long ret = -1;
    if (count_ - pos_ < n)
    {
      // There are enough bytes in the buffer
      pos_ += n;
      ret = n;
    }
    else
    {
      ret = count_ - pos_;
      n -= ret;
      pos_ = count_;
      while (n > 0)
      {
        fill();
        // pos_ always zero after fill()
        if (pos_ >= count_)
          break; // nothing read
        if (n < count_)
        {
          pos_ += n;
          ret += n;
          n = 0;
        }
        else
        {
          n -= count_;
          pos_ = count_;
          ret += count_;
        }
      }
    }
    return ret;
  }
  
  /**
   * Push the supplied array bytes into the stream. As a compromise
   * meaning we don't have to deal with buffer overflow, the stream
   * must be empty when this method is called.  This is OK as its
   * normal operation is only to buffer what it needs.
   */
  public synchronized void pushBack(byte[] b, int off, int len) throws IOException
  {
    if (pos_ != count_)
      throw new IOException("Stream not empty");
    
    pos_   = 0;
    count_ = len;
    System.arraycopy(b, off, buf_, 0, len);
  }
  
  // Get the next chunk of compreseed data from the stream, decompress
  // it and fill the read buffer, resizing if necessary
  private void fill() throws IOException
  {
    int len = readLength();

    //System.out.println("Infl length is " + len);
    
    if (len < 0)
    {
      pos_ = count_ = 0;
      return;
    }
    
    // If the current compressed data buffer is not big enough then
    // resize it.
    if (compressed_.length < len)
      compressed_ = new byte[len];
    
    // Read in the compressed data
    int readLen = 0;
    while (readLen < len)
    {
      int bytesRead = in.read(compressed_, readLen, len-readLen);
      if (bytesRead < 0)
        break;
        
      readLen += bytesRead;
    }
    
    if (readLen < len)
      throw new IOException("Premature EOF expected " + len + " got " + readLen);
    
    //System.out.println("Infl read " + len + " bytes");
    // Now inflate it
    try
    {
      inflater_.reset();
      inflater_.setInput(compressed_, 0, len);
      int inflated = inflater_.inflate(buf_);
      //System.out.println("Infl read " + len + " bytes inflated to " + inflated);
      while(!inflater_.finished())
      {
        // The buffer is full but there's more to do.  Resize the
        // decompressed data buffer and suck in the remaining
        byte buf[] = new byte[buf_.length + 256];
        System.arraycopy(buf_, 0, buf, 0, buf_.length);
        inflated += inflater_.inflate(buf,
                                      buf_.length,
                                      buf.length - buf_.length);
        buf_ = buf;
        System.out.println("********Infl resize " + buf_.length);
      }

      count_ = inflated;
      pos_   = 0;
    }
    catch(DataFormatException e)
    {
      e.printStackTrace();
      throw new IOException(e.getMessage());
    }
  }
  
  // read 4 bytes of data representing the length
  private int readLength() throws IOException
  {
    int i = in.read(len_);

    //System.out.println("Inf len_ " + len_[0] + "," + len_[1] + "," + len_[2] + "," + len_[3]);
    if (i < 0)
      return i;

    int len = len_[0] & 0xff;
    
    i = len_[1] & 0xff;
    i = i << 8;  len |= i;
    
    i = len_[2] & 0xff;
    i = i << 16; len |= i;
    
    i = len_[3] & 0xff;
    i = i << 24; len |= i;

    return len;
  }

  private void ensureOpen() throws IOException
  {
    if (in == null)
	    throw new IOException("Stream closed");
  }
}
