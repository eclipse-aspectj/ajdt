/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.contribution.jdt.preferences;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.contribution.jdt.IsWovenTester;
import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.core.internal.events.ILifecycleListener;
import org.eclipse.core.internal.events.LifecycleEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Andrew Eisenberg
 * @created Jan 19, 2009
 *
 * Listens for creations of weavable projects (based on project nature)
 */
public class WeavableProjectListener implements ILifecycleListener {
    
    public static String WEAVABLE_NATURE_EXTENSION_POINT = "org.eclipse.contribution.weaving.jdt.weavablenature"; //$NON-NLS-1$
    private Set<String> weavableNatures = null;
    static WeavableProjectListener INSTANCE = new WeavableProjectListener();
    protected WeavableProjectListener() {
        // singleton
    }
    
    public static WeavableProjectListener getInstance() {
        return INSTANCE;
    }
    
    /**
     * not API. For testing only
     */
    public static void setInstance(WeavableProjectListener mock) {
        INSTANCE = mock;
    }
    
    
    private void initWeavableNatures() {
        weavableNatures = new HashSet<String>();
        
        IExtensionPoint exP = null;
        try {
            exP = Platform.getExtensionRegistry().getExtensionPoint(WEAVABLE_NATURE_EXTENSION_POINT);
        } catch (InvalidRegistryObjectException e) {
            JDTWeavingPlugin.logException(e);
        }
        if (exP != null) {
            IExtension[] exs = exP.getExtensions();
            for (int i = 0; i < exs.length; i++) {
                IConfigurationElement[] configs = exs[i].getConfigurationElements();
                for (int j = 0; j < configs.length; j++) {
                    try {
                        IConfigurationElement config = configs[j];
                        String natureid = (String) config.getAttribute("natureid");
                        if (natureid != null) {
                            weavableNatures.add(natureid);
                        }
                    } catch (Exception e) {
                        JDTWeavingPlugin.logException(e);
                    }
                }
            }
        }
    }

    public boolean isWeavableProject(IProject project) throws CoreException {
        try {
            if (weavableNatures == null) {
                initWeavableNatures();
            }
            if (project.isAccessible()) {
                for (String natureId : weavableNatures) {
                    if (project.hasNature(natureId)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            JDTWeavingPlugin.logException(e);
        }
        return false;
    }



    public void handleEvent(LifecycleEvent event) throws CoreException {
        if (event.kind == LifecycleEvent.PRE_PROJECT_OPEN ||
                event.kind == LifecycleEvent.PRE_PROJECT_CREATE) {
            if (event.resource instanceof IProject) {
                if (isWeavableProject((IProject) event.resource)) {
                    askToEnableWeaving(); 
                }
            }
        }
    }
    
    public static void weavableNatureAdded(IProject project) {
        try {
            if (INSTANCE.isWeavableProject(project)) {
                INSTANCE.askToEnableWeaving(); 
            }
        } catch (CoreException e) {
            JDTWeavingPlugin.logException(e);
        }
    }

    protected void askToEnableWeaving() {
        
        try {
            if (!IsWovenTester.isWeavingActive()) {
                if (JDTWeavingPreferences.shouldAskToEnableWeaving()) {
                    // ensure that this runs in UI thread
                    UIJob job = new EnableWeavingServiceJob();
                    // don't schedule if we already have one running
                    if (Job.getJobManager().find(INSTANCE).length == 0) {
                        job.schedule();
                    }
                }
            }
        } catch (Exception e) {
            JDTWeavingPlugin.logException(e);
        }
    }

}
