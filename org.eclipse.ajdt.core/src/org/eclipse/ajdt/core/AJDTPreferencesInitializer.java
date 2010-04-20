/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public class AJDTPreferencesInitializer extends AbstractPreferenceInitializer {

    public void initializeDefaultPreferences() {
        Preferences store = AspectJPlugin.getDefault()
            .getPluginPreferences();
        store.setDefault(AspectJCorePreferences.OPTION_IncrementalCompilationOptimizations, true);
    }

}