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
 *     Matt Chapman - moved getAspectjrtClasspath here from ui plugin (84967)
 *******************************************************************************/
package org.eclipse.ajdt.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaProject;
import org.osgi.framework.Bundle;

/**
 * 
 * @author mchapman
 */
public class CoreUtils {

	/**
	 * Computed classpath to aspectjrt.jar
	 */
	private static String aspectjrtPath = null;
	
	/**
	 * Return the fully-qualified name of the root directory for a project.
	 */
	public static String getProjectRootDirectory(IProject project) {
		return project.getLocation().toOSString();
	}

	public static interface FilenameFilter {
		public boolean accept(String name);
	}

	public static final FilenameFilter ASPECTJ_SOURCE_ONLY_FILTER = new FilenameFilter() {
		public boolean accept(String name) {
			return (name.endsWith(".aj")); //$NON-NLS-1$
		}
	};
	
	public static final FilenameFilter ASPECTJ_SOURCE_FILTER = new FilenameFilter() {
		public boolean accept(String name) {
			return (name.endsWith(".java") || name.endsWith(".aj"));  //$NON-NLS-1$ //$NON-NLS-2$
		}
	};

	public static final FilenameFilter RESOURCE_FILTER = new FilenameFilter() {
		public boolean accept(String name) {
			return !(name.endsWith(".java") || name.endsWith(".aj") || name  //$NON-NLS-1$ //$NON-NLS-2$
					.endsWith(".class")); //$NON-NLS-1$
		}
	};

	/**
	 * Get the aspectjrt.jar classpath entry. This is usually in
	 * plugins/org.aspectj.ajde_ <VERSION>/aspectjrt.jar
	 */
	public static String getAspectjrtClasspath() {

		if (aspectjrtPath == null) {
			StringBuffer cpath = new StringBuffer();

			// This returns the bundle with the highest version or null if none
			// found
			// - for Eclipse 3.0 compatibility
			Bundle ajdeBundle = Platform
					.getBundle(AspectJPlugin.RUNTIME_PLUGIN_ID);

			String pluginLoc = null;
			// 3.0 using bundles instead of plugin descriptors
			if (ajdeBundle != null) {
				URL installLoc = ajdeBundle.getEntry("/"); //$NON-NLS-1$
				URL resolved = null;
				try {
					resolved = FileLocator.resolve(installLoc);
					pluginLoc = resolved.toExternalForm();
				} catch (IOException e) {
				}
			}
			if (pluginLoc != null) {
				if (pluginLoc.startsWith("file:")) { //$NON-NLS-1$
					cpath.append(pluginLoc.substring("file:".length())); //$NON-NLS-1$
					cpath.append("aspectjrt.jar"); //$NON-NLS-1$
				}
			}

			// Verify that the file actually exists at the plugins location
			// derived above. If not then it might be because we are inside
			// a runtime workbench. Check under the workspace directory.
			if (new File(cpath.toString()).exists()) {
				// File does exist under the plugins directory
				aspectjrtPath = cpath.toString();
			} else {
				// File does *not* exist under plugins. Try under workspace...
				IPath rootPath = AspectJPlugin.getWorkspace().getRoot()
						.getLocation();
				IPath installPath = rootPath.removeLastSegments(1);
				cpath = new StringBuffer().append(installPath.toOSString());
				cpath.append(File.separator);
				// TODO: what if the workspace isn't called workspace!!!
				cpath.append("workspace"); //$NON-NLS-1$
				cpath.append(File.separator);
				cpath.append(AspectJPlugin.RUNTIME_PLUGIN_ID);
				cpath.append(File.separator);
				cpath.append("aspectjrt.jar"); //$NON-NLS-1$

				// Only set the aspectjrtPath if the jar file exists here.
				if (new File(cpath.toString()).exists())
					aspectjrtPath = cpath.toString();
			}
		}
		return aspectjrtPath;
	}
	
	/**
	 * Get all projects within the workspace who have a dependency on the given
	 * project - this can either be a class folder dependency or on a library
	 * which the project exports.
	 * 
	 * @param IProject
	 *            project
	 * @return List of two IProject[] where the first is all the class folder
	 *         depending projects, and the second is all the exported library
	 *         dependent projects
	 */
	public static List getDependingProjects(IProject project) {
		List projects = new ArrayList();

		IProject[] projectsInWorkspace = AspectJPlugin.getWorkspace()
				.getRoot().getProjects();
		List outputLocationPaths = getOutputLocationPaths(project);
		IClasspathEntry[] exportedEntries = getExportedEntries(project);
		List classFolderDependingProjects = new ArrayList();
		List exportedLibraryDependingProjects = new ArrayList();

		workThroughProjects: for (int i = 0; i < projectsInWorkspace.length; i++) {
			if (projectsInWorkspace[i].equals(project)
					|| !(projectsInWorkspace[i].isOpen()))
				continue workThroughProjects;
			try {
				if (projectsInWorkspace[i].hasNature(JavaCore.NATURE_ID)) {
					JavaProject javaProject = (JavaProject) JavaCore
							.create(projectsInWorkspace[i]);
					if (javaProject == null)
						continue workThroughProjects;

					try {
						IClasspathEntry[] cpEntry = javaProject
								.getRawClasspath();
						for (int j = 0; j < cpEntry.length; j++) {
							IClasspathEntry entry = cpEntry[j];
							if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
								for (Iterator iter = outputLocationPaths
										.iterator(); iter.hasNext();) {
									IPath path = (IPath) iter.next();
									if (entry.getPath().equals(path)) {
										classFolderDependingProjects
												.add(projectsInWorkspace[i]);
										continue workThroughProjects;
									}
								}
								for (int k = 0; k < exportedEntries.length; k++) {
									if (entry.getPath().equals(
											exportedEntries[k].getPath())) {
										exportedLibraryDependingProjects
												.add(projectsInWorkspace[i]);
									}
								}
							}
						}
					} catch (JavaModelException e) {
						continue workThroughProjects;
					}
				}
			} catch (CoreException e) {
			}
		}
		projects.add(0, classFolderDependingProjects
				.toArray(new IProject[] {}));
		projects.add(1, exportedLibraryDependingProjects
				.toArray(new IProject[] {}));
		return projects;
	}

	private static IClasspathEntry[] getExportedEntries(IProject project) {
		List exportedEntries = new ArrayList();

		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null) {
			return new IClasspathEntry[0];
		}

		try {
			IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
			for (int j = 0; j < cpEntry.length; j++) {
				IClasspathEntry entry = cpEntry[j];
				if (entry.isExported()) {
					// we don't want to export it in the new classpath.
					if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
						IClasspathEntry nonExportedEntry = JavaCore
								.newLibraryEntry(entry.getPath(), null, null);
						exportedEntries.add(nonExportedEntry);
					}
				}
			}
		} catch (JavaModelException e) {
		}
		return (IClasspathEntry[]) exportedEntries
				.toArray(new IClasspathEntry[exportedEntries.size()]);
	}

	/**
	 * Get the output locations for the project
	 * 
	 * @param project
	 * @return list of IPath objects
	 */
	public static List getOutputLocationPaths(IProject project) {
		List outputLocations = new ArrayList();
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null)
			return outputLocations;

		try {
			// Have been unable to create a user scenario where the following
			// for
			// loop adds something to outputLocations, therefore always
			// fall through to the following if loop. However, if a project has
			// more than one output folder, then this for loop should pick them
			// up.
			// Needs testing.......
			IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
			for (int j = 0; j < cpEntry.length; j++) {
				IClasspathEntry entry = cpEntry[j];
				int contentKind = entry.getContentKind();
				if (contentKind == ClasspathEntry.K_OUTPUT) {
					if (entry.getOutputLocation() != null) {
						outputLocations.add(entry.getOutputLocation());
					}
				}
			}
			// If we haven't added anything from reading the .classpath
			// file, then use the default output location
			if (outputLocations.size() == 0) {
				outputLocations.add(javaProject.getOutputLocation());
			}
		} catch (JavaModelException e) {
		}
		return outputLocations;
	}
	
	public static IPath[] getOutputFolders(IJavaProject project) throws CoreException {
		List paths = new ArrayList();
		paths.add(project.getOutputLocation());
		IClasspathEntry[] cpe = project.getRawClasspath();
		for (int i = 0; i < cpe.length; i++) {
			if (cpe[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				IPath output = cpe[i].getOutputLocation();
				if (output != null) {
					paths.add(output);
				}
			}
		}
		return (IPath[])paths.toArray(new IPath[paths.size()]);
	}
	
}
