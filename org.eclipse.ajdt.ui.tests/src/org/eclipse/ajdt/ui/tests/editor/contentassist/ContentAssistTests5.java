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
 * Tests for content assist for bug 270337
 * @author Andrew Eisenberg
 *
 */
public class ContentAssistTests5 extends UITestCase {
    CompilationUnit cunit;
    
    String cunitContents;
    
    protected void setUp() throws Exception {
        super.setUp();
        IProject proj = createPredefinedProject("ITDContentAssist");
        
        cunit = (CompilationUnit) AspectJCore.create(proj.getFile("src/other/Gen.java"));
        cunit.becomeWorkingCopy(null);
        JavaModelManager.getJavaModelManager().getPerWorkingCopyInfo(cunit, true, false, null);
        
        cunitContents = new String(cunit.getContents());
    }
    

    
    public void testGenericContentAssist() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        // bar is a generic type that extends Number
        int offset = cunitContents.indexOf("bar.fl") + "bar.fl".length();  
        cunit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("Signature of proposal should have been the 'bar.floatValue()' method\n" + completionProposal, 
                "floatValue", new String(completionProposal.getName())); 
        assertEquals("Completion start is wrong", offset - "fl".length(), completionProposal.getReplaceStart());
    }    
    public void testGenericContentAssist2() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        // bar is a generic type that also extends List
        int offset = cunitContents.indexOf("bar.cle") + "bar.cle".length();  
        cunit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("Signature of proposal should have been the 'bar.clear()' method\n" + completionProposal, 
                "clear", new String(completionProposal.getName())); 
        assertEquals("Completion start is wrong", offset - "cle".length(), completionProposal.getReplaceStart());
    }    
    public void testGenericContentAssist3() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        // bar is a generic type that also extends List
        int offset = cunitContents.indexOf("baz.com") + "baz.com".length();  
        cunit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        // There are lots of completions for 'com' these days. Let's just verify the one we want *is* on the list
        assertTrue("Expected at least one proposal",requestor.accepted.size()>0);

        CompletionProposal completionProposal = (CompletionProposal) requestor.findProposal("compareTo");
        assertNotNull("Expected 'compareTo' to be proposed",completionProposal);
        assertEquals("Completion start is wrong", offset - "com".length(), completionProposal.getReplaceStart());
    }
    
    public void testGenericContentAssist4() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        // baz is a generic type that also extends HashMap
        int offset = cunitContents.indexOf("baz.cle") + "baz.cle".length();  
        cunit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("Signature of proposal should have been the 'baz.clear()' method\n" + completionProposal, 
                "clear", new String(completionProposal.getName())); 
        assertEquals("Completion start is wrong", offset - "cle".length(), completionProposal.getReplaceStart());
    }
}