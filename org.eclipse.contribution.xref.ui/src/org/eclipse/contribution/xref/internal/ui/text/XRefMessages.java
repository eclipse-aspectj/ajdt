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
package org.eclipse.contribution.xref.internal.ui.text;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 */
public class XRefMessages extends NLS {

	private static final String BUNDLE_NAME= XRefMessages.class.getName();

	private XRefMessages() {
		// Do not instantiate
	}
	
	public static String ToggleLinkingAction_label;
	public static String ToggleLinkingAction_tooltip;
	public static String ToggleLinkingAction_description;

	public static String ToggleShowXRefsForFileAction_label;
	public static String ToggleShowXRefsForFileAction_tooltip;
	public static String ToggleShowXRefsForFileAction_description;

	public static String CollapseAllAction_label;
	public static String CollapseAllAction_tooltip;
	public static String CollapseAllAction_description;

//	public static String XReferencePreferencePage_explanation;
//	public static String XReferencePreferencePage_description;
//	public static String XReferencePreferencePage_providersLabel;
//	public static String XReferencePreferencePage_noDescription;
	
	public static String XReferenceContentProvider_evaluate;

	// XReferenceInplaceDialog
	public static String XReferenceInplaceDialog_viewMenu_rememberBounds_label;
	public static String XReferenceInplaceDialog_viewMenu_resize_label;
	public static String XReferenceInplaceDialog_viewMenu_move_label;
	public static String XReferenceInplaceDialog_viewMenu_toolTipText;

	public static String XReferenceInplaceDialog_statusFieldText_hideParentCrosscutting;
	public static String XReferenceInplaceDialog_statusFieldText_showParentCrosscutting;
	
	// XReferenceUIPlugin
	public static String XRefUIPlugin_Jobs_XRefViewUpdate;
	public static String XRefUIPlugin_Jobs_Update;

	// Custom Filter Dialog
	public static String CustomFilterDialog_title;
	public static String CustomFilterDialog_message;

	public static String CustomFilterDialog_SelectAllButton_label;
	public static String CustomFilterDialog_DeselectAllButton_label;
	public static String CustomFilterDialog_RestoreDefaultsButton_label;

	public static String OpenCustomFiltersDialogAction_text;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, XRefMessages.class);
	}
}
