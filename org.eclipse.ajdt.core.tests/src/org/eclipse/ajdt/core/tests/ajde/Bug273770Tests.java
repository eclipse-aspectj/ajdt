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
    IProject project2;
    protected void setUp() throws Exception {
        super.setUp();
        project = createPredefinedProject("Bug273770");
        project2 = createPredefinedProject("Bug273770Part2");
    }
    
    public void testExtraAspectpathEntry() throws Exception {
        String[] entries = AspectJCorePreferences.getResolvedProjectAspectPath(project);
        // note that the trailing ':' is actually a path separator character
        assertEquals("Should have found junit.jar on the resolved aspectpath", "junit.jar", entries[0].substring(entries[0].length()-"junit.jar:".length(), entries[0].length()-1));
        assertTrue("Should have only one element on the aspectpath", entries[0].indexOf(File.pathSeparator) == entries[0].length()-1);

        entries = AspectJCorePreferences.getRawProjectAspectPath(project);
        assertEquals("Should have JUnit 3 on the raw aspectpath: " + entries[0], "org.eclipse.jdt.junit.JUNIT_CONTAINER/3:", entries[0]);
    }
    
    public void testExtraInpathEntry() throws Exception {
        String[] entries = AspectJCorePreferences.getResolvedProjectInpath(project);
        // note that the trailing ':' is actually a path separator character
        assertEquals("Should have found junit.jar on the resolved inpath", "junit.jar", entries[0].substring(entries[0].length()-"junit.jar:".length(), entries[0].length()-1));
        assertTrue("Should have only one element on the resolved inpath", entries[0].indexOf(File.pathSeparator) == entries[0].length()-1);

        entries = AspectJCorePreferences.getRawProjectInpath(project);
        assertEquals("Should have JUnit 3 on the raw inpath: " + entries[0], "org.eclipse.jdt.junit.JUNIT_CONTAINER/3:", entries[0]);
    }
    
    // first test the original project
    public void testExtraAspectpathEntry2() throws Exception {
        String[] entries = AspectJCorePreferences.getResolvedProjectAspectPath(project2);
        assertTrue("Should not have found org.eclipse.jdt on the resolved aspectpath", valueNotFound(entries[0], "org.eclipse.jdt_"));
        assertEquals("Should have found org.eclipse.jdt.apt.core on the resolved aspectpath", "org.eclipse.jdt.apt.core", findValue(entries[0], "org.eclipse.jdt.apt.core_"));
        assertEquals("Should have found org.eclipse.jdt.apt.ui on the resolved aspectpath", "org.eclipse.jdt.apt.ui", findValue(entries[0], "org.eclipse.jdt.apt.ui_"));
        assertEquals("Should have found org.eclipse.jdt.core on the resolved aspectpath", "org.eclipse.jdt.core", findValue(entries[0], "org.eclipse.jdt.core_"));

    }

    public void testExtraInpathEntry2() throws Exception {
        String[] entries = AspectJCorePreferences.getResolvedProjectInpath(project2);
        assertEquals("Should have found org.eclipse.jdt on the resolved inpath", "org.eclipse.jdt", findValue(entries[0], "org.eclipse.jdt_"));
        assertEquals("Should have found org.eclipse.jdt.apt.core on the resolved inpath", "org.eclipse.jdt.apt.core", findValue(entries[0], "org.eclipse.jdt.apt.core_"));
        assertEquals("Should have found org.eclipse.jdt.apt.ui on the resolved inpath", "org.eclipse.jdt.apt.ui", findValue(entries[0], "org.eclipse.jdt.apt.ui_"));
        assertEquals("Should have found org.eclipse.jdt.core on the resolved inpath", "org.eclipse.jdt.core", findValue(entries[0], "org.eclipse.jdt.core_"));
    }

    private String findValue(String string, String toFind) {
        return string.substring(string.indexOf(toFind), string.indexOf(toFind)+toFind.length()-1);
    }
    
    private boolean valueNotFound(String string, String toFind) {
        return string.indexOf(toFind) == -1;
    }
    
}
