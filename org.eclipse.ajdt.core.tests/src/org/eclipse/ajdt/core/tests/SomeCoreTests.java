/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ajdt.core.tests.dom.rewrite.ASTRewritingPointcutDeclTest;
import org.eclipse.ajdt.core.tests.refactoring.AspectRenameRefactoringTests;

/**
 * Defines all the AJDT Core tests. This can be run with either a 1.4.2 or 1.5
 * JVM. Tests which require a 1.5 JVM are only added to the suite if a 1.5 JVM
 * is detected.
 */
public class SomeCoreTests {

	public static Test suite() {
//		boolean is50 = System.getProperty("java.version").startsWith("1.5"); //$NON-NLS-1$ //$NON-NLS-2$
		TestSuite suite = new TestSuite(SomeCoreTests.class.getName());
//
//		suite.addTest(new TestSuite(AJCoreTest.class));
//		if (is50) {
//			suite.addTest(new TestSuite(AJCoreTestJava5.class));
//		}
//		suite.addTest(new TestSuite(AJPropertiesTest.class));
//		suite.addTest(new TestSuite(BuildConfigTest.class));
//		suite.addTest(new TestSuite(CoreUtilsTest.class));
//		suite.addTest(new TestSuite(ProjectDeletionTest.class));
//		
//		// code conversion tests
//		suite.addTest(new TestSuite(AspectsConvertingParserTest.class));
//		suite.addTest(new TestSuite(CodeCheckerTest.class));
//
//		suite.addTest(new TestSuite(AspectJCorePreferencesTest.class));
//
//		// model tests
//		suite.addTest(new TestSuite(AJCodeElementTest.class));
//		suite.addTest(new TestSuite(AJComparatorTest.class));
//		suite.addTest(new TestSuite(AJModelTest.class));
//		suite.addTest(new TestSuite(AJModelTest2.class));
//		suite.addTest(new TestSuite(AJModelTest3.class));
//		suite.addTest(new TestSuite(AJModelTest4.class));
//		suite.addTest(new TestSuite(AJModelPersistenceTest.class));
//		suite.addTest(new TestSuite(AJProjectModelTest.class));
//        suite.addTest(new TestSuite(AJProjectModelTest2.class));
//		suite.addTest(new TestSuite(AJRelationshipManagerTest.class));
//		suite.addTest(new TestSuite(BinaryWeavingSupportTest.class));
		
		// core compiler configuration
//        suite.addTest(new TestSuite(CoreCompilerConfigurationTests.class));
//        suite.addTest(new TestSuite(CoreCompilerConfigurationTests2.class));
//        suite.addTest(new TestSuite(CoreCompilerFactoryTests.class));

		
		
		
		// Java Element tests
//		suite.addTest(new TestSuite(AspectElementTests.class));
//
//		// builder tests
//		suite.addTest(new TestSuite(CoreOutputLocationManagerTest.class));
//		suite.addTest(new TestSuite(AJBuilderTest.class));
//        suite.addTest(new TestSuite(AJBuilderTest2.class));
//        suite.addTest(new TestSuite(AspectPathTests.class));
//		suite.addTest(new TestSuite(Bug99133Test.class));
//        suite.addTest(new TestSuite(Bug159197Test.class));
//        suite.addTest(new TestSuite(Bug43711Test.class));

		// AST tests
		suite.addTest(new TestSuite(ASTRewritingPointcutDeclTest.class));
		
		// refactoring tests
		suite.addTest(new TestSuite(AspectRenameRefactoringTests.class));
		
		return suite;
	}
}
