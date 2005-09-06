/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer, Andy Clement, Tracy Gardner - initial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Simple wizard page, presents two lists to the user.  On the left is a list
 * of the new resources that appear to have been added to the project.
 * On the right is a list of build configuration files in the current project.
 * This wizard page builds the two lists and handles the selection/deselection
 * of the various entries - once finish is selected, the wizard itself updates
 * the checked build configurations with the checked resources.
 * 
 * @author Andy Clement
 */
public class ResourceAddedPage
	extends WizardPage
	implements SelectionListener {


	// The resources to display in the left hand window
	private List newResourcesList;
	// A tree representation of the resources, with nodes that are checked/unchecked
	private Tree newResourcesTree;

	// The build configuration files to display in the right hand window
	private List buildConfigFiles;
	// A tree representation of the config files, with nodes that are checked/unchecked
	private Tree buildConfigFilesTree;

	private IProject project; // Relevant project for the set of resources

	private Composite topLevel; // Top level container for the page GUI components

	/**
	 * Set the various title and description fields for the wizard page and
	 * sort out the data that will be displayed.
	 */
	public ResourceAddedPage(
		List newResources) {

		super("ResourceAddedPage1", "", null); //$NON-NLS-1$ //$NON-NLS-2$

		this.newResourcesList = newResources;
		if (newResources == null)
			newResources = new ArrayList();

		project = ((IResource) newResourcesList.get(0)).getProject();
		AspectJPlugin.getDefault().setCurrentProject(project);

		setDescription(
			UIMessages.newResourcesWizard_pageDescription);
		setTitle(
			UIMessages.newResourcesWizard_pageTitle
				+ project.getName().toString());

		// Retrieve a list of all the .lst files that exist in that project
		buildConfigFiles =
			AspectJUIPlugin.getDefault().getListOfConfigFilesForCurrentProject();

		// ASCCHECKME: Shouldnt be possible to get here if buildConfigFiles was going to return null...
		// it was policed in the ResourceChangeListener code

		// Remove any of those config files if they are brand new and candidates in the newResources list!
		for (int i = 0; i < buildConfigFiles.size(); i++) {
			if (newResources.contains(buildConfigFiles.get(i))) {
				buildConfigFiles.remove(i);
			}
		}

	}

	/**
	 * The finish button can be clicked at anytime after the page is popped up.
	 */
	public boolean finish() {
		return true;
	}

	/**
	 * Build the controls that make up the page.
	 */
	public void createControl(Composite composite) {

		// Initialize the top level container for the controls
		topLevel = new Composite(composite, SWT.NONE);
		topLevel.setLayout(new GridLayout());

		// Inform the user what they have to do via a label
		Label l = new Label(topLevel, SWT.NONE);
		l.setText(UIMessages.newResourcesWizard_instructions);
			
		// Create a two column grid, left hand column is for resources, right hand column is for build config files
		GridLayout glayout = new GridLayout();
		Group group = new Group(topLevel, SWT.SHADOW_NONE);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		glayout.numColumns = 2;
		group.setLayout(glayout);

		// Calculate the appropriate path to trim off the full resource paths, so that
		// they present nicely in the lists.
		IPath trimPath = AspectJPlugin.getDefault().getCurrentProject().getLocation();
		trimPath = trimPath.removeLastSegments(1);
		String trimPathString = new String(trimPath.toOSString() + File.separator);

		// Populate the left hand group, a list and two buttons (selectAll/deselectAll)
		Group resGroup = new Group(group, SWT.NONE);
		GridLayout resLayout = new GridLayout();
		resLayout.numColumns = 1;
		resGroup.setLayout(resLayout);
		resGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		// The list of resources is presented as a checkbox tree
		newResourcesTree = new Tree(resGroup, SWT.CHECK | SWT.MULTI);
		newResourcesTree.setLayoutData(new GridData(GridData.FILL_BOTH));

		ListIterator resIterator = newResourcesList.listIterator();
		while (resIterator.hasNext()) {
			IResource iresource = (IResource) resIterator.next();
			String absolutePath = iresource.getLocation().toOSString();
			TreeItem titem = new TreeItem(newResourcesTree, SWT.NULL);
			titem.setText(absolutePath.substring(trimPathString.length()));
			titem.setData(iresource); // Store the iresource in the tree node for use later
			titem.setChecked(true); // Select all nodes by default
		}


		// Populate the right hand group, a list and two buttons (selectAll/deselectAll)
		Group bcfgGroup = new Group(group, SWT.NONE);
		GridLayout bcfgLayout = new GridLayout();
		bcfgLayout.numColumns = 1;
		bcfgGroup.setLayout(bcfgLayout);
		bcfgGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		// The list of resources is presented as a checkbox tree
		buildConfigFilesTree = new Tree(bcfgGroup, SWT.CHECK | SWT.MULTI);
		buildConfigFilesTree.setLayoutData(new GridData(GridData.FILL_BOTH));

		ListIterator iterator = buildConfigFiles.listIterator();
		while (iterator.hasNext()) {
			IResource iresource = (IResource) iterator.next();
			String absolutePath = iresource.getLocation().toOSString();
			TreeItem titem = new TreeItem(buildConfigFilesTree, SWT.NULL);
			titem.setText(absolutePath.substring(trimPathString.length()));
			titem.setData(iresource);
			titem.setChecked(true);

		}

		// Add the four buttons
		
		Button resSelectAllButton = new Button(group, SWT.NONE);
		resSelectAllButton.setText(UIMessages.newResourcesWizard_selectAllResources);
		resSelectAllButton.setLayoutData(
			new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		resSelectAllButton.addSelectionListener(this);

		Button bcfgSelectAllButton = new Button(group, SWT.NONE);
		bcfgSelectAllButton.setText(UIMessages.newResourcesWizard_selectAllConfigurations);
		bcfgSelectAllButton.setLayoutData(
			new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		bcfgSelectAllButton.addSelectionListener(this);

		Button resSelectNoneButton = new Button(group, SWT.NONE);
		resSelectNoneButton.setText(UIMessages.newResourcesWizard_deselectAllResources);
		resSelectNoneButton.setLayoutData(
			new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		resSelectNoneButton.addSelectionListener(this);

		Button bcfgSelectNoneButton = new Button(group, SWT.NONE);
		bcfgSelectNoneButton.setText(UIMessages.newResourcesWizard_deselectAllConfigurations);
		bcfgSelectNoneButton.setLayoutData(
			new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		bcfgSelectNoneButton.addSelectionListener(this);

		setErrorMessage(null);
		setMessage(null);
		setControl(topLevel);

		setPageComplete(true);
	}

	/**
	 * Delegate to the widgetSelected() method
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/**
	 * Called when a button is clicked, performs the appropriate action, which will
	 * either to be select all, or deselect all of the resources or build config
	 * files.
	 */
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() instanceof Button) {
			Tree treeToAffect = null;
			boolean select = false;

			// Depending on which button was clicked, update the appropriate tree
			Button b = (Button) e.getSource();
			String button_text = b.getText();
			if (button_text.equals(UIMessages.newResourcesWizard_selectAllResources)) {
				treeToAffect = newResourcesTree;
				select = true;
			} else if (button_text.equals(UIMessages.newResourcesWizard_selectAllConfigurations)) {
				treeToAffect = buildConfigFilesTree;
				select = true;
			} else if (button_text.equals(UIMessages.newResourcesWizard_deselectAllResources)) {
				treeToAffect = newResourcesTree;
				select = false;
			} else if (button_text.equals(UIMessages.newResourcesWizard_deselectAllConfigurations)) {
				treeToAffect = buildConfigFilesTree;
				select = false;
			}

			// Now perform the action...
			if (treeToAffect != null) {
				TreeItem[] items = treeToAffect.getItems();
				for (int i = 0; i < items.length; i++)
					items[i].setChecked(select);
			}
		}
	}

	/** 
	 * Called by the wizard to determine the list of config files selected
	 * in the wizard page.
	 */
	protected List getSelectedResources() {
		List results = new ArrayList();
		TreeItem[] items = newResourcesTree.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getChecked())
				results.add(items[i].getData());
		}
		return results;
	}

	/** 
	 * Called by the wizard to determine the list of config files selected
	 * in the wizard page.
	 */
	protected List getSelectedBuildConfigFiles() {
		List results = new ArrayList();
		TreeItem[] items = buildConfigFilesTree.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getChecked())
				results.add(items[i].getData());
		}
		return results;
	}

}