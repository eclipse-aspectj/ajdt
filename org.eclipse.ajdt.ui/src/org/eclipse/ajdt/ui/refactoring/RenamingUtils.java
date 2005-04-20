/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.ui.refactoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.ajdt.buildconfigurator.BuildConfiguration;
import org.eclipse.ajdt.buildconfigurator.BuildConfigurator;
import org.eclipse.ajdt.buildconfigurator.ProjectBuildConfigurator;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.visualiser.StructureModelUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.changes.RenameResourceChange;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;

public class RenamingUtils {

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
	public static void convertAspectsToAJAndOthersToJava(
			final boolean includeNonBuiltFiles, 
			final boolean updateBuildConfigs,
			final IProject project,
			Shell shell) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
			    try {
                    project.build(IncrementalProjectBuilder.FULL_BUILD,
                            AspectJPlugin.ID_BUILDER, null,
                            new SubProgressMonitor(monitor, 2));
                } catch (CoreException e1) {
                }
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
										isAspect = checkIsAspect(resource);
									}
									if (!isAspect
											&& resource.getFileExtension()
													.equals("aj")) { //$NON-NLS-1$								
										renameFile(false, resource, monitor,
												oldToNewNames);
									} else if (isAspect
											&& resource.getFileExtension()
													.equals("java")) { //$NON-NLS-1$
										renameFile(true, resource, monitor,
												oldToNewNames);
									}
								}
							} catch (JavaModelException e) {
							}
						}
						monitor.worked(1);
					}
					if (updateBuildConfigs) {
						updateBuildConfigurations(oldToNewNames, project,
								monitor);
					}
				} catch (JavaModelException e) {
				}
			}
		};

		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(
				runnable);
		try {
			new ProgressMonitorDialog(shell).run(true, true, op);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
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
	public static boolean checkIsAspect(IResource resource) {
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
	 * @param includeNotBuiltFiles -
	 *            include files not included in the active build configuration.
	 * @param updateBuildConfigs -
	 *            update build configuration files if true.
	 */
	public static void convertAllExtensions(final boolean convertToAJ,
			final boolean includeNotBuiltFiles, 
			final boolean updateBuildConfigs,
			final IProject project,
			Shell shell) {

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
										renameFile(convertToAJ, resource,
												monitor, oldNamesToNewNames);
									}
								}
							} catch (JavaModelException e) {
							}
						}
						monitor.worked(1);
					}
					if (updateBuildConfigs) {
						updateBuildConfigurations(oldNamesToNewNames, project,
								monitor);
					}
				} catch (JavaModelException e) {
				}
			}
		};

		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(
				runnable);
		try {
			new ProgressMonitorDialog(shell).run(true, true, op);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		}

	}

	/**
	 * Utility method - Rename a single file's extension. Add the old and new
	 * names to the map supplied.
	 * 
	 * @param newExtensionIsAJ
	 * @param file
	 * @param monitor
	 * @param oldToNewNames -
	 *            Map of old to new names augmented by this method
	 */
	public static void renameFile(boolean newExtensionIsAJ, IResource file,
			IProgressMonitor monitor, Map oldToNewNames) {
		String oldName = file.getName();
		String nameWithoutExtension = oldName
				.substring(0, oldName.indexOf('.')); //$NON-NLS-1$
		String newExtension = newExtensionIsAJ ? ".aj" : ".java"; //$NON-NLS-1$
		RenameResourceChange change = new RenameResourceChange(file,
				nameWithoutExtension + newExtension);
		try {
			change.perform(monitor);
			oldToNewNames.put(oldName, nameWithoutExtension + newExtension);
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

	public static void updateBuildConfigurations(Map oldNamesToNewNames,
			IProject project, IProgressMonitor monitor) {
		ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator()
				.getProjectBuildConfigurator(project);
		IFile[] buildConfigs = pbc.getConfigurationFiles();
		for (int i = 0; i < buildConfigs.length; i++) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(buildConfigs[i]
						.getContents()));
			} catch (CoreException e) {
				continue;
			}
			StringBuffer sb = new StringBuffer();
			try {
				String line = br.readLine();
				while (line != null) {
					for (Iterator iter = oldNamesToNewNames.keySet().iterator(); iter
							.hasNext();) {
						String oldName = (String) iter.next();
						String newName = (String) oldNamesToNewNames
								.get(oldName);
						line = line.replaceAll(oldName, newName);
					}
					sb.append(line);
					sb.append(System.getProperty("line.separator")); //$NON-NLS-1$
					line = br.readLine();
				}
				StringReader reader = new StringReader(sb.toString());
				buildConfigs[i].setContents(new ReaderInputStream(reader), true, true, monitor);
			} catch (IOException ioe) {
			} catch (CoreException e) {
			} finally {
				monitor.worked(10);
				try {
					br.close();
				} catch (IOException ioe) {
				}
			}
			Collection c = pbc.getBuildConfigurations();
			for (Iterator iter = c.iterator(); iter.hasNext();) {
				BuildConfiguration config = (BuildConfiguration) iter.next();
				config.update(true);
			}
		}
	}
	
}
