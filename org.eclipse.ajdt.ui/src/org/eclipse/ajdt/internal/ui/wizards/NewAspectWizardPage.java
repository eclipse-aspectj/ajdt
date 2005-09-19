/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.wizards;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.PointcutElement;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class NewAspectWizardPage extends NewTypeWizardPage {

	private final static String PAGE_NAME= "NewAspectWizardPage"; //$NON-NLS-1$
	private final static String SETTINGS_CREATEMAIN= "create_main"; //$NON-NLS-1$
	private final static String SETTINGS_CREATEUNIMPLEMENTED= "create_unimplemented"; //$NON-NLS-1$

	private final int ISSINGLETON_INDEX= 0, PERTHIS_INDEX= 1, PERTARGET_INDEX= 2;
	private final int PERCFLOW_INDEX= 3, PERCFLOWBELOW_INDEX= 4, PERTYPEWITHIN_INDEX= 5;

	private SelectionButtonDialogFieldGroup fStubsButtons;
	
	private SelectionButtonDialogFieldGroup fPerClauseButtons;

	private SelectionButtonDialogField fPerClauseSelection;

	
	public NewAspectWizardPage() {
		super(false, true, PAGE_NAME);
		
		setTitle(AspectJUIPlugin.getResourceString("NewAspectCreationWizardPage.title")); //$NON-NLS-1$
		setDescription(AspectJUIPlugin.getResourceString("NewAspectCreationWizardPage.description")); //$NON-NLS-1$
		
		String[] buttonNames3= new String[] {
				NewWizardMessages.getString("NewClassWizardPage.methods.main"), //$NON-NLS-1$
				AspectJUIPlugin.getResourceString("NewAspectCreationWizardPage.pointcuts.inherited") //$NON-NLS-1$
			};		
		fStubsButtons= new SelectionButtonDialogFieldGroup(SWT.CHECK, buttonNames3, 1);
		fStubsButtons.setLabelText(AspectJUIPlugin.getResourceString("NewAspectCreationWizardPage.stubs.label")); //$NON-NLS-1$

		fPerClauseSelection= new SelectionButtonDialogField(SWT.CHECK);
		fPerClauseSelection.setLabelText(AspectJUIPlugin.getResourceString("NewAspectCreationWizardPage.instantiation.label"));  //$NON-NLS-1$

		String[] perClauseButtonNames= new String[] {
				"issingleton", //$NON-NLS-1$
				"perthis", //$NON-NLS-1$
				"pertarget", //$NON-NLS-1$
				"percflow", //$NON-NLS-1$
				"percflowbelow", //$NON-NLS-1$
				"pertypewithin" //$NON-NLS-1$
		};
		fPerClauseButtons= new SelectionButtonDialogFieldGroup(SWT.RADIO, perClauseButtonNames, 3);
		fPerClauseSelection.attachDialogField(fPerClauseButtons);
	}
	
	/**
	 * The wizard owning this page is responsible for calling this method with the
	 * current selection. The selection is used to initialize the fields of the wizard 
	 * page.
	 * 
	 * @param selection used to initialize the fields
	 */
	public void init(IStructuredSelection selection) {
		IJavaElement jelem= getInitialJavaElement(selection);
		initContainerPage(jelem);
		initTypePage(jelem);
		doStatusUpdate();
		
		boolean createMain= false;
		boolean createUnimplemented= true;
		IDialogSettings section= getDialogSettings().getSection(PAGE_NAME);
		if (section != null) {
			createMain= section.getBoolean(SETTINGS_CREATEMAIN);
			createUnimplemented= section.getBoolean(SETTINGS_CREATEUNIMPLEMENTED);
		}
		
		setMethodStubSelection(createMain, createUnimplemented, true);
	}
	
	// ------ validation --------
	private void doStatusUpdate() {
		// status of all used components
		IStatus[] status= new IStatus[] {
			fContainerStatus,
			isEnclosingTypeSelected() ? fEnclosingTypeStatus : fPackageStatus,
			fTypeNameStatus,
			fModifierStatus,
			fSuperClassStatus,
			fSuperInterfacesStatus
		};
		
		// the mode severe status will be displayed and the OK button enabled/disabled.
		updateStatus(status);
	}
	
	/**
	 * Hook method that gets called when the type name has changed. The method validates the 
	 * type name and returns the status of the validation.
	 * <p>
	 * We first delegate the validation to the superclass, then perform our own additional
	 * validation.
	 * </p>
	 * 
	 * @return the status of the validation
	 */
	protected IStatus typeNameChanged() {
		StatusInfo status = (StatusInfo) super.typeNameChanged();
		IPackageFragment pack = getPackageFragment();
		if (pack == null) {
			return status;
		}
		IProject project = pack.getJavaProject().getProject();
		if (!AspectJPlugin.isAJProject(project)) {
			status.setError(AspectJUIPlugin.getFormattedResourceString("NewAspectCreationWizardPage.must.be.AJ.project", //$NON-NLS-1$
					project.getName()));
		}
		// must not exist as a .aj file
		if (!isEnclosingTypeSelected()
				&& (status.getSeverity() < IStatus.ERROR)) {
			if (pack != null) {
				String typeName = getTypeNameWithoutParameters();
				IContainer folder = (IContainer) pack.getResource();
				if (folder != null) {
					IResource res = folder.findMember(typeName + ".aj"); //$NON-NLS-1$
					if (res != null) {
						status.setError(NewWizardMessages.getString("NewTypeWizardPage.error.TypeNameExists")); //$NON-NLS-1$
						return status;
					}
				}
			}
		}
		return status;
	}
	
	/*
	 * @see NewContainerWizardPage#handleFieldChanged
	 */
	protected void handleFieldChanged(String fieldName) {
		super.handleFieldChanged(fieldName);
		
		doStatusUpdate();
	}
	
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		
		int nColumns= 4;
		
		GridLayout layout= new GridLayout();
		layout.numColumns= nColumns;		
		composite.setLayout(layout);
		
		// pick & choose the wanted UI components
		
		createContainerControls(composite, nColumns);	
		createPackageControls(composite, nColumns);	
		createEnclosingTypeControls(composite, nColumns);
				
		createSeparator(composite, nColumns);
		
		createTypeNameControls(composite, nColumns);
		createModifierControls(composite, nColumns);
			
		createInstantiationControls(composite, nColumns);
		
		createSuperClassControls(composite, nColumns);
		createSuperInterfacesControls(composite, nColumns);
				
		createStubSelectionControls(composite, nColumns);
		
		setControl(composite);
			
		Dialog.applyDialogFont(composite);
	}

	/*
	 * @see WizardPage#becomesVisible
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			setFocus();
		}
	}

	/**
	 * Returns the current selection state of the 'Create Main' checkbox.
	 * 
	 * @return the selection state of the 'Create Main' checkbox
	 */
	public boolean isCreateMain() {
		return fStubsButtons.isSelected(0);
	}
	
	/**
	 * Returns the current selection state of the 'Create inherited abstract pointcuts' 
	 * checkbox.
	 * 
	 * @return the selection state of the 'Create inherited abstract pointcuts' checkbox
	 */
	public boolean isCreateInherited() {
		return fStubsButtons.isSelected(1);
	}	

	/**
	 * Sets the selection state of the method stub checkboxes.
	 * 
	 * @param createMain initial selection state of the 'Create Main' checkbox.
	 * @param createInherited initial selection state of the 'Create inherited abstract pointcuts' checkbox.
	 * @param canBeModified if <code>true</code> the method stub checkboxes can be changed by 
	 * the user. If <code>false</code> the buttons are "read-only"
	 */
	public void setMethodStubSelection(boolean createMain, boolean createInherited, boolean canBeModified) {
		fStubsButtons.setSelection(0, createMain);
		fStubsButtons.setSelection(1, createInherited);
		
		fStubsButtons.setEnabled(canBeModified);
	}
	
	private String getTypeNameWithoutParameters() {
		String typeNameWithParameters= getTypeName();
		int angleBracketOffset= typeNameWithParameters.indexOf('<');
		if (angleBracketOffset == -1) {
			return typeNameWithParameters;
		} else {
			return typeNameWithParameters.substring(0, angleBracketOffset);
		}
	}
	
	private void createInstantiationControls(Composite composite, int nColumns) {
		fPerClauseSelection.doFillIntoGrid(composite, 1);
		
		Control buttonGroup= fPerClauseButtons.getSelectionButtonsGroup(composite);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= nColumns - 2;
		buttonGroup.setLayoutData(gd);
		
		DialogField.createEmptySpace(composite);
	}

	private void createStubSelectionControls(Composite composite, int nColumns) {
		Control labelControl= fStubsButtons.getLabelControl(composite);
		LayoutUtil.setHorizontalSpan(labelControl, nColumns);
		
		DialogField.createEmptySpace(composite);
		
		Control buttonGroup= fStubsButtons.getSelectionButtonsGroup(composite);
		LayoutUtil.setHorizontalSpan(buttonGroup, nColumns - 1);	
	}
	
	// TODO: refactor to use PointcutUtilities
	private void createInheritedPointcuts(IType type, ImportsManager imports,
			IProgressMonitor monitor) {
		String supertype = getSuperClass();
		if (supertype.length() == 0) {
			return;
		}
		try {
			String[][] res = type.resolveType(supertype);
			if ((res == null) || (res.length == 0)) {
				return;
			}
			String simpleName = res[0][1];
			IType stype = type.getJavaProject().findType(
					res[0][0] + "." + simpleName); //$NON-NLS-1$
			if (stype == null) {
				return;
			}
			ICompilationUnit cu = stype.getCompilationUnit();
			if (cu instanceof AJCompilationUnit) {
				AJCompilationUnit ajcu = (AJCompilationUnit) cu;
				IType[] stypes = ajcu.getTypes();
				for (int i = 0; i < stypes.length; i++) {
					if (stypes[i].getElementName().equals(simpleName)) {
						if (stypes[i] instanceof AspectElement) {
							AspectElement aspect = (AspectElement) stypes[i];
							PointcutElement[] pointcuts = aspect.getPointcuts();
							for (int j = 0; j < pointcuts.length; j++) {
								if (Flags.isAbstract(pointcuts[j].getFlags())) {
									String str = "pointcut " //$NON-NLS-1$
											+ pointcuts[j].getElementName()
											+ "();"; //$NON-NLS-1$
									if (Flags.isPublic(pointcuts[j].getFlags())) {
										str = "public " + str; //$NON-NLS-1$
									} else if (Flags.isProtected(pointcuts[j]
											.getFlags())) {
										str = "protected " + str; //$NON-NLS-1$
									}
									type.createMethod(str, null, false, null);
								}
							}
						}
					}
				}
			}
		} catch (JavaModelException e) {
		}
	}
	
	/*
	 * @see NewTypeWizardPage#createTypeMembers
	 */
	protected void createTypeMembers(IType type, ImportsManager imports, IProgressMonitor monitor) throws CoreException {
		boolean doInherited = isCreateInherited();
		if (doInherited) {
			createInheritedPointcuts(type, imports, new SubProgressMonitor(monitor, 1));
		}
		boolean doMain = isCreateMain();
		if (doMain) {
			StringBuffer buf= new StringBuffer();
			final String lineDelim= "\n"; // OK, since content is formatted afterwards //$NON-NLS-1$
			String comment= CodeGeneration.getMethodComment(type.getCompilationUnit(), type.getTypeQualifiedName('.'), "main", new String[] {"args"}, new String[0], Signature.createTypeSignature("void", true), null, lineDelim); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (comment != null) {
				buf.append(comment);
				buf.append(lineDelim);
			}
			buf.append("public static void main("); //$NON-NLS-1$
			buf.append(imports.addImport("java.lang.String")); //$NON-NLS-1$
			buf.append("[] args) {"); //$NON-NLS-1$
			buf.append(lineDelim);
			final String content= CodeGeneration.getMethodBodyContent(type.getCompilationUnit(), type.getTypeQualifiedName('.'), "main", false, "", lineDelim); //$NON-NLS-1$ //$NON-NLS-2$
			if (content != null && content.length() != 0)
				buf.append(content);
			buf.append(lineDelim);
			buf.append("}"); //$NON-NLS-1$
			type.createMethod(buf.toString(), null, false, null);
		}
		
		IDialogSettings section= getDialogSettings().getSection(PAGE_NAME);
		if (section == null) {
			section= getDialogSettings().addNewSection(PAGE_NAME);
		}
		section.put(SETTINGS_CREATEMAIN, doMain);
		section.put(SETTINGS_CREATEUNIMPLEMENTED, doInherited);
		
		if (monitor != null) {
			monitor.done();
		}	
	}

	protected void writePerClause(StringBuffer buf) {
		String s = ""; //$NON-NLS-1$
		if (fPerClauseSelection.isSelected()) {
			if (fPerClauseButtons.isSelected(ISSINGLETON_INDEX)) {
				s = " issingleton()"; //$NON-NLS-1$
			} else if (fPerClauseButtons.isSelected(PERTHIS_INDEX)) {
				s = " perthis(pointcut_name())"; //$NON-NLS-1$
			} else if (fPerClauseButtons.isSelected(PERTARGET_INDEX)) {
				s = " pertarget(pointcut_name())"; //$NON-NLS-1$
			} else if (fPerClauseButtons.isSelected(PERCFLOW_INDEX)) {
				s = " percflow(pointcut_name())"; //$NON-NLS-1$
			} else if (fPerClauseButtons.isSelected(PERCFLOWBELOW_INDEX)) {
				s = " percflowbelow(pointcut_name())"; //$NON-NLS-1$
			} else if (fPerClauseButtons.isSelected(PERTYPEWITHIN_INDEX)) {
				s = " pertypewithin(type_pattern)"; //$NON-NLS-1$
			}
		}
		buf.append(s);
	}

}
