/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.visual.tests;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.swt.SWT;

public class DeleteAJTest extends VisualTestCase {

	public void testDeleteAJWithNoSrcFolder() throws CoreException {
		IProject project = createPredefinedProject("WithoutSourceFolder"); //$NON-NLS-1$
		deleteFile(project, "A.aj"); //$NON-NLS-1$
	}

	public void testDeleteAJWithSrcFolder() throws CoreException {
		IProject project = createPredefinedProject("Simple AJ Project"); //$NON-NLS-1$
		deleteFile(project, "src/AspectInDefaultPackage.aj"); //$NON-NLS-1$
		deleteFile(project, "src/p2/Aspect.aj"); //$NON-NLS-1$
	}

	private void deleteFile(IProject project, String aj) {
		IFile ajFile = (IFile)project.findMember(aj);
		assertNotNull("Could not find file for "+aj,ajFile); //$NON-NLS-1$
		
		AJCompilationUnit ajUnit = AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(ajFile);
		assertNotNull("Could not find AJCompilationUnit for "+aj,ajUnit);  //$NON-NLS-1$
		
		PackageExplorerPart packageExplorer = PackageExplorerPart
			.getFromActivePerspective();
		packageExplorer.setFocus();
		packageExplorer.selectAndReveal(ajUnit);
		
		waitForJobsToComplete();
		
		postKey(SWT.DEL);
		Runnable r = new Runnable() {
			public void run() {
				sleep();
				postKey(SWT.CR);
				sleep();
			}
		};
		new Thread(r).start();
		
		waitForJobsToComplete();

		IResource ajFile2 = project.findMember(aj);
		assertNull(aj+" was not successfully deleted", ajFile2); //$NON-NLS-1$		
	}
}
