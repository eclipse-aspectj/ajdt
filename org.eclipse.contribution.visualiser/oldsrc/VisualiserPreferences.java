/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sian Whiting - initial version
 *******************************************************************************/
package org.eclipse.contribution.visualiser.views.old;

import org.eclipse.contribution.visualiser.VisualiserPlugin;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Holds the preferences for the Visualiser as set via the Workbench->preferences
 * pages or the VisualiserPreferencesDialog , which is launched for the Visualiser
 * view.
 */
public class VisualiserPreferences {

	public static final String MAX_BAR = "org.eclipse.contribution.visualiser.preferences.maxbarwidth";
	public static final String MIN_BAR = "org.eclipse.contribution.visualiser.preferences.minbarwidth";
	public static final String DEMARCATION = "org.eclipse.contribution.visualiser.preferences.demarcation";
	public static final String PROVIDER = "org.eclipse.contribution.visualiser.preferences.provider";
	public static final String STRIPE_SIZE = "org.eclipse.contribution.visualiser.preferences.stripesize";
	public static final String PALETTE = "org.eclipse.contribution.visualiser.preferences.palette"; //$NON-NLS-1$

	/**
	 * Returns true if demarcation is enabled for bars in package view
	 * @return
	 */
	static public boolean isDemarcationEnabled() {
		IPreferenceStore store = VisualiserPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(DEMARCATION);
	}
	
	
	/**
	 * Get the name of the current provider
	 * @return
	 */
	static public String getProvider() {
		IPreferenceStore store = VisualiserPlugin.getDefault().getPreferenceStore();
		return store.getString(PROVIDER);
	}
	
	
	/**
	 * Get the minimum bar width for bars in the visualiser
	 * @return
	 */
	static public int getMinBarSize(){
		IPreferenceStore store = VisualiserPlugin.getDefault().getPreferenceStore();
		return store.getInt(MIN_BAR);
	}
	
	
	/**
	 * Get the maximum bar width for bars in the visualsier
	 * @return
	 */
	static public int getMaxBarSize(){
		IPreferenceStore store = VisualiserPlugin.getDefault().getPreferenceStore();
		return store.getInt(MAX_BAR);
	}
	
	
	/**
	 * Get the minimum stripe height for the visualiser
	 * @return
	 */
	static public int getStripeSize(){
		IPreferenceStore store = VisualiserPlugin.getDefault().getPreferenceStore();
		return store.getInt(STRIPE_SIZE);
	}
	
	/**
	 * Get the name of the chosen palette
	 * @return
	 */
	public static String getPaletteName() {
		return "";
		//IPreferenceStore store = VisualiserPlugin.getDefault().getPreferenceStore();
		//return store.getString(PALETTE);
	}

	/**
	 * Store the given name as the chosen palette name
	 * @param value the palette name
	 */
	public static void setPaletteName(String value) {
//		IPreferenceStore store = VisualiserPlugin.getDefault().getPreferenceStore();
//		store.setValue(PALETTE,value);
//		PaletteManager.setCurrentPaletteByName(value);
	}
}