/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/csv/CsvIO.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:23 $
 */
 
package com.inqwell.any.io.csv;

import com.inqwell.any.*;
import com.inqwell.any.io.CSVStream;
import com.inqwell.any.io.PhysicalIO;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.nio.channels.FileChannel;

/**
 * An implementation that supports the I/O methods used to persist
 * managed object instances.
 * <p>
 * The underlying csv file is scanned for matching instances, reading
 * the whole file when the key is not unique.  When objects are written
 * the whole file is read from the beginning, replacing the object
 * to write when a match (if any) is found.
 * <p>
 * The csv file is written with the first line being headers and
 * representing the field names that the corresponding data elements
 * correspond to.
 * <p>
 * This persistence mechanism is intended for small numbers of
 * objects in the database, due to its inherent inefficiencies.
 */
public class CsvIO extends CSVStream
{
	public static Any FILE    = new ConstString("file");

  private AnyFile    file_;
  private AnyFile    backup_;
  
  // Although there may be a number of CsvIO instances, one
  // for each key, the descriptor is common to them all.
  // As there is a need to protect the underlying file
  // from multiple access, this is used to synchronize on.
  private Descriptor d_;
  private KeyDef     k_;
  
  // Can change during stream usage but if non-null means
  // we have evaluated the appropriate protos to use with the
  // when reading the current file layout.
  private AnyOrderedMap protoMap_;
  
  // When writing we may be in the unfortunate position of
  // having different headers/fields in the objects than the
  // current file.  This would happen when the object was
  // redefined and the file not yet re-written.  Once the
  // write protos are set up there is no need to do this
  // unless the object is once again redefined, in which case
  // this instance will be junked anyway.
  private AnyOrderedMap writeProtos_;
  
  private WriteConverter writeConverter_;
  private ShouldWrite    shouldWrite_;
  private AnyString      writeValue_ = new AnyString();
  
  private boolean        directoryExists_;
  
	public static ConstString bakExt__    = new ConstString(".bak");
  
	public CsvIO()
	{
	}
  
  protected boolean doCloseRead()
  {
    synchronized(d_)
    {
      protoMap_ = null;
      return super.doCloseRead();
    }
  }
  
  protected boolean doCloseWrite()
  {
    synchronized(d_)
    {
      protoMap_ = null;
      return super.doCloseWrite();
    }
  }

  public void setAuxInfo(Any a, Any subs)
  {
    if (a != null)
    {
      System.out.println ("CsvIO.setAuxInfo: " + a);
      
      if (!(a instanceof Map))
        throw new AnyRuntimeException("CsvIO aux info must be a map");
      
      Map m = (Map)a;

      // The file where our database of objects is
      if (m.contains(FILE))
        evaluateFile(m.get(FILE));
    }
  }

  public void setDescriptor(Descriptor d)
  {
    d_ = d;
    
    // Set up the prototypes in the CSVStream
    initIO();
  }
  
  public void setKey(KeyDef k)
  {
    k_ = k;
  }
  
  public Map read (Map ioKey,
									 Map outputProto) throws AnyException
	{
    // Must be unique.  The ioKey contains the fields we are interested
    // in.
    synchronized(d_)
    {
      Map ret = null;
      try
      {
        initIO();
        ret = doUniqueRead(ioKey, outputProto);
      }
      catch (AnyException e)
      {
        close();
        throw e;
      }
      return ret;
    }
  }
  
  private Map doUniqueRead(Map ioKey,
                           Map outputProto) throws AnyException
  {
    Map result = null;
    Map key;
    
    do
    {
      Array values = (Array)this.read();

      if (values == null)
        return null; // eof

      result = (Map)outputProto.cloneAny();

      setupInstance(result, values);
      
      key = k_.makeKeyVal(result);
      
    } while (!key.equals(ioKey));
    
    return result;
	}

  public int read (Map   ioKey,
                   Map   outputProto,
                   Array outputComposite,
                   int   maxCount) throws AnyException
  {
    // Non-unique
    synchronized(d_)
    {
      int numRead = 0;
      try
      {
        initIO();
        Array values = null;
        Map result = (Map)outputProto.cloneAny();
        System.out.println("read (non-unique) key " + ioKey);
  
        while ((values = (Array)this.read()) != null)
        {
          setupInstance(result, values);
  
          Map key = k_.makeKeyVal(result);
          if (key.equals(ioKey))
          {
            outputComposite.add(result.cloneAny());
            numRead++;
          }
        }
      }
      catch(AnyException e)
      {
        close();
        throw e;
      }
      return numRead;
    }
  }
  
  public boolean write (Any outputItem, Transaction t) throws AnyException
  {
		return write(null, (Map)outputItem, t);
  }
  
  public boolean write (Map ioKey,
                        Map outputItem,
                        Transaction t) throws AnyException
	{
    synchronized(d_)
    {
      System.out.println("CsvIO.write key " + ioKey);
      System.out.println("CsvIO.write val " + outputItem);
      
      boolean written = false;

      if (ioKey == null)
        ioKey = k_.makeKeyVal(outputItem);


      close();
      copyFile();

      // Read from the backup file until we find the record that matches the
      // key of the given item writing back to the main file until reached.
      // Then write new record and continue as before until eof.
      AnyFile tmp = file_;
      CSVStream writeCSV = null;
      try
      {
        // turn this into the backup file temporarily
        file_ = backup_;
        
        boolean haveFile = initIO();
        
        writeCSV = new CSVStream();
        //writeCSV.setProtos(this.getProtos());
        writeCSV.open(null, tmp, PhysicalIO.write__);
        initWriteHeaders();
        System.out.println("Writing wroteProtos_ array " + writeProtos_.getArray());
        writeCSV.write(writeProtos_.getArray(), t);
        
        Array values = null;
        Map result = null;
        
        if (haveFile)
        {
          result = (Map)outputItem.cloneAny();

          while ((values = (Array)this.read()) != null)
          {
            setupInstance(result, values);
            Map key = k_.makeKeyVal(result);
            if (key.equals(ioKey))
              break;

            // Copy the current object through to the output file
            writeObject(result, writeCSV, writeValue_, t);
          }
        }

        // Write the given object.
        writeObject(outputItem, writeCSV, writeValue_, t);

        if (haveFile)
        {
          // Write remaining objects.
          while ((values = (Array)this.read()) != null)
          {
            setupInstance(result, values);
            writeObject(result, writeCSV, writeValue_, t);
          }
        }

        writeCSV.close();
      }
      
      catch (AnyException e)
      {
        // If something went wrong with the write operation
        // then rename the backup file over the main one to
        // leave the situation untouched.  Rethrow the
        // exception.
        file_ = tmp;
        this.close();
        restoreFromBackup();
        throw e;
      }

      finally
      {
        file_ = tmp;
        this.close();
        if (writeCSV != null)
          writeCSV.close();

        written = true;
      }

      return written;
    }
	}
	
  public Object clone() throws CloneNotSupportedException
  {
    try
    {
      this.close();
    }
    catch (Exception e) {}
    
		CsvIO i   = (CsvIO)super.clone();
    i.backup_ = null;
    // Ok to share everything else at the moment but check if changes are made!
		
		return i;
  }
  
  private void evaluateFile(Any a)
  {
    try
    {
      Any file = EvalExpr.evalFunc(Transaction.NULL_TRANSACTION,
                                   a,
                                   a,
                                   StringI.class);
      if (file == null)
        throw new AnyRuntimeException("Could not evaulate CsvIO file name");
      
      file_ = new AnyFile(file.toString());
    }
    catch (AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  private boolean initIO() // throws AnyException
  {
    // Initially we don't know what order the fields in the
    // CSV file is and until we do we cannot set up prototypes
    // for the CSV parser to initialise.  Try to read the headers
    // which give us this information.
    boolean ret = true;
    
    if (protoMap_ == null)
    {
      ret = false;
      try
      {
        close();
        if (!directoryExists_)
        {
          // Create any necessary directory path
          File dir = file_.getParentFile().getFile();
          if (dir != null)
          {
            directoryExists_ = dir.mkdirs();
          }
          else
            directoryExists_ = true;
        }
        
        if (open(null, file_, PhysicalIO.read__))
        {
          setFirstLineHeaders(true);

          Array headers = (Array)read();

          if (headers != null)
          {
            System.out.println("CsvIO.initIO headers are " + headers);
            protoMap_ = new AnyOrderedMap();
            Array protos = AbstractComposite.array();
            Map m = (Map)d_.newInstance();
            
            ret = true;

            boolean hasValidFields = false;
            
            for (int i = 0; i < headers.entries(); i++)
            {
              Any k = headers.get(i);
              if (m.contains(k))
              {
                Any v = m.get(k);
                protoMap_.add(k, v);
                protos.add(v);
                hasValidFields = true;
              }
              else
              {
                // Even if the header is not in the typedef add a proto
                // for it to satisfy the csv parser.  Because there is a
                // dummy entry in the protoMap_ for it, it will not be
                // copied to the instance.
                protos.add(new AnyString());
                protoMap_.add(new AnyInt(i), null);
              }
            }
            
            if (!hasValidFields)
            {
              protoMap_ = null;
              protos    = null;
              ret       = false;
            }
            
            setProtos(protos);
          }
        }
      }

      catch (AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
    
    return ret;
  }
  
  private void setupInstance(Map instance, Array values)
  {
    System.out.println("setupInstance instance " + instance);
    System.out.println("setupInstance values   " + values);
    Array keys = protoMap_.getArray();
    System.out.println("setupInstance keys     " + keys);
    int entries = keys.entries();
    
    for (int i = 0; i < entries; i++)
    {
      Any k = keys.get(i);
      if (instance.contains(k))
        instance.get(k).copyFrom(values.get(i));
    }
    System.out.println("setupInstance returns  " + instance);
  }
  
  private void copyFile() throws AnyException
  {
    // Copy the current csv database file to a new temporary file.
    // We do this before undertaking any operations that, should they
    // fail, would destroy the database.
   
    if (backup_ == null)
    {
      Any fileName = file_.getNameNoExtension();
      Any parent   = file_.getParentPath();
      backup_ = new AnyFile(parent.toString(), fileName.toString() + bakExt__.toString());
    }
    
    if (backup_.exists())
      if(!backup_.deleteFile())
        throw new AnyException("Can't delete existing csv backup " + backup_);
    
    if (!backup_.createNewFileBoolean())
      throw new AnyException("Can't create csv backup " + backup_);
    
    if (open(null, file_, PhysicalIO.read__))
    {
      try
      {
        FileOutputStream fos = new FileOutputStream(backup_.getFile());
        FileInputStream  fis = new FileInputStream(file_.getFile());
        //System.out.println("istream_ class " + istream_.getClass());
        //BufferedInputStream bis = (BufferedInputStream)istream_;
        //FileInputStream  fis = (FileInputStream)bis.in;

        FileChannel fco = fos.getChannel();
        FileChannel fci = fis.getChannel();

        fci.transferTo(0, fci.size(), fco);
        fco.close();
        fci.close();
      }
      catch (Exception e)
      {
        throw new ContainedException(e);
      }
    }
    else
    {
      // If we couldn't open the main file then may be it doesn't exist
      // This is ok - just create a new output file
      if (!file_.createNewFileBoolean())
        throw new AnyException("Can't create new csv database file " + file_);
      
    }
  }
  
  // This is a bit naff and only exists because the BOTDescriptor does not
  // currently know about the order of its fields.  It will do for
  // now - this only happens in the unique key and there could be
  // other considerations, like transient fields, which we don't
  // currently handle.
  private void initWriteHeaders()
  {
    if (writeProtos_ == null)
    {
      writeProtos_ = new AnyOrderedMap();
      Map instance = (Map)d_.newInstance();
      
      if (shouldWrite_ == null)
        shouldWrite_ = new ShouldWrite();
      
      Iter i = instance.createKeysIterator();
      while (i.hasNext())
      {
        Any k = i.next();
        Any v = instance.get(k);
        if (shouldWrite_.shouldWrite(v))
          writeProtos_.add(k, v);
      }
    }
    System.out.println("initWriteHeaders " + writeProtos_);
  }
  
  // Write a single object as a line of csv.
  private void writeObject(Map m, CSVStream output, StringI strValue, Transaction t) throws AnyException
  {
    System.out.println("CsvIO.writeObject " + m);
    Array a = writeProtos_.getArray();
    int entries = a.entries();
    for (int i = 0; i < entries; i++)
    {
      Any value = m.get(a.get(i));
      output.write(formatWriteCell(value, strValue), t);
    }
    output.writeln(null, t);
  }
  
  private StringI formatWriteCell(Any value, StringI strValue)
  {
    if (writeConverter_ == null)
      writeConverter_ = new WriteConverter();
    
    strValue.setValue(writeConverter_.formatCSV(value));
    
    return strValue;
  }
  
  private void restoreFromBackup() throws AnyException
  {
    try
    {
      if (file_.exists())
        file_.deleteFile();

      backup_.renameTo(file_);
    }
    catch(Exception e)
    {
      throw new ContainedException(e);
    }
  }

  // A visitor to convert the fields of objects we are writing
  // to csv cell strings.  We don't use any formatters that may
  // be defined in the descriptor as these are for display formatting.
  // The csv formatting attempts to maintain precision
  private class WriteConverter extends AbstractVisitor
  {
    private String    cell_;
    private Any       anyCell_;
    private AnyFormat dateFormatter_;
    
    String formatCSV(Any a)
    {
      anyCell_ = a;
      a.accept(this);
      return getCell();
    }
    
    private String getCell()
    {
      String cell = cell_;
      cell_ = null;
      return cell;
    }
      
    public void visitAnyBoolean (BooleanI b)
    {
      cell_ = b.toString();
    }
  
    public void visitAnyByte (ByteI b)
    {
      cell_ = b.toString();
    }
  
    public void visitAnyChar (CharI c)
    {
      cell_ = c.toString();
    }
  
    public void visitAnyInt (IntI i)
    {
      cell_ = i.toString();
    }
  
    public void visitAnyShort (ShortI s)
    {
      cell_ = s.toString();
    }
  
    public void visitAnyLong (LongI l)
    {
      cell_ = l.toString();
    }
  
    public void visitAnyFloat (FloatI f)
    {
      cell_ = f.toString();
    }
  
    public void visitAnyDouble (DoubleI d)
    {
      cell_ = d.toString();
    }
  
    public void visitDecimal (Decimal d)
    {
      cell_ = d.toString();
    }
  
    public void visitAnyString (StringI s)
    {
      cell_ = s.toString();
    }
  
    public void visitAnyDate (DateI d)
    {
      // For csv persistence, dates are formatted as number of milliseconds
      // since epoch.
      if (dateFormatter_ == null)
        dateFormatter_ = AnyFormat.makeFormat(d, "#");
      
      cell_ = dateFormatter_.format(d);
    }
  
    /*
    We don't need these as ShouldWrite should have prevented them being
    entered into the writeProtos_ structure.  Default implementation
    throws an exception, which is OK should it ever happen.
    
    public void visitFunc (Func f)
    {
      cell_ = "";
    }
  
    public void visitMap (Map m)
    {
      cell_ = "";
    }

    public void visitArray (Array a)
    {
      cell_ = "";
    }

    public void visitAnyObject (AnyObject o)
    {
      cell_ = "";
    }
    
    public void visitUnknown(Any o)
    {
      cell_ = "";
    }
    */
  }
  
  private class ShouldWrite extends AbstractVisitor
  {
    private boolean   shouldWrite_;
    private Any       anyCell_;

    boolean shouldWrite(Any a)
    {
      anyCell_ = a;
      a.accept(this);
      return shouldWrite_;
    }
    
    public void visitAnyBoolean (BooleanI b)
    {
      shouldWrite_ = true;
    }

    public void visitAnyByte (ByteI b)
    {
      shouldWrite_ = true;
    }

    public void visitAnyChar (CharI c)
    {
      shouldWrite_ = true;
    }

    public void visitAnyInt (IntI i)
    {
      shouldWrite_ = true;
    }

    public void visitAnyShort (ShortI s)
    {
      shouldWrite_ = true;
    }

    public void visitAnyLong (LongI l)
    {
      shouldWrite_ = true;
    }

    public void visitAnyFloat (FloatI f)
    {
      shouldWrite_ = true;
    }

    public void visitAnyDouble (DoubleI d)
    {
      shouldWrite_ = true;
    }

    public void visitDecimal (Decimal d)
    {
      shouldWrite_ = true;
    }

    public void visitAnyString (StringI s)
    {
      shouldWrite_ = true;
    }

    public void visitAnyDate (DateI d)
    {
      shouldWrite_ = true;
    }

    public void visitMap (Map m)
    {
      shouldWrite_ = false;
    }

    public void visitArray (Array a)
    {
      shouldWrite_ = false;
    }

    public void visitFunc (Func f)
    {
      shouldWrite_ = false;
    }

    public void visitAnyObject (ObjectI o)
    {
      shouldWrite_ = false;
    }

    public void visitUnknown(Any o)
    {
      shouldWrite_ = false;
    }
  }
}
