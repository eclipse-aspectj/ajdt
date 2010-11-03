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

import java.net.URL;

import org.eclipse.contribution.jdt.preferences.AskToReindexJob;
import org.eclipse.contribution.jdt.preferences.EnableWeavingServiceJob;
import org.eclipse.contribution.jdt.preferences.JDTWeavingPreferences;
import org.eclipse.contribution.jdt.preferences.WeavableProjectListener;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class JDTWeavingPlugin extends AbstractUIPlugin {
    
    private static JDTWeavingPlugin INSTANCE;
    
    public static String ID = "org.eclipse.contribution.weaving.jdt"; //$NON-NLS-1$
    
    public JDTWeavingPlugin() {
        super();
        INSTANCE = this;
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        initImages();
        
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

    /**
     * maybe more images later
     */
    protected void initImages() {
        DESC_ASPECTJ_32 = createDescriptor(IMG_ASPECTJ_32);
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
    
    public static final String IMG_ASPECTJ_32 = "icons/aspectj32.png";
    public static ImageDescriptor DESC_ASPECTJ_32;

    public static ImageDescriptor createDescriptor(String path) {
        URL url = getInstance().getBundle().getEntry(path);
        ImageDescriptor descriptor = url == null ?
                ImageDescriptor.getMissingImageDescriptor() :
                    ImageDescriptor.createFromURL(url);
        return descriptor;
    }
}