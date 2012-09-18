/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Kris De Volder - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllPointcutDoctorTests {

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(PointcutDoctorXRefViewTest.class);
		return suite;
	}
}
