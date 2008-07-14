/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Andrew Eisenberg - initial version
 * ...
 **********************************************************************/
package org.eclipse.ajdt.ui.visual.tests;


/**
 * 
 * @author andrew
 * @created Jun 19, 2008
 * 
 * Tests launching 
 */
public class DeclareErrorLaunchingTest extends VisualTestCase {

	
    /**
     * Can't run this test if not on a mac
     */
	public void testDeclareErrorAndLaunch() throws Exception {
//		IProject project = createPredefinedProject("DeclareError"); //$NON-NLS-1$
//		waitForJobsToComplete();
//		IJavaProject jp = JavaCore.create(project);
//		IPackageFragment p1 = jp.getPackageFragmentRoot(project.findMember("src")).getPackageFragment("declared"); //$NON-NLS-1$ //$NON-NLS-2$
//        ICompilationUnit declareErrorOnMe = p1.getCompilationUnit("DeclareErrorOnMe.java"); //$NON-NLS-1$
//        
//        selectInPackageExplorer(declareErrorOnMe);
//
//		// Run as AspectJ/Java Application
//        postKeyDown(SWT.ALT);
//        postKey('r');   
//        postKeyUp(SWT.ALT); 
//        postKey('s');
//        postKey(SWT.CR);
//        
//        // dialog should come up warning of an error before launching
//        sleep();
//        
//        // press escape to leave dialog
//        postKey(SWT.ESC);
//        
//        // check the console viewer to ensure that test has not run.
//        String output = getConsoleViewContents();
//        assertTrue("Pogram should not have run, but did.", output.equals(""));
    }
}

