/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sian January - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor.contentassist;

import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.codeconversion.ConversionOptions;
import org.eclipse.ajdt.codeconversion.JavaCompatibleBuffer;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProcessor;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.TextContentAssistInvocationContext;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Code completion processor for the AspectJ editor
 */
public class AJCompletionProcessor extends JavaCompletionProcessor {
	
	protected static final String intertypeMemberTag = "ajc$";  //$NON-NLS-1$
	private int offset;
	
	public AJCompletionProcessor(IEditorPart editor, ContentAssistant assistant, String partition) {
		super(editor, assistant, partition);
	}
	
	// Filter out proposals starting with "ajc$" and add a "limited AspectJ Support" message
	protected List filterAndSortProposals(List proposals, IProgressMonitor monitor, TextContentAssistInvocationContext context) {
		List newProposals = super.filterAndSortProposals(proposals, monitor, context);
		for (Iterator iter = newProposals.iterator(); iter.hasNext();) {
			ICompletionProposal proposal = (ICompletionProposal) iter.next();			
			if (proposal.getDisplayString().startsWith(intertypeMemberTag)) {
				iter.remove();
			}
		}
		newProposals.add(newProposals.size(), 
				new CompletionProposal("", offset, 0, 0, null, UIMessages.codeAssist_limited_title, null, UIMessages.codeAssist_limited_message));//$NON-NLS-1$ )
		return newProposals;
	}
	
	protected TextContentAssistInvocationContext createContext(ITextViewer viewer, int offset) {
		this.offset = offset;
		return new AJContentAssistInvocationContext(viewer, offset, fEditor);
	}
	
	
	private class AJContentAssistInvocationContext extends JavaContentAssistInvocationContext {
		
		private IEditorPart fEditor;
		
		/**
		 * Creates a new context.
		 * 
		 * @param viewer the viewer used by the editor
		 * @param offset the invocation offset
		 * @param editor the editor that content assist is invoked in
		 */
		public AJContentAssistInvocationContext(ITextViewer viewer, int offset, IEditorPart editor) {
			super(viewer, offset, editor);
			fEditor= editor;
		}
		
		/**
		 * Returns the compilation unit that content assist is invoked in, <code>null</code> if there
		 * is none.
		 * 
		 * @return the compilation unit that content assist is invoked in, possibly <code>null</code>
		 */
		public ICompilationUnit getCompilationUnit() {
			return AspectJUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
		}

	}

}
