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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Holds the preferences and project specific properties for AspectJ as set via the Workbench->preferences
 * pages and the property pages accessible by right clicking a project.
 */
public class AspectJPreferences {


	/**
	 * Identifier for compiler options preference
	 */   
    public static final String COMPILER_OPTIONS= "org.eclipse.ajdt.core.compiler.nonStandardOptions";

    
	/**
	 * The jar containing the ajde plugin
	 */     
    public static final String AJDE_JAR= "ajde.jar";
    
	/**
	 * Identifier for whether it was the addAspectJNature() that added the ajde dependency
	 */   
    public static final String HAS_SET_AJPLUGIN_DEPENDENCY = "Has_Set_AJPlugin_Dependency";
    
    public static final String JAVA_OR_AJ_EXT = "aspectjPreferences.fileExt";

    public static final String ADVICE_DECORATOR = "aspectjPreferences.adviceDec";

    public static final String ACTIVE_CONFIG = "org.eclipse.ajdt.ui.activeBuildConfiguration";

    /**
	 * Identifier for outline view mode selection
	 */
	public static final String ASPECTJ_OUTLINE = "org.eclipse.ajdt.ui.ajoutline2";
	
	public static final String AUTOBUILD_SUPPRESSED = "org.eclipse.ajdt.ui.preferences.autobuildSuppressed";
	
	/**
	 * Identifier (key) for indication of whether AJDTPrefConfigWizard should
	 * be shown again.  If true, don't show.
	 */
	public static final String AJDT_PREF_CONFIG_DONE = "org.eclipse.ajdt.ui.preferences.ajdtPrefConfigDone";
	
	public static final String PREF_RUN_FOR_AJDT_VERSION = "org.eclipse.ajdt.ui.preferences.prefRunForAjdtVersion";

//	public static final String AJDT_PREF_RUN_120 = "org.eclipse.ajdt.ui.preferences.ajdtPrefRun120";
	
    public static final String PDE_AUTO_IMPORT_CONFIG_DONE = "org.eclipse.ajdt.ui.preferences.pdeAutoImportConfigDone";

    public static final String ASK_PDE_AUTO_IMPORT = "org.eclipse.ajdt.ui.preferences.askPdeAutoImport";

    public static final String DO_PDE_AUTO_IMPORT = "org.eclipse.ajdt.ui.preferences.doPdeAutoImport";
    
	public static final String VALUE_ERROR = JavaCore.ERROR;
	public static final String VALUE_WARNING = JavaCore.WARNING;
	public static final String VALUE_IGNORE = JavaCore.IGNORE;
	public static final String VALUE_ENABLED = JavaCore.ENABLED;
	public static final String VALUE_DISABLED = JavaCore.DISABLED;
	
	// project-scope preference to indicate if project-specific settings are in force
	public static final String OPTION_UseProjectSettings = "org.eclipse.ajdt.core.compiler.useProjectSettings";
	
	// AspectJ Lint options
	public static final String OPTION_ReportInvalidAbsoluteTypeName = "org.aspectj.ajdt.core.compiler.lint.InvalidAbsoluteTypeName"; 
	public static final String OPTION_ReportInvalidWildcardTypeName = "org.aspectj.ajdt.core.compiler.lint.WildcardTypeName"; 
	public static final String OPTION_ReportUnresolvableMember      = "org.aspectj.ajdt.core.compiler.lint.UnresolvableMember"; 
	public static final String OPTION_ReportTypeNotExposedToWeaver  = "org.aspectj.ajdt.core.compiler.lint.TypeNotExposedToWeaver"; 
	public static final String OPTION_ReportShadowNotInStructure    = "org.aspectj.ajdt.core.compiler.lint.ShadowNotInStructure"; 
	public static final String OPTION_ReportUnmatchedSuperTypeInCall    = "org.aspectj.ajdt.core.compiler.list.UnmatchedSuperTypeInCall";
	public static final String OPTION_ReportCannotImplementLazyTJP  = "org.aspectj.ajdt.core.compiler.lint.CannotImplementLazyTJP"; 
	public static final String OPTION_ReportNeedSerialVersionUIDField   = "org.aspectj.ajdt.core.compiler.lint.NeedSerialVersionUIDField"; 
	public static final String OPTION_ReportIncompatibleSerialVersion   = "org.aspectj.ajdt.core.compiler.lint.BrokeSerialVersionCompatibility";
	public static final String OPTION_ReportNoInterfaceCtorJoinpoint   = "org.aspectj.ajdt.core.compiler.lint.NoInterfaceCtorJoinpoint";

	// AspectJ 5 Options
	public static final String OPTION_1_5 = "org.aspectj.ajdt.core.compiler.aj5";
	
	// AspectJ 5 Lint Options
	public static final String OPTION_noJoinpointsForBridgeMethods = "org.aspectj.ajdt.core.compiler.lint.noJoinpointsForBridgeMethods";
	public static final String OPTION_cantMatchArrayTypeOnVarargs = "org.aspectj.ajdt.core.compiler.lint.cantMatchArrayTypeOnVarargs";
	public static final String OPTION_enumAsTargetForDecpIgnored = "org.aspectj.ajdt.core.compiler.lint.enumAsTargetForDecpIgnored";
	public static final String OPTION_annotationAsTargetForDecpIgnored = "org.aspectj.ajdt.core.compiler.lint.annotationAsTargetForDecpIgnored";
	
	
	// General AspectJ Compiler options
	public static final String OPTION_NoWeave                 = "org.aspectj.ajdt.core.compiler.weaver.NoWeave";
	public static final String OPTION_XSerializableAspects    = "org.aspectj.ajdt.core.compiler.weaver.XSerializableAspects";
	public static final String OPTION_XLazyThisJoinPoint      = "org.aspectj.ajdt.core.compiler.weaver.XLazyThisJoinPoint";
	public static final String OPTION_XNoInline               = "org.aspectj.ajdt.core.compiler.weaver.XNoInline";
	public static final String OPTION_XReweavable             = "org.aspectj.ajdt.core.compiler.weaver.XReweavable";
	public static final String OPTION_XReweavableCompress     = "org.aspectj.ajdt.core.compiler.weaver.XReweavableCompress";
	
	// Other compiler options
	public static final String OPTION_Incremental 			  = "org.aspectj.ajdt.core.compiler.BuildOptions.incrementalMode";
	public static final String OPTION_BuildASM				  = "org.aspectj.ajdt.core.compiler.BuildOptions.buildAsm";
	public static final String OPTION_WeaveMessages 		  = "org.aspectj.ajdt.core.compiler.BuildOptions.showweavemessages";
	
	// map preference keys to corresponding options for the properties file
	private static String[][] lintKeysName = {
			{ OPTION_ReportInvalidAbsoluteTypeName, "invalidAbsoluteTypeName" },
			{ OPTION_ReportInvalidWildcardTypeName, "invalidWildcardTypeName" },
			{ OPTION_ReportUnresolvableMember, "unresolvableMember" },
			{ OPTION_ReportTypeNotExposedToWeaver, "typeNotExposedToWeaver" },
			{ OPTION_ReportShadowNotInStructure, "shadowNotInStructure" },
			{ OPTION_ReportUnmatchedSuperTypeInCall, "unmatchedSuperTypeInCall" },
			{ OPTION_ReportCannotImplementLazyTJP, "canNotImplementLazyTjp" },
			{ OPTION_ReportNeedSerialVersionUIDField, "needsSerialVersionUIDField" },
			{ OPTION_ReportIncompatibleSerialVersion, "brokeSerialVersionCompatibility" },
			{ OPTION_ReportNoInterfaceCtorJoinpoint, "noInterfaceCtorJoinpoint" },
			{ OPTION_noJoinpointsForBridgeMethods, "noJoinpointsForBridgeMethods"},
			{ OPTION_cantMatchArrayTypeOnVarargs, "cantMatchArrayTypeOnVarargs"},
			{ OPTION_enumAsTargetForDecpIgnored, "enumAsTargetForDecpIgnored"},
			{ OPTION_annotationAsTargetForDecpIgnored, "annotationAsTargetForDecpIgnored"}
	};

	// name of the file to write the Xlint options to
	private static String XlintProperties = "Xlint.properties";

	/**
	 * A named preference that holds the path of the AJdoc command (tools.jar) 
	 * used by the AJdoc creation wizard.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 */ 
	public static final String AJDOC_COMMAND= "ajdocCommand"; //$NON-NLS-1$
		
	public static String getFileExt() {
		boolean javaOrAjExt = getJavaOrAjExt();
		return javaOrAjExt ? ".java" : ".aj";
	}
	
	private static boolean getJavaOrAjExt() {
		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(JAVA_OR_AJ_EXT);
	}
	
	public static String getLintOptions(IProject thisProject) {
		File optsFile = AspectJUIPlugin.getDefault().getStateLocation().append(XlintProperties).toFile();
		writeLintOptionsFile(thisProject, optsFile);
		return " -Xlintfile \""+optsFile+"\" ";	
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
    	IEclipsePreferences projectNode = projectScope.getNode(AspectJPlugin.PLUGIN_ID);
    	if (projectNode==null) {
     		return false;
    	}
    	return projectNode.getBoolean(OPTION_UseProjectSettings,false);		
	}
	
	public static void setUsingProjectSettings(IProject project, boolean isUsingProjectSettings) {
		IScopeContext projectScope = new ProjectScope(project);
    	IEclipsePreferences projectNode = projectScope.getNode(AspectJPlugin.PLUGIN_ID);
    	if (isUsingProjectSettings) {
    		projectNode.putBoolean(OPTION_UseProjectSettings,true);
    		CompilerPropertyPage.setDefaults(projectNode);
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

			for (int i=0; i<lintKeysName.length; i++) {
				String value = getStringPrefValue(thisProject,lintKeysName[i][0]);
				if(value.equals("")) { //catches and initializes uninitialized variables
//					store.setDefault(prefix+lintKeysName[i][0],VALUE_WARNING);
//					value = store.getString(prefix+lintKeysName[i][0]);
					value = VALUE_WARNING;
				}
				writer.write(lintKeysName[i][1]+" = "+value);
				writer.write(System.getProperty("line.separator"));				
			}
			writer.close();
		} catch (IOException e) {
		}
	}

	public static String getAdvancedOptions(IProject project) {
		String opts=" ";
		if (getBooleanPrefValue(project, OPTION_NoWeave)) {
			opts+="-XnoWeave ";
		}
		if (getBooleanPrefValue(project, OPTION_XSerializableAspects)) {
			opts+="-XserializableAspects ";
		}
		if (getBooleanPrefValue(project, OPTION_XLazyThisJoinPoint)) {
			opts+="-XlazyTjp ";
		}
		if (getBooleanPrefValue(project, OPTION_XNoInline)) {
			opts+="-XnoInline ";
		}
		if (getBooleanPrefValue(project, OPTION_XReweavable)) {
			opts+="-Xreweavable ";
		}
		if (getBooleanPrefValue(project, OPTION_XReweavableCompress)) {
			opts+="-Xreweavable:compress ";
		}
		if (getBooleanPrefValue(project, OPTION_1_5)) {
			opts+="-1.5 ";
		}
		return opts;
	}

	/**
	 * Helper get method used by AspectJPreference page
	 */
	static public boolean isAspectJOutlineEnabled() {
		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(ASPECTJ_OUTLINE);
	}

	static public boolean isAdviceDecoratorActive() {
		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(ADVICE_DECORATOR);
	}

	static public boolean isAutobuildSuppressed() {
		return false; // Bug 46653
//		IPreferenceStore store = AspectJPlugin.getDefault().getPreferenceStore();
//		return store.getBoolean(AUTOBUILD_SUPPRESSED);
	}
	
	/**
	 * Helper set method used by AJDTPrefConfigPage
	 * 
	 * @param done true if the AJDTPrefConfigWizard should not be shown 
	 * again (if user checked the "don't ask me again" box, we call this method
	 * with 'true")
	 */
	static public void setAJDTPrefConfigDone(boolean done, String version) {
		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
		store.setValue(AJDT_PREF_CONFIG_DONE, done);
		store.setValue(PREF_RUN_FOR_AJDT_VERSION, version);
	}
	
	/**
	 * Helper get method used by AJDTUtils to determine whether to show
	 * the AJDTPrefConfigWizard
	 * 
	 * @return boolean true if we should not show the AJDTPrefConfigWizard
	 * again (if user checked the "don't ask me again" box)
	 */
	static public boolean isAJDTPrefConfigDone(String version) {
		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
		String prefsAJDTVersion = store.getString(PREF_RUN_FOR_AJDT_VERSION);
		return version.equals(prefsAJDTVersion) ?  store.getBoolean(AJDT_PREF_CONFIG_DONE) : false; 
	}

	// whether or not the AJDT Peference config wizard is already showing
	private static boolean isShowing = false;
	
	/**
	 * This is used in the case of a CVS checkout of multiple projects
	 * only want to show the AJDT Preference Config Wizard once and not
	 * one copy for each AJ project checked out.
	 * 
	 */
	static public boolean isAJDTPrefConfigShowing() {
	    return isShowing;
	}

	/**
	 * This is used in the case of a CVS checkout of multiple projects
	 * only want to show the AJDT Preference Config Wizard once and not
	 * one copy for each AJ project checked out.
	 * 
	 */
	static public void setAJDTPrefConfigShowing(boolean showing) { 
	    isShowing = showing;
	}
	    
    /**
     * Helper set method
     * 
     * @param ask
     *            true if the user wants to be asked again about having
     *            auto import of aspectj runtime library upon adding
     *            aspectj nature to PDE project.
     */
    static public void setAskPDEAutoImport(boolean ask) {
        IPreferenceStore store =
            AspectJUIPlugin.getDefault().getPreferenceStore();
        store.setValue(ASK_PDE_AUTO_IMPORT, ask);
    }

    /**
     * Helper get method used to determine whether to ask the user if they want
     * to auto import the aspectj runtime library from the appropriate plugin. 
     *  
     * @return boolean true if user is to be asked
     */
    static public boolean askPDEAutoImport() {
        IPreferenceStore store =
            AspectJUIPlugin.getDefault().getPreferenceStore();
        return store.getBoolean(ASK_PDE_AUTO_IMPORT);
    }
    
    static public void setPDEAutoImportConfigDone(boolean done) {
        IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
        store.setValue(PDE_AUTO_IMPORT_CONFIG_DONE, done);
    }
    
    public static boolean isPDEAutoImportConfigDone() {
        IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();   
        return store.getBoolean(PDE_AUTO_IMPORT_CONFIG_DONE); 
    }

    static public void setDoPDEAutoImport(boolean doImport) {
        IPreferenceStore store =
            AspectJUIPlugin.getDefault().getPreferenceStore();
        store.setValue(DO_PDE_AUTO_IMPORT, doImport);
    }

    static public boolean doPDEAutoImport() {
        IPreferenceStore store =
            AspectJUIPlugin.getDefault().getPreferenceStore();
        return store.getBoolean(DO_PDE_AUTO_IMPORT);
    }

    // Project scope preferences
    
    public static String getActiveBuildConfigurationName(IProject project) {
     	IScopeContext projectScope = new ProjectScope(project);
    	IEclipsePreferences projectNode = projectScope.getNode(AspectJPlugin.PLUGIN_ID);
    	return projectNode.get(ACTIVE_CONFIG,"");
    }
    
    public static void setActiveBuildConfigurationName(IProject project, String configName) {
     	IScopeContext projectScope = new ProjectScope(project);
       	IEclipsePreferences projectNode = projectScope.getNode(AspectJUIPlugin.PLUGIN_ID);
      	projectNode.put(ACTIVE_CONFIG,configName);
      	if (configName.equals(BuildConfiguration.STANDARD_BUILD_CONFIGURATION_FILE)) {
      		projectNode.remove(ACTIVE_CONFIG);
      	}
       	try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
    }
    
	public static void setCompilerOptions(IProject project, String value) {
		IScopeContext projectScope = new ProjectScope(project);
       	IEclipsePreferences projectNode = projectScope.getNode(AspectJUIPlugin.PLUGIN_ID);
      	projectNode.put(COMPILER_OPTIONS,value);
      	if (value.equals("")) {
      		projectNode.remove(COMPILER_OPTIONS);
      	}
       	try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}	

	public static String getCompilerOptions(IProject project) {
    	IScopeContext projectScope = new ProjectScope(project);
    	IEclipsePreferences projectNode = projectScope.getNode(AspectJPlugin.PLUGIN_ID);
    	return projectNode.get(COMPILER_OPTIONS,"");
	}

	public static String getStringPrefValue(IProject project, String key) {
		if (isUsingProjectSettings(project)) {			
			IScopeContext projectScope = new ProjectScope(project);
    		IEclipsePreferences projectNode = projectScope.getNode(AspectJPlugin.PLUGIN_ID);
    		String v = projectNode.get(key,"");
    		return v;
		} else {
			IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
			return store.getString(key);
		}
	}

	private static boolean getBooleanPrefValue(IProject project, String key) {
		if (isUsingProjectSettings(project)) {			
			IScopeContext projectScope = new ProjectScope(project);
    		IEclipsePreferences projectNode = projectScope.getNode(AspectJPlugin.PLUGIN_ID);
    		boolean v = projectNode.getBoolean(key,false);
     		return v;
		} else {
			IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
			return store.getBoolean(key);
		}
	}

}
