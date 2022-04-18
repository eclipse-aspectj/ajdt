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

package org.eclipse.ajdt.internal.launching;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.SharedJavaMainTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * Tab for the AspectJ Application Launcher's tab group, mostly copied from
 * JavaMainTab to allow both Java classes and AspectJ aspects with main methods
 * to be launched.
 * Changed marked // AspectJ Change
 */
public class AspectJMainTab extends SharedJavaMainTab {


	/**
	 * Boolean launch configuration attribute indicating that external jars (on
	 * the runtime classpath) should be searched when looking for a main type.
	 * Default value is <code>false</code>.
	 *
	 * @since 2.1
	 */
	public static final String ATTR_INCLUDE_EXTERNAL_JARS = IJavaDebugUIConstants.PLUGIN_ID + ".INCLUDE_EXTERNAL_JARS"; //$NON-NLS-1$
	/**
	 * Boolean launch configuration attribute indicating whether types inheriting
	 * a main method should be considerd when searching for a main type.
	 * Default value is <code>false</code>.
	 *
	 * @since 3.0
	 */
	public static final String ATTR_CONSIDER_INHERITED_MAIN = IJavaDebugUIConstants.PLUGIN_ID + ".CONSIDER_INHERITED_MAIN"; //$NON-NLS-1$

	// UI widgets
	private Button fSearchExternalJarsCheckButton;
	private Button fConsiderInheritedMainButton;
	private Button fStopInMainCheckButton;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);
		GridLayout topLayout = new GridLayout();
		topLayout.verticalSpacing = 0;
		comp.setLayout(topLayout);
		comp.setFont(font);
		createProjectEditor(comp);
		createVerticalSpacer(comp, 1);
		fSearchExternalJarsCheckButton = createCheckButton(parent, LauncherMessages.JavaMainTab_E_xt__jars_6);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		fSearchExternalJarsCheckButton.setLayoutData(gd);
		fSearchExternalJarsCheckButton.addSelectionListener(getDefaultListener());
		fConsiderInheritedMainButton = createCheckButton(parent, LauncherMessages.JavaMainTab_22);
		gd = new GridData();
		gd.horizontalSpan = 2;
		fConsiderInheritedMainButton.setLayoutData(gd);
		fConsiderInheritedMainButton.addSelectionListener(getDefaultListener());
		fStopInMainCheckButton = createCheckButton(parent, LauncherMessages.JavaMainTab_St_op_in_main_1);
		gd = new GridData();
		fStopInMainCheckButton.setLayoutData(gd);
		fStopInMainCheckButton.addSelectionListener(getDefaultListener());
		createMainTypeEditor(comp, LauncherMessages.JavaMainTab_Main_cla_ss__4 /*, new Button[] {fSearchExternalJarsCheckButton, fConsiderInheritedMainButton, fStopInMainCheckButton}*/);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.JavaMainTab__Main_19;
	}

	/**
	 * Show a dialog that lists all main types
	 */
	protected void handleSearchButtonSelected() {
		IJavaProject project = getJavaProject();
		IJavaElement[] elements = null;
		if ((project == null) || !project.exists()) {
			IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
			if (model != null) {
				try {
					elements = model.getJavaProjects();
				}//end try
				catch (JavaModelException e) {JDIDebugUIPlugin.log(e);}
			}//end if
		}//end if
		else {
			elements = new IJavaElement[]{project};
		}//end else
		if (elements == null) {
			elements = new IJavaElement[]{};
		}//end if
		int constraints = IJavaSearchScope.SOURCES;
		if (fSearchExternalJarsCheckButton.getSelection()) {
			constraints |= IJavaSearchScope.APPLICATION_LIBRARIES;
			constraints |= IJavaSearchScope.SYSTEM_LIBRARIES;
		}//end if
		IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(elements, constraints);
		// AspectJ Change Begin
		AJMainMethodSearchEngine engine = new AJMainMethodSearchEngine();
		// AspectJ Change End
		IType[] types = null;
		try {
			// AspectJ Change Begin
			types = engine.searchMainMethodsIncludingAspects(getLaunchConfigurationDialog(), searchScope, fConsiderInheritedMainButton.getSelection());
			// AspectJ Change End
		}//end try
		catch (InvocationTargetException | InterruptedException e) {
			setErrorMessage(e.getMessage());
			return;
		}//end catch
    //end catch
		SelectionDialog dialog = null;
		// AspectJ Change Begin
		dialog = new AJMainTypeSelectionDialog(getShell(), types);
		// AspectJ Change End
		dialog.setTitle(LauncherMessages.JavaMainTab_Choose_Main_Type_11);
		dialog.setMessage(LauncherMessages.JavaMainTab_Choose_a_main__type_to_launch__12);
		if (dialog.open() == Window.CANCEL) {
			return;
		}//end if
		Object[] results = dialog.getResult();
		IType type = (IType)results[0];
		if (type != null) {
			fMainText.setText(type.getFullyQualifiedName());
			fProjText.setText(type.getJavaProject().getElementName());
		}//end if
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.AbstractJavaMainTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		super.initializeFrom(config);
		updateMainTypeFromConfig(config);
		updateStopInMainFromConfig(config);
		updateInheritedMainsFromConfig(config);
		updateExternalJars(config);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);
		String name = fProjText.getText().trim();
		if (name.length() > 0) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IStatus status = workspace.validateName(name, IResource.PROJECT);
			if (status.isOK()) {
				IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(name);
				if (!project.exists()) {
					setErrorMessage(NLS.bind(LauncherMessages.JavaMainTab_20, new String[] {name}));
					return false;
				}//end if
				if (!project.isOpen()) {
					setErrorMessage(NLS.bind(LauncherMessages.JavaMainTab_21, new String[] {name}));
					return false;
				}//end if
			}//end if
			else {
				setErrorMessage(NLS.bind(LauncherMessages.JavaMainTab_19, new String[]{status.getMessage()}));
				return false;
			}//end else
		}//end if
		name = fMainText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage(LauncherMessages.JavaMainTab_Main_type_not_specified_16);
			return false;
		}//end if
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText().trim());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, fMainText.getText().trim());

		// attribute added in 2.1, so null must be used instead of false for backwards compatibility
		if (fStopInMainCheckButton.getSelection()) {
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, true);
		}//end if
		else {
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, (String)null);
		}//end else

		// attribute added in 2.1, so null must be used instead of false for backwards compatibility
		if (fSearchExternalJarsCheckButton.getSelection()) {
			config.setAttribute(ATTR_INCLUDE_EXTERNAL_JARS, true);
		}//end if
		else {
			config.setAttribute(ATTR_INCLUDE_EXTERNAL_JARS, (String)null);
		}//end else

		// attribute added in 3.0, so null must be used instead of false for backwards compatibility
		if (fConsiderInheritedMainButton.getSelection()) {
			config.setAttribute(ATTR_CONSIDER_INHERITED_MAIN, true);
		}//end if
		else {
			config.setAttribute(ATTR_CONSIDER_INHERITED_MAIN, (String)null);
		}//end else
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		IJavaElement javaElement = getContext();
		if (javaElement != null) {
			initializeJavaProject(javaElement, config);
		}//end if
		else {
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
		}//end else
		initializeMainTypeAndName(javaElement, config);
	}

	/**
	 * updates the external jars attribute from the specified launch config
	 * @param config the config to load from
	 */
	private void updateExternalJars(ILaunchConfiguration config) {
		boolean search = false;
		try {
			search = config.getAttribute(ATTR_INCLUDE_EXTERNAL_JARS, false);
		}//end try
		catch (CoreException e) {JDIDebugUIPlugin.log(e);}
		fSearchExternalJarsCheckButton.setSelection(search);
	}

	/**
	 * update the inherited mains attribute from the specified launch config
	 * @param config the config to load from
	 */
	private void updateInheritedMainsFromConfig(ILaunchConfiguration config) {
		boolean inherit = false;
		try {
			inherit = config.getAttribute(ATTR_CONSIDER_INHERITED_MAIN, false);
		}//end try
		catch (CoreException e) {JDIDebugUIPlugin.log(e);}
		fConsiderInheritedMainButton.setSelection(inherit);
	}

	/**
	 * updates the stop in main attribute from the specified launch config
	 * @param config the config to load the stop in main attribute from
	 */
	private void updateStopInMainFromConfig(ILaunchConfiguration config) {
		boolean stop = false;
		try {
			stop = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);
		}//end try
		catch (CoreException e) {JDIDebugUIPlugin.log(e);}
		fStopInMainCheckButton.setSelection(stop);
	}

}
