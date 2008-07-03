/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version ...
 ******************************************************************************/

package org.eclipse.ajdt.internal.launching;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

/**
 * Launches a local VM, ensuring that the classpath is set correctly.
 */
public class AJApplicationLaunchConfigurationDelegate extends
		JavaLaunchDelegate {

	/**
	 * Override super to ensure that the aspectpath is on the runtime classpath
	 * 
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration,
	 *      java.lang.String, org.eclipse.debug.core.ILaunch,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {		
		LaunchConfigurationClasspathUtils.addAspectPathAndOutJarToClasspath(configuration);
		super.launch(configuration, mode, launch, monitor);
	}
	
	/**
	 * There is a launch problem if there exists an aspectj marker that is a kind of java problem
	 * The marker's severity must be an error.
	 * This includes things like declare errors
	 */
	protected boolean isLaunchProblem(IMarker problemMarker)
	        throws CoreException {
        return super.isLaunchProblem(problemMarker) ||
            (problemMarker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO) >= IMarker.SEVERITY_ERROR && 
             problemMarker.isSubtypeOf(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER));
	}

}
