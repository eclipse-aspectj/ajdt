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
package org.eclipse.ajdt.ui.tests.javamodel;

import java.io.File;

import org.eclipse.ajdt.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IBuffer;

/**
 * 
 * @author Luzius Meisser
 */
public abstract class AbstractTestCase extends UITestCase {

	protected AspectsConvertingParser myParser;
	protected IProject myProject;
	protected AJCompilationUnit unit;
	protected IBuffer buf;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		myProject = createPredefinedProject("javamodelEnhancementTesting"); //$NON-NLS-1$
		IFile f = myProject.getFile("src" + File.separator + "Aspect.aj"); //$NON-NLS-1$ //$NON-NLS-2$
		unit = AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(f);
		waitForJobsToComplete();
	}


}
