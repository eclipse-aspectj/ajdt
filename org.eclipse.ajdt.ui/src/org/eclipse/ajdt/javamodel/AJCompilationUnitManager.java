/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/

package org.eclipse.ajdt.javamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.buildconfigurator.BuildConfigurator;
import org.eclipse.ajdt.buildconfigurator.ProjectBuildConfigurator;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.internal.ui.ajde.ProjectProperties;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.OpenableElementInfo;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Maintains a cache containing ICompilationUnits for .aj files and is
 * responsible for their instantiation.
 * 
 * @author Luzius Meisser
 */
public class AJCompilationUnitManager {

	public final static AJCompilationUnitManager INSTANCE = new AJCompilationUnitManager();

	private HashMap compilationUnitStore = new HashMap();

	public AJCompilationUnit getAJCompilationUnit(IFile file) {
		AJCompilationUnit unit = getAJCompilationUnitFromCache(file);
		if (unit != null)
			return unit;
		if (creatingCUisAllowedFor(file))
			unit = createCU(file);
		return unit;
	}

	public AJCompilationUnit getAJCompilationUnitFromCache(IFile file) {
		return (AJCompilationUnit) compilationUnitStore.get(file);
	}
	
	//returns true if it was already there, and false if it needed to be inserted
	public boolean ensureUnitIsInModel(AJCompilationUnit unit) throws JavaModelException{
		//ensure unit is in the model
		OpenableElementInfo info = (OpenableElementInfo) ((JavaElement) unit.getParent()).getElementInfo();
		IJavaElement[] elems = info.getChildren();
		for (int i = 0; i < elems.length; i++) {
			IJavaElement element = elems[i];
			if (element == unit)
				return true;
		}
		info.addChild(unit);
		return false;
	}

	void removeFileFromModel(IFile file) {
		AJCompilationUnit unit = (AJCompilationUnit) compilationUnitStore
				.get(file);
		if (unit != null) {

			IWorkbenchPage page = JavaPlugin.getActivePage();
			if (page != null) {
				IEditorPart part = page.findEditor(new FileEditorInput(file));
				if (part != null)
					if (!page.closeEditor(part, true))
						//in case user cancels closeEditor, we should not
						// remove unit from model
						//TODO: maybe throw exception (?)
						return;
			}

			try {
				OpenableElementInfo info = (OpenableElementInfo) ((JavaElement) unit
						.getParent()).getElementInfo();
				info.removeChild(unit);
				JavaModelManager.getJavaModelManager().removeInfoAndChildren(
						unit);

			} catch (JavaModelException e) {
				AspectJUIPlugin.logException(e);
			}

			compilationUnitStore.remove(file);
		}

	}

	private AJCompilationUnit createCU(IFile file) {
		AJCompilationUnit unit = new AJCompilationUnit(file);

		try {
			OpenableElementInfo info = (OpenableElementInfo) ((JavaElement) unit
					.getParent()).getElementInfo();
			info.addChild(unit);

			//enable java search (experimental) - leads to exceptions when
			// using
			//AJIndexManager.addSource(unit);

			compilationUnitStore.put(file, unit);
		} catch (JavaModelException e) {
			AspectJUIPlugin.logException(e);
		}
		return unit;
	}

	private boolean creatingCUisAllowedFor(IFile file) {
		return (ProjectProperties.ASPECTJ_SOURCE_ONLY_FILTER.accept(file
				.getName())
				&& (BuildConfigurator.getBuildConfigurator()
						.getProjectBuildConfigurator(file.getProject()) != null) && (JavaCore
				.create(file.getProject()).isOnClasspath(file)));

	}

	public void initCompilationUnits(IProject project) {

		List l = new ArrayList(30);
		addProjectToList(project, l);
		Iterator iter = l.iterator();
		while (iter.hasNext()) {
			IFile ajfile = (IFile) iter.next();
			createCU(ajfile);
		}
	}

	public void removeCUsfromJavaModel(IProject project) {

		List l = new ArrayList(30);
		addProjectToList(project, l);
		Iterator iter = l.iterator();
		while (iter.hasNext()) {
			removeFileFromModel((IFile) iter.next());
		}
	}

	public void initCompilationUnits(IWorkspace workspace) {
		FileFilter.checkIfFileFilterEnabled();

		ArrayList l = new ArrayList(20);
		IProject[] projects = workspace.getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			addProjectToList(project, l);
			Iterator iter = l.iterator();
			while (iter.hasNext()) {
				IFile f = (IFile) iter.next();
				createCU(f);
			}
			l.clear();
		}
	}

	private void addProjectToList(IProject project, List l) {
		//check if aj project
		ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator()
				.getProjectBuildConfigurator(project);
		if (pbc != null) {
			try {
				IJavaProject jp = JavaCore.create(project);
				IClasspathEntry[] cpes = jp.getRawClasspath();
				for (int i = 0; i < cpes.length; i++) {
					IClasspathEntry entry = cpes[i];
					if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
						IPath p = entry.getPath();
						if (p.segmentCount() == 1)
							addAllAJFilesInFolder(project, l);
						else
							addAllAJFilesInFolder(project.getFolder(p
									.removeFirstSegments(1)), l);
					}
				}
			} catch (JavaModelException e) {
				AspectJUIPlugin.logException(e);
			}
		}
	}

	/**
	 * @param folder
	 * @param list
	 */
	private void addAllAJFilesInFolder(IContainer folder, List l) {
		if ((folder == null) || !folder.exists())
			return;
		try {
			IResource[] children = folder.members();
			for (int i = 0; i < children.length; i++) {
				IResource resource = children[i];
				if (resource.getType() == IResource.FOLDER)
					addAllAJFilesInFolder((IFolder) resource, l);
				else if ((resource.getType() == IResource.FILE)
						&& ProjectProperties.ASPECTJ_SOURCE_ONLY_FILTER
								.accept(resource.getName()))
					l.add(resource);
			}
		} catch (CoreException e) {
			AspectJUIPlugin.logException(e);
		}

	}

}