/**********************************************************************
 Copyright (c) 2002 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 Contributors:
 Adrian Colyer, Andy Clement, Tracy Gardner - initial version
 ...
 AMC 12/08/2002	Now pass classpath to build event trace
 ASC 13/08/2002  Call compiler monitor to output outstanding problems
 **********************************************************************/
package org.eclipse.ajdt.internal.builder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.BuildManager;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationshipMap;
import org.eclipse.ajdt.buildconfigurator.BuildConfiguration;
import org.eclipse.ajdt.buildconfigurator.BuildConfigurator;
import org.eclipse.ajdt.buildconfigurator.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.ajdt.internal.core.AJDTUtils;
import org.eclipse.ajdt.internal.ui.ajde.CompilerMonitor;
import org.eclipse.ajdt.internal.ui.ajde.ProjectProperties;
import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.visualiser.AJDTContentProvider;
import org.eclipse.ajdt.ui.visualiser.StructureModelUtil;
import org.eclipse.contribution.visualiser.VisualiserPlugin;
import org.eclipse.contribution.visualiser.core.ProviderManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * This Builder selects the appropriate .lst (build config file) for the project
 * and then invokes the ajc compiler. No incremental compilation support as yet -
 * recommend turning the "perform build automatically on resource modification"
 * option off under workbench->preferences.
 */
public class Builder extends IncrementalProjectBuilder {

	/**
	 * The name of the default build config file for an AspectJ project
	 */
	public static final String DEFAULT_CONFIG_FILE = ".generated.lst";

	/**
	 * Code returned when a failure occurs creating a build config file
	 */
	private static final int CONFIG_FILE_WRITE_ERROR = -10;

	/**
	 * Default timeout value for an AspectJ compile
	 */
	private static final int DEFAULT_TIMEOUT = 120;

	/**
	 * Preference setting for compile timeout
	 */
	private static final String COMPILE_TIMEOUT = "org.aspectj.ajdt.ui.compile-timeout";

	/**
	 * The last project we did a build for, needed by content outline view to
	 * decide which updates to accept.
	 */
	private static IProject lastBuiltProject = null;

	/**
	 * Indicates whether the build is for one particular AspectJ project only
	 * (i.e. caused by the build action button being clicked) or else is part of
	 * a build of all projects in workspace (i.e. caused by a rebuild-all
	 * action).
	 */
	public static boolean isLocalBuild = false;

	/**
	 * Indicates whether the build has been cancelled by the user
	 */
	private boolean buildCancelled = false;

	/**
	 * Map of projects with the IClasspathEntry corresponding
	 * to their outjar
	 */
	private HashMap outjars = null;

	/**
	 * The build manager used for this build
	 */
	private BuildManager buildManager = null;

	/**
	 * The progress monitor used for this build
	 */
	private IProgressMonitor monitor;

	/**
	 * keeps track of the last workbench preference 
	 * (for workaround for bug 73435)
	 */
	private String lastWorkbenchPreference = JavaCore.ABORT;
	
	/**
	 * Constructor for AspectJBuilder
	 */
	public Builder() {
	}

	/**
	 * What did we last build?
	 */
	public static IProject getLastBuildTarget() {
		return lastBuiltProject;
	}

	/**
	 * @see IncrementalProjectBuilder#build(int, Map, IProgressMonitor) kind is
	 *      one of: FULL_BUILD, INCREMENTAL_BUILD or AUTO_BUILD If doing a full
	 *      build whilst the project is set for incremental (it may be forced by
	 *      a 'rebuild project' selection in the UI), then we use the
	 *      buildFresh() call to aspectj rather than build().
	 */
	protected IProject[] build(int kind, Map args,
			IProgressMonitor progressMonitor) throws CoreException {
		if (AspectJUIPlugin.DEBUG_COMPILER)
			System.err
					.println("Building at " + new java.util.Date().toString());

		if (!AspectJPreferences.isAJDTPrefConfigShowing()) {
    		Display.getDefault().asyncExec(new Runnable() {
    			public void run() {
    				AJDTUtils.verifyWorkbenchConfiguration();
    			}
    		});            
        }
		
		buildCancelled = false;

		IProject project = getProject();
		AspectJUIPlugin ajPlugin = AspectJUIPlugin.getDefault();
		ajPlugin.setCurrentProject(project);
		long buildstarttime = System.currentTimeMillis();	

		// if using incremental compiilation, then attempt the incremental model repairs.
		boolean inc = AspectJPreferences.getIncrementalOption(project);
		AsmManager.attemptIncrementalModelRepairs = inc;		
		
		IProject[] requiredProjects = getRequiredProjects(project,true);
				
		//check if full build needed
		ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator()
				.getProjectBuildConfigurator(project);
		if (pbc != null) {
			if (pbc.fullBuildRequested()) {
				kind = IncrementalProjectBuilder.FULL_BUILD;
				pbc.requestFullBuild(false);
			}
		}

		// workaround for bug 73435
		IProject[] dependingProjects = getDependingProjects(project);
		JavaProject javaProject = (JavaProject)JavaCore.create(project);
		if (!javaProject.hasBuildState() && dependingProjects.length > 0) {
			updateJavaCompilerPreferences(project,dependingProjects);
		}
		// end of workaround
		
		// The message to feature in the problems view of depending projects
		String buildPrereqsMessage = "The project cannot be built until its prerequisite "
				+ project.getName()
				+ " is built. Cleaning and rebuilding all projects is recommended";
		try {
			String kindS = null;
			if (kind == AUTO_BUILD)
				kindS = "AUTOBUILD";
			if (kind == INCREMENTAL_BUILD)
				kindS = "INCREMENTALBUILD";
			if (kind == FULL_BUILD)
				kindS = "FULLBUILD";

			String mode = "";
			if (AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter()
					.getIncrementalMode())
				mode = "Incremental AspectJ compilation";
			else
				mode = "Full AspectJ compilation";
			AJDTEventTrace.generalEvent("build: Kind=" + kindS + " Project="
					+ project.getName() + " Mode=" + mode);

			// Check the delta - we only want to proceed if something relevant
			// in this project has changed (a .java file, a .aj file or a 
			// .lst file)
			IResourceDelta dta = getDelta(getProject());

			if (kind != FULL_BUILD) {
				// need to add check here for whether the classpath has changed
				if (!sourceFilesChanged(dta, project)){
					AJDTEventTrace
							.generalEvent("build: Examined delta - no source file changes for project " 
									+ project.getName() );
					
					boolean continueToBuild = false;
					// if the source files of any projects which the current
					// project depends on have changed, then need
					// also to build the current project
					for (int i = 0; i < requiredProjects.length; i++) {
						IResourceDelta delta = getDelta(requiredProjects[i]);
						if (sourceFilesChanged(delta, project)) {
							AJDTEventTrace
								.generalEvent("build: Examined delta - source file changes in "
										+ "required project " + requiredProjects[i].getName() );
							continueToBuild = true;
							break;
						}
					}
					if (!continueToBuild) {
						return requiredProjects;						
					}
				}
			}

			monitor = progressMonitor;
			
			AJDTEventTrace.build(project, AspectJUIPlugin
					.getBuildConfigurationFile(project), ajPlugin
					.getAjdtProjectProperties().getClasspath());
			
			ProjectProperties props = ajPlugin.getAjdtProjectProperties();
			List projectFiles = props.getProjectSourceFiles(project,
					ProjectProperties.ASPECTJ_SOURCE_FILTER);
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
					if (kind == FULL_BUILD) {
						props.clearMarkers(true);
						CompilerMonitor.clearOtherProjectMarkers(project);
					} else {
						props.clearMarkers(false);
					}
					markProject(project, referencedMessage);
					haveClearedMarkers = true;
				}
			}
			if (!(haveClearedMarkers)) {
				if (kind == FULL_BUILD) {
					props.clearMarkers(true);
					CompilerMonitor.clearOtherProjectMarkers(project);
				} else {
					props.clearMarkers(false);
				}
			}

			// PD: current thinking is not to change project dependencies to
			// class folder ones
			// therefore, commenting out the following call.
			//AJDTUtils.changeProjectDependencies(project);

			CompilerMonitor compilerMonitor = ajPlugin.getCompilerMonitor();

			//TODO!!! For auto builds lets not pass the progress monitor
			// through - something 'funny' happens and although there 
			// is a monitor (it seems to be shown in the status bar on
			// eclipse), sometimes it goes null after testing whether it is null
			// but before being updated. This causes error dialogs to 
			// appear and entries in the .log about 'Widget is disposed' 
			// with progress monitor mentioned in the stack trace.
			if (kind == FULL_BUILD) {
				IJavaProject ijp = JavaCore.create(project);
				if (ijp != null)
					cleanOutputFolders(ijp);
				else
					AJDTEventTrace
							.generalEvent("Unable to empty output folder on build all - why cant we find the IJavaProject?");
				compilerMonitor.prepare(project, projectFiles, progressMonitor);
			} else {
				compilerMonitor.prepare(project, projectFiles, null);
			}

			lastBuiltProject = project;
			try {
				buildManager = Ajde.getDefault().getBuildManager();
				if (!AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter()
						.getBuildAsm()) {
					AJDTEventTrace
							.generalEvent("build: No structure model to be built for project: "
									+ AspectJUIPlugin.getDefault()
											.getCurrentProject().getName());
					buildManager.setBuildModelMode(false);
				} else {
					buildManager.setBuildModelMode(true);
				}

				if (kind == FULL_BUILD) {
					buildManager.buildFresh(getBuildFilePath(project));
				} else {
					buildManager.build(getBuildFilePath(project));
				}
			} catch (Exception e) {
			}

			waitForBuildCompletion(compilerMonitor);
			
			if (buildCancelled) {
				markReferencingProjects(project, buildPrereqsMessage);
			} else {
				removeMarkerOnReferencingProjects(project, buildPrereqsMessage);
			}

			// Bug22258: Get the compiler monitor to display any issues with
			// that compile.
			CompilerMonitor.showOutstandingProblems();

			if (!AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter()
					.getBuildAsm()) {
				AspectJEditor.forceEditorUpdates(project);
			}

			StructureModelUtil.wipeCache();

			// refresh the eclipse project to pickup generated artifacts
			project.refreshLocal(IResource.DEPTH_INFINITE, null);

			AJDTEventTrace.generalEvent("build: build time = "
					+ (System.currentTimeMillis() - buildstarttime) + "ms");

			// before returning, check to see if the project sent it's output
			// to an outjar and if so, then update any depending projects
			checkOutJarEntry(project);
			
			if (AspectJUIPlugin.getDefault().getDisplay().isDisposed())
				AJDTEventTrace.generalEvent("Not updating vis, display is disposed!");
			else
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
			
		} catch (Exception e) {
			Ajde.getDefault().getErrorHandler().handleError("Compile failed.",
					e);
		}	
		return requiredProjects;
	}
	
	/**
	 * This is the workaround discussed in bug 73435 for the case when projects are
	 * checked out from CVS, the AJ projects have no valid build state and projects
	 * depend on them.
	 */
	private void updateJavaCompilerPreferences(IProject project, IProject[] dependingProjects) {
		boolean setWorkbenchPref = false;
		for (int i = 0; i < dependingProjects.length; i++) {
			IProject dependingProject = dependingProjects[i];
			try {
				// Skip over any dependents that are themselves
				// AspectJ projects
				if (dependingProject.hasNature(AspectJUIPlugin.ID_NATURE)){
					continue;
				}
				
				// Only update dependent projects that have Java natures.
				// These could be ordinary Java projects or if we running inside
				// other Eclipse-based tools, they could be J2EE projects like dynamic
				// web projects.
				// Note that if the project does not have a Java nature then
				// the JavaCore.create() call appears to return a null. 
				if (dependingProject.hasNature(JavaCore.NATURE_ID)) {
					JavaProject jp = (JavaProject)JavaCore.create(dependingProject);
					String[] names = jp.getPreferences().propertyNames();
					if (names.length == 0 && !setWorkbenchPref) {
						Hashtable options = JavaCore.getOptions();
						String workbenchSetting = (String)options.get(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH);
						if (lastWorkbenchPreference.equals(JavaCore.ABORT) && workbenchSetting.equals(JavaCore.IGNORE)) {
							lastWorkbenchPreference = JavaCore.IGNORE;
						} else if (lastWorkbenchPreference.equals(JavaCore.ABORT) 
								&& workbenchSetting.equals(JavaCore.ABORT)){
							if (!setWorkbenchPref) {
								options.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH,JavaCore.IGNORE);
								JavaCore.setOptions(options);	
								setWorkbenchPref = true;	
								lastWorkbenchPreference = JavaCore.IGNORE;
							}
						} else if (lastWorkbenchPreference.equals(JavaCore.IGNORE) 
								&& workbenchSetting.equals(JavaCore.ABORT)){
							lastWorkbenchPreference = JavaCore.ABORT;
						}
					} else if (names.length > 0) {
						jp.setOption(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH,JavaCore.IGNORE);
						lastWorkbenchPreference = (String)JavaCore.getOptions().get(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH);
					}
				}// end if dependent has a Java nature
			} catch (CoreException e) {
			}
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
		if (outJar != null && !(outJar.equals(""))) {
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
	 * This is taken straight from the JavaBuilder - and is what is returned
	 * from the build method
	 */
	private IProject[] getRequiredProjects(IProject project,
			boolean includeBinaryPrerequisites) {

		JavaProject javaProject = (JavaProject) JavaCore.create(project);
		IWorkspaceRoot workspaceRoot = project.getWorkspace().getRoot();

		if (javaProject == null || workspaceRoot == null)
			return new IProject[0];

		ArrayList projects = new ArrayList();
		try {
			IClasspathEntry[] entries = javaProject.getExpandedClasspath(true);
			for (int i = 0, l = entries.length; i < l; i++) {
				IClasspathEntry entry = entries[i];
				IPath path = entry.getPath();
				IProject p = null;
				switch (entry.getEntryKind()) {
				case IClasspathEntry.CPE_PROJECT:
					p = workspaceRoot.getProject(path.lastSegment()); // missing
					// projects
					// are
					// considered
					// too
					break;
				case IClasspathEntry.CPE_LIBRARY:
					if (includeBinaryPrerequisites && path.segmentCount() > 1) {
						// some binary resources on the class path can come from
						// projects that are not included in the project
						// references
						IResource resource = workspaceRoot.findMember(path
								.segment(0));
						if (resource instanceof IProject)
							p = (IProject) resource;
					}
				}
				if (p != null && !projects.contains(p))
					projects.add(p);
			}
		} catch (JavaModelException e) {
			return new IProject[0];
		}
		IProject[] result = new IProject[projects.size()];
		projects.toArray(result);
		return result;
	}

	/**
	 * Get all the projects that depend on this project. This includes both
	 * project and class folder dependencies.
	 */
	private IProject[] getDependingProjects(IProject project) {
		IProject[] referencingProjects = project.getReferencingProjects();
		// this only gets the class folder depending projects
		IProject[] classFolderReferences = (IProject[]) AJDTUtils
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

	/**
	 * Get all the projects this project has a dependency on. This includes both
	 * project and class folder dependencies.
	 */
	private IProject[] getRequiredProjects(IProject project) {
		IProject[] referencedProjects;
		try {
			referencedProjects = project.getReferencedProjects();
		} catch (CoreException e) {
			referencedProjects = new IProject[0];
		}
		IProject[] classFolderRequirements = AJDTUtils
				.getRequiredClassFolderProjects(project);
		IProject[] requiredProjects = new IProject[referencedProjects.length
				+ classFolderRequirements.length];
		for (int i = 0; i < referencedProjects.length; i++) {
			requiredProjects[i] = referencedProjects[i];
		}
		for (int i = 0; i < classFolderRequirements.length; i++) {
			requiredProjects[i + referencedProjects.length] = classFolderRequirements[i];
		}
		return requiredProjects;
	}

	/**
	 * Check whether the user has pressed "cancel" and act accordingly
	 */
	private void checkAndHandleCancelation() {
		if (monitor != null && buildManager != null && monitor.isCanceled()) {
			buildManager.abortBuild();
			buildCancelled = true;
			AJDTEventTrace.generalEvent("build: Build cancelled as requested");
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

	private void markProject(IProject project, String errorMessage) {
		try {
			IMarker errorMarker = project.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
			errorMarker.setAttribute(IMarker.MESSAGE, errorMessage); //$NON-NLS-1$
			errorMarker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		} catch (CoreException e) {
			AJDTEventTrace
					.generalEvent("build: Problem occured creating the error marker for project "
							+ project.getName() + ": " + e.getStackTrace());
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
						IMarker.PROBLEM, false, IResource.DEPTH_INFINITE);
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
			AJDTEventTrace
					.generalEvent("build: Problem occured either finding the markers for project "
							+ project.getName()
							+ ", or deleting the error marker: "
							+ e.getStackTrace());
		}
	}

	/**
	 * Only want to mark referencing projects once, therefore need to check
	 * whether they've been marked already. Also, if a project has been marked
	 * dont want to build it until its prerequisites have been rebuilt.
	 */
	private boolean projectAlreadyMarked(IProject project, String errorMessage) {
		try {
			IMarker[] problemMarkers = project.findMarkers(IMarker.PROBLEM,
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
			AJDTEventTrace
					.generalEvent("build: Problem occured finding the markers for project "
							+ project.getName() + ": " + e.getStackTrace());
		}
		return false;
	}

	/**
	 * By default, just walks the model and produces some useful stats to
	 * AJDTEventTrace. If you have builder debugging activated, it will also
	 * dump the entire contents of the model.
	 */
	private void dumpStructureModel() {
		IHierarchy hierarchy = AsmManager.getDefault().getHierarchy();
		if (null == hierarchy) {
			AJDTEventTrace.generalEvent("build: no structure model !!");
			return;
		}
		totalnodes = 0;
		totalrels = 0;
		dumpModelHelper(0, hierarchy.getRoot(), AsmManager.getDefault()
				.getRelationshipMap());
		try {
			AJDTEventTrace
					.generalEvent("build: ProgramElements in ASM: "
							+ totalnodes
							+ (totalnodes == 1 ? "  (Probably just the .lst file)"
									: ""));
			AJDTEventTrace.generalEvent("build: Relationships in ASM: "
					+ totalrels);
		} catch (Exception e) {
		}
	}

	int totalnodes = 0;

	int totalrels = 0;

	private void dumpModelHelper(int depth, IProgramElement node,
			IRelationshipMap irm) {
		totalnodes++;
		if (node instanceof IProgramElement) {
			IProgramElement ipe = (IProgramElement) node;
			List rels = irm.get(ipe);
			if (AspectJUIPlugin.DEBUG_BUILDER) {
				StringBuffer line = new StringBuffer();
				for (int i = 0; i < depth; i++)
					line.append("-");
				line.append(">");
				line.append(node.toString());
				if (ipe.getSourceLocation() != null)
					line.append("    ").append(
							ipe.getSourceLocation().getSourceFile());
				AJDTEventTrace.generalEvent(line.toString());
			}
			if (rels != null)
				totalrels += rels.size();
			if (ipe.getChildren() != null) {
				Iterator iter = ipe.getChildren().iterator();
				while (iter.hasNext()) {
					IProgramElement h = (IProgramElement) iter.next();
					dumpModelHelper(depth + 2, h, irm);
				}
			}
		} else {
			AJDTEventTrace.generalEvent("What the hell is this?" + node);
		}
	}

	private boolean sourceFilesChanged(IResourceDelta dta, IProject project) {
		if (dta == null)
			return true;
		String resname = dta.getFullPath().toString();

		if (resname.endsWith(".java") || resname.endsWith(".aj")) {
			ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator()
				.getProjectBuildConfigurator(project);
			BuildConfiguration bc = pbc.getActiveBuildConfiguration();
			List includedFileNames = bc.getIncludedJavaFileNames(ProjectProperties.ASPECTJ_SOURCE_FILTER);
		    if (includedFileNames.contains(dta.getResource().getLocation().toOSString())) {
                return true;
            } else {
                return false;
            }
		} else if (resname.endsWith(".lst")
				&& !resname.endsWith("/generated.lst")) {
			return true;
		} else {
			boolean kids_results = false;
			int i = 0;
			IResourceDelta[] kids = dta.getAffectedChildren();
			while (!kids_results && i < kids.length) {
				kids_results = kids_results | sourceFilesChanged(kids[i], project);
				i++;
			}
			return kids_results;
		}
	}

	/**
	 * Wait up to COMPILE_WAIT_TIME seconds for a build to finish
	 */
	private void waitForBuildCompletion(CompilerMonitor monitor) {
		int timesTried = 0;
		// we learn the appropriate timeout over time...
		int timeout = DEFAULT_TIMEOUT;
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		if (store.contains(COMPILE_TIMEOUT)) {
			timeout = store.getInt(COMPILE_TIMEOUT);
		} else {
			store.setValue(COMPILE_TIMEOUT, timeout);
		}

		while (!monitor.finished() && timesTried < timeout) {
			timesTried++;
			try {
				checkAndHandleCancelation();
				Thread.sleep(1000);
			} catch (Exception e) {
			}

		}
		if (timesTried == timeout) {
			// commenting out continueCompilation(monitor) if statement since
			// dont want popup to appear
			// anymore, now checking whether the user has cancelled the build
			//if (continueCompilation( monitor )) {
			waitForBuildCompletion(monitor);
			//}
		}
	}

	/**
	 * The timeout's fired waiting for a compile to finish... Could be a BIG
	 * project, could be a compiler bug. Let's see what the user wants to do.
	 */
	private boolean continueCompilation(final CompilerMonitor monitor) {

		final String title = AspectJUIPlugin
				.getResourceString("suspiciouslyLongCompileDialog");
		final String message = AspectJUIPlugin
				.getResourceString("isYourProjectReallyBig");

		AspectJUIPlugin.getDefault().getDisplay().syncExec(new Runnable() {
			public void run() {
				boolean keepWaiting = false;
				Shell[] s = AspectJUIPlugin.getDefault().getDisplay().getShells();
				if (s != null && s.length > 0) {
					Shell shell = s[0];
					keepWaiting = MessageDialog.openQuestion(shell, title,
							message);
				}

				if (keepWaiting) {
					//exponential growth to avoid bugging user!
					IPreferenceStore store = AspectJUIPlugin.getDefault()
							.getPreferenceStore();
					int timeout = store.getInt(COMPILE_TIMEOUT);
					timeout *= 2;
					store.setValue(COMPILE_TIMEOUT, timeout);
				} else {
					monitor.finish();
				}
			}
		});

		return !monitor.finished();
	}

	/**
	 * Get the fully qualified path to the build configuation file specified for
	 * this project.
	 */
	private String getBuildFilePath(IProject project) {
		String buildfile = AspectJUIPlugin.getBuildConfigurationFile(project);
		return buildfile;
	}

	/**
	 * If the project uses a default build file, then it will be regenerated to
	 * ensure that all project files are included.
	 */
	private void updateBuildConfigIfNecessary(IProject project,
			List projectFiles) throws CoreException {
		if (getBuildFilePath(project).endsWith(DEFAULT_CONFIG_FILE)) {
			writeBuildConfigFile(projectFiles, project);
		}
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
				if (fileName.endsWith(".java") || fileName.endsWith(".aj")) {
					bw.write(fileName);
					bw.write("\n");
				}
			}
			bw.flush();
			fw.flush();
			bw.close();
			// now tell eclipse the file has been updated
			IResource res = project.findMember(DEFAULT_CONFIG_FILE);
			if (res != null) {
				// Fix for 40556.
				// No progress reporting required on this
				// refresh operation so pass in null for second argument.
				res.refreshLocal(IResource.DEPTH_ZERO, null);
			}
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, AspectJUIPlugin.PLUGIN_ID,
					CONFIG_FILE_WRITE_ERROR, AspectJUIPlugin
							.getResourceString("configFileCreateError"), e);
			throw new CoreException(status);
		}

	}

	/**
	 * Tidies up the output folder before a build. JDT does this by going
	 * through the source and deleting the relevant .class files. That works ok
	 * if you are also listening to things like resource deletes so that you
	 * tidy up some .class files as you go along. AJDT does not do this, so we
	 * use a different approach here. We go through the output directory and
	 * recursively delete all .class files. This, of course, doesn't cope with
	 * resources that might be in the output directory - but I can't delete
	 * everything because some people have the output directory set to the top
	 * level of their project.
	 * 
	 * There is a subtlety with linked folders being used as output folders, but
	 * it does work, I added an AJDTUtils helper method which attempts IPath
	 * dereferencing. if the IPath is a 'linked folder' then the helper method
	 * returns the dereferenced value.
	 */
	protected void cleanOutputFolders(IJavaProject project)
			throws CoreException {
		// Check the project property
		boolean deleteAll = JavaCore.CLEAN.equals(project.getOption(
				JavaCore.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER, true));

		if (deleteAll) {
			boolean linked = false;
			String realOutputLocation = null;

			// Retrieve the output location: e.g. /Proj2/bin
			IPath workspaceRelativeOutputPath = project.getOutputLocation();

			if (workspaceRelativeOutputPath.segmentCount() == 1) { // project
				// root
				realOutputLocation = project.getResource().getLocation()
						.toOSString();
			} else {
				IFolder out = ResourcesPlugin.getWorkspace().getRoot()
						.getFolder(workspaceRelativeOutputPath);
				linked = out.isLinked();
				realOutputLocation = out.getLocation().toOSString();
			}

			File outputDir = new File(realOutputLocation);

			// Recurse through the
			int numberDeleted = wipeClasses(outputDir.listFiles());

			AJDTEventTrace
					.generalEvent("Builder: Tidied output folder, deleted "
							+ numberDeleted
							+ " .class files from "
							+ realOutputLocation
							+ (linked ? " (Linked output folder from "
									+ workspaceRelativeOutputPath.toOSString()
									+ ")" : ""));
		}
	}

	/**
	 * Recursively calling function. Given some set of files (which might be
	 * dirs) it deletes any class files from the filesystem and then for any
	 * directories, recursively calls itself.
	 */
	private int wipeClasses(File[] fs) {
		int count = 0;
		if (fs != null) {
			for (int fcounter = 0; fcounter < fs.length; fcounter++) {
				File file = fs[fcounter];
				if (file.getName().endsWith(".class")) {
					file.delete();
					count++;
				}
				if (file.isDirectory())
					count += wipeClasses(file.listFiles());
			}
		}
		return count;
	}
}