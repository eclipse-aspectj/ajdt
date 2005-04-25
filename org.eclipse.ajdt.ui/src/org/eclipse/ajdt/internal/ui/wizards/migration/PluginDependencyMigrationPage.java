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

import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.internal.core.AJDTUtils;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * This page handles changing the plugin dependency of plugin projects
 * from org.aspectj.ajde to the smaller org.aspectj.runtime plugin. Because
 * there may be legitimate reasons why someone might want to keep their
 * dependency on org.aspectj.ajde, the user is presented with a check list
 * of projects they wish to switch. 
 */
public class PluginDependencyMigrationPage extends WizardPage {

	private CheckedListDialogField checkedListDialogField;

	private List ajPluginProjects;
	
	private PluginDependencyMigrationPage() {
		super(AspectJUIPlugin.getResourceString("PluginDependencyMigrationPage.name")); //$NON-NLS-1$
		this.setTitle(AspectJUIPlugin.getResourceString("PluginDependencyMigrationPage.title")); //$NON-NLS-1$		
		this.setDescription( AspectJUIPlugin
				.getResourceString("PluginDependencyMigrationPage.description")); //$NON-NLS-1$
	}
	
	protected PluginDependencyMigrationPage(List projects) {
	    this();
	    ajPluginProjects = projects;
	}
	
	public void createControl(Composite parent) {		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(composite);

		String[] buttonLabels= new String[] {
		        /* 0 */ NewWizardMessages.getString("ProjectsWorkbookPage.projects.checkall.button"), //$NON-NLS-1$
		        /* 1 */ NewWizardMessages.getString("ProjectsWorkbookPage.projects.uncheckall.button") //$NON-NLS-1$
		};

		checkedListDialogField = new CheckedListDialogField(null, buttonLabels, new AJProjectListLabelProvider());
		checkedListDialogField.setLabelText(AspectJUIPlugin
				.getResourceString("PluginDependencyMigrationPage.updatePluginDependencyButton.message")); //$NON-NLS-1$
		checkedListDialogField.setCheckAllButtonIndex(0);
		checkedListDialogField.setUncheckAllButtonIndex(1);
		checkedListDialogField.setElements(ajPluginProjects);
		checkedListDialogField.setCheckedElements(ajPluginProjects);
		checkedListDialogField.setViewerSorter(new ViewerSorter());
		
		LayoutUtil.doDefaultLayout(composite, new DialogField[] { checkedListDialogField }, true, SWT.DEFAULT, SWT.DEFAULT);
		LayoutUtil.setHorizontalGrabbing(checkedListDialogField.getListControl(null));
		
		PixelConverter converter= new PixelConverter(parent);
		int buttonBarWidth= converter.convertWidthInCharsToPixels(24);
		checkedListDialogField.setButtonsMinWidth(buttonBarWidth);
	}
	
	public void finishPressed(IProgressMonitor monitor) {
	    List checkedProjects =  checkedListDialogField.getCheckedElements();
		for (Iterator iter = checkedProjects.iterator(); iter.hasNext();) {
			IProject project = (IProject) iter.next();
			updatePluginDependency(project);
			monitor.worked(2);
		}
		monitor.worked(2*(ajPluginProjects.size() - checkedProjects.size()));
	}
	
	private void updatePluginDependency(IProject project) {
		boolean hasAJDEDependency = hasAJDEPluginDependency(project);
		boolean hasRuntimeDependency = AJDTUtils.hasAJPluginDependency(project);
		
		if (hasRuntimeDependency) {
			if (!hasAJDEDependency) {
				return;
			} else {
				removeAJDEPluginDependency(project);
			}	
		} else {
			AJDTUtils.importRuntimePlugin(project);			
			if (hasAJDEDependency) {
				removeAJDEPluginDependency(project);
			}
		}
	}
	
	private boolean hasAJDEPluginDependency(IProject project) {
		ManifestEditor manEd = AJDTUtils.getAndPrepareToChangePDEModel(project);
		//ManifestEditor manEd = AJDTUtils.getPDEManifestEditor(project);
		IPluginModel model = null;
		IPluginImport[] imports = null;

		if (manEd != null) {
			model = (IPluginModel) manEd.getAggregateModel();
			imports = model.getPluginBase().getImports();
		} else {
			try {
				//checks the classpath for plugin dependencies
				IPackageFragmentRoot[] dependencies = JavaCore.create(project)
						.getPackageFragmentRoots();
				for (int i = 0; i < dependencies.length; i++) {
					if (dependencies[i].getElementName().equals(
							"aspectjrt.jar")) // $NON-NLS-1$
						return true;
				}
			} catch (JavaModelException e) {
			}
			return false;
		}

		for (int i = 0; i < imports.length; i++) {
			IPluginImport importObj = imports[i];
			if (importObj.getId().equals("org.aspectj.ajde")) { // $NON-NLS-1$
				return true;
			}
		}
		return false;		
	}
	
	private void removeAJDEPluginDependency(IProject project) {
		IWorkbenchWindow window = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		// Attempt to get hold of the open manifest editor
		// for the current project.
		//ManifestEditor manEd = AJDTUtils.getPDEManifestEditor(project);
		ManifestEditor manEd = AJDTUtils.getAndPrepareToChangePDEModel(project);

		if (manEd != null) {
			IPluginModel model = (IPluginModel) manEd.getAggregateModel();
			try {
				AJDTUtils.removeImportFromPDEModel(model, "org.aspectj.ajde"); // $NON-NLS-1$
				manEd.doSave(new NullProgressMonitor());
			} catch (CoreException e) {
				AspectJUIPlugin
						.getDefault()
						.getErrorHandler()
						.handleError(
								AspectJUIPlugin
										.getResourceString("AutoPluginRemoveErrorDialog.title"),
								AspectJUIPlugin
										.getResourceString("AutoPluginRemoveErrorDialog.message"),
								e);
			}
		}// end if we got a reference to the manifest editor
		else {
			MessageDialog
					.openError(
							AspectJUIPlugin.getDefault().getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
							AspectJUIPlugin
									.getResourceString("AutoPluginRemoveDialog.noEditor.title"),
							AspectJUIPlugin
									.getResourceString("AutoPluginRemoveDialog.noEditor.message"));
		}
	}
	
	private class AJProjectListLabelProvider extends LabelProvider {
	    public String getText(Object element) {
	        if (element instanceof IProject) {
                return ((IProject)element).getName();
            }
	    	return element.toString();
	    }
	}
}
