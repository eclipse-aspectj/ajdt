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

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;

public class RemoveFromInpathAction extends AJBuildPathAction implements IObjectActionDelegate {

	public void run(IAction action) {
		IProject project = jarFile.getProject();
		IJavaProject javaProject = JavaCore.create(project);
		String jarPath = jarFile.getFullPath().toPortableString();
		try {
			IClasspathEntry[] cp = javaProject.getRawClasspath();
			for (int i = 0; i < cp.length; i++) {
				if (cp[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					String entry = JavaCore.getResolvedClasspathEntry(cp[i])
							.getPath().toPortableString();
					if (entry.equals(jarPath)) {
						IClasspathAttribute[] attributes = cp[i]
								.getExtraAttributes();
						IClasspathAttribute[] newattrib = new IClasspathAttribute[attributes.length - 1];
						int count = 0;
						for (int j = 0; j < attributes.length; j++) {
							if (!attributes[j]
									.equals(AspectJCorePreferences.INPATH_ATTRIBUTE)) {
								newattrib[count++] = attributes[j];
							}
						}
						cp[i] = JavaCore.newLibraryEntry(cp[i].getPath(), cp[i]
								.getSourceAttachmentPath(), cp[i]
								.getSourceAttachmentRootPath(), cp[i]
								.getAccessRules(), newattrib, cp[i]
								.isExported());
					}
				}
			}
			javaProject.setRawClasspath(cp, null);
		} catch (JavaModelException e) {
		}
		AJDTUtils.refreshPackageExplorer();
	}

	public void selectionChanged(IAction action, ISelection sel) {
		boolean enable = false;
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sel;
			Object element = selection.getFirstElement();
			try {
				if (element instanceof IPackageFragmentRoot) {
					jarFile = (IFile)((IPackageFragmentRoot)element).getUnderlyingResource();
				}
				if (jarFile != null){
					IProject project = jarFile.getProject();
					enable = AspectJCorePreferences.isOnInpath(project,jarFile.getFullPath().toPortableString());
				}
			} catch (JavaModelException e) {
			}
			action.setEnabled(enable);
		}
	}

}
