/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matt Chapman - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.lazystart;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.DeclareElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;

public class ImageDecoratorTest extends UITestCase {

	public void testBug158937() throws Exception {
		IProject project = createPredefinedProject("bug158937"); //$NON-NLS-1$

		ILabelProvider labelProvider = new DecoratingLabelProvider(
				new JavaElementLabelProvider(), AspectJUIPlugin.getDefault()
						.getWorkbench().getDecoratorManager()
						.getLabelDecorator());

		IResource aj = project.findMember("src/test/MyAspect.aj"); //$NON-NLS-1$
		assertNotNull("Couldn't find MyAspect.aj file", aj); //$NON-NLS-1$
		AJCompilationUnit cu = AJCompilationUnitManager.INSTANCE
				.getAJCompilationUnit((IFile) aj);
		IType[] types = cu.getAllTypes();
		assertTrue(
				"Compilation unit should contain exactly one type", types.length == 1); //$NON-NLS-1$
		assertTrue(
				"Contained type should be an AspectElement", types[0] instanceof AspectElement); //$NON-NLS-1$
		AspectElement aspect = (AspectElement) types[0];
		DeclareElement[] declares = aspect.getDeclares();
		assertNotNull(
				"AspectElement.getDeclares() should not return null", declares); //$NON-NLS-1$
		assertTrue(
				"AspectElement.getDeclares() should return exactly one declare element", declares.length == 1); //$NON-NLS-1$
		String text = labelProvider.getText(declares[0]);
		assertEquals(
				"Label provider returned wrong text for declare statement", "declare warning", text); //$NON-NLS-1$ //$NON-NLS-2$

		IntertypeElement[] itds = aspect.getITDs();
		assertNotNull("AspectElement.getITDs() should not return null", itds); //$NON-NLS-1$
		assertEquals(
				"AspectElement.getITDs() didn't return the expected number of ITDs", 2, itds.length); //$NON-NLS-1$
		text = labelProvider.getText(itds[0]);
		assertEquals(
				"Label provider returned wrong text for ITD", "Demo.x", text); //$NON-NLS-1$ //$NON-NLS-2$

		text = labelProvider.getText(itds[1]);
		assertEquals(
				"Label provider returned wrong text for ITD", "Demo.foo()", text); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
