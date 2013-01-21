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

package org.eclipse.ajdt.ui.tests.editor.quickfix;

import org.eclipse.ajdt.internal.ui.editor.quickfix.JavaQuickFixProcessor;
import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.ui.text.correction.SerialVersionSubProcessor;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Andrew Eisenberg
 * @created Jan 2, 2009
 *
 */
public class QuickFixTest extends AbstractQuickFixTest {
    
    private IProject project;
    private IFile serializableFile;

    protected void setUp() throws Exception {
        super.setUp();
        project = createPredefinedProject("QuickFix"); //$NON-NLS-1$
        serializableFile = (IFile) project.findMember("src/serialization/Class2.java");
        setUpPluginEnvironment();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        resetPluginEnvironment();
    }
    
    public void testHasSerialIDQuickFix() throws Exception {
        ITextEditor editor = quickFixSetup(serializableFile, false);
        IJavaCompletionProposal[] proposals = getQuickFixes(serializableFile, new JavaQuickFixProcessor(), "Class2");
        assertEquals("Should have found 2 quickfixes for serialization", 2, proposals.length);
        
        assertTrue("Should only have serialziation quickfixes", proposals[0] instanceof SerialVersionSubProcessor.SerialVersionProposal);
        assertTrue("Should only have serialziation quickfixes", proposals[1] instanceof SerialVersionSubProcessor.SerialVersionProposal);
        
        // find the proposal for generating the serial ID
        IJavaCompletionProposal proposal;
        if (proposals[0].getAdditionalProposalInfo().contains("generated")) {
            proposal = proposals[0];
        } else if (proposals[1].getAdditionalProposalInfo().contains("generated")) {
            proposal = proposals[1];
        } else {
            proposal = null;
            fail("Could not find the generate serial ID proposal");
        }
        IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        proposal.apply(doc);
        
        // test the document to ensure that it has the string in it.
        assertTrue("Does not contain proper serial UID after applying quickfix", 
                doc.get().contains("-8766141914748822473L;"));
        
    }
    public void testHasSerialIDQuickFixInnerClass() throws Exception {
        ITextEditor editor = quickFixSetup(serializableFile, false);
        IJavaCompletionProposal[] proposals = getQuickFixes(serializableFile, new JavaQuickFixProcessor(), "Class3");
        assertEquals("Should have found 2 quickfixes for serialization", 2, proposals.length);
        
        assertTrue("Should only have serialziation quickfixes", proposals[0] instanceof SerialVersionSubProcessor.SerialVersionProposal);
        assertTrue("Should only have serialziation quickfixes", proposals[1] instanceof SerialVersionSubProcessor.SerialVersionProposal);
        
        // find the proposal for generating the serial ID
        IJavaCompletionProposal proposal;
        if (proposals[0].getAdditionalProposalInfo().contains("generated")) {
            proposal = proposals[0];
        } else if (proposals[1].getAdditionalProposalInfo().contains("generated")) {
            proposal = proposals[1];
        } else {
            proposal = null;
            fail("Could not find the generate serial ID proposal");
        }
        IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        proposal.apply(doc);
        
        // test the document to ensure that it has the string in it.
        assertTrue("Does not contain proper serial UID after applying quickfix", 
                doc.get().contains("-6071201175706894658L;"));
        
    }
    
    public void testNotAJProject() throws Exception {
        AJDTUtils.removeAspectJNature(project);
        IJavaCompletionProposal[] proposals = getQuickFixes(serializableFile, new JavaQuickFixProcessor(), "Class2");
        assertEquals("Java projects should not have AJ-specific quick fix proposals", 0, proposals == null ? 0 : proposals.length);
    }
}
