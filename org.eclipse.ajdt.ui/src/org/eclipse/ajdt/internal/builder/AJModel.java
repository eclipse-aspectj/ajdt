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
package org.eclipse.ajdt.internal.builder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.ajdt.internal.core.AJDTUtils;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.visualiser.StructureModelUtil;
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
	
	// needs which project is being built, if any
	private IProject beingBuilt = null;
	
	//new
	private Map projectModelMap = new HashMap();
	
	private AJModel() {

	}

	public static AJModel getInstance() {
		if (instance == null) {
			instance = new AJModel();
		}
		return instance;
	}

	/* These commented out routines are the basis for a complete conversion layer
	 * on top of the underlying structure model. It would be good if we could perform
	 * this conversion after a build, and then discard the underlying structure model,
	 * using only this Eclipse-centric one throughout the rest of AJDT. We would then
	 * have to handle the life-cycle and persistence ourselves - serialising this
	 * structure instead of the .ajsym files used by the underlying structure model.
	 * To progress this we really need to be able to get character offset information
	 * from IProgramElement instead of line numbers, as otherwise we have to do a
	 * somewhat time consuming conversion. We also have to make sure our new data
	 * structure contains all the information we need throughout AJDT.

	 
	
	private void init(IJavaElement je) {
		IProject project = je.getJavaProject().getProject();
		if (!projectSet.contains(project)) {
			System.out.println("map requested for project: " + project
					+ " project not known");
			String lst = AspectJUIPlugin.getBuildConfigurationFile(project);
			System.out.println("lst file=" + lst);
			long start = System.currentTimeMillis();
			AsmManager.getDefault().readStructureModel(lst);
			long elapsed = System.currentTimeMillis() - start;
			System.out.println("read structure model in " + elapsed + "ms");
			createMap(project);
		} else {
			System.out.println("map requested for project: " + project
					+ " project known");
		}
	}
*/

	private AJProjectModel getModelForProject(IProject project) {
		AJProjectModel pm = (AJProjectModel)projectModelMap.get(project);
		if (pm==null) {
			AJDTEventTrace.generalEvent("No current AJ model for project "+project.getName());
			StructureModelUtil.initialiseAJDE(project);
			createMap(project);
			pm = (AJProjectModel)projectModelMap.get(project);
		}
		return pm;
	}
	
	/**
	 * Query the AJ model for elements that have a certain relationship to the given element
	 * @param rel the relationship of interest
	 * @param je the IJavaElement to query as the source of the relationship
	 * @return a possibly null list of related elements
	 */
	public List getRelatedElements(AJRelationship rel, IJavaElement je) {
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
	
	public IJavaElement[] getExtraChildren(IJavaElement je) {
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
		//System.out.println("creating map for project: " + project);
		final long start = System.currentTimeMillis();
		final AJProjectModel projectModel = new AJProjectModel(project);
		projectModelMap.put(project,projectModel);
		//clearAJModel(project);
		try {
			AspectJUIPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) {
					projectModel.createProjectMap();
					long elapsed = System.currentTimeMillis() - start;
					AJDTEventTrace.generalEvent("Created AJ model for project "+project.getName()+" in "+elapsed+"ms");
				}
			}, null);
		} catch (CoreException coreEx) {
		}
	}
	
	public void clearMap(final IProject project) {
		projectModelMap.remove(project);
	}
	
//	private void initForProject(IProject project) {
//		StructureModelUtil.initialiseAJDE(project);
//		if (!projectSet.contains(project)) {
//			System.out.println("map requested for project: " + project
//					+ " project not known");
//			String lst = AspectJUIPlugin.getBuildConfigurationFile(project);
//			long start = System.currentTimeMillis();
//			AsmManager.getDefault().readStructureModel(lst);
//			long elapsed = System.currentTimeMillis() - start;
//			System.out.println("read structure model in " + elapsed + "ms");
//			//projectSet.add(project);
//		}
//	}
	
	/*
	private void initForFile(IFile file) {
		IProject project = file.getProject();
		initForProject(project);
		Set fileSet = (Set)perProjectFileSet.get(project);
		if (fileSet==null) {
			fileSet = new HashSet();
			perProjectFileSet.put(project,fileSet);
		}
		if (!fileSet.contains(file)) {
			createMapForFile(file);
			fileSet.add(file);
		}
	}
*/
	public void aboutToBuild(IProject project) {
		beingBuilt = project;
	}
	
	/*
	public void clearAJModel(IProject project) {
		beingBuilt = null;
//		Set fileSet = (Set)perProjectFileSet.get(project);
//		if (fileSet!=null) {
//			fileSet.clear();
//		}
		Map ipeToije = (Map)perProjectProgramElementMap.get(project);
		if (ipeToije!=null) {
			ipeToije.clear();
		}
		Map advisesMap = (Map)perProjectAdvisesMap.get(project);
		if (advisesMap != null) {
			advisesMap.clear();
		}
		Map advisedByMap = (Map)perProjectAdvisedByMap.get(project);
		if (advisedByMap != null) {
			advisedByMap.clear();
		}
		System.out.println("cleared maps for project "+project);
	}
*/
	
	public String getJavaElementLinkName(IJavaElement je) {
		if (je==null) {
			return "";
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
	 * Maps the given IProgramElement to its corresponding IJavaElement
	 * @param ipe
	 * @return
	 */
	public IJavaElement getCorrespondingJavaElement(IProgramElement ipe) {
		IResource res = programElementToResource(ipe);
		if (res!=null && (res instanceof IFile)) {
			IFile file = (IFile)res;
			//initForFile(file);
			System.out.println("ipe="+ipe+" ("+ipe.hashCode()+")");
			System.out.println("project="+file.getProject());
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
			//System.out.println("f=" + fileString);
			IProject[] projects = AspectJUIPlugin.getWorkspace().getRoot()
					.getProjects();
			for (int i = 0; i < projects.length; i++) {
				try {
					if (projects[i].isOpen()
							&& projects[i].hasNature(AspectJUIPlugin.ID_NATURE)) {
						String root = AJDTUtils
								.getProjectRootDirectory(projects[i]);
						//System.out.println("project="+projects[i]);
						//System.out.println("root="+root);
						if (fileString.startsWith(root)) {
							String path = fileString.substring(root.length());
							//System.out.println("path="+path);
							IPath ipath = new Path(path);
							//System.out.println("ipath="+ipath);
							IResource res = projects[i].findMember(ipath);
							//System.out.println("res="+res);
							return res;
						}
					}
				} catch (CoreException ce) {
				}
			}
		} catch (IOException e) {
		}
		return null;
	}
	
	/*
	public boolean isAdvisedBy(IJavaElement je) {
		System.out.println("isAdvisedBy: "+je);
		if (beingBuilt!=null) {
			return false;
		}
		try {
			IResource ir = je.getUnderlyingResource();
			if ((ir != null) && (ir instanceof IFile)) {
				IFile file = (IFile) ir;
				initForFile(file);
				IProject project = file.getProject();
				Map ipeToije = (Map)perProjectProgramElementMap.get(project);
				// look for je in map to find corresponding ipe
				IProgramElement pe = null;
				for (Iterator iter = ipeToije.entrySet().iterator(); (pe==null) && iter.hasNext();) {
					Map.Entry e = (Map.Entry)iter.next();
					if (je==e.getValue()) {
						pe = (IProgramElement)e.getKey();
					}
				}
				//System.out.println("pe="+pe);
				if (pe!=null) {
					IRelationshipMap irm = AsmManager.getDefault()
						.getRelationshipMap();
					IRelationship advisedBy = irm.get(pe,
							IRelationship.Kind.ADVICE, "advised by", false, false);
					IRelationship advisedByR = irm.get(pe,
							IRelationship.Kind.ADVICE, "advised by", true, false);
					if ((advisedBy!=null) || (advisedByR!=null)) {
						return true;
					}
					return false;
				}
			}
		} catch (JavaModelException e) {
		}
		return false;
	}
*/

	/*
	private void fillLineToOffsetMap(Map map, ICompilationUnit unit) {
		String source;
		try {
			source = unit.getSource();
			int lines = 0;
			map.put(new Integer(1), new Integer(0));
			for (int i = 0; i < source.length(); i++) {
				if (source.charAt(i) == '\n') {
					lines++;
					//System.out.println("line="+(lines+1)+" offset="+i);
					map.put(new Integer(lines + 1), new Integer(i));
				}
			}
		} catch (JavaModelException e) {
		}
	}
*/
	

}