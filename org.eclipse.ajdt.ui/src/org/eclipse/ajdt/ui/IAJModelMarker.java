/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ... 
 ******************************************************************************/
package org.eclipse.ajdt.ui;

/**
 * 
 */
public interface IAJModelMarker {

//	AJDT Markers as defined in plugin.xml
	
	// supertypes
	public static final String ADVICE_MARKER = "org.eclipse.ajdt.ui.advicemarker"; //$NON-NLS-1$
	public static final String DECLARATION_MARKER = "org.eclipse.ajdt.ui.declarationmarker"; //$NON-NLS-1$
	public static final String SOURCE_ADVICE_MARKER = "org.eclipse.ajdt.ui.sourceadvicemarker"; //$NON-NLS-1$

	// concrete marker types
	public static final String AJDT_PROBLEM_MARKER = "org.eclipse.ajdt.ui.problemmarker"; //$NON-NLS-1$
	public static final String ITD_MARKER = "org.eclipse.ajdt.ui.itdmarker"; //$NON-NLS-1$
	public static final String AFTER_ADVICE_MARKER = "org.eclipse.ajdt.ui.afteradvicemarker"; //$NON-NLS-1$
	public static final String DYNAMIC_AFTER_ADVICE_MARKER = "org.eclipse.ajdt.ui.dynamicafteradvicemarker"; //$NON-NLS-1$
	public static final String AROUND_ADVICE_MARKER = "org.eclipse.ajdt.ui.aroundadvicemarker"; //$NON-NLS-1$
	public static final String DYNAMIC_AROUND_ADVICE_MARKER = "org.eclipse.ajdt.ui.dynamicaroundadvicemarker"; //$NON-NLS-1$
	public static final String BEFORE_ADVICE_MARKER = "org.eclipse.ajdt.ui.beforeadvicemarker"; //$NON-NLS-1$
	public static final String DYNAMIC_BEFORE_ADVICE_MARKER = "org.eclipse.ajdt.ui.dynamicbeforeadvicemarker"; //$NON-NLS-1$
	public static final String DYNAMIC_ADVICE_MARKER = "org.eclipse.ajdt.ui.dynamicadvicemarker"; //$NON-NLS-1$
	public static final String SOURCE_BEFORE_ADVICE_MARKER = "org.eclipse.ajdt.ui.sourcebeforeadvicemarker"; //$NON-NLS-1$
	public static final String SOURCE_AFTER_ADVICE_MARKER = "org.eclipse.ajdt.ui.sourceafteradvicemarker"; //$NON-NLS-1$
	public static final String SOURCE_AROUND_ADVICE_MARKER = "org.eclipse.ajdt.ui.sourcearoundadvicemarker"; //$NON-NLS-1$
	public static final String SOURCE_ITD_MARKER = "org.eclipse.ajdt.ui.sourceitdmarker"; //$NON-NLS-1$
	public static final String SOURCE_DYNAMIC_ADVICE_MARKER = "org.eclipse.ajdt.ui.sourcedynamicadvicemarker"; //$NON-NLS-1$
	public static final String SOURCE_DYNAMIC_BEFORE_ADVICE_MARKER = "org.eclipse.ajdt.ui.sourcedynamicbeforeadvicemarker"; //$NON-NLS-1$
	public static final String SOURCE_DYNAMIC_AFTER_ADVICE_MARKER = "org.eclipse.ajdt.ui.sourcedynamicafteradvicemarker"; //$NON-NLS-1$
	public static final String SOURCE_DYNAMIC_AROUND_ADVICE_MARKER = "org.eclipse.ajdt.ui.sourcedynamicaroundadvicemarker"; //$NON-NLS-1$
	public static final String SOURCE_AND_TARGET_MARKER = "org.eclipse.ajdt.ui.sourceandtargetmarker"; //$NON-NLS-1$ 
	public static final String DYNAMIC_SOURCE_AND_TARGET_MARKER = "org.eclipse.ajdt.ui.dynamicsourceandtargetmarker"; //$NON-NLS-1$ 
	public static final String CUSTOM_MARKER = "org.eclipse.ajdt.ui.customadvicemarker"; //$NON-NLS-1$
//	public static final String CHANGED_ADVICE_MARKER = "org.eclipse.ajdt.ui.changedadvicemarker"; //$NON-NLS-1$	
}