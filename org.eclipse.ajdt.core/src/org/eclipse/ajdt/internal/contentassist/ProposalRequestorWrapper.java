/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.contentassist;

import org.eclipse.ajdt.internal.codeconversion.JavaCompatibleBuffer;
import org.eclipse.jdt.core.ICompletionRequestor;
import org.eclipse.jdt.core.compiler.IProblem;

/**
 * Translates code positions from fakeBuffer into realBuffer before
 * passing them on to the wrapped ICompletionRequestor.
 * 
 * A description of how code completion works in AJDT can be found in bug 74419.
 * 
 * @author Luzius Meisser
 */
public class ProposalRequestorWrapper implements ICompletionRequestor {

	ICompletionRequestor wrapped;
	JavaCompatibleBuffer buffer;

	/**
	 * @param wrapped
	 * @param buffer
	 */
	public ProposalRequestorWrapper(ICompletionRequestor wrapped,
			JavaCompatibleBuffer buffer) {
		super();
		this.wrapped = wrapped;
		this.buffer = buffer;
	}
	
	private int trans(int pos){
		return buffer.translatePositionToReal(pos);
	}
	
	public void acceptAnonymousType(char[] superTypePackageName,
			char[] superTypeName, char[][] parameterPackageNames,
			char[][] parameterTypeNames, char[][] parameterNames,
			char[] completionName, int modifiers, int completionStart,
			int completionEnd, int relevance) {
		wrapped.acceptAnonymousType(superTypePackageName, superTypeName,
				parameterPackageNames, parameterTypeNames, parameterNames,
				completionName, modifiers, trans(completionStart), trans(completionEnd),
				relevance);
	}
	public void acceptClass(char[] packageName, char[] className,
			char[] completionName, int modifiers, int completionStart,
			int completionEnd, int relevance) {
		wrapped.acceptClass(packageName, className, completionName, modifiers,
				trans(completionStart), trans(completionEnd), relevance);
	}
	public void acceptError(IProblem error) {
		wrapped.acceptError(error);
	}
	public void acceptField(char[] declaringTypePackageName,
			char[] declaringTypeName, char[] name, char[] typePackageName,
			char[] typeName, char[] completionName, int modifiers,
			int completionStart, int completionEnd, int relevance) {
		wrapped.acceptField(declaringTypePackageName, declaringTypeName, name,
				typePackageName, typeName, completionName, modifiers,
				trans(completionStart), trans(completionEnd), relevance);
	}
	public void acceptInterface(char[] packageName, char[] interfaceName,
			char[] completionName, int modifiers, int completionStart,
			int completionEnd, int relevance) {
		wrapped.acceptInterface(packageName, interfaceName, completionName,
				modifiers, trans(completionStart), trans(completionEnd), relevance);
	}
	public void acceptKeyword(char[] keywordName, int completionStart,
			int completionEnd, int relevance) {
		wrapped.acceptKeyword(keywordName, trans(completionStart), trans(completionEnd),
				relevance);
	}
	public void acceptLabel(char[] labelName, int completionStart,
			int completionEnd, int relevance) {
		wrapped.acceptLabel(labelName,trans(completionStart) , trans(completionEnd),
				relevance);
	}
	public void acceptLocalVariable(char[] name, char[] typePackageName,
			char[] typeName, int modifiers, int completionStart,
			int completionEnd, int relevance) {
		wrapped.acceptLocalVariable(name, typePackageName, typeName, modifiers,
				trans(completionStart), trans(completionEnd), relevance);
	}
	public void acceptMethod(char[] declaringTypePackageName,
			char[] declaringTypeName, char[] selector,
			char[][] parameterPackageNames, char[][] parameterTypeNames,
			char[][] parameterNames, char[] returnTypePackageName,
			char[] returnTypeName, char[] completionName, int modifiers,
			int completionStart, int completionEnd, int relevance) {
		wrapped.acceptMethod(declaringTypePackageName, declaringTypeName,
				selector, parameterPackageNames, parameterTypeNames,
				parameterNames, returnTypePackageName, returnTypeName,
				completionName, modifiers, trans(completionStart), trans(completionEnd),
				relevance);
	}
	public void acceptMethodDeclaration(char[] declaringTypePackageName,
			char[] declaringTypeName, char[] selector,
			char[][] parameterPackageNames, char[][] parameterTypeNames,
			char[][] parameterNames, char[] returnTypePackageName,
			char[] returnTypeName, char[] completionName, int modifiers,
			int completionStart, int completionEnd, int relevance) {
		wrapped.acceptMethodDeclaration(declaringTypePackageName,
				declaringTypeName, selector, parameterPackageNames,
				parameterTypeNames, parameterNames, returnTypePackageName,
				returnTypeName, completionName, modifiers, trans(completionStart),
				trans(completionEnd), relevance);
	}
	public void acceptModifier(char[] modifierName, int completionStart,
			int completionEnd, int relevance) {
		wrapped.acceptModifier(modifierName, trans(completionStart), trans(completionEnd),
				relevance);
	}
	public void acceptPackage(char[] packageName, char[] completionName,
			int completionStart, int completionEnd, int relevance) {
		wrapped.acceptPackage(packageName, completionName, trans(completionStart),
				trans(completionEnd), relevance);
	}
	public void acceptType(char[] packageName, char[] typeName,
			char[] completionName, int completionStart, int completionEnd,
			int relevance) {
		wrapped.acceptType(packageName, typeName, completionName,
				trans(completionStart), trans(completionEnd), relevance);
	}
	public void acceptVariableName(char[] typePackageName, char[] typeName,
			char[] name, char[] completionName, int completionStart,
			int completionEnd, int relevance) {
		wrapped.acceptVariableName(typePackageName, typeName, name,
				completionName, trans(completionStart), trans(completionEnd), relevance);
	}
	public boolean equals(Object obj) {
		return wrapped.equals(obj);
	}
	public int hashCode() {
		return wrapped.hashCode();
	}
	public String toString() {
		return wrapped.toString();
	}

}
