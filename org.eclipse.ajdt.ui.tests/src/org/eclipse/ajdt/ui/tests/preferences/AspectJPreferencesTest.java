/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matt Chapman  - initial version
 *     Helen Hawkins - testing compiler options are set correctly 
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.preferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.ui.preferences.AJCompilerPreferencePage;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.ajdt.ui.tests.builder.ProjectDependenciesUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Test AspectJPreferences. In particular, there are two 
 * mechanisms through which options can be set, one via Window > Preferences
 * and the other via right click > preferences. Where appropriate, the
 * methods in BuildOptionsAdapter are tested when options are set in both
 * these ways.
 */
public class AspectJPreferencesTest extends UITestCase {

	IProject project;
	IJavaProject jp;
	IPreferenceStore prefStore;
	IEclipsePreferences projectNode;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = createPredefinedProject("Simple AJ Project");
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
	}

	public void testBuildConfigSetting() throws Exception {
		BuildConfigurator conf = BuildConfigurator.getBuildConfigurator();
		ProjectBuildConfigurator pbc = conf
				.getProjectBuildConfigurator(project);
		assertNotNull("Didn't get a project build configurator", pbc);

		// create a new configuration and activate it
		final String newconfig = "newconfig";
		BuildConfiguration bc = new BuildConfiguration(newconfig, jp, pbc);
		pbc.addBuildConfiguration(bc);
		pbc.setActiveBuildConfiguration(bc);

		waitForJobsToComplete();

		// active config should now have been written to .settings file
		IResource res = project.findMember(".settings");

		if (res.getType() != IResource.FOLDER) {
			fail(".settings must be a folder");
		}
		IFolder settings = (IFolder) res;

		final boolean[] success = new boolean[1];
		success[0] = false;
		settings.accept(new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (resource.getType() == IResource.FILE) {
					if (resource.getName().indexOf("ajdt") >= 0) {
						// found an AJDT prefs file, let's see if it mentions
						// our build config
						IFile file = (IFile) resource;
						BufferedReader input = new BufferedReader(
								new InputStreamReader(file.getContents()));
						try {
							String line = input.readLine();
							while (line != null) {
								if (line.indexOf(newconfig) >= 0) {
									success[0] = true;
								}
								line = input.readLine();
							}
							input.close();
						} catch (IOException e) {
						}
					}
				}
				return true;
			}
		});
		assertTrue(
				"Didn't find a .settings preferences file mentioning the new build configuration",
				success[0]);
	}
	
	public void testIsUsingProjectSettings() throws Exception {
		boolean b1 = AspectJPreferences.isUsingProjectSettings(project);	
		assertFalse("by default, should not be using project compiler settings",b1);
		projectNode.putBoolean(AspectJPreferences.OPTION_UseProjectSettings, true);
		b1 = AspectJPreferences.isUsingProjectSettings(project);
		assertTrue("project should now be using project compiler settings",b1);
		projectNode.putBoolean(AspectJPreferences.OPTION_UseProjectSettings, false);
		b1 = AspectJPreferences.isUsingProjectSettings(project);
		assertFalse("project should now be using workbench compiler settings",b1);
	}
	
	public void testSetUsingProjectSettings() throws Exception {
		assertFalse("by default, should not be using project compiler settings",
				AspectJPreferences.isUsingProjectSettings(project));
		AspectJPreferences.setUsingProjectSettings(project,true,true);
		assertTrue("project should now be using project compiler settings",
				AspectJPreferences.isUsingProjectSettings(project));
		AspectJPreferences.setUsingProjectSettings(project,false);
		assertFalse("project should no longer be using project compiler settings",
				AspectJPreferences.isUsingProjectSettings(project));
	}
	
	/**
	 * This test ensures that any previously set project settings are not 
	 * overwritten if say so, but they are as default.
	 * 
	 * @throws Exception
	 */
	public void testSetUsingProjectSettings2() throws Exception {
		
		assertFalse("by default, should not be using project compiler settings",
				AspectJPreferences.isUsingProjectSettings(project));
		
		projectNode.putBoolean(AspectJPreferences.OPTION_WeaveMessages,true);
		
		AspectJPreferences.setUsingProjectSettings(project,true,false);
		boolean b1 = projectNode.getBoolean(AspectJPreferences.OPTION_WeaveMessages,false);
		assertTrue("should not have reset OPTION_WeaveMessages to default",b1);
		
		AspectJPreferences.setUsingProjectSettings(project,true);
		b1 = projectNode.getBoolean(AspectJPreferences.OPTION_WeaveMessages,false);
		assertFalse("should have reset OPTION_WeaveMessages to default",b1);
		
		AspectJPreferences.setUsingProjectSettings(project,false);
		boolean threwExpectedException = false;
		try {
			projectNode.keys();
		} catch (IllegalStateException e) {
			// we expect this exception because setting "use project settings" to false
			// should clear all keys and consequently throw this exception.
			threwExpectedException = true;
		}
		assertEquals("there should be no project settings",true,threwExpectedException);
	}
	
	public void testGetShowWeaveMessagesOptionViaWorkbenchPreferences() throws Exception {
		assertFalse("default setting is not to show weave info",
				AspectJPreferences.getShowWeaveMessagesOption(project));
		// know that when "show weave messages" is selected in the preference
		// page, then set this store value to true because use the
		// getSelection() call on the button to see whether it
		// is selected (weave messages on) or not (weave messages off)
		prefStore.setValue(AspectJPreferences.OPTION_WeaveMessages,true);
		assertTrue("have chosen to show weave info",
				AspectJPreferences.getShowWeaveMessagesOption(project));
		
		prefStore.setValue(AspectJPreferences.OPTION_WeaveMessages,false);
		assertFalse("have chosen not to show weave info",
				AspectJPreferences.getShowWeaveMessagesOption(project));
	}
	
	public void testGetShowWeaveMessagesOptionViaProjectPreferences() throws Exception {
		AspectJPreferences.setUsingProjectSettings(project,true,true);

		assertFalse("default setting is not to show weave info",
				AspectJPreferences.getShowWeaveMessagesOption(project));
		// know that when "show weave messages" is selected in the preference
		// page, then set this project node value to true because use the
		// following :
		// 		String stringValue = curr.getSelection() ? "true" : "false";
		// to see whether it is selected (weave messages on) or not (weave messages off)
		projectNode.put(AspectJPreferences.OPTION_WeaveMessages,"true");
		assertTrue("have chosen to show weave info",
				AspectJPreferences.getShowWeaveMessagesOption(project));
		
		projectNode.put(AspectJPreferences.OPTION_WeaveMessages,"false");
		assertFalse("have chosen not to show weave info",
				AspectJPreferences.getShowWeaveMessagesOption(project));
	}

	public void testGetIncrementalOptionViaWorkbenchPreferences() throws Exception {
		assertTrue("default setting is use incremental compilation",
				AspectJPreferences.getIncrementalOption(project));
		// know that when "show weave messages" is selected in the preference
		// page, then set this store value to true because use the
		// getSelection() call on the button to see whether it
		// is selected (weave messages on) or not (weave messages off)
		prefStore.setValue(AspectJPreferences.OPTION_Incremental,false);
		assertFalse("have chosen not to use incremental compilation",
				AspectJPreferences.getIncrementalOption(project));
		
		prefStore.setValue(AspectJPreferences.OPTION_Incremental,true);
		assertTrue("have chosen to use incremental compilation",
				AspectJPreferences.getIncrementalOption(project));
	}
	
	public void testGetIncrementalOptionViaProjectPreferences() throws Exception {
		AspectJPreferences.setUsingProjectSettings(project,true,true);

		assertTrue("default setting is use incremental compilation",
				AspectJPreferences.getIncrementalOption(project));
		// know that when "show weave messages" is selected in the preference
		// page, then set this project node value to true because use the
		// following :
		// 		String stringValue = curr.getSelection() ? "true" : "false";
		// to see whether it is selected (weave messages on) or not (weave messages off)
		projectNode.put(AspectJPreferences.OPTION_Incremental,"false");
		assertFalse("have chosen not to use incremental compilation",
				AspectJPreferences.getIncrementalOption(project));
		
		projectNode.put(AspectJPreferences.OPTION_Incremental,"true");
		assertTrue("have chosen to use incremental compilation",
				AspectJPreferences.getIncrementalOption(project));
	}
	
	public void testGetBuildASMOptionViaWorkbenchPreferences() throws Exception {
		assertTrue("default setting is to build ASM",
				AspectJPreferences.getBuildASMOption(project));
		// know that when "show weave messages" is selected in the preference
		// page, then set this store value to true because use the
		// getSelection() call on the button to see whether it
		// is selected (weave messages on) or not (weave messages off)
		prefStore.setValue(AspectJPreferences.OPTION_BuildASM,false);
		assertFalse("have chosen not to build ASM",
				AspectJPreferences.getBuildASMOption(project));
		
		prefStore.setValue(AspectJPreferences.OPTION_BuildASM,true);
		assertTrue("have chosen to build ASM",
				AspectJPreferences.getBuildASMOption(project));
	}
	
	public void testGetBuildASMOptionViaProjectPreferences() throws Exception {
		AspectJPreferences.setUsingProjectSettings(project,true,true);

		assertTrue("default setting is to build ASM",
				AspectJPreferences.getBuildASMOption(project));
		// know that when "show weave messages" is selected in the preference
		// page, then set this project node value to true because use the
		// following :
		// 		String stringValue = curr.getSelection() ? "true" : "false";
		// to see whether it is selected (weave messages on) or not (weave messages off)
		projectNode.put(AspectJPreferences.OPTION_BuildASM,"false");
		assertFalse("have chosen not to build ASM",
				AspectJPreferences.getBuildASMOption(project));
		
		projectNode.put(AspectJPreferences.OPTION_BuildASM,"true");
		assertTrue("have chosen to build ASM",
				AspectJPreferences.getBuildASMOption(project));
	}
	
	public void testGetAdvancedOptionViaWorkbenchPreferences() throws Exception {
		assertFalse("should not be using project settings",
				AspectJPreferences.isUsingProjectSettings(project));
		assertEquals("should have no advanced options set",
				" ",AspectJPreferences.getAdvancedOptions(project));
		
		prefStore.setValue(AspectJPreferences.OPTION_NoWeave,true);
		assertEquals("should have set -XnoWeave option",
				" -XnoWeave ", 
				AspectJPreferences.getAdvancedOptions(project));
		
		prefStore.setValue(AspectJPreferences.OPTION_XSerializableAspects,true);
		assertEquals("should have set -XSerializableAspects option",
				" -XnoWeave -XserializableAspects ", 
				AspectJPreferences.getAdvancedOptions(project));
		
		prefStore.setValue(AspectJPreferences.OPTION_XLazyThisJoinPoint,true);
		assertEquals("should have set -XlazyTjp option",
				" -XnoWeave -XserializableAspects -XlazyTjp ", 
				AspectJPreferences.getAdvancedOptions(project));
		
		prefStore.setValue(AspectJPreferences.OPTION_NoWeave,false);
		prefStore.setValue(AspectJPreferences.OPTION_XSerializableAspects,false);
		prefStore.setValue(AspectJPreferences.OPTION_XLazyThisJoinPoint,false);
		assertEquals("should have no advanced options set",
				" ",AspectJPreferences.getAdvancedOptions(project));
		
		prefStore.setValue(AspectJPreferences.OPTION_XNoInline,true);
		assertEquals("should have set -XnoInline e option",
				" -XnoInline ", 
				AspectJPreferences.getAdvancedOptions(project));
		
		prefStore.setValue(AspectJPreferences.OPTION_XReweavable,true);
		assertEquals("should have set -Xreweavable option",
				" -XnoInline -Xreweavable ", 
				AspectJPreferences.getAdvancedOptions(project));
		
		prefStore.setValue(AspectJPreferences.OPTION_XReweavable,false);
		prefStore.setValue(AspectJPreferences.OPTION_XReweavableCompress,true);
		assertEquals("should have set -Xreweavable:compress option",
				" -XnoInline -Xreweavable:compress ", 
				AspectJPreferences.getAdvancedOptions(project));

		prefStore.setValue(AspectJPreferences.OPTION_XNoInline,false);
		prefStore.setValue(AspectJPreferences.OPTION_XReweavableCompress,false);
		assertEquals("should have no advanced options set",
				" ",AspectJPreferences.getAdvancedOptions(project));

	}
	
	public void testGetAdvancedOptionViaProjectPreferences() throws Exception {
		AspectJPreferences.setUsingProjectSettings(project,true);
		assertTrue("should be using project settings",
				AspectJPreferences.isUsingProjectSettings(project));
		assertEquals("should have no advanced options set",
				" ",AspectJPreferences.getAdvancedOptions(project));
		
		projectNode.put(AspectJPreferences.OPTION_NoWeave,"true");
		assertEquals("should have set -XnoWeave option",
				" -XnoWeave ", 
				AspectJPreferences.getAdvancedOptions(project));
		
		projectNode.put(AspectJPreferences.OPTION_XSerializableAspects,"true")	;	
		assertEquals("should have set -XSerializableAspects option",
				" -XnoWeave -XserializableAspects ", 
				AspectJPreferences.getAdvancedOptions(project));
		
		projectNode.put(AspectJPreferences.OPTION_XLazyThisJoinPoint,"true");				
		assertEquals("should have set -XlazyTjp option",
				" -XnoWeave -XserializableAspects -XlazyTjp ", 
				AspectJPreferences.getAdvancedOptions(project));
		
		projectNode.put(AspectJPreferences.OPTION_NoWeave,"false");
		projectNode.put(AspectJPreferences.OPTION_XSerializableAspects,"false")	;	
		projectNode.put(AspectJPreferences.OPTION_XLazyThisJoinPoint,"false")	;			
		assertEquals("should have no advanced options set",
				" ",AspectJPreferences.getAdvancedOptions(project));
		
		projectNode.put(AspectJPreferences.OPTION_XNoInline,"true")	;			
		assertEquals("should have set -XnoInline option",
				" -XnoInline ", 
				AspectJPreferences.getAdvancedOptions(project));
		
		projectNode.put(AspectJPreferences.OPTION_XReweavable,"true")	;			
		assertEquals("should have set -Xreweavable option",
				" -XnoInline -Xreweavable ", 
				AspectJPreferences.getAdvancedOptions(project));
		
		projectNode.put(AspectJPreferences.OPTION_XReweavable,"false")	;			
		projectNode.put(AspectJPreferences.OPTION_XReweavableCompress,"true")	;			
		assertEquals("should have set -Xreweavable:compress option",
				" -XnoInline -Xreweavable:compress ", 
				AspectJPreferences.getAdvancedOptions(project));
	
		AspectJPreferences.setUsingProjectSettings(project,false);
		assertFalse("should not be using project settings",
				AspectJPreferences.isUsingProjectSettings(project));
		assertEquals("should have no advanced options set",
				" ",AspectJPreferences.getAdvancedOptions(project));
	}

	public void testGetXLintOptions() throws Exception {
		String lint = AspectJPreferences.getLintOptions(project);
		int ind = lint.indexOf("-Xlintfile");
		if (ind == -1) {
			fail("Didn't find -Xlintfile in string returned from AspectJPreferences.getLintOptions(). Got: "+lint);
		}
		int ind2 = lint.indexOf('\"',ind);
		if (ind2 == -1) {
			fail("Didn't find start quote in string returned from AspectJPreferences.getLintOptions(). Got: "+lint);
		}
		int ind3 = lint.indexOf('\"',ind2+1);
		if (ind3 == -1) {
			fail("Didn't find end quote in string returned from AspectJPreferences.getLintOptions(). Got: "+lint);
		}
		String fileName = lint.substring(ind2+1,ind3);
		
		// check the file exists
		File file = new File(fileName);
		assertTrue("Xlintfile does not exist: "+file,file.exists());
		
		// now try to read from it and check for typeNotExposedToWeaver=warning
		boolean gotWarning = checkXlintOption(file,"typeNotExposedToWeaver","warning");
		assertTrue("Did not find typeNotExpostedToWeaver entry set to warning",gotWarning);

		boolean isProjectSettings = AspectJPreferences.isUsingProjectSettings(project);
		String original = prefStore.getString(AspectJPreferences.OPTION_ReportTypeNotExposedToWeaver);
		try {
			// change option to ignore
			AspectJPreferences.setUsingProjectSettings(project,false);
			prefStore.setValue(AspectJPreferences.OPTION_ReportTypeNotExposedToWeaver,JavaCore.IGNORE);
		
			// recheck
			boolean gotIgnore = checkXlintOption(file,"typeNotExposedToWeaver","ignore");
			assertTrue("Did not find typeNotExpostedToWeaver entry set to ignore",gotWarning);
		} finally {
			// restore settings
			AspectJPreferences.setUsingProjectSettings(project,isProjectSettings);
			prefStore.setValue(AspectJPreferences.OPTION_ReportTypeNotExposedToWeaver,original);		
		}
	}
	
	public void testSetCompilerOptions() throws Exception {
	    assertFalse("project shouldn't have any error markers",
	            ProjectDependenciesUtils.projectIsMarkedWithError(project,null));
	    AspectJPreferences.setCompilerOptions(project,"blah");
	    project.build(IncrementalProjectBuilder.FULL_BUILD, null);
	    waitForJobsToComplete();
	    assertTrue("build should fail because can't understand compiler options",
	            ProjectDependenciesUtils.projectIsMarkedWithError(project,null));
	    AspectJPreferences.setCompilerOptions(project,"");
	    project.build(IncrementalProjectBuilder.FULL_BUILD, null);
	    waitForJobsToComplete();
	    assertFalse("project shouldn't have any error markers",
	            ProjectDependenciesUtils.projectIsMarkedWithError(project,null));    
	}
	
	public void testGetCompilerOptions() throws Exception {
	    AspectJPreferences.setCompilerOptions(project,"blah");
	    String compilerOptions = AspectJPreferences.getCompilerOptions(project);
	    assertEquals("should have \"blah\" as compiler options, instead has " + compilerOptions,
	            "blah",
	            compilerOptions);
	    AspectJPreferences.setCompilerOptions(project,"");
	    compilerOptions = AspectJPreferences.getCompilerOptions(project);
	    assertEquals("should have \" \" as compiler options, instead has " + compilerOptions,
	            "",
	            compilerOptions);
	}
	
	
	private boolean checkXlintOption(File file, String option, String value) throws Exception {
		boolean gotValue = false;
		BufferedReader is = new BufferedReader(new FileReader(file));
		String line = is.readLine();
		while (line!=null) {
			if (line.indexOf(option) != -1) {
				if (line.indexOf(value) != -1) {
					gotValue = true;
				}
			}
			line = is.readLine();
		}
		return gotValue;
	}
}
