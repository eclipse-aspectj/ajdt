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
package org.eclipse.contribution.xref.ui;

import org.eclipse.contribution.xref.internal.ui.views.NavigationHistoryManagerTest;
import org.eclipse.contribution.xref.internal.ui.views.XReferenceViewTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author hawkinsh
 *
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite =
			new TestSuite("Test for org.eclipse.contribution.xref.ui");
		//$JUnit-BEGIN$
		suite.addTestSuite(XReferenceViewTest.class);
		suite.addTestSuite(NavigationHistoryManagerTest.class);
		//$JUnit-END$
		return suite;
	}
}
