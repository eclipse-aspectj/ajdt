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
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.internal.core.AJWorkingCopyOwner;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * Tests for content assist for bug 280508
 * 
 * aspectOf and hasAspect assist proposals should appear appropriately
 * 
 * @author Andrew Eisenberg
 * 
 *
 */
public class Bug280508ContentAssist extends UITestCase {
    CompilationUnit cunit;
    AJCompilationUnit ajcunit;
    
    String cunitContents;
    String ajcunitContents;
    
    protected void setUp() throws Exception {
        super.setUp();
        IProject proj = createPredefinedProject("Bug280508AspectOf");
        
        cunit = (CompilationUnit) AspectJCore.create(proj.getFile("src/Class.java"));
        cunit.becomeWorkingCopy(null);
        JavaModelManager.getJavaModelManager().getPerWorkingCopyInfo(cunit, true, false, null);
        cunitContents = new String(cunit.getContents());

        ajcunit = (AJCompilationUnit) AspectJCore.create(proj.getFile("src/Aspect.aj"));
        ajcunit.becomeWorkingCopy(null);
        JavaModelManager.getJavaModelManager().getPerWorkingCopyInfo(ajcunit, true, false, null);
        ajcunitContents = new String(ajcunit.getContents());
    }
    

    
    // ensure that aspectOf appears as a proposal
    public void testAspectOfInClass() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = cunitContents.indexOf("Aspect.a") + "Aspect.a".length();  
        cunit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("Signature of proposal should have been the 'aspectOf()' method\n" + completionProposal, 
                "aspectOf", new String(completionProposal.getName())); 
        assertEquals("Completion start is wrong", offset - "a".length(), completionProposal.getReplaceStart());
    }    
    
    // ensure that the type of aspectOf is correct
    public void testAspectOfTypeInClass() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = cunitContents.indexOf("aspectOf().hh") + "aspectOf().hh".length();  
        cunit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("Signature of proposal should have been the 'hhh' field\n" + completionProposal, 
                "hhh", new String(completionProposal.getName())); 
        assertEquals("Completion start is wrong", offset - "hh".length(), completionProposal.getReplaceStart());
    }    

    // aspectOf will not appear as a proposal when the aspect being asked is the current aspect
    // but it will appear when the aspect is another.
    // this test ensures that this is so
    public void testAspectOfInAspect() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = ajcunitContents.indexOf("Aspect2.a") + "Aspect2.a".length();  
        ajcunit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("Signature of proposal should have been the 'aspectOf()' method\n" + completionProposal, 
                "aspectOf", new String(completionProposal.getName())); 
        assertEquals("Completion start is wrong", offset - "a".length(), completionProposal.getReplaceStart());
    }    
    
    // ensure that the type of aspectOf is correct
    public void testAspectOfTypeInAspect() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = ajcunitContents.indexOf("aspectOf().hh") + "aspectOf().hh".length();  
        ajcunit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("Signature of proposal should have been the 'hhh' field\n" + completionProposal, 
                "hhh", new String(completionProposal.getName())); 
        assertEquals("Completion start is wrong", offset - "hh".length(), completionProposal.getReplaceStart());
    }    

    public void testHasAspectInClass() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = cunitContents.indexOf("Aspect.h") + "Aspect.h".length();  
        cunit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("Signature of proposal should have been the 'hasApsect()' method\n" + completionProposal, 
                "hasAspect", new String(completionProposal.getName())); 
        assertEquals("Completion start is wrong", offset - "h".length(), completionProposal.getReplaceStart());
    }
    
    public void testGetWithinTypeName() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = cunitContents.indexOf("substring") + "substring".length();  
        cunit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 2 proposal, but found:\n" + requestor.toString(), 2, requestor.accepted.size());

        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("Signature of proposal should have been the 'substring()' method\n" + completionProposal, 
                "substring", new String(completionProposal.getName())); 
        assertEquals("Completion start is wrong", offset - "substring".length(), completionProposal.getReplaceStart());
    }
}