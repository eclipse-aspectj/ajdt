/**********************************************************************
 Copyright (c) 2002, 2006 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Adrian Colyer, Andy Clement - initial version
 Helen Hawkins - updated for new ajde interface (bug 148190)

 **********************************************************************/
package org.eclipse.ajdt.internal.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.internal.launching.LaunchConfigurationManagementUtils;
import org.eclipse.ajdt.internal.ui.ajde.AJDTErrorHandler;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.internal.ui.wizards.AspectPathBlock;
import org.eclipse.ajdt.internal.ui.wizards.InPathBlock;
import org.eclipse.ajdt.internal.ui.wizards.TabFolderLayout;
import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathBasePage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
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
public class AspectJProjectPropertiesPage extends PropertyPage implements
		IStatusChangeListener {

	private static final String INDEX = "pageIndex"; //$NON-NLS-1$

	private int fPageIndex;

	private static final String PAGE_SETTINGS = "AspectJBuildPropertyPage";

	public static final String PROP_ID = "org.eclipse.ajdt.internal.ui.AspectJProjectPropertiesPage"; //$NON-NLS-1$

	// compiler options for ajc 
	private StringFieldEditor outputJarEditor;

	// Relevant project for which the properties are being set
	private IProject thisProject;

	private BuildPathBasePage fCurrPage;

	private InPathBlock fInPathBlock;

	private AspectPathBlock fAspectPathBlock;

	/**
	 * Build the page of properties that can be set on a per project basis for the
	 * AspectJ compiler.
	 */
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		// Grab the resource (must be a project) for which this property page
		// is being created
		thisProject = getProject();

		Composite composite = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.numColumns = 1;
		composite.setLayout(layout);

		TabFolder folder = new TabFolder(composite, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		fInPathBlock = new InPathBlock(this, 0);

		IClasspathEntry[] initalInpath = null;

		try {
			initalInpath = getInitialInpathValue(thisProject);
		} catch (CoreException ce) {
			AJDTErrorHandler.handleAJDTError(
					UIMessages.InPathProp_exceptionInitializingInpath_title,
					UIMessages.InPathProp_exceptionInitializingInpath_message,
					ce);
		}

		fInPathBlock.init(JavaCore.create(thisProject), null, initalInpath);

		fInPathBlock.tabContent(folder);

		fAspectPathBlock = new AspectPathBlock(this, 0);

		IClasspathEntry[] initialAspectpath = null;

		try {
			initialAspectpath = getInitialAspectpathValue(thisProject);
		} catch (CoreException ce) {
			AJDTErrorHandler.handleAJDTError(
							UIMessages.AspectPathProp_exceptionInitializingAspectpath_title,
							UIMessages.AspectPathProp_exceptionInitializingAspectpath_message,
							ce);
		}

		fAspectPathBlock.init(JavaCore.create(thisProject), null,
				initialAspectpath);

		fAspectPathBlock.tabContent(folder);

		TabItem item;
		item = new TabItem(folder, SWT.NONE);
		item.setText(UIMessages.compilerPropsPage_outputJar);
		item.setControl(outputTab(folder));

		folder.setSelection(fPageIndex);
		fCurrPage = (BuildPathBasePage) folder.getItem(fPageIndex).getData();
		folder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tabChanged(e.item);
			}
		});

		updatePageContents();
		return composite;
	}

	private IClasspathEntry[] getInitialAspectpathValue(IProject project)
			throws CoreException {
		List result = new ArrayList();
		String[] v = AspectJCorePreferences.getProjectAspectPath(project);
		if (v == null) {
			return null;
		}
		String paths = v[0];
		String cKinds = v[1];
		String eKinds = v[2];
		if ((paths != null && paths.length() > 0)
				&& (cKinds != null && cKinds.length() > 0)
				&& (eKinds != null && eKinds.length() > 0)) {
			StringTokenizer sTokPaths = new StringTokenizer(paths,
					File.pathSeparator);
			StringTokenizer sTokCKinds = new StringTokenizer(cKinds,
					File.pathSeparator);
			StringTokenizer sTokEKinds = new StringTokenizer(eKinds,
					File.pathSeparator);
			if ((sTokPaths.countTokens() == sTokCKinds.countTokens())
					&& (sTokPaths.countTokens() == sTokEKinds.countTokens())) {
				while (sTokPaths.hasMoreTokens()) {
					IClasspathEntry entry = new ClasspathEntry(Integer
							.parseInt(sTokCKinds.nextToken()), // content kind
							Integer.parseInt(sTokEKinds.nextToken()), // entry
							// kind
							new Path(sTokPaths.nextToken()), // path
							new IPath[] {}, // inclusion patterns
							new IPath[] {}, // exclusion patterns
							null, // src attachment path
							null, // src attachment root path
							null, // output location
							false, // is exported ?
							null, // accessRules
							false, // combine access rules?
							new IClasspathAttribute[0] // extra attributes?
					);
					result.add(entry);
				}// end while
			}// end if string token counts tally
		}// end if we have something valid to work with

		if (result.size() > 0) {
			return (IClasspathEntry[]) result.toArray(new IClasspathEntry[0]);
		} else {
			return null;
		}
	}

	private Composite outputTab(Composite composite) {
		Composite pageComposite = createPageComposite(composite, 3);

		// This will cover the top row of the panel.
		Composite row0Composite = createRowComposite(pageComposite, 1);
		//createText(row0Composite, UIMessages.compilerPropsPage_description);
		Label title = new Label(row0Composite, SWT.LEFT | SWT.WRAP);
		title.setText(UIMessages.compilerPropsPage_description);
		
		Composite row3Comp = createRowComposite(pageComposite, 2);

		outputJarEditor = new StringFieldEditor("", //$NON-NLS-1$
				UIMessages.compilerPropsPage_outputJar, row3Comp);

		return pageComposite;

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

	private Composite createRowComposite(Composite parent, int numColumns) {
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
	
	private boolean checkIfOnInpath(IProject project, String string) {
		String[] oldInpath = AspectJCorePreferences.getProjectInPath(project);
		String[] seperatedOldInpath = oldInpath[0].split(";"); //$NON-NLS-1$

		String outJar = ('/'+thisProject.getName()+'/'+string);
		for (int j = 0; j < seperatedOldInpath.length; j++) {
			if ((seperatedOldInpath[j].equals(outJar))&& 
					!(seperatedOldInpath[j].equals(""))) {
				return true;
			}
		}
		return false;
	}
	
	private boolean checkIfOnAspectpath(IProject project, String string) {
		String[] oldAspectpath = AspectJCorePreferences
				.getProjectAspectPath(project);
		String[] seperatedOldAspectpath = oldAspectpath[0].split(";"); //$NON-NLS-1$
		
		String outJar = ('/'+thisProject.getName()+'/'+string);
		for (int j = 0; j < seperatedOldAspectpath.length; j++) {
			if ((seperatedOldAspectpath[j].equals(outJar)) && 
					!(seperatedOldAspectpath[j].equals(""))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * overriding performApply() for PreferencePageBuilder.aj
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
		if (oldOutJar != null && !oldOutJar.equals("")) { //$NON-NLS-1$
			oldEntry = new org.eclipse.jdt.internal.core.ClasspathEntry(
					IPackageFragmentRoot.K_BINARY, // content kind
					IClasspathEntry.CPE_LIBRARY, // entry kind
					new Path(thisProject.getName() + '/' + oldOutJar)
							.makeAbsolute(), // path
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
		if (outJar != null && !outJar.equals("")) { //$NON-NLS-1$
			newEntry = new org.eclipse.jdt.internal.core.ClasspathEntry(
					IPackageFragmentRoot.K_BINARY, // content kind
					IClasspathEntry.CPE_LIBRARY, // entry kind
					new Path(thisProject.getName() + '/' + outJar)
							.makeAbsolute(), // path
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
		if (checkIfOnInpath(thisProject, outJar)||
				checkIfOnAspectpath(thisProject, outJar)){
			MessageDialog.openInformation(getShell(), UIMessages.buildpathwarning_title, UIMessages.buildConfig_invalidOutjar);
			outputJarEditor.setStringValue(oldOutJar);
		}else{
		LaunchConfigurationManagementUtils.updateOutJar(JavaCore
				.create(thisProject), oldEntry, newEntry);
		AspectJCorePreferences.setProjectOutJar(thisProject, outputJarEditor
				.getStringValue());
		}
		if (fInPathBlock != null) {
			Shell shell = getControl().getShell();
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					try {
						fInPathBlock.configureJavaProject(monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			};
			IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(
					runnable);
			try {
				new ProgressMonitorDialog(shell).run(true, true, op);
			} catch (InvocationTargetException e) {
				return false;
			} catch (InterruptedException e) {
				// cancelled
				return false;
			}
		}

		if (fAspectPathBlock != null) {
			getSettings().put(INDEX, fAspectPathBlock.getPageIndex());

			Shell shell = getControl().getShell();
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					try {
						fAspectPathBlock.configureJavaProject(monitor);
					} catch (CoreException e) {
						AJDTErrorHandler.handleAJDTError(
										PreferencesMessages.BuildPathsPropertyPage_error_message,
										e);
					}
				}
			};
			IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(
					runnable);
			try {
				new ProgressMonitorDialog(shell).run(true, true, op);
			} catch (InvocationTargetException e) {
				return false;
			} catch (InterruptedException e) {
				// cancelled
				return false;
			}
		}
		AJDTUtils.refreshPackageExplorer();
		return true;
	}

	protected IDialogSettings getSettings() {
		IDialogSettings pathSettings = AspectJUIPlugin.getDefault()
				.getDialogSettings();
		IDialogSettings pageSettings = pathSettings.getSection(PAGE_SETTINGS);
		if (pageSettings == null) {
			pageSettings = pathSettings.addNewSection(PAGE_SETTINGS);
			// Important. Give the key INDEX a value which is one less than the
			// number of tabs that will be displayed in the page. The aspectpath
			// page will have two tabs hence ...
			pageSettings.put(INDEX, 2);
		}
		return pageSettings;
	}

	private IClasspathEntry[] getInitialInpathValue(IProject project)
			throws CoreException {
		List result = new ArrayList();
		String[] v = AspectJCorePreferences.getProjectInPath(project);
		if (v == null) {
			return null;
		}
		String paths = v[0];
		String cKinds = v[1];
		String eKinds = v[2];
		if ((paths != null && paths.length() > 0)
				&& (cKinds != null && cKinds.length() > 0)
				&& (eKinds != null && eKinds.length() > 0)) {
			StringTokenizer sTokPaths = new StringTokenizer(paths,
					File.pathSeparator);
			StringTokenizer sTokCKinds = new StringTokenizer(cKinds,
					File.pathSeparator);
			StringTokenizer sTokEKinds = new StringTokenizer(eKinds,
					File.pathSeparator);
			if ((sTokPaths.countTokens() == sTokCKinds.countTokens())
					&& (sTokPaths.countTokens() == sTokEKinds.countTokens())) {
				while (sTokPaths.hasMoreTokens()) {
					IClasspathEntry entry = new ClasspathEntry(Integer
							.parseInt(sTokCKinds.nextToken()), // content kind
							Integer.parseInt(sTokEKinds.nextToken()), // entry
							// kind
							new Path(sTokPaths.nextToken()), // path
							new IPath[] {}, // inclusion patterns
							new IPath[] {}, // exclusion patterns
							null, // src attachment path
							null, // src attachment root path
							null, // output location
							false, // is exported ?
							null, // accessRules
							false, // combine access rules?
							new IClasspathAttribute[0] // extra attributes?
					);
					result.add(entry);
				}// end while
			}// end if string token counts tally
		}// end if we have something valid to work with

		if (result.size() > 0) {
			return (IClasspathEntry[]) result.toArray(new IClasspathEntry[0]);
		} else {
			return null;
		}

	}

	/**
	 * Bug 76811: All fields in the preference page are put back to their
	 * default values. The underlying settings are not changed until "ok" is
	 * clicked. This now behaves like the jdt pages.
	 */
	public void performDefaults() {
		AJLog
				.log("Compiler properties reset to default for project: " + thisProject.getName()); //$NON-NLS-1$
		outputJarEditor.setStringValue(""); //$NON-NLS-1$
	}

	/**
	 * Ensure the widgets state reflects the persistent property values.
	 */
	public void updatePageContents() {
		outputJarEditor.setStringValue(AspectJCorePreferences
				.getProjectOutJar(thisProject));
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

	private IProject getProject() {
		if (testing) {
			return thisProject;
		} else {
			return (IProject) getElement();
		}
	}

	// ---------------- methods for testing -----------------
	private boolean testing = false;

	// set the project for which this properties page is dealing with
	public void setThisProject(IProject project) {
		thisProject = project;
	}

	// set whether or not we are testing
	public void setIsTesting(boolean isTesting) {
		testing = isTesting;
	}

	// set the outjar value
	public void setOutjarValue(String outjar) {
		outputJarEditor.setStringValue(outjar);
	}

	// get the outjar value
	public String getOutjarValue() {
		return outputJarEditor.getStringValue();
	}

	public void statusChanged(IStatus status) {
		// TODO Auto-generated method stub

	}

	protected void tabChanged(Widget widget) {
		if (widget instanceof TabItem) {
			TabItem tabItem = (TabItem) widget;
			BuildPathBasePage newPage = (BuildPathBasePage) tabItem.getData();
			if (fCurrPage != null) {
				List selection = fCurrPage.getSelection();
				if (!selection.isEmpty()) {
					newPage.setSelection(selection, false);
				}
			}
			fCurrPage = newPage;
			fPageIndex = tabItem.getParent().getSelectionIndex();
		}
	}
}