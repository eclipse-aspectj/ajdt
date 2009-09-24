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

package org.eclipse.ajdt.core.tests.codeconversion;

import junit.framework.TestCase;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/**
 * @author Andrew Eisenberg
 * @created Jul 9, 2009
 * 
 * Tests that content assist in ITDs are working properly
 * specifically, this tests if the extra filtering for
 * target types are turned on or not
 */
public class Bug279974Tests extends AJDTCoreTestCase {
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    static class MockAJCompilationUnit extends AJCompilationUnit {

        public MockAJCompilationUnit() {
            super(ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("")));
        }

        // make accessible
        protected static boolean positionIsAtDottedExpression(String source,
                int posInSource) {
            return AJCompilationUnit.positionIsAtDottedExpression(source, posInSource);
        }
        
    }
    
    void expectingTrue(String source) {
        if (!MockAJCompilationUnit.positionIsAtDottedExpression(source, source.indexOf("<here>"))) {
            fail("Should not have found a dotted expression in:\n" + source);
        }
    }
    void expectingFalse(String source) {
        if (MockAJCompilationUnit.positionIsAtDottedExpression(source, source.indexOf("<here>"))) {
            fail("Should have found a dotted expression in:\n" + source);
        }
    }
    
    
    public void testPosIsAtDottedExpression1() throws Exception {
        expectingTrue("foo.<here>");
    }
    public void testPosIsAtDottedExpression2() throws Exception {
        expectingTrue("foo.b<here>");
    }
    public void testPosIsAtDottedExpression3() throws Exception {
        expectingTrue("foo.     <here>");
    }
    public void testPosIsAtDottedExpression4() throws Exception {
        expectingTrue("foo.  b<here>");
    }
    public void testPosIsAtDottedExpression5() throws Exception {
        expectingTrue("fdsdsa  foo.     <here>");
    }
    public void testPosIsAtDottedExpression6() throws Exception {
        expectingTrue("fadsfdsdfs  foo.  b<here>");
    }
    public void testPosIsAtDottedExpression7() throws Exception {
        expectingTrue("?foo.     <here>");
    }
    public void testPosIsAtDottedExpression8() throws Exception {
        expectingTrue("?foo.  b<here>");
    }
    public void testPosIsAtDottedExpression9() throws Exception {
        expectingTrue("this.foo.b<here>");
    }

    
    public void testPosIsAtDottedExpressionFalse1() throws Exception {
        expectingFalse("b<here>");
    }
    public void testPosIsAtDottedExpressionFalse2() throws Exception {
        expectingFalse("b<here>");
    }
    public void testPosIsAtDottedExpressionFalse3() throws Exception {
        expectingFalse("  b<here>");
    }
    public void testPosIsAtDottedExpressionFalse4() throws Exception {
        expectingFalse("  this<here>");
    }
    public void testPosIsAtDottedExpressionFalse5() throws Exception {
        expectingFalse("this<here>");
    }
    public void testPosIsAtDottedExpressionFalse6() throws Exception {
        expectingFalse("fdadsfd  b<here>");
    }
    public void testPosIsAtDottedExpressionFalse7() throws Exception {
        expectingFalse("fsdfdsafdas  this<here>");
    }
    public void testPosIsAtDottedExpressionFalse8() throws Exception {
        expectingFalse("?b<here>");
    }
    public void testPosIsAtDottedExpressionFalse9() throws Exception {
        expectingFalse("?<here>");
    }
    public void testPosIsAtDottedExpressionFalse10() throws Exception {
        expectingFalse("?this<here>");
    }
    public void testPosIsAtDottedExpressionFalse11() throws Exception {
        expectingFalse("this   <here>");
    }
    public void testPosIsAtDottedExpressionFalse12() throws Exception {
        expectingFalse("this.<here>");
    }
    public void testPosIsAtDottedExpressionFalse13() throws Exception {
        expectingFalse("this.f<here>");
    }
    public void testPosIsAtDottedExpressionFalse14() throws Exception {
        expectingFalse("this . <here>");
    }
    public void testPosIsAtDottedExpressionFalse15() throws Exception {
        expectingFalse("this . f<here>");
    }
}
