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

import java.util.Set;

import org.eclipse.ajdt.buildconfigurator.BuildConfigurator;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.javamodel.AJCompilationUnitManager;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.visualiser.StructureModelUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.changes.RenameResourceChange;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

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

		Label note = new Label(composite, SWT.BOLD);
		note.setText(AspectJUIPlugin.getResourceString("Refactoring.Note")); //$NON-NLS-1$
		//note.setFont(JFaceResources.getBannerFont());
		note.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

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

		//Label spacer = new Label(composite, SWT.NONE);

		applyDialogFont(composite);
		return composite;
	}

	protected void okPressed() {
		if (convertAllToAJButton.getSelection()) {
			convertAllExtensions(true);
		} else if (convertAllToJavaButton.getSelection()) {
			convertAllExtensions(false);
		} else if (convertAspectsToAJButton.getSelection()) {
			convertAspectsToAJAndOthersToJava();
		}
		super.okPressed();
	}

	/**
	 *  Convert aspects' file extensions to .aj, and classes and interfaces to .java.
	 */
	private void convertAspectsToAJAndOthersToJava() {

		// Set of all the currently active aspects in the project
		Set aspects = StructureModelUtil.getAllAspects(project, true);
		IJavaProject jp = JavaCore.create(project);
		try {
			IPackageFragment[] packages = jp.getPackageFragments();
			IProgressMonitor monitor = new NullProgressMonitor();
			for (int i = 0; i < packages.length; i++) {
				if (!(packages[i].isReadOnly())) {
					try {
						ICompilationUnit[] files = packages[i]
								.getCompilationUnits();
						for (int j = 0; j < files.length; j++) {
							IResource resource = files[j].getResource();
							boolean isAspect = aspects.contains(resource);
							if (!isAspect
									&& !(BuildConfigurator
											.getBuildConfigurator()
											.getProjectBuildConfigurator(jp)
											.getActiveBuildConfiguration()
											.isIncluded(resource))) {
								// If the file is not included in the active
								// build configuration it may still be an aspect
								isAspect = checkIsAspect(resource);
							}
							if (!isAspect
									&& resource.getFileExtension().equals("aj")) { //$NON-NLS-1$								
								renameFile(false, resource, monitor);
							} else if (isAspect
									&& resource.getFileExtension().equals(
											"java")) { //$NON-NLS-1$
								renameFile(true, resource, monitor);
							}
						}
					} catch (JavaModelException e) {
						AspectJUIPlugin.logException(e);
					}
				}
			}
		} catch (JavaModelException e) {
			AspectJUIPlugin.logException(e);
		}
	}

	/**
	 * If the file is not on the build path we cannot tell if it is an aspect
	 * form the structure model. Check the JDT model and
	 * AJCompilationUnitManager to find out.
	 * 
	 * @param resource -
	 *            resource to test
	 * @return true if resource is an aspect, otherwise false.
	 */
	private boolean checkIsAspect(IResource resource) {
		if (resource instanceof IFile) {
			IJavaElement jEl = JavaCore.create(resource);
			if (jEl instanceof ICompilationUnit) {
				IType[] types;
				try {
					types = ((ICompilationUnit) jEl).getAllTypes();
					if (types.length == 0) {
						return true;
					}

				} catch (JavaModelException e) {
					AspectJUIPlugin.logException(e);
				}
			} else {
				AJCompilationUnit unit = AJCompilationUnitManager.INSTANCE
						.getAJCompilationUnit((IFile) resource);
				if (unit != null) {
					try {
						IType[] types = unit.getAllTypes();
						for (int i = 0; i < types.length; i++) {
							if (types[i] instanceof AspectElement) {
								return true;
							}
						}
					} catch (JavaModelException e) {
						AspectJUIPlugin.logException(e);
					}
				}
			}
		}
		return false;
	}

	/**
	 * Convert all the extensions for files in a project
	 * 
	 * @param convertToAJ -
	 *            if true convert to .aj, otherwise convert to .java
	 */
	private void convertAllExtensions(boolean convertToAJ) {
		IJavaProject jp = JavaCore.create(project);
		try {
			IPackageFragment[] packages = jp.getPackageFragments();
			IProgressMonitor monitor = new NullProgressMonitor();
			for (int i = 0; i < packages.length; i++) {
				if (!(packages[i].isReadOnly())) {
					try {
						ICompilationUnit[] files = packages[i]
								.getCompilationUnits();
						for (int j = 0; j < files.length; j++) {
							IResource resource = files[j].getResource();
							if ((!convertToAJ && resource.getFileExtension()
									.equals("aj")) //$NON-NLS-1$
									|| (convertToAJ && resource
											.getFileExtension().equals("java"))) { //$NON-NLS-1$
								renameFile(convertToAJ, resource, monitor);
							}
						}
					} catch (JavaModelException e) {
						AspectJUIPlugin.logException(e);
					}
				}
			}
		} catch (JavaModelException e) {
			AspectJUIPlugin.logException(e);
		}

	}

	/**
	 * Utility method - Rename a single file's extension.
	 * 
	 * @param newExtensionIsAJ
	 * @param file
	 * @param monitor
	 */
	private void renameFile(boolean newExtensionIsAJ, IResource file,
			IProgressMonitor monitor) {
		String name = file.getName();
		name = name.substring(0, name.indexOf('.')); //$NON-NLS-1$
		String extension = newExtensionIsAJ ? ".aj" : ".java"; //$NON-NLS-1$
		RenameResourceChange change = new RenameResourceChange(file, name
				+ extension);
		try {
			change.perform(monitor);
		} catch (CoreException e) {
			AspectJUIPlugin
					.getDefault()
					.getErrorHandler()
					.handleError(
							AspectJUIPlugin
									.getResourceString("Refactoring.ErrorRenamingResource") //$NON-NLS-1$
							, e);
		}
	}

}
