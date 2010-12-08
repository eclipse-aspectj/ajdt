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

import org.eclipse.contribution.jdt.IsWovenTester;
import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.osgi.framework.Version;

/**
 * @author Andrew Eisenberg
 * @created Jan 19, 2009
 *
 */
public class JDTWeavingPreferences {
    
    public final static String ASK_TO_ENABLE_WEAVING = "org.eclipse.contribution.weaving.jdt.ask";
    public final static String LAST_VERSION = "org.eclipse.contribution.weaving.jdt.version";
    public final static String HOOK_ID = "org.eclipse.equinox.weaving.hook";
    public final static String ASK_TO_REINDEX_PROJECTS = "org.eclipse.contribution.weaving.jdt.reindex";

    
    private static final boolean TEST_MODE;

    static {
        String application = System.getProperty("eclipse.application", "");
        if (application.length() > 0) {
            TEST_MODE = application.endsWith("testapplication"); //$NON-NLS-1$
        } else {
            TEST_MODE = false;
        }
    }
    private JDTWeavingPreferences() {
        // singleton
    }
    
    /**
     * if not in test mode and 
     * if the user has checked "don't ask again" AND the version has not changed,
     * don't ask
     * 
     * If version has changed, then ask
     */
    public static boolean shouldAskToEnableWeaving() {
        return ! TEST_MODE && (getAskToEnableWeaving() || 
          ! getCurrentVersion().equals(getLastVersion())); 
    }
    
    public static void setAskToEnableWeaving(boolean value) {
        internalSetAsk(value);
        setLastVersion();
    }
 
    private static void internalSetAsk(boolean value) {
        IPreferenceStore store = getPreferences();
        store.setValue(ASK_TO_ENABLE_WEAVING, value ? "true" : "false");
    }
    
    public static boolean getAskToEnableWeaving() {
        IPreferenceStore store = getPreferences();
        String value = store.getString(ASK_TO_ENABLE_WEAVING);
        return value == "" || value.equals("true");
    }
    
    /**
     * only ask to reindex if the property is set and 
     * currently weaving
     */
    public static boolean shouldAskToReindex() {
        return getAskToReindex() &&
          IsWovenTester.isWeavingActive(); 
    }

    public static boolean getAskToReindex() {
        IPreferenceStore store = getPreferences();
        String value = store.getString(ASK_TO_REINDEX_PROJECTS);
        return value.equals("true");
    }

    public static void setAskToReindex(boolean value) {
        IPreferenceStore store = getPreferences();
        store.setValue(ASK_TO_REINDEX_PROJECTS, value ? "true" : "false");
    }
    
    public static void setLastVersion() {
        IPreferenceStore store = getPreferences();
        store.setValue(LAST_VERSION, getCurrentVersion().toString());
    }
    
    public static String getLastVersion() {
        IPreferenceStore store = getPreferences();
        return store.getString(LAST_VERSION);
    }
    
    
    private static String getCurrentVersion() {
        BundleDescription hook = 
            Platform.getPlatformAdmin().getState(false).
            getBundle(HOOK_ID, null);
        return (hook != null ? hook.getVersion() : new Version(0,0,0)).toString();
    }
    
    
    
    public static IPreferenceStore getPreferences() {
        return JDTWeavingPlugin.getInstance().getPreferenceStore();
    }
}
