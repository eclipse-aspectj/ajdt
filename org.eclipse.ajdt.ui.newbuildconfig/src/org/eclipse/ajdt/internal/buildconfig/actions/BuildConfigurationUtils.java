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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.internal.bc.BuildConfiguration;
import org.eclipse.ajdt.internal.bc.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.buildconfig.DefaultBuildConfigurator;
import org.eclipse.ajdt.ui.buildconfig.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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


	public static List getCurrentFilters(IProject project) {
		List stringList = new ArrayList();
		try {
			List cpListEntries = ClasspathModifier.getExistingEntries(JavaCore.create(project));
			for (Iterator iter = cpListEntries.iterator(); iter.hasNext();) {
				CPListElement element = (CPListElement) iter.next();
				if (element.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IClasspathEntry entry = element.getClasspathEntry();
					IPath srcpath = entry.getPath();
					srcpath = srcpath.removeFirstSegments(1);
					stringList.add("src.includes = " + srcpath + "/");
					// TODO: Should probably include these...
//					IPath[] inclusions = entry.getInclusionPatterns();
//					for (int i = 0; i < inclusions.length; i++) {
//						stringList.add("src.includes = " + srcpath + "/" + inclusions[i]);	
//					}
					IPath[] exclusions = entry.getExclusionPatterns();
					for (int i = 0; i < exclusions.length; i++) {
						stringList.add("src.excludes = " + srcpath + "/" + exclusions[i]); 
					}					
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return stringList;
	}
}
