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

import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
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
    

    private JDTWeavingPreferences() {
        // singleton
    }
    
    /**
     * if the user has checked "don't ask again" AND the version has not changed,
     * don't ask
     * 
     * If version has changed, then ask
     */
    public static boolean shouldAskToEnableWeaving() {
        return getAsk() || 
          ! getCurrentVersion().equals(getLastVersion()); 
    }
    
    public static void setAskToEnableWeaving(boolean value) {
        setAsk(value);
        setLastVersion();
    }
 
    public static void setAsk(boolean value) {
        Preferences store = getPreferences();
        store.setValue(ASK_TO_ENABLE_WEAVING, value ? "true" : "false");
        JDTWeavingPlugin.getInstance().savePluginPreferences();
    }
    
    public static boolean getAsk() {
        Preferences store = getPreferences();
        String value = store.getString(ASK_TO_ENABLE_WEAVING);
        return value == "" || value.equals("true");
    }
    
   
    public static void setLastVersion() {
        Preferences store = getPreferences();
        store.setValue(LAST_VERSION, getCurrentVersion().toString());
        JDTWeavingPlugin.getInstance().savePluginPreferences();
    }
    
    public static String getLastVersion() {
        Preferences store = getPreferences();
        return store.getString(LAST_VERSION);
    }
    
    
    private static String getCurrentVersion() {
        BundleDescription hook = 
            Platform.getPlatformAdmin().getState(false).
            getBundle(HOOK_ID, null);
        return (hook != null ? hook.getVersion() : new Version(0,0,0)).toString();
    }
    
    
    
    public static Preferences getPreferences() {
        return JDTWeavingPlugin.getInstance().getPluginPreferences();
    }
}
