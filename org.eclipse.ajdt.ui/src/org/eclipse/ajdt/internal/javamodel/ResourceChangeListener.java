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
package org.eclipse.ajdt.internal.javamodel;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
//			if (delta != null){
//				try {
//					delta.accept(myDeltaVisitor);
//				} catch (CoreException e) {
//				}
//			}
			// avoid processing deltas for non-AspectJ projects,
			if (delta != null) {
				IResourceDelta[] cd = delta.getAffectedChildren();
				if (cd == null) {
					try {
						delta.accept(myDeltaVisitor);
					} catch (CoreException e) {
					}
				} else {
					for (int i = 0; i < cd.length; i++) {
						try {
							IResource res = cd[i].getResource();
							if (res == null) {
								cd[i].accept(myDeltaVisitor);
							} else {
								IProject proj = res.getProject();
								if ((proj == null) || (!proj.isAccessible())
										|| AspectJPlugin.isAJProject(proj)) {
									cd[i].accept(myDeltaVisitor);
								} else {
									System.out.println("not processing: res="+res+" in project "+proj);
								}
							}
						} catch (CoreException e) {
						}
					}
				}
			}
		}
	}
}