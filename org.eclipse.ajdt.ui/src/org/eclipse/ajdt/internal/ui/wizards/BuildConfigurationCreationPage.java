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

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.ajdt.internal.ui.ajde.ProjectProperties;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;

/**
 * This class is the only page of the ".lst" file resource creation wizard.  
 * It subclasses the standard file resource creation page class, 
 * and consequently inherits the file resource creation functionality.
 */
public class BuildConfigurationCreationPage extends WizardNewFileCreationPage {
	private IWorkbench workbench;

	// widgets
	private Button includeProjectFilesCheckbox;
	private Button subsectionCheckbox;
	private Button openFileCheckbox;

	// constants
	private static int nameCounter = 1;
	
	
	/**
	 * Creates the page for the build configuration file creation wizard.
	 *
	 * @param workbench  the workbench on which the page should be created
	 * @param selection  the current selection
	 */
	public BuildConfigurationCreationPage(
		IWorkbench workbench,
		IStructuredSelection selection) {
		super("CreateBuildConfigurationFilePage1", selection);
		this.setTitle( AspectJUIPlugin.getResourceString( "BuildConfig.createLstFile" ) ); 
		this.setDescription( AspectJUIPlugin.getResourceString( "BuildConfig.createLstDesc" ) );
		this.workbench = workbench;
	}
	
	/**
     * Build the GUI representation of the page.
	 */
	public void createControl(Composite parent) {
		// inherit default container and name specification widgets
		super.createControl(parent);
		Composite composite = (Composite) getControl();
		
        //ASCFIXME: Add help!
		//WorkbenchHelp.setHelp(composite, new String[] {IReadmeConstants.CREATION_WIZARD_PAGE_CONTEXT});

		GridData data = (GridData) composite.getLayoutData();

		//ASCFIXME: What happens to name counter after workbench restart???		
		this.setFileName("buildConfig" + nameCounter + ".lst");

		new Label(composite, SWT.NONE); // vertical spacer

		// sample section generation group
		Group group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setText( AspectJUIPlugin.getResourceString( "BuildConfig.autoPopulate" ) );
		group.setLayoutData(
			new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

		// sample section generation checkboxes
		includeProjectFilesCheckbox = new Button(group, SWT.CHECK);
		includeProjectFilesCheckbox.setText(AspectJUIPlugin.getResourceString( "BuildConfig.includeAllSource" ) );
		includeProjectFilesCheckbox.setSelection(true);
		includeProjectFilesCheckbox.addListener(SWT.Selection, this);

//		subsectionCheckbox = new Button(group, SWT.CHECK);
//		subsectionCheckbox.setText("Do something really useful");
//		subsectionCheckbox.setSelection(true);
//		subsectionCheckbox.addListener(SWT.Selection, this);

		new Label(composite, SWT.NONE); // vertical spacer

		// open file for editing checkbox
		openFileCheckbox = new Button(composite, SWT.CHECK);
		openFileCheckbox.setText(AspectJUIPlugin.getResourceString( "BuildConfig.openForEdit" ) );
		openFileCheckbox.setSelection(true);

		setPageComplete(validatePage());

	}
	
	
	/**
	 * Creates a new file resource as requested by the user. If everything
	 * is OK then answer true. If not, false will cause the dialog
	 * to stay open.
	 *
	 * @return whether creation was successful
	 * @see ReadmeCreationWizard#performFinish()
	 */
	public boolean finish() {
		// create the new file resource
		IFile newFile = createNewFile();

		
		if (newFile == null)
			return false; // ie.- creation was unsuccessful

		// Since the file resource was created fine, open it for editing
		// if requested by the user
		try {
			if (openFileCheckbox.getSelection()) {
				IWorkbenchWindow dwindow = workbench.getActiveWorkbenchWindow();
				IWorkbenchPage page = dwindow.getActivePage();
				if (page != null) IDE.openEditor(page,newFile,true);
			}
		} catch (PartInitException e) {
			e.printStackTrace();
			return false;
		}
		nameCounter++;
		AJDTEventTrace.newConfigFileCreated( newFile );
		return true;
	}
	
	
	/** 
	 * The <code>BuildConfigurationCreationPage</code> implementation of this
	 * <code>WizardNewFileCreationPage</code> method 
	 * generates includes for all project files if checked.
	 */
	protected InputStream getInitialContents() {
		
		// Check if they wanted to include all project source files in this config file
		if (!includeProjectFilesCheckbox.getSelection())
			return null;
			
			
		IProject proj = AspectJUIPlugin.getDefault().getCurrentProject();
		
		// Where in the project is the user creating the new configuration file?
		String containerFullPath =  this.getContainerFullPath().makeAbsolute().toOSString();
		// containerFullPath will be something like \TracingAspects\src\tracing\version3
		
		java.util.List projectFiles = AspectJUIPlugin.getDefault( ).getAjdtProjectProperties( ).getProjectSourceFiles( proj, ProjectProperties.ASPECTJ_SOURCE_FILTER );
		
		// Work out the full path in the file system to the workspace
		String projectPath = proj.getFullPath().toOSString();
		IPath workspacePath = proj.getLocation();
		workspacePath = workspacePath.removeLastSegments(1);
		// workspacePath will be something like C:\eclipse\workspace
		String fullPath = workspacePath.toOSString() + containerFullPath;
		// fullPath will be something like c:\eclipse\workspace\TracingAspects\src\tracing\version3
		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < projectFiles.size(); i++) {
			
			    // Go through all the files in the project.  
				File file = (File)projectFiles.get(i);
				
				// Check the file is within the containerFullPath location or below.
				String filename = (String) (file.getAbsolutePath());

				filename = getRelativePath(fullPath, filename);
				sb.append(filename);
				sb.append("\n");
		}	
		return new ByteArrayInputStream(sb.toString().getBytes());
	}
	
	//helper function to obtain rel path from source folder to destination file using ../
	private String getRelativePath(String source, String dest){
		//AJDTEventTrace.generalEvent("\nCreating relative path for\n" + source + "\n" + dest);
		source = source.replace('/', '\\');
		dest = dest.replace('/', '\\');
		
		//if on different drive, return absolute path
		int isource = source.indexOf('\\');
		int idest = dest.indexOf('\\');
		if ((idest != isource) || (!source.startsWith(dest.substring(0,idest)))){
			return dest;
		}
		
		source = source.substring(isource + 1).concat("\\");
		dest = dest.substring(idest + 1);
		String relPath = "";
		String curfols, curfold;
		boolean different = false;
		isource = source.indexOf('\\');
		idest = dest.indexOf('\\');
		while(isource > 0 && idest > 0){
			curfols = source.substring(0, isource);
			curfold = dest.substring(0, idest);			
			if (different || !curfols.equals(curfold)){
				different = true;
				relPath = "..\\".concat(relPath.concat(curfold + "\\"));
			}
			source = source.substring(isource + 1);
			dest = dest.substring(idest + 1);
			isource = source.indexOf('\\');
			idest = dest.indexOf('\\');
		}
		if(idest <= 0){
			while (isource > 0){
				curfols = source.substring(0, isource);		
				relPath = "..\\".concat(relPath);
				source = source.substring(isource + 1);
				isource = source.indexOf('\\');
			}
		}
		//AJDTEventTrace.generalEvent("Result: " + relPath.concat(dest));
		return relPath.concat(dest);
	}


	/** (non-Javadoc)
	 * Method declared on WizardNewFileCreationPage.
	 */
	protected String getNewFileLabel() {
		return AspectJUIPlugin.getResourceString( "BuildConfig.newLstFile" );
	}

}