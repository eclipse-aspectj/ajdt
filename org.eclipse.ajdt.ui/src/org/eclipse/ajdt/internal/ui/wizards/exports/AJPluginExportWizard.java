/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.ajdt.internal.ui.wizards.exports;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.wizards.exports.AntGeneratingExportWizard;
import org.eclipse.pde.internal.ui.wizards.exports.BaseExportWizardPage;
import org.eclipse.ui.progress.IProgressConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Mostly copied from PluginExportWizard.
 * Enables AspectJ plugins to be correctly exported. Changes marked // AspectJ change
 */
public class AJPluginExportWizard extends AntGeneratingExportWizard {

	private static final String STORE_SECTION = "PluginExportWizard"; //$NON-NLS-1$

	public AJPluginExportWizard() {
		setDefaultPageImageDescriptor(PDEPluginImages.DESC_PLUGIN_EXPORT_WIZ);
	}

	protected BaseExportWizardPage createPage1() {
		// AspectJ change - use AJPluginExportWizardPage
		return new AJPluginExportWizardPage(getSelection());
	}
	
	protected String getSettingsSectionName() {
		return STORE_SECTION;
	}
	
	protected void scheduleExportJob() {
		FeatureExportInfo info = new FeatureExportInfo();
		info.toDirectory = ((AJPluginExportWizardPage)fPage).doExportToDirectory();
		info.useJarFormat = ((AJPluginExportWizardPage)fPage).useJARFormat();
		info.exportSource = ((AJPluginExportWizardPage)fPage).doExportSource();
		info.destinationDirectory = ((AJPluginExportWizardPage)fPage).getDestination();
		info.zipFileName = ((AJPluginExportWizardPage)fPage).getFileName();
		info.items = ((AJPluginExportWizardPage)fPage).getSelectedItems();
		info.signingInfo = ((AJPluginExportWizardPage)fPage).useJARFormat() ? ((AJPluginExportWizardPage)fPage).getSigningInfo() : null;
		info.allowBinaryCycles = ((AJPluginExportWizardPage)fPage).allowBinaryCycles(); 
		
		
		AJPluginExportJob job = new AJPluginExportJob(info);
		job.setUser(true);
		job.schedule();
		job.setProperty(IProgressConstants.ICON_PROPERTY, PDEPluginImages.DESC_PLUGIN_OBJ);
	}

	protected Document generateAntTask() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			Document doc = factory.newDocumentBuilder().newDocument();
			Element root = doc.createElement("project"); //$NON-NLS-1$
			root.setAttribute("name", "build"); //$NON-NLS-1$ //$NON-NLS-2$
			root.setAttribute("default", "plugin_export"); //$NON-NLS-1$ //$NON-NLS-2$
			doc.appendChild(root);
			
			Element target = doc.createElement("target"); //$NON-NLS-1$
			target.setAttribute("name", "plugin_export"); //$NON-NLS-1$ //$NON-NLS-2$
			root.appendChild(target);
			
			Element export = doc.createElement("pde.exportPlugins"); //$NON-NLS-1$
			export.setAttribute("plugins", getPluginIDs()); //$NON-NLS-1$
			export.setAttribute("destination", ((AJPluginExportWizardPage)fPage).getDestination()); //$NON-NLS-1$
			String filename = ((AJPluginExportWizardPage)fPage).getFileName();
			if (filename != null)
				export.setAttribute("filename", filename); //$NON-NLS-1$
			export.setAttribute("exportType", getExportOperation());  //$NON-NLS-1$
			export.setAttribute("useJARFormat", Boolean.toString(((AJPluginExportWizardPage)fPage).useJARFormat()));  //$NON-NLS-1$
			export.setAttribute("exportSource", Boolean.toString(((AJPluginExportWizardPage)fPage).doExportSource()));  //$NON-NLS-1$
			target.appendChild(export);
			return doc;
		} catch (DOMException e) {
		} catch (FactoryConfigurationError e) {
		} catch (ParserConfigurationException e) {
		}
		return null;
	}
	
	private String getPluginIDs() {
		StringBuffer buffer = new StringBuffer();
		Object[] objects = fPage.getSelectedItems();
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			if (object instanceof IPluginModelBase) {
				buffer.append(((IPluginModelBase)object).getPluginBase().getId());
				if (i < objects.length - 1)
					buffer.append(",");					 //$NON-NLS-1$
			}
		}
		return buffer.toString();
	}

}
