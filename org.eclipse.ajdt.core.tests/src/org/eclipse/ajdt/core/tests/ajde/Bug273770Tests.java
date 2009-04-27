/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *         Andrew Eisenberg - Initial implementation
 ******************************************************************************/
package org.eclipse.ajdt.core.tests.ajde;

import java.io.File;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IProject;

/**
 * 
 * @author Andrew Eisenberg
 * @created Apr 8, 2009
 * Tests to ensure that extra aspectpath and inpath entries
 * are correctly added to the aspectpath and inpath
 * 
 * Only tests non-UI portion of this bug.
 */
public class Bug273770Tests extends AJDTCoreTestCase {
    IProject project;
    protected void setUp() throws Exception {
        super.setUp();
        project = createPredefinedProject("Bug273770");
    }
    
    public void testExtraAspectpathEntry() throws Exception {
        String[] entries = AspectJCorePreferences.getResolvedProjectAspectPath(project);
        assertEquals("Should have found junit.jar on the resolved aspectpath", "junit.jar", entries[0].substring(entries[0].length()-"junit.jar:".length(), entries[0].length()-1));
        assertTrue("Should have only one element on the aspectpath", entries[0].indexOf(File.pathSeparator) == entries[0].length()-1);

        entries = AspectJCorePreferences.getRawProjectAspectPath(project);
        assertEquals("Should have no elements on the raw aspectpath", 0, entries[0].length());
    }
    
    public void testExtraInpathEntry() throws Exception {
        String[] entries = AspectJCorePreferences.getResolvedProjectInpath(project);
        assertEquals("Should have found junit.jar on the resolved inpath", "junit.jar", entries[0].substring(entries[0].length()-"junit.jar:".length(), entries[0].length()-1));
        assertTrue("Should have only one element on the resolved inpath", entries[0].indexOf(File.pathSeparator) == entries[0].length()-1);

        entries = AspectJCorePreferences.getRawProjectInpath(project);
        assertEquals("Should have no elements on the raw inpath", 0, entries[0].length());
    }
    
}
