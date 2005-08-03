/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.editor;

import junit.framework.TestCase;

import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.ajdt.ui.tests.testutils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.internal.ui.viewsupport.ImageImageDescriptor;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Test for bug 105299 - editor image is sometimes wrong
 */
public class AspectJEditorIconTest extends TestCase {
	
	public void testEditorIcon() throws Exception {
		// Create the project
		IProject project = Utils.createPredefinedProject("bug105299");
		assertTrue("The example project should have been created", project != null);
		try {
			// Find the files
			IFile boundPointFile = (IFile)project.findMember("src/bean/BoundPoint.aj");		
			assertTrue("Should have found Boundpoint.aj", boundPointFile.exists());
			IFile demoFile = (IFile)project.findMember("src/bean/Demo.java");		
			assertTrue("Should have found Demo.java", demoFile.exists());
			IFile pointFile = (IFile)project.findMember("src/bean/Point.java");		
			assertTrue("Should have found Point.java", pointFile.exists());

			// Use the same registry as AspectJEditorTitleImageUpdater
			ImageDescriptorRegistry registry = JavaPlugin.getImageDescriptorRegistry();
			
			// Set up the expected images
			Image plainAJEditorImage = registry.get(AspectJImages.ASPECTJ_FILE.getImageDescriptor());
			Rectangle bounds= plainAJEditorImage.getBounds();
			int adornmentFlags = JavaElementImageDescriptor.WARNING;
			JavaElementImageDescriptor id = new JavaElementImageDescriptor(new ImageImageDescriptor(plainAJEditorImage), adornmentFlags, new Point(bounds.width, bounds.height));
			Image warningAJEditorImage = registry.get(id);
			adornmentFlags = JavaElementImageDescriptor.ERROR;
			id = new JavaElementImageDescriptor(new ImageImageDescriptor(plainAJEditorImage), adornmentFlags, new Point(bounds.width, bounds.height));
			final Image errorAJEditorImage = registry.get(id);
			
			// Open each of the files in the editor and check that the title images are correct 
			ITextEditor editor1 = (ITextEditor)Utils.openFileInAspectJEditor(boundPointFile, false);
			Utils.waitForJobsToComplete();
			assertTrue("Boundpoint.aj should have the plain editor image", editor1.getTitleImage().equals(plainAJEditorImage));
			ITextEditor editor2 = (ITextEditor)Utils.openFileInAspectJEditor(demoFile, false);
			Utils.waitForJobsToComplete();
			assertTrue("Demo.java should have the error editor image", editor2.getTitleImage().equals(errorAJEditorImage));			
			ITextEditor editor3 = (ITextEditor)Utils.openFileInAspectJEditor(pointFile, false);
			Utils.waitForJobsToComplete();
			assertTrue("Point.java should have the warning editor image", editor3.getTitleImage().equals(warningAJEditorImage));
			
			// Do a full build
			project.build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor());
			Utils.waitForJobsToComplete();
			
			// Test the icons again
			assertTrue("Boundpoint.aj should have the plain editor image after a build", editor1.getTitleImage().equals(plainAJEditorImage));
			assertTrue("Demo.java should have the error editor image after a build", editor2.getTitleImage().equals(errorAJEditorImage));			
			assertTrue("Point.java should have the warning editor image after a build", editor3.getTitleImage().equals(warningAJEditorImage));
			
		} finally {
			// Delete the project
			Utils.deleteProject(project);
		}
	}
	
}
