/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyFile.java $
 * $Author: sanderst $
 * $Revision: 1.8 $
 * $Date: 2011-05-03 20:50:50 $
 */
package com.inqwell.any;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.inqwell.any.io.ByteStream;
import com.inqwell.any.io.PhysicalIO;

/**
 * Wrap up the JDK <code>File</code> class.
 */
public class AnyFile extends    PropertyAccessMap
										 implements Value,
										            Cloneable
{
	private static final long serialVersionUID = 1L;

	private File file_;
  private Map  propertyMap_;

  private transient CopyFrom copier_;
  
  private static Any filter__ = new ConstString("filter__");
  private static Any md5__    = new ConstString("MD5");
  private static Any sha1__   = new ConstString("SHA-1");
  private static Any sha256__ = new ConstString("SHA-256");
  private static Any sha384__ = new ConstString("SHA-384");
  private static Any sha512__ = new ConstString("SHA-512");

  static public AnyFile toFile(Any file)
  {
    if (file == null || AnyNull.isNullInstance(file))
      return null;

    if (file instanceof AnyFile)
      return (AnyFile)file;

    return new AnyFile(file.toString());
  }

	public AnyFile() {}

	public AnyFile(File f)
	{
		file_ = f;
	}

	public AnyFile(String s)
	{
		file_ = new File(s);
	}

	public AnyFile(String parent, String child)
	{
		file_ = new File(parent, child);
	}

	public AnyFile(Any s)
	{
		this(s.toString());
	}

	public File getFile()
	{
		return file_;
	}

	public void setFile(File f)
	{
		file_ = f;
	}

  /**
   * Rebase the file represented by this to the path of base
   */
  public void setBase(Any base)
  {
    Any fileName = getName();
    AnyURL u = new AnyURL(fileName);
    // Ensure base is a URL
    AnyURL b = new AnyURL(base);
    URL url = u.getURL(b);
    try
    {
      URI uri = new URI(url.toString());
      this.setFile(new File(uri));
    }
    catch (URISyntaxException use)
    {
      throw new RuntimeContainedException(use);
    }
  }
  
  
  public boolean isNull()
  {
    return file_ == null;
  }

  public void setNull()
  {
    file_ = null;
  }

  /**
   * Override.  If the key is <code>"properties"</code> then
   * return true.
   */
  public boolean contains (Any key)
  {
    if (properties__.equals(key))
      return true;

    return false;
  }

  /**
   * Override.  If the key is <code>"properties"</code> then (make and)
   * return a property binding object.
   */
  public Any get (Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }

      return propertyMap_;
    }
    else
    {
      handleNotExist(key); // throws
      return null;
    }
  }

  public Any getIfContains(Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }

      return propertyMap_;
    }
    else
    {
      return null;
    }
  }

	/**
	 * Returns the string representation of the URL of this file.  By
	 * standardising on URLs we bridge files to all other URL specified
	 * stream types.
	 */
	public String toString()
	{
		if (getFile() == null)
			return "AnyFile:null";

		String ret = null;

		try
		{
			ret = getFile().toURI().toURL().toString();
		}
		catch (MalformedURLException e)
		{
			throw new RuntimeContainedException(e);
		}

		return ret;
	}

	public int hashCode()
	{
		return (getFile() == null) ? 0 : getFile().hashCode();
	}

  public void accept (Visitor v)
  {
    // Override from AbstractMap or equals operator doesn't work.
    // Bit messy but oh well
    if (v instanceof Equals || v instanceof Assign || v instanceof NotEquals)
      v.visitUnknown(this);
    else
      super.accept(v);
  }

	public boolean equals(Any a)
	{
	  boolean ret = false;
	  
		if (a instanceof AnyFile)
		{
		  AnyFile other = (AnyFile)a;
		  if (this.getFile() == null)
		    ret = other.getFile() == null;
		  else
		    ret = this.getFile().equals(other.getFile());
		}
	  return ret;
	}

	public Object clone() throws CloneNotSupportedException
	{
		AnyFile a = (AnyFile)super.clone();  // Underlying object remains shared
    a.propertyMap_ = null;
		return a;
	}

	public Iter createIterator () {return DegenerateIter.i__;}

	public Any copyFrom (Any a)
	{
	  if (AnyNull.isNullInstance(a))
	  {
	    this.setNull();
	    return this;
	  }
	  
    if (copier_ == null)
      copier_ = new CopyFrom();

    a.accept(copier_);

		return this;
	}

	/**
	 * Tests whether the application can read the file denoted by
	 * this abstract pathname.
	 */
  public Any getReadable()
  {
    return new AnyBoolean(getFile().canRead());
  }

  public Any getExecutable()
  {
    return new AnyBoolean(getFile().canExecute());
  }

  public void setReadableAll(BooleanI a)
  {
    File f = getFile();
    if (!f.setReadable(a.getValue(), false))
      throw new AnyRuntimeException("Unsuccessful operation");
    
  }

  public void setReadableOwner(BooleanI a)
  {
    File f = getFile();
    if (!f.setReadable(a.getValue(), true))
      throw new AnyRuntimeException("Unsuccessful operation");
    
  }

  public void setExecutableAll(BooleanI a)
  {
    File f = getFile();
    if (!f.setExecutable(a.getValue(), false))
      throw new AnyRuntimeException("Unsuccessful operation");
    
  }

  public void setExecutableOwner(BooleanI a)
  {
    File f = getFile();
    if (!f.setExecutable(a.getValue(), true))
      throw new AnyRuntimeException("Unsuccessful operation");
    
  }

	public Any getReadOnly()
	{
		return new AnyBoolean(getFile().canRead() && !getFile().canWrite());
	}

	/**
	 * Tests whether the application can modify to the file denoted
	 * by this abstract pathname.
	 */
	public Any getWritable()
	{
		return new AnyBoolean(getFile().canWrite());
	}

//	/**
//						Compares two abstract pathnames lexicographically.
//	 */
//	public int compareTo(File pathname)
//
//	/**
//	 * Compares this abstract pathname to another object.
//	 */
//	public int compareTo(Object o)

	/**
	 * Atomically creates a new, empty file named by this abstract
	 * pathname if and only if a file with this name does not yet exist.
	 */
	public Any createNewFile() throws AnyException
	{
		try
		{
			return new AnyBoolean(getFile().createNewFile());
		}
		catch (IOException e)
		{
			throw new ContainedException(e);
		}
	}

  public boolean createNewFileBoolean() throws AnyException
  {
		try
		{
			return getFile().createNewFile();
		}
		catch (IOException e)
		{
			throw new ContainedException(e);
		}
  }

	/**
	 * Creates a new empty file in the specified directory, using the
	 * given prefix and suffix strings to generate its name.
	 */
	public static AnyFile createTempFile(Any prefix,
	                                     Any suffix,
	                                     AnyFile directory) throws AnyException
	{
		try
		{
			return new AnyFile(File.createTempFile(prefix.toString(),
																						 (suffix != null) ? suffix.toString() : null,
																						 (directory != null) ? directory.getFile() : null));
		}
		catch (IOException e)
		{
			throw new ContainedException(e);
		}
	}

	/**
	 * Deletes the file or directory denoted by this abstract pathname.
	 */
	public Any delete()
	{
		return new AnyBoolean(getFile().delete());
	}

	/**
	 * Deletes the file or directory denoted by this abstract pathname.
	 */
	public boolean deleteFile()
	{
		return getFile().delete();
	}

	/**
	 * Requests that the file or directory denoted by this abstract
	 * pathname be deleted when the virtual machine terminates.
	 */
	public void setDeleteOnExit(Any deleteOnExit)
	{
    AnyBoolean b = new AnyBoolean(deleteOnExit);

    // If we are setting this property to false then have to create a
    // new underlying file object to achieve the desired result
    // (but surely this won't actually work?)
    if (b.getValue())
      getFile().deleteOnExit();
    else
      setFile(new File(file_.toString()));
	}

	/**
   * Tests whether the file denoted by this abstract pathname exists.
	 */
	public Any getExists()
	{
		return new AnyBoolean(getFile().exists());
	}

	/**
   * Tests whether the file denoted by this abstract pathname exists.
	 */
	public boolean exists()
	{
		return getFile().exists();
	}

	/**
	 * Returns the absolute form of this abstract pathname.
	 */
	public AnyFile getAbsoluteFile()
	{
		return new AnyFile(getFile().getAbsoluteFile());
	}

	/**
   * Returns the absolute pathname string of this abstract pathname.
	 */
	public Any getAbsolutePath()
	{
		return new AnyString(getFile().getAbsolutePath());
	}

	/**
   * Returns the canonical form of this abstract pathname.
	 */
	public AnyFile getCanonicalFile() throws AnyException
	{
		try
		{
			return new AnyFile(getFile().getCanonicalFile());
		}
		catch (IOException e)
		{
			throw new ContainedException(e);
		}
	}

	/**
   * Returns the canonical pathname string of this abstract pathname.
	 */
	public Any getCanonicalPath() throws AnyException
	{
		try
		{
			return new AnyString(getFile().getCanonicalPath());
		}
		catch (IOException e)
		{
			throw new ContainedException(e);
		}
	}


	/*
	 * Get the extension part of the of name of the file or directory
	 * denoted by this abstract pathname.  The extension is that sub-string
	 * of the name after the last <code>"."</code>.  If there is no
	 * extension then the empty file is returned.
	 * @return an <code>AnyFile</code> object representing the file extension.
	 * By comparing <code>AnyFile</code>s instead of strings we can
	 * take advantage of Java's case sensitivity handling for files on
	 * various platforms.
	 */
	public Any getExtension()
	{
		return getExtensionName();
	}
  
  public void setExtension(Any newExt)
  {
    String str = this.toString();
    Any ext = this.getExtension();
    if (ext != null)
    {
      String newFs = str.substring(0, str.length() - ext.toString().length() - 1);
      if (newExt.toString().length() == 0)
        copyFrom(new AnyString(newFs));
      else
        copyFrom(new AnyString(newFs + "." + newExt));
    }
    else
    {
      // We didn't originally have an extension so just append new one
      if (newExt.toString().length() != 0)
        copyFrom(new AnyString(str + "." + newExt));
    }
  }

	/**
	 * Returns the name of the file or directory denoted by this abstract
	 * pathname.
	 */
	public Any getName()
	{
		return new AnyString(getFile().getName());
	}

	/**
   * Returns the name part of the last path component without any
   * extension, that is the component after the last <code>"."</code>.
   * If there is no <code>"."</code> in the file name then this
   * method behaves the same as <code>getName()</code>
   */
	public Any getNameNoExtension()
	{
		String s = getFile().getName();
		int i = s.lastIndexOf('.');

		if (i > 0)
		{
			String name = s.substring(0, i);
			return new AnyString(name);
		}
		else
		{
			return this.getName();
		}
  }

	/**
	 * Returns the pathname string of this abstract pathname's parent,
	 * or null if this pathname does not name a parent directory.
	 */
	public Any getParentPath()
	{
		return new AnyString(getFile().getParent());
	}

	/**
   * Returns the abstract pathname of this abstract pathname's parent,
   * or null if this pathname does not name a parent directory.
	 */
	public AnyFile getParentFile()
	{
		return new AnyFile(getFile().getParentFile());
	}

	/**
   * Converts this abstract pathname into a pathname string.
	 */
	public Any getPath()
	{
		return new AnyString(getFile().getPath());
	}

	/**
   * Tests whether this abstract pathname is absolute.
	 */
	public Any getAbsolute()
	{
		return new AnyBoolean(getFile().isAbsolute());
	}

	/**
	 * Tests whether the file denoted by this abstract pathname is a directory.
	 * @return const boolean <code>true</code> if this file represents
	 * a directory, <code>false</code> if not.
	 */
	public Any getDirectory()
	{
		return getFile().isDirectory() ? AnyBoolean.TRUE
				                           : AnyBoolean.FALSE;
	}

	/**
   * Tests whether the file denoted by this abstract pathname is a normal
   * file.
   * @return native boolean
	 */
	public Any getRegularFile()
	{
	  return new AnyBoolean(getFile().isFile());
	}

	/**
   * Tests whether the file named by this abstract pathname is a hidden file.
	 */
	public Any getHidden()
	{
		return new AnyBoolean(getFile().isHidden());
	}

	/**
	 * Returns the time that the file denoted by this abstract pathname
	 * was last modified.
	 */
	public DateI getLastModified()
	{
		return new AnyDate(getFile().lastModified());
	}

	/**
   * Returns the length of the file denoted by this abstract pathname.
	 */
	public Any getLength()
	{
		return new AnyLong(getFile().length());
	}

	/**
	 * Returns an array of strings naming the files and directories in the
	 * directory denoted by this abstract pathname.
	 */
	public Array getList()
	{
		Array ret = AbstractComposite.array();

		String[] s = getFile().list();

		for (int i = 0; i < s.length; i++)
		{
			ret.add(new AnyString(s[i]));
		}

		return ret;
	}
  
  /**
   * Returns an array of AnyFile listing the available file system roots.
   */
  public Array getListRoots()
  {
    return AnyFile.listRoots();
  }

	/**
	 * If the file exists, performs a MD5 checksum on it and returns
	 * it as a hex string representation.
	 * 
	 * @return MD5 string or null if file does not exist
	 */
	public Any getMD5() throws AnyException
	{
		Any ret = null;
		
		if (getFile().exists())
		{
			ByteStream bs = new ByteStream();
			bs.setDigest(md5__);
			ret = getDigest(bs);
		}
		
		return ret;
	}
	
	/**
	 * If the file exists, performs a SHA-1 checksum on it and returns
	 * it as a hex string representation.
	 * 
	 * @return SHA-1 string or null if file does not exist
	 */
	public Any getSHA1() throws AnyException
	{
		Any ret = null;
		
		if (getFile().exists())
		{
			ByteStream bs = new ByteStream();
			bs.setDigest(sha1__);
			ret = getDigest(bs);
		}
		
		return ret;
	}
	
	/**
	 * If the file exists, performs a SHA-256 checksum on it and returns
	 * it as a hex string representation.
	 * 
	 * @return SHA-1 string or null if file does not exist
	 */
	public Any getSHA256() throws AnyException
	{
		Any ret = null;
		
		if (getFile().exists())
		{
			ByteStream bs = new ByteStream();
			bs.setDigest(sha256__);
			ret = getDigest(bs);
		}
		
		return ret;
	}
	
	/**
	 * If the file exists, performs a SHA-384 checksum on it and returns
	 * it as a hex string representation.
	 * 
	 * @return SHA-1 string or null if file does not exist
	 */
	public Any getSHA384() throws AnyException
	{
		Any ret = null;
		
		if (getFile().exists())
		{
			ByteStream bs = new ByteStream();
			bs.setDigest(sha384__);
			ret = getDigest(bs);
		}
		
		return ret;
	}
	
	/**
	 * If the file exists, performs a SHA-512 checksum on it and returns
	 * it as a hex string representation.
	 * 
	 * @return SHA-1 string or null if file does not exist
	 */
	public Any getSHA512() throws AnyException
	{
		Any ret = null;
		
		if (getFile().exists())
		{
			ByteStream bs = new ByteStream();
			bs.setDigest(sha512__);
			ret = getDigest(bs);
		}
		
		return ret;
	}
	
	/**
	 * Returns an array of abstract pathnames denoting the files
	 * in the directory denoted by this abstract pathname.
	 * <p>
	 * If the <code>fileFilter</code> property has been set then
	 * the list will be filtered accordingly.
	 */
	public Array getListFiles()
	{
		AnyFileFilter filter = null;
		// There's always a filter__ child, null == no filter.
		// Has to be like this because PropertyMap always returns true
		// for contains()
		if (propertyMap_ != null)
			filter = (AnyFileFilter)propertyMap_.get(filter__);

		File[] f;
		
		if (filter == null)
			f = getFile().listFiles();
		else
			f = getFile().listFiles((java.io.FileFilter)filter.inqFileFilter());

		Array ret = AbstractComposite.array();
		
		for (int i = 0; i < f.length; i++)
		{
			ret.add(new AnyFile(f[i]));
		}

		return ret;
	}

  public Any getFileFilter()
  {
    Any ret = null;
    
    if (propertyMap_ != null)
      ret = propertyMap_.get(filter__);
    
    return ret;
  }

	/**
	 * List the available filesystem roots.
	 */
	public static Array listRoots()
	{
		Array ret = AbstractComposite.array();

		File[] f = File.listRoots();

		for (int i = 0; i < f.length; i++)
		{
			ret.add(new AnyFile(f[i]));
		}

		return ret;
	}

	/**
   * Creates the directory named by this abstract pathname.
	 */
	public AnyBoolean mkdir()
	{
		return new AnyBoolean(getFile().mkdir());
	}

	/**
	 * Creates the directory named by this abstract pathname, including
	 * any necessary but nonexistent parent directories.
	 */
	public AnyBoolean mkdirs()
	{
		return new AnyBoolean(getFile().mkdirs());
	}

	/**
   * Renames the file denoted by this abstract pathname.
	 */
	public Any renameTo(AnyFile dest)
	{
		return new AnyBoolean(getFile().renameTo(dest.getFile()));
	}
	
	/**
	 * Sets the last-modified time of the file or directory named by
	 * this abstract pathname.
	 */
	public void setLastModified(DateI time)
	{
		if (!getFile().setLastModified(time.getTime()))
      throw new AnyRuntimeException("setLastModified failed");
	}

	/**
   * Marks the file or directory named by this abstract pathname so
   * that only read operations are allowed. Although written to be a
   * property for the purposes of the Inq language, because of the
   * underlying JDK File methods it can only be set true.  Trying to
   * set false generates a runtime exception.
	 */
	public void setReadOnly(Any readOnly)
	{
	  AnyBoolean b = new AnyBoolean(readOnly);
    if (!b.getValue())
      throw new AnyRuntimeException("Can't unset readonly");

		if (!getFile().setReadOnly())
      throw new AnyRuntimeException("setReadOnly failed");
	}
	
	public void setFileFilter(Any f)
	{
		// We don't want the overhead of storing a filter in
		// a class as commonly used as this one, so store it
		// in the property map instead.
		if (propertyMap_ == null)
			propertyMap_ = makePropertyMap();
		
		if (f == null || AnyNull.isNull(f))
		{
		  propertyMap_.replaceItem(filter__, null);
		}
		else
		{
      if (f instanceof StringI)
      {
        AnyFileFilter ff = new AnyFileFilter();
        AnyMatcher m = new AnyMatcher();
        m.setPattern(f);
        f = ff;
      }
      else if (!(f instanceof AnyFileFilter))
				throw new IllegalArgumentException(f.getClass().toString() +
						                               " not a filefilter or a string pattern");
		  
			propertyMap_.replaceItem(filter__, f);
		}
	}

  public boolean isEmpty() { return false; }

	protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	protected void emptying() {}

	/**
	 *  Make sure there is a filter__ child, because of PropertyMap.contains()
	 *  always returning true.
	 */
  protected Map makePropertyMap()
  {
  	Map m = super.makePropertyMap();
  	m.add(filter__, null);
  	return m;
  }
  

	/*
	 * Get the extension part of the of name of the file or directory
	 * denoted by this abstract pathname.  The extension is that sub-string
	 * of the name after the last <code>"."</code>.  If there is no
	 * extension then null is returned.
	 */
	private Any getExtensionName()
	{
		Any       ret = null;
		String    ext = null;

		String s = getFile().getName();
		int i = s.lastIndexOf('.');

		if (i > 0 &&  i < s.length() - 1)
		{
			ext = s.substring(i+1);
      
      // Check if there's a file separator in the string, in which
      // case its not really an extension.
      String fs  = System.getProperties().getProperty("file.separator");
      if (ext.indexOf(fs) >= 0)
        return AnyString.NULL;
      
      // If fs was "\" and its a real URL check for "/" also
      if (ext.indexOf('/') >= 0)
        return AnyString.NULL;
      
			ret = new AnyString(ext);
		}
		else
		{
			ret = AnyString.NULL;
		}

		return ret;
	}
	
	private Any getDigest(ByteStream bs) throws AnyException
	{
		Any ret = null;

		if (bs.open(Globals.getProcessForThread(Thread.currentThread()),
        this,
        PhysicalIO.read__))
		{
			// Suck everything through the digest stream
			while(bs.read() != AnyNull.instance());
			bs.close();
			AnyByteArray ab = (AnyByteArray)bs.getDigest();
			byte[] ba = ab.getValue();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < ba.length; i++)
			{
				sb.append(' ');
				byte b = ba[i];
				byte c = (byte)(((b & 0xF0) >> 4) + 0x30);
				if (c > 0x39)
					c += 7;
				sb.setCharAt(i*2, (char)c);
				
				sb.append(' ');
				c = (byte)((b & 0x0F) + 0x30);
				if (c > 0x39)
					c += 7;
				sb.setCharAt(i*2+1, (char)c);
			}
		  ret = new AnyString(sb.toString());
		}

	  return ret;
	}

  private class CopyFrom extends AbstractVisitor
  {
    public void visitAnyString (StringI s)
    {
      if (s.isNull())
        AnyFile.this.setNull();
      else
        fromString(s.toString());
    }

    public void visitMap(Map m)
    {
			if (m instanceof AnyFile)
			{
				AnyFile af = (AnyFile)m;
				AnyFile.this.file_ = af.getFile();
			}
      else
        throw new IllegalArgumentException("Illegal copy not from AnyFile");
    }
    
    public void visitAnyObject(ObjectI o)
    {
      if (o.getValue() == null)
        AnyFile.this.setNull();
      else
        fromString(o.toString());
    }
    
    private void fromString(String s)
    {
      // Expect either an absolute file URL or base on cwd otherwise.
      String cwd = System.getProperties().getProperty("user.dir");
      String fs  = System.getProperties().getProperty("file.separator");
      
      AnyURL    baseUrl = new AnyURL("file:///" + cwd + fs + "dummy");
      AnyURL    openUrl = new AnyURL(s);
      
      try
      {
        URL url = openUrl.getURL(baseUrl);
        URI uri = new URI(url.toString());
        AnyFile.this.setFile(new File(uri));
      }
      catch (URISyntaxException use)
      {
        throw new RuntimeContainedException(use);
      }
    }
  }
}

