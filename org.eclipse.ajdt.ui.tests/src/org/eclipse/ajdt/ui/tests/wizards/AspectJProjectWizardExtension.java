/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Ian McGrath  - initial version
 *   Sian January - updated when wizard was updated to new Java project wizard
 *                  style (bug 78264)
 ******************************************************************************/

package org.eclipse.ajdt.ui.tests.wizards;

import org.eclipse.ajdt.internal.ui.wizards.AspectJProjectWizard;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.swt.widgets.Composite;

import java.lang.reflect.Field;

public class AspectJProjectWizardExtension extends AspectJProjectWizard {
  private String projectName;

  public AspectJProjectWizardExtension(String projectName) {
    this.projectName = projectName;
  }

  /**
   * Overridden to add simulated user input
   */
  public void createPageControls(Composite pageContainer) {
    super.createPageControls(pageContainer);
    fFirstPage.setProjectName(projectName);
    useProjectFolderAsRoot();
    doNotCreateModuleInfo();
  }

  protected void useProjectFolderAsRoot() {
    SelectionButtonDialogField fStdRadio;
    try {
      // Reflectively fetch: fFirstPage.fLayoutGroup.fStdRadio
      Field field = NewJavaProjectWizardPageOne.class.getDeclaredField("fLayoutGroup"); //$NON-NLS-1$
      field.setAccessible(true);
      Object fLayoutGroup = field.get(fFirstPage);
      field = fLayoutGroup.getClass().getDeclaredField("fStdRadio"); //$NON-NLS-1$
      field.setAccessible(true);
      fStdRadio = (SelectionButtonDialogField) field.get(fLayoutGroup);
    }
    catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Could not select \"Use project folder as root\" option", e);
    }

    fStdRadio.setSelection(true);
  }

  protected void doNotCreateModuleInfo() {
    SelectionButtonDialogField fCreateModuleInfo;
    try {
      // Reflectively fetch: fFirstPage.fModuleGroup.fCreateModuleInfo
      Field field = NewJavaProjectWizardPageOne.class.getDeclaredField("fModuleGroup"); //$NON-NLS-1$
      field.setAccessible(true);
      Object fModuleGroup = field.get(fFirstPage);
      field = fModuleGroup.getClass().getDeclaredField("fCreateModuleInfo"); //$NON-NLS-1$
      field.setAccessible(true);
      fCreateModuleInfo = (SelectionButtonDialogField) field.get(fModuleGroup);
    }
    catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Could not uncheck \"Create module-info.java\" option", e);
    }

    // Deactivate creation of module-info.java, because
    //   a) AJDT is unprepared for JMS Java modules anyway (AJC is, though) -> TODO: fix
    //   b) we want to avoid the pop-up dialogue asking about module name, which would make the test time out
    //      because we are not handling the pop-up.
    fCreateModuleInfo.setEnabled(true);
    fCreateModuleInfo.setSelection(false);
  }

}
