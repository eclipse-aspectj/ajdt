/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *	   Matthew Ford - Bug154339
 *     Helen Hawkins - updated for new ajde interface (bug 148190)
 *******************************************************************************/

package org.eclipse.ajdt.ui.tests.testutils;

import junit.framework.TestCase;

import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.ajdt.ui.tests.ajde.UICompilerFactoryTests;
import org.eclipse.ajdt.ui.tests.javamodel.Bug154339Test;

public aspect Enforcement {

	declare error: execution(* TestCase+.*(..)) 
	&& !execution(* AJDTCoreTestCase+.*(..)) 
	&& !execution(* UITestCase+.*(..)) 
	:
		"All test classes should extend AJDTCoreTestCase or UITestCase"; //$NON-NLS-1$
	
	declare error: call(* UITestCase.deleteProject(..)) && !within(UITestCase)
		&& !within(Bug154339Test) && !within(UICompilerFactoryTests):
		"Projects are automatically deleted for you at the end of each test."; //$NON-NLS-1$
}
