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

import org.eclipse.ajdt.core.tests.codeconversion.CodeCheckerTest;
import org.eclipse.ajdt.core.tests.model.AJCodeElementTest;
import org.eclipse.ajdt.core.tests.model.AJComparatorTest;
import org.eclipse.ajdt.core.tests.model.AJModelTest;
import org.eclipse.ajdt.core.tests.model.AJProjectModelTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 */
public class AllAJDTCoreTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllAJDTCoreTests.class.getName());
		
		suite.addTest(new TestSuite(AJCoreTest.class));

		suite.addTest(new TestSuite(CodeCheckerTest.class));
		
		// model tests
		suite.addTest(new TestSuite(AJCodeElementTest.class));
		suite.addTest(new TestSuite(AJComparatorTest.class));
		suite.addTest(new TestSuite(AJModelTest.class));
		suite.addTest(new TestSuite(AJProjectModelTest.class));
		
		return suite;
	}
}
