/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - adjusted for ajdoc
 *******************************************************************************/

package org.eclipse.ajdt.internal.ui.ajdocexport;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jdt.internal.ui.javadocexport.JavadocExportMessages;
import org.eclipse.jdt.internal.ui.javadocexport.JavadocLinkDialogLabelProvider;
import org.eclipse.jdt.internal.ui.javadocexport.JavadocLinkRef;
import org.eclipse.jdt.internal.ui.preferences.JavadocConfigurationBlock;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.internal.ui.util.SWTUtil;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * Copied from org.eclipse.jdt.internal.ui.javadocexport.JavadocStandardWizardPage
 * Changes marked with // AspectJ Extension
 */
public class AJdocStandardWizardPage extends AJdocWizardPage {


	private final int STYLESHEETSTATUS= 1;
	private final int LINK_REFERENCES= 2;

//	 AspectJ Extension
	private final AJdocOptionsManager fStore;
	private Composite fUpperComposite;

	private Group fBasicOptionsGroup;
	private Group fTagsGroup;

	private Button fTitleButton;
	private Text fTitleText;
	private Text fStyleSheetText;
	private FlaggedButton fDeprecatedList;
	private FlaggedButton fDeprecatedCheck;
	private FlaggedButton fIndexCheck;
	private FlaggedButton fSeperatedIndexCheck;
	private Button fStyleSheetBrowseButton;
	private Button fStyleSheetButton;

	private CheckedListDialogField<JavadocLinkRef> fListDialogField;

	private StatusInfo fStyleSheetStatus;
	private StatusInfo fLinkRefStatus;

	private final ArrayList<FlaggedButton> fButtonsList;
//	 AspectJ Extension
	private final AJdocTreeWizardPage fFirstPage;

  public AJdocStandardWizardPage(String pageName, AJdocTreeWizardPage firstPage, AJdocOptionsManager store) {
		super(pageName);
		fFirstPage = firstPage;
		setDescription(JavadocExportMessages.JavadocStandardWizardPage_description);

		fStore = store;
		fButtonsList = new ArrayList<>();
		fStyleSheetStatus = new StatusInfo();
		fLinkRefStatus = new StatusInfo();
	}

	/*
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {

		initializeDialogUnits(parent);

		fUpperComposite= new Composite(parent, SWT.NONE);
		fUpperComposite.setLayoutData(createGridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL, 1, 0));

		GridLayout layout= createGridLayout(4);
		layout.marginHeight= 0;
		fUpperComposite.setLayout(layout);

		createBasicOptionsGroup(fUpperComposite);
		createTagOptionsGroup(fUpperComposite);
		createListDialogField(fUpperComposite);
		createStyleSheetGroup(fUpperComposite);

		setControl(fUpperComposite);
		Dialog.applyDialogFont(fUpperComposite);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(fUpperComposite, IJavaHelpContextIds.JAVADOC_STANDARD_PAGE);
	}
	private void createBasicOptionsGroup(Composite composite) {

		fTitleButton= createButton(composite, SWT.CHECK, JavadocExportMessages.JavadocStandardWizardPage_titlebutton_label, createGridData(1));
		fTitleText= createText(composite, SWT.SINGLE | SWT.BORDER, null, createGridData(GridData.FILL_HORIZONTAL, 3, 0));
		String text= fStore.getTitle();
		if (!text.equals("")) { //$NON-NLS-1$
			fTitleText.setText(text);
			fTitleButton.setSelection(true);
		} else
			fTitleText.setEnabled(false);

		fBasicOptionsGroup= new Group(composite, SWT.SHADOW_ETCHED_IN);
		fBasicOptionsGroup.setLayout(createGridLayout(1));
		fBasicOptionsGroup.setLayoutData(createGridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL, 2, 0));
		fBasicOptionsGroup.setText(JavadocExportMessages.JavadocStandardWizardPage_basicgroup_label);

		new FlaggedButton(fBasicOptionsGroup, JavadocExportMessages.JavadocStandardWizardPage_usebutton_label, new GridData(GridData.FILL_HORIZONTAL), fStore.USE, true);
		new FlaggedButton(fBasicOptionsGroup, JavadocExportMessages.JavadocStandardWizardPage_hierarchybutton_label, new GridData(GridData.FILL_HORIZONTAL), fStore.NOTREE, false);
		new FlaggedButton(fBasicOptionsGroup, JavadocExportMessages.JavadocStandardWizardPage_navigartorbutton_label, new GridData(GridData.FILL_HORIZONTAL), fStore.NONAVBAR, false);

		fIndexCheck= new FlaggedButton(fBasicOptionsGroup, JavadocExportMessages.JavadocStandardWizardPage_indexbutton_label, new GridData(GridData.FILL_HORIZONTAL), fStore.NOINDEX, false);

		fSeperatedIndexCheck= new FlaggedButton(fBasicOptionsGroup, JavadocExportMessages.JavadocStandardWizardPage_seperateindexbutton_label, createGridData(GridData.GRAB_HORIZONTAL, 1, convertWidthInCharsToPixels(3)), fStore.SPLITINDEX, true);
		fSeperatedIndexCheck.getButton().setEnabled(fIndexCheck.getButton().getSelection());

		fIndexCheck.getButton().addSelectionListener(new ToggleSelectionAdapter(new Control[] { fSeperatedIndexCheck.getButton()}));
		fTitleButton.addSelectionListener(new ToggleSelectionAdapter(new Control[] { fTitleText }));

	}

	private void createTagOptionsGroup(Composite composite) {
		fTagsGroup= new Group(composite, SWT.SHADOW_ETCHED_IN);
		fTagsGroup.setLayout(createGridLayout(1));
		fTagsGroup.setLayoutData(createGridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL, 2, 0));
		fTagsGroup.setText(JavadocExportMessages.JavadocStandardWizardPage_tagsgroup_label);

		new FlaggedButton(fTagsGroup, JavadocExportMessages.JavadocStandardWizardPage_authorbutton_label, new GridData(GridData.FILL_HORIZONTAL), fStore.AUTHOR, true);
		new FlaggedButton(fTagsGroup, JavadocExportMessages.JavadocStandardWizardPage_versionbutton_label, new GridData(GridData.FILL_HORIZONTAL), fStore.VERSION, true);
		fDeprecatedCheck= new FlaggedButton(fTagsGroup, JavadocExportMessages.JavadocStandardWizardPage_deprecatedbutton_label, new GridData(GridData.FILL_HORIZONTAL), fStore.NODEPRECATED, false);
		fDeprecatedList= new FlaggedButton(fTagsGroup, JavadocExportMessages.JavadocStandardWizardPage_deprecatedlistbutton_label, createGridData(GridData.FILL_HORIZONTAL, 1, convertWidthInCharsToPixels(3)), fStore.NODEPRECATEDLIST, false);
		fDeprecatedList.getButton().setEnabled(fDeprecatedCheck.getButton().getSelection());

		fDeprecatedCheck.getButton().addSelectionListener(new ToggleSelectionAdapter(new Control[] { fDeprecatedList.getButton()}));
	} //end createTagOptionsGroup

	private void createStyleSheetGroup(Composite composite) {
		Composite c= new Composite(composite, SWT.NONE);
		c.setLayout(createGridLayout(3));
		c.setLayoutData(createGridData(GridData.FILL_HORIZONTAL, 4, 0));
		((GridLayout) c.getLayout()).marginWidth= 0;

		fStyleSheetButton= createButton(c, SWT.CHECK, JavadocExportMessages.JavadocStandardWizardPage_stylesheettext_label, createGridData(1));
		fStyleSheetText= createText(c, SWT.SINGLE | SWT.BORDER, null, createGridData(GridData.FILL_HORIZONTAL, 1, 0));
		//there really aught to be a way to specify this
		 ((GridData) fStyleSheetText.getLayoutData()).widthHint= 200;
		fStyleSheetBrowseButton= createButton(c, SWT.PUSH, JavadocExportMessages.JavadocStandardWizardPage_stylesheetbrowsebutton_label, createGridData(GridData.HORIZONTAL_ALIGN_END, 1, 0));
		SWTUtil.setButtonDimensionHint(fStyleSheetBrowseButton);

		String str= fStore.getStyleSheet();
		if (str.equals("")) { //$NON-NLS-1$
			//default
			fStyleSheetText.setEnabled(false);
			fStyleSheetBrowseButton.setEnabled(false);
		} else {
			fStyleSheetButton.setSelection(true);
			fStyleSheetText.setText(str);
		}

		//Listeners
		fStyleSheetButton.addSelectionListener(new ToggleSelectionAdapter(new Control[] { fStyleSheetText, fStyleSheetBrowseButton }) {
			public void validate() {
				doValidation(STYLESHEETSTATUS);
			}
		});

		fStyleSheetText.addModifyListener(e -> doValidation(STYLESHEETSTATUS));

		fStyleSheetBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleFileBrowseButtonPressed(fStyleSheetText, new String[] { "*.css" }, JavadocExportMessages.JavadocSpecificsWizardPage_stylesheetbrowsedialog_title);  //$NON-NLS-1$
			}
		});

	}

	private void createListDialogField(Composite composite) {
		Composite c= new Composite(composite, SWT.NONE);
		c.setLayout(createGridLayout(3));
		c.setLayoutData(createGridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL, 4, 0));
		((GridLayout) c.getLayout()).marginWidth= 0;

		String[] buttonlabels= new String[] { JavadocExportMessages.JavadocStandardWizardPage_selectallbutton_label, JavadocExportMessages.JavadocStandardWizardPage_clearallbutton_label, JavadocExportMessages.JavadocStandardWizardPage_configurebutton_label};

		JavadocLinkDialogLabelProvider labelProvider= new JavadocLinkDialogLabelProvider();

		ListAdapter adapter= new ListAdapter();

		fListDialogField = new CheckedListDialogField<>(adapter, buttonlabels, labelProvider);
		fListDialogField.setDialogFieldListener(adapter);
		fListDialogField.setCheckAllButtonIndex(0);
		fListDialogField.setUncheckAllButtonIndex(1);
		fListDialogField.setViewerSorter(new ViewerSorter());

		createLabel(c, SWT.NONE, JavadocExportMessages.JavadocStandardWizardPage_referencedclasses_label, createGridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 4, 0));
		fListDialogField.doFillIntoGrid(c, 3);

		LayoutUtil.setHorizontalGrabbing(fListDialogField.getListControl(null));

		fListDialogField.enableButton(2, false);
	}

	private List<JavadocLinkRef> getCheckedReferences(JavadocLinkRef[] referencesClasses) {
		List<JavadocLinkRef> checkedElements = new ArrayList<>();
		String[] hrefs = fStore.getHRefs();
		if (hrefs.length > 0) {
			HashSet<String> set = new HashSet<>();
			Collections.addAll(set, hrefs);
			for (JavadocLinkRef curr : referencesClasses) {
				URL url = curr.getURL();
				if (url != null && set.contains(url.toExternalForm()))
					checkedElements.add(curr);
			}
		}
		return checkedElements;
	}

	/**
	 * Returns IJavaProjects and IPaths that will be on the classpath
	 */
	private JavadocLinkRef[] getReferencedElements(IJavaProject[] checkedProjects) {
		HashSet<JavadocLinkRef> result = new HashSet<>();
		for (IJavaProject project : checkedProjects) {
			try {
				collectReferencedElements(project, result);
			}
			catch (CoreException e) {
				JavaPlugin.log(e);
				// ignore
			}
		}
		return result.toArray(new JavadocLinkRef[0]);
	}

	private void collectReferencedElements(IJavaProject project, Set<JavadocLinkRef> result) throws CoreException {
		IRuntimeClasspathEntry[] unresolved = JavaRuntime.computeUnresolvedRuntimeClasspath(project);
		for (IRuntimeClasspathEntry curr : unresolved) {
			if (curr.getType() == IRuntimeClasspathEntry.PROJECT)
				result.add(new JavadocLinkRef(JavaCore.create((IProject) curr.getResource())));
			else {
				IRuntimeClasspathEntry[] entries = JavaRuntime.resolveRuntimeClasspathEntry(curr, project);
				for (IRuntimeClasspathEntry entry : entries) {
					if (entry.getType() == IRuntimeClasspathEntry.PROJECT)
						result.add(new JavadocLinkRef(JavaCore.create((IProject) entry.getResource())));
					else if (entry.getType() == IRuntimeClasspathEntry.ARCHIVE) {
						IClasspathEntry classpathEntry = entry.getClasspathEntry();
						if (classpathEntry != null) {
							IPath containerPath = null;
							if (curr.getType() == IRuntimeClasspathEntry.CONTAINER)
								containerPath = curr.getPath();
							result.add(new JavadocLinkRef(containerPath, classpathEntry, project));
						}
					}
				}
			}
		}
	}

	final void doValidation(int VALIDATE) {
		switch (VALIDATE) {
			case STYLESHEETSTATUS:
				fStyleSheetStatus = new StatusInfo();
				if (fStyleSheetButton.getSelection()) {
					String filename = fStyleSheetText.getText();
					if (filename.length() == 0)
						fStyleSheetStatus.setError(JavadocExportMessages.JavadocSpecificsWizardPage_overviewnotfound_error);
					else {
						File file = new File(filename);
						String ext = filename.substring(filename.lastIndexOf('.') + 1);
						if (!file.isFile())
							fStyleSheetStatus.setError(JavadocExportMessages.JavadocStandardWizardPage_stylesheetnopath_error);
						else if (!ext.equalsIgnoreCase("css")) //$NON-NLS-1$
							fStyleSheetStatus.setError(JavadocExportMessages.JavadocStandardWizardPage_stylesheetnotcss_error);
					}
				}
				break;
			case LINK_REFERENCES:
				fLinkRefStatus = new StatusInfo();
				List<JavadocLinkRef> list = fListDialogField.getCheckedElements();
				for (JavadocLinkRef linkRef : list) {
					URL url = linkRef.getURL();
					if (url == null) {
						fLinkRefStatus.setWarning(JavadocExportMessages.JavadocStandardWizardPage_nolinkref_error);
						break;
					}
					else if ("jar".equals(url.getProtocol())) { //$NON-NLS-1$
						fLinkRefStatus.setWarning(JavadocExportMessages.JavadocStandardWizardPage_nojarlinkref_error);
						break;
					}
				}
				break;
		}

		updateStatus(findMostSevereStatus());
	}

	private IStatus findMostSevereStatus() {
		return StatusUtil.getMostSevere(new IStatus[] { fStyleSheetStatus, fLinkRefStatus });
	}

	public void updateStore() {
		fStore.setTitle(fTitleButton.getSelection() ? fTitleText.getText(): ""); //$NON-NLS-1$

		//don't store the buttons if they are not enabled
		//this will change when there is a single page aimed at the standard doclet
		FlaggedButton[] buttons = fButtonsList.toArray(new FlaggedButton[0]);
		for (FlaggedButton button : buttons) {
			if (button.getButton().getEnabled())
				fStore.setBoolean(button.getFlag(), button.getButton().getSelection() == button.show());
			else
				fStore.setBoolean(button.getFlag(), !button.show());
		}

		if (fStyleSheetText.getEnabled())
			fStore.setStyleSheet(fStyleSheetText.getText());
		else
			fStore.setStyleSheet(""); //$NON-NLS-1$

		fStore.setHRefs(getHRefs());
	}

	private String[] getHRefs() {
		Set<String> hrefs = new HashSet<>();
		List<JavadocLinkRef> checkedElements = fListDialogField.getCheckedElements();
		for (JavadocLinkRef checkedElement : checkedElements) {
			URL url = checkedElement.getURL();
			if (url != null)
				hrefs.add(url.toExternalForm());
		}
		return hrefs.toArray(new String[0]);
	}

	//get the links

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			doValidation(STYLESHEETSTATUS);
			updateHRefList(fFirstPage.getCheckedProjects());
		} else {
			fStore.setHRefs(getHRefs());
		}
	}

	/**
	 * Method will refresh the list of referenced libraries and projects
	 * depended on the projects or elements of projects selected in the
	 * TreeViewer on the AJdocTreeWizardPage.
	 */
	private void updateHRefList(IJavaProject[] checkedProjects) {
		JavadocLinkRef[] res = getReferencedElements(checkedProjects);
		fListDialogField.setElements(Arrays.asList(res));
		List<JavadocLinkRef> checked= getCheckedReferences(res);
		fListDialogField.setCheckedElements(checked);
	}

	public void init() {
		updateStatus(new StatusInfo());
	}

	protected class FlaggedButton {

		private final Button fButton;
		private final String fFlag;
		private final boolean fShowFlag;

		public FlaggedButton(Composite composite, String message, GridData gridData, String flag, boolean show) {
			fFlag= flag;
			fShowFlag= show;
			fButton= createButton(composite, SWT.CHECK, message, gridData);
			fButtonsList.add(this);
			setButtonSettings();
		}

		public Button getButton() {
			return fButton;
		}

		public String getFlag() {
			return fFlag;
		}
		public boolean show() {
			return fShowFlag;
		}

		private void setButtonSettings() {

			fButton.setSelection(fStore.getBoolean(fFlag) == fShowFlag);
		}

	} //end class FlaggesButton

	private class ListAdapter implements IListAdapter<JavadocLinkRef>, IDialogFieldListener {
		@Override
		public void customButtonPressed(ListDialogField<JavadocLinkRef> field, int index) {
			if (index == 2)
				doEditButtonPressed();
		}

		@Override
		public void selectionChanged(ListDialogField<JavadocLinkRef> field) {
			List<JavadocLinkRef> selection = fListDialogField.getSelectedElements();
			fListDialogField.enableButton(2, selection.size() == 1);
		}

		@Override
		public void doubleClicked(ListDialogField<JavadocLinkRef> field) {
			doEditButtonPressed();
		}

		public void dialogFieldChanged(DialogField field) {
			doValidation(LINK_REFERENCES);
		}

	}

	/**
	 * Method doEditButtonPressed.
	 */
	private void doEditButtonPressed() {
		List<JavadocLinkRef> selected = fListDialogField.getSelectedElements();
		if (selected.isEmpty())
			return;
		JavadocLinkRef linkRef = selected.get(0);
		if (linkRef != null) {
			JavadocPropertyDialog jdialog = new JavadocPropertyDialog(getShell(), linkRef);
			if (jdialog.open() == Window.OK)
				fListDialogField.refresh();
		}
	}

	private class JavadocPropertyDialog extends StatusDialog implements IStatusChangeListener {

		private final JavadocConfigurationBlock fJavadocConfigurationBlock;
		private final JavadocLinkRef fElement;

		public JavadocPropertyDialog(Shell parent, JavadocLinkRef selection) {
			super(parent);
			setTitle(JavadocExportMessages.JavadocStandardWizardPage_javadocpropertydialog_title);

			fElement= selection;
			URL initialLocation= selection.getURL();
			fJavadocConfigurationBlock= new JavadocConfigurationBlock(parent, this, initialLocation, selection.isProjectRef());
		}

		protected Control createDialogArea(Composite parent) {
			Composite composite= (Composite) super.createDialogArea(parent);
			Control inner= fJavadocConfigurationBlock.createContents(composite);
			inner.setLayoutData(new GridData(GridData.FILL_BOTH));
			applyDialogFont(composite);
			return composite;
		}

		public void statusChanged(IStatus status) {
			updateStatus(status);

		}

		/**
		 * @see Dialog#okPressed()
		 */
		protected void okPressed() {
			try {
				IWorkspaceRunnable runnable = monitor -> {
					URL javadocLocation = fJavadocConfigurationBlock.getJavadocLocation();
					fElement.setURL(javadocLocation, monitor);
				};
				PlatformUI.getWorkbench().getProgressService().run(true, true, new WorkbenchRunnableAdapter(runnable));
			}
			catch (InvocationTargetException e) {
				String title = JavadocExportMessages.JavadocStandardWizardPage_configurecontainer_error_title;
				String message = JavadocExportMessages.JavadocStandardWizardPage_configurecontainer_error_message;
				ExceptionHandler.handle(e, getShell(), title, message);
			}
			catch (InterruptedException e) {
				// user cancelled
			}

			fListDialogField.refresh();
			doValidation(LINK_REFERENCES);
			super.okPressed();
		}

		/*
		 * @see org.eclipse.jface.window.Window#configureShell(Shell)
		 */
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
            PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IJavaHelpContextIds.JAVADOC_PROPERTY_DIALOG);
		}
	}
}
