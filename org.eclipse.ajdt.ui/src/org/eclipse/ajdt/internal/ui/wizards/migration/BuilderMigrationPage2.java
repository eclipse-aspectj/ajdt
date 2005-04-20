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

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.osgi.service.prefs.BackingStoreException;

/**
 * This page deals with other builder related changes, namely switching
 * the default compilation mode to be incremental, and removing all the 
 * redundent files (.ajsym files etc). Under the covers, this page also
 * updates any AJDT settings to the new way of recording them which means
 * that these settings will be maintained, for example whether the user is
 * using project compiler settings or not.
 */
public class BuilderMigrationPage2 extends WizardPage {

	// on this page:
	//  - set incremental as the default
	//  - clean up the generated.ajsym files and .generated.lst file

	private Button useIncrementalButton;
	private Button clearupRedundantFilesButton;
	
	protected BuilderMigrationPage2() {
		super(AspectJUIPlugin.getResourceString("BuilderMigrationPage2.name")); //$NON-NLS-1$
		this.setTitle(AspectJUIPlugin.getResourceString("BuilderMigrationPage2.title")); //$NON-NLS-1$		
		this.setDescription( AspectJUIPlugin.
				getResourceString("BuilderMigrationPage2.description")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(composite);
		
		Label label2 = new Label(composite, SWT.NONE);
		label2.setText(AspectJUIPlugin
				.getResourceString("BuilderMigrationPage2.useIncrementalButton.message")); //$NON-NLS-1$

		Label spacer2 = new Label(composite, SWT.NONE);
		
		useIncrementalButton = new Button(composite, SWT.CHECK);
		useIncrementalButton.setText(AspectJUIPlugin
				.getResourceString("BuilderMigrationPage2.useIncrementalButton.label")); //$NON-NLS-1$
		useIncrementalButton.setSelection(true);
		
		Label spacer3 = new Label(composite, SWT.NONE);
		
		Label label3 = new Label(composite, SWT.NONE);
		label3.setText(AspectJUIPlugin
				.getResourceString("BuilderMigrationPage2.clearupRedundantFilesButton.message")); //$NON-NLS-1$

		Label spacer4 = new Label(composite, SWT.NONE);
		
		clearupRedundantFilesButton = new Button(composite, SWT.CHECK);
		clearupRedundantFilesButton.setText(AspectJUIPlugin
				.getResourceString("BuilderMigrationPage2.clearupRedundantFilesButton.label")); //$NON-NLS-1$
		clearupRedundantFilesButton.setSelection(true);

	}
	
	public void finishPressed(List ajProjects) {
		if (useIncrementalButton.getSelection()) {
			useIncrementalCompilationAsDefault(ajProjects);
		}
		if (clearupRedundantFilesButton.getSelection()) {
			clearupRedundantFilesButton(ajProjects);
		}		
	}
	
	private void useIncrementalCompilationAsDefault(List ajProjects) {
		// update the workbench compiler settings
		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
		store.setDefault(AspectJPreferences.OPTION_Incremental, true);
		store.setToDefault(AspectJPreferences.OPTION_Incremental);
		
		final QualifiedName INCREMENTAL_COMPILATION =
			new QualifiedName("org.eclipse.ajdt.ui", "BuildOptions.incrementalMode");
		
		for (Iterator iter = ajProjects.iterator(); iter.hasNext();) {
			IProject project = (IProject) iter.next();
			// in AJDT 1.2.0 M2 set whether user chose to use project settings
			// in by the following
			boolean useProjectSettings = AspectJPreferences.isUsingProjectSettings(project);
			if (!useProjectSettings) {
				// in AJDT 1.1.12 recorded whether project was using project settings
				// by the following
				useProjectSettings = store.getBoolean(project + "useProjectSettings");
			}
			if (useProjectSettings) {
				// HELEN - hard coded string!!!!!!!
				setPrefValue(project,AspectJPreferences.OPTION_Incremental,"true");
				flushPrefs(project);
			}
			// in the world of AJDT 1.1.12, we set the following persistent
			// property if the user wanted to use incremental compilation - we
			// therefore want to unset this
			try {
				if (project.getPersistentProperty(INCREMENTAL_COMPILATION) != null) {
					project.setPersistentProperty(INCREMENTAL_COMPILATION,null);
				}
			} catch (CoreException e) {
			}
		}
	}
	
	private void setPrefValue(IProject project, String key, String value) {
    	IScopeContext projectScope = new ProjectScope(project);
    	IEclipsePreferences projectNode = projectScope.getNode(AspectJPlugin.PLUGIN_ID);
    	projectNode.put(key,value);
	}

	private void flushPrefs(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope.getNode(AspectJPlugin.PLUGIN_ID);
       	try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}
	
	private void clearupRedundantFilesButton(List ajProjects) {
		for (Iterator iter = ajProjects.iterator(); iter.hasNext();) {
			IProject project = (IProject) iter.next();
			IFile ajsmFile = project.getFile(".generated.ajsym");
			IFile generatedFile = project.getFile(".generated.lst");
			// HELEN - do we want null progress monitors?
			if (ajsmFile != null) {
				try {
					ajsmFile.delete(true,null);
				} catch(CoreException ce) {
				}	
			}
			if (generatedFile != null) {
				try {
					generatedFile.delete(true,null);
				} catch (CoreException e) {
				}				
			}
		}	
	}

}
