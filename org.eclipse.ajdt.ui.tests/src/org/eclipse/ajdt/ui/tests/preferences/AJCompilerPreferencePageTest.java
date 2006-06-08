/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.ui.tests.preferences;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.preferences.AJCompilerPreferencePage;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This class tests the AJCompilerPreferencePage. 
 */
public class AJCompilerPreferencePageTest extends UITestCase {

	IProject project;
	IJavaProject jp;
	IEclipsePreferences projectNode;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = createPredefinedProject("Simple AJ Project"); //$NON-NLS-1$
		jp = JavaCore.create(project);
		
		IScopeContext projectScope = new ProjectScope(project);
		projectNode = projectScope.getNode(AspectJPlugin.PLUGIN_ID);

	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		projectNode = null;
	}
	
	/**
	 * Want to test that the defaults being used in the AJCompilerPreferencePage
	 * (via Window > Preferences) is the same as in the CompilerPreferencePage
	 * (via right click > Preferences).
	 *
	 */
	public void testSetDefaults() throws Exception {
		IPreferenceStore prefStore = AspectJUIPlugin.getDefault().getPreferenceStore();		
		AJCompilerPreferencePage.initDefaults(prefStore);
		AJCompilerPreferencePage.setProjectDefaults(projectNode);

		String[] keys = projectNode.keys(); 
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			String compilerDefault = projectNode.get(key,""); //$NON-NLS-1$
			String storeDefault = prefStore.getDefaultString(key);
			assertEquals("default settings should be the same",storeDefault,compilerDefault); //$NON-NLS-1$
		}
	}

	public void testSetDefaultsIfValueNotAlreadySet() throws Exception {
		IPreferenceStore prefStore = AspectJUIPlugin.getDefault().getPreferenceStore();		
		AJCompilerPreferencePage.initDefaults(prefStore);
		projectNode.put(AspectJPreferences.OPTION_WeaveMessages,"true"); //$NON-NLS-1$
		AJCompilerPreferencePage.setProjectDefaultsIfValueNotAlreadySet(projectNode);

		String[] keys = projectNode.keys(); 
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			String compilerSetting = projectNode.get(key,""); //$NON-NLS-1$
			String storeDefault = prefStore.getDefaultString(key);
			if (key.equals(AspectJPreferences.OPTION_WeaveMessages)) {
				boolean eq = storeDefault.equals(compilerSetting);
				assertFalse("should not have overwritten showWeaveMessages option",eq); //$NON-NLS-1$
			} else {
				assertEquals("default settings should be the same",storeDefault,compilerSetting);				 //$NON-NLS-1$
			}
		}
	}

	public void testRemoveValues() throws Exception {
		AJCompilerPreferencePage.setProjectDefaults(projectNode);
		AJCompilerPreferencePage.removeProjectValues(projectNode);
		String[] keys = projectNode.keys();
		assertEquals("there should be no settings",0,keys.length);		 //$NON-NLS-1$
	}

}
