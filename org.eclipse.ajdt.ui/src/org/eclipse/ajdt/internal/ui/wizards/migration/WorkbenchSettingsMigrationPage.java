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

import org.eclipse.ajdt.internal.ui.AJDTConfigSettings;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This page handles the migration of workbench settings, for example,
 * it turns on red sqiggles, enables unused imports in aspects and 
 * resets the file association default of .java files to be the Java Editor
 */
public class WorkbenchSettingsMigrationPage extends WizardPage {

	// this page handles:
	// - .aj resource filter
	// - enables redsquiggles
	// - enablign unused imports in aspects
	
//	private Button ajResourceFilterButton;
	private Button enableRedSquigglesButton;
	private Button enableUnusedImportsButton;
	private Button fileAssociationsButton;
	
	protected WorkbenchSettingsMigrationPage() {
		super(AspectJUIPlugin.getResourceString("WorkbenchSettingsMigrationPage.name")); //$NON-NLS-1$
		this.setTitle(AspectJUIPlugin.getResourceString("WorkbenchSettingsMigrationPage.title")); //$NON-NLS-1$		
		this.setDescription( AspectJUIPlugin.getResourceString("WorkbenchSettingsMigrationPage.description")); //$NON-NLS-1$
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
		
//		Label label1 = new Label(composite, SWT.NONE);
//		label1.setText(AspectJUIPlugin
//				.getResourceString("WorkbenchSettingsMigrationPage.ajResourceFilterButton.message")); //$NON-NLS-1$
//		
//		Label spacer = new Label(composite, SWT.NONE);
//		
//		ajResourceFilterButton = new Button(composite, SWT.CHECK);
//		ajResourceFilterButton.setText(AspectJUIPlugin
//				.getResourceString("WorkbenchSettingsMigrationPage.ajResourceFilterButton.label")); //$NON-NLS-1$
//		ajResourceFilterButton.setSelection(true);
//		
//		Label spacer1 = new Label(composite, SWT.NONE);
		
		Label label2 = new Label(composite, SWT.NONE);
		label2.setText(AspectJUIPlugin
				.getResourceString("WorkbenchSettingsMigrationPage.enableRedSquigglesButton.message")); //$NON-NLS-1$

		Label spacer3 = new Label(composite, SWT.NONE);
		
		enableRedSquigglesButton = new Button(composite, SWT.CHECK);
		enableRedSquigglesButton.setText(AspectJUIPlugin
				.getResourceString("WorkbenchSettingsMigrationPage.enableRedSquigglesButton.label")); //$NON-NLS-1$
		enableRedSquigglesButton.setSelection(true);
		
		Label spacer4 = new Label(composite, SWT.NONE);
		
		Label label3 = new Label(composite, SWT.NONE);
		label3.setText(AspectJUIPlugin
				.getResourceString("WorkbenchSettingsMigrationPage.enableUnusedImportsButton.message")); //$NON-NLS-1$

		Label spacer5 = new Label(composite, SWT.NONE);
		
		enableUnusedImportsButton = new Button(composite, SWT.CHECK);
		enableUnusedImportsButton.setText(AspectJUIPlugin
				.getResourceString("WorkbenchSettingsMigrationPage.enableUnusedImportsButton.label")); //$NON-NLS-1$
		enableUnusedImportsButton.setSelection(true);

		Label spacer6 = new Label(composite, SWT.NONE);
		
		Label label4 = new Label(composite, SWT.NONE);
		label4.setText(AspectJUIPlugin
				.getResourceString("WorkbenchSettingsMigrationPage.updateFileAssociations.message")); //$NON-NLS-1$

		Label spacer8 = new Label(composite, SWT.NONE);
		
		fileAssociationsButton = new Button(composite, SWT.CHECK);
		fileAssociationsButton.setText(AspectJUIPlugin
				.getResourceString("WorkbenchSettingsMigrationPage.updateFileAssociations.label")); //$NON-NLS-1$
		fileAssociationsButton.setSelection(true);

	}

	public void finishPressed() {
	    // .aj resource filter is done via plugin.xml
		// enable the .aj resource filter if this button is checked
//		IPreferenceStore javaStore = JavaPlugin.getDefault().getPreferenceStore();
//		javaStore.setValue(FileFilter.ID, ajResourceFilterButton.getSelection());
//		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
//		// don't want to prompt the user in future
//		store.setValue(FileFilter.FILTER_DIALOG_ID, false);
	    
		// turn off red squgglies if this button is checked
		AJDTConfigSettings.disableAnalyzeAnnotations(!(enableRedSquigglesButton.getSelection()));
		
		// update the unused imports setting
		if (enableUnusedImportsButton.getSelection()) {
			AJDTConfigSettings.enableUnusedImports();
		} else {
			AJDTConfigSettings.disableUnusedImports();
		}
		
		// update file associations if need be
		if (fileAssociationsButton.getSelection()) {
			if (AJDTConfigSettings.isAspectJEditorDefault()) {
				AJDTConfigSettings.setDefaultEditorForJavaFiles(false);
			}
			AspectJUIPlugin.getDefault().getPreferenceStore()
				.setToDefault(AspectJPreferences.JAVA_OR_AJ_EXT);
		}		
	}

}
