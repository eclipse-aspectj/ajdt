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
 * This class filters either all completion proposals that depend on
 * the class the code is located in or all the others.
 * 
 * Used for code completion in intertype methods.
 * A description of how code completion works in AJDT can be found in bug 74419.
 * 
 * @author Luzius Meisser
 */
public class ProposalRequestorFilter extends ProposalRequestorWrapper {
	
	boolean acceptMemberMode = false;
	
	private int proposalCounter = 0;
	
	public ProposalRequestorFilter(ICompletionRequestor wrapped,
			JavaCompatibleBuffer buffer) {
		super(wrapped, buffer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ICompletionRequestor#acceptAnonymousType(char[], char[], char[][], char[][], char[][], char[], int, int, int, int)
	 */
	public void acceptAnonymousType(char[] superTypePackageName,
			char[] superTypeName, char[][] parameterPackageNames,
			char[][] parameterTypeNames, char[][] parameterNames,
			char[] completionName, int modifiers, int completionStart,
			int completionEnd, int relevance) {
		if (!acceptMemberMode){proposalCounter++;
			proposalCounter++;
		super.acceptAnonymousType(superTypePackageName, superTypeName,
				parameterPackageNames, parameterTypeNames, parameterNames,
				completionName, modifiers, completionStart, completionEnd,
				relevance);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ICompletionRequestor#acceptClass(char[], char[], char[], int, int, int, int)
	 */
	public void acceptClass(char[] packageName, char[] className,
			char[] completionName, int modifiers, int completionStart,
			int completionEnd, int relevance) {
		if (!acceptMemberMode){proposalCounter++;
		super.acceptClass(packageName, className, completionName, modifiers,
				completionStart, completionEnd, relevance);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ICompletionRequestor#acceptError(org.eclipse.jdt.core.compiler.IProblem)
	 */
	public void acceptError(IProblem error) {
		if (!acceptMemberMode){proposalCounter++;
		super.acceptError(error);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ICompletionRequestor#acceptField(char[], char[], char[], char[], char[], char[], int, int, int, int)
	 */
	public void acceptField(char[] declaringTypePackageName,
			char[] declaringTypeName, char[] name, char[] typePackageName,
			char[] typeName, char[] completionName, int modifiers,
			int completionStart, int completionEnd, int relevance) {
		if (acceptMemberMode){proposalCounter++;
		super.acceptField(declaringTypePackageName, declaringTypeName, name,
				typePackageName, typeName, completionName, modifiers,
				completionStart, completionEnd, relevance);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ICompletionRequestor#acceptInterface(char[], char[], char[], int, int, int, int)
	 */
	public void acceptInterface(char[] packageName, char[] interfaceName,
			char[] completionName, int modifiers, int completionStart,
			int completionEnd, int relevance) {
		if (!acceptMemberMode){proposalCounter++;
		super.acceptInterface(packageName, interfaceName, completionName,
				modifiers, completionStart, completionEnd, relevance);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ICompletionRequestor#acceptKeyword(char[], int, int, int)
	 */
	public void acceptKeyword(char[] keywordName, int completionStart,
			int completionEnd, int relevance) {
		if (!acceptMemberMode){proposalCounter++;
		super.acceptKeyword(keywordName, completionStart, completionEnd,
				relevance);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ICompletionRequestor#acceptLabel(char[], int, int, int)
	 */
	public void acceptLabel(char[] labelName, int completionStart,
			int completionEnd, int relevance) {
		if (!acceptMemberMode){proposalCounter++;
		super.acceptLabel(labelName, completionStart, completionEnd, relevance);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ICompletionRequestor#acceptLocalVariable(char[], char[], char[], int, int, int, int)
	 */
	public void acceptLocalVariable(char[] name, char[] typePackageName,
			char[] typeName, int modifiers, int completionStart,
			int completionEnd, int relevance) {
		if (!acceptMemberMode){proposalCounter++;
		super.acceptLocalVariable(name, typePackageName, typeName, modifiers,
				completionStart, completionEnd, relevance);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ICompletionRequestor#acceptMethod(char[], char[], char[], char[][], char[][], char[][], char[], char[], char[], int, int, int, int)
	 */
	public void acceptMethod(char[] declaringTypePackageName,
			char[] declaringTypeName, char[] selector,
			char[][] parameterPackageNames, char[][] parameterTypeNames,
			char[][] parameterNames, char[] returnTypePackageName,
			char[] returnTypeName, char[] completionName, int modifiers,
			int completionStart, int completionEnd, int relevance) {
		if (acceptMemberMode){proposalCounter++;
		super.acceptMethod(declaringTypePackageName, declaringTypeName,
				selector, parameterPackageNames, parameterTypeNames,
				parameterNames, returnTypePackageName, returnTypeName,
				completionName, modifiers, completionStart, completionEnd,
				relevance);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ICompletionRequestor#acceptMethodDeclaration(char[], char[], char[], char[][], char[][], char[][], char[], char[], char[], int, int, int, int)
	 */
	public void acceptMethodDeclaration(char[] declaringTypePackageName,
			char[] declaringTypeName, char[] selector,
			char[][] parameterPackageNames, char[][] parameterTypeNames,
			char[][] parameterNames, char[] returnTypePackageName,
			char[] returnTypeName, char[] completionName, int modifiers,
			int completionStart, int completionEnd, int relevance) {
		if (!acceptMemberMode){proposalCounter++;
		super.acceptMethodDeclaration(declaringTypePackageName,
				declaringTypeName, selector, parameterPackageNames,
				parameterTypeNames, parameterNames, returnTypePackageName,
				returnTypeName, completionName, modifiers, completionStart,
				completionEnd, relevance);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ICompletionRequestor#acceptModifier(char[], int, int, int)
	 */
	public void acceptModifier(char[] modifierName, int completionStart,
			int completionEnd, int relevance) {
		if (!acceptMemberMode){proposalCounter++;
		super.acceptModifier(modifierName, completionStart, completionEnd,
				relevance);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ICompletionRequestor#acceptPackage(char[], char[], int, int, int)
	 */
	public void acceptPackage(char[] packageName, char[] completionName,
			int completionStart, int completionEnd, int relevance) {
		if (!acceptMemberMode){proposalCounter++;
		super.acceptPackage(packageName, completionName, completionStart,
				completionEnd, relevance);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ICompletionRequestor#acceptType(char[], char[], char[], int, int, int)
	 */
	public void acceptType(char[] packageName, char[] typeName,
			char[] completionName, int completionStart, int completionEnd,
			int relevance) {
		if (!acceptMemberMode){proposalCounter++;
		super.acceptType(packageName, typeName, completionName,
				completionStart, completionEnd, relevance);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ICompletionRequestor#acceptVariableName(char[], char[], char[], char[], int, int, int)
	 */
	public void acceptVariableName(char[] typePackageName, char[] typeName,
			char[] name, char[] completionName, int completionStart,
			int completionEnd, int relevance) {
		if (acceptMemberMode){proposalCounter++;
		super.acceptVariableName(typePackageName, typeName, name,
				completionName, completionStart, completionEnd, relevance);
		}
	}
	/**
	 * @return Returns the filterMembers.
	 */
	public boolean isAcceptMemberMode() {
		return acceptMemberMode;
	}
	/**
	 * @param filterMembers The filterMembers to set.
	 */
	public void setAcceptMemberMode(boolean filterMembers) {
		this.acceptMemberMode = filterMembers;
	}
	public int getProposalCounter() {
		return proposalCounter;
	}
}
