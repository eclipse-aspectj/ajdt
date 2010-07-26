/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *     Kris De Volder - Bug 318509 related
 *******************************************************************************/

package org.eclipse.ajdt.core.tests.model;

import java.util.HashMap;

import org.eclipse.contribution.jdt.itdawareness.INameEnvironmentProvider;
import org.eclipse.contribution.jdt.itdawareness.ITDAwarenessAspect;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jface.text.IRegion;

/**
 * @author Andrew Eisenberg
 * @created Jun 6, 2009
 * Tests code selection for ITDs
 * Further testing of ITD hyperlinks.
 * Ensure that hyperlinking works when target aspect is in separate project
 */
public class ITDAwareCodeSelectionTests3 extends AbstractITDAwareCodeSelectionTests {
    
    // need to set a NameEnviromentProvider, since this is typically
    // set by AJDT.UI
    // BAD!!!
    INameEnvironmentProvider origProvider;
    INameEnvironmentProvider mockProvider = new INameEnvironmentProvider() {
    
        public ISourceType transformSourceTypeInfo(ISourceType info) {
            // don't need
            return null;
        }
    
        public boolean shouldFindProblems(CompilationUnit unitElement) {
            return true;
        }
    
        public CompilationUnitDeclaration problemFind(CompilationUnit unitElement,
                SourceElementParser parer, WorkingCopyOwner workingCopyOwner,
                HashMap problems, boolean creatingAST, int reconcileFlags,
                IProgressMonitor monitor) throws JavaModelException {
            // don't need
            return null;
        }
    
        public SearchableEnvironment getNameEnvironment(JavaProject project,
                ICompilationUnit[] workingCopies) {
            // don't need
            return null;
        }
    
        public SearchableEnvironment getNameEnvironment(JavaProject project,
                WorkingCopyOwner owner) {
            // don't need
            return null;
        }
    };

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        origProvider = ITDAwarenessAspect.aspectOf().nameEnvironmentAdapter.getProvider();
        ITDAwarenessAspect.aspectOf().nameEnvironmentAdapter.setProvider(mockProvider);
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        ITDAwarenessAspect.aspectOf().nameEnvironmentAdapter.setProvider(origProvider);
    }

    public void testBug318509MethodAndITDWithSameNumberOfArgs() throws Exception {
    	IProject project = createPredefinedProject("Bug318509MethodAndITDWithSameNumberOfArgs");
    	ICompilationUnit main = getCompilationUnit(project);
    	project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
    	
    	//Tests for methods:
    	validateCodeSelect(main, findRegion(main, "method", 1), "src/MyClass.java", "MyClass.method(int)");
    	validateCodeSelect(main, findRegion(main, "method", 2), "src/MyAspect.aj", "MyAspect.MyClass.method(String)");
    	//Tests for constructors:
    	validateCodeSelect(main, findRegion(main, "MyClass", 2), "src/MyClass.java", "MyClass.MyClass(int)");
    	validateCodeSelect(main, findRegion(main, "MyClass", 3), "src/MyAspect.aj", "MyAspect.MyClass.MyClass_new(String)");
    }

	private ICompilationUnit getCompilationUnit(IProject project) {
		ICompilationUnit main = JavaCore.createCompilationUnitFrom(project.getFile("src/Main.java"));
		return main;
	}
    
	private void validateCodeSelect(ICompilationUnit unit,
            IRegion region, String expectedSrcFile, String expectedSignature) throws Exception {
        IJavaElement[] result = unit.codeSelect(region.getOffset(), region.getLength());
        assertEquals("Should have found exactly one hyperlink", 1, result.length);
        IJavaElement elt = result[0];
        assertTrue("Java element " + elt.getHandleIdentifier() + " should exist", elt.exists());
        String actualSrcFile = elt.getResource().getFullPath().toString();
		assertTrue("Element found is in the wrong source file:\n" +
        		   "   expected: "+expectedSrcFile+"\n" +
        		   "   found:    "+actualSrcFile,
        		actualSrcFile.endsWith(expectedSrcFile));
        assertEquals("Element found has wrong signature",
        		expectedSignature, getSignature(elt));
    }

   	/**
   	 * Convenience method to get a String that can be used to test whether you found what you
   	 * expected to find. It returns a method signature in following format:
   	 * 
   	 *   <DeclaringType>.<methodName>(<ParamType>, ...)
   	 *   
   	 * For ITDs the declaring type will be the Aspect, and the method name will include the
   	 * target type:
   	 * 
   	 *   <AspectType>.<TargetType>.<methodName>(<ParamType>, ...)
   	 * 
   	 * Type names are "simple" (i.e. don't include package name)
   	 * <p>
   	 * The method name for a constructor is the name of the class, for a regular constructor.
   	 * It is <ClassName>.<ClassName_new> for an intertype constructor..
   	 */
    private String getSignature(IJavaElement elt) {
    	if (elt instanceof IMethod) {
    		IMethod method = (IMethod) elt;
    		String sig = method.getDeclaringType().getFullyQualifiedName();
    		sig += "." + method.getElementName() + "(";
    		String[] paramTypeSigs = method.getParameterTypes();
    		for (int i = 0; i < paramTypeSigs.length; i++) {
    			if (i>0) sig+= ", ";
				sig += Signature.getSignatureSimpleName(paramTypeSigs[i]);
			}
    		sig += ")";
    		return sig;
    	}
    	else {
    		throw new Error("Not implemented, if you have tests with fields... implement this :-)");
    	}
    }

}
