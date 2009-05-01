/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.core.tests;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.core.resources.IProject;


/**
 * @author Helen Hawkins
 *
 */
public class AspectJCorePreferencesTest extends AJDTCoreTestCase {

    IProject project;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        project = createPredefinedProject("CorePreferencesTestProject"); //$NON-NLS-1$
    }

    public void testGetProjectOutJar() {
    	String outjar = AspectJCorePreferences.getProjectOutJar(project);
    	assertEquals("outjar should be set","outjar.jar",outjar); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void testSetProjectOutJar() {
    	String newOutjar = "new.jar"; //$NON-NLS-1$
    	AspectJCorePreferences.setProjectOutJar(project,newOutjar);
    	String outjar = AspectJCorePreferences.getProjectOutJar(project);
    	assertEquals("outjar should be set","new.jar",outjar); //$NON-NLS-1$ //$NON-NLS-2$
    	AspectJCorePreferences.setProjectOutJar(project,""); //$NON-NLS-1$
    	outjar = AspectJCorePreferences.getProjectOutJar(project);
    	assertEquals("outjar should not be set","",outjar); //$NON-NLS-1$ //$NON-NLS-2$
    	
    }

    public void testGetProjectAspectPath() {
    	String[] aspectPath = AspectJCorePreferences.getResolvedProjectAspectPath(project);
    	assertEquals("there should be 3 entries on the aspect path",3,aspectPath.length); //$NON-NLS-1$
    	assertTrue("hello.jar should be on the aspectpath",aspectPath[0].startsWith("/CorePreferencesTestProject/hello.jar")); //$NON-NLS-1$ //$NON-NLS-2$
    	assertTrue("Content kind should be BINARY",aspectPath[1].startsWith("2")); //$NON-NLS-1$ //$NON-NLS-2$
    	assertTrue("Entry kind should be LIBRARY", aspectPath[2].startsWith("1")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void testSetProjectAspectPath() {
    	AspectJCorePreferences.setProjectAspectPath(project,"","",""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	String[] aspectPath = AspectJCorePreferences.getResolvedProjectAspectPath(project);
    	assertEquals("there should be 3 entries on the aspect path",3,aspectPath.length); //$NON-NLS-1$
    	for (int i = 0; i < aspectPath.length; i++) {
			assertEquals("should be empty string on aspectpath","",aspectPath[i]); //$NON-NLS-1$ //$NON-NLS-2$
		}
    	AspectJCorePreferences.setProjectAspectPath(project,"/CorePreferencesTestProject/hello.jar","2","1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	aspectPath = AspectJCorePreferences.getResolvedProjectAspectPath(project);   	   	
    	assertEquals("there should be 3 entries on the aspect path",3,aspectPath.length); //$NON-NLS-1$
    	assertTrue("hello.jar should be on the aspectpath",aspectPath[0].startsWith("/CorePreferencesTestProject/hello.jar")); //$NON-NLS-1$ //$NON-NLS-2$
    	assertTrue("Content kind should be BINARY",aspectPath[1].startsWith("2")); //$NON-NLS-1$ //$NON-NLS-2$
    	assertTrue("Entry kind should be LIBRARY", aspectPath[2].startsWith("1")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void testGetProjectInPath() {
    	String[] inPath = AspectJCorePreferences.getResolvedProjectInpath(project);
    	assertEquals("there should be 3 entries on the in path",3,inPath.length);   //$NON-NLS-1$
    	assertTrue("test.jar should be on the inpath",inPath[0].startsWith("/CorePreferencesTestProject/test.jar")); //$NON-NLS-1$ //$NON-NLS-2$
    	assertTrue("Content kind should be BINARY",inPath[1].startsWith("2")); //$NON-NLS-1$ //$NON-NLS-2$
    	assertTrue("Entry kind should be LIBRARY", inPath[2].startsWith("1")); //$NON-NLS-1$ //$NON-NLS-2$

    }

    public void testSetProjectInPath() {
    	AspectJCorePreferences.setProjectInPath(project,"","",""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	String[] inPath = AspectJCorePreferences.getResolvedProjectInpath(project);
    	assertEquals("there should be 3 entries on the inpath",3,inPath.length); //$NON-NLS-1$
    	for (int i = 0; i < inPath.length; i++) {
			assertEquals("should be empty string on inpath","",inPath[i]); //$NON-NLS-1$ //$NON-NLS-2$
		}
    	AspectJCorePreferences.setProjectInPath(project,"/CorePreferencesTestProject/test.jar","2","1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	inPath = AspectJCorePreferences.getResolvedProjectInpath(project);   	   	
    	assertEquals("there should be 3 entries on the inpath",3,inPath.length); //$NON-NLS-1$
    	assertTrue("test.jar should be on the inpath",inPath[0].startsWith("/CorePreferencesTestProject/test.jar")); //$NON-NLS-1$ //$NON-NLS-2$
    	assertTrue("Content kind should be BINARY",inPath[1].startsWith("2")); //$NON-NLS-1$ //$NON-NLS-2$
    	assertTrue("Entry kind should be LIBRARY", inPath[2].startsWith("1"));  //$NON-NLS-1$ //$NON-NLS-2$
    }

}
