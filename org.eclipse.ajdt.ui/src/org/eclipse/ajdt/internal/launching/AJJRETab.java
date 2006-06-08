/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.launching;

import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.JavaRuntime;

public class AJJRETab extends JavaJRETab {

	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		boolean valid = super.isValid(config);
		if (valid) {
			// bug 132132: LTW requires a >= 1.4 jre
			IVMInstall jre = JavaRuntime.getVMInstall(fJREBlock.getPath());
			if (jre instanceof IVMInstall2) {
				String version = ((IVMInstall2)jre).getJavaVersion();
				if (version.charAt(0) == '1'
						&& version.charAt(1) == '.'
						&& (version.charAt(2) >= '0' && version.charAt(2) <= '3')) {
					setErrorMessage(UIMessages.LTW_error_wrong_jre);
					return false;
				}
			}
		}
		return valid;
	}
}
