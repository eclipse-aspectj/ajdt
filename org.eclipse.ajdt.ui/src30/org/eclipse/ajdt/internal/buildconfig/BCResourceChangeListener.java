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
package org.eclipse.ajdt.internal.buildconfig;

import java.io.File;

import org.aspectj.ajdt.internal.core.builder.IncrementalStateManager;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.model.AJModel;
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
 * -informs BuildConfigurator if a project gets closed or deleted -lets
 * BCResourceDeltaVisitor visit the delta otherwise
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
		if ((event.getType() == IResourceChangeEvent.PRE_CLOSE)
				|| (event.getType() == IResourceChangeEvent.PRE_DELETE)) {
			IResource res = event.getResource();
			if (res.getType() == IResource.PROJECT) {
				IProject project = (IProject) res;
				myBCor.closeProject(project);
				boolean delete = (event.getType() == IResourceChangeEvent.PRE_DELETE);
				String lstFileName = AspectJPlugin.getBuildConfigurationFile(project);
				File lstFile = new File(lstFileName);
				if (delete && lstFile.exists()) {
					lstFile.delete();
				}
				AJModel.getInstance().clearMap(project, delete);
				IncrementalStateManager
						.removeIncrementalStateInformationFor(AspectJPlugin
								.getBuildConfigurationFile(project));
			}
		} else if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			// avoid processing deltas for non-AspectJ projects,
			IResourceDelta delta = event.getDelta();
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
								// if we don't know the project, or it is
								// no longer accessible, we'd better process
								// the delta. Otherwise we only process it
								// if it is an AspectJ project.
								if ((proj == null) || !proj.isAccessible()
										|| AspectJPlugin.isAJProject(proj)) {
									cd[i].accept(myDeltaVisitor);
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