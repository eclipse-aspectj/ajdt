/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:  
 * 				 Andrew Eisenberg   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.core.tests.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.core.tests.testutils.ReaderInputStream;
import org.eclipse.ajdt.core.tests.testutils.TestLogger;
import org.eclipse.ajdt.core.tests.testutils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * 
 * @author Andrew Eisenberg
 * @created Apr 30, 2009
 * 
 * Test that the old style project settings remain after being applied
 * but that there is a flag so that they are not re-applied.
 * 
 * Testing AspectJCorePreferences.getOldProjectPath
 */
public class Bug268609Test extends AJDTCoreTestCase {
    
    IProject onAspectPath;
    IProject hasAspectPath;
    
    String settingsFilePath = ".settings/org.eclipse.ajdt.ui.prefs";
    
    protected void setUp() throws Exception {
        super.setUp();
        onAspectPath = createPredefinedProject("MyAspectLibrary");
        hasAspectPath = createPredefinedProject("WeaveMe");
    }
    
    /**
     * Test that a project without old style settings does not
     * get the flag to say that old style settings have been visited.
     */
	public void testNoOldStyleSettings() throws Exception {
	    // A build has already occurred.  Check to make sure that .settings file has not been created
	    assertFalse(".settings folder should not exist", onAspectPath.getFile(settingsFilePath).exists());
    }
	
	public void testOldStyleSettingsAppliedAndFlagSet() throws Exception {
	    // a build has already occurred
	    IFile settingsFile = hasAspectPath.getFile(settingsFilePath);
	    assertTrue(".settings folder should exist", settingsFile.exists());
	    String contents = getContents(settingsFile);
        assertTrue("The settings file should contain \"org.eclipse.ajdt.ui.aspectPath=visited\"", 
                contents.indexOf("org.eclipse.ajdt.ui.aspectPath=visited") != -1);
        assertTrue("Aspect path line should still exist \"org.eclipse.ajdt.ui.aspectPath1=/MyAspectLibrary/bin\"", 
                contents.indexOf("org.eclipse.ajdt.ui.aspectPath1=/MyAspectLibrary/bin") != -1);
        
        // no in path in the file, so should not contain
        assertTrue("The settings file should *not* contain \"org.eclipse.ajdt.ui.inPath=visited\"", 
                contents.indexOf("org.eclipse.ajdt.ui.inPath=visited") == -1);
    }
	
	public void testOldStyleSettingsNotReapplied() throws Exception {
	    hasAspectPath.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
	    hasAspectPath.build(IncrementalProjectBuilder.FULL_BUILD, null);

	    IFile settingsFile = hasAspectPath.getFile(settingsFilePath);
        assertTrue(".settings folder should exist", settingsFile.exists());
        String contents = getContents(settingsFile);
        assertTrue("The settings file should contain \"org.eclipse.ajdt.ui.aspectPath=visited\"",
                contents.indexOf("org.eclipse.ajdt.ui.aspectPath=visited") != -1);
        assertTrue("Aspect path line should still exist \"org.eclipse.ajdt.ui.aspectPath1=/MyAspectLibrary/bin\"",
                contents.indexOf("org.eclipse.ajdt.ui.aspectPath1=/MyAspectLibrary/bin") != -1);

        // no in path, so this line should not appear
        assertTrue("The settings file should contain \"org.eclipse.ajdt.ui.inPath=visited\"",
                contents.indexOf("org.eclipse.ajdt.ui.inPath=visited") == -1);
        
        // now check that build did not produce any errors, indicating that
        // the classpath is stil valid
        assertEquals("Project should not have any errors on it", 0, hasAspectPath.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE).length);
        assertEquals("Project should not have any errors on it", 0, hasAspectPath.findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE).length);

    }
    
	
    private String getContents(IFile javaFile) throws CoreException, IOException {
        InputStream is = javaFile.getContents();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer= new StringBuffer();
        char[] readBuffer= new char[2048];
        int n= br.read(readBuffer);
        while (n > 0) {
            buffer.append(readBuffer, 0, n);
            n= br.read(readBuffer);
        }
        return buffer.toString();
    }

}
