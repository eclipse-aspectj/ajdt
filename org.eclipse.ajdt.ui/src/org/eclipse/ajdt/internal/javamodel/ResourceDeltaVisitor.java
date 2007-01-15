/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *	   Matthew Ford - Bug 154339
 *     Helen Hawkins - updated for new ajde interface (bug 148190)
 *******************************************************************************/
package org.eclipse.ajdt.internal.javamodel;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;

/**
 * Notifies the AJCompilationUnitManager if files got added or removed.
 * 
 * @author Luzius Meisser
 * 
 */
public class ResourceDeltaVisitor implements IResourceDeltaVisitor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public ResourceDeltaVisitor() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
	 */
	public boolean visit(IResourceDelta delta) {
		IResource myRes = delta.getResource();
		if (myRes.getType() == IResource.FILE) {
			switch (delta.getKind()) {
			case IResourceDelta.REMOVED:
				AJCompilationUnitUtils
						.removeFileFromModelAndCloseEditors((IFile) myRes);
				AJDTUtils.refreshPackageExplorer();
				break;
			case IResourceDelta.ADDED:
				AJCompilationUnitManager.INSTANCE
						.getAJCompilationUnit((IFile) myRes);
				AJDTUtils.refreshPackageExplorer();
				break;
			}
		} else if (myRes.getType() == IResource.PROJECT) {
			switch (delta.getKind()) {
			case IResourceDelta.REMOVED:
				// remove the compiler associated with this project from the factory
				AspectJPlugin.getDefault().getCompilerFactory()
						.removeCompilerForProject(myRes.getProject());
				break;
			case IResourceDelta.CHANGED:
				if (!myRes.getProject().isOpen()) {
					// remove the compiler associated with this project from the 
					// factory - project could remain closed indefinitely, therefore, 
					// don't want to hang on to the compiler instance
					AspectJPlugin.getDefault().getCompilerFactory()
							.removeCompilerForProject(myRes.getProject());
				}
				break;
			}
		}
		return true;
	}
}