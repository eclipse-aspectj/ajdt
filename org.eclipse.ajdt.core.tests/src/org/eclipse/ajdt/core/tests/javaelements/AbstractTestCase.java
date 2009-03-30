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

import java.io.File;

import org.eclipse.ajdt.core.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IBuffer;

/**
 * 
 * @author Luzius Meisser
 */
public abstract class AbstractTestCase extends AJDTCoreTestCase {

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
		super.waitForAutoBuild();
	}


}
