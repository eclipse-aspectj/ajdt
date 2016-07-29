/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.editor.contentassist;

import org.eclipse.ajdt.internal.core.AJWorkingCopyOwner;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Tests for content assist of ITITs
 * @author Andrew Eisenberg
 *
 */
public class ITITContentAssistTests extends UITestCase {
    private IJavaProject proj;

    protected void setUp() throws Exception {
        super.setUp();
        proj = JavaCore.create(createPredefinedProject("DefaultEmptyProject"));
    }
    
    public void testContentAssistInOtherType() throws Exception {
        
        String contents =
        "package q;\n" + 
        "import p.City;\n" + 
        "public class Test {\n" + 
        "    public static void main(String[] args) { \n" + 
        "        City.Keys.CITY.getter().get(0).substring(0);   \n" + 
        "        p.City.Keys.A_CITY.getter().put(null, null);   \n" + 
        "    }\n" + 
        "}";

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
                            "        public static final Function<java.util.Map<String, String>, City> A_CITY = null;\n" + 
                            "    }\n" + 
                            "    void x() {\n" + 
                            "        City.Keys.CITY.getter();\n" + 
                            "    }\n" + 
                            "}",
                            
                            "package p;\n" + 
                            "public class Function<K, V> {\n" + 
                            "    public K getter() { return null; }\n" + 
                            "}",
                            
                            "package p;\n" +
                            "public class City { }",
                            contents
                    }, proj)[3];
        
        assertContentAssist(unit, findLocation(contents, "Keys", 1), "Keys");
        assertContentAssist(unit, findLocation(contents, "CITY", 1), "CITY");
        assertContentAssist(unit, findLocation(contents, "getter", 1), "getter");
        assertContentAssist(unit, findLocation(contents, "get", 2), "get", 2);
        assertContentAssist(unit, findLocation(contents, "substring", 1), "substring", 2);
        assertContentAssist(unit, findLocation(contents, "A_CITY", 1), "A_CITY");
        assertContentAssist(unit, findLocation(contents, "getter", 2), "getter");
        assertContentAssist(unit, findLocation(contents, "put", 1), "put", 3);

    }
    
    public void testContentAssistInAspect() throws Exception {
        
        String contents = 
        "package p;\n" + 
        "privileged aspect AspectCity {\n" + 
        "    void x() {\n" + 
        "        City.Keys.CITY.getter();\n" + 
        "    }\n" + 
        "    public static class City.Keys {\n" + 
        "        public static final Function<Object, City> CITY = null;\n" + 
        "    }\n" + 
        "}";
        createUnits(
                new String[] { "p", "p", "p" }, 
                new String[] { "AspectCity.aj", "Function.java", "City.java" }, 
                new String[] {
                        contents,
                        
                        "package p;\n" + 
                        "public class Function<K, V> {\n" + 
                        "    public void getter() { }\n" + 
                        "}",
                        "package p;\n" +
                        "public class City {\n" +
                        "}",
                        contents,
                }, proj);
        
        ICompilationUnit unit = proj.findType("p.AspectCity").getCompilationUnit();
        try {
            // must be a working copy or else AJCompilationUnit thinks
            // that we are not in an AJ editor
            unit.becomeWorkingCopy(null);
            assertContentAssist(unit, findLocation(contents, "getter"), "getter");
            assertContentAssist(unit, findLocation(contents, "Keys"), "Keys");
            assertContentAssist(unit, findLocation(contents, "City", 2), "City");
            assertContentAssist(unit, findLocation(contents, "CITY"), "CITY");
        } finally {
            unit.discardWorkingCopy();
        }
    }


    
    // This is not working yet. See Bug 336185
    public void testContentAssistInTargetType() throws Exception {
        String contents = "package p;\n" +
        "public class City {\n" +
        "   void x() {\n" +
        "      City.Keys.CITY.getter();\n" +
        "      City.Keys.xxx().get(\"\").charAt(0);" +
        "   }\n" +
        "}";
        
        ICompilationUnit unit = createUnits(
                new String[] { "p", "p", "p" }, 
                new String[] { "AspectCity.aj", "Function.java", "City.java" }, 
                new String[] {
                        "package p;\n" + 
                        "import java.util.List;" + 
                        "import java.util.Map;" + 
                        "privileged aspect AspectCity {\n" + 
                        "    public static class City.Keys {\n" + 
                        "        public static final Function<Object, City> CITY = null;\n" + 
                        "        public static final Map<String, String> xxx() { return null; }\n" + 
                        "    }\n" + 
                        "    void x() {\n" + 
                        "        City.Keys.CITY.getter();\n" + 
                        "    }\n" + 
                        "}",
                        
                        "package p;\n" + 
                        "public class Function<K, V> {\n" + 
                        "    public void getter() { }\n" + 
                        "}",
                        
                        contents,
                }, proj)[2];
        
        assertContentAssist(unit, findLocation(contents, "getter"), "getter");
        assertContentAssist(unit, findLocation(contents, "Keys"), "Keys");
        assertContentAssist(unit, findLocation(contents, "City", 2), "City");
        assertContentAssist(unit, findLocation(contents, "CITY"), "CITY");
        assertContentAssist(unit, findLocation(contents, "xxx"), "xxx");
        assertContentAssist(unit, findLocation(contents, "get", 2), "get", 3);
        assertContentAssist(unit, findLocation(contents, "charAt"), "charAt");
    }
    
    private void assertContentAssist(ICompilationUnit unit, int offset,
            String proposalName, int amount) throws JavaModelException {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        unit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Wrong number of proposals found, all proposals:\n" + requestor.toString(), amount, requestor.accepted.size());

        for (int i = 0; i < requestor.accepted.size(); i++) {
            CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(i);
            if (completionProposal.getCompletion() != null && String.valueOf(completionProposal.getCompletion()).equals(proposalName)) {
                return;
            }
        }
        fail("Proposal name " + proposalName + " not found.  All Proposals:\n" + requestor.toString()); 
    }

    private void assertContentAssist(ICompilationUnit unit, int offset, String proposalName) throws Exception {
        assertContentAssist(unit, offset, proposalName, 1);
    }    
    
    
    protected final int findLocation(String contents, String toFind) {
        return findLocation(contents, toFind, 1);
    }
    protected final int findLocation(String contents, String toFind,
            int occurrence) {
        int start = 0;
        while (occurrence-- > 0) {
            start = contents.indexOf(toFind, start + 1);
            if (start < 0)
                fail("Too few occurrences of '" + toFind + "' where found");
        }
        return start + toFind.length();
    }

}