/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.wizards.exports;

import org.eclipse.ajdt.internal.core.exports.FeatureExportOperation;
import org.eclipse.ajdt.internal.core.exports.PluginExportOperation;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.ui.PDEUIMessages;


public class AJPluginExportJob extends AJFeatureExportJob {

	public AJPluginExportJob(FeatureExportInfo info) {
		super(info);
	}
	
	protected FeatureExportOperation getOperation() {
		return new PluginExportOperation(fInfo, PDEUIMessages.PluginExportJob_name);
	}

}
