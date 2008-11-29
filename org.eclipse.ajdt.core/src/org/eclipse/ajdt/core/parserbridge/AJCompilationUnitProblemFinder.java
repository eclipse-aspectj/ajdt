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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.codeconversion.ITDAwareCancelableNameEnvironment;
import org.eclipse.ajdt.core.codeconversion.ITDAwareLookupEnvironment;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitInfo;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.core.CancelableNameEnvironment;
import org.eclipse.jdt.internal.core.CancelableProblemFactory;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.CompilationUnitProblemFinder;
import org.eclipse.jdt.internal.core.JavaElement;
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

	private CompilationUnit cu; // AspectJ Change

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
			CompilationUnit cu) { // AspectJ Change
		super(environment, policy, compilerOptions, requestor, problemFactory);
		this.cu = cu; // AspectJ Change
		initializeParser();
		
		// begin AspectJ Change
		// the custom lookup environment will insert mock ITD elements
		lookupEnvironment = 
		    new ITDAwareLookupEnvironment(lookupEnvironment, environment);
        // end AspectJ Change
	}

	/* (non-Javadoc)
	 * Use the AspectJ parser
	 * @see org.eclipse.jdt.internal.compiler.Compiler#initializeParser()
	 */
	public void initializeParser() {
		// AspectJ Change Begin
	    if (cu != null) {  // wait until object is initialized to initialize parser
    		 Map options = cu.getJavaProject().getOptions(true);
    		 CompilerOptions compilerOptions = new CompilerOptions(options);
             try {
            	 Object elementInfo = ((JavaElement) cu).getElementInfo();
                if (elementInfo instanceof AJCompilationUnitInfo) {
            	     AJCompilationUnit ajcu = (AJCompilationUnit) cu;
            	     ajcu.discardOriginalContentMode();
            		 this.parser = new AJSourceElementParser2(
            				 new AJCompilationUnitStructureRequestor(cu, (AJCompilationUnitInfo) elementInfo, null), new DefaultProblemFactory(), compilerOptions, this.options.parseLiteralExpressionsAsConstants,false);
            		 ajcu.requestOriginalContentMode();
            	 } else {
            	     this.parser = new CommentRecorderParser(this.problemReporter, this.options.parseLiteralExpressionsAsConstants);
            	 }
    		} catch (JavaModelException e) {
    		}
	    }
	    // AspectJ Change End
	}

	
	protected void internalBeginToCompile(
	        org.eclipse.jdt.internal.compiler.env.ICompilationUnit[] sourceUnits,
	        int maxUnits) {
	    // only insert ITDs for the units we are compiling directly
	    // all others will have ITDs inserted by the ITDAwareCancelableNameEnvironment
	    // don't want to insert ITDs twice.
	    ((ITDAwareLookupEnvironment) lookupEnvironment).setInsertITDs(true);
	    super.internalBeginToCompile(sourceUnits, maxUnits);
        ((ITDAwareLookupEnvironment) lookupEnvironment).setInsertITDs(false);
	}
	
	public static CompilationUnitDeclaration processAJ(
	        CompilationUnit unitElement, // AspectJ Change
	        WorkingCopyOwner workingCopyOwner,
	        HashMap problems,
	        boolean creatingAST,
	        int reconcileFlags,
	        IProgressMonitor monitor)
	        throws JavaModelException {
        return processAJ(unitElement, (AJSourceElementParser2) null/*use default Parser*/, workingCopyOwner, problems, creatingAST, reconcileFlags, monitor);

	}
	
	public static CompilationUnitDeclaration processAJ(
            CompilationUnit unitElement, // AspectJ Change
	        CommentRecorderParser parser, // AspectJ Change
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
            // use an ITDAware environment to ensure that ITDs are included for source types
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
                    if (parser instanceof SourceElementParser) {
                        unit = ((SourceElementParser) parser).parseCompilationUnit(
                                unitElement, true/* full parse */, monitor);
                        problemFinder.resolve(unit, unitElement,
                                true, // verify methods
                                true, // analyze code
                                true); // generate code
                    }
                } catch (AbortCompilation e) {
                    problemFinder.handleInternalException(e, unit);
                }
            } else {
                unit = problemFinder.resolve(unitElement, 
                        true, // verify methods
                        true, // analyze code
                        true); // generate code
            }
            
            // revert the compilation units that have ITDs in them
            ((ITDAwareLookupEnvironment) problemFinder.lookupEnvironment).revertCompilationUnits();
            
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
            CategorizedProblem[] categorizedProblems, CompilationUnit unit) {
	    
	    AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(unit);
	    boolean hasModel = model.hasModel();

	    Set ajIdentifiers = new HashSet();
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
            CategorizedProblem categorizedProblem, Set ajIdentifiers, CompilationUnit unit, boolean hasModel) {
        
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

        if (numArgs > 0 && id == IProblem.UndefinedMethod && 
                ("proceed".equals(firstArg) || "proceed".equals(secondArg))) {
            // proceed statement
            return false;
        }
        
        
        if (numArgs == 1 && id == IProblem.ParsingErrorDeleteToken &&
                validAJNames.contains(firstArg)) {
            // the implements or extends clause of a declare statement
            return false;
        }
        
        if (numArgs == 1 && 
                id == IProblem.ParsingErrorDeleteToken &&
                firstArg.equals("@")) {
            // likely to be declare annotation declaration
            // declare @type, declare @constructor, declare @method, declare @field
            String problemRegion = extractNextJavaIdentifier(unit, categorizedProblem.getSourceEnd());
            if (declareAnnotationKinds.contains(problemRegion)) {
                return false;
            }
        }
        
        if (numArgs == 1 && id == IProblem.UndefinedType && declareAnnotationKinds.contains(firstArg)) {
            // alternate error of declare annotations
            return false;
        }
                
        
        
        if (numArgs == 1 && id == IProblem.UndefinedType && firstArg.equals("declare")) {
            // from a declare declaration
            return false;
        }
        
        if (numArgs == 1 && id == IProblem.UndefinedType && firstArg.equals("pointcut")) {
            // from a pointcut declaration
            return false;
        }
        
        try {
            if (numArgs == 1 && 
                    (id == IProblem.UndefinedName || 
                     id == IProblem.UndefinedField ||
                     id == IProblem.UndefinedMethod ||
                     id == IProblem.UndefinedType ||
                     id == IProblem.UndefinedConstructor)
                    &&
                    unit.getElementAt(categorizedProblem.getSourceStart()) instanceof IntertypeElement) {
                // this is an intertype element inside of an aspect.
                // it is likely that the problem is actually a reference to something added by an ITD
                return false;
            }
        } catch(JavaModelException e) {
        }
        
        if (hasModel && id == IProblem.ShouldReturnValue && 
                categorizedProblem.getSourceStart() == 0 && 
                categorizedProblem.getSourceEnd() == 0) {
            // from an inserted ITD that has already been removed
            // this problem comes because the bodies of the inserted ITDs 
            // are always empty even when there should be a return value
            return false;
        }
        
        if (hasModel && (
                id == IProblem.NotVisibleType ||
                id == IProblem.MethodReducesVisibility
                ) && 
                categorizedProblem.getSourceStart() == 0) {
            // declare parents type that is not visible by current
            // type.  this is fine as long as it is visible
            // in the scope of the declare parents declaration.
            return false;
        }
        
        
        return true;
    }

    private static String extractProblemRegion(
            CategorizedProblem categorizedProblem, CompilationUnit unit) {
        char[] contents = ((org.eclipse.jdt.internal.core.CompilationUnit) unit).getContents();
        StringBuffer sb = new StringBuffer();
        for (int i = categorizedProblem.getSourceStart(); 
                i < categorizedProblem.getSourceEnd()+1 && i < contents.length; i++) {
            sb.append(contents[i]);
        }
        return sb.toString();
    }
    
    private static String extractNextJavaIdentifier(CompilationUnit unit, int start) {
        char[] contents = ((org.eclipse.jdt.internal.core.CompilationUnit) unit).getContents();
        StringBuffer sb = new StringBuffer();
        int next = start;
        while (! Character.isJavaIdentifierStart(contents[next]) &&
                next < contents.length) {
            next++;
        }
        while (Character.isJavaIdentifierPart(contents[next]) &&
                next < contents.length) {
            sb.append(contents[next++]);
        }
        return sb.toString();
    }

    static final Set validAJNames = new HashSet();
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
        validAJNames.add("privileged");
    }
    
    static final Set declareAnnotationKinds = new HashSet();
    static {
        declareAnnotationKinds.add("constructor");
        declareAnnotationKinds.add("field");
        declareAnnotationKinds.add("method");
        declareAnnotationKinds.add("type");
    }
}