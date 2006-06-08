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
 *******************************************************************************/
package org.eclipse.ajdt.core.builder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.ajde.OutputLocationManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

public class CoreOutputLocationManager implements OutputLocationManager {

	private String projectName;

	private File defaultOutput;
	
	private Map /*File*/ srcFolderToOutput = new HashMap();
	
	private static File workspacePathToFile(IPath path) {
		IFolder out = ResourcesPlugin.getWorkspace().getRoot().getFolder(
				path);
		return out.getLocation().toFile();
	}
	
	public CoreOutputLocationManager(IJavaProject jp) {
		projectName = jp.getProject().getName();
		try {
			defaultOutput = workspacePathToFile(jp.getOutputLocation());
			// store separate output folders in map
			IClasspathEntry[] cpe = jp.getRawClasspath();
			for (int i = 0; i < cpe.length; i++) {
				if (cpe[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath output = cpe[i].getOutputLocation();
					if (output != null) {
						String srcFolder = cpe[i].getPath().lastSegment();
						File out = workspacePathToFile(output);
						srcFolderToOutput.put(srcFolder,out);
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
		String fileName = resource.toString().replace('\\', '/');
		int ind = fileName.indexOf(projectName);
		if (ind != -1) {
			String rest = fileName.substring(ind + projectName.length() + 1);
			int ind2 = rest.indexOf('/');
			if (ind2 != -1) {
				String srcFolder = rest.substring(0,ind2);
				File out = (File)srcFolderToOutput.get(srcFolder);
				if (out != null) {
					return out;
				}
			}
		}
		return defaultOutput;
	}

}
