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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.ajde.Ajde;
import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.visualiser.StructureModelUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaApplicationLaunchShortcut;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

/**
 * Shortcut to launching an aspectJ application. Extends
 * JavaApplicationLaunchShortcut to enable the launch of main methods in both
 * Java classes and Aspects. Methods are partly copied from the super class.
 */
public class AspectJApplicationLaunchShortcut extends
		JavaApplicationLaunchShortcut {

	public static final String ASPECTJ_LAUNCH_ID = "org.eclipse.ajdt.launching.AspectJApplication"; //$NON-NLS-1$

	public static final String AJ_FILE_EXTENSION = "aj"; //$NON-NLS-1$

	public static final String JAVA_FILE_EXTENSION = "java"; //$NON-NLS-1$

	/**
	 * @param search
	 *            the java elements to search for a main type
	 * @param mode
	 *            the mode to launch in
	 * @param editor
	 *            activated on an editor (or from a selection in a viewer)
	 */
	public void searchAndLaunch(Object[] search, String mode, boolean editor) {
		Object[] types = null;
		if (search != null) {
			try {
				IJavaElement[] elements = getJavaElements(search);
				if (elements.length > 0) {
					AJMainMethodSearchEngine engine = new AJMainMethodSearchEngine();
					IJavaSearchScope scope = SearchEngine
							.createJavaSearchScope(elements, false);
					types = engine
							.searchMainMethodsIncludingAspects(
									PlatformUI.getWorkbench()
											.getProgressService(),
									scope,
									IJavaElementSearchConstants.CONSIDER_BINARIES
											| IJavaElementSearchConstants.CONSIDER_EXTERNAL_JARS,
									true);
				} else {
					types = search;
				}
			} catch (InterruptedException e) {
				return;
			} catch (InvocationTargetException e) {
				MessageDialog.openError(getShell(),
						"Launch Failed", e.getMessage()); //$NON-NLS-1$
				return;
			}
			Object type = null;
			if (types.length == 0) {
				String message = null;
				if (editor) {
					message = LauncherMessages
							.getString("JavaApplicationLaunchShortcut.The_active_editor_does_not_contain_a_main_type._1"); //$NON-NLS-1$
				} else {
					message = LauncherMessages
							.getString("JavaApplicationLaunchShortcut.The_selection_does_not_contain_a_main_type._2"); //$NON-NLS-1$
				}
				MessageDialog
						.openError(
								getShell(),
								LauncherMessages
										.getString("JavaApplicationAction.Launch_failed_7"), message); //$NON-NLS-1$
			} else if (types.length > 1) {
				type = chooseType(types, mode);
			} else {
				type = types[0];
			}
			if (type != null) {
				launch(type, mode);
			}
		}

	}

	/**
	 * Returns the Java elements corresponding to the given objects.
	 * 
	 * @param objects
	 *            selected objects
	 * @return corresponding Java elements
	 */
	private IJavaElement[] getJavaElements(Object[] objects) {
		List list = new ArrayList(objects.length);
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			if (object instanceof IAdaptable) {
				IJavaElement element = (IJavaElement) ((IAdaptable) object)
						.getAdapter(IJavaElement.class);
				if (element != null) {
					if (element instanceof IMember) {
						// Use the declaring type if available
						IJavaElement type = ((IMember) element)
								.getDeclaringType();
						if (type != null) {
							element = type;
						}
					}
					list.add(element);
				}
			}
		}
		return (IJavaElement[]) list.toArray(new IJavaElement[list.size()]);
	}

	/**
	 * Launches a configuration for the given type
	 */
	protected void launch(Object type, String mode) {
		ILaunchConfiguration config = findLaunchConfiguration(type, mode);
		if (config != null) {
			LaunchConfigurationClasspathUtils.addAspectPathToClasspath(config);
			DebugUITools.launch(config, mode);
		}
	}

	/**
	 * Locate a configuration to relaunch for the given type. If one cannot be
	 * found, create one.
	 * 
	 * @return a re-useable config or <code>null</code> if none
	 */
	protected ILaunchConfiguration findLaunchConfiguration(Object type,
			String mode) {
		ILaunchConfigurationType configType = getAJLaunchConfigType();
		List candidateConfigs = Collections.EMPTY_LIST;
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault()
					.getLaunchManager().getLaunchConfigurations(configType);
			candidateConfigs = new ArrayList(configs.length);
			for (int i = 0; i < configs.length; i++) {
				ILaunchConfiguration config = configs[i];
				String fullyQualifiedName = null;
				String projectName = null;
				if (type instanceof IType) {
					fullyQualifiedName = ((IType) type).getFullyQualifiedName();
					projectName = ((IType) type).getJavaProject()
							.getElementName();
				} else if (type instanceof Object[]) {
					IProgramElement element = (IProgramElement) ((Object[]) type)[0];
					IProject project = (IProject) ((Object[]) type)[1];
					IJavaProject jp = JavaCore.create(project);
					fullyQualifiedName = element.getPackageName()
							+ "." + element.getName(); //$NON-NLS-1$
					if (jp != null) {
						projectName = jp.getElementName();
					}
				}

				if (config.getAttribute(
						IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
						"").equals(fullyQualifiedName)) { //$NON-NLS-1$
					if (config
							.getAttribute(
									IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
									"").equals(projectName)) { //$NON-NLS-1$
						candidateConfigs.add(config);
					}
				}
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}

		// If there are no existing configs associated with the IType, create
		// one.
		// If there is exactly one config associated with the IType, return it.
		// Otherwise, if there is more than one config associated with the
		// IType, prompt the
		// user to choose one.
		int candidateCount = candidateConfigs.size();
		if (candidateCount < 1) {
			return createConfiguration(type);
		} else if (candidateCount == 1) {
			return (ILaunchConfiguration) candidateConfigs.get(0);
		} else {
			// Prompt the user to choose a config. A null result means the user
			// cancelled the dialog, in which case this method returns null,
			// since cancelling the dialog should also cancel launching
			// anything.
			ILaunchConfiguration config = chooseConfiguration(candidateConfigs,
					mode);
			if (config != null) {
				return config;
			}
		}

		return null;
	}

	/**
	 * Create & return a new configuration based on the specified
	 * <code>IType</code>.
	 */
	protected ILaunchConfiguration createConfiguration(Object type) {
		ILaunchConfiguration config = null;
		if (type instanceof IType) {
			config = createConfigurationForIType((IType) type);
		} else if (type instanceof Object[]) {
			IProgramElement aspectElement = (IProgramElement) ((Object[]) type)[0];
			IJavaProject jp = JavaCore.create((IProject) ((Object[]) type)[1]);
			ILaunchConfigurationWorkingCopy wc = null;
			try {
				ILaunchConfigurationType configType = getAJLaunchConfigType();
				wc = configType.newInstance(null, getLaunchManager()
						.generateUniqueLaunchConfigurationNameFrom(
								aspectElement.getName()));
			} catch (CoreException exception) {
				reportCreatingConfiguration(exception);
				return null;
			}
			wc.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
					aspectElement.getPackageName()
							+ "." + aspectElement.getName()); //$NON-NLS-1$
			wc.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, jp
							.getElementName());
			try {
				config = wc.doSave();
			} catch (CoreException exception) {
				reportCreatingConfiguration(exception);
			}
		}
		return config;
	}

	/**
	 * Create a launch configuration for an IType
	 * @param type
	 * @return
	 */
	private ILaunchConfiguration createConfigurationForIType(IType type) {
		ILaunchConfiguration config = null;
		ILaunchConfigurationWorkingCopy wc = null;
		try {
			ILaunchConfigurationType configType = getAJLaunchConfigType();
			wc = configType.newInstance(null, getLaunchManager().generateUniqueLaunchConfigurationNameFrom(type.getElementName()));
		} catch (CoreException exception) {
			reportCreatingConfiguration(exception);
			return null;		
		} 
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, type.getFullyQualifiedName());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, type.getJavaProject().getElementName());
		try {
			config = wc.doSave();		
		} catch (CoreException exception) {
			reportCreatingConfiguration(exception);			
		}
		return config;
	}

	/**
	 * Returns the AspectJ launch config type
	 */
	protected static ILaunchConfigurationType getAJLaunchConfigType() {
		return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(ASPECTJ_LAUNCH_ID);
	}

	/**
	 * Prompts the user to select a type
	 * 
	 * @return the selected type or <code>null</code> if none.
	 */
	protected Object chooseType(Object[] types, String mode) {
		AJMainTypeSelectionDialog dialog = new AJMainTypeSelectionDialog(
				getShell(), types);
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			dialog.setTitle(LauncherMessages
					.getString("JavaApplicationAction.Type_Selection_Debug")); //$NON-NLS-1$
		} else {
			dialog.setTitle(LauncherMessages
					.getString("JavaApplicationAction.Type_Selection_Run")); //$NON-NLS-1$
		}
		dialog.setMultipleSelection(false);
		if (dialog.open() == Window.OK) {
			return (Object) dialog.getFirstResult();
		}
		return null;
	}

	/**
	 * @see ILaunchShortcut#launch(IEditorPart, String)
	 */
	public void launch(IEditorPart editor, String mode) {
		boolean error = true;
		IEditorInput input = editor.getEditorInput();
		IJavaElement je = (IJavaElement) input.getAdapter(IJavaElement.class);
		if (je != null) {
			searchAndLaunch(new Object[] { je }, mode, true);
			error = false;
		} else if (input instanceof IFileEditorInput) {
			IFile file = (IFile) input.getAdapter(IFile.class);
			if (file.getFileExtension().equals(AJ_FILE_EXTENSION)) {
				IProgramElement aspectElement = getAspectForFile(file);
				if (aspectElement.isRunnable()) {
					searchAndLaunch(new Object[] { new Object[] {
							aspectElement, file.getProject() } }, mode, true);
					error = false;
				}
			}
		}
		if (error) {
			MessageDialog
					.openError(
							getShell(),
							LauncherMessages
									.getString("JavaApplicationAction.Launch_failed_7"), LauncherMessages.getString("JavaApplicationLaunchShortcut.The_active_editor_does_not_contain_a_main_type._1")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * @param file
	 * @return
	 */
	private IProgramElement getAspectForFile(IFile file) {
		IProject project = file.getProject();
		String configFile = AspectJUIPlugin.getBuildConfigurationFile(project);
		if (!(configFile.equals(Ajde.getDefault().getConfigurationManager()
				.getActiveConfigFile()))) {
			Ajde.getDefault().getConfigurationManager().setActiveConfigFile(
					configFile);
		}

		List packages = StructureModelUtil.getPackagesInModel();

		IPath path = file.getProjectRelativePath();
		String pathStr = path.toString();
		if (pathStr.endsWith("." + AJ_FILE_EXTENSION)) { //$NON-NLS-1$
			pathStr = pathStr.substring(0, pathStr.length() - 3);
		} else if (pathStr.endsWith("." + JAVA_FILE_EXTENSION)) { //$NON-NLS-1$
			pathStr = pathStr.substring(0, pathStr.length() - 5);
		}

		Iterator it = packages.iterator();
		while (it.hasNext()) {
			Object[] progNodes = (Object[]) it.next();

			IProgramElement packageNode = (IProgramElement) progNodes[0];

			if (pathStr.indexOf(packageNode.getName().replace('.', '/')) != -1) {

				Set temp = getAspectsInPackage(packageNode, project);
				for (Iterator iter = temp.iterator(); iter.hasNext();) {
					IProgramElement element = (IProgramElement) iter.next();
					String pathString = element.getPackageName()
							+ "." + element.getName(); //$NON-NLS-1$
					pathString = pathString.replace('.', '/');
					if (pathStr.endsWith(pathString)) {
						return element;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param packageNode
	 * @return
	 */
	private Set getAspectsInPackage(IProgramElement packageNode,
			IProject project) {
		List files = StructureModelUtil.getFilesInPackage(packageNode);
		Set aspects = new HashSet();
		for (Iterator it = files.iterator(); it.hasNext();) {
			IProgramElement fileNode = (IProgramElement) it.next();
			if (fileNode.getKind().equals(IProgramElement.Kind.FILE_JAVA)
					|| fileNode.getKind().equals(
							IProgramElement.Kind.FILE_ASPECTJ)) {
				List children = fileNode.getChildren();
				for (Iterator iter = children.iterator(); iter.hasNext();) {
					IProgramElement child = (IProgramElement) iter.next();
					if (child != null
							&& child.getKind().equals(
									IProgramElement.Kind.ASPECT)) {
						if (child.isRunnable()) {
							aspects.add(child);
						}
					}
				}
			}
		}
		return aspects;
	}

	/**
	 * @see ILaunchShortcut#launch(ISelection, String)
	 */
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection struct = (IStructuredSelection) selection;
			List elements = new ArrayList();
			for (Iterator iter = struct.iterator(); iter.hasNext();) {
				Object element = (Object) iter.next();
				if (element instanceof IJavaElement) {
					elements.add(element);
				} else if (element instanceof IFile) {
					IFile file = (IFile) element;
					if (file.getFileExtension().equals(AJ_FILE_EXTENSION)
							|| file.getFileExtension().equals(
									JAVA_FILE_EXTENSION)) {
						IProgramElement aspectElement = getAspectForFile(file);
						if (aspectElement.isRunnable()) {
							elements.add(new Object[] { aspectElement,
									file.getProject() });
						}
					}
				}
			}
			searchAndLaunch(elements.toArray(), mode, false);
		} else {
			MessageDialog
					.openError(
							getShell(),
							LauncherMessages
									.getString("JavaApplicationAction.Launch_failed_7"), LauncherMessages.getString("JavaApplicationLaunchShortcut.The_selection_does_not_contain_a_main_type._2")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

}