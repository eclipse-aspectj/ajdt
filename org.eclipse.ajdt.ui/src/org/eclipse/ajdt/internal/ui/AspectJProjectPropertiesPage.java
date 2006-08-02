/**********************************************************************
Copyright (c) 2002, 2006 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html
Contributors:
Adrian Colyer, Andy Clement - initial version

**********************************************************************/
package org.eclipse.ajdt.internal.ui;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.internal.launching.LaunchConfigurationManagementUtils;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * The properties page for the AspectJ compiler options that can be set.
 * These options can be set on a per-project basis and so because of that
 * are held as persistent properties against the project resource.
 * 
 * ASCFIXME: Well, if I'd thought of it earlier, I would have put all knowledge of
 * state persistent in the BuildOptionsAdapter and used set/get methods to access it.
 * The get methods already exist, I would have to add the set methods.
 */
public class AspectJProjectPropertiesPage extends PropertyPage {

	// compiler options for ajc 
	private StringFieldEditor outputJarEditor;

	// Relevant project for which the properties are being set
	private IProject thisProject;

	/**
	 * Build the page of properties that can be set on a per project basis for the
	 * AspectJ compiler.
	 */
	protected Control createContents(Composite parent) {
		// Grab the resource (must be a project) for which this property page
		// is being created
		thisProject = (IProject) getElement();
		Composite pageComposite = createPageComposite(parent, 3);

        // This will cover the top row of the panel.
        Composite row0Composite = createRowComposite(pageComposite,1);
		createText(
                row0Composite,
                UIMessages.compilerPropsPage_description);

		Composite row3Comp = createRowComposite(pageComposite,2);

		outputJarEditor =
		  new StringFieldEditor("", //$NON-NLS-1$
				  UIMessages.compilerPropsPage_outputJar,
            row3Comp);
								
		createLabel(pageComposite,"");				 //$NON-NLS-1$

		createLabel(pageComposite,""); //$NON-NLS-1$

		updatePageContents();
		return pageComposite;
	}

	/**
	 * Helper method that creates a label instance.
	 *
	 */
	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);

		GridData data = new GridData();
		data.horizontalSpan = 3;
		data.horizontalAlignment = GridData.BEGINNING;
		label.setLayoutData(data);

		return label;
	}

	/**
	 * Helper method that creates a read-only Text.
	 */
	private Text createText(Composite parent, String text) {
		Text label = new Text(parent, SWT.LEFT | SWT.READ_ONLY);
		label.setText(text);

		GridData data = new GridData();
		data.horizontalSpan = 3;
		data.horizontalAlignment = GridData.BEGINNING;
		label.setLayoutData(data);

		return label;
	}
	/**
	* Creates composite control and sets the default layout data.
	*
	* @param parent  the parent of the new composite
	* @param numColumns  the number of columns for the new composite
	* @return the newly-created coposite
	*/
	private Composite createPageComposite(Composite parent, int numColumns) {
		Composite composite = new Composite(parent, SWT.NONE);

		//GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
        layout.makeColumnsEqualWidth = true;
		composite.setLayout(layout);

		//GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

    private Composite createRowComposite(Composite parent, int numColumns)
    {
        Composite composite = new Composite(parent, SWT.NONE);

        //GridLayout
        GridLayout layout = new GridLayout();
        layout.numColumns = numColumns;
        layout.makeColumnsEqualWidth = true;
        composite.setLayout(layout);

        //GridData
        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 3;
        composite.setLayoutData(data);

        return composite;   
    }
	
	/**
	 * overriding performApply() for PreferencePaageBuilder.aj
	 */
	public void performApply() {  
	    performOk();
	}
	
	/**
	 * When OK is clicked on the property page, this method stores the current
	 * values of all the buttons/fields on the page.  The state is stored as a set
	 * of persistent properties against the project resource.
	 * This method is also called if the user clicks 'Apply' on the property page.
	 */
	public boolean performOk() {
		String oldOutJar = AspectJCorePreferences.getProjectOutJar(thisProject);		
		IClasspathEntry oldEntry = null;
		if(oldOutJar != null && !oldOutJar.equals("")) { //$NON-NLS-1$
			oldEntry = new org.eclipse.jdt.internal.core.ClasspathEntry(		
				IPackageFragmentRoot.K_BINARY, // content kind
				IClasspathEntry.CPE_LIBRARY, // entry kind
				new Path(thisProject.getName() + '/' + oldOutJar).makeAbsolute(), // path
				new IPath[] {}, // inclusion patterns
				new IPath[] {}, // exclusion patterns
				null, // src attachment path
				null, // src attachment root path
				null, // output location
				false, // is exported ?
				null, //accessRules
				false, //combine access rules?
				new IClasspathAttribute[0] // extra attributes?
        		);
		}
		String outJar = outputJarEditor.getStringValue();
		IClasspathEntry newEntry = null;
		if(outJar != null && !outJar.equals("")) { //$NON-NLS-1$
			newEntry = new org.eclipse.jdt.internal.core.ClasspathEntry(		
				IPackageFragmentRoot.K_BINARY, // content kind
				IClasspathEntry.CPE_LIBRARY, // entry kind
				new Path(thisProject.getName() + '/' + outJar).makeAbsolute(), // path
				new IPath[] {}, // inclusion patterns
				new IPath[] {}, // exclusion patterns
				null, // src attachment path
				null, // src attachment root path
				null, // output location
				false, // is exported ?
				null, //accessRules
				false, //combine access rules?
				new IClasspathAttribute[0] // extra attributes?
        		);
		}
		LaunchConfigurationManagementUtils.updateOutJar(JavaCore.create(thisProject), oldEntry, newEntry);
		AspectJCorePreferences.setProjectOutJar(thisProject,outputJarEditor.getStringValue());
		return true;
	}
	
	/**
	 * Bug 76811: All fields in the preference page are put back to their
	 * default values. The underlying settings are not changed until "ok"
	 * is clicked. This now behaves like the jdt pages.
	 */
	public void performDefaults() {
		AJLog.log("Compiler properties reset to default for project: " + thisProject.getName()); //$NON-NLS-1$
		outputJarEditor.setStringValue(""); //$NON-NLS-1$
	}

	/**
	 * Ensure the widgets state reflects the persistent property values.
	 */
	public void updatePageContents() {
		outputJarEditor.setStringValue(AspectJCorePreferences.getProjectOutJar(thisProject));
	}
 	
    /**
     * Returns the project for which this page is currently open.
     */
    public IProject getThisProject() {
        return thisProject;
    }
    
	/**
	 * overriding dispose() for PreferencePaageBuilder.aj
	 */   
	public void dispose() {
		super.dispose();
	}  
}