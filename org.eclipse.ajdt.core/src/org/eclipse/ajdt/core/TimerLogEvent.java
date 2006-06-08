/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core;

/**
 * 
 */
public class TimerLogEvent {
	public static final String CREATE_MODEL = "Create element map"; //$NON-NLS-1$
	public static final String LOAD_MODEL = "Load serialized element map"; //$NON-NLS-1$
	public static final String TIME_IN_BUILD = "Total time spent in AJBuilder.build()"; //$NON-NLS-1$
	public static final String FIRST_COMPILED = "Time to first compiled message"; //$NON-NLS-1$
	public static final String FIRST_WOVEN = "Time to first woven message"; //$NON-NLS-1$
	public static final String TIME_IN_AJDE = "Total time spent in AJDE"; //$NON-NLS-1$
	public static final String ADD_MARKERS = "Add markers"; //$NON-NLS-1$
	public static final String UPDATE_TYPES_CACHE_FOR_PROJECT = "Update types cache for project "; //$NON-NLS-1$
	public static final String UPDATE_TYPES_CACHE_FOR_WORKSPACE = "Update types cache for the workspace"; //$NON-NLS-1$
}
