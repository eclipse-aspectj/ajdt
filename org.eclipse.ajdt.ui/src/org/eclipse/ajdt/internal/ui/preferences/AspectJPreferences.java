/**********************************************************************
Copyright (c) 2002, 2004 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer, Andy Clement, Tracy Gardner - initial version
Julie Waterhouse - added methods to get/set ajdtPrefConfigDone - August 3, 2003
Matt Chapman - added support for Xlint and advanced options
Ian McGrath - added support for the properties page
**********************************************************************/
package org.eclipse.ajdt.internal.ui.preferences;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.core.resources.IProject;

/**
 * Holds the preferences and project specific properties for AspectJ as set via the Workbench->preferences
 * pages and the property pages accessible by right clicking a project.
 */
public class AspectJPreferences {
		

	/**
	 * Identifier for compiler options preference
	 */   
    public static final String COMPILER_OPTIONS= "aspectj.compiler.options.flags";

    
	/**
	 * The jar containing the ajde plugin
	 */     
    public static final String AJDE_JAR= "ajde.jar";
    
	/**
	 * Identifier for whether it was the addAspectJNature() that added the ajde dependency
	 */   
    public static final String HAS_SET_AJPLUGIN_DEPENDENCY = "Has_Set_AJPlugin_Dependency";
    
    public static final String JAVA_OR_AJ_EXT = "aspectjPreferences.fileExt";
	/**
	 * Identifier for outline view mode selection
	 */
	public static final String ASPECTJ_OUTLINE = "org.eclipse.ajdt.ui.ajoutline";
	
	public static final String AUTOBUILD_SUPPRESSED = "org.eclipse.ajdt.ui.preferences.autobuildSuppressed";
	
	/**
	 * Identifier (key) for indication of whether AJDTPrefConfigWizard should
	 * be shown again.  If true, don't show.
	 */
	public static final String AJDT_PREF_CONFIG_DONE = "org.eclipse.ajdt.ui.preferences.ajdtPrefConfigDone";
	
    public static final String PDE_AUTO_IMPORT_CONFIG_DONE = "org.eclipse.ajdt.ui.preferences.pdeAutoImportConfigDone";

    public static final String ASK_PDE_AUTO_IMPORT = "org.eclipse.ajdt.ui.preferences.askPdeAutoImport";

    public static final String DO_PDE_AUTO_IMPORT = "org.eclipse.ajdt.ui.preferences.doPdeAutoImport";
    
	private static final String WARNING = JavaCore.WARNING;
	
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
	
	// General AspectJ Compiler options
	public static final String OPTION_NoWeave                 = "org.aspectj.ajdt.core.compiler.weaver.NoWeave";
	public static final String OPTION_XSerializableAspects    = "org.aspectj.ajdt.core.compiler.weaver.XSerializableAspects";
	public static final String OPTION_XLazyThisJoinPoint      = "org.aspectj.ajdt.core.compiler.weaver.XLazyThisJoinPoint";
	public static final String OPTION_XNoInline               = "org.aspectj.ajdt.core.compiler.weaver.XNoInline";
	public static final String OPTION_XReweavable             = "org.aspectj.ajdt.core.compiler.weaver.XReweavable";
	public static final String OPTION_XReweavableCompress     = "org.aspectj.ajdt.core.compiler.weaver.XReweavableCompress";

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
			{ OPTION_ReportNoInterfaceCtorJoinpoint, "noInterfaceCtorJoinpoint" }
	};

	// name of the file to write the Xlint options to
	private static String XlintProperties = "Xlint.properties";
    
	/**
	 * Helper get method used by AspectJPreference page
	 */
	static public String getCompilerOptions() {
		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
		return store.getString(COMPILER_OPTIONS);
	}
	
	public static String getFileExt() {
		boolean javaOrAjExt = getJavaOrAjExt();
		return javaOrAjExt ? ".java" : ".aj";
	}
	
	private static boolean getJavaOrAjExt() {
		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(JAVA_OR_AJ_EXT);
	}
	
	/**
	 * Helper set method used by AspectJPreference page
	 */
	static public void setCompilerOptions(String value) {
		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
		store.setValue(COMPILER_OPTIONS, value);
	}	

	static public String getLintOptions(IProject thisProject) {
		File optsFile = AspectJUIPlugin.getDefault().getStateLocation().append(XlintProperties).toFile();
		writeLintOptionsFile(thisProject);
		String opts=" -Xlintfile \""+optsFile+"\" ";	
		return opts;
	}
	
	static private void writeLintOptionsFile(IProject thisProject) {
		File optsFile = AspectJUIPlugin.getDefault().getStateLocation().append(XlintProperties).toFile();
		String prefix; // is initialised to the the project name if it needs to use project specific options
		try {
			FileWriter writer = new FileWriter(optsFile);
			IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
			
			if(store.getBoolean(thisProject + "useProjectSettings"))
				prefix = thisProject.toString();
			else
				prefix="";
			for (int i=0; i<lintKeysName.length; i++) {
				String value = store.getString(prefix+lintKeysName[i][0]);
				if(value.equals("")) { //catches and initializes uninitialized variables
					store.setDefault(prefix+lintKeysName[i][0],WARNING);
					value = store.getString(prefix+lintKeysName[i][0]);
				}
				writer.write(lintKeysName[i][1]+" = "+value);
				writer.write(System.getProperty("line.separator"));				
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static public String getAdvancedOptions(IProject thisProject) {
		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
		String opts=" ";
		String prefix;
		if(store.getBoolean(thisProject + "useProjectSettings"))
			prefix = thisProject.toString();
		else
			prefix = "";
		if (store.getBoolean(prefix+OPTION_NoWeave)) {
			opts+="-XnoWeave ";
		}
		if (store.getBoolean(prefix+OPTION_XSerializableAspects)) {
			opts+="-XserializableAspects ";
		}
		if (store.getBoolean(prefix+OPTION_XLazyThisJoinPoint)) {
			opts+="-XlazyTjp ";
		}
		if (store.getBoolean(prefix+OPTION_XNoInline)) {
			opts+="-XnoInline ";
		}
		if (store.getBoolean(prefix+OPTION_XReweavable)) {
			opts+="-Xreweavable ";
		}
		if (store.getBoolean(prefix+OPTION_XReweavableCompress)) {
			opts+="-Xreweavable:compress ";
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
	static public void setAJDTPrefConfigDone(boolean done) {
		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
		store.setValue(AJDT_PREF_CONFIG_DONE, done);
	}
	
	/**
	 * Helper get method used by AJDTUtils to determine whether to show
	 * the AJDTPrefConfigWizard
	 * 
	 * @return boolean true if we should not show the AJDTPrefConfigWizard
	 * again (if user checked the "don't ask me again" box)
	 */
	static public boolean isAJDTPrefConfigDone() {
		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();	
		return store.getBoolean(AJDT_PREF_CONFIG_DONE); 
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
}
