/*******************************************************************************
 * Copyright (c) 2000, 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Luzius Meisser  - adjusted for ajdoc
 *     Helen Hawkins   - updated for Eclipse 3.1 (bug 109484)
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.ajdocexport;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Copied from org.eclipse.jdt.internal.ui.javadocexport.JavadocProjectContentProvider
 * Updated for eclipse 3.1
 * Changes marked with // AspectJ Extension
 */
public class AJdocProjectContentProvider implements ITreeContentProvider {

	/*
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parentElement) {
		// AspectJ Extension - we only want a list of projects since the
		// build config determines what is built
/*		try {
			if (parentElement instanceof IJavaProject) {
				IJavaProject project= (IJavaProject) parentElement;
				return getPackageFragmentRoots(project);
			} else if (parentElement instanceof IPackageFragmentRoot) {
				return getPackageFragments((IPackageFragmentRoot) parentElement);
			}
		} catch (JavaModelException e) {
			JavaPlugin.log(e);
		} */
		return new Object[0];
	}

	/*
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object inputElement) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		try {
			// AspectJ Extension begin - only return aspectj projects
			IJavaProject[] javaprojects = JavaCore.create(root).getJavaProjects();
			List<IJavaProject> ajProjects = new ArrayList<>(javaprojects.length);
			for (IJavaProject javaproject : javaprojects) {
				if (AspectJPlugin.isAJProject(javaproject.getProject()))
					ajProjects.add(javaproject);
			}
			return ajProjects.toArray();
			// AspectJ Extension end
		}
		catch (JavaModelException e) {
			JavaPlugin.log(e);
		}
		return new Object[0];
	}

	/*
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object element) {

		IJavaElement parent= ((IJavaElement)element).getParent();
		if (parent instanceof IPackageFragmentRoot) {
			IPackageFragmentRoot root= (IPackageFragmentRoot) parent;
			if (root.getPath().equals(root.getJavaProject().getProject().getFullPath())) {
				return root.getJavaProject();
			}
		}
		return parent;
	}

	/*
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element) {
		return (getChildren(element).length > 0);
	}

	/*
	 * @see IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/*
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	// AspectJ Extension - commenting out unused code
/*	private Object[] getPackageFragmentRoots(IJavaProject project) throws JavaModelException {
		ArrayList result= new ArrayList();

		IPackageFragmentRoot[] roots= project.getPackageFragmentRoots();
		for (int i= 0; i < roots.length; i++) {
			IPackageFragmentRoot root= roots[i];
			if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
				if (root.getPath().equals(root.getJavaProject().getPath())) {
					Object[] packageFragments= getPackageFragments(root);
					for (int k= 0; k < packageFragments.length; k++) {
						result.add(packageFragments[k]);
					}
				} else {
					result.add(root);
				}
			}
		}
		return result.toArray();
	}

	private Object[] getPackageFragments(IPackageFragmentRoot root) throws JavaModelException {
		ArrayList packageFragments= new ArrayList();

		IJavaElement[] children= root.getChildren();
		for (int i= 0; i < children.length; i++) {
			if (((IPackageFragment) children[i]).containsJavaResources())
				packageFragments.add(children[i]);
		}
		return packageFragments.toArray();
	}*/


}
