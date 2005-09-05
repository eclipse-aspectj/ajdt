/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Julie Waterhouse - initial version
Julie Waterhouse - removed methods for new aspect and AspectJ project.  
This functionality has moved to the plugin.xml. - Aug 13, 2003
...
**********************************************************************/
package org.eclipse.ajdt.internal.ui;

import java.util.Hashtable;

import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;


/**
 * Contains methods to set and query the workbench and perspective settings
 * specific to the configuration of AJDT, specifically:
 * 1. disable analyze annotations
 * 2. disable unused imports warning
 * 3. make the AspectJ editor the default for .java files
 */
public class AJDTConfigSettings {	
	/**
	 * Query whether the editor preference to analyze annotations
	 * has already been turned off
	 * @return boolean true if analyze annotations has been disabled
	 */
	static public boolean isAnalyzeAnnotationsDisabled() {
		IPreferenceStore store = JavaPlugin.getDefault().getPreferenceStore();		
		return (store.getBoolean(PreferenceConstants.EDITOR_EVALUTE_TEMPORARY_PROBLEMS) == false) ? true : false;		
	}
	
	/**
	 * Set the editor preference to turn off analyze annotations
	 */
	static public void disableAnalyzeAnnotations(boolean disable) {
		IPreferenceStore store = JavaPlugin.getDefault().getPreferenceStore();		
		store.setValue(PreferenceConstants.EDITOR_EVALUTE_TEMPORARY_PROBLEMS, !disable);		
	}
	
	/**
	 * Query whether the compiler preference for unused imports has already been
	 * set to "ignore"
	 * @return boolean true if unused imports option already set to "ignore"
	 */
	static public boolean isUnusedImportsDisabled() {
		Hashtable map = JavaCore.getOptions();
		return ((String)map.get(JavaCore.COMPILER_PB_UNUSED_IMPORT)).equals(JavaCore.IGNORE) ? true : false;
	}
	
	/**
	 * Set the compiler preference for unused imports to "ignore"
	 */
	static public void disableUnusedImports() {
		Hashtable map = JavaCore.getOptions();
		map.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.IGNORE);
		JavaCore.setOptions(map);
	}
	
	/**
	 * Set the compiler preference for unused imports to "warning"
	 */
	static public void enableUnusedImports() {
		Hashtable map = JavaCore.getOptions();
		map.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.WARNING);
		JavaCore.setOptions(map);
	}
	
	/**
	 * Query whether the workbench file associations preference has already been
	 * set to make the AspectJ editor the default for .java files
	 * @return boolean true if AspectJ editor is the default for .java files
	 */
	static public boolean isAspectJEditorDefault() {
		IEditorRegistry editorRegistry = WorkbenchPlugin.getDefault().getEditorRegistry();
		IEditorDescriptor desc = editorRegistry.getDefaultEditor("*.java"); //$NON-NLS-1$
		return AspectJUIPlugin.getResourceString("ajEditor").equals(desc.getLabel()); //$NON-NLS-1$
	}
	
	/*
	 * IMPORTANT NOTE: This method contains a HACK.  In two cases, an interface
	 * is cast to the underlying type to enable a call to be made, since to public 
	 * API exists that accepts the interface.  In the one case (cast to do the 
	 * save), the code is copied from org.eclipse.internal.ui.dialogs.FileEditorsPreferencePage.performOk()
	 * where the same hack is done for the same reason.
	 */
	/**  
	 * Set the workbench file associations preference to make the AspectJ editor 
	 * the default for .java files
	 */
	static public void setDefaultEditorForJavaFiles(boolean aspectJ) {
		
		EditorRegistry editorRegistry = (EditorRegistry)WorkbenchPlugin.getDefault().getEditorRegistry(); // HACK: cast to allow save to be called
		IFileEditorMapping[] array = WorkbenchPlugin.getDefault().getEditorRegistry().getFileEditorMappings();
		editorRegistry.setFileEditorMappings((FileEditorMapping[])array); // HACK: cast to allow set to be called
		String defaultEditor = editorRegistry.getDefaultEditor("*.java").getId(); //$NON-NLS-1$
		if(aspectJ) {
			if(!(defaultEditor.equals(AspectJEditor.ASPECTJ_EDITOR_ID))) {
				editorRegistry.setDefaultEditor("*.java", AspectJEditor.ASPECTJ_EDITOR_ID); //$NON-NLS-1$
				editorRegistry.saveAssociations();
			}
		} else {
			if(!(defaultEditor.equals(JavaUI.ID_CU_EDITOR))) {
				editorRegistry.setDefaultEditor("*.java", JavaUI.ID_CU_EDITOR); //$NON-NLS-1$
				editorRegistry.saveAssociations();
			}
		}	
	}

	/**
	 * Query whether the workbench file associations preference has been
	 * set to associate the AspectJ editor with .java files
	 * @return boolean true if AspectJ editor is the associated with .java files
	 */
	static public boolean isAspectJEditorAssociatedWithJavaFiles() {
		IEditorRegistry editorRegistry = WorkbenchPlugin.getDefault().getEditorRegistry();
		IEditorDescriptor[] desc = editorRegistry.getEditors("*.java"); //$NON-NLS-1$
		for (int i = 0; i < desc.length; i++) {
			IEditorDescriptor descriptor = desc[i];
			if (descriptor.getLabel().equals(AspectJUIPlugin.getResourceString("ajEditor"))) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}
}
