/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matt Chapman - initial version
 *******************************************************************************/

package org.eclipse.ajdt.ui.tests;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import junit.framework.TestCase;

/**
 */
public class VerificationTest extends TestCase {

	/**
	 * The Structure Model view is defined in the plugin.xml but it should be
	 * commented out as it only for development or debugging purposes. This test
	 * checks in the registry that the view is not defined.
	 *  
	 */
	public void testAbsentStructureModelView() {
		assertFalse(
				"Structure Model view should be commented out in plugin.xml",
				isViewDefined("org.eclipse.ajdt.ui.ajde.StructureModelView"));
	}

	/**
	 * The Pointcut Navigator view is defined in the plugin.xml but it should be
	 * commented out as it is still under development. This test
	 * checks in the registry that the view is not defined.
	 *  
	 */
	public void testAbsentNavigatorView() {
		assertFalse(
				"Pointcut Navigator view should be commented out in plugin.xml",
				isViewDefined("org.eclipse.ajdt.ui.navigator.PointcutNavigatorView"));
	}

	private boolean isViewDefined(String viewID) {
		String viewExtension = "org.eclipse.ui.views";

		IExtensionPoint exP = Platform.getExtensionRegistry()
				.getExtensionPoint(viewExtension);
		IExtension[] exs = exP.getExtensions();

		for (int i = 0; i < exs.length; i++) {
			IConfigurationElement[] ces = exs[i].getConfigurationElements();
			for (int j = 0; j < ces.length; j++) {
				String id = ces[j].getAttribute("id");
				if (id.equals(viewID)) {
					return true;
				}
			}
		}
		return false;
	}
}
