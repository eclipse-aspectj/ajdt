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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.internal.bc.BuildConfiguration;
import org.eclipse.ajdt.internal.bc.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.buildconfig.DefaultBuildConfigurator;
import org.eclipse.ajdt.ui.buildconfig.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class BuildConfigurationUtils {

	
	public static List minimizeIncludes(List files) {
		//if all files of a folder are included, include folder instead of
		// files seperately
		HashSet fileList = new HashSet(files);
		HashSet temp1 = (HashSet) fileList.clone();
		HashSet temp2 = new HashSet();
		HashSet temp3;
		boolean hasChanged = true;
		Iterator iter = temp1.iterator();

		while (hasChanged) {

			hasChanged = false;
			iter = temp1.iterator();
			while (iter.hasNext()) {
				IResource f = (IResource) iter.next();
				IContainer cont = f.getParent();
				if (cont.getType() != IResource.PROJECT) {
					try {
						IResource[] mems = cont.members();
						boolean containsAll = true;
						for (int i = 0; i < mems.length; i++) {
							if (!temp1.contains(mems[i])
									&& CoreUtils.ASPECTJ_SOURCE_FILTER
											.accept(mems[i].getName())) {
								containsAll = false;
								break;
							} else if (!temp1.contains(mems[i])
									&& (mems[i].getType() == IResource.FOLDER)) {
								containsAll = false;
								break;
							}
						}
						if (!containsAll) {
							for (int i = 0; i < mems.length; i++) {
								if (temp1.remove(mems[i]))
									temp2.add(mems[i]);
							}
						} else {
							for (int i = 0; i < mems.length; i++) {
								temp1.remove(mems[i]);
							}
							hasChanged = true;
							temp2.add(cont);
						}
					} catch (CoreException e1) {
					}
				} else {
					temp1.remove(f);
					temp2.add(f);
				}
				iter = temp1.iterator();
			}

			temp3 = temp2;
			temp2 = temp1;
			temp1 = temp3;
		}

		return new ArrayList(temp1);
	}
	
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
}
