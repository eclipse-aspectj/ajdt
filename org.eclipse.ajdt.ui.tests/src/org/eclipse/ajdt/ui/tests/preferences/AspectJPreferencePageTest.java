/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     George Harley - initial version
 * 	   Helen Hawkins - converting for use with AJDT 1.1.11 codebase  
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.preferences;

import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferencePage;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This class is required because of a frustrating PDE Junit 
 * problem that prevents classes in the same logical package 
 * from making calls to protected and default access methods
 * across plug-ins. 
 * To test a protected/default access method   
 *
 */
class LocalAspectJPreferencePage extends AspectJPreferencePage {
    public IPreferenceStore doGetPreferenceStore() {
        return super.doGetPreferenceStore();
    }
}

/**
 * @author gharley
 *  
 */
public class AspectJPreferencePageTest extends UITestCase {

    LocalAspectJPreferencePage prefPage;

    protected void setUp() throws Exception {
        super.setUp();

        prefPage = new LocalAspectJPreferencePage();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDoGetPreferenceStore() throws Exception {
        IPreferenceStore ps = prefPage.doGetPreferenceStore();
        assertNotNull(ps);
    }

    public void testInitDefaultPreferences() {
        IPreferenceStore ps = AspectJUIPlugin.getDefault().getPreferenceStore();
        AspectJPreferencePage.initDefaults(ps);
        
        ps.setToDefault(AspectJPreferences.JAVA_OR_AJ_EXT);
        ps.setToDefault(AspectJPreferences.AUTOBUILD_SUPPRESSED);
        ps.setToDefault(AspectJPreferences.PDE_AUTO_IMPORT_CONFIG_DONE);
        ps.setToDefault(AspectJPreferences.ASK_PDE_AUTO_IMPORT);
        ps.setToDefault(AspectJPreferences.DO_PDE_AUTO_IMPORT);
        
        assertEquals(false, ps.getBoolean(AspectJPreferences.JAVA_OR_AJ_EXT));
        assertEquals(true, ps.getBoolean(AspectJPreferences.AUTOBUILD_SUPPRESSED));
        assertEquals(false, ps.getBoolean(AspectJPreferences.PDE_AUTO_IMPORT_CONFIG_DONE));
        assertEquals(true, ps.getBoolean(AspectJPreferences.ASK_PDE_AUTO_IMPORT));
        assertEquals(false, ps.getBoolean(AspectJPreferences.DO_PDE_AUTO_IMPORT));
        assertEquals("", ps.getString("Non-existent-preference"));  
    }

}
