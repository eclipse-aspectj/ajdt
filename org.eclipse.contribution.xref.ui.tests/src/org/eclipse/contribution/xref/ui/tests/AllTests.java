/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.contribution.xref.ui.tests.views.XReferenceViewTest;

/**
 * @author hawkinsh
 *
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite =
			new TestSuite("Test for org.eclipse.contribution.xref.ui"); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTestSuite(XReferenceViewTest.class);
		suite.addTestSuite(AJXReferenceProviderTest.class);
		//$JUnit-END$
		return suite;
	}
}