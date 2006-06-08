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
package org.eclipse.contribution.xref.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author hawkinsh
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.eclipse.contribution.xref.core.tests"); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTestSuite(XReferencePluginTest.class);
		suite.addTestSuite(XReferenceTest.class);
		suite.addTestSuite(XReferenceAdapterFactoryTest.class);
		suite.addTestSuite(XReferenceAdapterTest.class);
		suite.addTestSuite(XReferenceProviderManagerTest.class);
		suite.addTestSuite(XReferenceProviderDefinitionTest.class);
		//$JUnit-END$
		return suite;
	}
}
