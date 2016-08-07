/**
 * Copyright (C) 2012 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.scripts.interactive;

import org.junit.Assert;
import org.junit.Test;

import com.inqwell.any.Array;
import com.inqwell.any.AnyFile;
import com.inqwell.any.parser.InteractiveTestSupport;

/**
 * Various forms of file handling
 * @author tom
 *
 */
public class FilesTest extends InteractiveTestSupport
{
	/**
	 * Interrogate the various supported properties of a given file.
	 * This regression uses pom.xml in the project root.
	 */
  @Test
  public void fileInfoTest()
  {
  	String args[] = { "-in", "src/main/examples/files/fileinfo.inq", "pom.xml" };
  	run(args);
  }

	/**
	 * Read a pre-canned CSV file.
	 */
  @Test
  public void csvTest()
  {
  	String args[] = { "-in", "src/main/examples/files/csvtest.inq" };
  	run(args);
  }

	/**
	 * Run the concatenate files test.
	 */
  @Test
  public void fixPixTest()
  {
  	String args[] = { "-in", "src/main/examples/files/fixpix.inq" };
  	Array outfiles = (Array)run(args);
  	Assert.assertEquals(4554752, ((AnyFile)outfiles.get(0)).getFile().length());
  	Assert.assertEquals(3637248, ((AnyFile)outfiles.get(1)).getFile().length());
  	Assert.assertEquals(3702784, ((AnyFile)outfiles.get(2)).getFile().length());
  }
}
