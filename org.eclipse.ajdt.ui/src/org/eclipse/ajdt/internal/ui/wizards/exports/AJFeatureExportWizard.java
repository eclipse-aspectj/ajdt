/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation, SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Felix Velasco - adapted for use with AspectJ
 *     Andrew Eisenberg - adapted for use with AspectJ
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.wizards.exports;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.pde.internal.build.site.QualifierReplacer;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.build.RuntimeInstallJob;
import org.eclipse.pde.internal.ui.wizards.exports.AntGeneratingExportWizard;
import org.eclipse.pde.internal.ui.wizards.exports.BaseExportWizardPage;
import org.eclipse.pde.internal.ui.wizards.exports.CrossPlatformExportPage;
import org.eclipse.pde.internal.ui.wizards.exports.FeatureExportWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @created Sep 17, 2009
 * Largely copied from {@link FeatureExportWizard}
 * 
 * AspectJ Changes marked with  // AspectJ Change
 */
public class AJFeatureExportWizard extends AntGeneratingExportWizard {
	private static final String STORE_SECTION = "FeatureExportWizard"; //$NON-NLS-1$
	private CrossPlatformExportPage fPage2;

	/**
	 * The constructor.
	 */
	public AJFeatureExportWizard() {
		setDefaultPageImageDescriptor(PDEPluginImages.DESC_FEATURE_EXPORT_WIZ);
	}

	public void addPages() {
		super.addPages();
		FeatureModelManager manager = PDECore.getDefault().getFeatureModelManager();
		IFeatureModel model = manager.getDeltaPackFeature();
		if (model != null) {
			fPage2 = new CrossPlatformExportPage("environment", model); //$NON-NLS-1$
			addPage(fPage2);
		}
	}

	protected BaseExportWizardPage createPage1() {
		// AspectJ change - use AJPluginExportWizardPage
		return new AJFeatureExportWizardPage(getSelection());
	}

	protected String getSettingsSectionName() {
		return STORE_SECTION;
	}

	protected void scheduleExportJob() {
		final FeatureExportInfo info = new FeatureExportInfo();
		info.toDirectory = ((AJFeatureExportWizardPage) fPage).doExportToDirectory();  // AspectJ Change
		info.useJarFormat = ((AJFeatureExportWizardPage) fPage).useJARFormat();  // AspectJ Change
		info.exportSource = ((AJFeatureExportWizardPage) fPage).doExportSource();  // AspectJ Change
        info.exportSourceBundle = ((AJFeatureExportWizardPage) fPage).doExportSourceBundles();  // AspectJ Change
        info.allowBinaryCycles = ((AJFeatureExportWizardPage) fPage).allowBinaryCycles();  // AspectJ Change
        info.useWorkspaceCompiledClasses = ((AJFeatureExportWizardPage) fPage).useWorkspaceCompiledClasses();  // AspectJ Change
		info.destinationDirectory = ((AJFeatureExportWizardPage) fPage).getDestination();  // AspectJ Change
		info.zipFileName = ((AJFeatureExportWizardPage) fPage).getFileName();  // AspectJ Change
		if (fPage2 != null && ((AJFeatureExportWizardPage) fPage).doMultiPlatform())  // AspectJ Change
			info.targets = fPage2.getTargets();
		info.exportMetadata = ((AJFeatureExportWizardPage) fPage).doExportMetadata();  // AspectJ Change
		info.items = ((AJFeatureExportWizardPage) fPage).getSelectedItems();  // AspectJ Change
		info.signingInfo = ((AJFeatureExportWizardPage) fPage).getSigningInfo();  // AspectJ Change
		info.jnlpInfo = ((AJFeatureExportWizardPage) fPage).getJNLPInfo();  // AspectJ Change
		info.qualifier = ((AJFeatureExportWizardPage) fPage).getQualifier();  // AspectJ Change
		
        if (((AJFeatureExportWizardPage) fPage).getCategoryDefinition() != null) // AspectJ Change
            info.categoryDefinition = URIUtil
                    .toUnencodedString(((AJFeatureExportWizardPage) fPage)
                            .getCategoryDefinition());
        
        
        final boolean installAfterExport = ((AJFeatureExportWizardPage) fPage).doInstall(); // AspectJ Change
        if (installAfterExport) {
            info.useJarFormat = true;
            info.exportMetadata = true;
            if (info.qualifier == null) {
                // Set the date explicitly since the time can change before the install job runs 
                info.qualifier = QualifierReplacer.getDateQualifier();
            }
        }


		final AJFeatureExportJob job = new AJFeatureExportJob(info);  // AspectJ Change
        job.setUser(true);
        job.setRule(ResourcesPlugin.getWorkspace().getRoot());
        job.setProperty(IProgressConstants.ICON_PROPERTY, PDEPluginImages.DESC_FEATURE_OBJ);
        job.addJobChangeListener(new JobChangeAdapter() {
            public void done(IJobChangeEvent event) {
                if (job.getOperation().hasAntErrors()) {
                    // If there were errors when running the ant scripts, inform the user where the logs can be found.
                    final File logLocation = new File(info.destinationDirectory, "logs.zip"); //$NON-NLS-1$
                    if (logLocation.exists()) {
                        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                            public void run() {
                                AntErrorDialog dialog = new AntErrorDialog(logLocation);
                                dialog.open();
                            }
                        });
                    }
                } else if (event.getResult().isOK() && installAfterExport) {
                    // Install the export into the current running platform
                    RuntimeInstallJob installJob = new RuntimeInstallJob(PDEUIMessages.PluginExportWizard_InstallJobName, info);
                    installJob.setUser(true);
                    installJob.setProperty(IProgressConstants.ICON_PROPERTY, PDEPluginImages.DESC_FEATURE_OBJ);
                    installJob.schedule();
                }
            }
        });
        job.schedule();
	}

	protected Document generateAntTask() {
		try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc = factory.newDocumentBuilder().newDocument();
            Element root = doc.createElement("project"); //$NON-NLS-1$
            root.setAttribute("name", "build"); //$NON-NLS-1$ //$NON-NLS-2$
            root.setAttribute("default", "feature_export"); //$NON-NLS-1$ //$NON-NLS-2$
            doc.appendChild(root);

            Element target = doc.createElement("target"); //$NON-NLS-1$
            target.setAttribute("name", "feature_export"); //$NON-NLS-1$ //$NON-NLS-2$
            root.appendChild(target);

            Element export = doc.createElement("pde.exportFeatures"); //$NON-NLS-1$
            export.setAttribute("features", getFeatureIDs()); //$NON-NLS-1$
            export.setAttribute("destination", ((AJFeatureExportWizardPage) fPage).getDestination()); //$NON-NLS-1$  // AspectJ Change
            String filename = ((AJFeatureExportWizardPage) fPage).getFileName();  // AspectJ Change
            if (filename != null)
                export.setAttribute("filename", filename); //$NON-NLS-1$
            export.setAttribute("exportType", getExportOperation()); //$NON-NLS-1$
            export.setAttribute("useJARFormat", Boolean.toString(((AJFeatureExportWizardPage) fPage).useJARFormat())); //$NON-NLS-1$  // AspectJ Change
            export.setAttribute("exportSource", Boolean.toString(((AJFeatureExportWizardPage) fPage).doExportSource())); //$NON-NLS-1$  // AspectJ Change
            String qualifier = ((AJFeatureExportWizardPage) fPage).getQualifier();  // AspectJ Change
            if (qualifier != null)
                export.setAttribute("qualifier", qualifier); //$NON-NLS-1$
            target.appendChild(export);
            return doc;
		} catch (DOMException e) {
		} catch (FactoryConfigurationError e) {
		} catch (ParserConfigurationException e) {
		}
		return null;
	}

	private String getFeatureIDs() {
		StringBuffer buffer = new StringBuffer();
		Object[] objects = ((AJFeatureExportWizardPage) fPage).getSelectedItems();  // AspectJ Change
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			if (object instanceof IFeatureModel) {
				buffer.append(((IFeatureModel) object).getFeature().getId());
				if (i < objects.length - 1)
					buffer.append(","); //$NON-NLS-1$
			}
		}
		return buffer.toString();
	}

}
