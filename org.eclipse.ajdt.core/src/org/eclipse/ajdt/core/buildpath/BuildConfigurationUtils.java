/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sian January - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.buildpath;

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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class BuildConfigurationUtils {
	
	public static void saveBuildConfiguration(IFile ifile) {
		File file = ifile.getLocation().toFile();
		IProject project = ifile.getProject(); 
		try {
			IJavaProject jp = JavaCore.create(project);
			IClasspathEntry[] entries = jp.getRawClasspath();
			List srcIncludes = new ArrayList();
			List srcExcludes = new ArrayList();
			List srcInclusionpatterns = new ArrayList();
			for (int i = 0; i < entries.length; i++) {
				IClasspathEntry entry = entries[i];
				if(entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath srcpath = entry.getPath();
					srcpath = srcpath.removeFirstSegments(1);
					String path = srcpath.toString().trim();
					if(!path.endsWith("/")) { //$NON-NLS-1$
						path = path + "/"; //$NON-NLS-1$
					}
					srcIncludes.add(path);
					IPath[] inclusions = entry.getInclusionPatterns();
					for (int j = 0; j < inclusions.length; j++) {
						srcInclusionpatterns.add((path.length() > 1 ? path : "" ) + inclusions[j]); //$NON-NLS-1$	
					}
					IPath[] exclusions = entry.getExclusionPatterns();
					for (int j = 0; j < exclusions.length; j++) {
						srcExcludes.add((path.length() > 1 ? path : "" ) + exclusions[j]);  //$NON-NLS-1$
					}					
				}
			}
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(file));
				printProperties(bw, "src.includes", srcIncludes); //$NON-NLS-1$
				printProperties(bw, "src.excludes", srcExcludes); //$NON-NLS-1$
				printProperties(bw, "src.inclusionpatterns", srcInclusionpatterns);	 //$NON-NLS-1$	
			} catch (IOException e) {
			} finally {
				if (bw != null) {				
					try {
						bw.close();
					} catch (IOException e) {
					}
				}
			}
		} catch (JavaModelException e) {
		}
	}


	private static void printProperties(BufferedWriter bw, String key, List values) throws IOException {
		if (values.size() > 0) {
			bw.write(key + " = "); //$NON-NLS-1$
			boolean first = true;
			for (Iterator iter = values.iterator(); iter.hasNext();) {
				String value = (String) iter.next();
				if (!first) {
					for (int i = 0; i < key.length(); i++) {
						bw.write(' ');
					}
					bw.write("   "); //$NON-NLS-1$
				}
				first = false;
				bw.write(value);
				boolean last = !iter.hasNext();
				if (!last) {
					bw.write(",\\"); //$NON-NLS-1$
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
			IClasspathEntry[] originalEntries = project.getRawClasspath();
			for (int i = 0; i < originalEntries.length; i++) {
				IClasspathEntry entry = originalEntries[i];
				if(entry.getEntryKind() != IClasspathEntry.CPE_SOURCE) {			
					classpathEntries.add(entry);
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
				String[] values = value.split(","); //$NON-NLS-1$
				if (name.equals("src.includes")) { //$NON-NLS-1$
					for (int i = 0; i < values.length; i++) {
						String inc = values[i];
						if(inc.equals("/")) { //$NON-NLS-1$
							srcFolders.add(inc);
						} else if(inc.indexOf("/") == inc.length() - 1) { //$NON-NLS-1$
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
				srcFolders.add(""); //$NON-NLS-1$
			}
			while (iter.hasMoreElements()) {
				String name = iter.nextElement().toString();
				String value = properties.get(name).toString();
				String[] values = value.split(","); //$NON-NLS-1$
				if (name.equals("src.inclusionpatterns")) { //$NON-NLS-1$
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
				} else if (name.equals("src.excludes")) { //$NON-NLS-1$
					for (int i = 0; i < values.length; i++) {
						String exc = values[i];
						for (Iterator iterator = srcFolders.iterator(); iterator
						.hasNext();) {
							String srcFolder = (String) iterator.next();
							if(srcFolder.equals("/") || exc.startsWith(srcFolder)) { //$NON-NLS-1$
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
			IClasspathEntry[] entries = new IClasspathEntry[srcFolders.size() + classpathEntries.size()];
			for (int i = 0; i < entries.length; i++) {
				if(srcFolders.size() > i) {
					String srcFolder = (String) srcFolders.get(i);
					IPath path = project.getPath().append(stripSlash(srcFolder));
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
					IClasspathEntry classpathEntry = JavaCore.newSourceEntry(path, exclusionPatterns);
						//new ClasspathEntry(IPackageFragmentRoot.K_SOURCE, IClasspathEntry.CPE_SOURCE, path, ClasspathEntry.INCLUDE_ALL, exclusionPatterns, null, null, null, true, ClasspathEntry.NO_ACCESS_RULES, false, ClasspathEntry.NO_EXTRA_ATTRIBUTES);
					entries[i] = classpathEntry;
				} else {
					entries[i] = (IClasspathEntry) classpathEntries.get(i-srcFolders.size());
				}
			}
			project.setRawClasspath(entries, null);
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
	
	// bug 262953: strip '/' from end of paths so that handles are generated properly
	private static String stripSlash(String folder) {
	    if (folder != null && folder.endsWith("/")) {
	        int strLen = folder.length();
	        return folder.substring(0, strLen-1);
	    } else {
	        return folder;
	    }
	}
}
