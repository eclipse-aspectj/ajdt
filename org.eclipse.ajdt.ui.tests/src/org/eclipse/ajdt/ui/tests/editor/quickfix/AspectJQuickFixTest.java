/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.ui.tests.editor.quickfix;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;


/**
 * Tests for light bulb quick fix markers
 * 
 * @author Matt Chapman
 */
public class AspectJQuickFixTest extends AbstractQuickFixTest {

	IProject project;
    IFile javaFile;
    IFile javaWithAspectFile;
    IFile aspectFile;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = createPredefinedProject("QuickFix"); //$NON-NLS-1$
		
        javaFile = (IFile) project.findMember("src/test/TestJava.java");
        javaWithAspectFile = (IFile) project.findMember("src/test/TestAspect.java");
        aspectFile = (IFile) project.findMember("src/test/TestAspect2.aj");
	}

    public void testJavaQuickFixSetUp() throws Exception {
        quickFixSetup(javaFile);
    }
    
    public void testJavaWithAspectQuickFixSetUp() throws Exception {
        quickFixSetup(javaWithAspectFile);
    }
    
    public void testAspectQuickFixSetUp() throws Exception {
        quickFixSetup(aspectFile);
    }
    
    // just test to see that we have some completions coming back
    public void testJavaHasQuickFixes() throws Exception {
        IJavaCompletionProposal[] proposals = getQuickFixes(javaFile);
        assertTrue("Should have some completions, but doesn't", proposals.length > 0);
    }
    
    // just test to see that we have some completions coming back
    public void testJavaWithAspectHasQuickFixes() throws Exception {
        IJavaCompletionProposal[] proposals = getQuickFixes(javaWithAspectFile);
        assertTrue("Should not have any completions because we don't support completions for Aspects in Java files", proposals.length == 0);
    }
    
    // just test to see that we have some completions coming back
    public void testAspectHasQuickFixes() throws Exception {
        IJavaCompletionProposal[] proposals = getQuickFixes(aspectFile);
        assertTrue("Should have some completions, but doesn't", proposals.length > 0);
    }
}