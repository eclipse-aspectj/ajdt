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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import org.eclipse.ajdt.internal.bc.BuildConfiguration;
import org.eclipse.ajdt.internal.ui.ImageDecorator;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jdt.internal.ui.viewsupport.DecoratingJavaLabelProvider;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.internal.console.ConsoleView;
import org.eclipse.ui.part.IPage;

/**
 * Build configuration visual tests
 */
public class BuildConfigurationTest extends VisualTestCase {
	
	IConsole console;

	/**
	 * Build configuration test
	 * @throws Exception
	 */
	public void test1() throws Exception {
		
		// Open the 'New' wizard
		postKeyDown(SWT.CTRL);		
		postKey('n');
		postKeyUp(SWT.CTRL);
		
		// give the wizard chance to pop up
		Runnable r = new Runnable() {
			public void run() {
				sleep();
				
				// Open the 'New AspectJ Project' wizard
				postKey(SWT.CR);

				sleep();

				// Enter a name for the project
				postString("Project1"); //$NON-NLS-1$

				// Complete the wizard
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		
		// Wait for the project to be created
		waitForJobsToComplete();
		
		final IWorkspace workspace= JavaPlugin.getWorkspace();		
		
		new DisplayHelper() {

			protected boolean condition() {
				boolean ret = workspace.getRoot().getProject("Project1").exists(); //$NON-NLS-1$
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);

		IProject project = workspace.getRoot().getProject("Project1"); //$NON-NLS-1$

		assertTrue("Should have created a project", project.exists());	 //$NON-NLS-1$
		
		// Test that a build file has been created
		IFile buildFile = checkBuildFileExists(project);
		
		// Test that the new build file has the correct contents
		checkOriginalContents(buildFile);

		// Create a source directory and check that the build file updates correctly
		addNewSourceFolderAndCheckBuildFile(buildFile);
		
		// Create a package and a class and test that they are included in the build
		addNewPackage();
		IJavaProject jp = JavaCore.create(project);
		IPackageFragment p1 = jp.getPackageFragmentRoot(project.findMember("src")).getPackageFragment("p1"); //$NON-NLS-1$ //$NON-NLS-2$
		PackageExplorerPart packageExplorer = PackageExplorerPart.getFromActivePerspective();
		packageExplorer.setFocus();
		packageExplorer.selectAndReveal(p1);
		addNewClass();				
		IResource res = project.findMember("src/p1/Hello.java"); //$NON-NLS-1$
		assertNotNull("New class Hello.java wan't created",res); //$NON-NLS-1$
		
		DecoratingJavaLabelProvider djlp = (DecoratingJavaLabelProvider)packageExplorer.getTreeViewer().getLabelProvider();
		Image image = djlp.getImage(p1);
		Image expected = JavaPlugin.getImageDescriptorRegistry().get(ImageDecorator.getJavaImageDescriptor(JavaPluginImages.DESC_OBJS_PACKAGE, image.getBounds(), 0));
		assertTrue("The new package should have a filled-in image", expected.equals(image)); //$NON-NLS-1$
		
		// Add a main method and run the class to test that it has been built
		ICompilationUnit hello = p1.getCompilationUnit("Hello.java"); //$NON-NLS-1$
		addMainMethod(hello);
		packageExplorer.setFocus();
		packageExplorer.selectAndReveal(hello);

		postKeyDown(SWT.ALT);
		postKey('r');	
		postKeyUp(SWT.ALT);
		postKey('s');
		if(!runningEclipse31) {
			postKey('s');
			postKey(SWT.ARROW_RIGHT);
			postKey(SWT.ARROW_DOWN);
		}		
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.CR);
		
		waitForJobsToComplete();
		ConsoleView cview = null;
		IViewReference[] views = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
		for (int i = 0; i < views.length; i++) {
			if (views[i].getView(false) instanceof ConsoleView) {
				cview = (ConsoleView)views[i].getView(false);
			}
		}
		assertNotNull("Console view should be open", cview); //$NON-NLS-1$
		String output = null;
		IPage page = cview.getCurrentPage();
		Class cl = page.getClass();
		try {
			Method m = cl.getMethod("getConsoleViewer", new Class[0]); //$NON-NLS-1$
			Object o = m.invoke(page, new Object[0]);
			TextViewer viewer = (TextViewer)o;
			output = viewer.getDocument().get();
		} catch (NoSuchMethodException nsme) {
			// We are on Eclipse 3.1
			Method m = cl.getMethod("getViewer", new Class[0]); //$NON-NLS-1$
			Object o = m.invoke(page, new Object[0]);
			TextViewer viewer = (TextViewer)o;
			output = viewer.getDocument().get();
		}
		assertNotNull(output);
		assertTrue("program did not run correctly", output.indexOf("Hello") != -1); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @param hello
	 * @throws CoreException
	 */
	private void addMainMethod(ICompilationUnit hello) throws CoreException {
		IFile helloFile = (IFile)hello.getResource();
		String s = "package p1; \n" + //$NON-NLS-1$
			"public class Hello { \n\n" + //$NON-NLS-1$
			"	public static void main(String[] args) { \n" + //$NON-NLS-1$
			"		System.out.println(\"Hello\"); \n" +				 //$NON-NLS-1$
			"	} \n" + 	 //$NON-NLS-1$
			"} \n"; //$NON-NLS-1$
		InputStream stream = new ByteArrayInputStream(s.getBytes()); 
		helloFile.setContents(stream, true, true, null);
		waitForJobsToComplete();
	}

	private void addNewClass() {
		postKeyDown(SWT.ALT);
		postKeyDown(SWT.SHIFT);
		postKey('n');
		postKeyUp(SWT.SHIFT);
		postKeyUp(SWT.ALT);

		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);			
	
		postKey(SWT.CR);
		postString("Hello"); //$NON-NLS-1$
		postKey(SWT.CR);
		waitForJobsToComplete();	
	}

	private void addNewPackage() {
		postKeyDown(SWT.ALT);
		postKeyDown(SWT.SHIFT);
		postKey('n');
		postKeyUp(SWT.SHIFT);
		postKeyUp(SWT.ALT);
		
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.CR);
		
		Runnable r = new Runnable() {
			
			public void run() {
				sleep();
				postKey('p');
				postKey('1');
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		
		waitForJobsToComplete();
	}

	private void addNewSourceFolderAndCheckBuildFile(IFile buildFile) throws CoreException, IOException {
		PackageExplorerPart.getFromActivePerspective().setFocus();
		
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
		if (runningEclipse31) {
			postKey(SWT.ARROW_DOWN);
			postKey(SWT.ARROW_DOWN);			
		}
		postKey(SWT.CR);
		
		Runnable r = new Runnable() {
			public void run() {
				sleep();
				postString("src"); //$NON-NLS-1$
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		
		waitForJobsToComplete();
	
		InputStream stream = buildFile.getContents();
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String line1 = br.readLine();
		br.close();
		assertTrue("Contents of the build configuration file are wrong after adding a source folder", line1.trim().equals("src.includes = src/"));		 //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Check that when the build file is first created it has the correct contents
	 * @param buildFile
	 */
	private void checkOriginalContents(IFile buildFile) throws CoreException, IOException {
		InputStream stream = buildFile.getContents();
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String line1 = br.readLine();
		br.close();
		assertTrue("Original contents of the build configuration file are wrong", line1.trim().equals("src.includes = /")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private IFile checkBuildFileExists(IProject project) {
		IFile buildFile = (IFile)project.findMember(BuildConfiguration.STANDARD_BUILD_CONFIGURATION_FILE);		
		assertTrue("Should have created a build configuration file", buildFile.exists()); //$NON-NLS-1$
		return buildFile;
	}
	
}
