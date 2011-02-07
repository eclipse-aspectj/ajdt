/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.codeselect;

import org.eclipse.contribution.jdt.itdawareness.INameEnvironmentProvider;
import org.eclipse.contribution.jdt.itdawareness.NameEnvironmentAdapter;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Tests that code selection opccurs correctly on intertype inner type references
 * @author Andrew Eisenberg
 * @created Aug 16, 2010
 */
public class ITITAwareCodeSelectionTests extends
        AbstractITDAwareCodeSelectionTests {
    
    IJavaProject project;
    
    // need to set a NameEnviromentProvider, since this is typically
    // set by AJDT.UI
    INameEnvironmentProvider origProvider;
    INameEnvironmentProvider mockProvider = new MockNameEnvironmentProvider();
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        origProvider = NameEnvironmentAdapter.getInstance().getProvider();
        NameEnvironmentAdapter.getInstance().setProvider(mockProvider);
        super.setUp();
        project = JavaCore.create(createPredefinedProject("DefaultEmptyProject"));
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            NameEnvironmentAdapter.getInstance().setProvider(origProvider);
        }
    }
    

    public void testSelectInTargetType() throws Exception {
        
        ICompilationUnit unit = createUnits(
                new String[] { "p", "p", "p" }, 
                new String[] { "AspectCity.aj", "Function.java", "City.java" }, 
                new String[] {
                        "package p;\n" + 
                        "import java.util.List;" + 
                        "import java.util.HashMap;" + 
                        "privileged aspect AspectCity {\n" + 
                        "    public static class City.Keys {\n" + 
                        "        public static final Function<Object, City> CITY = null;\n" + 
                        "        public static final HashMap<String, String> xxx() { return null; }\n" + 
                        "    }\n" + 
                        "    void x() {\n" + 
                        "        City.Keys.CITY.getter();\n" + 
                        "    }\n" + 
                        "}",
                        
                        "package p;\n" + 
                        "public class Function<K, V> {\n" + 
                        "    public void getter() { }\n" + 
                        "}",
                        
                        "package p;\n" +
                        "public class City {\n" +
                        "   void x() {\n" +
                        "      City.Keys.CITY.getter();\n" +
                        "      City.Keys.xxx().get(\"\").charAt(0);" +
                        "   }\n" +
                        "}",
                }, project)[2];
        
        // after the ITIT reference, we can't do any more code selection.
        // that is why commenting out the gets
//        validateCodeSelect(unit, findRegion(unit, "getter", 1), "getter");
        validateCodeSelect(unit, findRegion(unit, "Keys", 1), "Keys");
        validateCodeSelect(unit, findRegion(unit, "City", 1), "City");
        validateCodeSelect(unit, findRegion(unit, "xxx", 1), "xxx");
//        validateCodeSelect(unit, findRegion(unit, "get", 2), "get");
//        validateCodeSelect(unit, findRegion(unit, "charAt", 1), "charAt");
    }
    
    public void testSelectInAspect() throws Exception {
    	ICompilationUnit unit =
            createUnits(
                    new String[] { "p", "p", "p" }, 
                    new String[] { "AspectCity.aj", "Function.java", "City.java" }, 
                    new String[] {
                            "package p;\n" + 
                            "privileged aspect AspectCity {\n" + 
                            "    void x() {\n" + 
                            "        City.Keys.CITY.getter();\n" + 
                            "    }\n" + 
                            "    public static class City.Keys {\n" + 
                            "        public static final Function<Object, City> CITY = null;\n" + 
                            "    }\n" + 
                            "}",
                            
                            "package p;\n" + 
                            "public class Function<K, V> {\n" + 
                            "    public void getter() { }\n" + 
                            "}",

                            "package p;\n" +
                            "public class City {\n" +
                            "}",
                    }, project)[0];

            try {
                // must be a working copy or else AJCompilationUnit thinks
                // that we are not in an AJ editor
                unit.becomeWorkingCopy(null);
//                validateCodeSelect(unit, findRegion(unit, "getter", 1), "getter");
                validateCodeSelect(unit, findRegion(unit, "Keys", 1), "Keys");
                validateCodeSelect(unit, findRegion(unit, "City", 2), "City");
                validateCodeSelect(unit, findRegion(unit, "CITY", 1), "CITY");
            } finally {
                unit.discardWorkingCopy();
            }

    }
    
    public void testSelectOtherType1() throws Exception {
    	ICompilationUnit unit = 
        createUnits(
                new String[] { "p", "p", "p", "q" }, 
                new String[] { "CityAspect.aj", "Function.java", "City.java", "Test.java" }, 
                new String[] {
                        "package p;\n" + 
                        "import java.util.List;" + 
                        "privileged aspect CityAspect {\n" + 
                        "    public static class City.Keys {\n" + 
                        "        public static final Function<List<String>, City> CITY = null;\n" + 
                        "        public static final Function<java.util.HashMap<String, String>, City> CITY2 = null;\n" + 
                        "    }\n" + 
                        "    void x() {\n" + 
                        "        City.Keys.CITY.get();\n" + 
                        "    }\n" + 
                        "}",
                        
                        "package p;\n" + 
                        "public class Function<K, V> {\n" + 
                        "    public K get() { return null; }\n" + 
                        "}",
                        
                        "package p;\n" +
                        "public class City { }",
                        
                        "package q;\n" + 
                        "import p.City;\n" + 
                        "public class Test {\n" + 
                        "    public static void main(String[] args) { \n" + 
                        "        City.Keys.CITY.get().get(0).substring(0);   \n" + 
                        "        p.City.Keys.CITY2.get().put(null, null);   \n" + 
                        "    }\n" + 
                        "}"
                }, project)[3];
    	
    	// after the ITIT reference, we can't do any more code selection.
    	// that is why commenting out the gets
      validateCodeSelect(unit, findRegion(unit, "Keys", 1), "Keys");
      validateCodeSelect(unit, findRegion(unit, "CITY", 1), "CITY");
//      validateCodeSelect(unit, findRegion(unit, "get", 1), "get");
//      validateCodeSelect(unit, findRegion(unit, "get", 2), "get");
//      validateCodeSelect(unit, findRegion(unit, "substring", 1), "substring");
      validateCodeSelect(unit, findRegion(unit, "CITY2", 1), "CITY2");
//      validateCodeSelect(unit, findRegion(unit, "get", 3), "get");
//      validateCodeSelect(unit, findRegion(unit, "put", 1), "put");
    }

    public void testSelectOtherType2() throws Exception {
        ICompilationUnit unit = 
        createUnits(
                new String[] { "p", "p", "p", "q" }, 
                new String[] { "CityAspect.aj", "Function.java", "City.java", "Test.java" }, 
                new String[] {
                        "package p;\n" + 
                        "import java.util.List;" + 
                        "privileged aspect CityAspect {\n" + 
                        "    public static class City.Keys {\n" + 
                        "        public static final Function<List<String>, City> CITY = null;\n" + 
                        "        public static final Function<java.util.HashMap<String, String>, City> CITY2 = null;\n" + 
                        "    }\n" + 
                        "    void x() {\n" + 
                        "        City.Keys.CITY.get();\n" + 
                        "    }\n" + 
                        "}",
                        
                        "package p;\n" + 
                        "public class Function<K, V> {\n" + 
                        "    public K get() { return null; }\n" + 
                        "}",
                        
                        "package p;\n" +
                        "public class City { }",
                        
                        "package q;\n" + 
                        "import p.City;\n" + 
                        "import p.City.Keys;\n" + 
                        "import static p.City.Keys.CITY;\n" + 
                        "public class Test {\n" + 
                        "    public static void main(String[] args) { \n" + 
                        "        City.Keys.CITY.get();\n" + 
                        "        Keys.CITY.get();\n" + 
                        "        CITY.get(); \n" + 
                        "    }\n" + 
                        "}" 
                }, project)[3];
        
        // after the ITIT reference, we can't do any more code selection.
        // that is why commenting out the gets
      validateCodeSelect(unit, findRegion(unit, "Keys", 1), "Keys");
      validateCodeSelect(unit, findRegion(unit, "Keys", 2), "Keys");
      validateCodeSelect(unit, findRegion(unit, "CITY", 1), "CITY");
      validateCodeSelect(unit, findRegion(unit, "Keys", 3), "Keys");
      validateCodeSelect(unit, findRegion(unit, "CITY", 2), "CITY");
//      validateCodeSelect(unit, findRegion(unit, "get", 1), "get");
      validateCodeSelect(unit, findRegion(unit, "Keys", 4), "Keys");
      validateCodeSelect(unit, findRegion(unit, "CITY", 3), "CITY");
//      validateCodeSelect(unit, findRegion(unit, "get", 2), "get");
      validateCodeSelect(unit, findRegion(unit, "CITY", 4), "CITY");
//      validateCodeSelect(unit, findRegion(unit, "get", 3), "get");
    }
}
