package org.eclipse.ajdt.core;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public class AJDTPreferencesInitializer extends AbstractPreferenceInitializer {

    public void initializeDefaultPreferences() {
        Preferences store = AspectJPlugin.getDefault()
            .getPluginPreferences();
        store.setDefault(AspectJCorePreferences.OPTION_AutobuildSuppressed, false);
        store.setDefault(AspectJCorePreferences.OPTION_IncrementalCompilationOptimizations, true);
    }

}