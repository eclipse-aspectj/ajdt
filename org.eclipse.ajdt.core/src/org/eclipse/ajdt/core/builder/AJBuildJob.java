/*******************************************************************************
 * Copyright (c) 2008 SpringSource.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - Initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.builder;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;


public class AJBuildJob extends Job {

    private final IProject project;
    private final int kind;
    
    public AJBuildJob(IProject project, int kind) {
        super("Running AJ Builder...");
        this.project = project;
        this.kind = kind;
    }

    protected IStatus run(IProgressMonitor monitor) {
        if (CoreUtils.isAJProject(project)) {
            try {
                project.build(kind, monitor);
            } catch (CoreException e) {
                return new Status(IStatus.ERROR, AspectJPlugin.PLUGIN_ID, "AJBuildJob failed", e);
            }
        }
        monitor.done();
        return Status.OK_STATUS;
    }

}
