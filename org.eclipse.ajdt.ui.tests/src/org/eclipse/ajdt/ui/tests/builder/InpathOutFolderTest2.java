/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
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

import org.eclipse.ajdt.core.tests.testutils.Utils;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.ajdt.ui.tests.testutils.SynchronizationUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;

/**
 * @author andrew eisenberg
 * Test bug 329190 that jars from a classpath container are properly placed in the in path out folder 
 */
public class InpathOutFolderTest2 extends UITestCase {
    

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testJarOnInpath() throws CoreException {
	    Utils.setAutobuilding(false);
	    try {
    	    // this project has an in path out folder
    	    // and it is a plugin project that relies on the aspectj rt plugin
    	    // after a build, in the out folder, the entire contents of the plugin should be there
    	    IProject pluginProjectWithInpathOutFolder = createPredefinedProject("InPathOutFolderPluginTesting"); //$NON-NLS-1$
    	    SynchronizationUtils.joinBackgroudActivities();
    	    pluginProjectWithInpathOutFolder.build(IncrementalProjectBuilder.FULL_BUILD, null);
    	    
    	    IFolder out = pluginProjectWithInpathOutFolder.getFolder("out");
    	    
    	    SynchronizationUtils.joinBackgroudActivities();
    	    // should not need to refresh
    	    out.refreshLocal(IResource.DEPTH_INFINITE, null);
    	    
    	    IFile joinPointClass = out.getFile("org/aspectj/lang/JoinPoint.class");
    	    if (! joinPointClass.exists()) {
    	        System.out.println("Out folder: " + out.getLocation());
    	        System.out.println("All resources in project:");
    	        IResourceVisitor visitor = new IResourceVisitor() {
                    public boolean visit(IResource resource) throws CoreException {
                        System.out.println("Resource: " + resource.getLocation());
                        return true;
                    }
                };
                pluginProjectWithInpathOutFolder.accept(visitor);
    	        fail("AspectJ RT should exist in the in path out folder");
    	    }
            
            pluginProjectWithInpathOutFolder.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
            SynchronizationUtils.joinBackgroudActivities();

            // should not need to refresh
            out.refreshLocal(IResource.DEPTH_INFINITE, null);
            assertFalse("AspectJ RT should not exist in the in path out folder", joinPointClass.exists());
        } finally {
            Utils.setAutobuilding(true);
        }
	}

}
