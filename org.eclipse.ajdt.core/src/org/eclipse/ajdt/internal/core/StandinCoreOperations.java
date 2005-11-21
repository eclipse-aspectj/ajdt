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
package org.eclipse.ajdt.internal.core;

import org.eclipse.ajdt.core.ICoreOperations;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;

/**
 * This is only for when running in headless mode without a UI plugin.
 * Implementations of the required operations are required which don't use UI
 * functionality.
 */
public class StandinCoreOperations implements ICoreOperations {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ajdt.internal.core.CoreOperations#isFullBuildRequested(org.eclipse.core.resources.IProject)
	 */
	public boolean isFullBuildRequested(IProject project) {
		return false;
	}

	public boolean sourceFilesChanged(IResourceDelta dta, IProject project) {
		if (dta == null)
			return true;
		String resname = dta.getFullPath().toString();

		if (resname.endsWith(".java") || resname.endsWith(".aj")) { //$NON-NLS-1$ //$NON-NLS-2$
			// TODO: fix this - need build config support in core
                return true;
		} else if (resname.endsWith(".lst") //$NON-NLS-1$
				&& !resname.endsWith("/generated.lst")) { //$NON-NLS-1$
			return true;
		} else if (resname.endsWith(".classpath")) { //$NON-NLS-1$
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
