/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.tests.performance;


import java.util.ArrayList;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ajdt.internal.core.AJDTUtils;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.text.tests.performance.EditorTestHelper;
import org.eclipse.jdt.text.tests.performance.ResourceTestHelper;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.ui.PartInitException;

/**
 * @since 3.1
 */
public class OpenAspectJEditorTest extends OpenEditorTest {

	public static class Setup extends TestSetup {

		private boolean fTearDown;

		public Setup(Test test) {
			this(test, true);
		}

		public Setup(Test test, boolean tearDown) {
			super(test);
			fTearDown= tearDown;
		}

		protected void setUp() throws Exception {
			ResourceTestHelper.copy(PREFIX + ORIG_FILE_SUFFIX, PREFIX + FILE_SUFFIX);
			ResourceTestHelper.replicate(PREFIX + FILE_SUFFIX, PREFIX, FILE_SUFFIX, WARM_UP_RUNS + MEASURED_RUNS, FILE_PREFIX, FILE_PREFIX, ResourceTestHelper.SKIP_IF_EXISTS);
		}

		protected void tearDown() throws Exception {
			if (fTearDown)
				ResourceTestHelper.delete(PREFIX, FILE_SUFFIX, WARM_UP_RUNS + MEASURED_RUNS);
		}
	}

	private static final Class THIS= OpenAspectJEditorTest.class;

	private static final String SHORT_NAME_FIRST_RUN= "Open AspectJ editor (first in session)";

	private static final String SHORT_NAME_WARM_RUN= "Open AspectJ editor (reopen)";

	private static final String SHORT_NAME_WARM_RUN_FIRST= "Open AspectJ editor (reopen first)";
	
	private static final int WARM_UP_RUNS= 10;
	
	private static final int MEASURED_RUNS= 5;
	
	private static final String PATH= "/Eclipse SWT/win32/org/eclipse/swt/graphics/";
	
	private static final String FILE_PREFIX= "TextLayout";
	
	private static final String PREFIX= "/" + PerformanceTestSetup.PROJECT + PATH + FILE_PREFIX;
	
	private static final String FILE_SUFFIX= ".aj";

	private static final String ORIG_FILE_SUFFIX= ".java";

	private static final String ORIG_LARGE_FILE= "/org.eclipse.swt/Eclipse SWT Custom Widgets/common/org/eclipse/swt/custom/StyledText.java";

	private static final String LARGE_FILE= "/org.eclipse.swt/Eclipse SWT Custom Widgets/common/org/eclipse/swt/custom/StyledText.aj";
	
	private boolean ajSetupDone = false;
	
	public OpenAspectJEditorTest() {
		super();
	}

	public OpenAspectJEditorTest(String name) {
		super(name);
	}

	public static Test suite() {
		// ensure sequence
		TestSuite suite= new TestSuite(THIS.getName());
		suite.addTest(new OpenAspectJEditorTest("testOpenFirstEditor"));
		suite.addTest(new OpenAspectJEditorTest("testOpenAspectJEditor1"));
		suite.addTest(new OpenAspectJEditorTest("testOpenAspectJEditor2"));
		suite.addTest(new OpenAspectJEditorTest("testOpenEditor3"));
		suite.addTest(new OpenAspectJEditorTest("testOpenEditor4"));
		suite.addTest(new OpenAspectJEditorTest("testOpenEditor5"));
		suite.addTest(new OpenAspectJEditorTest("testOpenEditor6"));
		return new PerformanceTestSetup(new Setup(suite));
	}
	
	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		ajSetup();
		EditorTestHelper.runEventQueue();
		setWarmUpRuns(WARM_UP_RUNS);
		setMeasuredRuns(MEASURED_RUNS);
	}
	
	private void ajSetup() throws Exception {
		if (ajSetupDone) {
			return;
		}
		Utils.blockPreferencesConfigWizard();
		AspectJPreferences.setAskPDEAutoImport(false);
		AspectJPreferences.setDoPDEAutoImport(true);
		AspectJPreferences.setPDEAutoImportConfigDone(true);
		AJDTUtils.addAspectJNature(ResourceTestHelper.getProject(PerformanceTestSetup.PROJECT));	
		ajSetupDone = true;
	}
	
	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		EditorTestHelper.closeAllEditors();
	}

	public void testOpenFirstEditor() throws Exception {
		ResourceTestHelper.copy(PREFIX + ORIG_FILE_SUFFIX, PREFIX + FILE_SUFFIX, ResourceTestHelper.SKIP_IF_EXISTS);
		PerformanceMeter performanceMeter= createPerformanceMeterForSummary(SHORT_NAME_FIRST_RUN, Dimension.ELAPSED_PROCESS); 
		measureOpenInEditor(new IFile[] { ResourceTestHelper.findFile(PREFIX + FILE_SUFFIX) }, performanceMeter, false);
	}
	
//	public static IFile[] findFiles(String prefix, String suffix, int i, int n) {
//		List files= new ArrayList(n);
//		for (int j= i; j < i + n; j++) {
//			String path= prefix + j + suffix;
//			files.add(findFile(path));
//		}
//		return (IFile[]) files.toArray(new IFile[files.size()]);
//	}
	
	private static IFile[] copyAndFindFiles(String prefix, String suffix, int i, int n) {
		List files= new ArrayList(n);
		for (int j= i; j < i + n; j++) {
			String srcPath= prefix + j + ORIG_FILE_SUFFIX;
			String path= prefix + j + suffix;
			try {
				ResourceTestHelper.copy(srcPath,path,ResourceTestHelper.SKIP_IF_EXISTS);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			files.add(ResourceTestHelper.findFile(path));
		}
		return (IFile[]) files.toArray(new IFile[files.size()]);
	}
	
	public void testOpenAspectJEditor1() throws Exception {
		measureOpenInEditor(copyAndFindFiles(PREFIX, FILE_SUFFIX, 0, getWarmUpRuns()), Performance.getDefault().getNullPerformanceMeter(), false);
		measureOpenInEditor(copyAndFindFiles(PREFIX, FILE_SUFFIX, getWarmUpRuns(), getMeasuredRuns()), createPerformanceMeter(), false);
	}
	
	public void testOpenAspectJEditor2() throws Exception {
		measureOpenInEditor(ResourceTestHelper.findFiles(PREFIX, FILE_SUFFIX, 0, getWarmUpRuns()), Performance.getDefault().getNullPerformanceMeter(), false);
		PerformanceMeter performanceMeter= createPerformanceMeterForGlobalSummary(SHORT_NAME_WARM_RUN, Dimension.ELAPSED_PROCESS); 
		measureOpenInEditor(ResourceTestHelper.findFiles(PREFIX, FILE_SUFFIX, getWarmUpRuns(), getMeasuredRuns()), performanceMeter, false);
	}
	
	public void testOpenEditor3() throws Exception {
		ResourceTestHelper.copy(ORIG_LARGE_FILE, LARGE_FILE, ResourceTestHelper.SKIP_IF_EXISTS);
		PerformanceMeter performanceMeter= createPerformanceMeterForSummary(SHORT_NAME_WARM_RUN_FIRST, Dimension.ELAPSED_PROCESS); 
		measureOpenInEditor(LARGE_FILE, true, true, performanceMeter);
	}

	public void testOpenEditor4() throws Exception {
		ResourceTestHelper.copy(ORIG_LARGE_FILE, LARGE_FILE, ResourceTestHelper.SKIP_IF_EXISTS);
		measureOpenInEditor(LARGE_FILE, false, true, createPerformanceMeter());
	}

	public void testOpenEditor5() throws Exception {
		ResourceTestHelper.copy(ORIG_LARGE_FILE, LARGE_FILE, ResourceTestHelper.SKIP_IF_EXISTS);
		measureOpenInEditor(LARGE_FILE, true, false, createPerformanceMeter());
	}

	public void testOpenEditor6() throws Exception {
		ResourceTestHelper.copy(ORIG_LARGE_FILE, LARGE_FILE, ResourceTestHelper.SKIP_IF_EXISTS);
		measureOpenInEditor(LARGE_FILE, false, false, createPerformanceMeter());
	}
	
	protected void measureOpenInEditor(String file, boolean enableFolding, boolean showOutline, PerformanceMeter performanceMeter) throws PartInitException {
		boolean shown= EditorTestHelper.isViewShown(EditorTestHelper.OUTLINE_VIEW_ID);
		try {
			EditorTestHelper.enableFolding(enableFolding);
			showOutline(showOutline);
			measureOpenInEditor(file, performanceMeter);
		} finally {
			EditorTestHelper.resetFolding();
			showOutline(shown);
		}
	}

	private boolean showOutline(boolean show) throws PartInitException {
		return EditorTestHelper.showView(EditorTestHelper.OUTLINE_VIEW_ID, show);
	}
}
