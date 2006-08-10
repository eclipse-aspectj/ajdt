/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Adrian Colyer, Andy Clement, Tracy Gardner - initial version
 *     Matt Chapman - moved and refactored from ui plugin to core
 *******************************************************************************/
package org.eclipse.ajdt.core.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.BuildManager;
import org.aspectj.ajde.ProjectPropertiesAdapter;
import org.aspectj.ajdt.internal.core.builder.AjState;
import org.aspectj.ajdt.internal.core.builder.IStateListener;
import org.aspectj.ajdt.internal.core.builder.IncrementalStateManager;
import org.aspectj.asm.AsmManager;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.BuildConfig;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.core.TimerLogEvent;
import org.eclipse.ajdt.core.lazystart.IAdviceChangedListener;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.text.CoreMessages;
import org.eclipse.ajdt.internal.core.AspectJRTInitializer;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.util.Util;
import org.osgi.service.prefs.BackingStoreException;

/**
 * 
 */
public class AJBuilder extends IncrementalProjectBuilder {

	private static IStateListener isl = null;
	
	private static List buildListeners = new ArrayList();
	
	/**
	 * The build manager used for this build
	 */
	private BuildManager buildManager = null;

	/**
	 * Indicates whether the build has been cancelled by the user
	 */
	private boolean buildCancelled = false;

	/**
	 * The progress monitor used for this build
	 */
	private IProgressMonitor progressMonitor;

	/**
	 * keeps track of the last workbench preference 
	 * (for workaround for bug 73435)
	 */
	private String lastWorkbenchPreference = JavaCore.ABORT;

	/**
	 * The last project we did a build for, needed by content outline view to
	 * decide which updates to accept.
	 */
	private static IProject lastBuiltProject = null;

	public AJBuilder() {
	}
	
	/**
	 * What did we last build?
	 */
	public static IProject getLastBuildTarget() {
		return lastBuiltProject;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor progressMonitor) throws CoreException {
		this.progressMonitor = progressMonitor;
		// 100 ticks for the compiler, 1 for the pre-build actions, 1 for the post-build actions
		progressMonitor.beginTask(CoreMessages.builder_taskname, 102);
		AJLog.logStart(TimerLogEvent.TIME_IN_BUILD);
		String kindS = null;
		if (kind == IncrementalProjectBuilder.AUTO_BUILD)
			kindS = "AUTOBUILD";  //$NON-NLS-1$
		if (kind == IncrementalProjectBuilder.INCREMENTAL_BUILD)
			kindS = "INCREMENTALBUILD";  //$NON-NLS-1$
		if (kind == IncrementalProjectBuilder.FULL_BUILD)
			kindS = "FULLBUILD";  //$NON-NLS-1$
		if (kind == IncrementalProjectBuilder.CLEAN_BUILD)
			kindS = "CLEANBUILD";  //$NON-NLS-1$
		AJLog.log(AJLog.BUILDER,"==========================================================================================="); //$NON-NLS-1$
		AJLog.log(AJLog.BUILDER,"Build kind = " + kindS); //$NON-NLS-1$
		
		IProject project = getProject();
		AspectJPlugin.getDefault().setCurrentProject(project);
		buildCancelled = false;
		
		IProject[] requiredProjects = getRequiredProjects(project,true);
		
		if (IncrementalStateManager.retrieveStateFor(AspectJPlugin
			.getBuildConfigurationFile(project)) == null ) {
		    // bug 101481 - if there is no incremental state then
		    // next build should be a full one.
		    kind = IncrementalProjectBuilder.FULL_BUILD;
		}

		// must call this after checking whether a full build has been requested,
		// otherwise the listeners are called with a different build kind than
		// is actually carried out. In the case of the ui, then this means that 
		// the markers may not be cleared properly.
		preCallListeners(kind, project, requiredProjects);		
		progressMonitor.worked(1);
		
		buildManager = Ajde.getDefault().getBuildManager();
		buildManager.setBuildModelMode(true);

		String mode = "";  //$NON-NLS-1$
		boolean incremental = buildManager.getBuildOptions().getIncrementalMode();
		if (incremental && kind!=IncrementalProjectBuilder.FULL_BUILD) {
			mode = "Incremental AspectJ compilation"; //$NON-NLS-1$
		} else {
			mode = "Full AspectJ compilation"; //$NON-NLS-1$
		}
		AJLog.log(AJLog.BUILDER,"Project=" //$NON-NLS-1$
				+ project.getName() + ", kind of build requested=" + mode); //$NON-NLS-1$

		// if using incremental compiilation, then attempt the incremental model repairs.
		AsmManager.attemptIncrementalModelRepairs = incremental;		

		
		
		// workaround for bug 73435
		IProject[] dependingProjects = getDependingProjects(project);
		IJavaProject javaProject = JavaCore.create(project);
		if (!javaProject.hasBuildState() && dependingProjects.length > 0) {
			updateJavaCompilerPreferences(dependingProjects);
		}
		// end of workaround
		
		// Flush the list of included source files stored for this project
		BuildConfig.flushIncludedSourceFileCache(project);

		ProjectPropertiesAdapter adapter = Ajde.getDefault().getProjectProperties();

		// Check the delta - we only want to proceed if something relevant
		// in this project has changed (a .java file, a .aj file or a 
		// .lst file)
		IResourceDelta dta = getDelta(getProject());
		// copy over any new resources (bug 78579)
		if(dta != null) {
			copyResources(javaProject,dta);
		}
		if (kind != FULL_BUILD) {
		    // need to add check here for whether the classpath has changed
		    if (!sourceFilesChanged(dta, project)){
				AJLog.log(AJLog.BUILDER,"build: Examined delta - no source file changes for project "  //$NON-NLS-1$
								+ project.getName() );
				
				// if the source files of any projects which the current
				// project depends on have changed, then need
				// also to build the current project				
				boolean continueToBuild = false;
				for (int i = 0; !continueToBuild && i < requiredProjects.length; i++) {
					IResourceDelta delta = getDelta(requiredProjects[i]);
					continueToBuild = sourceFilesChanged(delta,requiredProjects[i]);
				}
				if (!continueToBuild) {
					// bug 107027
					if (adapter instanceof CoreProjectProperties) {
						((CoreProjectProperties)adapter).flushClasspathCache();
					}
					postCallListeners(true);
					// Adding this log call because we need to know that
					// AJDT has definitely decided not to pass anything down
					// to the compiler
					AJLog.logEnd(AJLog.BUILDER, TimerLogEvent.TIME_IN_BUILD);
					progressMonitor.done();
					return requiredProjects;						
				}
			}
		}

		migrateToRTContainerIfNecessary(javaProject);

		IAJCompilerMonitor compilerMonitor = AspectJPlugin.getDefault().getCompilerMonitor();
		if (kind == FULL_BUILD) {
			IJavaProject ijp = JavaCore.create(project);
			if (ijp != null)
				cleanOutputFolders(ijp,false);
			else
				AJLog.log(AJLog.BUILDER,"Unable to empty output folder on build all - why cant we find the IJavaProject?"); //$NON-NLS-1$
		}
		compilerMonitor.prepare(project, null/*projectFiles*/, new SubProgressMonitor(progressMonitor,100));

		lastBuiltProject = project;
		
		AJLog.log(AJLog.BUILDER_CLASSPATH,"Classpath="+adapter.getClasspath());
		
		String configFile = AspectJPlugin.getBuildConfigurationFile(project);
		AJLog.logStart(TimerLogEvent.TIME_IN_AJDE);
		if (kind == FULL_BUILD) {
			buildManager.buildFresh(configFile);
		} else {
			buildManager.build(configFile);
		}
		waitForBuildCompletion(compilerMonitor);
		AJLog.logEnd(AJLog.BUILDER, TimerLogEvent.TIME_IN_AJDE);
		
		// We previously refreshed the project to infinite depth to pickup
		// generated artifacts, but this can be very slow and isn't generally
		// required. One case it is required is when a Java project depends on
		// us - without an full refresh, it won't detect the class files written
		// by AJC. A better solution might be for AJC to give us a list of the
		// files it wrote, so we can just tell Eclipse about those.
		boolean javaDep = false;
		for (int i = 0; !javaDep && (i < dependingProjects.length); i++) {
			if (dependingProjects[i].hasNature(JavaCore.NATURE_ID)) {
				javaDep = true;
			}
		}
		try {
			if (javaDep) {
				project.refreshLocal(IResource.DEPTH_INFINITE, null);
			} else {
			    // bug 101481 - need to refresh the output directory
			    // so that the compiled classes can be found
				IPath[] paths = CoreUtils.getOutputFolders(javaProject);
				for (int i = 0; i < paths.length; i++) {
					IPath workspaceRelativeOutputPath = paths[i];
					if (workspaceRelativeOutputPath.segmentCount() == 1) { // project
						// root
						project.refreshLocal(IResource.DEPTH_INFINITE, null);
					} else {
						IFolder out = ResourcesPlugin.getWorkspace().getRoot()
								.getFolder(workspaceRelativeOutputPath);
						out.refreshLocal(IResource.DEPTH_INFINITE, null);
						project.refreshLocal(IResource.DEPTH_ONE, null);
					}
				}
			}
		} catch (CoreException e) {
		}
		
		AJModel.getInstance().createMap(project);
		// bug 107027
		if (adapter instanceof CoreProjectProperties) {
			((CoreProjectProperties)adapter).flushClasspathCache();
		}
		postCallListeners(false);
		progressMonitor.worked(1);
		progressMonitor.done();
		
		AJLog.logEnd(AJLog.BUILDER, TimerLogEvent.TIME_IN_BUILD);
		return requiredProjects;
	}

	/**
	 * Wait until compiler monitor indicates completion
	 */
	private void waitForBuildCompletion(IAJCompilerMonitor monitor) {
		while (!monitor.finished()) {
			try {
				if (checkAndHandleCancelation()) {
					return;
				}
				Thread.sleep(100);
			} catch (Exception e) { }
		}
	}

	/**
	 * Check whether the user has pressed "cancel" and act accordingly
	 */
	private boolean checkAndHandleCancelation() {
		if (progressMonitor != null && buildManager != null && progressMonitor.isCanceled()) {
			buildManager.abortBuild();
			buildCancelled = true;
			AJLog.log(AJLog.BUILDER,"build: Build cancelled as requested"); //$NON-NLS-1$
			return true;
		}
		return false;
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
			IClasspathEntry[] entries = javaProject.getExpandedClasspath();
			for (int i = 0, l = entries.length; i < l; i++) {
				IClasspathEntry entry = entries[i];
				IPath path = entry.getPath();
				IProject p = null;
				switch (entry.getEntryKind()) {
				case IClasspathEntry.CPE_PROJECT:
					// missing projects are considered too
					p = workspaceRoot.getProject(path.lastSegment());
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
	
	/**
	 * Returns the CPE_SOURCE classpath entries for the given IJavaProject
	 * 
	 * @param IJavaProject
	 */
	private IClasspathEntry[] getSrcClasspathEntry(IJavaProject javaProject) throws JavaModelException {
		List srcEntries = new ArrayList();
		if (javaProject == null) {
			return new IClasspathEntry[0];
		}
		IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
		for (int j = 0; j < cpEntry.length; j++) {
			IClasspathEntry entry = cpEntry[j];
			if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				srcEntries.add(entry);
			}
		}	
		return (IClasspathEntry[]) srcEntries.toArray(new IClasspathEntry[srcEntries.size()]);
	}

	/**
	 * Copies non-src resources to the output directory (bug 78579). The main
	 * part of this method was taken from 
	 * org.eclipse.jdt.internal.core.builder.IncrementalImageBuilder.findSourceFiles(IResourceDelta)
	 * 
	 * @param IJavaProject - the project which is being built
	 * @param IResourceDelta - the projects delta
	 * @throws CoreException
	 */
	private boolean copyResources(IJavaProject project, IResourceDelta delta) throws CoreException {
		IClasspathEntry[] srcEntries = getSrcClasspathEntry(project);

		for (int i = 0, l = srcEntries.length; i < l; i++) {
			IClasspathEntry srcEntry = srcEntries[i];
			IPath srcPath = srcEntry.getPath().removeFirstSegments(1);			
			IContainer srcContainer = getContainerForGivenPath(srcPath,project.getProject());

			if(srcContainer.equals(project.getProject())) {				
				int segmentCount = delta.getFullPath().segmentCount();
				IResourceDelta[] children = delta.getAffectedChildren();
				for (int j = 0, m = children.length; j < m; j++)
					if (!isExcludedFromProject(project,children[j].getFullPath(),srcEntries))
						copyResources(project, children[j], srcEntry, segmentCount);
			} else {
				IPath projectRelativePath = srcEntry.getPath().removeFirstSegments(1);
				projectRelativePath.makeRelative();
				
				IResourceDelta sourceDelta = delta.findMember(projectRelativePath);
				if (sourceDelta != null) {
					if (sourceDelta.getKind() == IResourceDelta.REMOVED) {
						return false; // removed source folder should not make it here, but handle anyways (ADDED is supported)
					}
					int segmentCount = sourceDelta.getFullPath().segmentCount();
					IResourceDelta[] children = sourceDelta.getAffectedChildren();
					try {
						for (int j = 0, m = children.length; j < m; j++)
							copyResources(project, children[j], srcEntry, segmentCount);
					} catch (org.eclipse.core.internal.resources.ResourceException e) {
						// catch the case that a package has been renamed and collides on disk with an as-yet-to-be-deleted package
						if (e.getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
							return false;
						}
						throw e; // rethrow
					}
				}
			}
		}
		return true;
	}

	/**
	 * Copies non-src resources to the output directory (bug 78579). The main
	 * part of this method was taken from 
	 * org.eclipse.jdt.internal.core.builder.IncrementalImageBuilder.findSourceFiles(IResourceDelta,ClasspathMultiDirectory,int)
	 * 
	 * @param IJavaProject - the project which is being built
	 * @param IResourceDelta - the projects delta
	 * @param IClasspathEntry - the src entry on the classpath
	 * @param int - the segment count
	 * 
	 * @throws CoreException
	 */
	private void copyResources(IJavaProject javaProject, IResourceDelta sourceDelta, IClasspathEntry srcEntry, int segmentCount) throws CoreException {
		IResource resource = sourceDelta.getResource();
        IPath outputPath = srcEntry.getOutputLocation();
        if (outputPath == null) {
			outputPath = javaProject.getOutputLocation();
		}
        outputPath = outputPath.removeFirstSegments(1).makeRelative();   

        IContainer outputFolder = getContainerForGivenPath(outputPath,javaProject.getProject());        
        IContainer srcContainer = getContainerForGivenPath(srcEntry.getPath().removeFirstSegments(1),javaProject.getProject());

		switch(resource.getType()) {
			case IResource.FOLDER :
				switch (sourceDelta.getKind()) {
					case IResourceDelta.ADDED :
						IPath addedPackagePath = resource.getFullPath().removeFirstSegments(segmentCount);
						createFolder(addedPackagePath, outputFolder); // ensure package exists in the output folder
						// fall thru & collect all the resource files
					case IResourceDelta.CHANGED :
						IResourceDelta[] children = sourceDelta.getAffectedChildren();
						for (int i = 0, l = children.length; i < l; i++)
							copyResources(javaProject, children[i],srcEntry, segmentCount);
						return;
					case IResourceDelta.REMOVED :
						IPath removedPackagePath = resource.getFullPath().removeFirstSegments(segmentCount);
					    IClasspathEntry[] srcEntries = getSrcClasspathEntry(javaProject);
					    if (srcEntries.length > 1) {
							for (int i = 0, l = srcEntries.length; i < l; i++) {
								IPath srcPath = srcEntries[i].getPath().removeFirstSegments(1);
								IFolder srcFolder = javaProject.getProject().getFolder(srcPath);
								if (srcFolder.getFolder(removedPackagePath).exists()) {
									// only a package fragment was removed, same as removing multiple source files
									createFolder(removedPackagePath, outputFolder); // ensure package exists in the output folder
									IResourceDelta[] removedChildren = sourceDelta.getAffectedChildren();
									for (int j = 0, m = removedChildren.length; j < m; j++)
										copyResources(javaProject,removedChildren[j], srcEntry, segmentCount);
									return;
								}
							}
						}
						IFolder removedPackageFolder = outputFolder.getFolder(removedPackagePath);
						if (removedPackageFolder.exists())
							removedPackageFolder.delete(IResource.FORCE, null);
				}
				return;
			case IResource.FILE :
				// only do something if the output folder is different to the src folder
				if (!outputFolder.equals(srcContainer)) {
					// copy all resource deltas to the output folder
					IPath resourcePath = resource.getFullPath().removeFirstSegments(segmentCount);
					if (resourcePath == null) return;
					// don't want to copy over .aj or .java files
			        if (resourcePath.getFileExtension() != null 
			        		&& (resourcePath.getFileExtension().equals("aj") //$NON-NLS-1$
			        				|| resourcePath.getFileExtension().equals("java"))) { //$NON-NLS-1$
						return;
					}
			        IResource outputFile = outputFolder.getFile(resourcePath);
					switch (sourceDelta.getKind()) {
						case IResourceDelta.ADDED :
							if (outputFile.exists()) {
								AJLog.log(AJLog.BUILDER,"Deleting existing file " + resourcePath);//$NON-NLS-1$
								outputFile.delete(IResource.FORCE, null);
							}
							AJLog.log(AJLog.BUILDER,"Copying added file " + resourcePath);//$NON-NLS-1$
							createFolder(resourcePath.removeLastSegments(1), outputFolder); 
							resource.copy(outputFile.getFullPath(), IResource.FORCE, null);
							outputFile.setDerived(true);
							Util.setReadOnly(outputFile, false); // just in case the original was read only
							outputFile.refreshLocal(IResource.DEPTH_ZERO,null);
							return;
						case IResourceDelta.REMOVED :
							if (outputFile.exists()) {
								AJLog.log(AJLog.BUILDER,"Deleting removed file " + resourcePath);//$NON-NLS-1$
								outputFile.delete(IResource.FORCE, null);
							}
							return;
						case IResourceDelta.CHANGED :
							if ((sourceDelta.getFlags() & IResourceDelta.CONTENT) == 0
									&& (sourceDelta.getFlags() & IResourceDelta.ENCODING) == 0)
								return; // skip it since it really isn't changed
							if (outputFile.exists()) {
								AJLog.log(AJLog.BUILDER,"Deleting existing file " + resourcePath);//$NON-NLS-1$
								outputFile.delete(IResource.FORCE, null);
							}
							AJLog.log(AJLog.BUILDER,"Copying changed file " + resourcePath);//$NON-NLS-1$
							createFolder(resourcePath.removeLastSegments(1), outputFolder);
							resource.copy(outputFile.getFullPath(), IResource.FORCE, null);
							outputFile.setDerived(true);
							Util.setReadOnly(outputFile, false); // just in case the original was read only
							outputFile.refreshLocal(IResource.DEPTH_ZERO,null);
					}					
				}
				return;
		}
	}
	
	/**
	 * Returns the IContainer for the given path in the given project
	 * 
	 * @param path
	 * @param project
	 * @return
	 */
	private IContainer getContainerForGivenPath(IPath path, IProject project) {
		if (path.toOSString().equals("")) { //$NON-NLS-1$
			return project;
		}	
		return project.getFolder(path);
	}

	/**
	 * Creates folder with the given path in the given output folder. This method is taken
	 * from org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.createFolder(..)
	 */
	private IContainer createFolder(IPath packagePath, IContainer outputFolder) throws CoreException {
		// Fix for 98663 - create the bin folder if it doesn't exist
		if(!outputFolder.exists() && outputFolder instanceof IFolder) {
			((IFolder)outputFolder).create(true, true, null);
		}
		if (packagePath.isEmpty()) return outputFolder;
		IFolder folder = outputFolder.getFolder(packagePath);
		folder.refreshLocal(IResource.DEPTH_ZERO,null);
		if (!folder.exists()) {
			createFolder(packagePath.removeLastSegments(1), outputFolder);
			folder.create(true, true, null);
			folder.setDerived(true);
			folder.refreshLocal(IResource.DEPTH_ZERO,null);
		}
		return folder;
	}

	/**
	 * This method is taken
	 * from org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.isExcludedFromProject(IPath)
	 */
	private boolean isExcludedFromProject(IJavaProject javaProject, IPath childPath, IClasspathEntry[] srcEntries) throws JavaModelException {
		// answer whether the folder should be ignored when walking the project as a source folder
		if (childPath.segmentCount() > 2) return false; // is a subfolder of a package

		for (int j = 0, k = srcEntries.length; j < k; j++) {
			IPath outputPath = srcEntries[j].getOutputLocation();
	        if (outputPath == null) {
	        	outputPath = javaProject.getOutputLocation();
	        }
	        outputPath = outputPath.removeFirstSegments(1).makeRelative();        
			if (childPath.equals(getContainerForGivenPath(outputPath,javaProject.getProject()).getFullPath())) return true;
			
			IPath srcPath = srcEntries[j].getPath().removeFirstSegments(1);
			if (childPath.equals(getContainerForGivenPath(srcPath,javaProject.getProject()).getFullPath())) return true;
		}
		// skip default output folder which may not be used by any source folder
		return childPath.equals(javaProject.getOutputLocation());
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
	protected void cleanOutputFolders(IJavaProject project, boolean refresh)
			throws CoreException {
		// Check the project property
		boolean deleteAll = JavaCore.CLEAN.equals(project.getOption(
				JavaCore.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER, true));
		if (deleteAll) {
			int numberDeleted = 0;
			IPath[] paths = CoreUtils.getOutputFolders(project);
			for (int i = 0; i < paths.length; i++) {
				numberDeleted += cleanFolder(project, paths[i], refresh);
			}
			AJLog.log(AJLog.BUILDER,"Builder: Tidied output folder(s), deleted " //$NON-NLS-1$
							+ numberDeleted + " .class files"); //$NON-NLS-1$
		}
	}
	
	private int cleanFolder(IJavaProject project, IPath outputFolder, boolean refresh) throws CoreException {
		String realOutputLocation = null;
		IResource outputResource;
		if (outputFolder.segmentCount() == 1) {
			// project root
			outputResource = project.getProject();
			realOutputLocation = project.getResource().getLocation()
					.toOSString();
		} else {
			outputResource = ResourcesPlugin.getWorkspace().getRoot()
					.getFolder(outputFolder);
			realOutputLocation = outputResource.getLocation().toOSString();
		}

		File outputDir = new File(realOutputLocation);
		int numberDeleted = wipeFiles(outputDir.listFiles(), ".class"); //$NON-NLS-1$
		if (refresh) {
			outputResource.refreshLocal(IResource.DEPTH_INFINITE, null);
        }
		return numberDeleted;
	}
	
	public static void cleanAJFilesFromOutputFolder(
			IPath workspaceRelativeOutputPath) throws CoreException {
		IFolder out = ResourcesPlugin.getWorkspace().getRoot().getFolder(
				workspaceRelativeOutputPath);
		String realOutputLocation = out.getLocation().toOSString();
		File outputDir = new File(realOutputLocation);
		int numberDeleted = wipeFiles(outputDir.listFiles(), ".aj"); //$NON-NLS-1$
		out.refreshLocal(IResource.DEPTH_INFINITE, null);
		AJLog.log(AJLog.BUILDER,"Builder: Tidied output folder, deleted " //$NON-NLS-1$
				+ numberDeleted
				+ " .aj files from " //$NON-NLS-1$
				+ realOutputLocation
				+ (out.isLinked() ? " (Linked output folder from " //$NON-NLS-1$
						+ workspaceRelativeOutputPath.toOSString() + ")" : "")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Recursively calling function. Given some set of files (which might be
	 * dirs) it deletes any files with the given extension from the filesystem and then for any
	 * directories, recursively calls itself.
	 */
	private static int wipeFiles(File[] fs, String fileExtension) {
		int count = 0;
		if (fs != null) {
			for (int fcounter = 0; fcounter < fs.length; fcounter++) {
				File file = fs[fcounter];
				if (file.getName().endsWith(fileExtension)) { //$NON-NLS-1$
					file.delete();
					count++;
				}
				if (file.isDirectory())
					count += wipeFiles(file.listFiles(), fileExtension);
			}
		}
		return count;
	}

	
	/**
	 * This is the workaround discussed in bug 73435 for the case when projects are
	 * checked out from CVS, the AJ projects have no valid build state and projects
	 * depend on them.
	 */
	private void updateJavaCompilerPreferences(IProject[] dependingProjects) {
		boolean setWorkbenchPref = false;
		for (int i = 0; i < dependingProjects.length; i++) {
			IProject dependingProject = dependingProjects[i];
			try {
				// Skip over any dependents that are themselves
				// AspectJ projects
				if (AspectJPlugin.isAJProject(dependingProject)) {
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
					// Bug 91131 - In Eclipse 3.1 need to use IEclipsePreferences
					IEclipsePreferences projectPreferences = jp.getEclipsePreferences();
					String[] keys = projectPreferences.keys();

					if (keys.length == 0 && !setWorkbenchPref) {
						Hashtable options = JavaCore.getOptions();
						String workbenchSetting = (String)options.get(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH);
						if (lastWorkbenchPreference.equals(JavaCore.ABORT) 
								&& workbenchSetting.equals(JavaCore.IGNORE)) {
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
							if (!setWorkbenchPref) {
								options.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH,JavaCore.IGNORE);
								JavaCore.setOptions(options);	
								setWorkbenchPref = true;
							} else {
								lastWorkbenchPreference = JavaCore.ABORT;
							}
						}
					} else if (keys.length > 0 && usingProjectBuildingOptions(keys)) {
						projectPreferences.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, JavaCore.IGNORE);
						try {
							projectPreferences.flush();
						} catch (BackingStoreException e) {
							// problem with pref store - quietly ignore
						}
						lastWorkbenchPreference = (String)JavaCore.getOptions().get(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH);
					}
				}// end if dependent has a Java nature
			} catch (CoreException e) {
			} catch (BackingStoreException e) {
			}
		}		
	}
	
	/**
	 * Bug 91131 - Checking to see if the user has selected to use project 
	 * setting for building. Unfortunately, there is no way of checking 
	 * whether the user has selected to use project settings other than to 
	 * see whether the options contained on the building page are in the
	 * IEclipsePreferences. There is also the need for this extra check,
	 * rather than just whether there are any IEclipsePreferences, 
	 * in Eclipse 3.1 because there are several property pages for the 
	 * different compiler options.
	 */
	private boolean usingProjectBuildingOptions(String[] keys) {
		List listOfKeys = Arrays.asList(keys);
		if (listOfKeys.contains(JavaCore.COMPILER_PB_MAX_PER_UNIT)
				|| listOfKeys.contains(JavaCore.CORE_JAVA_BUILD_DUPLICATE_RESOURCE)
				|| listOfKeys.contains(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH)
				|| listOfKeys.contains(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER)
				|| listOfKeys.contains(JavaCore.CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS)
				|| listOfKeys.contains(JavaCore.CORE_CIRCULAR_CLASSPATH)
				|| listOfKeys.contains(JavaCore.CORE_INCOMPLETE_CLASSPATH)
				|| listOfKeys.contains(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL)) {
			return true;
		}		
		return false;
	}
	
	public static void addAJBuildListener(IAJBuildListener listener) {
		if (!buildListeners.contains(listener)) {
			buildListeners.add(listener);
		}
	}

	public static void removeAJBuildListener(IAJBuildListener listener) {
		buildListeners.remove(listener);
	}

	public static void addAdviceListener(IAdviceChangedListener adviceListener) {
		for (Iterator iter = buildListeners.iterator(); iter.hasNext();) {
			IAJBuildListener listener = (IAJBuildListener) iter.next();
			listener.addAdviceListener(adviceListener);
		}
	}

	public static void removeAdviceListener(IAdviceChangedListener adviceListener) {
		for (Iterator iter = buildListeners.iterator(); iter.hasNext();) {
			IAJBuildListener listener = (IAJBuildListener) iter.next();
			listener.removeAdviceListener(adviceListener);
		}
	}
	
	private void preCallListeners(int kind, IProject project, IProject[] requiredProjects) {
		for (Iterator iter = buildListeners.iterator(); iter.hasNext();) {
			IAJBuildListener listener = (IAJBuildListener) iter.next();
			listener.preAJBuild(kind, project, requiredProjects);
		}
	}
	
	private void postCallListeners(boolean noSourceChanges) {
		for (Iterator iter = buildListeners.iterator(); iter.hasNext();) {
			IAJBuildListener listener = (IAJBuildListener) iter.next();
			listener.postAJBuild(getProject(), buildCancelled, noSourceChanges);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#clean(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void clean(IProgressMonitor monitor) throws CoreException {
	    // implemented as part of bug 101481
		IProject project = getProject();
		IncrementalStateManager
				.removeIncrementalStateInformationFor(AspectJPlugin
						.getBuildConfigurationFile(project));
	    
	    removeProblemsAndTasksFor(project);
	    // clean the output folders and do a refresh if not
	    // automatically building (so that output dir reflects the
	    // changes)
	    if (AspectJPlugin.getWorkspace().getDescription().isAutoBuilding()) {
	        cleanOutputFolders(JavaCore.create(project),false);
        } else {
            cleanOutputFolders(JavaCore.create(project),true);
        }
	}
	
	private void removeProblemsAndTasksFor(IResource resource) {
		try {
			if (resource != null && resource.exists()) {
				resource.deleteMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
				resource.deleteMarkers(IJavaModelMarker.TASK_MARKER, false, IResource.DEPTH_INFINITE);
			}
		} catch (CoreException e) {
		}
		AJLog.log(AJLog.BUILDER,"Removed problems and tasks for project "+resource.getName()); //$NON-NLS-1$
	}
		
	public boolean sourceFilesChanged(IResourceDelta delta, IProject project) { 
		if (delta!=null && delta.getAffectedChildren().length!=0) {
			List includedFileNames = BuildConfig.getIncludedSourceFiles(project);
			IJavaProject ijp = JavaCore.create(project);		
			if (ijp == null) {
				return true;
			}			
			try {
				IPath outputPath = ijp.getOutputLocation();
				if (project.getFullPath().equals(outputPath)) {
					outputPath = null;
				}
				if (sourceFilesChanged(delta, includedFileNames,outputPath)) {
					AJLog.log(AJLog.BUILDER,"build: Examined delta - source file changes in " //$NON-NLS-1$
							+ "required project " + project.getName() ); //$NON-NLS-1$
					return true;
				} else {
					return false;
				}
			} catch (JavaModelException e) {}
		}
		return true;
	}
	
	private boolean sourceFilesChanged(IResourceDelta dta, List includedFileNames,IPath outputLocation) { //IProject project) {
		if (dta == null) return false;

		if (outputLocation!=null && outputLocation.equals(dta.getFullPath()) ) {
			return false;
		}
		String resname = dta.getFullPath().toString();
		
		if (CoreUtils.ASPECTJ_SOURCE_FILTER.accept(resname)) {
		    if ((includedFileNames==null) || includedFileNames.contains(dta.getResource())
		    		|| dta.getKind() == IResourceDelta.REMOVED) {
                return true;
            } else {
                return false;
            }
		} else if (resname.endsWith(".classpath")){ //$NON-NLS-1$
			return true;
		} else {
			boolean kids_results = false;
			int i = 0;
			IResourceDelta[] kids = dta.getAffectedChildren();
			while (!kids_results && i < kids.length) {
				kids_results = kids_results | sourceFilesChanged(kids[i], includedFileNames, outputLocation);
				i++;
			}
			return kids_results;
		}
	}
	
	public static void addStateListener() {
		if (isl == null) {
			// Uses secret API in state to get callbacks on useful events
			isl = new IStateListener() {
				public void detectedClassChangeInThisDir(File f) {
				}

				public void aboutToCompareClasspaths(List oldClasspath, List newClasspath) {
				}

				public void pathChangeDetected() {
				}

				public void buildSuccessful(boolean arg0) {
					AJLog.log(AJLog.COMPILER,"AspectJ reports build successful, build was: "+(arg0?"FULL":"INCREMENTAL")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}

				public void detectedAspectDeleted(File f) {
				}

				public void recordDecision(String decision) {
					AJLog.log(AJLog.COMPILER,decision);
				}

				public void recordInformation(String info) {
					AJLog.log(AJLog.COMPILER,info);
				}};
		}
		AjState.stateListener = isl;
	}
	
	public static void removeStateListener() {
		AjState.stateListener = null;
	}
	
	private void migrateToRTContainerIfNecessary(IJavaProject javaProject) {
		if (!AspectJRTInitializer.hasBeenUsed) {
			// if the old ASPECTJRT_LIB var hasn't been initialized
			// then there definitely won't be anything to migrate
			return;
		}
		try {
			IClasspathEntry[] entries = javaProject.getRawClasspath();
			for (int i = 0; i < entries.length; i++) {
				if (entries[i].getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
					String var = entries[i].getPath().segment(0);
					if (var.equals("ASPECTJRT_LIB")) { //$NON-NLS-1$
						// replace with AspectJRT container
						entries[i] = JavaCore.newContainerEntry(
								new Path(AspectJPlugin.ASPECTJRT_CONTAINER), false);
						javaProject.setRawClasspath(entries, new NullProgressMonitor());
						return;
					}
				}
			}
		} catch (JavaModelException e) {
		}
	}

}
