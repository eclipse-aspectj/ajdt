/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.core.tests.testutils;

import junit.framework.TestCase;

import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.core.tests.ProjectDeletionTest;
import org.eclipse.ajdt.core.tests.ajde.CoreCompilerFactoryTests;
import org.eclipse.ajdt.core.tests.model.AJModelPersistenceTest;

public aspect Enforcement {

	declare error: execution(* TestCase+.*(..)) && !execution(* AJDTCoreTestCase+.*(..)):
		"All test classes should extend AJDTCoreTestCase"; //$NON-NLS-1$

	declare error: call(* AJDTCoreTestCase.deleteProject(..)) 
	    && !within(AJDTCoreTestCase)
        && !within(AJModelPersistenceTest) // this test needs to call delete
		&& !within(ProjectDeletionTest)  // this test is specifically about project deletion
        && !within(CoreCompilerFactoryTests) : // this test needs to call delete
		"Projects are automatically deleted for you at the end of each test."; //$NON-NLS-1$
}
