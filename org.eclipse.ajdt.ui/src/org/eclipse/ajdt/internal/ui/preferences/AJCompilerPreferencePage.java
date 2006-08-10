/**********************************************************************
 Copyright (c) 2003, 2006 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Matt Chapman - initial version
 Ian McGrath - Modified to facilitate access from the project properties page
 
 Changes:
 Bug 104334 - Update AspectJ compiler preferences and property pages
 Changed from tabs, to twistys.
 Bug 151707 - Reorganise AspectJ compiler style options
 **********************************************************************/

package org.eclipse.ajdt.internal.ui.preferences;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.EclipseVersion;
import org.eclipse.ajdt.internal.ui.ajde.ErrorHandler;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage;
import org.eclipse.jdt.internal.ui.preferences.ScrolledPageContent;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The AspectJ compiler preferences that appear under the "AspectJ" category
 * under Workbench->preferences. The policy for preferences in AspectJ mode is
 * to use the Java mode preference where it exists, and only the AspectJ
 * specific preferences in this page.
 */
public class AJCompilerPreferencePage extends PropertyAndPreferencePage
		implements IWorkbenchPreferencePage {

	public static final String PREF_ID = "org.eclipse.ajdt.ui.preferences.AJCompilerPreferencePage"; //$NON-NLS-1$

	public static final String PROP_ID = "org.eclipse.ajdt.ui.propertyPages.AJCompilerPreferencePage"; //$NON-NLS-1$

	// bug 90174 - leave these as strings to keep the code simple
	private static final String VALUE_TRUE = "true"; //$NON-NLS-1$

	private static final String VALUE_FALSE = "false"; //$NON-NLS-1$

	private static final String PREF_AJ_INVALID_ABSOLUTE_TYPE_NAME = AspectJPreferences.OPTION_ReportInvalidAbsoluteTypeName; //potential matching probs 1

	private static final String PREF_AJ_SHADOW_NOT_IN_STRUCTURE = AspectJPreferences.OPTION_ReportShadowNotInStructure;

	private static final String PREF_AJ_CANNOT_IMPLEMENT_LAZY_TJP = AspectJPreferences.OPTION_ReportCannotImplementLazyTJP;

	private static final String PREF_AJ_INVALID_WILDCARD_TYPE_NAME = AspectJPreferences.OPTION_ReportInvalidWildcardTypeName; //potential matching probs 2

	private static final String PREF_AJ_TYPE_NOT_EXPOSED_TO_WEAVER = AspectJPreferences.OPTION_ReportTypeNotExposedToWeaver; //potential matching probs 3

	private static final String PREF_AJ_UNRESOLVABLE_MEMBER = AspectJPreferences.OPTION_ReportUnresolvableMember;

	private static final String PREF_AJ_UNMATCHED_SUPER_TYPE_IN_CALL = AspectJPreferences.OPTION_ReportUnmatchedSuperTypeInCall; //potential matching probs 4

	private static final String PREF_AJ_INCOMPATIBLE_SERIAL_VERSION = AspectJPreferences.OPTION_ReportIncompatibleSerialVersion;

	private static final String PREF_AJ_NEED_SERIAL_VERSION_UID_FIELD = AspectJPreferences.OPTION_ReportNeedSerialVersionUIDField;

	private static final String PREF_AJ_NO_INTERFACE_CTOR_JOINPOINT = AspectJPreferences.OPTION_ReportNoInterfaceCtorJoinpoint; //potential matching probs 5

	private static final String PREF_ENABLE_SERIALIZABLE_ASPECTS = AspectJPreferences.OPTION_XSerializableAspects;

	private static final String PREF_ENABLE_NO_INLINE = AspectJPreferences.OPTION_XNoInline;

	private static final String PREF_ENABLE_NOT_REWEAVABLE = AspectJPreferences.OPTION_XNotReweavable;

	private static final String PREF_ENABLE_HAS_MEMBER = AspectJPreferences.OPTION_XHasMember;

	private static final String PREF_ENABLE_OUTXML = AspectJPreferences.OPTION_Outxml;

	private static final String PREF_ENABLE_INCREMENTAL = AspectJPreferences.OPTION_Incremental;

	private static final String PREF_ENABLE_BUILD_ASM = AspectJPreferences.OPTION_BuildASM;

	private static final String PREF_ENABLE_WEAVE_MESSAGES = AspectJPreferences.OPTION_WeaveMessages;

	private static final String ERROR = JavaCore.ERROR;

	private static final String WARNING = JavaCore.WARNING;

	private static final String IGNORE = JavaCore.IGNORE;

	private static final String ENABLED = JavaCore.ENABLED;

	private static final String DISABLED = JavaCore.DISABLED;

	protected List fComboBoxes;

	protected List fCheckBoxes;

	protected final ArrayList fExpandedComposites;
	
	private IProject[] projects;
	
	// Non standard compiler options that should be passed to ajc
	private StringFieldEditor nonStandardOptionsEditor;
	
	private static final String SETTINGS_EXPANDED= "expanded"; //$NON-NLS-1$
	
	private static final String SETTINGS_SECTION_NAME= "AJDTCompilerOptionsBlock"; //$NON-NLS-1$

	/**
	 * The default values used when the plugin is first installed or when
	 * "restore defaults" is clicked.
	 */
	private static final Map defaultValueMap = new HashMap();
	static {
		defaultValueMap.put(
				AspectJPreferences.OPTION_ReportInvalidAbsoluteTypeName,
				AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(
				AspectJPreferences.OPTION_ReportShadowNotInStructure,
				AspectJPreferences.VALUE_IGNORE);
		defaultValueMap.put(
				AspectJPreferences.OPTION_ReportCannotImplementLazyTJP,
				AspectJPreferences.VALUE_IGNORE);
		defaultValueMap.put(
				AspectJPreferences.OPTION_ReportInvalidWildcardTypeName,
				AspectJPreferences.VALUE_IGNORE);
		defaultValueMap.put(
				AspectJPreferences.OPTION_ReportTypeNotExposedToWeaver,
				AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(AspectJPreferences.OPTION_ReportUnresolvableMember,
				AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(
				AspectJPreferences.OPTION_ReportUnmatchedSuperTypeInCall,
				AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(
				AspectJPreferences.OPTION_ReportIncompatibleSerialVersion,
				AspectJPreferences.VALUE_IGNORE);
		defaultValueMap.put(
				AspectJPreferences.OPTION_ReportNeedSerialVersionUIDField,
				AspectJPreferences.VALUE_IGNORE);
		defaultValueMap.put(
				AspectJPreferences.OPTION_ReportNoInterfaceCtorJoinpoint,
				AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(
				AspectJPreferences.OPTION_runtimeExceptionNotSoftened,
				AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(
				AspectJPreferences.OPTION_multipleAdviceStoppingLazyTJP,
				AspectJPreferences.VALUE_IGNORE);
		defaultValueMap.put(AspectJPreferences.OPTION_noGuardForLazyTjp,
				AspectJPreferences.VALUE_IGNORE);
		defaultValueMap.put(
				AspectJPreferences.OPTION_noExplicitConstructorCall,
				AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(
				AspectJPreferences.OPTION_aspectExcludedByConfiguration,
				AspectJPreferences.VALUE_IGNORE);
		defaultValueMap.put(AspectJPreferences.OPTION_unorderedAdviceAtShadow,
				AspectJPreferences.VALUE_IGNORE);

		// these options are being set to "true" or "false" (rather than
		// AspectJPreferences.VALUE_ENABLED
		// or AspectJPreferences.VALUE_DISABLED) because the underlying code
		// works in true/false
		// (mimic behaviour of AJCompilerPreferencePage) - bug 87128
		defaultValueMap.put(AspectJPreferences.OPTION_XSerializableAspects,
				VALUE_FALSE);
		defaultValueMap.put(AspectJPreferences.OPTION_XNoInline, VALUE_FALSE);
		defaultValueMap.put(AspectJPreferences.OPTION_XNotReweavable,
				VALUE_FALSE);
		defaultValueMap.put(AspectJPreferences.OPTION_XHasMember, VALUE_FALSE);

		defaultValueMap.put(AspectJPreferences.OPTION_Incremental, VALUE_TRUE);
		defaultValueMap.put(AspectJPreferences.OPTION_BuildASM, VALUE_TRUE);
		defaultValueMap.put(AspectJPreferences.OPTION_WeaveMessages,
				VALUE_FALSE);

		defaultValueMap.put(
				AspectJPreferences.OPTION_noJoinpointsForBridgeMethods,
				AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(
				AspectJPreferences.OPTION_cantMatchArrayTypeOnVarargs,
				AspectJPreferences.VALUE_IGNORE);
		defaultValueMap.put(
				AspectJPreferences.OPTION_enumAsTargetForDecpIgnored,
				AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(
				AspectJPreferences.OPTION_annotationAsTargetForDecpIgnored,
				AspectJPreferences.VALUE_WARNING);

		defaultValueMap.put(
				AspectJPreferences.OPTION_invalidTargetForAnnotation,
				AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(AspectJPreferences.OPTION_elementAlreadyAnnotated,
				AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(AspectJPreferences.OPTION_adviceDidNotMatch,
				AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(AspectJPreferences.OPTION_unmatchedTargetKind,
				AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(AspectJPreferences.OPTION_uncheckedArgument,
				AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(
				AspectJPreferences.OPTION_uncheckedAdviceConversion,
				AspectJPreferences.VALUE_WARNING);
		defaultValueMap.put(
				AspectJPreferences.OPTION_swallowedExceptionInCatchBlock,
				AspectJPreferences.VALUE_IGNORE);
		
		defaultValueMap.put(AspectJPreferences.COMPILER_OPTIONS, "");
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
			AspectJPreferences.OPTION_adviceDidNotMatch,
			AspectJPreferences.OPTION_multipleAdviceStoppingLazyTJP,
			AspectJPreferences.OPTION_noGuardForLazyTjp,
			AspectJPreferences.OPTION_noExplicitConstructorCall,
			AspectJPreferences.OPTION_aspectExcludedByConfiguration,
			AspectJPreferences.OPTION_unorderedAdviceAtShadow,
			AspectJPreferences.OPTION_unmatchedTargetKind,
			AspectJPreferences.OPTION_uncheckedArgument,
			AspectJPreferences.OPTION_uncheckedAdviceConversion,
			AspectJPreferences.OPTION_swallowedExceptionInCatchBlock,
			AspectJPreferences.COMPILER_OPTIONS };

	public AJCompilerPreferencePage() {
		super();
		setTitle(UIMessages.AJCompilerPreferencePage_aspectj_compiler);
		fCheckBoxes = new ArrayList();
		fComboBoxes = new ArrayList();
		fExpandedComposites = new ArrayList();
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
		store.setDefault(PREF_AJ_CANNOT_IMPLEMENT_LAZY_TJP, IGNORE);
		store.setDefault(PREF_AJ_INVALID_WILDCARD_TYPE_NAME, IGNORE);
		store.setDefault(PREF_AJ_TYPE_NOT_EXPOSED_TO_WEAVER, WARNING);
		store.setDefault(PREF_AJ_UNRESOLVABLE_MEMBER, WARNING);
		store.setDefault(PREF_AJ_UNMATCHED_SUPER_TYPE_IN_CALL, WARNING);
		store.setDefault(PREF_AJ_INCOMPATIBLE_SERIAL_VERSION, IGNORE);
		store.setDefault(PREF_AJ_NEED_SERIAL_VERSION_UID_FIELD, IGNORE);
		store.setDefault(PREF_AJ_NO_INTERFACE_CTOR_JOINPOINT, WARNING);

		store.setDefault(PREF_ENABLE_SERIALIZABLE_ASPECTS, false);
		store.setDefault(PREF_ENABLE_NO_INLINE, false);
		store.setDefault(PREF_ENABLE_NOT_REWEAVABLE, false);
		store.setDefault(PREF_ENABLE_HAS_MEMBER, false);
		store.setDefault(PREF_ENABLE_OUTXML, false);

		store.setDefault(PREF_ENABLE_INCREMENTAL, true);
		store.setDefault(PREF_ENABLE_BUILD_ASM, true);
		store.setDefault(PREF_ENABLE_WEAVE_MESSAGES, false);

		store
				.setDefault(
						AspectJPreferences.OPTION_noJoinpointsForBridgeMethods,
						WARNING);
		store.setDefault(AspectJPreferences.OPTION_cantMatchArrayTypeOnVarargs,
				IGNORE);
		store.setDefault(AspectJPreferences.OPTION_enumAsTargetForDecpIgnored,
				WARNING);
		store.setDefault(
				AspectJPreferences.OPTION_annotationAsTargetForDecpIgnored,
				WARNING);
		store
				.setDefault(
						AspectJPreferences.OPTION_multipleAdviceStoppingLazyTJP,
						IGNORE);
		store.setDefault(AspectJPreferences.OPTION_noGuardForLazyTjp, IGNORE);
		store.setDefault(AspectJPreferences.OPTION_noExplicitConstructorCall,
				WARNING);
		store
				.setDefault(
						AspectJPreferences.OPTION_aspectExcludedByConfiguration,
						IGNORE);
		store.setDefault(AspectJPreferences.OPTION_unorderedAdviceAtShadow,
				IGNORE);

		store.setDefault(AspectJPreferences.OPTION_invalidTargetForAnnotation,
				WARNING);
		store.setDefault(AspectJPreferences.OPTION_elementAlreadyAnnotated,
				WARNING);
		store.setDefault(AspectJPreferences.OPTION_runtimeExceptionNotSoftened,
				WARNING);
		store.setDefault(AspectJPreferences.OPTION_adviceDidNotMatch, WARNING);
		store
				.setDefault(AspectJPreferences.OPTION_unmatchedTargetKind,
						WARNING);
		store.setDefault(AspectJPreferences.OPTION_uncheckedArgument, WARNING);
		store.setDefault(AspectJPreferences.OPTION_uncheckedAdviceConversion,
				WARNING);
		store.setDefault(
				AspectJPreferences.OPTION_swallowedExceptionInCatchBlock,
				IGNORE);
	}

	private Composite createCompilerPreferencesContent(Composite parent) {

		String[] errorWarningIgnore = new String[] { ERROR, WARNING, IGNORE };

		String[] errorWarningIgnoreLabels = new String[] {
				UIMessages.CompilerConfigurationBlock_error,
				UIMessages.CompilerConfigurationBlock_warning,
				UIMessages.CompilerConfigurationBlock_ignore };
		
		String[] enableDisableValues = new String[] { ENABLED, DISABLED };

		int nColumns = 3;
		final ScrolledPageContent pageContent = new ScrolledPageContent(parent);

		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;

		Composite composite= pageContent.getBody();
		composite.setLayout(layout);

		String label = UIMessages.CompilerConfigurationBlock_aj_messages_matching;
		ExpandableComposite excomposite = createStyleSection(composite, label,
				nColumns);

		Composite othersComposite = new Composite(excomposite, SWT.NONE);
		excomposite.setClient(othersComposite);
		othersComposite.setLayout(new GridLayout(nColumns, false));
		
		label = UIMessages.CompilerConfigurationBlock_aj_invalid_absolute_type_name_label;
		addComboBox(othersComposite, label, PREF_AJ_INVALID_ABSOLUTE_TYPE_NAME,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		
		label = UIMessages.CompilerConfigurationBlock_aj_invalid_wildcard_type_name_label;
		addComboBox(othersComposite, label, PREF_AJ_INVALID_WILDCARD_TYPE_NAME,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = UIMessages.CompilerConfigurationBlock_aj_type_not_exposed_to_weaver_label;
		addComboBox(othersComposite, label, PREF_AJ_TYPE_NOT_EXPOSED_TO_WEAVER,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		
		label = UIMessages.CompilerConfigurationBlock_aj_unmatched_super_type_in_call_label;
		addComboBox(othersComposite, label,
				PREF_AJ_UNMATCHED_SUPER_TYPE_IN_CALL, errorWarningIgnore,
				errorWarningIgnoreLabels, 0);
		
		label = UIMessages.CompilerConfigurationBlock_aj_no_interface_ctor_joinpoint_label;
		addComboBox(othersComposite, label,
				PREF_AJ_NO_INTERFACE_CTOR_JOINPOINT, errorWarningIgnore,
				errorWarningIgnoreLabels, 0);
//		 AJ5 options do not apply to Eclipse 3.0
		if (!((EclipseVersion.MAJOR_VERSION == 3) && (EclipseVersion.MINOR_VERSION == 0))) {
		label = UIMessages.CompilerConfigurationBlock_adviceDidNotMatch;
		addComboBox(othersComposite, label,
				AspectJPreferences.OPTION_adviceDidNotMatch,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		}
		label = UIMessages.CompilerConfigurationBlock_aspect_excluded_by_configuration;
		addComboBox(othersComposite, label,
				AspectJPreferences.OPTION_aspectExcludedByConfiguration,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		
		label = UIMessages.CompilerConfigurationBlock_aj_messages_optimization;
		excomposite = createStyleSection(composite, label, nColumns);

		othersComposite = new Composite(excomposite, SWT.NONE);
		excomposite.setClient(othersComposite);
		othersComposite.setLayout(new GridLayout(nColumns, false));
		
		label = UIMessages.CompilerConfigurationBlock_aj_cannot_implement_lazy_tjp_label;
		addComboBox(othersComposite, label, PREF_AJ_CANNOT_IMPLEMENT_LAZY_TJP,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		
		label = UIMessages.CompilerConfigurationBlock_multiple_advice_stopping_lazy_tjp;
		addComboBox(othersComposite, label,
				AspectJPreferences.OPTION_multipleAdviceStoppingLazyTJP,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		
		label = UIMessages.CompilerConfigurationBlock_no_guard_for_lazy_tjp;
		addComboBox(othersComposite, label,
				AspectJPreferences.OPTION_noGuardForLazyTjp,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		
		label = UIMessages.CompilerConfigurationBlock_aj_x_no_inline_label;
		addCheckBox(othersComposite, label, PREF_ENABLE_NO_INLINE,
				enableDisableValues, 0);

		//		 AJ5 options do not apply to Eclipse 3.0
		if (!((EclipseVersion.MAJOR_VERSION == 3) && (EclipseVersion.MINOR_VERSION == 0))) {
		label = UIMessages.CompilerConfigurationBlock_aj_messages_java5;
		excomposite = createStyleSection(composite, label, nColumns);
		
		othersComposite = new Composite(excomposite, SWT.NONE);
		excomposite.setClient(othersComposite);
		othersComposite.setLayout(new GridLayout(nColumns, false));
		
		Text description = new Text(othersComposite, SWT.WRAP | SWT.READ_ONLY);
		description
				.setText(UIMessages.CompilerConfigurationBlock_aj_messages_java5_label);
		GridData gd= new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan = nColumns;
		description.setLayoutData(gd);

		
		label = UIMessages.CompilerConfigurationBlock_unmatchedTargetKind;
		addComboBox(othersComposite, label,
				AspectJPreferences.OPTION_unmatchedTargetKind,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		
		label = UIMessages.CompilerConfigurationBlock_noJoinpointsForBridgeMethods;
		addComboBox(othersComposite, label,
				AspectJPreferences.OPTION_noJoinpointsForBridgeMethods,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		
		label = UIMessages.CompilerConfigurationBlock_enumAsTargetForDecpIgnored;
		addComboBox(othersComposite, label,
				AspectJPreferences.OPTION_enumAsTargetForDecpIgnored,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
	
		label = UIMessages.CompilerConfigurationBlock_annotationAsTargetForDecpIgnored;
		addComboBox(othersComposite, label,
				AspectJPreferences.OPTION_annotationAsTargetForDecpIgnored,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		
		label = UIMessages.CompilerConfigurationBlock_cantMatchArrayTypeOnVarargs;
		addComboBox(othersComposite, label,
				AspectJPreferences.OPTION_cantMatchArrayTypeOnVarargs,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		
		label = UIMessages.CompilerConfigurationBlock_invalidTargetForAnnotation;
		addComboBox(othersComposite, label,
				AspectJPreferences.OPTION_invalidTargetForAnnotation,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = UIMessages.CompilerConfigurationBlock_elementAlreadyAnnotated;
		addComboBox(othersComposite, label,
				AspectJPreferences.OPTION_elementAlreadyAnnotated,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = UIMessages.CompilerConfigurationBlock_uncheckedArgument;
		addComboBox(othersComposite, label,
				AspectJPreferences.OPTION_uncheckedArgument,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = UIMessages.CompilerConfigurationBlock_uncheckedAdviceConversion;
		addComboBox(othersComposite, label,
				AspectJPreferences.OPTION_uncheckedAdviceConversion,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		}
		label = UIMessages.CompilerConfigurationBlock_aj_messages_programming;
		excomposite = createStyleSection(composite, label, nColumns);

		othersComposite = new Composite(excomposite, SWT.NONE);
		excomposite.setClient(othersComposite);
		othersComposite.setLayout(new GridLayout(nColumns, false));
		
		label = UIMessages.CompilerConfigurationBlock_aj_need_serial_version_uid_field_label;
		addComboBox(othersComposite, label,
				PREF_AJ_NEED_SERIAL_VERSION_UID_FIELD, errorWarningIgnore,
				errorWarningIgnoreLabels, 0);
		
		label = UIMessages.CompilerConfigurationBlock_aj_incompatible_serial_version_label;
		addComboBox(othersComposite, label,
				PREF_AJ_INCOMPATIBLE_SERIAL_VERSION, errorWarningIgnore,
				errorWarningIgnoreLabels, 0);
		
		label = UIMessages.CompilerConfigurationBlock_no_explicit_constructor_call;
		addComboBox(othersComposite, label,
				AspectJPreferences.OPTION_noExplicitConstructorCall,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = UIMessages.CompilerConfigurationBlock_runtimeExceptionNotSoftened;
		addComboBox(othersComposite, label,
				AspectJPreferences.OPTION_runtimeExceptionNotSoftened,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = UIMessages.CompilerConfigurationBlock_unordered_advice_at_shadow;
		addComboBox(othersComposite, label,
				AspectJPreferences.OPTION_unorderedAdviceAtShadow,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = UIMessages.CompilerConfigurationBlock_swallowed_exception_in_catch_block;
		addComboBox(othersComposite, label,
				AspectJPreferences.OPTION_swallowedExceptionInCatchBlock,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		
		label = UIMessages.CompilerConfigurationBlock_aj_messages_information;
		excomposite = createStyleSection(composite, label, nColumns);

		othersComposite = new Composite(excomposite, SWT.NONE);
		excomposite.setClient(othersComposite);
		othersComposite.setLayout(new GridLayout(nColumns, false));

		label = UIMessages.CompilerConfigurationBlock_aj_unresolvable_member_label;
		addComboBox(othersComposite, label, PREF_AJ_UNRESOLVABLE_MEMBER,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = UIMessages.CompilerConfigurationBlock_aj_shadow_not_in_structure_label;
		addComboBox(othersComposite, label, PREF_AJ_SHADOW_NOT_IN_STRUCTURE,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = UIMessages.CompilerConfigurationBlock_aj_enable_weave_messages_label;
		addCheckBox(othersComposite, label, PREF_ENABLE_WEAVE_MESSAGES,
				enableDisableValues, 0);
		
		label = UIMessages.CompilerConfigurationBlock_aj_other_tabtitle;
		excomposite = createStyleSection(composite, label, nColumns);

		othersComposite = new Composite(excomposite, SWT.NONE);
		excomposite.setClient(othersComposite);
		othersComposite.setLayout(new GridLayout(nColumns, false));
		
		label = UIMessages.CompilerConfigurationBlock_aj_x_serializable_aspects_label;
		addCheckBox(othersComposite, label, PREF_ENABLE_SERIALIZABLE_ASPECTS,
				enableDisableValues, 0);

		label = UIMessages.CompilerConfigurationBlock_aj_x_not_reweavable_label;
		addCheckBox(othersComposite, label, PREF_ENABLE_NOT_REWEAVABLE,
				enableDisableValues, 0);

		label = UIMessages.CompilerConfigurationBlock_aj_x_has_member_label;
		addCheckBox(othersComposite, label, PREF_ENABLE_HAS_MEMBER,
				enableDisableValues, 0);

		label = UIMessages.CompilerConfigurationBlock_aj_out_xml;
		addCheckBox(othersComposite, label, PREF_ENABLE_OUTXML,
				enableDisableValues, 0);

		label = UIMessages.CompilerConfigurationBlock_aj_enable_incremental_label;
		addCheckBox(othersComposite, label, PREF_ENABLE_INCREMENTAL,
				enableDisableValues, 0, false);

		label = UIMessages.CompilerConfigurationBlock_aj_enable_build_asm_label;
		addCheckBox(othersComposite, label, PREF_ENABLE_BUILD_ASM,
				enableDisableValues, 0, false);

		Composite row3Comp = createRowComposite(othersComposite,2);

		//fills the editor with the stored preference if there is one.
		String currValue = "";
		if (isProjectPreferencePage()) {
			if (hasProjectSpecificOptions(getProject())) {
				currValue = AspectJPreferences.getStringPrefValue(getProject(), AspectJPreferences.COMPILER_OPTIONS);
			} else {
				currValue = getPreferenceStore().getString(AspectJPreferences.COMPILER_OPTIONS);
			}
		} else {
			currValue = getPreferenceStore().getString(AspectJPreferences.COMPILER_OPTIONS);
		}
		
		nonStandardOptionsEditor =
			new StringFieldEditor(
				currValue, //$NON-NLS-1$
				UIMessages.compilerPropsPage_nonStandardOptions,
				StringFieldEditor.UNLIMITED,
				row3Comp);

		nonStandardOptionsEditor.setStringValue(currValue);		

		IDialogSettings section= JavaPlugin.getDefault().getDialogSettings().getSection(SETTINGS_SECTION_NAME);
		restoreSectionExpansionStates(section);
		
		return pageContent;
	}

	/**
	 * Get the preference store for AspectJ mode
	 */
	protected IPreferenceStore doGetPreferenceStore() {
		return AspectJUIPlugin.getDefault().getPreferenceStore();
	}
	
	/**
	 * overriding performApply() for PreferencePageBuilder.aj
	 */
	public void performApply() {  
	    performOk();
	}
	
	/*
	 * (non-Javadoc) Method declared on PreferencePage
	 */
	public boolean performOk() {
		if (isProjectPreferencePage()) {
			return projectPerformOK();
		} else {
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
			
			boolean compilerChanges = false;
			String value = nonStandardOptionsEditor.getStringValue();
			if (!value.equals(store.getString(AspectJPreferences.COMPILER_OPTIONS))){
				store.setValue(AspectJPreferences.COMPILER_OPTIONS,nonStandardOptionsEditor.getStringValue());
				AJLog.log("Non Standard Compiler properties changed: " + store.getString(AspectJPreferences.COMPILER_OPTIONS)); //$NON-NLS-1$
				compilerChanges = true;
			}
			
			AspectJUIPlugin.getDefault().savePluginPreferences();

			if (lintChanges || advancedOrOtherChanges || compilerChanges) {
				boolean doBuild = false;
				String[] strings = getFullBuildDialogStrings();
				if (strings != null) {
					MessageDialog dialog = new MessageDialog(getShell(),
							strings[0], null, strings[1],
							MessageDialog.QUESTION, new String[] {
									IDialogConstants.YES_LABEL,
									IDialogConstants.NO_LABEL,
									IDialogConstants.CANCEL_LABEL }, 2);
					if (isTesting) {
						dialog.setBlockOnOpen(false);
					}
					int res = dialog.open();
					// simulate user input if we're testing
					if (isTesting) {
						// choices are "Yes", "No" or "Cancel"
						dialog.close();
						if (buildNow) {
							res = dialog.OK;
						} else {
							res = dialog.CANCEL; // simulating cancel or no being pressed.
						}
					}
					if (res == 0) {
						doBuild = true;
						projects= (ResourcesPlugin.getWorkspace().getRoot().getProjects());
					} else if (res != 1) {
						return false; // cancel pressed
					}
				}
				// PreferencePageBuilder handles building so
				// don't need to do it here
//				if (doBuild) {
//					doFullBuild();
//				}
			}
			
			return true;
		}
	}
	
	public IProject[] getProjects() {
		return projects;
	}

	/**
	 * Checks whether the project settings have changed and updates the store
	 * accordingly if there is a change.
	 */
	private boolean updateProjectSettings() {
		List tempComboBoxes = new ArrayList();
		tempComboBoxes.addAll(fComboBoxes);
		List tempCheckBoxes = new ArrayList();
		tempCheckBoxes.addAll(fCheckBoxes);

		boolean settingsChanged = false;

		for (int i = fComboBoxes.size() - 1; i >= 0; i--) {
			Combo curr = (Combo) fComboBoxes.get(i);
			ControlData data = (ControlData) curr.getData();
			String value = data.getValue(curr.getSelectionIndex());
			if (!value.equals(AspectJPreferences.getStringPrefValue(
					getProject(), data.getKey()))) {
				settingsChanged = true;
				setPrefValue(getProject(), data.getKey(), value);
			}
		}

		for (int i = fCheckBoxes.size() - 1; i >= 0; i--) {
			Button curr = (Button) fCheckBoxes.get(i);
			ControlData data = (ControlData) curr.getData();
			boolean value = curr.getSelection();
			if (value != getBooleanForString(AspectJPreferences
					.getStringPrefValue(getProject(), data.getKey()))) {
				settingsChanged = true;
				setPrefValue(getProject(), data.getKey(), value ? VALUE_TRUE
						: VALUE_FALSE);
			}
		}
		
		String value = nonStandardOptionsEditor.getStringValue();
		if (!value.equals(AspectJPreferences
				.getStringPrefValue(getProject(), AspectJPreferences.COMPILER_OPTIONS))){
			settingsChanged = true;
			setPrefValue(getProject(), AspectJPreferences.COMPILER_OPTIONS, value);
		}
	
		if (settingsChanged) {
			flushPrefs(getProject());
		}
		
		return settingsChanged;
	}

	private void flushPrefs(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.PLUGIN_ID);
		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}

	private void setPrefValue(IProject project, String key, String value) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.PLUGIN_ID);
		projectNode.put(key, value);
	}

	private boolean getBooleanForString(String stringPrefValue) {
		return stringPrefValue.equals(VALUE_TRUE);
	}

	/**
	 * Get the preference store for AspectJ mode
	 */
	protected String[] getProjectBuildDialogStrings() {
		String title = UIMessages.CompilerConfigurationBlock_needsbuild_title;
		String message = UIMessages.CompilerConfigurationBlock_needsprojectbuild_message;
		return new String[] { title, message };
	}

	private boolean projectPerformOK() {
		boolean projectSettingsChanged = updateProjectSettings();

		boolean projectWorkspaceChanges = false;
		if (AspectJPreferences.isUsingProjectSettings(getProject()) != useProjectSettings()) {
			projectWorkspaceChanges = true;
			// don't want to overwrite existing project settings
			// because have just set them in the above call to
			// updateProjectSettings();
			AspectJPreferences.setUsingProjectSettings(getProject(),
					useProjectSettings(), false);
		}

		AspectJUIPlugin.getDefault().savePluginPreferences();

		if (projectWorkspaceChanges
				|| (projectSettingsChanged && useProjectSettings())) {
			String[] strings = getProjectBuildDialogStrings();
			if (strings != null) {
				MessageDialog dialog = new MessageDialog(getShell(),
						strings[0], null, strings[1], MessageDialog.QUESTION,
						new String[] { IDialogConstants.YES_LABEL,
								IDialogConstants.NO_LABEL,
								IDialogConstants.CANCEL_LABEL }, 2);
				// if we're testing then we don't want to block
				// the dialog on open otherwise we require real user input
				// rather than being able to simulate it
				if (isTesting) {
					dialog.setBlockOnOpen(false);
				}
				int res = dialog.open();
				// simulate user input if we're testing
				if (isTesting) {
					// choices are "Yes", "No" or "Cancel"
					dialog.close();
					if (buildNow) {
						res = dialog.OK;
					} else {
						res = dialog.CANCEL; // simulating cancel or no being pressed.
					}
				}
				if ((res == 0)) {
					// by only setting compilerSettingsUpdated to be true here,
					// means that
					// the user wont select "don't want to build" here and then
					// get a build
					// from other pages.
				} else if (res != 1) {
					return false; // cancel pressed
				}
			}
		}
		return true;
	}

	protected String[] getFullBuildDialogStrings() {
		String title = UIMessages.CompilerConfigurationBlock_needsbuild_title;
		String message = UIMessages.CompilerConfigurationBlock_needsfullbuild_message;
		return new String[] { title, message };
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
								.setTaskName(UIMessages.OptionsConfigurationBlock_buildall_taskname);
						ResourcesPlugin.getWorkspace().build(
								IncrementalProjectBuilder.FULL_BUILD,
								new SubProgressMonitor(monitor, 2));
					} catch (CoreException e) {
						ErrorHandler
								.handleAJDTError(
										UIMessages.OptionsConfigurationBlock_builderror_message,
										e);
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InterruptedException e) {
			// cancelled by user
		} catch (InvocationTargetException e) {
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
		
		AJLog.log("Non Standard Compiler properties reset to default"); //$NON-NLS-1$
		nonStandardOptionsEditor.setStringValue(""); //$NON-NLS-1$

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

	private Composite createRowComposite(Composite parent, int numColumns)
	    {
	        Composite composite = new Composite(parent, SWT.NONE);

	        GridLayout layout = new GridLayout();
	        layout.numColumns = numColumns;
	        layout.makeColumnsEqualWidth = true;
	        composite.setLayout(layout);

	        GridData data = new GridData();
	        data.verticalAlignment = GridData.FILL;
	        data.horizontalAlignment = GridData.FILL;
	        data.horizontalSpan = 3;
	        composite.setLayoutData(data);

	        return composite;   
	    }

	protected Button addCheckBox(Composite parent, String label, String key,
			String[] values, int indent) {
		return addCheckBox(parent, label, key, values, indent, true);
	}

	protected Button addCheckBox(Composite parent, String label, String key,
			String[] values, int indent, boolean fillGridVertically) {
		ControlData data = new ControlData(key, values);

		int idx = label.indexOf("-"); //$NON-NLS-1$
		String optionname = label.substring(0, idx);
		String optiondesc = label.substring(idx + 1);
		optiondesc = optiondesc.trim();

		GridData gd = new GridData();
		if (fillGridVertically) {
			gd.verticalAlignment = GridData.FILL;
		}
		gd.horizontalSpan = 3;
		gd.horizontalIndent = indent;

		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(optionname);
		checkBox.setData(data);
		checkBox.setLayoutData(gd);
		Label l = new Label(parent, SWT.WRAP);
		l.setText(optiondesc);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent = 20;
		l.setLayoutData(gridData);
		createLabel(parent, "");// filler //$NON-NLS-1$
		boolean currValue;
		if (isProjectPreferencePage()) {
			if (hasProjectSpecificOptions(getProject())) {
				currValue = getBooleanForString(AspectJPreferences
						.getStringPrefValue(getProject(), key));
			} else {
				currValue = getPreferenceStore().getBoolean(key);
			}
		} else {
			currValue = getPreferenceStore().getBoolean(key);
		}
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

		Label placeHolder = new Label(parent, SWT.NONE);
		placeHolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Combo comboBox = new Combo(parent, SWT.READ_ONLY);
		comboBox.setItems(valueLabels);
		comboBox.setData(data); 
		comboBox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		String currValue = getPreferenceStore().getString(key);
		if ((currValue != null) && (currValue.length() > 0)) {
			comboBox.select(data.getSelection(currValue));
		}

		fComboBoxes.add(comboBox);
	}

	protected ScrolledPageContent getParentScrolledComposite(Control control) {
		Control parent = control.getParent();
		while (!(parent instanceof ScrolledPageContent) && parent != null) {
			parent = parent.getParent();
		}
		if (parent instanceof ScrolledPageContent) {
			return (ScrolledPageContent) parent;
		}
		return null;
	}

	protected ExpandableComposite getParentExpandableComposite(Control control) {
		Control parent = control.getParent();
		while (!(parent instanceof ExpandableComposite) && parent != null) {
			parent = parent.getParent();
		}
		if (parent instanceof ExpandableComposite) {
			return (ExpandableComposite) parent;
		}
		return null;
	}

	private void makeScrollableCompositeAware(Control control) {
		ScrolledPageContent parentScrolledComposite = getParentScrolledComposite(control);
		if (parentScrolledComposite != null) {
			parentScrolledComposite.adaptChild(control);
		}
	}

	protected ExpandableComposite createStyleSection(Composite parent,
			String label, int nColumns) {
		ExpandableComposite excomposite = new ExpandableComposite(parent,
				SWT.NONE, ExpandableComposite.TWISTIE
						| ExpandableComposite.CLIENT_INDENT);
		excomposite.setText(label);
		excomposite.setExpanded(false);
		excomposite.setFont(JFaceResources.getFontRegistry().getBold(
				JFaceResources.DIALOG_FONT));
		excomposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, false, nColumns, 1));
		excomposite.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				expandedStateChanged((ExpandableComposite) e.getSource());
			}
		});
		fExpandedComposites.add(excomposite);
		makeScrollableCompositeAware(excomposite);
		return excomposite;
	}

	protected final void expandedStateChanged(ExpandableComposite expandable) {
		ScrolledPageContent parentScrolledComposite = getParentScrolledComposite(expandable);
		if (parentScrolledComposite != null) {
			parentScrolledComposite.reflow(true);
		}
	}
	
	protected void restoreSectionExpansionStates(IDialogSettings settings) {
		for (int i= 0; i < fExpandedComposites.size(); i++) {
			ExpandableComposite excomposite= (ExpandableComposite) fExpandedComposites.get(i);
			if (settings == null) {
				excomposite.setExpanded(i == 0); // only expand the first node by default
			} else {
				excomposite.setExpanded(settings.getBoolean(SETTINGS_EXPANDED + String.valueOf(i)));
			}
		}
	}
	
	protected void storeSectionExpansionStates(IDialogSettings settings) {
		for (int i= 0; i < fExpandedComposites.size(); i++) {
			ExpandableComposite curr= (ExpandableComposite) fExpandedComposites.get(i);
			settings.put(SETTINGS_EXPANDED + String.valueOf(i), curr.isExpanded());
		}
	}

	protected Control createPreferenceContent(Composite parent) {
		Composite mainComp = new Composite(parent, SWT.NONE);
		mainComp.setFont(parent.getFont());
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mainComp.setLayout(layout);

		Composite othersComposite = createCompilerPreferencesContent(mainComp);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.heightHint= new PixelConverter(parent).convertHeightInCharsToPixels(20);
		othersComposite.setLayoutData(gridData);

		return mainComp;
	}

	protected boolean hasProjectSpecificOptions(IProject project) {
		return project != null
				&& AspectJPreferences.isUsingProjectSettings(project);
	}

	public static void setProjectDefaults(IEclipsePreferences projectNode) {
		for (int i = 0; i < keys.length; i++) {
			String value = (String) defaultValueMap.get(keys[i]);
			projectNode.put(keys[i], value);
		}
	}

	public static void setProjectDefaultsIfValueNotAlreadySet(
			IEclipsePreferences projectNode) {
		List existingKeysList = new ArrayList();
		try {
			existingKeysList = Arrays.asList(projectNode.keys());
		} catch (BackingStoreException e) {
		}
		for (int i = 0; i < keys.length; i++) {
			String value = (String) defaultValueMap.get(keys[i]);
			boolean keyExists = false;
			if (existingKeysList.contains(keys[i])) {
				keyExists = true;
			}
			if (!keyExists) {
				projectNode.put(keys[i], value);
			}
		}
	}

	public static void removeProjectValues(IEclipsePreferences projectNode) {
		for (int i = 0; i < keys.length; i++) {
			projectNode.remove(keys[i]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#getPreferencePageID()
	 */
	protected String getPreferencePageID() {
		return PREF_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#getPropertyPageID()
	 */
	protected String getPropertyPageID() {
		return PROP_ID;
	}

	// Override to make visible to PreferencePageBuilder aspect
	protected IProject getProject() {
		if (isTesting) {
			return project;
		}
		return super.getProject();
	}

	// Override to make it possible to be advised by PreferencePageBuilder
	// aspect
	public void dispose() {
		IDialogSettings settings= JavaPlugin.getDefault().getDialogSettings().addNewSection(SETTINGS_SECTION_NAME);
		storeSectionExpansionStates(settings);
		super.dispose();
	}

	// Override to make it possible to be advised by PreferencePageBuilder
	// aspect
	protected Control createContents(Composite parent) {
		return super.createContents(parent);
	}

	// Override to make it possible to be called by PreferencePageBuilder aspect
	protected boolean useProjectSettings() {
		if (isTesting) {
			return isProjectPreferencePage() && testingProjectSettings; 
		}
		return super.useProjectSettings();
	}

	// ---------------- methods and fields used for testing ----------------
	private IProject project;
	private boolean isTesting; 
	private boolean buildNow;
	private boolean testingProjectSettings;
	public void setProject(IProject project) {
		this.project = project;
	}
	public void setIsTesting(boolean isTesting) {
		this.isTesting = isTesting;
	}
	public void setIsUsingProjectSettings(boolean useProjectSettings) {
		testingProjectSettings = isTesting;
	}
	// changes one of the button values to simulate user input
	public void setButtonChanged() {
		Button curr = (Button) fCheckBoxes.get(0);
		boolean value = curr.getSelection();
		curr.setSelection(!value);
	}
	
	// override so we can use this in testing
	protected boolean isProjectPreferencePage() {
		if (isTesting) {
			return project != null;
		}
		return super.isProjectPreferencePage();
	}

	/** 
	 * Set whether or not to perform a build now - used to
	 * simulate user input in the "Compiler Settings Have Changed: do
	 * you want to build now" dialog. If want to answer "yes" then call
	 * this method with "true" otherwise, call with false.
	 */
	public void setBuildNow(boolean buildNow) {
		this.buildNow = buildNow;
	}
	
	/**
	 * Setting this to true enables us to simulate user input
	 * when testing
	 */
	public boolean isTesting(){
		return isTesting;
	}
	
	/**
	 * @return true if have set that want to build now in the "Compiler
	 * Settings Have Changed" dialog, false otherwise.
	 */
	public boolean isBuildNow() {
		return buildNow;
	}
	
	/**
	 * Sets the non standard options field to have the given contents
	 */
	public void setNonStandardOption(String option) {
		nonStandardOptionsEditor.setStringValue(option);
	}
}