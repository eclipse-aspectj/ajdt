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

import java.util.Iterator;
import java.util.List;

import org.aspectj.ajde.core.AjCompiler;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.core.tests.testutils.Utils;
import org.eclipse.ajdt.internal.core.ajde.CoreBuildMessageHandler;
import org.eclipse.ajdt.internal.core.ajde.CoreBuildProgressMonitor;
import org.eclipse.ajdt.internal.core.ajde.CoreCompilerConfiguration;
import org.eclipse.ajdt.internal.core.ajde.CoreCompilerFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;

/**
 * 
 * @author Andrew Eisenberg
 * @created Apr 8, 2009
 * 
 *          Tests to ensure that
 *          {@link CoreCompilerConfiguration#setClasspathElementsWithModifiedContents}
 *          does not have duplicated classpath elements
 */
public class Bug270325Tests extends AJDTCoreTestCase {

    class MockCoreCompilerConfiguration extends CoreCompilerConfiguration {

        List modifiedContents;

        public MockCoreCompilerConfiguration(IProject project) {
            super(project);
        }

        public void setClasspathElementsWithModifiedContents(
                List modifiedContents) {
            super.setClasspathElementsWithModifiedContents(modifiedContents);
            this.modifiedContents = modifiedContents;
        }
    }

    class MockCompilerFactory extends CoreCompilerFactory {
        protected AjCompiler createCompiler(IProject project) {
            AjCompiler compiler = new AjCompiler(project.getName(),
                    new MockCoreCompilerConfiguration(project),
                    new CoreBuildProgressMonitor(project),
                    new CoreBuildMessageHandler());
            return compiler;
        }

    }

    IProject jp1;

    IProject jp2;
    IProject jp3;
    IProject ap1;
    IProject ap2;
    IProject ap3;
    IProject myProj;

    protected void setUp() throws Exception {
        super.setUp();
        AspectJPlugin.getDefault()
                .setCompilerFactory(new MockCompilerFactory());
        Utils.setAutobuilding(false);
        jp1 = createPredefinedProject("JavaProj1");
        jp2 = createPredefinedProject("JavaProj2-On Inpath");
        jp3 = createPredefinedProject("JavaProj3-ClassFolder");
        ap1 = createPredefinedProject("AspectProj1");
        ap2 = createPredefinedProject("AspectProj2-On AspectPath");
        ap3 = createPredefinedProject("AspectProj3-Has Outjar");
        myProj = createPredefinedProject("AspectProjWeCareAbout");
        getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, null);
    }

    protected void tearDown() throws Exception {
        Utils.setAutobuilding(true);
        super.tearDown();
    }

    public void testNoDupsOnClasspath() throws Exception {
        myProj.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        MockCoreCompilerConfiguration config = (MockCoreCompilerConfiguration) 
                AspectJPlugin.getDefault().getCompilerFactory().getCompilerForProject(myProj)
                .getCompilerConfiguration();
        
        
        jp1.getFile("src/c/B.java").touch(null);
        jp2.getFile("src/Nothing2.java").touch(null);
        Utils.sleep(1000);

        jp1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        jp2.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        
        Utils.sleep(1000);
        // incremental build
        myProj.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertEquals("Should have only entries of modified contents.  Entries were:\n" + config.modifiedContents, 2, config.modifiedContents.size());
        assertTrue("'JavaProj2-On Inpath/bin' should be a modified entry.  Entries were:\n" + config.modifiedContents,
                listContains(config.modifiedContents, "JavaProj2-On Inpath"));
        assertTrue("'JavaProj1/bin' should be a modified entry.  Entries were:\n" + config.modifiedContents,
                listContains(config.modifiedContents, "JavaProj1"));        
    }
    
    boolean listContains(List strings, String msg) {
        for (Iterator iterator = strings.iterator(); iterator.hasNext();) {
            String str = (String) iterator.next();
            if (str.indexOf(msg) != -1) {
                return true;
            }
        }
        return false;
    }
}
