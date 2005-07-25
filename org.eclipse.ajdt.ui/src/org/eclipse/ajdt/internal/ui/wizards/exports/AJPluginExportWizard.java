/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.ajdt.internal.ui.wizards.exports;

import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.wizards.exports.PluginExportJob;
import org.eclipse.pde.internal.ui.wizards.exports.PluginExportWizard;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * Mostly copied from PluginExportWizard.
 * Enables AspectJ plugins to be correctly exported.
 */
public class AJPluginExportWizard extends PluginExportWizard {

	/**
	 * The constructor.
	 */
	public AJPluginExportWizard() {
		setDefaultPageImageDescriptor(PDEPluginImages.DESC_PLUGIN_EXPORT_WIZ);
		setWindowTitle("Export Plugins and Fragments with AspectJ Support");
	}
	

	protected void scheduleExportJob() {
		PluginExportJob job =
			new AJPluginExportJob(
				page1.getExportType(),
				page1.doExportSource(),
				page1.getDestination(),
				page1.getFileName(),
				page1.getSelectedItems());
		job.setUser(true);
		job.schedule();
		job.setProperty(IProgressConstants.ICON_PROPERTY, PDEPluginImages.DESC_PLUGIN_OBJ);
	}
}
