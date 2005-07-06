/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.builder;

import java.util.Map;
import java.util.Set;

import org.aspectj.ajde.BuildOptionsAdapter;

public class CoreBuildOptions implements BuildOptionsAdapter {

	public Map getJavaOptionsMap() {
		return null;
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

}
