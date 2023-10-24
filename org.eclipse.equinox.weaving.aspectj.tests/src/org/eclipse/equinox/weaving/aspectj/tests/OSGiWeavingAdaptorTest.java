/*******************************************************************************
 * Copyright (c) 2023 Stefan Winkler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Winkler               initial implementation
 *******************************************************************************/
package org.eclipse.equinox.weaving.aspectj.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import junit.framework.TestCase;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;

import org.aspectj.weaver.tools.WeavingAdaptor;
import org.aspectj.weaver.tools.GeneratedClassHandler;
import org.eclipse.equinox.weaving.aspectj.loadtime.OSGiWeavingAdaptor;

/**
 * Test for the OSGiWeavingAdaptor
 *
 * @author Stefan Winkler <stefan@winklerweb.net>
 */
public class OSGiWeavingAdaptorTest extends TestCase {

    /**
     * Test for https://github.com/eclipse-aspectj/ajdt/issues/45
     */
    public void testGeneratedClasses() throws Exception {
        // GIVEN:
        // - an OSGiWeavingAdaptor instance
        final OSGiWeavingAdaptor adaptor = new OSGiWeavingAdaptor(null, null, "dummy");

        // - with the weaver set to a mock that fills the generatedClasses list
        //   (see documentation there)
        Field weaverField = WeavingAdaptor.class.getDeclaredField("weaver");
        weaverField.setAccessible(true);
        weaverField.set(adaptor, new WeaverMock());

        // - and generatedClassHandler set to an empty mock
        Field generatedClassHandlerField = WeavingAdaptor.class.getDeclaredField("generatedClassHandler");
        generatedClassHandlerField.setAccessible(true);
        generatedClassHandlerField.set(adaptor, new GeneratedClassHandler() {
            @Override
            public void acceptClass(String name, byte[] originalBytes,
                    byte[] weavedBytes) {
            }
        });

        // WHEN:
        // - the class is woven

        //   (Trying to mock the handling is too complex, so instead, we provide a valid .class file
        //   here. This is enough to not throw an exception along the way. Other than that, we don't care about the
        //   classBytes. It's the keys of the generatedClasses field set by the getWovenBytes() method, we are interested in...)
        //   So, read our own .class file here:
        byte[] classBytes;
        try (InputStream is = getClass().getResourceAsStream("OSGiWeavingAdaptorTest.class")) {
            classBytes = is.readAllBytes();
        }

        //   and call the method getWovenBytes("my.foo.MyClass", classBytes) - this is private, so do it via reflection
        //   (actually, the method weaveClass() is invoked, but getWovenBytes() is easier to call in the test, because
        //   we don't need to initialize all the logging, caching, ... facilities)
        Method getWovenBytes = WeavingAdaptor.class
                .getDeclaredMethod("getWovenBytes", String.class, byte[].class);
        getWovenBytes.setAccessible(true);
        getWovenBytes.invoke(adaptor, "my.foo.MyClass", classBytes); // WeavingAdaptor:385

        // - then the woven class would be defined, which causes another call to getWovenBytes()
        //   but this time for the base class. Before that call, the delegateForCurrentClass is reset to null

        Field delegateForCurrentClassField = WeavingAdaptor.class.getDeclaredField("delegateForCurrentClass");
        delegateForCurrentClassField.setAccessible(true);
        delegateForCurrentClassField.set(adaptor, null); // WeavingAdaptor:356 / WeavingAdaptor:421
        getWovenBytes.invoke(adaptor, "my.foo.MyBaseClass", classBytes); // WeavingAdaptor:385

        // - and then the generated classes are read
        Map<String, byte[]> actualResult = adaptor.getGeneratedClassesFor("my.foo.MyClass");

        // THEN:
        // - the result should not include the non-generated class, only the four generated ones

        // build a string representation to provide details in the assertion message
        StringBuilder actualContentsBuilder = new StringBuilder("[");
        for (String key : actualResult.keySet()) {
            actualContentsBuilder.append(key).append(",");
        }
        actualContentsBuilder.setCharAt(actualContentsBuilder.length() - 1, ']');
        String actualContents = actualContentsBuilder.toString();

        // assert our expected result
        assertTrue("my.foo.MyClass$AjcClosure1 is not contained in " + actualContents,
                actualResult.containsKey("my.foo.MyClass$AjcClosure1"));
        assertTrue("my.foo.MyClass$AjcClosure3 is not contained in " + actualContents,
                actualResult.containsKey("my.foo.MyClass$AjcClosure3"));
        assertTrue("my.foo.MyBaseClass$AjcClosure1 is not contained in " + actualContents,
                actualResult.containsKey("my.foo.MyBaseClass$AjcClosure1"));
        assertTrue("my.foo.MyBaseClass$AjcClosure3 is not contained in " + actualContents,
                actualResult.containsKey("my.foo.MyBaseClass$AjcClosure3"));
        assertEquals("Expected size 4 of result " + actualContents,
                4, actualResult.size());
    }
}
