/**********************************************************************
Copyright (c) 2003, 2005 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Matt Chapman - initial version
Ian McGrath - Adapted for the properties page
Matt Chapman - added project scoped preferences (40446)
**********************************************************************/

package org.eclipse.ajdt.internal.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.EclipseVersion;
import org.eclipse.ajdt.internal.ui.preferences.AJCompilerPreferencePage;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.internal.ui.wizards.TabFolderLayout;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

/**
* Used to operate the AspectJ compiler properties page that appear when an aspectJ project is right
* clicked and the properties option selected, found under the AspectJ Compiler tab.
*
*/
public class CompilerPropertyPage extends PropertyPage {

	// bug 90174 - leave these as strings to keep the code simple
	private static final String VALUE_TRUE = "true"; //$NON-NLS-1$
	private static final String VALUE_FALSE = "false"; //$NON-NLS-1$

	private Button noweaveButton, noinlineButton, notReweaveableButton, hasMemberButton;  
	
	private IProject thisProject;
	
	protected List fComboBoxes;
	protected List fCheckBoxes;
	private SelectionButtonDialogField fUseWorkspaceSettings;
	private SelectionButtonDialogField fChangeWorkspaceSettings;
	private SelectionButtonDialogField fUseProjectSettings;
	private TabFolder folder;

	/**
	 * The default values used when the plugin is first installed or when
	 * "restore defaults" is clicked.
	 */
	private static final Map defaultValueMap = new HashMap();
	static {
		defaultValueMap.put(AspectJPreferences.OPTION_ReportInvalidAbsoluteTypeName, AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(AspectJPreferences.OPTION_ReportShadowNotInStructure, AspectJPreferences.VALUE_IGNORE);
		defaultValueMap.put(AspectJPreferences.OPTION_ReportCannotImplementLazyTJP, AspectJPreferences.VALUE_IGNORE);
		defaultValueMap.put(AspectJPreferences.OPTION_ReportInvalidWildcardTypeName, AspectJPreferences.VALUE_IGNORE);
		defaultValueMap.put(AspectJPreferences.OPTION_ReportTypeNotExposedToWeaver, AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(AspectJPreferences.OPTION_ReportUnresolvableMember, AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(AspectJPreferences.OPTION_ReportUnmatchedSuperTypeInCall, AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(AspectJPreferences.OPTION_ReportIncompatibleSerialVersion, AspectJPreferences.VALUE_IGNORE);
		defaultValueMap.put(AspectJPreferences.OPTION_ReportNeedSerialVersionUIDField, AspectJPreferences.VALUE_IGNORE);
		defaultValueMap.put(AspectJPreferences.OPTION_ReportNoInterfaceCtorJoinpoint, AspectJPreferences.VALUE_WARNING);
		
		// these options are being set to "true" or "false" (rather than AspectJPreferences.VALUE_ENABLED
		// or AspectJPreferences.VALUE_DISABLED) because the underlying code works in true/false
		// (mimic behaviour of AJCompilerPreferencePage) - bug 87128
		defaultValueMap.put(AspectJPreferences.OPTION_NoWeave, VALUE_FALSE);
		defaultValueMap.put(AspectJPreferences.OPTION_XSerializableAspects, VALUE_FALSE);
		defaultValueMap.put(AspectJPreferences.OPTION_XNoInline, VALUE_FALSE);
		defaultValueMap.put(AspectJPreferences.OPTION_XNotReweavable, VALUE_FALSE);
		defaultValueMap.put(AspectJPreferences.OPTION_XHasMember,VALUE_FALSE);
		
		defaultValueMap.put(AspectJPreferences.OPTION_Incremental, VALUE_TRUE);
		defaultValueMap.put(AspectJPreferences.OPTION_BuildASM, VALUE_TRUE);
		defaultValueMap.put(AspectJPreferences.OPTION_WeaveMessages, VALUE_FALSE);
		
		defaultValueMap.put(AspectJPreferences.OPTION_noJoinpointsForBridgeMethods, AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(AspectJPreferences.OPTION_cantMatchArrayTypeOnVarargs, AspectJPreferences.VALUE_IGNORE);
		defaultValueMap.put(AspectJPreferences.OPTION_enumAsTargetForDecpIgnored, AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(AspectJPreferences.OPTION_annotationAsTargetForDecpIgnored, AspectJPreferences.VALUE_WARNING);

		defaultValueMap.put(AspectJPreferences.OPTION_invalidTargetForAnnotation, AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(AspectJPreferences.OPTION_elementAlreadyAnnotated, AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(AspectJPreferences.OPTION_runtimeExceptionNotSoftened, AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(AspectJPreferences.OPTION_adviceDidNotMatch, AspectJPreferences.VALUE_WARNING);
	}

	/**
	 * List of all the preference keys for this page
	 */
	private static final String[] keys = new String[] {
		AspectJPreferences.OPTION_ReportInvalidAbsoluteTypeName,
		AspectJPreferences.OPTION_ReportShadowNotInStructure,
	    	AspectJPreferences.OPTION_ReportCannotImplementLazyTJP,
	    	AspectJPreferences.OPTION_ReportInvalidWildcardTypeName,
	    	AspectJPreferences.OPTION_ReportTypeNotExposedToWeaver,
	    	AspectJPreferences.OPTION_ReportUnresolvableMember,
	    	AspectJPreferences.OPTION_ReportUnmatchedSuperTypeInCall,
	    	AspectJPreferences.OPTION_ReportIncompatibleSerialVersion,
	    	AspectJPreferences.OPTION_ReportNeedSerialVersionUIDField,
	    	AspectJPreferences.OPTION_ReportNoInterfaceCtorJoinpoint,
	    	AspectJPreferences.OPTION_NoWeave,
	    	AspectJPreferences.OPTION_XSerializableAspects,
	    	AspectJPreferences.OPTION_XNoInline,
	    	AspectJPreferences.OPTION_XNotReweavable,
	    	AspectJPreferences.OPTION_XHasMember,
	    	AspectJPreferences.OPTION_BuildASM,
			AspectJPreferences.OPTION_Incremental,
			AspectJPreferences.OPTION_WeaveMessages,
			AspectJPreferences.OPTION_annotationAsTargetForDecpIgnored,
			AspectJPreferences.OPTION_cantMatchArrayTypeOnVarargs,
			AspectJPreferences.OPTION_enumAsTargetForDecpIgnored,
			AspectJPreferences.OPTION_noJoinpointsForBridgeMethods,
			AspectJPreferences.OPTION_invalidTargetForAnnotation,
			AspectJPreferences.OPTION_elementAlreadyAnnotated,
			AspectJPreferences.OPTION_runtimeExceptionNotSoftened,
			AspectJPreferences.OPTION_adviceDidNotMatch
		};

	public CompilerPropertyPage() {
		super();
		fCheckBoxes = new ArrayList();
		fComboBoxes = new ArrayList();

		IDialogFieldListener listener = new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				doDialogFieldChanged(field);
			}
		};

		fUseWorkspaceSettings = new SelectionButtonDialogField(SWT.RADIO);
		fUseWorkspaceSettings.setDialogFieldListener(listener);
		fUseWorkspaceSettings
				.setLabelText(AspectJUIPlugin
						.getResourceString("CompilerPropertyPage.useworkspacesettings.label")); //$NON-NLS-1$

		fChangeWorkspaceSettings = new SelectionButtonDialogField(SWT.PUSH);
		fChangeWorkspaceSettings
				.setLabelText(AspectJUIPlugin
						.getResourceString("CompilerPropertyPage.useworkspacesettings.change")); //$NON-NLS-1$
		fChangeWorkspaceSettings.setDialogFieldListener(listener);

		fUseWorkspaceSettings.attachDialogField(fChangeWorkspaceSettings);

		fUseProjectSettings = new SelectionButtonDialogField(SWT.RADIO);
		fUseProjectSettings.setDialogFieldListener(listener);
		fUseProjectSettings
				.setLabelText(AspectJUIPlugin
						.getResourceString("CompilerPropertyPage.useprojectsettings.label")); //$NON-NLS-1$
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

	public static void setDefaults(IEclipsePreferences projectNode) {
		for (int i = 0; i < keys.length; i++) {
			String value = (String)defaultValueMap.get(keys[i]);
			projectNode.put(keys[i], value);
		}
	}

	public static void setDefaultsIfValueNotAlreadySet(IEclipsePreferences projectNode) {
		List existingKeysList = new ArrayList();
		try {
			existingKeysList = Arrays.asList(projectNode.keys());
		} catch (BackingStoreException e) {
		}
		for (int i = 0; i < keys.length; i++) {
			String value = (String)defaultValueMap.get(keys[i]);
			boolean keyExists = false;
			if (existingKeysList.contains(keys[i])) {
				keyExists = true;
			}
			if (!keyExists) {
				projectNode.put(keys[i], value);				
			}
		}
	}

	public static void removeValues(IEclipsePreferences projectNode) {
		for (int i = 0; i < keys.length; i++) {
			projectNode.remove(keys[i]);
		}
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
	 * Generates the GUI and initialises variables
	 */
	protected Control createContents(Composite parent) {

		thisProject = (IProject) getElement();	
		
		//Composite is the project-workspace settings selection part of the gui at the top of the page
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		composite.setLayout(layout);
		
		fUseWorkspaceSettings.doFillIntoGrid(composite, 1);
		LayoutUtil.setHorizontalGrabbing(fUseWorkspaceSettings.getSelectionButton(null));
		
		fChangeWorkspaceSettings.doFillIntoGrid(composite, 1);
		
		fUseProjectSettings.doFillIntoGrid(composite, 2);
		
		GridData data= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL );
		data.horizontalSpan= 2;
		
		//folder holds the tabed selection chart on the lower two thirds of the properties page
		folder = new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(data);

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
		
		Dialog.applyDialogFont(composite);
		if(AspectJPreferences.isUsingProjectSettings(thisProject)) {
			fUseProjectSettings.setSelection(true);
		} else {
			fUseWorkspaceSettings.setSelection(true);
		}
		updateEnableState();
		return composite;
	}
	
	/*
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	private Composite createMessagesTabContent(Composite folder) {
		String[] errorWarningIgnore = new String[]{AspectJPreferences.VALUE_ERROR, AspectJPreferences.VALUE_WARNING, AspectJPreferences.VALUE_IGNORE};

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
		addComboBox(composite, label, AspectJPreferences.OPTION_ReportInvalidAbsoluteTypeName,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_invalid_wildcard_type_name.label"); //$NON-NLS-1$
		addComboBox(composite, label, AspectJPreferences.OPTION_ReportInvalidWildcardTypeName,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_unresolvable_member.label"); //$NON-NLS-1$
		addComboBox(composite, label, AspectJPreferences.OPTION_ReportUnresolvableMember,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_type_not_exposed_to_weaver.label"); //$NON-NLS-1$
		addComboBox(composite, label, AspectJPreferences.OPTION_ReportTypeNotExposedToWeaver,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_shadow_not_in_structure.label"); //$NON-NLS-1$
		addComboBox(composite, label, AspectJPreferences.OPTION_ReportShadowNotInStructure,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_unmatched_super_type_in_call.label"); //$NON-NLS-1$
		addComboBox(composite, label, AspectJPreferences.OPTION_ReportUnmatchedSuperTypeInCall,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_cannot_implement_lazy_tjp.label"); //$NON-NLS-1$
		addComboBox(composite, label, AspectJPreferences.OPTION_ReportCannotImplementLazyTJP,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_need_serial_version_uid_field.label"); //$NON-NLS-1$
		addComboBox(composite, label, AspectJPreferences.OPTION_ReportNeedSerialVersionUIDField,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_incompatible_serial_version.label"); //$NON-NLS-1$
		addComboBox(composite, label, AspectJPreferences.OPTION_ReportIncompatibleSerialVersion,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_no_interface_ctor_joinpoint.label"); //$NON-NLS-1$
		addComboBox(composite, label, AspectJPreferences.OPTION_ReportNoInterfaceCtorJoinpoint,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		return composite;
	}
	
	/**
	 * Generates the gui for the Advanced tab
	 */
	private Composite createAdvancedTabContent(Composite folder) {
		String[] enableDisableValues = new String[]{AspectJPreferences.VALUE_ENABLED, AspectJPreferences.VALUE_DISABLED};
		
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
		noweaveButton = addCheckBox(composite, label, AspectJPreferences.OPTION_NoWeave, enableDisableValues, 0);
	    noweaveButton.addSelectionListener(checkBoxListener);
		
		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_x_serializable_aspects.label"); //$NON-NLS-1$
		addCheckBox(composite, label, AspectJPreferences.OPTION_XSerializableAspects, enableDisableValues, 0);
	
		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_x_no_inline.label"); //$NON-NLS-1$
		noinlineButton = addCheckBox(composite, label, AspectJPreferences.OPTION_XNoInline, enableDisableValues, 0);
		
		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_x_not_reweavable.label"); //$NON-NLS-1$
		notReweaveableButton = addCheckBox(composite, label, AspectJPreferences.OPTION_XNotReweavable, enableDisableValues, 0);
		notReweaveableButton.addSelectionListener(checkBoxListener);

		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_x_has_member.label"); //$NON-NLS-1$
		hasMemberButton = addCheckBox(composite, label, AspectJPreferences.OPTION_XHasMember, enableDisableValues, 0);
		hasMemberButton.addSelectionListener(checkBoxListener);

		checkNoWeaveSelection();
		
		return composite;
	}

	private Composite createOtherTabContent(Composite folder) {
		String[] enableDisableValues = new String[]{AspectJPreferences.VALUE_ENABLED, AspectJPreferences.VALUE_DISABLED};

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
		addCheckBox(composite, label, AspectJPreferences.OPTION_Incremental, enableDisableValues, 0, false);
		
		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_enable_build_asm.label"); //$NON-NLS-1$
		addCheckBox(composite, label, AspectJPreferences.OPTION_BuildASM, enableDisableValues, 0, false);

		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_enable_weave_messages.label"); //$NON-NLS-1$
		addCheckBox(composite, label, AspectJPreferences.OPTION_WeaveMessages,enableDisableValues, 0);

		checkNoWeaveSelection();
		
		return composite;
	}


	/**
	 * @param folder
	 * @return
	 */
	private Composite createAJ5TabContent(TabFolder folder) {
		String[] errorWarningIgnore = new String[]{AspectJPreferences.VALUE_ERROR, AspectJPreferences.VALUE_WARNING, AspectJPreferences.VALUE_IGNORE};

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
	 * overriding performApply() for PreferencePaageBuilder.aj
	 */
	public void performApply() {  
	    performOk();
	}

	public boolean performOk() {
		boolean projectSettingsChanged = updateProjectSettings();

		boolean projectWorkspaceChanges = false;
		if(AspectJPreferences.isUsingProjectSettings(thisProject) !=  useProjectSettings()) {
			projectWorkspaceChanges = true;
			// don't want to overwrite existing project settings
			// because have just set them in the above call to 
			// updateProjectSettings();
			AspectJPreferences.setUsingProjectSettings(thisProject, useProjectSettings(),false);
		}

		
		AspectJUIPlugin.getDefault().savePluginPreferences();

		if (projectWorkspaceChanges || (projectSettingsChanged && useProjectSettings())) {
			String[] strings = getProjectBuildDialogStrings();
			if (strings != null) {
				MessageDialog dialog = new MessageDialog(getShell(),
						strings[0], null, strings[1], MessageDialog.QUESTION,
						new String[]{IDialogConstants.YES_LABEL,
								IDialogConstants.NO_LABEL,
								IDialogConstants.CANCEL_LABEL}, 2);
				int res = dialog.open();
				if (res != 1) {
					return false; // cancel pressed
				}
			}
		}

		return true;
	}
	
	
	private void setPrefValue(IProject project, String key, String value) {
	    	IScopeContext projectScope = new ProjectScope(project);
	    	IEclipsePreferences projectNode = projectScope.getNode(AspectJPlugin.PLUGIN_ID);
	    	projectNode.put(key,value);
	}

	private void flushPrefs(IProject project) {
			IScopeContext projectScope = new ProjectScope(project);
			IEclipsePreferences projectNode = projectScope.getNode(AspectJPlugin.PLUGIN_ID);
	       	try {
				projectNode.flush();
			} catch (BackingStoreException e) {
			}
	}
	
	/**
	 * Checks whether the project settings have changed and 
	 * updates the store accordingly if there is a change.
	 */
	private boolean updateProjectSettings() {
		List tempComboBoxes = new ArrayList();
		tempComboBoxes.addAll(fComboBoxes);
		List tempCheckBoxes = new ArrayList();
		tempCheckBoxes.addAll(fCheckBoxes);

		boolean settingsChanged = false;

		walkThroughKeys: for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			String storeValue = AspectJPreferences.getStringPrefValue(thisProject, key);
			if (!storeValue.equals("")) { //$NON-NLS-1$
				// bug 87128 - why checking against "true/false"
				if (!storeValue.equals(VALUE_TRUE) && !storeValue.equals(VALUE_FALSE)) {
					// this is a combo box
					for (int j = 0; j < tempComboBoxes.size(); j++) {
						Combo curr = (Combo) tempComboBoxes.get(j);
						ControlData data = (ControlData) curr.getData();
						if (key.equals(data.getKey())) {
							if (!storeValue.equals(data.getValue(curr
									.getSelectionIndex()))) {
								settingsChanged = true;
								setPrefValue(thisProject, data.getKey(), data
										.getValue(curr.getSelectionIndex()));
							}
							tempComboBoxes.remove(curr);
							continue walkThroughKeys;
						}
					}
				} else {
					// this is a check box
					for (int j = 0; j < tempCheckBoxes.size(); j++) {
						Button curr = (Button) tempCheckBoxes.get(j);
						ControlData data = (ControlData) curr.getData();
						if (key.equals(data.getKey())) {
							// bug 87128 - why checking against "true/false"
							String stringValue = curr.getSelection() ? VALUE_TRUE : VALUE_FALSE;
							if (!storeValue.equals(stringValue)) {
								settingsChanged = true;
								setPrefValue(thisProject, data.getKey(),
										stringValue);
							}
							tempCheckBoxes.remove(curr);
							continue walkThroughKeys;
						}
					}
				}
			}
		}
		
		if (settingsChanged) {
			flushPrefs(thisProject);
		}
		return settingsChanged;
	}
	
	/**
	 * Get the preference store for AspectJ mode
	 */
	protected String[] getProjectBuildDialogStrings() {
		String title = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.needsbuild.title"); //$NON-NLS-1$
		String message = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.needsprojectbuild.message"); //$NON-NLS-1$
		return new String[]{title, message};
	}

	/*
	 * (non-Javadoc) Method declared on PreferencePage
	 */
	protected void performDefaults() {
		super.performDefaults();

		fUseWorkspaceSettings.setSelection(true);
		fUseProjectSettings.setSelection(false);
		
		for (int i = fComboBoxes.size() - 1; i >= 0; i--) {
			Combo curr = (Combo) fComboBoxes.get(i);
			ControlData data = (ControlData) curr.getData();
			String defaultValue = (String)defaultValueMap.get(data.getKey());
			curr.select(data.getSelection(defaultValue));
		}
		for (int i = fCheckBoxes.size() - 1; i >= 0; i--) {
			Button curr = (Button) fCheckBoxes.get(i);
			// must enable the button as it may have been disabled
			// if -XnoWeave was previously selected

			if(useProjectSettings())
				curr.setEnabled(true);
			ControlData data = (ControlData) curr.getData();
			String defaultValue = (String)defaultValueMap.get(data.getKey());
			curr.setSelection(defaultValue.equals(VALUE_TRUE));
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
		
		String currValue = AspectJPreferences.getStringPrefValue(thisProject,key);
		// bug 87128
		if (currValue.equals("")) {  //$NON-NLS-2$ //$NON-NLS-1$
			currValue = (String)defaultValueMap.get(key);
		} else if (currValue.equals(AspectJPreferences.VALUE_ENABLED)) {
			// this case deals with backwards compatibility
			currValue = VALUE_TRUE; 
		}
		checkBox.setSelection(currValue.equals(VALUE_TRUE));

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

		String currValue = AspectJPreferences.getStringPrefValue(thisProject, key);
		if (currValue.equals("")) { //$NON-NLS-1$
			currValue = (String)defaultValueMap.get(key);
		}
		if (currValue.length() > 0) {
			comboBox.select(data.getSelection(currValue));
		}

		fComboBoxes.add(comboBox);
	}

	/**
	 * Class which listens for selections of the
	 * -XnoWeave option and updates
	 * the remaining buttons accordingly (to make it less confusing
	 * for the user)
	 */	
	 private class CheckBoxListener implements SelectionListener {

		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(noweaveButton)) {
				boolean buttonSelected = noweaveButton.getSelection();
				if (buttonSelected) {
					noinlineButton.setSelection(false);
					notReweaveableButton.setSelection(false);
				}				
				noinlineButton.setEnabled(!buttonSelected);
				notReweaveableButton.setEnabled(!buttonSelected);
			}
		}
		public void widgetDefaultSelected(SelectionEvent e) {
		    widgetSelected(e);
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
			noinlineButton.setEnabled(!buttonSelected);
			notReweaveableButton.setEnabled(!buttonSelected);
		}						
	}
		
	/**
	 * Listens to various buttons and can load the workspace preference page in a seperate window
	 * and determines if the enabled status of the GUI's button and checkboxes need refreshing
	 */
	private void doDialogFieldChanged(DialogField field) {
		if (field == fChangeWorkspaceSettings) {
			String id= "org.eclipse.ajdt.ui.preferences.AJCompilerPreferencePage"; //$NON-NLS-1$
			AJCompilerPreferencePage page= new AJCompilerPreferencePage();
			PreferencePageSupport.showPreferencePage(getShell(), id, page);
		} else {
			updateEnableState();
		}
	}

	/**
	 * Enables and disables the appropriate buttons
	 */	
	private void updateEnableState() {
		if (useProjectSettings()) {
		    readStateForAndEnable(folder);
			boolean buttonSelected = noweaveButton.getSelection();
			if (buttonSelected) {
				noinlineButton.setSelection(false);
				notReweaveableButton.setSelection(false);
			}				
			noinlineButton.setEnabled(!buttonSelected);
			notReweaveableButton.setEnabled(!buttonSelected);
		}
		else {
		    readStateForAndDisable(folder);
		}
	}

	/**
	 * Disables all the composites below the given one (the
	 * reverse of readStateForAndEnable(Control control)).
	 * Edited from the private method in ControlEnableState
	 * of the same name.
	 */
	private void readStateForAndDisable(Control control) {
		if (control instanceof Composite) {
			Composite c = (Composite) control;
			Control[] children = c.getChildren();
			for (int i = 0; i < children.length; i++) {
				readStateForAndDisable(children[i]);
			}
		}
		control.setEnabled(false);
	}
	
	/**
	 * Enables all the composites below the given one (the
	 * reverse of readStateForAndDiable(Control control))
	 */
	private void readStateForAndEnable(Control control) {
		if (control instanceof Composite) {
			Composite c = (Composite) control;
			Control[] children = c.getChildren();
			for (int i = 0; i < children.length; i++) {
				readStateForAndEnable(children[i]);
			}
		}
		control.setEnabled(true);
	}
	
	/**
	 * Checks the status of the project settings button, returns true if selected
	 */
	private boolean useProjectSettings() {
		return fUseProjectSettings.isSelected();
	}
		
    /**
     * @return Returns the the project for which this preference
     *         page is open.
     */
    public IProject getThisProject() {
        return thisProject;
    }
    
	/**
	 * overriding dispose() for PreferencePaageBuilder.aj
	 */   
	public void dispose() {
		super.dispose();
	}
}
