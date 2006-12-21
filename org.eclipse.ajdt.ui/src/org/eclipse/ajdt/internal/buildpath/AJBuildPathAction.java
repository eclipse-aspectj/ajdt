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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.ArchiveFileFilter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

public abstract class AJBuildPathAction {

	protected IFile jarFile;

	public AJBuildPathAction() {
		super();
	}

	private static IFile getCandidate(IAdaptable element) throws JavaModelException {
		IResource resource = (IResource) element.getAdapter(IResource.class);
		if (!(resource instanceof IFile)
				|| !ArchiveFileFilter.isArchivePath(resource.getFullPath()))
			return null;
	
		IJavaProject project = JavaCore.create(resource.getProject());
		if (project != null
				&& project.exists()
				&& (project.findPackageFragmentRoot(resource.getFullPath()) == null))
			return (IFile) resource;
		return null;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	protected static IFile getJARFile(IStructuredSelection selection) throws JavaModelException {
		if (selection.size() != 1) {
			return null;
		}
		Object element = selection.getFirstElement();
		if (element instanceof IAdaptable) {
			IFile file = getCandidate((IAdaptable) element);
			if (file != null) {
				return file;
			}
		}
		return null;
	}
	
	protected boolean checkIfAddingOutjar(IProject project) {

		String inpath = jarFile.getFullPath().toPortableString();
		String outJar = AspectJCorePreferences.getProjectOutJar(project);
			if (outJar.length()>0 && (inpath.indexOf(outJar) != -1)) {
				return true;
			}
		
		return false;
	}

}