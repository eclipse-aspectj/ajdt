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

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.internal.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IBuffer;

/**
 * 
 * @author Luzius Meisser
 */
public abstract class AbstractTestCase extends TestCase {

	protected AspectsConvertingParser myParser;
	protected IProject myProject;
	protected AJCompilationUnit unit;
	protected IBuffer buf;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		myProject = Utils.getPredefinedProject("javamodelEnhancementTesting", true);
		IFile f = myProject.getFile("src" + File.separator + "Aspect.aj");
		unit = AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(f);
		Utils.waitForJobsToComplete();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		Utils.deleteProject(myProject);
	}

}
