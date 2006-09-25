/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.ajdt.ui.tests.wizards.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.ajdt.core.exports.AJAntScript;
import org.eclipse.ajdt.core.exports.AJCTask;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

/**
 * Basic test for the AJC task to test that the xml produced contains the
 * correct tags
 */
public class AJCTaskTest extends UITestCase {

	private static final String srcdirEntry = "srcdirectory"; //$NON-NLS-1$

	private File tempFile;

	private AJCTask task;

	private AJAntScript script;

	private FileOutputStream os;

	protected void setUp() throws Exception {
		URL location = FileLocator.resolve(Platform.getBundle(
				"org.eclipse.ajdt.ui.tests").getEntry("/")); //$NON-NLS-1$ //$NON-NLS-2$
		URL fileURL = new URL(location, "temp.xml"); //$NON-NLS-1$
		tempFile = new File(fileURL.getPath());

		tempFile.createNewFile();
		os = new FileOutputStream(tempFile);
		script = new AJAntScript(os);
		task = new AJCTask(null,new String[]{});
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
			if (line.indexOf("fork") != -1) { //$NON-NLS-1$
				if (line.indexOf("true") != -1) { //$NON-NLS-1$
					fork = true;
				}
			}
			if (line.indexOf("iajc") != -1) { //$NON-NLS-1$
				containsIajc = true;
			}
			line = reader.readLine();
		}
		if (!fork) {
			fail("fork needs to be set to true"); //$NON-NLS-1$
		}
		if (!containsIajc) {
			fail("The iajc ant task should be used"); //$NON-NLS-1$
		}
		stream.close();
	}

	public void testSetClasspath() throws Exception {
		InputStream stream;

		stream = new FileInputStream(tempFile);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		boolean forkclasspath = false;
		String line = reader.readLine();
		while (line != null) {
			if (line.indexOf("forkclasspath") != -1) { //$NON-NLS-1$
				forkclasspath = true;
			}
			line = reader.readLine();
		}
		if (!forkclasspath) {
			fail("There should be a forkclasspath entry in the script."); //$NON-NLS-1$
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
			if (line.indexOf("srcdir") != -1) { //$NON-NLS-1$
				srcdir = true;
			}
			if (srcdir) {
				if (line.indexOf("path") != -1 //$NON-NLS-1$
						&& line.indexOf(srcdirEntry) != -1) {
					foundsrcdirEntry = true;
				}
			}
			line = reader.readLine();
		}
		if (!srcdir) {
			fail("There should be a srcdir entry in the script."); //$NON-NLS-1$
		}
		if (!foundsrcdirEntry) {
			fail("The srcdir entry should contain the attribute: \"" //$NON-NLS-1$
					+ srcdirEntry + "\""); //$NON-NLS-1$
		}
		stream.close();
	}

	protected void tearDown() {
		tempFile.deleteOnExit();
	}

}