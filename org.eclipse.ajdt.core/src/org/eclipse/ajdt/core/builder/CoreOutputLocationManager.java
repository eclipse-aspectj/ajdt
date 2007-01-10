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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.ajde.OutputLocationManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

public class CoreOutputLocationManager implements OutputLocationManager {

	private String projectName;

	private File defaultOutput;
	
	private Map /*File*/ srcFolderToOutput = new HashMap();
	private List /*File*/ allOutputLocs = new ArrayList();
	
	private boolean outputIsRoot;
	
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
	
	public CoreOutputLocationManager(IJavaProject jp) {
		outputIsRoot = false;
		projectName = jp.getProject().getName();
		try {
			defaultOutput = workspacePathToFile(jp.getOutputLocation());
			allOutputLocs.add(defaultOutput);
			// store separate output folders in map
			IClasspathEntry[] cpe = jp.getRawClasspath();
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
						if (!allOutputLocs.contains(out)) allOutputLocs.add(out);
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

	public List getAllOutputLocations() {
		return allOutputLocs;
	}

	public File getDefaultOutputLocation() {
		return defaultOutput;
	}

}
