/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor.quickfix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Adapted from org.eclipse.jdt.internal.ui.text.correction.QuickFixProcessor
 */
public class AJQuickFixProcessor extends QuickFixProcessor implements IQuickAssistProcessor { // AspectJ Change

	public boolean hasCorrections(ICompilationUnit cu, int problemId) {
		switch (problemId) {
		case IProblem.ImportNotFound:
		case IProblem.UndefinedMethod:
		case IProblem.UndefinedField:
		case IProblem.UndefinedType:
		case IProblem.ParsingError:
			return true;
		default:
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see IAssistProcessor#getCorrections(org.eclipse.jdt.internal.ui.text.correction.IAssistContext, org.eclipse.jdt.internal.ui.text.correction.IProblemLocation[])
	 */
	public IJavaCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
		if (locations == null || locations.length == 0)
			return null;

		final IProject project = context.getCompilationUnit().getJavaProject().getProject();

		if (AspectJPlugin.isAJProject(project)) {
			// We're looking at a problem in an AspectJ Project
			IWorkbenchWindow workbenchWindow = getActiveWorkbenchWindow();
			if (workbenchWindow == null)
				return null;
			IEditorPart ed = workbenchWindow.getActivePage().getActiveEditor();
			if ((ed instanceof AspectJEditor)) {
				// Only apply to the Java editor
				return null;
			}
		}
		else {
			// We're looking at a problem in a Java Project
			boolean relevantError = false;
			for (int i = 0; i < locations.length && !relevantError; i++) {
				if (locations[i].getProblemId() == IProblem.ParsingError) {
					String[] args = locations[i].getProblemArguments();
					if (args[0].equals("aspect")) //$NON-NLS-1$
						relevantError = true;
				}
			}
			if (!relevantError) {
				// Only apply to relevant appearances of the problem
				return null;
			}
		}

		HashSet<Integer> handledProblems = new HashSet<>(locations.length);
		List<IJavaCompletionProposal> resultingCollections = new ArrayList<>();
		for (IProblemLocation location : locations) {
			Integer id = location.getProblemId();
			if (handledProblems.add(id))
				process(context, location, resultingCollections);
		}
		return resultingCollections.toArray(new IJavaCompletionProposal[0]);
	}

	private void process(
		IInvocationContext context,
		IProblemLocation problem,
		Collection<IJavaCompletionProposal> proposals
	)
		throws CoreException
	{
		int id = problem.getProblemId();
		// no proposals for none-problem locations
		if (id == 0)
			return;
		switch (id) {
			case IProblem.ImportNotFound:
			case IProblem.UndefinedMethod:
			case IProblem.UndefinedField:
			case IProblem.UndefinedType:
				AspectsProcessor.switchToAJEditorProposal(context, problem, proposals);
				break;
			case IProblem.ParsingError:
				final IProject project = context.getCompilationUnit().getJavaProject().getProject();
				if (AspectJPlugin.isAJProject(project))
					AspectsProcessor.switchToAJEditorProposal(context, problem, proposals);
				else
					AspectsProcessor.convertToAJProjectProposal(context, problem, proposals);
				break;
			default:
		}
	}

	// begin AspectJ Change
	// implementing methods of IQuickAssistProcessor
	public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations)
		throws CoreException
	{
		return getCorrections(context, locations);
	}

	public boolean hasAssists(IInvocationContext context) {
		IProblem[] problems = context.getASTRoot().getProblems();
		for (IProblem problem : problems) {
			if (hasCorrections(context.getCompilationUnit(), problem.getID()))
				return true;
		}
		return false;
	}
	// end AspectJ Change
}
