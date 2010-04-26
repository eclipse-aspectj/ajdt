/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.core.refactoring;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import org.aspectj.asm.IProgramElement.Kind;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextEditChangeGroup;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

/**
 * @author Andrew Eisenberg
 * @created Apr 22, 2010
 * fields that have ITDs that are getters or setters are
 * renamed with this participant
 */
public class ITDAccessorRenameParticipant extends RenameParticipant {

    private IField field;
    
    private boolean useIsForBooleanGetter = false;
    
    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm,
            CheckConditionsContext context) throws OperationCanceledException {
        return new RefactoringStatus();
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException,
            OperationCanceledException {
        checkCancelled(pm);
        
        boolean shouldRenameGetter = shouldRename(true);
        boolean shouldRenameSetter = shouldRename(false);

        if (! shouldRenameGetter && ! shouldRenameSetter) {
            return null;
        }
        
        IType declaring = field.getDeclaringType();
        AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(declaring);
        if (model.hasModel()) {
            List<IJavaElement> itds = model.getRelationshipsForElement(declaring, AJRelationshipManager.ASPECT_DECLARATIONS);
            IntertypeElement getter = null, setter = null;
            for (IJavaElement elt : itds) {
                if (shouldRenameGetter && isGetter(elt)) {
                    getter = (IntertypeElement) elt;
                } else if (shouldRenameSetter && isSetter(elt)) {
                    setter = (IntertypeElement) elt;
                }
            }
            
            checkCancelled(pm);
            
            // do a search for the getter
            List<SearchMatch> getterReferences;
            if (getter != null) {
                getterReferences = findReferences(getter);
            } else {
                getterReferences = Collections.emptyList();
            }
            checkCancelled(pm);

            // do a search for the setter
            List<SearchMatch> setterReferences;
            if (setter != null) {
                setterReferences = findReferences(setter);
            } else {
                setterReferences = Collections.emptyList();
            }

            checkCancelled(pm);
            
            CompositeChange change = new CompositeChange(getName());
            createDeclarationChange(getter, change, getOldGetterName(useIsForBooleanGetter), getNewGetterName(useIsForBooleanGetter));
            createDeclarationChange(setter, change, getOldSetterName(), getNewSetterName());
            createMatchedChanges(getterReferences, change, getOldGetterName(useIsForBooleanGetter), getNewGetterName(useIsForBooleanGetter));
            createMatchedChanges(setterReferences, change, getOldSetterName(), getNewSetterName());
            if (change.getChildren().length > 0) {
                return change;
            }
            
        }
        return null;
    }

    private Field fRenameGetterField;
    private Field fRenameSetterField;
    private boolean shouldRename(boolean getter) {
        try {
            RefactoringProcessor processor = getProcessor();
            Field thisField;
            if (getter) {
                if (fRenameGetterField == null) {
                    fRenameGetterField = processor.getClass().getDeclaredField("fRenameGetter");
                    fRenameGetterField.setAccessible(true);
                }
                thisField = fRenameGetterField;
            } else {
                if (fRenameSetterField == null) {
                    fRenameSetterField = processor.getClass().getDeclaredField("fRenameSetter");
                    fRenameSetterField.setAccessible(true);
                } 
                thisField = fRenameSetterField;
            }
            return thisField.getBoolean(processor);
        } catch (Exception e) {
        }
        return false;
    }

    private void createMatchedChanges(List<SearchMatch> references,
            CompositeChange finalChange, String oldName, String newName) {
        for (SearchMatch searchMatch : references) {
            Object elt = searchMatch.getElement();
            if (elt instanceof IMember) {
                addChange(finalChange, (IMember) elt, searchMatch.getOffset(), oldName.length(), newName);
            }
        }
    }

    private void createDeclarationChange(IntertypeElement accessor,
            CompositeChange finalChange, String oldName, String newName) throws JavaModelException {
        if (accessor == null) {
            return;
        }
        ISourceRange region = accessor.getNameRange();
        addChange(finalChange, accessor, region.getOffset()+region.getLength()-oldName.length(), oldName.length(), newName);
    }

    private void addChange(CompositeChange finalChange, IMember enclosingElement,
            int offset, int length, String newName) {
        CompilationUnitChange existingChange = findOrCreateChange(
                enclosingElement, finalChange);
        TextEditChangeGroup[] groups = existingChange.getTextEditChangeGroups();
        TextEdit occurrenceEdit = new ReplaceEdit(offset, length, newName);
        
        boolean isOverlapping = false;
        for (TextEditChangeGroup group : groups) {
            if (group.getTextEdits()[0].covers(occurrenceEdit)) {
                isOverlapping = true;
                break;
            }
        }
        if (isOverlapping) {
            // don't step on someone else's feet
            return;
        }
        existingChange.addEdit(occurrenceEdit);
        existingChange.addChangeGroup(new TextEditChangeGroup(existingChange, 
                new TextEditGroup("Update ITD accessor occurrence", occurrenceEdit)));

    }

    private CompilationUnitChange findOrCreateChange(IMember accessor,
            CompositeChange finalChange) {
        TextChange textChange = getTextChange(accessor.getCompilationUnit());
        CompilationUnitChange existingChange = null;
        if (textChange instanceof CompilationUnitChange) {
            // check to see if change exists from some other part of the refactoring
            existingChange = (CompilationUnitChange) textChange;
        } else {
            
            // check to see if we have already touched this file
            Change[] children = finalChange.getChildren();
            for (Change change : children) {
                if (change instanceof CompilationUnitChange) {
                    if (((CompilationUnitChange) change).getCompilationUnit().equals(accessor.getCompilationUnit())) {
                        existingChange = (CompilationUnitChange) change;
                        break;
                    }
                }
            }
        }
        
        if (existingChange == null) {
            // nope...must create a new change
            existingChange = new CompilationUnitChange("ITD accessor renamings for " + accessor.getCompilationUnit().getElementName(), accessor.getCompilationUnit());
            existingChange.setEdit(new MultiTextEdit());
            finalChange.add(existingChange);
//            existingChange.setDescriptor(get)
        }
        return existingChange;
    }

    private void checkCancelled(IProgressMonitor pm) {
        if (pm.isCanceled()) {
            throw new OperationCanceledException();
        }
    }

    private List<SearchMatch> findReferences(IntertypeElement accessor) {
        SearchPattern pattern = SearchPattern.createPattern(accessor, IJavaSearchConstants.REFERENCES);
        SearchEngine engine = new SearchEngine();
        JavaSearchScope scope = new JavaSearchScope();
        try {
            scope.add(accessor.getJavaProject());
            CollectingSearchRequestor requestor = new CollectingSearchRequestor();
            engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, 
                    scope, requestor, new NullProgressMonitor());
            return requestor.getResults();
        } catch (JavaModelException e) {
        } catch (CoreException e) {
        }

        return Collections.emptyList();
    }

    private boolean isSetter(IJavaElement elt) throws JavaModelException {
        if (elt instanceof IntertypeElement && 
                elt.getElementName().endsWith("." + getOldSetterName())) {
            IntertypeElement itd = (IntertypeElement) elt;
            String[] parameterTypes = itd.getParameterTypes();
            if (itd.getAJKind() == Kind.INTER_TYPE_METHOD &&
                    parameterTypes != null && 
                    parameterTypes.length == 1 &&
                    parameterTypes[0].equals(field.getTypeSignature()) &&
                    itd.getReturnType().equals(Signature.SIG_VOID)) {
                 return true;
            }
        }
        return false;
    }

    private boolean isGetter(IJavaElement elt) throws JavaModelException {
        boolean nameMatch = false;
        boolean isFound = false;
        if (elt instanceof IntertypeElement && 
                elt.getElementName().endsWith("." + getOldGetterName(false))) {
            nameMatch = true;
        } else if (elt instanceof IntertypeElement && 
                elt.getElementName().endsWith("." + getOldGetterName(true))) {
            nameMatch = true;
        }
        if (nameMatch) {
            useIsForBooleanGetter = isFound;
            IntertypeElement itd = (IntertypeElement) elt;
            String[] parameterTypes = itd.getParameterTypes();
            if (itd.getAJKind() == Kind.INTER_TYPE_METHOD &&
                    (parameterTypes == null || 
                     parameterTypes.length == 0) &&
                    itd.getReturnType().equals(field.getTypeSignature())) {
                 return true;
            }
        }
        return false;
    }
    
    private String getNewSetterName() {
        return accessorName("set", getArguments().getNewName());
    }
    private String getNewGetterName(boolean useIsForBoolean) {
        return accessorName("get", getArguments().getNewName());
    }
    private String getOldSetterName() {
        return accessorName("set", field.getElementName());
    }
    private String getOldGetterName(boolean useIsForBoolean) {
        return accessorName(useIsForBoolean ? "is" : "get", field.getElementName());
    }

    private String accessorName(String prefix, String name) {
        return prefix + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    @Override
    public String getName() {
        return "Rename occurrences of intertype getters and setters";
    }

    @Override
    protected boolean initialize(Object element) {
        if (element instanceof IField) {
            field = (IField) element;
            if (AspectJPlugin.isAJProject(field.getJavaProject().getProject())) {
                return true;
            }
        }
        return false;
    }

}
