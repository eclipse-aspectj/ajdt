/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January - copied to AJDT and adapted to enable AspectJ problems
 *                    to be found
 ******************************************************************************/
package org.eclipse.ajdt.core.parserbridge;

import java.util.Map;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitInfo;
import org.eclipse.ajdt.internal.core.parserbridge.AJCompilationUnitDeclarationWrapper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.core.BasicCompilationUnit;
import org.eclipse.jdt.internal.core.CompilationUnitProblemFinder;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Problem finder for AspectJ problems
 * Mostly copied from CompilationUnitProblemFinder
 * Changes Marked "// AspectJ Change"
 */
public class AJCompilationUnitProblemFinder extends
		CompilationUnitProblemFinder {

	private static AJCompilationUnit ajcu;

	/**
	 * @param environment
	 * @param policy
	 * @param settings
	 * @param requestor
	 * @param problemFactory
	 */
	public AJCompilationUnitProblemFinder(INameEnvironment environment,
			IErrorHandlingPolicy policy, CompilerOptions compilerOptions,
			ICompilerRequestor requestor, IProblemFactory problemFactory, AJCompilationUnit unit) {
		super(environment, policy, compilerOptions, requestor, problemFactory);
		ajcu = unit;
	}

	/* (non-Javadoc)
	 * Use the AspectJ parser
	 * @see org.eclipse.jdt.internal.compiler.Compiler#initializeParser()
	 */
	public void initializeParser() {
		// AspectJ Change Begin
		 Map options = ajcu.getJavaProject().getOptions(true);
		 CompilerOptions compilerOptions = new CompilerOptions(options);
         try {
        	 if(ajcu.getElementInfo() instanceof AJCompilationUnitInfo) {
        		 this.parser = new AJSourceElementParser2(
        				 new AJCompilationUnitStructureRequestor(ajcu, (AJCompilationUnitInfo)ajcu.getElementInfo(), null), new DefaultProblemFactory(), compilerOptions, this.options.parseLiteralExpressionsAsConstants,false);
        	 }	 
		} catch (JavaModelException e) {
		}
		// AspectJ Change End
	}


	public static CompilationUnitDeclaration process(
			ICompilationUnit unitElement, 
			char[] contents,
			WorkingCopyOwner workingCopyOwner,
			IProblemRequestor problemRequestor,
			boolean cleanupCU,
			IProgressMonitor monitor)
			throws JavaModelException {
				
			return process(null/*no CompilationUnitDeclaration*/, unitElement, contents, null/*use default Parser*/, workingCopyOwner, problemRequestor, new DefaultProblemFactory(), cleanupCU, monitor);
		}
	
	public static CompilationUnitDeclaration process(
			CompilationUnitDeclaration unit,
			ICompilationUnit unitElement, 
			char[] contents,
			Parser parser,
			WorkingCopyOwner workingCopyOwner,
			IProblemRequestor problemRequestor,
			IProblemFactory problemFactory,
			boolean cleanupCU,
			IProgressMonitor monitor)
			throws JavaModelException {

			char[] fileName = unitElement.getElementName().toCharArray();
			
			JavaProject project = (JavaProject) unitElement.getJavaProject();
			// AspectJ Change Begin
			AJCompilationUnitProblemFinder.ajcu = (AJCompilationUnit)unitElement;
			CompilationUnitProblemFinder problemFinder =
				new AJCompilationUnitProblemFinder(
					project.newSearchableNameEnvironment(workingCopyOwner),
					getHandlingPolicy(),
					getCompilerOptions(project.getOptions(true), true, true),
					getRequestor(),
					problemFactory,
					(AJCompilationUnit)unitElement);
			// AspectJ Change End
			if (parser != null) {
				problemFinder.parser = parser;
				// commented out See bug 93316
//			} else {
//				return null;
			}

			try {
				
				IPackageFragment packageFragment = (IPackageFragment)unitElement.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
				char[][] expectedPackageName = null;
				if (packageFragment != null){
					expectedPackageName = CharOperation.splitOn('.', packageFragment.getElementName().toCharArray());
				}
				if (unit == null) {
					unit = problemFinder.resolve(
						new BasicCompilationUnit(
							contents,
							expectedPackageName,
							new String(fileName),
							unitElement),
						true, // verify methods
						true, // analyze code
						true); // generate code
				} else {
					// AspectJ Change Begin
					((AJCompilationUnitDeclarationWrapper)unit).scope = new CompilationUnitScope(unit, problemFinder.lookupEnvironment);
					((AJCompilationUnitDeclarationWrapper)unit).reconcileVars();
					// AspectJ Change End
					problemFinder.resolve(
						unit,
						null, // no need for source
						true, // verify methods
						true, // analyze code
						true); // generate code
				}
				// AspectJ Change Begin
				if(unit instanceof AJCompilationUnitDeclarationWrapper) {
				// AspectJ Change End
					reportProblems(unit, problemRequestor, monitor);
				}
				return unit;				
			} catch(RuntimeException e) { 
				// avoid breaking other tools due to internal compiler failure (40334)
				Util.log(e, "Exception occurred during problem detection: "); //$NON-NLS-1$ 
				throw new JavaModelException(e, IJavaModelStatusConstants.COMPILER_FAILURE);
			} finally {
				if (cleanupCU && unit != null) {
					unit.cleanUp();
				}
				problemFinder.lookupEnvironment.reset();			
			}
		}

	private static void reportProblems(CompilationUnitDeclaration unit, IProblemRequestor problemRequestor, IProgressMonitor monitor) {
		CompilationResult unitResult = unit.compilationResult;
		IProblem[] problems = unitResult.getAllProblems();
		for (int i = 0, problemLength = problems == null ? 0 : problems.length; i < problemLength; i++) {
			if (JavaModelManager.VERBOSE){
				System.out.println("PROBLEM FOUND while reconciling : "+problems[i].getMessage());//$NON-NLS-1$
			}
			if (monitor != null && monitor.isCanceled()) break;
			problemRequestor.acceptProblem(problems[i]);				
		}
	}	
}
 
