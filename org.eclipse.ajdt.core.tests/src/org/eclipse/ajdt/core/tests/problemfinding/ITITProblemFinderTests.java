/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.ajdt.core.tests.problemfinding;

import java.util.List;

import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Tests problem finding with intertype inner types
 * @author Andrew Eisenberg
 * @created Feb 2, 2010
 */
public class ITITProblemFinderTests extends AbstractProblemFindingTests {
    public void testITITReferenceInThirdClass() throws Exception {
        assertNoProblems(
                new String[] { "p", "p", "p", "q" }, 
                new String[] { "CityAspect.aj", "Function.java", "City.java", "Test.java" }, 
                new String[] {
                        "package p;\n" + 
                        "import java.util.List;" + 
                        "privileged aspect CityAspect {\n" + 
                        "    public static class City.Keys {\n" + 
                        "        public static final Function<List<String>, City> CITY = null;\n" + 
                        "        public static final Function<java.util.HashMap<String, String>, City> CITY2 = null;\n" +
                        "        public static final List<String> xxx(int y, String z) { return null; }" +
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
                        "        City.Keys.xxx(0, null).get(0).substring(0);   \n" + 
                        "        p.City.Keys.CITY2.get().put(null, null);   \n" + 
                        "    }\n" + 
                        "}"
                });
        checkModel();
    }
    
    public void testITITReferenceWithOddImportStatements() throws Exception {
        assertNoProblems(
                new String[] { "p", "p", "p", "q" }, 
                new String[] { "CityAspect.aj", "Function.java", "City.java", "Test.java" }, 
                new String[] {
                        "package p;\n" + 
                        "import java.util.List;" + 
                        "privileged aspect CityAspect {\n" + 
                        "    public static class City.Keys {\n" + 
                        "        public static final Function<List<String>, City> CITY = null;\n" + 
                        "        public static final Function<java.util.HashMap<String, String>, City> CITY2 = null;\n" + 
                        "        public static final List<String> xxx(int y, String z) { return null; }" +
                        "    }\n" + 
                        "    void x() {\n" + 
                        "        City.Keys.CITY.get();\n" + 
                        "    }\n" + 
                        "}",
                        
                        "package p;\n" + 
                        "public class Function<K, V> {\n" + 
                        "    public void get() { }\n" + 
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
                        "        City.Keys.xxx(0, null).get(0).substring(0);   \n" + 
                        "    }\n" + 
                        "}" 
                });
        
        checkModel();
    }

    public void testITITReferenceInTargetType() throws Exception {
        assertNoProblems(
                new String[] { "p", "p", "p" }, 
                new String[] { "CityAspect.aj", "Function.java", "City.java" }, 
                new String[] {
                        "package p;\n" + 
                        "import java.util.List;" + 
                        "privileged aspect CityAspect {\n" + 
                        "    public static class City.Keys {\n" + 
                        "        public static final Function<Object, City> CITY = null;\n" + 
                        "        public static final List<String> xxx(int y, String z) { return null; }" +
                        "    }\n" + 
                        "    void x() {\n" + 
                        "        City.Keys.CITY.get();\n" + 
                        "    }\n" + 
                        "}",
                        
                        "package p;\n" + 
                        "public class Function<K, V> {\n" + 
                        "    public void get() { }\n" + 
                        "}",
                        
                        "package p;\n" +
                        "public class City {\n" +
                        "   void x() { \n" +
                        "      City.Keys.CITY.get();\n" +
                        "        City.Keys.xxx(0, null).get(0).substring(0);   \n" + 
                        "  }\n" +
                        "}",
                });
        checkModel();
    }

    public void testITITReferenceInTargetTypeWithOddImportStatements() throws Exception {
        assertNoProblems(
                new String[] { "p", "p", "p" }, 
                new String[] { "CityAspect.aj", "Function.java", "City.java" }, 
                new String[] {
                        "package p;\n" + 
                        "import java.util.List;" + 
                        "privileged aspect CityAspect {\n" + 
                        "    public static class City.Keys {\n" + 
                        "        public static final Function<Object, City> CITY = null;\n" + 
                        "        public static final List<String> xxx(int y, String z) { return null; }" +
                        "    }\n" + 
                        "    void x() {\n" + 
                        "        City.Keys.CITY.get();\n" + 
                        "    }\n" + 
                        "}",
                        
                        "package p;\n" + 
                        "public class Function<K, V> {\n" + 
                        "    public void get() { }\n" + 
                        "}",
                        
                        "package p;\n" +
                        "import p.City;\n" + 
                        "import p.City.Keys;\n" + 
                        "import static p.City.Keys.CITY;\n" + 
                        "public class City {\n" +
                        "   void x() { \n" +
                        "     City.Keys.CITY.get();\n" +
                        "     Keys.CITY.get();\n" +
                        "     CITY.get();\n" +
                        "        City.Keys.xxx(0, null).get(0).substring(0);   \n" + 
                        "   }\n" +
                        "}",
                });
        checkModel();
    }

    protected void checkModel() throws JavaModelException {
        AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(proj);
        IType cityType = proj.findType("p.City");
        List<IJavaElement> rels = model.getRelationshipsForElement(cityType, AJRelationshipManager.ASPECT_DECLARATIONS, false);
        assertEquals("Should have found exactly one relationship to target type\n" + rels, 1, rels.size());
        IJavaElement elt = rels.get(0);
        assertTrue("Relationship should be to a type " + elt, elt instanceof IType);
        assertTrue("Element should exist " + elt, elt.exists());
        assertEquals("wrong name for element", "Keys", elt.getElementName());
        
        rels = model.getRelationshipsForElement(elt, AJRelationshipManager.DECLARED_ON, false);
        assertEquals("Should have found exactly one relationship to target type\n" + rels, 1, rels.size());
        assertEquals(cityType, rels.get(0));
        
        assertEquals("Should have exactly 2 relationships in the entire project", 2, model.getRelationshipsForProject(AJRelationshipManager.getAllRelationshipTypes()).size());
    }
}
