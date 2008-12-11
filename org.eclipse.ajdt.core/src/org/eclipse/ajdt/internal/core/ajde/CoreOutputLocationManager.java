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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajde.core.IOutputLocationManager;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
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
	private final IProject project;
	private final IJavaProject jProject;

	// if there is more than one output directory then the default output
	// location to use is recorded in the 'defaultOutput' field
	private File defaultOutput;

	private final Map /* String,File */srcFolderToOutput = new HashMap();

	private final List /* File */allOutputFolders = new ArrayList();

	private final List /* IPath */allSourceFolders = new ArrayList();

	// Bug 243376
	// Gather all of the files that are touched by this compilation
	// and use it to determine which files need to have their
	// Relationship maps updated.
	// XXX Really, this logic should not be in this class
	// This class is about output locations.
	// I am waiting for an extension to the compiler so
	// that I can grab this information directly.
	// NO LONGER MANAGING THIS
	private final Set /* String */touchedCUs = new HashSet();

	private boolean outputIsRoot;
	// if there is only one output directory then this is recorded in the
	// 'commonOutputDir' field.
	private File commonOutputDir;

	public CoreOutputLocationManager(IProject project) {
		this.project = project;
		jProject = JavaCore.create(project);
		initSourceFolders();
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
	 * initialize the source folder locations only
	 */
	private void initSourceFolders() {
		try {
			IClasspathEntry[] cpe = jProject.getRawClasspath();
			for (int i = 0; i < cpe.length; i++) {
				if (cpe[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath path = cpe[i].getPath();
					path = path.removeFirstSegments(1).makeRelative();
					allSourceFolders.add(path);
				}
			}
		} catch (JavaModelException e) {
		}
	}

	/**
	 * Calculate all the output locations
	 */
	private void init() {
		outputIsRoot = false;
		projectName = jProject.getProject().getName();
		String inpathOutFolder = getInpathOutputFolder();
		boolean isUsingNonDefaultInpathOutfolder = inpathOutFolder != null;

		try {
			defaultOutput = workspacePathToFile(jProject.getOutputLocation());
			allOutputFolders.add(defaultOutput);
			// store separate output folders in map
			IClasspathEntry[] cpe = jProject.getRawClasspath();
			outer: for (int i = 0; i < cpe.length; i++) {
				// check to see if on inpath
				if (isUsingNonDefaultInpathOutfolder) {
					IClasspathAttribute[] attributes = cpe[i].getExtraAttributes();
					for (int j = 0; j < attributes.length; j++) {
						if (AspectJCorePreferences.isInPathAttribute(attributes[j])) {
							IPath path = cpe[i].getPath();
							File f = workspacePathToFile(path);
							if (f != null && f.exists()) {
								// use full path
								String srcFolder = new Path(f.getPath()).toPortableString();
								File out = workspacePathToFile(new Path(inpathOutFolder));
								srcFolderToOutput.put(srcFolder, out);
							} else {
								// outfolder does not exist
								// probably because Project has been renamed
								// and inpath output location has not been
								// updated.
								// this is handled with a message to the user
							}
							continue outer;
						}
					}
				}

				if (cpe[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath output = cpe[i].getOutputLocation();
					if (output != null) {
						IPath path = cpe[i].getPath();

						String srcFolder = path.removeFirstSegments(1).toPortableString();
						if (path.segmentCount() == 1) { // output folder is
							// project
							srcFolder = path.toPortableString();
						}
						File out = workspacePathToFile(output);
						srcFolderToOutput.put(srcFolder, out);
						if (!allOutputFolders.contains(out)) {
							allOutputFolders.add(out);
						}
						if (outputIsRoot) {
							// bug 153682: if the project is the source folder
							// then this output folder will always apply
							defaultOutput = out;
						}

					}
				}
			}
		} catch (JavaModelException e) {
		}
	}

	public File getOutputLocationForClass(File compilationUnit) {
		// remember that this file has been asked for
		// presumably it is being recompiled
		if (CoreUtils.ASPECTJ_SOURCE_FILTER.accept(compilationUnit.getName())) {
			touchedCUs.add(compilationUnit);
		}

		return getOutputLocationForResource(compilationUnit);
	}

	public File getOutputLocationForResource(File resource) {
		if (!isUsingSeparateOutputFolders(jProject)) {
			return commonOutputDir;
		}
		if (resource == null || resource.toString() == null) {
			return defaultOutput;
		}
		String fileName = resource.toString().replace('\\', '/');
		if (projectName == null) {
			projectName = jProject.getProject().getName();
			if (projectName == null) {
				AJLog.log(AJLog.DEFAULT, "CoreOutputLocationManager: cannot determine project name of this project: " + jProject); //$NON-NLS-1$
			}
		}
		int ind = fileName.indexOf(projectName);
		if (ind != -1) {
			String rest = fileName.substring(ind + projectName.length() + 1);
			for (Iterator iter = srcFolderToOutput.keySet().iterator(); iter.hasNext();) {
				String src = (String) iter.next();
				if (rest.startsWith(src)) {
					File out = (File) srcFolderToOutput.get(src);
					return out;
				}
			}
		} else {
			// we might have a folder from a different project
			for (Iterator iter = srcFolderToOutput.keySet().iterator(); iter.hasNext();) {
				String src = (String) iter.next();
				if (fileName.startsWith(src)) {
					File out = (File) srcFolderToOutput.get(src);
					return out;
				}
			}
		}
		return defaultOutput;
	}

	/**
	 * @return true if there is more than one output directory being used by
	 *         this project and false otherwise
	 */
	private boolean isUsingSeparateOutputFolders(IJavaProject jp) {
		if (getInpathOutputFolder() != null) {
			return true;
		}
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
	 * Record the 'common output directory', namely the one where all the output
	 * goes
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
		IFolder out = ResourcesPlugin.getWorkspace().getRoot().getFolder(workspaceRelativeOutputPath);
		commonOutputDir = out.getLocation().toFile();
	}

	private File workspacePathToFile(IPath path) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (path.segmentCount() == 1) {
			// bug 153682: getFolder fails when the path is a project
			IResource res = root.findMember(path);
			outputIsRoot = true;
			return res.getLocation().toFile();
		}
		IFolder out = root.getFolder(path);

		IPath outPath = out.getLocation();
		if (outPath != null) {
			return outPath.toFile();
		} else {
			return null;
		}

	}

	/**
	 * return all output directories used by this project
	 */
	public List getAllOutputLocations() {
		return allOutputFolders;
	}

	public String getInpathOutputFolder() {
		String inpathOutFolder = AspectJCorePreferences.getProjectInpathOutFolder(project);
		// assume that the folder is valid...
		// null means that the default out folder is used
		return inpathOutFolder;
	}

	public File[] getTouchedClassFiles() {
		return (File[]) touchedCUs.toArray(new File[touchedCUs.size()]);
	}

	public void resetTouchedClassFiles() {
		touchedCUs.clear();
	}

	/**
	 * If there's only one output directory return this one, otherwise return
	 * the one marked as default
	 */
	public File getDefaultOutputLocation() {
		if (!isUsingSeparateOutputFolders(jProject)) {
			return commonOutputDir;
		} else {
			return defaultOutput;
		}
	}

	public String getSourceFolderForFile(File sourceFile) {
		IPath sourceFilePath = new Path(sourceFile.getAbsolutePath());
		IPath projLoc = project.getLocation();
		if (projLoc.isPrefixOf(sourceFilePath)) {
			sourceFilePath = sourceFilePath.setDevice(null).removeFirstSegments(projLoc.segmentCount()).makeRelative();
		}

		for (Iterator pathIter = allSourceFolders.iterator(); pathIter.hasNext();) {
			IPath sourceFolderPath = (IPath) pathIter.next();
			if (sourceFolderPath.isPrefixOf(sourceFilePath)) {
				return sourceFolderPath.toPortableString();
			}
		}
		return null;
	}

	public void reportClassFileWrite(String outputfile) {
		// TODO something useful
	}
}
