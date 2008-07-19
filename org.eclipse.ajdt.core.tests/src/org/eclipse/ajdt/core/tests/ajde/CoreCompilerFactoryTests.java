/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.eclipse.ajdt.core.tests.ajde;

import org.aspectj.ajde.core.AjCompiler;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.internal.core.ajde.ICompilerFactory;
import org.eclipse.core.resources.IProject;

/**
 * Tests the behaviour of the CoreCompilerFactory
 */
public class CoreCompilerFactoryTests extends AJDTCoreTestCase {

	public void testCompilerInstanceRemovedOnProjectDeletion() throws Exception {
		ICompilerFactory factory = AspectJPlugin.getDefault().getCompilerFactory();
		IProject p = createPredefinedProject("TJP Example"); //$NON-NLS-1$
		assertTrue("expected there to be a 'Compiler' associated with the" + //$NON-NLS-1$
				" 'TJP Example' project but couldn't find one",factory.hasCompilerForProject(p)); //$NON-NLS-1$
		deleteProject(p,true);
		assertFalse("didn't expect there to be a 'Compiler' associated with the" + //$NON-NLS-1$
				" deleted 'TJP Example' project but found one",factory.hasCompilerForProject(p)); //$NON-NLS-1$
	}

	public void testCompilerInstanceRemovedOnProjectClosure() throws Exception {
		ICompilerFactory factory = AspectJPlugin.getDefault().getCompilerFactory();
		IProject p = createPredefinedProject("TJP Example"); //$NON-NLS-1$
		assertTrue("expected there to be a 'Compiler' associated with the" + //$NON-NLS-1$
				" 'TJP Example' project but couldn't find one",factory.hasCompilerForProject(p)); //$NON-NLS-1$
		p.close(null);
		assertFalse("didn't expect there to be a 'Compiler' associated with the" + //$NON-NLS-1$
				" deleted 'TJP Example' project but found one",factory.hasCompilerForProject(p)); //$NON-NLS-1$
	}
	
	public void testCompilerInstanceSavedInMap() throws Exception {
		ICompilerFactory factory = AspectJPlugin.getDefault().getCompilerFactory();
		IProject p = createPredefinedProject("TJP Example"); //$NON-NLS-1$
		AjCompiler c = factory.getCompilerForProject(p);
		AjCompiler c2 = factory.getCompilerForProject(p);
		assertEquals("expected the same Compiler instance but found different" + //$NON-NLS-1$
				" ones ", c,c2); //$NON-NLS-1$
	}
	
	public void testRemoveCompiler() throws Exception {
		ICompilerFactory factory = AspectJPlugin.getDefault().getCompilerFactory();
		IProject p = createPredefinedProject("TJP Example"); //$NON-NLS-1$
		factory.removeCompilerForProject(p);
		assertFalse("didn't expect there to be a 'Compiler' associated with the" + //$NON-NLS-1$
				" 'TJP Example' project but found one",factory.hasCompilerForProject(p)); //$NON-NLS-1$
	}
	
	public void testGetCompiler() throws Exception {
		ICompilerFactory factory = AspectJPlugin.getDefault().getCompilerFactory();
		IProject p = createPredefinedProject("TJP Example"); //$NON-NLS-1$
		AjCompiler c = factory.getCompilerForProject(p);
		IProject p2 = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		AjCompiler c2 = factory.getCompilerForProject(p2);
		assertNotSame("expected different Compiler instances for different projects" + //$NON-NLS-1$
				" but found the same one ", c,c2); //$NON-NLS-1$
	}
	
}
