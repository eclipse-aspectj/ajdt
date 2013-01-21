/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Kris De Volder - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.refactoring;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.tests.refactoring.AbstractAJDTRefactoringTest;
import org.eclipse.ajdt.internal.ui.refactoring.pullout.PullOutRefactoring;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;

/**
 * @author kdvolder
 */
public class PullOutRefactoringTests extends AbstractAJDTRefactoringTest {
	
	/**
	 * To remember positions of a "target marker". 
	 */
	private static class Target {
		public int cu;  // index of a compilation unit
		public int loc; // source location in cu
		public Target(int cu, int loc) {
			super();
			this.cu = cu;
			this.loc = loc;
		}
		@Override
		public String toString() {
			return "("+cu+", "+loc+")";
		}
	}

	private static final String TARGET_MARKER = "<***>";

	private static final String TAB_CHAR = DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR;

	private static final String TAB_SIZE = DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE;
	
	private ICompilationUnit[] units;

	private PullOutRefactoring refactoring;

	private String[] expectedResults;
	
	private static class CU {
		public String packName;
		public String cuName;
		public String initialContents;
		public String expectedContents; // may be null: means it shouldn't change
		public CU(String packName, String cuName, String initialContents,
				String expectedContents) {
			super();
			this.packName = packName;
			this.cuName = cuName;
			this.initialContents = initialContents;
			this.expectedContents = expectedContents;
		}
		public CU(String packName, String cuName, String initialContents) {
			this(packName, cuName, initialContents, null);
		}
	}

	protected void setUp() throws Exception {
		super.setUp();
		setJavaOption(TAB_CHAR, JavaCore.SPACE);
		setJavaOption(TAB_SIZE, ""+4);
	}
		
	private void setJavaOption(String name, String value) {
		project.setOption(name, value);
		assertEquals(value, project.getOption(name, true));
	}

    private RefactoringStatus setupRefactoring(CU... cus) throws CoreException {
    	
    	units = new ICompilationUnit[cus.length];
    	expectedResults = new String[cus.length];
    	refactoring = new PullOutRefactoring();
    	
    	List<Target> targets = new ArrayList<Target>(); 
    	
    	for (int i = 0; i < cus.length; i++) {
    		CU cu = cus[i];
    		int targetLoc = cu.initialContents.indexOf(TARGET_MARKER);
    		while (targetLoc>=0) {
    			cu.initialContents = cu.initialContents.substring(0, targetLoc) 
    							+ cu.initialContents.substring(targetLoc+TARGET_MARKER.length());
    			targets.add(new Target(i,targetLoc));
    			targetLoc = cu.initialContents.indexOf(TARGET_MARKER);
    		}
    		expectedResults[i] = cu.expectedContents;
    		if (expectedResults[i]==null) expectedResults[i] = cu.initialContents;
    		units[i] = createCompilationUnitAndPackage(cu.packName, cu.cuName, cu.initialContents, project);
    	}

    	buildProject(project);

    	RefactoringStatus status = new RefactoringStatus();
    	for (Target targetInfo : targets) {
    		ICompilationUnit unit = units[targetInfo.cu];
			IJavaElement target = unit.getElementAt(targetInfo.loc);
    		if (target instanceof AspectElement) {
        		Assert.assertNull("More than one aspect target in test case", refactoring.getAspect());
    			refactoring.setAspect((AspectElement)target);
    		}
    		else if (target instanceof IMember) {
    			refactoring.addMember((IMember) target, status);
    		}
    		else {
    			throw new Error("Target for refactoring not found");
    		}
    	}
    	return status;
    }

    /**
     * Bug 316945: pull out problems when comments near pulled out member
     */
    public void testPullWeirdComments() throws Exception {
    	setupRefactoring(
    			new CU("", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"public aspect TestAspect {\n" + 
    					"    // public static void main(String[] args) {\n" + 
    					"//    new Clazz().m();\n" + 
    					"// }\n" + 
    					"    public void C.foo() {\n" + 
    					"    }\n" + 
    					"}"
    			),
    			new CU("", "C.java",
    					//////////////////////////////////////////
    					// Initial 
    					"public class C {\n" +
    					"// public static void main(String[] args) {\n"+
    					"//    new Clazz().m();\n"+
    					"// }\n"+
    					"\n"+
    					"    public void <***>foo() {\n" +
    					"\n"+
    					"    }\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"public class C {\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck();
    }

    /**
     * Bug 316945: also test modifier changes for the weird comment case.
     */
    public void testAddModifierWeirdComments() throws Exception {
    	setupRefactoring(
    			new CU("pack", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package pack;\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pack;\n" +
    					"import claszes.C;\n" +
    					"public aspect TestAspect {\n"+
    					"    //Weird\n" +
    					"    //comment\n" +
    					"    public void C.foo() {\n" +
    					"    }\n"+
    					"    //Weird\n" +
    					"    //comment\n" +
    					"    public void C.bar() {\n" +
    					"    }\n"+
    					"}"
    			),
    			new CU("claszes", "C.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package claszes;\n" +
    					"public class C {\n" +
    					"    //Weird\n" +
    					"    //comment\n" +
    					"    void <***>foo() {\n" +
    					"    }\n"+
    					"    //Weird\n" +
    					"    //comment\n" +
    					"    private void <***>bar() {\n" +
    					"    }\n"+
    					"    //I'm using the weird ones!\n" +
    					"    void user() {\n" +
    					"       foo(); bar();\n" +
    					"    }\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package claszes;\n" +
    					"public class C {\n" +
    					"    //I'm using the weird ones!\n" +
    					"    void user() {\n" +
    					"       foo(); bar();\n" +
    					"    }\n" +
    					"}"
    			)
    	);
    	refactoring.setAllowMakePublic(true);
    	performRefactoringAndCheck();
    }
    
    /**
     * Test whether our test scaffolding sets up compilation units and refactoring targets correctly.
     * @throws Exception
     */
    public void testScaffolding() throws Exception {
    	setupRefactoring(
    			new CU("", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"public aspect TestAspect {\n"+
    					"    public void Klass.pullMe() {\n" +
    					"        System.out.println();\n" +
    					"    }\n" +
    					"\n" +
    					"}"
    			),
    			new CU("", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"public class Klass {\n" +
    					"    public void <***>pullMe() {\n" +
    					"        System.out.println();\n" +
    					"    }\n" +
    					"    public void <***>pullMeToo() {\n" +
    					"        System.out.println();\n" +
    					"    }\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"public class Klass {\n" +
    					"}"
    			)
    	);
    	
    	assertEquals("TestAspect", refactoring.getAspect().getElementName());
    	assertEquals("pullMe",     refactoring.getMembers()[0].getElementName());
    	assertEquals("pullMeToo",  refactoring.getMembers()[1].getElementName());
	}
    
    /**
     * Test a simple case that doesn't need to do anything except move the method
     */
    public void testSimple() throws Exception {
    	setupRefactoring(
    			new CU("", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"public aspect TestAspect {\n"+
    					"    public void Klass.pullMe() {\n" +
    					"        System.out.println();\n" +
    					"    }\n" +
    					"\n" +
    					"}"
    			),
    			new CU("", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"public class Klass {\n" +
    					"    public void <***>pullMe() {\n" +
    					"        System.out.println();\n" +
    					"    }\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"public class Klass {\n" +
    					"}"
    			)
    	);
    	
    	performRefactoringAndCheck();
	}

	private void performRefactoringAndCheck() throws Exception {
		RefactoringStatus status = performRefactoring(refactoring, true, true);
    	assertExpectedResults();
    	if (status.isOK()) {
    		// If the refactoring says "ok", we should not have any build errors
    		// after the refactoring.
    		buildProject(project.getJavaProject());
    	}
    	else {
    		fail(status.toString());
    	}
	}

	private void performRefactoringAndCheck(String... message) throws Exception {
		RefactoringStatus status = performRefactoring(refactoring, true, true);
    	if (status.isOK()) {
    		fail("Status should not be ok, problems should be reported with this refactoring");
    	}
    	else {
    		assertNiceMessagesIncluded(status, message);
    		assertNoExcessMessages(status, message);
    	}
    	assertExpectedResults();
	}
	
	private void assertNoExcessMessages(RefactoringStatus status,
			String[] expectedMsgs) {
		String unexpectedMsg = null;
    	for (RefactoringStatusEntry entry : status.getEntries()) {
    		if (!messageIsExpected(entry.getMessage(), expectedMsgs))
    			unexpectedMsg = entry.getMessage();
		}
    	if (unexpectedMsg!=null) {
    		String allMessages = "";
    		for (RefactoringStatusEntry entry : status.getEntries()) {
				allMessages += entry.getMessage()+"\n";
			}
    		fail("Unexpected message: "+unexpectedMsg+"\n"+ 
    			 "All messages: "+allMessages);
    	}
	}

	private boolean messageIsExpected(String message, String[] expectedMsgs) {
		for (String expected : expectedMsgs) {
			if (message.contains(expected)) return true;
		}
		return false;
	}

	private void assertNiceMessagesIncluded(RefactoringStatus status, String... messages) {
		for (String message : messages) {
			assertNiceMessageIncluded(status, message);
		}
	}
	
    private void assertNiceMessageIncluded(RefactoringStatus status, String message) {
    	boolean ok = false;
    	RefactoringStatusEntry[] entries = status.getEntries();
    	for (RefactoringStatusEntry entry : entries) {
			if (entry.getMessage().contains(message)) {
				ok = true;
				assertNotNull("Nice status messages provide context info!\n"+entry, entry.getContext());
			}
		}
    	if (!ok)
    		fail("Expected a status message containing '"+message+"'but found only \n"+status);
	}

	/**
     * When moving from class to apsect in different package should add an import for
     * the class in the aspect.
     */
    public void testDifferentPackage() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    public void Klass.pullMe() {\n" +
    					"        System.out.println();\n" +
    					"    }\n" +
    					"\n" +
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    public void <***>pullMe() {\n" +
    					"        System.out.println();\n" +
    					"    }\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck();
	}

    
    /**
     * When an aspect is privileged no problems with outgoing references should be reported.
     */
    public void testPrivilegedAspect() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public privileged <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public privileged aspect TestAspect {\n"+
    					"    public void Klass.pullMe() {\n" +
    					"        System.out.println(prot+deflt+priv);\n" +
    					"    }\n" +
    					"\n" +
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private int priv;\n" +
    					"    protected int prot;\n" +
    					"    int deflt;\n" +
    					"    public void <***>pullMe() {\n" +
    					"        System.out.println(prot+deflt+priv);\n" +
    					"    }\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private int priv;\n" +
    					"    protected int prot;\n" +
    					"    int deflt;\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck();
    }
    
    /**
     * When the refactoring option, "make aspect priviliged" is enabled, no problems with outgoing references 
     * should be reported.
     */
    public void testMakePrivilegedAspect() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public privileged aspect TestAspect {\n"+
    					"    public void Klass.pullMe() {\n" +
    					"        System.out.println(prot+deflt+priv);\n" +
    					"    }\n" +
    					"\n" +
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private int priv;\n" +
    					"    protected int prot;\n" +
    					"    int deflt;\n" +
    					"    public void <***>pullMe() {\n" +
    					"        System.out.println(prot+deflt+priv);\n" +
    					"    }\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private int priv;\n" +
    					"    protected int prot;\n" +
    					"    int deflt;\n" +
    					"}"
    			)
    	);
    	refactoring.setMakePrivileged(true);
    	performRefactoringAndCheck();
    }
    
    /**
     * Making an already privileged aspect privileged shouldn't cause problems,
     * just leave aspect privileged and not give any warnings about outgoing 
     * references.
     */
    public void testMakePrivilegedPrivileged() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public privileged <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public privileged aspect TestAspect {\n"+
    					"    public void Klass.pullMe() {\n" +
    					"        System.out.println(prot+deflt+priv);\n" +
    					"    }\n" +
    					"\n" +
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private int priv;\n" +
    					"    protected int prot;\n" +
    					"    int deflt;\n" +
    					"    public void <***>pullMe() {\n" +
    					"        System.out.println(prot+deflt+priv);\n" +
    					"    }\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private int priv;\n" +
    					"    protected int prot;\n" +
    					"    int deflt;\n" +
    					"}"
    			)
    	);
    	refactoring.setMakePrivileged(true);
    	performRefactoringAndCheck();
    }
    
    /**
     * When an aspect is privileged no problems with outgoing references should be reported, even 
     * when those references are to nested types.
     * <p>
     * This test is disabled, it is failing because the created import statements for the
     * restricted types are not allowed by the AspectJ compiler, even though actually using
     * those types in the aspect would be allowed. An AspectJ bug has been raised for this,
     * when this "enhancement" is implemented in the aspectj compiler, then this test should
     * also pass.
     */
    public void _testPrivilegedAspectNestedTypes() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public privileged <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"import pclass.Klass.Default;\n" +
    					"import pclass.Klass.Protected;\n" +
    					"import pclass.Klass.Secret;\n" +
    					"\n" +
    					"public privileged aspect TestAspect {\n"+
    					"    public void Klass.pullMe() {\n" +
    					"        Secret s = null;\n" +
    					"        Protected p = null;\n" +
    					"        Default d = null;\n" +
    					"        System.out.println(\"\"+s+p+d+prot+deflt+priv);\n" +
    					"    }\n" +
    					"\n" +
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private class Secret {}\n" +
    					"    protected class Protected {}\n" +
    					"    class Default {}\n" +
    					"    private int priv;\n" +
    					"    protected int prot;\n" +
    					"    int deflt;\n" +
    					"    public void <***>pullMe() {\n" +
    					"        Secret s = null;\n" +
    					"        Protected p = null;\n" +
    					"        Default d = null;\n" +
    					"        System.out.println(\"\"+s+p+d+prot+deflt+priv);\n" +
    					"    }\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private class Secret {}\n" +
    					"    protected class Protected {}\n" +
    					"    class Default {}\n" +
    					"    private int priv;\n" +
    					"    protected int prot;\n" +
    					"    int deflt;\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck();
    }
    
    /**
     * A test with all three different types of imports: static, typebinding and fully qualified name (for
     * declaring type).
     */
    public void testImports() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import static javax.swing.SwingConstants.BOTTOM;\n" +
    					"import java.net.URI;\n" +
    					"import java.net.URISyntaxException;\n" +
    					"import java.net.URL;\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    /**\n"+
    					"     * Pull me and see if my type and static imports get pulled along\n"+
    					"     * with me.\n"+
    					"     * @throws URISyntaxException \n"+
    					"     */\n"+
    					"    public int Klass.pullMyImports(URL url) throws URISyntaxException {\n"+
    					"        URI uri = new URI(url.toString());\n"+
    					"        int stickTo = BOTTOM;\n"+
    					"        return stickTo;\n"+
    					"    }\n"+
    					"\n" +
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"import java.net.URI;\n" +
    					"import java.net.URISyntaxException;\n" +
    					"import java.net.URL;\n" +
    					"\n" +
    					"import static javax.swing.SwingConstants.BOTTOM;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    /**\n"+
    					"     * Pull me and see if my type and static imports get pulled along\n"+
    					"     * with me.\n"+
    					"     * @throws URISyntaxException \n"+
    					"     */\n"+
    					"    public int <***>pullMyImports(URL url) throws URISyntaxException {\n"+
    					"        URI uri = new URI(url.toString());\n"+
    					"        int stickTo = BOTTOM;\n"+
    					"        return stickTo;\n"+
    					"    }\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"import java.net.URI;\n" +
    					"import java.net.URISyntaxException;\n" +
    					"import java.net.URL;\n" +
    					"\n" +
    					"import static javax.swing.SwingConstants.BOTTOM;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck();
	}
    
    /**
     * Does this also work for fields?
     */
    public void testFieldPulling() throws Exception {
    	setupRefactoring(
    			new CU("", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"public privileged aspect TestAspect {\n"+
    					"    private int Klass.field;\n"+
    					"\n" +
    					"}"
    			),
    			new CU("", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"public class Klass {\n" +
    					"    private int <***>field;\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"public class Klass {\n" +
    					"}"
    			)
    	);
    	refactoring.setMakePrivileged(true);
    	performRefactoringAndCheck();
	}
    
    /**
     * References to private fields should give status errors if aspect not privileged.
     */
    public void testPrivateFieldReference() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    public void Klass.pullMe() {\n"+
    					"        System.out.println(\"Are you pulling my leg, \"+this.name+\"?\");\n" +
    					"    }\n"+
    					"\n" +
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private String name;\n" +
    					"\n" +
    					"    public void <***>pullMe() {\n"+
    					"        System.out.println(\"Are you pulling my leg, \"+this.name+\"?\");\n" +
    					"    }\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private String name;\n" +
    					"\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck("private member 'name'");
    }

    /**
     * References to protected and default members should also give status errors if aspect not privileged.
     */
    public void testProtectedAndDefaulReference() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    public void Klass.pullMe() {\n"+
    					"        System.out.println(deflt + prot);\n" +
    					"    }\n"+
    					"\n" +
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    protected String prot;\n" +
    					"    /**deflt*/ String deflt;\n" +
    					"\n" +
    					"    public void <***>pullMe() {\n"+
    					"        System.out.println(deflt + prot);\n" +
    					"    }\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    protected String prot;\n" +
    					"    /**deflt*/ String deflt;\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck(
    			"package restricted member 'deflt'",
    			"protected member 'prot'"
    	);
    }
    
    /**
     * References to private fields should also status errors even when referenced
     * in the simplified syntax (i.e. not <exp>.<field-name> but just <field-name>. 
     */
    public void testPrivateFieldReference2() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    public void Klass.pullMe() {\n"+
    					"        System.out.println(\"Are you pulling my leg, \"+name+\"?\");\n" +
    					"    }\n"+
    					"\n" +
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private String name;\n" +
    					"\n" +
    					"    public void <***>pullMe() {\n"+
    					"        System.out.println(\"Are you pulling my leg, \"+name+\"?\");\n" +
    					"    }\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private String name;\n" +
    					"\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck("private member 'name'");
    }

    /**
     * References to private fields should also status errors even when referenced
     * in the simplified syntax (i.e. not <exp>.<field-name> but just <field-name>. 
     */
    public void testPrivateMethodReference() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    public void Klass.pullMe() {\n"+
    					"        System.out.println(\"Are you pulling my leg, \"+getName()+\"?\");\n" +
    					"    }\n"+
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private String getName() { return \"Kris\"; }\n" +
    					"\n" +
    					"    public void <***>pullMe() {\n"+
    					"        System.out.println(\"Are you pulling my leg, \"+getName()+\"?\");\n" +
    					"    }\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private String getName() { return \"Kris\"; }\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck("private member 'getName'");
    }

    /**
     * References to private types should give status errors.
     */
    public void testSimplePrivateTypeRef() throws Exception {
    	doTestPrivateTypeReference("Secret");
	}

    /**
     * References to private types should give status errors, even
     * when they are "hidden" inside more complex types.
     */
    public void testArrayPrivateTypeRef() throws Exception {
    	doTestPrivateTypeReference("Secret[]");
	}
    
    /**
     * References to private types should give status errors, even
     * when they are "hidden" inside more complex types.
     */
    public void testGenericParamPrivateTypeRef() throws Exception {
    	doTestPrivateTypeReference("List<Secret>");
	}

    /**
     * References to private types should give status errors.
     */
    public void doTestPrivateTypeReference(String typeRef) throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"import java.util.List;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import java.util.List;\n" +
    					"import pclass.Klass;\n" +
    					"import pclass.Klass.Secret;" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    public String Klass.pullMe() {\n"+
    					"        "+typeRef+" xxx = null;\n" +
    					"        return \"\"+xxx;\n" +
    					"    }\n"+
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"import java.util.List;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private static class Secret {}\n" +
    					"\n" +
    					"    public String <***>pullMe() {\n"+
    					"        "+typeRef+" xxx = null;\n" +
    					"        return \"\"+xxx;\n" +
    					"    }\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"import java.util.List;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private static class Secret {}\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck("private member 'Secret'");
    }
    
    public void testGenericTypeParamUsed() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    public E Klass<E>.pullMe(E e) {\n"+
    					"        return e;\n" +
    					"    }\n"+
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass<E> {\n" +
    					"    public E <***>pullMe(E e) {\n"+
    					"        return e;\n" +
    					"    }\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass<E> {\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck();
	}
    
    public void testMultipleGenericTypeParamUsed() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    public E Klass<E, F>.pullMe(E e, F f) {\n"+
    					"        return e;\n" +
    					"    }\n"+
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass<E, F> {\n" +
    					"    public E <***>pullMe(E e, F f) {\n"+
    					"        return e;\n" +
    					"    }\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass<E, F> {\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck();
	}

    /**
     * Do we handle the import of multiple types with the same name ok?
     */
    public void testImportNameCollision() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Idiot;\n" +
    					"import pclass.Klass;\n" +
    					"import pclass.List;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    public void Idiot.pullMeToo(List l, Object e) {\n"+
    					"        l.addIt(e);\n" +
    					"    }\n"+
    					"    public void Klass.pullMe(java.util.List l, Object e) {\n"+
    					"        l.add(e);\n" +
    					"    }\n"+
    					"}"
    			),
    			new CU("pclass", "List.java",
    					////////////////////////////////////////
    					//Initial (unchanged)
    					"package pclass;\n" +
    					"\n" +
    					"/**\n" +
    					" * My own version of List, better than java.util.List!\n" +
    					" */\n" +
    					"public class List {\n" +
    					"    public void addIt(Object e) {}\n"+
    					"}\n"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"import java.util.List;\n"+
    					"\n" +
    					"public class Klass {\n" +
    					"    public void <***>pullMe(List l, Object e) {\n"+
    					"        l.add(e);\n" +
    					"    }\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"import java.util.List;\n"+
    					"\n" +
    					"public class Klass {\n" +
    					"}"
    			),
    			new CU("pclass", "Idiot.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"import pclass.List;\n"+
    					"\n" +
    					"public class Idiot {\n" +
    					"    public void <***>pullMeToo(List l, Object e) {\n"+
    					"        l.addIt(e);\n" +
    					"    }\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"import pclass.List;\n"+
    					"\n" +
    					"public class Idiot {\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck();
    }
    
    /**
     * Do we handle also handle import name clahs ok if it is inside a 
     * declaring type reference for the an ITD?
     */
    public void testImportNameCollisionInDeclaringTypeRef() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"import java.util.List;\n"+
    					"\n"+
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import java.util.List;\n"+
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    void pclass.List.pullMe(Object e) {}\n"+
    					"}"
    			),
    			new CU("pclass", "List.java",
    					////////////////////////////////////////
    					//Initial
    					"package pclass;\n" +
    					"\n" +
    					"public class List {\n" +
    					"    void <***>pullMe(Object e) {}\n"+
    					"}\n",
    					////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class List {\n" +
    					"}\n"
    			)
    	);
    	performRefactoringAndCheck();
    }
    
    /**
     * When we pull a method that refers a private, this is ok if the private is
     * pulled as well!
     * 
     * @throws Exception
     */
    public void testPullMultipleTargetsSimple() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    public int Klass.advertise;\n" +
    					"    public int Klass.pullMe() {\n"+
    					"        return advertise;\n" +
    					"    }\n"+
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    public int <***>advertise;\n" +
    					"    public int <***>pullMe() {\n"+
    					"        return advertise;\n" +
    					"    }\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck();
    }
    
    /**
     * When we pull a method that refers a private, this is ok if the private is
     * pulled as well!
     * 
     * @throws Exception
     */
    public void testPullMultipleTargetsCanPrivate() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    private int Klass.secret;\n" +
    					"    public int Klass.pullMe() {\n"+
    					"        return secret;\n" +
    					"    }\n"+
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private int <***>secret;\n" +
    					"    public int <***>pullMe() {\n"+
    					"        return secret;\n" +
    					"    }\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck();
    }
    
    /**
     * Pulling a private member out of its original context may break code that refers to it (if it
     * remains private in the aspect.
     */
    public void testPullPrivate() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    private int Klass.secret;\n" +
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private int <***>secret;\n" +
    					"    public int pullMe() {\n"+
    					"        return secret;\n" +
    					"    }\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    public int pullMe() {\n"+
    					"        return secret;\n" +
    					"    }\n"+
    					"}"
    			)
    	);
    	performRefactoringAndCheck("moved private member 'secret' will not be accessible");
    }
    
    /**
     * Pulling a protected member should provide warning that ajc doesn't support protected
     * ITDs. 
     */
    public void testPullProtected() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    protected int Klass.secret;\n" +
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    protected int <***>secret;\n" +
    					"    public int pullMe() {\n"+
    					"        return secret;\n" +
    					"    }\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    public int pullMe() {\n"+
    					"        return secret;\n" +
    					"    }\n"+
    					"}"
    			)
    	);
    	performRefactoringAndCheck(
    			"moved member 'secret' is protected",
    			"moved member 'secret' may not be accessible");
    }

    /**
     * When option "drop protected from ITDs" is on, protected should be silently dropped from
     * ITD.
     */
    public void testProtectedDropping() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    Klass.new(int x) {this();}\n" +
    					"    int Klass.prot = 0;\n" +
    					"    int Klass.pullProtected() { return 0; }\n" +
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					/////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    public Klass() {}\n"+
    					"    protected <***>Klass(int x) {this();}\n" +
    					"    protected int <***>prot = 0;\n" +
    					"    protected int <***>pullProtected() { return 0; }\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    public Klass() {}\n"+
    					"}"
    			)
    	);
    	refactoring.setAllowDeleteProtected(true);
    	performRefactoringAndCheck();
    }

    /**
     * When the option "allow modifier conversion" is on, we should not get any warnings/errors
     * related to incoming references.
     */
    public void testPublicizeITDs() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    //Weird doc\n"+
    					"    public Klass.new() {}\n"+
    					"    /**\n" +
    				    "     * JavaDoc ok?\n" +
    				    "     */\n" +
    				    "    public int Klass.docked;\n" +
    					"    public int Klass.secret;\n" +
    					"    public int Klass.getSecret() {return secret;}\n" +
    					"    public int Klass.getSecretPackage() {return secret;}\n" +
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					/////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    //Weird doc\n"+
    					"    <***>Klass() {}\n"+
    					"    /**\n" +
    				    "     * JavaDoc ok?\n" +
    				    "     */\n" +
    				    "    int <***>docked;\n" +
    					"    private int <***>secret;\n" +
    					"    protected int <***>getSecret() {return secret;}\n" +
    					"    int <***>getSecretPackage() {return secret;}\n" +
    					"    int secretUser() {\n" +
    					"        return new Klass().docked + secret + getSecret() + getSecretPackage();\n" +
    					"    }\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    int secretUser() {\n" +
    					"        return new Klass().docked + secret + getSecret() + getSecretPackage();\n" +
    					"    }\n" +
    					"}"
    			)
    	);
    	refactoring.setAllowMakePublic(true);
    	performRefactoringAndCheck("moved 'Klass' constructor has no this() call");
    }
    
    /**
     * When pulling out constructors, in some cases the semantics of the
     * program might change, because the initialisers in the target class
     * will not be executed.
     * 
     * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=318936
     */
    public void testPullConstructorWithThisWarning() throws Exception {
    	setupRefactoring(
    			new CU("", "MyClass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"public class MyClass {\n" +
    					"    private int countdown = 10;\n" +
    					"    private int step;\n" +
    					"    public <***>MyClass(int step) {\n" +
    					"        this.step = step;\n" +
    					"    }\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"public class MyClass {\n" +
    					"    private int countdown = 10;\n" +
    					"    private int step;\n" +
    					"}"
    			),
    			new CU("","TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"public privileged <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"public privileged aspect TestAspect {\n"+
    					"    public MyClass.new(int step) {\n" +
    					"        this.step = step;\n" +
    					"    }\n" +
    					"}"
    			)
    	);
    	refactoring.setAllowDeleteProtected(true);
    	performRefactoringAndCheck(
    			"moved 'MyClass' constructor has no this() call"
    	);
	}
    
    
    /**
     * Pulling a private member out of its original context may break code that refers to it (if it
     * remains private in the aspect.
     */
    public void testPullConstructorWithPrivateWarnings() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    public Klass.new(int hideIt) { this(); secret = hideIt; }\n" +
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private int secret;\n" +
    					"    public Klass() {}\n" +
    					"    public <***>Klass(int hideIt) { this(); secret = hideIt; }\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private int secret;\n" +
    					"    public Klass() {}\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck("private member 'secret'");
    }

    /**
     * Pulling a private member out of its original context may break code that refers to it (if it
     * remains private in the aspect.
     */
    public void testPullConstructor() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public privileged aspect TestAspect {\n"+
    					"    public Klass.new(int hideIt) { this(); secret = hideIt; }\n" +
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private int secret;\n" +
    					"    private Klass() {}\n" +
    					"    public <***>Klass(int hideIt) { this(); secret = hideIt; }\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    private int secret;\n" +
    					"    private Klass() {}\n" +
    					"}"
    			)
    	);
    	refactoring.setMakePrivileged(true);
    	performRefactoringAndCheck();
    }
    
    /**
     * Pulling methods from aspects should also work.
     */
    public void testPullAspectMethod() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.OtherAspect;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    public int OtherAspect.pullMe() { return 0; }\n" +
    					"}"
    			),
    			new CU("pclass", "OtherAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public aspect OtherAspect {\n" +
    					"    public int <***>pullMe() { return 0; }\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public aspect OtherAspect {\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck();
    }
    
    /**
     * When aspect and class are in same package, references to protected / default stuff
     * should not be checked.
     */
    public void testPullSamePackage() throws Exception {
    	setupRefactoring(
    			new CU("samepack", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package samepack;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package samepack;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    int Klass.pullDeflt() { return deflt+prot; }\n" +
    					"}"
    			),
    			new CU("samepack", "Klass.java",
    					/////////////////////////////////////////
    					// Initial 
    					"package samepack;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    int deflt;\n" +
    					"    protected int prot;\n" +
    					"    int stayHere() { return pullDeflt(); }\n" +
    					"    int <***>pullDeflt() { return deflt+prot; }\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package samepack;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    int deflt;\n" +
    					"    protected int prot;\n" +
    					"    int stayHere() { return pullDeflt(); }\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck();
    }
    
    /**
     * When aspect and class are in same compilation unit, refactoring should not break.
     */
    public void testPullSameCU() throws Exception {
    	setupRefactoring(
    			new CU("samepack", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package samepack;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}\n" +
    					"class Klass {\n" +
    					"    int deflt;\n" +
    					"    protected int prot;\n" +
    					"    int <***>pullDeflt() { return deflt+prot; }\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package samepack;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    int Klass.pullDeflt() { return deflt+prot; }\n" +
    					"}\n" +
    					"class Klass {\n" +
    					"    int deflt;\n" +
    					"    protected int prot;\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck();
    }
    
    /**
     * We should also be able to pull a method from an aspect into itself (or at least
     * this should not crash the refactoring.
     */
    public void testPullSameAspect() throws Exception {
    	setupRefactoring(
    			new CU("myspects", "MyAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package myspects;\n" + 
    					"\n" + 
    					"import classzes.MyClass;\n" + 
    					"\n" + 
    					"public <***>aspect MyAspect {\n" + 
    					"\n" + 
    					"    public void <***>test() {}\n" + 
    					"\n" + 
    					"    public void MyClass.pullMe() {\n" + 
    					"        System.out.println(\"Hi\");\n" + 
    					"    }\n" + 
    					"\n" + 
    					"}\n",
    					//////////////////////////////////////////
    					// Expected
    					"package myspects;\n" + 
    					"\n" + 
    					"import classzes.MyClass;\n" + 
    					"\n" + 
    					"public aspect MyAspect {\n" + 
    					"\n" + 
    					"    public void MyClass.pullMe() {\n" + 
    					"        System.out.println(\"Hi\");\n" + 
    					"    }\n" + 
    					"\n" + 
    					"    public void MyAspect.test() {}\n" + 
    					"}\n"
    			),
    			new CU("classzes", "MyClass.java",
    					///////////////////////////////////////////
    					// Initial == Expected (no changes)
    					"package classzes;\n" +
    					"\n" +
    					"public class MyClass {}"
    			)
    	);
    	performRefactoringAndCheck();
    }
    
    /**
     * What if a member for pulling was actually an ITD in another aspect?
     * We should detect this case and simply move the ITD to the other aspect.
     * We should not add another "AspectName." in front.
     * We should rewrite imports for the target aspect just as if we pulled 
     * out the ITD.
     */
    public void testPullITDMethod() throws Exception {
    	setupRefactoring(
    			new CU("fromaspect", "FromAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package fromaspect;\n" +
    					"\n" +
    					"import classes.MyClass;\n" +
    					"import java.io.File;\n" +
    					"\n"+
    					"public aspect FromAspect {\n" +
    					"\n" +
    					"    public boolean <***>MyClass.check(File file) {\n" +
    					"        return file.exists();\n" +
    					"    }\n" +
    					"\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package fromaspect;\n" +
    					"\n" +
    					"import classes.MyClass;\n" +
    					"import java.io.File;\n" +
    					"\n"+
    					"public aspect FromAspect {\n"+
    					"}"
    			),
    			new CU("classes", "MyClass.java",
    					"package classes;\n" +
    					"\n" +
    					"public class MyClass {\n" +
    					"}"
    			),
    			new CU("toaspect", "ToAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package toaspect;\n" +
    					"\n" +
    					"public <***>aspect ToAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package toaspect;\n" +
    					"\n" +
    					"import classes.MyClass;\n" +
    					"import java.io.File;\n" +
    					"\n" +
    					"public aspect ToAspect {\n"+
    					"    public boolean MyClass.check(File file) {\n" +
    					"        return file.exists();\n" +
    					"    }\n"+
    					"}"
    			)
    	);
    	performRefactoringAndCheck();
    }
        
    /**
     * If pulling ITDs works for ITDMethods it should also work for fields...
     */
    public void testPullITDField() throws Exception {
    	setupRefactoring(
    			new CU("fromaspect", "FromAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package fromaspect;\n" +
    					"\n" +
    					"import classes.MyClass;\n" +
    					"import java.io.File;\n" +
    					"\n"+
    					"public aspect FromAspect {\n" +
    					"    public File <***>MyClass.file;\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package fromaspect;\n" +
    					"\n" +
    					"import classes.MyClass;\n" +
    					"import java.io.File;\n" +
    					"\n"+
    					"public aspect FromAspect {\n"+
    					"}"
    			),
    			new CU("classes", "MyClass.java",
    					"package classes;\n" +
    					"\n" +
    					"public class MyClass {\n" +
    					"}"
    			),
    			new CU("toaspect", "ToAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package toaspect;\n" +
    					"\n" +
    					"public <***>aspect ToAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package toaspect;\n" +
    					"\n" +
    					"import classes.MyClass;\n" +
    					"import java.io.File;\n" +
    					"\n" +
    					"public aspect ToAspect {\n"+
    					"    public File MyClass.file;\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck();
    }
    
    /**
     * If pulling ITDs works for ITDMethods it should also work for constructors...
     */
    public void testPullITDConstructor() throws Exception {
    	setupRefactoring(
    			new CU("fromaspect", "FromAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package fromaspect;\n" +
    					"\n" +
    					"import classes.MyClass;\n" +
    					"import java.io.File;\n" +
    					"\n"+
    					"public aspect FromAspect {\n" +
    					"    public <***>MyClass.new(File file) {}\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package fromaspect;\n" +
    					"\n" +
    					"import classes.MyClass;\n" +
    					"import java.io.File;\n" +
    					"\n"+
    					"public aspect FromAspect {\n"+
    					"}"
    			),
    			new CU("classes", "MyClass.java",
    					"package classes;\n" +
    					"\n" +
    					"public class MyClass {\n" +
    					"}"
    			),
    			new CU("toaspect", "ToAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package toaspect;\n" +
    					"\n" +
    					"public <***>aspect ToAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package toaspect;\n" +
    					"\n" +
    					"import classes.MyClass;\n" +
    					"import java.io.File;\n" +
    					"\n" +
    					"public aspect ToAspect {\n"+
    					"    public MyClass.new(File file) {}\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck();
    }
    
    /**
     * Pulling static stuff should work also
     */
    public void testPullStatic() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    public static void Klass.pullMe() {\n" +
    					"        System.out.println();\n" +
    					"    }\n" +
    					"\n" +
    					"}"
    			),
    			new CU("pclass", "Klass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"    public static void <***>pullMe() {\n" +
    					"        System.out.println();\n" +
    					"    }\n" +
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Klass {\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck();
    }
        
    /**
     * Pull interface methods (this test is currently failing, 
     * feature being tested not yet implemented)
     */
    public void _testPullVoidInterfaceMethod() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Klass;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    public void Intervace.pullMe1() {}\n"+
    					"    public void Intervace.pullMe2() {}\n"+
    					"    public void Intervace.pullMe3() {}\n"+
    					"    public void Intervace.pullMe4() {}\n"+
    					"}"
    			),
    			new CU("pclass", "Intervace.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public interface Intervace {\n" +
    					"    public abstract void <***>pullMe1();\n"+
    					"    abstract void <***>pullMe2();\n"+
    					"    public void <***>pullMe3();\n"+
    					"    void <***>pullMe4();\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public class Intervace {\n" +
    					"}"
    			)
    	);
    	refactoring.setGenerateAbstractMethodStubs(true);
    	performRefactoringAndCheck();
    }
    
    /**
     * Pull interface methods, without the option to allow/convert abstract
     * methods should result in warning messages.
     */
    public void testPullInterfaceMethodWarning() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Intervace;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    public abstract void Intervace.pullMe1();\n"+
    					"    public abstract void Intervace.pullMe2();\n"+
    					"    public abstract void Intervace.pullMe3();\n"+
    					"    public abstract void Intervace.pullMe4();\n"+
    					"}"
    			),
    			new CU("pclass", "Intervace.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public interface Intervace {\n" +
    					"    public abstract void <***>pullMe1();\n"+
    					"    abstract void <***>pullMe2();\n"+
    					"    public void <***>pullMe3();\n"+
    					"    void <***>pullMe4();\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public interface Intervace {\n" +
    					"}"
    			)
    	);
    	performRefactoringAndCheck(
    			"member 'pullMe1' is abstract",
    			"member 'pullMe2' is abstract",
    			"member 'pullMe3' is abstract",
    			"member 'pullMe4' is abstract"
    	);
    }
    
    /**
     * Pull interface methods, *with* the option to allow/convert abstract
     * methods set should *not* result in warning messages and should create
     * stub methods.
     */
    public void testPullInterfaceVoidStubMethods() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.Intervace;\n" +
    					"\n" +
    					"public aspect TestAspect {\n"+
    					"    public void Intervace.pullMe1() { throw new Error(\"abstract method stub\"); }\n"+
    					"    public void Intervace.pullMe2() { throw new Error(\"abstract method stub\"); }\n"+
    					"    public void Intervace.pullMe3() { throw new Error(\"abstract method stub\"); }\n"+
    					"    public void Intervace.pullMe4() { throw new Error(\"abstract method stub\"); }\n"+
    					"}"
    			),
    			new CU("pclass", "Intervace.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public interface Intervace {\n" +
    					"    public abstract void <***>pullMe1();\n"+
    					"    abstract void <***>pullMe2();\n"+
    					"    public void <***>pullMe3();\n"+
    					"    void <***>pullMe4();\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public interface Intervace {\n" +
    					"}"
    			)
    	);
    	refactoring.setGenerateAbstractMethodStubs(true);
    	performRefactoringAndCheck();
    }
    
    /**
     * Pull abstract methods from an abstract class, *with* the option to allow/convert abstract
     * methods set should *not* result in warning messages and should create
     * stub methods.
     */
    public void testPullAbstractStubMethods() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public privileged <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.MyClass;\n" +
    					"\n" +
    					"public privileged aspect TestAspect {\n"+
    					"    public void MyClass.pullMe1() { throw new Error(\"abstract method stub\"); }\n"+
    					"    public void MyClass.pullMe2() { throw new Error(\"abstract method stub\"); }\n"+
    					"    void MyClass.pullMe3() { throw new Error(\"abstract method stub\"); }\n"+
    					"    void MyClass.pullMe4() { throw new Error(\"abstract method stub\"); }\n"+
    					"    void MyClass.pullMe5() { throw new Error(\"abstract method stub\"); }\n"+
    					"}"
    			),
    			new CU("pclass", "MyClass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public abstract class MyClass {\n" +
    					"    public abstract void <***>pullMe1();\n"+
    					"    abstract public void <***>pullMe2();\n"+
    					"    abstract void <***>pullMe3();\n"+
    					"    protected abstract void <***>pullMe4();\n"+
    					"    abstract protected void <***>pullMe5();\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public abstract class MyClass {\n" +
    					"}"
    			)
    	);
    	refactoring.setGenerateAbstractMethodStubs(true);
    	refactoring.setAllowDeleteProtected(true);
    	performRefactoringAndCheck();
    }
    
    /**
     * Pull abstract methods from an abstract class, *without* the option to allow/convert abstract
     * methods set *should* result in warning messages and should *not* create
     * stub methods.
     */
    public void testPullAbstractMethodWarnings() throws Exception {
    	setupRefactoring(
    			new CU("paspect", "TestAspect.aj",
    					//////////////////////////////////////////
    					// Initial 
    					"package paspect;\n" +
    					"\n" +
    					"public privileged <***>aspect TestAspect {\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package paspect;\n" +
    					"\n" +
    					"import pclass.MyClass;\n" +
    					"\n" +
    					"public privileged aspect TestAspect {\n"+
    					"    public abstract void MyClass.pullMe1();\n"+
    					"    public abstract void MyClass.pullMe2();\n"+
    					"    abstract void MyClass.pullMe3();\n"+
    					"    abstract void MyClass.pullMe4();\n"+
    					"    abstract void MyClass.pullMe5();\n"+
    					"}"
    			),
    			new CU("pclass", "MyClass.java",
    					//////////////////////////////////////////
    					// Initial 
    					"package pclass;\n" +
    					"\n" +
    					"public abstract class MyClass {\n" +
    					"    public abstract void <***>pullMe1();\n"+
    					"    abstract public void <***>pullMe2();\n"+
    					"    abstract void <***>pullMe3();\n"+
    					"    protected abstract void <***>pullMe4();\n"+
    					"    abstract protected void <***>pullMe5();\n"+
    					"}",
    					//////////////////////////////////////////
    					// Expected
    					"package pclass;\n" +
    					"\n" +
    					"public abstract class MyClass {\n" +
    					"}"
    			)
    	);
    	refactoring.setAllowDeleteProtected(true);
    	performRefactoringAndCheck(
    			"member 'pullMe1' is abstract",
    			"member 'pullMe2' is abstract",
    			"member 'pullMe3' is abstract",
    			"member 'pullMe4' is abstract",
    			"member 'pullMe5' is abstract"
    	);
    }
    
    /**
     * Note: not using the assert method from superclass, because I prefer to use 
     * assertEquals with Strings, which gives a nice comparison view in the
     * JUnit Eclipse View (easier to see diffs than in printed output).
     * @throws JavaModelException 
     */
	private void assertExpectedResults() throws JavaModelException {
		for (int i = 0; i < units.length; i++) {
			char[] contents;
			if (units[i] instanceof AJCompilationUnit) {
				((AJCompilationUnit) units[i]).requestOriginalContentMode();
			}
			contents = ((CompilationUnit) units[i]).getContents();
			if (units[i] instanceof AJCompilationUnit) {
				((AJCompilationUnit) units[i]).discardOriginalContentMode();
			}
			String actualContents = String.valueOf(contents);
			// for a smoother testing experience let's ignore streams of newlines
			assertEquals("CompilationUnit: "+units[i].getElementName(), 
					simplifyText(expectedResults[i]), 
					simplifyText(actualContents));
		}
	}
	
	/**
	 * To make comparison more likely to succeed with variations in spacing we use
	 * this method ...
	 */
	private String simplifyText(String text) {
		while (text.contains("\n\n"))
			text = text.replace("\n\n", "\n");
		return text;
	}
}
