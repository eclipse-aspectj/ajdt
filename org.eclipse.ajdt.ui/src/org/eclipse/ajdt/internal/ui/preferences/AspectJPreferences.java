/**********************************************************************
 Copyright (c) 2002, 2005 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 Contributors:
 Adrian Colyer, Andy Clement, Tracy Gardner - initial version
 Julie Waterhouse - added methods to get/set ajdtPrefConfigDone - August 3, 2003
 Matt Chapman - added support for Xlint and advanced options
 Ian McGrath - added support for the properties page
 Sian January - moved in other options and added 1.5 options
 Matt Chapman - added project scoped preferences (40446)
 **********************************************************************/
package org.eclipse.ajdt.internal.ui.preferences;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.ajdt.buildconfigurator.BuildConfiguration;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.CompilerPropertyPage;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Holds the preferences and project specific properties for AspectJ as set via
 * the Workbench->preferences pages and the property pages accessible by right
 * clicking a project.
 */
public class AspectJPreferences {

	/**
	 * Identifier for compiler options preference
	 */
	public static final String COMPILER_OPTIONS = "org.eclipse.ajdt.core.compiler.nonStandardOptions"; //$NON-NLS-1$

	/**
	 * The jar containing the ajde plugin
	 */
	public static final String AJDE_JAR = "ajde.jar"; //$NON-NLS-1$

	public static final String JAVA_OR_AJ_EXT = "aspectjPreferences.fileExt"; //$NON-NLS-1$

	public static final String ADVICE_DECORATOR = "aspectjPreferences.adviceDec"; //$NON-NLS-1$

	public static final String ACTIVE_CONFIG = "org.eclipse.ajdt.ui.activeBuildConfiguration"; //$NON-NLS-1$

	/**
	 * Identifier for outline view mode selection
	 */
	public static final String ASPECTJ_OUTLINE = "org.eclipse.ajdt.ui.ajoutline2"; //$NON-NLS-1$

	public static final String AUTOBUILD_SUPPRESSED = "org.eclipse.ajdt.ui.preferences.autobuildSuppressed"; //$NON-NLS-1$

	/**
	 * Identifier (key) for indication of whether AJDTPrefConfigWizard should be
	 * shown again. If true, don't show.
	 */
	public static final String AJDT_PREF_CONFIG_DONE = "org.eclipse.ajdt.ui.preferences.ajdtPrefConfigDone"; //$NON-NLS-1$

	public static final String PERFORM_AUTO_BUILDER_MIGRATION = "org.eclipse.ajdt.ui.preferences.perform.auto.migration";

	public static final String AUTO_BUILDER_MIGRATION_SETTING = "org.eclipse.ajdt.ui.preferences.auto.migration.setting";

	public static final String PDE_AUTO_IMPORT_CONFIG_DONE = "org.eclipse.ajdt.ui.preferences.pdeAutoImportConfigDone"; //$NON-NLS-1$

	public static final String PDE_AUTO_REMOVE_IMPORT_CONFIG_DONE = "org.eclipse.ajdt.ui.preferences.pdeAutoRemoveImportConfigDone"; //$NON-NLS-1$

	public static final String ASK_PDE_AUTO_IMPORT = "org.eclipse.ajdt.ui.preferences.askPdeAutoImport"; //$NON-NLS-1$

	public static final String DO_PDE_AUTO_IMPORT = "org.eclipse.ajdt.ui.preferences.doPdeAutoImport"; //$NON-NLS-1$

	public static final String ASK_PDE_AUTO_REMOVE_IMPORT = "org.eclipse.ajdt.ui.preferences.askPdeAutoRemoveImport"; //$NON-NLS-1$

	public static final String DO_PDE_AUTO_REMOVE_IMPORT = "org.eclipse.ajdt.ui.preferences.doPdeAutoRemoveImport"; //$NON-NLS-1$

	public static final String VALUE_ERROR = JavaCore.ERROR;

	public static final String VALUE_WARNING = JavaCore.WARNING;

	public static final String VALUE_IGNORE = JavaCore.IGNORE;

	public static final String VALUE_ENABLED = JavaCore.ENABLED;

	public static final String VALUE_DISABLED = JavaCore.DISABLED;

	// project-scope preference to indicate if project-specific settings are in
	// force
	public static final String OPTION_UseProjectSettings = "org.eclipse.ajdt.core.compiler.useProjectSettings"; //$NON-NLS-1$

	// AspectJ Lint options
	public static final String OPTION_ReportInvalidAbsoluteTypeName = "org.aspectj.ajdt.core.compiler.lint.InvalidAbsoluteTypeName"; //$NON-NLS-1$

	public static final String OPTION_ReportInvalidWildcardTypeName = "org.aspectj.ajdt.core.compiler.lint.WildcardTypeName"; //$NON-NLS-1$

	public static final String OPTION_ReportUnresolvableMember = "org.aspectj.ajdt.core.compiler.lint.UnresolvableMember"; //$NON-NLS-1$

	public static final String OPTION_ReportTypeNotExposedToWeaver = "org.aspectj.ajdt.core.compiler.lint.TypeNotExposedToWeaver"; //$NON-NLS-1$

	public static final String OPTION_ReportShadowNotInStructure = "org.aspectj.ajdt.core.compiler.lint.ShadowNotInStructure"; //$NON-NLS-1$

	public static final String OPTION_ReportUnmatchedSuperTypeInCall = "org.aspectj.ajdt.core.compiler.list.UnmatchedSuperTypeInCall"; //$NON-NLS-1$

	public static final String OPTION_ReportCannotImplementLazyTJP = "org.aspectj.ajdt.core.compiler.lint.CannotImplementLazyTJP"; //$NON-NLS-1$

	public static final String OPTION_ReportNeedSerialVersionUIDField = "org.aspectj.ajdt.core.compiler.lint.NeedSerialVersionUIDField"; //$NON-NLS-1$

	public static final String OPTION_ReportIncompatibleSerialVersion = "org.aspectj.ajdt.core.compiler.lint.BrokeSerialVersionCompatibility"; //$NON-NLS-1$

	public static final String OPTION_ReportNoInterfaceCtorJoinpoint = "org.aspectj.ajdt.core.compiler.lint.NoInterfaceCtorJoinpoint"; //$NON-NLS-1$

	// AspectJ 5 Lint Options
	public static final String OPTION_noJoinpointsForBridgeMethods = "org.aspectj.ajdt.core.compiler.lint.noJoinpointsForBridgeMethods"; //$NON-NLS-1$

	public static final String OPTION_cantMatchArrayTypeOnVarargs = "org.aspectj.ajdt.core.compiler.lint.cantMatchArrayTypeOnVarargs"; //$NON-NLS-1$

	public static final String OPTION_enumAsTargetForDecpIgnored = "org.aspectj.ajdt.core.compiler.lint.enumAsTargetForDecpIgnored"; //$NON-NLS-1$

	public static final String OPTION_annotationAsTargetForDecpIgnored = "org.aspectj.ajdt.core.compiler.lint.annotationAsTargetForDecpIgnored"; //$NON-NLS-1$

	public static final String OPTION_adviceDidNotMatch = "org.aspectj.ajdt.core.compiler.lint.adviceDidNotMatch"; //$NON-NLS-1$

	public static final String OPTION_invalidTargetForAnnotation = "org.aspectj.ajdt.core.compiler.lint.invalidTargetForAnnotation"; //$NON-NLS-1$

	public static final String OPTION_elementAlreadyAnnotated = "org.aspectj.ajdt.core.compiler.lint.elementAlreadyAnnotated"; //$NON-NLS-1$

	public static final String OPTION_runtimeExceptionNotSoftened = "org.aspectj.ajdt.core.compiler.lint.runtimeExceptionNotSoftened"; //$NON-NLS-1$
		
	// General AspectJ Compiler options
	public static final String OPTION_NoWeave = "org.aspectj.ajdt.core.compiler.weaver.NoWeave"; //$NON-NLS-1$

	public static final String OPTION_XSerializableAspects = "org.aspectj.ajdt.core.compiler.weaver.XSerializableAspects"; //$NON-NLS-1$

	public static final String OPTION_XLazyThisJoinPoint = "org.aspectj.ajdt.core.compiler.weaver.XLazyThisJoinPoint"; //$NON-NLS-1$

	public static final String OPTION_XNoInline = "org.aspectj.ajdt.core.compiler.weaver.XNoInline"; //$NON-NLS-1$

	public static final String OPTION_XReweavable = "org.aspectj.ajdt.core.compiler.weaver.XReweavable"; //$NON-NLS-1$

	public static final String OPTION_XReweavableCompress = "org.aspectj.ajdt.core.compiler.weaver.XReweavableCompress"; //$NON-NLS-1$

	// Other compiler options
	public static final String OPTION_Incremental = "org.aspectj.ajdt.core.compiler.BuildOptions.incrementalMode"; //$NON-NLS-1$

	public static final String OPTION_BuildASM = "org.aspectj.ajdt.core.compiler.BuildOptions.buildAsm"; //$NON-NLS-1$

	public static final String OPTION_WeaveMessages = "org.aspectj.ajdt.core.compiler.BuildOptions.showweavemessages"; //$NON-NLS-1$

	// map preference keys to corresponding options for the properties file
	private static String[][] lintKeysName = {
			{ OPTION_ReportInvalidAbsoluteTypeName, "invalidAbsoluteTypeName" }, //$NON-NLS-1$
			{ OPTION_ReportInvalidWildcardTypeName, "invalidWildcardTypeName" }, //$NON-NLS-1$
			{ OPTION_ReportUnresolvableMember, "unresolvableMember" }, //$NON-NLS-1$
			{ OPTION_ReportTypeNotExposedToWeaver, "typeNotExposedToWeaver" }, //$NON-NLS-1$
			{ OPTION_ReportShadowNotInStructure, "shadowNotInStructure" }, //$NON-NLS-1$
			{ OPTION_ReportUnmatchedSuperTypeInCall, "unmatchedSuperTypeInCall" }, //$NON-NLS-1$
			{ OPTION_ReportCannotImplementLazyTJP, "canNotImplementLazyTjp" }, //$NON-NLS-1$
			{ OPTION_ReportNeedSerialVersionUIDField,
					"needsSerialVersionUIDField" }, //$NON-NLS-1$
			{ OPTION_ReportIncompatibleSerialVersion,
					"brokeSerialVersionCompatibility" }, //$NON-NLS-1$
			{ OPTION_ReportNoInterfaceCtorJoinpoint, "noInterfaceCtorJoinpoint" }, //$NON-NLS-1$
			{ OPTION_noJoinpointsForBridgeMethods,
					"noJoinpointsForBridgeMethods" }, //$NON-NLS-1$
			{ OPTION_cantMatchArrayTypeOnVarargs, "cantMatchArrayTypeOnVarargs" }, //$NON-NLS-1$
			{ OPTION_enumAsTargetForDecpIgnored, "enumAsTargetForDecpIgnored" }, //$NON-NLS-1$
			{ OPTION_annotationAsTargetForDecpIgnored,
					"annotationAsTargetForDecpIgnored" }, //$NON-NLS-1$
			{ OPTION_adviceDidNotMatch, "adviceDidNotMatch" }, //$NON-NLS-1$
			{ OPTION_invalidTargetForAnnotation, "invalidTargetForAnnotation" }, //$NON-NLS-1$
			{ OPTION_elementAlreadyAnnotated, "elementAlreadyAnnotated" }, //$NON-NLS-1$
			{ OPTION_runtimeExceptionNotSoftened, "runtimeExceptionNotSoftened" } //$NON-NLS-1$
	};

	// name of the file to write the Xlint options to
	private static String XlintProperties = "Xlint.properties"; //$NON-NLS-1$

	/**
	 * A named preference that holds the path of the AJdoc command (tools.jar)
	 * used by the AJdoc creation wizard.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 */
	public static final String AJDOC_COMMAND = "ajdocCommand"; //$NON-NLS-1$

	// migration wizard options	
	public static final String NEVER_RUN_MIGRATION_WIZARD = "neverRunMigrationWizard"; //$NON-NLS-1$
	public static final String DONE_AUTO_OPEN_XREF_VIEW = "doneAutoOpenXRefView"; //$NON-NLS-1$
	// have we run the migration wizard for this workspace or for this session?
	private static boolean doNotRunMigrationWizard = false;
	// is the migration wizard running? (to prevent multiple copies popping up)
	private static boolean migrationWizardIsRunning = false;
		
	public static String getFileExt() {
		// only use .aj for now
		return ".aj"; //$NON-NLS-1$
//		boolean javaOrAjExt = getJavaOrAjExt();
//		return javaOrAjExt ? ".java" : ".aj"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static boolean getJavaOrAjExt() {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		return store.getBoolean(JAVA_OR_AJ_EXT);
	}

	public static String getLintOptions(IProject thisProject) {
		File optsFile = AspectJUIPlugin.getDefault().getStateLocation().append(
				XlintProperties).toFile();
		writeLintOptionsFile(thisProject, optsFile);
		return " -Xlintfile \"" + optsFile + "\" "; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static boolean getShowWeaveMessagesOption(IProject thisProject) {
		return getBooleanPrefValue(thisProject, OPTION_WeaveMessages);
	}

	public static boolean getBuildASMOption(IProject thisProject) {
		return getBooleanPrefValue(thisProject, OPTION_BuildASM);
	}

	public static boolean getIncrementalOption(IProject thisProject) {
		return getBooleanPrefValue(thisProject, OPTION_Incremental);
	}

	public static boolean isUsingProjectSettings(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.PLUGIN_ID);
		if (projectNode == null) {
			return false;
		}
		return projectNode.getBoolean(OPTION_UseProjectSettings, false);
	}

	public static void setUsingProjectSettings(IProject project,
			boolean isUsingProjectSettings) {
		setUsingProjectSettings(project,isUsingProjectSettings,true);
	}
	
	public static void setUsingProjectSettings(IProject project,
			boolean isUsingProjectSettings,
			boolean overwriteExistingProjectSettings) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.PLUGIN_ID);
		if (isUsingProjectSettings) {
			projectNode.putBoolean(OPTION_UseProjectSettings, true);
			if (overwriteExistingProjectSettings) {
				CompilerPropertyPage.setDefaults(projectNode);
			} else {
				CompilerPropertyPage.setDefaultsIfValueNotAlreadySet(projectNode);
			}						
		} else {
			projectNode.remove(OPTION_UseProjectSettings);
			CompilerPropertyPage.removeValues(projectNode);
		}
		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}

	private static void writeLintOptionsFile(IProject thisProject, File optsFile) {
		try {
			FileWriter writer = new FileWriter(optsFile);

			for (int i = 0; i < lintKeysName.length; i++) {
				String value = getStringPrefValue(thisProject,
						lintKeysName[i][0]);
				if (value.equals("")) {  //$NON-NLS-1$
					// catches and initializes uninitialized variables
				// store.setDefault(prefix+lintKeysName[i][0],VALUE_WARNING);
				// value = store.getString(prefix+lintKeysName[i][0]);
					value = VALUE_WARNING;
				}
				writer.write(lintKeysName[i][1] + " = " + value); //$NON-NLS-1$
				writer.write(System.getProperty("line.separator")); //$NON-NLS-1$
			}
			writer.close();
		} catch (IOException e) {
		}
	}

	public static String getAdvancedOptions(IProject project) {
		String opts = " "; //$NON-NLS-1$
		if (getBooleanPrefValue(project, OPTION_NoWeave)) {
			opts += "-XnoWeave "; //$NON-NLS-1$
		}
		if (getBooleanPrefValue(project, OPTION_XSerializableAspects)) {
			opts += "-XserializableAspects "; //$NON-NLS-1$
		}
		if (getBooleanPrefValue(project, OPTION_XLazyThisJoinPoint)) {
			opts += "-XlazyTjp "; //$NON-NLS-1$
		}
		if (getBooleanPrefValue(project, OPTION_XNoInline)) {
			opts += "-XnoInline "; //$NON-NLS-1$
		}
		if (getBooleanPrefValue(project, OPTION_XReweavable)) {
			opts += "-Xreweavable "; //$NON-NLS-1$
		}
		if (getBooleanPrefValue(project, OPTION_XReweavableCompress)) {
			opts += "-Xreweavable:compress "; //$NON-NLS-1$
		}
		return opts;
	}

	/**
	 * Helper get method used by AspectJPreference page
	 */
	static public boolean isAspectJOutlineEnabled() {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		return store.getBoolean(ASPECTJ_OUTLINE);
	}

	static public boolean isAdviceDecoratorActive() {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		return store.getBoolean(ADVICE_DECORATOR);
	}

	static public boolean isAutobuildSuppressed() {
		return false; // Bug 46653
		// IPreferenceStore store =
		// AspectJPlugin.getDefault().getPreferenceStore();
		// return store.getBoolean(AUTOBUILD_SUPPRESSED);
	}

	// preferences relating to migration of ajdt eclipse builder
	
	public static boolean isAutoBuilderMigrationEnabled() {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		return store.getBoolean(PERFORM_AUTO_BUILDER_MIGRATION);
	}

	public static void setAutoBuilderMigrationEnabled(boolean enabled) {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		store.setValue(PERFORM_AUTO_BUILDER_MIGRATION, enabled);
	}

	public static boolean isAutoBuilderMigrationSetToRemoveOldBuilder() {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		return store.getBoolean(AUTO_BUILDER_MIGRATION_SETTING);
	}

	public static void setAutoBuilderMigrationRemoveOldBuilder(boolean remove) {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		store.setValue(AUTO_BUILDER_MIGRATION_SETTING, remove);
	}


	/**
	 * Helper set method
	 * 
	 * @param ask
	 *            true if the user wants to be asked again about having auto
	 *            import of aspectj runtime library upon adding aspectj nature
	 *            to PDE project.
	 */
	static public void setAskPDEAutoImport(boolean ask) {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		store.setValue(ASK_PDE_AUTO_IMPORT, ask);
	}

	/**
	 * Helper get method used to determine whether to ask the user if they want
	 * to auto import the aspectj runtime library from the appropriate plugin.
	 * 
	 * @return boolean true if user is to be asked
	 */
	static public boolean askPDEAutoImport() {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		return store.getBoolean(ASK_PDE_AUTO_IMPORT);
	}

	static public void setPDEAutoImportConfigDone(boolean done) {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		store.setValue(PDE_AUTO_IMPORT_CONFIG_DONE, done);
	}

	public static boolean isPDEAutoImportConfigDone() {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		return store.getBoolean(PDE_AUTO_IMPORT_CONFIG_DONE);
	}

	static public void setDoPDEAutoImport(boolean doImport) {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		store.setValue(DO_PDE_AUTO_IMPORT, doImport);
	}

	static public boolean doPDEAutoImport() {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		return store.getBoolean(DO_PDE_AUTO_IMPORT);
	}

	/**
	 * Helper set method
	 * 
	 * @param ask
	 *            true if the user wants to be asked again about having auto
	 *            removal of aspectj runtime library import upon removing aspectj nature
	 *            from PDE projects.
	 */
	static public void setAskPDEAutoRemoveImport(boolean ask) {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		store.setValue(ASK_PDE_AUTO_REMOVE_IMPORT, ask);
	}
	
	/**
	 * Helper get method used to determine whether to ask the user if they want
	 * to automatically remove the the aspectj runtime library import from the 
	 * appropriate plugin.
	 * 
	 * @return boolean true if user is to be asked
	 */
	static public boolean askPDEAutoRemoveImport() {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		return store.getBoolean(ASK_PDE_AUTO_REMOVE_IMPORT);
	}

	static public void setPDEAutoRemoveImportConfigDone(boolean done) {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		store.setValue(PDE_AUTO_REMOVE_IMPORT_CONFIG_DONE, done);
	}

	public static boolean isPDEAutoRemoveImportConfigDone() {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		return store.getBoolean(PDE_AUTO_REMOVE_IMPORT_CONFIG_DONE);
	}

	static public void setDoPDEAutoRemoveImport(boolean doImport) {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		store.setValue(DO_PDE_AUTO_REMOVE_IMPORT, doImport);
	}

	static public boolean doPDEAutoRemoveImport() {
		IPreferenceStore store = AspectJUIPlugin.getDefault()
				.getPreferenceStore();
		return store.getBoolean(DO_PDE_AUTO_REMOVE_IMPORT);
	}
	
	// Project scope preferences

	public static String getActiveBuildConfigurationName(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJUIPlugin.PLUGIN_ID);
		return projectNode.get(ACTIVE_CONFIG, ""); //$NON-NLS-1$
	}

	public static void setActiveBuildConfigurationName(IProject project,
			String configName) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJUIPlugin.PLUGIN_ID);
		projectNode.put(ACTIVE_CONFIG, configName);
		if (configName
				.equals(BuildConfiguration.STANDARD_BUILD_CONFIGURATION_FILE)) {
			projectNode.remove(ACTIVE_CONFIG);
		}
		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}

	public static void setCompilerOptions(IProject project, String value) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.PLUGIN_ID);
		projectNode.put(COMPILER_OPTIONS, value);
		if (value.length()==0) {
			projectNode.remove(COMPILER_OPTIONS);
		}
		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}

	public static String getCompilerOptions(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.PLUGIN_ID);
		return projectNode.get(COMPILER_OPTIONS, ""); //$NON-NLS-1$
	}
	
	public static String getStringPrefValue(IProject project, String key) {
		if (isUsingProjectSettings(project)) {
			IScopeContext projectScope = new ProjectScope(project);
			IEclipsePreferences projectNode = projectScope
					.getNode(AspectJPlugin.PLUGIN_ID);
			String v = projectNode.get(key, ""); //$NON-NLS-1$
			return v;
		} else {
			IPreferenceStore store = AspectJUIPlugin.getDefault()
					.getPreferenceStore();
			return store.getString(key);
		}
	}

	private static boolean getBooleanPrefValue(IProject project, String key) {
		if (isUsingProjectSettings(project)) {
			IScopeContext projectScope = new ProjectScope(project);
			IEclipsePreferences projectNode = projectScope
					.getNode(AspectJPlugin.PLUGIN_ID);
			boolean v = projectNode.getBoolean(key, false);
			return v;
		} else {
			IPreferenceStore store = AspectJUIPlugin.getDefault()
					.getPreferenceStore();
			return store.getBoolean(key);
		}
	}
	
	public static boolean dontRunMigrationWizard() {
		// TODO: activate the migration wizard when ready
//	    return true; 
	    return doNotRunMigrationWizard;
	}
	
	public static void setDontRunMigrationWizard(boolean hasRun) {
	    doNotRunMigrationWizard = hasRun;
	}

	public static boolean migrationWizardIsRunning() {
	    return migrationWizardIsRunning;
	}
	
	public static void setMigrationWizardIsRunning(boolean isRunning) {
	    migrationWizardIsRunning = isRunning;
	}
}
