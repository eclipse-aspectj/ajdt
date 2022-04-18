/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.tracing;

import java.util.List;

import org.aspectj.bridge.Version;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.builder.AJBuilder;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

public class DebugTracing {

	/**
	 * General debug trace for the plug-in enabled through the master trace
	 * switch.
	 */
	public static boolean DEBUG = false;

	/**
	 * More detailed trace for the compiler - mainly output from ajde
	 */
	public static boolean DEBUG_COMPILER = true;

	/**
	 * Progress information for the compiler
	 */
	public static boolean DEBUG_COMPILER_PROGRESS = false;

	/**
	 * More detailed trace for compiler task list messages
	 */
	public static boolean DEBUG_COMPILER_MESSAGES = false;

	/**
	 * More detailed trace for the project builder
	 */
	public static boolean DEBUG_BUILDER = true;

	/**
	 * More detailed trace for project classpaths
	 */
	public static boolean DEBUG_BUILDER_CLASSPATH = false;

    /**
     * More detailed trace for the parser
     */
    public static boolean DEBUG_PARSER = true;

    /**
     * Enable/disable model sanity checking
     */
    public static boolean DEBUG_MODEL = true;

    public static final String[] categoryNames = new String[]{
		UIMessages.eventTrace_category_compiler,
		UIMessages.eventTrace_category_compiler_progress,
		UIMessages.eventTrace_category_compiler_messages,
		UIMessages.eventTrace_category_builder,
		UIMessages.eventTrace_category_builder_classpath,
		UIMessages.eventTrace_category_parser,
		UIMessages.eventTrace_category_model
	};

	public static String startupInfo() {
		Bundle bundle = AspectJUIPlugin.getDefault().getBundle();
		String version = bundle.getHeaders().get(
				Constants.BUNDLE_VERSION);

		StringBuilder eventData = new StringBuilder( );
		eventData.append( "Startup information: ");  //$NON-NLS-1$
		eventData.append( "\n   AJDT version: " ); //$NON-NLS-1$
		eventData.append( version );

		eventData.append( "\n   AspectJ Compiler version: " ); //$NON-NLS-1$
		eventData.append( Version.getText() );
		eventData.append("\n   usingVisualiser=").append(AspectJUIPlugin.usingVisualiser); //$NON-NLS-1$
		eventData.append("\n   usingXref=").append(AspectJUIPlugin.usingXref); //$NON-NLS-1$
		eventData.append("\n   usingCUprovider=").append(AspectJPlugin.USING_CU_PROVIDER); //$NON-NLS-1$

		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
		String[] props = AspectJUIPlugin.getDefault().getPluginPreferences().propertyNames();
    for (String prop : props) {
      eventData.append("\n   "); //$NON-NLS-1$
      eventData.append(prop);
      eventData.append(" = "); //$NON-NLS-1$
      eventData.append(store.getString(prop));
    }
		return eventData.toString();
	}

	public static void setDebug(boolean on) {
		DEBUG = on;
		if (on) {
			AJBuilder.addStateListener();
		} else {
			AJBuilder.removeStateListener();
		}
	}

	public static void setDebugCategories(List checked) {
    DEBUG_COMPILER = checked.contains(categoryNames[0]);
    DEBUG_COMPILER_PROGRESS = checked.contains(categoryNames[1]);
    DEBUG_COMPILER_MESSAGES = checked.contains(categoryNames[2]);
    DEBUG_BUILDER = checked.contains(categoryNames[3]);
    DEBUG_BUILDER_CLASSPATH = checked.contains(categoryNames[4]);
    DEBUG_PARSER = checked.contains(categoryNames[5]);
    DEBUG_MODEL = checked.contains(categoryNames[6]);
	}

}
