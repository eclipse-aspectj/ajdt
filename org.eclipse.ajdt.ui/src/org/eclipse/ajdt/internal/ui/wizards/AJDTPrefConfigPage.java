/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Mik Kersten, Julie Waterhouse - initial version
Julie Waterhouse - removed methods for new aspect and AspectJ project.  
This functionality has moved to the plugin.xml. - Aug 13, 2003
...
**********************************************************************/
package org.eclipse.ajdt.internal.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ajdt.internal.ui.AJDTConfigSettings;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;

/**
 * This class is the only page of the AJDT preferences configuration wizard.  
 */
public class AJDTPrefConfigPage extends WizardPage {
	private IWorkbench workbench;

	// widgets
	private Button aspectJEditorDefaultCheckbox = null;
//	private Button unusedImportsCheckbox = null;
	private Button analyzeAnnotationsCheckbox = null;
	private Button openXRefView;
//	private Button dontAskAgainCheckbox = null;
	
	/**
	 * Creates the page AJDT Preference Configuration main (only) page.
	 */
	public AJDTPrefConfigPage(){
		super(AspectJUIPlugin.getResourceString("AJDTPrefConfigWizardPage.name"));
		this.setTitle(AspectJUIPlugin.getResourceString("AJDTPrefConfigWizardPage.title"));		
		this.setDescription( AspectJUIPlugin.getResourceString("AJDTPrefConfigWizardPage.description"));
	}
	
	/**
     * Build the GUI representation of the page.
     * @param Composite The parent control.
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setControl(composite);
	
		// override: always create checkboxes
		// create workbench preferences group
		//if (!AJDTConfigSettings.isAspectJEditorDefault()
		//		/*||
		//	!AJDTConfigSettings.isUnusedImportsDisabled()*/ ||
		//	!AJDTConfigSettings.isAnalyzeAnnotationsDisabled()) {
			Group workbench = new Group(composite, SWT.NONE);
			workbench.setLayout(new GridLayout());
			workbench.setText(AspectJUIPlugin.getResourceString("AJDTPrefConfigWizardPage.workbench.group"));
			workbench.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
			// create workbench preferences checkboxes; only create checkboxes
			// for those settings that are not already configured for AJDT
			//if (!AJDTConfigSettings.isAspectJEditorDefault()) {			
				aspectJEditorDefaultCheckbox = new Button(workbench, SWT.CHECK);		
				aspectJEditorDefaultCheckbox.setText(AspectJUIPlugin.getResourceString("AJDTPrefConfigWizardPage.workbench.ajeditordefault"));
				aspectJEditorDefaultCheckbox.setSelection(false);
				
				Label label = new Label(workbench, SWT.NONE);
				label.setText(AspectJUIPlugin.getResourceString("AJDTPrefConfigWizardPage.editorMessage"));
				GridData gd = new GridData(GridData.FILL_HORIZONTAL);
				gd.horizontalIndent = 20;
				label.setLayoutData(gd);							

			//}
//			if (!AJDTConfigSettings.isUnusedImportsDisabled()) {	
//				unusedImportsCheckbox = new Button(workbench, SWT.CHECK);	
//				unusedImportsCheckbox.setText(AspectJPlugin.getResourceString("AJDTPrefConfigWizardPage.workbench.unusedimports"));
//				unusedImportsCheckbox.setSelection(true);
//			}
			//if (!AJDTConfigSettings.isAnalyzeAnnotationsDisabled()) {	
				analyzeAnnotationsCheckbox = new Button(workbench, SWT.CHECK);		
				analyzeAnnotationsCheckbox.setText(AspectJUIPlugin.getResourceString("AJDTPrefConfigWizardPage.workbench.analyzeannotations"));
				analyzeAnnotationsCheckbox.setSelection(false);
				
				label = new Label(workbench, SWT.NONE);
				label.setText(AspectJUIPlugin.getResourceString("AJDTPrefConfigWizardPage.annotationsMessage"));
				gd = new GridData(GridData.FILL_HORIZONTAL);
				gd.horizontalIndent = 20;
				label.setLayoutData(gd);							

				openXRefView = new Button(workbench, SWT.CHECK);		
				openXRefView.setText(AspectJUIPlugin.getResourceString("AJDTPrefConfigWizardPage.workbench.openXRefView"));
				openXRefView.setSelection(true);
			//}
		//}	
		new Label(composite, SWT.NONE); // vertical spacer
		
		// create "don't ask again" checkbox
//		dontAskAgainCheckbox = new Button(composite, SWT.CHECK);
//		dontAskAgainCheckbox.setText(AspectJUIPlugin.getResourceString("AJDTPrefConfigWizardPage.workbench.askagain"));
//		dontAskAgainCheckbox.setSelection(true);
	}
	
	
	/**
	 * Applies the AJDT Conmfiguration settings chosen by the user. If everything
	 * is OK then answer true. If not, false will cause the dialog
	 * to stay open.
	 *
	 * @return boolean whether creation was successful
	 * @see ReadmeCreationWizard#performFinish()
	 */
	public boolean finish() {
		boolean makeAspectJEditorDefault = false;
		if (aspectJEditorDefaultCheckbox != null) {
			makeAspectJEditorDefault = aspectJEditorDefaultCheckbox.getSelection();
		} 
//		boolean disableUnusedImports = false;
//		if (unusedImportsCheckbox != null) {
//			disableUnusedImports = unusedImportsCheckbox.getSelection();
//		}
		
		boolean disableAnalyzeAnnotations = false;
		if (analyzeAnnotationsCheckbox != null) {
			disableAnalyzeAnnotations = analyzeAnnotationsCheckbox.getSelection();
		} 
			
		//boolean dontAskAgain = dontAskAgainCheckbox.getSelection();
		
		// turn the "analyse annotations" off
		AJDTConfigSettings.disableAnalyzeAnnotations(disableAnalyzeAnnotations);
		
		
		// set the unused imports to warning, rather than error
//		if (disableUnusedImports) {
//			AJDTConfigSettings.disableUnusedImports();
//		}
		
		// set the default editor for .java files
		AJDTConfigSettings.setDefaultEditorForJavaFiles(makeAspectJEditorDefault);
			
		if(openXRefView.getSelection()) {
			try {
				AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(XReferenceView.ID);
			} catch (PartInitException e) {
				AspectJUIPlugin.getDefault().getErrorHandler().handleError(
						AspectJUIPlugin.getResourceString("AJDTPrefConfigWizardPage.ErrorOpeningXRefView"), e);
			}
		}
		return true;
	}
}