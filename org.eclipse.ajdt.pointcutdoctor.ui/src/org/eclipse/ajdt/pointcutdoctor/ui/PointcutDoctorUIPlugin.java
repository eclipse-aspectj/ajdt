/*******************************************************************************
 * Copyright (c) 2007 Linton Ye.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Linton Ye - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.ui;

import org.eclipse.ajdt.pointcutdoctor.core.PointcutDoctorCorePlugin;
import org.eclipse.ajdt.pointcutdoctor.core.PointcutRelaxMungerFactory;
import org.eclipse.ajdt.pointcutdoctor.core.almost.AlmostJPSPluginFacade;
import org.eclipse.ajdt.pointcutdoctor.core.explain.Explainer;
import org.eclipse.ajdt.pointcutdoctor.core.explain.Reason;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class PointcutDoctorUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ajdt.pointcutdoctor.ui";

	// The shared instance
	private static PointcutDoctorUIPlugin plugin;
 
	/**
	 * When the pointcut doctor is not enabled, it shall not impact weaving or memory usage.
	 */
	private boolean isEnabled = false;

	//////////////////////////////////////////////////////////////////////////////////////////
	///// plugin state... needs cleaning up when deactivated and initializing when activated
	
	private PointcutDoctorCorePlugin corePlugin;
	private XReferenceTreeSelectionListener xRefSelectionListener;
	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	public PointcutDoctorUIPlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	private void enable() {
		corePlugin = new PointcutDoctorCorePlugin();
		initXRefViewListener();
	}
	
	private void disable() {
		if (corePlugin!=null) {
			corePlugin.dispose();
			corePlugin = null;
		}
		deregisterXRefViewListener();
	}

	private void initXRefViewListener() {
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow window:windows) {
			ISelectionService service = window.getSelectionService();
			xRefSelectionListener = new XReferenceTreeSelectionListener(this);
			service.addSelectionListener(XReferenceView.ID, xRefSelectionListener);
//			setupLabelProvider(window);
		}
	}
	
	private void deregisterXRefViewListener() {
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow window:windows) {
			ISelectionService service = window.getSelectionService();
			service.removeSelectionListener(XReferenceView.ID, xRefSelectionListener);
		}
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		setEnabled(false);
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static PointcutDoctorUIPlugin getDefault() {
		return plugin;
	}

	public Explainer getExplainer() {
		return getCorePlugin().getExplainer();
	}

	private PointcutDoctorCorePlugin getCorePlugin() {
		Assert.isTrue(isEnabled(), "The pointcut doctor is not enabled");
		if (corePlugin==null) {
			corePlugin = new PointcutDoctorCorePlugin();
		}
		return corePlugin;
	}

	public boolean isEnabled() {
		return isEnabled;
	}
	
	public void setEnabled(boolean enable) {
		if (enable==isEnabled) return;
		if (enable) 
			enable();
		else
			disable();
		isEnabled = enable;
	}

	public AlmostJPSPluginFacade getAlmostJPSPluginFacade() {
		return corePlugin.getAlmostJPSPluginFacade();
	}

	private Reason lastReasonExplained = null;
	
	/**
	 * @param filename
	 *            This file name has to be absolute path, produced by toOSString
	 *            for example
	 * @param offset
	 * @return the textual reason for a specific part at the location specified,
	 *         returns null if no such reason exists
	 */
	public String getTexualReasonByFileAndOffset(String filename, int offset) {
		if (lastReasonExplained != null) {
			return lastReasonExplained.getTexualReasonByFileAndOffset(filename,
					offset);
		} else
			return null;
	}
	
	public Reason explain(ShadowNode sn) {
		PointcutRelaxMungerFactory factory = getAlmostJPSPluginFacade().getPointcutRelaxMungerFactory(sn
				.getProject());
		lastReasonExplained = getExplainer().explain(sn.getMainPointcut(),
				sn.getShadowWrapper(), factory);
		return lastReasonExplained;
	}

}
