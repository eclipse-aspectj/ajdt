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
import org.eclipse.ajdt.core.javaelements.DeclareElement;
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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
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
        Set/* Name */ staticImports;
        Set/* Name */ typeImports;
        Set/* String */ extraImports;
        ICompilationUnit unit;
        
        NewImportsHolder(ICompilationUnit unit) {
            staticImports = new HashSet();
            typeImports = new HashSet();
            extraImports = new HashSet();
            this.unit = unit;
        }
        
        // does not handle imports for declare annotations and declare parents
        void rewriteImports() throws CoreException {
            // first check to see if this unit has been deleted.
            Change change = (Change) allChanges.get(unit);
            if (! (change instanceof TextFileChange)) {
                return;
            }
            
            ImportRewrite rewrite = ImportRewrite.create(unit, true);
            for (Iterator importIter = typeImports.iterator(); importIter.hasNext();) {
                Name name = (Name) importIter.next();
                ITypeBinding binding = name.resolveTypeBinding();
                if (binding != null) {
                    rewrite.addImport(binding);
                }
            }
            for (Iterator importIter = staticImports.iterator(); importIter.hasNext();) {
                Name name = (Name) importIter.next();
                rewrite.addImport(name.resolveTypeBinding());
            }

            for (Iterator importIter = extraImports.iterator(); importIter.hasNext();) {
                String qualName = (String) importIter.next();
                rewrite.addImport(qualName);
            }
            TextEdit importEdit = rewrite.rewriteImports(new NullProgressMonitor());
            
            TextFileChange textChange = (TextFileChange) change;
            if (change == null) {
                change= new TextFileChange(unit.getElementName(), (IFile) unit.getResource());
                textChange.setTextType("java");
                textChange.setEdit(new MultiTextEdit());
                allChanges.put(unit, change);
            }
            textChange.getEdit().addChild(importEdit);
        }

        void computeImports(List/* IntertypeElement */itds, ICompilationUnit ajUnit, IProgressMonitor monitor)
                throws JavaModelException {
            IJavaProject project = ajUnit.getJavaProject();
            ASTParser parser = ASTParser.newParser(AST.JLS3);
            parser.setProject(project);
            parser.setResolveBindings(true);
            parser.setSource(ajUnit);
            ASTNode ajAST = parser.createAST(monitor);

            for (Iterator itdsIter = itds.iterator(); itdsIter.hasNext();) {
                IAspectJElement itd = (IAspectJElement) itdsIter.next();
                ISourceRange range = itd.getSourceRange();
                ImportReferencesCollector.collect(ajAST, project, new Region(
                        range.getOffset(), range.getLength()),
                        typeImports, staticImports);
                if (itd instanceof DeclareElement) {
                    // need to find out the qualified name of the type being declared
                    
                    System.out.println(itd.getHandleIdentifier());
                }
            }
        }
        
        void addExtraImport(String typeImport) {
            extraImports.add(typeImport);
        }
    }
    
    private boolean deleteEmpty = true;
    
    private Map /* ICompilationUnit -> Change*/ allChanges = null; 

    private List/*IntertypeElement*/ itds = null;
    

    public RefactoringStatus checkFinalConditions(IProgressMonitor monitor)
            throws CoreException, OperationCanceledException {
        final RefactoringStatus status = new RefactoringStatus();
        try {
            monitor.beginTask("Checking preconditions...", 2);
            allChanges = new LinkedHashMap();
            Map/*ICompiltionUnit->ITD*/ unitToITDs = new HashMap();
            for (Iterator itdIter = itds.iterator(); itdIter.hasNext();) {
                IAspectJElement itd = (IAspectJElement) itdIter.next();
                ICompilationUnit unit = itd.getCompilationUnit();
                List itds;
                if (unitToITDs.containsKey(unit)) {
                    itds = (List) unitToITDs.get(unit);
                } else {
                    itds = new LinkedList();
                    unitToITDs.put(unit, itds);
                }
                itds.add(itd);
            }
            
            Map/*ICompilationUnit -> NewImportsHolder*/ importsMap = new HashMap();
            for (Iterator unitIter = unitToITDs.entrySet().iterator(); unitIter.hasNext();) {
                Map.Entry entry = (Map.Entry) unitIter.next();
                
                status.merge(checkFinalConditionsForITD(
                        (ICompilationUnit) entry.getKey(),
                        (List) entry.getValue(), importsMap,
                        monitor));
            }
            
            // now go through and create the import edits
            for (Iterator iterator = importsMap.values().iterator(); iterator.hasNext();) {
                NewImportsHolder holder = (NewImportsHolder) iterator.next();
                holder.rewriteImports();
            }
            
        } finally {
            monitor.done();
        }

        return status;
    }

    private RefactoringStatus checkFinalConditionsForITD(final ICompilationUnit ajUnit, 
            final List/*IAspectJElement*/ itdsForUnit, 
            final Map/*ICompilationUnit -> NewImportsHolder*/ imports, 
            final IProgressMonitor monitor) throws JavaModelException {
        
        final RefactoringStatus status = new RefactoringStatus();
        
        
        final Map/*ICompilationUnit -> IMember[]*/ unitsToTypes = getUnitTypeMap(getTargets(itdsForUnit));
        final Map/*IJavaProject, Collection<ICompilationUnit>*/ projects= new HashMap();
        
        for (Iterator unitIter = unitsToTypes.keySet().iterator(); unitIter.hasNext();) {
            ICompilationUnit targetUnit = (ICompilationUnit) unitIter.next();
            IJavaProject project= targetUnit.getJavaProject();
            if (project != null) {
                Collection collection = (Collection) projects.get(project);
                if (collection == null) {
                    collection= new ArrayList();
                    projects.put(project, collection);
                }
                collection.add(targetUnit);
            }
        }
        
        
        Collection units;
        IJavaProject aspectProject = ajUnit.getJavaProject();
        if (projects.containsKey(aspectProject)) {
            units = (Collection) projects.get(aspectProject);
        } else {
            units = new ArrayList();
            projects.put(aspectProject, units);
        }
        units.add(ajUnit);
        
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
                    if (isCUnitContainingITD(source, (IAspectJElement) itdsForUnit.get(0))) {
                        rewriteAspectType(itdsForUnit, source, ast, status);
                    } else {
                        // only do the declare parents once in order to a
                        // avoid overlapping edits...the declare parents rewrite
                        // takes care of *all* declare parents on that target
                        boolean declareParentsDone = false;
                        
                        
                        for (Iterator itdIter = itdsForUnit.iterator(); itdIter.hasNext();) {
                            IAspectJElement itd = (IAspectJElement) itdIter.next();
                            
                            // filter out the types not affected by itd 
                            Collection/*IMember*/ members = new ArrayList();
                            members.addAll((Collection) unitsToTypes.get(source));
                            AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(itd);
                            List/*IJavaElement*/ realTargets;
                            if (itd.getAJKind().isDeclareAnnotation()) {
                                realTargets = model.getRelationshipsForElement(itd, AJRelationshipManager.ANNOTATES);
                            } else {
                                realTargets = model.getRelationshipsForElement(itd, AJRelationshipManager.DECLARED_ON);
                            }
                            for (Iterator memberIter = members.iterator(); memberIter
                                    .hasNext();) {
                                IMember member = (IMember) memberIter.next();
                                if (!realTargets.contains(member)) {
                                    memberIter.remove();
                                }
                            }

                            if (members.size() > 0) {
                                //  hmmmm...this may break if there are more than one 
                                // type in a CU that has declare parents on it being pushed in
                                if (itd.getAJKind() == Kind.DECLARE_PARENTS) {
                                    if (declareParentsDone) {
                                        continue;
                                    } else {
                                        declareParentsDone = true;
                                    }
                                }
                                rewriteTargetTypes(itd, source, members, ast, status);
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
                final Set set= projects.keySet();
                subMonitor.beginTask("Compiling source...", set.size());
                for (Iterator projIter = projects.keySet().iterator(); projIter.hasNext();) {
                    IJavaProject project = (IJavaProject) projIter.next();
                    ASTParser parser= ASTParser.newParser(AST.JLS3);
                    parser.setProject(project);
                    parser.setResolveBindings(true);
                    Collection collection= (Collection) projects.get(project);
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


    
    protected void rewriteAspectType(List itdsForUnit,
            ICompilationUnit source, CompilationUnit ast,
            RefactoringStatus status) throws JavaModelException, CoreException {
            
        // check to see if we need to delete this type
        Map/*Type->Integer*/ removalStored = new HashMap();
        Map/*Type->List of DeleteEdit*/ typeDeletes = new HashMap();
        
        for (Iterator itdIter = itdsForUnit.iterator(); itdIter.hasNext();) {
            IAspectJElement itd = (IAspectJElement) itdIter.next();
            IType type = (IType) itd.getParent();
            int numRemovals;
            if (removalStored.containsKey(type)) {
                numRemovals = ((Integer) removalStored.get(type)).intValue();
                removalStored.put(type, new Integer(++numRemovals));
            } else {
                removalStored.put(type, new Integer(1));
            }
            List deletes;
            if (typeDeletes.containsKey(type)) {
                deletes = (List) typeDeletes.get(type);
            } else {
                deletes = new LinkedList();
                typeDeletes.put(type, deletes);
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

    // Deletes all aspect types that have no more children
    // returns true if the compilation unit should be deleted
    private boolean deleteTypes(CompilationUnit ast, Map typeDeletes,
            Map removalStored) throws JavaModelException {
        if (!deleteEmpty) {
            return false;
        }
        int typesDeleted = 0;
        for (Iterator removalIter = removalStored.entrySet().iterator(); removalIter.hasNext();) {
            Map.Entry entry = (Map.Entry) removalIter.next();
            IType type = (IType) entry.getKey();
            int removals = ((Integer) entry.getValue()).intValue();;
            
            if (type.getChildren().length == removals) {
                List typeNodes = ast.types();
                for (Iterator typeIter = typeNodes.iterator(); typeIter.hasNext();) {
                    AbstractTypeDeclaration typeNode = (AbstractTypeDeclaration) typeIter.next();
                    if (typeNode.getName().toString().equals(type.getElementName())) {
                        List deletes = (List) typeDeletes.get(type);
                        deletes.clear();
                        deletes.add(new DeleteEdit(type.getSourceRange().getOffset(), type.getSourceRange().getLength()));
                        typesDeleted++;
                    }
                }
            }
        }
        return (ast.types().size() == typesDeleted);
    }


    protected void rewriteTargetTypes(
            IAspectJElement itd,
            ICompilationUnit source, Collection/*IMember*/ targets,
            CompilationUnit node, RefactoringStatus status) throws JavaModelException, CoreException {
        
        
        applyTargetTypeEdits(itd, source, targets);

    }

    private void applyTargetTypeEdits(IAspectJElement itd,
            ICompilationUnit source, Collection/*IMember*/ targets) throws CoreException, JavaModelException {
        MultiTextEdit multiEdit = new MultiTextEdit();
        for (Iterator targetIter = targets.iterator(); targetIter.hasNext();) {
            IMember target = (IMember) targetIter.next();
            TextEdit edit = null;
            if (itd instanceof IntertypeElement) {
                if (target instanceof IType) {
                    IType type = (IType) target;
                    // ignore ITD fields and constructors on interfaces
                    if (type.isInterface() && itd.getAJKind() != Kind.INTER_TYPE_METHOD) {
                        edit = null;
                    } else {
                        edit = createEditForITDTarget((IntertypeElement) itd, type);
                    }
                }
            } else if (itd.getAJKind().isDeclareAnnotation()) {
                edit = createEditForDeclareTarget((DeclareElement) itd, target);
            } else if (itd.getAJKind() == Kind.DECLARE_PARENTS) {
                edit = createEditForDeclareParents((IType) target);
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


    // Uses AspectsConvertingParser to recreate the class/interface declaration line
    // this inserts *all* declare parents into the target type.
    // The assumption is that this target type is having all of its declare parents pushed in.
    // So, it is not exactly right in all situations.
    private TextEdit createEditForDeclareParents(IType type) throws JavaModelException {
        AspectsConvertingParser parser = new AspectsConvertingParser(null);
        parser.setUnit(type.getCompilationUnit());
        char[] implementsExtends = 
            parser.createImplementExtendsITDs(type.getElementName().toCharArray());
        // now find source location
        String source = type.getSource();
        String toSearch = type.isClass() ? "class" : "interface";
        int implExtEnd = source.indexOf("{");
        int implExtStart = source.lastIndexOf(toSearch, implExtEnd);
        int offset = type.getSourceRange().getOffset();
        return new ReplaceEdit(offset+implExtStart, implExtEnd-implExtStart, new String(implementsExtends));
    }

    private TextEdit createEditForDeclareTarget(DeclareElement itd,
            IMember target) throws JavaModelException {
        return new InsertEdit(getDeclareInsertLocation(target), getTextForDeclare(itd));
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

        String targetSource = "\n\t" + itd.getSource() + "\n";
        targetSource = targetSource.replaceAll(itdName, newName);
        return targetSource;
    }
    private String getTargetTextForInterface(IntertypeElement itd) throws JavaModelException {
        String itdName = itd.getElementName();
        String[] splits = itdName.split("\\.");
        String newName = splits[splits.length-1];
        itdName = itdName.replaceAll("\\.", "\\\\\\$");
        String targetSource = "\t" + itd.getSource() + "\n";
        int nameStart = targetSource.indexOf(itdName);
        int closeParen = targetSource.indexOf(")", nameStart);  // big assumption here...that closing paren doesn't exist in comments 
        targetSource = "\n" + targetSource.substring(0, closeParen) + ";\n";
        targetSource = targetSource.replaceAll(itdName, newName);
        return targetSource;
    }


    private int getITDInsertLocation(IType type) throws JavaModelException {
        return type.getSourceRange().getOffset()+type.getSourceRange().getLength()-1;
    }


    private void applyAspectEdits(ICompilationUnit source, Map typeDeletes) throws JavaModelException,
            CoreException {
        MultiTextEdit edit = new MultiTextEdit();
        for (Iterator typeDeletesIter = typeDeletes.values().iterator(); typeDeletesIter.hasNext();) {
            List deletesForOneType = (List) typeDeletesIter.next();
            for (Iterator deleteIter = deletesForOneType.iterator(); deleteIter
                    .hasNext();) {
                DeleteEdit delete = (DeleteEdit) deleteIter.next();
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
    



    private Map/* ICompilationUnit -> Set of IMember */ getUnitTypeMap(IMember[] targets) {
        Map unitToTypes = new HashMap();
        for (int i = 0; i < targets.length; i++) {
            IMember target = targets[i];
            ICompilationUnit unit = target.getCompilationUnit();
            Set currTypes;
            if (unitToTypes.containsKey(unit)) {
                currTypes = (Set) unitToTypes.get(unit);
            } else {
                currTypes = new HashSet();
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
            for (Iterator itdIter = itds.iterator(); itdIter.hasNext();) {
                IAspectJElement itd = (IAspectJElement) itdIter.next();
                status.merge(initialITDCheck(itd));
            }
        } finally {
            monitor.done();
        }
        return status;
    }
    
    
    private RefactoringStatus initialITDCheck(IAspectJElement itd) {
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
            final Collection changes= (Collection) allChanges.values();
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
        for (Iterator itdIter = itds.iterator(); itdIter.hasNext();) {
            IAspectJElement itd = (IAspectJElement) itdIter.next();
            projectsb.append(itd.getJavaProject().getElementName() + "\n");
            descriptionsb.append(MessageFormat.format("Push In intertype declaration for ''{0}''\n", new Object[] { itd.getElementName()}));
            String itdLabel = AJProjectModelFactory.getInstance().getModelForJavaElement(itd).getJavaElementLinkName(itd);
            commentsb.append(MessageFormat.format("Push In intertype declaration for ''{0}''\n", new Object[] { itdLabel }));
            argssb.append(itd.getHandleIdentifier() + "\n");
        }
        Map arguments = new HashMap();
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


    public RefactoringStatus initialize(Map arguments) {
        String value= (String) arguments.get(ALL_ITDS);
        if (value != null) {
            String[] values = value.split("\\n");
            List/*IntertypeElement*/ newitds = new ArrayList(values.length);
            for (int i = 0; i < values.length; i++) {
                newitds.add(AspectJCore.create(value));
            }
            setITDs(newitds);
            return new RefactoringStatus();
        } else {
            return RefactoringStatus.createErrorStatus("No ITD specified.");
        }
    }
    
    public void setITDs(List/*IntertypeElement*/ itds) {
        this.itds = itds;
    }
    
    public List/*IntertypeElement*/ getITDs() {
        return itds;
    }


    // this method returns all members by any itd being pushed in from a CU
    // it does not distinguish between which type is affected by which ITD
    // later, we need to do that filtering
    private IMember[] getTargets(List/*IAspectJElement*/ itds) throws JavaModelException {
        AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement((IJavaElement) itds.get(0));
        List/*IMember*/ targets = new ArrayList();
        
        for (Iterator itdIter = itds.iterator(); itdIter.hasNext();) {
            IAspectJElement itd = (IAspectJElement) itdIter.next();
            List/*IJavaElement*/ elts;
            if (itd.getAJKind().isDeclareAnnotation()) {
                elts = model.getRelationshipsForElement(itd, AJRelationshipManager.ANNOTATES);
            } else {
                elts = model.getRelationshipsForElement(itd, AJRelationshipManager.DECLARED_ON);
            }

            for (Iterator eltIter = elts.iterator(); eltIter.hasNext();) {
                targets.add(eltIter.next());
            }
        }
        return (IMember[]) targets.toArray(new IMember[targets.size()]);
    }

    private boolean isEmptyEdit(TextEdit edit) {
        return edit.getClass() == MultiTextEdit.class && !edit.hasChildren();
    }
    
    private boolean isCUnitContainingITD(ICompilationUnit unit, IAspectJElement itd) {
        return itd != null && itd.getCompilationUnit().equals(unit);
    }

    /**
     * field has been transformed in the AST, so must look for a different name
     */
    private String transformedITDName(IntertypeElement itd) {
        String name = itd.getElementName();
        if (name.endsWith("_new")) {
            // check to see if constructor
            String maybeConstructor = name.substring(0, name.length()-"_new".length());
            String[] maybeConstructorArr = maybeConstructor.split("\\.");
            if (maybeConstructorArr.length == 2 && maybeConstructorArr[0].equals(maybeConstructorArr[1])) {
                return maybeConstructorArr[0] + "$new";
            }
        }
        return name.replace('.', '$');
    }
}
