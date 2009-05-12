/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SpringSource - initial API and implementation 
 *              Andrew Eisenberg
 ******************************************************************************/
package org.eclipse.ajdt.core.tests.builder;

import java.io.File;

import org.aspectj.ajde.core.IOutputLocationManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.core.tests.testutils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Tests that linked folders are properly handled by AJDT and aspectj when 
 * handles are being created.
 * 
 * @author andrew
 *
 */
public class LinkedFoldersTestBug275903 extends AJDTCoreTestCase {

    private IProject project;
    private IProject otherProj;
    
    private IFolder outFolder1;
    private IFolder outFolder2;
    private IFolder outFolder3;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Utils.setAutobuilding(false);
        project = createPredefinedProject("DifferentProjectNameBug275903");
        otherProj = createPredefinedProject("OtherProjectBug275903");
        
        // now set up the linked folder
        // line in .classpath already exists, so
        // just need to create the link
        IFolder rawLocation = otherProj.getFolder("src3");
        IPath rawPath = rawLocation.getLocation();
        IFolder src3 = project.getFolder("src3");
        src3.createLink(rawPath, 0, null);

        project.build(IncrementalProjectBuilder.FULL_BUILD, null);
        outFolder1 = project.getFolder("bin");
        outFolder2 = project.getFolder("bin2");
        outFolder3 = project.getFolder("bin3");
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        Utils.setAutobuilding(true);
    }
    
    public void testBuildArtifactsPlacedCorrectly() throws Exception {
        assertLink();
        assertEquals(outFolder1.getName() + " should have 2 children", 2, outFolder1.members().length);
        assertTrue("from_src file should exist", outFolder1.getFile("from_src").exists());
        assertTrue("FromSrc.class file should exist", outFolder1.getFile("FromSrc.class").exists());
        
        assertEquals(outFolder2.getName() + " should have 2 children", 2, outFolder2.members().length);
        assertTrue("from_src2 file should exist", outFolder2.getFile("from_src2").exists());
        assertTrue("FromSrc2.class file should exist", outFolder2.getFile("FromSrc2.class").exists());
        
        assertEquals(outFolder3.getName() + " should have 2 children", 2, outFolder3.members().length);
        assertTrue("from_src3 file should exist", outFolder3.getFile("from_src3").exists());
        assertTrue("FromSrc3.class file should exist", outFolder3.getFile("FromSrc3.class").exists());
    }

    private void assertLink() {
        assertEquals("Linked source folder not set up properly", 
                project.getFolder("src3").getLocation().toOSString(), 
                otherProj.getFolder("src3").getLocation().toOSString());
    }
}
