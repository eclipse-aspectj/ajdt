/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.javamodel;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.test.utils.Utils;
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
			fail("AJCompilationUnit has not been created for Aspect.aj");
		
		myProject.close(null);
		Utils.waitForJobsToComplete(myProject);
		
		if (AJCompilationUnitManager.INSTANCE.getAJCompilationUnitFromCache(file) != null)
			fail("AJCompilationUnit for Aspect.aj has not been disposed when project got closed.");
		
		file = myProject.getFile("src/C.java");
		if (AJCompilationUnitManager.INSTANCE.getAJCompilationUnitFromCache(file) != null)
			fail("Could create AJCompilationUnit for non .aj file.");
		
		
		
	}

	public void testGetAJCompilationUnit() {
		
		if (AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(file) == null)
			fail("Could not create AJCompilationUnit for Aspect.aj file.");
		
		file = myProject.getFile("src/C.java");
		
		if (AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(file) != null)
			fail("Could create AJCompilationUnit for non .aj file.");
		
	}

	/*
	 * Class under test for void initCompilationUnits(IProject)
	 */
	public void testInitCompilationUnitsIProject() {
		testRemoveCUsfromJavaModel();
		
		AJCompilationUnitManager.INSTANCE.initCompilationUnits(myProject);
		
		if (AJCompilationUnitManager.INSTANCE.getAJCompilationUnitFromCache(file) == null)
			fail("AJCompilationUnit should have been created when opening project.");
	}

	public void testRemoveCUsfromJavaModel() {
		AJCompilationUnitManager.INSTANCE.removeCUsfromJavaModel(myProject);
		
		if (AJCompilationUnitManager.INSTANCE.getAJCompilationUnitFromCache(file) != null)
			fail("AJCompilationUnit should have been removed from cache.");

	}

	/*
	 * Class under test for void initCompilationUnits(IWorkspace)
	 */
	public void testInitCompilationUnitsIWorkspace() {
		testRemoveCUsfromJavaModel();
		
		AJCompilationUnitManager.INSTANCE.initCompilationUnits(myProject.getWorkspace());
		
		if (AJCompilationUnitManager.INSTANCE.getAJCompilationUnitFromCache(file) == null)
			fail("AJCompilationUnit should have been created when opening project.");
	}
	
	public void testIfProjectWithoutSourceFolderWorks() throws CoreException{
		IProject project = Utils.getPredefinedProject("WithoutSourceFolder", true);
		Utils.waitForJobsToComplete(project);
		IFile f = project.getFile("A.aj");
		unit = AJCompilationUnitManager.INSTANCE.getAJCompilationUnitFromCache(f);
		if (unit == null)
			fail("Compilation Unit for A.aj has not been created and inserted into the model.");
	
	}

}
