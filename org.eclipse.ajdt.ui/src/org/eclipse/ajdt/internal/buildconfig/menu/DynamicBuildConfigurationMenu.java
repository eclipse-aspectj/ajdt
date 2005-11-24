/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/

package org.eclipse.ajdt.internal.buildconfig.menu;

import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.IBuildConfigurationChangedListener;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Luzius Meisser
 * 
 * Submenu displayed in the Project menu.
 *  
 */
public class DynamicBuildConfigurationMenu extends MenuManager implements
		IBuildConfigurationChangedListener {
	BuildConfigurator buildconf;

	IContributionItem separator;

	private ImageDescriptor id;
	
	public DynamicBuildConfigurationMenu(String menuID) {
		super(AspectJUIPlugin
				.getResourceString("BCLabels.ConfigurationSelectionMenu"),menuID); //$NON-NLS-1$
		separator = new Separator();

		id = AspectJImages.BC_TICK.getImageDescriptor();
		buildconf = BuildConfigurator.getBuildConfigurator();
		buildconf.addBuildConfigurationChangedListener(this);
		IAction addAction = new AddBuildConfigurationAction();
		addAction.setEnabled(false);
		this.add(addAction);
	}

	public void buildConfigurationChanged(final ProjectBuildConfigurator pbc) {
		Job job = new UIJob(AspectJUIPlugin.getResourceString("dynamicBuildConfigurationMenu.job.name")) { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				new AddBuildConfigurationAction();
				rebuildMenu(pbc);
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
		job.schedule();
	}

	private void rebuildMenu(final ProjectBuildConfigurator pbc) {
		final IAction addAction = new AddBuildConfigurationAction();
		if (pbc == null) {
			DynamicBuildConfigurationMenu.this.removeAll();
			DynamicBuildConfigurationMenu.this.setVisible(true);
			addAction.setEnabled(false);
			DynamicBuildConfigurationMenu.this.add(separator);
			DynamicBuildConfigurationMenu.this.add(addAction);
			return;
		}

		BuildConfiguration[] bcs = (BuildConfiguration[]) pbc
				.getBuildConfigurations().toArray(new BuildConfiguration[0]);
		Util.sort(bcs);
		final BuildConfiguration activeBuildConfiguration = pbc.getActiveBuildConfiguration();
		final BuildConfiguration[] bcs2 = new BuildConfiguration[bcs.length];
		System.arraycopy(bcs, 0, bcs2, 0, bcs.length);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				DynamicBuildConfigurationMenu.this.removeAll();
				for (int i = 0; i < bcs2.length; i++) {
					Action act = new BuildConfigurationSelectedAction(bcs2[i]);
					if (bcs2[i] == activeBuildConfiguration)
						act.setImageDescriptor(id);
					DynamicBuildConfigurationMenu.this.add(act);
				}
				DynamicBuildConfigurationMenu.this.add(separator);
				DynamicBuildConfigurationMenu.this.add(addAction);
				DynamicBuildConfigurationMenu.this.setVisible(true);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IContributionItem#isDynamic()
	 */
	public boolean isDynamic() {
		return true;
	}
}