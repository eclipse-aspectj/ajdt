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

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.core.tests.testutils.TestLogger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

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
	    IFile toDelete = p.getFile("src/todelete/SingleSourceFile.java"); //$NON-NLS-1$
	    toDelete.delete(true, false, null);
        waitForAutoBuild();

        IFile shouldNotExist = p.getFile("bin/todelete/SingleSourceFile.class"); //$NON-NLS-1$
        assertFalse("Class file associated with Java file not removed.", //$NON-NLS-1$
                shouldNotExist.exists());
	}
	
	public void testDeleteSourceFileWithInner() throws CoreException {
        IFile toDelete = p.getFile("src/todelete/SourceFileWithInnerClass.java"); //$NON-NLS-1$
        toDelete.delete(true, false, null);
        waitForAutoBuild();

        IFile shouldNotExist = p.getFile("bin/todelete/SourceFileWithInnerClass.class"); //$NON-NLS-1$
        IFile shouldNotExistInner = p.getFile("bin/todelete/SourceFileWithInnerClass$Inner.class"); //$NON-NLS-1$
        assertFalse("Class file associated with Java file not removed.", //$NON-NLS-1$
                shouldNotExist.exists());
        assertFalse("Inner class file associated with Java file not removed.", //$NON-NLS-1$
                shouldNotExistInner.exists());
	}

	
	public void testDeleteSourceFileWithNested() throws CoreException {
        IFile toDelete = p.getFile("src/todelete/SourceFileWithNestedClass.java"); //$NON-NLS-1$
        toDelete.delete(true, false, null);
        waitForAutoBuild();

        IFile shouldNotExist = p.getFile("bin/todelete/SourceFileWithNestedClass.class"); //$NON-NLS-1$
        IFile shouldNotExistInner = p.getFile("bin/todelete/Nested.class"); //$NON-NLS-1$
        assertFalse("Class file associated with Java file not removed.", //$NON-NLS-1$
                shouldNotExist.exists());
        assertFalse("Nested class file associated with Java file not removed.", //$NON-NLS-1$
                shouldNotExistInner.exists());
	}
	
}
