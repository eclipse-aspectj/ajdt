/*******************************************************************************
 * Copyright (c) 2008 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      SpringSource
 *      Andrew Eisenberg (initial implementation)
 *******************************************************************************/
package org.eclipse.contribution.jdt;

import org.eclipse.contribution.jdt.preferences.AskToReindexJob;
import org.eclipse.contribution.jdt.preferences.EnableWeavingServiceJob;
import org.eclipse.contribution.jdt.preferences.JDTWeavingPreferences;
import org.eclipse.contribution.jdt.preferences.WeavableProjectListener;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class JDTWeavingPlugin extends AbstractUIPlugin {
    
    private static JDTWeavingPlugin INSTANCE;
    
    public static String ID = "org.eclipse.contribution.jdt"; //$NON-NLS-1$
    
    public JDTWeavingPlugin() {
        super();
        INSTANCE = this;
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        Workspace workspace = (Workspace) ResourcesPlugin.getWorkspace();
        workspace.addLifecycleListener(WeavableProjectListener.getInstance());
        
        // check to see if we should ask to turn weaving on
        if (!IsWovenTester.isWeavingActive() && JDTWeavingPreferences.shouldAskToEnableWeaving()) {
            boolean found = false;
            IProject[] projects = workspace.getRoot().getProjects();
            for (int i = 0; i < projects.length; i++) {
                if (WeavableProjectListener.getInstance().isWeavableProject(projects[i])) {
                    found = true;
                    break;
                }
            }
            if (found) {
                new EnableWeavingServiceJob().schedule();
            }
        }
        
        if (JDTWeavingPreferences.shouldAskToReindex()) {
            new AskToReindexJob().schedule();
        }
    }

    
    public static void logException(Throwable t) {
        INSTANCE.getLog().log(new Status(IStatus.ERROR, ID, t.getMessage(), t));
    }
    
    public static void logException(String message, Throwable t) {
        INSTANCE.getLog().log(new Status(IStatus.ERROR, ID, message, t));
    }
    
    
    public static JDTWeavingPlugin getInstance() {
        return INSTANCE;
    }
    
}