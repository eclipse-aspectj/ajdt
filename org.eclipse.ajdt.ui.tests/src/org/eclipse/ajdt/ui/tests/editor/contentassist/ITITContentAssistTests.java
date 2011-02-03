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

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.internal.core.AJWorkingCopyOwner;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaModelManager;

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
        
        String contents = "package q;\n" +
        "import p.City;\n" +
        "public class Test {\n" +
        "   void x() { City.Keys.CITY.getter(); }\n" +
        "}";
        createUnits(
                new String[] { "p", "p", "p", "q" }, 
                new String[] { "AspectCity.aj", "Function.java", "City.java", "Test.java" }, 
                new String[] {
                        "package p;\n" + 
                        "privileged aspect AspectCity {\n" + 
                        "    public static class City.Keys {\n" + 
                        "        public static final Function<Object, City> CITY = null;\n" + 
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
                        "   void x() { City.Keys.CITY.getter(); }\n" +
                        "}",
                        contents,
                }, proj);
        
        ICompilationUnit unit = proj.findType("q.Test").getCompilationUnit();
        assertContentAssist(unit, contents.indexOf("Y.g"), "CITY");
        assertContentAssist(unit, contents.indexOf("er()"), "getter");
        assertContentAssist(unit, contents.indexOf("s.CITY"), "Keys");
        assertContentAssist(unit, contents.indexOf("y.Keys"), "City");
    }
    
    // FIXADE this test is disabled, but we should look into why it is failing. 
    public void _testContentAssistInAspect() throws Exception {
        
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
        assertContentAssist(unit, contents.indexOf("er()"), "getter");
        assertContentAssist(unit, contents.indexOf("s.CITY"), "Keys");
        assertContentAssist(unit, contents.indexOf("y.Keys"), "City");
        assertContentAssist(unit, contents.indexOf("Y.g"), "CITY");
    }


    
    // This is not working yet. See Bug 336185
    public void _testContentAssistInTargetType() throws Exception {
        String contents = "package p;\n" +
        "public class City {\n" +
        "   void x() { City.Keys.CITY.getter(); }\n" +
        "}";
        
        createUnits(
                new String[] { "p", "p", "p" }, 
                new String[] { "AspectCity.aj", "Function.java", "City.java" }, 
                new String[] {
                        "package p;\n" + 
                        "privileged aspect AspectCity {\n" + 
                        "    public static class City.Keys {\n" + 
                        "        public static final Function<Object, City> CITY = null;\n" + 
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
                }, proj);
        
        ICompilationUnit unit = proj.findType("p.City").getCompilationUnit();
        assertContentAssist(unit, contents.indexOf("er()"), "getter");
        assertContentAssist(unit, contents.indexOf("s.CITY"), "Keys");
        assertContentAssist(unit, contents.indexOf("y.Keys"), "City");
        assertContentAssist(unit, contents.indexOf("Y.g"), "CITY");
    }
    
    public void assertContentAssist(ICompilationUnit unit, int offset, String proposalName) throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        unit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("wrong proposal name", 
                proposalName, new String(completionProposal.getCompletion())); 
    }    
}