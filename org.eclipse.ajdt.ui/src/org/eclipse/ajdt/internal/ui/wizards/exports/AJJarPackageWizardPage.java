/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.internal.ui.wizards.exports;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.filters.EmptyInnerPackageFilter;
import org.eclipse.jdt.internal.ui.util.SWTUtil;
import org.eclipse.jdt.internal.ui.viewsupport.LibraryFilter;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.dialogs.WizardExportResourcesPage;

/**
 * Copied from org.eclipse.jdt.internal.ui.jarpackager.JarPackageWizardPage to
 * enable AspectJ resourcesd to be exported correctly.
 * Changes marked with // AspectJ Change
 */
public class AJJarPackageWizardPage extends WizardExportResourcesPage implements IJarPackageWizardPage {
	private final AJJarPackageData fJarPackage;
	private final IStructuredSelection fInitialSelection;
	private CheckboxTreeAndListGroup fInputGroup;

	// widgets
	private Button	fExportClassFilesCheckbox;
	private Button	fExportOutputFoldersCheckbox;
	private Button	fExportJavaFilesCheckbox;

	private Combo	fDestinationNamesCombo;
	private Button	fDestinationBrowseButton;

	private Button		fCompressCheckbox;
	private Button		fOverwriteCheckbox;
	private Button		fIncludeDirectoryEntriesCheckbox;
	private Text		fDescriptionFileText;

	// dialog store id constants
	private static final String PAGE_NAME= "JarPackageWizardPage"; //$NON-NLS-1$

	private static final String STORE_EXPORT_CLASS_FILES= PAGE_NAME + ".EXPORT_CLASS_FILES"; //$NON-NLS-1$
	private static final String STORE_EXPORT_OUTPUT_FOLDERS= PAGE_NAME + ".EXPORT_OUTPUT_FOLDER"; //$NON-NLS-1$
	private static final String STORE_EXPORT_JAVA_FILES= PAGE_NAME + ".EXPORT_JAVA_FILES"; //$NON-NLS-1$

	private static final String STORE_DESTINATION_NAMES= PAGE_NAME + ".DESTINATION_NAMES_ID"; //$NON-NLS-1$

	private static final String STORE_COMPRESS= PAGE_NAME + ".COMPRESS"; //$NON-NLS-1$
	private final static String STORE_OVERWRITE= PAGE_NAME + ".OVERWRITE"; //$NON-NLS-1$
	private final static String STORE_INCLUDE_DIRECTORY_ENTRIES= PAGE_NAME + ".INCLUDE_DIRECTORY_ENTRIES"; //$NON-NLS-1$

	// other constants
	private static final int SIZING_SELECTION_WIDGET_WIDTH= 480;
	private static final int SIZING_SELECTION_WIDGET_HEIGHT= 150;

	/**
	 *	Create an instance of this class
	 */
	public AJJarPackageWizardPage(AJJarPackageData jarPackage, IStructuredSelection selection) {
		super(PAGE_NAME, selection);
		setTitle(JarPackagerMessages.JarPackageWizardPage_title);
		setDescription(JarPackagerMessages.JarPackageWizardPage_description);
		fJarPackage= jarPackage;
		fInitialSelection= selection;
	}

	/*
	 * Method declared on IDialogPage.
	 */
	public void createControl(final Composite parent) {

		initializeDialogUnits(parent);

		Composite composite= new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(
			new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		createPlainLabel(composite, JarPackagerMessages.JarPackageWizardPage_whatToExport_label);
		createInputGroup(composite);

		createExportTypeGroup(composite);

		new Label(composite, SWT.NONE); // vertical spacer


		createPlainLabel(composite, JarPackagerMessages.JarPackageWizardPage_whereToExport_label);
		createDestinationGroup(composite);

		createPlainLabel(composite, JarPackagerMessages.JarPackageWizardPage_options_label);
		createOptionsGroup(composite);

		restoreResourceSpecificationWidgetValues(); // superclass API defines this hook
		restoreWidgetValues();
		if (fInitialSelection != null)
			BusyIndicator.showWhile(parent.getDisplay(), this::setupBasedOnInitialSelections);

		setControl(composite);
		update();
		giveFocusToDestination();

		Dialog.applyDialogFont(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IJavaHelpContextIds.JARPACKAGER_WIZARD_PAGE);
	}

	/**
	 *	Create the export options specification widgets.
	 *
	 *	@param parent org.eclipse.swt.widgets.Composite
	 */
	protected void createOptionsGroup(Composite parent) {
		Composite optionsGroup= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		optionsGroup.setLayout(layout);

		fCompressCheckbox= new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		fCompressCheckbox.setText(JarPackagerMessages.JarPackageWizardPage_compress_text);
		fCompressCheckbox.addListener(SWT.Selection, this);

		fIncludeDirectoryEntriesCheckbox= new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		fIncludeDirectoryEntriesCheckbox.setText(JarPackagerMessages.JarPackageWizardPage_includeDirectoryEntries_text);
		fIncludeDirectoryEntriesCheckbox.addListener(SWT.Selection, this);

		fOverwriteCheckbox= new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		fOverwriteCheckbox.setText(JarPackagerMessages.JarPackageWizardPage_overwrite_text);
		fOverwriteCheckbox.addListener(SWT.Selection, this);
	}

	/**
	 *	Answer the contents of the destination specification widget. If this
	 *	value does not have the required suffix then add it first.
	 *
	 *	@return java.lang.String
	 */
	protected String getDestinationValue() {
		String destinationText= fDestinationNamesCombo.getText().trim();
		if (destinationText.indexOf('.') < 0)
			destinationText += getOutputSuffix();
		return destinationText;
	}

	/**
	 *	Answer the string to display in self as the destination type
	 *
	 *	@return java.lang.String
	 */
	protected String getDestinationLabel() {
		return JarPackagerMessages.JarPackageWizardPage_destination_label;
	}

	/**
	 *	Answer the suffix that files exported from this wizard must have.
	 *	If this suffix is a file extension (which is typically the case)
	 *	then it must include the leading period character.
	 *
	 *	@return java.lang.String
	 */
	protected String getOutputSuffix() {
		return "." + AJJarPackagerUtil.JAR_EXTENSION; //$NON-NLS-1$
	}

	/**
	 * Returns an iterator over this page's collection of currently-specified
	 * elements to be exported. This is the primary element selection facility
	 * accessor for subclasses.
	 *
	 * @return an iterator over the collection of elements currently selected for export
	 */
	protected Iterator getSelectedResourcesIterator() {
		return fInputGroup.getAllCheckedListItems();
	}

	/**
	 * Persists resource specification control setting that are to be restored
	 * in the next instance of this page. Subclasses wishing to persist
	 * settings for their controls should extend the hook method
	 * <code>internalSaveWidgetValues</code>.
	 */
	public final void saveWidgetValues() {
		// update directory names history
		IDialogSettings settings= getDialogSettings();
		if (settings != null) {
			String[] directoryNames= settings.getArray(STORE_DESTINATION_NAMES);
			if (directoryNames == null)
				directoryNames= new String[0];
			directoryNames= addToHistory(directoryNames, getDestinationValue());
			settings.put(STORE_DESTINATION_NAMES, directoryNames);

			settings.put(STORE_EXPORT_CLASS_FILES, fJarPackage.areClassFilesExported());
			settings.put(STORE_EXPORT_OUTPUT_FOLDERS, fJarPackage.areOutputFoldersExported());
			settings.put(STORE_EXPORT_JAVA_FILES, fJarPackage.areJavaFilesExported());

			// options
			settings.put(STORE_COMPRESS, fJarPackage.isCompressed());
			settings.put(STORE_INCLUDE_DIRECTORY_ENTRIES, fJarPackage.areDirectoryEntriesIncluded());
			settings.put(STORE_OVERWRITE, fJarPackage.allowOverwrite());
		}
		// Allow subclasses to save values
		internalSaveWidgetValues();
	}

	/**
	 * Hook method for subclasses to persist their settings.
	 */
	protected void internalSaveWidgetValues() {
	}

	/**
	 *	Hook method for restoring widget values to the values that they held
	 *	last time this wizard was used to completion.
	 */
	protected void restoreWidgetValues() {
		if (!((AJJarPackageWizard)getWizard()).isInitializingFromJarPackage())
			initializeJarPackage();

		fExportClassFilesCheckbox.setSelection(fJarPackage.areClassFilesExported());
		fExportOutputFoldersCheckbox.setSelection(fJarPackage.areOutputFoldersExported());
		fExportJavaFilesCheckbox.setSelection(fJarPackage.areJavaFilesExported());

		// destination
		if (fJarPackage.getJarLocation().isEmpty())
			fDestinationNamesCombo.setText(""); //$NON-NLS-1$
		else
			fDestinationNamesCombo.setText(fJarPackage.getJarLocation().toOSString());
		IDialogSettings settings= getDialogSettings();
		if (settings != null) {
			String[] directoryNames= settings.getArray(STORE_DESTINATION_NAMES);
			if (directoryNames == null)
				return; // ie.- no settings stored
			if (! fDestinationNamesCombo.getText().equals(directoryNames[0]))
				fDestinationNamesCombo.add(fDestinationNamesCombo.getText());
      for (String directoryName : directoryNames)
        fDestinationNamesCombo.add(directoryName);
		}

		// options
		fCompressCheckbox.setSelection(fJarPackage.isCompressed());
		fIncludeDirectoryEntriesCheckbox.setSelection(fJarPackage.areDirectoryEntriesIncluded());
		fOverwriteCheckbox.setSelection(fJarPackage.allowOverwrite());
	}

	/**
	 *	Initializes the JAR package from last used wizard page values.
	 */
	protected void initializeJarPackage() {
		IDialogSettings settings= getDialogSettings();
		if (settings != null) {
			// source
			fJarPackage.setElements(getSelectedElements());
			fJarPackage.setExportClassFiles(settings.getBoolean(STORE_EXPORT_CLASS_FILES));
			fJarPackage.setExportOutputFolders(settings.getBoolean(STORE_EXPORT_OUTPUT_FOLDERS));
			fJarPackage.setExportJavaFiles(settings.getBoolean(STORE_EXPORT_JAVA_FILES));

			// options
			fJarPackage.setCompress(settings.getBoolean(STORE_COMPRESS));
			fJarPackage.setIncludeDirectoryEntries(settings.getBoolean(STORE_INCLUDE_DIRECTORY_ENTRIES));
			fJarPackage.setOverwrite(settings.getBoolean(STORE_OVERWRITE));

			// destination
			String[] directoryNames= settings.getArray(STORE_DESTINATION_NAMES);
			if (directoryNames == null)
				return; // ie.- no settings stored
			fJarPackage.setJarLocation(Path.fromOSString(directoryNames[0]));
		}
	}

	/**
	 *	Stores the widget values in the JAR package.
	 */
	protected void updateModel() {
		if (getControl() == null)
			return;

		// source
		if (fExportClassFilesCheckbox.getSelection() && !fJarPackage.areClassFilesExported())
			fExportOutputFoldersCheckbox.setSelection(false);
		if (fExportOutputFoldersCheckbox.getSelection() && !fJarPackage.areOutputFoldersExported())
			fExportClassFilesCheckbox.setSelection(false);
		fJarPackage.setExportClassFiles(fExportClassFilesCheckbox.getSelection());
		fJarPackage.setExportOutputFolders(fExportOutputFoldersCheckbox.getSelection());
		fJarPackage.setExportJavaFiles(fExportJavaFilesCheckbox.getSelection());
		fJarPackage.setElements(getSelectedElements());

		// destination
		String comboText= fDestinationNamesCombo.getText();
		IPath path= Path.fromOSString(comboText);

		if (path.segmentCount() > 0 && ensureTargetFileIsValid(path.toFile()) && path.getFileExtension() == null)
			// append .jar
//			AspectJ Change Begin
			path= path.addFileExtension(AJJarPackagerUtil.JAR_EXTENSION);
//			AspectJ Change End

		fJarPackage.setJarLocation(path);

		// options
		fJarPackage.setCompress(fCompressCheckbox.getSelection());
		fJarPackage.setIncludeDirectoryEntries(fIncludeDirectoryEntriesCheckbox.getSelection());
		fJarPackage.setOverwrite(fOverwriteCheckbox.getSelection());
	}

	/**
	 * Returns a boolean indicating whether the passed File handle is
	 * is valid and available for use.
	 *
	 * @return boolean
	 */
	protected boolean ensureTargetFileIsValid(File targetFile) {
		if (targetFile.exists() && targetFile.isDirectory() && fDestinationNamesCombo.getText().length() > 0) {
			setErrorMessage(JarPackagerMessages.JarPackageWizardPage_error_exportDestinationMustNotBeDirectory);
			fDestinationNamesCombo.setFocus();
			return false;
		}
		if (targetFile.exists()) {
			if (!targetFile.canWrite()) {
				setErrorMessage(JarPackagerMessages.JarPackageWizardPage_error_jarFileExistsAndNotWritable);
				fDestinationNamesCombo.setFocus();
				return false;
			}
		}
		return true;
	}

	/*
	 * Overrides method from WizardExportPage
	 */
	protected void createDestinationGroup(Composite parent) {

		initializeDialogUnits(parent);

		// destination specification group
		Composite destinationSelectionGroup= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		destinationSelectionGroup.setLayout(layout);
		destinationSelectionGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));

		new Label(destinationSelectionGroup, SWT.NONE).setText(getDestinationLabel());

		// destination name entry field
		fDestinationNamesCombo= new Combo(destinationSelectionGroup, SWT.SINGLE | SWT.BORDER);
		fDestinationNamesCombo.addListener(SWT.Modify, this);
		fDestinationNamesCombo.addListener(SWT.Selection, this);
		GridData data= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		data.widthHint= SIZING_TEXT_FIELD_WIDTH;
		fDestinationNamesCombo.setLayoutData(data);

		// destination browse button
		fDestinationBrowseButton= new Button(destinationSelectionGroup, SWT.PUSH);
		fDestinationBrowseButton.setText(JarPackagerMessages.JarPackageWizardPage_browseButton_text);
		fDestinationBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		SWTUtil.setButtonDimensionHint(fDestinationBrowseButton);
		fDestinationBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleDestinationBrowseButtonPressed();
			}
		});
	}

	/**
	 *	Open an appropriate destination browser so that the user can specify a source
	 *	to import from
	 */
	protected void handleDescriptionFileBrowseButtonPressed() {
		SaveAsDialog dialog= new SaveAsDialog(getContainer().getShell());
		dialog.create();
		dialog.getShell().setText(JarPackagerMessages.JarPackageWizardPage_saveAsDialog_title);
		dialog.setMessage(JarPackagerMessages.JarPackageWizardPage_saveAsDialog_message);
		dialog.setOriginalFile(createFileHandle(fJarPackage.getDescriptionLocation()));
		if (dialog.open() == Window.OK) {
			IPath path= dialog.getResult();
			path= path.removeFileExtension().addFileExtension(AJJarPackagerUtil.DESCRIPTION_EXTENSION);
			fDescriptionFileText.setText(path.toString());
		}
	}

	/**
	 *	Open an appropriate destination browser so that the user can specify a source
	 *	to import from
	 */
	protected void handleDestinationBrowseButtonPressed() {
		FileDialog dialog= new FileDialog(getContainer().getShell(), SWT.SAVE);
		dialog.setFilterExtensions(new String[] {"*.jar", "*.zip"}); //$NON-NLS-1$ //$NON-NLS-2$

		String currentSourceString= getDestinationValue();
		int lastSeparatorIndex= currentSourceString.lastIndexOf(File.separator);
		if (lastSeparatorIndex != -1) {
			dialog.setFilterPath(currentSourceString.substring(0, lastSeparatorIndex));
			dialog.setFileName(currentSourceString.substring(lastSeparatorIndex + 1));
		}
		else
			dialog.setFileName(currentSourceString);
		String selectedFileName= dialog.open();
		if (selectedFileName != null)
			fDestinationNamesCombo.setText(selectedFileName);
	}

	/**
	 * Returns the resource for the specified path.
	 *
	 * @param path	the path for which the resource should be returned
	 * @return the resource specified by the path or <code>null</code>
	 */
	protected IResource findResource(IPath path) {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IStatus result= workspace.validatePath(
							path.toString(),
							IResource.ROOT | IResource.PROJECT | IResource.FOLDER | IResource.FILE);
		if (result.isOK() && workspace.getRoot().exists(path))
			return workspace.getRoot().findMember(path);
		return null;
	}

	/**
	 * Creates the checkbox tree and list for selecting resources.
	 *
	 * @param parent the parent control
	 */
	protected void createInputGroup(Composite parent) {
		int labelFlags= JavaElementLabelProvider.SHOW_BASICS
						| JavaElementLabelProvider.SHOW_OVERLAY_ICONS
						| JavaElementLabelProvider.SHOW_SMALL_ICONS;
		ITreeContentProvider treeContentProvider=
			new StandardJavaElementContentProvider() {
				public boolean hasChildren(Object element) {
					// prevent the + from being shown in front of packages
					return !(element instanceof IPackageFragment) && super.hasChildren(element);
				}

			};
		fInputGroup= new CheckboxTreeAndListGroup(
					parent,
					JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()),
					treeContentProvider,
					new JavaElementLabelProvider(labelFlags),
					new StandardJavaElementContentProvider(),
					new JavaElementLabelProvider(labelFlags),
					SWT.NONE,
					SIZING_SELECTION_WIDGET_WIDTH,
					SIZING_SELECTION_WIDGET_HEIGHT);
		fInputGroup.addTreeFilter(new EmptyInnerPackageFilter());
		fInputGroup.setTreeComparator(new JavaElementComparator());
		fInputGroup.setListSorter(new JavaElementComparator());
		fInputGroup.addTreeFilter(new ContainerFilter(ContainerFilter.FILTER_NON_CONTAINERS));
		fInputGroup.addTreeFilter(new LibraryFilter());
		fInputGroup.addListFilter(new ContainerFilter(ContainerFilter.FILTER_CONTAINERS));
		fInputGroup.getTree().addListener(SWT.MouseUp, this);
		fInputGroup.getTable().addListener(SWT.MouseUp, this);
	}

	/**
	 * Creates the export type controls.
	 *
	 * @param parent the parent control
	 */
	protected void createExportTypeGroup(Composite parent) {
		Composite optionsGroup= new Composite(parent, SWT.NONE);
		GridLayout optionsLayout= new GridLayout();
		optionsLayout.marginHeight= 0;
		optionsGroup.setLayout(optionsLayout);

		fExportClassFilesCheckbox= new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		fExportClassFilesCheckbox.setText(JarPackagerMessages.JarPackageWizardPage_exportClassFiles_text);
		fExportClassFilesCheckbox.addListener(SWT.Selection, this);

		fExportOutputFoldersCheckbox= new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		fExportOutputFoldersCheckbox.setText(JarPackagerMessages.JarPackageWizardPage_exportOutputFolders_text);
		fExportOutputFoldersCheckbox.addListener(SWT.Selection, this);

		fExportJavaFilesCheckbox= new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		fExportJavaFilesCheckbox.setText(JarPackagerMessages.JarPackageWizardPage_exportJavaFiles_text);
		fExportJavaFilesCheckbox.addListener(SWT.Selection, this);
	}

	/**
	 * Updates the enablements of this page's controls. Subclasses may extend.
	 */
	protected void updateWidgetEnablements() {
	}

	/*
	 * Overrides method from IJarPackageWizardPage
	 */
	public boolean isPageComplete() {
		boolean complete= validateSourceGroup();
		complete= validateDestinationGroup() && complete;
		complete= validateOptionsGroup() && complete;
		if (complete)
			setErrorMessage(null);
		return complete;
	}

	/*
	 * Implements method from Listener
	 */
	public void handleEvent(Event e) {
		if (getControl() == null)
			return;
		update();
	}

	protected void update() {
		updateModel();
		updateWidgetEnablements();
		updatePageCompletion();
	}

	protected void updatePageCompletion() {
		boolean pageComplete= isPageComplete();
		setPageComplete(pageComplete);
		if (pageComplete)
			setErrorMessage(null);
	}

	/*
	 * Overrides method from WizardDataTransferPage
	 */
	protected boolean validateDestinationGroup() {
		if (fDestinationNamesCombo.getText().length() == 0) {
			// Clear error
			if (getErrorMessage() != null)
				setErrorMessage(null);
			if (getMessage() != null)
				setMessage(null);
			return false;
		}
		if (fJarPackage.getAbsoluteJarLocation().toString().endsWith("/")) { //$NON-NLS-1$
			setErrorMessage(JarPackagerMessages.JarPackageWizardPage_error_exportDestinationMustNotBeDirectory);
			fDestinationNamesCombo.setFocus();
			return false;
		}
		if (getWorkspaceLocation() != null && getWorkspaceLocation().isPrefixOf(fJarPackage.getAbsoluteJarLocation())) {
			int segments= getWorkspaceLocation().matchingFirstSegments(fJarPackage.getAbsoluteJarLocation());
			IPath path= fJarPackage.getAbsoluteJarLocation().removeFirstSegments(segments);
			IResource resource= ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			if (resource != null && resource.getType() == IResource.FILE) {
				// test if included
				if (AJJarPackagerUtil.contains(AJJarPackagerUtil.asResources(fJarPackage.getElements()), (IFile)resource)) {
					setErrorMessage(JarPackagerMessages.JarPackageWizardPage_error_cantExportJARIntoItself);
					return false;
				}
			}
		}
		// Inform user about relative directory
		String currentMessage= getMessage();
		if (!(new File(fDestinationNamesCombo.getText()).isAbsolute())) {
			if (currentMessage == null)
				setMessage(JarPackagerMessages.JarPackageWizardPage_info_relativeExportDestination, IMessageProvider.INFORMATION);
		} else {
			if (currentMessage != null)
				setMessage(null);
		}
		return ensureTargetFileIsValid(fJarPackage.getAbsoluteJarLocation().toFile());
	}

	/*
	 * Overrides method from WizardDataTransferPage
	 */
	protected boolean validateOptionsGroup() {
		return true;
	}

	/*
	 * Overrides method from WizardDataTransferPage
	 */
	protected boolean validateSourceGroup() {
		if (!(fExportClassFilesCheckbox.getSelection() || fExportOutputFoldersCheckbox.getSelection() || fExportJavaFilesCheckbox.getSelection())) {
			setErrorMessage(JarPackagerMessages.JarPackageWizardPage_error_noExportTypeChecked);
			return false;
		}

		if (getSelectedResources().size() == 0) {
			if (getErrorMessage() != null)
				setErrorMessage(null);
			return false;
		}
		if (fExportClassFilesCheckbox.getSelection() || fExportOutputFoldersCheckbox.getSelection())
			return true;

		// Source file only export - check if there are source files
		Iterator iter= getSelectedResourcesIterator();
		while (iter.hasNext()) {
			Object element= iter.next();
			if (element instanceof IClassFile) {
				IPackageFragmentRoot root= (IPackageFragmentRoot)((IClassFile)element).getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
				if (root == null)
					continue;
				IClasspathEntry cpEntry;
				try {
					cpEntry= root.getRawClasspathEntry();
				} catch (JavaModelException e) {
					continue;
				}
				if (cpEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					return true;
				}
			} else {
				return true;
			}
		}

		if (getErrorMessage() != null)
			setErrorMessage(null);
		return false;
	}

	/**
	 * Creates a file resource handle for the file with the given workspace path.
	 * This method does not create the file resource; this is the responsibility
	 * of <code>createFile</code>.
	 *
	 * @param filePath the path of the file resource to create a handle for
	 * @return the new file resource handle
	 */
	protected IFile createFileHandle(IPath filePath) {
		if (filePath.isValidPath(filePath.toString()) && filePath.segmentCount() >= 2)
			return ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
		else
			return null;
	}

	/**
	 * Set the current input focus to self's destination entry field
 	 */
	protected void giveFocusToDestination() {
		fDestinationNamesCombo.setFocus();
	}

	/*
	 * Overrides method from WizardExportResourcePage
	 */
	protected void setupBasedOnInitialSelections() {
    for (Object selectedElement : fInitialSelection) {
      if (selectedElement instanceof IResource && !((IResource) selectedElement).isAccessible())
        continue;

      if (selectedElement instanceof IJavaElement && !((IJavaElement) selectedElement).exists())
        continue;

      if (selectedElement instanceof ICompilationUnit || selectedElement instanceof IClassFile || selectedElement instanceof IFile)
        fInputGroup.initialCheckListItem(selectedElement);
      else {
        if (selectedElement instanceof IFolder) {
          // Convert resource to Java element if possible
          IJavaElement je = JavaCore.create((IResource) selectedElement);
          if (je != null && je.exists() && je.getJavaProject().isOnClasspath((IResource) selectedElement))
            selectedElement = je;
        }
        fInputGroup.initialCheckTreeItem(selectedElement);
      }
    }

		TreeItem[] items= fInputGroup.getTree().getItems();
		int i= 0;
		while (i < items.length && !items[i].getChecked())
			i++;
		if (i < items.length) {
			fInputGroup.getTree().setSelection(new TreeItem[] {items[i]});
			fInputGroup.getTree().showSelection();
			fInputGroup.populateListViewer(items[i].getData());
		}
	}

	/*
	 * Implements method from IJarPackageWizardPage.
	 */
	public void finish() {
		saveWidgetValues();
	}

	/*
	 * Method declared on IWizardPage.
	 */
	public void setPreviousPage(IWizardPage page) {
		super.setPreviousPage(page);
		if (getControl() != null)
			updatePageCompletion();
	}

	Object[] getSelectedElementsWithoutContainedChildren() {
		Set closure= removeContainedChildren(fInputGroup.getWhiteCheckedTreeItems());
		closure.addAll(getExportedNonContainers());
		return closure.toArray();
	}

	private Set removeContainedChildren(Set elements) {
		Set newList= new HashSet(elements.size());
		Set javaElementResources= getCorrespondingContainers(elements);
		Iterator iter= elements.iterator();
		boolean removedOne= false;
		while (iter.hasNext()) {
			Object element= iter.next();
			Object parent;
			if (element instanceof IResource)
				parent= ((IResource)element).getParent();
			else if (element instanceof IJavaElement) {
				parent= ((IJavaElement)element).getParent();
				if (parent instanceof IPackageFragmentRoot) {
					IPackageFragmentRoot pkgRoot= (IPackageFragmentRoot)parent;
					try {
						if (pkgRoot.getCorrespondingResource() instanceof IProject)
							parent= pkgRoot.getJavaProject();
					} catch (JavaModelException ex) {
						// leave parent as is
					}
				}
			}
			else {
				// unknown type
				newList.add(element);
				continue;
			}
			if (element instanceof IJavaModel || ((!(parent instanceof IJavaModel)) && (elements.contains(parent) || javaElementResources.contains(parent))))
				removedOne= true;
			else
				newList.add(element);
		}
		if (removedOne)
			return removeContainedChildren(newList);
		else
			return newList;
	}

	private Set getExportedNonContainers() {
		Set whiteCheckedTreeItems= fInputGroup.getWhiteCheckedTreeItems();
		Set exportedNonContainers= new HashSet(whiteCheckedTreeItems.size());
		Set javaElementResources= getCorrespondingContainers(whiteCheckedTreeItems);
		Iterator iter= fInputGroup.getAllCheckedListItems();
		while (iter.hasNext()) {
			Object element= iter.next();
			Object parent= null;
			if (element instanceof IResource)
				parent= ((IResource)element).getParent();
			else if (element instanceof IJavaElement)
				parent= ((IJavaElement)element).getParent();
			if (!whiteCheckedTreeItems.contains(parent) && !javaElementResources.contains(parent))
				exportedNonContainers.add(element);
		}
		return exportedNonContainers;
	}

	/*
	 * Create a list with the folders / projects that correspond
	 * to the Java elements (Java project, package, package root)
	 */
	private Set getCorrespondingContainers(Set elements) {
		Set javaElementResources= new HashSet(elements.size());
    for (Object element : elements) {
      if (element instanceof IJavaElement) {
        IJavaElement je = (IJavaElement) element;
        int type = je.getElementType();
        if (type == IJavaElement.JAVA_PROJECT || type == IJavaElement.PACKAGE_FRAGMENT || type == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
          // exclude default package since it is covered by the root
          if (!(type == IJavaElement.PACKAGE_FRAGMENT && ((IPackageFragment) element).isDefaultPackage())) {
            Object resource;
            try {
              resource = je.getCorrespondingResource();
            }
            catch (JavaModelException ex) {
              resource = null;
            }
            if (resource != null)
              javaElementResources.add(resource);
          }
        }
      }
    }
		return javaElementResources;
	}

	private Object[] getSelectedElements() {
		return getSelectedResources().toArray();
	}

	/**
	 * @return the location or <code>null</code>
	 */
	private IPath getWorkspaceLocation() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation();
	}
}
