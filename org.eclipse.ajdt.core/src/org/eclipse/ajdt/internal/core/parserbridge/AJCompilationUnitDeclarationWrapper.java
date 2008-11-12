/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 ******************************************************************************/
package org.eclipse.ajdt.internal.core.parserbridge;

import org.aspectj.org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;

/**
 * Wrapper class that extends org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration 
 * and wraps an org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration
 */
public class AJCompilationUnitDeclarationWrapper extends
		CompilationUnitDeclaration {

	
	private org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration delegate;
	private AJCompilationUnit cUnit;

	/**
	 * @param problemReporter
	 * @param compilationResult
	 * @param sourceLength
	 */
	public AJCompilationUnitDeclarationWrapper(org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration delegate, AJCompilationUnit cUnit) {
		super(null, null, 0);
		this.delegate = delegate;
		this.cUnit = cUnit;
	}

	/*
	 *	We cause the compilation task to abort to a given extent.
	 */
	public void abort(int abortLevel, CategorizedProblem problem) {
		delegate.abort(abortLevel, new org.aspectj.org.eclipse.jdt.internal.compiler.problem.DefaultProblem(
				problem.getOriginatingFileName(),
				problem.getMessage(),
				problem.getID(),
				problem.getArguments(),
				problem.isWarning()? ProblemSeverities.Error : ProblemSeverities.Warning,
				problem.getSourceStart(),
				problem.getSourceEnd(),
				problem.getSourceLineNumber(),0/*FudgedIt: need correct column*/));
	}

	/*
	 * Dispatch code analysis AND request saturation of inner emulation
	 */
	public void analyseCode() {
		delegate.analyseCode();
	}

	/*
	 * When unit result is about to be accepted, removed back pointers
	 * to compiler structures.
	 */
	public void cleanUp() {
		delegate.cleanUp();
	}


	public void checkUnusedImports(){
		delegate.checkUnusedImports();
		
	}
	
	public CompilationResult compilationResult() {
		CompilationResult cr = new CompilationResult(cUnit,
				delegate.compilationResult.unitIndex,
				delegate.compilationResult.totalUnitsKnown,
				500);
		cr.lineSeparatorPositions = delegate.compilationResult.lineSeparatorPositions;
		cr.problemCount = delegate.compilationResult.problemCount;
		cr.compiledTypes = delegate.compilationResult.compiledTypes;
		cr.hasBeenAccepted = delegate.compilationResult.hasBeenAccepted;
		cr.qualifiedReferences = delegate.compilationResult.qualifiedReferences;
		cr.simpleNameReferences = delegate.compilationResult.simpleNameReferences;
		if(delegate.compilationResult.problems != null) {
			cr.problems = new CategorizedProblem[delegate.compilationResult.problems.length];
			for (int i = 0; i < delegate.compilationResult.problems.length; i++) {
				org.aspectj.org.eclipse.jdt.core.compiler.IProblem ajprob = delegate.compilationResult.problems[i];
				if(ajprob != null) {
					cr.problems[i] = new DefaultProblem(
							ajprob.getOriginatingFileName(),
							ajprob.getMessage(),
							ajprob.getID(),
							ajprob.getArguments(),
							ajprob.isWarning()? ProblemSeverities.Error : ProblemSeverities.Warning,
							ajprob.getSourceStart(),
							ajprob.getSourceEnd(),
							ajprob.getSourceLineNumber(),
							0); // unknown column
				} 
			}
		} else { 
			cr.problems = new CategorizedProblem[0];
		}
		cr.taskCount = delegate.compilationResult.taskCount;
		return cr;
	}
	
	/*
	 * Finds the matching type amoung this compilation unit types.
	 * Returns null if no type with this name is found.
	 * The type name is a compound name
	 * eg. if we're looking for X.A.B then a type name would be {X, A, B}
	 */
	public TypeDeclaration declarationOfType(char[][] typeName) {
		return new TypeDeclaration(compilationResult());
	}

	/**
	 * Bytecode generation
	 */
	public void generateCode() {
	    delegate.generateCode();
	}

	public char[] getFileName() {
	    return delegate.getFileName();
	}

	public char[] getMainTypeName() {
	    return delegate.getMainTypeName();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public boolean hasErrors() {
		return delegate.hasErrors();
	}

	public StringBuffer print(int indent, StringBuffer output) {
		return delegate.print(indent, output);
	}
	
	/*
	 * Force inner local types to update their innerclass emulation
	 */
	public void propagateInnerEmulationForAllLocalTypes() {
	    delegate.propagateInnerEmulationForAllLocalTypes();
	}

	/*
	 * Keep track of all local types, so as to update their innerclass
	 * emulation later on.
	 */
	public void record(LocalTypeBinding localType) {
	}

	public void resolve() {
		delegate.resolve();
	}

	public void tagAsHavingErrors() {
		delegate.tagAsHavingErrors();
	}

	public void traverse(
		ASTVisitor visitor,
		CompilationUnitScope unitScope) {
		
		if (delegate.ignoreFurtherInvestigation)
			return;
		try {
			if (visitor.visit(this, unitScope)) {
				if (currentPackage != null) {
					currentPackage.traverse(visitor, unitScope);
				}
				if (delegate.imports != null) {
					int importLength = delegate.imports.length;
					for (int i = 0; i < importLength; i++) {
						AJLog.log("AJCompilationUnitDeclarationWrapper - Not traversing import: " + delegate.imports[i]); //$NON-NLS-1$
					}
				}
				if (delegate.types != null) {
					int typesLength = delegate.types.length;
					for (int i = 0; i < typesLength; i++) {
						AJLog.log("AJCompilationUnitDeclarationWrapper - Not traversing type: " + delegate.types[i]); //$NON-NLS-1$
					}
				}
			}
			visitor.endVisit(this, scope);
		} catch (AbortCompilationUnit e) {
			// ignore
		}
	}

	public void reconcileVars() {
		this.compilationResult = compilationResult();
	}
	
}