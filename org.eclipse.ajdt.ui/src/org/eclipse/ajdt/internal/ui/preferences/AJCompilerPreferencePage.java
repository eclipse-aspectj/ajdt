/**********************************************************************
 Copyright (c) 2003, 2004 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 Contributors:
 Matt Chapman - initial version
 Ian McGrath - Modified to facilitate access from the project properties page
 ...
 **********************************************************************/

package org.eclipse.ajdt.internal.ui.preferences;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.core.EclipseVersion;
import org.eclipse.ajdt.internal.ui.wizards.TabFolderLayout;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The AspectJ compiler preferences that appear under the "AspectJ" category
 * under Workbench->preferences. The policy for preferences in AspectJ mode is
 * to use the Java mode preference where it exists, and only the AspectJ
 * specific preferences in this page.
 */
public class AJCompilerPreferencePage extends PreferencePage 
		implements
			IWorkbenchPreferencePage {

	private static final String PREF_AJ_INVALID_ABSOLUTE_TYPE_NAME = AspectJPreferences.OPTION_ReportInvalidAbsoluteTypeName;
	private static final String PREF_AJ_SHADOW_NOT_IN_STRUCTURE = AspectJPreferences.OPTION_ReportShadowNotInStructure;
	private static final String PREF_AJ_CANNOT_IMPLEMENT_LAZY_TJP = AspectJPreferences.OPTION_ReportCannotImplementLazyTJP;
	private static final String PREF_AJ_INVALID_WILDCARD_TYPE_NAME = AspectJPreferences.OPTION_ReportInvalidWildcardTypeName;
	private static final String PREF_AJ_TYPE_NOT_EXPOSED_TO_WEAVER = AspectJPreferences.OPTION_ReportTypeNotExposedToWeaver;
	private static final String PREF_AJ_UNRESOLVABLE_MEMBER = AspectJPreferences.OPTION_ReportUnresolvableMember;
	private static final String PREF_AJ_UNMATCHED_SUPER_TYPE_IN_CALL = AspectJPreferences.OPTION_ReportUnmatchedSuperTypeInCall;
	private static final String PREF_AJ_INCOMPATIBLE_SERIAL_VERSION = AspectJPreferences.OPTION_ReportIncompatibleSerialVersion;
	private static final String PREF_AJ_NEED_SERIAL_VERSION_UID_FIELD = AspectJPreferences.OPTION_ReportNeedSerialVersionUIDField;
	private static final String PREF_AJ_NO_INTERFACE_CTOR_JOINPOINT = AspectJPreferences.OPTION_ReportNoInterfaceCtorJoinpoint;

	private static final String PREF_ENABLE_NO_WEAVE = AspectJPreferences.OPTION_NoWeave;
	private static final String PREF_ENABLE_SERIALIZABLE_ASPECTS = AspectJPreferences.OPTION_XSerializableAspects;
	private static final String PREF_ENABLE_LAZY_TJP = AspectJPreferences.OPTION_XLazyThisJoinPoint;
	private static final String PREF_ENABLE_NO_INLINE = AspectJPreferences.OPTION_XNoInline;
	private static final String PREF_ENABLE_REWEAVABLE = AspectJPreferences.OPTION_XReweavable;
	private static final String PREF_ENABLE_REWEAVABLE_COMPRESS = AspectJPreferences.OPTION_XReweavableCompress;
	
	private static final String PREF_ENABLE_INCREMENTAL = AspectJPreferences.OPTION_Incremental;
	private static final String PREF_ENABLE_BUILD_ASM = AspectJPreferences.OPTION_BuildASM;
	private static final String PREF_ENABLE_WEAVE_MESSAGES = AspectJPreferences.OPTION_WeaveMessages;

	private static final String ERROR = JavaCore.ERROR;
	private static final String WARNING = JavaCore.WARNING;
	private static final String IGNORE = JavaCore.IGNORE;

	private static final String ENABLED = JavaCore.ENABLED;
	private static final String DISABLED = JavaCore.DISABLED;
	
	private Button noweaveButton, lazytjpButton, noinlineButton, reweaveButton, reweaveCompressButton; 
	
	protected List fComboBoxes;
	protected List fCheckBoxes;

	public AJCompilerPreferencePage() {
		super();
		setTitle("AspectJ Compiler");
		fCheckBoxes = new ArrayList();
		fComboBoxes = new ArrayList();
	}

	protected static class ControlData {
		private String fKey;
		private String[] fValues;

		public ControlData(String key, String[] values) {
			fKey = key;
			fValues = values;
		}

		public String getKey() {
			return fKey;
		}

		public String getValue(boolean selection) {
			int index = selection ? 0 : 1;
			return fValues[index];
		}

		public String getValue(int index) {
			return fValues[index];
		}

		public int getSelection(String value) {
			for (int i = 0; i < fValues.length; i++) {
				if (value.equals(fValues[i])) {
					return i;
				}
			}
			return 0;
		}
	}

	/**
	 * The default values used when the plugin is first installed or when
	 * "restore defaults" is clicked.
	 */
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(PREF_AJ_INVALID_ABSOLUTE_TYPE_NAME, WARNING);
		store.setDefault(PREF_AJ_SHADOW_NOT_IN_STRUCTURE, IGNORE);
		store.setDefault(PREF_AJ_CANNOT_IMPLEMENT_LAZY_TJP, WARNING);
		store.setDefault(PREF_AJ_INVALID_WILDCARD_TYPE_NAME, IGNORE);
		store.setDefault(PREF_AJ_TYPE_NOT_EXPOSED_TO_WEAVER, WARNING);
		store.setDefault(PREF_AJ_UNRESOLVABLE_MEMBER, WARNING);
		store.setDefault(PREF_AJ_UNMATCHED_SUPER_TYPE_IN_CALL, WARNING);
		store.setDefault(PREF_AJ_INCOMPATIBLE_SERIAL_VERSION, IGNORE);
		store.setDefault(PREF_AJ_NEED_SERIAL_VERSION_UID_FIELD, IGNORE);
		store.setDefault(PREF_AJ_NO_INTERFACE_CTOR_JOINPOINT, WARNING);

		store.setDefault(PREF_ENABLE_NO_WEAVE, false);
		store.setDefault(PREF_ENABLE_SERIALIZABLE_ASPECTS, false);
		store.setDefault(PREF_ENABLE_LAZY_TJP, false);
		store.setDefault(PREF_ENABLE_NO_INLINE, false);
		store.setDefault(PREF_ENABLE_REWEAVABLE, false);
		store.setDefault(PREF_ENABLE_REWEAVABLE_COMPRESS, false);
		
		store.setDefault(PREF_ENABLE_INCREMENTAL, true);
		store.setDefault(PREF_ENABLE_BUILD_ASM, true);
		store.setDefault(PREF_ENABLE_WEAVE_MESSAGES, false);
		
		store.setDefault(AspectJPreferences.OPTION_noJoinpointsForBridgeMethods, WARNING);
		store.setDefault(AspectJPreferences.OPTION_cantMatchArrayTypeOnVarargs, IGNORE);
		store.setDefault(AspectJPreferences.OPTION_enumAsTargetForDecpIgnored, WARNING);
		store.setDefault(AspectJPreferences.OPTION_annotationAsTargetForDecpIgnored, WARNING);

		store.setDefault(AspectJPreferences.OPTION_invalidTargetForAnnotation, WARNING);
		store.setDefault(AspectJPreferences.OPTION_elementAlreadyAnnotated, WARNING);
		store.setDefault(AspectJPreferences.OPTION_runtimeExceptionNotSoftened, WARNING);
		store.setDefault(AspectJPreferences.OPTION_adviceDidNotMatch, WARNING);

	}

	/**
	 * from IWorkbenchPreferencePage
	 */
	public void init(IWorkbench workbench) {
	}

	/**
	 * from IWorkbenchPreferencePage
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		TabFolder folder = new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite aspectjComposite = createMessagesTabContent(folder);
		TabItem item = new TabItem(folder, SWT.NONE);
		item
				.setText(AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.aj_messages.tabtitle")); //$NON-NLS-1$
		item.setControl(aspectjComposite);

		aspectjComposite = createAdvancedTabContent(folder);
		item = new TabItem(folder, SWT.NONE);
		item
				.setText(AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.aj_advanced.tabtitle")); //$NON-NLS-1$
		item.setControl(aspectjComposite);

		aspectjComposite = createOtherTabContent(folder);
		item = new TabItem(folder, SWT.NONE);
		item
				.setText(AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.aj_other.tabtitle")); //$NON-NLS-1$
		item.setControl(aspectjComposite);

		// AJ5 options do not apply to Eclipse 3.0
		if (!((EclipseVersion.MAJOR_VERSION == 3) && (EclipseVersion.MINOR_VERSION == 0))) {
			aspectjComposite = createAJ5TabContent(folder);
			item = new TabItem(folder, SWT.NONE);
			item
					.setText(AspectJUIPlugin
							.getResourceString("CompilerConfigurationBlock.aj_5.tabtitle")); //$NON-NLS-1$
			item.setControl(aspectjComposite);
		}
		
		return folder;
	}


	private Composite createMessagesTabContent(Composite folder) {
		String[] errorWarningIgnore = new String[]{ERROR, WARNING, IGNORE};

		String[] errorWarningIgnoreLabels = new String[]{
				AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.error"), //$NON-NLS-1$
				AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.warning"), //$NON-NLS-1$
				AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.ignore") //$NON-NLS-1$
		};

		int nColumns = 3;

		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;

		Composite composite = new Composite(folder, SWT.NULL);
		composite.setLayout(layout);

		Label description = new Label(composite, SWT.WRAP);
		description
				.setText(AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.aj_messages.description")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = nColumns;
		description.setLayoutData(gd);

		String label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_invalid_absolute_type_name.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_INVALID_ABSOLUTE_TYPE_NAME,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_invalid_wildcard_type_name.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_INVALID_WILDCARD_TYPE_NAME,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_unresolvable_member.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_UNRESOLVABLE_MEMBER,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_type_not_exposed_to_weaver.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_TYPE_NOT_EXPOSED_TO_WEAVER,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_shadow_not_in_structure.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_SHADOW_NOT_IN_STRUCTURE,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_unmatched_super_type_in_call.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_UNMATCHED_SUPER_TYPE_IN_CALL,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_cannot_implement_lazy_tjp.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_CANNOT_IMPLEMENT_LAZY_TJP,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_need_serial_version_uid_field.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_NEED_SERIAL_VERSION_UID_FIELD,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_incompatible_serial_version.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_INCOMPATIBLE_SERIAL_VERSION,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_no_interface_ctor_joinpoint.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_NO_INTERFACE_CTOR_JOINPOINT,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		return composite;
	}

	private Composite createAdvancedTabContent(Composite folder) {
		String[] enableDisableValues = new String[]{ENABLED, DISABLED};
		
		CheckBoxListener checkBoxListener = new CheckBoxListener();
		
		int nColumns = 3;

		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;

		Composite composite = new Composite(folder, SWT.NULL);
		composite.setLayout(layout);

		Label description = new Label(composite, SWT.WRAP);
		description
				.setText(AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.aj_advanced.description")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = nColumns;
		description.setLayoutData(gd);

		String label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_no_weave.label"); //$NON-NLS-1$
		noweaveButton = addCheckBox(composite, label, PREF_ENABLE_NO_WEAVE, enableDisableValues, 0);
		noweaveButton.addSelectionListener(checkBoxListener);
		
		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_x_serializable_aspects.label"); //$NON-NLS-1$
		addCheckBox(composite, label, PREF_ENABLE_SERIALIZABLE_ASPECTS,enableDisableValues, 0);

		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_x_lazy_tjp.label"); //$NON-NLS-1$
		lazytjpButton = addCheckBox(composite, label, PREF_ENABLE_LAZY_TJP,enableDisableValues, 0);

		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_x_no_inline.label"); //$NON-NLS-1$
		noinlineButton = addCheckBox(composite, label, PREF_ENABLE_NO_INLINE,enableDisableValues, 0);

		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_x_reweavable.label"); //$NON-NLS-1$
		reweaveButton = addCheckBox(composite, label, PREF_ENABLE_REWEAVABLE,enableDisableValues, 0);
		reweaveButton.addSelectionListener(checkBoxListener);

		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_x_reweavable_compress.label"); //$NON-NLS-1$
		reweaveCompressButton = addCheckBox(composite, label, PREF_ENABLE_REWEAVABLE_COMPRESS,enableDisableValues, 0);
		reweaveCompressButton.addSelectionListener(checkBoxListener);

		checkNoWeaveSelection();
		
		return composite;
	}

	private Composite createOtherTabContent(Composite folder) {
		String[] enableDisableValues = new String[]{ENABLED, DISABLED};
		
		int nColumns = 3;

		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;

		Composite composite = new Composite(folder, SWT.NULL);
		composite.setLayout(layout);

		Label description = new Label(composite, SWT.WRAP);
		description
				.setText(AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.aj_other.description")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = nColumns;
		description.setLayoutData(gd);

		String label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_enable_incremental.label"); //$NON-NLS-1$
		addCheckBox(composite, label, PREF_ENABLE_INCREMENTAL, enableDisableValues, 0, false);
		
		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_enable_build_asm.label"); //$NON-NLS-1$
		addCheckBox(composite, label, PREF_ENABLE_BUILD_ASM,enableDisableValues, 0, false);

		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_enable_weave_messages.label"); //$NON-NLS-1$
		addCheckBox(composite, label, PREF_ENABLE_WEAVE_MESSAGES,enableDisableValues, 0);

		checkNoWeaveSelection();
		
		return composite;
	}


	/**
	 * @param folder
	 * @return
	 */
	private Composite createAJ5TabContent(TabFolder folder) {
		String[] errorWarningIgnore = new String[]{ERROR, WARNING, IGNORE};

		String[] errorWarningIgnoreLabels = new String[]{
				AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.error"), //$NON-NLS-1$
				AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.warning"), //$NON-NLS-1$
				AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.ignore") //$NON-NLS-1$
		};

		int nColumns = 3;

		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;

		Composite composite = new Composite(folder, SWT.NULL);
		composite.setLayout(layout);

		Label description = new Label(composite, SWT.WRAP);
		description
				.setText(AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.aj_5.description")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = nColumns;
		//gd.widthHint= fPixelConverter.convertWidthInCharsToPixels(50);
		description.setLayoutData(gd);
	
		Label spacer = new Label(composite, SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan = nColumns;
		spacer.setLayoutData(gd);
		
		Label description2 = new Label(composite, SWT.WRAP);
		description2
				.setText(AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.aj_messages.description")); //$NON-NLS-1$
		GridData gd2 = new GridData();
		gd2.horizontalSpan = nColumns;
		description2.setLayoutData(gd2);
		
		String label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.noJoinpointsForBridgeMethods"); //$NON-NLS-1$
		addComboBox(composite, label, AspectJPreferences.OPTION_noJoinpointsForBridgeMethods,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		
		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.cantMatchArrayTypeOnVarargs"); //$NON-NLS-1$
		addComboBox(composite, label, AspectJPreferences.OPTION_cantMatchArrayTypeOnVarargs,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		
		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.enumAsTargetForDecpIgnored"); //$NON-NLS-1$
		addComboBox(composite, label, AspectJPreferences.OPTION_enumAsTargetForDecpIgnored,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		
		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.annotationAsTargetForDecpIgnored"); //$NON-NLS-1$
		addComboBox(composite, label, AspectJPreferences.OPTION_annotationAsTargetForDecpIgnored,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.invalidTargetForAnnotation"); //$NON-NLS-1$
		addComboBox(composite, label, AspectJPreferences.OPTION_invalidTargetForAnnotation,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.elementAlreadyAnnotated"); //$NON-NLS-1$
		addComboBox(composite, label, AspectJPreferences.OPTION_elementAlreadyAnnotated,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.runtimeExceptionNotSoftened"); //$NON-NLS-1$
		addComboBox(composite, label, AspectJPreferences.OPTION_runtimeExceptionNotSoftened,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.adviceDidNotMatch"); //$NON-NLS-1$
		addComboBox(composite, label, AspectJPreferences.OPTION_adviceDidNotMatch,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		return composite;
	}

	/**
	 * Get the preference store for AspectJ mode
	 */
	protected IPreferenceStore doGetPreferenceStore() {
		return AspectJUIPlugin.getDefault().getPreferenceStore();
	}

	/*
	 * (non-Javadoc) Method declared on PreferencePage
	 */
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();

		boolean lintChanges = false;
		for (int i = fComboBoxes.size() - 1; i >= 0; i--) {
			Combo curr = (Combo) fComboBoxes.get(i);
			ControlData data = (ControlData) curr.getData();
			String value = data.getValue(curr.getSelectionIndex());
			if (!value.equals(store.getString(data.getKey()))) {
				lintChanges = true;
				store.setValue(data.getKey(), value);
			}
		}

		boolean advancedOrOtherChanges = false;
		for (int i = fCheckBoxes.size() - 1; i >= 0; i--) {
			Button curr = (Button) fCheckBoxes.get(i);
			ControlData data = (ControlData) curr.getData();
			boolean value = curr.getSelection();
			if (value != store.getBoolean(data.getKey())) {
				advancedOrOtherChanges = true;
				store.setValue(data.getKey(), value);
			}
		}

		AspectJUIPlugin.getDefault().savePluginPreferences();

		if (lintChanges || advancedOrOtherChanges) {
			boolean doBuild = false;
			String[] strings = getFullBuildDialogStrings();
			if (strings != null) {
				MessageDialog dialog = new MessageDialog(getShell(),
						strings[0], null, strings[1], MessageDialog.QUESTION,
						new String[]{IDialogConstants.YES_LABEL,
								IDialogConstants.NO_LABEL,
								IDialogConstants.CANCEL_LABEL}, 2);
				int res = dialog.open();
				if (res == 0) {
					doBuild = true;
				} else if (res != 1) {
					return false; // cancel pressed
				}
			}
			if (doBuild) {
				doFullBuild();
			}
		}

		return true;
	}

	protected String[] getFullBuildDialogStrings() {
		String title = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.needsbuild.title"); //$NON-NLS-1$
		String message = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.needsfullbuild.message"); //$NON-NLS-1$
		return new String[]{title, message};
	}

	protected void doFullBuild() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		try {
			dialog.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException {
					monitor.beginTask("", 2); //$NON-NLS-1$
					try {
						monitor
								.setTaskName(AspectJUIPlugin
										.getResourceString("OptionsConfigurationBlock.buildall.taskname")); //$NON-NLS-1$
						ResourcesPlugin.getWorkspace().build(
								IncrementalProjectBuilder.FULL_BUILD,
								new SubProgressMonitor(monitor, 2));
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InterruptedException e) {
			// cancelled by user
		} catch (InvocationTargetException e) {
			String message = AspectJUIPlugin
					.getResourceString("OptionsConfigurationBlock.builderror.message"); //$NON-NLS-1$
			AspectJUIPlugin.getDefault().getErrorHandler().handleError(message, e);
		}
	}

	/*
	 * (non-Javadoc) Method declared on PreferencePage
	 */
	protected void performDefaults() {
		super.performDefaults();

		for (int i = fComboBoxes.size() - 1; i >= 0; i--) {
			Combo curr = (Combo) fComboBoxes.get(i);
			ControlData data = (ControlData) curr.getData();
			String defaultValue = getPreferenceStore().getDefaultString(
					data.getKey());
			curr.select(data.getSelection(defaultValue));
		}
		for (int i = fCheckBoxes.size() - 1; i >= 0; i--) {
			Button curr = (Button) fCheckBoxes.get(i);
			// must enable the button as it may have been disabled
			// if -XnoWeave was previously selected
			curr.setEnabled(true);
			ControlData data = (ControlData) curr.getData();
			String defaultValue = getPreferenceStore().getDefaultString(
					data.getKey());
			curr.setSelection(defaultValue.equals("true")); //$NON-NLS-1$
		}
	}

	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}

	protected Button addCheckBox(Composite parent, String label, String key,
			String[] values, int indent) {
		return addCheckBox(parent, label, key, values, indent, true);
	}
	
	protected Button addCheckBox(Composite parent, String label, String key,
			String[] values, int indent, boolean fillGridVertically) {
		ControlData data = new ControlData(key, values);

		int idx = label.indexOf("-"); //$NON-NLS-1$
		String optionname = label.substring(0,idx);
		String optiondesc = label.substring(idx+1);
		optiondesc=optiondesc.trim();
		
		GridData gd = new GridData();
		if(fillGridVertically)
			gd.verticalAlignment = GridData.FILL;
		gd.horizontalSpan = 3;
		gd.horizontalIndent = indent;
		

		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(optionname);
		checkBox.setData(data);
		checkBox.setLayoutData(gd);
		Label l = new Label(parent,SWT.WRAP);
		l.setText(optiondesc);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent = 20;
		l.setLayoutData(gridData);
		createLabel(parent,"");//filler //$NON-NLS-1$
		

		boolean currValue = getPreferenceStore().getBoolean(key);
		checkBox.setSelection(currValue);

		fCheckBoxes.add(checkBox);

		return checkBox;
	}

	protected void addComboBox(Composite parent, String label, String key,
			String[] values, String[] valueLabels, int indent) {
		ControlData data = new ControlData(key, values);

		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent = indent;

		Label labelControl = new Label(parent, SWT.LEFT | SWT.WRAP);
		labelControl.setText(label);
		labelControl.setLayoutData(gd);

		Combo comboBox = new Combo(parent, SWT.READ_ONLY);
		comboBox.setItems(valueLabels);
		comboBox.setData(data);
		comboBox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		
		Label placeHolder = new Label(parent, SWT.NONE);
		placeHolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		String currValue = getPreferenceStore().getString(key);
		if ((currValue != null) && (currValue.length() > 0)) {
			comboBox.select(data.getSelection(currValue));
		}

		fComboBoxes.add(comboBox);
	}

	/**
	 * Class which listens for selections of the advanced options
	 * -XnoWeave, -Xreweavable and -Xreweavable:compress and updates
	 * the remaining buttons accordingly (to make it less confusing
	 * for the user)
	 */	
	private class CheckBoxListener implements SelectionListener {

		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(noweaveButton)) {
				boolean buttonSelected = noweaveButton.getSelection();
				if (buttonSelected) {
					lazytjpButton.setSelection(false);
					noinlineButton.setSelection(false);
					reweaveButton.setSelection(false);
					reweaveCompressButton.setSelection(false);
				}				
				lazytjpButton.setEnabled(!buttonSelected);
				noinlineButton.setEnabled(!buttonSelected);
				reweaveButton.setEnabled(!buttonSelected);
				reweaveCompressButton.setEnabled(!buttonSelected);
			} else if (e.getSource().equals(reweaveButton)) {
				boolean buttonSelected = reweaveButton.getSelection();
				if (buttonSelected) {
					reweaveCompressButton.setSelection(false);
				}
			} else if (e.getSource().equals(reweaveCompressButton)) {
				boolean buttonSelected = reweaveCompressButton.getSelection();
				if (buttonSelected) {
					reweaveButton.setSelection(false);
				}
			}
		}
		public void widgetDefaultSelected(SelectionEvent e) {
		}		
	}
	
	/**
	 * When the advanced tab is initialized, check whether the
	 * user last chose the -XnoWeave option, if so, disable all
	 * other options.
	 */	
	private void checkNoWeaveSelection() {
		boolean buttonSelected = noweaveButton.getSelection();
		if (buttonSelected) {
			lazytjpButton.setEnabled(!buttonSelected);
			noinlineButton.setEnabled(!buttonSelected);
			reweaveButton.setEnabled(!buttonSelected);
			reweaveCompressButton.setEnabled(!buttonSelected);
		}						
	}
}