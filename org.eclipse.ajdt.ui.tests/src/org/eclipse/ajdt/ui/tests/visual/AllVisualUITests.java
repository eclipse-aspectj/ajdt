/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.ui.tests.visual;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ajdt.ui.tests.AllUITests;

public class AllVisualUITests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllVisualUITests.class.getName());
		
		AllUITests.setupAJDTPlugin();
		
		suite.addTest(new TestSuite(AspectJBreakpointKeyboardActionTest.class));
		suite.addTest(new TestSuite(BuildConfigurationTest.class));
		suite.addTest(new TestSuite(EagerParsingTest.class));
		suite.addTest(new TestSuite(LinkWithEditorTest.class));
		suite.addTest(new TestSuite(Bug100018Test.class));
		
		// xref view tests
		suite.addTest(new TestSuite(XReferenceViewTest.class));
		suite.addTest(new TestSuite(XReferenceInplaceDialogTest.class));	

		// AspectJ Outline view tests
		suite.addTest(new TestSuite(AJInplaceOutlineTest.class));
		
		return suite;
	}
}
