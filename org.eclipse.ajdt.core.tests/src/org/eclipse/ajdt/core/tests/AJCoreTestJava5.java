/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.tests;

import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.core.resources.IProject;

/**
 * AJCoreTests that are require a 5.0 compliance setting
 * 
 */
public class AJCoreTestJava5 extends AJDTCoreTestCase {
	/**
	 * Test use of generic types in a 5.0 project
	 * 
	 * @throws Exception
	 */
	public void testHandleCreateRoundtripBug108552() throws Exception {
		// the project has a .settings file to set source level to 5.0
		IProject project = createPredefinedProject("bug108552"); //$NON-NLS-1$
		AJRelationshipType[] rels = new AJRelationshipType[] {
				AJRelationshipManager.DECLARED_ON,
				AJRelationshipManager.ASPECT_DECLARATIONS };
		AJCoreTest.compareElementsFromRelationships(rels, project);
	}
}
