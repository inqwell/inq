/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/CompressingOutputStream.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
 
package com.inqwell.any.io;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

/**
 * The class implements a compressing output stream. An instance
 * of java.util.zip.Deflater is used to compress data written
 * to this stream in units of the specified buffer size.
 * Compressed data is then accumulated until the buffer is
 * full or until the stream is flushed.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class CompressingOutputStream extends FilterOutputStream
{
  /**
   * The internal buffer where data is stored. 
   */
  protected byte buf_[];

  /**
   * The number of valid bytes in the buffer. This value is always 
   * in the range <tt>0</tt> through <tt>buf_.length</tt>; elements 
   * <tt>buf_[0]</tt> through <tt>buf_[count_-1]</tt> contain valid 
   * byte data.
   */
  protected int count_;
  
  private Deflater deflater_;
  private byte     compressed_[];
  private byte     len_[];
  
  private long     totalIn_;
  private long     totalOut_;
  
  /**
   * Creates a new CompressingOutputStream to write data to the 
   * specified underlying output stream with a default 512-byte 
   * buffer size.
   *
   * @param   out   the underlying output stream.
   */
  public CompressingOutputStream(OutputStream out)
  {
    this(out, 512);
  }

  /**
   * Creates a new CompressingOutputStream to write data to the 
   * specified underlying output stream with the specified buffer 
   * size. 
   *
   * @param   out    the underlying output stream.
   * @param   size   the buffer size.
   * @exception IllegalArgumentException if size &lt;= 0.
   */
  public CompressingOutputStream(OutputStream out, int size)
  {
    super(out);
    if (size <= 0)
    {
      throw new IllegalArgumentException("Buffer size <= 0");
    }
    buf_        = new byte[size];
    deflater_   = new Deflater(Deflater.BEST_COMPRESSION);
    compressed_ = new byte[size];  // presumably must always be less
    len_        = new byte[4];
    //System.out.println("CompressingOutputStream buf size is " + size);
  }

  /** Flush the internal buffer */
  private void flushBuffer() throws IOException
  {
    if (count_ > 0)
    {
      deflater_.reset();
      deflater_.setInput(buf_, 0, count_);
      deflater_.finish();
      int compressedDataLength = deflater_.deflate(compressed_);
      //System.out.println("WRITTEN " + count_ + " bytes compressed to " + compressedDataLength);
      writeDataLen(compressedDataLength);
      out.write(compressed_, 0, compressedDataLength);
      totalIn_ += count_;
      totalOut_ += compressedDataLength;
	    count_ = 0;
    }
  }
  
  private void writeDataLen(int len) throws IOException
  {
    len_[0] = (byte)(len & 0xff);
    len_[1] = (byte)((len >> 8)  & 0xff);
    len_[2] = (byte)((len >> 16) & 0xff);
    len_[3] = (byte)((len >> 24) & 0xff);
    out.write(len_, 0, 4);
    //System.out.println("Comp writen len " + len);
    //System.out.println("Comp as " + len_[0] + "," + len_[1] + "," + len_[2] + "," + len_[3]);
  }

  /**
   * Writes the specified byte to this buffered output stream. 
   *
   * @param      b   the byte to be written.
   * @exception  IOException  if an I/O error occurs.
   */
  public synchronized void write(int b) throws IOException
  {
    if (count_ >= buf_.length)
    {
	    flushBuffer();
    }
    buf_[count_++] = (byte)b;
  }

  /**
   * Writes <code>len</code> bytes from the specified byte array 
   * starting at offset <code>off</code> to this buffered output stream.
   *
   * <p> Ordinarily this method stores bytes from the given array into this
   * stream's buffer, flushing the buffer to the underlying output stream as
   * needed.  If the requested length is at least as large as this stream's
   * buffer, however, then this method will flush the buffer and write the
   * bytes directly to the underlying output stream.  Thus redundant
   * <code>CompressingOutputStream</code>s will not copy data unnecessarily.
   *
   * @param      b     the data.
   * @param      off   the start offset in the data.
   * @param      len   the number of bytes to write.
   * @exception  IOException  if an I/O error occurs.
   */
  public synchronized void write(byte b[], int off, int len) throws IOException
  {
    if (len > buf_.length)
    {
      // If the request length exceeds the size of the output buffer,
      // flush the output buffer and then write the data in chunks.
      flushBuffer();
      int offset = 0;
      int buflen = buf_.length;
      while (offset < len)
      {
        this.write(b, offset, buflen);
        offset += buflen;
        if (offset+buflen > len)
          buflen = len - offset;
      }
      return;
    }
    if (len > buf_.length - count_)
    {
      flushBuffer();
    }
    System.arraycopy(b, off, buf_, count_, len);
    count_ += len;
  }

  /**
   * Flushes this buffered output stream. This forces any buffered 
   * output bytes to be written out to the underlying output stream. 
   *
   * @exception  IOException  if an I/O error occurs.
   * @see        java.io.FilterOutputStream#out
   */
  public synchronized void flush() throws IOException
  {
    flushBuffer();
    out.flush();
  }
  
  public long getTotalIn()
  {
    return totalIn_;
  }
  
  public long getTotalOut()
  {
    return totalOut_;
  }
}
