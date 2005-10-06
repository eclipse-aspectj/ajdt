/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.xref;

import java.util.List;

import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.contribution.xref.core.XReferenceAdapter;
import org.eclipse.contribution.xref.internal.ui.providers.TreeParent;
import org.eclipse.contribution.xref.internal.ui.providers.XReferenceContentProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;

public class XReferenceViewContentsTest extends UITestCase {

	/**
	 * Test for bug 111189 - the xrefs not being added to the
	 * tree if there is advice on methods inside inner classes.
	 */
	public void testBug111189() throws CoreException {
		IProject project = createPredefinedProject("bug111189"); //$NON-NLS-1$
		waitForJobsToComplete();
		
		XReferenceContentProvider viewContentProvider =
			new XReferenceContentProvider();
		
		AJModel ajmodel = AJModel.getInstance();
		ajmodel.createMap(project);
		waitForJobsToComplete();
		
		AJRelationshipType[] rels = new AJRelationshipType[]{
				AJRelationshipManager.MATCHES_DECLARE,
				AJRelationshipManager.MATCHED_BY};
		
		List listOfRels = ajmodel.getAllRelationships(project,rels);
		assertTrue("there should be some relationships",!listOfRels.isEmpty()); //$NON-NLS-1$

		// want to get the IJavaElement corresponding to Test
		AJRelationship rel = (AJRelationship)listOfRels.get(0);
		boolean cont = true;
		IJavaElement source = null;
		IJavaElement je = rel.getSource();
		while (cont) {
			IJavaElement parent = je.getParent();
			if (parent.getElementName().equals("Test")) { //$NON-NLS-1$
				source = parent;
				cont = false;
			} else {
				je = parent;
			}			
		}
		
		XReferenceAdapter xra = new XReferenceAdapter(source);
		viewContentProvider.inputChanged(null,null,xra);
		Object[] elements = viewContentProvider.getElements(xra);
		assertEquals("There should be one source of xref",1, elements.length); //$NON-NLS-1$
		assertEquals(source, ((TreeParent) elements[0]).getData());
		
		TreeParent testMethod = null;
		TreeParent innerClassC1 = null;
		
		Object[] children = viewContentProvider.getChildren(elements[0]);
		// if there aren't two children here then the recursion is 
		// wrong in the XReferenceContentProvider.addChildren(..) method
		// and this is a regression of bug 111189
		assertEquals("There should be two children",2, children.length); //$NON-NLS-1$
		for (int i = 0; i < children.length; i++) {
			TreeParent t = (TreeParent)children[i];
			IJavaElement x = (IJavaElement)t.getData();
			if (x.getElementName().equals("testMethod")) { //$NON-NLS-1$
				testMethod = t;
			} else {
				innerClassC1 = t;
			}
		}
		
		// Check that all the children have been added to the testMethod node
		Object[] c1 = viewContentProvider.getChildren(testMethod);
		assertEquals("testMethod should have 1 child",1,c1.length); //$NON-NLS-1$
		Object[] c2 = viewContentProvider.getChildren(c1[0]);
		assertEquals("named innerclass C should have 1 child",1,c2.length); //$NON-NLS-1$
		Object[] c3 = viewContentProvider.getChildren(c2[0]);
		assertEquals("method m() of named innerclass C should have 1 child",1,c3.length); //$NON-NLS-1$
		Object[] c4 = viewContentProvider.getChildren(c3[0]);
		assertEquals("matches declare should have 1 child",1,c4.length); //$NON-NLS-1$
		
		// Check that all the children have been added to the innerclass node
		Object[] d1 = viewContentProvider.getChildren(innerClassC1);
		assertEquals("inner class should have 1 child",1,d1.length); //$NON-NLS-1$
		Object[] d2 = viewContentProvider.getChildren(d1[0]);
		// if there aren't two children then this is a regression of bug 111189
		//  - the recursion in XReferenceContentProvider.addChildren(..) method
		assertEquals("method m() of inner class should have 2 children",2,d2.length); //$NON-NLS-1$
		// there are two children of m(): 'matches declare' and 
		// 'method-call(.....)'
		TreeParent matchesDeclare = null;
		TreeParent methodCall = null;
		for (int i = 0; i < d2.length; i++) {
			TreeParent t = (TreeParent)d2[i];
			if (t.getData() == null) {
				matchesDeclare = t;
			} else {
				methodCall = t;
			}
		}
		Object[] d3 = viewContentProvider.getChildren(matchesDeclare);
		assertEquals("matches declare should have one child",1,d3.length); //$NON-NLS-1$
		Object[] d4 = viewContentProvider.getChildren(methodCall);
		assertEquals("method-call should have one child",1,d4.length); //$NON-NLS-1$
		Object[] d5 = viewContentProvider.getChildren(d4[0]);
		assertEquals("matches declare should have one child",1,d5.length); //$NON-NLS-1$
	}

	
}
