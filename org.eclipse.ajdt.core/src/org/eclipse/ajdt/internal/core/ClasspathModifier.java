/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/*
 * Selected methods copied from JDT/UI
 * org.eclipse.jdt.internal.corext.buildpath.ClasspathModifer
 */
public class ClasspathModifier {
	/**
	 * Find out whether the <code>IResource</code> excluded or not.
	 * 
	 * @param resource
	 *            the resource to be checked
	 * @param project
	 *            the Java project
	 * @return <code>true</code> if the resource is excluded, <code>
	 * false</code>
	 *         otherwise
	 * @throws JavaModelException
	 */
	public static boolean isExcluded(IResource resource, IJavaProject project)
			throws JavaModelException {
		IPackageFragmentRoot root = getFragmentRoot(resource, project, null);
		if (root == null)
			return false;
		String fragmentName = getName(resource.getFullPath(), root.getPath());
		fragmentName = completeName(fragmentName);
		IClasspathEntry entry = root.getRawClasspathEntry();
		return entry != null
				&& contains(new Path(fragmentName), entry
						.getExclusionPatterns());
	}

	/**
	 * Find out whether one of the <code>IResource</code>'s parents
	 * is excluded.
	 * 
	 * @param resource check the resources parents whether they are
	 * excluded or not
	 * @param project the Java project
	 * @return <code>true</code> if there is an excluded parent, 
	 * <code>false</code> otherwise
	 * @throws JavaModelException
	 */
	public static boolean parentExcluded(IResource resource, IJavaProject project) throws JavaModelException {
		if (resource.getFullPath().equals(project.getPath()))
			return false;
		IPackageFragmentRoot root= getFragmentRoot(resource, project, null);
		if (root == null) {
			return true;
		}
		IPath path= resource.getFullPath().removeFirstSegments(root.getPath().segmentCount());
		IClasspathEntry entry= root.getRawClasspathEntry();
		if (entry == null)
			return true; // there is no build path entry, this is equal to the fact that the parent is excluded
		while (path.segmentCount() > 0) {
			if (contains(path, entry.getExclusionPatterns()))
				return true;
			path= path.removeLastSegments(1);
		}
		return false;
	}

	/**
	 * Get the source folder of a given <code>IResource</code> element,
	 * starting with the resource's parent.
	 * 
	 * @param resource
	 *            the resource to get the fragment root from
	 * @param project
	 *            the Java project
	 * @param monitor
	 *            progress monitor, can be <code>null</code>
	 * @return resolved fragment root
	 * @throws JavaModelException
	 */
	public static IPackageFragmentRoot getFragmentRoot(IResource resource,
			IJavaProject project, IProgressMonitor monitor)
			throws JavaModelException {
		IJavaElement javaElem = null;
		if (resource.getFullPath().equals(project.getPath()))
			return project.getPackageFragmentRoot(resource);
		IContainer container = resource.getParent();
		do {
			if (container instanceof IFolder)
				javaElem = JavaCore.create((IFolder) container);
			if (container.getFullPath().equals(project.getPath())) {
				javaElem = project;
				break;
			}
			container = container.getParent();
			if (container == null)
				return null;
		} while (javaElem == null
				|| !(javaElem instanceof IPackageFragmentRoot));
		if (javaElem instanceof IJavaProject)
			javaElem = project.getPackageFragmentRoot(project.getResource());
		return (IPackageFragmentRoot) javaElem;
	}

	/**
	 * Returns a string corresponding to the <code>path</code> with the
	 * <code>rootPath<code>'s number of segments
	 * removed
	 * 
	 * @param path path to remove segments
	 * @param rootPath provides the number of segments to
	 * be removed
	 * @return a string corresponding to the mentioned
	 * action
	 */
	private static String getName(IPath path, IPath rootPath) {
		return path.removeFirstSegments(rootPath.segmentCount()).toString();
	}

	/**
	 * Add a '/' at the end of the name if it does not end with '.java', or
	 * other Java-like extension.
	 * 
	 * @param name
	 *            append '/' at the end if necessary
	 * @return modified string
	 */
	private static String completeName(String name) {
		if (!JavaCore.isJavaLikeFileName(name)) {
			name = name + "/"; //$NON-NLS-1$
			name = name.replace('.', '/');
			return name;
		}
		return name;
	}

	/**
	 * Find out whether the provided path equals to one in the array.
	 * 
	 * @param path
	 *            path to find an equivalent for
	 * @param paths
	 *            set of paths to compare with
	 * @return <code>true</code> if there is an occurrence, <code>
	 * false</code>
	 *         otherwise
	 */
	private static boolean contains(IPath path, IPath[] paths) {
		if (path == null)
			return false;
		if (path.getFileExtension() == null)
			path = new Path(completeName(path.toString()));
		for (int i = 0; i < paths.length; i++) {
			if (paths[i].equals(path))
				return true;
		}
		return false;
	}

}
