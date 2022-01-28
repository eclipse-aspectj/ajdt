/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors: IBM Corporation - initial API and implementation
 * 				 Helen Hawkins   - iniital version
 *******************************************************************/
package org.aspectj.org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * DeclareSoftDeclaration DOM AST node.
 *
 * Has everything a DeclareDeclaration has plus:
 *      a Type called 'type'
 *   	a PointcutDesignator called 'pointcut'
 */
public class DeclareSoftDeclaration extends DeclareDeclaration {

	public static final ChildPropertyDescriptor JAVADOC_PROPERTY =
		internalJavadocPropertyFactory(DeclareSoftDeclaration.class);

	public static final ChildPropertyDescriptor TYPE_PATTERN_PROPERTY =
		new ChildPropertyDescriptor(DeclareSoftDeclaration.class, "typePattern", AbstractTypePattern.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	public static final ChildPropertyDescriptor POINTCUT_PROPERTY =
		new ChildPropertyDescriptor(DeclareSoftDeclaration.class, "pointcut", PointcutDesignator.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$


	private static final List PROPERTY_DESCRIPTORS_2_0;
	private static final List PROPERTY_DESCRIPTORS_3_0;

	static {
		List propertyList = new ArrayList(3);
		createPropertyList(DeclareSoftDeclaration.class, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(POINTCUT_PROPERTY, propertyList);
		addProperty(TYPE_PATTERN_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(propertyList);

		propertyList = new ArrayList(3);
		createPropertyList(DeclareSoftDeclaration.class, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(POINTCUT_PROPERTY, propertyList);
		addProperty(TYPE_PATTERN_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_3_0 = reapPropertyList(propertyList);
	}

	private AbstractTypePattern typePattern;
	private PointcutDesignator pointcut;

	DeclareSoftDeclaration(AST ast) {
		super(ast);
	}

	ASTNode clone0(AST target) {
		DeclareSoftDeclaration result = new DeclareSoftDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		result.setPointcut((PointcutDesignator)ASTNode.copySubtree(target,getPointcut()));
		result.setTypePattern((AbstractTypePattern)ASTNode.copySubtree(target,getTypePattern()));

		return result;
	}

	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return ((AjASTMatcher)matcher).match(this, other);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		if (visitor instanceof AjASTVisitor) {
			boolean visitChildren = ((AjASTVisitor)visitor).visit(this);
			if (visitChildren) {
				// visit children in normal left to right reading order
				acceptChild(visitor, getJavadoc());
				acceptChild(visitor,getPointcut());
				acceptChild(visitor,getTypePattern());
			}
			((AjASTVisitor)visitor).endVisit(this);
		}
	}

	/* (omit javadoc for this method)
	 * Method declared on BodyDeclaration.
	 *
	 * There are no modifiers declared for DeclareErrorDeclaration -
	 * therefore we don't do anything with this
	 */
	SimplePropertyDescriptor internalModifiersProperty() {
		return internalModifiersPropertyFactory(DeclareErrorDeclaration.class);
	}

	/* (omit javadoc for this method)
	 * Method declared on BodyDeclaration.
	 *
	 * There are no modifiers declared for DeclareErrorDeclaration -
	 * therefore we don't do anything with this
	 */
	ChildListPropertyDescriptor internalModifiers2Property() {
		return internalModifiers2PropertyFactory(DeclareErrorDeclaration.class);
	}

	/* (omit javadoc for this method)
	 * Method declared on BodyDeclaration.
	 */
	ChildPropertyDescriptor internalJavadocProperty() {
		return JAVADOC_PROPERTY;
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS&ast;</code> constants
	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		if (apiLevel == AST.JLS2_INTERNAL) {
			return PROPERTY_DESCRIPTORS_2_0;
		} else {
			return PROPERTY_DESCRIPTORS_3_0;
		}
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == JAVADOC_PROPERTY) {
			if (get) {
				return getJavadoc();
			} else {
				setJavadoc((Javadoc) child);
				return null;
			}
		}
		if (property == POINTCUT_PROPERTY) {
			if (get) {
				return getPointcut();
			} else {
				setPointcut((PointcutDesignator) child);
				return null;
			}
		}
		if (property == TYPE_PATTERN_PROPERTY) {
			if (get) {
				return getTypePattern();
			} else {
				setTypePattern((AbstractTypePattern) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	public AbstractTypePattern getTypePattern(){
		return typePattern;
	}

	public void setTypePattern(AbstractTypePattern typePattern) {
		if (typePattern == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.typePattern;
		preReplaceChild(oldChild, typePattern, TYPE_PATTERN_PROPERTY);
		this.typePattern = typePattern;
		postReplaceChild(oldChild, typePattern, TYPE_PATTERN_PROPERTY);
	}

	public PointcutDesignator getPointcut(){
		return pointcut;
	}

	public void setPointcut(PointcutDesignator pointcut) {
		if (pointcut == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.pointcut;
		preReplaceChild(oldChild, pointcut, POINTCUT_PROPERTY);
		this.pointcut = pointcut;
		postReplaceChild(oldChild, pointcut, POINTCUT_PROPERTY);
	}

}
