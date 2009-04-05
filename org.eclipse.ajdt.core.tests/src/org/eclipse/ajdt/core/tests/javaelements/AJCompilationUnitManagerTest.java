/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.javaelements;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.tests.testutils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * 
 * @author Luzius Meisser
 */
public class AJCompilationUnitManagerTest extends AbstractTestCase {

	protected IFile file;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		file = (IFile)unit.getResource();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetAJCompilationUnitFromCache() throws CoreException {
	
		if (AJCompilationUnitManager.INSTANCE.getAJCompilationUnitFromCache(file) == null)
			fail("AJCompilationUnit has not been created for Aspect.aj"); //$NON-NLS-1$
		
        // XXX this test is not working any more
        // the listener for project closures sits in 
        // AJDT ui, so it is not running now
        // so instead simulate its effect when a project closes
        AJCompilationUnitManager.INSTANCE.removeCUsfromJavaModel(myProject);

		myProject.close(null);
		
        // wait for project to be closed
		int counter = 0;
		while (myProject.isOpen() && counter < 10) {
		    System.out.println("Waiting for project to close");
		    Utils.sleep(1000);
		    counter++;
		}
		
		if (AJCompilationUnitManager.INSTANCE.getAJCompilationUnitFromCache(file) != null)
			fail("AJCompilationUnit for Aspect.aj has not been disposed when project got closed."); //$NON-NLS-1$
		
		file = myProject.getFile("src/C.java"); //$NON-NLS-1$
		if (AJCompilationUnitManager.INSTANCE.getAJCompilationUnitFromCache(file) != null)
			fail("Could create AJCompilationUnit for non .aj file."); //$NON-NLS-1$
		
		
	}

	public void testGetAJCompilationUnit() {
		
		if (AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(file) == null)
			fail("Could not create AJCompilationUnit for Aspect.aj file."); //$NON-NLS-1$
		
		file = myProject.getFile("src/C.java"); //$NON-NLS-1$
		
		if (AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(file) != null)
			fail("Could create AJCompilationUnit for non .aj file."); //$NON-NLS-1$
		
	}

	/*
	 * Class under test for void initCompilationUnits(IProject)
	 */
	public void testInitCompilationUnitsIProject() {
		testRemoveCUsfromJavaModel();
		
		AJCompilationUnitManager.INSTANCE.initCompilationUnits(myProject);
		
		if (AJCompilationUnitManager.INSTANCE.getAJCompilationUnitFromCache(file) == null)
			fail("AJCompilationUnit should have been created when opening project."); //$NON-NLS-1$
	}

	public void testRemoveCUsfromJavaModel() {
		AJCompilationUnitManager.INSTANCE.removeCUsfromJavaModel(myProject);
		
		if (AJCompilationUnitManager.INSTANCE.getAJCompilationUnitFromCache(file) != null)
			fail("AJCompilationUnit should have been removed from cache."); //$NON-NLS-1$

	}

	/*
	 * Class under test for void initCompilationUnits(IWorkspace)
	 */
	public void testInitCompilationUnitsIWorkspace() {
		testRemoveCUsfromJavaModel();
		
		AJCompilationUnitManager.INSTANCE.initCompilationUnits(myProject.getWorkspace());
		
		if (AJCompilationUnitManager.INSTANCE.getAJCompilationUnitFromCache(file) == null)
			fail("AJCompilationUnit should have been created when opening project."); //$NON-NLS-1$
	}
	
	public void testIfProjectWithoutSourceFolderWorks() throws Exception{
		IProject project = createPredefinedProject("WithoutSourceFolder"); //$NON-NLS-1$
		IFile f = project.getFile("A.aj"); //$NON-NLS-1$
		unit = AJCompilationUnitManager.INSTANCE.getAJCompilationUnitFromCache(f);
		if (unit == null)
			fail("Compilation Unit for A.aj has not been created and inserted into the model."); //$NON-NLS-1$
	}

}
