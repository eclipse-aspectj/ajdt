/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.BuildManager;
import org.aspectj.asm.AsmManager;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.builder.IAJBuildListener;
import org.eclipse.ajdt.internal.core.AJDTUtils;
import org.eclipse.ajdt.internal.core.AJLog;
import org.eclipse.ajdt.internal.core.CoreUtils;
import org.eclipse.ajdt.internal.ui.ajde.CompilerTaskListManager;
import org.eclipse.ajdt.internal.ui.ajde.ProjectProperties;
import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.visualiser.AJDTContentProvider;
import org.eclipse.contribution.visualiser.VisualiserPlugin;
import org.eclipse.contribution.visualiser.core.ProviderManager;
import org.eclipse.contribution.xref.internal.ui.XReferenceUIPlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Display;

/**
 * 
 */
public class UIBuildListener implements IAJBuildListener {

	/**
	 * Code returned when a failure occurs creating a build config file
	 */
	private static final int CONFIG_FILE_WRITE_ERROR = -10;

	/**
	 * Map of projects with the IClasspathEntry corresponding
	 * to their outjar
	 */
	private HashMap outjars = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ajdt.core.builder.AJBuildListener#preAJBuild(org.eclipse.core.resources.IProject)
	 */
	public void preAJBuild(int kind, IProject project, IProject[] requiredProjects) {
		if (!AspectJPreferences.isAJDTPrefConfigShowing()) {
    		Display.getDefault().asyncExec(new Runnable() {
    			public void run() {
    				AJDTUtils.verifyWorkbenchConfiguration();
    			}
    		});            
        }
		
		String kindS = null;
		if (kind == IncrementalProjectBuilder.AUTO_BUILD)
			kindS = "AUTOBUILD";  //$NON-NLS-1$
		if (kind == IncrementalProjectBuilder.INCREMENTAL_BUILD)
			kindS = "INCREMENTALBUILD";  //$NON-NLS-1$
		if (kind == IncrementalProjectBuilder.FULL_BUILD)
			kindS = "FULLBUILD";  //$NON-NLS-1$

		String mode = "";  //$NON-NLS-1$
		if (AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter()
				.getIncrementalMode() && kind!=IncrementalProjectBuilder.FULL_BUILD)
			mode = "Incremental AspectJ compilation";
		else
			mode = "Full AspectJ compilation";
		AJLog.log("build: Kind=" + kindS + " Project="
				+ project.getName() + " Mode=" + mode);

		// if using incremental compiilation, then attempt the incremental model repairs.
		boolean inc = AspectJPreferences.getIncrementalOption(project);
		AsmManager.attemptIncrementalModelRepairs = inc;		

		ProjectProperties props = AspectJUIPlugin.getDefault().getAjdtProjectProperties();
		List projectFiles = props.getProjectSourceFiles(project,
				CoreUtils.ASPECTJ_SOURCE_FILTER);
		updateBuildConfigIfNecessary(project, projectFiles);


		// checking to see if the current project has been marked as needing
		// a required project to be rebuilt.
		// IProject[] referencedProjects = getRequiredProjects(project);
		//IProject[] referencedProjects = getRequiredProjects(project, true);
		boolean haveClearedMarkers = false;
		for (int i = 0; i < requiredProjects.length; i++) {
			String referencedMessage = "The project cannot be built until its prerequisite "
					+ requiredProjects[i].getName()
					+ " is rebuilt. Cleaning and rebuilding all projects is recommended";
			if (projectAlreadyMarked(project, referencedMessage)) {
				if (kind == IncrementalProjectBuilder.FULL_BUILD) {
					props.clearMarkers(true);
					CompilerTaskListManager.clearOtherProjectMarkers(project);
				} else {
					props.clearMarkers(false);
				}
				markProject(project, referencedMessage);
				haveClearedMarkers = true;
			}
		}
		if (!(haveClearedMarkers)) {
			if (kind == IncrementalProjectBuilder.FULL_BUILD) {
				props.clearMarkers(true);
				CompilerTaskListManager.clearOtherProjectMarkers(project);
			} else {
				props.clearMarkers(false);
			}
		}

		
		BuildManager buildManager = Ajde.getDefault().getBuildManager();
		if (!AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter()
				.getBuildAsm()) {
			AJLog.log("build: No structure model to be built for project: "
							+ project.getName());
			buildManager.setBuildModelMode(false);
		} else {
			buildManager.setBuildModelMode(true);
		}

		MarkerUpdating.deleteAllMarkers(project);
	}

	/**
	 * Only want to mark referencing projects once, therefore need to check
	 * whether they've been marked already. Also, if a project has been marked
	 * dont want to build it until its prerequisites have been rebuilt.
	 */
	private boolean projectAlreadyMarked(IProject project, String errorMessage) {
		try {
			IMarker[] problemMarkers = project.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
					false, IResource.DEPTH_INFINITE);
			if (problemMarkers.length > 0) {
				for (int j = 0; j < problemMarkers.length; j++) {
					IMarker marker = problemMarkers[j];
					int markerSeverity = marker.getAttribute(IMarker.SEVERITY,
							-1);
					String markerMessage = marker.getAttribute(IMarker.MESSAGE,
							"no message");
					if (markerSeverity == IMarker.SEVERITY_ERROR
							&& markerMessage.equals(errorMessage)) {
						return true;
					}
				}
			}
		} catch (CoreException e) {
			AJLog.log("build: Problem occured finding the markers for project "
							+ project.getName() + ": " + e.getStackTrace());
		}
		return false;
	}
	
	private void markProject(IProject project, String errorMessage) {
		try {
			IMarker errorMarker = project.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
			errorMarker.setAttribute(IMarker.MESSAGE, errorMessage); //$NON-NLS-1$
			errorMarker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		} catch (CoreException e) {
			AJLog.log("build: Problem occured creating the error marker for project "
							+ project.getName() + ": " + e.getStackTrace());
		}
	}

	/**
	 * If the project uses a default build file, then it will be regenerated to
	 * ensure that all project files are included.
	 */
	private void updateBuildConfigIfNecessary(IProject project,
			List projectFiles) {
		if (getBuildFilePath(project).endsWith(AspectJPlugin.DEFAULT_CONFIG_FILE)) {
			try {
				writeBuildConfigFile(projectFiles, project);
			} catch (CoreException e) {
			}
		}
	}

	/**
	 * Get the fully qualified path to the build configuation file specified for
	 * this project.
	 */
	private String getBuildFilePath(IProject project) {
		String buildfile = AspectJPlugin.getBuildConfigurationFile(project);
		return buildfile;
	}

	/**
	 * Create a full build configuration file for this project
	 */
	private void writeBuildConfigFile(List projectFiles, IProject project)
			throws CoreException {
		String configurationFilename = getBuildFilePath(project);

		try {
			FileWriter fw = new FileWriter(configurationFilename);
			BufferedWriter bw = new BufferedWriter(fw);
			for (Iterator it = projectFiles.iterator(); it.hasNext();) {
				File jf = (File) it.next();
				String fileName = jf.toString();
				if (fileName.endsWith(".java") || fileName.endsWith(".aj")) { //$NON-NLS-1$ //$NON-NLS-2$
					bw.write(fileName);
					bw.write(System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			bw.flush();
			fw.flush();
			bw.close();
			// now tell eclipse the file has been updated
			IResource res = project.findMember(AspectJPlugin.DEFAULT_CONFIG_FILE);
			if (res != null) {
				// Fix for 40556.
				// No progress reporting required on this
				// refresh operation so pass in null for second argument.
				res.refreshLocal(IResource.DEPTH_ZERO, null);
			}
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, AspectJUIPlugin.PLUGIN_ID,
					CONFIG_FILE_WRITE_ERROR, AspectJUIPlugin
							.getResourceString("configFileCreateError"), e); //$NON-NLS-1$
			throw new CoreException(status);
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ajdt.core.builder.AJBuildListener#postAJBuild(org.eclipse.core.resources.IProject)
	 */
	public void postAJBuild(IProject project, boolean buildCancelled, boolean noSourceChanges) {
		AspectJUIPlugin.getDefault().getAjdtProjectProperties().flushClasspathCache();
		
		if (noSourceChanges) {
			MarkerUpdating.addNewMarkers(project);
			return;
		}
		
		// The message to feature in the problems view of depending projects
		String buildPrereqsMessage = AspectJUIPlugin.getFormattedResourceString("buildPrereqsMessage",
				project.getName());
		if (buildCancelled) {
			markReferencingProjects(project, buildPrereqsMessage);
		} else {
			removeMarkerOnReferencingProjects(project, buildPrereqsMessage);
		}

		// Bug22258: Get the compiler monitor to display any issues with
		// that compile.
		CompilerTaskListManager.showOutstandingProblems();

		if (!AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter()
				.getBuildAsm()) {
			AspectJEditor.forceEditorUpdates(project);
		}

		
		// before returning, check to see if the project sent it's output
		// to an outjar and if so, then update any depending projects
		checkOutJarEntry(project);

		MarkerUpdating.addNewMarkers(project);
		
		if (AspectJPreferences.isAdviceDecoratorActive()) {
			AJModelUtils.refreshOutlineViews();
		}

		if (AspectJUIPlugin.getDefault().getDisplay().isDisposed()) {
			AJLog.log("Not updating vis or xref view, display is disposed!");
		} else {
			AspectJUIPlugin.getDefault().getDisplay().syncExec(
					new Runnable() {
						public void run() {
							if (ProviderManager.getContentProvider() instanceof AJDTContentProvider) {
								AJDTContentProvider provider = (AJDTContentProvider) ProviderManager
										.getContentProvider();
								provider.reset();
								VisualiserPlugin.refresh();
							}
						}
					});
			AspectJUIPlugin.getDefault().getDisplay().syncExec(
					new Runnable() {
						public void run() {
							XReferenceUIPlugin.refresh();
						}
					});
		}
	}

	/**
	 * If the build has been aborted then mark any referencing projects with a
	 * marker saying so
	 */
	private void markReferencingProjects(IProject project, String errorMessage) {
		IProject[] referencingProjects = getDependingProjects(project);
		for (int i = 0; i < referencingProjects.length; i++) {
			IProject referencingProject = referencingProjects[i];
			if (!(projectAlreadyMarked(referencingProject, errorMessage))) {
				markProject(referencingProject, errorMessage);
			}
		}
	}

	/**
	 * The build wasn't cancelled therefore need to remove any markers on
	 * referencing projects indicating that the current project needs to be
	 * built
	 */
	private void removeMarkerOnReferencingProjects(IProject project,
			String errorMessage) {
		try {
			IProject[] referencingProjects = getDependingProjects(project);
			for (int i = 0; i < referencingProjects.length; i++) {
				IProject referencingProject = referencingProjects[i];
				IMarker[] problemMarkers = referencingProject.findMarkers(
						IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
						false, IResource.DEPTH_INFINITE);
				if (problemMarkers.length > 0) {
					for (int j = 0; j < problemMarkers.length; j++) {
						IMarker marker = problemMarkers[j];
						int markerSeverity = marker.getAttribute(
								IMarker.SEVERITY, -1);
						String markerMessage = marker.getAttribute(
								IMarker.MESSAGE, "no message");

						if (markerSeverity == IMarker.SEVERITY_ERROR
								&& markerMessage.equals(errorMessage)) {
							marker.delete();
						}
					}
				}
			}
		} catch (CoreException e) {
			AJLog.log("build: Problem occured either finding the markers for project "
							+ project.getName()
							+ ", or deleting the error marker: "
							+ e.getStackTrace());
		}
	}

	/**
	 * If a project has specified an outjar then update the classpath of
	 * depending projects to include this outjar (unless the classpath already
	 * contains it). If the project hasn't specified an outjar then check
	 * whether it did last time it was built. In this case, remove the oujar
	 * from the classpath of depending projects. 
	 */
	private void checkOutJarEntry(IProject project) {
		String outJar = AspectJUIPlugin.getDefault().getAjdtProjectProperties().getOutJar();
		if (outJar != null && !(outJar.equals(""))) {  //$NON-NLS-1$
			if (outjars == null) {
				outjars = new HashMap();
			}
			IPath newPath = getRelativePath(project, outJar);
			IClasspathEntry newEntry = JavaCore.newLibraryEntry(newPath
					.makeAbsolute(), null, null);
			if (outjars.containsKey(project))  {
				if (!(outjars.get(project).equals(newEntry))) {
					IClasspathEntry oldEntry = (IClasspathEntry)outjars.get(project);
					outjars.remove(project);
					removeOutjarFromDependingProjects(project,oldEntry);
					outjars.put(project,newEntry);
					updateDependingProjectsWithJar(project,newEntry);
				}				
			} else {
				outjars.put(project,newEntry);
				updateDependingProjectsWithJar(project,newEntry);					
			}
		} else {
			if (outjars != null && outjars.containsKey(project)) {
				IClasspathEntry oldEntry = (IClasspathEntry)outjars.get(project);
				outjars.remove(project);
				if (outjars.size() == 0) {
					outjars = null;
				}
				removeOutjarFromDependingProjects(project, oldEntry);
			}
		}
	}

	private void removeOutjarFromDependingProjects(IProject project,
			IClasspathEntry unwantedEntry) {
		IProject[] dependingProjects = getDependingProjects(project);

		for (int i = 0; i < dependingProjects.length; i++) {
			IJavaProject javaProject = JavaCore.create(dependingProjects[i]);
			if (javaProject == null)
				continue;
			try {
				IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
				List newEntries = new ArrayList();
				for (int j = 0; j < cpEntry.length; j++) {
					if(!cpEntry[j].equals(unwantedEntry)) {
						newEntries.add(cpEntry[j]);
					}
				}
				IClasspathEntry[] newCP = (IClasspathEntry[]) newEntries
						.toArray(new IClasspathEntry[newEntries.size()]);
				javaProject.setRawClasspath(newCP, new NullProgressMonitor());
			} catch (CoreException e) {
			}
		}
	}

	private void updateDependingProjectsWithJar(IProject project, IClasspathEntry newEntry) {
		IProject[] dependingProjects = getDependingProjects(project);
		
		goThroughProjects: for (int i = 0; i < dependingProjects.length; i++) {
			IJavaProject javaProject = JavaCore.create(dependingProjects[i]);
			if (javaProject == null)
				continue;
			try {
				IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
				List newEntries = new ArrayList();
				for (int j = 0; j < cpEntry.length; j++) {
					if(cpEntry[j].equals(newEntry)) {
						continue goThroughProjects;
					} else {
						newEntries.add(cpEntry[j]);
					}
				}
			    newEntries.add(newEntry);
				IClasspathEntry[] newCP = (IClasspathEntry[]) newEntries
						.toArray(new IClasspathEntry[newEntries.size()]);
				javaProject.setRawClasspath(newCP, new NullProgressMonitor());
			} catch (CoreException e) {
			}
		}
	}

	/**
	 * Get all the projects that depend on this project. This includes both
	 * project and class folder dependencies.
	 */
	private IProject[] getDependingProjects(IProject project) {
		IProject[] referencingProjects = project.getReferencingProjects();
		// this only gets the class folder depending projects
		IProject[] classFolderReferences = (IProject[]) CoreUtils
				.getDependingProjects(project).get(0);
		IProject[] dependingProjects = new IProject[referencingProjects.length
				+ classFolderReferences.length];
		for (int i = 0; i < referencingProjects.length; i++) {
			dependingProjects[i] = referencingProjects[i];
		}
		for (int i = 0; i < classFolderReferences.length; i++) {
			dependingProjects[i + referencingProjects.length] = classFolderReferences[i];
		}
		return dependingProjects;
	}

	private IPath getRelativePath(IProject project, String outJar) {
		StringBuffer sb = new StringBuffer(outJar);
		int index = sb.lastIndexOf(project.getName());
		IPath path;
		if (index > 0) {
			path = new Path(sb.substring(sb.lastIndexOf(project.getName())));
		} else {
			path = new Path(outJar);
		}
		return path.makeAbsolute();
	}

}
