/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.core.contentassist;

import org.eclipse.ajdt.core.codeconversion.JavaCompatibleBuffer;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.ICompilationUnit;

/**
 * This class filters either all completion proposals that depend on
 * the class the code is located in or all the others.
 * 
 * Used for code completion in intertype methods.
 * A description of how code completion works in AJDT can be found in bug 74419.
 * 
 * @author Luzius Meisser
 */
public class ProposalRequestorFilter extends ProposalRequestorWrapper {
	
	// bug 279974 don't do extra filtering if position is in a dotted expression
	private boolean doExtraITDFiltering;
	
	
	public ProposalRequestorFilter(CompletionRequestor wrapped,
            ICompilationUnit unit,
			JavaCompatibleBuffer buffer, boolean doExtraITDFiltering) {
		super(wrapped, unit, buffer, "");
		this.doExtraITDFiltering = doExtraITDFiltering;
	}

	/* This logic mimics the src30 version, which has multiple acceptXXX methods.
	 * See bug 74419 for more info on code completion.
	 */
	public void accept(CompletionProposal proposal) {
		if ((proposal.getKind()==CompletionProposal.FIELD_REF)
				|| (proposal.getKind()==CompletionProposal.METHOD_REF)
				|| (proposal.getKind()==CompletionProposal.VARIABLE_DECLARATION)) {
			if (!doExtraITDFiltering) {
				super.accept(proposal);
			}			
		} else {				
		    super.accept(proposal);
		}
	}
	
	public void beginReporting() {
		// This is empty because we want to combine two sets of proposals
		// so we don't want the wrapped requestor to clear the list
	}
}
