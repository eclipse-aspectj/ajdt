/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer, Andy Clement, Tracy Gardner - initial version
...
**********************************************************************/

package org.eclipse.ajdt.internal.ui.preferences;

import org.aspectj.ajde.Ajde;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The AspectJ preferences that appear in the "AspectJ" category under
 * Workbench->preferences.
 * The policy for preferences in AspectJ mode is to use the Java mode
 * preference where it exists, and only the AspectJ specific preferences
 * in this page.
 * @todo What preferences do we need??
 */
public class AspectJPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	/**
	 * The default values used when the plugin is first installed or
	 * when "restore defaults" is clicked.
	 */
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(AspectJPreferences.JAVA_OR_AJ_EXT, false);
		store.setDefault(AspectJPreferences.ADVICE_DECORATOR, true);
		store.setDefault(AspectJPreferences.AUTOBUILD_SUPPRESSED, true);
        store.setDefault(AspectJPreferences.PDE_AUTO_IMPORT_CONFIG_DONE, false);
        store.setDefault(AspectJPreferences.ASK_PDE_AUTO_IMPORT, true);
        store.setDefault(AspectJPreferences.DO_PDE_AUTO_IMPORT, false);
        store.setDefault(AspectJPreferences.PDE_AUTO_REMOVE_IMPORT_CONFIG_DONE, false);
        store.setDefault(AspectJPreferences.ASK_PDE_AUTO_REMOVE_IMPORT, true);
        store.setDefault(AspectJPreferences.DO_PDE_AUTO_REMOVE_IMPORT, false);        
	}

	/**
	 * Constructor for AspectJPreferencePage.
	 */
	public AspectJPreferencePage() {
		super(GRID);
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

	/**
	 * From FieldEditorPreferencePage
	 */
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		Composite compilerOptionsComposite = createComposite(parent, 2);

		createLabel(
				compilerOptionsComposite,
				UIMessages.aspectjPreferences_description);

		createLabel(compilerOptionsComposite, ""); // Spacer //$NON-NLS-1$

		createLabel(
			compilerOptionsComposite,
			UIMessages.aspectjPreferences_compilerVersion
				+ " " //$NON-NLS-1$
				+ new String(Ajde.getDefault().getVersion()));

		createLabel(
			compilerOptionsComposite,
			UIMessages.aspectjPreferences_pluginVersion
				+ " " //$NON-NLS-1$
				+ new String(AspectJUIPlugin.VERSION));

		createLabel(compilerOptionsComposite, ""); // Spacer //$NON-NLS-1$

		createLabel(compilerOptionsComposite, UIMessages.AspectJPreferencePage_support_information);
		ITextViewer itw1 = new TextViewer(compilerOptionsComposite, SWT.READ_ONLY);

		itw1.setDocument(
			new Document(
			  UIMessages.AspectJPreferencePage_refer_to_forum+
			  UIMessages.AspectJPreferencePage_on_the_website));

		createLabel(compilerOptionsComposite, UIMessages.AspectJPreferencePage_licensing);
		ITextViewer itw2 = new TextViewer(compilerOptionsComposite, SWT.READ_ONLY);
		
		itw2.setDocument(
			new Document(
			  UIMessages.AspectJPreferencePage_copyright+
			  UIMessages.AspectJPreferencePage_copyright2+
			  UIMessages.AspectJPreferencePage_copyright3+
			  UIMessages.AspectJPreferencePage_copyright4+
			  UIMessages.AspectJPreferencePage_copyright5));
					
					

		createLabel(compilerOptionsComposite, ""); // Spacer //$NON-NLS-1$
		
		addField(createAdviceDecoratorField(compilerOptionsComposite));
		
	}

	/**
	 * Get the preference store for AspectJ mode
	 */
	protected IPreferenceStore doGetPreferenceStore() {
		return AspectJUIPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * Creates composite control and sets the default layout data.
	 *
	 * @param parent  the parent of the new composite
	 * @param numColumns  the number of columns for the new composite
	 * @return the newly-created coposite
	 */
	private Composite createComposite(Composite parent, int numColumns) {
		Composite composite = new Composite(parent, SWT.NULL);

		//GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		composite.setLayout(layout);

		//GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		return composite;
	}

	/**
	 * Utility method that creates a label instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new label
	 * @param text  the text for the new label
	 * @return the new label
	 */
	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}

	private FieldEditor createAdviceDecoratorField(Composite parent) {
		BooleanFieldEditor editor =
			new BooleanFieldEditor(
				AspectJPreferences.ADVICE_DECORATOR,
				UIMessages.aspectjPreferences_adviceDec,
				parent);
		return editor;
	}

}