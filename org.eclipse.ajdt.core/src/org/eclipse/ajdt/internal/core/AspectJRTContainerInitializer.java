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
package org.eclipse.ajdt.internal.core;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class AspectJRTContainerInitializer extends
		ClasspathContainerInitializer {

	public void initialize(IPath containerPath, IJavaProject project)
			throws CoreException {
		int size = containerPath.segmentCount();
		if (size > 0) {
			if (containerPath.segment(0).equals(
					AspectJPlugin.ASPECTJRT_CONTAINER)) {
				AspectJRTContainer container = new AspectJRTContainer();
				JavaCore.setClasspathContainer(containerPath,
						new IJavaProject[] { project },
						new IClasspathContainer[] { container }, null);
			}
		}
	}

}
