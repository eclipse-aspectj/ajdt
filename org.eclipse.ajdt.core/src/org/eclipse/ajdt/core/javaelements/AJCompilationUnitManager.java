/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/

package org.eclipse.ajdt.core.javaelements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.internal.core.AJWorkingCopyOwner;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.OpenableElementInfo;

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
	
	/**
	 * Returns the AJCompilationUnit corresponding to the given
	 * CompilationUnit, if there is one, otherwise return the unit itself
	 * @param cu
	 * @return
	 */
	public static ICompilationUnit mapToAJCompilationUnit(ICompilationUnit cu) {
		if (AspectJPlugin.USING_CU_PROVIDER) { // mapping not required
			return cu;
		}
		if (cu == null) {
			return cu;
		}
		IResource res = cu.getResource();
		if (res.getType() == IResource.FILE) {
			AJCompilationUnit ajcu = INSTANCE.getAJCompilationUnit((IFile) res);
			if (ajcu != null) {
				return ajcu;
			}
		}
		return cu;
	}
	
	/**
	 * Returns the WorkingCopyOwner used to create AJCompilationUnits
	 * @return
	 */
	public static WorkingCopyOwner defaultAJWorkingCopyOwner() {
		return AJWorkingCopyOwner.INSTANCE;
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
	
	public List getAJCompilationUnitsForPackage(IPackageFragment pFragment) throws CoreException, JavaModelException {
		final List ajcus = new ArrayList();
		final IResource folder = pFragment.getCorrespondingResource();
		if(folder != null) {
			folder.accept(new IResourceVisitor(){
	
				public boolean visit(IResource resource) {
					if(resource instanceof IFile) {
						if (CoreUtils.ASPECTJ_SOURCE_ONLY_FILTER.accept(resource.getName())) {
							ajcus.add(getAJCompilationUnit((IFile)resource));
						}
					}
					return resource.equals(folder);
				}
			});
		}
		return ajcus;
	}
	
	public List getAJCompilationUnits(IJavaProject jp) throws CoreException {
		final List ajcus = new ArrayList();
		jp.getProject().accept(new IResourceVisitor(){

			public boolean visit(IResource resource) {
				if(resource instanceof IFile && AspectJPlugin.AJ_FILE_EXT.equals(resource.getFileExtension())) {
					AJCompilationUnit ajcu = getAJCompilationUnit((IFile)resource);
					if(ajcu != null) {
						ajcus.add(ajcu);
					}
				}				
				return resource.getType() == IResource.FOLDER || resource.getType() == IResource.PROJECT;
			}});
		return ajcus;
	}
	
	public List getAJCompilationUnits(IPackageFragmentRoot root) throws CoreException {
		final List ajcus = new ArrayList();
		root.getResource().accept(new IResourceVisitor(){

			public boolean visit(IResource resource) {
				if(resource instanceof IFile && AspectJPlugin.AJ_FILE_EXT.equals(resource.getFileExtension())) {
					AJCompilationUnit ajcu = getAJCompilationUnit((IFile)resource);
					if(ajcu != null) {
						ajcus.add(ajcu);
					}
				}				
				return resource.getType() == IResource.FOLDER || resource.getType() == IResource.PROJECT;
			}});
		return ajcus;
	}

	public void removeFileFromModel(IFile file) {
		AJCompilationUnit unit = (AJCompilationUnit) compilationUnitStore
				.get(file);
		if (unit != null) {
			try {
				// Fix for bug 106813 - check if the project is open first
				if(file.getProject().isOpen()) {					
					OpenableElementInfo info = (OpenableElementInfo) ((JavaElement) unit
							.getParent()).getElementInfo();
					info.removeChild(unit);
				}
				JavaModelManager.getJavaModelManager().removeInfoAndChildren(
						unit);

			} catch (JavaModelException e) {
			}
			compilationUnitStore.remove(file);
		}
	}

	private AJCompilationUnit createCU(IFile file) {
		AJCompilationUnit unit = new AJCompilationUnit(file);

		try {
			OpenableElementInfo info = (OpenableElementInfo) ((JavaElement) unit
					.getParent()).getElementInfo();
			info.removeChild(unit); // Remove identical CompilationUnit if it exists
			info.addChild(unit);

			compilationUnitStore.put(file, unit);
		} catch (JavaModelException e) {
		}
		return unit;
	}

	private boolean creatingCUisAllowedFor(IFile file) {
		return file != null && (CoreUtils.ASPECTJ_SOURCE_ONLY_FILTER.accept(file.getName())
				&& AspectJPlugin.isAJProject(file.getProject()) && (JavaCore
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

	public List removeCUsfromJavaModel(IProject project) {
		List l = new ArrayList(30);
		addProjectToList(project, l);
		Iterator iter = l.iterator();
		while (iter.hasNext()) {
			removeFileFromModel((IFile) iter.next());
		}
		return l;
	}

	public void initCompilationUnits(IWorkspace workspace) {
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

	public List<AJCompilationUnit> getCachedCUs(IProject project) {
		List ajList = new ArrayList();
		for (Iterator iter = compilationUnitStore.keySet().iterator(); iter.hasNext();) {
			IFile f = (IFile) iter.next();
			if (f.getProject().equals(project)) {
				ajList.add(compilationUnitStore.get(f));
			}
		}
		return ajList;
	}
	
	private void addProjectToList(IProject project, List l) {
		if (AspectJPlugin.isAJProject(project)) {
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
						&& CoreUtils.ASPECTJ_SOURCE_ONLY_FILTER
								.accept(resource.getName()))
					l.add(resource);
			}
		} catch (CoreException e) {
		}
	}
	
	/**
	 * useful for testing
	 */
	public void clearCache() {
	   compilationUnitStore.clear(); 
	}
}