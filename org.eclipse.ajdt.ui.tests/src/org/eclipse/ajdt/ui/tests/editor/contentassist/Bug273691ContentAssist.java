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
 * Tests for content assist for bug 273691
 * @author Andrew Eisenberg
 * 
 * The aspect under test has a "void.class" expression.  
 * Must make sure that this usage of "class" is not confused
 * with a class declaration 
 *
 */
public class Bug273691ContentAssist extends UITestCase {
    CompilationUnit cunit;
    
    String cunitContents;
    
    protected void setUp() throws Exception {
        super.setUp();
        IProject proj = createPredefinedProject("Bug273691");
        
        cunit = (CompilationUnit) AspectJCore.create(proj.getFile("src/AspectWithDotClass.aj"));
        cunit.becomeWorkingCopy(null);
        JavaModelManager.getJavaModelManager().getPerWorkingCopyInfo(cunit, true, false, null);
        
        cunitContents = new String(cunit.getContents());
    }
    

    
    public void testGenericContentAssist() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        // bar is a generic type that extends Number
        int offset = cunitContents.indexOf("this.x") + "this.x".length();  
        cunit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("Signature of proposal should have been the 'x1' field\n" + completionProposal, 
                "x1", new String(completionProposal.getName())); 
        assertEquals("Completion start is wrong", offset - "x".length(), completionProposal.getReplaceStart());
    }    

}