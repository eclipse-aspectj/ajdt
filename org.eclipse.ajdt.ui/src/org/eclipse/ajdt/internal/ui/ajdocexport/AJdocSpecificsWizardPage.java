/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Luzius Meisser  - adjusted for ajdoc 
 *     Helen Hawkins   - updated for Eclipse 3.1 (bug 109484) 
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.ajdocexport;

import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.javadocexport.JavadocExportMessages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * Copied from org.eclipse.jdt.internal.ui.javadocexport.JavadocSpecificWizardPage
 * Updated for Eclipse 3.1 - bug 109484
 * Changes marked with // AspectJ Extension
 */
public class AJdocSpecificsWizardPage extends AJdocWizardPage {

//  AspectJ Extension begin - commenting out unused code
//  private Button fAntBrowseButton;
	private Button fCheckbrowser;
/*  private Text fAntText;
	private Button fOverViewButton;
	private Button fOverViewBrowseButton;
	private Button fAntButton;
*/	private Combo fSourceCombo;
	
	private Composite fLowerComposite;
	// private Text fOverViewText;
	private Text fExtraOptionsText;
	private Text fVMOptionsText;

	// private StatusInfo fOverviewStatus;
	// private StatusInfo fAntStatus;
	
	// private JavadocTreeWizardPage fFirstPage;
	// AspectJ Extension end
	
	// AspectJ Extension - using AJdocOptionsManager rather than JavadocOptionsManager
	private AJdocOptionsManager fStore;

//  AspectJ Extension - commenting out unused code
//  private final int OVERVIEWSTATUS= 1;
//  private final int ANTSTATUS= 2;

	// AspectJ Extension - using AJdocTreeWizardPage and AJdocOptionsManager in constructor
	protected AJdocSpecificsWizardPage(String pageName, AJdocTreeWizardPage firstPage, AJdocOptionsManager store) {
		super(pageName);
        // AspectJ Extension - using AJDT messages to set the description	
		setDescription(UIMessages.ajdocSpecificsWizardPage_description);
		fStore= store;
		
        // AspectJ Extension - commenting out unused code
		/* fOverviewStatus= new StatusInfo();
		fAntStatus= new StatusInfo();
		fFirstPage= firstPage;*/
	}

	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		fLowerComposite= new Composite(parent, SWT.NONE);
		fLowerComposite.setLayoutData(createGridData(GridData.FILL_BOTH, 1, 0));

		GridLayout layout= createGridLayout(3);
		layout.marginHeight= 0;
		fLowerComposite.setLayout(layout);

		createExtraOptionsGroup(fLowerComposite);
		createAntGroup(fLowerComposite);

		setControl(fLowerComposite);
		Dialog.applyDialogFont(fLowerComposite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(fLowerComposite, IJavaHelpContextIds.JAVADOC_SPECIFICS_PAGE);

	} //end method createControl

	private void createExtraOptionsGroup(Composite composite) {
		Composite c= new Composite(composite, SWT.NONE);
		c.setLayout(createGridLayout(3));
		c.setLayoutData(createGridData(GridData.FILL_HORIZONTAL, 3, 0));
		((GridLayout) c.getLayout()).marginWidth= 0;

		// AspectJ Extension - commenting out unused code
/*		fOverViewButton= createButton(c, SWT.CHECK, JavadocExportMessages.JavadocSpecificsWizardPage_overviewbutton_label, createGridData(1)); 
		fOverViewText= createText(c, SWT.SINGLE | SWT.BORDER, null, createGridData(GridData.FILL_HORIZONTAL, 1, 0));
		there really aught to be a way to specify this
		((GridData) fOverViewText.getLayoutData()).widthHint= 200;
		fOverViewBrowseButton= createButton(c, SWT.PUSH, JavadocExportMessages.JavadocSpecificsWizardPage_overviewbrowse_label, createGridData(GridData.HORIZONTAL_ALIGN_END, 1, 0)); 
		SWTUtil.setButtonDimensionHint(fOverViewBrowseButton);

		String str= fStore.getOverview();
		if (str.length() == 0) {
			//default
			fOverViewText.setEnabled(false);
			fOverViewBrowseButton.setEnabled(false);
		} else {
			fOverViewButton.setSelection(true);
			fOverViewText.setText(str);
		}
*/
		// AspectJ Extension - changing message string to use AJDT one
		createLabel(composite, SWT.NONE, UIMessages.ajdocSpecificsWizardPage_vmoptionsfield_label, createGridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 3, 0)); 
		fVMOptionsText= createText(composite, SWT.SINGLE | SWT.BORDER, null, createGridData(GridData.HORIZONTAL_ALIGN_FILL, 3, 0));
		fVMOptionsText.setText(fStore.getVMParams());
		
		
		createLabel(composite, SWT.NONE, JavadocExportMessages.JavadocSpecificsWizardPage_extraoptionsfield_label, createGridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 3, 0)); 
		fExtraOptionsText= createText(composite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL, null, createGridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL, 3, 0));
		//fExtraOptionsText.setSize(convertWidthInCharsToPixels(60), convertHeightInCharsToPixels(10));

		fExtraOptionsText.setText(fStore.getAdditionalParams());
		
		Composite inner= new Composite(composite, SWT.NONE);
		inner.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false, 3, 1));
		GridLayout layout= new GridLayout(2, false);
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		inner.setLayout(layout);
		
		createLabel(inner, SWT.NONE, JavadocExportMessages.JavadocSpecificsWizardPage_sourcecompatibility_label, createGridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 1, 0)); 

		fSourceCombo= createCombo(inner, SWT.NONE, fStore.getSource(), createGridData(1));
		String[] versions= { "-", "1.3", "1.4", "1.5" };//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
		fSourceCombo.setItems(versions);   
		fSourceCombo.setText(fStore.getSource());
		
		
		// AspectJ Extension - commenting out unused code
/*		//Listeners
		fOverViewButton.addSelectionListener(new ToggleSelectionAdapter(new Control[] { fOverViewBrowseButton, fOverViewText }) {
			public void validate() {
				doValidation(OVERVIEWSTATUS);
			}
		});

		fOverViewText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				doValidation(OVERVIEWSTATUS);
			}
		});

		fOverViewBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleFileBrowseButtonPressed(fOverViewText, new String[] { "*.html" }, JavadocExportMessages.JavadocSpecificsWizardPage_overviewbrowsedialog_title);  //$NON-NLS-1$
			}
		});
*/
	}

	private void createAntGroup(Composite composite) {
		Composite c= new Composite(composite, SWT.NONE);
		c.setLayout(createGridLayout(3));
		c.setLayoutData(createGridData(GridData.FILL_HORIZONTAL, 3, 0));
		((GridLayout) c.getLayout()).marginWidth= 0;

//		AspectJ Extension - commenting out unused code
/*		fAntButton= createButton(c, SWT.CHECK, JavadocExportMessages.JavadocSpecificsWizardPage_antscriptbutton_label, createGridData(3)); 
		createLabel(c, SWT.NONE, JavadocExportMessages.JavadocSpecificsWizardPage_antscripttext_label, createGridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 1, 0)); 
		fAntText= createText(c, SWT.SINGLE | SWT.BORDER, null, createGridData(GridData.FILL_HORIZONTAL, 1, 0));
		//there really aught to be a way to specify this
		 ((GridData) fAntText.getLayoutData()).widthHint= 200;

		fAntText.setText(fStore.getAntpath());

		fAntBrowseButton= createButton(c, SWT.PUSH, JavadocExportMessages.JavadocSpecificsWizardPage_antscriptbrowse_label, createGridData(GridData.HORIZONTAL_ALIGN_END, 1, 0)); 
		SWTUtil.setButtonDimensionHint(fAntBrowseButton);
		fAntText.setEnabled(false);
		fAntBrowseButton.setEnabled(false);
*/		
		fCheckbrowser= createButton(c, SWT.CHECK, JavadocExportMessages.JavadocSpecificsWizardPage_openbrowserbutton_label, createGridData(3)); 
		fCheckbrowser.setSelection(fStore.doOpenInBrowser());

// 	    AspectJ Extension - commenting out unused code
/*		fAntButton.addSelectionListener(new ToggleSelectionAdapter(new Control[] { fAntText, fAntBrowseButton }) {
			public void validate() {
				doValidation(ANTSTATUS);
			}
		});

		fAntText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				doValidation(ANTSTATUS);
			}
		});

		fAntBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String temp= fAntText.getText();
				IPath path= Path.fromOSString(temp);
				String file= path.lastSegment();
				if (file == null)
					file= "javadoc.xml";//$NON-NLS-1$
				path= path.removeLastSegments(1);

				String selected= handleFolderBrowseButtonPressed(path.toOSString(), JavadocExportMessages.JavadocSpecificsWizardPage_antscriptbrowsedialog_title, JavadocExportMessages.JavadocSpecificsWizardPage_antscriptbrowsedialog_label); 

				path= Path.fromOSString(selected).append(file);
				fAntText.setText(path.toOSString());

			}
		});
*/	} //end method createExtraOptionsGroup

//  AspectJ Extension - commenting out unused code
/*	private void doValidation(int val) {
		switch (val) {

			case OVERVIEWSTATUS :
				fOverviewStatus= new StatusInfo();
				if (fOverViewButton.getSelection()) {
					String filename= fOverViewText.getText();
					if (filename.length() == 0) {
						fOverviewStatus.setError(JavadocExportMessages.JavadocSpecificsWizardPage_overviewnotfound_error); 
					} else {
						File file= new File(filename);
						String ext= filename.substring(filename.lastIndexOf('.') + 1);
						if (!file.isFile()) {
							fOverviewStatus.setError(JavadocExportMessages.JavadocSpecificsWizardPage_overviewnotfound_error); 
						} else if (!ext.equalsIgnoreCase("html")) { //$NON-NLS-1$
							fOverviewStatus.setError(JavadocExportMessages.JavadocSpecificsWizardPage_overviewincorrect_error); 
						}
					}
				}
				break;
			case ANTSTATUS :
				fAntStatus= new StatusInfo();
				if (fAntButton.getSelection()) {
					String filename= fAntText.getText();
					if (filename.length() == 0) {
						fOverviewStatus.setError(JavadocExportMessages.JavadocSpecificsWizardPage_antfileincorrect_error); 
					} else {
						File file= new File(filename);
						String ext= filename.substring(filename.lastIndexOf('.') + 1);
						if (file.isDirectory() || !(ext.equalsIgnoreCase("xml"))) //$NON-NLS-1$
							fAntStatus.setError(JavadocExportMessages.JavadocSpecificsWizardPage_antfileincorrect_error); 
						else if (file.exists())
							fAntStatus.setWarning(JavadocExportMessages.JavadocSpecificsWizardPage_antfileoverwrite_warning); 
					}
				}
				break;
		}

		updateStatus(findMostSevereStatus());

	} */

	/*
	 * @see JavadocWizardPage#onFinish()
	 */

	protected void updateStore() {

		fStore.setVMParams(fVMOptionsText.getText());
		fStore.setAdditionalParams(fExtraOptionsText.getText());

//      AspectJ Extension - commenting out unused code
/*		if (fOverViewText.getEnabled())
			fStore.setOverview(fOverViewText.getText());
		else
			fStore.setOverview(""); //$NON-NLS-1$

		//for now if there are multiple then the ant file is not stored for specific projects	
		if (fAntText.getEnabled()) {
			fStore.setGeneralAntpath(fAntText.getText());
		}
*/		fStore.setOpenInBrowser(fCheckbrowser.getSelection());
		fStore.setSource(fSourceCombo.getText());

	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
//		AspectJ Extension - commenting out unused code
/*		if (visible) {
			doValidation(OVERVIEWSTATUS);
			doValidation(ANTSTATUS);
			fCheckbrowser.setVisible(!fFirstPage.getCustom());
		}
*/	}

	public void init() {
		updateStatus(new StatusInfo());
	}

//  AspectJ Extension - commenting out unused code
/*	private IStatus findMostSevereStatus() {
		return StatusUtil.getMostSevere(new IStatus[] { fAntStatus, fOverviewStatus });
	}

	public boolean generateAnt() {
		return fAntButton.getSelection();
	}
*/
	
}
