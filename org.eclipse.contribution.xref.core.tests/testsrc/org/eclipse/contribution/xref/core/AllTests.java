/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.core;
import org.eclipse.contribution.xref.internal.core.*;
import org.eclipse.contribution.xref.internal.core.XReferenceProviderManagerTest;
import org.eclipse.contribution.xref.internal.core.XReferenceAdapterFactoryTest;
import org.eclipse.contribution.xref.internal.core.XReferenceAdapterTest;
import org.eclipse.contribution.xref.internal.core.XReferencePluginTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author hawkinsh
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.eclipse.contribution.xref.core");
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
