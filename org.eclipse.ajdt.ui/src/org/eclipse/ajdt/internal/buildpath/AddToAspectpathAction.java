/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Ford - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.buildpath;

import java.io.File;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;

public class AddToAspectpathAction extends AJBuildPathAction implements IObjectActionDelegate {

	public void run(IAction action) {
		String contentKind = new Integer(IPackageFragmentRoot.K_BINARY)
				.toString();
		String entryKind = new Integer(IClasspathEntry.CPE_LIBRARY).toString();
		IProject project = jarFile.getProject();

		StringBuffer finalAspectpath = new StringBuffer();
		StringBuffer finalcontentKind = new StringBuffer();
		StringBuffer finalentryKind = new StringBuffer();
		String[] oldAspectpath = AspectJCorePreferences
				.getProjectAspectPath(project);

		String Aspectpath = jarFile.getFullPath().toPortableString();
		String[] seperatedOldAspectpath = oldAspectpath[0].split(";"); //$NON-NLS-1$

		boolean build = true;

		for (int j = 0; j < seperatedOldAspectpath.length; j++) {
			if ((seperatedOldAspectpath[j].equals(Aspectpath))) {
				build = false;
			}
		}

		if (build) {
			finalAspectpath.append(oldAspectpath[0]);
			finalAspectpath.append(Aspectpath);
			finalAspectpath.append(File.pathSeparator);

			finalcontentKind.append(oldAspectpath[1]);
			finalcontentKind.append(contentKind);
			finalcontentKind.append(File.pathSeparator);

			finalentryKind.append(oldAspectpath[2]);
			finalentryKind.append(entryKind);
			finalentryKind.append(File.pathSeparator);

			AspectJCorePreferences.setProjectAspectPath(project,
					finalAspectpath.toString(), finalcontentKind.toString(),
					finalentryKind.toString());
			try {
				project.build(IncrementalProjectBuilder.FULL_BUILD, null);
			} catch (CoreException e) {
			}
		}

	}

	public void selectionChanged(IAction action, ISelection sel) {
		boolean enable = false;
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sel;
			try {
				jarFile = getJARFile(selection);
				if (jarFile != null) {
					IProject project = jarFile.getProject();
					enable = (!checkIfOnAspectpath(project)&&
							!checkIfAddingOutjar(project));
				}
			} catch (JavaModelException e) {
			}
			action.setEnabled(enable);
		}
	}

}
