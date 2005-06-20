/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *     George Harley  - fix for bugzilla 73317
 *******************************************************************************/
package org.eclipse.ajdt.internal.buildconfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.internal.utils.AJDTEventTrace;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.RegistryPageContributor;

/**
 * @author Luzius Meisser
 *
 * This class can be used to register and unregister jdts project property page.
 */
public class PropertyPageManager {
	private static RegistryPageContributor jdtProjectPropertyPage = null;
	private static RegistryPageContributor jdtJavaProjectPropertyPage = null;
	private static boolean jdtPropertyPageActive = true;
	final static String projectID = "org.eclipse.core.resources.IProject";  //$NON-NLS-1$
	final static String javaProjectID = "org.eclipse.jdt.core.IJavaProject";  //$NON-NLS-1$
	final static String buildPathsPageID = "org.eclipse.jdt.ui.propertyPages.BuildPathsPropertyPage";  //$NON-NLS-1$
	
	
	private PropertyPageManager(){}
	
	public static void unregisterJDTPropertyPage(){
		if (jdtProjectPropertyPage == null){
			extractJDTRegistryPageContributors();
		}
		if (isJDTPropertyPageActive()){
			PropertyPageContributorManager ppcm = PropertyPageContributorManager.getManager();
			
			// Get number of contributors prior to attempting the unregistering.
			int beforeCount = getBuildPathContributorsCount(ppcm, buildPathsPageID);
			
			ppcm.unregisterContributor(jdtProjectPropertyPage, projectID);
			ppcm.unregisterContributor(jdtJavaProjectPropertyPage, javaProjectID);
			
			// Get number of contributors after attempting the unregistering.
			int afterCount = getBuildPathContributorsCount(ppcm, buildPathsPageID);
			
			// If the unregistering was successful then the before and after 
			// counts will differ by 2. If they don't then we got our "IJavaProject"
			// and "IProject" contributors mixed up. This can happen because when
			// we set the jdtProjectPropertyPage and jdtJavaProjectPropertyPage
			// values we (AFAIK) have no way of telling their real objectClass
			// property value. Re-try the unregistering using the alternative
			// contributor references.
			if (afterCount != (beforeCount - 2)) {
				ppcm.unregisterContributor(jdtProjectPropertyPage, javaProjectID);
				ppcm.unregisterContributor(jdtJavaProjectPropertyPage, projectID);
				afterCount = getBuildPathContributorsCount(ppcm, buildPathsPageID);
			}
			
			if (afterCount != (beforeCount - 2)) {
				AJDTEventTrace
						.generalEvent("Failed to unregister Java Build Path property pages");
			}
			else {
				AJDTEventTrace
						.generalEvent("Successfully unregistered Java Build Path property pages");
			}
			
			jdtPropertyPageActive = false;
		}
	}
	
	/**
	 * @param ppcm 
	 * @param pageId
	 * @return
	 */
	private static int getBuildPathContributorsCount(
			PropertyPageContributorManager ppcm, String pageId) {
		int count = 0;
		Iterator iter = ppcm.getContributors().iterator();
		while (iter.hasNext()) {
			List v = (List) iter.next();
			Iterator iter2 = v.iterator();
			while (iter2.hasNext()) {
				RegistryPageContributor rpc = (RegistryPageContributor) iter2
						.next();
				if (rpc.getPageId().equals(buildPathsPageID)) {
					count++;
				}
			}
		}
		return count;
	}

	public static void registerJDTPropertyPage(){
		if (jdtProjectPropertyPage == null){
			extractJDTRegistryPageContributors();
		}
		if (!isJDTPropertyPageActive()){
			PropertyPageContributorManager ppcm = PropertyPageContributorManager.getManager();
			ppcm.registerContributor(jdtProjectPropertyPage, projectID);
			ppcm.registerContributor(jdtJavaProjectPropertyPage, javaProjectID);
			jdtPropertyPageActive = true;
		}
	}
	
	private static void extractJDTRegistryPageContributors(){
		List list = new ArrayList(3);
		PropertyPageContributorManager ppcm = PropertyPageContributorManager.getManager();
		Iterator iter = ppcm.getContributors().iterator();
		while (iter.hasNext()){
			List v = (List)iter.next();
			Iterator iter2 = v.iterator();
			while(iter2.hasNext()){
				RegistryPageContributor rpc = (RegistryPageContributor)iter2.next();
				if (rpc.getPageId().equals(buildPathsPageID)){
					list.add(rpc);
				}
			}
		}
		
		// There does not seem to be a way of determining the objectClass
		// attribute of the contributor. That is, we cannot tell which of 
		// the contributors is for the "org.eclipse.jdt.core.IJavaProject"
		// objectClass and which is for "org.eclipse.core.resources.IProject".
		// Being able to distinguish them is essential when it comes to 
		// attempting to unregister the contributions.
		jdtProjectPropertyPage = (RegistryPageContributor)list.get(0);
		jdtJavaProjectPropertyPage = (RegistryPageContributor)list.get(1);
		jdtPropertyPageActive = true;
	}
	
	public static boolean isJDTPropertyPageActive(){
		return jdtPropertyPageActive;
	}
}
