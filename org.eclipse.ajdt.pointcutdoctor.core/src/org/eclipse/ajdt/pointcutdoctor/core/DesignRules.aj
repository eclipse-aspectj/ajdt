/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Kris De Volder - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.core;

import org.osgi.framework.BundleActivator;

/**
 * An aspect that generates Errors / Warnings for certains kinds of code structure that
 * should be discouraged / disallowed in the context of this plugin's implementation.
 * 
 * @author kdvolder
 */
public aspect DesignRules {

	/**
	 * To get a handle on where PointcutDoctor keeps state...
	 * <p>
	 * Should only keep state somehow hooked up into the PointcutDoctorCorePlugin's default instance.
	 */
	declare error: get(static !final * org.eclipse.ajdt.pointcutdoctor..*.*) 
	          && !withincode(* BundleActivator+.getDefault()): "Should not keep state in static fields!";
}
