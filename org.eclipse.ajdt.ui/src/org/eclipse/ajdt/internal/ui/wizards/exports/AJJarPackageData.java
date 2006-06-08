/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.ajdt.internal.ui.wizards.exports;

import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.eclipse.swt.widgets.Shell;


public class AJJarPackageData extends JarPackageData {

	/**
	 * Creates and returns a JarExportRunnable.
	 *
	 * @param	parent	the parent for the dialog,
	 * 			or <code>null</code> if no questions should be asked and
	 * 			no checks for unsaved files should be made.
	 * @return a JarExportRunnable
	 */
	public IJarExportRunnable createJarExportRunnable(Shell parent) {
		return new AJJarFileExportOperation(this, parent);
	}
	
	/**
	 * Creates and returns a JarExportRunnable for a list of JAR package
	 * data objects.
	 *
	 * @param	jarPackagesData	an array with JAR package data objects
	 * @param	parent			the parent for the dialog,
	 * 							or <code>null</code> if no dialog should be presented
	 * @return the {@link IJarExportRunnable}
	 */
	public IJarExportRunnable createJarExportRunnable(JarPackageData[] jarPackagesData, Shell parent) {
		return new AJJarFileExportOperation(jarPackagesData, parent);
	}
	
}
