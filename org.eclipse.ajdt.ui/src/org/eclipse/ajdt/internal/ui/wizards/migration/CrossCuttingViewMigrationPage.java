/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.wizards.migration;

import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;


/**
 * This page resets the Outline view to be the Java version (rather than
 * the AJDT custom version), resets the defaults to be not to use the AJDT
 * custom version, and opens the new Cross Reference view. 
 */
public class CrossCuttingViewMigrationPage extends WizardPage {

	private Button useNewCrossCuttingViews;
	
	protected CrossCuttingViewMigrationPage() {
		super(AspectJUIPlugin.getResourceString("CrossCuttingViewMigrationPage.name"));
		this.setTitle(AspectJUIPlugin.getResourceString("CrossCuttingViewMigrationPage.title"));		
		this.setDescription( AspectJUIPlugin
				.getResourceString("CrossCuttingViewMigrationPage.description"));
	}
	
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(composite);
		
		Label label1 = new Label(composite, SWT.NONE);
		label1.setText(AspectJUIPlugin
				.getResourceString("CrossCuttingViewMigrationPage.useNewCrossCuttingViews.message")); //$NON-NLS-1$
		
		Label spacer = new Label(composite, SWT.NONE);
		
		useNewCrossCuttingViews = new Button(composite, SWT.CHECK);
		useNewCrossCuttingViews.setText(AspectJUIPlugin
				.getResourceString("CrossCuttingViewMigrationPage.useNewCrossCuttingViews.label")); //$NON-NLS-1$
		useNewCrossCuttingViews.setSelection(true);
		
	}

	public void finishPressed() {
		if (useNewCrossCuttingViews.getSelection()) {
			// open Java perspective
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			try {
				PlatformUI.getWorkbench().showPerspective(JavaUI.ID_PERSPECTIVE,window);
			} catch (WorkbenchException e) {
			}
			useJDTOutlineView();
			openXRefView();
		}
	}
	
	private void useJDTOutlineView() {
		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
		try {
			IWorkbenchPage activePage = AspectJUIPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
			if (AspectJPreferences.isAspectJOutlineEnabled()) {
				store.setToDefault(AspectJPreferences.ASPECTJ_OUTLINE);
			}
			IViewPart viewPart = activePage.findView(IPageLayout.ID_OUTLINE);
			activePage.hideView(viewPart);
			activePage.showView(IPageLayout.ID_OUTLINE);
		} catch (PartInitException e) {
			AspectJUIPlugin.getDefault().getErrorHandler().handleError(
					AspectJUIPlugin.getResourceString("AJDTPrefConfigWizardPage.ErrorOpeningXRefView"), e);
		}
		
		//store.setDefault(AspectJPreferences.ASPECTJ_OUTLINE, false);
		store.setToDefault(AspectJPreferences.ASPECTJ_OUTLINE);
		store.setToDefault(AspectJPreferences.ADVICE_DECORATOR);
		//store.setDefault(AspectJPreferences.ADVICE_DECORATOR, true);
	}
	
	private void openXRefView() {
		try {
			AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(XReferenceView.ID);
		} catch (PartInitException e) {
			AspectJUIPlugin.getDefault().getErrorHandler().handleError(
					AspectJUIPlugin.getResourceString("AJDTPrefConfigWizardPage.ErrorOpeningXRefView"), e);
		}
	}
}
