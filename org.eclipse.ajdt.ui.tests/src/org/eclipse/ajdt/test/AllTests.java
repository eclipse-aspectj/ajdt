/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Helen Hawkins - initial version
 *******************************************************************************/
package org.eclipse.ajdt.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ajdt.buildconfigurator.BuildConfigurationTest;
import org.eclipse.ajdt.buildconfigurator.BuildConfiguratorTest;
import org.eclipse.ajdt.buildconfigurator.ProjectBuildConfigurationTest;
import org.eclipse.ajdt.buildconfigurator.UtilTests;
import org.eclipse.ajdt.internal.builder.AJModelTest;
import org.eclipse.ajdt.internal.builder.AdviceMarkersTest;
import org.eclipse.ajdt.internal.builder.AdviceMarkersTest2;
import org.eclipse.ajdt.internal.builder.ProjectDependenciesTest;
import org.eclipse.ajdt.internal.builder.ProjectDependenciesWithJarFilesTest;
import org.eclipse.ajdt.internal.core.AJDTUtilsTest;
import org.eclipse.ajdt.internal.core.NewAspectUtilsTest;
import org.eclipse.ajdt.internal.ui.AJDTConfigSettings;
import org.eclipse.ajdt.internal.ui.actions.AddAJNatureActionTest;
import org.eclipse.ajdt.internal.ui.actions.RemoveAJNatureActionTest;
import org.eclipse.ajdt.internal.ui.ajde.ClasspathOrderTest;
import org.eclipse.ajdt.internal.ui.editor.AspectJBreakpointRulerActionTest;
import org.eclipse.ajdt.internal.ui.editor.AspectJEditorTest;
import org.eclipse.ajdt.internal.ui.editor.codeFormatting.CodeFormatTest;
import org.eclipse.ajdt.internal.ui.editor.contentassist.ContentAssistTest;
import org.eclipse.ajdt.internal.ui.editor.quickfix.AspectJQuickFixTest;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferencePageTest;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferencesTest;
import org.eclipse.ajdt.internal.ui.wizards.AspectJProjectWizardTest;
import org.eclipse.ajdt.internal.ui.wizards.export.AJCTaskTest;
import org.eclipse.ajdt.javamodel.AJCompilationUnitManagerTest;
import org.eclipse.ajdt.javamodel.AspectsConvertingParserTest;
import org.eclipse.ajdt.javamodel.elements.AJCompilationUnitTest;
import org.eclipse.ajdt.ras.PluginFFDCTest;
import org.eclipse.ajdt.test.utils.TestForPredefinedProjectsTool;
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.ajdt.ui.CodeTemplatesTest;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for ajdt.tests");
		//$JUnit-BEGIN$
		
		setupAJDTPlugin();
		
		// test the predefined project tool
		suite.addTest(new TestSuite(TestForPredefinedProjectsTool.class));
		
		// buildconfigurator tests
		suite.addTest(new TestSuite(UtilTests.class));
		suite.addTest(new TestSuite(ProjectBuildConfigurationTest.class));
		suite.addTest(new TestSuite(BuildConfigurationTest.class));
		suite.addTest(new TestSuite(BuildConfiguratorTest.class));
		
		// internal.core tests
		suite.addTest(new TestSuite(NewAspectUtilsTest.class));
		suite.addTest(new TestSuite(AJDTUtilsTest.class));

		// internal.ui.actions tests
		suite.addTest(new TestSuite(AddAJNatureActionTest.class));
		suite.addTest(new TestSuite(RemoveAJNatureActionTest.class));

		// internal.ui.preferences tests
		suite.addTest(new TestSuite(AspectJPreferencePageTest.class));
		suite.addTest(new TestSuite(AspectJPreferencesTest.class));

		// internal.ui.editor tests
		suite.addTest(new TestSuite(AspectJEditorTest.class));
		suite.addTest(new TestSuite(AspectJBreakpointRulerActionTest.class));

		// code format tests
		suite.addTest(new TestSuite(CodeFormatTest.class));
		
		// internal.ui.ajde tests
		suite.addTest(new TestSuite(ClasspathOrderTest.class));

		// internal.ui.editor.quickfix tests
		suite.addTest(new TestSuite(AspectJQuickFixTest.class));
		
		// ui tests
		suite.addTest(new TestSuite(CodeTemplatesTest.class));
		suite.addTest(new TestSuite(ContentAssistTest.class));
		
		// new aspectJ project wizard
		suite.addTest(new TestSuite(AspectJProjectWizardTest.class));
		
		// export wizard tests
		suite.addTest(new TestSuite(AJCTaskTest.class));
		
		// visualiser tests
		suite.addTest(org.eclipse.contribution.visualiser.tests.AllTests.suite());
		
		// internal.builder tests
		suite.addTest(new TestSuite(ProjectDependenciesTest.class));		
		suite.addTest(new TestSuite(ProjectDependenciesWithJarFilesTest.class));
		suite.addTest(new TestSuite(AdviceMarkersTest.class));
		suite.addTest(new TestSuite(AdviceMarkersTest2.class));
		suite.addTest(new TestSuite(AJModelTest.class));
		
		// javamodel tests
		suite.addTest(new TestSuite(AJCompilationUnitManagerTest.class));		
		suite.addTest(new TestSuite(AspectsConvertingParserTest.class));
		suite.addTest(new TestSuite(AJCompilationUnitTest.class));
		
		// ras tests
		suite.addTest(new TestSuite(PluginFFDCTest.class));		

		// xref tests
		suite.addTest(org.eclipse.contribution.xref.core.AllTests.suite());
		suite.addTest(org.eclipse.contribution.xref.ui.AllTests.suite());

		
		//$JUnit-END$
		return suite;
	}
	
	//prevents AJDTPrefWizard from popping up during tests
	private static void setupAJDTPlugin(){
		AJDTConfigSettings.disableAnalyzeAnnotations();
		Utils.blockPreferencesConfigWizard();				
	}
}
