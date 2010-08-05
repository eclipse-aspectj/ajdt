/*******************************************************************************
 * Copyright (c) 2006 SpringSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SpringSource - initial API and implementation 
 *               Andrew Eisenebrg - initial implementation
 ******************************************************************************/
package org.eclipse.ajdt.ui.tests.builder;

import org.eclipse.ajdt.internal.launching.AspectJApplicationLaunchShortcut;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.UserLibraryClasspathContainerInitializer;

/**
 * 
 * @author andrew
 * @created Jul 2, 2008
 * These tests collectively ensure that the aspect and in paths
 * can contain various classpath entries
 *
 */
public class BuildPathTests extends UITestCase {

    private IProject hasInpath;
    private IProject hasAspectpath;
    private IProject containerProj;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        //ensure that projects are not build before the variable and container are set up.
        setAutobuilding(false);
        containerProj = createPredefinedProject("PathTesting-ContainerOnAspectPath"); //$NON-NLS-1$
        waitForJobsToComplete();
        IProject varProj = createPredefinedProject("PathTesting-VariableOnAspectPath"); //$NON-NLS-1$
        waitForJobsToComplete();
        IProject projProj = createPredefinedProject("PathTesting-ProjectOnAspectPath"); //$NON-NLS-1$
        waitForJobsToComplete();
        IProject jarProj = createPredefinedProject("PathTesting-JarOnAspectPath"); //$NON-NLS-1$
        waitForJobsToComplete();
        hasInpath = createPredefinedProject("PathTesting-HasInpath"); //$NON-NLS-1$
        waitForJobsToComplete();
        hasAspectpath = createPredefinedProject("PathTesting-HasAspectPath"); //$NON-NLS-1$
        waitForJobsToComplete();
        
        // create variable
        JavaCore.setClasspathVariable("Aspect_Path_Var", varProj.getLocation().append(new Path("variable.jar")), null); //$NON-NLS-1$ //$NON-NLS-2$

        // build all projects
        containerProj.build(IncrementalProjectBuilder.FULL_BUILD, null);
        varProj.build(IncrementalProjectBuilder.FULL_BUILD, null);
        projProj.build(IncrementalProjectBuilder.FULL_BUILD, null);
        jarProj.build(IncrementalProjectBuilder.FULL_BUILD, null);
        setAutobuilding(true);
        waitForJobsToComplete();
    }   

    public void testInpath() throws Exception {
        
        // create container
        IPath containerPath = new Path("org.eclipse.jdt.USER_LIBRARY/Aspect_Path_Lib"); //$NON-NLS-1$
        IJavaProject inpathJProj = JavaCore.create(hasInpath);
        IClasspathContainer containerHint = new IClasspathContainer() {
            public IPath getPath() {
                return new Path("org.eclipse.jdt.USER_LIBRARY/Aspect_Path_Lib"); //$NON-NLS-1$
            }
        
            public int getKind() {
                return IClasspathContainer.K_APPLICATION;
            }
        
            public String getDescription() {
                return ""; //$NON-NLS-1$
            }
            public IClasspathEntry[] getClasspathEntries() {
                return new IClasspathEntry[] { JavaCore.newLibraryEntry(containerProj.getLocation().append("container.jar"), null, null) }; //$NON-NLS-1$
            }
        
        };
        UserLibraryClasspathContainerInitializer initializer = new UserLibraryClasspathContainerInitializer();
        initializer.initialize(containerPath, inpathJProj);
        initializer.requestClasspathContainerUpdate(containerPath, inpathJProj, containerHint);
        waitForJobsToComplete();
        hasInpath.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

        waitForJobsToComplete();
        
        // do launch
        AspectJApplicationLaunchShortcut launcher = new AspectJApplicationLaunchShortcut();
        launcher.launch(
                openFileInAspectJEditor(
                        hasInpath.getFile("src/aspects/AdviseClassOnInpath.aj"), true),  //$NON-NLS-1$
                 ILaunchManager.RUN_MODE);
        waitForJobsToComplete();
        String console = getConsoleViewContents();
        String exp = "advised container!"; //$NON-NLS-1$
        assertTrue("Did not find expected string '"+exp+"' in console output:\n"+console,console.indexOf(exp)!=-1);  //$NON-NLS-1$//$NON-NLS-2$
        exp = "advised project!";//$NON-NLS-1$
        assertTrue("Did not find expected string '"+exp+"' in console output:\n"+console,console.indexOf(exp)!=-1);  //$NON-NLS-1$//$NON-NLS-2$
        exp = "advised variable!";//$NON-NLS-1$
        assertTrue("Did not find expected string '"+exp+"' in console output:\n"+console,console.indexOf(exp)!=-1);  //$NON-NLS-1$//$NON-NLS-2$
        exp = "advised jar!";//$NON-NLS-1$
        assertTrue("Did not find expected string '"+exp+"' in console output:\n"+console,console.indexOf(exp)!=-1);  //$NON-NLS-1$//$NON-NLS-2$
    }
    
    public void testAspectPath() throws Exception {
        // Ignore these tests on Linux because not passing
        if (System.getProperty("os.name").equals("Linux")) {
            return;
        }
        
        // create container
        IPath containerPath = new Path("org.eclipse.jdt.USER_LIBRARY/Aspect_Path_Lib"); //$NON-NLS-1$
        IJavaProject aspectpathJProj = JavaCore.create(hasAspectpath);
        IClasspathContainer containerHint = new IClasspathContainer() {
            public IPath getPath() {
                return new Path("org.eclipse.jdt.USER_LIBRARY/Aspect_Path_Lib"); //$NON-NLS-1$
            }
        
            public int getKind() {
                return IClasspathContainer.K_APPLICATION;
            }
        
            public String getDescription() {
                return ""; //$NON-NLS-1$
            }
            public IClasspathEntry[] getClasspathEntries() {
                return new IClasspathEntry[] { JavaCore.newLibraryEntry(containerProj.getLocation().append("container.jar"), null, null) }; //$NON-NLS-1$
            }
        };
        UserLibraryClasspathContainerInitializer initializer = new UserLibraryClasspathContainerInitializer();
        initializer.initialize(containerPath, aspectpathJProj);
        initializer.requestClasspathContainerUpdate(containerPath, aspectpathJProj, containerHint);

        waitForJobsToComplete();
        
        
        hasAspectpath.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        
        // do launch
        AspectJApplicationLaunchShortcut launcher = new AspectJApplicationLaunchShortcut();
        launcher.launch(
                openFileInAspectJEditor(
                        hasAspectpath.getFile("src/main/Main.java"), true),  //$NON-NLS-1$
                 ILaunchManager.RUN_MODE);
        waitForJobsToComplete();
        String console = getConsoleViewContents();
        String exp = "from project aspect"; //$NON-NLS-1$
        assertTrue("Did not find expected string '"+exp+"' in console output:\n"+console,console.indexOf(exp)!=-1);  //$NON-NLS-1$//$NON-NLS-2$
        exp = "from jar aspect"; //$NON-NLS-1$
        assertTrue("Did not find expected string '"+exp+"' in console output:\n"+console,console.indexOf(exp)!=-1);  //$NON-NLS-1$//$NON-NLS-2$
        exp = "from variable aspect"; //$NON-NLS-1$
        assertTrue("Did not find expected string '"+exp+"' in console output:\n"+console,console.indexOf(exp)!=-1);  //$NON-NLS-1$//$NON-NLS-2$
        exp = "from container aspect"; //$NON-NLS-1$
        assertTrue("Did not find expected string '"+exp+"' in console output:\n"+console,console.indexOf(exp)!=-1);  //$NON-NLS-1$//$NON-NLS-2$
    }    
}
