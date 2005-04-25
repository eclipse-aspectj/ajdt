/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ... 
 ******************************************************************************/

package org.eclipse.ajdt.ui.refactoring;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.ajdt.buildconfigurator.BuildConfiguration;
import org.eclipse.ajdt.buildconfigurator.BuildConfigurator;
import org.eclipse.ajdt.buildconfigurator.ProjectBuildConfigurator;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.visualiser.StructureModelUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;

/**
 * Dialog that renames file extensions for an entire project. Three options are
 * presented to the user - convert all extensions to .java, convert all
 * extensions to .aj and convert aspects to .aj and classes to .java.
 */
public class RenameFileExtensionsDialog extends Dialog {

	// The project
	private IProject project;

	private Button convertAllToJavaButton;

	private Button convertAllToAJButton;

	private Button convertAspectsToAJButton;

	private Button includeFilesNotInBuildButton;

	private Button updateBuildConfigsButton;

	/**
	 * @param parentShell
	 */
	public RenameFileExtensionsDialog(Shell parentShell, IProject project) {
		super(parentShell);
		this.project = project;
	}

	/**
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(AspectJUIPlugin
				.getResourceString("Refactoring.ConvertFileExtensions")); //$NON-NLS-1$
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		convertAspectsToAJButton = new Button(composite, SWT.RADIO);
		convertAspectsToAJButton
				.setText(AspectJUIPlugin
						.getResourceString("Refactoring.ConvertAspectsToAJAndClassesToJava")); //$NON-NLS-1$
		convertAspectsToAJButton.setSelection(true);

		convertAllToAJButton = new Button(composite, SWT.RADIO);
		convertAllToAJButton.setText(AspectJUIPlugin
				.getResourceString("Refactoring.ConvertAllToAJ")); //$NON-NLS-1$

		convertAllToJavaButton = new Button(composite, SWT.RADIO);
		convertAllToJavaButton.setText(AspectJUIPlugin
				.getResourceString("Refactoring.ConvertAllToJava")); //$NON-NLS-1$

		Label spacer = new Label(composite, SWT.NONE);

		includeFilesNotInBuildButton = new Button(composite, SWT.CHECK);
		includeFilesNotInBuildButton.setText(AspectJUIPlugin
				.getResourceString("Refactoring.IncludeFilesNotInBuild")); //$NON-NLS-1$
		includeFilesNotInBuildButton.setSelection(true);
		updateBuildConfigsButton = new Button(composite, SWT.CHECK);
		updateBuildConfigsButton.setText(AspectJUIPlugin
				.getResourceString("Refactoring.UpdateBuildConfigs")); //$NON-NLS-1$
		updateBuildConfigsButton.setSelection(true);
		applyDialogFont(composite);
		return composite;
	}

	protected void okPressed() {
		boolean updateBuildConfigs = updateBuildConfigsButton.getSelection();
		AspectJUIPlugin.getDefault().disableBuildConfiguratorResourceChangeListener();
		if (convertAllToAJButton.getSelection()) {
			convertAllExtensions(true, includeFilesNotInBuildButton
					.getSelection(), updateBuildConfigs);
		} else if (convertAllToJavaButton.getSelection()) {
			convertAllExtensions(false, includeFilesNotInBuildButton
					.getSelection(), updateBuildConfigs);
		} else if (convertAspectsToAJButton.getSelection()) {
			convertAspectsToAJAndOthersToJava(includeFilesNotInBuildButton
					.getSelection(), updateBuildConfigs);
		}
		super.okPressed();
		AspectJUIPlugin.getDefault().enableBuildConfiguratorResourceChangeListener();
	}

	/**
	 * Convert aspects' file extensions to .aj, and classes and interfaces to
	 * .java.
	 * 
	 * @param includeNotBuiltFiles -
	 *            include files not included in the active build configuration.
	 * @param monitor -
	 *            progress monitor
	 * @param updateBuildConfigs -
	 *            update build configurations
	 */
	private void convertAspectsToAJAndOthersToJava(
			final boolean includeNonBuiltFiles, final boolean updateBuildConfigs) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {

				// Set of all the currently active aspects in the project
				Set aspects = StructureModelUtil.getAllAspects(project, true);
				
				IJavaProject jp = JavaCore.create(project);
				ProjectBuildConfigurator pbc = BuildConfigurator
						.getBuildConfigurator().getProjectBuildConfigurator(jp);
				BuildConfiguration activeBuildConfig = pbc
						.getActiveBuildConfiguration();
				int numBuildConfigs = pbc.getBuildConfigurations().size();
				try {
					IPackageFragment[] packages = jp.getPackageFragments();
					monitor
							.beginTask(
									AspectJUIPlugin
											.getResourceString("Refactoring.ConvertingFileExtensions"),
									packages.length + (10 * numBuildConfigs));
					// map of old to new names - needed to update build config
					// files.
					Map oldToNewNames = new HashMap();
					for (int i = 0; i < packages.length; i++) {
						if (!(packages[i].isReadOnly())) {
							try {
								ICompilationUnit[] files = packages[i]
										.getCompilationUnits();
								for (int j = 0; j < files.length; j++) {

									IResource resource = files[j].getResource();

									if (!includeNonBuiltFiles
											&& !(activeBuildConfig
													.isIncluded(resource))) {
										// do not rename this file if it is not
										// active
										continue;
									}

									boolean isAspect = aspects
											.contains(resource);
									if (!isAspect
											&& !(activeBuildConfig
													.isIncluded(resource))) {
										// If the file is not included in the
										// active
										// build configuration it may still be
										// an aspect
										isAspect = RenamingUtils.checkIsAspect(resource);
									}
									if (!isAspect
											&& resource.getFileExtension()
													.equals("aj")) { //$NON-NLS-1$								
									    RenamingUtils.renameFile(false, resource, monitor,
												oldToNewNames);
									} else if (isAspect
											&& resource.getFileExtension()
													.equals("java")) { //$NON-NLS-1$
									    RenamingUtils.renameFile(true, resource, monitor,
												oldToNewNames);
									}
								}
							} catch (JavaModelException e) {
							}
						}
						monitor.worked(1);
					}
					if (updateBuildConfigs) {
					    RenamingUtils.updateBuildConfigurations(oldToNewNames, project,
								monitor,true);
					}
				} catch (JavaModelException e) {
				}
			}
		};

		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(
				runnable);
		try {
			new ProgressMonitorDialog(getShell()).run(true, true, op);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		}
	}


	/**
	 * Convert all the extensions for files in a project
	 * 
	 * @param convertToAJ -
	 *            if true convert to .aj, otherwise convert to .java
	 * @param includeNotBuiltFiles -
	 *            include files not included in the active build configuration.
	 * @param updateBuildConfigs -
	 *            update build configuration files if true.
	 */
	private void convertAllExtensions(final boolean convertToAJ,
			final boolean includeNotBuiltFiles, final boolean updateBuildConfigs) {

		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				IJavaProject jp = JavaCore.create(project);
				ProjectBuildConfigurator pbc = BuildConfigurator
						.getBuildConfigurator().getProjectBuildConfigurator(jp);
				BuildConfiguration activeBuildConfig = pbc
						.getActiveBuildConfiguration();
				int numBuildConfigs = pbc.getBuildConfigurations().size();
				try {
					IPackageFragment[] packages = jp.getPackageFragments();
					monitor
							.beginTask(
									AspectJUIPlugin
											.getResourceString("Refactoring.ConvertingFileExtensions"),
									packages.length + (10 * numBuildConfigs));

					// Map of old to new names - needed to update build config
					// files.
					Map oldNamesToNewNames = new HashMap();
					for (int i = 0; i < packages.length; i++) {
						if (!(packages[i].isReadOnly())) {
							try {
								ICompilationUnit[] files = packages[i]
										.getCompilationUnits();
								for (int j = 0; j < files.length; j++) {
									IResource resource = files[j].getResource();
									if (!includeNotBuiltFiles
											&& !(activeBuildConfig
													.isIncluded(resource))) {
										// do not rename this file if it is not
										// active
										continue;
									}
									if ((!convertToAJ && resource
											.getFileExtension().equals("aj")) //$NON-NLS-1$
											|| (convertToAJ && resource
													.getFileExtension().equals(
															"java"))) { //$NON-NLS-1$
									    RenamingUtils.renameFile(convertToAJ, resource,
												monitor, oldNamesToNewNames);
									}
								}
							} catch (JavaModelException e) {
							}
						}
						monitor.worked(1);
					}
					if (updateBuildConfigs) {
					    RenamingUtils.updateBuildConfigurations(oldNamesToNewNames, project,
								monitor,true);
					}
				} catch (JavaModelException e) {
				}
			}
		};

		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(
				runnable);
		try {
			new ProgressMonitorDialog(getShell()).run(true, true, op);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		}

	}

}