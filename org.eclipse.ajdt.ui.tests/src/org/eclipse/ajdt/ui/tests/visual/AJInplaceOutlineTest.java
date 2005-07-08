/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.ui.tests.visual;

import org.eclipse.ajdt.internal.ui.editor.outline.AJOutlineInformationControl;
import org.eclipse.ajdt.ui.tests.testutils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

public class AJInplaceOutlineTest extends VisualTestCase {

	private IProject project;
	
	protected void setUp() throws Exception {	
		super.setUp();
		project = Utils.createPredefinedProject("Simple AJ Project");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		Utils.deleteProject(project);
	}
	
	public void testBug80239() {
		IResource res = project.findMember("src/p2/Aspect.aj");
		if (res == null || !(res instanceof IFile)) {
			fail("src/p2/Aspect.aj file not found.");
		} 
		IFile ajFile = (IFile)res;

		// open Aspect.aj and select the pointcut
		ITextEditor editorPart = (ITextEditor)Utils.openFileInAspectJEditor(ajFile, false);
		editorPart.setFocus();
		gotoLine(17);
		Utils.waitForJobsToComplete();
		
		// open inplace outline view
		postKeyDown(SWT.CTRL);
		postCharacterKey('o');
		postKeyUp(SWT.CTRL);
				
		new DisplayHelper() {

			protected boolean condition() {
				AJOutlineInformationControl i = AJOutlineInformationControl.getInfoControl();
				boolean ret = (i != null);
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);

		final AJOutlineInformationControl info = AJOutlineInformationControl.getInfoControl();

		final Rectangle r = info.getBounds();
		
		final Shell shell = info.getShell();
		moveShell(shell,r.x + 50,r.y + 50,r.width + 100,r.height + 100);

		// wait for the bounds to be set and it to move
		new DisplayHelper() {

			protected boolean condition() {
				Rectangle rect = shell.getBounds();
				boolean ret = (rect != null) && !(rect.equals(r));
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		Rectangle r1 = shell.getBounds();
		assertTrue("the inplace view should have changed it's height", r.height != r1.height);
		assertTrue("the inplace view should have changed it's width", r.width != r1.width);
		assertTrue("the inplace view should have changed it's x coordinate", r.x != r1.x);
		assertTrue("the inplace view should have changed it's y coordinate", r.y != r1.y);
				
		// get rid of the inplace view
		postCharacterKey(SWT.ESC);
		
		// wait a few secs
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// open inplace outline view
		postKeyDown(SWT.CTRL);
		postCharacterKey('o');
		postKeyUp(SWT.CTRL);
		
		new DisplayHelper() {

			protected boolean condition() {
				AJOutlineInformationControl i = AJOutlineInformationControl.getInfoControl();
				boolean ret = (i != null) && !(i.equals(info));
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);

		// get hold of the new AJOutlineInformationControl
		AJOutlineInformationControl info2 = AJOutlineInformationControl.getInfoControl();
		
		Rectangle r2 = info2.getBounds();
		assertEquals("the inplace view should have remembered the changed height", r1.height, r2.height);
		assertEquals("the inplace view should have remembered the changed width", r1.width, r2.width);
		// for some bizarre reason, it always seems to add 3 to the x and y coordinates
		assertEquals("the inplace view should have remembered the changed x coordinate", r1.x + 3, r2.x);
		assertEquals("the inplace view should have remembered the changed y coordinate", r1.y + 3, r2.y);

		// get rid of the inplace view
		postCharacterKey(SWT.ESC);
	}
	
	private void moveShell(Shell s, int xCoord, int yCoord, int width, int height) {
		Rectangle r1 = new Rectangle(xCoord,yCoord,width,height);
		s.setBounds(r1);
	}
	
}
