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

import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.internal.WorkbenchPlugin;


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
		return UIMessages.ajEditor.equals(desc.getLabel());
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
			if (descriptor.getLabel().equals(UIMessages.ajEditor)) {
				return true;
			}
		}
		return false;
	}
}
