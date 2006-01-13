/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sian January - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.buildconfig.actions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.internal.bc.BuildConfiguration;
import org.eclipse.ajdt.internal.bc.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.buildconfig.DefaultBuildConfigurator;
import org.eclipse.ajdt.ui.buildconfig.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.corext.buildpath.ClasspathModifier;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;

public class BuildConfigurationUtils {
	
	/**
	 * Get a new filename for the given project.  Returns the name without the file extension.
	 * @param project
	 * @return a string that is NOT the name of a current build configuration in the project
	 */
	public static String getFreeFileName(IProject project) {
		String defaultFileName = UIMessages.BCDialog_SaveBuildConfigurationAs_default;
	
		int counter = 0;
		if(project != null) {
			boolean foundFreeName = false;
			while (!foundFreeName) {
				String name = counter==0 ? defaultFileName : defaultFileName+counter;
				IPath path = project.getFullPath().append(name + "." + IBuildConfiguration.EXTENSION); //$NON-NLS-1$
				if(!AspectJPlugin.getWorkspace().getRoot().getFile(path).exists()) {
					foundFreeName = true;
				} else {
					counter++;
				}
			}
		}
		return counter==0 ? defaultFileName : defaultFileName+counter;
	}
	

	public static List getIncludedFiles(IProject currentProject) {
		ProjectBuildConfigurator pbc = (ProjectBuildConfigurator) DefaultBuildConfigurator.getBuildConfigurator().getProjectBuildConfigurator(currentProject);
		BuildConfiguration bc = (BuildConfiguration) pbc.getActiveBuildConfiguration();
		return bc.getIncludedIResourceFiles(CoreUtils.ASPECTJ_SOURCE_FILTER);
	}


	public static void saveBuildConfiguration(IFile ifile) {
		File file = ifile.getLocation().toFile();
		IProject project = ifile.getProject(); 
		try {
			List cpListEntries = ClasspathModifier.getExistingEntries(JavaCore.create(project));
			List srcIncludes = new ArrayList();
			List srcExcludes = new ArrayList();
			List srcInclusionpatterns = new ArrayList();
			for (Iterator iter = cpListEntries.iterator(); iter.hasNext();) {
				CPListElement element = (CPListElement) iter.next();
				if (element.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IClasspathEntry entry = element.getClasspathEntry();
					IPath srcpath = entry.getPath();
					srcpath = srcpath.removeFirstSegments(1);
					srcIncludes.add(srcpath.toString() + "/");
					IPath[] inclusions = entry.getInclusionPatterns();
					for (int i = 0; i < inclusions.length; i++) {
						srcInclusionpatterns.add(srcpath + "/" + inclusions[i]);	
					}
					IPath[] exclusions = entry.getExclusionPatterns();
					for (int i = 0; i < exclusions.length; i++) {
						srcExcludes.add(srcpath + "/" + exclusions[i]); 
					}					
				}
			}
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(file));
				printProperties(bw, "src.includes", srcIncludes);
				printProperties(bw, "src.excludes", srcExcludes);
				printProperties(bw, "src.inclusionpatterns", srcInclusionpatterns);		
			} catch (IOException e) {
			} finally {
				if (bw != null) {				
					try {
						bw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}


	private static void printProperties(BufferedWriter bw, String key, List values) throws IOException {
		if (values.size() > 0) {
			bw.append(key + " = ");
			boolean first = true;
			for (Iterator iter = values.iterator(); iter.hasNext();) {
				String value = (String) iter.next();
				if (!first) {
					for (int i = 0; i < key.length(); i++) {
						bw.append(' ');
					}
					bw.append("   ");
				}
				first = false;
				bw.append(value);
				boolean last = !iter.hasNext();
				if (!last) {
					bw.append(",\\");
				}
				bw.newLine();
			}			
		}
	}


	public static void applyBuildConfiguration(IFile ifile) {
		File file = ifile.getLocation().toFile();
		BufferedReader br = null;
		try {
			IJavaProject project = JavaCore.create(ifile.getProject());
			List classpathEntries = new ArrayList();
			List cplistelements = ClasspathModifier.getExistingEntries(project);
			for (Iterator iter = cplistelements.iterator(); iter.hasNext();) {
				CPListElement element = (CPListElement) iter.next();
				if (element.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
					classpathEntries.add(element.getClasspathEntry());
				}
			}
			List srcFolders = new ArrayList();
			Map srcFoldersToIncludes = new HashMap();
			Map srcFoldersToExcludes = new HashMap();
			br = new BufferedReader(new FileReader(file));
			Properties properties = new Properties();
			properties.load(ifile.getContents());
			Enumeration iter = properties.keys();
			
			// first stage - find any source folders
			while (iter.hasMoreElements()) {
				String name = iter.nextElement().toString();
				String value = properties.get(name).toString();
				String[] values = value.split(",");
				if (name.equals("src.includes")) {
					for (int i = 0; i < values.length; i++) {
						String inc = values[i];
						if(inc.indexOf("/") == inc.length() - 1) {
							if(project.getProject().getFolder(inc) != null &&
									project.getProject().getFolder(inc).exists()) {
								srcFolders.add(inc);								
							}
						}
					}
				}
			}
	
				
			// second stage - identify include and exclude filters
			iter = properties.keys();
			if(srcFolders.isEmpty()) {
				srcFolders.add("");
			}
			while (iter.hasMoreElements()) {
				String name = iter.nextElement().toString();
				String value = properties.get(name).toString();
				String[] values = value.split(",");
				if (name.equals("src.inclusionpatterns")) {
					for (int i = 0; i < values.length; i++) {
						String inc = values[i];
						for (Iterator iterator = srcFolders.iterator(); iterator
								.hasNext();) {
							String srcFolder = (String) iterator.next();
							if(inc.startsWith(srcFolder)) {
								List incs = (List) srcFoldersToIncludes.get(srcFolder);
								if (incs == null) {
									incs = new ArrayList();
								}
								incs.add(inc);
								srcFoldersToIncludes.put(srcFolder, incs);
							}
						}
					}
				} else if (name.equals("src.excludes")) {
					for (int i = 0; i < values.length; i++) {
						String exc = values[i];
						for (Iterator iterator = srcFolders.iterator(); iterator
						.hasNext();) {
							String srcFolder = (String) iterator.next();
							if(exc.startsWith(srcFolder)) {
								List excs = (List) srcFoldersToExcludes.get(srcFolder);
								if (excs == null) {
									excs = new ArrayList();
								}
								excs.add(exc);
								srcFoldersToExcludes.put(srcFolder, excs);
							}
						}
					}
				}
			}
			
			// third stage - create classpath entries
			IClasspathEntry[] entries = new IClasspathEntry[srcFolders.size()];
			for (int i = 0; i < entries.length; i++) {
				String srcFolder = (String) srcFolders.get(i);
				IPath path = project.getPath().append(srcFolder);
				List exclusions = (List) srcFoldersToExcludes.get(srcFolder);
				if(exclusions == null) {
					exclusions = Collections.EMPTY_LIST;
				}
				List inclusions = (List) srcFoldersToIncludes.get(srcFolder);
				if(inclusions == null) {
					inclusions = Collections.EMPTY_LIST;
				}
				IPath[] exclusionPatterns = new IPath[exclusions.size()];
				for (int j = 0; j < exclusionPatterns.length; j++) {
					String exclusionPathStr = (String) exclusions.get(j);
					if (exclusionPathStr.startsWith(srcFolder)) {
						exclusionPathStr = exclusionPathStr.substring(srcFolder.length());
					}
					IPath exclusionPath = new Path(exclusionPathStr);
					exclusionPatterns[j] = exclusionPath;
					
				}
				IPath[] inclusionPatterns = new IPath[inclusions.size()];
				for (int j = 0; j < inclusionPatterns.length; j++) {
					String inclusionPathStr = (String) inclusions.get(j);
					if (inclusionPathStr.startsWith(srcFolder)) {
						inclusionPathStr = inclusionPathStr.substring(srcFolder.length());
					}
					IPath inclusionPath = new Path(inclusionPathStr);
					inclusionPatterns[j] = inclusionPath;
					
				}
				IClasspathEntry classpathEntry = new ClasspathEntry(IPackageFragmentRoot.K_SOURCE, IClasspathEntry.CPE_SOURCE, path, ClasspathEntry.INCLUDE_ALL, exclusionPatterns, null, null, null, true, ClasspathEntry.NO_ACCESS_RULES, false, ClasspathEntry.NO_EXTRA_ATTRIBUTES);
				entries[i] = classpathEntry;
			}
			((JavaProject)project).setRawClasspath(entries, null);
		} catch (FileNotFoundException e) {
		} catch (JavaModelException e) {
		} catch (IOException e) {
		} catch (CoreException e) {
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
