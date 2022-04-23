/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation     - initial API and implementation
 *               Helen Hawkins       - initial version
 *               Alexander Kriegisch - generics refactoring
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.builder.AJBuildJob;
import org.eclipse.ajdt.internal.ui.AspectJProjectPropertiesPage;
import org.eclipse.ajdt.internal.ui.wizards.PathBlock;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.TreeListDialogField;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.ui.IWorkbenchPropertyPage;

import static org.eclipse.ajdt.internal.ui.preferences.AJCompilerPreferencePage.NO_BUILD_ON_CHANGE;

/**
 * This aspect handles the building of projects whenever OK is pressed on any of
 * the AspectJ project preference pages.
 *
 * @author hawkinsh
 */
aspect PreferencePageBuilder {

  // manage whether or not the user has chosen to build on the compiler page
  private boolean compilerPageDoBuild = false;

  // manage those pages which have been activated
  private final List<IWorkbenchPropertyPage> activePages = new ArrayList<>();

  // manage those pages for whom performOk has not been executed
  private final List<IWorkbenchPropertyPage> remainingActivePages = new ArrayList<>();

  // manage buttons on all pages
  private final Map<IWorkbenchPropertyPage, Map<Button, Boolean>> buttonOriginalValues = new HashMap<>();

  // manage combo boxes on all pages
  private final Map<IWorkbenchPropertyPage, Map<Combo, Integer>> comboOriginalValues = new HashMap<>();

  // manage string field editors on all pages
  private final Map<IWorkbenchPropertyPage, Map<StringFieldEditor, String>> stringFieldEditorsOriginalValues = new HashMap<>();

  // manage selection buttons on all pages
  private final Map<IWorkbenchPropertyPage, Map<SelectionButtonDialogField, Boolean>> selectionButtonOriginalValues = new HashMap<>();

  // manage tree list dialog fields on all pages
  private final Map<IWorkbenchPropertyPage, Map<TreeListDialogField<?>, List<?>>> dialogFieldOriginalValues = new HashMap<>();

  private boolean useProjectSettingsOriginalValue;

  interface AJDTPathBlockPage { }

  declare parents:PathBlock implements AJDTPathBlockPage;

  pointcut interestingPage():
    within(AspectJProjectPropertiesPage) || within(AJCompilerPreferencePage);

  // interested in when the AJDT property page is created
  pointcut creationOfAnInterestingPage(IWorkbenchPropertyPage page):
    execution(protected Control createContents(Composite)) && interestingPage() && this(page);

  // before the page is created, add the page to the lists
  before(IWorkbenchPropertyPage page): creationOfAnInterestingPage(page) {
    activePages.add(page);
    remainingActivePages.add(page);
    if (page instanceof AJCompilerPreferencePage) {
      AJCompilerPreferencePage preferencePage = (AJCompilerPreferencePage) page;
      useProjectSettingsOriginalValue = preferencePage.hasProjectSpecificOptions(preferencePage.getProject());
    }
  }

  // interested in when the AJDT property page is disposed
  pointcut disposalOfAnInterestingPage(IWorkbenchPropertyPage page):
    execution(void dispose()) && interestingPage() && this(page);

  // before the page is disposed, remove it from the lists
  before(IWorkbenchPropertyPage page): disposalOfAnInterestingPage(page) {
    buttonOriginalValues.remove(page);
    comboOriginalValues.remove(page);
    stringFieldEditorsOriginalValues.remove(page);
    selectionButtonOriginalValues.remove(page);
    dialogFieldOriginalValues.remove(page);
    activePages.remove(page);
    if (page instanceof AJCompilerPreferencePage) {
      compilerPageDoBuild = false;
    }
    remainingActivePages.remove(page);
  }

  pointcut interestingPathBlockPages():
    within(PathBlock) || within(AspectPathBlock) || within(InPathBlock);

  pointcut setSelection(Button button, boolean val, IWorkbenchPropertyPage page):
    call(* setSelection(boolean)) && args(val) && target(button) && this(page);

  // remember the first value put into the buttons
  before(Button b, boolean val, IWorkbenchPropertyPage page):
    setSelection(b,val,page) && interestingPage() {

    // check to see if this button is ignored
    if (NO_BUILD_ON_CHANGE.equals(b.getData(NO_BUILD_ON_CHANGE))) {
      return;
    }

    if (!buttonOriginalValues.containsKey(page)) {
      Map<Button, Boolean> buttonValues = new HashMap<>();
      buttonValues.put(b, val);
      buttonOriginalValues.put(page, buttonValues);
    }
    else {
      Map<Button, Boolean> buttonValues = buttonOriginalValues.get(page);
      if (!buttonValues.containsKey(b)) {
        buttonValues.put(b, val);
      }
    }
  }

  pointcut comboSelect(Combo combo, int selection, IWorkbenchPropertyPage page):
    call(* select(int)) && args(selection) && target(combo) && this(page);

  // remember the first value put into the combo boxes
  before(Combo combo, int selection, IWorkbenchPropertyPage page):
    comboSelect(combo,selection,page) && interestingPage() {
    if (!comboOriginalValues.containsKey(page)) {
      Map<Combo, Integer> comboValues = new HashMap<>();
      comboValues.put(combo, selection);
      comboOriginalValues.put(page, comboValues);
    }
    else {
      Map<Combo, Integer> comboValues = comboOriginalValues.get(page);
      if (!comboValues.containsKey(combo)) {
        comboValues.put(combo, selection);
      }
    }
  }

  pointcut setStringValue(StringFieldEditor editor, String value, IWorkbenchPropertyPage page):
    call(* setStringValue(String)) && args(value) && target(editor) && this(page);

  // remember the first value put into the StringFieldEditors
  before(StringFieldEditor editor, String value, IWorkbenchPropertyPage page):
    setStringValue(editor,value,page) && interestingPage() {
    if (!stringFieldEditorsOriginalValues.containsKey(page)) {
      Map<StringFieldEditor, String> editorValues = new HashMap<>();
      editorValues.put(editor, value);
      stringFieldEditorsOriginalValues.put(page, editorValues);
    }
    else {
      Map<StringFieldEditor, String> editorValues = stringFieldEditorsOriginalValues.get(page);
      if (!editorValues.containsKey(editor))
        editorValues.put(editor, value);
    }
  }

  pointcut setElements(TreeListDialogField dialogField, List elements, AJDTPathBlockPage basePage):
    call(* setElements(List)) && args(elements) && target(dialogField) && this(basePage);

  // remember the first value put into the TreeListDialogFields
  before(TreeListDialogField dialogField, List elements, AJDTPathBlockPage basePage):
    setElements(dialogField, elements, basePage) && interestingPathBlockPages() {

    IWorkbenchPropertyPage page = null;
    // We need to associate the TreeListDialogField with the related IWorkbenchPropertyPage object
    // rather than the related AJDTPathBlockPage (so we can check if the contents
    // of the TreeListDialogField more easily in settingsHaveChanged(IWorkbenchPropertyPage)).
    // We therefore iterate through the active pages.
    for (IWorkbenchPropertyPage ajdtPage : activePages) {
      if (basePage instanceof PathBlock && ajdtPage instanceof AspectJProjectPropertiesPage)
        page = ajdtPage;
    }

    if (!dialogFieldOriginalValues.containsKey(page)) {
      Map<TreeListDialogField<?>, List<?>> fieldValues = new HashMap<>();
      List<?> listOfElements = new ArrayList(elements);
      fieldValues.put(dialogField, listOfElements);
      dialogFieldOriginalValues.put(page, fieldValues);
    }
    else {
      Map<TreeListDialogField<?>, List<?>> fieldValues = dialogFieldOriginalValues.get(page);
      if (!fieldValues.containsKey(dialogField)) {
        List<?> listOfElements = new ArrayList(elements);
        fieldValues.put(dialogField, listOfElements);
      }
    }
  }

  private boolean settingsHaveChanged() {
    for (IWorkbenchPropertyPage page : activePages) {
      if (settingsHaveChangedOnPage(page))
        return true;
    }
    return false;
  }

  private boolean settingsHaveChangedOnPage(IWorkbenchPropertyPage page) {
    Map<Button, Boolean> buttonsOnPage = buttonOriginalValues.get(page);
    if (buttonsOnPage != null) {
      for (Button b : buttonsOnPage.keySet()) {
        if (b.getSelection() != buttonsOnPage.get(b))
          return true;
      }
    }

    Map<Combo, Integer> comboBoxesOnPage = comboOriginalValues.get(page);
    if (comboBoxesOnPage != null) {
      for (Combo c : comboBoxesOnPage.keySet()) {
        if (c.getSelectionIndex() != comboBoxesOnPage.get(c))
          return true;
      }
    }

    Map<StringFieldEditor, String> editorsOnPage = stringFieldEditorsOriginalValues.get(page);
    if (editorsOnPage != null) {
      for (StringFieldEditor e : editorsOnPage.keySet()) {
        if (!(e.getStringValue().equals(editorsOnPage.get(e))))
          return true;
      }
    }

    Map<SelectionButtonDialogField, Boolean> selectionButtonsOnPage = selectionButtonOriginalValues.get(page);
    if (selectionButtonsOnPage != null) {
      for (SelectionButtonDialogField b : selectionButtonsOnPage.keySet()) {
        if (b.isSelected() != selectionButtonsOnPage.get(b))
          return true;
      }
    }

    Map<TreeListDialogField<?>, List<?>> dialogFieldsOnPage = dialogFieldOriginalValues.get(page);
    if (dialogFieldsOnPage != null) {
      for (TreeListDialogField<?> f : dialogFieldsOnPage.keySet()) {
        List<?> currentLibs = f.getElements();
        List<?> originalLibs = dialogFieldsOnPage.get(f);
        if (currentLibs.size() != originalLibs.size()) {
          return true;
        }
        for (int i = 0; i < originalLibs.size(); i++) {
          if (!(originalLibs.get(i).equals(currentLibs.get(i)))) {
            return true;
          }
        }
      }
    }
    if (page instanceof AJCompilerPreferencePage) {
      AJCompilerPreferencePage ajPage = (AJCompilerPreferencePage) page;
      return ajPage.useProjectSettings() != useProjectSettingsOriginalValue;
    }
    return false;
  }

  private void resetButtonsOnPage(IWorkbenchPropertyPage page) {
    Map<Button, Boolean> buttonsOnPage = buttonOriginalValues.get(page);
    if (buttonsOnPage != null) {
      // TODO: Apply "replace with 'replaceAll' method call" refactoring after
      //       https://youtrack.jetbrains.com/issue/IDEA-292430 is resolved
      //noinspection Java8MapApi
      for (Button b : buttonsOnPage.keySet())
        buttonsOnPage.put(b, b.getSelection());
    }
  }

  private void resetComboBoxesOnPage(IWorkbenchPropertyPage page) {
    Map<Combo, Integer> comboBoxesOnPage = comboOriginalValues.get(page);
    if (comboBoxesOnPage != null) {
      // TODO: Apply "replace with 'replaceAll' method call" refactoring after
      //       https://youtrack.jetbrains.com/issue/IDEA-292430 is resolved
      //noinspection Java8MapApi
      for (Combo c : comboBoxesOnPage.keySet())
        comboBoxesOnPage.put(c, c.getSelectionIndex());
    }
  }

  private void resetEditorsOnPage(IWorkbenchPropertyPage page) {
    Map<StringFieldEditor, String> editorsOnPage = stringFieldEditorsOriginalValues.get(page);
    if (editorsOnPage != null) {
      // TODO: Apply "replace with 'replaceAll' method call" refactoring after
      //       https://youtrack.jetbrains.com/issue/IDEA-292430 is resolved
      //noinspection Java8MapApi
      for (StringFieldEditor e : editorsOnPage.keySet())
        editorsOnPage.put(e, e.getStringValue());
    }
  }

  private void resetSelectionButtonsOnPage(IWorkbenchPropertyPage page) {
    Map<SelectionButtonDialogField, Boolean> buttonsOnPage = selectionButtonOriginalValues.get(page);
    if (buttonsOnPage != null) {
      // TODO: Apply "replace with 'replaceAll' method call" refactoring after
      //       https://youtrack.jetbrains.com/issue/IDEA-292430 is resolved
      //noinspection Java8MapApi
      for (SelectionButtonDialogField b : buttonsOnPage.keySet())
        buttonsOnPage.put(b, b.isSelected());
    }
  }

  private void resetDialogFieldsOnPage(IWorkbenchPropertyPage page) {
    Map<TreeListDialogField<?>, List<?>> fieldsOnPage = dialogFieldOriginalValues.get(page);
    if (fieldsOnPage != null)
      // TODO: Apply "replace with 'replaceAll' method call" refactoring after
      //       https://youtrack.jetbrains.com/issue/IDEA-292430 is resolved
      //noinspection Java8MapApi
      for (TreeListDialogField<?> f : fieldsOnPage.keySet())
        fieldsOnPage.put(f, f.getElements());
  }

  pointcut pageCompleting(IWorkbenchPropertyPage page):
    (execution(boolean performOk()) || execution(void performApply())) && interestingPage() && this(page);

  // performOk not running because of performApply
  after(IWorkbenchPropertyPage page) returning: pageCompleting(page) && !cflow(execution(* performApply())) {
    if (remainingActivePages.size() == 1) {
      if (wantToBuild() && settingsHaveChanged()) {
        doProjectBuild(page);
      }
      // clear all the lists
      buttonOriginalValues.clear();
      comboOriginalValues.clear();
      stringFieldEditorsOriginalValues.clear();
      selectionButtonOriginalValues.clear();
      dialogFieldOriginalValues.clear();
      activePages.clear();
      compilerPageDoBuild = false;
    }
    remainingActivePages.remove(page);
  }

  // performOk running because performApply is running
  after(IWorkbenchPropertyPage page) returning: pageCompleting(page) && cflow(execution(* performApply())) {
    if (wantToBuild() && settingsHaveChangedOnPage(page)) {
      doProjectBuild(page);
    }
    resetButtonsOnPage(page);
    resetComboBoxesOnPage(page);
    resetEditorsOnPage(page);
    resetSelectionButtonsOnPage(page);
    resetDialogFieldsOnPage(page);
    if (page instanceof AJCompilerPreferencePage) {
      AJCompilerPreferencePage preferencePage = (AJCompilerPreferencePage) page;
      useProjectSettingsOriginalValue = preferencePage.hasProjectSpecificOptions(preferencePage.getProject());
      compilerPageDoBuild = false;
    }
  }

  pointcut openingMessageDialog(MessageDialog dialog, IWorkbenchPropertyPage page):
    call(* open()) && target(dialog) && this(page);

  after(MessageDialog dialog, IWorkbenchPropertyPage page) returning :
    openingMessageDialog(dialog,page) && within(AJCompilerPreferencePage) {

    if (!((AJCompilerPreferencePage) page).isTesting()) {
      if (dialog.getReturnCode() == 0) {
        compilerPageDoBuild = true;
      }
    }
    else if (((AJCompilerPreferencePage) page).isBuildNow()) {
      compilerPageDoBuild = true;
    }
  }

  /**
   * Returning whether the user chose to build on the compiler page
   * or whether autobuilding is set
   */
  private boolean wantToBuild() {
    for (IWorkbenchPropertyPage page : activePages) {
      if (page instanceof AJCompilerPreferencePage && settingsHaveChangedOnPage(page))
        return compilerPageDoBuild;
    }
    return AspectJPlugin.getWorkspace().getDescription().isAutoBuilding();
  }

  /**
   * Build the project
   */
  private void doProjectBuild(IWorkbenchPropertyPage prefPage) {
    IProject tempProject = null;
    if (prefPage instanceof AspectJProjectPropertiesPage)
      tempProject = ((AspectJProjectPropertiesPage) prefPage).getThisProject();
    else if (prefPage instanceof AJCompilerPreferencePage) {
      AJCompilerPreferencePage preferencePage = (AJCompilerPreferencePage) prefPage;
      if (!preferencePage.isProjectPreferencePage())
        buildAllProjects(preferencePage);
      else
        tempProject = preferencePage.getProject();
    }
    final IProject project = tempProject;
    if (project != null) {
      AJBuildJob job = new AJBuildJob(project, IncrementalProjectBuilder.FULL_BUILD);
      job.schedule();
    }
  }

  private void buildAllProjects(AJCompilerPreferencePage prefPage) {
    IProject[] projects = prefPage.getProjects();
    for (IProject AJproject : projects) {
      if ((AJproject) != null) {
        AJBuildJob job = new AJBuildJob((AJproject), IncrementalProjectBuilder.FULL_BUILD);
        job.schedule();
      }
    }
  }

}
