/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matt Chapman - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.builder;

import org.eclipse.ajdt.ui.tests.UITestCase;

public class Bug151818Test extends UITestCase {

	public void testBug151818() throws Exception {
		createPredefinedProject("bug151818"); //$NON-NLS-1$
		waitForJobsToComplete();
		assertEquals("There should be one problem in the project", 1, getAllProblemViewMarkers().length); //$NON-NLS-1$
	}
	
}
