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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.internal.bc.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.editor.BuildProperties;
import org.eclipse.ajdt.ui.buildconfig.DefaultBuildConfigurator;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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
		if (currentlySelectedFile != null) {
			BuildProperties properties = new BuildProperties(currentlySelectedFile);
			List files = properties.getFiles(true);
			List currentlyIncludedFiles = ((BuildConfiguration)DefaultBuildConfigurator.getBuildConfigurator().getProjectBuildConfigurator(currentlySelectedFile.getProject()).getActiveBuildConfiguration()).getIncludedIResourceFiles(CoreUtils.ASPECTJ_SOURCE_FILTER);
			List newFiles = getNewFiles(currentlyIncludedFiles, files);
			List removedFiles = getRemovedFiles(currentlyIncludedFiles, files);
			List newJavaElements = new ArrayList();
			for (Iterator iter = newFiles.iterator(); iter.hasNext();) {
				IFile file = (IFile) iter.next();
				IJavaElement element;
				if(CoreUtils.ASPECTJ_SOURCE_ONLY_FILTER.accept(file.getName())) {
					element = AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(file);
				} else {
					element = JavaCore.create(file);
				}
				newJavaElements.add(element);	
			}
			List removedJavaElements = new ArrayList();
			for (Iterator iter = removedFiles.iterator(); iter.hasNext();) {
				IFile file = (IFile) iter.next();
				IJavaElement element;
				if(CoreUtils.ASPECTJ_SOURCE_ONLY_FILTER.accept(file.getName())) {
					element = AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(file);
				} else {
					element = JavaCore.create(file);
				}
				removedJavaElements.add(element);	
			}	
			try {
				new ClasspathModifier().include(newJavaElements, JavaCore.create(currentlySelectedFile.getProject()), null);
				new ClasspathModifier().exclude(removedJavaElements, JavaCore.create(currentlySelectedFile.getProject()), null);
			} catch (JavaModelException e) {
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