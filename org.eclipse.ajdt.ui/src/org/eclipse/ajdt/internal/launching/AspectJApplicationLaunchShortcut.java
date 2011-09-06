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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaApplicationLaunchShortcut;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;

/**
 * Shortcut to launching an AspectJ application. Extends
 * JavaApplicationLaunchShortcut to enable the launch of main methods in both
 * Java classes and Aspects. Methods are partly copied from the super class.
 */
public class AspectJApplicationLaunchShortcut extends
		JavaApplicationLaunchShortcut {

	public static final String ASPECTJ_LAUNCH_ID = "org.eclipse.ajdt.launching.AspectJApplication"; //$NON-NLS-1$

	public static final String AJ_FILE_EXTENSION = "aj"; //$NON-NLS-1$

	public static final String JAVA_FILE_EXTENSION = "java"; //$NON-NLS-1$

	
	protected IType[] findTypes(Object[] elements, IRunnableContext context) throws InterruptedException, CoreException {
		try {
			IJavaElement[] javaElements = getJavaElements(elements);
			AJMainMethodSearchEngine engine = new AJMainMethodSearchEngine();
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(javaElements, false);
			return engine.searchMainMethodsIncludingAspects(context, scope, true);
		} catch (InvocationTargetException e) {
			throw (CoreException)e.getTargetException(); 
		}
	}

	/**
	 * Returns the AspectJ launch config type
	 */
	protected static ILaunchConfigurationType getAJConfigurationType() {
		return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(ASPECTJ_LAUNCH_ID);
	}

	protected ILaunchConfiguration createConfiguration(IType type) {
		ILaunchConfiguration config = null;
		ILaunchConfigurationWorkingCopy wc = null;
		try {
			ILaunchConfigurationType configType = getAJConfigurationType();
			wc = configType.newInstance(null, getLaunchManager().generateUniqueLaunchConfigurationNameFrom(type.getElementName()));
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, type.getFullyQualifiedName());
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, type.getJavaProject().getElementName());
			config = wc.doSave();
		} catch (CoreException exception) {
			reportErorr(exception);		
		} 
		return config;
	}

	/**
	 * Returns the singleton launch manager.
	 * 
	 * @return launch manager
	 */
	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Opens an error dialog on the given exception.
	 * 
	 * @param exception
	 */
	protected void reportErorr(CoreException exception) {
		MessageDialog.openError(getShell(), LauncherMessages.JavaLaunchShortcut_3, exception.getStatus().getMessage());  
	}

	
}