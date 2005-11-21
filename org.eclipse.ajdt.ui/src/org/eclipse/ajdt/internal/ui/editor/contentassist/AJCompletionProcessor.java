/**********************************************************************
Copyright (c) 2004 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    Luzius Meisser - Initial implementation
**********************************************************************/
package org.eclipse.ajdt.internal.ui.editor.contentassist;

import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProcessor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Luzius Meisser
 * 
 * Filters completion proposals beginning with "ajc$"
 * 
 * A description of how code completion works in AJDT can be found in bug 74419.
 * 
 */
public class AJCompletionProcessor extends JavaCompletionProcessor {
	
	//indicates whether this editors file is in an AJ project or not
	boolean isInAspectJContext = false;
	
	public AJCompletionProcessor(IEditorPart editor) {
		super(editor);
		IDocumentProvider provider = null;
		if (editor instanceof ITextEditor){
			provider = ((ITextEditor)editor).getDocumentProvider();
		}
		IEditorInput input = editor.getEditorInput();
		if (input instanceof FileEditorInput){
			IFile file = ((FileEditorInput)input).getFile();
			ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator().getProjectBuildConfigurator(file.getProject());
			isInAspectJContext = (pbc != null);
		}
		if (isInAspectJContext)
			fManager = new WorkingCopyManagerForCompletionProcessor(fManager, provider);
			
	}
	
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		IContextInformation[] result = super.computeContextInformation(viewer, offset);
		if (isInAspectJContext)
			((WorkingCopyManagerForCompletionProcessor)fManager).discardWorkingCopy();
		return result;
	}
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		ICompletionProposal[] props = super.computeCompletionProposals(viewer, offset);
		if (isInAspectJContext){
			((WorkingCopyManagerForCompletionProcessor)fManager).discardWorkingCopy();

			ICompletionProposal[] propsnew = new ICompletionProposal[props.length + 1];
			System.arraycopy(props, 0, propsnew, 0, props.length);
			propsnew[propsnew.length - 1] = new CompletionProposal("", offset, 0, 0, null, UIMessages.codeAssist_limited_title, //$NON-NLS-1$
					null, UIMessages.codeAssist_limited_message);
			props = propsnew;
		}
		return filterAjcElements(props);
	}
	
	
	//removes all the proposals beginning with 'ajc$'
	private ICompletionProposal[] filterAjcElements(ICompletionProposal[] props){
		int toRemove = 0;
		for (int i = 0; i < props.length; i++) {
			if (shouldBeRemoved(props[i]))
				toRemove++;
		}
		if (toRemove > 0){
			ICompletionProposal[] newProps = new ICompletionProposal[props.length - toRemove];
			for (int i = 0, j = 0; i < newProps.length; i++) {
				while (shouldBeRemoved(props[j++]));
				newProps[i] = props[j-1];
			}
			props = newProps;
		}
		return props;
	}
	
	protected static final String intertypeMemberTag = "ajc$";  //$NON-NLS-1$
	
	protected boolean shouldBeRemoved(ICompletionProposal proposal){
		return proposal.getDisplayString().startsWith(intertypeMemberTag);
	}
	
}
