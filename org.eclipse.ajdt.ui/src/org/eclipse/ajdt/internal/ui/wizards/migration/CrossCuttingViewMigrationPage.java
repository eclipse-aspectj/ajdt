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
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.core.resources.ResourcesPlugin;
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
		super(UIMessages.CrossCuttingViewMigrationPage_name);
		this.setTitle(UIMessages.CrossCuttingViewMigrationPage_title);		
		this.setDescription(UIMessages.CrossCuttingViewMigrationPage_description);
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
		label1.setText(UIMessages.CrossCuttingViewMigrationPage_useNewCrossCuttingViews_message);
		
		new Label(composite, SWT.NONE);
		
		useNewCrossCuttingViews = new Button(composite, SWT.CHECK);
		useNewCrossCuttingViews.setText(UIMessages.CrossCuttingViewMigrationPage_useNewCrossCuttingViews_label);
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
			String workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
			AspectJUIPlugin.getDefault().getPreferenceStore()
				.setValue(AspectJPreferences.DONE_AUTO_OPEN_XREF_VIEW + workspaceLocation, true);
		}
	}
	
	private void useJDTOutlineView() {
		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
		try {
			IWorkbenchPage activePage = AspectJUIPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
			IViewPart viewPart = activePage.findView(IPageLayout.ID_OUTLINE);
			activePage.hideView(viewPart);
			activePage.showView(IPageLayout.ID_OUTLINE);
		} catch (PartInitException e) {
			AspectJUIPlugin.getDefault().getErrorHandler().handleError(
					UIMessages.AJDTPrefConfigWizardPage_ErrorOpeningXRefView, e);
		}		
		store.setToDefault(AspectJPreferences.ADVICE_DECORATOR);
	}
	
	private void openXRefView() {
		try {
			AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(XReferenceView.ID);
		} catch (PartInitException e) {
			AspectJUIPlugin.getDefault().getErrorHandler().handleError(
					UIMessages.AJDTPrefConfigWizardPage_ErrorOpeningXRefView, e);
		}
	}
}
