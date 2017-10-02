/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.internal.launching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.internal.ui.resources.AJDTIcon;
import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.AddAdvancedAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddExternalFolderAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddExternalJarAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddFolderAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddJarAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddLibraryAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddProjectAction;
import org.eclipse.jdt.internal.debug.ui.actions.AddVariableAction;
import org.eclipse.jdt.internal.debug.ui.actions.AttachSourceAction;
import org.eclipse.jdt.internal.debug.ui.actions.MoveDownAction;
import org.eclipse.jdt.internal.debug.ui.actions.MoveUpAction;
import org.eclipse.jdt.internal.debug.ui.actions.RemoveAction;
import org.eclipse.jdt.internal.debug.ui.actions.RuntimeClasspathAction;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathContentProvider;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathEntry;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathLabelProvider;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathModel;
import org.eclipse.jdt.internal.debug.ui.classpath.IClasspathEntry;
import org.eclipse.jdt.internal.debug.ui.classpath.RuntimeClasspathViewer;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

/**
 *
 */
public class LTWAspectPathTab extends JavaClasspathTab {

	private static final String ERROR_MESSAGE = UIMessages.LTWAspectPathTab_errormessage;
	public static final String ATTR_ASPECTPATH = LaunchingPlugin.getUniqueIdentifier() + ".ASPECTPATH"; //$NON-NLS-1$
	protected RuntimeClasspathViewer fClasspathViewer;
	private LTWAspectpathModel fModel;
	
	/**
	 * The last launch config this tab was initialized from
	 */
	protected ILaunchConfiguration fLaunchConfiguration;
	
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_CLASSPATH_TAB);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		comp.setLayout(topLayout);		
		GridData gd;
		
		Label label = new Label(comp, SWT.NONE);
		label.setText(UIMessages.LTWAspectPathTab_label);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		
		fClasspathViewer = new RuntimeClasspathViewer(comp);
		fClasspathViewer.addEntriesChangedListener(this);
		fClasspathViewer.getTreeViewer().getControl().setFont(font);
		fClasspathViewer.getTreeViewer().setLabelProvider(new ClasspathLabelProvider());
		fClasspathViewer.getTreeViewer().setContentProvider(new ClasspathContentProvider(this));

		Composite pathButtonComp = new Composite(comp, SWT.NONE);
		GridLayout pathButtonLayout = new GridLayout();
		pathButtonLayout.marginHeight = 0;
		pathButtonLayout.marginWidth = 0;
		pathButtonComp.setLayout(pathButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		pathButtonComp.setLayoutData(gd);
		pathButtonComp.setFont(font);
		
		createPathButtons(pathButtonComp);
	}
	
	/**
	 * Creates the buttons to manipulate the classpath.
	 * 
	 * @param pathButtonComp composite buttons are contained in
	 * @since 3.0
	 */
	protected void createPathButtons(Composite pathButtonComp) {
		List advancedActions = new ArrayList(5);
		
		createButton(pathButtonComp, new MoveUpAction(fClasspathViewer));
		createButton(pathButtonComp, new MoveDownAction(fClasspathViewer));
		createButton(pathButtonComp, new RemoveAction(fClasspathViewer));
		createButton(pathButtonComp, new AddProjectAction(fClasspathViewer));
		createButton(pathButtonComp, new AddJarAction(fClasspathViewer));
		createButton(pathButtonComp, new AddExternalJarAction(fClasspathViewer, DIALOG_SETTINGS_PREFIX));

		RuntimeClasspathAction action = new AddFolderAction(null);								
		advancedActions.add(action);
		
		action = new AddExternalFolderAction(null, DIALOG_SETTINGS_PREFIX);								
		advancedActions.add(action);		

		action = new AddVariableAction(null);								
		advancedActions.add(action);	
		
		action = new AddLibraryAction(null);
		advancedActions.add(action);
		
		action = new AttachSourceAction(null, SWT.RADIO);								
		advancedActions.add(action);
		
		IAction[] adv = (IAction[])advancedActions.toArray(new IAction[advancedActions.size()]);
		createButton(pathButtonComp, new AddAdvancedAction(fClasspathViewer, adv));

	}

	/**
	 * Creates a button for the given action.
	 * 
	 * @param pathButtonComp parent composite for the button
	 * @param action the action triggered by the button
	 * @return the button that was created
	 */
	protected Button createButton(Composite pathButtonComp, RuntimeClasspathAction action) {
		Button button  = createPushButton(pathButtonComp, action.getText(), null);
		action.setButton(button);
		return button;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {

	}
	
	/**
	 * Returns the classpath entries currently specified by this tab.
	 * 
	 * @return the classpath entries currently specified by this tab
	 */
	private IRuntimeClasspathEntry[] getCurrentClasspath() {
		IClasspathEntry[] user = fModel.getEntries(ClasspathModel.USER);
		List entries = new ArrayList(user.length);
		IRuntimeClasspathEntry entry;
		IClasspathEntry userEntry;
		for (int i = 0; i < user.length; i++) {
			userEntry= user[i];
			entry = null;
			if (userEntry instanceof ClasspathEntry) {
				entry = ((ClasspathEntry)userEntry).getDelegate();
			} else if (userEntry instanceof IRuntimeClasspathEntry) {
				entry= (IRuntimeClasspathEntry) user[i];
			}
			if (entry != null) {
				entry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
				entries.add(entry);
			}
		}			
		return (IRuntimeClasspathEntry[]) entries.toArray(new IRuntimeClasspathEntry[entries.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		refresh(configuration);
		fClasspathViewer.getTreeViewer().expandToLevel(2);
	}

	/**
	 * Refreshes the classpath entries based on the current state of the given
	 * launch configuration.
	 */
	private void refresh(ILaunchConfiguration configuration) {
		if (configuration == getLaunchConfiguration()) {
			setDirty(false);
			return;		
		}

		setErrorMessage(null);		
		setLaunchConfiguration(configuration);
		try {
			createClasspathModel(configuration);
			if(fModel.getEntries(ClasspathModel.USER).length == 0) {
				setErrorMessage(ERROR_MESSAGE);
			}
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
		}
		
		fClasspathViewer.setLaunchConfiguration(configuration);
		fClasspathViewer.getTreeViewer().setInput(fModel);
		setDirty(false);
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (isDirty()) {
			IRuntimeClasspathEntry[] classpath = getCurrentClasspath();			
			try {
				List mementos = new ArrayList(classpath.length);
				for (int i = 0; i < classpath.length; i++) {
					IRuntimeClasspathEntry entry = classpath[i];
					mementos.add(entry.getMemento());
				}
				configuration.setAttribute(ATTR_ASPECTPATH, mementos);
			} catch (CoreException e) {
				JDIDebugUIPlugin.errorDialog(LauncherMessages.JavaClasspathTab_Unable_to_save_classpath_1, e); 
			}
		}
	}

	private void createClasspathModel(ILaunchConfiguration configuration) throws CoreException {
		fModel= new LTWAspectpathModel();
		List entries = configuration.getAttribute(ATTR_ASPECTPATH, Collections.EMPTY_LIST);
		IRuntimeClasspathEntry[] rtes = new IRuntimeClasspathEntry[entries.size()];
		Iterator iter = entries.iterator();
		int i = 0;
		while (iter.hasNext()) {
			rtes[i] = JavaRuntime.newRuntimeClasspathEntry((String)iter.next());
			i++;
		}
		for (int j = 0; j < rtes.length; j++) {
			fModel.addEntry(ClasspathModel.USER, rtes[j]);			
		}	
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration configuration) {
		List entries = Collections.EMPTY_LIST;
		setErrorMessage(null);
		try {
			entries = configuration.getAttribute(ATTR_ASPECTPATH, Collections.EMPTY_LIST);
		} catch (CoreException e) {
		}
		if(entries.size() == 0) {
			setErrorMessage(ERROR_MESSAGE);
		}
		return entries.size() > 0;
	}
	
	public String getName() {
		return UIMessages.LTWAspectPathTab_title;
	}

	/**
	 * Sets the launch configuration for this classpath tab
	 */
	private void setLaunchConfiguration(ILaunchConfiguration config) {
		fLaunchConfiguration = config;
	}	
	
	/**
	 * Returns the current launch configuration
	 */
	public ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}
	
	public Image getImage() {
		return AspectJImages.instance().getRegistry().get(((AJDTIcon)AspectJImages.instance().getStructureIcon(IProgramElement.Kind.ASPECT, IProgramElement.Accessibility.PUBLIC)).getImageDescriptor());
	}

}
