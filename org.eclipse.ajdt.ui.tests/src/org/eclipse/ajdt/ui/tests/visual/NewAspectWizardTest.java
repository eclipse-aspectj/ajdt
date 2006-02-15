/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.visual;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.swt.SWT;

public class NewAspectWizardTest extends VisualTestCase {

	/*
	 * Format of each entry is:
	 * 
	 * Aspect name, Supertype name, {makeDefault, makeAbstract, makeFinal,
	 * makePrivileged, createMain}, perClause option
	 * (1=issingleton,2=perthis,3=pertarget,4=percflow,5=percflowbelow,6=pertypewithin),
	 * expected contents
	 * 
	 * Note: the wizard remembers the createMain and createInheritedPointcuts settings
	 * so if a test sets one, the next test needs to reverse it. The defaults are
	 * createMain=off, createInheritedPointcuts=on
	 */
	private static final Object[][] testData = new Object[][] {
			{
					"Aspect1", //$NON-NLS-1$
					"", //$NON-NLS-1$
					new Boolean[] { Boolean.FALSE, Boolean.FALSE,
							Boolean.FALSE, Boolean.FALSE, Boolean.FALSE },
					new Integer(0), "package tjp;public aspect Aspect1 {}" }, //$NON-NLS-1$
			{
					"Aspect2", //$NON-NLS-1$
					"", //$NON-NLS-1$
					new Boolean[] { Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
							Boolean.FALSE, Boolean.FALSE }, new Integer(0),
					"package tjp;aspect Aspect2 {}" }, //$NON-NLS-1$
			{
					"Aspect3", //$NON-NLS-1$
					"", //$NON-NLS-1$
					new Boolean[] { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE,
							Boolean.FALSE, Boolean.FALSE }, new Integer(0),
					"package tjp;public abstract aspect Aspect3 {}" }, //$NON-NLS-1$
			{
					"Aspect4", //$NON-NLS-1$
					"", //$NON-NLS-1$
					new Boolean[] { Boolean.FALSE, Boolean.FALSE, Boolean.TRUE,
							Boolean.FALSE, Boolean.FALSE }, new Integer(0),
					"package tjp;public final aspect Aspect4 {}" }, //$NON-NLS-1$
			{
					"Aspect5", //$NON-NLS-1$
					"", //$NON-NLS-1$
					new Boolean[] { Boolean.FALSE, Boolean.FALSE,
							Boolean.FALSE, Boolean.TRUE, Boolean.FALSE },
					new Integer(0),
					"package tjp;public privileged aspect Aspect5 {}" }, //$NON-NLS-1$
			{
					"Aspect6", //$NON-NLS-1$
					"", //$NON-NLS-1$
					new Boolean[] { Boolean.TRUE, Boolean.TRUE, Boolean.FALSE,
							Boolean.FALSE, Boolean.FALSE }, new Integer(0),
					"package tjp;abstract aspect Aspect6 {}" }, //$NON-NLS-1$
			{
					"Aspect7", //$NON-NLS-1$
					"", //$NON-NLS-1$
					new Boolean[] { Boolean.TRUE, Boolean.FALSE, Boolean.TRUE,
							Boolean.FALSE, Boolean.FALSE }, new Integer(0),
					"package tjp;final aspect Aspect7 {}" }, //$NON-NLS-1$
			{
					"Aspect8", //$NON-NLS-1$
					"", //$NON-NLS-1$
					new Boolean[] { Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
							Boolean.TRUE, Boolean.FALSE }, new Integer(0),
					"package tjp;privileged aspect Aspect8 {}" }, //$NON-NLS-1$
			{
					"Aspect9", //$NON-NLS-1$
					"", //$NON-NLS-1$
					new Boolean[] { Boolean.FALSE, Boolean.FALSE,
							Boolean.FALSE, Boolean.FALSE, Boolean.FALSE },
					new Integer(1),
					"package tjp;public aspect Aspect9 issingleton() {}" }, //$NON-NLS-1$
			{
					"Aspect10", //$NON-NLS-1$
					"", //$NON-NLS-1$
					new Boolean[] { Boolean.FALSE, Boolean.FALSE,
							Boolean.FALSE, Boolean.FALSE, Boolean.TRUE },
					new Integer(0),
					"package tjp;public aspect Aspect10 {\t/**\t * @param args\t */\tpublic static void main(String[] args) {\t\t// TODO Auto-generated method stub	}}" }, //$NON-NLS-1$
			{
					"Aspect11", //$NON-NLS-1$
					"AbstractGetInfo", //$NON-NLS-1$
					new Boolean[] { Boolean.FALSE, Boolean.FALSE,
							Boolean.FALSE, Boolean.FALSE, Boolean.TRUE },
					new Integer(0),
					"package tjp;import foo.AbstractGetInfo;public aspect Aspect11 extends AbstractGetInfo {\tpublic pointcut goCut();}" } //$NON-NLS-1$

	};

	public void testNewAspectWizard() throws Exception {
		IProject project = createPredefinedProject("OpenDeclaration"); //$NON-NLS-1$
		IJavaProject jp = JavaCore.create(project);
		IPackageFragment pack = jp.getPackageFragmentRoot(
				project.findMember("src")).getPackageFragment("tjp"); //$NON-NLS-1$ //$NON-NLS-2$
		// make sure ajdt knows about all the .aj files - we probably shouldn't
		// need this
		AJCompilationUnitManager.INSTANCE.initCompilationUnits(AspectJPlugin
				.getWorkspace());
		PackageExplorerPart packageExplorer = PackageExplorerPart
				.getFromActivePerspective();
		packageExplorer.setFocus();
		packageExplorer.selectAndReveal(pack);
		IFolder folder = (IFolder) pack.getResource();

		for (int i = 0; i < testData.length; i++) {
			String aspectName = (String) testData[i][0];
			String superName = (String) testData[i][1];
			Boolean[] args = (Boolean[]) testData[i][2];
			Integer perClause = (Integer) testData[i][3];
			String expected = (String) testData[i][4];
			addNewAspect(aspectName, superName, args[0].booleanValue(), args[1]
					.booleanValue(), args[2].booleanValue(), args[3]
					.booleanValue(), args[4].booleanValue(), perClause
					.intValue());
			IFile file = (IFile) folder.findMember(aspectName + ".aj"); //$NON-NLS-1$
			assertNotNull(
					"New Aspect Wizard didn't create a .aj file for aspect: " //$NON-NLS-1$
							+ aspectName, file);
			String contents = readFile(file);
			if(!contents.equals(expected)) {
				System.out.println("CONTENTS: " + contents);
				System.out.println("EXPECTED: " + expected);
			}
			assertEquals("Contents of new aspect " + aspectName //$NON-NLS-1$
					+ " don't match expected", expected, contents); //$NON-NLS-1$
		}
	}

	private String readFile(IFile file) throws Exception {
		StringBuffer contents = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(file
				.getContents()));
		String line = br.readLine();
		while (line != null) {
			contents.append(line);
			line = br.readLine();
		}
		br.close();
		return contents.toString();
	}

	private void addNewAspect(final String aspectName, final String superName,
			final boolean makeDefault, final boolean makeAbstract,
			final boolean makeFinal, final boolean makePrivileged,
			final boolean createMain, final int perClause) {
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
		postKey(SWT.CR);

		Runnable r = new Runnable() {
			public void run() {
				sleep();
				postString(aspectName);
				postKey(SWT.TAB);
				if (makeDefault) {
					postKey(SWT.ARROW_RIGHT);
				}
				postKey(SWT.TAB);
				if (makeAbstract) {
					postKey(' ');
				}
				postKey(SWT.TAB);
				if (makeFinal) {
					postKey(' ');
				}
				postKey(SWT.TAB);
				if (makePrivileged) {
					postKey(' ');
				}
				postKey(SWT.TAB);
				if (perClause > 0) {
					postKey(' ');
					postKey(SWT.TAB);
					for (int i = 0; i < perClause - 1; i++) {
						postKey(SWT.ARROW_RIGHT);
					}
				}
				postKey(SWT.TAB);
				postKey(SWT.TAB); // now on supertype Browse button
				if (superName.length() > 0) {
					postKey(' ');
					sleep();
					postString(superName);
					sleep();
					postKey(SWT.CR);
					sleep();
				}
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				if (createMain) {
					postKey(' ');
				}
				postKey(SWT.CR);
			}
		};
		new Thread(r).start();
		waitForJobsToComplete();
	}
}
