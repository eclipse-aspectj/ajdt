/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.examples;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public class AspectJExampleMessages {

	private static final String RESOURCE_BUNDLE= AspectJExampleMessages.class.getName();
	private static ResourceBundle fgResourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);

	private AspectJExampleMessages() {
	}

	public static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	/**
	 * Gets a string from the resource bundle and formats it with the argument
	 * 
	 * @param key	the string used to get the bundle value, must not be null
	 */
	public static String getFormattedString(String key, Object arg) {
		return NLS.bind(getString(key), new Object[] { arg });
	}


	/**
	 * Gets a string from the resource bundle and formats it with arguments
	 */	
	public static String getFormattedString(String key, Object[] args) {
		return NLS.bind(getString(key), args);
	}

}
