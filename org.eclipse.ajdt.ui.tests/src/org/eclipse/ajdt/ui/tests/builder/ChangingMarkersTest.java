/*******************************************************************************
 * Copyright (c) 2008 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.eclipse.ajdt.internal.ui.refactoring.ReaderInputStream;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.ajdt.ui.tests.testutils.SynchronizationUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;


/**
 * Tests that after a project has some relationships that change
 * the advice and problem markers are updated appropriately
 */
public class ChangingMarkersTest extends UITestCase {


    public void testMarkersUpdatedAfterChangedProject() throws Exception {
        // create project
        IProject project = createPredefinedProject("Changing Markers Test"); //$NON-NLS-1$
        waitForJobsToComplete();
        waitForJobsToComplete();

        // advice, itd, and declare error markers are in appropriate place
        // in both class and aspect
        IFile javaFile = (IFile) project.findMember("src/pkg/ClassWithChangingMarkers.java"); //$NON-NLS-1$
        IFile ajFile = (IFile) project.findMember("src/pkg2/AspectWithChangingMarkers.aj"); //$NON-NLS-1$

        IMarker[] adviceMarkers;
        IMarker[] declareMarkers;
        IMarker[] warningMarkers;
        
        // advice markers
        adviceMarkers = javaFile.findMarkers(IAJModelMarker.ADVICE_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of advice markers in " + javaFile, 1, adviceMarkers.length); //$NON-NLS-1$
        adviceMarkers = ajFile.findMarkers(IAJModelMarker.SOURCE_ADVICE_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of advice markers in " + ajFile, 1, adviceMarkers.length); //$NON-NLS-1$
        
        // declare markers---declare warning declarations sources markers are a declare marker
        declareMarkers = javaFile.findMarkers(IAJModelMarker.DECLARATION_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of declare markers in " + javaFile, 1, declareMarkers.length); //$NON-NLS-1$
        declareMarkers = ajFile.findMarkers(IAJModelMarker.DECLARATION_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of declare markers in " + ajFile, 3, declareMarkers.length); //$NON-NLS-1$
        
        // warnings---declare warning declarations target markers are an ajdt problem marker
        warningMarkers = javaFile.findMarkers(IAJModelMarker.AJDT_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of warning markers in " + javaFile, 1, warningMarkers.length); //$NON-NLS-1$
        warningMarkers = ajFile.findMarkers(IAJModelMarker.AJDT_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of warning markers in " + ajFile, 0, warningMarkers.length); //$NON-NLS-1$
        
        // change class so that none of them apply
        String origContents = getContents(javaFile);
        javaFile.setContents(new ReaderInputStream(new StringReader(
                "package pkg;\n\npublic class ClassWithChangingMarkers {\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n}")),  //$NON-NLS-1$
                true, false, null);
        project.refreshLocal(IResource.DEPTH_INFINITE, null);
        project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);

        waitForJobsToComplete();
        // periodically failing on build server only add extra waiting here
        SynchronizationUtils.sleep(1000);
        waitForJobsToComplete();

        // check that no markers are there
        // advice markers
        adviceMarkers = javaFile.findMarkers(IAJModelMarker.ADVICE_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of advice markers in " + javaFile, 0, adviceMarkers.length); //$NON-NLS-1$
        adviceMarkers = ajFile.findMarkers(IAJModelMarker.SOURCE_ADVICE_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of advice markers in " + ajFile, 0, adviceMarkers.length); //$NON-NLS-1$
        
        // declare markers---declare parents and ITD (2 markers in aspect, one marker in class)
        declareMarkers = javaFile.findMarkers(IAJModelMarker.DECLARATION_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of declare markers in " + javaFile, 1, declareMarkers.length); //$NON-NLS-1$
        declareMarkers = ajFile.findMarkers(IAJModelMarker.DECLARATION_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of declare markers in " + ajFile, 2, declareMarkers.length); //$NON-NLS-1$
        
        // warnings---no warnings should be matched any more
        warningMarkers = javaFile.findMarkers(IAJModelMarker.AJDT_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of warning markers in " + javaFile, 0, warningMarkers.length); //$NON-NLS-1$
        warningMarkers = ajFile.findMarkers(IAJModelMarker.AJDT_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of warning markers in " + ajFile, 0, warningMarkers.length); //$NON-NLS-1$
        
        
        // change back
        javaFile.setContents(new ReaderInputStream(new StringReader(
                origContents)), true, false, null);
        project.refreshLocal(IResource.DEPTH_INFINITE, null);
        project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);

        waitForJobsToComplete();
        // periodically failing on build server only add extra waiting here
        SynchronizationUtils.sleep(1000);
        waitForJobsToComplete();

        // check that markers have returned
        // advice markers
        adviceMarkers = javaFile.findMarkers(IAJModelMarker.ADVICE_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of advice markers in " + javaFile, 1, adviceMarkers.length); //$NON-NLS-1$
        adviceMarkers = ajFile.findMarkers(IAJModelMarker.SOURCE_ADVICE_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of advice markers in " + ajFile, 1, adviceMarkers.length); //$NON-NLS-1$
        
        // declare markers
        declareMarkers = javaFile.findMarkers(IAJModelMarker.DECLARATION_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of declare markers in " + javaFile, 1, declareMarkers.length); //$NON-NLS-1$
        declareMarkers = ajFile.findMarkers(IAJModelMarker.DECLARATION_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of declare markers in " + ajFile, 3, declareMarkers.length); //$NON-NLS-1$
        
        // warnings
        warningMarkers = javaFile.findMarkers(IAJModelMarker.AJDT_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
        for (int i = 0; i < warningMarkers.length; i++) {
            System.out.println(warningMarkers[i].getAttribute(IMarker.LINE_NUMBER) + " : " + warningMarkers[i].getAttribute(IMarker.MESSAGE) +  //$NON-NLS-1$
            " : " + warningMarkers[i].getAttribute(AspectJUIPlugin.RELATED_LOCATIONS_ATTRIBUTE_PREFIX+"0")); //$NON-NLS-1$ //$NON-NLS-2$
            System.out.println(warningMarkers[i].getAttributes().keySet());
            System.out.println(warningMarkers[i].getAttributes().values());
        }

        warningMarkers = javaFile.findMarkers(IAJModelMarker.AJDT_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of warning markers in " + javaFile, 1, warningMarkers.length); //$NON-NLS-1$
        warningMarkers = ajFile.findMarkers(IAJModelMarker.AJDT_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Didn't find correct number of warning markers in " + ajFile, 0, warningMarkers.length); //$NON-NLS-1$
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
