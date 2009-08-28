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
}
