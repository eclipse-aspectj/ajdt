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

import org.aspectj.asm.IProgramElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
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
	 * Show a dialog that lists all main types. Mostly copied from super.
	 */
	protected void handleSearchButtonSelected() {

		IJavaProject javaProject = getJavaProject();
		IJavaSearchScope searchScope = null;
		if ((javaProject == null) || !javaProject.exists()) {
			searchScope = SearchEngine.createWorkspaceScope();
		} else {
			searchScope = SearchEngine.createJavaSearchScope(
					new IJavaElement[] { javaProject }, false);
		}

		int constraints = IJavaElementSearchConstants.CONSIDER_BINARIES;
		if (fSearchExternalJarsCheckButton.getSelection()) {
			constraints |= IJavaElementSearchConstants.CONSIDER_EXTERNAL_JARS;
		}

		AJMainMethodSearchEngine engine = new AJMainMethodSearchEngine();
		Object[] types = null;
		try {
			types = engine.searchMainMethodsIncludingAspects(
					getLaunchConfigurationDialog(), searchScope, constraints,
					fConsiderInheritedMainButton.getSelection());
		} catch (InvocationTargetException e) {
			setErrorMessage(e.getMessage());
			return;
		} catch (InterruptedException e) {
			setErrorMessage(e.getMessage());
			return;
		}

		Shell shell = getShell();
		SelectionDialog dialog = new AJMainTypeSelectionDialog(shell, types);
		dialog.setTitle(LauncherMessages
				.getString("JavaMainTab.Choose_Main_Type_11")); //$NON-NLS-1$
		dialog.setMessage(LauncherMessages
				.getString("JavaMainTab.Choose_a_main_&type_to_launch__12")); //$NON-NLS-1$
		if (dialog.open() == Window.CANCEL) {
			return;
		}

		Object[] results = dialog.getResult();
		if ((results == null) || (results.length < 1)) {
			return;
		}
		Object type = results[0];
		if (type instanceof IType) {
			fMainText.setText(((IType) type).getFullyQualifiedName());
			javaProject = ((IType) type).getJavaProject();
			fProjText.setText(javaProject.getElementName());

		} else if (type instanceof Object[]) {
			IProgramElement element = (IProgramElement) ((Object[]) type)[0];
			IProject project = (IProject) ((Object[]) type)[1];
			fMainText.setText(element.getPackageName()
					+ "." + element.getName()); //$NON-NLS-1$
			IJavaProject jp = JavaCore.create(project);
			fProjText.setText(jp.getElementName());
		}		
	}

}