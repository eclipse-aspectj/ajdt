/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.preferences;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.internal.ui.AspectJProjectPropertiesPage;
import org.eclipse.ajdt.internal.ui.CompilerPropertyPage;
import org.eclipse.ajdt.internal.ui.wizards.AspectPathBlock;
import org.eclipse.ajdt.internal.ui.wizards.InPathBlock;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

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
    private Hashtable /* PropertyPage -> (Hashtable of Button -> Boolean)*/ buttonOriginalValues = new Hashtable();
    // manage combo boxes on all pages
    private Hashtable /* PropertyPage -> (Hashtable of Combo -> Integer)*/ comboOriginalValues = new Hashtable();
    // manage string field editors on all pages
    private Hashtable /* PropertyPage -> (Hashtable of StringFieldEditors -> String)*/ stringFieldEditorsOriginalValues = new Hashtable();
    // manage selection buttons on all pages
    private Hashtable /* PropertyPage -> (Hashtable of SelectionButtonDialogField -> Boolean)*/ selectionButtonOriginalValues = new Hashtable();
    // manage tree list dialog fields on all pages
    private Hashtable /* PropertyPage -> (Hashtable of ListDialogField -> List)*/ dialogFieldOriginalValues = new Hashtable();

    interface AJDTPathBlockPage{}
    
    declare parents: AspectPathBlock implements AJDTPathBlockPage;
    declare parents: InPathBlock implements AJDTPathBlockPage;
    
    pointcut interestingPage(): 
	    within(AspectJProjectPropertiesPage) ||
	    within(CompilerPropertyPage) ||
	    within(InPathPropertyPage) ||
	    within(AspectPathPropertyPage);

    // interested in when the AJDT property page is created
    pointcut creationOfAnInterestingPage(PropertyPage page) : 
	    (execution(protected Control createContents(Composite)))
	    && interestingPage() && this(page);

    // before the page is created, add the page to the lists
    before(PropertyPage page) : creationOfAnInterestingPage(page) {
        activePages.add(page);
        remainingActivePages.add(page);
    }

    pointcut interestingPathBlockPages() : within(AspectPathBlock) || within(InPathBlock);
    
    pointcut setSelection(Button button, boolean val, PropertyPage page):
        call(* setSelection(boolean)) && args(val) && target(button) && this(page);

    // remember the first value put into the buttons
    before(Button b, boolean val, PropertyPage page):
        	setSelection(b,val,page) && interestingPage(){
        if (!buttonOriginalValues.containsKey(page)) {
            Hashtable buttonValues = new Hashtable();
            buttonValues.put(b,new Boolean(val));
            buttonOriginalValues.put(page,buttonValues);
        } else {
            Hashtable buttonValues = (Hashtable)buttonOriginalValues.get(page);
            if (!buttonValues.containsKey(b)) {
                buttonValues.put(b, new Boolean(val)); 
            }
        }
    }
    
    pointcut comboSelect(Combo combo, int selection, PropertyPage page) :
        call(* select(int)) && args(selection) && target(combo) && this(page);
    
    // remember the first value put into the combo boxes
    before(Combo combo, int selection, PropertyPage page) :
        comboSelect(combo,selection,page) && interestingPage() {
        if (!comboOriginalValues.containsKey(page)) {
            Hashtable comboValues = new Hashtable();
            comboValues.put(combo,new Integer(selection));
            comboOriginalValues.put(page,comboValues);
        } else {
            Hashtable comboValues = (Hashtable)comboOriginalValues.get(page);
            if (!comboValues.containsKey(combo)) {
                comboValues.put(combo, new Integer(selection)); 
            }
        }       
    }
            
    pointcut setStringValue(StringFieldEditor editor, String value, PropertyPage page) :
        call(* setStringValue(String)) && args(value) && target(editor) && this(page);
    
    // remember the first value put into the StringFieldEditors
    before(StringFieldEditor editor, String value, PropertyPage page) :
        setStringValue(editor,value,page) && interestingPage() {
        if (!stringFieldEditorsOriginalValues.containsKey(page)) {
            Hashtable editorValues = new Hashtable();
            editorValues.put(editor,value);
            stringFieldEditorsOriginalValues.put(page,editorValues);
        } else {
            Hashtable editorValues = (Hashtable)stringFieldEditorsOriginalValues.get(page);
            if (!editorValues.containsKey(editor)) {
                editorValues.put(editor, value); 
            }
        }       
    }
    
    pointcut setButtonSelection(SelectionButtonDialogField button, boolean value, PropertyPage page) :
        call(* setSelection(boolean)) && args(value) && target(button) && this(page);
    
    // remember the first value put into the SelectionButtonDialogFields
    before(SelectionButtonDialogField button, boolean value, PropertyPage page) :
        setButtonSelection(button,value,page) && interestingPage() {
        if (!selectionButtonOriginalValues.containsKey(page)) {
            Hashtable buttonValues = new Hashtable();
            buttonValues.put(button,new Boolean(value));
            selectionButtonOriginalValues.put(page,buttonValues);
        } else {
            Hashtable buttonValues = (Hashtable)selectionButtonOriginalValues.get(page);
            if (!buttonValues.containsKey(button)) {
                buttonValues.put(button, new Boolean(value)); 
            }
        }    
    }

    pointcut setElements(ListDialogField dialogField, Collection elements, AJDTPathBlockPage basePage) :
        call(* setElements(Collection)) && args(elements) && target(dialogField) && this(basePage);
    
    // remember the first value put into the TreeListDialogFields
    before(ListDialogField dialogField, Collection elements, AJDTPathBlockPage basePage) :
        setElements(dialogField,elements,basePage) && interestingPathBlockPages() {
        
        PropertyPage page = null;
        // We need to associate the ListDialogField with the related PropertyPage object
        // rather than the related AJDTPathBlockPage (so we can check if the contents
        // of the ListDialogField more easily in settingsHaveChanged(PropertyPage)).
        // We therefore iterate through the active pages and if basePage is an
        // AspectPathBlock then we're looking for the AspectPathPropertyPage object, whereas
        // if basePage is an InPathBlock then we're looking for the InPathPropertyPage object.       
        for (Iterator iter = activePages.iterator(); iter.hasNext();) {
            PropertyPage ajdtPage = (PropertyPage) iter.next();
            if ((basePage instanceof AspectPathBlock)
                    && (ajdtPage instanceof AspectPathPropertyPage )) {
                page = ajdtPage;
            } else if ((basePage instanceof InPathBlock)
                        && (ajdtPage instanceof InPathPropertyPage )) {
                    page = ajdtPage;
            }
        }
        
        if (!dialogFieldOriginalValues.containsKey(page)) {
            Hashtable fieldValues = new Hashtable();
            List listOfElements = new ArrayList();
            listOfElements.addAll(elements);
            fieldValues.put(dialogField,listOfElements);
            dialogFieldOriginalValues.put(page,fieldValues);
        } else {
            Hashtable fieldValues = (Hashtable)dialogFieldOriginalValues.get(page);
            if (!fieldValues.containsKey(dialogField)) {
                List listOfElements = new ArrayList();
                listOfElements.addAll(elements);
                fieldValues.put(dialogField,listOfElements);
            }
        }        
    }

    private boolean settingsHaveChanged() {
        Iterator iterator = activePages.iterator();
        while (iterator.hasNext()) {
            PropertyPage page = (PropertyPage) iterator.next();
            if (settingsHaveChangedOnPage(page)) {
                return true;
            }            
        }
        return false;
    }

    private boolean settingsHaveChangedOnPage(PropertyPage page) {
        Hashtable buttonsOnPage = (Hashtable)buttonOriginalValues.get(page);
        if (buttonsOnPage != null) {
            Enumeration buttons = buttonsOnPage.keys();
            while (buttons.hasMoreElements()) {
                Button b = (Button) buttons.nextElement();
                if (b.getSelection() != ((Boolean) buttonsOnPage.get(b)).booleanValue())
                    return true;
            }            
        }
        
        Hashtable comboBoxesOnPage = (Hashtable)comboOriginalValues.get(page);
        if (comboBoxesOnPage != null) {
            Enumeration comboboxes = comboBoxesOnPage.keys();
            while (comboboxes.hasMoreElements()) {
                Combo c = (Combo) comboboxes.nextElement();
                if (c.getSelectionIndex() != ((Integer) comboBoxesOnPage.get(c)).intValue())
                    return true;            
            }            
        }
        
        Hashtable editorsOnPage = (Hashtable) stringFieldEditorsOriginalValues.get(page);
        if (editorsOnPage != null) {
            Enumeration editors = editorsOnPage.keys();
            while (editors.hasMoreElements()) {
                StringFieldEditor e = (StringFieldEditor) editors.nextElement();
                if (!(e.getStringValue().equals(((String) editorsOnPage.get(e)))))
                    return true;            
            }
        }

        Hashtable selectionButtonsOnPage = (Hashtable)selectionButtonOriginalValues.get(page);
        if (selectionButtonsOnPage != null) {
            Enumeration buttons = selectionButtonsOnPage.keys();
            while (buttons.hasMoreElements()) {
                SelectionButtonDialogField b = (SelectionButtonDialogField) buttons.nextElement();
                if (b.isSelected() != ((Boolean) selectionButtonsOnPage.get(b)).booleanValue())
                    return true;
            }            
        }

        Hashtable dialogFieldsOnPage = (Hashtable)dialogFieldOriginalValues.get(page);
        if (dialogFieldsOnPage != null) {
            Enumeration fields = dialogFieldsOnPage.keys();
            while (fields.hasMoreElements()) {
                ListDialogField f = (ListDialogField)fields.nextElement();
                List currentLibs = f.getElements();
                List originalLibs = (List)dialogFieldsOnPage.get(f);
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
        return false;
    }
    
    private void resetButtonsOnPage(PropertyPage page) {
        Hashtable buttonsOnPage = (Hashtable)buttonOriginalValues.get(page);
        if (buttonsOnPage != null) {
            Enumeration buttons = buttonsOnPage.keys();
            while (buttons.hasMoreElements()) {
                Button b = (Button) buttons.nextElement();
                buttonsOnPage.put(b, new Boolean(b.getSelection()));
             }            
        }
    }

    private void resetComboBoxesOnPage(PropertyPage page) {
        Hashtable comboBoxesOnPage = (Hashtable)comboOriginalValues.get(page);
        if (comboBoxesOnPage != null) {
            Enumeration boxes = comboBoxesOnPage.keys();
            while (boxes.hasMoreElements()) {
                Combo c = (Combo) boxes.nextElement();
                comboBoxesOnPage.put(c, new Integer(c.getSelectionIndex()));
             }            
        }
    }
    
    private void resetEditorsOnPage(PropertyPage page) {
        Hashtable editorsOnPage = (Hashtable)stringFieldEditorsOriginalValues.get(page);
        if (editorsOnPage != null) {
            Enumeration editors = editorsOnPage.keys();
            while (editors.hasMoreElements()) {
                StringFieldEditor e = (StringFieldEditor) editors.nextElement();
                editorsOnPage.put(e, e.getStringValue());
             }                    
        }
    }

    private void resetSelectionButtonsOnPage(PropertyPage page) {
        Hashtable buttonsOnPage = (Hashtable)selectionButtonOriginalValues.get(page);
        if (buttonsOnPage != null) {
            Enumeration buttons = buttonsOnPage.keys();
            while (buttons.hasMoreElements()) {
                SelectionButtonDialogField b = (SelectionButtonDialogField) buttons.nextElement();
                buttonsOnPage.put(b, new Boolean(b.isSelected()));
             }            
        }
    }

    private void resetDialogFieldsOnPage(PropertyPage page) {
        Hashtable fieldsOnPage = (Hashtable)dialogFieldOriginalValues.get(page);
        if (fieldsOnPage != null) {
            Enumeration fields = fieldsOnPage.keys();
            while (fields.hasMoreElements()) {
                ListDialogField f = (ListDialogField)fields.nextElement();
                fieldsOnPage.put(f, f.getElements());
             }            
        }
    }
    
    pointcut pageCompleting(PropertyPage page) : 
        execution(boolean performOk()) && interestingPage() && this(page);

    // performOk not running because of performApply
    after(PropertyPage page) returning: pageCompleting(page) && !cflow(execution(* performApply())) {
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
    after(PropertyPage page) returning: pageCompleting(page) && cflow(execution(* performApply())) {
        if (wantToBuild() && settingsHaveChangedOnPage(page)) {
            doProjectBuild(page);
        }        
        resetButtonsOnPage(page);
        resetComboBoxesOnPage(page);
        resetEditorsOnPage(page);
        resetSelectionButtonsOnPage(page);
        resetDialogFieldsOnPage(page);
        if (page instanceof CompilerPropertyPage) {
            compilerPageDoBuild = false;
        }
    }
  
    pointcut openingMessageDialog(MessageDialog dialog, PropertyPage page) :
        call(* open()) && target(dialog) && this(page);
    
    after(MessageDialog dialog, PropertyPage page ) returning : 
        openingMessageDialog(dialog,page) && within(CompilerPropertyPage) {        
        if (dialog.getReturnCode() == 0) {
            compilerPageDoBuild = true;
        } 
    }
    
    /**
     * Returning whether the user chose to build on the compiler page
     * or whether autobuilding is set
     */
    private boolean wantToBuild() {
        for (Iterator iter = activePages.iterator(); iter.hasNext();) {
            PropertyPage page = (PropertyPage) iter.next();
            if ((page instanceof CompilerPropertyPage) && settingsHaveChangedOnPage(page)) {
                return compilerPageDoBuild;
            }
        }
        return AspectJUIPlugin.getWorkspace().getDescription().isAutoBuilding();
    }
    
    /**
     * Build the project
     */
    private void doProjectBuild(PropertyPage prefPage) {
        IProject tempProject = null;
        if (prefPage instanceof AspectPathPropertyPage) {
            tempProject = ((AspectPathPropertyPage) prefPage).getThisProject();
        } else if (prefPage instanceof InPathPropertyPage) {
            tempProject = ((InPathPropertyPage) prefPage).getThisProject();
        } else if (prefPage instanceof CompilerPropertyPage) {
            tempProject = ((CompilerPropertyPage) prefPage).getThisProject();
        } else if (prefPage instanceof AspectJProjectPropertiesPage) {
            tempProject = ((AspectJProjectPropertiesPage) prefPage)
                    .getThisProject();
        }
        final IProject project = tempProject;
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                ((PreferencePage) prefPage).getShell());
        try {
            dialog.run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException {
                    monitor.beginTask("", 2); //$NON-NLS-1$
                    try {
                        monitor
                                .setTaskName(AspectJUIPlugin
                                        .getResourceString("OptionsConfigurationBlock.buildproject.taskname")); //$NON-NLS-1$
                        project.build(IncrementalProjectBuilder.FULL_BUILD,
                                "org.eclipse.ajdt.ui.ajbuilder", null,
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
            AspectJUIPlugin.getDefault().getErrorHandler().handleError(message,
                    e);
        }
    }

}