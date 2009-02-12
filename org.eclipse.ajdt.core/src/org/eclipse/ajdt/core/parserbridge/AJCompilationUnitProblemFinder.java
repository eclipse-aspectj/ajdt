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

import org.aspectj.ajdt.internal.compiler.ast.AdviceDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.DeclareDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.asm.IProgramElement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.codeconversion.ITDAwareLookupEnvironment;
import org.eclipse.ajdt.core.codeconversion.ITDAwareNameEnvironment;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitInfo;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.AspectJMemberElement;
import org.eclipse.ajdt.core.javaelements.DeclareElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.internal.core.ras.NoFFDC;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.ajdt.internal.core.parserbridge.IAspectSourceElementRequestor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor.FieldInfo;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor.TypeInfo;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor.TypeParameterInfo;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.AnnotatableInfo;
import org.eclipse.jdt.internal.core.Annotation;
import org.eclipse.jdt.internal.core.CancelableNameEnvironment;
import org.eclipse.jdt.internal.core.CancelableProblemFactory;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.CompilationUnitProblemFinder;
import org.eclipse.jdt.internal.core.ImportContainer;
import org.eclipse.jdt.internal.core.ImportDeclaration;
import org.eclipse.jdt.internal.core.Initializer;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.MemberValuePair;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.PackageDeclaration;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceRefElement;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.TypeParameter;
import org.eclipse.jdt.internal.core.util.CommentRecorderParser;
import org.eclipse.jdt.internal.core.util.MethodInfo;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Problem finder for AspectJ problems
 * Mostly copied from CompilationUnitProblemFinder
 * Changes Marked "// AspectJ Change"
 * 
 * Responsible for resolving types inside a compilation unit being reconciled,
 * reporting the discovered problems to a given IProblemRequestor.
 */
public class AJCompilationUnitProblemFinder extends
		CompilationUnitProblemFinder implements NoFFDC {
    

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
             try {
            	 Object elementInfo = ((JavaElement) cu).getElementInfo();
                if (elementInfo instanceof AJCompilationUnitInfo) {
            	     AJCompilationUnit ajcu = (AJCompilationUnit) cu;
            	     ajcu.discardOriginalContentMode();
            		 this.parser = new AJSourceElementParser2(
            				 new NullRequestor(), new DefaultProblemFactory(), getCompilerOptions(cu), true /* parse local declarations */,false);
            		 ajcu.requestOriginalContentMode();
            	 } else {
            	     // use a SourceElementParser to ensure that local declarations are parsed even when diet
                     this.parser = new SourceElementParser(new NullRequestor(), new DefaultProblemFactory(),  
                             getCompilerOptions(cu), true, false);
//                   this.parser =  new NonDietParser(this.problemReporter, this.options.parseLiteralExpressionsAsConstants);
//                   this.parser =  new CommentRecorderParser(this.problemReporter, this.options.parseLiteralExpressionsAsConstants);
            	 }
                
    		} catch (JavaModelException e) {
    		}
	    }
	    // AspectJ Change End
	}

    private CompilerOptions getCompilerOptions(IJavaElement elt) {
        IJavaProject project = elt.getJavaProject();
        Map options = project == null ? JavaCore.getOptions() : project.getOptions(true);
        return new CompilerOptions(options);
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
	
	public static CompilationUnitDeclaration processAJ( // AspectJ Change
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
            
            // AspectJ Change begin
            // use an ITDAware environment to ensure that ITDs are included for source types
            environment = new ITDAwareNameEnvironment(project,
                    workingCopyOwner, monitor);  
            // AspectJ Change end

            problemFactory = new CancelableProblemFactory(monitor);
            problemFinder = new AJCompilationUnitProblemFinder( // AspectJ Change
                    environment,
                    getHandlingPolicy(),
                    getCompilerOptions(project.getOptions(true), creatingAST, ((reconcileFlags & ICompilationUnit.ENABLE_STATEMENTS_RECOVERY) != 0)),
                    getRequestor(), problemFactory, unitElement);
            CompilationUnitDeclaration unit = null;
            
            // AspectJ Change begin 
            // the parser should be a SourceElementParser or AJSourceElementParser2.
            // this ensures that a diet parse can be done, while at the same time
            // all declarations be reported
            if (parser != null) {
                problemFinder.parser = parser;
            }
            try {
                if (problemFinder.parser instanceof SourceElementParser) {
                    unit = ((SourceElementParser) problemFinder.parser).parseCompilationUnit(
                            unitElement, true/* full parse */, monitor);
                    problemFinder.resolve(unit, unitElement,
                            true, // verify methods
                            true, // analyze code
                            true); // generate code
                } else if (problemFinder.parser instanceof AJSourceElementParser2) {
                    unit = ((AJSourceElementParser2) problemFinder.parser).parseCompilationUnit(
                            unitElement, true/* full parse */, monitor);
                    problemFinder.resolve(unit, unitElement,
                            true, // verify methods
                            true, // analyze code
                            true); // generate code
                } else {
                    unit = problemFinder.resolve(unitElement, 
                            true, // verify methods
                            true, // analyze code
                            true); // generate code
                }

            } catch (AbortCompilation e) {
                problemFinder.handleInternalException(e, unit);
            }
            
            // AspectJ Change begin
            // revert the compilation units that have ITDs in them
            ((ITDAwareLookupEnvironment) problemFinder.lookupEnvironment).revertCompilationUnits();
            // AspectJ Change end
            
            CompilationResult unitResult = unit.compilationResult;
            CategorizedProblem[] unitProblems = unitResult.getProblems();
            int length = unitProblems == null ? 0 : unitProblems.length;
            if (length > 0) {
                // AspectJ Change begin
                // filter out spurious problems
                CategorizedProblem[] categorizedProblems = new CategorizedProblem[length];
                System.arraycopy(unitProblems, 0, categorizedProblems, 0,
                        length);
                categorizedProblems = removeAJNonProblems(categorizedProblems, unitElement);
                if (categorizedProblems.length > 0) {
                    problems.put(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
                            categorizedProblems);
                }
                // AspectJ Change end
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

	
	// AspectJ Change to the end---removes spurious problems
	
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

        List newProblems = new LinkedList();
        for (int i = 0; i < categorizedProblems.length; i++) {
            // determine if this problem should be filtered
            if (isARealProblem(categorizedProblems[i], unit, model, hasModel)) {
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
            CategorizedProblem categorizedProblem, CompilationUnit unit, AJProjectModelFacade model, boolean hasModel) {
        
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
             id == IProblem.IllegalCast ||
             id == IProblem.AbstractMethodMustBeImplemented)  // anonymous interface with ITDs implementing abstract method
             ) {
            // if there is no model, don't take any chances.
            // everything that might be an ITD reference is ignored
            return false;
        }
        
        if (categorizedProblem.getSourceStart() == 0) {
            // a place for all problems that don't have source locations
            // because they come from ITDs
            return false;
        }
        
        if(numArgs > 0 && 
                id == IProblem.UndefinedMethod &&
                (extraAspectMethods.contains(firstArg)) || extraAspectMethods.contains(secondArg)) {
            // probably hasAspect or aspectOf
            return false;
        }
                
        if (numArgs > 1 &&
                (id == IProblem.DuplicateField ||
                 id == IProblem.DuplicateMethod) &&
                (aspectMemberNames.contains(firstArg) ||
                 aspectMemberNames.contains(secondArg))) {
            // declare statement if more than one exist in a file
            // or advice if more than one of the same kind exists in the aspect
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
            if (aspectMemberNames.contains(name)) {
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
            if (aspectMemberNames.contains(problemRegion)) {
                return false;
            }
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
            if (numArgs > 0 && 
                    (id == IProblem.UndefinedName || 
                     id == IProblem.UndefinedField ||
                     id == IProblem.UndefinedMethod ||
                     id == IProblem.UndefinedType ||
                     id == IProblem.UndefinedConstructor)
                    &&
                    insideITD(categorizedProblem, unit)) {
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
            // this problem comes because the bodies of the ITDs inserted by ITDInserter 
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
        
        if ((id == IProblem.NotVisibleConstructor ||
            id == IProblem.NotVisibleField || 
            id == IProblem.NotVisibleMethod ||
            id == IProblem.NotVisibleType) && 
            isPrivilegedAspect(categorizedProblem, unit)) {
        
            // a privileged aspect should be able to see all private/protected members
            return false;
        }
        
        try {
            if (numArgs > 0 && 
                    (id == IProblem.UndefinedMethod ||
                     id == IProblem.UndefinedName) &&
                   (adviceBodyNames.contains(firstArg) || adviceBodyNames.contains(secondArg) ) &&
                   unit.getElementAt(categorizedProblem.getSourceStart()) instanceof AdviceElement) {
                // proceed/thisJoinPoint... statement
                return false;
            }

            if (numArgs == 1 && (
                    id == IProblem.ParsingErrorDeleteToken ||
                    id == IProblem.ParsingErrorDeleteTokens
                    ) &&
                    aspectMemberNames.contains(firstArg) &&
                    insideITD(categorizedProblem, unit)) {
                // the implements or extends clause of a declare statement
                return false;
            }

            if (id == IProblem.ParameterMismatch && 
                    insideITD(categorizedProblem, unit)) {
                // Probably a reference to 'this' inside an ITD
                // compiler thinks 'this' refers to the containing aspect
                // not the target type
                return false;
            }
            
            if (id == IProblem.AbstractMethodInAbstractClass && 
                    insideITD(categorizedProblem, unit)) {
                // an abstract method ITD inside a concrete aspect
                // ITDs are allowed to be abstract if the target
                // type is an abstract class
                return false;
            }
            
            if (id == IProblem.IllegalAbstractModifierCombinationForMethod &&
                    insideITD(categorizedProblem, unit)) {
                // private abstract itd in aspect
                return false;
            }
            
            if (id == IProblem.UnusedPrivateField && 
                    insideITD(categorizedProblem, unit)) {
                // private itd is said to be unused, even if it is really used elsewhere
                return false;
            }

        } catch (JavaModelException e) {
        }
        
        
        // this one is very tricky and rare.
        // there is a abstract method ITD defined on a supertype
        // since this type was altered using AspectConvertingParser, 
        // the implementation of this abstract method is not necessarily there
        if (id == IProblem.AbstractMethodMustBeImplemented && 
                (!hasModel || isAbstractITD(categorizedProblem, model, unit))) {
            return false;
        }
        
        return true;
    }


    private static boolean isAbstractITD(CategorizedProblem categorizedProblem,
            AJProjectModelFacade model, CompilationUnit unit) {
        
        // first arg is the method name
        // then come the method params
        // then the implementing type
        // finally, the this type
        String[] args = categorizedProblem.getArguments();
        if (args.length < 3) {
            return false;
        }
        
        String methodName = args[0];
//        String[] methodParams = new String[args.length-3];
        String implementingTypeName = args[args.length-2];
//        String thisType = args[args.length-1];
        
        try {
            IType type = unit.getJavaProject().findType(implementingTypeName);
            if (type == null) {
                return false;
            }
            
            List itds = model.getRelationshipsForElement(type, AJRelationshipManager.ASPECT_DECLARATIONS);
            for (Iterator itdIter = itds.iterator(); itdIter.hasNext();) {
                AspectJMemberElement ajElt = (AspectJMemberElement) itdIter.next();
                if (ajElt.getElementName().endsWith("." + methodName)) {
                    return true;  // close enough...can also compare args and type variables
                }
            }
        } catch (JavaModelException e) {
        }
        
        
        return false;
    }

    private static boolean isPrivilegedAspect(CategorizedProblem problem, CompilationUnit unit) {
        if (unit instanceof AJCompilationUnit) {
            try {
                IJavaElement elt = unit.getElementAt(problem.getSourceStart());
                IType type = (IType) elt.getAncestor(IJavaElement.TYPE);
                if (type != null && type instanceof AspectElement) {
                    AspectElement aspectType = (AspectElement) type;
                    return aspectType.isPrivileged();
                }
            } catch (JavaModelException e) {
            }
        }
        return false;
    }

    private static boolean insideITD(CategorizedProblem categorizedProblem,
            CompilationUnit unit) throws JavaModelException {
        System.out.println("HHH Test Problem finding");
        IJavaElement elementAt = unit.getElementAt(categorizedProblem.getSourceStart());
        System.out.println("element is: " + elementAt);
        return elementAt instanceof IntertypeElement ||
               elementAt instanceof DeclareElement;
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

    static final Set aspectMemberNames = new HashSet();
    static {
        aspectMemberNames.add("parents");
        aspectMemberNames.add("declare");
        aspectMemberNames.add("after");
        aspectMemberNames.add("around");
        aspectMemberNames.add("before");
        aspectMemberNames.add("soft");
        aspectMemberNames.add("error");
        aspectMemberNames.add("pointcut");
        aspectMemberNames.add("implements");
        aspectMemberNames.add("extends");
        aspectMemberNames.add("privileged");
    }
    
    static final Set adviceBodyNames = new HashSet();
    static {
        adviceBodyNames.add("proceed");
        adviceBodyNames.add("thisJoinPoint");
        adviceBodyNames.add("thisJoinPointStaticPart");
        adviceBodyNames.add("thisEnclosingJoinPointStaticPart");
    }
    
    static final Set extraAspectMethods = new HashSet();
    static {
        extraAspectMethods.add("hasAspect");
        extraAspectMethods.add("aspectOf");
    }

    static final Set declareAnnotationKinds = new HashSet();
    static {
        declareAnnotationKinds.add("constructor");
        declareAnnotationKinds.add("field");
        declareAnnotationKinds.add("method");
        declareAnnotationKinds.add("type");
    }
}


class NullRequestor extends AJCompilationUnitStructureRequestor implements ISourceElementRequestor, IAspectSourceElementRequestor {

    public NullRequestor() {
        super(null, null, null);
    }

    public void acceptImport(int declarationStart, int declarationEnd,
            char[] name, boolean onDemand, int modifiers) {
    }

    public void enterMethod(
            org.eclipse.jdt.internal.compiler.ISourceElementRequestor.MethodInfo mi,
            AbstractMethodDeclaration mdecl) {
    }

    protected void enterType(int declarationStart, int modifiers, char[] name,
            int nameSourceStart, int nameSourceEnd, char[] superclass,
            char[][] superinterfaces, org.eclipse.jdt.internal.compiler.ISourceElementRequestor.TypeParameterInfo[] tpInfo,
            boolean isAspect, boolean isPrivilegedAspect) {
    }

    public void enterType(org.eclipse.jdt.internal.compiler.ISourceElementRequestor.TypeInfo typeInfo, boolean isAspect,
            boolean isPrivilegedAspect) { }

    public void setParser(Parser parser) { }

    public void setSource(char[] source) { }

    protected Annotation createAnnotation(JavaElement parent, String name) { 
        return null;
    }

    protected SourceField createField(JavaElement parent, org.eclipse.jdt.internal.core.util.FieldInfo fieldInfo) { 
        return null;
    }

    protected ImportContainer createImportContainer(ICompilationUnit parent) { 
        return null;
    }

    protected ImportDeclaration createImportDeclaration(ImportContainer parent,
            String name, boolean onDemand) { 
        return null;
    }

    protected Initializer createInitializer(JavaElement parent) { 
        return null;
    }

    protected SourceMethod createMethod(
            JavaElement parent,
            org.eclipse.jdt.internal.compiler.ISourceElementRequestor.MethodInfo methodInfo) {
        return null;
    }

    protected PackageDeclaration createPackageDeclaration(JavaElement parent,
            String name) {
        return null;
    }

    protected SourceType createType(JavaElement parent, org.eclipse.jdt.internal.compiler.ISourceElementRequestor.TypeInfo typeInfo) {
        return null;
    }

    protected TypeParameter createTypeParameter(JavaElement parent, String name) {
        return null;
    }

    protected IAnnotation enterAnnotation(
            org.eclipse.jdt.internal.compiler.ast.Annotation annotation,
            AnnotatableInfo parentInfo, JavaElement parentHandle) {
        return null;
    }

    protected void enterTypeParameter(org.eclipse.jdt.internal.compiler.ISourceElementRequestor.TypeParameterInfo typeParameterInfo) {
    }

    protected void exitMember(int declarationEnd) {
    }

    protected Object getMemberValue(MemberValuePair memberValuePair,
            Expression expression) {
        return null;
    }

    protected IMemberValuePair getMemberValuePair(
            org.eclipse.jdt.internal.compiler.ast.MemberValuePair memberValuePair) {
        return null;
    }

    protected IMemberValuePair[] getMemberValuePairs(
            org.eclipse.jdt.internal.compiler.ast.MemberValuePair[] memberValuePairs) {
        return null;
    }

    protected void resolveDuplicates(SourceRefElement handle) {
    }

    public void acceptAnnotationTypeReference(char[] annotation,
            int sourcePosition) { }

    public void acceptAnnotationTypeReference(char[][] annotation,
            int sourceStart, int sourceEnd) { }

    public void acceptConstructorReference(char[] typeName, int argCount,
            int sourcePosition) { }

    public void acceptFieldReference(char[] fieldName, int sourcePosition) { }

    public void acceptImport(int declarationStart, int declarationEnd,
            char[][] tokens, boolean onDemand, int modifiers) { }

    public void acceptLineSeparatorPositions(int[] positions) { }

    public void acceptMethodReference(char[] methodName, int argCount,
            int sourcePosition) { }

    public void acceptPackage(ImportReference importReference) { }

    public void acceptProblem(CategorizedProblem problem) { }

    public void acceptTypeReference(char[] typeName, int sourcePosition) { }

    public void acceptTypeReference(char[][] typeName, int sourceStart,
            int sourceEnd) { }

    public void acceptUnknownReference(char[] name, int sourcePosition) { }

    public void acceptUnknownReference(char[][] name, int sourceStart,
            int sourceEnd) { }

    public void enterCompilationUnit() { }

    public void enterConstructor(org.eclipse.jdt.internal.core.util.MethodInfo methodInfo) { }

    public void enterField(org.eclipse.jdt.internal.core.util.FieldInfo fieldInfo) { }

    public void enterInitializer(int declarationStart, int modifiers) { }

    public void enterMethod(org.eclipse.jdt.internal.core.util.MethodInfo methodInfo) { }

    public void enterType(org.eclipse.jdt.internal.compiler.ISourceElementRequestor.TypeInfo typeInfo) { }

    public void exitCompilationUnit(int declarationEnd) { }

    public void exitConstructor(int declarationEnd) { }

    public void exitField(int initializationStart, int declarationEnd,
            int declarationSourceEnd) { }

    public void exitInitializer(int declarationEnd) { }

    public void exitMethod(int declarationEnd, Expression defaultValue) { }

    public void exitType(int declarationEnd) { }

    public void enterConstructor(org.aspectj.org.eclipse.jdt.internal.core.util.MethodInfo methodInfo) { }

    public void enterField(org.aspectj.org.eclipse.jdt.internal.core.util.FieldInfo fieldInfo) { }

    public void enterMethod(org.aspectj.org.eclipse.jdt.internal.core.util.MethodInfo methodInfo) { }

    public void enterType(org.aspectj.org.eclipse.jdt.internal.compiler.ISourceElementRequestor.TypeInfo typeInfo) { }

    public void acceptPackage(org.aspectj.org.eclipse.jdt.internal.compiler.ast.ImportReference ir) { }

    public void enterAdvice(int declarationStart, int modifiers,
            char[] returnType, char[] name, int nameSourceStart,
            int nameSourceEnd, char[][] parameterTypes,
            char[][] parameterNames, char[][] exceptionTypes,
            AdviceDeclaration decl) { }

    public void enterDeclare(int declarationStart, int modifiers,
            char[] returnType, char[] name, int nameSourceStart,
            int nameSourceEnd, char[][] parameterTypes,
            char[][] parameterNames, char[][] exceptionTypes,
            DeclareDeclaration decl) { }

    public void enterInterTypeDeclaration(int declarationStart, int modifiers,
            char[] returnType, char[] name, int nameSourceStart,
            int nameSourceEnd, char[][] parameterTypes,
            char[][] parameterNames, char[][] exceptionTypes,
            InterTypeDeclaration decl) { }

    public void enterMethod(
            int declarationStart,
            int modifiers,
            char[] returnType,
            char[] name,
            int nameSourceStart,
            int nameSourceEnd,
            char[][] parameterTypes,
            char[][] parameterNames,
            char[][] exceptionTypes,
            org.aspectj.org.eclipse.jdt.internal.compiler.ISourceElementRequestor.TypeParameterInfo[] typeParameters,
            AbstractMethodDeclaration decl) { }

    public void enterMethod(
            org.aspectj.org.eclipse.jdt.internal.compiler.ISourceElementRequestor.MethodInfo methodInfo,
            AbstractMethodDeclaration decl) { }

    public void enterPointcut(int declarationStart, int modifiers,
            char[] returnType, char[] name, int nameSourceStart,
            int nameSourceEnd, char[][] parameterTypes,
            char[][] parameterNames, char[][] exceptionTypes,
            PointcutDeclaration decl) { }

    public void enterType(
            org.aspectj.org.eclipse.jdt.internal.compiler.ISourceElementRequestor.TypeInfo typeInfo,
            boolean isAspect, boolean isPrivilegedAspect) { }

    public void acceptPackage(int declarationStart, int declarationEnd,
            char[] name) { }

    public void acceptProblem(
            org.aspectj.org.eclipse.jdt.core.compiler.CategorizedProblem problem) { }

    public void enterConstructor(
            org.aspectj.org.eclipse.jdt.internal.compiler.ISourceElementRequestor.MethodInfo methodInfo) { }

    public void enterField(
            org.aspectj.org.eclipse.jdt.internal.compiler.ISourceElementRequestor.FieldInfo fieldInfo) { }

    public void enterMethod(
            org.aspectj.org.eclipse.jdt.internal.compiler.ISourceElementRequestor.MethodInfo methodInfo) { }


    public void exitMethod(int declarationEnd, int defaultValueStart,
            int defaultValueEnd) { }

    public void enterConstructor(
            org.eclipse.jdt.internal.compiler.ISourceElementRequestor.MethodInfo methodInfo) { }

    public void enterField(org.eclipse.jdt.internal.compiler.ISourceElementRequestor.FieldInfo fieldInfo) { }

    public void enterMethod(
            org.eclipse.jdt.internal.compiler.ISourceElementRequestor.MethodInfo methodInfo) { }

    protected SourceField createField(JavaElement parent, org.eclipse.jdt.internal.compiler.ISourceElementRequestor.FieldInfo fieldInfo) {
        return null;
    }
}