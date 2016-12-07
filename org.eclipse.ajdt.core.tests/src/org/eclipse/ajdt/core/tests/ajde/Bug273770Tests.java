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
    
    public void xtestExtraAspectpathEntry() throws Exception {
        String[] entries = AspectJCorePreferences.getResolvedProjectAspectPath(project);
        // note that the trailing ':' is actually a path separator character
        // should filter out the hamcrest jar, but keep the junit jar
        assertTrue("Should have found junit.jar on the resolved aspectpath:\n" + entries[0], entries[0].indexOf("junit.jar") != -1);
        assertTrue("Should have found the hamcrest jar on the resolved aspectpath:\n" + entries[0], entries[0].indexOf("hamcrest") == -1);
        
        entries = AspectJCorePreferences.getRawProjectAspectPath(project);
        assertTrue("Should have JUnit 4 on the raw inpath:\n" + entries[0], entries[0].indexOf("org.eclipse.jdt.junit.JUNIT_CONTAINER/4:") >= 0);
    }
    
    public void testExtraInpathEntry() throws Exception {
        String[] entries = AspectJCorePreferences.getResolvedProjectInpath(project);
        // note that the trailing ':' is actually a path separator character
        // should filter out the hamcrest jar, but keep the junit jar
        assertTrue("Should have found junit.jar on the resolved inpath:\n" + entries[0], entries[0].indexOf("junit.jar") != -1);
        assertTrue("Should have found the hamcrest jar on the resolved inpath:\n" + entries[0], entries[0].indexOf("hamcrest") == -1);

        entries = AspectJCorePreferences.getRawProjectInpath(project);
        assertTrue("Should have JUnit 4 on the raw inpath:\n" + entries[0], entries[0].indexOf("org.eclipse.jdt.junit.JUNIT_CONTAINER/4:") >= 0);
    }
    
    // first test the original project
    public void xtestExtraAspectpathEntry2() throws Exception {
        String[] entries = AspectJCorePreferences.getResolvedProjectAspectPath(project2);
        assertTrue("Should not have found org.eclipse.jdt on the resolved aspectpath", valueNotFound(entries[0], "org.eclipse.jdt_"));
        assertEquals("Should have found org.eclipse.jdt.apt.core on the resolved aspectpath", "org.eclipse.jdt.apt.core", findValue(entries[0], "org.eclipse.jdt.apt.core"));
        assertEquals("Should have found org.eclipse.jdt.apt.ui on the resolved aspectpath", "org.eclipse.jdt.apt.ui", findValue(entries[0], "org.eclipse.jdt.apt.ui"));
        assertEquals("Should have found org.eclipse.jdt.core on the resolved aspectpath", "org.eclipse.jdt.core", findValue(entries[0], "org.eclipse.jdt.core"));

    }

    public void testExtraInpathEntry2() throws Exception {
        String[] entries = AspectJCorePreferences.getResolvedProjectInpath(project2);
        int i =0;
        for (String entry: entries) {
        	System.out.println("ENTRY:"+i+":"+entry);
        	i++;
        }
        assertEquals("Should have found org.eclipse.jdt on the resolved inpath", "org.eclipse.jdt_", findValue(entries[0], "org.eclipse.jdt_"));
        assertEquals("Should have found org.eclipse.jdt.apt.core on the resolved inpath", "org.eclipse.jdt.apt.core", findValue(entries[0], "org.eclipse.jdt.apt.core"));
        assertEquals("Should have found org.eclipse.jdt.apt.ui on the resolved inpath", "org.eclipse.jdt.apt.ui", findValue(entries[0], "org.eclipse.jdt.apt.ui"));
        assertEquals("Should have found org.eclipse.jdt.core on the resolved inpath", "org.eclipse.jdt.core", findValue(entries[0], "org.eclipse.jdt.core"));
    }

    private String findValue(String string, String toFind) {
    	try {
    		return string.substring(string.indexOf(toFind), string.indexOf(toFind)+toFind.length());
    	} catch (Throwable t) {
    		System.out.println("Unable to find '"+toFind+"' in the string '"+string+"'");
    		throw new IllegalStateException("Unable to find '"+toFind+"' in the string '"+string+"'",t);
    	}
    }
    
    private boolean valueNotFound(String string, String toFind) {
        return string.indexOf(toFind) == -1;
    }
    
}
