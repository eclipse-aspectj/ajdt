package org.eclipse.ajdt.core.javaelements;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ICompletionRequestor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaElementInfo;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.core.Member;
import org.eclipse.jdt.internal.core.SourceMapper;
import org.eclipse.jdt.internal.core.SourceMethodElementInfo;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;

public class ITDAwareSourceTypeDELETEME extends SourceType {

    private SourceType delegate;
    
    private IMethod[] itdConstructors;
    private IMethod[] itdMethods;
    private IField [] itdFields;
    private IType     itdSuperType;       // not used
    private IType  [] itdSuperInterfaces; // not used
    private boolean   itdInitialized = false;
    
    private boolean initializeITDs() {
        AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(this);
        if (model.hasModel()) {
            try {
                List/*IJavaElement*/ constructors = new ArrayList();
                List/*IJavaElement*/ methods = new ArrayList();
                List/*IJavaElement*/ fields = new ArrayList();
                List/*IJavaElement*/ rels = model.getRelationshipsForElement(this, AJRelationshipManager.ASPECT_DECLARATIONS);
                for (Iterator relIter = rels.iterator(); relIter.hasNext();) {
                    IntertypeElement elt = (IntertypeElement) relIter.next();
                    IMember member = elt.createMockDeclaration(this);
                    switch (member.getElementType()) {
                    case IJavaElement.METHOD:
                        if (((IMethod) member).isConstructor()) {
                            constructors.add(member);
                        } else {
                            methods.add(member);
                        }
                        break;
                    case IJavaElement.FIELD:
                        fields.add(member);
                        break;
                    }
                }
                itdMethods = (IMethod[]) methods.toArray();
                itdFields = (IField[]) fields.toArray();
                itdConstructors = (IMethod[]) constructors.toArray();
                return itdInitialized = true;
            } catch (JavaModelException e) {
            }
        } 
        return itdInitialized = false;
    }
    
    
    
    
    public ITDAwareSourceTypeDELETEME(SourceType delegate) {
        super((JavaElement) delegate.getParent(), delegate.getElementName());
        this.delegate = delegate;
    }

    protected void closing(Object info) throws JavaModelException {
        throw new NotImplementedException();
    }

    public void codeComplete(char[] snippet, int insertion, int position,
            char[][] localVariableTypeNames, char[][] localVariableNames,
            int[] localVariableModifiers, boolean isStatic,
            CompletionRequestor requestor, WorkingCopyOwner owner)
            throws JavaModelException {
        delegate.codeComplete(snippet, insertion, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic, requestor, owner);
    }

    public void codeComplete(char[] snippet, int insertion, int position,
            char[][] localVariableTypeNames, char[][] localVariableNames,
            int[] localVariableModifiers, boolean isStatic,
            CompletionRequestor requestor) throws JavaModelException {
        delegate.codeComplete(snippet, insertion, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic, requestor);
    }

    public void codeComplete(char[] snippet, int insertion, int position,
            char[][] localVariableTypeNames, char[][] localVariableNames,
            int[] localVariableModifiers, boolean isStatic,
            ICompletionRequestor requestor, WorkingCopyOwner owner)
            throws JavaModelException {
        delegate.codeComplete(snippet, insertion, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic, requestor, owner);
    }

    public void codeComplete(char[] snippet, int insertion, int position,
            char[][] localVariableTypeNames, char[][] localVariableNames,
            int[] localVariableModifiers, boolean isStatic,
            ICompletionRequestor requestor) throws JavaModelException {
        delegate.codeComplete(snippet, insertion, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic, requestor);
    }

    public IField createField(String contents, IJavaElement sibling,
            boolean force, IProgressMonitor monitor) throws JavaModelException {
        return delegate.createField(contents, sibling, force, monitor);
    }

    public IInitializer createInitializer(String contents,
            IJavaElement sibling, IProgressMonitor monitor)
            throws JavaModelException {
        return delegate.createInitializer(contents, sibling, monitor);
    }

    public IMethod createMethod(String contents, IJavaElement sibling,
            boolean force, IProgressMonitor monitor) throws JavaModelException {
        return delegate.createMethod(contents, sibling, force, monitor);
    }

    public IType createType(String contents, IJavaElement sibling,
            boolean force, IProgressMonitor monitor) throws JavaModelException {
        return delegate.createType(contents, sibling, force, monitor);
    }

    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    public IMethod[] findMethods(IMethod method) {
        return delegate.findMethods(method);
    }

    public IAnnotation[] getAnnotations() throws JavaModelException {
        return delegate.getAnnotations();
    }

    public IJavaElement[] getChildrenForCategory(String category)
            throws JavaModelException {
        return delegate.getChildrenForCategory(category);
    }

    public IType getDeclaringType() {
        return delegate.getDeclaringType();
    }

    public int getElementType() {
        return delegate.getElementType();
    }

    public IField getField(String fieldName) {
        if (!itdInitialized) {
            initializeITDs();
        }
        
        IField field;
        for (int i = 0; i < itdFields.length; i++) {
            if (itdFields[i].getElementName().equals(fieldName)) {
                return itdFields[i];
            }
        }
        
        field = delegate.getField(fieldName);
        return field;
    }

    public IField[] getFields() throws JavaModelException {
//        IField[] regularFields = delegate.getFields();
//        IField[] allFields = new IField[regularFields.length + itdFields.length];
//        System.arraycopy(regularFields, 0, allFields, 0, regularFields.length);
//        System.arraycopy(itdFields, 0, allFields, regularFields.length, itdFields.length);
//        return allFields;
        return super.getFields();
    }

    public String getFullyQualifiedName() {
        return delegate.getFullyQualifiedName();
    }

    public String getFullyQualifiedName(char enclosingTypeSeparator) {
        return delegate.getFullyQualifiedName(enclosingTypeSeparator);
    }

    public String getFullyQualifiedParameterizedName()
            throws JavaModelException {
        return delegate.getFullyQualifiedParameterizedName();
    }

    public IJavaElement getHandleFromMemento(String token,
            MementoTokenizer memento, WorkingCopyOwner workingCopyOwner) {
        return delegate.getHandleFromMemento(token, memento, workingCopyOwner);
    }

    public IInitializer getInitializer(int count) {
        return delegate.getInitializer(count);
    }

    public IInitializer[] getInitializers() throws JavaModelException {
        return delegate.getInitializers();
    }

    public String getKey() {
        return delegate.getKey();
    }

    public IMethod getMethod(String selector, String[] parameterTypeSignatures) {
//        for (int i = 0; i < itdMethods.length; i++) {
//            if (selector.equals(itdMethods[i].getElementName())) {
//                boolean paramsOK = true;
//                for (int j = 0; j < parameterTypeSignatures.length; j++) {
//                    if (! itdMethods[j].getParameterTypes()[j].equals(parameterTypeSignatures[j])) {
//                        paramsOK = false;
//                    }
//                }
//                if (paramsOK) {
//                    return itdMethods[i];
//                }
//            }
//        }
//        return delegate.getMethod(selector, parameterTypeSignatures);
        return super.getMethod(selector, parameterTypeSignatures);
    }

    public IMethod[] getMethods() throws JavaModelException {
//        IMethod[] regularMethods = delegate.getMethods();
//        IMethod[] allMethods = new IMethod[regularMethods.length + itdMethods.length + itdConstructors.length];
//        System.arraycopy(regularMethods, 0, allMethods, 0, regularMethods.length);
//        System.arraycopy(itdMethods, 0, allMethods, regularMethods.length, itdMethods.length);
//        System.arraycopy(itdConstructors, 0, allMethods, regularMethods.length + itdMethods.length, itdConstructors.length);
//        return allMethods;
        return super.getMethods();
    }

    public IPackageFragment getPackageFragment() {
        return delegate.getPackageFragment();
    }

    public IJavaElement getPrimaryElement(boolean checkOwner) {
        return delegate.getPrimaryElement(checkOwner);
    }

    public String getSuperclassName() throws JavaModelException {
        // XXX
        return delegate.getSuperclassName();
    }

    public String getSuperclassTypeSignature() throws JavaModelException {
        // XXX
        return delegate.getSuperclassTypeSignature();
    }

    public String[] getSuperInterfaceNames() throws JavaModelException {
        // XXX
        return delegate.getSuperInterfaceNames();
    }

    public String[] getSuperInterfaceTypeSignatures() throws JavaModelException {
        //  XXX
        return delegate.getSuperInterfaceTypeSignatures();
    }

    public IType getType(String typeName) {
        return delegate.getType(typeName);
    }

    public ITypeParameter getTypeParameter(String typeParameterName) {
        return delegate.getTypeParameter(typeParameterName);
    }

    public ITypeParameter[] getTypeParameters() throws JavaModelException {
        return delegate.getTypeParameters();
    }

    public String[] getTypeParameterSignatures() throws JavaModelException {
        return delegate.getTypeParameterSignatures();
    }

    public String getTypeQualifiedName() {
        return delegate.getTypeQualifiedName();
    }

    public String getTypeQualifiedName(char enclosingTypeSeparator) {
        return delegate.getTypeQualifiedName(enclosingTypeSeparator);
    }

    public IType[] getTypes() throws JavaModelException {
        return delegate.getTypes();
    }

    public boolean isAnnotation() throws JavaModelException {
        return delegate.isAnnotation();
    }

    public boolean isAnonymous() {
        return delegate.isAnonymous();
    }

    public boolean isClass() throws JavaModelException {
        return delegate.isClass();
    }

    public boolean isEnum() throws JavaModelException {
        return delegate.isEnum();
    }

    public boolean isInterface() throws JavaModelException {
        return delegate.isInterface();
    }

    public boolean isLocal() {
        return delegate.isLocal();
    }

    public boolean isMember() {
        return delegate.isMember();
    }

    public boolean isResolved() {
        return delegate.isResolved();
    }

    public ITypeHierarchy loadTypeHierachy(InputStream input,
            IProgressMonitor monitor) throws JavaModelException {
        // XXX
        return delegate.loadTypeHierachy(input, monitor);
    }

    public ITypeHierarchy loadTypeHierachy(InputStream input,
            WorkingCopyOwner owner, IProgressMonitor monitor)
            throws JavaModelException {
        // XXX
        return delegate.loadTypeHierachy(input, owner, monitor);
    }

    public ITypeHierarchy newSupertypeHierarchy(
            ICompilationUnit[] workingCopies, IProgressMonitor monitor)
            throws JavaModelException {
        // XXX
        return delegate.newSupertypeHierarchy(workingCopies, monitor);
    }

    public ITypeHierarchy newSupertypeHierarchy(IProgressMonitor monitor)
            throws JavaModelException {
        // XXX
        return delegate.newSupertypeHierarchy(monitor);
    }

    public ITypeHierarchy newSupertypeHierarchy(IWorkingCopy[] workingCopies,
            IProgressMonitor monitor) throws JavaModelException {
        // XXX
        return delegate.newSupertypeHierarchy(workingCopies, monitor);
    }

    public ITypeHierarchy newSupertypeHierarchy(WorkingCopyOwner owner,
            IProgressMonitor monitor) throws JavaModelException {
        // XXX
        return delegate.newSupertypeHierarchy(owner, monitor);
    }

    public ITypeHierarchy newTypeHierarchy(ICompilationUnit[] workingCopies,
            IProgressMonitor monitor) throws JavaModelException {
        // XXX
        return delegate.newTypeHierarchy(workingCopies, monitor);
    }

    public ITypeHierarchy newTypeHierarchy(IJavaProject project,
            IProgressMonitor monitor) throws JavaModelException {
        // XXX
        return delegate.newTypeHierarchy(project, monitor);
    }

    public ITypeHierarchy newTypeHierarchy(IJavaProject project,
            WorkingCopyOwner owner, IProgressMonitor monitor)
            throws JavaModelException {
        // XXX
        return delegate.newTypeHierarchy(project, owner, monitor);
    }

    public ITypeHierarchy newTypeHierarchy(IProgressMonitor monitor)
            throws JavaModelException {
        // XXX
        return delegate.newTypeHierarchy(monitor);
    }

    public ITypeHierarchy newTypeHierarchy(IWorkingCopy[] workingCopies,
            IProgressMonitor monitor) throws JavaModelException {
        // XXX
        return delegate.newTypeHierarchy(workingCopies, monitor);
    }

    public ITypeHierarchy newTypeHierarchy(WorkingCopyOwner owner,
            IProgressMonitor monitor) throws JavaModelException {
        // XXX
        return delegate.newTypeHierarchy(owner, monitor);
    }

    public JavaElement resolved(Binding binding) {
        return delegate.resolved(binding);
    }

    protected void toStringInfo(int tab, StringBuffer buffer, Object info,
            boolean showResolvedInfo) {
        super.toStringInfo(tab, buffer, info, showResolvedInfo);
    }

    public String getElementName() {
        return delegate.getElementName();
    }

    public String getFullyQualifiedName(char enclosingTypeSeparator,
            boolean showParameters) throws JavaModelException {
        return delegate.getFullyQualifiedName(enclosingTypeSeparator, showParameters);
    }

    protected String getFullyQualifiedParameterizedName(
            String fullyQualifiedName, String uniqueKey)
            throws JavaModelException {
        return super.getFullyQualifiedParameterizedName(fullyQualifiedName, uniqueKey);
    }

    protected String getKey(IField field, boolean forceOpen)
            throws JavaModelException {
        return super.getKey(field, forceOpen);
    }

    protected String getKey(IMethod method, boolean forceOpen)
            throws JavaModelException {
        return super.getKey(method, forceOpen);
    }

    protected String getKey(IType type, boolean forceOpen)
            throws JavaModelException {
        return super.getKey(type, forceOpen);
    }

    public String getTypeQualifiedName(char enclosingTypeSeparator,
            boolean showParameters) throws JavaModelException {
        return delegate.getTypeQualifiedName(enclosingTypeSeparator, showParameters);
    }

    public String[][] resolveType(String typeName) throws JavaModelException {
        return delegate.resolveType(typeName);
    }

    public String[][] resolveType(String typeName, WorkingCopyOwner owner)
            throws JavaModelException {
        return delegate.resolveType(typeName, owner);
    }

    public String[] getCategories() throws JavaModelException {
        return delegate.getCategories();
    }

    public IClassFile getClassFile() {
        return delegate.getClassFile();
    }

    public int getFlags() throws JavaModelException {
        return delegate.getFlags();
    }

    protected char getHandleMementoDelimiter() {
        return JavaElement.JEM_TYPE;
    }

    public ISourceRange getJavadocRange() throws JavaModelException {
        return delegate.getJavadocRange();
    }

    public ISourceRange getNameRange() throws JavaModelException {
        return delegate.getNameRange();
    }

    public Member getOuterMostLocalContext() {
        return delegate.getOuterMostLocalContext();
    }

    public IType getType(String typeName, int count) {
        return delegate.getType(typeName, count);
    }

    public ITypeRoot getTypeRoot() {
        return delegate.getTypeRoot();
    }

    public boolean isBinary() {
        return delegate.isBinary();
    }

    protected boolean isMainMethod(IMethod method) throws JavaModelException {
        if ("main".equals(method.getElementName()) && Signature.SIG_VOID.equals(method.getReturnType())) { //$NON-NLS-1$
            int flags= method.getFlags();
            if (Flags.isStatic(flags) && Flags.isPublic(flags)) {
                String[] paramTypes= method.getParameterTypes();
                if (paramTypes.length == 1) {
                    String typeSignature=  Signature.toString(paramTypes[0]);
                    return "String[]".equals(Signature.getSimpleName(typeSignature)); //$NON-NLS-1$
                }
            }
        }
        return false;
    }

    public boolean isReadOnly() {
        return delegate.isReadOnly();
    }

    public String readableName() {
        return delegate.readableName();
    }

    protected void updateNameRange(int nameStart, int nameEnd) {
        super.updateNameRange(nameStart, nameEnd);
    }

    public void copy(IJavaElement container, IJavaElement sibling,
            String rename, boolean force, IProgressMonitor monitor)
            throws JavaModelException {

        delegate.copy(container, sibling, rename, force, monitor);
    }
    
    private class ITDAwareSourceTypeElementInfo extends SourceTypeElementInfo {
        protected void setSuperclassName(char[] superclassName) {
            super.setSuperclassName(superclassName);
        }
        protected void setFlags(int flags) {
            super.setFlags(flags);
        }
        protected void setHandle(IType handle) {
            super.setHandle(handle);
        }
        protected void setNameSourceEnd(int end) {
            super.setNameSourceEnd(end);
        }
        protected void setNameSourceStart(int start) {
            super.setNameSourceStart(start);
        }
        protected void setSourceRangeEnd(int end) {
            super.setSourceRangeEnd(end);
        }
        protected void setSourceRangeStart(int start) {
            super.setSourceRangeStart(start);
        }
        protected void setSuperInterfaceNames(char[][] superInterfaceNames) {
            super.setSuperInterfaceNames(superInterfaceNames);
        }
    }

    protected Object createElementInfo() {
        ITDAwareSourceTypeElementInfo newInfo;
        try {
            SourceTypeElementInfo origInfo = (SourceTypeElementInfo) delegate.getElementInfo();
            newInfo = new ITDAwareSourceTypeElementInfo();
            newInfo.setFlags(origInfo.getModifiers());
            newInfo.setHandle(this);
            newInfo.setNameSourceEnd(origInfo.getNameSourceEnd());
            newInfo.setNameSourceStart(origInfo.getNameSourceStart());
            newInfo.setSuperclassName(origInfo.getSuperclassName());
            newInfo.setSuperInterfaceNames(origInfo.getInterfaceNames());
            
            if (!itdInitialized) {
                initializeITDs();
            }
            
            IJavaElement[] regularChildren = origInfo.getChildren();
            IJavaElement[] allChildren = new IJavaElement[regularChildren.length + itdMethods.length + itdConstructors.length + itdFields.length];
            System.arraycopy(regularChildren, 0, allChildren, 0, regularChildren.length);
            System.arraycopy(itdMethods, 0, allChildren, regularChildren.length, itdMethods.length);
            System.arraycopy(itdConstructors, 0, allChildren, regularChildren.length + itdMethods.length, itdConstructors.length);
            System.arraycopy(itdFields, 0, allChildren, regularChildren.length + itdMethods.length + itdConstructors.length, itdFields.length);
            newInfo.setChildren(allChildren);
            
            
            return newInfo;
        } catch (JavaModelException e) {
            return new ITDAwareSourceTypeElementInfo();
        }
    }

    public void delete(boolean force, IProgressMonitor monitor)
            throws JavaModelException {

        delegate.delete(force, monitor);
    }

    public ASTNode findNode(CompilationUnit ast) {

        return delegate.findNode(ast);
    }

    protected void generateInfos(Object info, HashMap newElements,
            IProgressMonitor pm) throws JavaModelException {
        // OK to use super class
        super.generateInfos(info, newElements, pm);
    }

    public IAnnotation getAnnotation(String name) {
        return delegate.getAnnotation(name);
    }

    public ICompilationUnit getCompilationUnit() {
        return delegate.getCompilationUnit();
    }

    public IResource getCorrespondingResource() throws JavaModelException {
        return delegate.getCorrespondingResource();
    }

    protected void getHandleMemento(StringBuffer buff) {
        super.getHandleMemento(buff);
        if (this.occurrenceCount > 1) {
            buff.append(JEM_COUNT);
            buff.append(this.occurrenceCount);
        }
    }

    public IJavaElement getHandleUpdatingCountFromMemento(
            MementoTokenizer memento, WorkingCopyOwner owner) {
        return delegate.getHandleUpdatingCountFromMemento(memento, owner);
    }

    public int getOccurrenceCount() {
        return delegate.getOccurrenceCount();
    }

    public IOpenable getOpenableParent() {
        return delegate.getOpenableParent();
    }

    public IPath getPath() {
        return delegate.getPath();
    }

    public String getSource() throws JavaModelException {
        return delegate.getSource();
    }

    public ISourceRange getSourceRange() throws JavaModelException {
        return delegate.getSourceRange();
    }

    public IResource getUnderlyingResource() throws JavaModelException {
        return delegate.getUnderlyingResource();
    }

    public boolean hasChildren() throws JavaModelException {
        return delegate.hasChildren();
    }

    public boolean isStructureKnown() throws JavaModelException {
        return itdInitialized && delegate.isStructureKnown();
    }

    public void move(IJavaElement container, IJavaElement sibling,
            String rename, boolean force, IProgressMonitor monitor)
            throws JavaModelException {
        delegate.move(container, sibling, rename, force, monitor);
    }

    public void rename(String newName, boolean force, IProgressMonitor monitor)
            throws JavaModelException {

        delegate.rename(newName, force, monitor);
    }

    public IResource resource() {
        return delegate.resource();
    }

    protected void toStringName(StringBuffer buffer) {
        super.toStringName(buffer);
        if (this.occurrenceCount > 1) {
            buffer.append("#"); //$NON-NLS-1$
            buffer.append(this.occurrenceCount);
        }
    }

    public void close() throws JavaModelException {
        delegate.close();
    }

    protected void escapeMementoName(StringBuffer buffer, String mementoName) {
        // using the superclass is OK
        super.escapeMementoName(buffer, mementoName);
    }

    public boolean exists() {
        return delegate.exists();
    }

    public IJavaElement getAncestor(int ancestorType) {
        return delegate.getAncestor(ancestorType);
    }

    public String getAttachedJavadoc(IProgressMonitor monitor)
            throws JavaModelException {
        return delegate.getAttachedJavadoc(monitor);
    }

    public IJavaElement[] getChildren() throws JavaModelException {
        Object elementInfo = getElementInfo();
        if (elementInfo instanceof JavaElementInfo) {
            return ((JavaElementInfo)elementInfo).getChildren();
        } else {
            return NO_ELEMENTS;
        }
    }

    public ArrayList getChildrenOfType(int type) throws JavaModelException {
        // OK to use superclass
        return super.getChildrenOfType(type);
    }

    public Object getElementInfo() throws JavaModelException {
        return super.getElementInfo();
    }

    public Object getElementInfo(IProgressMonitor monitor)
            throws JavaModelException {
        return super.getElementInfo(monitor);
    }

    public IJavaElement getHandleFromMemento(MementoTokenizer memento,
            WorkingCopyOwner owner) {
        return delegate.getHandleFromMemento(memento, owner);
    }

    public String getHandleIdentifier() {
        return delegate.getHandleIdentifier();
    }

    public String getHandleMemento() {
        return delegate.getHandleMemento();
    }

    public IJavaModel getJavaModel() {
        return delegate.getJavaModel();
    }

    public IJavaProject getJavaProject() {
        return delegate.getJavaProject();
    }

    protected URL getJavadocBaseLocation() throws JavaModelException {
        return super.getJavadocBaseLocation();
    }

    public IOpenable getOpenable() {
        return delegate.getOpenable();
    }

    public IJavaElement getParent() {
        return delegate.getParent();
    }

    public IJavaElement getPrimaryElement() {
        return delegate.getPrimaryElement();
    }

    public IResource getResource() {
        return delegate.getResource();
    }

    public ISchedulingRule getSchedulingRule() {
        return delegate.getSchedulingRule();
    }

    protected IJavaElement getSourceElementAt(int position)
            throws JavaModelException {
        // OK to use super class
        return super.getSourceElementAt(position);
    }

    public SourceMapper getSourceMapper() {
        return super.getSourceMapper();
    }

    protected String getURLContents(String docUrlValue)
            throws JavaModelException {
        // OK to use super class
        return super.getURLContents(docUrlValue);
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public boolean isAncestorOf(IJavaElement e) {
        return delegate.isAncestorOf(e);
    }

    protected JavaModelStatus newDoesNotExistStatus() {
        // OK to use super class
        return super.newDoesNotExistStatus();
    }

    public JavaModelException newJavaModelException(IStatus status) {
        // OK to use super class
        return super.newJavaModelException(status);
    }

    public JavaModelException newNotPresentException() {
        // OK to use super class
        return super.newNotPresentException();
    }

    protected Object openWhenClosed(Object info, IProgressMonitor monitor)
            throws JavaModelException {
        return super.openWhenClosed(info, monitor);
    }

    protected String tabString(int tab) {
        // OK to use super class
        return super.tabString(tab);
    }

    public String toDebugString() {
        // OK to use super class
        return super.toDebugString();
    }

    public String toString() {
        // XXX
        return delegate.toString();
    }

    protected void toString(int tab, StringBuffer buffer) {
        // OK to use super class
        super.toString(tab, buffer);
    }

    protected void toStringAncestors(StringBuffer buffer) {
        // OK to use super class
        super.toStringAncestors(buffer);
    }

    protected void toStringChildren(int tab, StringBuffer buffer, Object info) {
        // OK to use super class
        super.toStringChildren(tab, buffer, info);
    }

    public Object toStringInfo(int tab, StringBuffer buffer) {

        return delegate.toStringInfo(tab, buffer);
    }

    public String toStringWithAncestors() {
        return delegate.toStringWithAncestors();
    }

    public String toStringWithAncestors(boolean showResolvedInfo) {
        return delegate.toStringWithAncestors(showResolvedInfo);
    }

    public JavaElement unresolved() {
        return delegate.unresolved();
    }

    public Object getAdapter(Class adapter) {
        return delegate.getAdapter(adapter);
    }
}
