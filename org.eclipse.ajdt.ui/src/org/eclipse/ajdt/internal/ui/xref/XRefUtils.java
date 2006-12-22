/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.xref;

import org.eclipse.ajdt.internal.ui.ajde.AJDTErrorHandler;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PartInitException;

public class XRefUtils {
	/**
	 * If an Aspect is opened in the Aspect Editor and the 'Cross References'
	 * view is NOT already open, it may be automatically opened for the user. A
	 * dialog will be displayed, asking the user:
	 * 
	 * a) if they want the view to be opened b) if they would like the view to
	 * be opened every time an Aspect is opened
	 * 
	 * -spyoung
	 */
	public static void autoOpenXRefView() {

		/*
		 * Case 1 the view is already open, by whatever means - do nothing.
		 */
		boolean viewAlreadyOpen = isXRefViewOpen();

		if (viewAlreadyOpen) {
			return;
		}

		/*
		 * Case 2 the view is not open AND the user DOESN'T want to have it
		 * opened for them.
		 */
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();

		// Extract information to local variables for debug
		boolean promptForAutoOpenCrossRefView = store
				.getBoolean(AspectJPreferences.PROMPT_FOR_AUTO_OPEN_CROSS_REF_VIEW);
		boolean autoOpenCrossRefView = store
				.getBoolean(AspectJPreferences.AUTO_OPEN_CROSS_REF_VIEW);

		if (!promptForAutoOpenCrossRefView && !autoOpenCrossRefView) {
			// Don't prompt - we've been asked not to open and to "remember
			// their decision"
			return;
		}

		/*
		 * Case 3 the view is not open AND the user HASN'T YET expressed an
		 * opinion.
		 */
		if (store
				.getBoolean(AspectJPreferences.PROMPT_FOR_AUTO_OPEN_CROSS_REF_VIEW)) {

			boolean userClickedYes = false;
			boolean rememberUsersChoice = false;

			MessageDialogWithToggle dialog = MessageDialogWithToggle
					.openYesNoQuestion(
							null,
							UIMessages.OpenAspect_showCrossReferencesView_question,
							UIMessages.OpenAspect_showCrossReferencesView_explanation,
							null, false, null, null);

			userClickedYes = (dialog.getReturnCode() == IDialogConstants.YES_ID);
			rememberUsersChoice = dialog.getToggleState();

			if (rememberUsersChoice && userClickedYes) {
				// 3.1 - remember that the user ALWAYS wants this view opened

				// do the same for all other projects...
				store.setValue(AspectJPreferences.AUTO_OPEN_CROSS_REF_VIEW,
						true);

				// ...without prompting
				store.setValue(
						AspectJPreferences.PROMPT_FOR_AUTO_OPEN_CROSS_REF_VIEW,
						false);
			} else if (rememberUsersChoice && !userClickedYes) {
				// 3.2 - remember that the user NEVER wants this view opened

				// do the same for all other projects...
				store.setValue(AspectJPreferences.AUTO_OPEN_CROSS_REF_VIEW,
						false);

				// ...without prompting
				store.setValue(
						AspectJPreferences.PROMPT_FOR_AUTO_OPEN_CROSS_REF_VIEW,
						false);

				// Nothing more to do now, so return
				return;
			} else if (!userClickedYes) {
				// Nothing more to do now, so return
				return;
			}
		}

		// And finally, open the Cross References view
		try {
			AspectJUIPlugin.getDefault().getActiveWorkbenchWindow()
					.getActivePage().showView(XReferenceView.ID);
		} catch (PartInitException e) {
			AJDTErrorHandler.handleAJDTError(
					UIMessages.BuildConfigurator_ErrorOpeningXRefView, e);
		}
	}

	public static boolean isXRefViewOpen() {
		IViewReference[] views = AspectJUIPlugin.getDefault()
				.getActiveWorkbenchWindow().getActivePage().getViewReferences();
		if (views != null) {
			for (int i = 0; i < views.length; i++) {
				if (XReferenceView.ID.equals(views[i].getId())) {
					return true;
				}
			}
		}
		return false;
	}

}
