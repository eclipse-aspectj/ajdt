/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/
package org.eclipse.ajdt.internal.ui.wizards.exports;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.pde.internal.ui.wizards.exports.PluginExportWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Extends PluginExportWizardPage to allow AJPluginExportWizard
 * access to protected methods
 */
public class AJPluginExportWizardPage extends PluginExportWizardPage {

	public AJPluginExportWizardPage(IStructuredSelection selection) {
		super(selection);
	}
	
	protected boolean doExportToDirectory() {
		return super.doExportToDirectory();
	}

	protected boolean useJARFormat() {
		return super.useJARFormat();
	}
	
	protected boolean doExportSource() {
		return super.doExportSource();
	}
	
	protected String getDestination() {
		return super.getDestination();
	}
	
	protected String getFileName() {
		return super.getFileName();
	}
	
	protected String[] getSigningInfo() {
		return super.getSigningInfo();
	}
	
	protected boolean allowBinaryCycles() {
		return super.allowBinaryCycles();
	}
	
	public void createControl(Composite parent) {
        Label l = new Label(parent, SWT.WRAP);
        l.setText("Warning...this wizard is not working in Eclipse 3.6 and\n" +
                "it will likely be removed before the final release.\n" +
                "For a better solution, please see:\n" +
                "https://bugs.eclipse.org/bugs/show_bug.cgi?id=303960");
        super.createControl(parent);
	}
}
