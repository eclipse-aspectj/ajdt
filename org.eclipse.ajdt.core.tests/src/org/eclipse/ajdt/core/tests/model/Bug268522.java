/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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

import java.util.Map;

import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;

/**
 * Tests that relationships are not accidentally removed.
 * 
 */
public class Bug268522 extends AJDTCoreTestCase {

    public void testRelationshipsAreNotDeleted() throws Exception {
        IProject proj = createPredefinedProject("Bug268522");
        AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForProject(proj);
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(proj.getFile("src/IsAdvised.java"));
        Map m = model.getRelationshipsForFile(unit, new AJRelationshipType[] {AJRelationshipManager.SOFTENS});
        assertEquals("Should not have found any relationships", 0, m.size());
        m = model.getRelationshipsForFile(unit, new AJRelationshipType[] {AJRelationshipManager.ADVISED_BY});
        assertEquals("Relationship should not have been removed", 1, m.size());
    }
}