/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
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

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.AspectJProjectNature;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;

/**
 * @author Andrew Eisenberg
 * This tests bug 270552
 * Import a bunch of different projects that have incorrect builders on them
 * and then check to see if the builders are correct
 */
public class EnsureAJBuilderTests extends UITestCase {
    
    public void testNoBuilder() throws Exception {
        IProject project = createPredefinedProject("Bug270552NoAJBuilder");
        checkNature(project);
    }
    
    public void testJavaBuilder() throws Exception {
        IProject project = createPredefinedProject("Bug270552JavaBuilder");
        checkNature(project);
    }
    
    public void testAJBuilder() throws Exception {
        IProject project = createPredefinedProject("Bug270552AJBuilder");
        checkNature(project);
    }
    
    public void testBothBuilders() throws Exception {
        IProject project = createPredefinedProject("Bug270552JavaBuilderAndAJBuilder");
        checkNature(project);
    }
    
    public void testJavaNature() throws Exception {
        IProject project = createPredefinedProject("Bug270552JavaNatureJavaBuilder");
        assertFalse("Project should not be an AspectJ prooject " + project, AspectJPlugin.isAJProject(project));
        assertTrue("Project should have javabuilder " + project, AspectJProjectNature.hasJavaBuilder(project));
    }
    
    // tests that Java builder is not removed when generate class files is disabled
    // Bug 278535
    public void testNoRemoveJavaBuilder() throws Exception {
        IProject project = createPredefinedProject("Bug278535JavaBuilderNoRemove");
        checkNatureHasBuilder(project);
        project.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
        checkNatureHasBuilder(project);
        project.build(IncrementalProjectBuilder.FULL_BUILD, null);
        checkNatureHasBuilder(project);
    }

    private void checkNature(IProject project) throws Exception {
        assertTrue("Project should have ajBuilder " + project, AspectJProjectNature.hasNewBuilder(project));
        assertFalse("Project should not have javaBuilder " + project, AspectJProjectNature.hasJavaBuilder(project));
    }
    
    private void checkNatureHasBuilder(IProject project) throws Exception {
        assertTrue("Project should have ajBuilder " + project, AspectJProjectNature.hasNewBuilder(project));
        assertTrue("Project should have javaBuilder " + project, AspectJProjectNature.hasJavaBuilder(project));
    }
}