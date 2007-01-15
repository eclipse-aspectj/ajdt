/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *     Helen Hawkins - updated for new ajde interface (bug 148190) 
 *******************************************************************************/
package org.eclipse.ajdt.internal.core.ajde;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.ajde.core.IOutputLocationManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * IOutputLocationManager implementation which uses the methods on IJavaProject
 * to work out where the output should be sent.
 */
public class CoreOutputLocationManager implements IOutputLocationManager {

	private String projectName;
	private IProject project;
	private IJavaProject jProject;

	// if there is more than one output directory then the default output
	// location to use is recorded in the 'defaultOutput' field
	private File defaultOutput;
	
	private Map /*File*/ srcFolderToOutput = new HashMap();
	
	private List /*File*/ allOutputFolders = new ArrayList();
	
	private boolean outputIsRoot;
	// if there is only one output directory then this is recorded in the
	// 'commonOutputDir' field.
	private File commonOutputDir;
	
	public CoreOutputLocationManager(IProject project) {
		this.project = project;
		jProject = JavaCore.create(project);
		if (!isUsingSeparateOutputFolders(jProject)) {
			// using the same output directory therefore record this one
			setCommonOutputDir();
			allOutputFolders.add(commonOutputDir);
		} else {
			// need to record all possible output directories
			init();
		}
	}
	
	/**
	 * Calculate all the output locations
	 */
	private void init() {
		outputIsRoot = false;
		projectName = jProject.getProject().getName();
		try {
			defaultOutput = workspacePathToFile(jProject.getOutputLocation());
			allOutputFolders.add(defaultOutput);
			// store separate output folders in map
			IClasspathEntry[] cpe = jProject.getRawClasspath();
			for (int i = 0; i < cpe.length; i++) {
				if (cpe[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath output = cpe[i].getOutputLocation();
					if (output != null) {
						IPath path = cpe[i].getPath();
						String srcFolder = path.removeFirstSegments(1).toPortableString();
						if (path.segmentCount() == 1) { // output folder is project
							srcFolder = path.toPortableString();
						}
						File out = workspacePathToFile(output);
						srcFolderToOutput.put(srcFolder,out);
						if(!allOutputFolders.contains(out)) allOutputFolders.add(out);
						if (outputIsRoot) {
							// bug 153682: if the project is the source folder
							//  then this output folder will always apply
							defaultOutput = out;
						}
					}
				}
			}
		} catch (JavaModelException e) {
		}
	}
	
	public File getOutputLocationForClass(File compilationUnit) {
		return getOutputLocationForResource(compilationUnit);
	}

	public File getOutputLocationForResource(File resource) {
		if (!isUsingSeparateOutputFolders(jProject)) {
			return commonOutputDir;
		}
		String fileName = resource.toString().replace('\\', '/');
		int ind = fileName.indexOf(projectName);
		if (ind != -1) {
			String rest = fileName.substring(ind + projectName.length() + 1);
			for (Iterator iter = srcFolderToOutput.keySet().iterator(); iter
					.hasNext();) {
				String src = (String) iter.next();
				if (rest.startsWith(src)) {
					File out = (File) srcFolderToOutput.get(src);
					return out;
				}
			}
		}
		return defaultOutput;
	}

	/**
	 * @return true if there is more than one output directory being used
	 * by this project and false otherwise
	 */
	private boolean isUsingSeparateOutputFolders(IJavaProject jp) {
		try {
			IClasspathEntry[] cpe = jp.getRawClasspath();
			for (int i = 0; i < cpe.length; i++) {
				if (cpe[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					if (cpe[i].getOutputLocation() != null) {
						return true;
					}
				}
			}
		} catch (JavaModelException e) {
		}
		return false;
	}
	
	/**
	 * Record the 'common output directory', namely the one where all 
	 * the output goes
	 */
	private void setCommonOutputDir() {
		IJavaProject jProject = JavaCore.create(project);
		IPath workspaceRelativeOutputPath;
		try {
			workspaceRelativeOutputPath = jProject.getOutputLocation();
		} catch (JavaModelException e) {
			commonOutputDir = project.getLocation().toFile();
			return;
		}
		if (workspaceRelativeOutputPath.segmentCount() == 1) { // project
			// root
			commonOutputDir = jProject.getResource().getLocation().toFile();
			return;
		}
		IFolder out = ResourcesPlugin.getWorkspace().getRoot().getFolder(
				workspaceRelativeOutputPath);
		commonOutputDir = out.getLocation().toFile();
	}
	
	private File workspacePathToFile(IPath path) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (path.segmentCount()==1) {
			// bug 153682: getFolder fails when the path is a project
			IResource res = root.findMember(path);
			outputIsRoot = true;
			return res.getLocation().toFile();
		}
		IFolder out = root.getFolder(path);
		return out.getLocation().toFile();
	}

	/**
	 * return all output directories used by this project
	 */
	public List getAllOutputLocations() {
		return allOutputFolders;
	}

	/**
	 * If there's only one output directory return this one, otherwise
	 * return the one marked as default
	 */
	public File getDefaultOutputLocation() {
		if (!isUsingSeparateOutputFolders(jProject)) {
			return commonOutputDir;
		} else {
			return defaultOutput;
		}
	}
}
