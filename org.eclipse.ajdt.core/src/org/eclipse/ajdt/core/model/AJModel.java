/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.ajde.Ajde;
import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

/**
 * 
 * @author mchapman
 */
public class AJModel {
	private static AJModel instance;
	
	private Map projectModelMap = new HashMap();
	
	private static String lastLoadedConfigFile;

	private AJModel() {

	}

	public static AJModel getInstance() {
		if (instance == null) {
			instance = new AJModel();
		}
		return instance;
	}

	public void saveAllModels() {
		for (Iterator iter = projectModelMap.keySet().iterator(); iter.hasNext();) {
			IProject project = (IProject) iter.next();
			AJProjectModel pm = (AJProjectModel)projectModelMap.get(project);
			pm.saveModel();
		}
	}
	
	public void saveModel(IProject project) {
		AJProjectModel pm = getModelForProject(project);
		if (pm!=null) {
			pm.saveModel();
		}
	}

	public AJProjectModel getModelForProject(IProject project) {
		if ((project==null) || !project.isAccessible() || !AspectJPlugin.isAJProject(project)) {
			return null;
		}
		AJProjectModel pm = (AJProjectModel) projectModelMap.get(project);
		if (pm != null) {
			return pm;
		}
		AJProjectModel projectModel = new AJProjectModel(project);
		projectModelMap.put(project, projectModel);
		projectModel.loadModel();
		return projectModel;
	}
		
	/**
	 * Query the AJ model for elements that have a certain relationship to the
	 * given element
	 * 
	 * @param rel
	 *            the relationship of interest
	 * @param je
	 *            the IJavaElement to query as the source of the relationship
	 * @return a possibly null list of related elements
	 */
	public List getRelatedElements(AJRelationshipType rel, IJavaElement je) {
		if (je==null) {
			return null;
		}
		IJavaProject jp= je.getJavaProject();
		if (jp==null) {
			return null;
		}
		AJProjectModel pm = getModelForProject(jp.getProject());
		if (pm==null) {
			return null;
		}
		return pm.getRelatedElements(rel, je);
	}
	
	/**
	 * Returns true if this element is advised, or if this element contains a sub-method
	 * element that is advised.
	 * @param je
	 * @return
	 */
	public boolean isAdvised(IJavaElement je) {
		if (je==null) {
			return false;
		}
		IJavaProject jp= je.getJavaProject();
		if (jp==null) {
			return false;
		}
		AJProjectModel pm = getModelForProject(jp.getProject());
		if (pm==null) {
			return false;
		}
		return pm.isAdvised(je);
	}
	
	public boolean hasRuntimeTest(IJavaElement je) {
		if (je==null) {
			return false;
		}
		IJavaProject jp= je.getJavaProject();
		if (jp==null) {
			return false;
		}
		AJProjectModel pm = getModelForProject(jp.getProject());
		if (pm==null) {
			return false;
		}
		return pm.hasRuntimeTest(je);
	}
	
	public List getExtraChildren(IJavaElement je) {
		if (je==null) {
			return null;
		}
		IJavaProject jp= je.getJavaProject();
		if (jp==null) {
			return null;
		}
		AJProjectModel pm = getModelForProject(jp.getProject());
		if (pm==null) {
			return null;
		}
		return pm.getExtraChildren(je);
	}
	
	public void createMap(final IProject project) {
		final AJProjectModel projectModel = new AJProjectModel(project);
		projectModelMap.put(project,projectModel);
		try {
			AspectJPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) {
					projectModel.createProjectMap();
				}
			}, null);
		} catch (CoreException coreEx) {
		}
	}
	
	/**
	 * Clears the map and delete the model file if required
	 * @param project
	 * @param delete
	 */
	public void clearMap(final IProject project, boolean delete) {
		AJProjectModel pm = (AJProjectModel) projectModelMap.get(project);
		if (pm != null && delete) {
			pm.deleteModelFile();
		}
		projectModelMap.remove(project);
		AJLog.log("Cleared AJDT relationship map for project "+project.getName());
	}
	
	/**
	 * Query all the relationships of interest in a project
	 * @param project
	 * @param rel array of relationships of interest
	 * @return List of relationships and endpoints
	 */
	public List getAllRelationships(IProject project, AJRelationshipType[] rels) {
		AJProjectModel pm = getModelForProject(project);
		if (pm==null) {
			return null;
		}
		return pm.getAllRelationships(rels);
	}
	
	/**
	 * Query the source line number of the given java element
	 * @param je
	 * @return line number, or -1 if unknown
	 */
	public int getJavaElementLineNumber(IJavaElement je) {
		if (je==null) {
			return -1;
		}
		IJavaProject jp = je.getJavaProject();
		if (jp==null) {
			return -1;
		}
		AJProjectModel pm = getModelForProject(jp.getProject());
		if (pm==null) {
			return -1;
		}
		return pm.getJavaElementLineNumber(je);
	}

	public String getJavaElementLinkName(IJavaElement je) {
		if (je==null) {
			return ""; //$NON-NLS-1$
		}
		IJavaProject jp = je.getJavaProject();
		if (jp==null) {
			return je.getElementName();
		}
		AJProjectModel pm = getModelForProject(jp.getProject());
		if (pm==null) {
			return je.getElementName();
		}
		return pm.getJavaElementLinkName(je);
	}
	
	/**
	 * Maps the given IProgramElement to its corresponding IJavaElement.
	 * Note this does not work after reloading a persisted model.
	 * @param ipe
	 * @return
	 */
	public IJavaElement getCorrespondingJavaElement(IProgramElement ipe) {
		IResource res = programElementToResource(ipe);
		if (res!=null && (res instanceof IFile)) {
			IFile file = (IFile)res;
			AJProjectModel pm = getModelForProject(file.getProject());
			if (pm==null) {
				return null;
			}
			return pm.getCorrespondingJavaElement(ipe);
		}
		return null;
	}
	
	private IResource programElementToResource(IProgramElement ipe) {
		try {
			String fileString = ipe.getSourceLocation().getSourceFile()
					.getCanonicalPath();
			IProject[] projects = AspectJPlugin.getWorkspace().getRoot()
					.getProjects();
			for (int i = 0; i < projects.length; i++) {
				if (projects[i].isOpen()
						&& AspectJPlugin.isAJProject(projects[i])) {
					String root = CoreUtils
							.getProjectRootDirectory(projects[i]);
					if (fileString.startsWith(root)) {
						String path = fileString.substring(root.length());
						IPath ipath = new Path(path);
						IResource res = projects[i].findMember(ipath);
						return res;
					}
				}
			}
		} catch (IOException e) {
		}
		return null;
	}
	
	
	// should be able to make this private, when all of AJDT uses the new model
	/**
	 * This method sets the current project and initialises AJDE
	 */
	public static void initialiseAJDE(IProject withProject) {
		String configFile = AspectJPlugin.getBuildConfigurationFile(withProject);
		if (!configFile.equals(lastLoadedConfigFile)) {
			Ajde.getDefault().getConfigurationManager().setActiveConfigFile(
				configFile);
			lastLoadedConfigFile = configFile;
		}
	}

}