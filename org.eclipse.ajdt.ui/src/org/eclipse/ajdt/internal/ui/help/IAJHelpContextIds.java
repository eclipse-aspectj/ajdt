/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January - inital version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.help;

import org.eclipse.ajdt.ui.AspectJUIPlugin;

/**
 * Help context ids for AJDT UI Plug-in.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 */
public interface IAJHelpContextIds {
	
	public static final String PREFIX= AspectJUIPlugin.PLUGIN_ID + '.';
	
	public static final String ASPECTJ_EDITOR= PREFIX + "aspectj_editor_context"; //$NON-NLS-1$
	public static final String CROSSCUTTING_COMPARISON_VIEW= PREFIX + "crosscutting_comparison_view_context"; //$NON-NLS-1$
	public static final String EVENT_TRACE_VIEW = PREFIX + "event_trace_view_context"; //$NON-NLS-1$
	
}
