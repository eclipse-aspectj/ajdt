/**********************************************************************
 Copyright (c) 2002, 2005 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Adrian Colyer, Andy Clement, Tracy Gardner - initial version
 
 AMC 08/12/2002  Changed getAspectjrtClasspath to get version info 
 from Ajde instead of being hard-coded.
 
 Geoff Longman 11/27/2002 Change getClasspath to retrieve entire classpath from
 Project dependencies.

 Matt Chapman - moved getAspectjrtClasspath to core plugin (84967)

 **********************************************************************/
package org.eclipse.ajdt.internal.ui.ajde;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.core.builder.CoreProjectProperties;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
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
	 * On windows, returns whether or not we have a match regardless of
	 * case - bug 82341
	 */
	private boolean caseInsensitiveMatch(String toMatch, IResource resource) {
		if((toMatch.charAt(1) == ':')) {
			return toMatch.toLowerCase().startsWith(resource.getLocation()
					.toString().toLowerCase());
		}
		return false;
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
							// bug 82341 - adding extra check for a match
							// regardless of case on windows
							if (toMatch.startsWith(toCanonical(f.getLocation().toString()))
									|| caseInsensitiveMatch(toMatch,f)) {
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
     						// bug 82341 - adding extra check for a match
							// regardless of case on windows
							if (toMatch.startsWith(toCanonical(iproj.getLocation()
									.toString()))
									|| caseInsensitiveMatch(toMatch,iproj)) {
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
		
		String projectPathStr = p.getLocation().toString();
		try {
			projectPathStr = p.getLocation().toFile().getCanonicalPath();
		} catch (IOException e) {
		}
		IPath projectPath = new Path(projectPathStr);
		IPath filePath = new Path(fullPath);
		if (projectPath.isPrefixOf(filePath)) {
			filePath = filePath.removeFirstSegments(projectPath.segmentCount());
		}
		IResource ret = p.findMember(filePath);
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
	 * Return a List containing strings, each string is the full path to a build
	 * configuration file.
	 */
	public List getBuildConfigFiles() {
		// we don't use .lst files in ajdt anymore
		return new ArrayList();
	}

}