/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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

import org.eclipse.core.resources.IProject;

/**
 * 
 * @author mchapman
 */
public class CoreUtils {

	/**
	 * Return the fully-qualified name of the root directory for a project.
	 */
	public static String getProjectRootDirectory(IProject project) {
		return project.getLocation().toOSString();
	}

	public static interface FilenameFilter {
		public boolean accept(String name);
	}

	public static final FilenameFilter ASPECTJ_SOURCE_ONLY_FILTER = new FilenameFilter() {
		public boolean accept(String name) {
			return (name.endsWith(".aj")); //$NON-NLS-1$
		}
	};
	
	public static final FilenameFilter ASPECTJ_SOURCE_FILTER = new FilenameFilter() {
		public boolean accept(String name) {
			return (name.endsWith(".java") || name.endsWith(".aj"));  //$NON-NLS-1$ //$NON-NLS-2$
		}
	};

	public static final FilenameFilter RESOURCE_FILTER = new FilenameFilter() {
		public boolean accept(String name) {
			return !(name.endsWith(".java") || name.endsWith(".aj") || name  //$NON-NLS-1$ //$NON-NLS-2$
					.endsWith(".class")); //$NON-NLS-1$
		}
	};

}
