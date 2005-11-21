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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ajdt.core.tests.builder.AJBuilderTest;
import org.eclipse.ajdt.core.tests.builder.AJBuilderTest2;
import org.eclipse.ajdt.core.tests.builder.Bug99133Test;
import org.eclipse.ajdt.core.tests.codeconversion.CodeCheckerTest;
import org.eclipse.ajdt.core.tests.javaelements.AspectElementTests;
import org.eclipse.ajdt.core.tests.model.AJCodeElementTest;
import org.eclipse.ajdt.core.tests.model.AJComparatorTest;
import org.eclipse.ajdt.core.tests.model.AJModelPersistenceTest;
import org.eclipse.ajdt.core.tests.model.AJModelTest;
import org.eclipse.ajdt.core.tests.model.AJModelTest2;
import org.eclipse.ajdt.core.tests.model.AJModelTest3;
import org.eclipse.ajdt.core.tests.model.AJProjectModelTest;
import org.eclipse.ajdt.core.tests.model.AJProjectModelTest2;

/**
 * 
 */
public class AllCoreTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllCoreTests.class.getName());
		
		suite.addTest(new TestSuite(AJCoreTest.class));

		suite.addTest(new TestSuite(CodeCheckerTest.class));
		
		suite.addTest(new TestSuite(AspectJCorePreferencesTest.class));
	
		// model tests
		suite.addTest(new TestSuite(AJCodeElementTest.class));
		suite.addTest(new TestSuite(AJComparatorTest.class));
		suite.addTest(new TestSuite(AJModelTest.class));
		suite.addTest(new TestSuite(AJModelTest2.class));
		suite.addTest(new TestSuite(AJModelTest3.class));
		suite.addTest(new TestSuite(AJModelPersistenceTest.class));
		suite.addTest(new TestSuite(AJProjectModelTest.class));
		suite.addTest(new TestSuite(AJProjectModelTest2.class));

		// Java Elemenet tests
		suite.addTest(new TestSuite(AspectElementTests.class));
		
		// builder tests
		suite.addTest(new TestSuite(AJBuilderTest.class));
		suite.addTest(new TestSuite(AJBuilderTest2.class));
		suite.addTest(new TestSuite(Bug99133Test.class));

		return suite;
	}
}
