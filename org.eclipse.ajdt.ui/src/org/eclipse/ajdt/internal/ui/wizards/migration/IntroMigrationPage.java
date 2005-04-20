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

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This is the introdcutory page to the migration wizard - giving a brief
 * outline of what is covered in the wizard and pointing out that to 
 * keep the defaults, the user can just press finish. 
 */
public class IntroMigrationPage extends WizardPage {
	
	protected IntroMigrationPage() {
		super(AspectJUIPlugin.getResourceString("IntroMigrationPage.name"));
		this.setTitle(AspectJUIPlugin.getResourceString("IntroMigrationPage.title"));		
		this.setDescription(AspectJUIPlugin.getResourceString("IntroMigrationPage.description"));
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(composite, SWT.NONE);
		label.setText(AspectJUIPlugin.getResourceString("IntroMigrationPage.message"));
		setControl(composite);
		
		Label spacer = new Label(composite, SWT.NONE);
	
		Label label2 = new Label(composite, SWT.NONE);
		label2.setText(AspectJUIPlugin.getResourceString("IntroMigrationPage.message2"));
		setControl(composite);

	}

}
