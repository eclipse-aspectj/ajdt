/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.contribution.xref.core.IXReferenceNode;
import org.eclipse.contribution.xref.core.XReferenceAdapterFactory;
import org.eclipse.contribution.xref.internal.ui.text.XRefMessages;
import org.eclipse.contribution.xref.internal.ui.utils.XRefUIUtils;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class XReferenceUIPlugin extends AbstractUIPlugin {

    //  the plugin id
	public static final String PLUGIN_ID = "org.eclipse.contribution.xref.ui"; //$NON-NLS-1$
	//The shared instance.
	private static XReferenceUIPlugin plugin;

	public static XReferenceView xrefView;
	
	private static Image relImage = null;
	private static Image evaluateImage = null;

	/**
	 * 3.0 compatible constructor
	 */
	public XReferenceUIPlugin() {
		super();
		plugin = this;
	} 
	
	/**
	 * This method is called upon plug-in activation - process any 
	 * defined extensions and add the resource change listener.
	 * 
	 * 3.0 compatible
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		registerAdapter();
	}

	/**
	 * This method is called when the plug-in is stopped
	 * 
	 * 3.0 compatible
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static XReferenceUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Set the XReference view
	 */
	public void setXReferenceView(XReferenceView view) {
		XReferenceUIPlugin.xrefView = view;
	}

	/**
	 * Refresh the XReference view.
	 */
	public static void refresh() {
		if (xrefView != null) {
		 	XReferenceViewUpdateJob.getInstance().schedule();
		}
	}
	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/*
	 * Helper method to register the required resources with a
	 * XReferenceAdapterFactory.
	 *  
	 */
	private void registerAdapter() {
		XReferenceAdapterFactory xra = new XReferenceAdapterFactory();
		Platform.getAdapterManager().registerAdapters(xra, IJavaElement.class);
		Platform.getAdapterManager().registerAdapters(xra, IResource.class);
		Platform.getAdapterManager().registerAdapters(xra, IXReferenceNode.class);
		// below registers the Editor part of the workbench with the XReferenceAdapterFactory
		// this means that it does update when the editor is selected
		Platform.getAdapterManager().registerAdapters(xra, IEditorPart.class);//<--------
	}

	public Image getEvaluateImage() {
		if (evaluateImage == null) {
			try {
				URL pluginInstallURL = Platform.getBundle(PLUGIN_ID).getEntry("/"); //$NON-NLS-1$
				ImageDescriptor d =
					ImageDescriptor.createFromURL(
						new URL(pluginInstallURL, "icons/update.gif")); //$NON-NLS-1$
				evaluateImage = d.createImage();
			} catch (MalformedURLException mex) {
				System.err.println("Couldn't create evaluateImage"); //$NON-NLS-1$
			}
		}
		return evaluateImage;
	}

	public Image getXReferenceImage() {
		if (relImage == null) {
			try {
				URL pluginInstallURL = Platform.getBundle(PLUGIN_ID).getEntry("/"); //$NON-NLS-1$
				ImageDescriptor d =
					ImageDescriptor.createFromURL(
						new URL(pluginInstallURL, "icons/arrow.gif")); //$NON-NLS-1$
				relImage = d.createImage();
			} catch (MalformedURLException mex) {
				System.err.println("Couldn't create relImage"); //$NON-NLS-1$
			}
		}
		return relImage;
	}
	
}

// UIJob that updates the XReference View
class XReferenceViewUpdateJob extends UIJob {
		 private static XReferenceViewUpdateJob theJob;
		 
		 private XReferenceViewUpdateJob(String name){
		 		 super (name);
		 }
		 
		 public static XReferenceViewUpdateJob getInstance() {
	 		 if(theJob == null) {
 		 		 theJob = new XReferenceViewUpdateJob(XRefMessages.XRefUIPlugin_Jobs_XRefViewUpdate);
 		 		 theJob.setSystem(true);
 		 		 theJob.setPriority(Job.SHORT);
	 		 }
	 		 return theJob;
		 }
		 
		/* (non-Javadoc)
		 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
		 */
		 public IStatus runInUIThread(IProgressMonitor monitor) {
				monitor.beginTask(XRefMessages.XRefUIPlugin_Jobs_Update, 1);
		 		if (XReferenceUIPlugin.xrefView !=null) {
		 			IWorkbenchPart workbenchPart = null;
		 			if (XRefUIUtils.getActiveWorkbenchWindow() != null) {
		 				workbenchPart = XRefUIUtils.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		 			}
		 			// fix for bug 107719 and 107589 and also required for 
		 			// enhancement 95724 when refreshing the xref views.
		 			ISelection selection = null;
		 			if (workbenchPart != null && workbenchPart.equals(XReferenceUIPlugin.xrefView.getLastSelectedWorkbenchPart())) {
		 				// if the active workbench part is the same as the last active
		 				// workbench part recorded in the XReferenceView, then the
		 				// selection needs to be the corresponding last selection
		 				// recorded in the XReferenceView.
		 				selection = XReferenceUIPlugin.xrefView.getLastSelection();
					} else {
						selection = XRefUIUtils.getCurrentSelection();
					}
		 			
		 		 	XReferenceUIPlugin.xrefView.setChangeDrivenByBuild(true);
		 		 	XReferenceUIPlugin.xrefView.selectionChanged(workbenchPart,selection);		

					
		 		 	XReferenceUIPlugin.xrefView.setChangeDrivenByBuild(false);
		 		}
		 		monitor.done();
		 		return Status.OK_STATUS;
			}
		 
}

