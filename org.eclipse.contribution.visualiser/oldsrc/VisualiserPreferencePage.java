/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Adrian Colyer - initial version
 *     Sian Whiting - added tabbed layout and drawing options
 *******************************************************************************/
package org.eclipse.contribution.visualiser.views.old;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.jdt.internal.ui.util.TabFolderLayout;
//TODO: Don't use internal APIs

import org.eclipse.contribution.visualiser.VisualiserPlugin;
import org.eclipse.contribution.visualiser.core.ProviderDefinition;
import org.eclipse.contribution.visualiser.core.ProviderManager;

/**
 * The preference page for the Visualiser plugin
 */
public class VisualiserPreferencePage extends PreferencePage 
	implements IWorkbenchPreferencePage {

	private IntegerFieldEditor stripeSizeSelector, minBarSizeSelector, maxBarSizeSelector;
	private Text descriptionText;
	private CheckboxTableViewer checkboxViewer;
	private BooleanFieldEditor demarcationSelector;

	/**
	 * Create the contents of the page
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		composite.setLayout(layout);
		
		TabFolder folder= new TabFolder(composite, SWT.NONE);
		folder.setLayout(new TabFolderLayout());	
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Control providerControl= createProviderControl(folder);
		Control drawingOptionsControl= createDrawingOptionsControl(folder);

		TabItem item= new TabItem(folder, SWT.NONE);
		item.setText("Providers"); 
		item.setControl(providerControl);

		item= new TabItem(folder, SWT.NONE);
		item.setText("Drawing Options");
		item.setControl(drawingOptionsControl);

		populateProviders();

		return composite;
	}


	/**
	 * Subsidiary method for createContents().
	 * Creates the contents of the drawing options tab. 
	 * @param parent
	 * @return the created control
	 */
	private Control createDrawingOptionsControl(TabFolder parent) {
		GridLayout layout= new GridLayout();
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayout(layout);	
		createStripeSizeArea(composite);
		createMinBarWidthArea(composite);
		createMaxBarWidthArea(composite);
		createDemarcationArea(composite);		
		return composite;
	}


	/**
	 * Subsidiary method for createContents().
	 * Creates the contents of the providers tab. 
	 * @param parent
	 * @return the created control
	 */
	private Control createProviderControl(TabFolder parent) {
		GridLayout layout= new GridLayout();
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayout(layout);
		createProvidersArea(composite);
		createDescriptionArea(composite);		
		return composite;
	}

	
	/**
	 * Subsidiary method for createDrawingOptionsControl().
	 * Creates the area used to specify the maximum bar width in the visualiser
	 * @param mainComposite
	 */
	private void createMaxBarWidthArea(Composite mainComposite) {
		Font mainFont = mainComposite.getFont();
		Composite maxBarComposite = new Composite(mainComposite, SWT.NONE);
		maxBarComposite.setLayoutData(new GridData(GridData.BEGINNING));		
		GridLayout maxBarLayout = new GridLayout();
		maxBarComposite.setLayout(maxBarLayout);
		maxBarComposite.setFont(mainFont);
		maxBarSizeSelector = new IntegerFieldEditor(
			VisualiserPreferences.MAX_BAR,
			VisualiserPlugin.getResourceString("VisualiserPreferencePage.maxBarSize"),
			maxBarComposite,
			3);
		maxBarSizeSelector.setValidRange(10, 500);
		maxBarSizeSelector.setErrorMessage(VisualiserPlugin.getResourceString("VisualiserPreferencePage.maxBarErrorMessage"));
		maxBarSizeSelector.setPreferencePage(this);
		maxBarSizeSelector.setPreferenceStore(VisualiserPlugin.getDefault().getPreferenceStore());
		maxBarSizeSelector.load();
			
	}


	/**
	 * Subsidiary method for createDrawingOptionsControl().
	 * Creates the area used to specify the minimum bar width in the visualiser
	 * @param mainComposite
	 */
	private void createMinBarWidthArea(Composite mainComposite) {
		Font mainFont = mainComposite.getFont();
		Composite minBarComposite = new Composite(mainComposite, SWT.NONE);
		minBarComposite.setLayoutData(new GridData(GridData.BEGINNING));		
		GridLayout minBarLayout = new GridLayout();
		minBarComposite.setLayout(minBarLayout);
		minBarComposite.setFont(mainFont);
		minBarSizeSelector = new IntegerFieldEditor(
			VisualiserPreferences.MIN_BAR,
			VisualiserPlugin.getResourceString("VisualiserPreferencePage.minBarSize"),
			minBarComposite,
			3);
		minBarSizeSelector.setValidRange(1, 100);
		minBarSizeSelector.setErrorMessage(VisualiserPlugin.getResourceString("VisualiserPreferencePage.minBarErrorMessage"));
		minBarSizeSelector.setPreferencePage(this);
		minBarSizeSelector.setPreferenceStore(VisualiserPlugin.getDefault().getPreferenceStore());
		minBarSizeSelector.load();
	}

	
	/**
	 * Subsidiary method for createDrawingOptionsControl().
	 * Creates the area used to specify the minimum stripe height in the visualiser
	 * @param mainComposite
	 */
	private void createStripeSizeArea(Composite mainComposite) {
		Font mainFont = mainComposite.getFont();
		Composite stripeComposite = new Composite(mainComposite, SWT.NONE);
		stripeComposite.setLayoutData(new GridData(GridData.BEGINNING));		
		GridLayout stripeLayout = new GridLayout();
		stripeComposite.setLayout(stripeLayout);
		stripeComposite.setFont(mainFont);
		stripeSizeSelector = new IntegerFieldEditor(
			VisualiserPreferences.STRIPE_SIZE,
			VisualiserPlugin.getResourceString("VisualiserPreferencePage.stripeSize"),
			stripeComposite,
			2);
		stripeSizeSelector.setValidRange(1, 20);
		stripeSizeSelector.setErrorMessage(VisualiserPlugin.getResourceString("VisualiserPreferencePage.stripeSizeErrorMessage"));	
		stripeSizeSelector.setPreferencePage(this);
		stripeSizeSelector.setPreferenceStore(VisualiserPlugin.getDefault().getPreferenceStore());
		stripeSizeSelector.load();	
	}


	/**
	 * Subsidiary method for createDrawingOptionsControl().
	 * Creates the area used to specify the whether demarcation is shown in group
	 * mode or not.
	 * @param mainComposite
	 */
	private void createDemarcationArea(Composite mainComposite) {
		demarcationSelector = new
			BooleanFieldEditor(
				VisualiserPreferences.DEMARCATION,
				VisualiserPlugin.getResourceString("VisualiserPreferencePage.demarcation"),
				mainComposite);
				
		demarcationSelector.setPreferencePage(this);
		demarcationSelector.setPreferenceStore(VisualiserPlugin.getDefault().getPreferenceStore());
		demarcationSelector.load();
	}


	/** 
	 * Creates the widgets for the list of providers.
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
		providersLabel.setText(
		VisualiserPlugin.getResourceString("VisualiserPreferencePage.providersLabel")); //$NON-NLS-1$
		providersLabel.setFont(mainFont);
		
		// Checkbox table viewer of decorators
		checkboxViewer =
			CheckboxTableViewer.newCheckList(
				providersComposite,
				SWT.SINGLE | SWT.TOP | SWT.BORDER);
		checkboxViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		checkboxViewer.getTable().setFont(providersComposite.getFont());
		checkboxViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				 return ((ProviderDefinition) element).getName();
			}
		});
		checkboxViewer.getTable().setFont(mainFont);
		
		checkboxViewer.setContentProvider(new IStructuredContentProvider() {
			
			public void dispose() {
				//Nothing to do on dispose
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public Object[] getElements(Object inputElement) {
				//Make an entry for each decorator definition
				//return sorter.sort((Object[]) inputElement);
				return (Object[]) inputElement;
			}
		
		});
		
		checkboxViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) event.getSelection();
					ProviderDefinition definition = 
						(ProviderDefinition) sel.getFirstElement();
					if (definition == null)
						clearDescription();
					else
						showDescription(definition);
				}
			}
		});
		
		checkboxViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				checkboxViewer.setAllChecked(false);
				checkboxViewer.setChecked(event.getElement(),true);
				checkboxViewer.setSelection(
					new StructuredSelection(event.getElement()),true);
			}
		});
	}

	
	/** 
	 * Creates the widgets for the provider description.
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
		descriptionLabel.setText(
			VisualiserPlugin.getResourceString("VisualiserPreferencePage.description")); //$NON-NLS-1$
		descriptionLabel.setFont(mainFont);
		
		descriptionText =
			new Text(textComposite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL);
		descriptionText.setLayoutData(new GridData(GridData.FILL_BOTH));
		descriptionText.setFont(mainFont);
	}

	
	/**
	 * Populates the list of providers.
	 */
	private void populateProviders() {
		ProviderDefinition[] definitions = getAllDefinitions();
		checkboxViewer.setInput(definitions);
		for (int i = 0; i < definitions.length; i++) {
			checkboxViewer.setChecked(definitions[i], definitions[i].isEnabled());
			if(definitions[i].isEnabled()){
				showDescription(definitions[i]);
			}
		}
	}

	
	/**
	 * Show the selected description in the text.
	 */
	private void showDescription(ProviderDefinition definition) {
		if (descriptionText == null || descriptionText.isDisposed()) {
			return;
		}
		String text = definition.getDescription();
		if (text == null || text.length() == 0)
			descriptionText.setText(
				VisualiserPlugin.getResourceString(
					"VisualiserPreferencePage.noDescription")); //$NON-NLS-1$
		else
			descriptionText.setText(text);
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
	 * Restore defaults
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		demarcationSelector.loadDefault();
		maxBarSizeSelector.loadDefault();
		minBarSizeSelector.loadDefault();
		stripeSizeSelector.loadDefault();
	}


	/**
	 * Called when the user presses OK.  
	 * Updates the Visualiser with the selections chosen.
	 * @see PreferencePage#performOk()
	 */
	public boolean performOk() {
		if (super.performOk()) {
			ProviderDefinition[] definitions = ProviderManager.getAllProviderDefinitions();
			for (int i = 0; i < definitions.length; i++) {
				boolean checked = checkboxViewer.getChecked(definitions[i]);
				definitions[i].setEnabled(checked);			
			}
			try{			
				VisualiserPlugin.getDefault().getPreferenceStore().setValue(VisualiserPreferences.STRIPE_SIZE, stripeSizeSelector.getIntValue());
				VisualiserPlugin.getDefault().getPreferenceStore().setValue(VisualiserPreferences.DEMARCATION, demarcationSelector.getBooleanValue());
				VisualiserPlugin.getDefault().getPreferenceStore().setValue(VisualiserPreferences.MIN_BAR, minBarSizeSelector.getIntValue());
				VisualiserPlugin.getDefault().getPreferenceStore().setValue(VisualiserPreferences.MAX_BAR, maxBarSizeSelector.getIntValue());
			} catch (NumberFormatException e){
				// TODO: Error box
				return false;
			}
			
			// if the Visualiser is showing, update to use the new settings
			if (VisualiserPlugin.visualiser != null) {
				VisualiserPlugin.visualiser.updateSettingsFromPreferences();
			}
			return true;
		}
		return false;
	}

	
	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	
	/**
	 * Set up default values for preferences set in this page.
	 */
	public static void initDefaults(){		
		VisualiserPlugin.getDefault().getPreferenceStore().setDefault(VisualiserPreferences.DEMARCATION, true);
		VisualiserPlugin.getDefault().getPreferenceStore().setDefault(VisualiserPreferences.PROVIDER, "");
		VisualiserPlugin.getDefault().getPreferenceStore().setDefault(VisualiserPreferences.STRIPE_SIZE, 3);
		VisualiserPlugin.getDefault().getPreferenceStore().setDefault(VisualiserPreferences.MIN_BAR, 20);
		VisualiserPlugin.getDefault().getPreferenceStore().setDefault(VisualiserPreferences.MAX_BAR, 60);
	}

	
	/**
	 * Get the Provider definitions for the workbench.
	 */
	private ProviderDefinition[] getAllDefinitions() {
		return ProviderManager.getAllProviderDefinitions();
	}

}