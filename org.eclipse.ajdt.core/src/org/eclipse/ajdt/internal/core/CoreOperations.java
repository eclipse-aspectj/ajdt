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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;

/**
 * This interface is to capture those operations which logically belong in the
 * ajdt.core plugin, but currently have some dependency on UI function. Future
 * refactorings should eventually make this redundant.
 */
public interface CoreOperations {

	public boolean isFullBuildRequested(IProject project);

	public boolean sourceFilesChanged(IResourceDelta dta, IProject project);
}
