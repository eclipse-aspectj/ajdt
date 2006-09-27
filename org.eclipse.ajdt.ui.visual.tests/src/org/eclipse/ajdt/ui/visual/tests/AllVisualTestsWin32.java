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
 *******************************************************************************/

package org.eclipse.ajdt.ui.visual.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllVisualTestsWin32 {

	// this is the full set of tests which only runs on Windows
	public static Test suite() {
		TestSuite suite = new TestSuite(AllVisualTestsWin32.class.getName());

		suite.addTest(new TestSuite(AJDocTest.class));
		suite.addTest(new TestSuite(AspectJBreakpointKeyboardActionTest.class));
		suite.addTest(new TestSuite(ExampleProjectsTest.class));
		suite.addTest(new TestSuite(Bug98663Test.class));
		suite.addTest(new TestSuite(Bug100018Test.class));
		suite.addTest(new TestSuite(ChangeFileExtensionTest.class));
		suite.addTest(new TestSuite(OutjarLaunchingTest.class));
		suite.addTest(new TestSuite(LoadTimeWeavingTest.class));
		suite.addTest(new TestSuite(DeleteAJTest.class));
		suite.addTest(new TestSuite(EagerParsingTest.class));
		suite.addTest(new TestSuite(LinkWithEditorTest.class));
		suite.addTest(new TestSuite(NewAspectWizardTest.class));
		suite.addTest(new TestSuite(OrganiseImportsTest.class));
		suite.addTest(new TestSuite(OrganiseImportsTest2.class));
		suite.addTest(new TestSuite(OpenDeclarationTest.class));
		suite.addTest(new TestSuite(OpenTypesTest.class));
		suite.addTest(new TestSuite(AspectpathAndInpathTest.class));
		suite.addTest(new TestSuite(UserAopFileTest.class));
		suite.addTest(new TestSuite(UserAopFileExculdeAspectTest.class));
//		suite.addTest(new TestSuite(RefactoringParticipationTest.class));
		
		// xref view tests
		suite.addTest(AllXRefVisualTests.suite());

		suite.addTest(new TestSuite(CustomFilterDialogTest.class));	

		// AspectJ Outline view tests
		suite.addTest(new TestSuite(AJInplaceOutlineTest.class));
		
		return suite;
	}
}
