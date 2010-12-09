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

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.contribution.xref.core.IXReference;

/**
 * <p>
 * IXReferenceProvider is used to contribute cross references.
 * </p>
 * <p>
 * To connect a cross reference provider into the reference service use the
 * <code>org.eclipse.contributions.xref.core.providers</code> extension point. The 
 * following example shows the plugin.xml fragment used to connect the
 * TypeProvider reference provider that displays "extends" and "implements"
 * references for all ITypes: 
 * </p>   
 * <pre>
 *    &lt;extension
 *          point="org.eclipse.contribution.xref.core.providers"&gt;
 *          &lt;xrefProvider
 *                class="org.eclipse.contribution.xref.internal.providers.TypeProvider"
 *                label="extends/implements"
 *                enabled="true"/&gt;
 *          &lt;/xrefProvider&gt;          
 *    &lt;/extension&gt;
 * </pre>
 * <p>See the documentation of the <code>org.eclipse.contribution.xref.core.providers</code> 
 * extension point for more details of this mechanism.</p>
 */
public interface IXReferenceProvider {
	/**
	 * The set of classes/interfaces for which this provider will
	 * contribute references. The getXReferences method will be
	 * called on this provider any time that someone requests the
	 * references for an object that is an instance of one of the
	 * types returned from this call. 
	 * <p>A call to this method must always return the same class set
	 * (dynamic modification of the class set is not supported).</p>
	 */
	public Class<?>[] getClasses();
	
	/**
     * Get the collection of {@link IXReference}s for the Object o. "o" is 
     * guaranteed to be non-null and of a type returned by getClasses.
     * This method will be called in "user time" and should have a 
     * sub-second response time. To contribute cross references that cannot 
     * guarantee to be computed in that timeframe, return an 
     * IDeferredXReference. See the 
     * <code>org.eclipse.contributions.xref.internal.providers.ProjectReferencesProvider</code> 
     * for an example of a provider that uses this technique.
     * @param o the object to get cross references for
     * @return {@link IXReference} collection of cross references for "o". If there
     * are no cross references to be contributed, either an empty collection or
     * null is an acceptable return value.
     * 
     * @deprecated use {@link IXReferenceProviderExtension#getXReferences(IAdaptable, List)} instead.  
	 */
	public Collection<IXReference> getXReferences(Object o, List<String> l);
	
	public IJavaElement[] getExtraChildren(IJavaElement je);

	/**
	 * Returns a description of the provider suitable for display 
	 * in a user interface.
	 */
	public String getProviderDescription();
	
	/**
	 * Enables the provider to handle the list of items to be filtered from the
	 * Cross References View
	 * 
	 * @param List of Strings corresponding to the items checked by the user
	 * to indicate the items to exclude in the Cross References View
	 */
	public void setCheckedFilters(List<String> l);

	/**
	 * Enables the provider to handle the list of items to be filtered from the
	 * Cross References Inplace View
	 * 
	 * @param List of Strings corresponding to the items checked by the user
	 * to indicate the items to exclude in the Cross References Inplace View
	 */
	public void setCheckedInplaceFilters(List<String> l);
	
	/**
	 * Returns a List of Strings corresponding to the items previously checked
	 * by the user to populate the Cross References View
	 */
	public List<String> getFilterCheckedList();
	
	/**
	 * Returns a List of Strings corresponding to the items previously checked
	 * by the user to populate the Cross References Inplace View
	 */
	public List<String> getFilterCheckedInplaceList();
	
	/**
	 * Returns a List of Strings corresponding to the items used to populate the checkBox with
	 */
	public List<String> getFilterList();
	
	/**
	 * Returns a List of Strings corresponding to the items specified to
	 * be checked by default in the Cross References Views
	 */
	public List<String> getFilterDefaultList();
	
	/**
	 * Providers are contributed by other plugins, and should be considered untrusted 
	 * code. Whenever we call such code, it should be wrapped in an ISafeRunnable. 
	 */
	static aspect SafeExecution {
		pointcut untrustedCall() : call(* IXReferenceProvider.*(..));
		
		Object around() : untrustedCall() {
			ISafeRunnableWithReturn safeRunnable = new ISafeRunnableWithReturn() {
				Object result = null;
				public void handleException(Throwable e) {
					// don't log the exception....it is already being logged in Workspace#run
				}
				public void run() throws Exception {
					result = proceed();
				}
				public Object getResult() {
					return result;
				}
			};
			SafeRunner.run(safeRunnable);
			return safeRunnable.getResult();
		}
		
		interface ISafeRunnableWithReturn extends ISafeRunnable{
			Object getResult();
		}
	}
}
