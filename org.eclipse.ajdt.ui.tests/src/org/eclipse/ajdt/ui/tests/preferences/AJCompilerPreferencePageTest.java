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
import org.eclipse.ajdt.ui.tests.testutils.TestLogger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
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
			assertEquals("default settings should be the same for key " + key,storeDefault,compilerDefault); //$NON-NLS-1$
		}
	}

	public void testSetDefaultsIfValueNotAlreadySet() throws Exception {
		IPreferenceStore prefStore = AspectJUIPlugin.getDefault().getPreferenceStore();		
		AJCompilerPreferencePage.initDefaults(prefStore);
		projectNode.put(AspectJPreferences.OPTION_XHasMember,"true"); //$NON-NLS-1$
		AJCompilerPreferencePage.setProjectDefaultsIfValueNotAlreadySet(projectNode);

		String[] keys = projectNode.keys(); 
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			String compilerSetting = projectNode.get(key,""); //$NON-NLS-1$
			String storeDefault = prefStore.getDefaultString(key);
			if (key.equals(AspectJPreferences.OPTION_XHasMember)) {
				boolean eq = storeDefault.equals(compilerSetting);
				assertFalse("should not have overwritten showWeaveMessages option",eq); //$NON-NLS-1$
			} else {
				assertEquals("default settings should be the same for key " + key,storeDefault,compilerSetting);				 //$NON-NLS-1$
			}
		}
	}
	
	public void testRemoveValues() throws Exception {
		AJCompilerPreferencePage.setProjectDefaults(projectNode);
		AJCompilerPreferencePage.removeProjectValues(projectNode);
		String[] keys = projectNode.keys();
		assertEquals("there should be no settings",0,keys.length);		 //$NON-NLS-1$
	}

	// Checks relating to newly exposed xlint option, added in fix for bug
	// 159704
	public void testNewXlintOptions() throws Exception {
		IPreferenceStore preferenceStore = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		try {
			// Use a logger to which we have access
			TestLogger testLog = new TestLogger();
			AspectJPlugin.getDefault().setAJLogger(testLog);

			/*
			 * These projects are only used for this test, so it seems
			 * misleading and pointless to initialise them in setup.
			 */
			createPredefinedProject("ThirdPartyLibrary"); //$NON-NLS-1$
			IProject userLibraryProject = createPredefinedProject("UserLibrary"); //$NON-NLS-1$
			createPredefinedProject("UserLibraryAspects"); //$NON-NLS-1$

			// 1. Check for expected error marker - default
			checkProjectForExpectedMarker(userLibraryProject,
					IMarker.SEVERITY_ERROR, "[Xlint:cantFindType]"); //$NON-NLS-1$

			/*
			 * 2. Change AspectJ Compiler Preferences Change them in the store
			 * as this is what the code does - I guess we really want to change
			 * the preferences via the GUI, but that's on the 'To Do' list...
			 * -spyoung
			 */
			preferenceStore.setValue(AspectJPreferences.OPTION_cantFindType,
					JavaCore.WARNING);
			AJCompilerPreferencePage.initDefaults(preferenceStore);

			// Re-build from clean
			IWorkspace workspace = AspectJPlugin.getWorkspace();
			workspace.build(IncrementalProjectBuilder.CLEAN_BUILD, null);

			waitForJobsToComplete();

			// 3. Check for expected warning marker, post preferences change
			checkProjectForExpectedMarker(userLibraryProject,
					IMarker.SEVERITY_WARNING, "[Xlint:cantFindType]"); //$NON-NLS-1$

		} finally {
			// 4. Tidy up state
			preferenceStore.setValue(AspectJPreferences.OPTION_cantFindType,
					JavaCore.ERROR);
			AJCompilerPreferencePage.initDefaults(preferenceStore);
			AspectJPlugin.getDefault().setAJLogger(null);
		}
	}

	private void checkProjectForExpectedMarker(IProject project, int expectedSeverity, String searchString) throws Exception {
		
		String severityString = "UNKNOWN"; //$NON-NLS-1$
		if(expectedSeverity == IMarker.SEVERITY_ERROR) {
			severityString = "ERROR"; //$NON-NLS-1$
		} else if(expectedSeverity == IMarker.SEVERITY_WARNING) {
			severityString = "WARNING"; //$NON-NLS-1$
		}
		
		// State to be checked at end of method
		boolean markerFound = false;
		String markerMessage = ""; //$NON-NLS-1$

		// Find all markers (errors, warnings etc) for the project
		IMarker[] markers = project.findMarkers(null, true, IResource.DEPTH_INFINITE);
		
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = (IMarker)markers[i];

			/*
			 * This may seem a little odd, but the getAttribute method can return either
			 * String, Integer, Boolean or null, depending on the arg passed in.
			 * There is a method:
			 * 
			 *    public int getAttribute(String attributeName, int defaultValue);
			 *    
			 * but choice of default value affects return value, so I think the casted
			 * Integer is cleaner.
			 *     
			 * - spyoung
			 */
			Integer severity = (Integer)marker.getAttribute(IMarker.SEVERITY);
			
			if(severity.intValue() == expectedSeverity) {
				markerFound = true;
				markerMessage = (String)marker.getAttribute(IMarker.MESSAGE);
			}
		}
		
		// Should show an error by default - need to build projects first?
		assertTrue("The project did not have the expected marker of type " + severityString, markerFound); //$NON-NLS-1$
		
		boolean markerMessageContainsSearchString = markerMessage.indexOf(searchString) >= 0;
		assertTrue("'" + searchString + "' not found in marker message : " + markerMessage, markerMessageContainsSearchString);  //$NON-NLS-1$//$NON-NLS-2$
	}
	
}
