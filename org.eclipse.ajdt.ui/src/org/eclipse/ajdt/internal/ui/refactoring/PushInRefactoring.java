/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.internal.ui.refactoring;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IProgramElement.Kind;
import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.DeclareElement;
import org.eclipse.ajdt.core.javaelements.DeclareElementInfo;
import org.eclipse.ajdt.core.javaelements.IAspectJElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.ImportReferencesCollector;
import org.eclipse.jface.text.Region;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourceChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Andrew Eisenberg
 * @created Apr 30, 2009
 *
 */
public class PushInRefactoring extends Refactoring {
    

    
    public static final String ALL_ITDS = "all.itds";
    public static final String DELETE_EMPTY = "delete.empty";
    
    /**
     * Since each class is visited multiple times, and each visit may introduce
     * new imports, we must delay import rewriting
     * until each class has its ITDs already pushed in.
     *
     */
    private class NewImportsHolder {
        Set<Name> staticImports;
        Set<Name> typeImports;
        Set<String> extraImports;
        ICompilationUnit unit;
        
        NewImportsHolder(ICompilationUnit unit) {
            staticImports = new HashSet<Name>();
            typeImports = new HashSet<Name>();
            extraImports = new HashSet<String>();
            this.unit = unit;
        }
        
        // does not handle imports for declare parents
        void rewriteImports() throws CoreException {
            // first check to see if this unit has been deleted.
            Change change = (Change) allChanges.get(unit);
            if (! (change instanceof TextFileChange)) {
                return;
            }
            
            ImportRewrite rewrite = ImportRewrite.create(unit, true);
            for (Name name : typeImports) {
                ITypeBinding binding = name.resolveTypeBinding();
                if (binding != null) {
                    rewrite.addImport(binding);
                }
            }
            for (Name name : staticImports) {
                rewrite.addImport(name.resolveTypeBinding());
            }

            for (String qualName : extraImports) {
                rewrite.addImport(qualName);
            }
            TextEdit importEdit = rewrite.rewriteImports(new NullProgressMonitor());
            
            TextFileChange textChange = (TextFileChange) change;
            textChange.getEdit().addChild(importEdit);
        }

        void computeImports(List<IMember> itds, ICompilationUnit ajUnit, IProgressMonitor monitor)
                throws JavaModelException {
            IJavaProject project = ajUnit.getJavaProject();
            ASTParser parser = ASTParser.newParser(AST.JLS3);
            parser.setProject(project);
            parser.setResolveBindings(true);
            parser.setSource(ajUnit);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            CompilationUnit ajAST = (CompilationUnit) parser.createAST(monitor);

            for (IMember itd : itds) {
                ISourceRange range = itd.getSourceRange();
                ImportReferencesCollector.collect(ajAST, project, new Region(
                        range.getOffset(), range.getLength()),
                        typeImports, staticImports);
                if (itd instanceof DeclareElement) {
                    DeclareElement declare = (DeclareElement) itd;
                    // no types added for declare removers
                    if (!((DeclareElementInfo) declare.getElementInfo()).isAnnotationRemover()) {
                        String qualType = getQualifiedTypeForDeclareAnnotation(declare);
                        if (qualType != null && qualType.length() > 0) {
                            extraImports.add(qualType);
                        }
                        List<Type> types = getExtraImportsFromDeclareElement(declare, ajAST);
                        for (Type type : types) {
                            ImportReferencesCollector.collect(type, project, new Region(
                                    type.getStartPosition(), type.getLength()),
                                    typeImports, staticImports);
                        }
                    }
                }
            }
        }
        
        // This method finds the extra imports required by a declare element
        // (eg- used inside of a declare @annotation's annotation)
        // We take advantage of the format of the converted source from aspectj to java
        // the last fields of the last type when following a particular naming convention, 
        // are around to force import statements to exist.  We can read them and use them 
        // to seed the extra imports list
        private List<Type> getExtraImportsFromDeclareElement(DeclareElement itd,
                CompilationUnit ajAST) {
            int numTypes = ajAST.types().size();
            if (numTypes == 0) {
                return Collections.emptyList();
            }
            
            String details = null;
            AbstractTypeDeclaration lastType = (AbstractTypeDeclaration) ajAST.types().get(numTypes-1);
            @SuppressWarnings("unchecked")
            List<BodyDeclaration> bodyDecls = lastType.bodyDeclarations();
            List<Type> extraSimpleNames = new LinkedList<Type>();
            for (int i = bodyDecls.size()-1; i >= 0; i--) {
                BodyDeclaration decl = (BodyDeclaration) bodyDecls.get(i);
                if (decl.getNodeType() == ASTNode.FIELD_DECLARATION) {
                    FieldDeclaration fDecl = (FieldDeclaration) decl;
                    if (fDecl.fragments().size() == 1) {
                        VariableDeclarationFragment frag = (VariableDeclarationFragment) fDecl.fragments().get(0);
                        if (frag.getName().toString().startsWith(AspectsConvertingParser.ITD_INSERTED_IDENTIFIER)) {
                            if (details == null) {
                                IProgramElement ipe = AJProjectModelFactory.getInstance()
                                    .getModelForJavaElement(itd).javaElementToProgramElement(itd);
                                details = ipe.getDetails();
                            }
                            Type type = fDecl.getType();
                            // only add if this type exists in the declare @annotation 
                            if (details.indexOf(type.toString()) != -1) {
                                 extraSimpleNames.add(type);
                            }
                            
                            continue;
                        }
                    }
                }
                // break on the first body declaration that does not conform
                break;
            }
            return extraSimpleNames;
        }
    }
    
    private boolean deleteEmpty = true;
    
    private Map<ICompilationUnit, Change> allChanges = null; 

    private List<IMember> itds = null;
    

    public RefactoringStatus checkFinalConditions(IProgressMonitor monitor)
            throws CoreException, OperationCanceledException {
        final RefactoringStatus status = new RefactoringStatus();
        try {
            monitor.beginTask("Checking preconditions...", 2);
            allChanges = new LinkedHashMap<ICompilationUnit, Change>();
            Map<ICompilationUnit, List<IMember>> unitToITDs = new HashMap<ICompilationUnit, List<IMember>>();
            for (IMember itd : itds) {
                ICompilationUnit unit = itd.getCompilationUnit();
                List<IMember> itds;
                if (unitToITDs.containsKey(unit)) {
                    itds = unitToITDs.get(unit);
                } else {
                    itds = new LinkedList<IMember>();
                    unitToITDs.put(unit, itds);
                }
                itds.add(itd);
            }
            
            Map<ICompilationUnit, NewImportsHolder> importsMap = new HashMap<ICompilationUnit, NewImportsHolder>();
            for (Map.Entry<ICompilationUnit,List<IMember>> entry :  unitToITDs.entrySet()) {
                status.merge(checkFinalConditionsForITD(
                        entry.getKey(), entry.getValue(), importsMap,
                        monitor));
            }
            
            // now go through and create the import edits
            for (NewImportsHolder holder : importsMap.values()) {
                holder.rewriteImports();
            }
            
        } finally {
            monitor.done();
        }

        return status;
    }

    /**
     * Checks the conditions for a single {@link AJCompilationUnit}
     * @param ajUnit the unit to check
     * @param itdsForUnit all itds in this unit
     * @param imports a map from target {@link ICompilationUnit} to imports that need to be added
     * initially empty, but populated in this method
     * @param monitor
     * @return
     * @throws JavaModelException
     */
    private RefactoringStatus checkFinalConditionsForITD(final ICompilationUnit ajUnit, 
            final List<IMember> itdsForUnit, 
            final Map<ICompilationUnit, NewImportsHolder> imports, 
            final IProgressMonitor monitor) throws JavaModelException {
        
        final RefactoringStatus status = new RefactoringStatus();
        
        // group all of the ITD targets by the ICompilationUnit that they are in
        final Map<ICompilationUnit, Set<IMember>> unitsToTypes = getUnitTypeMap(getTargets(itdsForUnit));
        
        // group all of the ICompilationUnits by project
        final Map<IJavaProject, Collection<ICompilationUnit>> projects= new HashMap<IJavaProject, Collection<ICompilationUnit>>();
        for (ICompilationUnit targetUnit : unitsToTypes.keySet()) {
            IJavaProject project= targetUnit.getJavaProject();
            if (project != null) {
                Collection<ICompilationUnit> collection = projects.get(project);
                if (collection == null) {
                    collection= new ArrayList<ICompilationUnit>();
                    projects.put(project, collection);
                }
                collection.add(targetUnit);
            }
        }
        
        // also add the ajunit to the collection of affected units
        Collection<ICompilationUnit> units;
        IJavaProject aspectProject = ajUnit.getJavaProject();
        if (projects.containsKey(aspectProject)) {
            units = projects.get(aspectProject);
        } else {
            units = new ArrayList<ICompilationUnit>();
            projects.put(aspectProject, units);
        }
        units.add(ajUnit);
        
        // this requestor performs the real work
        ASTRequestor requestors = new ASTRequestor() {
            public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
                try {
                    
                    // compute the imports that this itd adds to this unit
                    NewImportsHolder holder;
                    if (imports.containsKey(source)) {
                        holder = (NewImportsHolder) imports.get(source);
                    } else {
                        holder = new NewImportsHolder(source);
                        imports.put(source, holder);
                    }
                    holder.computeImports(itdsForUnit, ajUnit, monitor);


                
                    // make the simplifying assumption that the CU that contains the
                    // ITD does not also contain a target type
                    // Bug 310020
                    if (isCUnitContainingITD(source, (IMember) itdsForUnit.get(0))) {
                        // this is an AJCU.
                        rewriteAspectType(itdsForUnit, source, ast);
                    } else {
                        // this is a regular CU
                        
                        // only push in the declare parents once in order to a
                        // avoid overlapping edits...the declare parents rewrite
                        // takes care of *all* declare parents on that target
                        boolean declareParentsDone = false;
                        
                        for (IMember itd : itdsForUnit) {
                            
                            // filter out the types not affected by itd 
                            Collection<IMember> members = new ArrayList<IMember>();
                            members.addAll(unitsToTypes.get(source));
                            AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(itd);
                            List<IJavaElement> realTargets;
                            if (itd instanceof IAspectJElement && ((IAspectJElement) itd).getAJKind().isDeclareAnnotation()) {
                                realTargets = model
                                        .getRelationshipsForElement(
                                                itd,
                                                AJRelationshipManager.ANNOTATES);
                            } else {
                                // regular ITD or an ITIT
                                realTargets = model
                                        .getRelationshipsForElement(
                                                itd,
                                                AJRelationshipManager.DECLARED_ON);
                            }
                            for (Iterator<IMember> memberIter = members.iterator(); memberIter
                                    .hasNext();) {
                                IMember member = memberIter.next();
                                if (!realTargets.contains(member)) {
                                    memberIter.remove();
                                }
                            }

                            if (members.size() > 0) {
                                //  hmmmm...this may break if there are more than one 
                                // type in a CU that has declare parents on it being pushed in
                                if (itd instanceof IAspectJElement && 
                                        ((IAspectJElement) itd).getAJKind() == Kind.DECLARE_PARENTS) {
                                    if (declareParentsDone) {
                                        continue;
                                    } else {
                                        declareParentsDone = true;
                                    }
                                }
                                applyTargetTypeEdits(itd, source, members);
                            }
                        }  // for (Iterator itdIter = itdsForUnit.iterator(); itdIter.hasNext();) {
                    }
                } catch (JavaModelException e) {
                } catch (CoreException e) {
                }
            }
        };
        IProgressMonitor subMonitor= new SubProgressMonitor(monitor, 1);
        try {
            try {
                final Set<IJavaProject> set= projects.keySet();
                subMonitor.beginTask("Compiling source...", set.size());
                for (IJavaProject project : projects.keySet()) {
                    ASTParser parser= ASTParser.newParser(AST.JLS3);
                    parser.setProject(project);
                    parser.setResolveBindings(true);
                    Collection<ICompilationUnit> collection= projects.get(project);
                    parser.createASTs((ICompilationUnit[]) collection.toArray(
                            new ICompilationUnit[collection.size()]), new String[0], 
                            requestors, new SubProgressMonitor(subMonitor, 1));
                }

            } finally {
                subMonitor.done();
            }
            
        } finally {
            subMonitor.done();
        }
        return status;
    }


    /**
     * Removes all ITDs in the given compilation unit.
     * Will delete an aspect if it has no more members.
     * Will delete an {@link AJCompilationUnit} if all 
     * of its types are deleted.
     * @param itdsForUnit the ITDs for the given unit
     * @param source 
     * @param ast used to calculate imports
     * @throws JavaModelException
     * @throws CoreException
     */
    protected void rewriteAspectType(List<IMember> itdsForUnit,
            ICompilationUnit source, CompilationUnit ast) throws JavaModelException, CoreException {
            
        // used to keep track of aspect types that are deleted.
        Map<IType, Integer> removalStored = new HashMap<IType, Integer>();
        Map<IType, List<DeleteEdit>> typeDeletes = new HashMap<IType, List<DeleteEdit>>();
        
        // go through each ITD and create a delete edit for it
        for (IMember itd : itdsForUnit) {
            IType parentAspectType = (IType) itd.getParent();
            int numRemovals;
            if (removalStored.containsKey(parentAspectType)) {
                numRemovals = ((Integer) removalStored.get(parentAspectType)).intValue();
                removalStored.put(parentAspectType, new Integer(++numRemovals));
            } else {
                removalStored.put(parentAspectType, new Integer(1));
            }
            List<DeleteEdit> deletes;
            if (typeDeletes.containsKey(parentAspectType)) {
                deletes = typeDeletes.get(parentAspectType);
            } else {
                deletes = new LinkedList<DeleteEdit>();
                typeDeletes.put(parentAspectType, deletes);
            }
            
            DeleteEdit edit = new DeleteEdit(itd.getSourceRange().getOffset(), itd.getSourceRange().getLength()+1);
            deletes.add(edit);
        }
        
        if (deleteTypes(ast, typeDeletes, removalStored)) {
            allChanges.put(source, new DeleteResourceChange(source.getResource().getFullPath(), false));
        } else {
            applyAspectEdits(source, typeDeletes);
        }
    }

    /**
     * Deletes all aspect types that have no more children
     * returns true if the compilation unit should be deleted
     * @param ast
     * @param typeDeletes
     * @param removalStored
     * @return
     * @throws JavaModelException
     */
    private boolean deleteTypes(CompilationUnit ast, Map<IType, List<DeleteEdit>> typeDeletes,
            Map<IType, Integer> removalStored) throws JavaModelException {
        if (!deleteEmpty) {
            return false;
        }
        int typesDeleted = 0;
        for (Map.Entry<IType, Integer> entry : removalStored.entrySet()) {
            IType type = entry.getKey();
            int removals = entry.getValue();
            
            // check to see if all of the type's children 
            // have been removed.  If so, delete the type 
            if (type.getChildren().length == removals) {
                @SuppressWarnings("unchecked")
                List<AbstractTypeDeclaration> typeNodes = ast.types();
                for (AbstractTypeDeclaration typeNode : typeNodes) {
                    if (typeNode.getName().toString().equals(type.getElementName())) {
                        List<DeleteEdit> deletes = typeDeletes.get(type);
                        deletes.clear();
                        deletes.add(new DeleteEdit(type.getSourceRange().getOffset(), type.getSourceRange().getLength()));
                        typesDeleted++;
                    }
                }
            }
        }
        
        // check to see if all types have been deleted.
        return (ast.types().size() == typesDeleted);
    }



    private void applyTargetTypeEdits(IMember itd,
            ICompilationUnit source, Collection<IMember> targets) throws CoreException, JavaModelException {
        MultiTextEdit multiEdit = new MultiTextEdit();
        for (IMember target : targets) {
            TextEdit edit = null;
            if (itd instanceof IAspectJElement) {
                IAspectJElement ajElement = (IAspectJElement) itd;
                if (itd instanceof IntertypeElement) {
                    if (target instanceof IType) {
                        IType type = (IType) target;
                        // ignore ITD fields and constructors on interfaces
                        if (type.isInterface() && ajElement.getAJKind() != Kind.INTER_TYPE_METHOD) {
                            edit = null;
                        } else {
                            edit = createEditForITDTarget((IntertypeElement) itd, type);
                        }
                    }
                } else if (ajElement.getAJKind().isDeclareAnnotation()) {
                    edit = createEditForDeclareTarget((DeclareElement) itd, target);
                } else if (ajElement.getAJKind() == Kind.DECLARE_PARENTS) {
                    edit = createEditForDeclareParents((IType) target);
                }
            } else if (itd instanceof IType) {
                // an ITIT
                edit = createEditForIntertypeInnerType((IType) itd, (IType) target);
            }
            if (edit != null) {
                multiEdit.addChild(edit);
            }
        }
        
        if (!isEmptyEdit(multiEdit)) {
            TextFileChange change= (TextFileChange) allChanges.get(source);
            if (change == null) {
                change= new TextFileChange(source.getElementName(), (IFile) source.getResource());
                change.setTextType("java");
                change.setEdit(new MultiTextEdit());
                allChanges.put(source, change);
            }
            change.getEdit().addChild(multiEdit);
        }
    }


    private String getQualifiedTypeForDeclareAnnotation(DeclareElement itd) {
        IProgramElement ipe = AJProjectModelFactory.getInstance()
                .getModelForJavaElement(itd).javaElementToProgramElement(itd);
        if (ipe != null) {
            return ipe.getAnnotationType();
        }
        return null;
    }

    /**
     * Creates the text edit for pushing in an intertype inner type
     * @param itit the intertype inner type to push in 
     * @param target the target to push the itit into.
     * @return
     */
    private TextEdit createEditForIntertypeInnerType(IType itit, IType target) throws JavaModelException {
        String source = itit.getSource();
        // now, we must replace the '.' and the preceding identifier in the type name
        int nameOffset = itit.getNameRange().getOffset() - itit.getSourceRange().getOffset();
        int dotOffset = source.lastIndexOf('$', nameOffset);
        int classIndex = source.lastIndexOf("class ", dotOffset);
        source = "\n\t" + source.substring(0, classIndex + "class ".length()) + source.substring(dotOffset + 1, source.length()) + "\n";
        return new InsertEdit(getITDInsertLocation(target), source);
    }

    // Uses AspectsConvertingParser to recreate the class/interface declaration line
    // this inserts *all* declare parents into the target type.
    // The assumption is that this target type is having all of its declare parents pushed in.
    // So, it is not exactly right in all situations.
    private TextEdit createEditForDeclareParents(IType type) throws JavaModelException {
        AspectsConvertingParser parser = new AspectsConvertingParser(null);
        parser.setUnit(type.getCompilationUnit());
        char[] implementsExtends = 
            parser.createImplementExtendsITDs(type.getElementName().toCharArray());
        // now find the insert location
        String source = type.getSource();
        String toSearch = type.isClass() ? "class" : "interface";
        int nameOffset = type.getNameRange().getOffset() - type.getSourceRange().getOffset();
        int implExtEnd = source.indexOf("{", nameOffset);
        int implExtStart = source.lastIndexOf(toSearch, implExtEnd);
        int offset = type.getSourceRange().getOffset();
        return new ReplaceEdit(offset+implExtStart, implExtEnd-implExtStart, new String(implementsExtends));
    }

    private TextEdit createEditForDeclareTarget(DeclareElement itd,
            IMember target) throws JavaModelException {
        DeclareElementInfo declareElementInfo = (DeclareElementInfo) itd.getElementInfo();
        if (declareElementInfo.isAnnotationRemover()) {
            // must use the model to access removals
            AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(itd);
            IProgramElement ipe = model.javaElementToProgramElement(itd);
            return getAnnotationRemovalEdit(target, ipe.getRemovedAnnotationTypes());
        } else {
            return new InsertEdit(getDeclareInsertLocation(target), getTextForDeclare(itd));
        }
    }

    /**
     * Delete the specified annotations on the target
     * @param target target element to have annotations to delete
     * @param removedAnnotationTypes annotations to delete
     * @return the delete edit.  May be a MultiText edit
     * if multiple annotations are removed
     * @throws JavaModelException 
     */
    private TextEdit getAnnotationRemovalEdit(IMember target,
            String[] removedAnnotationTypes) throws JavaModelException {
        if (target instanceof IAnnotatable) {
            IAnnotatable annotatable = (IAnnotatable) target;
            IAnnotation[] anns = annotatable.getAnnotations();
            List<DeleteEdit> deletes = new ArrayList<DeleteEdit>(removedAnnotationTypes.length);
            for (String removedType : removedAnnotationTypes) {
                // there can be only one match per removed type
                // however, there can be two annotations with the
                // same simple name on the member, but at least one has
                // a matching qual name.
                // So, if there is a simple name match, keep on looking, 
                // but a qual name match, end it.
                DeleteEdit edit = null; 
                for (IAnnotation ann : anns) {
                    // don't know if ann is a simple or a qualified name, 
                    // so check both.
                    String annName = ann.getElementName();
                    boolean qualMatch = removedType.equals(annName);
                    boolean simpleMatch = removedType.endsWith("." + annName);
                    if (simpleMatch || qualMatch) {
                        // match!
                        ISourceRange range = ann.getSourceRange();
                        edit = new DeleteEdit(range.getOffset(), range.getLength());
                        if (qualMatch) {
                            // can only be one qualified match
                            break;
                        }
                    }
                }
                if (edit != null) {
                    deletes.add(edit);
                }
            }
            if (deletes.size() == 0) {
                return null;
            } else if (deletes.size() == 1) {
                return deletes.get(0);
            } else {
                MultiTextEdit multi = new MultiTextEdit();
                for (DeleteEdit delete : deletes) {
                    multi.addChild(delete);
                }
                return multi;
            }
        } else {
            // nothing found
            return null;
        }
    }

    private String getTextForDeclare(DeclareElement itd) throws JavaModelException {
        IProgramElement ipe = AJProjectModelFactory.getInstance()
                .getModelForJavaElement(itd).javaElementToProgramElement(itd);
        if (ipe != null) {
            String details = ipe.getDetails();
            int colonIndex = details.indexOf(':');
            String text = details.substring(colonIndex+1).trim();
            if (itd.getAJKind() == Kind.DECLARE_ANNOTATION_AT_TYPE) {
                // assume top level type
                return text + "\n";
            } else {
                return text + "\n\t";
            }
        } else {
            throw new RuntimeException("Could not find program element in AspectJ model for " + itd.getHandleIdentifier());
        }
    }   

    private int getDeclareInsertLocation(IMember target) throws JavaModelException {
        return target.getSourceRange().getOffset();
    }

    private TextEdit createEditForITDTarget(IntertypeElement itd, IType target) 
            throws JavaModelException {
        // don't add fields or constructors to interfaces
        TextEdit edit;
        if (target.isInterface()) {
            edit = new InsertEdit(getITDInsertLocation(target), getTargetTextForInterface(itd));
        } else {
            edit = new InsertEdit(getITDInsertLocation(target), getTargetTextForClass(itd));
        }
        return edit;
    }

    private String getTargetTextForClass(IntertypeElement itd) throws JavaModelException {
        String itdName = itd.getElementName();
        String[] splits = itdName.split("\\.");
        String newName = splits[splits.length-1];
        itdName = itdName.replaceAll("\\.", "\\\\\\$");
        
        // check for constructor
        if (itdName.endsWith("_new")) {
            // check to see if constructor
            String maybeConstructor = itdName.substring(0, itdName.length()-"_new".length());
            String[] maybeConstructorArr = maybeConstructor.split("\\\\\\$");
            if (maybeConstructorArr.length == 2 && maybeConstructorArr[0].equals(maybeConstructorArr[1])) {
                itdName = maybeConstructorArr[0] +"\\$new";
                newName = maybeConstructorArr[0];
            }
        }

        String targetSource = getTargetSource(newName, itd);
        targetSource = "\n\t" + targetSource + "\n";
        // also replace other pplaces where the itdName may exist
        targetSource = targetSource.replaceAll(itdName, newName);
        
        return targetSource;
    }

    private String getTargetTextForInterface(IntertypeElement itd) throws JavaModelException {
        String itdName = itd.getElementName();
        String[] splits = itdName.split("\\.");
        String newName = splits[splits.length-1];
        itdName = itdName.replaceAll("\\.", "\\\\\\$");
        String targetSource = getTargetSource(newName, itd);
        int closeParen = targetSource.indexOf(")");  // assumption here...closing paren doesn't exist in comments
        if (closeParen >= 0) {
            targetSource = targetSource.substring(0, closeParen+1) + ';';
        }
        targetSource = "\n\t" + targetSource + "\n\n";
        // also replace any other occurrences of the itdName
        targetSource = targetSource.replaceAll(itdName, newName);
        return targetSource;
    }

    /**
     * get the target source and replace the ITD name with the new name
     * @param newName
     * @param itd
     * @return
     * @throws JavaModelException 
     */
    private String getTargetSource(String newName, IntertypeElement itd) throws JavaModelException {
        int itdStart = itd.getSourceRange().getOffset();
        int itdNameStart = itd.getTargetTypeSourceRange().getOffset() - itdStart;
        int itdNameEnd = itd.getNameRange().getOffset() + itd.getNameRange().getLength() - itdStart;
        String targetSource = itd.getSource();
        targetSource = targetSource.substring(0, itdNameStart) + newName + targetSource.substring(itdNameEnd);
        return targetSource;
    }

    private int getITDInsertLocation(IType type) throws JavaModelException {
        return type.getSourceRange().getOffset()+type.getSourceRange().getLength()-1;
    }


    private void applyAspectEdits(ICompilationUnit source, Map<IType, List<DeleteEdit>> typeDeletes) throws JavaModelException,
            CoreException {
        MultiTextEdit edit = new MultiTextEdit();
        for (List<DeleteEdit> deletesForOneType : typeDeletes.values()) {
            for (DeleteEdit delete : deletesForOneType) {
                edit.addChild(delete);
            }
        }
        
        if (!isEmptyEdit(edit)) {
            TextFileChange change= (TextFileChange) allChanges.get(source);
            if (change == null) {
                change= new TextFileChange(source.getElementName(), (IFile) source.getResource());
                change.setTextType("java");
                change.setEdit(edit);
                allChanges.put(source, change);
            } else {
                change.getEdit().addChild(edit);
            }
        }
    }
    



    /**
     * Finds all of the compilation units for the given array of target elements
     * @param targets an array of target elements that are the target of ITDs in a single Aspect
     * @return all of the targets grouped by their {@link ICompilationUnit} 
     */
    private Map<ICompilationUnit, Set<IMember>> getUnitTypeMap(IMember[] targets) {
        Map<ICompilationUnit, Set<IMember>> unitToTypes = new HashMap<ICompilationUnit, Set<IMember>>();
        for (int i = 0; i < targets.length; i++) {
            IMember target = targets[i];
            ICompilationUnit unit = target.getCompilationUnit();
            Set<IMember> currTypes;
            if (unitToTypes.containsKey(unit)) {
                currTypes = unitToTypes.get(unit);
            } else {
                currTypes = new HashSet<IMember>();
                unitToTypes.put(unit, currTypes);
            }
            currTypes.add(target);
        }
        return unitToTypes;
    }

    public RefactoringStatus checkInitialConditions(IProgressMonitor monitor)
            throws CoreException, OperationCanceledException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        RefactoringStatus status= new RefactoringStatus();
        monitor.beginTask("Checking preconditions...", 1);
        
        try {
            for (IMember itd : itds) {
                status.merge(initialITDCheck(itd));
            }
        } finally {
            monitor.done();
        }
        return status;
    }
    
    
    private RefactoringStatus initialITDCheck(IMember itd) {
        RefactoringStatus status= new RefactoringStatus();
        AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(itd);
        if (!model.hasModel()) {
            status.merge(RefactoringStatus.createFatalErrorStatus("Rebuild project.  No crosscutting model available."));
        }
        try {
            if (itd == null) {
                status.merge(RefactoringStatus.createFatalErrorStatus("Intertype declaration has not been specified"));
            } else if (!itd.exists()) {
                status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format("ITD ''{0}'' does not exist.", new Object[] { itd.getElementName()})));
            } else if (!itd.getCompilationUnit().isStructureKnown()) {
                status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format("Compilation unit ''{0}'' contains compile errors.", new Object[] { itd.getCompilationUnit().getElementName()})));
            }
            
            // now check target types
            IMember[] targets = getTargets(Collections.singletonList(itd));
            for (int i = 0; i < targets.length; i++) {
                IMember target = targets[i];
                if (!target.exists()) {
                    status.merge(RefactoringStatus.createFatalErrorStatus(
                            MessageFormat.format("Target type ''{0}'' does not exist.", new Object[] { target.getElementName()})));
                } else if (target.isBinary()) {
                    status.merge(RefactoringStatus.createFatalErrorStatus(
                            MessageFormat.format("Target type ''{0}'' is binary.", new Object[] { target.getElementName()})));
                } else if (!itd.getCompilationUnit().isStructureKnown()) {
                    status.merge(RefactoringStatus.createFatalErrorStatus(
                            MessageFormat.format("Compilation unit ''{0}'' contains compile errors.", 
                                    new Object[] { target.getCompilationUnit().getElementName()})));
                }
            }
        } catch (JavaModelException e) {
            status.addFatalError("JavaModelException:\n\t" + e.getMessage() + "\n\t" + e.getJavaModelStatus().getMessage());
        }
        return status;
    }


    public Change createChange(IProgressMonitor monitor) throws CoreException,
            OperationCanceledException {
        monitor.beginTask("Creating change...", 1);
        try {
            final Collection<Change> changes= allChanges.values();
            CompositeChange change= new CompositeChange(getName(), (Change[]) changes.toArray(new Change[changes.size()])) {
    
                public ChangeDescriptor getDescriptor() {
                    return new RefactoringChangeDescriptor(createDescriptor());
                }

            };
            return change;
        } finally {
            monitor.done();
        }
    }
    
    public PushInRefactoringDescriptor createDescriptor() {
        StringBuffer projectsb = new StringBuffer();
        StringBuffer descriptionsb = new StringBuffer();
        StringBuffer commentsb = new StringBuffer();
        StringBuffer argssb = new StringBuffer();
        for (IMember itd : itds) {
            projectsb.append(itd.getJavaProject().getElementName() + "\n");
            descriptionsb.append(MessageFormat.format("Push In intertype declaration for ''{0}''\n", new Object[] { itd.getElementName()}));
            String itdLabel = AJProjectModelFactory.getInstance().getModelForJavaElement(itd).getJavaElementLinkName(itd);
            commentsb.append(MessageFormat.format("Push In intertype declaration for ''{0}''\n", new Object[] { itdLabel }));
            argssb.append(itd.getHandleIdentifier() + "\n");
        }
        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put(ALL_ITDS, argssb.toString());
        return new PushInRefactoringDescriptor(
                projectsb.toString(), 
                descriptionsb.toString(), 
                commentsb.toString(), 
                arguments);
    }

    
    public String getName() {
        return "Push-In";
    }


    public RefactoringStatus initialize(Map<String, String> arguments) {
        String value= arguments.get(ALL_ITDS);
        if (value != null) {
            String[] values = value.split("\\n");
            List<IMember> newitds = new ArrayList<IMember>(values.length);
            for (int i = 0; i < values.length; i++) {
                newitds.add((IMember) AspectJCore.create(value));
            }
            setITDs(newitds);
            return new RefactoringStatus();
        } else {
            return RefactoringStatus.createErrorStatus("No ITD specified.");
        }
    }
    
    public void setITDs(List<IMember> itds) {
        this.itds = itds;
    }
    
    public List<IMember> getITDs() {
        return itds;
    }


    /**
     * This method determines all of the targets of the set of ITDs passed in.
     * it does not distinguish between which type is affected by which ITD
     * later, we need to do that filtering
     * @param itds set of ITDs to putsh in from a single compilation unit
     * @return array of target elements for these ITDs.
     * @throws JavaModelException
     */
    private IMember[] getTargets(List<IMember> itds) throws JavaModelException {
        AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement((IJavaElement) itds.get(0));
        List<IMember> targets = new ArrayList<IMember>();
        
        for (IMember itd : itds) {
            List<IJavaElement> elts;
            if (itd instanceof IAspectJElement && ((IAspectJElement) itd).getAJKind().isDeclareAnnotation()) {
                elts = model.getRelationshipsForElement(itd, AJRelationshipManager.ANNOTATES);
            } else {
                // either an ITD or an ITIT (IType)
                elts = model.getRelationshipsForElement(itd, AJRelationshipManager.DECLARED_ON);
            }

            for (IJavaElement elt : elts) {
                targets.add((IMember) elt);
            }
        }
        return (IMember[]) targets.toArray(new IMember[targets.size()]);
    }

    private boolean isEmptyEdit(TextEdit edit) {
        return edit.getClass() == MultiTextEdit.class && !edit.hasChildren();
    }
    
    private boolean isCUnitContainingITD(ICompilationUnit unit, IMember itd) {
        return itd != null && itd.getCompilationUnit().equals(unit);
    }
}
