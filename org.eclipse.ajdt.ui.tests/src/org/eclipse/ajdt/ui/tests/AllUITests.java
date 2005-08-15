/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Helen Hawkins - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ajdt.internal.ui.AJDTConfigSettings;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.tests.actions.AddAJNatureActionTest;
import org.eclipse.ajdt.ui.tests.actions.ConvertToAJProjectTest;
import org.eclipse.ajdt.ui.tests.actions.RemoveAJNatureActionTest;
import org.eclipse.ajdt.ui.tests.ajde.BuildOptionsAdapterTest;
import org.eclipse.ajdt.ui.tests.ajde.ClasspathOrderTest;
import org.eclipse.ajdt.ui.tests.ajde.ProjectPropertiesTest;
import org.eclipse.ajdt.ui.tests.buildconfig.BuildConfigurationTest;
import org.eclipse.ajdt.ui.tests.buildconfig.BuildConfiguratorTest;
import org.eclipse.ajdt.ui.tests.buildconfig.ProjectBuildConfigurationTest;
import org.eclipse.ajdt.ui.tests.buildconfig.ProjectBuildConfigurationTest2;
import org.eclipse.ajdt.ui.tests.buildconfig.UtilTests;
import org.eclipse.ajdt.ui.tests.builder.AdviceMarkerTest3;
import org.eclipse.ajdt.ui.tests.builder.AdviceMarkersTest;
import org.eclipse.ajdt.ui.tests.builder.AdviceMarkersTest2;
import org.eclipse.ajdt.ui.tests.builder.BuilderTest;
import org.eclipse.ajdt.ui.tests.builder.ProblemMarkerTest;
import org.eclipse.ajdt.ui.tests.builder.ProjectDependenciesTest;
import org.eclipse.ajdt.ui.tests.builder.ProjectDependenciesWithJarFilesTest;
import org.eclipse.ajdt.ui.tests.editor.AspectJBreakpointRulerActionTest;
import org.eclipse.ajdt.ui.tests.editor.AspectJEditorIconTest;
import org.eclipse.ajdt.ui.tests.editor.AspectJEditorTest;
import org.eclipse.ajdt.ui.tests.editor.codeformat.CodeFormatTest;
import org.eclipse.ajdt.ui.tests.editor.contentassist.ContentAssistTest;
import org.eclipse.ajdt.ui.tests.editor.quickfix.AspectJQuickFixTest;
import org.eclipse.ajdt.ui.tests.javamodel.AJCompilationUnitManagerTest;
import org.eclipse.ajdt.ui.tests.javamodel.AspectsConvertingParserTest;
import org.eclipse.ajdt.ui.tests.javamodel.elements.AJCompilationUnitTest;
import org.eclipse.ajdt.ui.tests.javamodel.elements.AJCompilationUnitTest2;
import org.eclipse.ajdt.ui.tests.launching.AJMainMethodSearchEngineTest;
import org.eclipse.ajdt.ui.tests.preferences.AspectJFilterPreferencesTest;
import org.eclipse.ajdt.ui.tests.preferences.AspectJPreferencePageTest;
import org.eclipse.ajdt.ui.tests.preferences.AspectJPreferencesTest;
import org.eclipse.ajdt.ui.tests.ras.PluginFFDCTest;
import org.eclipse.ajdt.ui.tests.testutils.TestForPredefinedProjectsTool;
import org.eclipse.ajdt.ui.tests.testutils.Utils;
import org.eclipse.ajdt.ui.tests.utils.AJDTUtilsTest;
import org.eclipse.ajdt.ui.tests.utils.NewAspectUtilsTest;
import org.eclipse.ajdt.ui.tests.wizards.AspectJProjectWizardTest;
import org.eclipse.ajdt.ui.tests.wizards.BuildConfigurationFileWizardTest;
import org.eclipse.ajdt.ui.tests.wizards.export.AJCTaskTest;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.intro.IIntroPart;

public class AllUITests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllUITests.class.getName());
		//$JUnit-BEGIN$
		
		setupAJDTPlugin();
		
		// all tests from the core tests plugin
		//suite.addTest(AllAJDTCoreTests.suite());
		
		suite.addTest(new TestSuite(NLSWarningsTest.class));
		suite.addTest(new TestSuite(VerificationTest.class));
		suite.addTest(new TestSuite(Bug106813Test.class));
		
		
		// test the predefined project tool
		suite.addTest(new TestSuite(TestForPredefinedProjectsTool.class));
		
		// buildconfigurator tests
		suite.addTest(new TestSuite(UtilTests.class));
		suite.addTest(new TestSuite(ProjectBuildConfigurationTest.class));
		suite.addTest(new TestSuite(ProjectBuildConfigurationTest2.class));
		suite.addTest(new TestSuite(BuildConfigurationTest.class));
		suite.addTest(new TestSuite(BuildConfiguratorTest.class));
		
		// internal.core tests
		suite.addTest(new TestSuite(NewAspectUtilsTest.class));
		suite.addTest(new TestSuite(AJDTUtilsTest.class));

		// internal.ui tests
		suite.addTest(new TestSuite(CompilerPropertyPageTest.class));

		// internal.ui.actions tests
		suite.addTest(new TestSuite(AddAJNatureActionTest.class));
		suite.addTest(new TestSuite(RemoveAJNatureActionTest.class));
		suite.addTest(new TestSuite(ConvertToAJProjectTest.class));

		// internal.ui.preferences tests
		suite.addTest(new TestSuite(AspectJPreferencePageTest.class));
		suite.addTest(new TestSuite(AspectJPreferencesTest.class));
		suite.addTest(new TestSuite(AspectJFilterPreferencesTest.class));

		// internal.ui.editor tests
		suite.addTest(new TestSuite(AspectJEditorTest.class));
		suite.addTest(new TestSuite(AspectJBreakpointRulerActionTest.class));
		suite.addTest(new TestSuite(AspectJEditorIconTest.class));
		
		// code format tests
		suite.addTest(new TestSuite(CodeFormatTest.class));
		
		// internal.ui.ajde tests
		suite.addTest(new TestSuite(BuildOptionsAdapterTest.class));
		suite.addTest(new TestSuite(ClasspathOrderTest.class));
		suite.addTest(new TestSuite(ProjectPropertiesTest.class));

		// internal.ui.editor.quickfix tests
		suite.addTest(new TestSuite(AspectJQuickFixTest.class));
		
		suite.addTest(new TestSuite(AJMainMethodSearchEngineTest.class));
		
		// ui tests
		suite.addTest(new TestSuite(CodeTemplatesTest.class));
		suite.addTest(new TestSuite(ContentAssistTest.class));
		
		// new aspectJ project wizard
		suite.addTest(new TestSuite(AspectJProjectWizardTest.class));
		
		// new build configuration wizard
		suite.addTest(new TestSuite(BuildConfigurationFileWizardTest.class));
		
		// export wizard tests
		suite.addTest(new TestSuite(AJCTaskTest.class));
				
		// internal.builder tests
		suite.addTest(new TestSuite(ProjectDependenciesTest.class));		
		suite.addTest(new TestSuite(ProjectDependenciesWithJarFilesTest.class));
		suite.addTest(new TestSuite(AdviceMarkersTest.class));
		suite.addTest(new TestSuite(AdviceMarkersTest2.class));
		suite.addTest(new TestSuite(AdviceMarkerTest3.class));
		suite.addTest(new TestSuite(ProblemMarkerTest.class));
		suite.addTest(new TestSuite(BuilderTest.class));
		
		// javamodel tests
		suite.addTest(new TestSuite(AJCompilationUnitManagerTest.class));		
		suite.addTest(new TestSuite(AspectsConvertingParserTest.class));
		suite.addTest(new TestSuite(AJCompilationUnitTest.class));
		suite.addTest(new TestSuite(AJCompilationUnitTest2.class));
		
		// ras tests
		suite.addTest(new TestSuite(PluginFFDCTest.class));		

		// xref tests
		suite.addTest(org.eclipse.contribution.xref.core.tests.AllTests.suite());
		suite.addTest(org.eclipse.contribution.xref.ui.tests.AllTests.suite());

		// visualiser tests
		suite.addTest(org.eclipse.contribution.visualiser.tests.AllTests.suite());

		//$JUnit-END$
		return suite;
	}
		
	/**
	 * Prevents AJDTPrefWizard from popping up during tests and simulates normal
	 * usage by closing the welcome page, and opening the java perspective
	 */
	public static synchronized void setupAJDTPlugin() {
		if (setupDone) {
			return;
		}
		AJDTConfigSettings.disableAnalyzeAnnotations(true);

		AspectJPreferences.setAutoBuilderMigrationEnabled(true);
		AspectJPreferences.setAutoBuilderMigrationRemoveOldBuilder(true);
		
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();

		// close welcome page
		IIntroPart intro = PlatformUI.getWorkbench().getIntroManager()
				.getIntro();
		if (intro != null) {
			PlatformUI.getWorkbench().getIntroManager().closeIntro(intro);
		}

		// open Java perspective
		try {
			PlatformUI.getWorkbench().showPerspective(JavaUI.ID_PERSPECTIVE,
					window);
		} catch (WorkbenchException e) {
		}

		// open Cross Ref view
		try {
			window.getActivePage().showView(XReferenceView.ID);
		} catch (PartInitException e1) {
		}
		
		Utils.waitForJobsToComplete();
		setupDone = true;
	}

	private static boolean setupDone = false;
}
