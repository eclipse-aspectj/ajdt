/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer, Andy Clement - initial version

**********************************************************************/
package org.eclipse.ajdt.internal.ui;

import java.io.File;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
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

	// Non standard compiler options that should be passed to ajc
	private StringFieldEditor nonStandardOptionsEditor;

	// Relevant project for which the properties are being set
	private IProject thisProject;
    private static int BROWSE_FOR_CLASSJARS = 0;
    private static int BROWSE_FOR_ASPECTJARS = 1;

	/**
	 * Build the page of properties that can be set on a per project basis for the
	 * AspectJ compiler.
	 */
	protected Control createContents(Composite parent) {
		// Grab the resource (must be a project) for which this property page
		// is being created
		thisProject = (IProject) getElement();
		//BuildOptionsAdapter.ensurePropertiesInitialized(thisProject);
		Composite pageComposite = createPageComposite(parent, 3);

        // This will cover the top row of the panel.
        Composite row0Composite = createRowComposite(pageComposite,1);
		Label optionsLabel =
			createLabel(
                row0Composite,
				AspectJUIPlugin.getResourceString("compilerPropsPage.description"));

		Composite row3Comp = createRowComposite(pageComposite,2);

		outputJarEditor =
		  new StringFieldEditor("",
		    AspectJUIPlugin.getResourceString("compilerPropsPage.outputJar"),
            row3Comp);
				
		nonStandardOptionsEditor =
			new StringFieldEditor(
				"",
				AspectJUIPlugin.getResourceString("compilerPropsPage.nonStandardOptions"),
				StringFieldEditor.UNLIMITED,
				pageComposite);
				
		Label spacerLabel009 = createLabel(	pageComposite,"");				

		createLabel(pageComposite,"");

		updatePageContents();
		return pageComposite;
	}

	/**
	 * Helper method to build a labelled check box button.
	 */
	private Button buildButton(Composite container, String label) {
		Button btn = new Button(container, SWT.CHECK);
		btn.setText(label);

		GridData data = new GridData();
		data.horizontalSpan = 3;
		data.horizontalAlignment = GridData.FILL;
		btn.setLayoutData(data);

		return btn;
	}

    /**
     * Helper method to build a simple push button.
     */
    private Button buildPushButton(Composite container, String label) {
        Button btn = new Button(container, SWT.PUSH);
        btn.setText(label);
        return btn;
    }

	/**
	 * Helper method to build a labelled radio button.
	 */
	private Button createRadioButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.RADIO | SWT.LEFT);
		button.setText(label);

		GridData data = new GridData();
		data.horizontalSpan = 3;
		button.setLayoutData(data);

		return button;
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

//	/**
//	 * Preserves a boolean value for a named field on the project resource.
//	 * Actually converts the boolean to a string and delegates to the 
//	 * other implementation of preserveSetting() that takes a string as the
//	 * value to store.
//	 */
//	private void preserveSetting(QualifiedName key, boolean flag)
//		throws CoreException {
//		preserveSetting(key, new Boolean(flag).toString());
//	}
//
//	/**
//	 * Preserve a key/value pair as a persistent property against the 
//	 * project resource.
//	 */
//	private void preserveSetting(QualifiedName key, String value)
//		throws CoreException {
//		thisProject.setPersistentProperty(key, value);
//	}

//	/** 
//	 * Retrieve a persistent property value and return it.  If the key is
//	 * not found, this will return null *but* that should not occur because
//	 * ensurePropertiesInitialized() makes sure all the keys have valid 
//	 * values when the properties page first appears for a project.
//	 */
//	private String retrieveSettingString(QualifiedName key) {
//		try {
//			String value = thisProject.getPersistentProperty(key);
//			if (value==null) {
//				return "";
//			}
//			return value;
//		} catch (CoreException ce) {
//			AspectJUIPlugin.getDefault().getErrorHandler().handleError(
//				AspectJUIPlugin.getResourceString("projectProperties.exceptionDuringRetrieve"),
//				ce);
//		}
//		return "";
//	}

//	/**
//	 * Retrieve a persistent property value, convert it to a boolean and return it.
//	 * If the key is not found, this will return 'false' *but* that should not 
//	 * occur because ensurePropertiesInitialized() makes sure all the keys have 
//	 * valid values when the properties page first appears for a project.
//	 */
//	private boolean retrieveSettingBoolean(QualifiedName key) {
//		try {
//			String value = thisProject.getPersistentProperty(key);
//			if (value == null)
//				return false;
//			boolean valueB = new Boolean(value).booleanValue();
//			return valueB;
//		} catch (CoreException ce) {
//			AspectJUIPlugin.getDefault().getErrorHandler().handleError(
//				AspectJUIPlugin.getResourceString("projectProperties.exceptionDuringRetrieve"),
//				ce);
//		}
//		return false;
//	}
	
	private String findInvalidJars(String setOfJars) {

		if (setOfJars.length()==0) return null;
		String inputCopy = setOfJars;
		
		StringBuffer invalidEntries = new StringBuffer();
		
		// For relative paths (they don't start with a File.separator
		// or a drive letter on windows) - we prepend the projectBaseDirectory
		String projectBaseDirectory = thisProject.getLocation().toOSString();

		
		while (inputCopy.indexOf(java.io.File.pathSeparator)!=-1) {
		  int idx = inputCopy.indexOf(java.io.File.pathSeparator);
		  String path = inputCopy.substring(0,idx);
		  
		  java.io.File f = new java.io.File(path);
		  if (!f.isAbsolute())
		  	f = new File(projectBaseDirectory+java.io.File.separator+path);
		  	if (!f.exists()) invalidEntries.append(f+"\n");
		  inputCopy = inputCopy.substring(idx+1);	
		}
		
		// Process the final element
		if (inputCopy.length()!=0) {
		  java.io.File f = new java.io.File(inputCopy);
		  if (!f.isAbsolute())
		  	f = new File(projectBaseDirectory+java.io.File.separator+inputCopy);
		  	if (!f.exists()) invalidEntries.append(f+"\n");
	
		}
		
		if (invalidEntries.length()==0) return null;
		
		return invalidEntries.toString();
	}
	
	private String findInvalidDirs(String setOfDirs) {

		if (setOfDirs.length()==0) return null;
		String inputCopy = setOfDirs;
		
		StringBuffer invalidEntries = new StringBuffer();
		
		// For relative paths (they don't start with a File.separator
		// or a drive letter on windows) - we prepend the projectBaseDirectory
		String projectBaseDirectory = 
		  AspectJPlugin.getDefault().getCurrentProject().
		  getLocation().toOSString();
		
		while (inputCopy.indexOf(java.io.File.pathSeparator)!=-1) {
		  int idx = inputCopy.indexOf(java.io.File.pathSeparator);
		  String path = inputCopy.substring(0,idx);
		  
		  java.io.File f = new java.io.File(path);
		  if (!f.isAbsolute())
		  	f = new File(projectBaseDirectory+java.io.File.separator+path);
		  if (!f.isDirectory()) invalidEntries.append(f+"\n");
		  inputCopy = inputCopy.substring(idx+1);	
		}
		
		// Process the final element
		if (inputCopy.length()!=0) {
		  java.io.File f = new java.io.File(inputCopy);
		  if (!f.isAbsolute())
		  	f = new File(projectBaseDirectory+java.io.File.separator+inputCopy);
		  	if (!f.isDirectory()) invalidEntries.append(f+"\n");
	
		}
		
		if (invalidEntries.length()==0) return null;
		return invalidEntries.toString();
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
		// check the output jar value and make relative to project output dir if 
		// needed.
		String outJar = outputJarEditor.getStringValue();
		if (outJar != null) {
			try{
				if (!outJar.startsWith(File.separator)) {
					IJavaProject jp = JavaCore.create(thisProject);
					IPath workspaceRelativeOutpath = jp.getOutputLocation();
					IPath full = AspectJPlugin.getWorkspace().getRoot().getLocation().append(workspaceRelativeOutpath);
					outJar = full.toOSString();					
				}
			} catch (JavaModelException jme) {
				// leave the outJar setting unchanged
			}
		
		}
		AJDTEventTrace.projectPropertiesChanged(thisProject);
		AspectJCorePreferences.setProjectOutJar(thisProject,outputJarEditor.getStringValue());
		//preserveSetting(BuildOptionsAdapter.CHAR_ENC,"");
		AspectJPreferences.setCompilerOptions(thisProject,nonStandardOptionsEditor.getStringValue());
		return true;
	}
	
	/**
	 * Bug 76811: All fields in the preference page are put back to their
	 * default values. The underlying settings are not changed until "ok"
	 * is clicked. This now behaves like the jdt pages.
	 */
	public void performDefaults() {
		AJDTEventTrace.projectPropertiesDefaulted(thisProject);
		outputJarEditor.setStringValue("");
		nonStandardOptionsEditor.setStringValue("");
	}
	
	/**
	 * Convert the choice shown on the radio button group into a string
	 * for storing as a persistent property.
	 */
	public String calculateStrictnessFromButtons() {
//		if (compileMode_normalBtn.getSelection())
//			return BuildOptionsAdapter.COMPILATION_STRICTNESS_NORMAL;
//		if (compileMode_lenientBtn.getSelection())
//			return BuildOptionsAdapter.COMPILATION_STRICTNESS_LENIENT;
//		if (compileMode_strictBtn.getSelection())
//			return BuildOptionsAdapter.COMPILATION_STRICTNESS_STRICT;
		return null;
	}

	/**
	 * Ensure the widgets state reflects the persistent property values.
	 */
	public void updatePageContents() {
		outputJarEditor.setStringValue(AspectJCorePreferences.getProjectOutJar(thisProject));
		nonStandardOptionsEditor.setStringValue(AspectJPreferences.getCompilerOptions(thisProject));
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