/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 *               Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.builder.AJBuildJob;
import org.eclipse.ajdt.internal.ui.AspectJProjectPropertiesPage;
import org.eclipse.ajdt.internal.ui.wizards.AspectPathBlock;
import org.eclipse.ajdt.internal.ui.wizards.InPathBlock;
import org.eclipse.ajdt.internal.ui.wizards.PathBlock;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.TreeListDialogField;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;

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
    private List activePages = new ArrayList();

    // manage those pages for whom performOk has not been executed
    private List remainingActivePages = new ArrayList();

    // manage buttons on all pages
    private Map /* IWorkbenchPropertyPage -> (Map of Button -> Boolean)*/buttonOriginalValues = new HashMap();

    // manage combo boxes on all pages
    private Map /* IWorkbenchPropertyPage -> (Map of Combo -> Integer)*/comboOriginalValues = new HashMap();

    // manage string field editors on all pages
    private Map /* IWorkbenchPropertyPage -> (Map of StringFieldEditors -> String)*/stringFieldEditorsOriginalValues = new HashMap();

    // manage selection buttons on all pages
    private Map /* IWorkbenchPropertyPage -> (Map of SelectionButtonDialogField -> Boolean)*/selectionButtonOriginalValues = new HashMap();

    // manage tree list dialog fields on all pages
    private Map /* IWorkbenchPropertyPage -> (Map of TreeListDialogField -> List)*/dialogFieldOriginalValues = new HashMap();

    private boolean useProjectSettingsOriginalValue;

    interface AJDTPathBlockPage {
    }

    declare parents: PathBlock implements AJDTPathBlockPage;

    pointcut interestingPage(): 
        within(AspectJProjectPropertiesPage) ||
        within(AJCompilerPreferencePage);

    // interested in when the AJDT property page is created
    pointcut creationOfAnInterestingPage(IWorkbenchPropertyPage page) : 
        (execution(protected Control createContents(Composite)))
        && interestingPage() && this(page);

    // before the page is created, add the page to the lists
    before(IWorkbenchPropertyPage page) : creationOfAnInterestingPage(page) {
        activePages.add(page);
        remainingActivePages.add(page);
        if (page instanceof AJCompilerPreferencePage) {
            useProjectSettingsOriginalValue = ((AJCompilerPreferencePage) page)
                    .hasProjectSpecificOptions(((AJCompilerPreferencePage) page)
                            .getProject());
        }
    }

    // interested in when the AJDT property page is disposed
    pointcut disposalOfAnInterestingPage(IWorkbenchPropertyPage page) :
        (execution(void dispose()))
        && interestingPage() && this(page);

    // before the page is disposed, remove it from the lists
    before(IWorkbenchPropertyPage page) : disposalOfAnInterestingPage(page) {
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

    pointcut interestingPathBlockPages() : within(PathBlock) || within(AspectPathBlock) || within(InPathBlock);

    pointcut setSelection(Button button, boolean val,
            IWorkbenchPropertyPage page):
        call(* setSelection(boolean)) && args(val) && target(button) && this(page);
    
    // remember the first value put into the buttons
    before(Button b, boolean val, IWorkbenchPropertyPage page):
            setSelection(b,val,page) && interestingPage() { 
        
        // check to see if this button is ignored
        if (AJCompilerPreferencePage.NO_BUILD_ON_CHANGE.equals(b.getData(AJCompilerPreferencePage.NO_BUILD_ON_CHANGE))) {
            return;
        }
        
        if (!buttonOriginalValues.containsKey(page)) {
            Map buttonValues = new HashMap();
            buttonValues.put(b, new Boolean(val));
            buttonOriginalValues.put(page, buttonValues);
        } else {
            Map buttonValues = (Map) buttonOriginalValues.get(page);
            if (!buttonValues.containsKey(b)) {
                buttonValues.put(b, new Boolean(val));
            }
        }
    }

    pointcut comboSelect(Combo combo, int selection, IWorkbenchPropertyPage page) :
        call(* select(int)) && args(selection) && target(combo) && this(page);

    // remember the first value put into the combo boxes
    before(Combo combo, int selection, IWorkbenchPropertyPage page) :
        comboSelect(combo,selection,page) && interestingPage() {
        if (!comboOriginalValues.containsKey(page)) {
            Map comboValues = new HashMap();
            comboValues.put(combo, new Integer(selection));
            comboOriginalValues.put(page, comboValues);
        } else {
            Map comboValues = (Map) comboOriginalValues.get(page);
            if (!comboValues.containsKey(combo)) {
                comboValues.put(combo, new Integer(selection));
            }
        }
    }

    pointcut setStringValue(StringFieldEditor editor, String value,
            IWorkbenchPropertyPage page) :
        call(* setStringValue(String)) && args(value) && target(editor) && this(page);

    // remember the first value put into the StringFieldEditors
    before(StringFieldEditor editor, String value, IWorkbenchPropertyPage page) :
        setStringValue(editor,value,page) && interestingPage() {
        if (!stringFieldEditorsOriginalValues.containsKey(page)) {
            Map editorValues = new HashMap();
            editorValues.put(editor, value);
            stringFieldEditorsOriginalValues.put(page, editorValues);
        } else {
            Map editorValues = (Map) stringFieldEditorsOriginalValues
                    .get(page);
            if (!editorValues.containsKey(editor)) {
                editorValues.put(editor, value);
            }
        }
    }

    //    pointcut setButtonSelection(SelectionButtonDialogField button, boolean value, IWorkbenchPropertyPage page) :
    //        call(* setSelection(boolean)) && args(value) && target(button) && this(page);
    //    
    //    // remember the first value put into the SelectionButtonDialogFields
    //    before(SelectionButtonDialogField button, boolean value, IWorkbenchPropertyPage page) :
    //        setButtonSelection(button,value,page) && interestingPage() {
    //        if (!selectionButtonOriginalValues.containsKey(page)) {
    //            Map buttonValues = new HashMap();
    //            buttonValues.put(button,new Boolean(value));
    //            selectionButtonOriginalValues.put(page,buttonValues);
    //        } else {
    //            Map buttonValues = (Map)selectionButtonOriginalValues.get(page);
    //            if (!buttonValues.containsKey(button)) {
    //                buttonValues.put(button, new Boolean(value)); 
    //            }
    //        }    
    //    }

    pointcut setElements(TreeListDialogField dialogField, List elements,
            AJDTPathBlockPage basePage) :
        call(* setElements(List)) && args(elements) && target(dialogField) && this(basePage);

    // remember the first value put into the TreeListDialogFields
    before(TreeListDialogField dialogField, List elements,
            AJDTPathBlockPage basePage) :
        setElements(dialogField, elements, basePage) && interestingPathBlockPages() {

        IWorkbenchPropertyPage page = null;
        // We need to associate the TreeListDialogField with the related IWorkbenchPropertyPage object
        // rather than the related AJDTPathBlockPage (so we can check if the contents
        // of the TreeListDialogField more easily in settingsHaveChanged(IWorkbenchPropertyPage)).
        // We therefore iterate through the active pages.       
        for (Iterator iter = activePages.iterator(); iter.hasNext();) {
            IWorkbenchPropertyPage ajdtPage = (IWorkbenchPropertyPage) iter
                    .next();
            if ((basePage instanceof PathBlock)
                    && (ajdtPage instanceof AspectJProjectPropertiesPage)) {
                page = ajdtPage;
            }
        }

        if (!dialogFieldOriginalValues.containsKey(page)) {
            Map fieldValues = new HashMap();
            List listOfElements = new ArrayList();
            listOfElements.addAll(elements);
            fieldValues.put(dialogField, listOfElements);
            dialogFieldOriginalValues.put(page, fieldValues);
        } else {
            Map fieldValues = (Map) dialogFieldOriginalValues
                    .get(page);
            if (!fieldValues.containsKey(dialogField)) {
                List listOfElements = new ArrayList();
                listOfElements.addAll(elements);
                fieldValues.put(dialogField, listOfElements);
            }
        }
    }

    private boolean settingsHaveChanged() {
        Iterator iterator = activePages.iterator();
        while (iterator.hasNext()) {
            IWorkbenchPropertyPage page = (IWorkbenchPropertyPage) iterator
                    .next();
            if (settingsHaveChangedOnPage(page)) {
                return true;
            }
        }
        return false;
    }

    private boolean settingsHaveChangedOnPage(IWorkbenchPropertyPage page) {
        Map buttonsOnPage = (Map) buttonOriginalValues.get(page);
        if (buttonsOnPage != null) {
            Iterator buttons = buttonsOnPage.keySet().iterator();
            while (buttons.hasNext()) {
                Button b = (Button) buttons.next();
                if (b.getSelection() != ((Boolean) buttonsOnPage.get(b))
                        .booleanValue())
                    return true;
            }
        }

        Map comboBoxesOnPage = (Map) comboOriginalValues.get(page);
        if (comboBoxesOnPage != null) {
            Iterator comboboxes = comboBoxesOnPage.keySet().iterator();
            while (comboboxes.hasNext()) {
                Combo c = (Combo) comboboxes.next();
                if (c.getSelectionIndex() != ((Integer) comboBoxesOnPage.get(c))
                        .intValue())
                    return true;
            }
        }

        Map editorsOnPage = (Map) stringFieldEditorsOriginalValues
                .get(page);
        if (editorsOnPage != null) {
            Iterator editors = editorsOnPage.keySet().iterator();
            while (editors.hasNext()) {
                StringFieldEditor e = (StringFieldEditor) editors.next();
                if (!(e.getStringValue()
                        .equals(((String) editorsOnPage.get(e)))))
                    return true;
            }
        }

        Map selectionButtonsOnPage = (Map) selectionButtonOriginalValues
                .get(page);
        if (selectionButtonsOnPage != null) {
            Iterator buttons = selectionButtonsOnPage.keySet().iterator();
            while (buttons.hasNext()) {
                SelectionButtonDialogField b = (SelectionButtonDialogField) buttons
                        .next();
                if (b.isSelected() != ((Boolean) selectionButtonsOnPage.get(b))
                        .booleanValue())
                    return true;
            }
        }

        Map dialogFieldsOnPage = (Map) dialogFieldOriginalValues
                .get(page);
        if (dialogFieldsOnPage != null) {
            Iterator fields = dialogFieldsOnPage.keySet().iterator();
            while (fields.hasNext()) {
                TreeListDialogField f = (TreeListDialogField) fields.next();
                List currentLibs = f.getElements();
                List originalLibs = (List) dialogFieldsOnPage.get(f);
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
            if (ajPage.useProjectSettings() != useProjectSettingsOriginalValue) {
                return true;
            }
        }
        return false;
    }

    private void resetButtonsOnPage(IWorkbenchPropertyPage page) {
        Map buttonsOnPage = (Map) buttonOriginalValues.get(page);
        if (buttonsOnPage != null) {
            Iterator buttons = buttonsOnPage.keySet().iterator();
            while (buttons.hasNext()) {
                Button b = (Button) buttons.next();
                buttonsOnPage.put(b, new Boolean(b.getSelection()));
            }
        }
    }

    private void resetComboBoxesOnPage(IWorkbenchPropertyPage page) {
        Map comboBoxesOnPage = (Map) comboOriginalValues.get(page);
        if (comboBoxesOnPage != null) {
            Iterator boxes = comboBoxesOnPage.keySet().iterator();
            while (boxes.hasNext()) {
                Combo c = (Combo) boxes.next();
                comboBoxesOnPage.put(c, new Integer(c.getSelectionIndex()));
            }
        }
    }

    private void resetEditorsOnPage(IWorkbenchPropertyPage page) {
        Map editorsOnPage = (Map) stringFieldEditorsOriginalValues
                .get(page);
        if (editorsOnPage != null) {
            Iterator editors = editorsOnPage.keySet().iterator();
            while (editors.hasNext()) {
                StringFieldEditor e = (StringFieldEditor) editors.next();
                editorsOnPage.put(e, e.getStringValue());
            }
        }
    }

    private void resetSelectionButtonsOnPage(IWorkbenchPropertyPage page) {
        Map buttonsOnPage = (Map) selectionButtonOriginalValues
                .get(page);
        if (buttonsOnPage != null) {
            Iterator buttons = buttonsOnPage.keySet().iterator();
            while (buttons.hasNext()) {
                SelectionButtonDialogField b = (SelectionButtonDialogField) buttons
                        .next();
                buttonsOnPage.put(b, new Boolean(b.isSelected()));
            }
        }
    }

    private void resetDialogFieldsOnPage(IWorkbenchPropertyPage page) {
        Map fieldsOnPage = (Map) dialogFieldOriginalValues
                .get(page);
        if (fieldsOnPage != null) {
            Iterator fields = fieldsOnPage.keySet().iterator();
            while (fields.hasNext()) {
                TreeListDialogField f = (TreeListDialogField) fields.next();
                fieldsOnPage.put(f, f.getElements());
            }
        }
    }

    pointcut pageCompleting(IWorkbenchPropertyPage page) : 
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
            useProjectSettingsOriginalValue = ((AJCompilerPreferencePage) page)
            .hasProjectSpecificOptions(((AJCompilerPreferencePage) page)
                    .getProject());
            compilerPageDoBuild = false;
        }
    }

    pointcut openingMessageDialog(MessageDialog dialog,
            IWorkbenchPropertyPage page) :
        call(* open()) && target(dialog) && this(page);

    after(MessageDialog dialog, IWorkbenchPropertyPage page) returning : 
        openingMessageDialog(dialog,page) && within(AJCompilerPreferencePage) {
        if (!((AJCompilerPreferencePage) page).isTesting()) {
            if (dialog.getReturnCode() == 0) {
                compilerPageDoBuild = true;
            }
        } else if (((AJCompilerPreferencePage) page).isBuildNow()) {
            compilerPageDoBuild = true;
        }
    }

    /**
     * Returning whether the user chose to build on the compiler page
     * or whether autobuilding is set
     */
    private boolean wantToBuild() {
        for (Iterator iter = activePages.iterator(); iter.hasNext();) {
            IWorkbenchPropertyPage page = (IWorkbenchPropertyPage) iter.next();
            if ((page instanceof AJCompilerPreferencePage)
                    && settingsHaveChangedOnPage(page)) {
                return compilerPageDoBuild;
            }
        }
        return AspectJPlugin.getWorkspace().getDescription().isAutoBuilding();
    }

    /**
     * Build the project
     */
    private void doProjectBuild(IWorkbenchPropertyPage prefPage) {
        IProject tempProject = null;
        if (prefPage instanceof AspectJProjectPropertiesPage) {
            tempProject = ((AspectJProjectPropertiesPage) prefPage)
                    .getThisProject();
        } else if (prefPage instanceof AJCompilerPreferencePage) {

            AJCompilerPreferencePage Ajpage = ((AJCompilerPreferencePage) prefPage);

            if (!Ajpage.isProjectPreferencePage()) {
                buildAllProjects(Ajpage);
            } else
                tempProject = ((AJCompilerPreferencePage) prefPage)
                        .getProject();
        }
        final IProject project = tempProject;
        if (project != null) {
            AJBuildJob job = new AJBuildJob(project, IncrementalProjectBuilder.FULL_BUILD);
            job.schedule();
        }
    }

    private void buildAllProjects(AJCompilerPreferencePage prefPage) {
        IProject[] projects = prefPage.getProjects();
        for (int i = 0; i < projects.length; i++) {
            IProject AJproject = projects[i];

            final IProject project = (AJproject);
            if (project != null) {
                AJBuildJob job = new AJBuildJob(project, IncrementalProjectBuilder.FULL_BUILD);
                job.schedule();
            }

        }

    }

}