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
package org.eclipse.contribution.xref.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.contribution.xref.ras.NoFFDC;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * Registry for all cross reference providers extending the
 * org.eclipse.contribution.xref.providers extension point.
 * 
 */
public class XReferenceProviderManager implements NoFFDC {

	private static XReferenceProviderManager theManager;
	private List<XReferenceProviderDefinition> providerList;
	private static final String PROVIDERS_EXTENSION_POINT =
		"org.eclipse.contribution.xref.core.providers"; //$NON-NLS-1$
	
	// Identifies to the Provider which View requires its attention, (Inplace or View) (BUG 95724)
	private boolean isInplace;

	private XReferenceProviderManager() {
		// By default assume the Cross Reference View requires its attention
		isInplace = false;
	};

	public static XReferenceProviderManager getManager() {
		if (theManager == null) {
			theManager = new XReferenceProviderManager();
		}
		return theManager;
	}
	
	public void setIsInplace(boolean isInplace) {
		this.isInplace = isInplace;
	}
	
	public boolean getIsInplace() {
		return isInplace;
	}

	/**
	 * @param Object o
	 * @return list of providers for o from the list of
	 * registered providers for the extension point 
	 * org.eclipse.contribution.xref.providers
	 */
	public List<IXReferenceProvider> getProvidersFor(IAdaptable o) {
		List<IXReferenceProvider> providers = new ArrayList<IXReferenceProvider>();
		List<XReferenceProviderDefinition> registeredProviders = getRegisteredProviders();
		for (XReferenceProviderDefinition element : registeredProviders) {
			if (providesReferencesFor(o, element)) {
				providers.add(element.getProvider());
			}
		}
		return providers;
	}

	/**
	 * @return list of registered providers for the extension
	 * point org.eclipse.contribtuion.xref.providers
	 */
	public List<XReferenceProviderDefinition> getRegisteredProviders() {
		if (providerList == null) {
			providerList = new ArrayList<XReferenceProviderDefinition>();
			IExtensionPoint exP =
				Platform.getExtensionRegistry().getExtensionPoint(PROVIDERS_EXTENSION_POINT);
			IExtension[] exs = exP.getExtensions();

			for (int i = 0; i < exs.length; i++) {
				IConfigurationElement[] ces = exs[i].getConfigurationElements();
				for (IConfigurationElement ce : ces) {
					try {
						XReferenceProviderDefinition def =
							new XReferenceProviderDefinition(ce);
						providerList.add(def);
					} catch (CoreException e) {
						IStatus status =
							new Status(
								IStatus.WARNING,
								XReferencePlugin.PLUGIN_ID,
								XReferencePlugin.ERROR_BAD_PROVIDER,
								"Could not load provider " //$NON-NLS-1$
									+ ce.getAttribute("id"), //$NON-NLS-1$
								e);
						CoreException toLog = new CoreException(status);
						XReferencePlugin.log(toLog);
					}
				}
			}
		}
		return providerList;
	}

	private boolean providesReferencesFor(
		Object o,
		XReferenceProviderDefinition element) {
		if (!element.isEnabled()) {
			return false;
		}
		IXReferenceProvider provider = element.getProvider();
		if (provider != null) {
		    @SuppressWarnings("rawtypes")
            Class[] servedClasses = provider.getClasses();
		    if (servedClasses != null) {
		        for (int i = 0; i < servedClasses.length; i++) {
		            if (servedClasses[i].isInstance(o)) {
		                return true;
		            }
		        }
			}
		}
		return false;
	}
}
