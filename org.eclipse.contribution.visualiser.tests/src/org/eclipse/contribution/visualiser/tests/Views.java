/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial version
 *******************************************************************************/
package org.eclipse.contribution.visualiser.tests;

import junit.framework.TestCase;

import org.eclipse.contribution.visualiser.core.ProviderDefinition;
import org.eclipse.contribution.visualiser.core.ProviderManager;
import org.eclipse.contribution.visualiser.markerImpl.MarkerMarkupProvider;
import org.eclipse.contribution.visualiser.markerImpl.ResourceContentProvider;
import org.eclipse.contribution.visualiser.simpleImpl.FileContentProvider;
import org.eclipse.contribution.visualiser.simpleImpl.FileMarkupProvider;
import org.eclipse.contribution.visualiser.views.Menu;
import org.eclipse.contribution.visualiser.views.Visualiser;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class Views extends TestCase {

  private static Visualiser visView;
  private static Menu vismenuView;
  
  
  private IWorkbenchPage getPage() {
     IWorkbench workbench= PlatformUI.getWorkbench();
	 IWorkbenchWindow window= workbench.getActiveWorkbenchWindow();
	 return window.getActivePage();
  }

  public void showViews() throws PartInitException {
	vismenuView = (Menu)       getPage().showView("org.eclipse.contribution.visualiser.views.Menu"); //$NON-NLS-1$
	visView     = (Visualiser) getPage().showView("org.eclipse.contribution.visualiser.views.Visualiser"); //$NON-NLS-1$
  }
  
  public void hideViews() {
  	getPage().hideView(vismenuView);
  	getPage().hideView(visView);
  }
  
  public void testOne() {
    try {
		showViews();
	} catch (PartInitException e) {
		e.printStackTrace();
		fail("Should be able to show the two visualiser views"); //$NON-NLS-1$
	}
  }
  
  // Helper function
  private ProviderDefinition fetchFileProviderDefinition() {
	ProviderDefinition[] providers = ProviderManager.getAllProviderDefinitions();
	for (int i =0 ;i<providers.length;i++) {
		if (providers[i].getContentProvider() instanceof FileContentProvider) return providers[i];
	}
	return null;
  }
  
  // Check that the 'default' two sample providers are registered
  public void testSimpleproject() {
//	assertTrue("Views should be up after showViews() ?!??! ",viewsonscreen);
	ProviderDefinition[] providers = ProviderManager.getAllProviderDefinitions();
	int important_providers = 0; // Should have reached '2' by end of the next loop !
	StringBuffer providersString = new StringBuffer();
	for (int i = 0 ; i < providers.length; i++) {
		if (providers[i].getContentProvider() instanceof ResourceContentProvider 
				&& providers[i].getMarkupInstance() instanceof MarkerMarkupProvider) {
			important_providers++;
		} else if (providers[i].getContentProvider() instanceof FileContentProvider
				&& providers[i].getMarkupInstance() instanceof FileMarkupProvider) {
			important_providers++;
		}
		providersString.append(providers[i].getName()+" "); //$NON-NLS-1$
	}
	assertTrue("Should have found the Marker and File content providers, but instead found these:["+ //$NON-NLS-1$
	  providersString.toString()+"]",important_providers==2); //$NON-NLS-1$
  }
  
  // Check we can select the File provider successfully
  public void testSelectingProvider() {
  	ProviderDefinition cdp = fetchFileProviderDefinition();
  	ProviderManager.setCurrent(cdp);
  	assertTrue(ProviderManager.getCurrent().getContentProvider() instanceof FileContentProvider);
  	
  }
  
  public void testHideviews() {
    hideViews();
  }
}
