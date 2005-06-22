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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.core.builder.CoreProjectProperties;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
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

/**
 * ProjectProperties is used to pass all the user, project and plugin settings
 * to AJ Tools.
 */
public class ProjectProperties extends CoreProjectProperties  {
	
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
			lstFiles_Strings.add(ir.getFullPath().toOSString());
		}
		return lstFiles_Strings;
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

}