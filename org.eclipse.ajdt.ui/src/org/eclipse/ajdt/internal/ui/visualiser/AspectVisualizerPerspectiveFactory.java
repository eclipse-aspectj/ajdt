/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.visualiser;

import org.eclipse.contribution.visualiser.core.ProviderDefinition;
import org.eclipse.contribution.visualiser.core.ProviderManager;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;

/**
 * Based on
 * 
 * @see org.eclipse.jdt.internal.ui.JavaHierarchyPerspectiveFactory
 */
public class AspectVisualizerPerspectiveFactory implements IPerspectiveFactory {

	/*
	 * XXX: This is a workaround for:
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13070
	 */
	static IJavaElement fgJavaElementFromAction;

	/**
	 * Constructs a new Default layout engine.
	 */
	public AspectVisualizerPerspectiveFactory() {
		super();
	}

	public void createInitialLayout(IPageLayout layout) {
		//		if (AppearancePreferencePage.stackBrowsingViewsHorizontally())
		createHorizontalLayout(layout);
		//		else
		//			createVerticalLayout(layout);

		// action sets
		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		layout.addActionSet(JavaUI.ID_ACTION_SET);
		layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);

		// views - java
		layout.addShowViewShortcut(JavaUI.ID_TYPE_HIERARCHY);

		// views - search
		//layout.addShowViewShortcut(SearchUI.SEARCH_RESULT_VIEW_ID);
		layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);

		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);

		// new actions - Java project creation wizard
		layout
				.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewPackageCreationWizard"); //$NON-NLS-1$
		layout
				.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewClassCreationWizard"); //$NON-NLS-1$
		layout
				.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewInterfaceCreationWizard"); //$NON-NLS-1$
		layout
				.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSnippetCreationWizard"); //$NON-NLS-1$

		// Tell the Visualiser to switch to the AJDT providers
		ProviderDefinition[] definitions = ProviderManager
				.getAllProviderDefinitions();
		for (int i = 0; i < definitions.length; i++) {
			if (definitions[i].getMarkupInstance() instanceof AJDTMarkupProvider
					&& definitions[i].getContentProvider() instanceof AJDTContentProvider) {
				definitions[i].setEnabled(true);
			} else {
				definitions[i].setEnabled(false);
			}
		}
	}

	private void createHorizontalLayout(IPageLayout layout) {
		String relativePartId = IPageLayout.ID_EDITOR_AREA;
		int relativePos = IPageLayout.TOP;

		//		layout.addView(JavaUI.ID_PROJECTS_VIEW, IPageLayout.TOP, (float)0.25,
		// IPageLayout.ID_EDITOR_AREA);
		//		relativePartId= JavaUI.ID_PROJECTS_VIEW;
		//		relativePos= IPageLayout.RIGHT;

		layout.addView(JavaUI.ID_PROJECTS_VIEW, IPageLayout.TOP, (float) 0.75,
				IPageLayout.ID_EDITOR_AREA);
		layout.addView(JavaUI.ID_PACKAGES_VIEW, IPageLayout.RIGHT,
				(float) 0.15, JavaUI.ID_PROJECTS_VIEW);
		layout.addView("org.eclipse.contribution.visualiser.views.Visualiser",
				IPageLayout.RIGHT, (float) 0.15, JavaUI.ID_PACKAGES_VIEW);
		layout.addView("org.eclipse.contribution.visualiser.views.Menu",
				IPageLayout.RIGHT, (float) 0.80,
				"org.eclipse.contribution.visualiser.views.Visualiser");

		IPlaceholderFolderLayout placeHolderLeft = layout
				.createPlaceholderFolder(
						"left", IPageLayout.LEFT, (float) 0.25, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
		placeHolderLeft.addPlaceholder(JavaUI.ID_TYPE_HIERARCHY);
		placeHolderLeft.addPlaceholder(IPageLayout.ID_OUTLINE);
		placeHolderLeft.addPlaceholder(JavaUI.ID_PACKAGES);
		placeHolderLeft.addPlaceholder(IPageLayout.ID_RES_NAV);

		IPlaceholderFolderLayout placeHolderBottom = layout
				.createPlaceholderFolder(
						"bottom", IPageLayout.BOTTOM, (float) 0.75, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
		placeHolderBottom.addPlaceholder(IPageLayout.ID_TASK_LIST);
		//placeHolderBottom.addPlaceholder(SearchUI.SEARCH_RESULT_VIEW_ID);
		placeHolderBottom.addPlaceholder(NewSearchUI.SEARCH_VIEW_ID);
		placeHolderBottom.addPlaceholder(IPageLayout.ID_BOOKMARKS);
	}

	/*
	 * XXX: This is a workaround for:
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13070
	 */
	static void setInputFromAction(IAdaptable input) {
		if (input instanceof IJavaElement)
			fgJavaElementFromAction = (IJavaElement) input;
		else
			fgJavaElementFromAction = null;
	}
}