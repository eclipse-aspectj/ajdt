/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.ajdt.internal.ui.wizards.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.ajdt.internal.exports.AJAntScript;
import org.eclipse.ajdt.internal.exports.AJCTask;
import org.eclipse.core.runtime.Platform;

/**
 * Basic test for the AJC task to test that the xml produced contains the
 * correct tags
 */
public class AJCTaskTest extends TestCase {

	private static final String srcdirEntry = "srcdirectory";

	private File tempFile;

	private AJCTask task;

	private AJAntScript script;

	private FileOutputStream os;

	private String classpathEntry1 = "classpathEntry1/entry.jar";

	private String classpathEntry2 = "entry2$$hello";

	protected void setUp() throws Exception {
		URL location = Platform.resolve(Platform.getBundle(
				"org.eclipse.ajdt.test").getEntry("/"));
		URL fileURL = new URL(location, "temp.xml");
		tempFile = new File(fileURL.getPath());

		tempFile.createNewFile();
		os = new FileOutputStream(tempFile);
		script = new AJAntScript(os);
		task = new AJCTask();
		List classpath = new ArrayList();
		classpath.add(classpathEntry1);
		classpath.add(classpathEntry2);
		task.setClasspath(classpath);
		task.setSrcdir(new String[] { srcdirEntry });
		task.print(script);
		script.close();
		os.close();
	}

	public void testPrint() throws Exception {
		InputStream stream = new FileInputStream(tempFile);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		boolean fork = false;
		boolean containsIajc = false;
		String line = reader.readLine();
		while (line != null) {
			if (line.indexOf("fork") != -1) {
				if (line.indexOf("true") != -1) {
					fork = true;
				}
			}
			if (line.indexOf("iajc") != -1) {
				containsIajc = true;
			}
			line = reader.readLine();
		}
		if (!fork) {
			fail("fork needs to be set to true");
		}
		if (!containsIajc) {
			fail("The iajc ant task should be used");
		}
		stream.close();
	}

	public void testSetClasspath() throws Exception {
		InputStream stream;

		stream = new FileInputStream(tempFile);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		boolean forkclasspath = false;
		boolean foundclasspathentry1 = false;
		boolean foundclasspathentry2 = false;
		String line = reader.readLine();
		while (line != null) {
			if (line.indexOf("forkclasspath") != -1) {
				forkclasspath = true;
			}
			if (forkclasspath) {
				if (line.indexOf("path") != -1) {
					if (line.indexOf(classpathEntry1) != -1) {
						foundclasspathentry1 = true;
					} else if (line.indexOf(classpathEntry2) != -1) {
						foundclasspathentry2 = true;
					}
				}
			}
			line = reader.readLine();
		}
		if (!forkclasspath) {
			fail("There should be a forkclasspath entry in the script.");
		}
		if (!foundclasspathentry1) {
			fail("The classpath should contain \"" + classpathEntry1 + "\"");
		}
		if (!foundclasspathentry2) {
			fail("The classpath should contain \"" + classpathEntry2 + "\"");
		}
		stream.close();
	}

	public void testSetScrDir() throws Exception {
		InputStream stream;

		stream = new FileInputStream(tempFile);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		boolean srcdir = false;
		boolean foundsrcdirEntry = false;
		String line = reader.readLine();
		while (line != null) {
			if (line.indexOf("srcdir") != -1) {
				srcdir = true;
			}
			if (srcdir) {
				if (line.indexOf("path") != -1
						&& line.indexOf(srcdirEntry) != -1) {
					foundsrcdirEntry = true;
				}
			}
			line = reader.readLine();
		}
		if (!srcdir) {
			fail("There should be a srcdir entry in the script.");
		}
		if (!foundsrcdirEntry) {
			fail("The srcdir entry should contain the attribute: \""
					+ srcdirEntry + "\"");
		}
		stream.close();
	}

	protected void tearDown() {
		tempFile.deleteOnExit();
	}

}