/**********************************************************************
 Copyright (c) 2002, 2005 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 Contributors:
 Adrian Colyer, Andy Clement, Tracy Gardner - initial version
 
 AMC 08/12/2002  Changed getAspectjrtClasspath to get version info 
 from Ajde instead of being hard-coded.
 
 Geoff Longman 11/27/2002 Change getClasspath to retrieve entire classpath from
 Project dependencies.

 Matt Chapman - moved getAspectjrtClasspath to core plugin (84967)

 **********************************************************************/
package org.eclipse.ajdt.internal.ui.ajde;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ProjectPropertiesAdapter;
import org.eclipse.ajdt.buildconfigurator.BuildConfiguration;
import org.eclipse.ajdt.buildconfigurator.BuildConfigurator;
import org.eclipse.ajdt.buildconfigurator.ProjectBuildConfigurator;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.builder.CoreProjectProperties;
import org.eclipse.ajdt.internal.core.CoreUtils;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * ProjectProperties is used to pass all the user, project and plugin settings
 * to AJ Tools.
 */
public class ProjectProperties extends CoreProjectProperties implements ProjectPropertiesAdapter {
	
	/**
	 * Called to determine where the resultant class files should go when AJC is
	 * compiling code. We grab the location from the current project and convert
	 * it to a string.
	 */
	public String getOutputPath() {
		try {
			IProject currProject = AspectJPlugin.getDefault()
					.getCurrentProject();
			IJavaProject jProject = JavaCore.create(currProject);
			IPath workspaceRelativeOutputPath = jProject.getOutputLocation();

			if (workspaceRelativeOutputPath.segmentCount() == 1) { // project
				// root
				return jProject.getResource().getLocation().toOSString();
			} else {
				IFolder out = ResourcesPlugin.getWorkspace().getRoot()
						.getFolder(workspaceRelativeOutputPath);
				return out.getLocation().toOSString();
			}

			// Bug 37033 - get the path right, stupid!
			// // Bug21998: Use the project location rather than the workspace
			// location.
			// String workspaceLoc =
			// currProject.getLocation().removeLastSegments(1).toOSString();
			//
			// IJavaProject jProject = JavaCore.create(currProject);
			// String relativePath = jProject.getOutputLocation().toOSString();
			// return workspaceLoc + relativePath;

			// find the resource that corresponds with this path (if its a
			// linked folder this will enable us to dereference it).

			// IResource r =
			// currProject.findMember(workspaceRelativeOutputPath.removeFirstSegments(1));
			// String ret = "";
			// if ( r != null ) ret = r.getLocation().toOSString();
			// AJDTEventTrace.generalEvent("Output path =" + ret);
			//			
			// System.err.println(">>>> ret: " + ret + ", r: " + r);
			//			
			// return ret;
		} catch (CoreException ce) {
			Ajde.getDefault().getErrorHandler().handleError(
					AspectJUIPlugin.getResourceString("noOutputDir"), ce);
		}
		return null;
	}


	/**
	 * All the source files in the current project, as a List of java.io.Files.
	 */
	public List getProjectSourceFiles() {
		IProject activeProject = AspectJPlugin.getDefault()
				.getCurrentProject();
		return getProjectSourceFiles(activeProject,
				CoreUtils.ASPECTJ_SOURCE_FILTER);
	}

	/**
	 * version to use when you know the project
	 */
	public List getProjectSourceFiles(IProject project,
			CoreUtils.FilenameFilter filter) {

		ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator()
				.getProjectBuildConfigurator(project);
		if (pbc != null) {
			return pbc.getActiveBuildConfiguration().getIncludedJavaFiles(
					filter);
		}
		return new ArrayList(0);

	}

	
	/**
	 * get the classpath to use for compiling the current project. NOTE: This is
	 * not currently used, after being replaced by the above version which uses
	 * BuildClasspathResolver
	 */
	public String getOldClasspath() {
		// bug 36577 - about time we stopped using our own hokey classpath
		// building logic
		// and started using the proper APIs!
		StringBuffer classpath = new StringBuffer();

		try {
			IProject proj = AspectJPlugin.getDefault().getCurrentProject();
			IJavaProject jp = JavaCore.create(proj);
			IRuntimeClasspathEntry[] rtcp = JavaRuntime
					.computeUnresolvedRuntimeClasspath(jp);
			HashSet cp = new HashSet();
			for (int i = 0; i < rtcp.length; i++) {
				IRuntimeClasspathEntry[] resolved = RuntimeClasspathCalculator
						.resolveRuntimeClasspathEntry(
						// IRuntimeClasspathEntry[] resolved =
								// JavaRuntime.resolveRuntimeClasspathEntry(
								rtcp[i], jp);
				for (int j = 0; j < resolved.length; j++) {
					cp.add(resolved[j].getLocation());
				}
			}
			for (Iterator it = cp.iterator(); it.hasNext();) {
				classpath.append(it.next());
				classpath.append(File.pathSeparator);
			}
		} catch (CoreException cEx) {
		}

		// StringBuffer classpath = new StringBuffer();
		//
		// // Related to bug21998: Use the project location rather than the
		// workspace location, this allows
		// // for projects who keep their source/binaries in a location other
		// than the workspace directories.
		// //String prependPath =
		// AspectJPlugin.getWorkspace().getRoot().getLocation().toOSString();
		// IProject currProject =
		// AspectJPlugin.getDefault().getCurrentProject();
		//
		// classpath.append(getAspectjrtClasspath());
		// classpath.append(File.pathSeparator);
		// IProject project = AspectJPlugin.getDefault().getCurrentProject();
		//
		// Set projectClasspathSet = new HashSet();
		// Set circularDependencyPreventer = new HashSet();
		//
		// circularDependencyPreventer.add(project);
		//
		// if (project != null) {
		// try {
		//
		// getJavaProjectClasspath(currProject, projectClasspathSet,
		// circularDependencyPreventer,false);
		//
		// } catch (CoreException corex) {
		// String message = AspectJPlugin.getResourceString("jmCoreException");
		// Status status = new Status(Status.ERROR, AspectJPlugin.PLUGIN_ID,
		// Status.OK, message, corex);
		// Shell shell =
		// AspectJPlugin.getDefault().getActiveWorkbenchWindow().getShell();
		// ErrorDialog.openError(shell,
		// AspectJPlugin.getResourceString("ajErrorDialogTitle"), message,
		// status);
		// }
		// }
		//
		// for (Iterator iter = projectClasspathSet.iterator(); iter.hasNext();)
		// {
		// classpath.append((String) iter.next());
		// classpath.append(File.pathSeparator);
		// }

		if (AspectJUIPlugin.isDebugging) {
			// It's *always* a classpath problem ;-). Better print it out.
			System.out.println("Using classpath: " + classpath);
		}

		return classpath.toString();
	}

	private void getJavaProjectClasspath(IProject project,
			Set projectClasspathSet, Set circularDependencyPreventer,
			boolean processingDependantProjects) throws CoreException {

		// Bug 30461 discussion: Rons bug about not supporting dependant
		// projects correctly.
		// The intent here is that the routine is gathering 'classpath elements'
		// in order to build
		// a classpath that can be passed to AJC. The interesting case is when
		// you have a dependant
		// project. In this case recursion occurs - and this routine breaks
		// down. In the recursive case, we are
		// processing the project upon which our *REAL* project depends. And so
		// we want to add components that this
		// dependant project exports, not components that make up its own
		// classpath. To this end I have added the
		// boolean flag to the parameter set for this method, which tells us
		// whether we are in the recursive case.
		// I then added code that (in the case of processing a dependant
		// project) checks that the dependant
		// project has each classpath entry marked as exported, before it is
		// included in the classpath that
		// is being built.
		//
		// However, it is also true that getResolvedClasspath() does not return
		// the outputlocation of the project -
		// this is perfectly reasonable. So part (2) of this fix is to check if
		// we are processing a dependant
		// project (using the new flag) - if we are then we add the
		// outputlocation of the project to the classpath
		// we are building up.

		String projectLocalPrefix = File.separator + project.getName();

		// After this call prependPath will be something like
		// "c:\eclipse\runtime-workspace"
		String prependPath = project.getLocation().removeLastSegments(1)
				.toOSString();

		IJavaProject jProject = JavaCore.create(project);

		// After this call outputLocation will be something like
		// "\MyProject\bin"
		String outputLocation = jProject.getOutputLocation().toOSString();

		// In the recursive case, the project we depend on should have its
		// output directory
		// added to the classpath set.
		if (processingDependantProjects)
			projectClasspathSet.add(prependPath + outputLocation);

		IClasspathEntry[] classpathEntries = jProject
				.getResolvedClasspath(false);
		for (int i = 0; i < classpathEntries.length; i++) {

			String cpstring = classpathEntries[i].getPath().toOSString();

			// This says, for dependant project processing, check the classpath
			// element is marked exported
			// before proceeding.
			if (!processingDependantProjects
					|| classpathEntries[i].isExported()) {
				// what to do next depends on the entry kind
				switch (classpathEntries[i].getEntryKind()) {

				case IClasspathEntry.CPE_LIBRARY:
					// could be an external jar (fully qualified),
					// or a local jar (starts with our project name).
					// the latter needs qualifying for ajc
					if (cpstring != null
							&& cpstring.startsWith(projectLocalPrefix)) {
						cpstring = prependPath + cpstring;
					}
					break;

				case IClasspathEntry.CPE_PROJECT:

					String name = classpathEntries[i].getPath().lastSegment();
					IProject projectDependancy = AspectJPlugin.getWorkspace()
							.getRoot().getProject(name);

					// if classpathEntries[i].isExported())

					if (projectDependancy.exists()
							&& projectDependancy.isOpen()
							&& projectDependancy.hasNature(JavaCore.NATURE_ID)
							&& !circularDependencyPreventer
									.contains(projectDependancy)) {

						circularDependencyPreventer.add(projectDependancy);
						getJavaProjectClasspath(projectDependancy,
								projectClasspathSet,
								circularDependencyPreventer, true);

					}
					continue;

				case IClasspathEntry.CPE_SOURCE:
					// a source directory under my project, needs
					// qualification with the base location for the project.
					cpstring = prependPath + cpstring;
					break;

				case IClasspathEntry.CPE_VARIABLE:
				case IClasspathEntry.CPE_CONTAINER:
				// these two should already be qualified through use of
				// getResolvedClasspath

				default:
				// do nothing
				}

				projectClasspathSet.add(cpstring);
			}

		}
	}


	/*
	 * @see ProjectPropertiesAdapter#getExecutionArgs()
	 */
	public String getExecutionArgs() {
		IProject project = AspectJPlugin.getDefault().getCurrentProject();
		return AspectJPreferences.getCompilerOptions(project);
	}


	// ----------- end of ProjectPropertiesAdapter interface methods
	// ------------

	/**
	 * Called from builder before doing a build in order to clear all problem
	 * markers. If recurse is false then only the markers on the top level
	 * resource (the project) are removed.
	 */
	public void clearMarkers(boolean recurse) {
		IProject currProject = AspectJPlugin.getDefault().getCurrentProject();
		try {
			currProject
					.deleteMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
							false, (recurse ? IResource.DEPTH_INFINITE
									: IResource.DEPTH_ZERO));
			currProject
					.deleteMarkers(IAJModelMarker.AJDT_PROBLEM_MARKER, true,
							(recurse ? IResource.DEPTH_INFINITE
									: IResource.DEPTH_ZERO));
			currProject
					.deleteMarkers(IMarker.TASK, true,
							(recurse ? IResource.DEPTH_INFINITE
									: IResource.DEPTH_ZERO));
		} catch (Exception ex) {
		}
	}

	/**
	 * Return the IResource within the workspace that maps to the given File
	 */
	public IResource findResource(String fullPath) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IPath path = new Path(fullPath);
		return root.getFileForLocation(path);
	}

	/*
	 * Lightweight form of canonical path conversion - only converts
	 * windows-style drive letters to uppercase.
	 */
	private String toCanonical(String path) {
		if ((path.charAt(1) == ':')
				&& (((path.charAt(0) >= 'a') && (path.charAt(0) <= 'z')) || ((path
						.charAt(0) >= 'A') && (path.charAt(0) <= 'Z')))) {
			return Character.toUpperCase(path.charAt(0)) + path.substring(1);
		} else {
			return path;
		}
	}

	/**
	 * Return the IResource within the project that maps to the given File
	 */
	public IResource findResource(String fullPath, IProject p) {

		// full path contains absolute file system paths, we need to undo the
		// effects of any "symbolic linking" in the workspace to ensure that we
		// return the correct IResource.
		String toMatch = toCanonical(fullPath.replace('\\', '/'));
		try {
			IJavaProject jp = JavaCore.create(p);
			IClasspathEntry[] cpes = jp.getRawClasspath();
			for (int i = 0; i < cpes.length; i++) {
				IClasspathEntry e = cpes[i];
				if (e.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath pe = e.getPath();
					if (pe.segment(0).equals(p.getName())) {
						IResource ires = p
								.findMember(pe.removeFirstSegments(1));
						if (ires instanceof IFolder) {
							IFolder f = (IFolder) ires;
							if (toMatch.startsWith(toCanonical(f.getLocation()
									.toString()))) {
								// this is what it was all about!
								// we have a possible symbolic link within our
								// project to the file
								String postfix = toMatch.substring(f
										.getLocation().toString().length());
								IPath postfixPath = new Path(postfix);
								if (f.exists(postfixPath)) {
									return f.findMember(postfixPath);
								}
							}
						} else if (ires instanceof IProject) {
							// I think this is when the project has no src/bin
							// dirs
							IProject iproj = ((IProject) ires);
							if (toMatch.startsWith(toCanonical(iproj
									.getLocation().toString()))) {
								// this is what it was all about!
								// we have a possible symbolic link within our
								// project to the file
								String postfix = toMatch.substring(iproj
										.getLocation().toString().length());
								IPath postfixPath = new Path(postfix);
								if (iproj.exists(postfixPath)) {
									return iproj.findMember(postfixPath);
								}
							}
						}
					}
				}
			}
		} catch (JavaModelException ex) {
		}

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IPath rootPath = root.getLocation();
		IPath path = new Path(fullPath);
		if (rootPath.isPrefixOf(path)) {
			path = path.removeFirstSegments(rootPath.segmentCount());
		}
		IResource ret = p.findMember(path);
		return ret;
	}

	/**
	 * Get the aspectjrt.jar classpath entry. This is usually in
	 * plugins/org.aspectj.ajde_ <VERSION>/aspectjrt.jar
	 */
	public String getAspectjrtClasspath() {
		return CoreUtils.getAspectjrtClasspath();
	}

	/**
	 * Find all the ".java" and ".aj" files in the project.
	 */
	// Change Luzius begin
	// original version:
	// private void getAllFiles(IResource[] resource_list, List allProjectFiles,
	// Hashtable fileToResourceHt, FilenameFilter filter){
	// Luzius' version:
	private void getAllFiles(IResource[] resource_list, List allProjectFiles,
			Hashtable fileToResourceHt, CoreUtils.FilenameFilter filter,
			BuildConfiguration bc) {
		// change Luzius end
		// System.err.println(JavaCore.getOptions().keySet());
		// System.err.println("? getAllFiles() option="
		// +JavaCore.getOption("org.eclipse.jdt.core.builder.resourceCopyExclusionFilter"));

		try {
			for (int i = 0; i < resource_list.length; i++) {
				IResource ir = resource_list[i];

				// Change Luzius begin
				// check exclusion patterns
				// original version:
				// if (ir instanceof IContainer) {
				// getAllFiles(((IContainer) ir).members(), allProjectFiles,
				// fileToResourceHt,filter);
				// }
				// else if (filter.accept(ir.getName())) {
				// Luzius' version:
				if (ir instanceof IContainer) {
					getAllFiles(((IContainer) ir).members(), allProjectFiles,
							fileToResourceHt, filter, bc);
				} else if (filter.accept(ir.getName()) && bc.isIncluded(ir)) {
					// change Luzius end

					allProjectFiles
							.add(new File(ir.getLocation().toOSString()));
					if (fileToResourceHt != null)
						fileToResourceHt.put(new File(ir.getLocation()
								.toOSString()), ir);
				}
			}
		} catch (Exception e) {
		}
	}

	private void getProjectRelativePaths(IResource[] resource_list,
			List allProjectFiles, CoreUtils.FilenameFilter filter,
			int trimSegments) {
		try {
			for (int i = 0; i < resource_list.length; i++) {
				IResource ir = resource_list[i];
				if (ir instanceof IContainer) {
					getProjectRelativePaths(((IContainer) ir).members(),
							allProjectFiles, filter, trimSegments);
				} else if (filter.accept(ir.getName())) {
					String[] segments = ir.getProjectRelativePath().segments();
					String path = ""; //$NON-NLS-1$
					for (int j = trimSegments; j < segments.length; j++) {
						path += segments[j];
						if (j < segments.length - 1)
							path += '/'; // matches Eclipse's separator
					}
					allProjectFiles.add(path);
				}
			}
		} catch (Exception e) {
		}

	}

	/** New interface methods follow for build configuration management */

	/**
	 * Return a List containing strings, each string is the full path to a build
	 * configuration file.
	 */
	public List getBuildConfigFiles() {
		List lstFiles_Strings = new ArrayList();
		List lstFiles_IResources = AspectJUIPlugin.getDefault()
				.getListOfConfigFilesForCurrentProject();
		// Convert the IResource list to a list of strings for the full paths
		Iterator iter = lstFiles_IResources.iterator();
		IResource ir;
		while (iter.hasNext()) {

			ir = (IResource) iter.next();
			// System.err.println("AC_temp_debug:
			// ProjectProperties.getBuildConfigFiles(): Path to a .lst is
			// "+ir.getFullPath().toOSString());
			lstFiles_Strings.add(ir.getFullPath().toOSString());
		}
		return lstFiles_Strings;
	}

	// The following methods added for AspectJ 1.1
	// --------------------------------------------

	/**
	 * Get the set of input jar files for this compilation. Set members should
	 * be of type java.io.File. An empty set or null is acceptable for this
	 * option. From -injars.
	 */
	public Set getInJars() {
		return AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter()
				.getInJars();
	}

	/**
	 * Get the set of non-Java resoure files for this compilation. Set members
	 * should be of type java.io.File. An empty set or null is acceptable for
	 * this option.
	 */
	public Map getSourcePathResources() {
		IProject project = AspectJPlugin.getDefault().getCurrentProject();
		IJavaProject jProject = JavaCore.create(project);
		Map map = new HashMap();
		try {
			IClasspathEntry[] classpathEntries = jProject
					.getResolvedClasspath(false);

			// find the absolute output path
			String realOutputLocation;
			IPath workspaceRelativeOutputPath = jProject.getOutputLocation();
			if (workspaceRelativeOutputPath.segmentCount() == 1) { // project
				// root
				realOutputLocation = jProject.getResource().getLocation()
						.toOSString();
			} else {
				IFolder out = ResourcesPlugin.getWorkspace().getRoot()
						.getFolder(workspaceRelativeOutputPath);
				realOutputLocation = out.getLocation().toOSString();
			}
			// AJDTEventTrace.generalEvent("Project getOutputLocation path: " +
			// realOutputLocation);

			for (int i = 0; i < classpathEntries.length; i++) {
				if (classpathEntries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IClasspathEntry sourceEntry = classpathEntries[i];
					IPath sourcePath = sourceEntry.getPath();
					List files = new ArrayList();
					sourcePath = sourcePath.removeFirstSegments(1);
					IResource[] srcContainer = new IResource[] { project
							.findMember(sourcePath) };
					getProjectRelativePaths(srcContainer, files,
							CoreUtils.RESOURCE_FILTER, srcContainer[0]
									.getFullPath().segmentCount() - 1);

					ArrayList linkedSrcFolders = getLinkedChildFolders(srcContainer[0]);

					for (Iterator it = files.iterator(); it.hasNext();) {
						String relPath = (String) it.next();
						String fullPath = getResourceFullPath(srcContainer[0],
								relPath, linkedSrcFolders);

						// put file on list if not in output path
						if (!fullPath.startsWith(realOutputLocation)
								&& !relPath.endsWith(".classpath") //$NON-NLS-1$
								&& !relPath.endsWith(".project") //$NON-NLS-1$
								&& !relPath.endsWith(".ajsym") //$NON-NLS-1$
								&& !relPath.endsWith(".lst")) { //$NON-NLS-1$
							// AJDTEventTrace.generalEvent("Added to file list
							// (full): " + fullPath);
							File file = new File(fullPath);
							map.put(relPath, file);
						}
					}
				}
			}
		} catch (JavaModelException jmEx) {
			AspectJUIPlugin.getDefault().getErrorHandler().handleError(
					AspectJUIPlugin.getResourceString("ajErrorDialogTitle"), //$NON-NLS-1$
					AspectJUIPlugin.getResourceString("jmCoreException"), jmEx); //$NON-NLS-1$
		}

		return map;
		// return new HashSet(list);
	}

	// public String getFileExt() {
	// boolean javaOrAjExt = AspectJPlugin.getDefault()
	// .getAjdtBuildOptionsAdapter().getJavaOrAjExt();
	// return ".aj";//(javaOrAjExt ? ".java" : ".aj");
	// }

	/**
	 * Get the output jar file for the compilation results. Return null to leave
	 * classfiles unjar'd in output directory From -outjar
	 */
	public String getOutJar() {
		return AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter()
				.getOutJar();
	}

	/**
	 * Get a set of root source directories for the compilation. Set members
	 * should be of type java.io.File Returning null or an empty set disables
	 * the option. From -sourceroots
	 */
	public Set getSourceRoots() {
		return AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter()
				.getSourceRoots();
	}

	/**
	 * Get the set of aspect jar files to be used for the compilation. Returning
	 * null or an empty set disables this option. Set members should be of type
	 * java.io.File. From -aspectpath
	 */
	public Set getAspectPath() {
		return AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter()
				.getAspectPath();
	}

	private ArrayList getLinkedChildFolders(IResource resource) {
		ArrayList resultList = new ArrayList();

		if (resource instanceof IContainer) {
			try {
				IResource[] children = ((IContainer) resource).members();
				for (int i = 0; i < children.length; i++) {
					if ((children[i] instanceof IFolder)
							&& children[i].isLinked()) {
						resultList.add(children[i]);
					}
				}
			} catch (CoreException e) {
			}
		}
		return resultList;
	}

	private String getResourceFullPath(IResource srcContainer, String relPath,
			ArrayList linkedFolders) {
		String result = null;
		if (relPath.lastIndexOf('/') != -1) {
			// Check to see if the relPath under scrutiny is
			// under a linked folder in this project.
			Iterator it = linkedFolders.iterator();
			while (it.hasNext()) {
				IFolder folder = (IFolder) it.next();
				String linkedFolderName = folder.getName();
				if (relPath.indexOf(linkedFolderName + "/") == 0) { //$NON-NLS-1$
					// Do the replacement ensuring that the result uses
					// operating system separator characters.
					result = folder.getLocation().toString()
							+ relPath.substring(linkedFolderName.length());
					result = result.replace('/', File.separatorChar);
					break;
				}
			}
		}
		if (result == null) {
			result = srcContainer.getLocation().toOSString() + File.separator
					+ relPath;
		}
		return result;
	}

	public Set getInpath() {
		return AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter()
				.getInPath();
	}

}