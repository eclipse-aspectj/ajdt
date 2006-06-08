/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.contribution.visualiser.tests;

import org.eclipse.contribution.visualiser.core.resources.VisualiserImages;
import org.eclipse.contribution.visualiser.interfaces.IMarkupKind;
import org.eclipse.contribution.visualiser.simpleImpl.SimpleMarkupKind;
import org.eclipse.swt.graphics.Image;

import junit.framework.TestCase;


public class MarkupKindsTest extends TestCase {
	
	public void testEquals() {
		IMarkupKind kind1 = new SimpleMarkupKind("kind"); //$NON-NLS-1$
		IMarkupKind kind2 = new SimpleMarkupKind("kind"); //$NON-NLS-1$
		IMarkupKind kind3 = new SimpleMarkupKind("a different kind"); //$NON-NLS-1$
		assertEquals("SimpleMarkupKinds with the same name should be equal", kind1, kind2); //$NON-NLS-1$
		assertEquals("SimpleMarkupKinds that are the same Object should be equal", kind1, kind1); //$NON-NLS-1$
		assertNotSame("SimpleMarkupKinds with different names should not be equal", kind1, kind3); //$NON-NLS-1$
	}
	
	public void testEqualsWithImages() {
		Image image1 = VisualiserImages.MEMBER_VIEW.createImage();
		Image image2 = VisualiserImages.FIT_TO_VIEW.createImage();
		IMarkupKind kind1 = new SimpleMarkupKind("kind", image1); //$NON-NLS-1$
		IMarkupKind kind2 = new SimpleMarkupKind("kind", image1); //$NON-NLS-1$
		IMarkupKind kind3 = new SimpleMarkupKind("differnt kind", image1); //$NON-NLS-1$
		IMarkupKind kind4 = new SimpleMarkupKind("kind", image2); //$NON-NLS-1$
		assertEquals("SimpleMarkupKinds with the same name and image should be equal", kind1, kind2); //$NON-NLS-1$
		assertEquals("SimpleMarkupKinds that are the same Object should be equal", kind1, kind1); //$NON-NLS-1$
		assertNotSame("SimpleMarkupKinds with different names should not be equal", kind1, kind3); //$NON-NLS-1$
		assertNotSame("SimpleMarkupKinds with different images should not be equal", kind1, kind4); //$NON-NLS-1$
	}
	
	public void testHashCode() {
		IMarkupKind kind1 = new SimpleMarkupKind("kind"); //$NON-NLS-1$
		IMarkupKind kind2 = new SimpleMarkupKind("kind"); //$NON-NLS-1$
		assertEquals("SimpleMarkupKinds that are equal should have the same hashCode", kind1.hashCode(), kind2.hashCode()); //$NON-NLS-1$
		assertEquals("SimpleMarkupKinds that are the same Object should have the same hashCode", kind1.hashCode(), kind1.hashCode()); //$NON-NLS-1$
	}

	public void testHashCodeWithImages() {
		Image image1 = VisualiserImages.MEMBER_VIEW.createImage();
		IMarkupKind kind1 = new SimpleMarkupKind("kind", image1); //$NON-NLS-1$
		IMarkupKind kind2 = new SimpleMarkupKind("kind", image1); //$NON-NLS-1$
		assertEquals("SimpleMarkupKinds that are equal should have the same hashCode", kind1.hashCode(), kind2.hashCode()); //$NON-NLS-1$
		assertEquals("SimpleMarkupKinds that are the same Object should have the same hashCode", kind1.hashCode(), kind1.hashCode()); //$NON-NLS-1$
	}
	
}
