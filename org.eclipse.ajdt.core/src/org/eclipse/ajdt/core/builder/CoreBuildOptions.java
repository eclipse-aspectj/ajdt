/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.builder;

import java.util.Map;
import java.util.Set;

import org.aspectj.ajde.BuildOptionsAdapter;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;

public class CoreBuildOptions implements BuildOptionsAdapter {

	/**
	 * Tries to get a project-specific nature options map if it exists.  
	 * If it is not found returns the JavaCore's map.
	 */
	public Map getJavaOptionsMap() {
		Map optionsMap = null;
			
		JavaProject project;
		try {
			project = (JavaProject)AspectJPlugin.getDefault().getCurrentProject().getNature(JavaCore.NATURE_ID);
			optionsMap = project.getOptions(true);
		} catch (CoreException e) {
		}
		
		if (optionsMap == null) {
			return JavaCore.getOptions();
		} else {
			return optionsMap;
		}
	}

	public boolean getUseJavacMode() {
		return false;
	}

	public String getWorkingOutputPath() {
		return ""; //$NON-NLS-1$
	}

	public boolean getPreprocessMode() {
		return false;
	}

	public String getCharacterEncoding() {
		return null;
	}

	public boolean getSourceOnePointFourMode() {
		return false;
	}

	public boolean getIncrementalMode() {
		return true;
	}

	public boolean getLenientSpecMode() {
		return false;
	}

	public boolean getStrictSpecMode() {
		return false;
	}

	public boolean getPortingMode() {
		return false;
	}

	public String getNonStandardOptions() {
		return ""; //$NON-NLS-1$
	}

	public String getComplianceLevel() {
		return ""; //$NON-NLS-1$
	}

	public String getSourceCompatibilityLevel() {
		return ""; //$NON-NLS-1$
	}

	public Set getWarnings() {
		return null;
	}

	public Set getDebugLevel() {
		return null;
	}

	public boolean getNoImportError() {
		return false;
	}

	public boolean getPreserveAllLocals() {
		return false;
	}

	// Return formatted version of the current build options set
	public String toString() {
		StringBuffer formattedOptions = new StringBuffer();
		formattedOptions.append("Current Compiler options set:"); //$NON-NLS-1$
		formattedOptions.append(
			"[Incremental compilation=" + getIncrementalMode() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		formattedOptions.append(
			"[NonStandard options='" + getNonStandardOptions() + "']"); //$NON-NLS-1$ //$NON-NLS-2$
		return formattedOptions.toString();
	}
}
