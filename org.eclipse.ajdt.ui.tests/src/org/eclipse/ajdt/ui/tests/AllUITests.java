/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Helen Hawkins - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ajdt.ui.tests.actions.AddAJNatureActionTest;
import org.eclipse.ajdt.ui.tests.actions.RemoveAJNatureActionTest;
import org.eclipse.ajdt.ui.tests.ajde.AJDTErrorHandlerTest;
import org.eclipse.ajdt.ui.tests.ajde.BuildOptionsAdapterTest;
import org.eclipse.ajdt.ui.tests.ajde.ClasspathOrderTest;
import org.eclipse.ajdt.ui.tests.ajde.ProjectPropertiesTest;
import org.eclipse.ajdt.ui.tests.ajde.UIMessageHandlerTest;
import org.eclipse.ajdt.ui.tests.builder.AdviceMarkersTest;
import org.eclipse.ajdt.ui.tests.builder.AdviceMarkersTest2;
import org.eclipse.ajdt.ui.tests.builder.AdviceMarkersTest3;
import org.eclipse.ajdt.ui.tests.builder.AdviceMarkersTest4;
import org.eclipse.ajdt.ui.tests.builder.AdviceMarkersTest5;
import org.eclipse.ajdt.ui.tests.builder.AdviceMarkersTest6;
import org.eclipse.ajdt.ui.tests.builder.AdviceMarkersTest7;
import org.eclipse.ajdt.ui.tests.builder.Bug128803Test;
import org.eclipse.ajdt.ui.tests.builder.Bug151818Test;
import org.eclipse.ajdt.ui.tests.builder.Bug243376Test;
import org.eclipse.ajdt.ui.tests.builder.BuildPathTests;
import org.eclipse.ajdt.ui.tests.builder.BuilderTest;
import org.eclipse.ajdt.ui.tests.builder.ChangingMarkersTest;
import org.eclipse.ajdt.ui.tests.builder.CustomMarkersTest;
import org.eclipse.ajdt.ui.tests.builder.ITDinSeparateProjects;
import org.eclipse.ajdt.ui.tests.builder.InpathOutFolderTest;
import org.eclipse.ajdt.ui.tests.builder.ProblemMarkerTest;
import org.eclipse.ajdt.ui.tests.builder.ProjectDependenciesTest;
import org.eclipse.ajdt.ui.tests.builder.ProjectDependenciesWithJarFilesTest;
import org.eclipse.ajdt.ui.tests.editor.AspectJBreakpointRulerActionTest;
import org.eclipse.ajdt.ui.tests.editor.AspectJEditorIconTest;
import org.eclipse.ajdt.ui.tests.editor.AspectJEditorTest;
import org.eclipse.ajdt.ui.tests.editor.codeformat.CodeFormatTest;
import org.eclipse.ajdt.ui.tests.editor.contentassist.ContentAssistTests;
import org.eclipse.ajdt.ui.tests.editor.contentassist.ContentAssistTests2;
import org.eclipse.ajdt.ui.tests.editor.quickfix.AspectJQuickFixTest;
import org.eclipse.ajdt.ui.tests.javamodel.AJCompilationUnitManagerTest;
import org.eclipse.ajdt.ui.tests.javamodel.AspectsConvertingParserTest;
import org.eclipse.ajdt.ui.tests.javamodel.Bug117327Test;
import org.eclipse.ajdt.ui.tests.javamodel.Bug154339Test;
import org.eclipse.ajdt.ui.tests.javamodel.elements.AJCompilationUnitTest;
import org.eclipse.ajdt.ui.tests.javamodel.elements.AJCompilationUnitTest2;
import org.eclipse.ajdt.ui.tests.launching.AJMainMethodSearchEngineTest;
import org.eclipse.ajdt.ui.tests.launching.LTWUtilsTest;
import org.eclipse.ajdt.ui.tests.launching.LTWUtilsTest2;
import org.eclipse.ajdt.ui.tests.lazystart.ImageDecoratorTest;
import org.eclipse.ajdt.ui.tests.model.BinaryWeavingSupportTest;
import org.eclipse.ajdt.ui.tests.preferences.AJCompilerPreferencePageTest;
import org.eclipse.ajdt.ui.tests.preferences.AJCompilerPreferencePageTest2;
import org.eclipse.ajdt.ui.tests.preferences.AJCompilerPreferencePageWorkbenchTest;
import org.eclipse.ajdt.ui.tests.preferences.AspectJFilterPreferencesTest;
import org.eclipse.ajdt.ui.tests.preferences.AspectJPreferencesTest;
import org.eclipse.ajdt.ui.tests.preferences.AspectJProjectPropertiesPageTest;
import org.eclipse.ajdt.ui.tests.preferences.Bug162211Test;
import org.eclipse.ajdt.ui.tests.ras.PluginFFDCTest;
import org.eclipse.ajdt.ui.tests.reconciling.ProblemFinderTests;
import org.eclipse.ajdt.ui.tests.reconciling.ProblemFinderTests2;
import org.eclipse.ajdt.ui.tests.reconciling.ProblemFinderTests3;
import org.eclipse.ajdt.ui.tests.reconciling.ProblemFinderTests4;
import org.eclipse.ajdt.ui.tests.testutils.SynchronizationUtils;
import org.eclipse.ajdt.ui.tests.testutils.TestForPredefinedProjectsTool;
import org.eclipse.ajdt.ui.tests.utils.AJDTUtilsTest;
import org.eclipse.ajdt.ui.tests.visualiser.AJDTContentProviderTest;
import org.eclipse.ajdt.ui.tests.wizards.AspectJProjectWizardTest;
import org.eclipse.ajdt.ui.tests.wizards.export.AJCTaskTest;
import org.eclipse.ajdt.ui.tests.wizards.export.ExportPluginTest;
import org.eclipse.ajdt.ui.tests.wizards.export.ExportProductTest;
import org.eclipse.ajdt.ui.tests.xref.XReferenceViewContentsTest;
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
		
		boolean is50 = System.getProperty("java.version").startsWith("1.5"); //$NON-NLS-1$ //$NON-NLS-2$

		// AJDT UI Tests
		suite.addTest(AllAJDTUITests.suite());
		
		// visualiser tests
		suite.addTest(org.eclipse.contribution.visualiser.tests.AllTests.suite());

		// AJDT visualiser content provider tests
		suite.addTest(new TestSuite(AJDTContentProviderTest.class));
		
		
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

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();

		// close welcome page
		IIntroPart intro = PlatformUI.getWorkbench().getIntroManager()
				.getIntro();
		if (intro != null) {
			try {
				PlatformUI.getWorkbench().getIntroManager().setIntroStandby(intro, true);
			} catch (NullPointerException npe) {
				// don't care about this
			}
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

		// open Console view
		try {
			window.getActivePage().showView("org.eclipse.ui.console.ConsoleView"); //$NON-NLS-1$
		} catch (PartInitException e1) {
		}

		waitForJobsToComplete();
		setupDone = true;
	}
	
	private static void waitForJobsToComplete() {
		SynchronizationUtils.joinBackgroudActivities();
	}

	private static boolean setupDone = false;
}
