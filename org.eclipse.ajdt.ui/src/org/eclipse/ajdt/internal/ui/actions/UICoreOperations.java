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
package org.eclipse.ajdt.internal.ui.actions;

import java.util.List;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.core.ICoreOperations;
import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * This class captures the implementation of those operations which logically
 * belong in the ajdt.core plugin, but currently have some dependency on UI
 * function. Future refactorings should eventually make this redundant.
 */
public class UICoreOperations implements ICoreOperations {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ajdt.internal.core.CoreOperations#isFullBuildRequested(org.eclipse.core.resources.IProject)
	 */
	public boolean isFullBuildRequested(IProject project) {
		//check if full build needed
		ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator()
				.getProjectBuildConfigurator(project);
		if (pbc != null) {
			if (pbc.fullBuildRequested()) {
				pbc.requestFullBuild(false);
				return true;
			}
		}
		return false;
	}
	
	public boolean sourceFilesChanged(IResourceDelta delta, IProject project) { 
		if (delta!=null && delta.getAffectedChildren().length!=0) {
			ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator()
		                               .getProjectBuildConfigurator(project);
			if (pbc == null) {
				// may not be an AJ project, better assume there might be changes
				return true;
			}
			BuildConfiguration bc = pbc.getActiveBuildConfiguration();
			List includedFileNames = bc.getIncludedJavaFileNames(CoreUtils.ASPECTJ_SOURCE_FILTER);
			IJavaProject ijp = JavaCore.create(project);		
			if (ijp == null) {
				return true;
			}
			
			try {
				if (sourceFilesChanged(delta, includedFileNames,ijp.getOutputLocation())) {
					AJLog.log("build: Examined delta - source file changes in "
							+ "required project " + project.getName() );
					return true;
				} else {
					return false;
				}
			} catch (JavaModelException e) {}
		}
		return true;
	}
	
	private boolean sourceFilesChanged(IResourceDelta dta, List includedFileNames,IPath outputLocation) { //IProject project) {
		if (dta == null) return false;
		
		String resname = dta.getFullPath().toString();

		if ( outputLocation.equals(dta.getFullPath()) ) {
			return false;
		}
		
		if (resname.endsWith(".java") || resname.endsWith(".aj")) { //$NON-NLS-1$ //$NON-NLS-2$
		    if (includedFileNames.contains(dta.getResource().getLocation().toOSString())) {
                return true;
            } else {
                return false;
            }
		} else if (resname.endsWith(".lst") //$NON-NLS-1$
				&& !resname.endsWith("/generated.lst")) { //$NON-NLS-1$
			return true;
		} else {
			boolean kids_results = false;
			int i = 0;
			IResourceDelta[] kids = dta.getAffectedChildren();
			while (!kids_results && i < kids.length) {
				kids_results = kids_results | sourceFilesChanged(kids[i], includedFileNames, outputLocation);
				i++;
			}
			return kids_results;
		}
	}
}
