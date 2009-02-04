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
 * Tests content assist in Java editors and that they
 * are ITD-aware
 * @author Andrew Eisenberg
 *
 */
public class ContentAssistTests3 extends UITestCase {
    CompilationUnit hasITDsUnit;
    CompilationUnit usesITDsUnit;
    
    String hasITDsContents;
    String usesITDsContents;
    
    protected void setUp() throws Exception {
        super.setUp();
        IProject proj = createPredefinedProject("ITDContentAssist");
        
        hasITDsUnit = (CompilationUnit) AspectJCore.create(proj.getFile("src/injavafiles/hasitds/HasITDs.java"));
        hasITDsUnit.becomeWorkingCopy(null);
        JavaModelManager.getJavaModelManager().getPerWorkingCopyInfo(hasITDsUnit, true, false, null);
        
        usesITDsUnit = (CompilationUnit) AspectJCore.create(proj.getFile("src/injavafiles/uses/UsesITDs.java"));
        usesITDsUnit.becomeWorkingCopy(null);
        JavaModelManager.getJavaModelManager().getPerWorkingCopyInfo(usesITDsUnit, true, false, null);
        
        hasITDsContents = new String(hasITDsUnit.getContents());
        usesITDsContents = new String(usesITDsUnit.getContents());
    }
    
    public void testITDField() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = hasITDsContents.indexOf("this.lis") + "this.lis".length();
        hasITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());
        
        
        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("Proposal should have been the 'list' field", 
                "list", new String(completionProposal.getName()));
        assertEquals("Completion start is wrong", offset - "lis".length(), completionProposal.getReplaceStart());
    }
    
    public void testITDFieldInField() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = hasITDsContents.indexOf("this.list.addAll") + "this.list.addAll".length();
        hasITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 2 proposals, but found:\n" + requestor.toString(), 2, requestor.accepted.size());

        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("Proposal should have been the 'addAll' method\n" + requestor.accepted.get(0), 
                "addAll", new String(completionProposal.getName())); 
        assertEquals("Completion start is wrong", offset - "addAll".length(), completionProposal.getReplaceStart());
        
        completionProposal = (CompletionProposal) requestor.accepted.get(1);
        assertEquals("Proposal should have been the 'addAll' method\n" + requestor.accepted.get(1), 
                "addAll", new String(completionProposal.getName())); 
        assertEquals("Completion start is wrong", offset - "addAll".length(), completionProposal.getReplaceStart());
    }
    
    public void testITDMethod() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = hasITDsContents.indexOf("this.makeL") + "this.makeL".length();
        hasITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("Proposal should have been the 'makeList' method\n" + requestor.accepted.get(0),
                "makeList", new String(completionProposal.getName())); 
        assertEquals("Completion start is wrong", offset - "makeL".length(), completionProposal.getReplaceStart());
    }
    
    public void testITDConstructor() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = hasITDsContents.indexOf("new HasITDs(") + "new HasITDs(".length();
        hasITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 2 proposals, but found:\n" + requestor.toString(), 2, requestor.accepted.size());

        // the anonymous class decl
        assertEquals("Signature of proposal should have been the 'HasITDs' constructor\n" + requestor.accepted.get(0), 
                "HasITDs", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
        // the constructor
        assertEquals("Proposal should have been the 'HasITDs' constructor\n" + requestor.accepted.get(1), 
                "Linjavafiles.hasitds.HasITDs;", new String(((CompletionProposal) requestor.accepted.get(1)).getDeclarationSignature()));
    }
    
    public void testFromDeclareParent() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = hasITDsContents.indexOf("this.insid") + "this.insid".length();
        hasITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        assertEquals("Signature of proposal should have been the 'Foo.inside' field\n" + requestor.accepted.get(0), 
                "inside", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
    }

    public void testFromITDOnDeclareParent() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = hasITDsContents.indexOf("this.valu") + "this.valu".length();
        hasITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        assertEquals("Signature of proposal should have been the 'Foo.value' field\n" + requestor.accepted.get(0), 
                "value", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
    }
    
    
    // the remaining tests try the same thing, but in a different class

    public void testITDFieldInOtherClass() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = usesITDsContents.indexOf("h.lis") + "h.lis".length();
        usesITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());
        assertEquals("Proposal should have been the 'list' field", 
                "list", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
    }
    
    public void testITDFieldInFieldInOtherClass() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = usesITDsContents.indexOf("h.list.addAl") + "h.list.addAl".length();
        usesITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 2 proposals, but found:\n" + requestor.toString(), 2, requestor.accepted.size());
        assertEquals("Proposal should have been the 'addAll' method\n" + requestor.accepted.get(0), 
                "addAll", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
        assertEquals("Proposal should have been the 'addAll' method\n" + requestor.accepted.get(1), 
                "addAll", new String(((CompletionProposal) requestor.accepted.get(1)).getName())); 
    }
    
    public void testITDMethodInOtherClass() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = usesITDsContents.indexOf("h.makeL") + "h.makeL".length();
        usesITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());
        assertEquals("Proposal should have been the 'makeList' method\n" + requestor.accepted.get(0),
                "makeList", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
    }
    
    public void testITDConstructorInOtherClass() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = usesITDsContents.indexOf("new HasITDs(") + "new HasITDs(".length();
        usesITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 2 proposals, but found:\n" + requestor.toString(), 2, requestor.accepted.size());

        // the anonymous class decl
        assertEquals("Signature of proposal should have been the 'HasITDs' constructor\n" + requestor.accepted.get(0), 
                "HasITDs", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
        // the constructor
        assertEquals("Proposal should have been the 'HasITDs' constructor\n" + requestor.accepted.get(1), 
                "Linjavafiles.hasitds.HasITDs;", new String(((CompletionProposal) requestor.accepted.get(1)).getDeclarationSignature()));
    }
    
    public void testFromDeclareParentInOtherClass() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = usesITDsContents.indexOf("h.insid") + "h.insid".length();
        usesITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        assertEquals("Signature of proposal should have been the 'Foo.inside' field\n" + requestor.accepted.get(0), 
                "inside", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
    }

    public void testFromITDOnDeclareParentInOtherClass() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = usesITDsContents.indexOf("h.valu") + "h.valu".length();
        usesITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        assertEquals("Signature of proposal should have been the 'Foo.value' field\n" + requestor.accepted.get(0), 
                "value", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
    }

}