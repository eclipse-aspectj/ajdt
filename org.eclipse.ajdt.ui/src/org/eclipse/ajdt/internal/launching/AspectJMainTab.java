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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * Tab for the AspectJ Application Launcher's tab group, which extends
 * JavaMainTab to allow both Java classes and AspectJ aspects with main methods
 * to be launched.
 */
public class AspectJMainTab extends JavaMainTab {

	/**
	 * Show a dialog that lists all main types	 * 
	 */
	// Method copied from JavaMainTab - changes marked with // AspectJ Change
	protected void handleSearchButtonSelected() {
		
		IJavaProject javaProject = getJavaProject();
		IJavaElement[] elements = null;
		if ((javaProject == null) || !javaProject.exists()) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IJavaModel model = JavaCore.create(root);
			if (model != null) {
				try {
					elements = model.getJavaProjects();
				} catch (JavaModelException e) {
				}
			}
		} else {
			elements = new IJavaElement[]{javaProject};
		}		
		if (elements == null) {
			elements = new IJavaElement[]{};
		}
		int constraints = IJavaSearchScope.SOURCES;
		if (fSearchExternalJarsCheckButton.getSelection()) {
			constraints |= IJavaSearchScope.APPLICATION_LIBRARIES;
			constraints |= IJavaSearchScope.SYSTEM_LIBRARIES;
		}		
		IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(elements, constraints);
		
		// AspectJ Change
		AJMainMethodSearchEngine engine = new AJMainMethodSearchEngine();
		IType[] types = null;
		try {
			// AspectJ Change
			types = engine.searchMainMethodsIncludingAspects(getLaunchConfigurationDialog(), searchScope, fConsiderInheritedMainButton.getSelection());
		} catch (InvocationTargetException e) {
			setErrorMessage(e.getMessage());
			return;
		} catch (InterruptedException e) {
			setErrorMessage(e.getMessage());
			return;
		}
		
		Shell shell = getShell();
		// AspectJ Change
		SelectionDialog dialog = new AJMainTypeSelectionDialog(shell, types); 
		dialog.setTitle(LauncherMessages.JavaMainTab_Choose_Main_Type_11); //$NON-NLS-1$
		dialog.setMessage(LauncherMessages.JavaMainTab_Choose_a_main__type_to_launch__12); //$NON-NLS-1$
		if (dialog.open() == Window.CANCEL) {
			return;
		}
		
		Object[] results = dialog.getResult();
		if ((results == null) || (results.length < 1)) {
			return;
		}		
		IType type = (IType)results[0];
		if (type != null) {
			fMainText.setText(type.getFullyQualifiedName());
			javaProject = type.getJavaProject();
			fProjText.setText(javaProject.getElementName());
		}
	}
	
}