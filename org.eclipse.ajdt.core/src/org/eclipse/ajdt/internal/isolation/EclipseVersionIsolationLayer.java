/**********************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer - initial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.isolation;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * @author colyer
 * 
 * This class is used to encapsulate all usage of APIs that vary across Eclipse
 * versions (we support 2.0 through to 3.0).
 * 
 * MPC (9 Aug 2004): Not currently required - might be needed if changes are
 * required for 3.0.1 and 3.1
 */
public class EclipseVersionIsolationLayer {

	private static final String STRATEGY_PREFIX = 
		"org.eclipse.ajdt.internal.isolation.EclipseVersionStrategyImpl_";
	private static boolean strategyLoaded = false;
	private static EclipseVersionStrategy strategy = null;
	
	public static EclipseVersionStrategy getStrategy() {
		if (!strategyLoaded) {
			loadStrategy();
		}
		return strategy;
	}
	
	/** default constructor private - no instantiation */
	private EclipseVersionIsolationLayer() {}

	private static void loadStrategy() {
		if (!strategyLoaded) {
			// change required for 3.0 compatibility
			//PluginRegistry registry = Platform.getPluginRegistry();
			//IPluginDescriptor desc = 
			//	registry.getPluginDescriptor( ResourcesPlugin.PI_RESOURCES );
			//PluginVersionIdentifier ident = desc.getVersionIdentifier();
		    Bundle bundle = AspectJPlugin.getDefault().getBundle();
			String bundleVersion = (String)bundle.getHeaders().get(Constants.BUNDLE_VERSION);
			PluginVersionIdentifier ident = new PluginVersionIdentifier(bundleVersion);
			
			String version = ident.getMajorComponent() + "_" +
							 ident.getMinorComponent() + "_" +
							 ident.getServiceComponent();
			try {
				Class strategyClass = Class.forName(STRATEGY_PREFIX + version);
				strategy = (EclipseVersionStrategy) strategyClass.newInstance();
			} catch (ClassNotFoundException cnfEx) {
				// AspectJPlugin.getDefault().getErrorHandler().handleError()
				// TODO global exception handling policy in plugin
			} catch (IllegalAccessException illAccEx) {
				// TODO global exception handling policy in plugin				
			} catch (InstantiationException instEx) {
				// TODO global exception handling policy in plugin				
			}
		}
		strategyLoaded = true;
	}

}
