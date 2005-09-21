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
package org.eclipse.ajdt.ui.tests.visual;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.pde.internal.runtime.logview.LogEntry;
import org.eclipse.pde.internal.runtime.logview.LogView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.Workbench;


/**
 * Test for bug 98663
 */
public class Bug98663Test extends VisualTestCase {

	public void testBug98663() throws Exception {
		// Get the number of entries in the error log
		IViewPart view = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().getPage().showView("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
		if(view instanceof LogView) {
			LogView logView = (LogView)view;
			LogEntry[] logs = logView.getLogs();
			int originalNumberOfLogEntries = logs.length;
		
			// Add a new AspectJ project
			postKeyDown(SWT.ALT);
			postKeyDown(SWT.SHIFT);
			postKey('n');
			postKeyUp(SWT.SHIFT);
			postKeyUp(SWT.ALT);
			
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.CR);
			
			Runnable r = new Runnable() {				
				public void run() {
					sleep();
					postString("Project1"); //$NON-NLS-1$
					postKey(SWT.CR);
				}
			};
			new Thread(r).start();	
			waitForJobsToComplete();
			
			final IWorkspace workspace= JavaPlugin.getWorkspace();				
			new DisplayHelper() {
				protected boolean condition() {
					boolean ret = workspace.getRoot().getProject("Project1").exists(); //$NON-NLS-1$
					return ret;
				}			
			}.waitForCondition(Display.getCurrent(), 5000);
			IProject project = workspace.getRoot().getProject("Project1"); //$NON-NLS-1$
			assertTrue("Should have created a project", project.exists()); //$NON-NLS-1$
		
			IFolder src = project.getFolder("src"); //$NON-NLS-1$
			assertFalse("Should not have found a folder called src", src.exists()); //$NON-NLS-1$
			
			// Add a source folder		
			postKeyDown(SWT.ALT);
			postKeyDown(SWT.SHIFT);
			postKey('n');
			postKeyUp(SWT.SHIFT);
			postKeyUp(SWT.ALT);
			
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.CR);
			
			r = new Runnable() {					
				public void run() {
					sleep();
					postString("src"); //$NON-NLS-1$
					postKey(SWT.CR);
				}
			};
			new Thread(r).start();	
			waitForJobsToComplete();
			src = project.getFolder("src"); //$NON-NLS-1$
			assertTrue("Should have found a folder called src", src.exists()); //$NON-NLS-1$
			
			// Add a package		
			postKeyDown(SWT.ALT);
			postKeyDown(SWT.SHIFT);
			postKey('n');
			postKeyUp(SWT.SHIFT);
			postKeyUp(SWT.ALT);
			
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.CR);
			
			r = new Runnable() {					
				public void run() {
					sleep();
					postString("p1"); //$NON-NLS-1$
					postKey(SWT.CR);
				}
			};
			new Thread(r).start();	
			waitForJobsToComplete();
			IJavaProject jp = JavaCore.create(project);
			IPackageFragment p1 = jp.getPackageFragmentRoot(project.findMember("src")).getPackageFragment("p1"); //$NON-NLS-1$ //$NON-NLS-2$
			assertTrue("Should have created a package called p1", p1.exists()); //$NON-NLS-1$
			
			// Check that no more errors have appeared in the error log
			logs = logView.getLogs();
			assertEquals("The error log should not have had any errors added to it.", originalNumberOfLogEntries, logs.length); //$NON-NLS-1$
				
		} else {
			fail("Could not find the Error log."); //$NON-NLS-1$
		}
	}
	
}
