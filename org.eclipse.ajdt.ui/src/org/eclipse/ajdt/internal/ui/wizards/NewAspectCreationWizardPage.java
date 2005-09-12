/**********************************************************************
Copyright (c) 2003, 2004 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Matt Chapman - initial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.TypeInfo;
import org.eclipse.jdt.internal.ui.dialogs.TypeSelectionDialog;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementSorter;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class NewAspectCreationWizardPage extends WizardPage implements Listener {
	private Button createMainButton;
	private Button modPublicButton;
	private Button modAbstractButton;
	private Button modFinalButton;
	private Button modPrivilegedButton;
	private Button modStaticButton;
	private Button packageBrowseButton;
	private Button sourceBrowseButton;
	private Button enclosingTypeButton;
	private Button enclosingBrowseButton;
	private Button supertypeBrowseButton;
	private Button addInterfaceButton;
	private Button removeInterfaceButton;
	private Button instantiationButton;
	private Button instSingletonButton;
	private Button instPerthisButton;
	private Button instPertargetButton;
	private Button instPercflowButton;
	private Button instPercflowbelowButton;
	private Button instPertypewithinButton;
	
	private Label packageLabel;
	private List interfaceList;

	private Text sourceText;
	private Text packageText;
	private Text nameText;
	private Text enclosingText;
	private Text extendsText;

	private String initialSourceString;
	private String initialPackageString;
	private String initialEnclosingTypeString;

	// the current resource selection
	private IStructuredSelection currentSelection;

	private IWorkbench workbench;
	private IJavaProject jproject;
	private IPackageFragmentRoot fCurrRoot;
	private IJavaElement jelem;
	private IType fCurrEnclosingType;

	// dummy .java file handle so that we can create an ICompilationUnit
	private IFile dotjavaFile;

	// Status objects

	private static final Status okStatus = new Status(IStatus.OK,
			AspectJUIPlugin.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
	private static final int SOURCE_STATUS = 0;
	private static final int PACKAGE_STATUS = 1;
	private static final int ENCLOSING_STATUS = 2;
	private static final int NAME_STATUS = 3;
	private Status[] statusArray = new Status[4];

	private static String lineDelimiter = System.getProperty(
			"line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

	private ImportManager imports;

	/**
	 * Creates a new file creation wizard page. If the initial resource
	 * selection contains exactly one container resource then it will be used as
	 * the default container resource.
	 * 
	 * @param pageName
	 *            the name of the page
	 * @param selection
	 *            the current resource selection
	 */
	public NewAspectCreationWizardPage(String pageName, IWorkbench workbench,
			IStructuredSelection selection) {
		super(pageName);
		setPageComplete(false);
		this.currentSelection = selection;

		setTitle(AspectJUIPlugin
				.getResourceString("NewAspectCreationWizardPage.title")); //$NON-NLS-1$
		setDescription(AspectJUIPlugin
				.getResourceString("NewAspectCreationWizardPage.description")); //$NON-NLS-1$
		this.workbench = workbench;
		init();
	}

	public void init() {
		jelem = NewAspectUtils.getInitialJavaElement(currentSelection);
		initSourceFolder(jelem);
		initPackage(jelem);
		initEnclosingType(jelem);
	}

	private void initSourceFolder(IJavaElement elem) {
		initialSourceString = ""; //$NON-NLS-1$
		if (elem != null) {
			fCurrRoot = JavaModelUtil.getPackageFragmentRoot(elem);
			if (fCurrRoot == null || fCurrRoot.isArchive()) {
				jproject = elem.getJavaProject();
				if (jproject != null && jproject.exists()) {
					fCurrRoot = null;
					try {
						IPackageFragmentRoot[] roots = jproject
								.getPackageFragmentRoots();
						for (int i = 0; i < roots.length; i++) {
							if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE) {
								fCurrRoot = roots[i];
								break;
							}
						}
					} catch (JavaModelException e) {
					}
				}
			}
			if (fCurrRoot != null) {
				initialSourceString = fCurrRoot.getPath().makeRelative()
						.toString();
			}
		}
	}

	private void initPackage(IJavaElement elem) {
		initialPackageString = ""; //$NON-NLS-1$
		if (elem != null) {
			IPackageFragment pack = (IPackageFragment) elem
					.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
			if (pack != null) {
				initialPackageString = pack.getElementName();
			}
		}
	}

	private void initEnclosingType(IJavaElement elem) {
		IType enclosingType = null;

		if (elem != null) {
			// evaluate the enclosing type
			IType typeInCU = (IType) elem.getAncestor(IJavaElement.TYPE);
			if (typeInCU != null) {
				if (typeInCU.getCompilationUnit() != null) {
					enclosingType = typeInCU;
				}
			} else {
				ICompilationUnit cu = (ICompilationUnit) elem
						.getAncestor(IJavaElement.COMPILATION_UNIT);
				if (cu != null) {
					enclosingType = cu.findPrimaryType();
				}
			}

		}
		if (enclosingType != null) {
			initialEnclosingTypeString = enclosingType.getFullyQualifiedName();
		} else {
			initialEnclosingTypeString = ""; //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
		// create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 4;
		gl.numColumns = ncol;
		composite.setLayout(gl);
		// create the widgets and their grid data objects

		new Label(composite, SWT.NONE)
				.setText(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.sourceFolder.label")); //$NON-NLS-1$

		sourceText = new Text(composite, SWT.BORDER);
		sourceText.setText(initialSourceString);
		updateSourceStatus();

		GridData gd;
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol - 2;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 350;
		sourceText.setLayoutData(gd);

		sourceBrowseButton = new Button(composite, SWT.PUSH);
		sourceBrowseButton
				.setText(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.sourceFolder.button")); //$NON-NLS-1$
		sourceBrowseButton.addListener(SWT.Selection, this);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = false;
		gd.widthHint = NewAspectUtils.getButtonWidthHint(sourceBrowseButton);
		sourceBrowseButton.setLayoutData(gd);

		packageLabel = new Label(composite, SWT.NONE);
		packageLabel
				.setText(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.package.label")); //$NON-NLS-1$

		packageText = new Text(composite, SWT.BORDER);
		packageText.setText(initialPackageString);
		updatePackageStatus();

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol - 2;
		gd.grabExcessHorizontalSpace = true;
		packageText.setLayoutData(gd);

		packageBrowseButton = new Button(composite, SWT.PUSH);
		packageBrowseButton
				.setText(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.package.button")); //$NON-NLS-1$
		packageBrowseButton.addListener(SWT.Selection, this);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = false;
		gd.widthHint = NewAspectUtils.getButtonWidthHint(packageBrowseButton);
		packageBrowseButton.setLayoutData(gd);
		if (statusArray[SOURCE_STATUS] != okStatus) {
			packageBrowseButton.setEnabled(false);
		}

		enclosingTypeButton = new Button(composite, SWT.CHECK);
		enclosingTypeButton
				.setText(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.enclosingType.label")); //$NON-NLS-1$
		enclosingTypeButton.addListener(SWT.Selection, this);

		enclosingText = new Text(composite, SWT.BORDER);
		enclosingText.setText(initialEnclosingTypeString);
		enclosingText.setEnabled(false);
		updateEnclosingStatus();
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = ncol - 2;
		enclosingText.setLayoutData(gd);

		enclosingBrowseButton = new Button(composite, SWT.PUSH);
		enclosingBrowseButton
				.setText(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.enclosingType.button")); //$NON-NLS-1$
		enclosingBrowseButton.addListener(SWT.Selection, this);
		enclosingBrowseButton.setEnabled(false);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = false;
		gd.widthHint = NewAspectUtils.getButtonWidthHint(enclosingBrowseButton);
		enclosingBrowseButton.setLayoutData(gd);

		NewAspectUtils.createLine(composite, ncol);

		new Label(composite, SWT.NONE)
				.setText(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.typeName.label")); //$NON-NLS-1$

		nameText = new Text(composite, SWT.BORDER);
		updateNameStatus();

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = ncol - 2;
		nameText.setLayoutData(gd);

		NewAspectUtils.createBlank(composite);

		new Label(composite, SWT.NONE)
				.setText(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.modifiers.label")); //$NON-NLS-1$

		Composite modComposite = new Composite(composite, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol - 2;
		modComposite.setLayoutData(gd);

		GridLayout mgl = new GridLayout();
		mgl.marginWidth = 0;
		mgl.marginHeight = 0;
		mgl.numColumns = 5;
		modComposite.setLayout(mgl);

		modPublicButton = new Button(modComposite, SWT.CHECK);
		modPublicButton.setSelection(true);
		modPublicButton.setText("public"); //$NON-NLS-1$

		modAbstractButton = new Button(modComposite, SWT.CHECK);
		modAbstractButton.setSelection(false);
		modAbstractButton.setText("abstract"); //$NON-NLS-1$

		modFinalButton = new Button(modComposite, SWT.CHECK);
		modFinalButton.setSelection(false);
		modFinalButton.setText("final"); //$NON-NLS-1$

		modPrivilegedButton = new Button(modComposite, SWT.CHECK);
		modPrivilegedButton.setSelection(false);
		modPrivilegedButton.setText("privileged"); //$NON-NLS-1$

		modStaticButton = new Button(modComposite, SWT.CHECK);
		modStaticButton.setSelection(false);
		modStaticButton.setEnabled(false);
		modStaticButton.setText("static"); //$NON-NLS-1$

		NewAspectUtils.createBlank(composite);

		// instantiation options		
		instantiationButton = new Button(composite, SWT.CHECK);
		instantiationButton.setText(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.instantiation.label")); //$NON-NLS-1$
		instantiationButton.setSelection(false);
		instantiationButton.addListener(SWT.Selection, this);
		
		Composite instComposite = new Composite(composite, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol - 2;
		instComposite.setLayoutData(gd);

		GridLayout igl = new GridLayout();
		igl.marginWidth = 0;
		igl.marginHeight = 0;
		igl.numColumns = 3;
		instComposite.setLayout(igl);

		instSingletonButton = new Button(instComposite, SWT.RADIO);
		instSingletonButton.setEnabled(false);
		instSingletonButton.setSelection(true);
		instSingletonButton.setText("issingleton"); //$NON-NLS-1$

		instPerthisButton = new Button(instComposite, SWT.RADIO);
		instPerthisButton.setEnabled(false);
		instPerthisButton.setSelection(false);
		instPerthisButton.setText("perthis"); //$NON-NLS-1$

		instPertargetButton = new Button(instComposite, SWT.RADIO);
		instPertargetButton.setEnabled(false);
		instPertargetButton.setSelection(false);
		instPertargetButton.setText("pertarget"); //$NON-NLS-1$

		instPercflowButton = new Button(instComposite, SWT.RADIO);
		instPercflowButton.setEnabled(false);
		instPercflowButton.setSelection(false);
		instPercflowButton.setText("percflow"); //$NON-NLS-1$

		instPercflowbelowButton = new Button(instComposite, SWT.RADIO);
		instPercflowbelowButton.setEnabled(false);
		instPercflowbelowButton.setSelection(false);
		instPercflowbelowButton.setText("percflowbelow"); //$NON-NLS-1$

		instPertypewithinButton = new Button(instComposite, SWT.RADIO);
		instPertypewithinButton.setEnabled(false);
		instPertypewithinButton.setSelection(false);
		instPertypewithinButton.setText("pertypewithin"); //$NON-NLS-1$

		NewAspectUtils.createBlank(composite);
		
		new Label(composite, SWT.NONE)
				.setText(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.supertype.label")); //$NON-NLS-1$

		extendsText = new Text(composite, SWT.BORDER);
		extendsText.setText("java.lang.Object"); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol - 2;
		gd.grabExcessHorizontalSpace = true;
		extendsText.setLayoutData(gd);

//		NewAspectUtils.createBlank(composite);
		supertypeBrowseButton = new Button(composite, SWT.PUSH);
		supertypeBrowseButton
				.setText(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.enclosingType.button")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = false;
		gd.widthHint = NewAspectUtils.getButtonWidthHint(supertypeBrowseButton);
		supertypeBrowseButton.setLayoutData(gd);
		supertypeBrowseButton.addListener(SWT.Selection, this);

		Label interfaceLabel = new Label(composite, SWT.NONE);
		interfaceLabel
				.setText(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.interfaces.label")); //$NON-NLS-1$
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		interfaceLabel.setLayoutData(gd);

		interfaceList = new List(composite, SWT.MULTI | SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = ncol - 2;
		interfaceList.setLayoutData(gd);

		Composite contents = new Composite(composite, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		contents.setLayout(layout);

		addInterfaceButton = new Button(contents, SWT.PUSH);
		addInterfaceButton
				.setText(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.interfaces.add.button")); //$NON-NLS-1$
		addInterfaceButton.addListener(SWT.Selection, this);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = NewAspectUtils.getButtonWidthHint(addInterfaceButton);
		addInterfaceButton.setLayoutData(gd);

		NewAspectUtils.createBlank(contents);

		removeInterfaceButton = new Button(contents, SWT.PUSH);
		removeInterfaceButton
				.setText(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.interfaces.remove.button")); //$NON-NLS-1$
		removeInterfaceButton.addListener(SWT.Selection, this);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = false;
		gd.widthHint = NewAspectUtils.getButtonWidthHint(removeInterfaceButton);
		sourceBrowseButton.setLayoutData(gd);

		removeInterfaceButton.setLayoutData(gd);

		Label stubsLabel = new Label(composite, SWT.NONE);
		stubsLabel
				.setText(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.methodStubs.label")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = ncol;
		stubsLabel.setLayoutData(gd);

		NewAspectUtils.createBlank(composite);

		createMainButton = new Button(composite, SWT.CHECK);
		createMainButton.setSelection(false);
		createMainButton.setText("public static void main(String[] args)"); //$NON-NLS-1$

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol - 1;
		createMainButton.setLayoutData(gd);

		/*
		 * createBlank(composite);
		 * 
		 * fileExtButton = new Button(composite, SWT.CHECK);
		 * fileExtButton.setSelection(false); fileExtButton.setText("Use .java
		 * as the file extension instead of .aj");
		 * 
		 * gd = new GridData(GridData.FILL_HORIZONTAL); gd.horizontalSpan =
		 * ncol-1; fileExtButton.setLayoutData(gd);
		 */

		setControl(composite);

		sourceText.addListener(SWT.KeyUp, this);
		packageText.addListener(SWT.KeyUp, this);
		nameText.addListener(SWT.KeyUp, this);
		enclosingText.addListener(SWT.KeyUp, this);
	}

	/*
	 * Sets the completed field on the wizard class when all the information is
	 * entered and the wizard can be completed
	 */
	public boolean isPageComplete() {
		if (statusArray[SOURCE_STATUS] == null
				|| statusArray[SOURCE_STATUS].matches(IStatus.ERROR)) {
			return false;
		}
		if (statusArray[PACKAGE_STATUS] == null
				|| statusArray[PACKAGE_STATUS].matches(IStatus.ERROR)) {
			return false;
		}
		if (statusArray[NAME_STATUS] == null
				|| statusArray[NAME_STATUS].matches(IStatus.ERROR)) {
			return false;
		}
		return true;
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			nameText.setFocus();
		}
	}

	private void updateNameStatus() {
        statusArray[NAME_STATUS] = okStatus;
        if (NewAspectUtils.isTextNonEmpty(nameText)) {
            String typeName = getTypeName();

            // Bugzilla 66569 : Don't allow dots in the new aspect name
            if (typeName.indexOf('.') != -1) {
                statusArray[NAME_STATUS] = new Status(
                        IStatus.ERROR,
                        AspectJUIPlugin.PLUGIN_ID,
                        0,
                        AspectJUIPlugin
                                .getResourceString("NewAspectCreationWizardPage.Name_field_cannot_be_qualified"), //$NON-NLS-1$
                        null);
            }// end if dot detected
            else {
                IStatus val = JavaConventions.validateJavaTypeName(typeName);
                if (val.matches(IStatus.ERROR)) {
                    statusArray[NAME_STATUS] = new Status(IStatus.ERROR,
                            AspectJUIPlugin.PLUGIN_ID, 0, val.getMessage(), null);
                } else if (val.matches(IStatus.WARNING)) {
                    statusArray[NAME_STATUS] = new Status(IStatus.WARNING,
                            AspectJUIPlugin.PLUGIN_ID, 0, val.getMessage(), null);
                }
            }// end else no dot detected
        } else {
            statusArray[NAME_STATUS] = new Status(
                    IStatus.ERROR,
                    AspectJUIPlugin.PLUGIN_ID,
                    0,
                    AspectJUIPlugin
                            .getResourceString("NewAspectCreationWizardPage.Name_field_cannot_be_empty"), null); //$NON-NLS-1$
        }
    }

	private void updateSourceStatus() {
		statusArray[SOURCE_STATUS] = okStatus;
		if (!NewAspectUtils.isTextNonEmpty(sourceText)) {
			statusArray[SOURCE_STATUS] = new Status(
					IStatus.ERROR,
					AspectJUIPlugin.PLUGIN_ID,
					0,
					AspectJUIPlugin
							.getResourceString("NewAspectCreationWizardPage.Source_folder_cannot_be_empty"), null); //$NON-NLS-1$
		} else {
			IPath path = new Path(sourceText.getText());
			IResource res = ResourcesPlugin.getWorkspace().getRoot()
					.findMember(path);
			if (res == null) {
				statusArray[SOURCE_STATUS] = new Status(
						IStatus.ERROR,
						AspectJUIPlugin.PLUGIN_ID,
						0,
						AspectJUIPlugin
								.getResourceString("NewAspectCreationWizardPage.Source_folder_does_not_exist"), null); //$NON-NLS-1$
			}
		}
	}

	private void updatePackageStatus() {
		statusArray[PACKAGE_STATUS] = okStatus;
		if (NewAspectUtils.isTextNonEmpty(packageText)) {
			IStatus val = JavaConventions.validatePackageName(packageText
					.getText());
			if (val.matches(IStatus.ERROR)) {
				statusArray[PACKAGE_STATUS] = new Status(IStatus.ERROR,
						AspectJUIPlugin.PLUGIN_ID, 0, val.getMessage(), null);
			} else if (val.matches(IStatus.WARNING)) {
				statusArray[PACKAGE_STATUS] = new Status(IStatus.WARNING,
						AspectJUIPlugin.PLUGIN_ID, 0, val.getMessage(), null);
			}
		}
	}

	private void updateEnclosingStatus() {
		statusArray[ENCLOSING_STATUS] = okStatus;
		if (!enclosingTypeButton.getSelection()) {
			return;
		}
		fCurrEnclosingType = null;

		IPackageFragmentRoot root = getPackageFragmentRoot();

		//fEnclosingTypeDialogField.enableButton(root != null);
		if (root == null) {
			//status.setError(""); //$NON-NLS-1$
			return;
		}

		String enclName = enclosingText.getText();
		if (enclName.length() == 0) {
			statusArray[ENCLOSING_STATUS] = new Status(
					IStatus.ERROR,
					AspectJUIPlugin.PLUGIN_ID,
					0,
					AspectJUIPlugin
							.getResourceString("NewAspectCreationWizardPage.error.EnclosingTypeEnterName"), null); //$NON-NLS-1$
			return;
		}
		try {
			IType type = findType(root.getJavaProject(), enclName);
			if (type == null) {
				statusArray[ENCLOSING_STATUS] = new Status(
						IStatus.ERROR,
						AspectJUIPlugin.PLUGIN_ID,
						0,
						AspectJUIPlugin
								.getResourceString("NewAspectCreationWizardPage.error.EnclosingTypeNotExists"), null); //$NON-NLS-1$
				return;
			}

			if (type.getCompilationUnit() == null) {
				statusArray[ENCLOSING_STATUS] = new Status(
						IStatus.ERROR,
						AspectJUIPlugin.PLUGIN_ID,
						0,
						AspectJUIPlugin
								.getResourceString("NewAspectCreationWizardPage.error.EnclosingNotInCU"), null); //$NON-NLS-1$
				return;
			}
			if (!JavaModelUtil.isEditable(type.getCompilationUnit())) {
				statusArray[ENCLOSING_STATUS] = new Status(
						IStatus.ERROR,
						AspectJUIPlugin.PLUGIN_ID,
						0,
						AspectJUIPlugin
								.getResourceString("NewAspectCreationWizardPage.error.EnclosingNotEditable"), null); //$NON-NLS-1$
				return;
			}

			fCurrEnclosingType = type;
			IPackageFragmentRoot enclosingRoot = JavaModelUtil
					.getPackageFragmentRoot(type);
			if (!enclosingRoot.equals(root)) {
				statusArray[ENCLOSING_STATUS] = new Status(
						IStatus.WARNING,
						AspectJUIPlugin.PLUGIN_ID,
						0,
						AspectJUIPlugin
								.getResourceString("NewAspectCreationWizardPage.warning.EnclosingNotInSourceFolder"), null); //$NON-NLS-1$
			}
			return;
		} catch (JavaModelException e) {
			statusArray[ENCLOSING_STATUS] = new Status(
					IStatus.ERROR,
					AspectJUIPlugin.PLUGIN_ID,
					0,
					AspectJUIPlugin
							.getResourceString("NewAspectCreationWizardPage.error.EnclosingTypeNotExists"), null); //$NON-NLS-1$
			return;
		}
	}

	public void handleEvent(Event event) {
		if (event.widget == nameText) {
			updateNameStatus();
		} else if (event.widget == sourceText) {
			updateSourceStatus();
			if (statusArray[SOURCE_STATUS] == okStatus) {
				packageBrowseButton.setEnabled(true);
			} else {
				packageBrowseButton.setEnabled(false);
			}
		} else if (event.widget == packageText) {
			updatePackageStatus();
		} else if (event.widget == enclosingText) {
			updateEnclosingStatus();
		} else if (event.widget == sourceBrowseButton) {
			IPackageFragmentRoot newSource = chooseSourceContainer(jelem);
			if (newSource != null) {
				sourceText.setText(newSource.getPath().makeRelative()
						.toString());
				fCurrRoot = newSource;
				updateSourceStatus();
			}
			if (statusArray[SOURCE_STATUS] == okStatus) {
				packageBrowseButton.setEnabled(true);
			} else {
				packageBrowseButton.setEnabled(false);
			}
		} else if (event.widget == packageBrowseButton) {
			IPackageFragment newPack = choosePackage();
			if (newPack != null) {
				packageText.setText(newPack.getElementName());
				updatePackageStatus();
			}
		} else if (event.widget == enclosingTypeButton) {
			if (enclosingTypeButton.getSelection()) {
				enclosingText.setEnabled(true);
				enclosingBrowseButton.setEnabled(true);
				packageLabel.setEnabled(false);
				packageText.setEnabled(false);
				packageBrowseButton.setEnabled(false);
				modStaticButton.setEnabled(true);
				modStaticButton.setSelection(true);
			} else {
				enclosingText.setEnabled(false);
				enclosingBrowseButton.setEnabled(false);
				packageLabel.setEnabled(true);
				packageText.setEnabled(true);
				packageBrowseButton.setEnabled(true);
				modStaticButton.setEnabled(false);
				modStaticButton.setSelection(false);
			}
			updateEnclosingStatus();
		} else if (event.widget == addInterfaceButton) {
			SuperInterfaceSelectionDialog dialog = new SuperInterfaceSelectionDialog(
					getShell(), getWizard().getContainer(), interfaceList,
					jproject);
			dialog
					.setTitle(AspectJUIPlugin
							.getResourceString("NewAspectCreationWizardPage.InterfacesDialog.class.title")); //$NON-NLS-1$
			dialog
					.setMessage(AspectJUIPlugin
							.getResourceString("NewAspectCreationWizardPage.InterfacesDialog.message")); //$NON-NLS-1$
			dialog.open();
		} else if (event.widget == removeInterfaceButton) {
			String[] selected = interfaceList.getSelection();
			for (int i = 0; i < selected.length; i++) {
				interfaceList.remove(selected[i]);
			}
		} else if (event.widget == enclosingBrowseButton) {
			IType type = chooseEnclosingType();
			if (type != null) {
				enclosingText.setText(type.getFullyQualifiedName());
				updateEnclosingStatus();
			}
		} else if (event.widget == instantiationButton) {
			boolean iset = instantiationButton.getSelection();
			instSingletonButton.setEnabled(iset);
			instPerthisButton.setEnabled(iset);
			instPertargetButton.setEnabled(iset);
			instPercflowButton.setEnabled(iset);
			instPercflowbelowButton.setEnabled(iset);
			instPertypewithinButton.setEnabled(iset);
		} else if (event.widget == supertypeBrowseButton) {
			IType type = chooseSuperType();
			if (type != null) {
				extendsText.setText(type.getFullyQualifiedName());
			}
		}

		applyToStatusLine(mostSevereStatus());
		getWizard().getContainer().updateButtons();
	}

	private IType findType(IJavaProject project, String typeName)
			throws JavaModelException {
		if (project.exists()) {
			return project.findType(typeName);
		}
		return null;
	}

	private IStatus mostSevereStatus() {
		IStatus s_error = severityMatch(IStatus.ERROR);
		if (s_error != null) {
			return s_error;
		}
		IStatus s_warn = severityMatch(IStatus.WARNING);
		if (s_warn != null) {
			return s_warn;
		}
		IStatus s_info = severityMatch(IStatus.INFO);
		if (s_info != null) {
			return s_info;
		}
		return okStatus;
	}

	private IStatus severityMatch(int sev) {
		for (int i = 0; i < statusArray.length; i++) {
			if (statusArray[i].matches(sev)) {
				return statusArray[i];
			}
		}
		return null;
	}

	/**
	 * Applies the status to the status line of a dialog page.
	 */
	private void applyToStatusLine(IStatus status) {
		String message = status.getMessage();
		if (message.length() == 0) {
			message = null;
		}
		switch (status.getSeverity()) {
			case IStatus.OK :
				setErrorMessage(null);
				setMessage(message);
				break;
			case IStatus.WARNING :
				setErrorMessage(null);
				setMessage(message, WizardPage.WARNING);
				break;
			case IStatus.INFO :
				setErrorMessage(null);
				setMessage(message, WizardPage.INFORMATION);
				break;
			default :
				setErrorMessage(message);
				setMessage(null);
				break;
		}
	}

	/**
	 * Returns the type name entered into the type input field.
	 * 
	 * @return the type name
	 */
	public String getTypeName() {
		return nameText.getText();
	}

	private IType chooseEnclosingType() {
		IPackageFragmentRoot root = getPackageFragmentRoot();
		if (root == null) {
			return null;
		}

		IJavaSearchScope scope = SearchEngine
				.createJavaSearchScope(new IJavaElement[]{root});

		TypeSelectionDialog dialog = new TypeSelectionDialog(getShell(),
				getWizard().getContainer(), IJavaSearchConstants.TYPE, scope);
		dialog
				.setTitle(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.ChooseEnclosingTypeDialog.title")); //$NON-NLS-1$
		dialog
				.setMessage(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.ChooseEnclosingTypeDialog.description")); //$NON-NLS-1$
		//dialog.setFilter(Signature.getSimpleName(getEnclosingTypeText()));

		if (dialog.open() == TypeSelectionDialog.OK) {
			return (IType) dialog.getFirstResult();
		}
		return null;
	}

	private IPackageFragmentRoot chooseSourceContainer(IJavaElement initElement) {
		Class[] acceptedClasses = new Class[]{IJavaModel.class,
				IPackageFragmentRoot.class, IJavaProject.class};
		ViewerFilter filter = new TypedViewerFilter(acceptedClasses) {
			public boolean select(Viewer viewer, Object parent, Object element) {
				if (element instanceof IPackageFragmentRoot) {
					try {
						return (((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE);
					} catch (JavaModelException e) {
						return false;
					}
				}
				return super.select(viewer, parent, element);
			}
		};

		StandardJavaElementContentProvider provider = new StandardJavaElementContentProvider();
		ILabelProvider labelProvider = new JavaElementLabelProvider(
				JavaElementLabelProvider.SHOW_DEFAULT);
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
				getShell(), labelProvider, provider);
		//dialog.setValidator(validator);
		dialog.setSorter(new JavaElementSorter());
		dialog
				.setTitle(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.FolderSelectionDialog.title")); //$NON-NLS-1$
		dialog
				.setMessage(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.FolderSelectionDialog.message")); //$NON-NLS-1$
		dialog.addFilter(filter);
		dialog.setInput(JavaCore.create(ResourcesPlugin.getWorkspace()
				.getRoot()));
		dialog.setInitialSelection(initElement);

		if (dialog.open() == ElementTreeSelectionDialog.OK) {
			Object element = dialog.getFirstResult();
			if (element instanceof IJavaProject) {
				IJavaProject jproject = (IJavaProject) element;
				return jproject.getPackageFragmentRoot(jproject.getProject());
			} else if (element instanceof IPackageFragmentRoot) {
				return (IPackageFragmentRoot) element;
			}
			return null;
		}
		return null;
	}

	private IPackageFragment choosePackage() {
		IPackageFragmentRoot froot = getPackageFragmentRoot();
		IJavaElement[] packages = null;
		try {
			if (froot != null && froot.exists()) {
				packages = froot.getChildren();
			}
		} catch (JavaModelException e) {
		}
		if (packages == null) {
			packages = new IJavaElement[0];
		}

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getShell(), new JavaElementLabelProvider(
						JavaElementLabelProvider.SHOW_DEFAULT));
		dialog.setIgnoreCase(false);
		dialog
				.setTitle(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.ChoosePackageDialog.title")); //$NON-NLS-1$
		dialog
				.setMessage(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.ChoosePackageDialog.message")); //$NON-NLS-1$
		dialog
				.setEmptyListMessage(AspectJUIPlugin
						.getResourceString("NewAspectCreationWizardPage.ChoosePackageDialog.empty")); //$NON-NLS-1$
		dialog.setElements(packages);
		IPackageFragment pack = getPackageFragment();
		if (pack != null) {
			dialog.setInitialSelections(new Object[]{pack});
		} 	
		
		if (dialog.open() == ElementListSelectionDialog.OK) {
			return (IPackageFragment) dialog.getFirstResult();
		}
		return null;
	}

	 private IType chooseSuperType() {
		IPackageFragmentRoot root = getPackageFragmentRoot();
		if (root == null) {
			return null;
		}

		IJavaElement[] elements = new IJavaElement[] { root.getJavaProject() };
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elements);

		org.eclipse.ajdt.internal.ui.dialogs.TypeSelectionDialog dialog = new org.eclipse.ajdt.internal.ui.dialogs.TypeSelectionDialog(getShell(),
				getWizard().getContainer(), IJavaSearchConstants.CLASS, scope);
		dialog.setTitle(AspectJUIPlugin
				.getResourceString("NewTypeWizardPage.SuperClassDialog.title")); //$NON-NLS-1$
		dialog.setMessage(AspectJUIPlugin
				.getResourceString("NewTypeWizardPage.SuperClassDialog.message")); //$NON-NLS-1$
		//dialog.setFilter(getSuperClass());

		if (dialog.open() == Window.OK) {
			return (IType) dialog.getFirstResult();
		}
		return null;
	}
	 
	private IPackageFragment getPackageFragment() {
		return fCurrRoot.getPackageFragment(packageText.getText());
	}

	/**
	 * Returns the enclosing type corresponding to the current input.
	 * 
	 * @return the enclosing type or <code>null</code> if the enclosing type
	 *         is not selected or the input could not be resolved
	 */
	public IType getEnclosingType() {
		if (enclosingTypeButton.getSelection()) {
			return fCurrEnclosingType;
		}
		return null;
	}

	public IPackageFragmentRoot getPackageFragmentRoot() {
		return fCurrRoot;
	}

	/**
	 * Creates a new file resource as requested by the user. If everything is OK
	 * then answer true. If not, false will cause the dialog to stay open.
	 * 
	 * @return whether creation was successful
	 *  
	 */
	public boolean finish() {
		if (enclosingTypeButton.getSelection()) {
			createInnerType();
			return true;
		}

		// create the new file resource
		IFile newFile = createNewFile();

		if (newFile == null) {
			return false; // ie.- creation was unsuccessful
		}

		InputStream initialContents = getInitialContents();
		createType(newFile, initialContents);

		// Open file for editing
		try {
			IWorkbenchWindow dwindow = workbench.getActiveWorkbenchWindow();
			IWorkbenchPage page = dwindow.getActivePage();
			if (page != null) {
				IDE.openEditor(page, newFile, true);
			}
		} catch (PartInitException e) {
			return false;
		}

		BasicNewResourceWizard.selectAndReveal(newFile, workbench
				.getActiveWorkbenchWindow());
		AJLog.log("New aspect file created: " + newFile.getName()); //$NON-NLS-1$

		return true;
	}

	private IFile createNewFile() {
		String sourceName = sourceText.getText();
		IPath path = new Path(sourceName);
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IResource res = workspaceRoot.findMember(path);

		IProject proj = res.getProject();

		IResource pack = null;
		String packName = packageText.getText();

		if (res.getType() == IResource.FOLDER) {
			IFolder folder = (IFolder) res;
			pack = folder.findMember(packName);
		} else if (res.getType() == IResource.PROJECT) {
			pack = ((IProject) res).findMember(packName);
		} else {
			return null;
		}

		if (pack == null) { // need to create a new package
			IJavaProject jproject = JavaCore.create(proj);
			IPackageFragmentRoot root = jproject.getPackageFragmentRoot(res);
			try {
				IPackageFragment frag = root.createPackageFragment(packName,
						true, null);
				pack = frag.getResource();
			} catch (JavaModelException e) {
			}
		}

		String aspectName = getTypeName();
		String extName = AspectJPreferences.getFileExt();
		IPath newpath = pack.getFullPath().append(aspectName + extName);
		IFile newfile = workspaceRoot.getFile(newpath);

		if (extName.equals(".java")) { // we've already got a .java file handle //$NON-NLS-1$
			dotjavaFile = newfile;
		} else { // need to create a dummy file handle with a .java extension
			// instead of .aj
			IPath dummyPath = newpath.removeFileExtension().addFileExtension(
					"java"); //$NON-NLS-1$
			dotjavaFile = workspaceRoot.getFile(dummyPath);
		}

		return newfile;
	}

	protected InputStream getInitialContents() {
		String contents = constructTypeStub();
		String sb = doTemplate(contents, lineDelimiter);
		return new ByteArrayInputStream(sb.getBytes());
	}

	private String getInnerTypeContents(ICompilationUnit cu, int indent) {
		StringBuffer content = new StringBuffer();
		content.append(lineDelimiter);
		String comment = getTypeComment(cu);
		if (comment != null) {
			content.append(comment);
			content.append(lineDelimiter);
		}
		content.append(constructTypeStub());

		return codeFormat(content.toString(), indent,
				lineDelimiter);
	}

	private void createInnerType() {
		try {
			IType enclosingType = getEnclosingType();
			IFile enclosingFile = null;
			if (enclosingType.getResource().getType() == IResource.FILE) {
				enclosingFile = (IFile) (enclosingType.getResource());
			}

			ICompilationUnit parentCU = enclosingType.getCompilationUnit();

			IEditorPart editor = JavaUI.openInEditor(parentCU);
			IEditorInput input = editor.getEditorInput();
			IWorkingCopyManager manager = JavaUI.getWorkingCopyManager();
			manager.connect(input);
			try {
				ICompilationUnit workingCopy = manager.getWorkingCopy(input);
				// do the modifications on workingCopy
				IBuffer buf = workingCopy.getBuffer();
				String originalContent = buf.getContents();
				int pos = NewAspectUtils.getInnerInsertionPoint(
						originalContent, enclosingType.getElementName());
				int indent = NewAspectUtils.getIndentUsed(enclosingType) + 1;
				String formattedContent = getInnerTypeContents(workingCopy,
						indent);
				buf.replace(pos, 0, formattedContent);

				// insert import statements if required
				String importContent = imports.getImports();
				if (importContent.length() > 0) {
					int importPos = NewAspectUtils
							.getInnerImportsInsertionPoint(originalContent
									.substring(0, pos));
					buf.replace(importPos, 0, lineDelimiter + importContent);
				}
			} finally {
				manager.disconnect(input);
			}

			AJLog.log("New aspect file created: " + enclosingFile.getName()); //$NON-NLS-1$
		} catch (CoreException e) {
		}

	}

	public boolean createType(final IFile newFile,
			final InputStream initialContents) {
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor)
					throws CoreException, InterruptedException {
				try {
					monitor
							.beginTask(
									AspectJUIPlugin
											.getResourceString("NewAspectCreationWizardPage.CreatingAspect.message"), 2000); //$NON-NLS-1$
					newFile.create(initialContents, false, monitor);
				} catch (CoreException e) {
				} finally {
					monitor.done();
				}
			}
		};

		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return true;
		} catch (InvocationTargetException e) {
			return true;
		}
		return true;
	}

	private String doTemplate(String typeContent, String lineDelimiter) {
		// We need an ICompilationUnit in order to use the template
		// functionality from
		// JDT, but the .java file doesn't need to actually exist
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(dotjavaFile);

		try {
			String imp = imports.getImports();
			String typeComment = imp + getTypeComment(cu);
			String content = CodeGeneration.getCompilationUnitContent(cu,
					typeComment, typeContent, lineDelimiter);
			return content;
		} catch (CoreException e) {
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Hook method that gets called from <code>createType</code> to retrieve a
	 * type comment. This default implementation returns the content of the
	 * 'typecomment' template.
	 * 
	 * @return the type comment or <code>null</code> if a type comment is not
	 *         desired
	 */
	protected String getTypeComment(ICompilationUnit parentCU) {
		if (PreferenceConstants.getPreferenceStore().getBoolean(
				PreferenceConstants.CODEGEN_ADD_COMMENTS)) {
			try {
				StringBuffer typeName = new StringBuffer();
				if (enclosingTypeButton.getSelection()) {
					typeName.append(
							JavaModelUtil
									.getTypeQualifiedName(getEnclosingType()))
							.append('.');
				}
				typeName.append(getTypeName());
				String comment = CodeGeneration.getTypeComment(parentCU,
						typeName.toString(), String.valueOf('\n'));
				if (comment != null && isValidComment(comment)) {
					return comment;
				}
			} catch (CoreException e) {
			}
		}
		return ""; //$NON-NLS-1$
	}

	private boolean isValidComment(String template) {
		IScanner scanner = ToolFactory.createScanner(true, false, false, false);
		scanner.setSource(template.toCharArray());
		try {
			int next = scanner.getNextToken();
			while (next == ITerminalSymbols.TokenNameCOMMENT_LINE
					|| next == ITerminalSymbols.TokenNameCOMMENT_JAVADOC
					|| next == ITerminalSymbols.TokenNameCOMMENT_BLOCK) {
				next = scanner.getNextToken();
			}
			return next == ITerminalSymbols.TokenNameEOF;
		} catch (InvalidInputException e) {
			// can be ignored
		}
		return false;
	}

	private String getInterfaceString() {
		String[] interfaces = interfaceList.getItems();
		if (interfaces.length == 0) {
			return ""; //$NON-NLS-1$
		}
		StringBuffer sb = new StringBuffer();
		sb.append("implements "); //$NON-NLS-1$
		for (int i = 0; i < interfaces.length; i++) {
			sb.append(imports.addImport(interfaces[i]));
			if (i < interfaces.length - 1) {
				sb.append(", "); //$NON-NLS-1$
			}
		}
		sb.append(" "); //$NON-NLS-1$
		return sb.toString();
	}

	private String getInstantiationString() {
		String s = ""; //$NON-NLS-1$
		if (instantiationButton.getSelection()) {
			if (instSingletonButton.getSelection()) {
				s = "issingleton() "; //$NON-NLS-1$
			} else if (instPerthisButton.getSelection()) {
				s = "perthis(pointcut_name()) "; //$NON-NLS-1$
			} else if (instPertargetButton.getSelection()) {
				s = "pertarget(pointcut_name()) "; //$NON-NLS-1$
			} else if (instPercflowButton.getSelection()) {
				s = "percflow(pointcut_name()) "; //$NON-NLS-1$
			} else if (instPercflowbelowButton.getSelection()) {
				s = "percflowbelow(pointcut_name()) "; //$NON-NLS-1$
			} else if (instPertypewithinButton.getSelection()) {
				s = "pertypewithin(type_pattern) "; //$NON-NLS-1$
			}
		}
		return s;
	}
	
	private String constructTypeStub() {
		imports = new ImportManager();
		StringBuffer sb = new StringBuffer();

		//The template adds the package statement, using the ICompilationUnit
		//if (isTextNonEmpty(packageText)) {
		//	sb.append("package "+packageText.getText()+";\n\n");
		//}
		if (modPublicButton.getSelection()) {
			sb.append("public "); //$NON-NLS-1$
		}
		if (modAbstractButton.getSelection()) {
			sb.append("abstract "); //$NON-NLS-1$
		}
		if (modFinalButton.getSelection()) {
			sb.append("final "); //$NON-NLS-1$
		}
		if (modPrivilegedButton.getSelection()) {
			sb.append("privileged "); //$NON-NLS-1$
		}
		if (modStaticButton.getSelection()) {
			sb.append("static "); //$NON-NLS-1$
		}

		sb.append("aspect " + getTypeName() + " " + getInstantiationString()); //$NON-NLS-1$ //$NON-NLS-2$
		if (NewAspectUtils.isTextNonEmpty(extendsText)) {
			String extText = extendsText.getText();
			if (!extText.equals("java.lang.Object")) { //$NON-NLS-1$
				sb.append("extends " + imports.addImport(extText) + " "); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		sb.append(getInterfaceString());
		sb.append("{"); //$NON-NLS-1$
		sb.append(lineDelimiter);
		if (createMainButton.getSelection()) {
			sb.append(lineDelimiter);
			sb.append("\tpublic static void main(String[] args) {"); //$NON-NLS-1$
			sb.append(lineDelimiter);
			sb.append("\t}"); //$NON-NLS-1$
		}
		sb.append(lineDelimiter);
		sb.append("}"); //$NON-NLS-1$
		sb.append(lineDelimiter);

		return sb.toString();
	}

	private String codeFormat(String sourceString, int initialIndentationLevel,
			String lineDelim) {
		return CodeFormatterUtil.format(
				CodeFormatter.K_CLASS_BODY_DECLARATIONS, sourceString,
				initialIndentationLevel, null, lineDelim, jproject);
	}

	public static class NewAspectUtils {

		public static void createBlank(Composite parent) {
			new Label(parent, SWT.NONE);
		}

		public static void createLine(Composite parent, int ncol) {
			Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL
					| SWT.BOLD);
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = ncol;
			line.setLayoutData(gridData);
		}

		public static boolean isTextNonEmpty(Text t) {
			String s = t.getText();
			if ((s != null) && (s.trim().length() > 0))
				return true;
			return false;
		}

		/**
		 * Returns a width hint for a button control.
		 */
		public static int getButtonWidthHint(Button button) {
			if (button.getFont().equals(JFaceResources.getDefaultFont())) {
				button.setFont(JFaceResources.getDialogFont());
			}
			GC gc = new GC(button);
			gc.setFont(button.getFont());
			FontMetrics fFontMetrics = gc.getFontMetrics();
			gc.dispose();
			int widthHint = Dialog.convertHorizontalDLUsToPixels(fFontMetrics,
					IDialogConstants.BUTTON_WIDTH);
			return Math.max(widthHint, button.computeSize(SWT.DEFAULT,
					SWT.DEFAULT, true).x);
		}

		/**
		 * Utility method to inspect a selection to find a Java element.
		 * 
		 * @param selection
		 *            the selection to be inspected
		 * @return a Java element to be used as the initial selection, or
		 *         <code>null</code>, if no Java element exists in the given
		 *         selection
		 */
		protected static IJavaElement getInitialJavaElement(
				IStructuredSelection selection) {
			IJavaElement jelem = null;
			if (selection != null && !selection.isEmpty()) {
				Object selectedElement = selection.getFirstElement();
				if (selectedElement instanceof IAdaptable) {
					IAdaptable adaptable = (IAdaptable) selectedElement;

					jelem = (IJavaElement) adaptable
							.getAdapter(IJavaElement.class);
					if (jelem == null) {
						IResource resource = (IResource) adaptable
								.getAdapter(IResource.class);
						if (resource != null
								&& resource.getType() != IResource.ROOT) {
							while (jelem == null
									&& resource.getType() != IResource.PROJECT) {
								resource = resource.getParent();
								jelem = (IJavaElement) resource
										.getAdapter(IJavaElement.class);
							}
							if (jelem == null) {
								jelem = JavaCore.create(resource);
								// java project
							}
						}
					}
				}
			}
			if (jelem == null
					|| jelem.getElementType() == IJavaElement.JAVA_MODEL) {
				try {
					IJavaProject[] projects = JavaCore.create(
							ResourcesPlugin.getWorkspace().getRoot())
							.getJavaProjects();
					if (projects.length == 1) {
						jelem = projects[0];
					}
				} catch (JavaModelException e) {
				}
			}
			return jelem;
		}

		public static int getTabWidth() {
			try {
				return Integer.parseInt((String) JavaCore.getOptions().get(
						DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE));
			} catch (NumberFormatException e) {
				return 4;
			}
		}

		/**
		 * Evaluates the indention used by a Java element. (in tabulators)
		 */
		public static int getIndentUsed(IJavaElement elem)
				throws JavaModelException {
			if (elem instanceof ISourceReference) {
				ICompilationUnit cu = (ICompilationUnit) elem
						.getAncestor(IJavaElement.COMPILATION_UNIT);
				if (cu != null) {
					IBuffer buf = cu.getBuffer();
					int offset = ((ISourceReference) elem).getSourceRange()
							.getOffset();
					int i = offset;
					// find beginning of line
					while (i > 0 && !isLineDelimiterChar(buf.getChar(i - 1))) {
						i--;
					}
					return computeIndent(buf.getText(i, offset - i),
							getTabWidth());
				}
			}
			return 0;
		}

		/**
		 * Line delimiter chars are '\n' and '\r'.
		 */
		public static boolean isLineDelimiterChar(char ch) {
			return ch == '\n' || ch == '\r';
		}

		/**
		 * Indent char is a space char but not a line delimiters.
		 * <code>== Character.isWhitespace(ch) && ch != '\n' && ch != '\r'</code>
		 */
		public static boolean isIndentChar(char ch) {
			return Character.isWhitespace(ch) && !isLineDelimiterChar(ch);
		}

		/**
		 * Returns the indent of the given string.
		 * 
		 * @param line
		 *            the text line
		 * @param tabWidth
		 *            the width of the '\t' character.
		 */
		public static int computeIndent(String line, int tabWidth) {
			int result = 0;
			int blanks = 0;
			int size = line.length();
			for (int i = 0; i < size; i++) {
				char c = line.charAt(i);
				if (c == '\t') {
					result++;
					blanks = 0;
				} else if (isIndentChar(c)) {
					blanks++;
					if (blanks == tabWidth) {
						result++;
						blanks = 0;
					}
				} else {
					return result;
				}
			}
			return result;
		}

		/*
		 * Determine insertion point for our new aspect in enclosing type
		 */
		public static int getInnerInsertionPoint(String contents, String name) {
			char prev = 0;
			boolean inShortComment = false;
			boolean inLongComment = false;
			for (int i = 0; i < contents.length(); i++) {
				char c = contents.charAt(i);
				if (prev != 0) {
					if (prev == '/' && c == '*') {
						inLongComment = true;
					}
					if (!inLongComment && (prev == '/' && c == '/')) {
						inShortComment = true;
					}
					if (!inShortComment && !inLongComment) {
						if (contents.regionMatches(i, name, 0, name.length())) {
							// now search for an opening brace
							int pos = contents.indexOf("{", i + name.length()); //$NON-NLS-1$
							if (pos != -1) {
								return pos + 1;
							}
						}
					}
					if (prev == '*' && c == '/') {
						inLongComment = false;
					} else if (inShortComment && c == '\n') {
						inShortComment = false;
					}
				}
				prev = c;
			}
			return 0;
		}

		/*
		 * Determine insertion point for import statements in our new inner
		 * aspect
		 */
		public static int getInnerImportsInsertionPoint(String contents) {
			char prev = 0;
			boolean inShortComment = false;
			boolean inLongComment = false;
			String importStatement = "import"; //$NON-NLS-1$
			String packageStatement = "package"; //$NON-NLS-1$
			int lastImportPos = -1;
			int packagePos = -1;

			for (int i = 0; i < contents.length(); i++) {
				char c = contents.charAt(i);
				if (prev != 0) {
					if (prev == '/' && c == '*') {
						inLongComment = true;
					}
					if (!inLongComment && (prev == '/' && c == '/')) {
						inShortComment = true;
					}
					if (!inShortComment && !inLongComment) {
						if (contents.regionMatches(i, importStatement, 0,
								importStatement.length())) {
							lastImportPos = i;
						} else if (contents.regionMatches(i, packageStatement,
								0, packageStatement.length())) {
							packagePos = i;
						}
					}
					if (prev == '*' && c == '/') {
						inLongComment = false;
					} else if (inShortComment && c == '\n') {
						inShortComment = false;
					}
				}
				prev = c;
			}
			if (lastImportPos == -1) {
				if (packagePos != -1) {
					// no import statements found, insert after package
					// statement
					int p = contents.indexOf(';', packagePos);
					if (p >= 0) {
						return p + 1;
					}
				}
			} else {
				// insert after last import statement
				int p = contents.indexOf(';', lastImportPos);
				if (p >= 0) {
					return p + 1;
				}
			}
			// if we haven't found a good place, use the beginning - should
			// still be lexically correct
			return 0;
		}

	}

	class ImportManager {
		private java.util.List importList = new ArrayList();

		public String addImport(String type) {
			int ind = type.lastIndexOf("."); //$NON-NLS-1$
			if (ind == -1) {
				return type;
			}
			String pack = type.substring(0, ind + 1);
			if (!pack.equals("java.lang.")) { //$NON-NLS-1$
				importList.add(type);
			}
			String name = type.substring(ind + 1);
			return name;
		}

		public String getImports() {
			if (importList.size() == 0) {
				return ""; //$NON-NLS-1$
			}
			StringBuffer sb = new StringBuffer();
			for (Iterator i = importList.iterator(); i.hasNext();) {
				sb.append("import "); //$NON-NLS-1$
				sb.append(i.next());
				sb.append(";"); //$NON-NLS-1$
				sb.append(lineDelimiter);
			}
			return sb.toString();
		}
	}

	class TypedViewerFilter extends ViewerFilter {

		private Class[] fAcceptedTypes;
		private Object[] fRejectedElements;

		/**
		 * Creates a filter that only allows elements of gives types.
		 * 
		 * @param acceptedTypes
		 *            The types of accepted elements
		 */
		public TypedViewerFilter(Class[] acceptedTypes) {
			this(acceptedTypes, null);
		}

		/**
		 * Creates a filter that only allows elements of gives types, but not
		 * from a list of rejected elements.
		 * 
		 * @param acceptedTypes
		 *            Accepted elements must be of this types
		 * @param rejectedElements
		 *            Element equals to the rejected elements are filtered out
		 */
		public TypedViewerFilter(Class[] acceptedTypes,
				Object[] rejectedElements) {
			Assert.isNotNull(acceptedTypes);
			fAcceptedTypes = acceptedTypes;
			fRejectedElements = rejectedElements;
		}

		/**
		 * @see ViewerFilter#select
		 */
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (fRejectedElements != null) {
				for (int i = 0; i < fRejectedElements.length; i++) {
					if (element.equals(fRejectedElements[i])) {
						return false;
					}
				}
			}
			for (int i = 0; i < fAcceptedTypes.length; i++) {
				if (fAcceptedTypes[i].isInstance(element)) {
					return true;
				}
			}
			return false;
		}

	}

	static class SuperInterfaceSelectionDialog extends TypeSelectionDialog {

		private static final int ADD_ID = IDialogConstants.CLIENT_ID + 1;

		private List fList;
		private String[] fOldContent;

		public SuperInterfaceSelectionDialog(Shell parent,
				IRunnableContext context, List list, IJavaProject p) {
			super(parent, context, IJavaSearchConstants.INTERFACE,
					createSearchScope(p));
			fList = list;
			// to restore the content of the dialog field if the dialog is
			// canceled
			fOldContent = fList.getItems();
			setStatusLineAboveButtons(true);
		}

		/*
		 * @see Dialog#createButtonsForButtonBar
		 */
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, ADD_ID, "Add", true); //$NON-NLS-1$
			super.createButtonsForButtonBar(parent);
		}

		/*
		 * @see Dialog#cancelPressed
		 */
		protected void cancelPressed() {
			fList.setItems(fOldContent);
			super.cancelPressed();
		}

		/*
		 * @see Dialog#buttonPressed
		 */
		protected void buttonPressed(int buttonId) {
			if (buttonId == ADD_ID) {
				addSelectedInterface();
			}
			super.buttonPressed(buttonId);
		}

		/*
		 * @see Dialog#okPressed
		 */
		protected void okPressed() {
			addSelectedInterface();
			super.okPressed();
		}

		private void addSelectedInterface() {
			Object ref = getLowerSelectedElement();
			if (ref instanceof TypeInfo) {
				String qualifiedName = ((TypeInfo) ref).getFullyQualifiedName();
				if (fList.indexOf(qualifiedName) == -1) {
					fList.add(qualifiedName);
				}
				updateStatus(new Status(
						IStatus.INFO,
						AspectJUIPlugin.PLUGIN_ID,
						0,
						AspectJUIPlugin
								.getResourceString("NewAspectCreationWizardPage.SuperInterfaceSelectionDialog.interfaceadded.info"), null)); //$NON-NLS-1$
			}
		}

		private static IJavaSearchScope createSearchScope(IJavaProject p) {
			return SearchEngine.createJavaSearchScope(new IJavaProject[]{p});
		}

		/*
		 * @see AbstractElementListSelectionDialog#handleDefaultSelected()
		 */
		protected void handleDefaultSelected() {
			if (validateCurrentSelection())
				buttonPressed(ADD_ID);
		}

		/*
		 * @see org.eclipse.jface.window.Window#configureShell(Shell)
		 */
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			//WorkbenchHelp.setHelp(newShell,
			// IJavaHelpContextIds.SUPER_INTERFACE_SELECTION_DIALOG);
		}

	}

}