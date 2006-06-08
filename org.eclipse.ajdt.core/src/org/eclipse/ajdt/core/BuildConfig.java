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
package org.eclipse.ajdt.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.ajdt.internal.core.ClasspathModifier;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class BuildConfig {
	
	private static Map projectsToIncludedSourceFiles = new WeakHashMap();
	
	/**
	 * Returns all of the currently included source files in a project
	 * This list is cached and reset every build (or on request by calling flushIncludedSourceFileCache)
	 * @param project
	 * @return a list of IFiles
	 */
	public static List /*IFile*/ getIncludedSourceFiles(IProject project) {
		if(projectsToIncludedSourceFiles.get(project) instanceof List) {
			return (List) projectsToIncludedSourceFiles.get(project);
		}
		List sourceFiles = new ArrayList();
		try {
			IJavaProject jp = JavaCore.create(project);
			IClasspathEntry[] cpes = jp.getRawClasspath();
			for (int i = 0; i < cpes.length; i++) {
				if (cpes[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath path = cpes[i].getPath();
					IResource res = project.findMember(path
							.removeFirstSegments(1));
					if ((res != null) && (res instanceof IContainer)) {
						List l = allFiles((IContainer) res);
						for (Iterator iter = l.iterator(); iter.hasNext();) {
							IFile file = (IFile) iter.next();
							if(!((ClasspathModifier.isExcluded(file, jp)) || ClasspathModifier.parentExcluded(file, jp))) {
								sourceFiles.add(file);
							}
						}
					}
				}
			}
		} catch (JavaModelException e) {
		}
		projectsToIncludedSourceFiles.put(project, sourceFiles);
		return sourceFiles;
	}
	
	/**
	 * Invalidate the list of included source files for a project
	 * @param project
	 */
	public static void flushIncludedSourceFileCache(IProject project) {
		projectsToIncludedSourceFiles.remove(project);
	}
		
	/**
	 * Find out whether a file is included.  This does NOT use the cached version,
	 * so if you are calling it a lot and don't need the most up-to date information
	 * it would be better to use getIncludedSourceFiles(file.getProject()).contains(file) instead.
	 * @param file
	 * @return
	 */
	public static boolean isIncluded(IResource file) {
		IJavaProject jp = JavaCore.create(file.getProject());
		try {
			return !(ClasspathModifier.isExcluded(file, jp) || ClasspathModifier.parentExcluded(file, jp));
		} catch (JavaModelException e) {
		}
		return true;
	}
	
	//return a list of all IFiles in the given folder, including all
	// sub-folders
	private static List allFiles(IContainer folder) {
		final List contents = new ArrayList();
		try {
			folder.accept(new IResourceVisitor() {
				public boolean visit(IResource res) {
					if (res.getType() == IResource.FILE
							&& JavaCore.isJavaLikeFileName((res
									.getName()))) {
						contents.add(res);
					}
					return true;
				}
			});
		} catch (CoreException e) {
		}
		return contents;
	}
}
