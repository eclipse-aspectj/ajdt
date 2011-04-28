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

/**
 * 
 * @author Andrew Eisenberg
 * @created Nov 24, 2010
 */
public class ExtraAspectMethodProblemFinderTests extends AbstractProblemFindingTests {
    public void testNoProblemsHasAspect() throws Exception {
        assertNoProblems(
                new String[] { "p", "q" }, 
                new String[] { "Aspect.aj", "Java.java" }, 
                new String[] {
                        "package p;\n" +
                        "public aspect Aspect {\n" +
                        "  void x() { \n" +
                        "    hasAspect();\n" +
                        "    this.hasAspect();\n" +
                        "    Aspect.hasAspect();\n" +
                        "  }" +
                        "}",
                
                        "package q;\n" +
                        "import p.Aspect;" +
                        "public class Java {\n" +
                        "  void x() { \n" +
                        "    Aspect.hasAspect();\n" +
                        "    p.Aspect.hasAspect();\n" +
                        "  }" +
                        "}"
                });
    }

    public void testNoProblemsAspectOf() throws Exception {
        assertNoProblems(
                new String[] { "p", "q" }, 
                new String[] { "Aspect.aj", "Java.java" }, 
                new String[] {
                        "package p;\n" +
                        "public aspect Aspect {\n" +
                        "  void x() { \n" +
                        "    aspectOf();\n" +
                        "    this.aspectOf();\n" +
                        "    Aspect.aspectOf();\n" +
                        "  }" +
                        "}",
                        
                        "package q;\n" +
                        "import p.Aspect;" +
                        "public class Java {\n" +
                        "  void x() { \n" +
                        "    Aspect.aspectOf();\n" +
                        "    p.Aspect.aspectOf();\n" +
                        "  }" +
                        "}"
                });
    }

    public void testNoProblemsGetWithinTypeName() throws Exception {
        assertNoProblems(
                new String[] { "p", "q" }, 
                new String[] { "Aspect.aj", "Java.java" }, 
                new String[] {
                        "package p;\n" +
                        "public aspect Aspect pertypewithin(p.*) {\n" +
                        "  void x() { \n" +
                        "    getWithinTypeName();\n" +
                        "    this.getWithinTypeName();\n" +
                        "  }" +
                        "}",
                        
                        "package q;\n" +
                        "import p.Aspect;" +
                        "public class Java {\n" +
                        "  void x(Aspect a) { \n" +
                        "    Aspect.aspectOf(Java.class).getWithinTypeName();\n" +
                        "    p.Aspect.aspectOf(Java.class).getWithinTypeName();\n" +
                        "    a.getWithinTypeName();\n" +
                        "  }" +
                        "}"
                });
    }


}
