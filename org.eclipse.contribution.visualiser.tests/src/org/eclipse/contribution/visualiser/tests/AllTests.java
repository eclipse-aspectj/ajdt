/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial version
 *******************************************************************************/
package org.eclipse.contribution.visualiser.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for Visualiser.tests"); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(MarkupUtilsTest.class));
		suite.addTest(new TestSuite(StripeTest.class));
		suite.addTest(new TestSuite(Views.class));
		suite.addTest(new TestSuite(MarkupKindsTest.class));
		//$JUnit-END$
		return suite;
	}
}
