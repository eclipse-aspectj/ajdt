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

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.internal.console.ConsoleView;
import org.eclipse.ui.internal.console.IOConsolePage;

/**
 * Visual test for bug 103232 - run projects with outjars properly
 */
public class OutjarLaunchingTest extends VisualTestCase {

	private String outputStringStart = "p1 =";
	
	public void testLaunchingWithAnOutJar() throws Exception {
		IProject project = createPredefinedProject("Outjar Example");
		assertTrue("The Outjar Example project should have been created", project != null);
		try {
			IJavaProject jp = JavaCore.create(project);
			String outJar = AspectJCorePreferences.getProjectOutJar(project);
			assertTrue("The Outjar Example project should have an outjar", outJar != null && outJar.equals("bean.jar"));
			IPackageFragment p1 = jp.getPackageFragmentRoot(project.findMember("src")).getPackageFragment("bean");
			ICompilationUnit demo = p1.getCompilationUnit("Demo.java");
			PackageExplorerPart packageExplorer = PackageExplorerPart.getFromActivePerspective();
			packageExplorer.setFocus();
			packageExplorer.selectAndReveal(demo);
	
			// Run as AspectJ/Java Application
			postKeyDown(SWT.ALT);
			postKey('r');	
			postKeyUp(SWT.ALT);
			postKey('s');
			postKey(SWT.CR);
			
			waitForJobsToComplete();
			ConsoleView cview = null;
			IViewReference[] views = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
			for (int i = 0; i < views.length; i++) {
				if (views[i].getView(false) instanceof ConsoleView) {
					cview = (ConsoleView)views[i].getView(false);
				}
			}
			assertNotNull("Console view should be open", cview);
			String output = null;
			IOConsolePage page = (IOConsolePage) cview.getCurrentPage();
			TextViewer viewer = page.getViewer();
			output = viewer.getDocument().get();
			assertNotNull(output);
			assertTrue("program did not run correctly", output.indexOf(outputStringStart) != -1);
		} finally {
			deleteProject(project);
		}
	}
	
}
