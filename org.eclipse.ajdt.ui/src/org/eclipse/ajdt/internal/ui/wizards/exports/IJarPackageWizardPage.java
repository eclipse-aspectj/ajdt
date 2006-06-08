/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.internal.ui.wizards.exports;

import org.eclipse.jface.wizard.IWizardPage;


/**
 * Copied from org.eclipse.jdt.internal.ui.jarpackager.IJarPackageWizardPage
 * Common interface for all AJ JAR package wizard pages.
 */
interface IJarPackageWizardPage extends IWizardPage {
	/**
	 * Tells the page that the user has pressed finish.
	 */
	void finish();
}
