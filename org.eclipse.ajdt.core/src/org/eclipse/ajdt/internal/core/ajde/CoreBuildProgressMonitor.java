/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.eclipse.ajdt.internal.core.ajde;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.builder.IAJCompilerMonitor;
import org.eclipse.ajdt.core.text.CoreMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * IBuildProgressMonitor and IAJCompilerMonitor implementation which
 * records the build progress by calling AJLog.log(...)
 */
public class CoreBuildProgressMonitor implements IAJCompilerMonitor {

	private IProject project;
	private IProgressMonitor monitor;
	private boolean buildWasCancelled = false;
	
	public CoreBuildProgressMonitor(IProject project) {
		this.project = project;
	}
	
	// --------------- IBuildProgressMonitor implementation ---------
	
	public void finish(boolean wasFullBuild) {
		AJLog.log(AJLog.COMPILER,"AJC: Build finished. Was full build: "+wasFullBuild); //$NON-NLS-1$
	}

	public void setProgressText(String text) {
		AJLog.log(AJLog.COMPILER_PROGRESS,"AJC: "+text); //$NON-NLS-1$
	}

	public void begin() {
		// ajdt ui provides impl	
	}
	
	public void setProgress(double percentDone) {
		// do nothing since recording progress by logging the message
	}
	
	public boolean isCancelRequested() {
		buildWasCancelled = (monitor != null ? monitor.isCanceled() : false);
		return buildWasCancelled;
	}

	// ------------------ IAJCompilerMonitor implementation -------------	
	
	public void prepare(IProgressMonitor eclipseMonitor) {
		buildWasCancelled = false;
		monitor = eclipseMonitor;
		if (eclipseMonitor != null) {
			eclipseMonitor.beginTask(CoreMessages.builder_taskname, 100);
		}

		AJLog.log(AJLog.COMPILER,"AJC: Starting new build for project " + project.getName()); //$NON-NLS-1$
	}

	public boolean buildWasCancelled() {
		return buildWasCancelled;
	}

}
