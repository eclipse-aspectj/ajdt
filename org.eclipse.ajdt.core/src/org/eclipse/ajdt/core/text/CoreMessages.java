/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ben Dalziel     - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.text;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 */
public class CoreMessages extends NLS {

	private static final String BUNDLE_NAME= CoreMessages.class.getName();

	private CoreMessages() {
		// Do not instantiate
	}
	
	public static String advises_displayName;
	public static String advised_by_displayName;
	public static String declared_on_displayName;
	public static String aspect_declarations_displayName;
	public static String matched_by_displayName;
	public static String matches_declare_displayName;
	public static String annotates_displayName;
	public static String annotated_by_displayName;
	public static String softens_displayName;
	public static String softened_by_displayName;
	public static String uses_pointcut_displayName;
	public static String pointcut_used_by_displayName;

	public static String advises_menuName;
	public static String advised_by_menuName;
	public static String declared_on_menuName;
	public static String aspect_declarations_menuName;
	public static String matched_by_menuName;
	public static String matches_declare_menuName;
	public static String annotates_menuName;
	public static String annotated_by_menuName;
	public static String softens_menuName;
	public static String softened_by_menuName;
	public static String uses_pointcut_menuName;
	public static String pointcut_used_by_menuName;

	public static String injarElementLabel;
	public static String injarElementLabel2;

	public static String missingJarsWarning;

	public static String renameTypeReferences;

	public static String ajRuntimeContainerName;

	public static String BuilderMissingInpathEntry;
	public static String BuilderMissingAspectpathEntry;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, CoreMessages.class);
	}

	public static String builder_taskname;
	
	public static String noAJDEFound;
}
