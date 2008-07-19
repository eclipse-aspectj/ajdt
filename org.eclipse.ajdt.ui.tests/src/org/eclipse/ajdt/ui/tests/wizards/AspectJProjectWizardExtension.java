/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Ian McGrath - initial version
 * 	Sian January - updated when wizard was 
 * 		updated to new Java project wizard style (bug 78264)
 ******************************************************************************/

package org.eclipse.ajdt.ui.tests.wizards;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.internal.ui.wizards.AspectJProjectWizard;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
import org.eclipse.swt.widgets.Composite;


public class AspectJProjectWizardExtension extends AspectJProjectWizard {

	private String projectDefaultName;
	
	/**
	 * Used by the test suite to simulate user input to the dialog pages
	 */
	public AspectJProjectWizardExtension() {
		super();
	}

	public void setProjectDefaultName(String name) {
		projectDefaultName = name;
	}
	
	/**
	 * Overridden to use JaveProjectWizardFirstPageExtension instead of JavaProjectWizardFirstPage 
	 */
	
	public void addPages() {
        fFirstPage= new NewJavaProjectWizardPageOne();
		fFirstPage.setTitle(UIMessages.NewAspectJProject_CreateAnAspectJProject);
		fFirstPage.setDescription(UIMessages.NewAspectJProject_CreateAnAspectJProjectDescription);
        addPage(fFirstPage);
        fSecondPage= new NewJavaProjectWizardPageTwo(fFirstPage);
        fSecondPage.setTitle(UIMessages.NewAspectJProject_BuildSettings);
        fSecondPage.setDescription(UIMessages.NewAspectJProject_BuildSettingsDescription);
        addPage(fSecondPage);
    }
	
	/**
	 * Overridden to add simulated user input
	 */
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
		try {
			fFirstPage.setProjectName(projectDefaultName);

			// Following reflection code does this:
			// fFirstPage.fLocationGroup.fWorkspaceRadio.setSelection(true);
			Field f = NewJavaProjectWizardPageOne.class.getDeclaredField("fLocationGroup"); //$NON-NLS-1$
			f.setAccessible(true);
			Object o = f.get(fFirstPage);
			f =o.getClass().getDeclaredField("fWorkspaceRadio"); //$NON-NLS-1$
			f.setAccessible(true);
			o = f.get(o);
			Method m = o.getClass().getDeclaredMethod("setSelection",new Class[]{Boolean.TYPE}); //$NON-NLS-1$
			m.setAccessible(true);
			m.invoke(o,new Object[]{Boolean.TRUE});
			
			// Following reflection code does this:
			// fFirstPage.fLayoutGroup.fStdRadio.setSelection(true);
			f = NewJavaProjectWizardPageOne.class.getDeclaredField("fLayoutGroup"); //$NON-NLS-1$
			f.setAccessible(true);
			o = f.get(fFirstPage);
			f = o.getClass().getDeclaredField("fStdRadio"); //$NON-NLS-1$
			f.setAccessible(true);
			o = f.get(o);
			m = o.getClass().getDeclaredMethod("setSelection",new Class[]{Boolean.TYPE}); //$NON-NLS-1$
			m.setAccessible(true);
			m.invoke(o,new Object[]{Boolean.TRUE});
			
			
		} catch (IllegalArgumentException e) {
		} catch (SecurityException e) {
		} catch (IllegalAccessException e) {
		} catch (NoSuchFieldException e) {
		} catch (NoSuchMethodException e) {
		} catch (InvocationTargetException e) {
		}	
	}
}
