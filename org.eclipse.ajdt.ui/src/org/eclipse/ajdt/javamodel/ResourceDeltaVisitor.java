/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.javamodel;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.internal.core.AJDTUtils;
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
//			case IResourceDelta.CHANGED:
//				System.out.println("Changed: " + myRes);
//				break;
			case IResourceDelta.REMOVED:
				//System.out.println("Removed: " + myRes);
				AJCompilationUnitUtils.removeFileFromModelAndCloseEditors((IFile)myRes);
				AJDTUtils.refreshPackageExplorer();
				break;
			case IResourceDelta.ADDED:
				//System.out.println("Added: " + myRes);
				AJCompilationUnitManager.INSTANCE.getAJCompilationUnit((IFile)myRes);
				AJDTUtils.refreshPackageExplorer();
				break;
			}
		}
		return true;
	}
}