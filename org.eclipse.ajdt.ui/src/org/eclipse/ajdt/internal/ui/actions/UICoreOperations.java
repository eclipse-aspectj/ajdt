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
import org.eclipse.ajdt.ui.buildconfig.DefaultBuildConfigurator;
import org.eclipse.ajdt.ui.buildconfig.IBuildConfiguration;
import org.eclipse.ajdt.ui.buildconfig.IProjectBuildConfigurator;
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
		IProjectBuildConfigurator pbc = DefaultBuildConfigurator.getBuildConfigurator()
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
			IProjectBuildConfigurator pbc = DefaultBuildConfigurator.getBuildConfigurator()
		                               .getProjectBuildConfigurator(project);
			if (pbc == null) {
				// may not be an AJ project, better assume there might be changes
				return true;
			}
			IJavaProject ijp = JavaCore.create(project);		
			if (ijp == null) {
				return true;
			}
			IBuildConfiguration bc = pbc.getActiveBuildConfiguration();
			List includedFileNames;
			if (bc == null) {
				includedFileNames = null;
			} else {
				includedFileNames = bc.getIncludedJavaFileNames(CoreUtils.ASPECTJ_SOURCE_FILTER);
			}
			
			try {
				IPath outputPath = ijp.getOutputLocation();
				if (project.getFullPath().equals(outputPath)) {
					outputPath = null;
				}
				if (sourceFilesChanged(delta, includedFileNames,outputPath)) {
					AJLog.log("build: Examined delta - source file changes in " //$NON-NLS-1$
							+ "required project " + project.getName() ); //$NON-NLS-1$
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

		if (outputLocation!=null && outputLocation.equals(dta.getFullPath()) ) {
			return false;
		}
		
		if (resname.endsWith(".java") || resname.endsWith(".aj")) { //$NON-NLS-1$ //$NON-NLS-2$
		    if ((includedFileNames==null) || includedFileNames.contains(dta.getResource().getLocation().toOSString())) {
                return true;
            } else {
                return false;
            }
		} else if (resname.endsWith(".lst") //$NON-NLS-1$
				&& !resname.endsWith("/generated.lst")) { //$NON-NLS-1$
			return true;
		} else if (resname.endsWith(".classpath")){ //$NON-NLS-1$
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
