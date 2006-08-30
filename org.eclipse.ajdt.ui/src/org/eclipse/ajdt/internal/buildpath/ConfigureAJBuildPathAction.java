/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Ford - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.buildpath;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.packageview.ClassPathContainer;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class ConfigureAJBuildPathAction extends Action implements
		IActionDelegate {
	private IProject fProject;

	public ConfigureAJBuildPathAction() {
		super(
				NewWizardMessages.NewSourceContainerWorkbookPage_ToolBar_ConfigureBP_label,
				JavaPluginImages.DESC_ELCL_CONFIGURE_BUILDPATH);
		setToolTipText(NewWizardMessages.NewSourceContainerWorkbookPage_ToolBar_ConfigureBP_tooltip);
		setDisabledImageDescriptor(JavaPluginImages.DESC_DLCL_CONFIGURE_BUILDPATH);
	}

	private Shell getShell() {
		return AspectJUIPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getShell();
	}

	public void run(IAction action) {
		if (fProject != null) {
			PreferencesUtil.createPropertyDialogOn(getShell(), fProject,
					"org.eclipse.ajdt.ui.ProjectPage", null, null).open(); //$NON-NLS-1$
		}
	}

	private IProject getProjectFromSelectedElement(Object firstElement) {
		if (firstElement instanceof IJavaElement) {
			IJavaElement element = (IJavaElement) firstElement;
			IPackageFragmentRoot root = JavaModelUtil
					.getPackageFragmentRoot(element);
			if (root != null && root != element && root.isArchive()) {
				return null;
			}
			IJavaProject project = element.getJavaProject();
			if (project != null) {
				return project.getProject();
			}
			return null;
		} else if (firstElement instanceof ClassPathContainer) {
			return ((ClassPathContainer) firstElement).getJavaProject()
					.getProject();
		} else if (firstElement instanceof IAdaptable) {
			IResource res = (IResource) ((IAdaptable) firstElement)
					.getAdapter(IResource.class);
			if (res != null) {
				return res.getProject();
			}
		}
		return null;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object firstElement = ((IStructuredSelection) selection)
					.getFirstElement();
			fProject = getProjectFromSelectedElement(firstElement);
		}
	}

}
