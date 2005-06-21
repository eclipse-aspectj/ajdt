/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.ui.tests.ajde;

import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.preferences.AJCompilerPreferencePage;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.testutils.Utils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This class tests BuildOptionsAdapter. In particular, there are two 
 * mechanisms through which options can be set, one via Window > Preferences
 * and the other via right click > preferences. Where appropriate, the
 * methods in BuildOptionsAdapter are tested when options are set in both
 * these ways.
 */
public class BuildOptionsAdapterTest extends TestCase {

	IProject project;
	IJavaProject jp;
	IPreferenceStore prefStore;
	IEclipsePreferences projectNode;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = Utils.createPredefinedProject("Simple AJ Project");
		jp = JavaCore.create(project);

		prefStore = AspectJUIPlugin.getDefault().getPreferenceStore();
		AJCompilerPreferencePage.initDefaults(prefStore);
		
		IScopeContext projectScope = new ProjectScope(project);
		projectNode = projectScope.getNode(AspectJPlugin.PLUGIN_ID);

	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		prefStore = null;
		projectNode = null;
		Utils.deleteProject(project);
	}

	public void testGetNonStandardOptionsViaWorkbenchPreferences() throws Exception {
		String nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		String[] nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should only have -Xlintfile option set",2,nonStandardOptions.length);
		assertEquals("first element should be -Xlintfile","-Xlintfile",nonStandardOptions[0]);
		
		prefStore.setValue(AspectJPreferences.OPTION_NoWeave,true);
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should have set -XnoWeave option","-XnoWeave", nonStandardOptions[2]);
		
		prefStore.setValue(AspectJPreferences.OPTION_XSerializableAspects,true);
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should have set -XserializableAspects option","-XserializableAspects", 
				nonStandardOptions[3]);
		
		prefStore.setValue(AspectJPreferences.OPTION_XLazyThisJoinPoint,true);
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should have set -XlazyTjp option","-XlazyTjp", 
				nonStandardOptions[4]);
		
		prefStore.setValue(AspectJPreferences.OPTION_NoWeave,false);
		prefStore.setValue(AspectJPreferences.OPTION_XSerializableAspects,false);
		prefStore.setValue(AspectJPreferences.OPTION_XLazyThisJoinPoint,false);
		nonStandardOptions = disectOptions(AspectJUIPlugin.getDefault().
				getAjdtBuildOptionsAdapter().getNonStandardOptions());
		assertEquals("should only have -Xlintfile option set",2,nonStandardOptions.length);
		
		prefStore.setValue(AspectJPreferences.OPTION_XNoInline,true);
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should have set -XnoInline option","-XnoInline", 
				nonStandardOptions[2]);
		
		prefStore.setValue(AspectJPreferences.OPTION_XReweavable,true);
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should have set -Xreweavable","-Xreweavable", 
				nonStandardOptions[3]);
		
		prefStore.setValue(AspectJPreferences.OPTION_XReweavable,false);
		prefStore.setValue(AspectJPreferences.OPTION_XReweavableCompress,true);
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should have set -Xreweavable:compress","-Xreweavable:compress", 
				nonStandardOptions[3]);
		
		prefStore.setValue(AspectJPreferences.OPTION_XNoInline,false);
		prefStore.setValue(AspectJPreferences.OPTION_XReweavableCompress,false);
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should only have -Xlintfile option set",2,nonStandardOptions.length);
		
		prefStore.setValue(AspectJPreferences.OPTION_WeaveMessages,true);
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should have set -showWeaveInfo","-showWeaveInfo", 
				nonStandardOptions[2]);

		prefStore.setValue(AspectJPreferences.OPTION_WeaveMessages,false);
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should only have -Xlintfile option set",2,nonStandardOptions.length);

	}

	public void testGetNonStandardOptionsViaProjectPreferences() throws Exception {
		AspectJPreferences.setUsingProjectSettings(project,true);
		assertTrue("should be using project settings",
				AspectJPreferences.isUsingProjectSettings(project));
		
		String nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		String[] nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should only have -Xlintfile option set",2,nonStandardOptions.length);
		assertEquals("first element should be -Xlintfile","-Xlintfile",nonStandardOptions[0]);
		
		projectNode.put(AspectJPreferences.OPTION_NoWeave,"true");
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should have set -XnoWeave option","-XnoWeave", nonStandardOptions[2]);
		
		projectNode.put(AspectJPreferences.OPTION_XSerializableAspects,"true");
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should have set -XserializableAspects option","-XserializableAspects", 
				nonStandardOptions[3]);
		
		projectNode.put(AspectJPreferences.OPTION_XLazyThisJoinPoint,"true");
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should have set -XlazyTjp option","-XlazyTjp", 
				nonStandardOptions[4]);
		
		projectNode.put(AspectJPreferences.OPTION_NoWeave,"false");
		projectNode.put(AspectJPreferences.OPTION_XSerializableAspects,"false");
		projectNode.put(AspectJPreferences.OPTION_XLazyThisJoinPoint,"false");
		nonStandardOptions = disectOptions(AspectJUIPlugin.getDefault().
				getAjdtBuildOptionsAdapter().getNonStandardOptions());
		assertEquals("should only have -Xlintfile option set",2,nonStandardOptions.length);
		
		projectNode.put(AspectJPreferences.OPTION_XNoInline,"true");
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should have set -XnoInline option","-XnoInline", 
				nonStandardOptions[2]);
		
		projectNode.put(AspectJPreferences.OPTION_XReweavable,"true");
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should have set -Xreweavable","-Xreweavable", 
				nonStandardOptions[3]);
		
		projectNode.put(AspectJPreferences.OPTION_XReweavable,"false");
		projectNode.put(AspectJPreferences.OPTION_XReweavableCompress,"true");
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should have set -Xreweavable:compress","-Xreweavable:compress", 
				nonStandardOptions[3]);
		
		projectNode.put(AspectJPreferences.OPTION_XNoInline,"false");
		projectNode.put(AspectJPreferences.OPTION_XReweavableCompress,"false");
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should only have -Xlintfile option set",2,nonStandardOptions.length);
		
		projectNode.put(AspectJPreferences.OPTION_WeaveMessages,"true");
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should have set -showWeaveInfo","-showWeaveInfo", 
				nonStandardOptions[2]);

		projectNode.put(AspectJPreferences.OPTION_WeaveMessages,"false");
		nonStandard = AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getNonStandardOptions();
		nonStandardOptions = disectOptions(nonStandard);
		assertEquals("should only have -Xlintfile option set",2,nonStandardOptions.length);
		
		AspectJPreferences.setUsingProjectSettings(project,false);

	}

	public void testGetIncrementalModeViaWorkbenchPreferences() throws Exception {
		assertTrue("default setting is use incremental",
				AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getIncrementalMode());
		// know that when "use incremental" is selected in the preference
		// page, then set this store value to true because use the
		// getSelection() call on the button to see whether it
		// is selected (weave messages on) or not (weave messages off)
		prefStore.setValue(AspectJPreferences.OPTION_Incremental,false);
		assertFalse("have chosen not to use incremental building",
				AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getIncrementalMode());
		
		prefStore.setValue(AspectJPreferences.OPTION_Incremental,true);
		assertTrue("have chosen to use incremental building",
				AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getIncrementalMode());
	}

	public void testGetIncrementalModeViaProjectPreferences() {
		AspectJPreferences.setUsingProjectSettings(project,true);

		assertTrue("default setting is use incremental",
				AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getIncrementalMode());
		// know that when "use incremental" is selected in the preference
		// page, then set this store value to true because use the
		// following :
		// 		String stringValue = curr.getSelection() ? "true" : "false";
		// to see whether it is selected (weave messages on) or not (weave messages off)
		projectNode.put(AspectJPreferences.OPTION_Incremental,"false");
		assertFalse("have chosen not to use incremental buildino",
				AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getIncrementalMode());
		
		projectNode.put(AspectJPreferences.OPTION_Incremental,"true");
		assertTrue("have chosen to use incremental building",
				AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getIncrementalMode());
	}

	/**
	 * There are two ways that this can be set: one via Window > Preferences
	 * and one via right click > preferences. In both these cases we need to
	 * know that the option is being passed through. Therefore, in this test
	 * are following the mechanism used by AJCompilerPreferencePage, i.e. using
	 * the preference store
	 */
	public void testGetShowWeaveMessagesViaWorkbenchPreferences() throws Exception {
		assertFalse("default setting is not to show weave info",
				AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getShowWeaveMessages());
		// know that when "show weave messages" is selected in the preference
		// page, then set this store value to true because use the
		// getSelection() call on the button to see whether it
		// is selected (weave messages on) or not (weave messages off)
		prefStore.setValue(AspectJPreferences.OPTION_WeaveMessages,true);
		assertTrue("have chosen to show weave info",
				AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getShowWeaveMessages());
		
		prefStore.setValue(AspectJPreferences.OPTION_WeaveMessages,false);
		assertFalse("have chosen not to show weave info",
				AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getShowWeaveMessages());
	}
	
	/**
	 * There are two ways that this can be set: one via Window > Preferences
	 * and one via right click > preferences. In both these cases we need to
	 * know that the option is being passed through. Therefore, in this test
	 * are following the mechanism used by CompilerPropertyPage i.e. using 
	 * the projectNode
	 */
	public void testGetShowWeaveMessagesViaProjectPreferences() throws Exception {
		AspectJPreferences.setUsingProjectSettings(project,true);

		assertFalse("default setting is not to show weave info",
				AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getShowWeaveMessages());
		// know that when "show weave messages" is selected in the preference
		// page, then set this store value to true because use the
		// following :
		// 		String stringValue = curr.getSelection() ? "true" : "false";
		// to see whether it is selected (weave messages on) or not (weave messages off)
		projectNode.put(AspectJPreferences.OPTION_WeaveMessages,"true");
		assertTrue("have chosen to show weave info",
				AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getShowWeaveMessages());
		
		projectNode.put(AspectJPreferences.OPTION_WeaveMessages,"false");
		assertFalse("have chosen not to show weave info",
				AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getShowWeaveMessages());
	}

	private String[] disectOptions(String nonStandardOptions) {
		StringTokenizer st = new StringTokenizer(nonStandardOptions," ");
		String[] options = new String[st.countTokens()];
		int counter = 0;
		while (st.hasMoreTokens()) {
			options[counter] = st.nextToken();
			counter++;
		}
		return options;
	}
	
}
