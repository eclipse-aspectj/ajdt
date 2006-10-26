/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.core.tests.model;

import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;

/**
 * Tests the public API in AJRelationshipManager
 */
public class AJRelationshipManagerTest extends AJDTCoreTestCase {

	public void testGetAllRelationshipTypes() {
		AJRelationshipType[] allRels = AJRelationshipManager
				.getAllRelationshipTypes();
		// check for a couple of well known relationships
		boolean foundAdvisedBy = false;
		boolean foundDeclaredOn = false;
		for (int i = 0; i < allRels.length; i++) {
			if (allRels[i] == AJRelationshipManager.ADVISED_BY) {
				foundAdvisedBy = true;
			} else if (allRels[i] == AJRelationshipManager.DECLARED_ON) {
				foundDeclaredOn = true;
			}
		}
		assertTrue(
				"Didn't found advised by relationship type in all relationship types", //$NON-NLS-1$
				foundAdvisedBy);
		assertTrue(
				"Didn't found declared onrelationship type in all relationship types", //$NON-NLS-1$
				foundDeclaredOn);
	}

	public void testGetInversionRelationship() {
		// test the inverse of a couple of well known relationships
		assertEquals("Incorrect inverse relationship", //$NON-NLS-1$
				AJRelationshipManager.ADVISED_BY, AJRelationshipManager
						.getInverseRelationship(AJRelationshipManager.ADVISES));
		assertEquals(
				"Incorrect inverse relationship", //$NON-NLS-1$
				AJRelationshipManager.ADVISES,
				AJRelationshipManager
						.getInverseRelationship(AJRelationshipManager.ADVISED_BY));
		assertEquals(
				"Incorrect inverse relationship", //$NON-NLS-1$
				AJRelationshipManager.DECLARED_ON,
				AJRelationshipManager
						.getInverseRelationship(AJRelationshipManager.ASPECT_DECLARATIONS));
		assertEquals(
				"Incorrect inverse relationship", //$NON-NLS-1$
				AJRelationshipManager.ASPECT_DECLARATIONS,
				AJRelationshipManager
						.getInverseRelationship(AJRelationshipManager.DECLARED_ON));
	}

	public void testIdempotency() {
		// check that inverse(inverse(rel)) == rel
		AJRelationshipType[] allRels = AJRelationshipManager
				.getAllRelationshipTypes();
		for (int i = 0; i < allRels.length; i++) {
			AJRelationshipType inv = AJRelationshipManager
					.getInverseRelationship(allRels[i]);
			AJRelationshipType inv2 = AJRelationshipManager
					.getInverseRelationship(inv);
			assertEquals(
					"Getting inverse of inverse should give original relationship type", //$NON-NLS-1$
					allRels[i], inv2);
		}
	}
}