/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.wizards.migration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ajdt.codeconversion.CodeChecker;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.ui.refactoring.RenamingUtils;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * This page handles renaming those files which contain aspects
 * (or non pure java code) to .aj files. The user can choose which
 * projects it wants to do this for.
 */
public class RenameFileExtensionsPage extends WizardPage {
	
	private CheckedListDialogField checkedListDialogField;
	
	private List ajProjects;
		
	private RenameFileExtensionsPage() {
		super(UIMessages.RenameFileExtensionsPage_name);
		this.setTitle(UIMessages.RenameFileExtensionsPage_title);
		this.setDescription(UIMessages.RenameFileExtensionsPage_description);
	}
	
	protected RenameFileExtensionsPage(List projects) {
	    this();
	    ajProjects = projects;	    
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(composite);

		String[] buttonLabels= new String[] {
		        /* 0 */ NewWizardMessages.BuildPathsBlock_classpath_checkall_button, //$NON-NLS-1$
		        /* 1 */ NewWizardMessages.BuildPathsBlock_classpath_uncheckall_button //$NON-NLS-1$
		};

		checkedListDialogField = new CheckedListDialogField(null, buttonLabels, new AJProjectListLabelProvider());
		checkedListDialogField.setLabelText(UIMessages.RenameFileExtensionsMigrationPage_message);
		checkedListDialogField.setCheckAllButtonIndex(0);
		checkedListDialogField.setUncheckAllButtonIndex(1);
		checkedListDialogField.setElements(ajProjects);
		checkedListDialogField.setCheckedElements(ajProjects);
		checkedListDialogField.setViewerSorter(new ViewerSorter());
		
		LayoutUtil.doDefaultLayout(composite, new DialogField[] { checkedListDialogField }, true, SWT.DEFAULT, SWT.DEFAULT);
		LayoutUtil.setHorizontalGrabbing(checkedListDialogField.getListControl(null));
		
		PixelConverter converter= new PixelConverter(parent);
		int buttonBarWidth= converter.convertWidthInCharsToPixels(24);
		checkedListDialogField.setButtonsMinWidth(buttonBarWidth);
		
	}
	
	public void finishPressed(IProgressMonitor monitor) {
		AspectJUIPlugin.getDefault().disableBuildConfiguratorResourceChangeListener();
		// need to iterate through the selected projects......
		List checkedProjects =  checkedListDialogField.getCheckedElements();
		monitor.worked((ajProjects.size() - checkedProjects.size())*20);
		for (Iterator iter = checkedProjects.iterator(); iter.hasNext();) {
            IProject project = (IProject) iter.next();
            convertAspectsToAJAndOthersToJava(true,true, project, monitor); 
        }
		AspectJUIPlugin.getDefault().enableBuildConfiguratorResourceChangeListener();
	}

	public static void convertAspectsToAJAndOthersToJava(
		final boolean includeNonBuiltFiles, 
		final boolean updateBuildConfigs,
		final IProject project,
		IProgressMonitor monitor) {
		    try {
                   project.build(IncrementalProjectBuilder.FULL_BUILD,
                           AspectJPlugin.ID_BUILDER, null,
                           new NullProgressMonitor());
                   
            } catch (CoreException e1) {
            } 
        	monitor.worked(6);
				
			IJavaProject jp = JavaCore.create(project);
			ProjectBuildConfigurator pbc = BuildConfigurator
					.getBuildConfigurator().getProjectBuildConfigurator(jp);
			BuildConfiguration activeBuildConfig = pbc
					.getActiveBuildConfiguration();
			int numBuildConfigs = pbc.getBuildConfigurations().size();
			try {
				IPackageFragment[] packages = jp.getPackageFragments();
//				monitor
//						.beginTask(
//								AspectJUIPlugin
//										.getResourceString("Refactoring.ConvertingFileExtensions"),
//								packages.length + (10 * numBuildConfigs));
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
								boolean isAspect = CodeChecker.containsAspectJConstructs((IFile)resource);
								if (!isAspect
										&& resource.getFileExtension()
												.equals("aj")) { //$NON-NLS-1$								
									RenamingUtils.renameFile(false, resource, new NullProgressMonitor(),
											oldToNewNames);
								} else if (isAspect
										&& resource.getFileExtension()
												.equals("java")) { //$NON-NLS-1$
								    RenamingUtils.renameFile(true, resource, new NullProgressMonitor(),
											oldToNewNames);
								}
							}
						} catch (JavaModelException e) {
						}
					}
				}
				monitor.worked(10);
				if (updateBuildConfigs) {
				    RenamingUtils.updateBuildConfigurations(oldToNewNames, project,
				    		 new NullProgressMonitor());
				}
				monitor.worked(4);
			} catch (JavaModelException e) {
			}
	}
}
