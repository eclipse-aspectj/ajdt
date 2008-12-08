/*******************************************************************************
 * Copyright (c) 2008 SpringSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Andrew Eisenberg - Initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.parserbridge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.parser.TypeConverter;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * Inserts ITD information into a TypeDeclaration so that
 * when reconciling occurs introduced methods, fields, and constructors
 * can be found.
 * 
 * This class works by mocking up the ITDs in the type declaration including 
 * altering the type hierarchy.  
 * 
 * This mocking up occurs after parsing, but before binding.  The mocked up elements
 * are then used as part of the type binding.
 * 
 * The mocked up elements are then removed after the compilation is finished because
 * the compilation results are used elsewhere.
 * 
 * This class is a visitor.  It visits a compilation unit and augments all types
 * with ITDs.
 * 
 * See Bug 255848
 * 
 * @author andrew
 * @created Nov 26, 2008
 */
public class ITDInserter extends ASTVisitor {
    
    private class OrigContents {
        AbstractMethodDeclaration[] methods;
        FieldDeclaration[] fields;
        TypeReference superClass;
        TypeReference[] superInterfaces;
    }
    
    class ITDTypeConverter extends TypeConverter {
        public ITDTypeConverter(ProblemReporter reporter) {
            super(reporter, Signature.C_DOT);
        }
        protected TypeReference createTypeReference(char[] typeName) {
            return super.createTypeReference(typeName, 0, typeName.length);
        }
    }

    private final ICompilationUnit unit;

    private Map /*TypeDeclaration -> OrigContents*/ origMap = new HashMap();

    private final ITDTypeConverter typeConverter;
    
    public ITDInserter(ICompilationUnit unit, ProblemReporter reporter) {
        this.unit = unit;
        typeConverter = new ITDTypeConverter(reporter);
    }
    
    public boolean visit(TypeDeclaration type, BlockScope blockScope) {
        return false; // no local/anonymous type
    }
    public boolean visit(TypeDeclaration type, CompilationUnitScope compilationUnitScope) {
        augmentType(type);
        return true;
    }
    public boolean visit(TypeDeclaration memberType, ClassScope classScope) {
        augmentType(memberType);
        return true;
    }
    
    /**
     * augments a type with ITD info
     */
    private void augmentType(TypeDeclaration type) {
        
        OrigContents orig = new OrigContents();
        orig.methods = type.methods;
        orig.fields = type.fields;
        orig.superClass = type.superclass;
        orig.superInterfaces = type.superInterfaces;
        
        try {
            List/*FieldDeclaration*/ itdFields = new LinkedList();
            List/*MethodDeclaration*/ itdMethods = new LinkedList();
            
            List/*IProgramElement*/ ipes = getITDs(type);
            for (Iterator iterator = ipes.iterator(); iterator.hasNext();) {
                IProgramElement elt = (IProgramElement) iterator.next();
                if (elt.getKind() == IProgramElement.Kind.INTER_TYPE_METHOD) {
                    // ignore if type is an interface.
                    // assumption is that this ITD is an implementation of an interface method
                    // adding it here would cause a duplicate method error.
                    // See bug 257437
                    if (TypeDeclaration.kind(type.modifiers) == TypeDeclaration.CLASS_DECL) {
                        itdMethods.add(createMethod(elt, type));
                    }
                } else if (elt.getKind() == IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR) {
                    itdMethods.add(createConstructor(elt, type));
                } else if (elt.getKind() == IProgramElement.Kind.INTER_TYPE_FIELD) {
                    itdFields.add(createField(elt, type));
                } else if (elt.getKind() == IProgramElement.Kind.DECLARE_PARENTS) {
                    String details = elt.getDetails();
                    boolean isExtends = details != null && details.startsWith("extends");
                    if (elt.getParentTypes() != null && elt.getParentTypes().size() > 0) {
                        if (isExtends && TypeDeclaration.kind(type.modifiers) == TypeDeclaration.CLASS_DECL) {
                            addSuperClass(elt, type);
                        } else {
                            addSuperInterfaces(elt, type);
                        }
                    }
                }
            }
            
            if (ipes.size() > 0) {
                origMap.put(type, orig);
                
                // now add the ITDs into the declaration
                if (itdFields.size() > 0) {
                    int numFields = type.fields == null ? 0 : type.fields.length;
                    FieldDeclaration[] fields = new FieldDeclaration[numFields + itdFields.size()];
                    if (numFields > 0) {
                        System.arraycopy(type.fields, 0, fields, 0, numFields);
                    }
                    for (int i = 0; i < itdFields.size(); i++) {
                        fields[i + numFields] = 
                            (FieldDeclaration) itdFields.get(i);
                    }
                    type.fields = fields;
                }    
                if (itdMethods.size() > 0) {
                    int numMethods = type.methods == null ? 0 : type.methods.length;
                    AbstractMethodDeclaration[] methods = new AbstractMethodDeclaration[numMethods + itdMethods.size()];
                    if (numMethods > 0) {
                        System.arraycopy(type.methods, 0, methods, 0, numMethods);
                    }
                    for (int i = 0; i < itdMethods.size(); i++) {
                        methods[i + numMethods] = 
                            (AbstractMethodDeclaration) itdMethods.get(i);
                    }
                    type.methods = methods;
                }
            }
        } catch (Exception e) {
            // back out what we have done
            origMap.remove(type);
            revertType(type, orig);
        }
    }
    
    private FieldDeclaration createField(IProgramElement field, TypeDeclaration type) {
        FieldDeclaration decl = new FieldDeclaration();
        decl.name = field.getName().split("\\.")[1].toCharArray();
        decl.type = createTypeReference(field.getCorrespondingType(true));
        decl.modifiers = field.getRawModifiers();
        return decl;
    }
    
    private MethodDeclaration createMethod(IProgramElement method, TypeDeclaration type) {
        MethodDeclaration decl = new MethodDeclaration(type.compilationResult);
        decl.scope = new MethodScope(type.scope, decl, true);
        
        decl.selector = method.getName().split("\\.")[1].toCharArray();
        
        decl.returnType = createTypeReference(method.getCorrespondingType(true));
        decl.modifiers = method.getRawModifiers();
        Argument[] args = method.getParameterTypes() != null ? 
                new Argument[method.getParameterTypes().size()] :
                    new Argument[0];
        for (int i = 0; i < args.length; i++) {
            args[i] = new Argument(((String) method.getParameterNames().get(i)).toCharArray(),
                    0,
                    createTypeReference(new String((char[]) method.getParameterTypes().get(i))),
                    0);
        }
        decl.arguments = args;
        return decl;
    }
    
    private ConstructorDeclaration createConstructor(IProgramElement constructor, TypeDeclaration type) {
        ConstructorDeclaration decl = new ConstructorDeclaration(type.compilationResult);
        decl.scope = new MethodScope(type.scope, decl, true);
        decl.selector = constructor.getName().split("\\.")[1].toCharArray();
        decl.modifiers = constructor.getRawModifiers();
        Argument[] args = constructor.getParameterTypes() != null ? 
                new Argument[constructor.getParameterTypes().size()] :
                    new Argument[0];
        for (int i = 0; i < args.length; i++) {
            args[i] = new Argument(((String) constructor.getParameterNames().get(i)).toCharArray(),
                    0,
                    createTypeReference(new String((char[]) constructor.getParameterTypes().get(i))),
                    0);
        }
        decl.arguments = args;
        return decl;
    }
    
    private void addSuperClass(IProgramElement ipe, TypeDeclaration decl) {
        String typeName = (String) ipe.getParentTypes().get(0);
        typeName = typeName.replaceAll("\\$", "\\.");
        decl.superclass = createTypeReference(typeName); 
    }
    
    private void addSuperInterfaces(IProgramElement ipe, TypeDeclaration decl) {
        List/*String*/ types = ipe.getParentTypes();
        if (types != null) {
            int index = 0;
            for (Iterator typeIter = types.iterator(); typeIter
                    .hasNext();) {
                String type = (String) typeIter.next();
                type = type.replaceAll("\\$", "\\.");
                types.set(index++, type);
            }
            int superInterfacesNum = decl.superInterfaces == null ? 0 : decl.superInterfaces.length;
            TypeReference[] refs = new TypeReference[superInterfacesNum + types.size()];
            if (superInterfacesNum > 0) {
                System.arraycopy(decl.superInterfaces, 0, refs, 0, decl.superInterfaces.length);
            }
            for (int i = 0; i < refs.length-superInterfacesNum; i++) {
                refs[i + superInterfacesNum] = createTypeReference((String) types.get(i));
            }
            decl.superInterfaces = refs;
        }
    }
    

    // will this get inner types properly???
    private IType getHandle(TypeDeclaration type) {
        try {
            IType[] types = unit.getAllTypes();
            for (int i = 0; i < types.length; i++) {
                if (types[i].getElementName().equals(new String(type.name))) {
                    return types[i];
                }
            }
        } catch (JavaModelException e) {
        }
        return unit.getType(new String(type.name));
    }
    
    private List/*IProgramElement*/ getITDs(TypeDeclaration type) {
        AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(unit);
        if (model.hasModel()) {
            IType handle = getHandle(type);
            if (model.hasProgramElement(handle)) {
                List/*IRelationship*/ rels = model
                        .getRelationshipsForElement(handle,
                                AJRelationshipManager.ASPECT_DECLARATIONS);
                List elts = new ArrayList();
                for (Iterator relIter = rels.iterator(); relIter
                        .hasNext();) {
                    IJavaElement je = (IJavaElement) relIter.next();
                    IProgramElement declareElt = model
                            .javaElementToProgramElement(je);
                    elts.add(declareElt);
                }
                return elts;
            }
        }
        return Collections.EMPTY_LIST;
    }
    
    private TypeReference createTypeReference(String origTypeName) {
        // remove any references to #RAW
        if (origTypeName .endsWith("#RAW")) {
            origTypeName = origTypeName.substring(0, origTypeName.length()-4);
        }
        // can't use the binary name
        origTypeName = origTypeName.replace('$', '.');
        
        return typeConverter.createTypeReference(origTypeName.toCharArray());
    }


    /**
     * replaces type declarations with their original contents after the compilation is
     * complete
     */
    public void revert() {
        for (Iterator origIter = origMap.entrySet().iterator(); origIter.hasNext();) {
            Map.Entry entry = (Map.Entry) origIter.next();
            TypeDeclaration type = (TypeDeclaration) entry.getKey();
            OrigContents orig = (OrigContents) entry.getValue();
            revertType(type, orig);
        }
    }

    private void revertType(TypeDeclaration type, OrigContents orig) {
        type.methods = orig.methods;
        type.fields = orig.fields;
        type.superclass = orig.superClass;
        type.superInterfaces = orig.superInterfaces;
    }
}
