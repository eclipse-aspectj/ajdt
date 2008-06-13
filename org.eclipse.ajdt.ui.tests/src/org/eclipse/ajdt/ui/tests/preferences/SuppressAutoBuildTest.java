/*******************************************************************************
 * Copyright (c) 2008 Contributors.  All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Andrew Eisenberg
 ******************************************************************************/
package org.eclipse.ajdt.ui.tests.preferences;

import org.aspectj.org.eclipse.jdt.internal.compiler.batch.Main.Logger;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
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
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * This class tests the AJCompilerPreferencePage. 
 */
public class SuppressAutoBuildTest extends UITestCase {

	IProject project;
    TestLogger logger;
    AspectJEditor editor;
    IDocument doc;

	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = createPredefinedProject("Simple AJ Project"); //$NON-NLS-1$
		editor = (AspectJEditor) openFileInAspectJEditor(project.getFile("src/p2/Aspect.aj"), true);
		doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		logger = new TestLogger();
		AJLog.setLogger(logger);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * Want to test that the defaults being used in the AJCompilerPreferencePage
	 * (via Window > Preferences) is the same as in the CompilerPreferencePage
	 * (via right click > Preferences).
	 *
	 */
	public void testSuppressAutoBuild() throws Exception {
        
	    // set defaults
	    IPreferenceStore uiPrefs = AspectJUIPlugin.getDefault().getPreferenceStore();        
	    AJCompilerPreferencePage.initDefaults(uiPrefs);
	    assertFalse("Autobuild should not be suppressed by default.",  //$NON-NLS-1$
	            AspectJCorePreferences.isAutobuildSuppressed());
	    
	    // try to build
	    doc.set(doc.get() + " ");  //$NON-NLS-1$
	    editor.doSave(null);
	    project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        waitForJobsToComplete();
        
        // check log to make sure build occurred
        String msg = logger.getMostRecentMatchingMessage("suppressed"); //$NON-NLS-1$
        assertNull("Auto build should not have been suppressed", msg); //$NON-NLS-1$
        logger.clearLog();
        
        // set suppress build and rebuild
        AspectJCorePreferences.setAutobuildSuppressed(true);
        doc.set(doc.get() + " ");  //$NON-NLS-1$
        editor.doSave(null);
        project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        waitForJobsToComplete();

        // check log to ensure build has been suppressed
        msg = logger.getMostRecentMatchingMessage("suppressed"); //$NON-NLS-1$
        assertNotNull("Auto build should have been suppressed", msg); //$NON-NLS-1$
        logger.clearLog();
        
        // unset suppress build and rebuild
        AspectJCorePreferences.setAutobuildSuppressed(false);
        doc.set(doc.get() + " ");  //$NON-NLS-1$
        editor.doSave(null);
        project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        waitForJobsToComplete();
        
        // check log to ensure that build has occurred
        msg = logger.getMostRecentMatchingMessage("suppressed"); //$NON-NLS-1$
        assertNull("Auto build should not have been suppressed", msg); //$NON-NLS-1$
        logger.clearLog();

	}

	
}
