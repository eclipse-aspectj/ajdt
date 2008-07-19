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
		createHorizontalLayout(layout);

		// action sets
		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		layout.addActionSet(JavaUI.ID_ACTION_SET);
		layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);

		// views - java
		layout.addShowViewShortcut(JavaUI.ID_TYPE_HIERARCHY);

		// views - search
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
		/*
		 * 158988 - change the views used by this perspective
		 *  
		 * NB The order in which you add the views makes a lot of difference to how easy it 
		 * is to arrange them as you want! 
		 */
		final String VISUALISER_VIEW_ID = "org.eclipse.contribution.visualiser.views.Visualiser";  //$NON-NLS-1$
		final String VISUALISER_MENU_VIEW_ID = "org.eclipse.contribution.visualiser.views.Menu";  //$NON-NLS-1$

		// Add the Visualiser Menu above the Java editor view
		layout.addView(VISUALISER_MENU_VIEW_ID, 
				IPageLayout.TOP, (float) 0.75, IPageLayout.ID_EDITOR_AREA);		

		// Add the package explorer to the left
		layout.addView(JavaUI.ID_PACKAGES, 
				IPageLayout.LEFT, (float) 0.20, VISUALISER_MENU_VIEW_ID);

		// Add the main visualiser view to the left of the v.menu
		layout.addView(VISUALISER_VIEW_ID, 
				IPageLayout.LEFT, (float) 0.75, VISUALISER_MENU_VIEW_ID);		


		/*
		 * Define slots to position common views if they are opened in this
		 * perspective.
		 */
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