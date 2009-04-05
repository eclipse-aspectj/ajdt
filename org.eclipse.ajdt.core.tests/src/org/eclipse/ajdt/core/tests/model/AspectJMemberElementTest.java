/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     SpringSource   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.model;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AspectJMemberElement;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author Andrew Eisenberg
 *
 * tests the implementation for bug 264008
 */
public class AspectJMemberElementTest extends AJDTCoreTestCase {

	IProject project;
	IJavaProject jProject;
	
	
	protected void setUp() throws Exception {
	    super.setUp();
	    project = createPredefinedProject("Spacewar Example");
	    jProject = JavaCore.create(project);
	}
	
	public void testGetSignature() throws Exception {
	    IPackageFragmentRoot[] roots = jProject.getAllPackageFragmentRoots();
	    for (int i = 0; i < roots.length; i++) {
            IPackageFragmentRoot root = roots[i];
            if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                doVisit(root);
            }
        }
	}

    private void doVisit(IParent parent) throws Exception {
        IJavaElement[] children = parent.getChildren();
        for (int i = 0; i < children.length; i++) {
            IJavaElement child = children[i];
            if (child instanceof AspectJMemberElement) {
                AspectJMemberElement member = (AspectJMemberElement) child;
                AJCompilationUnit ajcu = (AJCompilationUnit) member.getCompilationUnit();
                ajcu.requestOriginalContentMode();
                String source = member.getSource();
                source = source.replaceAll("\\s+", " ");
                
                ajcu.discardOriginalContentMode();
                
                String sig = member.retrieveSignatureFromSource();
                
                assertEquals("Signature should not contain a '{' sig: " + sig, -1, sig.indexOf('{'));
                assertEquals("Signature should not contain a ';' sig: " + sig, -1, sig.indexOf(';'));
                
                assertTrue("Signature should be contained in source source: \n\t" + 
                        source + "\nsignature\n\t" + sig, source.indexOf(sig) != -1);
            }
            
            if (child instanceof IParent) {
                doVisit((IParent) child);
            }
        }
    }
	
}