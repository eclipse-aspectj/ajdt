/*******************************************************************************
 * Copyright (c) 2005, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contributions for
 *								Bug 463330 - [dom] DOMFinder doesn't find the VariableBinding corresponding to a method argument
 *								Bug 464463 - [dom] DOMFinder doesn't find an ITypeParameter
 *								Bug 429813 - [1.8][dom ast] IMethodBinding#getJavaElement() should return IMethod for lambda
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.internal.core.util;

import org.aspectj.org.eclipse.jdt.core.IInitializer;
import org.aspectj.org.eclipse.jdt.core.ILocalVariable;
import org.aspectj.org.eclipse.jdt.core.IMember;
import org.aspectj.org.eclipse.jdt.core.ISourceRange;
import org.aspectj.org.eclipse.jdt.core.ITypeParameter;
import org.aspectj.org.eclipse.jdt.core.JavaModelException;
import org.aspectj.org.eclipse.jdt.core.dom.ASTNode;
import org.aspectj.org.eclipse.jdt.core.dom.ASTVisitor;
import org.aspectj.org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.aspectj.org.eclipse.jdt.core.dom.CompilationUnit;
import org.aspectj.org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.EnumDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.IBinding;
import org.aspectj.org.eclipse.jdt.core.dom.ImportDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.Initializer;
import org.aspectj.org.eclipse.jdt.core.dom.LambdaExpression;
import org.aspectj.org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.aspectj.org.eclipse.jdt.core.dom.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.ModuleDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.NormalAnnotation;
import org.aspectj.org.eclipse.jdt.core.dom.PackageDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.ParameterizedType;
import org.aspectj.org.eclipse.jdt.core.dom.RecordDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.aspectj.org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.TypeParameter;
import org.aspectj.org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.aspectj.org.eclipse.jdt.internal.core.LambdaMethod;
import org.aspectj.org.eclipse.jdt.internal.core.SourceRefElement;

public class DOMFinder extends ASTVisitor {

	public ASTNode foundNode = null;
	public IBinding foundBinding = null;

	private CompilationUnit ast;
	private SourceRefElement element;
	private boolean resolveBinding;
	private int rangeStart = -1, rangeLength = 0;

	public DOMFinder(CompilationUnit ast, SourceRefElement element, boolean resolveBinding) {
		this.ast = ast;
		this.element = element;
		this.resolveBinding = resolveBinding;
	}

	protected boolean found(ASTNode node, ASTNode name) {
		if (name.getStartPosition() == this.rangeStart && name.getLength() == this.rangeLength) {
			this.foundNode = node;
			return true;
		}
		return false;
	}

	public ASTNode search() throws JavaModelException {
		ISourceRange range = null;
		if (this.element instanceof IMember && !(this.element instanceof IInitializer)
				&& !(this.element instanceof LambdaMethod) && !(this.element instanceof org.aspectj.org.eclipse.jdt.internal.core.LambdaExpression))
			range = ((IMember) this.element).getNameRange();
		else if (this.element instanceof ITypeParameter || this.element instanceof ILocalVariable)
			range = this.element.getNameRange();
		else
			range = this.element.getSourceRange();
		this.rangeStart = range.getOffset();
		this.rangeLength = range.getLength();
		this.ast.accept(this);
		return this.foundNode;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		if (found(node, node.getName()) && this.resolveBinding)
			this.foundBinding = node.resolveBinding();
		return true;
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		if (found(node, node.getName()) && this.resolveBinding)
			this.foundBinding = node.resolveBinding();
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		ASTNode name;
		ASTNode parent = node.getParent();
		switch (parent.getNodeType()) {
			case ASTNode.CLASS_INSTANCE_CREATION:
				name = ((ClassInstanceCreation) parent).getType();
				if (name.getNodeType() == ASTNode.PARAMETERIZED_TYPE) {
					name = ((ParameterizedType) name).getType();
				}
				break;
			case ASTNode.ENUM_CONSTANT_DECLARATION:
				name = ((EnumConstantDeclaration) parent).getName();
				break;
			default:
				return true;
		}
		if (found(node, name) && this.resolveBinding)
			this.foundBinding = node.resolveBinding();
		return true;
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		if (found(node, node.getName()) && this.resolveBinding)
			this.foundBinding = node.resolveVariable();
		return true;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		if (found(node, node.getName()) && this.resolveBinding)
			this.foundBinding = node.resolveBinding();
		return true;
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		if (found(node, node) && this.resolveBinding)
			this.foundBinding = node.resolveBinding();
		return true;
	}

	@Override
	public boolean visit(Initializer node) {
		// note that no binding exists for an Initializer
		found(node, node);
		return true;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		if (found(node, node) && this.resolveBinding)
			this.foundBinding = node.resolveAnnotationBinding();
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (found(node, node.getName()) && this.resolveBinding)
			this.foundBinding = node.resolveBinding();
		return true;
	}

	@Override
	public boolean visit(ModuleDeclaration node) {
		if (found(node, node.getName()) && this.resolveBinding) {
			this.foundBinding = node.resolveBinding();
		}
		return true;
	}
	@Override
	public boolean visit(NormalAnnotation node) {
		if (found(node, node) && this.resolveBinding)
			this.foundBinding = node.resolveAnnotationBinding();
		return true;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		if (found(node, node) && this.resolveBinding)
			this.foundBinding = node.resolveBinding();
		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		if (found(node, node) && this.resolveBinding)
			this.foundBinding = node.resolveAnnotationBinding();
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if (found(node, node.getName()) && this.resolveBinding)
			this.foundBinding = node.resolveBinding();
		return true;
	}

	@Override
	public boolean visit(RecordDeclaration node) {
		if (found(node, node.getName()) && this.resolveBinding)
			this.foundBinding = node.resolveBinding();
		return true;
	}

	@Override
	public boolean visit(TypeParameter node) {
		if (found(node, node.getName()) && this.resolveBinding)
			this.foundBinding = node.resolveBinding();
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		if (found(node, node.getName()) && this.resolveBinding)
			this.foundBinding = node.resolveBinding();
		return true;
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		if (found(node, node.getName()) && this.resolveBinding)
			this.foundBinding = node.resolveBinding();
		return true;
	}

	@Override
	public boolean visit(LambdaExpression node) {
		if (found(node, node) && this.resolveBinding)
			this.foundBinding = node.resolveMethodBinding();
		return true;
	}
}
