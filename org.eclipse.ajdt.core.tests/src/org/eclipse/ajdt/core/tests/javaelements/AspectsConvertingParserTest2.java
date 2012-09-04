/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation, SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *     Andrew Eisenberg - tests for ITD replacement
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.javaelements;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.ajdt.core.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.core.codeconversion.ConversionOptions;

/**
 * 
 * @author Luzius Meisser
 * @author andrew
 */
public class AspectsConvertingParserTest2 extends AbstractTestCase {
	


	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		unit.requestOriginalContentMode();
		char[] content = (char[])unit.getContents().clone();
		unit.discardOriginalContentMode();
		myParser = new AspectsConvertingParser(content);

	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testConvert() {
//		int len = myParser.content.length;
		
		myParser.convert(ConversionOptions.STANDARD);
//		if (myParser.content.length !=  len + 4)
//			fail("Reference to C has not been added (?).");
		if (new String(myParser.content).indexOf(':') != -1)
			fail("Some pointcut designators have not been removed."); //$NON-NLS-1$
	}
	
	public void testConvert2() {
 		myParser.convert(new ConversionOptions(true, true, false));
 		int pos = new String(myParser.content).indexOf("org.aspectj.lang.JoinPoint thisJoinPoint;"); //$NON-NLS-1$
 		if (pos < 0)
 			fail("tjp has not been added."); //$NON-NLS-1$
 		
 		pos = new String(myParser.content).indexOf("org.aspectj.lang.JoinPoint.StaticPart thisJoinPointStaticPart;"); //$NON-NLS-1$
 		if (pos < 0)
 			fail("tjpsp has not been added."); //$NON-NLS-1$
 		
//		if (myParser.content.length != 1086)
//			fail("tjp and tjpsp have not been added correctly.");
	}
	
	public void testConvert3() {
		int len = myParser.content.length;
 		myParser.convert(ConversionOptions.CONSTANT_SIZE);
		if (myParser.content.length !=  len)
			fail("Length of content has changed."); //$NON-NLS-1$
		if (new String(myParser.content).indexOf(':') != -1)
			fail("Some pointcut designators have not been removed."); //$NON-NLS-1$
	}
	
	public void testConvert4() {
 		myParser.convert(ConversionOptions.CODE_COMPLETION);
		assertEquals("Wrong size of content.",1129,myParser.content.length); //$NON-NLS-1$
		if (new String(myParser.content).indexOf(':') != -1)
			fail("Some pointcut designators have not been removed."); //$NON-NLS-1$
	}

	public void testBug93248() {
		String statement = "System.out.println(true?\"foo\":\"bar\");"; //$NON-NLS-1$
		char[] testContent = ("public aspect ABC {\npublic static void main(String[] args) {\n" //$NON-NLS-1$
				+ statement + "\n}\n}").toCharArray(); //$NON-NLS-1$
		AspectsConvertingParser pars = new AspectsConvertingParser(testContent);
		pars.convert(ConversionOptions.STANDARD);
		String converted = new String(pars.content);
		if (converted.indexOf(statement) == -1) {
			fail("Regression of bug 93248: tertiary operator breaks organise imports"); //$NON-NLS-1$
		}
	}
	
	public void testBug93248again() {
		// nested conditional statements
		String statement = "System.out.println(true?true?\"foo\":\"foobar\":\"bar\");"; //$NON-NLS-1$
		char[] testContent = ("public aspect ABC {\npublic static void main(String[] args) {\n" //$NON-NLS-1$
				+ statement + "\n}\n}").toCharArray(); //$NON-NLS-1$
		AspectsConvertingParser pars = new AspectsConvertingParser(testContent);
		pars.convert(ConversionOptions.STANDARD);
		String converted = new String(pars.content);
		if (converted.indexOf(statement) == -1) {
			fail("Regression of bug 93248: tertiary operator breaks organise imports"); //$NON-NLS-1$
		}
	}	
	
	
	/*
	 * Class under test for int findPrevious(char, char[], int)
	 */
	public void testFindPreviouscharcharArrayint() {
		char[] testContent = "abc abc abc xyz xyz".toCharArray(); //$NON-NLS-1$
		char target = 'b';
		myParser.content = testContent;
		if (myParser.findPrevious(target, 3) != 1)
			fail("Find previous failed."); //$NON-NLS-1$
		if (myParser.findPrevious(target, 0) != -1)
			fail("Find previous failed."); //$NON-NLS-1$
		
	}

	/*
	 * Class under test for int findPrevious(char[], char[], int)
	 */
	public void testFindPreviouscharArraycharArrayint() {
		
		char[] testContent = "abc abc abc xyz xyz".toCharArray(); //$NON-NLS-1$
		char[] target = "bx".toCharArray(); //$NON-NLS-1$
		myParser.content = testContent;
		if (myParser.findPrevious(target, 3) != 1)
			fail("Find previous failed."); //$NON-NLS-1$
		if (myParser.findPrevious(target, 0) != -1)
			fail("Find previous failed."); //$NON-NLS-1$
		if (myParser.findPrevious(target, 13) != 12)
			fail("Find previous failed."); //$NON-NLS-1$
	}

	public void testFindPreviousNonSpace() {
		char[] testContent = "abc abc abc xyz xyz".toCharArray(); //$NON-NLS-1$
		myParser.content = testContent;
		if (myParser.findPreviousNonSpace(3) != 2)
			fail("Find previous failed."); //$NON-NLS-1$
		if (myParser.findPreviousNonSpace(0) != 0)
			fail("Find previous failed, returns " + myParser.findPreviousNonSpace(0)); //$NON-NLS-1$
		
	}

	public void testFindNext() {
		char[] testContent = "abc abc abc xyz xyz".toCharArray(); //$NON-NLS-1$
		char[] target = "bx".toCharArray(); //$NON-NLS-1$
		myParser.content = testContent;
		if (myParser.findNext(target, 0) != 1)
			fail("Find next failed."); //$NON-NLS-1$
		if (myParser.findNext(target, 100) != -1)
			fail("Find next failed."); //$NON-NLS-1$
		if (myParser.findNext(target, 7) != 9)
			fail("Find next failed."); //$NON-NLS-1$
		if (myParser.findNext(target, 17) != -1)
			fail("Find next failed."); //$NON-NLS-1$
		if (myParser.findNext(target, 12) != 12)
			fail("Find next failed."); //$NON-NLS-1$
		
	}
	

    public void testITDReplace1() {
        char[] testContent = "aspect Foo { void foo.bar.Circle<java.util.List<String>>.nothing(h.y.Z f, h.y.Z y) }".toCharArray(); //$NON-NLS-1$
        String target      = "class  Foo { void foo$bar$Circle$java$util$List$String$$$nothing(h.y.Z f, h.y.Z y) }"; //$NON-NLS-1$
        AspectsConvertingParser parser = new AspectsConvertingParser(testContent);
        parser.content = testContent;
        parser.convert(ConversionOptions.CONSTANT_SIZE);
        assertEquals(target, new String(parser.content));
    }
    public void testITDReplace2() {
        char[] testContent = "aspect Foo { void foo .  bar .   Circle <   java .  util  .  List  <  String  >  >  .  nothing(h.y.Z f, h.y.Z y) }".toCharArray(); //$NON-NLS-1$
        String target      = "class  Foo { void foo$$$$bar$$$$$Circle$$$$$java$$$$util$$$$$List$$$$$String$$$$$$$$$$$nothing(h.y.Z f, h.y.Z y) }"; //$NON-NLS-1$
        AspectsConvertingParser parser = new AspectsConvertingParser(testContent);
        parser.content = testContent;
        parser.convert(ConversionOptions.CONSTANT_SIZE);
        assertEquals(target, new String(parser.content));
    }
    public void testITDReplace3() {
        char[] testContent = "aspect Foo { void foo .  bar .   Circle <   java .  util  .  List  <  String  >  >  .  nothing<? extends java.util.List<String> >(h.y.Z f, h.y.Z y) }".toCharArray(); //$NON-NLS-1$
        String target      = "class  Foo { void foo$$$$bar$$$$$Circle$$$$$java$$$$util$$$$$List$$$$$String$$$$$$$$$$$nothing<? extends java.util.List<String> >(h.y.Z f, h.y.Z y) }"; //$NON-NLS-1$
        AspectsConvertingParser parser = new AspectsConvertingParser(testContent);
        parser.content = testContent;
        parser.convert(ConversionOptions.CONSTANT_SIZE);
        assertEquals(target, new String(parser.content));
    }
    
    public void testTypeParamOnLocalVariable1() throws Exception {
        assertConvertingParse(
                "aspect Test {\n" +
        		"  private pointcut none();\n" +
        		"  before(): none() {\n" + 
        		"    Class<?>[] x;\n" + 
                "  }\n" +
                "  before() : none() {}\n" +  
                "}");
    }
    public void testTypeParamOnLocalVariable2() throws Exception {
        assertConvertingParse(
                "aspect Test {\n" +
                "  private pointcut none();\n" +
                "  before(): none() {\n" + 
                "    Class<?>[] x;\n" +
                "    int y = 0;\n" +
                "    if ((y < 8) ? true : false) {} \n" + 
                "  }\n" +
                "  before() : none() {}\n" + 
                "}");
    }
    public void testTypeParamOnLocalVariable3() throws Exception {
        assertConvertingParse(
                "aspect Test {\n" +
                "  private pointcut none();\n" +
                "  before(): none() {\n" + 
                "    Class<?>[] x;\n" +
                "    int Y = 0;\n" +
                "    if ((Y < 8) ? true : false) {} \n" + 
                "  }\n" +
                "  before() : none() {}\n" +  
                "}");
    }
    public void testTypeParamOnLocalVariable4() throws Exception {
        assertConvertingParse(
                "aspect Test {\n" +
                "  private pointcut none();\n" +
                "  before(): none() {\n" + 
                "    Class<?>[] x;\n" +
                "    int Y = 6 < 7 ? 6: 7;\n" +
                "    if ((Y < 8) ? true : false) {} \n" + 
                "  }\n" +
                "  before() : none() {}\n" +  
        "}");
    }
    
    // Bug 384422
    public void testAnnotationStylePointcutInAspect1() throws Exception {
        assertConvertingParse(
                "import org.aspectj.lang.JoinPoint;\n" + 
                "import org.aspectj.lang.annotation.AfterThrowing;\n" + 
                "public aspect Test {\n" + 
                "        @AfterThrowing(pointcut=\"execution(* *(..))\")\n" + 
                "        public void bizLoggerWithException(JoinPoint thisJoinPoint) { }\n" + 
                "}");
    }

    // Bug 384422
    public void testAnnotationStylePointcutInAspect2() throws Exception {
        assertConvertingParse(
                "import org.aspectj.lang.JoinPoint;\n" + 
                "import org.aspectj.lang.annotation.AfterThrowing;\n" + 
                "public aspect Test {\n" + 
                "        @AfterThrowing(throwing=\"e\", pointcut=\"execution(* *(..))\")\n" + 
                "        public void bizLoggerWithException(JoinPoint thisJoinPoint) { }\n" + 
                "}");
    }
    
    // Bug 384422
    public void testAnnotationStylePointcutInAspect3() throws Exception {
        assertConvertingParse(
                "import org.aspectj.lang.JoinPoint;\n" + 
                "import org.aspectj.lang.annotation.AfterThrowing;\n" + 
                "public aspect Test {\n" + 
                "        @AfterThrowing(pointcut=\"execution(* *(..))\", throwing=\"e\")\n" + 
                "        public void bizLoggerWithException(JoinPoint thisJoinPoint) { }\n" + 
                "}");
    }
    
    // Main type name must be "Test" and must be in default package
    private void assertConvertingParse(String testContentStr) {
        char[] testContent = testContentStr.toCharArray();
        final AspectsConvertingParser convertingParser = new AspectsConvertingParser(testContent);
        convertingParser.content = testContent;
        convertingParser.convert(ConversionOptions.CONSTANT_SIZE);
        
        ICompilationUnit unit = new ICompilationUnit() {
            
            public char[] getFileName() {
                return "Test.java".toCharArray();
            }
            
            public char[][] getPackageName() {
                return new char[0][];
            }
            
            public char[] getMainTypeName() {
                return "Test".toCharArray();
            }
            
            public char[] getContents() {
                return convertingParser.content;
            }
            
            public boolean ignoreOptionalProblems() {
                return false;
            }
        };
        CompilerOptions options = new CompilerOptions();
        options.sourceLevel = ClassFileConstants.JDK1_5;
        options.targetJDK = ClassFileConstants.JDK1_5;
        Parser parser = new Parser(
                new ProblemReporter(DefaultErrorHandlingPolicies
                        .proceedWithAllProblems(), options,
                        new DefaultProblemFactory()), true);
        CompilationResult result = new CompilationResult(unit, 0, 1, 100);
        CompilationUnitDeclaration decl = parser.parse(unit, result);
        if (result.hasErrors()) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < result.getErrors().length; i++) {
                sb.append("\n\t" + result.getErrors()[i].getMessage());
            }
            sb.append("\n============\nOriginal text:\n" + testContentStr);
            sb.append("\n============\nConverted text:\n" + String.valueOf(convertingParser.content));
            fail("Converted unit has errors:" + sb.toString());
        }
    }
}
