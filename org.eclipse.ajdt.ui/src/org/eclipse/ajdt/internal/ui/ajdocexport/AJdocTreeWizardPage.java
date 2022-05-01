/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Luzius Meisser  - adjusted for ajdoc
 *     Helen Hawkins   - updated to Eclipse 3.1
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.ajdocexport;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jdt.internal.ui.javadocexport.JavadocExportMessages;
import org.eclipse.jdt.internal.ui.util.SWTUtil;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

/**
 * Copied from org.eclipse.jdt.internal.ui.javadocexport.JavadocTreeWizardPage
 * Updated for Eclipse 3.1 - bug 109484
 * Changes marked with // AspectJ Extension
 */
public class AJdocTreeWizardPage extends AJdocWizardPage {

	// AspectJ Extension - use a CheckboxTreeViewer instead (bug 111063)
	private CheckboxTreeViewer fInputGroup;

	private Text fDestinationText;
	// AspectJ Extension - renaming fJavadocCommandText to be fAJdocCommandText
	private Combo fAJdocCommandText;
	// AspectJ Extension begin - commenting out unused code
	//private Text fDocletText;
	//private Text fDocletTypeText;
	private Button fStandardButton;
	private Button fDestinationBrowserButton;
	//private Button fCustomButton;
	private Button fPrivateVisibility;
	private Button fProtectedVisibility;
	private Button fPackageVisibility;
	private Button fPublicVisibility;
	//private Label fDocletLabel;
	//private Label fDocletTypeLabel;
	// AspectJ Extension end
	private Label fDestinationLabel;
	private CLabel fDescriptionLabel;

	private String fVisibilitySelection;

	// AspectJ Extension - using AJdocOptionsManager instead
	private final AJdocOptionsManager fStore;

	private StatusInfo fJavadocStatus;
	private StatusInfo fDestinationStatus;
	private StatusInfo fDocletStatus;
	private StatusInfo fTreeStatus;
	private StatusInfo fPreferenceStatus;
	private final StatusInfo fWizardStatus;

	private final int PREFERENCESTATUS= 0;
	private final int CUSTOMSTATUS= 1;
	private final int STANDARDSTATUS= 2;
	private final int TREESTATUS= 3;
	private final int JAVADOCSTATUS= 4;

	/**
	 * Constructor for AJdocTreeWizardPage.
	 * @param pageName
	 */
	protected AJdocTreeWizardPage(String pageName, AJdocOptionsManager store) {
		super(pageName);
		// AspectJ Extension - message
		setDescription(UIMessages.ajdocTreeWizardPage_javadoctreewizardpage_description);

		fStore= store;

		// Status variables
		fJavadocStatus= new StatusInfo();
		fDestinationStatus= new StatusInfo();
		fDocletStatus= new StatusInfo();
		fTreeStatus= new StatusInfo();
		fPreferenceStatus= new StatusInfo();
		fWizardStatus= store.getWizardStatus();
	}

	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		final Composite composite= new Composite(parent, SWT.NONE);
		final GridLayout layout= new GridLayout();
		layout.numColumns= 6;
		composite.setLayout(layout);

		// AspectJ Extension - renaming method to be ajdoc
		createAJdocCommandSet(composite);
		createInputGroup(composite);
		createVisibilitySet(composite);
		createOptionsSet(composite);

		setControl(composite);
		Dialog.applyDialogFont(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IJavaHelpContextIds.JAVADOC_TREE_PAGE);
	}

	// AspectJ Extension - renaming method to be ajdoc
	protected void createAJdocCommandSet(Composite composite) {

		final int numColumns= 2;

		GridLayout layout= createGridLayout(numColumns);
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		Composite group = new Composite(composite, SWT.NONE);
		group.setLayoutData(createGridData(GridData.FILL_HORIZONTAL, 6, 0));
		group.setLayout(layout);

//		 AspectJ Extension - message
		String labelText;
        if (!AJDTUtils.isMacOS()) {
            labelText = UIMessages.ajdocTreeWizardPage_ajdoccommand_label;
        } else {
            labelText = UIMessages.ajdocTreeWizardPage_MAC_ajdoccommand_label;
        }

		createLabel(group, SWT.NONE, labelText, createGridData(GridData.HORIZONTAL_ALIGN_BEGINNING, numColumns, 0));
		fAJdocCommandText= createCombo(group, SWT.NONE, null, createGridData(GridData.FILL_HORIZONTAL, numColumns - 1, 0));

		fAJdocCommandText.addModifyListener(e -> doValidation(JAVADOCSTATUS));

		final Button javadocCommandBrowserButton= createButton(group, SWT.PUSH, JavadocExportMessages.JavadocTreeWizardPage_javadoccommand_button_label, createGridData(GridData.HORIZONTAL_ALIGN_FILL, 1, 0));
		SWTUtil.setButtonDimensionHint(javadocCommandBrowserButton);

		javadocCommandBrowserButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				// AspectJ Extension - updating method name
				browseForAJdocCommand();
			}
		});
	}




	protected void createInputGroup(Composite composite) {
//		 AspectJ Extension - message
		createLabel(composite, SWT.NONE, UIMessages.ajdoc_info_projectselection, createGridData(6));
		Composite c= new Composite(composite, SWT.NONE);

		// AspectJ Extension begin - fill layout
		FillLayout f = new FillLayout();
		c.setLayout(f);
		c.setLayoutData(createGridData(GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL | GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL, 6, 1));
		// AspectJ Extension end

		// AspectJ Extension - using aj equivalents
		ITreeContentProvider treeContentProvider= new AJdocProjectContentProvider();

		// AspectJ Extension Begin -
		fInputGroup= new CheckboxTreeViewer(c, SWT.BORDER);
		fInputGroup.setLabelProvider(new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT));
		fInputGroup.setContentProvider(treeContentProvider);
		fInputGroup.setInput(this);

		fInputGroup.addCheckStateListener(e -> doValidation(TREESTATUS));
		fInputGroup.setComparator(new JavaElementComparator());

		IJavaElement[] elements= fStore.getInitialElements();
		setTreeChecked(elements);
		if (elements.length > 0) {
			fInputGroup.setSelection(new StructuredSelection(elements[0].getJavaProject()));
		}
		c.layout();

//		fInputGroup.aboutToOpen();
		// AspectJ Extension End
	}

	private void createVisibilitySet(Composite composite) {

		GridLayout visibilityLayout= createGridLayout(4);
		visibilityLayout.marginHeight= 0;
		visibilityLayout.marginWidth= 0;
		Composite visibilityGroup= new Composite(composite, SWT.NONE);
		visibilityGroup.setLayoutData(createGridData(GridData.FILL_HORIZONTAL, 6, 0));
		visibilityGroup.setLayout(visibilityLayout);

//		 AspectJ Extension - message
		createLabel(visibilityGroup, SWT.NONE, UIMessages.ajdocTreeWizardPage_visibilitygroup_label, createGridData(GridData.FILL_HORIZONTAL, 4, 0));
		fPrivateVisibility= createButton(visibilityGroup, SWT.RADIO, JavadocExportMessages.JavadocTreeWizardPage_privatebutton_label, createGridData(GridData.FILL_HORIZONTAL, 1, 0));
		fPackageVisibility= createButton(visibilityGroup, SWT.RADIO, JavadocExportMessages.JavadocTreeWizardPage_packagebutton_label, createGridData(GridData.FILL_HORIZONTAL, 1, 0));
		fProtectedVisibility= createButton(visibilityGroup, SWT.RADIO, JavadocExportMessages.JavadocTreeWizardPage_protectedbutton_label, createGridData(GridData.FILL_HORIZONTAL, 1, 0));
		fPublicVisibility= createButton(visibilityGroup, SWT.RADIO, JavadocExportMessages.JavadocTreeWizardPage_publicbutton_label, createGridData(GridData.FILL_HORIZONTAL, 1, 0));

		fDescriptionLabel= new CLabel(visibilityGroup, SWT.LEFT);
		fDescriptionLabel.setLayoutData(createGridData(GridData.FILL_HORIZONTAL, 4, convertWidthInCharsToPixels(3) -  3)); // INDENT of CLabel

		fPrivateVisibility.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (((Button) e.widget).getSelection()) {
					fVisibilitySelection= fStore.PRIVATE;
					// AspectJ Extension - messages
					fDescriptionLabel.setText(UIMessages.ajdocTreeWizardPage_privatevisibilitydescription_label);
				}
			}
		});
		fPackageVisibility.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (((Button) e.widget).getSelection()) {
					fVisibilitySelection= fStore.PACKAGE;
					// AspectJ Extension - messages
					fDescriptionLabel.setText(UIMessages.ajdocTreeWizardPage_packagevisibledescription_label);
				}
			}
		});
		fProtectedVisibility.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (((Button) e.widget).getSelection()) {
					fVisibilitySelection= fStore.PROTECTED;
					// AspectJ Extension - messages
					fDescriptionLabel.setText(UIMessages.ajdocTreeWizardPage_protectedvisibilitydescription_label);
				}
			}
		});

		fPublicVisibility.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (((Button) e.widget).getSelection()) {
					fVisibilitySelection= fStore.PUBLIC;
					// AspectJ Extension - messages
					fDescriptionLabel.setText(UIMessages.ajdocTreeWizardPage_publicvisibilitydescription_label);
				}
			}
		});

		setVisibilitySettings();

	}

	protected void setVisibilitySettings() {
//		 AspectJ Extension - messages
		fVisibilitySelection= fStore.getAccess();
		fPrivateVisibility.setSelection(fVisibilitySelection.equals(fStore.PRIVATE));
		if (fPrivateVisibility.getSelection())
			fDescriptionLabel.setText(UIMessages.ajdocTreeWizardPage_privatevisibilitydescription_label);
		//$NON-NLS-1$
		fProtectedVisibility.setSelection(fVisibilitySelection.equals(fStore.PROTECTED));
		if (fProtectedVisibility.getSelection())
			fDescriptionLabel.setText(UIMessages.ajdocTreeWizardPage_protectedvisibilitydescription_label);
		//$NON-NLS-1$
		fPackageVisibility.setSelection(fVisibilitySelection.equals(fStore.PACKAGE));
		if (fPackageVisibility.getSelection())
			fDescriptionLabel.setText(UIMessages.ajdocTreeWizardPage_packagevisibledescription_label);
		//$NON-NLS-1$
		fPublicVisibility.setSelection(fVisibilitySelection.equals(fStore.PUBLIC));
		if (fPublicVisibility.getSelection())
			fDescriptionLabel.setText(UIMessages.ajdocTreeWizardPage_publicvisibilitydescription_label);
		//$NON-NLS-1$
	}

	private void createOptionsSet(Composite composite) {

		final int numColumns= 4;

		final GridLayout layout= createGridLayout(numColumns);
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		Composite group= new Composite(composite, SWT.NONE);
		group.setLayoutData(createGridData(GridData.FILL_HORIZONTAL, 6, 0));
		group.setLayout(layout);

		fStandardButton= createButton(group, SWT.RADIO, JavadocExportMessages.JavadocTreeWizardPage_standarddocletbutton_label, createGridData(GridData.HORIZONTAL_ALIGN_FILL, numColumns, 0));

		fDestinationLabel= createLabel(group, SWT.NONE, JavadocExportMessages.JavadocTreeWizardPage_destinationfield_label, createGridData(GridData.HORIZONTAL_ALIGN_FILL, 1, convertWidthInCharsToPixels(3)));
		fDestinationText= createText(group, SWT.SINGLE | SWT.BORDER, null, createGridData(GridData.FILL_HORIZONTAL, numColumns - 2, 0));
		((GridData) fDestinationText.getLayoutData()).widthHint= 0;
		fDestinationText.addModifyListener(e -> doValidation(STANDARDSTATUS));

		fDestinationBrowserButton= createButton(group, SWT.PUSH, JavadocExportMessages.JavadocTreeWizardPage_destinationbrowse_label, createGridData(GridData.HORIZONTAL_ALIGN_END, 1, 0));
		SWTUtil.setButtonDimensionHint(fDestinationBrowserButton);

//      AspectJ Extension - commenting out unused code
/*		//Option to use custom doclet
		fCustomButton= createButton(group, SWT.RADIO, JavadocExportMessages.JavadocTreeWizardPage_customdocletbutton_label, createGridData(GridData.HORIZONTAL_ALIGN_FILL, numColumns, 0));

		//For Entering location of custom doclet
		fDocletTypeLabel= createLabel(group, SWT.NONE, JavadocExportMessages.JavadocTreeWizardPage_docletnamefield_label, createGridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 1, convertWidthInCharsToPixels(3)));
		fDocletTypeText= createText(group, SWT.SINGLE | SWT.BORDER, null, createGridData(GridData.HORIZONTAL_ALIGN_FILL, numColumns - 1, 0));
		((GridData) fDocletTypeText.getLayoutData()).widthHint= 0;


		fDocletTypeText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				doValidation(CUSTOMSTATUS);
			}
		});

		fDocletLabel= createLabel(group, SWT.NONE, JavadocExportMessages.JavadocTreeWizardPage_docletpathfield_label, createGridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 1, convertWidthInCharsToPixels(3)));
		fDocletText= createText(group, SWT.SINGLE | SWT.BORDER, null, createGridData(GridData.HORIZONTAL_ALIGN_FILL, numColumns - 1, 0));
		((GridData) fDocletText.getLayoutData()).widthHint= 0;

		fDocletText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				doValidation(CUSTOMSTATUS);
			}

		});

		//Add Listeners
		fCustomButton.addSelectionListener(new EnableSelectionAdapter(new Control[] { fDocletLabel, fDocletText, fDocletTypeLabel, fDocletTypeText }, new Control[] { fDestinationLabel, fDestinationText, fDestinationBrowserButton }));
		fCustomButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doValidation(CUSTOMSTATUS);
			}
		});
		fStandardButton.addSelectionListener(new EnableSelectionAdapter(new Control[] { fDestinationLabel, fDestinationText, fDestinationBrowserButton }, new Control[] { fDocletLabel, fDocletText, fDocletTypeLabel, fDocletTypeText }));
		fStandardButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doValidation(STANDARDSTATUS);
			}
		});
*/		fDestinationBrowserButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String text= handleFolderBrowseButtonPressed(fDestinationText.getText(), JavadocExportMessages.JavadocTreeWizardPage_destinationbrowsedialog_title,
				   		JavadocExportMessages.JavadocTreeWizardPage_destinationbrowsedialog_label);
				fDestinationText.setText(text);
			}
		});

		setOptionSetSettings();
	}

	// AspectJ Extension - commenting out unused code
/*	public boolean getCustom() {
		return fCustomButton.getSelection();
	}
*/
	private void setOptionSetSettings() {

		if (!fStore.isFromStandard()) {
		    // AspectJ Extension - commenting out unused code
/*			fCustomButton.setSelection(true);
			fDocletText.setText(fStore.getDocletPath());
			fDocletTypeText.setText(fStore.getDocletName());
*/			fDestinationText.setText(fStore.getDestination());
			fDestinationText.setEnabled(false);
			fDestinationBrowserButton.setEnabled(false);
			fDestinationLabel.setEnabled(false);

		} else {
			fStandardButton.setSelection(true);
			fDestinationText.setText(fStore.getDestination());
		    // AspectJ Extension - commenting out unused code
/*			fDocletText.setText(fStore.getDocletPath());
			fDocletTypeText.setText(fStore.getDocletName());
			fDocletText.setEnabled(false);
			fDocletLabel.setEnabled(false);
			fDocletTypeText.setEnabled(false);
			fDocletTypeLabel.setEnabled(false);
*/		}

		// AspectJ Extension - using fAJdocCommandText instead
		fAJdocCommandText.setItems(fStore.getJavadocCommandHistory());
		fAJdocCommandText.select(0);
	}

	/**
	 * Receives of list of elements selected by the user and passes them
	 * to the CheckedTree. List can contain multiple projects and elements from
	 * different projects. If the list of seletected elements is empty a default
	 * project is selected.
	 */
	private void setTreeChecked(IJavaElement[] sourceElements) {
    for (IJavaElement curr : sourceElements) {
      // AspectJ Extension Begin - change due to use of CheckboxTreeViewer
      if (curr instanceof IJavaProject) {
        fInputGroup.setChecked(curr, true);
      }
      // AspectJ Extension End
    }
	}

	private IPath[] getSourcePath(IJavaProject[] projects) {
		HashSet<IPath> sourcePath = new HashSet<>();
		//loops through all projects and gets a list if of thier sourpaths
		for (IJavaProject iJavaProject : projects) {
			try {
				IPackageFragmentRoot[] roots = iJavaProject.getPackageFragmentRoots();
				for (IPackageFragmentRoot curr : roots) {
					if (curr.getKind() == IPackageFragmentRoot.K_SOURCE) {
						IResource resource = curr.getResource();
						if (resource != null) {
							IPath p = resource.getLocation();
							if (p != null)
								sourcePath.add(p);
						}
					}
				}
			}
			catch (JavaModelException e) {
				JavaPlugin.log(e);
			}
		}
		return sourcePath.toArray(new IPath[0]);
	}

	private IPath[] getClassPath(IJavaProject[] javaProjects) {
		HashSet<IPath> classPath = new HashSet<>();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		for (IJavaProject curr : javaProjects) {
			try {
				IPath outputLocation = null;
				IResource outputPathFolder = root.findMember(curr.getOutputLocation());
				if (outputPathFolder != null)
					outputLocation = outputPathFolder.getLocation();

				String[] defaultRuntimeClassPath = JavaRuntime.computeDefaultRuntimeClassPath(curr);
				for (String s : defaultRuntimeClassPath) {
					IPath path = Path.fromOSString(s);
					if (!path.equals(outputLocation))
						classPath.add(path);
				}
			}
			catch (CoreException e) {
				JavaPlugin.log(e);
			}
		}
		return classPath.toArray(new IPath[0]);
	}


	/**
	 * Gets a list of elements to generated javadoc for from each project.
	 * Javadoc can be generated for either a IPackageFragmentRoot or a ICompilationUnit.
	 */
//	 AspectJ Extension - commenting out unused code
/*	private IJavaElement[] getSourceElements(IJavaProject[] projects) {
		ArrayList res= new ArrayList();
		try {
			Set allChecked= fInputGroup.getAllCheckedTreeItems();

			Set incompletePackages= new HashSet();
			for (int h= 0; h < projects.length; h++) {
				IJavaProject iJavaProject= projects[h];

				IPackageFragmentRoot[] roots= iJavaProject.getPackageFragmentRoots();
				for (int i= 0; i < roots.length; i++) {
					IPackageFragmentRoot root= roots[i];
					if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
						IJavaElement[] packs= root.getChildren();
						for (int k= 0; k < packs.length; k++) {
							IJavaElement curr= packs[k];
							if (curr.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
								// default packages are always incomplete
								if (curr.getElementName().length() == 0 || !allChecked.contains(curr) || fInputGroup.isTreeItemGreyChecked(curr)) {
									incompletePackages.add(curr.getElementName());
								}
							}
						}
					}
				}
			}

			Iterator checkedElements= fInputGroup.getAllCheckedListItems();
			while (checkedElements.hasNext()) {
				Object element= checkedElements.next();
				if (element instanceof ICompilationUnit) {
					ICompilationUnit unit= (ICompilationUnit) element;
					if (incompletePackages.contains(unit.getParent().getElementName())) {
						res.add(unit);
					}
				}
			}

			Set addedPackages= new HashSet();

			checkedElements= allChecked.iterator();
			while (checkedElements.hasNext()) {
				Object element= checkedElements.next();
				if (element instanceof IPackageFragment) {
					IPackageFragment fragment= (IPackageFragment) element;
					String name= fragment.getElementName();
					if (!incompletePackages.contains(name) && !addedPackages.contains(name)) {
						res.add(fragment);
						addedPackages.add(name);
					}
				}
			}

		} catch (JavaModelException e) {
			JavaPlugin.log(e);
		}
		return (IJavaElement[]) res.toArray(new IJavaElement[res.size()]);
	}
*/
	protected void updateStore(IJavaProject[] checkedProjects) {
	    // AspectJ Extension - commenting out unused code
/*		if (fCustomButton.getSelection()) {
			fStore.setDocletName(fDocletTypeText.getText());
			fStore.setDocletPath(fDocletText.getText());
			fStore.setFromStandard(false);
		}
*/		if (fStandardButton.getSelection()) {
			fStore.setFromStandard(true);
			//the destination used in javadoc generation
			fStore.setDestination(fDestinationText.getText());
		}

		fStore.setSourcepath(getSourcePath(checkedProjects));
		fStore.setClasspath(getClassPath(checkedProjects));
		fStore.setAccess(fVisibilitySelection);
		// AspectJ Extension - setting the selected elements to be the checked
		// projects because that's all we're interested in - the build
		// configs will sort out what needs to be included.
		fStore.setSelectedElements(checkedProjects);

		ArrayList<String> commands = new ArrayList<>();
		// AspectJ Extension - adding ajdoc command instead of javadoc
		commands.add(fAJdocCommandText.getText()); // must be first
		String[] items = fAJdocCommandText.getItems();
		for (String curr : items) {
			if (!commands.contains(curr))
				commands.add(curr);
		}
		fStore.setJavadocCommandHistory(commands.toArray(new String[0]));
	}

	public IJavaProject[] getCheckedProjects() {
		ArrayList<IJavaProject> checkedProjects = new ArrayList<>();
		TreeItem[] treeItems = fInputGroup.getTree().getItems();
		for (TreeItem treeItem : treeItems) {
			if (treeItem.getChecked()) {
				Object curr = treeItem.getData();
				if (curr instanceof IJavaProject)
					checkedProjects.add((IJavaProject) curr);
			}
		}
		return checkedProjects.toArray(new IJavaProject[0]);
	}

	protected void doValidation(int validate) {


		switch (validate) {
			case PREFERENCESTATUS :
				fPreferenceStatus= new StatusInfo();
				fDocletStatus= new StatusInfo();
				updateStatus(findMostSevereStatus());
				break;
			case CUSTOMSTATUS :
				// AspectJ Extension - commenting out unused code
/*				if (fCustomButton.getSelection()) {
					fDestinationStatus= new StatusInfo();
					fDocletStatus= new StatusInfo();
					String doclet= fDocletTypeText.getText();
					String docletPath= fDocletText.getText();
					if (doclet.length() == 0) {
						fDocletStatus.setError(JavadocExportMessages.JavadocTreeWizardPage_nodocletname_error);

					} else if (JavaConventions.validateJavaTypeName(doclet).matches(IStatus.ERROR)) {
						fDocletStatus.setError(JavadocExportMessages.JavadocTreeWizardPage_invaliddocletname_error);
					} else if ((docletPath.length() == 0) || !validDocletPath(docletPath)) {
						fDocletStatus.setError(JavadocExportMessages.JavadocTreeWizardPage_invaliddocletpath_error);
					}
					updateStatus(findMostSevereStatus());
				}
*/				break;

			case STANDARDSTATUS :
				if (fStandardButton.getSelection()) {
					fDestinationStatus= new StatusInfo();
					fDocletStatus= new StatusInfo();
					String dest= fDestinationText.getText();
					if (dest.length() == 0) {
						fDestinationStatus.setError(JavadocExportMessages.JavadocTreeWizardPage_nodestination_error);
					}
					File file= new File(dest);
					if (!Path.ROOT.isValidPath(dest) || file.isFile()) {
						fDestinationStatus.setError(JavadocExportMessages.JavadocTreeWizardPage_invaliddestination_error);
					}
					// AspectJ Extension - message
					if (new File(dest, "package-list").exists() || new File(dest, "index.html").exists()) //$NON-NLS-1$//$NON-NLS-2$
						fDestinationStatus.setWarning(UIMessages.ajdocTreeWizardPage_warning_mayoverwritefiles);
					updateStatus(findMostSevereStatus());
				}
				break;

			case TREESTATUS :

				fTreeStatus= new StatusInfo();
				// AspectJ Extension - there is just a list of projects, thefore we need
				// to check if there are any checked projects rather than getting all the
				// checked items.
				//if (!fInputGroup.getAllCheckedListItems().hasNext())
				if (fInputGroup.getCheckedElements().length == 0)
					// AspectJ Extension - updated message
					fTreeStatus.setError(UIMessages.ajdoc_error_noProjectSelected);
				updateStatus(findMostSevereStatus());

				break;

			case JAVADOCSTATUS:
				fJavadocStatus= new StatusInfo();
				// AspectJ Extension - using fAJdocCommandText instead
				String text= fAJdocCommandText.getText();
				// AspectJ Extension - messages
				if (text.length() == 0) {
			        String errorText;
			        if (!AJDTUtils.isMacOS()) {
			            errorText = UIMessages.ajdocTreeWizardPage_ajdoccmd_error_enterpath;
			        } else {
			            errorText = UIMessages.ajdocTreeWizardPage_MAC_ajdoccmd_error_enterpath;
			        }

					fJavadocStatus.setError(errorText);
				} else {
					File file= new File(text);
					if (!file.isFile()) {
	                    String errorText;
	                    if (!AJDTUtils.isMacOS()) {
	                        errorText = UIMessages.ajdocTreeWizardPage_ajdoccmd_error_notexists;
	                    } else {
	                        errorText = UIMessages.ajdocTreeWizardPage_MAC_ajdoccmd_error_notexists;
	                    }
						fJavadocStatus.setError(errorText);
					}
				}
				updateStatus(findMostSevereStatus());
				break;
		} //end switch


	}

	// AspectJ Extension - changed name to "browseForAJDocCommand"
	protected void browseForAJdocCommand() {
		FileDialog dialog = new FileDialog(getShell());
//		 AspectJ Extension - message
		String dialogText;
		if (!AJDTUtils.isMacOS())
			dialogText = UIMessages.AJdocTreeWizardPage_ajdoccmd_dialog_title;
		else
			dialogText = UIMessages.AJdocTreeWizardPage_MAC_ajdoccmd_dialog_title;
		dialog.setText(dialogText);
		String dirName = fAJdocCommandText.getText();
		dialog.setFileName(dirName);
		String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			ArrayList<String> newItems = new ArrayList<>();
			String[] items = fAJdocCommandText.getItems();
			newItems.add(selectedDirectory);
			for (int i = 0; i < items.length && newItems.size() < 5; i++) { // only keep the last 5 entries
				String curr = items[i];
				if (!newItems.contains(curr))
					newItems.add(curr);
			}
			fAJdocCommandText.setItems(newItems.toArray(new String[0]));
			fAJdocCommandText.select(0);
		}
	}

	// AspectJ Extension - commenting out unused code
/*	private boolean validDocletPath(String docletPath) {
		StringTokenizer tokens= new StringTokenizer(docletPath, ";"); //$NON-NLS-1$
		while (tokens.hasMoreTokens()) {
			File file= new File(tokens.nextToken());
			if (!file.exists())
				return false;
		}
		return true;
	}
*/
	/**
	 * Finds the most severe error (if there is one)
	 */
	private IStatus findMostSevereStatus() {
		return StatusUtil.getMostSevere(new IStatus[] { fJavadocStatus, fPreferenceStatus, fDestinationStatus, fDocletStatus, fTreeStatus, fWizardStatus });
	}

	public void init() {
		updateStatus(new StatusInfo());
	}

	public void setVisible(boolean visible) {
		if (visible) {
			doValidation(STANDARDSTATUS);
			doValidation(CUSTOMSTATUS);
			doValidation(TREESTATUS);
			doValidation(PREFERENCESTATUS);
			doValidation(JAVADOCSTATUS);
		}
		super.setVisible(visible);
	}

}
