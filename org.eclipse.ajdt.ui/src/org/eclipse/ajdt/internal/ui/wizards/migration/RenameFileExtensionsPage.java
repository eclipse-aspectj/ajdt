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

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.refactoring.RenamingUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jface.viewers.LabelProvider;
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
		super(AspectJUIPlugin.getResourceString("RenameFileExtensionsPage.name")); //$NON-NLS-1$
		this.setTitle(AspectJUIPlugin.getResourceString("RenameFileExtensionsPage.title"));	//$NON-NLS-1$	
		this.setDescription( AspectJUIPlugin.getResourceString("RenameFileExtensionsPage.description")); //$NON-NLS-1$
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
		        /* 0 */ NewWizardMessages.getString("ProjectsWorkbookPage.projects.checkall.button"), //$NON-NLS-1$
		        /* 1 */ NewWizardMessages.getString("ProjectsWorkbookPage.projects.uncheckall.button") //$NON-NLS-1$
		};

		checkedListDialogField = new CheckedListDialogField(null, buttonLabels, new AJProjectListLabelProvider());
		checkedListDialogField.setLabelText(AspectJUIPlugin
				.getResourceString("RenameFileExtensionsMigrationPage.message")); //$NON-NLS-1$
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
	
	public void finishPressed() {
		AspectJUIPlugin.getDefault().disableBuildConfiguratorResourceChangeListener();
		// need to iterate through the selected projects......
		List checkedProjects =  checkedListDialogField.getCheckedElements();
		for (Iterator iter = checkedProjects.iterator(); iter.hasNext();) {
            IProject project = (IProject) iter.next();
            RenamingUtils.convertAspectsToAJAndOthersToJava(true,true, project, getShell());            
        }
		AspectJUIPlugin.getDefault().enableBuildConfiguratorResourceChangeListener();
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
