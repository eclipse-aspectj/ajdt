/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version (bug 148190)
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.ajde;

import org.aspectj.ajde.core.IBuildMessageHandler;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.ajde.AJDTErrorHandler;
import org.eclipse.ajdt.internal.ui.ajde.UIMessageHandler;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaModelMarker;

/**
 * Tests the ErrorHandler class
 */
public class UIMessageHandlerTest extends UITestCase {

	/**
	 * For a warning expect a problem marker to be created
	 */
//	public void testHandleWarning() throws Exception {
//		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
//		IBuildMessageHandler handler = AspectJPlugin.getDefault()
//				.getCompilerFactory().getCompilerForProject(project).getMessageHandler();
//
//		IMessage msg = new Message("fake warning", IMessage.WARNING, null, null); //$NON-NLS-1$
//		handler.handleMessage(msg);
//		((UIMessageHandler)AspectJPlugin.getDefault().getCompilerFactory()
//				.getCompilerForProject(project).getMessageHandler()).showOutstandingProblems(project);
//
//		waitForJobsToComplete();
//		IMarker[] markers = project.findMarkers(
//				IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true,
//				IResource.DEPTH_INFINITE);
//		boolean foundFakeWarning = false;
//		for (int i = 0; i < markers.length; i++) {
//			IMarker marker = markers[i];
//			if (marker.getAttribute(IMarker.MESSAGE).equals("fake warning")) { //$NON-NLS-1$
//				foundFakeWarning = true;
//			}
//		}
//		assertTrue("expected to handle AspectJ warning by adding a marker" + //$NON-NLS-1$
//				" to the project, but couldn't find marker", foundFakeWarning); //$NON-NLS-1$
//	}

	/**
	 * For an error without any throwable we expect a problem marker to be
	 * created
	 */
//	public void testHandleErrorWithMessage() throws Exception {
//		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
//		IBuildMessageHandler handler = AspectJPlugin.getDefault()
//				.getCompilerFactory().getCompilerForProject(project).getMessageHandler();
//
//		IMessage msg = new Message("fake error", IMessage.ERROR, null, null); //$NON-NLS-1$
//		handler.handleMessage(msg);
//		((UIMessageHandler)AspectJPlugin.getDefault().getCompilerFactory()
//				.getCompilerForProject(project).getMessageHandler()).showOutstandingProblems(project);
//
//		waitForJobsToComplete();
//		IMarker[] markers = project.findMarkers(
//				IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true,
//				IResource.DEPTH_INFINITE);
//		boolean foundFakeError = false;
//		for (int i = 0; i < markers.length; i++) {
//			IMarker marker = markers[i];
//			if (marker.getAttribute(IMarker.MESSAGE).equals("fake error")) { //$NON-NLS-1$
//				foundFakeError = true;
//			}
//		}
//		assertTrue(
//				"expected to handle AspectJ error without throwable by adding a marker" + //$NON-NLS-1$
//						" to the project, but couldn't find marker", //$NON-NLS-1$
//				foundFakeError);
//	}

	/**
	 * For an error with a throwable we expect an error dialog to appear
	 * containing the information
	 */
	public void testHandleErrorWithMessageAndThrowable() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		AJDTErrorHandler.setShowErrorDialogs(false);
		String message = ""; //$NON-NLS-1$
		try {
			IBuildMessageHandler handler = AspectJPlugin.getDefault()
					.getCompilerFactory().getCompilerForProject(project)
					.getMessageHandler();

			IMessage msg = new Message(
					"fake error", IMessage.ERROR, new AbortException("fake abort"), null); //$NON-NLS-1$ //$NON-NLS-2$
			handler.handleMessage(msg);
		} catch (RuntimeException re) {
			message = re.getMessage();
		}
		assertTrue("expected a runtime error with message 'fake abort' when " + //$NON-NLS-1$
				" testing error handling but didn't find one", //$NON-NLS-1$
				message.equals("org.aspectj.bridge.AbortException: fake abort")); //$NON-NLS-1$
	}

	/**
	 * For an abort message we expect an error dialog to appear containing the
	 * information
	 */
	public void testHandleAbortWithMessageAndThrowable() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		AJDTErrorHandler.setShowErrorDialogs(false);
		String message = ""; //$NON-NLS-1$
		try {
			IBuildMessageHandler handler = AspectJPlugin.getDefault()
					.getCompilerFactory().getCompilerForProject(project)
					.getMessageHandler();

			IMessage msg = new Message(
					"fake abort", IMessage.ABORT, new AbortException("fake abort"), null); //$NON-NLS-1$ //$NON-NLS-2$
			handler.handleMessage(msg);
		} catch (RuntimeException re) {
			message = re.getMessage();
		}
		assertTrue("expected a runtime error with message 'fake abort' when " + //$NON-NLS-1$
				" testing error handling but didn't find one", //$NON-NLS-1$
				message.equals("org.aspectj.bridge.AbortException: fake abort")); //$NON-NLS-1$
	}

	public void testDefaultSettingsMessagesThatAreIgnored() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		IBuildMessageHandler handler = AspectJPlugin.getDefault()
				.getCompilerFactory().getCompilerForProject(project).getMessageHandler();
		assertTrue("by default should be ignoring 'INFO' messages but " + //$NON-NLS-1$
				"are not", handler.isIgnoring(IMessage.INFO)); //$NON-NLS-1$
		assertTrue("by default should be ignoring 'WEAVEINFO' messages but " + //$NON-NLS-1$
				"are not", handler.isIgnoring(IMessage.WEAVEINFO)); //$NON-NLS-1$
	}
	
	public void testShowWeaveInfoMessagesAreNotIgnoredAfterWorkbenchPreferenceSet() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		IBuildMessageHandler handler = AspectJPlugin.getDefault()
				.getCompilerFactory().getCompilerForProject(project).getMessageHandler();
		
		AspectJPreferences.setShowWeaveMessagesOption(project,true);	
		assertFalse("should not be ignoring 'WEAVEINFO' messages but " + //$NON-NLS-1$
				"still are", handler.isIgnoring(IMessage.WEAVEINFO)); //$NON-NLS-1$
		
		AspectJPreferences.setShowWeaveMessagesOption(project,false);	
		assertTrue("by default should be ignoring 'WEAVEINFO' messages but " + //$NON-NLS-1$
				"are not", handler.isIgnoring(IMessage.WEAVEINFO)); //$NON-NLS-1$		
	}
	
	public void testShowWeaveInfoMessagesAreNotIgnoredAfterProjectPreferenceSet() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		IBuildMessageHandler handler = AspectJPlugin.getDefault()
				.getCompilerFactory().getCompilerForProject(project).getMessageHandler();
		
		AspectJPreferences.setUsingProjectSettings(project, true);
		
		AspectJPreferences.setShowWeaveMessagesOption(project,true);	
		assertFalse("should not be ignoring 'WEAVEINFO' messages but " + //$NON-NLS-1$
				"still are", handler.isIgnoring(IMessage.WEAVEINFO)); //$NON-NLS-1$
		
		AspectJPreferences.setShowWeaveMessagesOption(project,false);	
		assertTrue("by default should be ignoring 'WEAVEINFO' messages but " + //$NON-NLS-1$
				"are not", handler.isIgnoring(IMessage.WEAVEINFO)); //$NON-NLS-1$		
	}
	
	// set whether or not to show weaveinfo messages before the UIMessageHandler is created
	// - not sure if in reality can ever get into this state but need to make sure that
	// the UIMessageHandler honours any pre-set settings
	public void testShowWeaveInfoMessagesAreNotIgnoredIfWorkbenchPreferenceAlreadySet() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		// remove the "compiler" instance associated with this project
		AspectJPlugin.getDefault().getCompilerFactory().removeCompilerForProject(project);
		// decide to show weaveinfo messages
		AspectJPreferences.setShowWeaveMessagesOption(project,true);
		IBuildMessageHandler handler = AspectJPlugin.getDefault()
			.getCompilerFactory().getCompilerForProject(project).getMessageHandler();
		assertFalse("should not be ignoring 'WEAVEINFO' messages but " + //$NON-NLS-1$
				"still are", handler.isIgnoring(IMessage.WEAVEINFO)); //$NON-NLS-1$	
	}
	
	// set whether or not to show weaveinfo messages before the UIMessageHandler is created
	// - not sure if in reality can ever get into this state but need to make sure that
	// the UIMessageHandler honours any pre-set settings
	public void testShowWeaveInfoMessagesAreNotIgnoredIfWorkbenchPreferenceAlreadySet_2() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		// decide to show weaveinfo messages
		AspectJPreferences.setShowWeaveMessagesOption(project,true);
		// remove the "compiler" instance associated with this project
		AspectJPlugin.getDefault().getCompilerFactory().removeCompilerForProject(project);
		IBuildMessageHandler handler = AspectJPlugin.getDefault()
			.getCompilerFactory().getCompilerForProject(project).getMessageHandler();
		assertFalse("should not be ignoring 'WEAVEINFO' messages but " + //$NON-NLS-1$
				"still are", handler.isIgnoring(IMessage.WEAVEINFO)); //$NON-NLS-1$	
	}
	
	// set whether or not to show weaveinfo messages before the UIMessageHandler is created
	// - not sure if in reality can ever get into this state but need to make sure that
	// the UIMessageHandler honours any pre-set settings
	public void testShowWeaveInfoMessagesAreNotIgnoredIProjectPreferenceAlreadySet() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		// remove the "compiler" instance associated with this project
		AspectJPlugin.getDefault().getCompilerFactory().removeCompilerForProject(project);
		AspectJPreferences.setUsingProjectSettings(project, true);
		// decide to show weaveinfo messages
		AspectJPreferences.setShowWeaveMessagesOption(project,true);
		IBuildMessageHandler handler = AspectJPlugin.getDefault()
			.getCompilerFactory().getCompilerForProject(project).getMessageHandler();
		assertFalse("should not be ignoring 'WEAVEINFO' messages but " + //$NON-NLS-1$
				"still are", handler.isIgnoring(IMessage.WEAVEINFO)); //$NON-NLS-1$	
	}

	// set whether or not to show weaveinfo messages before the UIMessageHandler is created
	// - not sure if in reality can ever get into this state but need to make sure that
	// the UIMessageHandler honours any pre-set settings
	public void testShowWeaveInfoMessagesAreNotIgnoredIProjectPreferenceAlreadySet_2() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		AspectJPreferences.setUsingProjectSettings(project, true);
		// decide to show weaveinfo messages
		AspectJPreferences.setShowWeaveMessagesOption(project,true);
		// remove the "compiler" instance associated with this project
		AspectJPlugin.getDefault().getCompilerFactory().removeCompilerForProject(project);
		IBuildMessageHandler handler = AspectJPlugin.getDefault()
			.getCompilerFactory().getCompilerForProject(project).getMessageHandler();
		assertFalse("should not be ignoring 'WEAVEINFO' messages but " + //$NON-NLS-1$
				"still are", handler.isIgnoring(IMessage.WEAVEINFO)); //$NON-NLS-1$	
	}
}
