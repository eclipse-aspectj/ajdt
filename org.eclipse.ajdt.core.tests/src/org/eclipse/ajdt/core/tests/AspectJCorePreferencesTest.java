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
    	assertEquals("hello.jar should be on the aspectpath","/CorePreferencesTestProject/hello.jar;",aspectPath[0]);
    	assertEquals("Content kind should be BINARY","2;",aspectPath[1]);
    	assertEquals("Entry kind should be LIBRARY","1;",aspectPath[2]);   	
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
    	assertEquals("hello.jar should be on the aspectpath","/CorePreferencesTestProject/hello.jar;",aspectPath[0]);
    	assertEquals("Content kind should be BINARY","2;",aspectPath[1]);
    	assertEquals("Entry kind should be LIBRARY","1;",aspectPath[2]);   	
    }

    public void testGetProjectInPath() {
    	String[] inPath = AspectJCorePreferences.getProjectInPath(project);
    	assertEquals("there should be 3 entries on the aspect path",3,inPath.length);
    	assertEquals("test.jar should be on the aspectpath","/CorePreferencesTestProject/test.jar;",inPath[0]);
    	assertEquals("Content kind should be BINARY","2;",inPath[1]);
    	assertEquals("Entry kind should be LIBRARY","1;",inPath[2]);     
    }

    public void testSetProjectInPath() {
    	AspectJCorePreferences.setProjectInPath(project,"","","");
    	String[] inPath = AspectJCorePreferences.getProjectInPath(project);
    	assertEquals("there should be 3 entries on the aspect path",3,inPath.length);
    	for (int i = 0; i < inPath.length; i++) {
			assertEquals("should be empty string on aspectpath","",inPath[i]);
		}
    	AspectJCorePreferences.setProjectInPath(project,"/CorePreferencesTestProject/test.jar","2","1");
    	inPath = AspectJCorePreferences.getProjectInPath(project);   	   	
    	assertEquals("there should be 3 entries on the aspect path",3,inPath.length);
    	assertEquals("test.jar should be on the aspectpath","/CorePreferencesTestProject/test.jar;",inPath[0]);
    	assertEquals("Content kind should be BINARY","2;",inPath[1]);
    	assertEquals("Entry kind should be LIBRARY","1;",inPath[2]);  
    }

}
