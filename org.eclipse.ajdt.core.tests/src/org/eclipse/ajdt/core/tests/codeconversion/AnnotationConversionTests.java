/*******************************************************************************
 * Copyright (c) 2011 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - Initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.codeconversion;

import java.util.Arrays;

import junit.framework.AssertionFailedError;

import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Version;

/**
 * Tests for converting Annotations from the AJ AST into JDT JavaModel
 * Only working for methods and ITDs
 * @author Andrew Eisenberg
 * @created Jan 20, 2011
 */
public class AnnotationConversionTests extends AJDTCoreTestCase {
	
    protected IJavaProject project;
    protected IPackageFragment p;
    protected void setUp() throws Exception {
        super.setUp();
        project = JavaCore.create(createPredefinedProject("DefaultEmptyProject"));
        p = createPackage("p", project);
    }
    
    // Marker annotations
    public void testSimpleMarkerAnnotationMethod1() throws Exception {
        assertAnnotations("@Deprecated( )\n", getAnnotationsForMethod("package p;\naspect Aspect {\n @Deprecated void foo() { }  }"));
    }
    
    public void testSimpleMarkerAnnotationMethod2() throws Exception {
        assertAnnotations("@Deprecated( )\n@Other( )\n", getAnnotationsForMethod("package p;\naspect Aspect { @Deprecated @Other void foo() { } }\n @interface Other { }"));
    }
    
    public void testQualifiedMarkerAnnotationMethod1() throws Exception {
        createUnit("q", "Other.java", "package q;\npublic @interface Other { }");
        assertAnnotations("@java.lang.Deprecated( )\n@q.Other( )\n", getAnnotationsForMethod("package p;\naspect Aspect {\n @java.lang.Deprecated @q.Other void foo() { } }"));
    }
    
    public void testQualifiedMarkerAnnotationITDMethod1() throws Exception {
        createUnit("q", "Other.java", "package q;\npublic @interface Other { }");
        assertAnnotations("@java.lang.Deprecated( )\n@q.Other( )\n", 
        		getAnnotationsForITD("package p;\naspect Aspect {\n @java.lang.Deprecated\n @q.Other\n void F.foo() { }\n class F { } }"));
    }
    
    // single member annotations
    public void testSingleMemberAnnotationMethod1() throws Exception {
        assertAnnotations("@Other( value = 1, )\n", getAnnotationsForMethod("package p;\naspect Aspect { @Other(1) void foo() { } }\n @interface Other {\n int value(); }"));
    }
    
    public void testSingleMemberAnnotationMethod2() throws Exception {
        assertAnnotations("@Other( value = val, )\n", getAnnotationsForMethod("package p;\naspect Aspect { @Other(\"val\") void foo() { } }\n @interface Other {\n String value(); }"));
    }
    
    public void testSingleMemberAnnotationMethod3() throws Exception {
        assertAnnotations("@Other( value = 1, )\n", getAnnotationsForMethod("package p;\naspect Aspect { @Other(1L) void foo() { } }\n @interface Other {\n long value(); }"));
    }
    
    public void testSingleMemberAnnotationMethod4() throws Exception {
        assertAnnotations("@Other( value = c, )\n", getAnnotationsForMethod("package p;\naspect Aspect { @Other('c') void foo() { } }\n @interface Other {\n char value(); }"));
    }
    
    public void testSingleMemberAnnotationMethod5() throws Exception {
        assertAnnotations("@Other( value = 1.0, )\n", getAnnotationsForMethod("package p;\naspect Aspect { @Other(1.0f) void foo() { } }\n @interface Other {\n float value(); }"));
    }
    
    public void testSingleMemberAnnotationMethod6() throws Exception {
        assertAnnotations("@Other( value = 1.0, )\n", getAnnotationsForMethod("package p;\naspect Aspect { @Other(1.0d) void foo() { } }\n @interface Other {\n double value(); }"));
    }
    
    public void testSingleMemberAnnotationMethodClass1() throws Exception {
        assertAnnotations("@Other( value = String, )\n", getAnnotationsForMethod("package p;\naspect Aspect { @Other(String.class) void foo() { } }\n @interface Other {\n Class<?> value(); }"));
    }
    
    public void testSingleMemberAnnotationMethodArray1() throws Exception {
        assertAnnotations("@Other( value = [1, 2], )\n", getAnnotationsForMethod("package p;\naspect Aspect { @Other({1, 2}) void foo() { } }\n @interface Other {\n int[] value(); }"));
    }
    
    public void testSingleMemberAnnotationMethodArray2() throws Exception {
        assertAnnotations("@Other( value = [a, b], )\n", getAnnotationsForMethod("package p;\naspect Aspect { @Other({\"a\", \"b\"}) void foo() { } }\n @interface Other {\n String[] value(); }"));
    }
    
    public void testSingleMemberAnnotationMethodArray3() throws Exception {
        assertAnnotations("@Other( value = [], )\n", getAnnotationsForMethod("package p;\naspect Aspect { @Other({}) void foo() { } }\n @interface Other {\n String[] value(); }"));
    }
    
    public void testSingleMemberAnnotationMethodArray4() throws Exception {
        assertAnnotations("@Other( value = [CONST1, Aspect.CONST2], )\n", getAnnotationsForMethod("package p;\naspect Aspect { @Other({CONST1, Aspect.CONST2}) void foo() { } \nfinal static int CONST1 = 9; \n final static int CONST2 = 9; }\n @interface Other {\n int[] value(); }"));
    }
    
    public void testSingleMemberAnnotationMethodBoolean1() throws Exception {
        assertAnnotations("@Other( value = true, )\n", 
                getAnnotationsForMethod("package p;\naspect Aspect { @Other(true) void foo() { } \n" +
                		" }\n " +
                		"@interface Other {\n boolean value(); }"));
    }
    
    public void testSingleMemberAnnotationMethodBoolean2() throws Exception {
        assertAnnotations("@Other( value = false, )\n", 
                getAnnotationsForMethod("package p;\naspect Aspect { @Other(false) void foo() { } \n" +
                		" }\n " +
                		"@interface Other {\n boolean value(); }"));
    }
    
    public void testSingleMemberAnnotationMethodBoolean3() throws Exception {
        assertAnnotations("@Other( value = Boolean, )\n", 
                getAnnotationsForMethod("package p;\naspect Aspect { @Other(Boolean.class) void foo() { } \n" +
                		" }\n @interface Other {\n Class<?> value(); }"));
    }
    
    // Java 7 support
    public void testSingleMemberAnnotationMethodLiteral1() throws Exception {
        // requires 3.7.1 or greater
        int compare = Platform.getBundle("org.eclipse.jdt.core").getVersion().compareTo(Version.parseVersion("3.7.1"));
        if (compare < 0) {
            return;
        }
        setJava7SourceLevel(project);
        assertAnnotations("@Other( value = 26, )\n", 
                getAnnotationsForMethod("package p;\naspect Aspect { @Other(0b11010) void foo() { } \n }\n @interface Other {\n byte value(); }"));
    }
    
    public void testSingleMemberAnnotationMethodLiteral2() throws Exception {
        assertAnnotations("@Other( value = 26, )\n", getAnnotationsForMethod("package p;\naspect Aspect { @Other(0x1a) void foo() { } \n }\n @interface Other {\n int value(); }"));
    }
    
    public void testSingleMemberAnnotationMethodLiteral3() throws Exception {
        assertAnnotations("@Other( value = 123.4, )\n", getAnnotationsForMethod("package p;\naspect Aspect { @Other(1.234e2) void foo() { } \n }\n @interface Other {\n double value(); }"));
    }
    
    public void testSingleMemberAnnotationMethodLiteral4() throws Exception {
        assertAnnotations("@Other( value = 123.4, )\n", getAnnotationsForMethod("package p;\naspect Aspect { @Other(123.4f) void foo() { } \n }\n @interface Other {\n float value(); }"));
    }
    
    // Java 7 support
    public void testSingleMemberAnnotationMethodLiteral5() throws Exception {
        // requires 3.7.1 or greater
        int compare = Platform.getBundle("org.eclipse.jdt.core").getVersion().compareTo(Version.parseVersion("3.7.1"));
        if (compare < 0) {
            return;
        }
        setJava7SourceLevel(project);
        assertAnnotations("@Other( value = 9223372036854775807, )\n", getAnnotationsForMethod("package p;\naspect Aspect { @Other(0x7fff_ffff_ffff_ffffL) void foo() { } \n }\n @interface Other {\n long value(); }"));
    }
    
    public void testSingleMemberAnnotationMethod7() throws Exception {
        assertAnnotations("@Other( value = CONST, )\n", getAnnotationsForMethod("package p;\naspect Aspect { \n @Other(CONST) void foo() { }\nfinal static int CONST = 9; }\n @interface Other {\n int value(); }"));
    }
    
    public void testSingleMemberAnnotationMethod8() throws Exception {
        assertAnnotations("@Other( value = Aspect.CONST, )\n", getAnnotationsForMethod("package p;\naspect Aspect {\n @Other(Aspect.CONST) void foo() { }\nfinal static int CONST = 9; }\n @interface Other {\n int value(); }"));
    }
    
    public void testNormalAnnotationITD1() throws Exception {
        assertAnnotations("@Other( value1 = CONST1, value2 = CONST2, )\n", getAnnotationsForMethod("package p;\naspect Aspect {\n @Other(value1=CONST1,\n value2=CONST2)\n void F.foo() { } \nclass F { }\nfinal static int CONST1 = 9; \n final static int CONST2 = 9; }\n @interface Other {\n int value1(); \n int value2(); }"));
    }
    
    public void testNormalAnnotationITD2() throws Exception {
        assertAnnotations("@Other( value1 = [CONST1, 1], value2 = [CONST2, Aspect.CONST2], )\n", getAnnotationsForITD("package p;\naspect Aspect {\n @Other(value1={CONST1,1},\n value2={CONST2,Aspect.CONST2})\n void F.foo() { } \nclass F { }\nfinal static int CONST1 = 9; \n final static String CONST2 = \"a\"; }\n @interface Other {\n int[] value1(); \n String[] value2(); }"));
    }
    
    public void testNormalAnnotationEnumITD1() throws Exception {
        createUnit("q", "MyEnum.java", "package q;\n public enum MyEnum{\n A, B, C }");
        createUnit("q", "Other.java", "package q;\n public @interface Other {\n MyEnum value1(); \n MyEnum value2(); }");
        assertAnnotations("@Other( value1 = MyEnum.A, value2 = q.MyEnum.B, )\n", 
                getAnnotationsForITD("package p;\n" +
                		"import q.Other;\n" +
                		"import q.MyEnum;\n" +
                		"aspect Aspect {\n" +
                		" @Other(value1=MyEnum.A,\n value2=q.MyEnum.B)\n" +
                		" void F.foo() { }\n" +
                		" class F { }\n" +
                		"}"));
    }
    
    public void testNormalAnnotationEnumArrayITD2() throws Exception {
        createUnit("q", "MyEnum.java", "package q;\n public enum MyEnum{\n A, B, C }");
        createUnit("q", "Other.java", "package q;\n public @interface Other {\n MyEnum[] value1(); \n MyEnum[] value2(); }");
        assertAnnotations("@Other( value1 = [MyEnum.A], value2 = [MyEnum.A, q.MyEnum.B, C], )\n", 
                getAnnotationsForITD("package p;\n" +
                        "import q.Other;\n" +
                        "import q.MyEnum;\n" +
                        "import static q.MyEnum.C;\n" +
                        "aspect Aspect {\n" +
                        " @Other(value1={MyEnum.A},\n value2={MyEnum.A,q.MyEnum.B,C})\n" +
                        " void F.foo() { }\n" +
                        " class F { }\n" +
                        "}"));
    }
    
    public void testNormalAnnotationNestedAnnsITD1() throws Exception {
        createUnits(
                new String[] { "q", "q", "q"},
                new String[] { "Ann1.java", "Ann2.java", "Ann3.java" },
                new String[] { 
                 "package q;\n public @interface Ann1 { Ann2 value1(); Ann3 value2(); }",
                 "package q;\n public @interface Ann2 { int value(); }",
                 "package q;\n public @interface Ann3 { String value1(); }"
                 }, project);
        assertAnnotations("@Ann1( value1 = @Ann2( value = 9, )\n, value2 = @Ann3( value1 = A, )\n, )\n", 
                getAnnotationsForITD("package p;\n" +
                        "import q.*;\n" +
                        "aspect Aspect {\n" +
                        " @Ann1(value1=@Ann2(9),\n value2=@Ann3(value1=\"A\") )\n" +
                        " void F.foo() { }\n" +
                        " class F { }\n" +
                "}"));
    }
    
    protected IAnnotation[] getAnnotationsForITD(String cuContents) throws Exception {
    	ICompilationUnit unit = createUnit(cuContents);
    	return getFirstIntertypeElement(unit).getAnnotations();
    }
    
    protected IAnnotation[] getAnnotationsForMethod(String cuContents) throws Exception {
        ICompilationUnit unit = createUnit(cuContents);
        return getFirstMethod(unit).getAnnotations();
    }
    
    protected ICompilationUnit createUnit(String cuContents) throws CoreException {
    	return super.createUnit("p", "Aspect.aj", cuContents, project);
    }
    
    protected ICompilationUnit createUnit(String pkg, String cuName, String cuContents) throws CoreException {
        return super.createUnit(pkg, cuName, cuContents, project);
    }
    
    protected void assertAnnotations(String expectedAnnotationString, IAnnotation[] actualAnnotations) throws JavaModelException {
    	String actualAnnotationString = convertToString(actualAnnotations);
    	assertEquals(expectedAnnotationString, actualAnnotationString);
    }

    private String convertToString(IAnnotation[] actualAnnotations) throws JavaModelException {
    	if (actualAnnotations == null) {
    		return "";
    	}
    	StringBuilder sb = new StringBuilder();
    	for (IAnnotation actualAnnotation : actualAnnotations) {
    	    sb.append(printAnnotation(actualAnnotation)); 
        }
        return sb.toString();
    }

    private String printAnnotation(IAnnotation actualAnnotation)
            throws JavaModelException {
        StringBuilder sb = new StringBuilder();
        sb.append("@" + actualAnnotation.getElementName() + "( ");
        for (IMemberValuePair mvp : actualAnnotation.getMemberValuePairs()) {
            sb.append(mvp.getMemberName() + " = " + printValue(mvp) + ", ");
        }
        sb.append(")\n");
        return sb.toString();
    }

    public String printValue(IMemberValuePair mvp) throws JavaModelException {
        Object value = mvp.getValue();
        if (value instanceof Object[]) {
            return Arrays.toString((Object[]) value);
        } else if (value instanceof IAnnotation) {
            return printAnnotation((IAnnotation) value);
        } else if (value == null) {
            return "null";
        } else {
            return value.toString();
        }
    }
}
