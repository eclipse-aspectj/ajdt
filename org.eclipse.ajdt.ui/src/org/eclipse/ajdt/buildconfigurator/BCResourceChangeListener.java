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
package org.eclipse.ajdt.buildconfigurator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
/**
 * @author Luzius Meisser
 * 
 * Listens to resource changes and:
 * 
 * -informs BuildConfigurator if a project gets closed or deleted
 * -lets BCResourceDeltaVisitor visit the delta otherwise
 * 
 */
public class BCResourceChangeListener implements IResourceChangeListener {
	private BCResourceDeltaVisitor myDeltaVisitor;
	private BuildConfigurator myBCor;
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public BCResourceChangeListener() {
		myDeltaVisitor = new BCResourceDeltaVisitor();
		myBCor = BuildConfigurator.getBuildConfigurator();
	}
	public void resourceChanged(IResourceChangeEvent event) {
		/*try {
			IResource r = event.getResource();
			if (r == null) return;
			IProject proj = r.getProject();
			if (!proj.hasNature(AspectJPlugin.ID_NATURE)) return;
		} catch (CoreException e1) {
			e1.printStackTrace();
			return;
		}*/
		if ((event.getType() == IResourceChangeEvent.PRE_CLOSE) || (event.getType() == IResourceChangeEvent.PRE_DELETE)) {
			IResource res = event.getResource();
			if (res.getType() == IResource.PROJECT){
				myBCor.closeProject((IProject)res);
			}
		} else if (event.getType() == IResourceChangeEvent.POST_CHANGE){
			IResourceDelta delta = event.getDelta();
			if (delta != null)
				try {
					delta.accept(myDeltaVisitor);
				} catch (CoreException e) {
				}
		}
	}
}