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
import java.lang.reflect.InvocationTargetException;

import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.ajdt.internal.ui.ajde.BuildOptionsAdapter;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
public class AspectJProjectPropertiesPage
	extends PropertyPage
	implements SelectionListener {

	// Start of compiler options for ajc /////////////////////////////////////////
	// These are the widgets for all the options.

	// Use javac to create the .class files - no longer supported in 1.1
	private Button incremental_modeBtn;
	
	private Button buildAsmBtn; 
	
	private Button showweavemessagesBtn;

//	private Button fileExtBtn;
	
	// Where to place intermediate files when using javac mode
	// or pre-process mode
//	private StringFieldEditor workingDirEditor; - no longer supported in 1.1
	
	//ASCFIXME - Do the javadoc you lazy git
	private StringFieldEditor outputJarEditor;
//    private FileFieldEditor inputJarsEditor, aspectJarsEditor;

	// Use pre-process mode to create .java files - don't build
	// .class files
//	private Button preprocessModeBtn; - no longer supported in 1.1

	// Character encoding for source files
	// private StringFieldEditor characterEncodingEditor;

	// Supporting assertions as defined in JLS-1.4
//	private Button source14modeBtn; - no longer supported in 1.1

	// Report the use of some features from pre-1.0 versions 
	// of AspectJ be warnings to ease porting of old code.
//	private Button portingModeBtn; - no longer supported in 1.1

	// Non standard compiler options that should be passed to ajc
	private StringFieldEditor nonStandardOptionsEditor;

	// Set of radio buttons to select the compilation strictness choice:
	// 1) Do a normal compile
	// 2) Be extra lenient in interpreting the Java spec
	// 3) Be extra strict in interpreting the Java spec
//	private Button compileMode_normalBtn;  - no longer supported in 1.1
//	private Button compileMode_lenientBtn;
//	private Button compileMode_strictBtn;
	// End of compiler options for ajc ///////////////////////////////////////////

	// Relevant project for which the properties are being set
	private IProject thisProject;
    private static int BROWSE_FOR_CLASSJARS = 0;
    private static int BROWSE_FOR_ASPECTJARS = 1;

    private static boolean aspectjSettingChanged = false;
    private static boolean aspectjSettingUpdated = false;

	/**
	 * Build the page of properties that can be set on a per project basis for the
	 * AspectJ compiler.
	 * 
	 */
	protected Control createContents(Composite parent) {
		// Grab the resource (must be a project) for which this property page
		// is being created
		thisProject = (IProject) getElement();
		BuildOptionsAdapter.ensurePropertiesInitialized(thisProject);
		Composite pageComposite = createPageComposite(parent, 3);

        // This will cover the top row of the panel.
        Composite row0Composite = createRowComposite(pageComposite,1);
		Label optionsLabel =
			createLabel(
                row0Composite,
				AspectJUIPlugin.getResourceString("compilerPropsPage.description"));

        // -------------------------- END OF TOP ROW ------------------------

//        Composite row1Comp = createRowComposite(pageComposite,3);
//
//        inputJarsEditor =
//            new PathButtonFieldEditor(
//                "",
//                AspectJPlugin.getResourceString("compilerPropsPage.inputJars"),
//                row1Comp,
//                BROWSE_FOR_CLASSJARS);
//        inputJarsEditor.setChangeButtonText(
//            AspectJPlugin.getResourceString("compilerPropsPage.browseLabel"));
       
        // ------------------------- END OF SECOND ROW ------------------------

//        Composite row2Comp = createRowComposite(pageComposite,3);
//       
//        aspectJarsEditor =
//            new PathButtonFieldEditor(
//                "",
//                AspectJPlugin.getResourceString("compilerPropsPage.aspectJars"),
//                row2Comp,
//                BROWSE_FOR_ASPECTJARS);
//        aspectJarsEditor.setChangeButtonText(
//            AspectJPlugin.getResourceString("compilerPropsPage.browseLabel"));
                
        // ------------------------- END OF THIRD ROW ------------------------
        //Label spacerLabel = createLabel( pageComposite,"");
                
        Composite row3Comp = createRowComposite(pageComposite,2);

		outputJarEditor =
		  new StringFieldEditor("",
		    AspectJUIPlugin.getResourceString("compilerPropsPage.outputJar"),
            row3Comp);
				
        // ------------------------- END OF FOURTH ROW ------------------------

        //Composite row4Comp = createRowComposite(pageComposite,2);
        	
	    //Label spacerLabel008 =
		//	createLabel(
		//		pageComposite,"");

//		preprocessModeBtn =
//			buildButton(
//				pageComposite,
//				AspectJPlugin.getResourceString("compilerPropsPage.preprocessMode"));
//
//		workingDirEditor =
//			new StringFieldEditor(
//				"",
//				AspectJPlugin.getResourceString("compilerPropsPage.workingDirPath"),
//				StringFieldEditor.UNLIMITED,
//				pageComposite);


		
		/* Removed, doesn't seem to be useful when invoking Compiler from Eclipse environment
		characterEncodingEditor =
			new StringFieldEditor(
				"",
				AspectJPlugin.getResourceString("compilerPropsPage.characterEncoding"),
				StringFieldEditor.UNLIMITED,
				pageComposite);
				*/

//		source14modeBtn =
//			buildButton(
//				pageComposite,
//				AspectJPlugin.getResourceString("compilerPropsPage.source14"));
//
//		portingModeBtn =
//			buildButton(
//				pageComposite,
//				AspectJPlugin.getResourceString("compilerPropsPage.portingMode"));
//
//Label spacerLabel001 =
//			createLabel(
//				pageComposite,"");
//				
//		Label strictnessLabel =
//			createLabel(
//				pageComposite,
//				AspectJPlugin.getResourceString("compilerPropsPage.compilerStrictness"));
//
//		compileMode_normalBtn =
//			createRadioButton(
//				pageComposite,
//				AspectJPlugin.getResourceString("compilerPropsPage.compilerStrictnessNormal"));
//		compileMode_lenientBtn =
//			createRadioButton(
//				pageComposite,
//				AspectJPlugin.getResourceString("compilerPropsPage.compilerStrictnessLenient"));
//		compileMode_strictBtn =
//			createRadioButton(
//				pageComposite,
//				AspectJPlugin.getResourceString("compilerPropsPage.compilerStrictnessStrict"));
//				
//				Label spacerLabel002 =
//			createLabel(
//				pageComposite,"");

		nonStandardOptionsEditor =
			new StringFieldEditor(
				"",
				AspectJUIPlugin.getResourceString("compilerPropsPage.nonStandardOptions"),
				StringFieldEditor.UNLIMITED,
				pageComposite);
				
		Label spacerLabel009 = createLabel(	pageComposite,"");				

		incremental_modeBtn =
			buildButton(
				pageComposite,
				AspectJUIPlugin.getResourceString("compilerPropsPage.useIncrementalCompiler"));

        // TODO Add in a message about incremental being experimental in this release.
        Label incExptLabel =
            createLabel(
            pageComposite,
            AspectJUIPlugin.getResourceString("compilerPropsPage.incrementalCompilerStatus"));

		//Label spacerLabel010 = createLabel(	pageComposite,"");				

		buildAsmBtn = buildButton( pageComposite,
		  AspectJUIPlugin.getResourceString("compilerPropsPage.buildasm"));

		Label asmDetails =
			createLabel(
			pageComposite,
			AspectJUIPlugin.getResourceString("compilerPropsPage.buildasm.details"));
 
		
		showweavemessagesBtn = buildButton(pageComposite,
				AspectJUIPlugin.getResourceString("compilerPropsPage.showweavemessages"));
		Label showweavemessagesDetails =
			createLabel(pageComposite,
					AspectJUIPlugin.getResourceString("compilerPropsPage.showweavemessages.details"));
		createLabel(pageComposite,"");
		
//		fileExtBtn = buildButton( pageComposite,
//		  AspectJPlugin.getResourceString("compilerPropsPage.fileExt"));

//		Label extDetails =
//			createLabel(
//			pageComposite,
//			AspectJPlugin.getResourceString("compilerPropsPage.fileExt.details"));


        updatePageContents();

        aspectjSettingChanged = false;
        aspectjSettingUpdated = false;
        
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

        // Need make sure we are listening for pushes...
        btn.addSelectionListener(this);
        return btn;
    }

	/**
	 * Helper method to build a labelled radio button.
	 */
	private Button createRadioButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.RADIO | SWT.LEFT);
		button.setText(label);

		button.addSelectionListener(this);
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

	/**
	 * Preserves a boolean value for a named field on the project resource.
	 * Actually converts the boolean to a string and delegates to the 
	 * other implementation of preserveSetting() that takes a string as the
	 * value to store.
	 */
	private void preserveSetting(QualifiedName key, boolean flag)
		throws CoreException {
		preserveSetting(key, new Boolean(flag).toString());
	}

	/**
	 * Preserve a key/value pair as a persistent property against the 
	 * project resource.
	 */
	private void preserveSetting(QualifiedName key, String value)
		throws CoreException {
		thisProject.setPersistentProperty(key, value);
	}

	/** 
	 * Retrieve a persistent property value and return it.  If the key is
	 * not found, this will return null *but* that should not occur because
	 * ensurePropertiesInitialized() makes sure all the keys have valid 
	 * values when the properties page first appears for a project.
	 */
	private String retrieveSettingString(QualifiedName key) {
		try {
			String value = thisProject.getPersistentProperty(key);
			return value;
		} catch (CoreException ce) {
			AspectJUIPlugin.getDefault().getErrorHandler().handleError(
				AspectJUIPlugin.getResourceString("projectProperties.exceptionDuringRetrieve"),
				ce);
		}
		return "";
	}

	/**
	 * Retrieve a persistent property value, convert it to a boolean and return it.
	 * If the key is not found, this will return 'false' *but* that should not 
	 * occur because ensurePropertiesInitialized() makes sure all the keys have 
	 * valid values when the properties page first appears for a project.
	 */
	private boolean retrieveSettingBoolean(QualifiedName key) {
		try {
			String value = thisProject.getPersistentProperty(key);
			if (value == null)
				return false;
			boolean valueB = new Boolean(value).booleanValue();
			return valueB;
		} catch (CoreException ce) {
			AspectJUIPlugin.getDefault().getErrorHandler().handleError(
				AspectJUIPlugin.getResourceString("projectProperties.exceptionDuringRetrieve"),
				ce);
		}
		return false;
	}
	
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
		  AspectJUIPlugin.getDefault().getCurrentProject().
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
					IPath full = AspectJUIPlugin.getWorkspace().getRoot().getLocation().append(workspaceRelativeOutpath);
					outJar = full.toOSString();					
				}
			} catch (JavaModelException jme) {
				// leave the outJar setting unchanged
			}
		
		}
		AJDTEventTrace.projectPropertiesChanged(thisProject);
		try {
		    if (somethingHasChanged()) {
		        // NOTE: in this property page, we only note changes when Ok is pressed
		        // rather than keeping a record as we go along. This is probably ok in this
		        // case because the AspectJ page is always looked at first. Might need
		        // to change this though by attaching listeners to the various boxes
		        // and acting accordingly
		        aspectjSettingChanged = true;
		    }
		    
			preserveSetting(BuildOptionsAdapter.INCREMENTAL_COMPILATION, incremental_modeBtn.getSelection());
			preserveSetting(BuildOptionsAdapter.BUILD_ASM,buildAsmBtn.getSelection());
			preserveSetting(BuildOptionsAdapter.WEAVEMESSAGES,showweavemessagesBtn.getSelection());
			preserveSetting(BuildOptionsAdapter.OUTPUTJAR,outputJarEditor.getStringValue());
			preserveSetting(BuildOptionsAdapter.CHAR_ENC,"");
			preserveSetting(
				BuildOptionsAdapter.NON_STANDARD_OPTS,
				nonStandardOptionsEditor.getStringValue());

			aspectjSettingUpdated = true;
		} catch (CoreException ce) {
			AspectJUIPlugin.getDefault().getErrorHandler().handleError(
				AspectJUIPlugin.getResourceString("projectProperties.exceptionDuringStore"),
				ce);
			return false;
		}
		return true;
	}

	private boolean somethingHasChanged() {
	    if (!AspectJUIPlugin.getWorkspace().getDescription().isAutoBuilding()) {
            return false;
        }
	    if (!(retrieveSettingString(BuildOptionsAdapter.OUTPUTJAR).equals(outputJarEditor.getStringValue()))
	            || !(retrieveSettingString(BuildOptionsAdapter.NON_STANDARD_OPTS).equals(nonStandardOptionsEditor.getStringValue()))
	            || (retrieveSettingBoolean(BuildOptionsAdapter.INCREMENTAL_COMPILATION) != incremental_modeBtn.getSelection()) 
	            || (retrieveSettingBoolean(BuildOptionsAdapter.BUILD_ASM) != buildAsmBtn.getSelection()) 
	            || (retrieveSettingBoolean(BuildOptionsAdapter.WEAVEMESSAGES) != showweavemessagesBtn.getSelection())) {
	        return true;
        }
	    return false;
	}
	
	/**
	 * Bug 76811: All fields in the preference page are put back to their
	 * default values. The underlying settings are not changed until "ok"
	 * is clicked. This now behaves like the jdt pages.
	 */
	public void performDefaults() {
		AJDTEventTrace.projectPropertiesDefaulted(thisProject);
		incremental_modeBtn.setSelection(BuildOptionsAdapter.INCREMENTAL_COMPILATION_DEFAULT);
		buildAsmBtn.setSelection(BuildOptionsAdapter.BUILD_ASM_DEFAULT);		
		showweavemessagesBtn.setSelection(BuildOptionsAdapter.WEAVE_MESSAGES_DEFAULT);
		outputJarEditor.setStringValue("");
		nonStandardOptionsEditor.setStringValue("");
	}

	/**
	 * Handle selection of an item in the menu.
	 */
	public void widgetDefaultSelected(SelectionEvent se) {
		widgetSelected(se);
	}

	/**
	 * Handle selection of an item in the menu.
	 */
	public void widgetSelected(SelectionEvent se) {
		Object source = se.getSource();
		if (source instanceof Button) {
			Button btn = (Button) source;
			// Keep the buttons in the radio group consistent
            
//			if (btn.equals(compileMode_normalBtn)) {
//				compileMode_normalBtn.setSelection(true);
//				compileMode_lenientBtn.setSelection(false);
//				compileMode_strictBtn.setSelection(false);
//			} else if (btn.equals(compileMode_lenientBtn)) {
//				compileMode_normalBtn.setSelection(false);
//				compileMode_lenientBtn.setSelection(true);
//				compileMode_strictBtn.setSelection(false);
//			} else if (btn.equals(compileMode_strictBtn)) {
//				compileMode_normalBtn.setSelection(false);
//				compileMode_lenientBtn.setSelection(false);
//				compileMode_strictBtn.setSelection(true);
//			}
		}
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
		incremental_modeBtn.setSelection(retrieveSettingBoolean(BuildOptionsAdapter.INCREMENTAL_COMPILATION));
		buildAsmBtn.setSelection(retrieveSettingBoolean(BuildOptionsAdapter.BUILD_ASM));		
		showweavemessagesBtn.setSelection(retrieveSettingBoolean(BuildOptionsAdapter.WEAVEMESSAGES));
		outputJarEditor.setStringValue(retrieveSettingString(BuildOptionsAdapter.OUTPUTJAR));
		nonStandardOptionsEditor.setStringValue(retrieveSettingString(BuildOptionsAdapter.NON_STANDARD_OPTS));
	}
 
	protected void doProjectBuild() { 
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		try {
			dialog.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException {
					monitor.beginTask("", 2); //$NON-NLS-1$
					try {
						monitor
								.setTaskName(AspectJUIPlugin
										.getResourceString("OptionsConfigurationBlock.buildproject.taskname")); //$NON-NLS-1$
						thisProject.build(IncrementalProjectBuilder.FULL_BUILD,
						        "org.eclipse.ajdt.ui.ajbuilder", 
						        null,
						        new SubProgressMonitor(monitor, 2));
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InterruptedException e) {
			// cancelled by user
		} catch (InvocationTargetException e) {
			String message = AspectJUIPlugin
					.getResourceString("OptionsConfigurationBlock.builderror.message"); //$NON-NLS-1$
			AspectJUIPlugin.getDefault().getErrorHandler().handleError(message, e);
		}
	}

	
//    class PathButtonFieldEditor extends FileFieldEditor
//    {
//        private int browseType;
//        private Composite parentComposite;
//        
//        PathButtonFieldEditor(String name, String labelText, Composite parent, int browseType)
//        {
//            super(name,labelText,parent);
//            this.parentComposite = parent;
//            this.browseType = browseType;
//            GridData gd = (GridData)getTextControl().getLayoutData();
//            gd.widthHint = 20;
//        }
//        
//		/* (non-Javadoc)
//		 * @see org.eclipse.jface.preference.StringButtonFieldEditor#changePressed()
//		 */
//        protected String changePressed() {
//            browseForJars(this.browseType);
//            return getStringValue();
//        }
// 
//        /**
//         * Open up a FileDialog for the user to browse for jar files that will
//         * be passed to the AspectJ compiler. If the browseType argument has value
//         * BROWSE_FOR_CLASSJARS then the jar files chosen are passed as injars
//         * arguments. If the browseType argument has a value of
//         * BROWSE_FOR_ASPECTJARS then the selected files are passed as aspectpath
//         * arguments.
//         */
//        private void browseForJars(int browseType) {
//            // Try and obtain the last used path for browsing. If this is the first
//            // time browsing for injars then the default should have been set
//            // to something sensible !
//            // See method BuildOptionsAdapter.ensurePropertiesInitialized...  
//            String lastUsedDir = getLastBrowsedDir(browseType);
//        
//            // Set the title that will appear on the FileDialog. 
//            String title = getDialogTitle(browseType);
//            FileDialog dialog = new FileDialog(this.parentComposite.getShell(), SWT.MULTI);
//            dialog.setText(title);
//            dialog.setFilterExtensions(new String[] { "*.jar;*.zip" });
//            dialog.setFilterPath(lastUsedDir);
//    
//            // The open() call below will pop open the dialog box and allow
//            // jar selection to commence. The returned String will either be the
//            // name of the first selected jar or else null if nothing was chosen
//            // or an error occurred : either way, a null return signifies that 
//            // there is nothing else to do in this method.
//            String res = dialog.open();
//            if (res == null) {
//                return;
//            }
//
//            // Concoct a string containing the fully qualified names of all file
//            // selections made when the FileDialog was in use.
//            String selectedFiles = getSelectedFiles(dialog);
//        
//            // Take the select files string and append to the 
//            // appropriate text field in the properties panel.
//            appendSelectionToTextField(browseType, selectedFiles);
//
//            // Finally, want to persist the most recently used directory
//            // where we picked jars from. Doing this means that the
//            // next time the dialog box is opened it will already be in this
//            // directory.
//            lastUsedDir = dialog.getFilterPath();
//            setLastBrowsedDir(browseType, lastUsedDir);
//        }
//
//        /**
//         * @param dialog
//         * @return
//         */
//        private String getSelectedFiles(FileDialog dialog)
//        {
//            StringBuffer appendBuffer = new StringBuffer();
//            String lastUsedDir = dialog.getFilterPath();
//            String[] fileNames = dialog.getFileNames();
//            int nChosen = fileNames.length;
//            for (int i = 0; i < nChosen; i++) {
//                appendBuffer.append(lastUsedDir + File.separator + fileNames[i]);
//                if ((i + 1) < nChosen)
//                    appendBuffer.append(File.pathSeparator);
//            }
//            return appendBuffer.toString();
//        }
//
//        /**
//         * @param browseType
//         * @param lastUsedDir
//         */
//        private void setLastBrowsedDir(int browseType, String lastUsedDir) {
//            QualifiedName qName = null;
//            if (browseType == BROWSE_FOR_CLASSJARS)
//                qName = BuildOptionsAdapter.INPUTJARSBROWSEDIR;
//            else if (browseType == BROWSE_FOR_ASPECTJARS)
//                qName = BuildOptionsAdapter.ASPECTJARSBROWSEDIR;
//            try {
//                preserveSetting(qName, lastUsedDir);
//            } catch (CoreException e) {
//                AspectJPlugin.getDefault().getErrorHandler().handleError(
//                    AspectJPlugin.getResourceString(
//                        "projectProperties.exceptionDuringStore"),
//                    e);
//            }
//        }
//
//        /**
//         * @param browseType
//         * @param appendBuffer
//         */
//        private void appendSelectionToTextField(
//            int browseType,
//            String appendText) {
//            FileFieldEditor field = null;
//            if (browseType == BROWSE_FOR_CLASSJARS)
//                field = inputJarsEditor;
//            else if (browseType == BROWSE_FOR_ASPECTJARS)
//                field = aspectJarsEditor;
//            String currentPathText = field.getStringValue();
//        
//            if (currentPathText.length() == 0) {
//                field.setStringValue(appendText);
//            } else {
//                field.setStringValue(
//                    currentPathText
//                    + File.pathSeparator
//                    + appendText);
//            }
//        }
//
//        /**
//         * @param browseType
//         * @return
//         */
//        private String getDialogTitle(int browseType) {
//            // TODO Below string needs to be a property.
//            String title = null;
//            if (browseType == BROWSE_FOR_CLASSJARS)
//                title = "Injars Selection";
//            else if (browseType == BROWSE_FOR_ASPECTJARS)
//                title = "Aspect Jars Selection";    
//            return title;
//        }
//
//        /**
//         * @param browseType
//         * @return
//         */
//        private String getLastBrowsedDir(int browseType) {
//            String lastUsedDir = null;
//            if (browseType == BROWSE_FOR_CLASSJARS)
//                lastUsedDir =
//                    retrieveSettingString(BuildOptionsAdapter.INPUTJARSBROWSEDIR);
//            else if (browseType == BROWSE_FOR_ASPECTJARS)
//                lastUsedDir =
//                    retrieveSettingString(BuildOptionsAdapter.ASPECTJARSBROWSEDIR);        
//            return lastUsedDir;
//        }
//    }
//    

	/**
     * Returns whether or not the aspecj settings have changed 
     * in the preference page
     */
    public static boolean aspectjSettingHasChanged() {
        return aspectjSettingChanged;
    }

    /**
     * Returns whether or not the aspectj settings saved in the 
     * project preference store have been updated 
     */
    public static boolean aspectjSettingHasBeenUpdated() {
        return aspectjSettingUpdated;
    }
    
    /**
     * Returns the project for which this page is currently open.
     */
    public IProject getThisProject() {
        return thisProject;
    }
    
    /**
     * Resets the change settings to be false e.g. says
     * that the compiler settings in the preference page haven't been
     * changed and that the preference store settings also haven't
     * been updated.
     */   
    public void resetChangeSettings() {
        aspectjSettingChanged = false;
        aspectjSettingUpdated = false;
    }
}