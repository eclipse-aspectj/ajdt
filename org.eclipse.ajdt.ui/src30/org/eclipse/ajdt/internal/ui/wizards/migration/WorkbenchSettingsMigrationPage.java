/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.wizards.migration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.launching.LaunchConfigurationManagementUtils;
import org.eclipse.ajdt.internal.ui.AJDTConfigSettings;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.osgi.service.prefs.BackingStoreException;

/**
 * This page handles the migration of workbench settings, for example,
 * it turns on red sqiggles, enables unused imports in aspects and 
 * resets the file association default of .java files to be the Java Editor
 * 
 * Other things it does, without prompting the user, is:
 * 	- enable .aj resource filter
 *  - reset other compiler/AJ settings
 *  - clear up redundent .ajsym and .generated.lst files
 */
public class WorkbenchSettingsMigrationPage extends WizardPage {

    // these are the keys used in AJDT 1.1.12 to store various settings
    private final QualifiedName INCREMENTAL_COMPILATION = new QualifiedName(
            AspectJUIPlugin.PLUGIN_ID, "BuildOptions.incrementalMode"); //$NON-NLS-1$
    private final QualifiedName BUILD_ASM = new QualifiedName(
            AspectJUIPlugin.PLUGIN_ID, "BuildOptions.buildAsm"); //$NON-NLS-1$
    private final QualifiedName WEAVEMESSAGES = new QualifiedName(
            AspectJUIPlugin.PLUGIN_ID, "BuildOptions.showweavemessages"); //$NON-NLS-1$
    private final QualifiedName OUTJAR = new QualifiedName(
            AspectJUIPlugin.PLUGIN_ID, "BuildOptions.outputJar"); //$NON-NLS-1$
    private final QualifiedName NON_STANDARD_OPTS = new QualifiedName(
            AspectJUIPlugin.PLUGIN_ID, "BuildOptions.nonStandardOptions"); //$NON-NLS-1$
    private final QualifiedName INPATH = new QualifiedName(
            AspectJUIPlugin.PLUGIN_ID, "BuildOptions.inpath"); //$NON-NLS-1$
    private final QualifiedName INPATH_CON_KINDS = new QualifiedName(
            AspectJUIPlugin.PLUGIN_ID, "BuildOptions.inpathConKinds"); //$NON-NLS-1$
    private final QualifiedName INPATH_ENT_KINDS = new QualifiedName(
            AspectJUIPlugin.PLUGIN_ID, "BuildOptions.inpathEntKinds"); //$NON-NLS-1$
    private final QualifiedName ASPECTPATH = new QualifiedName(
            AspectJUIPlugin.PLUGIN_ID, "BuildOptions.aspectpath"); //$NON-NLS-1$
    private final QualifiedName ASPECTPATH_CON_KINDS = new QualifiedName(
            AspectJUIPlugin.PLUGIN_ID, "BuildOptions.aspectpathConKinds"); //$NON-NLS-1$
    private final QualifiedName ASPECTPATH_ENT_KINDS = new QualifiedName(
            AspectJUIPlugin.PLUGIN_ID, "BuildOptions.aspectpathEntKinds"); //$NON-NLS-1$
    private final QualifiedName INPUTJARS =
	    new QualifiedName(AspectJUIPlugin.PLUGIN_ID, "BuildOptions.inputJars");  //$NON-NLS-1$
    private final QualifiedName INPUTJARSBROWSEDIR =
        new QualifiedName(AspectJUIPlugin.PLUGIN_ID, "BuildOptions.inputJarsBrowseDir");   //$NON-NLS-1$  
    private final QualifiedName ASPECTJARSBROWSEDIR =
        new QualifiedName(AspectJUIPlugin.PLUGIN_ID, "BuildOptions.aspectJarsBrowseDir");  //$NON-NLS-1$   
	public final static QualifiedName ASPECTJARS =
	    new QualifiedName(AspectJUIPlugin.PLUGIN_ID, "BuildOptions.inputJars"); //$NON-NLS-1$ 
    public final static QualifiedName SOURCEROOTS =
        new QualifiedName(AspectJUIPlugin.PLUGIN_ID,"BuildOptions.inputJarsBrowseDir"); //$NON-NLS-1$    
    public final static QualifiedName CHAR_ENC =
        new QualifiedName(AspectJUIPlugin.PLUGIN_ID,"BuildOptions.aspectJarsBrowseDir"); //$NON-NLS-1$    

    
	private Button enableRedSquigglesButton;
	private Button enableUnusedImportsButton;
	private Button fileAssociationsButton;
    //private Button useIncrementalButton;

	
	protected WorkbenchSettingsMigrationPage() {
		super(AspectJUIPlugin.getResourceString("WorkbenchSettingsMigrationPage.name")); //$NON-NLS-1$
		this.setTitle(AspectJUIPlugin.getResourceString("WorkbenchSettingsMigrationPage.title")); //$NON-NLS-1$		
		this.setDescription( AspectJUIPlugin.getResourceString("WorkbenchSettingsMigrationPage.description")); //$NON-NLS-1$
	}
	
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(composite);
				
		Label label2 = new Label(composite, SWT.NONE);
		label2.setText(AspectJUIPlugin
				.getResourceString("WorkbenchSettingsMigrationPage.enableRedSquigglesButton.message")); //$NON-NLS-1$

		Label spacer3 = new Label(composite, SWT.NONE);
		
		enableRedSquigglesButton = new Button(composite, SWT.CHECK);
		enableRedSquigglesButton.setText(AspectJUIPlugin
				.getResourceString("WorkbenchSettingsMigrationPage.enableRedSquigglesButton.label")); //$NON-NLS-1$
		enableRedSquigglesButton.setSelection(true);
		
		Label spacer4 = new Label(composite, SWT.NONE);
		
		Label label3 = new Label(composite, SWT.NONE);
		label3.setText(AspectJUIPlugin
				.getResourceString("WorkbenchSettingsMigrationPage.enableUnusedImportsButton.message")); //$NON-NLS-1$

		Label spacer5 = new Label(composite, SWT.NONE);
		
		enableUnusedImportsButton = new Button(composite, SWT.CHECK);
		enableUnusedImportsButton.setText(AspectJUIPlugin
				.getResourceString("WorkbenchSettingsMigrationPage.enableUnusedImportsButton.label")); //$NON-NLS-1$
		enableUnusedImportsButton.setSelection(true);

		Label spacer6 = new Label(composite, SWT.NONE);
		
		Label label4 = new Label(composite, SWT.NONE);
		label4.setText(AspectJUIPlugin
				.getResourceString("WorkbenchSettingsMigrationPage.updateFileAssociations.message")); //$NON-NLS-1$

		Label spacer8 = new Label(composite, SWT.NONE);
		
		fileAssociationsButton = new Button(composite, SWT.CHECK);
		fileAssociationsButton.setText(AspectJUIPlugin
				.getResourceString("WorkbenchSettingsMigrationPage.updateFileAssociations.label")); //$NON-NLS-1$
		fileAssociationsButton.setSelection(true);
		
		Label spacer9 = new Label(composite, SWT.NONE);
		
        Label label5 = new Label(composite, SWT.NONE);
        label5.setText(AspectJUIPlugin
                .getResourceString("WorkbenchSettingsMigrationPage.useIncrementalButton.message")); //$NON-NLS-1$

        //Label spacer10 = new Label(composite, SWT.NONE);

        //useIncrementalButton = new Button(composite, SWT.CHECK);
        //useIncrementalButton.setText(AspectJUIPlugin
        //     .getResourceString("WorkbenchSettingsMigrationPage.useIncrementalButton.label")); //$NON-NLS-1$
        //useIncrementalButton.setSelection(true);


	}

	public void finishPressed(List ajProjects, IProgressMonitor monitor) {
	  
        // preserve the other compiler settings first so that we can
        // tell easily if we're using project settings or not
        preserveOtherCompilerSettings(ajProjects,monitor);

        // clear up the redundent .ajsym and .generated.lst files
        clearupRedundantFilesButton(ajProjects,monitor);
	    
        //useIncrementalCompilationAsDefault(ajProjects, monitor, useIncrementalButton.getSelection());       
	    	    
		// turn off red squgglies if this button is checked
		AJDTConfigSettings.disableAnalyzeAnnotations(!(enableRedSquigglesButton.getSelection()));
		
		// update the unused imports setting
		if (enableUnusedImportsButton.getSelection()) {
			AJDTConfigSettings.enableUnusedImports();
		} else {
			AJDTConfigSettings.disableUnusedImports();
		}
		monitor.worked(ajProjects.size());
		
		// update editor file associations
		AJDTConfigSettings.setDefaultEditorForJavaFiles(!fileAssociationsButton.getSelection());			
		AspectJUIPlugin.getDefault().getPreferenceStore()
			.setValue(AspectJPreferences.JAVA_OR_AJ_EXT, !fileAssociationsButton.getSelection());

		monitor.worked(ajProjects.size());
		
		
	}

    private void clearupRedundantFilesButton(List ajProjects,IProgressMonitor monitor) {
        for (Iterator iter = ajProjects.iterator(); iter.hasNext();) {
            IProject project = (IProject) iter.next();
            IFile ajsmFile = project.getFile(".generated.ajsym"); //$NON-NLS-1$
            IFile generatedFile = project.getFile(".generated.lst"); //$NON-NLS-1$
            if (ajsmFile != null) {
                try {
                    ajsmFile.delete(true, new NullProgressMonitor());
                } catch (CoreException ce) {
                }
            }
            if (generatedFile != null) {
                try {
                    generatedFile.delete(true, new NullProgressMonitor());
                } catch (CoreException e) {
                }
            }
            monitor.worked(1);
        }
    }
    
    private void preserveOtherCompilerSettings(List ajProjects, IProgressMonitor monitor) {
        IPreferenceStore store = AspectJUIPlugin.getDefault()
                .getPreferenceStore();
        for (Iterator iter = ajProjects.iterator(); iter.hasNext();) {
            IProject project = (IProject) iter.next();
            // must work out whether using project settings or not first
            preserveUseProjectSettingsChoice(project, store);
            //preserveBuildAsmSetting(project);
            //preserveWeaveMessagesSetting(project);
            preserveOutJarSetting(project);
            preserveNonStandardOptions(project);
            preserveAspectPathSetting(project);
            preserveInPathSetting(project);

            finalClearup(project);
            flushPrefs(project);
            monitor.worked(1);
        }
    }

    private void preserveUseProjectSettingsChoice(IProject project,
            IPreferenceStore store) {
        boolean useProjectSettings = store.getBoolean(project
                + "useProjectSettings"); //$NON-NLS-1$
        AspectJPreferences.setUsingProjectSettings(project, useProjectSettings,
                false);
    }

    private void preserveOutJarSetting(IProject project) {
        try {
            String outjar = project.getPersistentProperty(OUTJAR);
            AspectJCorePreferences.setProjectOutJar(project,outjar);
        } catch (CoreException e) {
        }
        // in the world of AJDT 1.1.12, we set the following persistent
        // property if the user wanted to set the outjar - we
        // therefore want to unset this
        try {
            if (project.getPersistentProperty(OUTJAR) != null) {
                project.setPersistentProperty(OUTJAR, null);
            }
        } catch (CoreException e) {
        }
    }

    private void preserveNonStandardOptions(IProject project) {
        try {
            String nonStandardOptions = project
                    .getPersistentProperty(NON_STANDARD_OPTS);
            AspectJPreferences.setCompilerOptions(project,nonStandardOptions);
        } catch (CoreException e) {
        }
        // in the world of AJDT 1.1.12, we set the following persistent
        // property if the user wanted to set non standard options - we
        // therefore want to unset this
        try {
            if (project.getPersistentProperty(NON_STANDARD_OPTS) != null) {
                project.setPersistentProperty(NON_STANDARD_OPTS, null);
            }
        } catch (CoreException e) {
        }
    }

    private void preserveBuildAsmSetting(IProject project) {
        if (AspectJPreferences.isUsingProjectSettings(project)) {
            try {
                String buildAsm = project.getPersistentProperty(BUILD_ASM);
                setPrefValue(project, AspectJPreferences.OPTION_BuildASM,
                        buildAsm);

            } catch (CoreException e) {
            }
        }
        // in the world of AJDT 1.1.12, we set the following persistent
        // property if the user wanted to use build the structure model - we
        // therefore want to unset this
        try {
            if (project.getPersistentProperty(BUILD_ASM) != null) {
                project.setPersistentProperty(BUILD_ASM, null);
            }
        } catch (CoreException e) {
        }
    }

    private void preserveWeaveMessagesSetting(IProject project) {
        if (AspectJPreferences.isUsingProjectSettings(project)) {
            try {
                String weaveMessages = project
                        .getPersistentProperty(WEAVEMESSAGES);
                setPrefValue(project, AspectJPreferences.OPTION_WeaveMessages,
                        weaveMessages);

            } catch (CoreException e) {
            }
        }
        // in the world of AJDT 1.1.12, we set the following persistent
        // property if the user wanted to show weave messages - we
        // therefore want to unset this
        try {
            if (project.getPersistentProperty(WEAVEMESSAGES) != null) {
                project.setPersistentProperty(WEAVEMESSAGES, null);
            }
        } catch (CoreException e) {
        }
    }

    private void preserveAspectPathSetting(IProject project) {
        try {
            IClasspathEntry[] aspectPath = getInitialAspectpathValue(project);
            if (aspectPath != null && aspectPath.length > 0) {
                internalConfigureJavaProjectForAspectPath(project,aspectPath);
            }
            if (project.getPersistentProperty(ASPECTPATH) != null) {
                project.setPersistentProperty(ASPECTPATH,null);
            }
            if (project.getPersistentProperty(ASPECTPATH_CON_KINDS) != null) {
                project.setPersistentProperty(ASPECTPATH_CON_KINDS,null);
            }
            if (project.getPersistentProperty(ASPECTPATH_ENT_KINDS) != null) {
                project.setPersistentProperty(ASPECTPATH_ENT_KINDS,null);
            }
        } catch (CoreException e) {
        } catch (InterruptedException e) {
        }
    }

    private void preserveInPathSetting(IProject project) {
        try {
            IClasspathEntry[] inPath = getInitialInpathValue(project);
            if (inPath != null && inPath.length > 0) {
                internalConfigureJavaProjectForInpath(project,inPath);
            }
            if (project.getPersistentProperty(INPATH) != null) {
                project.setPersistentProperty(INPATH,null);
            }
            if (project.getPersistentProperty(INPATH_CON_KINDS) != null) {
                project.setPersistentProperty(INPATH_CON_KINDS,null);
            }
            if (project.getPersistentProperty(INPATH_ENT_KINDS) != null) {
                project.setPersistentProperty(INPATH_ENT_KINDS,null);
            }
        } catch (CoreException e) {
        } catch (InterruptedException e) {
        }
    }

    // taken from AJDT 1.1.12 InpathPropertyPage
    private IClasspathEntry[] getInitialInpathValue(IProject project)
            throws CoreException {
        List result = new ArrayList();
        String paths = project.getPersistentProperty(INPATH);
        String cKinds = project.getPersistentProperty(INPATH_CON_KINDS);
        String eKinds = project.getPersistentProperty(INPATH_ENT_KINDS);

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
                            false); // is exported ?
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

    // taken from AJDT 1.2.0 M3 InPathBlock
    protected void internalConfigureJavaProjectForInpath(IProject project,
            IClasspathEntry[] inpath)
            throws CoreException, InterruptedException {
        StringBuffer inpathBuffer = new StringBuffer();
        StringBuffer contentKindBuffer = new StringBuffer();
        StringBuffer entryKindBuffer = new StringBuffer();
        for (int i = 0; i < inpath.length; i++) {
            inpathBuffer.append(inpath[i].getPath());
            inpathBuffer.append(File.pathSeparator);
            contentKindBuffer.append(inpath[i].getContentKind());
            contentKindBuffer.append(File.pathSeparator);
            entryKindBuffer.append(inpath[i].getEntryKind());
            entryKindBuffer.append(File.pathSeparator);
        }// end for

        inpathBuffer = removeFinalPathSeparatorChar(inpathBuffer);
        contentKindBuffer = removeFinalPathSeparatorChar(contentKindBuffer);
        entryKindBuffer = removeFinalPathSeparatorChar(entryKindBuffer);

        AspectJCorePreferences.setProjectInPath(project, inpathBuffer.toString(),
                contentKindBuffer.toString(), entryKindBuffer.toString());
    }

    protected void internalConfigureJavaProjectForAspectPath(IProject project,
            IClasspathEntry[] aspectpath)
            throws CoreException, InterruptedException {        
        StringBuffer aspectpathBuffer = new StringBuffer();
        StringBuffer contentKindBuffer = new StringBuffer();
        StringBuffer entryKindBuffer = new StringBuffer();
        for (int i = 0; i < aspectpath.length; i++) {
            aspectpathBuffer.append(aspectpath[i].getPath());
            aspectpathBuffer.append(File.pathSeparator);
            contentKindBuffer.append(aspectpath[i].getContentKind());
            contentKindBuffer.append(File.pathSeparator);
            entryKindBuffer.append(aspectpath[i].getEntryKind());
            entryKindBuffer.append(File.pathSeparator);
        }// end for

        aspectpathBuffer = removeFinalPathSeparatorChar(aspectpathBuffer);
        contentKindBuffer = removeFinalPathSeparatorChar(contentKindBuffer);
        entryKindBuffer = removeFinalPathSeparatorChar(entryKindBuffer);

        AspectJCorePreferences.setProjectAspectPath(project, aspectpathBuffer
                .toString(), contentKindBuffer.toString(), entryKindBuffer
                .toString());

        IJavaProject javaProject = JavaCore.create(project);
        LaunchConfigurationManagementUtils.updateAspectPaths(javaProject,
                new ArrayList(), Arrays.asList(aspectpath));
    }

    private StringBuffer removeFinalPathSeparatorChar(StringBuffer buffer) {
        // Chop off extra path separator from end of the string.
        if ((buffer.length() > 0)
                && (buffer.charAt(buffer.length() - 1) == File.pathSeparatorChar)) {
            buffer = buffer.deleteCharAt(buffer.length() - 1);
        }
        return buffer;
    }

    // taken from AJDT 1.1.12 AspectPathPropertyPage
    private IClasspathEntry[] getInitialAspectpathValue(IProject project)
            throws CoreException {
        List result = new ArrayList();
        String paths = project.getPersistentProperty(ASPECTPATH);
        String cKinds = project.getPersistentProperty(ASPECTPATH_CON_KINDS);
        String eKinds = project.getPersistentProperty(ASPECTPATH_ENT_KINDS);

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
                            false); // is exported ?
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
    
    private void finalClearup(IProject project) {
        try {
            if (project.getPersistentProperty(INPUTJARS) != null) {
                project.setPersistentProperty(INPUTJARS,null);
            }
            if (project.getPersistentProperty(INPUTJARSBROWSEDIR) != null) {
                project.setPersistentProperty(INPUTJARSBROWSEDIR,null);
            }
            if (project.getPersistentProperty(ASPECTJARSBROWSEDIR) != null) {
                project.setPersistentProperty(ASPECTJARSBROWSEDIR,null);
            }
            if (project.getPersistentProperty(ASPECTJARS) != null) {
                project.setPersistentProperty(ASPECTJARS,null);
            }
            if (project.getPersistentProperty(SOURCEROOTS) != null) {
                project.setPersistentProperty(SOURCEROOTS,null);
            }
            if (project.getPersistentProperty(CHAR_ENC) != null) {
                project.setPersistentProperty(CHAR_ENC,null);
            }
            
        } catch (CoreException e) {
        }
    }
	
    private void useIncrementalCompilationAsDefault(List ajProjects, IProgressMonitor monitor, boolean value) {
        // update the workbench compiler settings
        IPreferenceStore store = AspectJUIPlugin.getDefault()
                .getPreferenceStore();
        store.setValue(AspectJPreferences.OPTION_Incremental, value);

        for (Iterator iter = ajProjects.iterator(); iter.hasNext();) {
            IProject project = (IProject) iter.next();
            boolean useProjectSettings = AspectJPreferences.isUsingProjectSettings(project);
            if (useProjectSettings) {
                // HELEN - hard coded string!!!!!!!
            	if(value) {
            		setPrefValue(project, AspectJPreferences.OPTION_Incremental,"true"); //$NON-NLS-1$
            	} else {
            		setPrefValue(project, AspectJPreferences.OPTION_Incremental,"false"); //$NON-NLS-1$
            	}
                flushPrefs(project);
            }
            // in the world of AJDT 1.1.12, we set the following persistent
            // property if the user wanted to use incremental compilation - we
            // therefore want to unset this
            try {
                if (project.getPersistentProperty(INCREMENTAL_COMPILATION) != null) {
                    project.setPersistentProperty(INCREMENTAL_COMPILATION,null);
                }
            } catch (CoreException e) {
            }
            monitor.worked(1);
        }
    }
    
    private void setPrefValue(IProject project, String key, String value) {
        if (key == null || value == null) {
            return;
        }
        IScopeContext projectScope = new ProjectScope(project);
        IEclipsePreferences projectNode = projectScope
                .getNode(AspectJPlugin.PLUGIN_ID);
        projectNode.put(key, value);
    }

    private void flushPrefs(IProject project) {
        IScopeContext projectScope = new ProjectScope(project);
        IEclipsePreferences projectNode = projectScope
                .getNode(AspectJPlugin.PLUGIN_ID);
        try {
            projectNode.flush();
        } catch (BackingStoreException e) {
        }
    }
}
