/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/
package org.eclipse.ajdt.internal.launching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.internal.utils.AJDTEventTrace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.classpath.BootpathFilter;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathContentProvider;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathLabelProvider;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathModel;
import org.eclipse.jdt.internal.debug.ui.classpath.RuntimeClasspathViewer;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * A launch configuration tab that displays and edits the user, bootstrap and
 * aspect path classes comprising the classpath launch configuration attribute.
 * 
 * Mostly copied from
 * org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab
 */
public class AJClasspathTab extends JavaClasspathTab {

	private AJClasspathModel fModel;

	protected static final String DIALOG_SETTINGS_PREFIX = "JavaClasspathTab"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();

		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		WorkbenchHelp
				.setHelp(
						getControl(),
						IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_CLASSPATH_TAB);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		comp.setLayout(topLayout);
		GridData gd;

		Label label = new Label(comp, SWT.NONE);
		label.setText(LauncherMessages.getString("JavaClasspathTab.0")); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		fClasspathViewer = new RuntimeClasspathViewer(comp);
		fClasspathViewer.addEntriesChangedListener(this);
		fClasspathViewer.getControl().setFont(font);
		fClasspathViewer.setLabelProvider(new ClasspathLabelProvider());
		fClasspathViewer.setContentProvider(new ClasspathContentProvider(this));
		if (!isShowBootpath()) {
			fClasspathViewer.addFilter(new BootpathFilter());
		}

		Composite pathButtonComp = new Composite(comp, SWT.NONE);
		GridLayout pathButtonLayout = new GridLayout();
		pathButtonLayout.marginHeight = 0;
		pathButtonLayout.marginWidth = 0;
		pathButtonComp.setLayout(pathButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING
				| GridData.HORIZONTAL_ALIGN_FILL);
		pathButtonComp.setLayoutData(gd);
		pathButtonComp.setFont(font);

		createPathButtons(pathButtonComp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		try {
			fModel = LaunchConfigurationClasspathUtils
					.createClasspathModel(configuration);
			updateClassPathWithAspectPath(configuration);
		} catch (CoreException e) {
			AJDTEventTrace.generalEvent(e.getMessage());
		}

	}

	/**
	 * This tab has been activated. Override super to ensure that the local
	 * classpath model is used for setting up the tab.
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		boolean useDefault = true;
		setErrorMessage(null);
		try {
			useDefault = workingCopy.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH,
					true);
			if (useDefault) {
				if (!LaunchConfigurationClasspathUtils.isDefaultClasspath(
						LaunchConfigurationClasspathUtils
								.getCurrentClasspath(fModel), workingCopy)) {
					initializeFrom(workingCopy);
					return;
				}
			}
			fClasspathViewer.refresh();
		} catch (CoreException e) {
		}
	}

	/**
	 * Override to call local refresh method.
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		refresh(configuration);
		fClasspathViewer.expandToLevel(2);
	}

	/**
	 * Update the launch configuration runtime classpath to contain the contents
	 * of the aspect path, and save the configuration.
	 */
	private void updateClassPathWithAspectPath(
			ILaunchConfiguration configuration) {
		ILaunchConfigurationWorkingCopy wc;
		try {
			if (configuration instanceof ILaunchConfigurationWorkingCopy) {
				wc = (ILaunchConfigurationWorkingCopy) configuration;
			} else {
				wc = configuration.getWorkingCopy();
			}

			IRuntimeClasspathEntry[] classpath = LaunchConfigurationClasspathUtils
					.getCurrentClasspath(fModel);
			boolean def = LaunchConfigurationClasspathUtils.isDefaultClasspath(
					classpath, wc);
			if (def) {
				wc
						.setAttribute(
								IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH,
								(String) null);
				wc.setAttribute(
						IJavaLaunchConfigurationConstants.ATTR_CLASSPATH,
						(String) null);
			} else {
				wc
						.setAttribute(
								IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH,
								false);
				try {
					List mementos = new ArrayList(classpath.length);
					for (int i = 0; i < classpath.length; i++) {
						IRuntimeClasspathEntry entry = classpath[i];
						mementos.add(entry.getMemento());
					}
					wc.setAttribute(
							IJavaLaunchConfigurationConstants.ATTR_CLASSPATH,
							mementos);
				} catch (CoreException e) {
					AJDTEventTrace.generalEvent(e.getMessage());
				}
				wc.doSave();
			}
		} catch (CoreException e1) {
			AJDTEventTrace.generalEvent(e1.getMessage());
		}
	}

	/**
	 * Refreshes the classpath entries based on the current state of the given
	 * launch configuration.
	 */
	private void refresh(ILaunchConfiguration configuration) {
		boolean useDefault = true;
		setErrorMessage(null);
		try {
			useDefault = configuration.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH,
					true);
		} catch (CoreException e) {
			AJDTEventTrace.generalEvent(e.getMessage());
		}

		if (configuration == getLaunchConfiguration()) {
			// no need to update if an explicit path is being used and this
			// setting has not changed (and viewing the same config as last
			// time)
			if (!useDefault) {
				setDirty(false);
				return;
			}
		}

		setLaunchConfiguration(configuration);
		try {
			fModel = LaunchConfigurationClasspathUtils
					.createClasspathModel(configuration);
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
		}

		fClasspathViewer.setLaunchConfiguration(configuration);
		fClasspathViewer.setInput(fModel);
		setDirty(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (isDirty()) {
			IRuntimeClasspathEntry[] classpath = LaunchConfigurationClasspathUtils
					.getCurrentClasspath(fModel);
			boolean def = LaunchConfigurationClasspathUtils.isDefaultClasspath(
					classpath, configuration);
			if (def) {
				configuration
						.setAttribute(
								IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH,
								(String) null);
				configuration.setAttribute(
						IJavaLaunchConfigurationConstants.ATTR_CLASSPATH,
						(String) null);
			} else {
				configuration
						.setAttribute(
								IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH,
								false);
				try {
					List mementos = new ArrayList(classpath.length);
					for (int i = 0; i < classpath.length; i++) {
						IRuntimeClasspathEntry entry = classpath[i];
						mementos.add(entry.getMemento());
					}
					configuration.setAttribute(
							IJavaLaunchConfigurationConstants.ATTR_CLASSPATH,
							mementos);
				} catch (CoreException e) {
					AJDTEventTrace.generalEvent(e.getMessage());
				}
			}
		}
	}

	/**
	 * Sets the launch configuration for this classpath tab
	 */
	private void setLaunchConfiguration(ILaunchConfiguration config) {
		fLaunchConfiguration = config;
	}

	/**
	 * Returns the current launch configuration
	 */
	public ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}

	/**
	 * Returns the classpath model.
	 */
	protected ClasspathModel getModel() {
		return fModel;
	}

}
