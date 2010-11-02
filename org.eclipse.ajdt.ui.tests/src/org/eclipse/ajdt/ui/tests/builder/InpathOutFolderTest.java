/*******************************************************************************
 * Copyright (c) 2008 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     SpringSource - initial API and implementation
 *     Andrew Eisenberg   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.builder;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.ajdt.ui.tests.testutils.SynchronizationUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author andrew eisenberg
 */
public class InpathOutFolderTest extends UITestCase {
    
    private IJavaProject jarOnInpath;

	protected void setUp() throws Exception {
		super.setUp();
		createPredefinedProject("ExportAsJar"); //$NON-NLS-1$
		IProject proj = createPredefinedProject("JarOnInpath"); //$NON-NLS-1$
		jarOnInpath = JavaCore.create(proj);
	}

	public void testJarOnInpath() throws CoreException {
	    // test that the built version properly uses the inpath out location
	    String outFolder = AspectJCorePreferences.getProjectInpathOutFolder(jarOnInpath.getProject());
        jarOnInpath.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
        SynchronizationUtils.joinBackgroudActivities();
        jarOnInpath.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        SynchronizationUtils.joinBackgroudActivities();
        jarOnInpath.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
        SynchronizationUtils.joinBackgroudActivities();
        assertTrue("File on inpath out folder does not exist: " + outFolder + "/SomeClass.class",  //$NON-NLS-1$ //$NON-NLS-2$
                jarOnInpath.getProject().getWorkspace().getRoot().getFile(new Path(outFolder + "/SomeClass.class")).exists()); //$NON-NLS-1$
	}
	
	public void testClean() throws CoreException {
	    // test that the out location gets cleaned
        String outFolder = AspectJCorePreferences.getProjectInpathOutFolder(jarOnInpath.getProject());
        setAutobuilding(false);
        jarOnInpath.getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
        waitForJobsToComplete();
        assertFalse("File on inpath out folder still exists even after a build clean: " + outFolder + "/SomeClass.class",  //$NON-NLS-1$ //$NON-NLS-2$
                jarOnInpath.getProject().getWorkspace().getRoot().getFile(new Path(outFolder + "/SomeClass.class")).exists()); //$NON-NLS-1$
        setAutobuilding(true);
	}
	
	public void testChangeInpathOutLocation() throws CoreException {
	    // test that when the out location changes, the new one is used instead
	    jarOnInpath.getProject().getFolder("newOutFolder").create(true, true, null); //$NON-NLS-1$
	    AspectJCorePreferences.setProjectInpathOutFolder(jarOnInpath.getProject(), "JarOnInpath/newOutFolder"); //$NON-NLS-1$
	    jarOnInpath.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
        SynchronizationUtils.joinBackgroudActivities();
	    String outFolder = AspectJCorePreferences.getProjectInpathOutFolder(jarOnInpath.getProject());
        jarOnInpath.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        SynchronizationUtils.joinBackgroudActivities();
        jarOnInpath.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
        SynchronizationUtils.joinBackgroudActivities();
        assertTrue("File on inpath out folder does not exist: " + outFolder + "/SomeClass.class",  //$NON-NLS-1$ //$NON-NLS-2$
                jarOnInpath.getProject().getWorkspace().getRoot().getFile(new Path(outFolder + "/SomeClass.class")).exists()); //$NON-NLS-1$
	}

}
