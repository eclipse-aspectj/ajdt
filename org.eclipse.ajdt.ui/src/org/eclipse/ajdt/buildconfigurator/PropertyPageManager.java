/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.buildconfigurator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

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
	final static String projectID = "org.eclipse.core.resources.IProject";
	final static String javaProjectID = "org.eclipse.jdt.core.IJavaProject";
	
	
	private PropertyPageManager(){}
	
	public static void unregisterJDTPropertyPage(){
		//System.out.println("unregister called");
		if (jdtProjectPropertyPage == null){
			extractJDTRegistryPageContributors();
		}
		if (isJDTPropertyPageActive()){
			PropertyPageContributorManager ppcm = PropertyPageContributorManager.getManager();
			ppcm.unregisterContributor(jdtProjectPropertyPage, projectID);
			ppcm.unregisterContributor(jdtJavaProjectPropertyPage, javaProjectID);
			jdtPropertyPageActive = false;
			//System.out.println("unregister executed");
			
			//printNames();
		}
	}
	
	public static void registerJDTPropertyPage(){
		//System.out.println("register called");
		if (jdtProjectPropertyPage == null){
			extractJDTRegistryPageContributors();
		}
		if (!isJDTPropertyPageActive()){
			PropertyPageContributorManager ppcm = PropertyPageContributorManager.getManager();
			ppcm.registerContributor(jdtProjectPropertyPage, projectID);
			ppcm.registerContributor(jdtJavaProjectPropertyPage, javaProjectID);
			jdtPropertyPageActive = true;
			//System.out.println("register executed");
			
			//printNames();
		}
	}
	
	private static void extractJDTRegistryPageContributors(){
		ArrayList list = new ArrayList(3);
		PropertyPageContributorManager ppcm = PropertyPageContributorManager.getManager();
		Iterator iter = ppcm.getContributors().iterator();
		while (iter.hasNext()){
			Vector v = (Vector)iter.next();
			Iterator iter2 = v.iterator();
			while(iter2.hasNext()){
				RegistryPageContributor rpc = (RegistryPageContributor)iter2.next();
				if (rpc.getPageId().equals("org.eclipse.jdt.ui.propertyPages.BuildPathsPropertyPage")){
					list.add(rpc);
				}
			}
		}
		
		jdtProjectPropertyPage = (RegistryPageContributor)list.get(0);
		jdtJavaProjectPropertyPage = (RegistryPageContributor)list.get(1);
		jdtPropertyPageActive = true;
	}
	
	public static boolean isJDTPropertyPageActive(){
		return jdtPropertyPageActive;
	}
}
