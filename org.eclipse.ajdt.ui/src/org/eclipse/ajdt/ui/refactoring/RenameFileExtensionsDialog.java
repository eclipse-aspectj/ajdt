/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ... 
 ******************************************************************************/

package org.eclipse.ajdt.ui.refactoring;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog that renames file extensions for an entire project. Three options are
 * presented to the user - convert all extensions to .java, convert all
 * extensions to .aj and convert aspects to .aj and classes to .java.
 */
public class RenameFileExtensionsDialog extends Dialog {

	// The project
	private IProject project;

	private Button convertAllToJavaButton;

	private Button convertAllToAJButton;

	private Button convertAspectsToAJButton;

	private Button includeFilesNotInBuildButton;

	private Button updateBuildConfigsButton;

	/**
	 * @param parentShell
	 */
	public RenameFileExtensionsDialog(Shell parentShell, IProject project) {
		super(parentShell);
		this.project = project;
	}

	/**
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(AspectJUIPlugin
				.getResourceString("Refactoring.ConvertFileExtensions")); //$NON-NLS-1$
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		convertAspectsToAJButton = new Button(composite, SWT.RADIO);
		convertAspectsToAJButton
				.setText(AspectJUIPlugin
						.getResourceString("Refactoring.ConvertAspectsToAJAndClassesToJava")); //$NON-NLS-1$
		convertAspectsToAJButton.setSelection(true);

		convertAllToAJButton = new Button(composite, SWT.RADIO);
		convertAllToAJButton.setText(AspectJUIPlugin
				.getResourceString("Refactoring.ConvertAllToAJ")); //$NON-NLS-1$

		convertAllToJavaButton = new Button(composite, SWT.RADIO);
		convertAllToJavaButton.setText(AspectJUIPlugin
				.getResourceString("Refactoring.ConvertAllToJava")); //$NON-NLS-1$

		Label spacer = new Label(composite, SWT.NONE);

		includeFilesNotInBuildButton = new Button(composite, SWT.CHECK);
		includeFilesNotInBuildButton.setText(AspectJUIPlugin
				.getResourceString("Refactoring.IncludeFilesNotInBuild")); //$NON-NLS-1$
		includeFilesNotInBuildButton.setSelection(true);
		updateBuildConfigsButton = new Button(composite, SWT.CHECK);
		updateBuildConfigsButton.setText(AspectJUIPlugin
				.getResourceString("Refactoring.UpdateBuildConfigs")); //$NON-NLS-1$
		updateBuildConfigsButton.setSelection(true);
		applyDialogFont(composite);
		return composite;
	}

	protected void okPressed() {
		boolean updateBuildConfigs = updateBuildConfigsButton.getSelection();
		AspectJUIPlugin.getDefault().disableBuildConfiguratorResourceChangeListener();
		if (convertAllToAJButton.getSelection()) {
			RenamingUtils.convertAllExtensions(true, includeFilesNotInBuildButton
					.getSelection(), updateBuildConfigs, project, getShell());
		} else if (convertAllToJavaButton.getSelection()) {
			RenamingUtils.convertAllExtensions(false, includeFilesNotInBuildButton
					.getSelection(), updateBuildConfigs, project, getShell());
		} else if (convertAspectsToAJButton.getSelection()) {
			RenamingUtils.convertAspectsToAJAndOthersToJava(includeFilesNotInBuildButton
					.getSelection(), updateBuildConfigs, project, getShell());
		}
		super.okPressed();
		AspectJUIPlugin.getDefault().enableBuildConfiguratorResourceChangeListener();
	}

}