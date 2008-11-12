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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.codeconversion.ITDAwareCancelableNameEnvironment;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitInfo;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.core.CancelableNameEnvironment;
import org.eclipse.jdt.internal.core.CancelableProblemFactory;
import org.eclipse.jdt.internal.core.CompilationUnitProblemFinder;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.util.CommentRecorderParser;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Problem finder for AspectJ problems
 * Mostly copied from CompilationUnitProblemFinder
 * Changes Marked "// AspectJ Change"
 */
public class AJCompilationUnitProblemFinder extends
		CompilationUnitProblemFinder {

	private AJCompilationUnit ajcu; // AspectJ Change

	/**
	 * @param environment
	 * @param policy
	 * @param settings
	 * @param requestor
	 * @param problemFactory
	 */
	public AJCompilationUnitProblemFinder(
	        INameEnvironment environment,
			IErrorHandlingPolicy policy, 
			CompilerOptions compilerOptions,
			ICompilerRequestor requestor, 
			IProblemFactory problemFactory,
			AJCompilationUnit ajcu) { // AspectJ Change
		super(environment, policy, compilerOptions, requestor, problemFactory);
		this.ajcu = ajcu; // AspectJ Change
		initializeParser();
	}

	/* (non-Javadoc)
	 * Use the AspectJ parser
	 * @see org.eclipse.jdt.internal.compiler.Compiler#initializeParser()
	 */
	public void initializeParser() {
		// AspectJ Change Begin
	    if (ajcu != null) {  // wait until object is initialized to initialize parser
    		 Map options = ajcu.getJavaProject().getOptions(true);
    		 CompilerOptions compilerOptions = new CompilerOptions(options);
             try {
            	 if (ajcu.getElementInfo() instanceof AJCompilationUnitInfo) {
            	     ajcu.discardOriginalContentMode();
            		 this.parser = new AJSourceElementParser2(
            				 new AJCompilationUnitStructureRequestor(ajcu, (AJCompilationUnitInfo)ajcu.getElementInfo(), null), new DefaultProblemFactory(), compilerOptions, this.options.parseLiteralExpressionsAsConstants,false);
            		 ajcu.requestOriginalContentMode();
            	 } else {
            	     this.parser = new CommentRecorderParser(this.problemReporter, this.options.parseLiteralExpressionsAsConstants);
            	 }
    		} catch (JavaModelException e) {
    		}
	    }
	    // AspectJ Change End
	}


	public static CompilationUnitDeclaration processAJ(
	        AJCompilationUnit unitElement, // AspectJ Change
	        WorkingCopyOwner workingCopyOwner,
	        HashMap problems,
	        boolean creatingAST,
	        int reconcileFlags,
	        IProgressMonitor monitor)
	        throws JavaModelException {
        return processAJ(unitElement, (AJSourceElementParser2) null/*use default Parser*/, workingCopyOwner, problems, creatingAST, reconcileFlags, monitor);

	}
	
	public static CompilationUnitDeclaration processAJ(
            AJCompilationUnit unitElement, // AspectJ Change
	        AJSourceElementParser2 parser, // AspectJ Change
	        WorkingCopyOwner workingCopyOwner,
	        HashMap problems,
	        boolean creatingAST,
	        int reconcileFlags,
	        IProgressMonitor monitor)
	        throws JavaModelException {
	    
	    
	    JavaProject project = (JavaProject) unitElement.getJavaProject();
        CancelableNameEnvironment environment = null;
        CancelableProblemFactory problemFactory = null;
        AJCompilationUnitProblemFinder problemFinder = null; // AspectJ Change
        try {
            environment = new ITDAwareCancelableNameEnvironment(project,
                    workingCopyOwner, monitor);
            problemFactory = new CancelableProblemFactory(monitor);
            problemFinder = new AJCompilationUnitProblemFinder( // AspectJ Change
                    environment,
                    getHandlingPolicy(),
                    getCompilerOptions(
                            project.getOptions(true),
                            creatingAST,
                            ((reconcileFlags & ICompilationUnit.ENABLE_STATEMENTS_RECOVERY) != 0)),
                    getRequestor(), problemFactory, unitElement);
            CompilationUnitDeclaration unit = null;
            if (parser != null) {
                problemFinder.parser = parser;
                try {
                    unit = parser.parseCompilationUnit(
                            unitElement, true/* full parse */, monitor);
                    problemFinder.resolve(unit, unitElement,
                            true, // verify methods
                            true, // analyze code
                            true); // generate code
                } catch (AbortCompilation e) {
                    problemFinder.handleInternalException(e, unit);
                }
            } else {
                unit = problemFinder.resolve(unitElement, 
                        true, // verify methods
                        true, // analyze code
                        true); // generate code
            }
            CompilationResult unitResult = unit.compilationResult;
            CategorizedProblem[] unitProblems = unitResult.getProblems();
            int length = unitProblems == null ? 0 : unitProblems.length;
            if (length > 0) {
                CategorizedProblem[] categorizedProblems = new CategorizedProblem[length];
                System.arraycopy(unitProblems, 0, categorizedProblems, 0,
                        length);
                categorizedProblems = removeAJNonProblems(categorizedProblems, unitElement);
                if (categorizedProblems.length > 0) {
                    problems.put(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
                            categorizedProblems);
                }
            }
            unitProblems = unitResult.getTasks();
            length = unitProblems == null ? 0 : unitProblems.length;
            if (length > 0) {
                CategorizedProblem[] categorizedProblems = new CategorizedProblem[length];
                System.arraycopy(unitProblems, 0, categorizedProblems, 0,
                        length);
                problems.put(IJavaModelMarker.TASK_MARKER, categorizedProblems);
            }
            if (NameLookup.VERBOSE) {
                AJLog.log(Thread.currentThread()
                        + " TIME SPENT in NameLoopkup#seekTypesInSourcePackage: " + environment.nameLookup.timeSpentInSeekTypesInSourcePackage + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
                AJLog.log(Thread.currentThread()
                        + " TIME SPENT in NameLoopkup#seekTypesInBinaryPackage: " + environment.nameLookup.timeSpentInSeekTypesInBinaryPackage + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return unit;
        } catch (OperationCanceledException e) {
            // catch this exception so as to not enter the
            // catch(RuntimeException e) below
            throw e;
        } catch (RuntimeException e) {
            // avoid breaking other tools due to internal compiler failure
            // (40334)
            String lineDelimiter = unitElement.findRecommendedLineSeparator();
            StringBuffer message = new StringBuffer(
                    "Exception occurred during problem detection:"); //$NON-NLS-1$ 
            message.append(lineDelimiter);
            message.append("----------------------------------- SOURCE BEGIN -------------------------------------"); //$NON-NLS-1$
            message.append(lineDelimiter);
            message.append(unitElement.getSource());
            message.append(lineDelimiter);
            message.append("----------------------------------- SOURCE END -------------------------------------"); //$NON-NLS-1$
            Util.log(e, message.toString());
            throw new JavaModelException(e,
                    IJavaModelStatusConstants.COMPILER_FAILURE);
        } finally {
            if (environment != null)
                environment.monitor = null; // don't hold a reference to this
                                            // external object
            if (problemFactory != null)
                problemFactory.monitor = null; // don't hold a reference to this
                                               // external object
            // NB: unit.cleanUp() is done by caller
            if (problemFinder != null && !creatingAST)
                problemFinder.lookupEnvironment.reset();
        }
	}

	/**
	 * removes all problems that have come from 
	 * valid ITDs
	 * 
	 * Not quite right...this will assume that all ITDs apply to all 
	 * types declared here.  So, it may erroneously remove errors.
	 * 
	 * @param categorizedProblems
	 * @return
	 */
	private static CategorizedProblem[] removeAJNonProblems(
            CategorizedProblem[] categorizedProblems, AJCompilationUnit unit) {
        Set ajIdentifiers = gatherITDsForCU(unit);
        boolean hasModel;
        if (ajIdentifiers == null) {
            // project hasn't had a successful build yet
            hasModel = false;
            ajIdentifiers = new HashSet();
        } else {
            hasModel = true;
        }
        ajIdentifiers.addAll(validAJNames);
        List newProblems = new LinkedList();
        for (int i = 0; i < categorizedProblems.length; i++) {
            // determine if this problem should be filtered
            if (isARealProblem(categorizedProblems[i], ajIdentifiers, unit, hasModel)) {
                newProblems.add(categorizedProblems[i]);
            }
        }
        categorizedProblems = (CategorizedProblem[]) 
                newProblems.toArray(new CategorizedProblem[newProblems.size()]);
        return categorizedProblems;
     }

	// be eger about what we discard.  If unsure
	// it is better to discard.  because the real errors will show up when a compile happens
    private static boolean isARealProblem(
            CategorizedProblem categorizedProblem, Set ajIdentifiers, AJCompilationUnit unit, boolean hasModel) {
        
        int numArgs = categorizedProblem.getArguments() == null ? 
                0 : categorizedProblem.getArguments().length;
        String firstArg = numArgs > 0 ? categorizedProblem.getArguments()[0] : null;
        String secondArg = numArgs > 1 ? categorizedProblem.getArguments()[1] : null;
        
        int id = categorizedProblem.getID();
        
        
        
        if (!hasModel && 
            (id == IProblem.UndefinedType ||
             id == IProblem.UndefinedName ||
             id == IProblem.UndefinedField ||
             id == IProblem.UndefinedMethod ||
             id == IProblem.UndefinedConstructor ||
             id == IProblem.IllegalCast)) {
            // if there is no model, don't take any chances.
            // everything that might be an ITD reference is ignored
            return false;
        }
        
        if ((id == IProblem.UndefinedName ||
             id == IProblem.UndefinedField) &&
                   numArgs > 0 &&
                   ajIdentifiers.contains(firstArg)) {
               // possibly from an ITD
               return false;
           }

        
        if ((id == IProblem.UndefinedType ||
             id == IProblem.UndefinedMethod) &&
                numArgs >= 1 &&
                ajIdentifiers.contains(firstArg) ||
                ajIdentifiers.contains(secondArg)) {
            // possibly from an ITD
            return false;
        }
        
        if (id == IProblem.UndefinedConstructor &&
                numArgs > 0) {
            String[] nameParts = firstArg.split("\\.");
            if (nameParts.length > 0 && 
                    ajIdentifiers.contains(nameParts[nameParts.length-1])) {
                // sometimes the error for an undefined constructor uses 
                // a fully qualified name in the error text.
                return false;
            }
        }
                
                
                
        if (numArgs > 1 &&
                (id == IProblem.DuplicateField ||
                 id == IProblem.DuplicateMethod) &&
                (validAJNames.contains(firstArg) ||
                 validAJNames.contains(secondArg))) {
            // declare statement if more than one exist in a file
            // advice if more than one of the same kind exists in the aspect
            return false;
        }
        
        
        if (numArgs == 0 && 
        		id == IProblem.MissingReturnType) {
            // ITD constructors don't have return types
            // check the name to see if there is a $ in it
            String problemRegion = extractProblemRegion(categorizedProblem, unit);
            if (problemRegion.indexOf("$") != -1) {
                return false;
            }
            String[] parts = problemRegion.split("\\(");
            String name = parts[0].trim();
            if (validAJNames.contains(name)) {
                // advice---before or after
                return false;
            }
        }
                
        if (numArgs == 0 && id == IProblem.InvalidExplicitConstructorCall) {
            // ITD constructor making explicit this() call.
            // lots of potential for false negatives
            return false;
        }
        
        if (numArgs == 0 && id == IProblem.MethodRequiresBody) {
            // Likely to be a pointcut definition
            return false;
        }
        
        if (numArgs == 2 && id == IProblem.ParsingErrorInsertToComplete &&
                firstArg.equals(";") && secondArg.equals("FieldDeclaration")) {
            // might be a declare statement
            String problemRegion = extractProblemRegion(categorizedProblem, unit);
            if (validAJNames.contains(problemRegion)) {
                return false;
            }
        }

        if (numArgs == 1 && id == IProblem.ParsingErrorDeleteToken &&
                validAJNames.contains(firstArg)) {
            // the implements or extends clause of a declare statement
            return false;
        }
        
        try {
            if (numArgs == 1 && id == IProblem.UndefinedName &&
                    unit.getElementAt(categorizedProblem.getSourceStart()) instanceof IntertypeElement) {
                // this is an intertype element inside of an aspect.
                // it is likely that the problem is actually a reference to something added by an ITD
                return false;
            }
        } catch(JavaModelException e) {
        }
        
        return true;
    }

    private static String extractProblemRegion(
            CategorizedProblem categorizedProblem, AJCompilationUnit unit) {
        char[] contents = unit.getContents();
        StringBuffer sb = new StringBuffer();
        for (int i = categorizedProblem.getSourceStart(); 
                i < categorizedProblem.getSourceEnd()+1 && i < contents.length; i++) {
            sb.append(contents[i]);
        }
        return sb.toString();
    }

    
    private static Set gatherITDsForCU(AJCompilationUnit unit) {
        try {
            AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(unit);
            if (model.hasModel()) {
                Set/*String*/ allITDNames = new HashSet();
                IType[] types = unit.getAllTypes();
                for (int i = 0; i < types.length; i++) {
                    if (model.hasProgramElement(types[i])) {
                        List /*IRelationship*/ rels = model.getRelationshipsForElement(types[i], AJRelationshipManager.ASPECT_DECLARATIONS);
                        for (Iterator relIter = rels.iterator(); relIter.hasNext();) {
                            IJavaElement je = (IJavaElement) relIter.next();
                            IProgramElement declareElt = model.javaElementToProgramElement(je);
                            if (declareElt != null && declareElt.getParent() != null && declareElt.getKind().isInterTypeMember()) { // checks to see if this element is valid
                                // should be fully qualified type and simple name
                                int lastDot = declareElt.getName().lastIndexOf('.');
                                String name = declareElt.getName().substring(lastDot+1);
                                allITDNames.add(name);
                            }
                        }
                    } else {
                        // there is a problem with one of the types 
                        // forget the whole thing and assume there is no model
                        return null;
                    }
                    
                }
                return allITDNames;
            }
        } catch (JavaModelException e) {
        }
        return null;
    }
    
    static Set validAJNames = new HashSet();
    static {
        // there will be more...
        validAJNames.add("thisJoinPoint");
        validAJNames.add("thisJoinPointStaticPart");
        validAJNames.add("thisEnclosingJoinPointStaticPart");
        validAJNames.add("parents");
        validAJNames.add("declare");
        validAJNames.add("after");
        validAJNames.add("around");
        validAJNames.add("before");
        validAJNames.add("soft");
        validAJNames.add("error");
        validAJNames.add("pointcut");
        validAJNames.add("implements");
        validAJNames.add("extends");
        validAJNames.add("proceed");
    }
}
 
