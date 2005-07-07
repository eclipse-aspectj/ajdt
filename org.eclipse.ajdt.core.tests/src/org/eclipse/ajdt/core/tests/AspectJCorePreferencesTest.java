/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
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
        project = createPredefinedProject("CorePreferencesTestProject");
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        deleteProject(project);
    }

    public void testGetProjectOutJar() {
    	String outjar = AspectJCorePreferences.getProjectOutJar(project);
    	assertEquals("outjar should be set","outjar.jar",outjar);
    }

    public void testSetProjectOutJar() {
    	String newOutjar = "new.jar";
    	AspectJCorePreferences.setProjectOutJar(project,newOutjar);
    	String outjar = AspectJCorePreferences.getProjectOutJar(project);
    	assertEquals("outjar should be set","new.jar",outjar);
    	AspectJCorePreferences.setProjectOutJar(project,"");
    	outjar = AspectJCorePreferences.getProjectOutJar(project);
    	assertEquals("outjar should not be set","",outjar);
    	
    }

    public void testGetProjectAspectPath() {
    	String[] aspectPath = AspectJCorePreferences.getProjectAspectPath(project);
    	assertEquals("there should be 3 entries on the aspect path",3,aspectPath.length);
    	assertTrue("hello.jar should be on the aspectpath",aspectPath[0].startsWith("/CorePreferencesTestProject/hello.jar"));
    	assertTrue("Content kind should be BINARY",aspectPath[1].startsWith("2"));
    	assertTrue("Entry kind should be LIBRARY", aspectPath[2].startsWith("1"));
    }

    public void testSetProjectAspectPath() {
    	AspectJCorePreferences.setProjectAspectPath(project,"","","");
    	String[] aspectPath = AspectJCorePreferences.getProjectAspectPath(project);
    	assertEquals("there should be 3 entries on the aspect path",3,aspectPath.length);
    	for (int i = 0; i < aspectPath.length; i++) {
			assertEquals("should be empty string on aspectpath","",aspectPath[i]);
		}
    	AspectJCorePreferences.setProjectAspectPath(project,"/CorePreferencesTestProject/hello.jar","2","1");
    	aspectPath = AspectJCorePreferences.getProjectAspectPath(project);   	   	
    	assertEquals("there should be 3 entries on the aspect path",3,aspectPath.length);
    	assertTrue("hello.jar should be on the aspectpath",aspectPath[0].startsWith("/CorePreferencesTestProject/hello.jar"));
    	assertTrue("Content kind should be BINARY",aspectPath[1].startsWith("2"));
    	assertTrue("Entry kind should be LIBRARY", aspectPath[2].startsWith("1"));
    }

    public void testGetProjectInPath() {
    	String[] inPath = AspectJCorePreferences.getProjectInPath(project);
    	assertEquals("there should be 3 entries on the aspect path",3,inPath.length);  
    	assertTrue("test.jar should be on the inpath",inPath[0].startsWith("/CorePreferencesTestProject/test.jar"));
    	assertTrue("Content kind should be BINARY",inPath[1].startsWith("2"));
    	assertTrue("Entry kind should be LIBRARY", inPath[2].startsWith("1"));

    }

    public void testSetProjectInPath() {
    	AspectJCorePreferences.setProjectInPath(project,"","","");
    	String[] inPath = AspectJCorePreferences.getProjectInPath(project);
    	assertEquals("there should be 3 entries on the inpath",3,inPath.length);
    	for (int i = 0; i < inPath.length; i++) {
			assertEquals("should be empty string on inpath","",inPath[i]);
		}
    	AspectJCorePreferences.setProjectInPath(project,"/CorePreferencesTestProject/test.jar","2","1");
    	inPath = AspectJCorePreferences.getProjectInPath(project);   	   	
    	assertEquals("there should be 3 entries on the inpath",3,inPath.length);
    	assertTrue("test.jar should be on the inpath",inPath[0].startsWith("/CorePreferencesTestProject/test.jar"));
    	assertTrue("Content kind should be BINARY",inPath[1].startsWith("2"));
    	assertTrue("Entry kind should be LIBRARY", inPath[2].startsWith("1")); 
    }

}
