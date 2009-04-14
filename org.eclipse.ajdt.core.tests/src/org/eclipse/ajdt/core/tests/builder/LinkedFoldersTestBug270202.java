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
public class LinkedFoldersTestBug270202 extends AJDTCoreTestCase {

    private IProject project;
    private IFile linkedFile;
    private IFile nonLinkedFile1;
    private IFile nonLinkedFile2;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Utils.setAutobuilding(false);
        project = createPredefinedProject("Bug270202");
        
        // now set up the linked folder
        // line in .classpath already exists, so
        // just need to create the link
        IFolder rawLocation = project.getFolder("raw_location");
        IPath rawPath = rawLocation.getLocation();
        IFolder src = project.getFolder("src");
        src.createLink(rawPath, 0, null);

        project.build(IncrementalProjectBuilder.FULL_BUILD, null);
        linkedFile = project.getFile("src/p/AnAspect.aj");
        nonLinkedFile1 = project.getFile("src1/q/AClass.java");
        nonLinkedFile2 = project.getFile("src2/r/AnotherClass.java");
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        Utils.setAutobuilding(true);
    }
    
    public void testGetSourceFolderForLinkedFile() throws Exception {
        assertEquals("Linked source folder not set up properly", 
                project.getFolder("src").getLocation().toOSString(), 
                project.getFolder("raw_location").getLocation().toOSString());
        IOutputLocationManager locationManager = AspectJPlugin.getDefault().getCompilerFactory()
                .getCompilerForProject(project).getCompilerConfiguration().getOutputLocationManager();
        String sourceFolderStr = locationManager.getSourceFolderForFile(new File(linkedFile.getLocation().toOSString()));
        assertEquals("Source folder 'src' not found", "src", sourceFolderStr);
    }

    public void testGetSourceFolderForNonLinkedFile1() throws Exception {
        IOutputLocationManager locationManager = AspectJPlugin.getDefault().getCompilerFactory()
                .getCompilerForProject(project).getCompilerConfiguration().getOutputLocationManager();
        String sourceFolderStr = locationManager.getSourceFolderForFile(new File(nonLinkedFile1.getLocation().toOSString()));
        assertEquals("Source folder 'src1' not found", "src1", sourceFolderStr);
    }
    
    public void testGetSourceFolderForNonLinkedFile2() throws Exception {
        IOutputLocationManager locationManager = AspectJPlugin.getDefault().getCompilerFactory()
                .getCompilerForProject(project).getCompilerConfiguration().getOutputLocationManager();
        String sourceFolderStr = locationManager.getSourceFolderForFile(new File(nonLinkedFile2.getLocation().toOSString()));
        assertEquals("Source folder 'src2' not found", "src2", sourceFolderStr);
    }
    
    
    public void testHandlesInsideLinkedFolders() throws Exception {
        IJavaElement ije = AspectJCore.create(linkedFile);
        assertTrue("Compilation unit should exist " + ije.getHandleIdentifier(), ije.exists());
        AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForProject(project);
        IProgramElement ipe = model.javaElementToProgramElement(ije);
        assertNotSame("IProgramElement should exist", IHierarchy.NO_STRUCTURE, ipe);
        IJavaElement recreated = model.programElementToJavaElement(ipe);
        assertTrue("Compilation unit should exist " + recreated.getHandleIdentifier(), recreated.exists());
    }
}
