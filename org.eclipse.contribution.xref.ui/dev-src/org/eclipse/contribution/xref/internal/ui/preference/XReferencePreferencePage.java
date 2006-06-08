/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.internal.ui.preference;

import java.util.List;

import org.eclipse.contribution.xref.core.XReferenceProviderDefinition;
import org.eclipse.contribution.xref.core.XReferenceProviderManager;
import org.eclipse.contribution.xref.internal.ui.text.XRefMessages;
import org.eclipse.contribution.xref.ui.XReferenceUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Cross Reference Preference Page
 */
public class XReferencePreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {

	private Text descriptionText;
	private CheckboxTableViewer checkboxViewer;

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Font font = parent.getFont();

		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		mainComposite.setFont(font);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 10;
		mainComposite.setLayout(layout);

		Label topLabel = new Label(mainComposite, SWT.NONE);
		topLabel.setText(XRefMessages.XReferencePreferencePage_explanation);
		topLabel.setFont(font);

		createProvidersArea(mainComposite);
		createDescriptionArea(mainComposite);
		populateProviders();

		return mainComposite;
	}

	/**
	 * Creates the widgets for the description.
	 */
	private void createDescriptionArea(Composite mainComposite) {
		Font mainFont = mainComposite.getFont();
		Composite textComposite = new Composite(mainComposite, SWT.NONE);
		textComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout textLayout = new GridLayout();
		textLayout.marginWidth = 0;
		textLayout.marginHeight = 0;
		textComposite.setLayout(textLayout);
		textComposite.setFont(mainFont);

		Label descriptionLabel = new Label(textComposite, SWT.NONE);
		descriptionLabel.setText(XRefMessages.XReferencePreferencePage_description);
		descriptionLabel.setFont(mainFont);

		descriptionText =
			new Text(
				textComposite,
				SWT.MULTI
					| SWT.WRAP
					| SWT.READ_ONLY
					| SWT.BORDER
					| SWT.H_SCROLL);
		descriptionText.setLayoutData(new GridData(GridData.FILL_BOTH));
		descriptionText.setFont(mainFont);
	}

	/**
	 * Creates the widgets for the list of providers
	 */
	private void createProvidersArea(Composite mainComposite) {
		Font mainFont = mainComposite.getFont();
		Composite providersComposite = new Composite(mainComposite, SWT.NONE);
		providersComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout providersLayout = new GridLayout();
		providersLayout.marginWidth = 0;
		providersLayout.marginHeight = 0;
		providersComposite.setLayout(providersLayout);
		providersComposite.setFont(mainFont);

		Label providersLabel = new Label(providersComposite, SWT.NONE);
		providersLabel.setText(XRefMessages.XReferencePreferencePage_providersLabel);
		providersLabel.setFont(mainFont);

		// Checkbox table viewer of providers
		checkboxViewer =
			CheckboxTableViewer.newCheckList(
				providersComposite,
				SWT.SINGLE | SWT.TOP | SWT.BORDER);
		checkboxViewer.getTable().setLayoutData(
			new GridData(GridData.FILL_BOTH));
		checkboxViewer.getTable().setFont(providersComposite.getFont());
		checkboxViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((XReferenceProviderDefinition) element).getLabel();
			}
		});
		checkboxViewer.getTable().setFont(mainFont);

		checkboxViewer.setContentProvider(new IStructuredContentProvider() {

			public void dispose() {
				//Nothing to do on dispose
			}
			public void inputChanged(
				Viewer viewer,
				Object oldInput,
				Object newInput) {
			}
			public Object[] getElements(Object inputElement) {
				//Make an entry for each provider definition
				return (Object[]) inputElement;
			}

		});

		checkboxViewer
			.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel =
						(IStructuredSelection) event.getSelection();
					XReferenceProviderDefinition definition =
						(XReferenceProviderDefinition) sel.getFirstElement();
					if (definition == null)
						clearDescription();
					else
						showDescription(definition);
				}
			}
		});

		checkboxViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				checkboxViewer.setSelection(
					new StructuredSelection(event.getElement()));
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/**
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		XReferenceProviderManager manager =
			XReferenceProviderManager.getManager();
		List providers = manager.getRegisteredProviders();
		for (int i = 0; i < providers.size(); i++) {
			XReferenceProviderDefinition definition =
				(XReferenceProviderDefinition) providers.get(i);
			checkboxViewer.setChecked(
				definition,
				definition.getDefaultEnablementValue());
		}
	}

	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		if (super.performOk()) {
			List providers = getAllRegisteredProviders();
			for (int i = 0; i < providers.size(); i++) {
				XReferenceProviderDefinition definition =
					(XReferenceProviderDefinition) providers.get(i);
				boolean checked = checkboxViewer.getChecked(definition);
				definition.setEnabled(checked);
			}
			reset();
			return true;
		}
		return false;
	}

	private void populateProviders() {
		List providers = getAllRegisteredProviders();
		checkboxViewer.setInput(providers.toArray());
		for (int i = 0; i < providers.size(); i++) {
			XReferenceProviderDefinition definition =
				(XReferenceProviderDefinition) providers.get(i);
			checkboxViewer.setChecked(definition, definition.isEnabled());
		}
	}

	private List getAllRegisteredProviders() {
		XReferenceProviderManager manager =
			XReferenceProviderManager.getManager();
		return manager.getRegisteredProviders();
	}

	/**
	 * Clear the selected description in the text.
	 */
	private void clearDescription() {
		if (descriptionText == null || descriptionText.isDisposed()) {
			return;
		}
		descriptionText.setText(""); //$NON-NLS-1$
	}

	/**
	 * Show the selected description in the text.
	 */
	private void showDescription(XReferenceProviderDefinition definition) {
		if (descriptionText == null || descriptionText.isDisposed()) {
			return;
		}
		String text = definition.getDescription();
		if (text == null || text.length() == 0)
			descriptionText.setText(XRefMessages.XReferencePreferencePage_noDescription);
		else
			descriptionText.setText(text);
	}

	private void reset() {
		// called after perform ok has updated providerDefinitions
		IPreferenceStore store =
			XReferenceUIPlugin.getDefault().getPreferenceStore();
		if (store.needsSaving()) {
			XReferenceUIPlugin.getDefault().savePluginPreferences();
		}
	}

}
