/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.tests;

import org.eclipse.ajdt.core.tests.builder.AJBuilderTest;
import org.eclipse.ajdt.core.tests.builder.Bug99133Test;
import org.eclipse.ajdt.core.tests.codeconversion.CodeCheckerTest;
import org.eclipse.ajdt.core.tests.model.AJCodeElementTest;
import org.eclipse.ajdt.core.tests.model.AJComparatorTest;
import org.eclipse.ajdt.core.tests.model.AJModelPersistenceTest;
import org.eclipse.ajdt.core.tests.model.AJModelTest;
import org.eclipse.ajdt.core.tests.model.AJModelTest2;
import org.eclipse.ajdt.core.tests.model.AJProjectModelTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Defines all the AJDT Core tests. This can be run with either a 1.4.2 or 1.5
 * JVM. Tests which require a 1.5 JVM are only added to the suite if a 1.5 JVM
 * is detected.
 */
public class AllCoreTests {

	public static Test suite() {
		boolean is50 = System.getProperty("java.version").startsWith("1.5");
		TestSuite suite = new TestSuite(AllCoreTests.class.getName());

		suite.addTest(new TestSuite(AJCoreTest.class));
		if (is50) {
			suite.addTest(new TestSuite(AJCoreTestJava5.class));
		}

		suite.addTest(new TestSuite(CodeCheckerTest.class));

		suite.addTest(new TestSuite(AspectJCorePreferencesTest.class));

		// model tests
		suite.addTest(new TestSuite(AJCodeElementTest.class));
		suite.addTest(new TestSuite(AJComparatorTest.class));
		suite.addTest(new TestSuite(AJModelTest.class));
		suite.addTest(new TestSuite(AJModelTest2.class));
		suite.addTest(new TestSuite(AJModelPersistenceTest.class));
		suite.addTest(new TestSuite(AJProjectModelTest.class));

		// builder tests
		suite.addTest(new TestSuite(AJBuilderTest.class));
		suite.addTest(new TestSuite(Bug99133Test.class));

		return suite;
	}
}
