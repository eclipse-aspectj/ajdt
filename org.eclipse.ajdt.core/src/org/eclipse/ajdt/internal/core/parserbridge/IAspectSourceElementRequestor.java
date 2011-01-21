/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    	IBM Corporation - initial API and implementation
 * 	  	Luzius Meisser - added support for creating AspectJElements
 *******************************************************************************/
package org.eclipse.ajdt.internal.core.parserbridge;

import org.aspectj.ajdt.internal.compiler.ast.AdviceDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.DeclareDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.core.AnnotatableInfo;

/*
 * Part of the source element parser responsible for building the output.
 * It gets notified of structural information as they are detected, relying
 * on the requestor to assemble them together, based on the notifications it got.
 *
 * The structural investigation includes:
 * - package statement
 * - import statements
 * - top-level types: package member, member types (member types of member types...)
 * - fields
 * - methods
 *
 * If reference information is requested, then all source constructs are
 * investigated and type, field & method references are provided as well.
 *
 * Any (parsing) problem encountered is also provided.
 *
 * All positions are relative to the exact source fed to the parser.
 *
 * Elements which are complex are notified in two steps:
 * - enter<Element> : once the element header has been identified
 * - exit<Element> : once the element has been fully consumed
 *
 * other simpler elements (package, import) are read all at once:
 * - accept<Element>
 */
public interface IAspectSourceElementRequestor extends ISourceElementRequestor {

    
    // make the two methods accessible here
    class AJAnnotatableInfo extends AnnotatableInfo {
        @Override
        public void setSourceRangeStart(int start) {
            super.setSourceRangeStart(start);
        }
        @Override
        public void setSourceRangeEnd(int end) {
            super.setSourceRangeEnd(end);
        }
    }
    
    /* AJDT 1.7 begin */
    class AspectTypeInfo extends org.eclipse.jdt.internal.compiler.ISourceElementRequestor.TypeInfo {
        public boolean isAspect;
        public boolean isPrivilegedAspect;
    }
    /* AJDT 1.7 end */

    void enterMethod(
    	int declarationStart,
    	int modifiers,
    	char[] returnType,
    	char[] name,
    	int nameSourceStart,
    	int nameSourceEnd,	
    	char[][] parameterTypes,
    	char[][] parameterNames,
    	char[][] exceptionTypes,
        boolean isConstructor,
        boolean isMethod,
    	TypeParameterInfo[] typeParameters,
    	AbstractMethodDeclaration decl);
    
    void enterMethod(MethodInfo methodInfo,AbstractMethodDeclaration decl);
    
    void enterType(TypeInfo typeInfo,boolean isAspect, boolean isPrivilegedAspect);
    
    void enterAdvice(
    		int declarationStart,
    		int modifiers,
    		char[] returnType,
    		char[] name,
    		int nameSourceStart,
    		int nameSourceEnd,	
    		char[][] parameterTypes,
    		char[][] parameterNames,
    		char[][] exceptionTypes,
    		AdviceDeclaration decl);
    
    void enterPointcut(
    		int declarationStart,
    		int modifiers,
    		char[] returnType,
    		char[] name,
    		int nameSourceStart,
    		int nameSourceEnd,	
    		char[][] parameterTypes,
    		char[][] parameterNames,
    		char[][] exceptionTypes,
    		PointcutDeclaration decl);
    
    void enterDeclare(
    		int declarationStart,
    		int modifiers,
    		char[] returnType,
    		char[] name,
    		int nameSourceStart,
    		int nameSourceEnd,	
    		char[][] parameterTypes,
    		char[][] parameterNames,
    		char[][] exceptionTypes,
    		DeclareDeclaration decl);
    
    void enterInterTypeDeclaration(
    		int declarationStart,
    		int modifiers,
    		char[] returnType,
    		char[] name,
    		int nameSourceStart,
    		int nameSourceEnd,	
    		char[][] parameterTypes,
    		char[][] parameterNames,
    		char[][] exceptionTypes,
    		InterTypeDeclaration decl);
    
    void acceptPackage(org.aspectj.org.eclipse.jdt.internal.compiler.ast.ImportReference ir);
}