/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.internal.core.search;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.aspectj.org.eclipse.jdt.core.dom.ASTNode;
import org.aspectj.org.eclipse.jdt.core.dom.AjASTVisitor;
import org.aspectj.org.eclipse.jdt.core.dom.Assignment;
import org.aspectj.org.eclipse.jdt.core.dom.Block;
import org.aspectj.org.eclipse.jdt.core.dom.FieldAccess;
import org.aspectj.org.eclipse.jdt.core.dom.InterTypeMethodDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.MethodInvocation;
import org.aspectj.org.eclipse.jdt.core.dom.MethodRef;
import org.aspectj.org.eclipse.jdt.core.dom.MethodRefParameter;
import org.aspectj.org.eclipse.jdt.core.dom.SimpleName;
import org.aspectj.org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.aspectj.org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.search.FieldReferenceMatch;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.search.matching.FieldPattern;
import org.eclipse.jdt.internal.core.search.matching.MethodPattern;

/**
 * @author Andrew Eisenberg
 * @created Apr 16, 2010
 *
 */
public class ITDReferenceVisitor extends AjASTVisitor {
    
    /**
     * keeps track of currently seen variables that
     * would override direct (ie- non-this) references
     * of ITD variables
     */
    class Scope {
        private Set<String> varNames;
        final Scope parent;
        
        public Scope(Scope parent) {
            this.parent = parent;
            this.varNames = new HashSet<String>();
        }

        void addVariableName(String name) {
            varNames.add(name);
        }
        
        boolean isVarInScope(String name) {
            if (varNames.contains(name)) {
                return true;
            } else {
                return parent != null ? parent.isVarInScope(name) : false;
            }
        }
    }
    
    private Scope currentScope;
    private IntertypeElement itd;
    private FieldPattern fieldPattern;
    private MethodPattern methodPattern;
    private SearchParticipant participant;
    private List<SearchMatch> definitiveMatches;
    
    // these are matches that we know about, but 
    // may have arlready been reported by the requestor
    private List<SearchMatch> tentativeMatches;
    
    public ITDReferenceVisitor(IntertypeElement itd, SearchPattern pattern, SearchParticipant participant) {
        this.itd = itd;
        if (pattern instanceof MethodPattern) {
            this.methodPattern = (MethodPattern) pattern;
        }
        if (pattern instanceof FieldPattern) {
            this.fieldPattern = (FieldPattern) pattern;
        }
        this.definitiveMatches = new LinkedList<SearchMatch>();
        this.tentativeMatches = new LinkedList<SearchMatch>();
        this.participant = participant;
    }
    
    public boolean visit(InterTypeMethodDeclaration node) {
        currentScope = new Scope(null);
        return true;
    }

    @Override
    public boolean visit(MethodRef node) {
        checkMethodPattern(node);
        return false;
    }

    @Override
    public boolean visit(SimpleName node) {
        if (node.getParent() instanceof Type) {
            return false;
        }
        // need to be careful here in that
        // there are many situations where 
        // we don't want to visit this.
        String identifier = node.getIdentifier();
        if (! currentScope.isVarInScope(identifier)) {
            checkFieldPattern(node);
        }
        return false;
    }

    @Override
    public boolean visit(SingleVariableDeclaration node) {
        if (node.getName() != null) {
            currentScope.addVariableName(node.getName().getIdentifier());
        }
        if (node.getInitializer() != null) {
            node.getInitializer().accept(this);
        }
        return false;
    }

    @Override
    public boolean visit(FieldAccess node) {
        if (node.getExpression() != null && 
                node.getExpression().getNodeType() == ASTNode.THIS_EXPRESSION) {
            checkFieldPattern(node.getName());
        }
        return false;
    }
    

    @Override
    public boolean visit(MethodInvocation node) {
        checkMethodPattern(node);
        return super.visit(node);
    }

    @Override
    public boolean visit(VariableDeclarationFragment node) {
        currentScope.addVariableName(node.getName().getIdentifier());
        if (node.getInitializer() != null) {
            node.getInitializer().accept(this);
        }
        return false;
    }
    
    @Override
    public boolean visit(Block node) {
        currentScope = new Scope(currentScope);
        return super.visit(node);
    }
    
    @Override
    public void endVisit(Block node) {
        currentScope = currentScope.parent;
        super.endVisit(node);
    }

    public List<SearchMatch> doVisit(InterTypeMethodDeclaration itdNode) {
        itdNode.accept(this);
        return definitiveMatches;
    }
    
    /**
     * Get matches that we know about, but may have already been reported.
     * Not currently being used...
     */
    public List<SearchMatch> getTentativeMatches() {
        return tentativeMatches;
    }
    
    @SuppressWarnings("unchecked")
    private void checkMethodPattern(MethodRef node) {
        if (methodPattern != null) {
            if (node.getName().equals(String.valueOf(methodPattern.selector))) {
                int i = 0;
                // don't do matching on qualified names
                if (node.parameters().size() == methodPattern.parameterCount) {
                    for (MethodRefParameter param : (Iterable<MethodRefParameter>) node.parameters()) {
                        if (! (param.getName().equals(
                                String.valueOf(methodPattern.parameterSimpleNames[i])))) {
                            return;
                        }
                    }
                }
                definitiveMatches.add(new MethodReferenceMatch(itd, 
                        SearchMatch.A_ACCURATE, 
                        node.getName().getStartPosition(), 
                        node.getName().getLength(), false, false, false, 
                        true, participant, itd.getResource()));
            }
        }
    }

    private void checkMethodPattern(MethodInvocation node) {
        if (methodPattern != null) {
            if (node.getName().getIdentifier().equals(String.valueOf(methodPattern.selector))) {
                if (node.getExpression() == null || node.getExpression().getNodeType() == ASTNode.THIS_EXPRESSION) {
                    // only match on number of parameters
                    if (node.arguments().size() == methodPattern.parameterCount) {
                        // avoid double reporting.  These are already 
                        // reported by standard searching
                        // so, add to tentative matches instead.
                        tentativeMatches.add(new MethodReferenceMatch(itd, 
                                SearchMatch.A_ACCURATE, 
                                node.getStartPosition(), 
                                node.getLength(), false, false, false, 
                                true, participant, itd.getResource()));
                    }
                }
            }
        }
    }

    private void checkFieldPattern(SimpleName name) {
        boolean isWrite = (name.getParent().getNodeType() == ASTNode.ASSIGNMENT && 
                ((Assignment) name.getParent()).getLeftHandSide() == name);

        if (fieldPattern != null && String.valueOf(fieldPattern.getIndexKey()).equals(name.getIdentifier())) {
            definitiveMatches.add(new FieldReferenceMatch(itd, SearchMatch.A_ACCURATE, name.getStartPosition(), name.getLength(), !isWrite, 
                    isWrite, name.getParent().getNodeType() == ASTNode.MEMBER_REF, 
                    participant, itd.getResource()));
        }
    }
}
