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
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
/**
 * @author Luzius Meisser
 * 
 */
public class ResourceChangeListener implements IResourceChangeListener {
	private ResourceDeltaVisitor myDeltaVisitor;
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public ResourceChangeListener() {
		myDeltaVisitor = new ResourceDeltaVisitor();
	}
	
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.POST_CHANGE){
			IResourceDelta delta = event.getDelta();
			if (delta != null){
				try {
					delta.accept(myDeltaVisitor);
				} catch (CoreException e) {
					AspectJUIPlugin.logException(e);
				}
			}
		}
	}
}