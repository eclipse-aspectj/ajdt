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

import org.eclipse.ajdt.buildconfigurator.BuildConfiguration;
import org.eclipse.ajdt.buildconfigurator.BuildConfigurator;
import org.eclipse.ajdt.buildconfigurator.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.core.CoreOperations;
import org.eclipse.ajdt.internal.core.CoreUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;

/**
 * This class captures the implementation of those operations which logically
 * belong in the ajdt.core plugin, but currently have some dependency on UI
 * function. Future refactorings should eventually make this redundant.
 */
public class UICoreOperations implements CoreOperations {

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

	public boolean sourceFilesChanged(IResourceDelta dta, IProject project) {
		if (dta == null)
			return true;
		String resname = dta.getFullPath().toString();

		if (resname.endsWith(".java") || resname.endsWith(".aj")) { //$NON-NLS-1$ //$NON-NLS-2$
			ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator()
				.getProjectBuildConfigurator(project);
			BuildConfiguration bc = pbc.getActiveBuildConfiguration();
			List includedFileNames = bc.getIncludedJavaFileNames(CoreUtils.ASPECTJ_SOURCE_FILTER);
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
				kids_results = kids_results | sourceFilesChanged(kids[i], project);
				i++;
			}
			return kids_results;
		}
	}
}
