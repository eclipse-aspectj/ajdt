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
package org.eclipse.ajdt.core.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.core.CoreUtils;
import org.eclipse.ajdt.internal.core.builder.BuildClasspathResolver;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Core version of project properties - subclassed by UI version
 *
 */
public class CoreProjectProperties implements IProjectProperties {

	/**
	 * Created in getClasspath(), should be flushed at end of build
	 */
	private String cachedClasspath = null;

	private Map filesKnownMap = new HashMap();
	
	/**
	 * The name of the current project
	 */
	public String getProjectName() {
		return AspectJPlugin.getDefault().getCurrentProject().getName();
	}

	/*
	 * @see ProjectPropertiesAdapter#getClassToExecute()
	 */
	public String getClassToExecute() {
		System.err
				.println("*** AJ Plugin: ProjectProperties.getClassToExecute NOT IMPLEMENTED ***");
		return null;
	}

	/**
	 * The home directory of the current project
	 */
	public String getRootProjectDir() {
		IProject project = AspectJPlugin.getDefault().getCurrentProject();
		return project.getLocation().toOSString();
	}


	public List getBuildConfigFiles() {
		return null;
	}

	public String getDefaultBuildConfigFile() {
		String defaultLstFile = (AspectJPlugin.getDefault()
				.getCurrentProject().getLocation().toOSString()
				+ File.separator + AspectJPlugin.DEFAULT_CONFIG_FILE);
		return defaultLstFile;
	}

	public String getLastActiveBuildConfigFile() {

		String currentLstFile = AspectJPlugin
				.getBuildConfigurationFile(AspectJPlugin.getDefault()
						.getCurrentProject());
		// System.err.println("AC_temp_debug:
		// ProjectProperties.getLastActiveBuildConfigFile(): Returning
		// "+currentLstFile);
		return currentLstFile;
	}

	public List getProjectSourceFiles() {
		// TODO Use build configs when available in core plugin
		// include all source files for now
		IProject project = AspectJPlugin.getDefault()
			.getCurrentProject();
		List sourceFiles = new ArrayList();
		try {
			IJavaProject jp = JavaCore.create(project);
			IClasspathEntry[] cpes = jp.getRawClasspath();
			for (int i = 0; i < cpes.length; i++) {
				if (cpes[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath path = cpes[i].getPath();
					IResource res = project.findMember(path.removeFirstSegments(1));
					if ((res!=null) && (res.getType() == IResource.FOLDER)) {
						List l = allFiles((IFolder)res);
						sourceFiles.addAll(l);
					}
				}
			}			
		} catch (JavaModelException e) {
		}
		return sourceFiles;
	}

	public void setProjectSourceFileListKnown(IProject project, boolean known) {
		filesKnownMap.put(project,new Boolean(known));
	}
	
	public boolean isProjectSourceFileListKnown(IProject project) {
		Boolean known = (Boolean)filesKnownMap.get(project);
		if (known==null) {
			return false;
		}
		return known.booleanValue();
	}

	//return a list of all file resources in the given folder, including all
	// sub-folders
	// copied from BuildProperties in UI plugin
	private List allFiles(IContainer folder) {
		final List contents = new ArrayList();
		try {
			folder.accept(new IResourceVisitor() {
				public boolean visit(IResource res) {
					if (res.getType() == IResource.FILE
							&& CoreUtils.ASPECTJ_SOURCE_FILTER
									.accept(res.getName())) {
						contents.add(res.getLocation().toFile());
					}
					return true;
				}
			});
		} catch (CoreException e) {
		}
		return contents;
	}

	/*
	 * @see ProjectPropertiesAdapter#getProjectSourcePath()
	 */
	public String getProjectSourcePath() {
		IProject p = AspectJPlugin.getDefault().getCurrentProject();
		// todo - how to tell if the root of the project source tree??
		// is it a getPersistentProperty( )??
		return p.getLocation().toOSString();
	}

	/**
	 * get the classpath to use for compiling the current project.
	 */
	public String getClasspath() {
		if (cachedClasspath!=null) return cachedClasspath;
		IProject proj = AspectJPlugin.getDefault().getCurrentProject();
		IJavaProject jp = JavaCore.create(proj);
		// bug 73035: use this build classpath resolver which is a direct
		// copy from JDT, so the classpath environment is much closer between
		// AspectJ and Java projects.
		cachedClasspath = new BuildClasspathResolver().getClasspath(AspectJPlugin
				.getWorkspace().getRoot(), jp);
		return cachedClasspath;
	}

	public void flushClasspathCache() {
		cachedClasspath = null;
	}

	/**
	 * Called to determine where the resultant class files should go when AJC is
	 * compiling code. We grab the location from the current project and convert
	 * it to a string.
	 */
	public String getOutputPath() {
		IProject currProject = AspectJPlugin.getDefault().getCurrentProject();
		IJavaProject jProject = JavaCore.create(currProject);
		IPath workspaceRelativeOutputPath;
		try {
			workspaceRelativeOutputPath = jProject.getOutputLocation();
		} catch (JavaModelException e) {
			return currProject.getLocation().toOSString();
		}
		if (workspaceRelativeOutputPath.segmentCount() == 1) { // project
			// root
			return jProject.getResource().getLocation().toOSString();
		} else {
			IFolder out = ResourcesPlugin.getWorkspace().getRoot().getFolder(
					workspaceRelativeOutputPath);
			return out.getLocation().toOSString();
		}
	}

	/*
	 * @see ProjectPropertiesAdapter#getBootClasspath()
	 */
	public String getBootClasspath() {
		return null;
	}

	public String getExecutionArgs() {
		return "";
	}

	/*
	 * @see ProjectPropertiesAdapter#getVmArgs()
	 */
	public String getVmArgs() {
		return null;
	}

	public Set getInJars() {
		return null;
	}

	public Set getInpath() {
		return null;
	}

	public Map getSourcePathResources() {
		return null;
	}

	public String getOutJar() {
		return "";
	}

	public Set getSourceRoots() {
		return null;
	}

	public Set getAspectPath() {
		return null;
	}

}
