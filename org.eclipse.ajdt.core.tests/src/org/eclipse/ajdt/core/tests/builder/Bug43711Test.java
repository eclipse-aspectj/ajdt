/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.core.tests.builder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.core.tests.testutils.ReaderInputStream;
import org.eclipse.ajdt.core.tests.testutils.TestLogger;
import org.eclipse.ajdt.core.tests.testutils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Bug 43711 - When a Java source file is deleted, 
 * the corresponding class file should be deleted during the
 * next incremental build
 * 
 * Also, inner classes must be deleted.
 * 
 * Also, nested classes must be deleted.  However, we are not 
 * checking for this.  
 */
public class Bug43711Test extends AJDTCoreTestCase {
	
	IProject p;
	TestLogger testLog;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		testLog = new TestLogger();
		AspectJPlugin.getDefault().setAJLogger(testLog);
		p = createPredefinedProject("Bug43711DeleteSourceFile"); //$NON-NLS-1$
		waitForAutoBuild();
		waitForAutoBuild();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		AspectJPlugin.getDefault().setAJLogger(null);
		testLog = null;
	}
	
	public void testDeleteSourceFile() throws CoreException {
	    IFile toDelete = p.getFile("src/todelete/SingleSourceFile.java");
	    toDelete.delete(true, false, null);
        waitForAutoBuild();

        IFile shouldNotExist = p.getFile("bin/todelete/SingleSourceFile.class");
        assertFalse("Class file associated with Java file not removed.",
                shouldNotExist.exists());
	}
	
	public void testDeleteSourceFileWithInner() throws CoreException {
        IFile toDelete = p.getFile("src/todelete/SourceFileWithInnerClass.java");
        toDelete.delete(true, false, null);
        waitForAutoBuild();

        IFile shouldNotExist = p.getFile("bin/todelete/SourceFileWithInnerClass.class");
        IFile shouldNotExistInner = p.getFile("bin/todelete/SourceFileWithInnerClass$Inner.class");
        assertFalse("Class file associated with Java file not removed.",
                shouldNotExist.exists());
        assertFalse("Inner class file associated with Java file not removed.",
                shouldNotExistInner.exists());
	}

	
	public void testDeleteSourceFileWithNested() throws CoreException {
        IFile toDelete = p.getFile("src/todelete/SourceFileWithNestedClass.java");
        toDelete.delete(true, false, null);
        waitForAutoBuild();

        IFile shouldNotExist = p.getFile("bin/todelete/SourceFileWithNestedClass.class");
        IFile shouldNotExistInner = p.getFile("bin/todelete/Nested.class");
        assertFalse("Class file associated with Java file not removed.",
                shouldNotExist.exists());
        assertFalse("Nested class file associated with Java file not removed.",
                shouldNotExistInner.exists());
	}
	
}
