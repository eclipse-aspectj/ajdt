/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *     Andrew Eisenberg - Rewritten for AJDT 2.1.0
 *******************************************************************************/
package org.eclipse.ajdt.internal.core.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.IAspectJElement;
import org.eclipse.ajdt.core.javaelements.PointcutUtilities;
import org.eclipse.ajdt.core.text.CoreMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextEditChangeGroup;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

/**
 * When types are renamed this participant updates any references
 * AspectJ-specific references in Aspects in the same project. What this means
 * is that references inside of ITD names, inside of piointcut designators, etc
 * are renamed, but not the references that would otherwise be found by Java.
 */
public class AspectRenameParticipant extends RenameParticipant {

    private IType fType;
    private String qualifiedName;

    public RefactoringStatus checkConditions(IProgressMonitor pm,
            CheckConditionsContext context) throws OperationCanceledException {
        return new RefactoringStatus();
    }

    public Change createChange(IProgressMonitor pm) throws CoreException,
            OperationCanceledException {
        CompositeChange finalChange = new CompositeChange(
                CoreMessages.renameTypeReferences);
        final String oldName = fType.getElementName();
        final String newName = getArguments().getNewName();
        IProject project = fType.getResource().getProject();
        AJLog.log("Rename type references in aspects from " + oldName + " to " + newName); //$NON-NLS-1$ //$NON-NLS-2$
        AJLog.log("qualified name: " + fType.getFullyQualifiedName()); //$NON-NLS-1$
        
        // use the AJCompilationUnitManager because we need all Aspects in the project
        // this is less than ideal because the CUs from the manager have children
        // whose parent pointers don't point to the CU Manager.
        List<AJCompilationUnit> ajs = AJCompilationUnitManager.INSTANCE
                .getCachedCUs(project);
        pm.beginTask(CoreMessages.renameTypeReferences, ajs.size());
        for (AJCompilationUnit ajcu : ajs) {
            if (!targetTypeIsAccessible(ajcu)) {
                continue;
            }
            
            AJLog.log("Looking for type references for " + oldName + " in " + ajcu.getElementName()); //$NON-NLS-1$ //$NON-NLS-2$
            TextEdit[] edits = renameAspectSpecificReferences(ajcu, newName);

            if (edits.length > 0) {
                CompilationUnitChange change = findOrCreateChange(ajcu, finalChange);
                // ensure that these changes haven't been added by someone else
                TextEditChangeGroup[] groups = change.getTextEditChangeGroups();
                middle:
                for (TextEdit typeRenameEdit : edits) {
                    
                    // before adding an edit, must check to see if it already exists
                    for (TextEditChangeGroup group : groups) {
                        for (TextEdit existingEdit : group.getTextEdits()) {
                            if (existingEdit.covers(typeRenameEdit)) {
                                // don't step on someone else's feet
                                continue middle;
                            }
                        }
                    }
                    change.addChangeGroup(new TextEditChangeGroup(change, 
                            new TextEditGroup("Update type reference in aspect", typeRenameEdit)));
                    change.addEdit(typeRenameEdit);
                }
            }
            pm.worked(1);
        }

        pm.done();
        if (finalChange.getChildren().length == 0) {
            return null;
        }
        return finalChange;
    }

    /**
     * Finds or creates a {@link CompilationUnitChange} for the given AJCU.  
     * This change may already exist if this AJCU has been changed by the JDT
     * rename refactoring.
     */
    private CompilationUnitChange findOrCreateChange(AJCompilationUnit ajcu, CompositeChange finalChange) {
        CompilationUnitChange existingChange = null;

        TextChange textChange = getTextChange(ajcu);
        if (textChange instanceof CompilationUnitChange) {
            // change exists from some other part of the refactoring
            existingChange = (CompilationUnitChange) textChange;
        } else {
            
            // check to see if we have already touched this file
            Change[] children = finalChange.getChildren();
            for (Change change : children) {
                if (change instanceof CompilationUnitChange) {
                    if (((CompilationUnitChange) change).getCompilationUnit().equals(ajcu)) {
                        existingChange = (CompilationUnitChange) change;
                        break;
                    }
                }
            }
        }

        if (existingChange == null) {
            // nope...must create a new change
            existingChange = new CompilationUnitChange("ITD accessor renamings for " + ajcu.getElementName(), ajcu);
            existingChange.setEdit(new MultiTextEdit());
            finalChange.add(existingChange);
        }
        return existingChange;
    }

    static class AspectChange {
        IAspectJElement element;
        List<Integer> offsets;
    }

    private TextEdit[] renameAspectSpecificReferences(AJCompilationUnit ajcu, final String newName) throws JavaModelException {
        List<TextEdit> editList = new ArrayList<TextEdit>();
        String name = fType.getElementName();
        IType[] types = ajcu.getTypes();

        for (int i = 0; i < types.length; i++) {
            if (types[i] instanceof AspectElement) {
                ReplaceEdit[] aspectChanges = searchForReferenceInPointcut(
                        (AspectElement) types[i], name, newName);
                if (aspectChanges.length == 0) {
                    continue;
                }
                editList.addAll(Arrays.asList(aspectChanges));
            }
        }
        return (TextEdit[]) editList.toArray(new TextEdit[editList.size()]);
    }

    private ReplaceEdit[] searchForReferenceInPointcut(AspectElement aspect, String name, String newName)
            throws JavaModelException {
        // get the containing CU explicitly because the one from the AJCUManager 
        // does not have proper children.
        AJCompilationUnit ajcu = (AJCompilationUnit) aspect.getCompilationUnit();
        List<ReplaceEdit> replaceEdits = new ArrayList<ReplaceEdit>();
        IAspectJElement[] elementsToSearch = aspect.getAllAspectMemberElements();
        for (IAspectJElement element : elementsToSearch) {
            if (element instanceof ISourceReference) {
                ajcu.requestOriginalContentMode();
                String src = ((ISourceReference) element).getSource();
                int elementStart = element.getSourceRange().getOffset();
                ajcu.discardOriginalContentMode();
                if (src == null) {
                    // this ajcu is likely closed or disposed
                    continue;
                } 
                Map<String, List<Integer>> map = PointcutUtilities.findAllIdentifiers(src);
                if (map != null) {
                    for (Map.Entry<String, List<Integer>> entry : map.entrySet()) {
                        if (entry.getKey().equals(name)) {
                            for (Integer offset : entry.getValue()) {
                                AJLog.log("  found reference at offset " + offset); //$NON-NLS-1$
                                replaceEdits.add(new ReplaceEdit(elementStart + offset, name.length(), newName));
                            }
                        }
                    }
                }
            }
        }
        return (ReplaceEdit[]) replaceEdits.toArray(new ReplaceEdit[0]);
    }

    
    /**
     * Determine if the renamed type is accessible in this ajcu
     */
    private boolean targetTypeIsAccessible(AJCompilationUnit ajcu) {
        if (inSamePackage(ajcu)) {
            return true;
        }
        IImportDeclaration imp = ((ICompilationUnit) ajcu).getImport(qualifiedName);
        return imp.exists();
    }

    private boolean inSamePackage(AJCompilationUnit ajcu) {
        String ajPackage = CharOperation.toString(ajcu.getPackageName());
        return ajPackage.equals(removeTypeName(qualifiedName));
    }

    private String removeTypeName(String qualifiedName) {
        int ind = qualifiedName.lastIndexOf('.');
        if (ind == -1) {
            return "";
        }
        return qualifiedName.substring(0, ind);
    }

    public String getName() {
        return "Rename type references in Aspects"; //$NON-NLS-1$
    }

    protected boolean initialize(Object element) {
        if (element instanceof IType) {
            fType = (IType) element;
            if (AspectJPlugin.isAJProject(fType.getJavaProject().getProject())) {
                qualifiedName = fType.getFullyQualifiedName();
                return true;
            }
        }
        return false;
    }
}
