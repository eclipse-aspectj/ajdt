/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
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
	public static final String AJDT_PROBLEM_MARKER = "org.eclipse.ajdt.ui.problemmarker";
	public static final String ITD_MARKER = "org.eclipse.ajdt.ui.itdmarker";
	public static final String AFTER_ADVICE_MARKER = "org.eclipse.ajdt.ui.afteradvicemarker";
	public static final String DYNAMIC_AFTER_ADVICE_MARKER = "org.eclipse.ajdt.ui.dynamicafteradvicemarker";
	public static final String AROUND_ADVICE_MARKER = "org.eclipse.ajdt.ui.aroundadvicemarker";
	public static final String DYNAMIC_AROUND_ADVICE_MARKER = "org.eclipse.ajdt.ui.dynamicaroundadvicemarker";
	public static final String BEFORE_ADVICE_MARKER = "org.eclipse.ajdt.ui.beforeadvicemarker";
	public static final String DYNAMIC_BEFORE_ADVICE_MARKER = "org.eclipse.ajdt.ui.dynamicbeforeadvicemarker";
	public static final String DYNAMIC_ADVICE_MARKER = "org.eclipse.ajdt.ui.dynamicadvicemarker";
	public static final String ADVICE_MARKER = "org.eclipse.ajdt.ui.advicemarker";
	public static final String DECLARATION_MARKER = "org.eclipse.ajdt.ui.declarationmarker";
	public static final String SOURCE_ADVICE_MARKER = "org.eclipse.ajdt.ui.sourceadvicemarker";
	public static final String SOURCE_BEFORE_ADVICE_MARKER = "org.eclipse.ajdt.ui.sourcebeforeadvicemarker";
	public static final String SOURCE_AFTER_ADVICE_MARKER = "org.eclipse.ajdt.ui.sourceafteradvicemarker";
	public static final String SOURCE_AROUND_ADVICE_MARKER = "org.eclipse.ajdt.ui.sourcearoundadvicemarker";
}
