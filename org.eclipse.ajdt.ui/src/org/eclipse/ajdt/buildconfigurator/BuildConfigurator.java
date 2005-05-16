/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.buildconfigurator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.core.AJDTUtils;
import org.eclipse.ajdt.internal.core.CoreUtils;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.packageview.ClassPathContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;
/**
 * @author Luzius Meisser
 * 
 * This class manages the ProjectBuildConfiguratiors.
 * It offers functionality to
 * - get the ProjectBuildConfigurator for a specific Project
 * - register IBuildConfigurationChangedListeners
 * - notify these change listeners
 * 
 * It creates exaclty one ProjectBuildConfigurator per Project
 * that fulfills the criteria specified in 
 * public boolen canManage(IProject p);
 * 
 */
public class BuildConfigurator implements ISelectionListener {
	private HashMap projectConfigurators;
	private Vector changeListeners;
	private IProject currentProj;
	private int notificationType;
	private static BuildConfigurator buildConfigurator;
	
	private HashSet fileList;
	private boolean triedToOpenXRefView;
	
	//make it singleton
	private BuildConfigurator() {
		projectConfigurators = new HashMap();
		changeListeners = new Vector();
		notificationType = -1;
	}
	
	//guarantuees to return a non-null value
	public static synchronized BuildConfigurator getBuildConfigurator(){
		if (buildConfigurator == null)
			buildConfigurator = new BuildConfigurator();
		return buildConfigurator;
	}

	public synchronized void selectionChanged(IWorkbenchPart action, ISelection selection) {
		IResource res;
		IProject selectedProj;
		// Run migration wizard if we haven't before or if it's not running at the
		// moment, if we have previously run the old preference wizard on this 
		// workspace and if this is not a brand new workspace
		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
		if (AJDTUtils.FORCE_MIGRATION
		        || (!AspectJPreferences.migrationWizardHasRun() 
				&& !store.getBoolean(AspectJPreferences.NEVER_RUN_MIGRATION_WIZARD)
		        && !AspectJUIPlugin.getDefault().workspaceIsEmpty(AspectJPlugin.getWorkspace().getRoot()))) {			
		    AJDTUtils.migrateWorkbench();
		    AJDTUtils.FORCE_MIGRATION = false;
		} else if (!triedToOpenXRefView 
		        && !AspectJPreferences.migrationWizardIsRunning()) { // only try this once
			String workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
			if (!AspectJPreferences.migrationWizardIsRunning()
			        && !store.getBoolean(AspectJPreferences.DONE_AUTO_OPEN_XREF_VIEW + workspaceLocation)
			        && !store.getBoolean(workspaceLocation)) {
		        // open xref view in perspective if the migration wizard either isn't running 
			    // or hasn't run and we haven't opened the xref view before.
				Job job = new UIJob(AspectJUIPlugin.getResourceString("AJDTPrefConfigWizardPage.workbench.openXRefView")) { //$NON-NLS-1$
					public IStatus runInUIThread(IProgressMonitor monitor) {
						try {
			                AspectJUIPlugin.getDefault().getActiveWorkbenchWindow()
								.getActivePage().showView(XReferenceView.ID);
			                return Status.OK_STATUS;
				        } catch (PartInitException e) {
				            AspectJUIPlugin.getDefault().getErrorHandler().handleError(
								AspectJUIPlugin.getResourceString("AJDTPrefConfigWizardPage.ErrorOpeningXRefView"), e); //$NON-NLS-1$
				            return Status.OK_STATUS;
				        }
					}
				};
			    job.schedule();
		        store.setValue(AspectJPreferences.DONE_AUTO_OPEN_XREF_VIEW + workspaceLocation, true);
		    }
			triedToOpenXRefView = true;
			
		}
		if (action instanceof IEditorPart) {
			res = (IResource) ((IEditorPart) action).getEditorInput()
					.getAdapter(IResource.class);
			if (res != null) {
				selectedProj = res.getProject();
			} else {
				selectedProj = null;
			}
		} else {
			selectedProj = getProjectFromSelection(selection);
		}
		if ((selectedProj != currentProj) && (selectedProj != null)) {
			fileList = null;
			currentProj = selectedProj;
			if (canManage(currentProj)){
				
				PropertyPageManager.unregisterJDTPropertyPage();
			} else {
				if (currentProj.isOpen())
					PropertyPageManager.registerJDTPropertyPage();
			}
			notifyChangeListeners();
		}
	}
	public void notifyChangeListeners() {
		// this notificationType check is done to break infinite notifyChangeListeners loops
		// TODO: implement a more elegant notification model
		if (notificationType == -1) {
			try {
				notificationType = 0;
				ProjectBuildConfigurator pbc = getActiveProjectBuildConfigurator();
				Iterator iter = changeListeners.iterator();
				while (iter.hasNext()) {
					((IBuildConfigurationChangedListener) iter.next())
							.buildConfigurationChanged(pbc);
				}
			} finally {
//				whatever happens here, we must reset notificationType to -1
				notificationType = -1;
			}		
		}
	}

	private IProject getProjectFromSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			//get the Project & set text
			Object first = ((IStructuredSelection) selection).getFirstElement();
			if (first instanceof IJavaElement)
				return ((IJavaElement) first).getJavaProject().getProject();
			if (first instanceof IResource){
				IResource myRes = (IResource) first;
				return myRes.getProject();
			}
			if (first instanceof ClassPathContainer)
				return ((ClassPathContainer) first).getJavaProject().getProject();
		}
		return null;
	}
	
	/**
	 * @return Returns the activeBuildConfigurator or null if none are active
	 */
	public ProjectBuildConfigurator getActiveProjectBuildConfigurator() {
		return getProjectBuildConfigurator(currentProj);
	}
	
	public Set getProjectBuildConfigurators() {
		return projectConfigurators.entrySet();
	}
	
/**
 * @return The ProjectBuildConfigurator for the given Project if it is an open aj project or null otherwise
 */
	public ProjectBuildConfigurator getProjectBuildConfigurator(IJavaProject proj) {
		if ((proj != null) && (this.canManage(proj.getProject()))) {
			ProjectBuildConfigurator pbc = (ProjectBuildConfigurator) projectConfigurators
					.get(proj.getProject().getName());
			if (pbc != null)
				return pbc;
			return createPBC(proj);
		}
		return null;
	}
	
	private synchronized ProjectBuildConfigurator createPBC(IJavaProject proj){
		//check if already there
		ProjectBuildConfigurator pbc = (ProjectBuildConfigurator) projectConfigurators
		.get(proj.getProject().getName());
		if (pbc != null)
			return pbc;
		//if not, create:
		pbc = new ProjectBuildConfigurator(proj);
		//pbc.init();
		projectConfigurators.put(proj.getProject().getName(), pbc);
		return pbc;
	}
	
	/**
	 * @return The ProjectBuildConfigurator for the given Project if it is an open aj project or null otherwise
	 */
	public ProjectBuildConfigurator getProjectBuildConfigurator(IProject proj) {
		return getProjectBuildConfigurator(JavaCore.create(proj));
	}
	
	public boolean canManage(IProject proj) {
		if ((proj == null) || (!proj.isOpen())) {
			return false;
		}
		if (AspectJPlugin.isAJProject(proj)) {
			return true;
		}
		return false;
	}
	
	public void addProjectBuildConfigurator(ProjectBuildConfigurator pbc) {
		projectConfigurators.put(pbc.getJavaProject().getProject().getName(),
				pbc);
	}
	/**
	 * @param menu
	 */
	public void addBuildConfigurationChangedListener(
			IBuildConfigurationChangedListener bccl) {
		changeListeners.add(bccl);
	}
	/**
	 * @param jp
	 */
	public void closeProject(IProject proj) {
		fileList = null;
		ProjectBuildConfigurator pbc = (ProjectBuildConfigurator)projectConfigurators.get(proj.getName());
		if (pbc != null) {
			if (proj == currentProj)
				currentProj = null;
			projectConfigurators.remove(proj.getName());
			notifyChangeListeners();
		}
	}
	/**
	 * @param project
	 */
	public void checkNature(IProject project) {
		if (projectConfigurators.containsKey(project.getName())){
			//if aj nature was removed, remove pbc
			if (!this.canManage(project)){
				ProjectBuildConfigurator pbc = (ProjectBuildConfigurator)projectConfigurators.get(project.getName());
				if (pbc != null){
					closeProject(project);
				}
			}
		} else {
			//aj nature has been added
			if (this.canManage(project)){
				currentProj = project;
				this.notifyChangeListeners();
			}
		}
		
	}

	/**
	 * @param project
	 */
	public void restoreJDTState(IProject project) {
		PropertyPageManager.registerJDTPropertyPage();
		this.checkNature(project);
	}
	/**
	 * @param project
	 */
	public void setup(IProject project) {
		fileList = null;
		PropertyPageManager.unregisterJDTPropertyPage();
		try {
			IJavaProject jp = JavaCore.create(project);
			IClasspathEntry[] cpes = jp.getRawClasspath();
			
			if (haveExclusionPatterns(cpes)){
				fileList = getFileSetFromCPE(cpes, jp);
				for (int i=0; i<cpes.length; i++){
					if (cpes[i].getEntryKind() == IClasspathEntry.CPE_SOURCE){
						cpes[i] = JavaCore.newSourceEntry(cpes[i].getPath());
					}
				}		
				jp.setRawClasspath(cpes, null);
			}
		} catch (JavaModelException e) {
			fileList = null;
		}
		
		if ((currentProj == null) || (currentProj == project)){
			currentProj = project;
			this.notifyChangeListeners();
		}
	}
	
	HashSet getFileSetFromCPE(IClasspathEntry[] cpes, IJavaProject jp){
		HashSet fileSet = new HashSet(30);
		for (int i=0; i<cpes.length; i++){
			if (cpes[i].getEntryKind() == IClasspathEntry.CPE_SOURCE){
				IResource res = jp.getProject().getParent().findMember(cpes[i].getPath());
				if (res!=null && (res.getType()==IResource.FOLDER || res.getType()==IResource.PROJECT))
					addAllIncludedMembers(fileSet, (IContainer)res, jp);
			}
		}
		return fileSet;
	}
	
	boolean haveExclusionPatterns(IClasspathEntry[] cpes){
		for (int i=0; i<cpes.length; i++){
			if (cpes[i].getEntryKind() == IClasspathEntry.CPE_SOURCE){
				if ((cpes[i].getExclusionPatterns().length > 0)
					|| (cpes[i].getInclusionPatterns().length > 0))
					return true;
			}
		}
		return false;
	}

	
	HashSet getInitialFileList(){
		return fileList;
	}
	
	
	private void addAllIncludedMembers(HashSet l, IContainer con, IJavaProject jp){
		try {
			IResource[] reses = con.members();
			for(int i=0; i<reses.length; i++){
				if (reses[i] instanceof IContainer){
					addAllIncludedMembers(l, (IContainer)reses[i], jp);
				} else {
					if (jp.isOnClasspath(reses[i]) && CoreUtils.ASPECTJ_SOURCE_FILTER
							.accept(reses[i].getName())){
						l.add(reses[i]);
					}
				}
			}
		} catch (CoreException e) {
		}
	}

	/**
	 * Get a new filename for the given project.  Returns the name without the file extension.
	 * @param project
	 * @return a string that is NOT the name of a current build configuration in the project
	 */
	public static String getFreeFileName(IProject project) {
		String defaultFileName = AspectJUIPlugin.getResourceString("BCDialog.SaveBuildConfigurationAs.default"); //$NON-NLS-1$
	
		int counter = 0;
		if(project != null) {
			boolean foundFreeName = false;
			while (!foundFreeName) {
				String name = counter==0 ? defaultFileName : defaultFileName+counter;
				IPath path = project.getFullPath().append(name + "." + BuildConfiguration.EXTENSION); //$NON-NLS-1$
				if(!AspectJPlugin.getWorkspace().getRoot().getFile(path).exists()) {
					foundFreeName = true;
				} else {
					counter++;
				}
			}
		}
		return counter==0 ? defaultFileName : defaultFileName+counter;
	}	
	
}