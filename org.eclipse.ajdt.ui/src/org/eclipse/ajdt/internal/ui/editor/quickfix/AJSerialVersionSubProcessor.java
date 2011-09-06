/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation, SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.internal.ui.editor.quickfix;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.internal.corext.fix.IProposableFix;
import org.eclipse.jdt.internal.corext.fix.PotentialProgrammingProblemsFix;
import org.eclipse.jdt.internal.corext.fix.SerialVersionDefaultOperation;
import org.eclipse.jdt.internal.ui.text.correction.SerialVersionSubProcessor;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IProblemLocation;


/**
 * @author Andrew Eisenberg
 * @created Dec 27, 2008
 *
 * Copied from {@link org.eclipse.jdt.internal.ui.text.correction.SerialVersionSubProcessor} and
 * {@link org.eclipse.jdt.internal.corext.fix.PotentialProgrammingProblemsFix}
 */
public class AJSerialVersionSubProcessor extends PotentialProgrammingProblemsFix {
    
    protected AJSerialVersionSubProcessor(String name,
            CompilationUnit compilationUnit,
            CompilationUnitRewriteOperation[] fixRewriteOperations) {
        super(name, compilationUnit, fixRewriteOperations);
    }

    /**
     * Determines the serial version quickfix proposals.
     * 
     * from SerialVersionSubProcessor
     *
     * @param context
     *        the invocation context
     * @param location
     *        the problem location
     * @param proposals
     *        the proposal collection to extend
     */
    public static final void getSerialVersionProposals(final IInvocationContext context, final IProblemLocation location, final Collection proposals) {

        Assert.isNotNull(context);
        Assert.isNotNull(location);
        Assert.isNotNull(proposals);

        IProposableFix[] fixes= createMissingSerialVersionFixes(context.getASTRoot(), location);
        if (fixes != null) {
            proposals.add(new SerialVersionSubProcessor.SerialVersionProposal(fixes[0], 9, context, true));
            proposals.add(new SerialVersionSubProcessor.SerialVersionProposal(fixes[1], 9, context, false));
        }
    }
    
    /**
     * from PotentialProgrammingProblemsFix
     */
    public static IProposableFix[] createMissingSerialVersionFixes(CompilationUnit compilationUnit, IProblemLocation problem) {
        if (problem.getProblemId() != IProblem.MissingSerialVersion)
            return null;
        
        final ICompilationUnit unit= (ICompilationUnit)compilationUnit.getJavaElement();
        if (unit == null)
            return null;
        
        final SimpleName simpleName= getSelectedName(compilationUnit, problem);
        if (simpleName == null)
            return null;
        
        ASTNode declaringNode= getDeclarationNode(simpleName);
        if (declaringNode == null)
            return null;
        
        SerialVersionDefaultOperation defop= new SerialVersionDefaultOperation(unit, new ASTNode[] {declaringNode});
        IProposableFix fix1= new AJSerialVersionSubProcessor("Add default serial version ID (AspectJ)", compilationUnit, new CompilationUnitRewriteOperation[] {defop});
        
        AJSerialVersionHashOperation hashop= new AJSerialVersionHashOperation(unit, new ASTNode[] {declaringNode});
        IProposableFix fix2= new AJSerialVersionSubProcessor("Add generated serial version ID (AspectJ)", compilationUnit, new CompilationUnitRewriteOperation[] {hashop});
    
        return new IProposableFix[] {fix1, fix2};
    }

    /**
     * from PotentialProgrammingProblemsFix
     */
    private static SimpleName getSelectedName(CompilationUnit compilationUnit, IProblemLocation problem) {
        final ASTNode selection= problem.getCoveredNode(compilationUnit);
        if (selection == null)
            return null;
        
        Name name= null;
        if (selection instanceof SimpleType) {
            final SimpleType type= (SimpleType) selection;
            name= type.getName();
        } else if (selection instanceof ParameterizedType) {
            final ParameterizedType type= (ParameterizedType) selection;
            final Type raw= type.getType();
            if (raw instanceof SimpleType)
                name= ((SimpleType) raw).getName();
            else if (raw instanceof QualifiedType)
                name= ((QualifiedType) raw).getName();
        } else if (selection instanceof Name) {
            name= (Name) selection;
        }
        if (name == null)
            return null;
        
        if (name.isSimpleName()) {
            return (SimpleName)name;
        } else {
            return ((QualifiedName)name).getName();
        }
    }
    
    /**
     * from PotentialProgrammingProblemsFix
     */
    private static ASTNode getDeclarationNode(SimpleName name) {        
        ASTNode parent= name.getParent();
        if (!(parent instanceof AbstractTypeDeclaration)) {

            parent= parent.getParent();
            if (parent instanceof ParameterizedType || parent instanceof Type)
                parent= parent.getParent();
            if (parent instanceof ClassInstanceCreation) {

                final ClassInstanceCreation creation= (ClassInstanceCreation) parent;
                parent= creation.getAnonymousClassDeclaration();
            }
        }
        return parent;
    }

}
