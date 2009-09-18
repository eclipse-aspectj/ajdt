/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/
package org.eclipse.ajdt.internal.ui.wizards.exports;

import java.net.URI;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.pde.internal.ui.wizards.exports.FeatureExportWizardPage;

/**
 * Extends {@link FeatureExportWizardPage} to allow {@link AJFeatureExportWizard}
 * access to protected methods
 */
public class AJFeatureExportWizardPage extends FeatureExportWizardPage {

	public AJFeatureExportWizardPage(IStructuredSelection selection) {
		super(selection);
	}
	
	protected boolean doExportToDirectory() {
		return super.doExportToDirectory();
	}

	protected boolean useJARFormat() {
		return super.useJARFormat();
	}
	
	protected boolean doExportSource() {
		return super.doExportSource();
	}
	
	protected String getDestination() {
		return super.getDestination();
	}
	
	protected String getFileName() {
		return super.getFileName();
	}
	
	protected String[] getSigningInfo() {
		return super.getSigningInfo();
	}

	protected boolean doMultiPlatform() {
		return super.doMultiPlatform();
	}

	protected String[] getJNLPInfo() {
		return super.getJNLPInfo();
	}

	protected boolean doExportMetadata() {
		return super.doExportMetadata();
	}

	protected String getQualifier() {
		return super.getQualifier();
	}
	
    protected boolean allowBinaryCycles() {
        return super.allowBinaryCycles();
    }
    
    protected boolean useWorkspaceCompiledClasses() {
        return super.useWorkspaceCompiledClasses();
    }

    protected boolean doExportSourceBundles() {
        return super.doExportSourceBundles();
    }
    
    protected boolean doInstall() {
        return super.doInstall();
    }
    
    protected URI getCategoryDefinition() {
        return super.getCategoryDefinition();
    }
}
