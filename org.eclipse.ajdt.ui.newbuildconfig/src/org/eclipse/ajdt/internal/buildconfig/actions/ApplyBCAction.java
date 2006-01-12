/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sian January - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.buildconfig.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * This action is triggered from a popup menu on a build configuration
 * file. It applies the saved exclusion filters to the project.
 */
public class ApplyBCAction implements IWorkbenchWindowActionDelegate {

	private IFile currentlySelectedFile = null;
	
	/**
	 *  Executed when "Apply Build Configuration" in the context menu is clicked
	 */
	public void run(IAction action) {
		// TODO: Get this to work with build configuration files in the old style
		if (currentlySelectedFile != null) {
			File file = currentlySelectedFile.getLocation().toFile();
			BufferedReader br = null;
			try {
				IJavaProject project = JavaCore.create(currentlySelectedFile.getProject());
				List classpathEntries = new ArrayList();
				List cplistelements = ClasspathModifier.getExistingEntries(project);
				for (Iterator iter = cplistelements.iterator(); iter.hasNext();) {
					CPListElement element = (CPListElement) iter.next();
					if (element.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
						classpathEntries.add(element.getClasspathEntry());
					}
				}
				br = new BufferedReader(new FileReader(file));
				String line = br.readLine();
				while (line != null) {
					if (line.startsWith("src.includes")) {
						String pathStr = line.substring(15);
						IPath path = project.getPath().append(pathStr);
						line = br.readLine();						
						List exclusions = new ArrayList();
						while (line != null && line.startsWith("src.excludes")) {
							String exclusionPathStr = line.substring(15);
							if (exclusionPathStr.startsWith(pathStr)) {
								exclusionPathStr = exclusionPathStr.substring(pathStr.length());
							}
							IPath exclusionPath = //project.getPath().append(exclusionPathStr);
							new Path(exclusionPathStr);
							exclusions.add(exclusionPath);
							line = br.readLine();
						}
						IPath[] exclusionPatterns = new IPath[exclusions.size()];
						for (int i = 0; i < exclusionPatterns.length; i++) {
							exclusionPatterns[i] = (IPath) exclusions.get(i);
						}
						IClasspathEntry classpathEntry = new ClasspathEntry(IPackageFragmentRoot.K_SOURCE, IClasspathEntry.CPE_SOURCE, path, ClasspathEntry.INCLUDE_ALL, exclusionPatterns, null, null, null, true, ClasspathEntry.NO_ACCESS_RULES, false, ClasspathEntry.NO_EXTRA_ATTRIBUTES);
						classpathEntries.add(classpathEntry);
					} else {
						line = br.readLine();
					}
				}
				IClasspathEntry[] entries = new IClasspathEntry[classpathEntries.size()];
				for (int i = 0; i < entries.length; i++) {
					entries[i] = (IClasspathEntry) classpathEntries.get(i);
				}
				((JavaProject)project).setRawClasspath(entries, null);
			} catch (FileNotFoundException e) {
			} catch (JavaModelException e) {
			} catch (IOException e) {
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

//  For Debugging:
//	private void printFiles(List files) {
//		for (Iterator iter = files.iterator(); iter.hasNext();) {
//			IFile file = (IFile) iter.next();
//			System.out.println(file.getName());
//		}
//		System.out.println("");
//	}


	private List getRemovedFiles(List currentlyIncludedFiles, List files) {
		List removedFiles = new ArrayList();
		for (Iterator iter = currentlyIncludedFiles.iterator(); iter.hasNext();) {
			Object file = (Object) iter.next();
			boolean found = false;
			for (Iterator iterator = files.iterator(); iterator
					.hasNext();) {
				Object file2 = (Object) iterator.next();
				if (file.equals(file2)) {
					found = true;
					break;
				}
			}	
			if (!found) {
				removedFiles.add(file);
			}
		}
		return removedFiles;
	}



	private List getNewFiles(List currentlyIncludedFiles, List files) {
		List newFiles = new ArrayList();
		for (Iterator iter = files.iterator(); iter.hasNext();) {
			Object file = iter.next();
			boolean found = false;
			for (Iterator iterator = currentlyIncludedFiles.iterator(); iterator.hasNext();) {
				Object file2 = iterator.next();
				if (file.equals(file2)) {
					found = true;
					break;
				}
			}
			if (!found) {
				newFiles.add(file);
			}
			
		}
		return newFiles;
	}


	/**
	 * Selection has changed - if we've selected a build file then remember it
	 * as the new project build config.
	 */
	public void selectionChanged(IAction action, ISelection selection) {

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object first = structuredSelection.getFirstElement();
			if (first instanceof IFile) {
				currentlySelectedFile = (IFile) first;
			}
		}
	}

	/**
	 * From IWorkbenchWindowActionDelegate
	 */
	public void dispose() {}

	/**
	 * From IWorkbenchWindowActionDelegate
	 */
	public void init(IWorkbenchWindow window) {
	}
}