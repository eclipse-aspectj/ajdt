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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.testutils.Utils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.internal.console.ConsoleView;
import org.eclipse.ui.part.IPage;

/**
 * Visual test for bug 103232 - run projects with outjars properly
 */
public class OutjarLaunchingTest extends VisualTestCase {

	private String outputStringStart = "p1 =";
	
	public void testLaunchingWithAnOutJar() throws Exception {
		// This is a workaround to avoid an exception being thrown on Eclipse 3.1,
		// but does not affect the test.
		Field field = null;
		try {
			field = Util.class.getDeclaredField("JAVA_LIKE_EXTENSIONS");
		} catch (NoSuchFieldException nsfe) {
			System.out.println("no such field..");
			// do nothing - we are on eclipse 3.0
		}
		if(field != null) {
			char[][] extensions = new char[2][];
			extensions[0] = new char[] {'.', 'j', 'a', 'v', 'a'};
			extensions[1] = new char[] {'.', 'a', 'j'};
			field.setAccessible(true);
			field.set(null, extensions);
		}
		
		IProject project = Utils.createPredefinedProject("Outjar Example");
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
			postCharacterKey('r');	
			postKeyUp(SWT.ALT);
			postCharacterKey('s');
			if(!runningEclipse31) {
				postCharacterKey('s');
				postKey(SWT.ARROW_RIGHT);
				postKey(SWT.ARROW_DOWN);
			}		
			postCharacterKey(SWT.CR);
			
			Utils.waitForJobsToComplete();
			ConsoleView cview = null;
			IViewReference[] views = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
			for (int i = 0; i < views.length; i++) {
				if (views[i].getView(false) instanceof ConsoleView) {
					cview = (ConsoleView)views[i].getView(false);
				}
			}
			assertNotNull("Console view should be open", cview);
			String output = null;
			IPage page = cview.getCurrentPage();
			Class cl = page.getClass();
			try {
				Method m = cl.getMethod("getConsoleViewer", new Class[0]);
				Object o = m.invoke(page, new Object[0]);
				TextViewer viewer = (TextViewer)o;
				output = viewer.getDocument().get();
			} catch (NoSuchMethodException nsme) {
				// We are on Eclipse 3.1
				Method m = cl.getMethod("getViewer", new Class[0]);
				Object o = m.invoke(page, new Object[0]);
				TextViewer viewer = (TextViewer)o;
				output = viewer.getDocument().get();
			}
			assertNotNull(output);
			assertTrue("program did not run correctly", output.indexOf(outputStringStart) != -1);
		} finally {
			Utils.deleteProject(project);
			if(field != null) {
				char[][] extensions = new char[2][];
				extensions[0] = new char[] {'.', 'j', 'a', 'v', 'a'};
				extensions[1] = new char[] {'.', 'J', 'A', 'V', 'A'};
				field.set(null, extensions);
			}
		}
	}
	
}
