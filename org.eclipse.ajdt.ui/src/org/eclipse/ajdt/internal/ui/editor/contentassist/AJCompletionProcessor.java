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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProcessor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.TextContentAssistInvocationContext;
import org.eclipse.ui.IEditorPart;

/**
 * Code completion processor for the AspectJ editor
 */
public class AJCompletionProcessor extends JavaCompletionProcessor {
	
	protected static final String intertypeMemberTag = "ajc$";  //$NON-NLS-1$
	
	public AJCompletionProcessor(IEditorPart editor, ContentAssistant assistant, String partition) {
		super(editor, assistant, partition);
	}
	
	// Filter out proposals starting with "ajc$"
	@Override
	protected List filterAndSortProposals(List proposals, IProgressMonitor monitor, TextContentAssistInvocationContext context) {
		List newProposals = super.filterAndSortProposals(proposals, monitor, context);
		for (Iterator iter = newProposals.iterator(); iter.hasNext();) {
			ICompletionProposal proposal = (ICompletionProposal) iter.next();			
			if (proposal.getDisplayString().startsWith(intertypeMemberTag)) {
				iter.remove();
			}
		}
		return newProposals;
	}
	
	@Override
	protected TextContentAssistInvocationContext createContext(ITextViewer viewer, int offset) {
		viewer = new TextViewerWrapper(viewer);
		return super.createContext(viewer, offset);
	}

}
