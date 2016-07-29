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

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.internal.core.AJWorkingCopyOwner;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * Tests for content assist in aspects
 * @author Andrew Eisenberg
 *
 */
public class ContentAssistTests4 extends UITestCase {
    CompilationUnit aspectsUnit;
    
    String aspectsUnitContents;
    
    protected void setUp() throws Exception {
        super.setUp();
        IProject proj = createPredefinedProject("ITDContentAssist");
        
        aspectsUnit = (CompilationUnit) AspectJCore.create(proj.getFile("src/itds/ITDAspect.aj"));
        aspectsUnit.becomeWorkingCopy(null);
        JavaModelManager.getJavaModelManager().getPerWorkingCopyInfo(aspectsUnit, true, false, null);
        
        aspectsUnitContents = new String(aspectsUnit.getContents());
    }
    

    public void testInMainMethod() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = aspectsUnitContents.indexOf("args.len") + "args.len".length();
        aspectsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("Signature of proposal should have been the 'args.length' field\n" + completionProposal, 
                "length", new String(completionProposal.getName())); 
        assertEquals("Completion start is wrong", offset - "len".length(), completionProposal.getReplaceStart());
    }    
    public void testInITD() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = aspectsUnitContents.indexOf("= new ArrayLis") + "= new ArrayLis".length();
        aspectsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        requestor.filter("java.util.ArrayListSpliterator");
        requestor.filter("com.sun.javafx.collections.ArrayListenerHelper");
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("Signature of proposal should have been the 'ArrayList' type\n" + completionProposal, 
                "ArrayList", new String(completionProposal.getCompletion())); 
        assertEquals("Completion start is wrong", offset - "ArrayLis".length(), completionProposal.getReplaceStart());
    }    
    public void testInAdvice() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = aspectsUnitContents.indexOf("thisJoinPoin") + "thisJoinPoin".length();
        aspectsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 2 proposal, but found:\n" + requestor.toString(), 2, requestor.accepted.size());

        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("Signature of proposal should have been 'thisJoinPoint'\n" + completionProposal, 
                "thisJoinPoint", new String(completionProposal.getName())); 
        assertEquals("Completion start is wrong", offset - "thisJoinPoin".length(), completionProposal.getReplaceStart());
        
        completionProposal = (CompletionProposal) requestor.accepted.get(1);
        assertEquals("Signature of proposal should have been 'thisJoinPointStaticPart'\n" + completionProposal, 
                "thisJoinPointStaticPart", new String(completionProposal.getName())); 
        assertEquals("Completion start is wrong", offset - "thisJoinPoin".length(), completionProposal.getReplaceStart());
    }    
}