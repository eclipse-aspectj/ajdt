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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.swt.widgets.Display;

// copied from org.eclipse.pde.internal.ui.build.FeatureExportJob
public class AJFeatureExportJob extends Job {
	
	class SchedulingRule implements ISchedulingRule {
		public boolean contains(ISchedulingRule rule) {
			return rule instanceof SchedulingRule || rule instanceof IResource;
		}

		public boolean isConflicting(ISchedulingRule rule) {
			return rule instanceof SchedulingRule;
		}
	}

	protected FeatureExportInfo fInfo;
	protected FeatureExportOperation op;
	
	public AJFeatureExportJob(FeatureExportInfo info) {
		super(PDEUIMessages.FeatureExportJob_name); 
		fInfo = info;
		setRule(new SchedulingRule());
	}

	protected IStatus run(IProgressMonitor monitor) {
		 op = getOperation();
		return op.run(monitor);
	}
	
	protected FeatureExportOperation getOperation() {
	    if (op == null) {
	        op = new FeatureExportOperation(fInfo, PDEUIMessages.FeatureExportJob_name);
	    }
	    return op;
	}
	
	/**
	 * Returns the standard display to be used. The method first checks, if the
	 * thread calling this method has an associated disaply. If so, this display
	 * is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		return display;
	}

	private void asyncNotifyExportException(String errorMessage) {
		getStandardDisplay().beep();
		MessageDialog.openError(PDEPlugin.getActiveWorkbenchShell(), "Export feature error", errorMessage); 
		done(new Status(IStatus.OK, PDEPlugin.getPluginId(), IStatus.OK, "", null)); //$NON-NLS-1$
	}

	protected String getLogFoundMessage() {
		return NLS.bind("Export feature error", fInfo.destinationDirectory); 
	} 

}
