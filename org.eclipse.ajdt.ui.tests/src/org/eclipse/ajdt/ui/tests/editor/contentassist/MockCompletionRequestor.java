/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.ui.tests.editor.contentassist;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;

/**
 * @author Andrew Eisenberg
 * @created Jan 2, 2009
 *
 */
class MockCompletionRequestor extends CompletionRequestor {
    
    List accepted = new LinkedList();
    
    public void accept(CompletionProposal proposal) {
        accepted.add(proposal);
    }
    
    public List getAccepted() {
        return accepted;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Accepted completion proposals:\n");
        if (accepted.size() > 0) {
            for (Iterator iterator = accepted.iterator(); iterator.hasNext();) {
                CompletionProposal proposal = (CompletionProposal) iterator.next();
                sb.append("\t" + proposal.toString() + "\n");
            }
        } else {
            sb.append("\t<none>\n");
        }
        return sb.toString();
    }
    
}