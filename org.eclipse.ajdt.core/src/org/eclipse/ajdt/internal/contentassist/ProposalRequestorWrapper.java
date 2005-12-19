/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *     Sian January - changed for Eclipse 3.2
 *******************************************************************************/
package org.eclipse.ajdt.internal.contentassist;

import org.eclipse.ajdt.codeconversion.JavaCompatibleBuffer;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.compiler.IProblem;

/**
 * Translates code positions from fakeBuffer into realBuffer before
 * passing them on to the wrapped ICompletionRequestor.
 * 
 * A description of how code completion works in AJDT can be found in bug 74419.
 * 
 * @author Luzius Meisser
 */
public class ProposalRequestorWrapper extends CompletionRequestor {

	CompletionRequestor wrapped;
	JavaCompatibleBuffer buffer;

	/**
	 * @param wrapped
	 * @param buffer
	 */
	public ProposalRequestorWrapper(CompletionRequestor wrapped,
			JavaCompatibleBuffer buffer) {
		super();
		this.wrapped = wrapped;
		this.buffer = buffer;
	}
	
	public void accept(CompletionProposal proposal) {
		int s = proposal.getReplaceStart();
		int e = proposal.getReplaceEnd();
		proposal.setReplaceRange(trans(s), trans(e));
		wrapped.accept(proposal);
	}
	
	private int trans(int pos){
		return buffer.translatePositionToReal(pos);
	}
	
	public void acceptContext(CompletionContext context) {
		wrapped.acceptContext(context);
	}
	
	public void endReporting() {
		wrapped.endReporting();
	}
	
	public void beginReporting() {
		wrapped.beginReporting();
	}
	
	public void completionFailure(IProblem problem) {
		wrapped.completionFailure(problem);
	}

	public boolean equals(Object obj) {
		return wrapped.equals(obj);
	}
	public int hashCode() {
		return wrapped.hashCode();
	}
	public String toString() {
		return wrapped.toString();
	}

}
