/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   David Knibb               initial implementation
 *   Matthew Webster           Eclipse 3.2 changes
 *   Heiko Seeberger           AJDT 1.5.1 changes
 *   Martin Lippert            minor changes and bugfixes
 *   Martin Lippert            reworked
 *   Martin Lippert            caching of generated classes
 *   Martin Lippert            added locking for weaving
 *   Stefan Winkler            fixed issue with generated classes in hierarchies
 *******************************************************************************/

package org.eclipse.equinox.weaving.aspectj.loadtime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.aspectj.weaver.IUnwovenClassFile;
import org.aspectj.weaver.bcel.BcelWeakClassLoaderReference;
import org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptor;
import org.aspectj.weaver.tools.GeneratedClassHandler;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;
import org.eclipse.equinox.weaving.aspectj.AspectJWeavingStarter;

/**
 * The weaving adaptor for AspectJs load-time weaving API that deals with the
 * OSGi specifics for load-time weaving
 */
public class OSGiWeavingAdaptor extends ClassLoaderWeavingAdaptor {

    /**
     * internal class to collect generated classes (produced by the weaving) to
     * define then after the weaving itself
     */
    static class GeneratedClass {

        private final byte[] bytes;

        private final String name;

        public GeneratedClass(final String name, final byte[] bytes) {
            this.name = name;
            this.bytes = bytes;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * generated class handler to collect generated classes (produced by the
     * weaving) to define then after the weaving itself
     */
    class OSGiGeneratedClassHandler implements GeneratedClassHandler {

        private final ConcurrentLinkedQueue<GeneratedClass> classesToBeDefined;

        private final BcelWeakClassLoaderReference loaderRef;

        public OSGiGeneratedClassHandler(final ClassLoader loader) {
            loaderRef = new BcelWeakClassLoaderReference(loader);
            classesToBeDefined = new ConcurrentLinkedQueue<>();
        }

        /**
         * Callback when we need to define a generated class in the JVM (version
         * for older AspectJ versions)
         */
        public void acceptClass(final String name, final byte[] bytes) {
            try {
                if (shouldDump(name.replace('/', '.'), false)) {
                    dump(name, bytes, false);
                }
            } catch (final Throwable throwable) {
                throwable.printStackTrace();
            }
            classesToBeDefined.offer(new GeneratedClass(name, bytes));
        }

        /**
         * Callback when we need to define a generated class in the JVM (version
         * for newer AspectJ versions, but we can ignore the originalBytes here)
         */
        public void acceptClass(final String name, final byte[] originalBytes, final byte[] weavedBytes) {
            acceptClass(name, weavedBytes);
        }

        public void defineGeneratedClasses() {
            while (!classesToBeDefined.isEmpty()) {
                final GeneratedClass generatedClass = classesToBeDefined.poll();
                if (generatedClass != null) {
                    defineClass(loaderRef.getClassLoader(), generatedClass.getName(), generatedClass.getBytes());
                } else {
                    break;
                }
            }
        }

    }

    private static Trace trace = TraceFactory.getTraceFactory().getTrace(ClassLoaderWeavingAdaptor.class);

    private final ClassLoader classLoader;
    private boolean initialized;
    private final Object initializeLock = new Object();
    private final String namespace;
    private final OSGiWeavingContext weavingContext;

    /**
     * The OSGi weaving adaptor provides a bridge to the AspectJ weaving adaptor
     * implementation for general classloaders. This weaving adaptor exists per
     * bundle that should be woven.
     *
     * @param loader The classloader of the bundle to be woven
     * @param context The bridge to the weaving context
     * @param namespace The namespace of this adaptor, some kind of unique ID
     *            for this weaver
     */
    public OSGiWeavingAdaptor(final ClassLoader loader, final OSGiWeavingContext context, final String namespace) {
        super();
        this.classLoader = loader;
        this.weavingContext = context;
        this.namespace = namespace;
    }

    protected void defineClass(final ClassLoader loader, final String name, final byte[] bytes) {
        if (trace.isTraceEnabled()) {
            trace.enter("defineClass", this,
                    new Object[] { loader, name, bytes });
        }
        debug("generating class '" + name + "'");

        try {
            super.defineClass(loader, name, bytes);
        } catch (final Exception e) {
            warn("define generated class failed", e);
        }

        if (trace.isTraceEnabled()) {
            trace.exit("defineClass", name);
        }
    }

    /**
     * In some situations, weaving creates new classes on the fly that are not part of the original bundle. This is the
     * case when the weaver needs to create closure-like constructs for the woven code.
     * <p>
     * This method returns a map of the generated classes (name -> bytecode) and  flushes the internal cache afterward
     * to avoid memory damage over time.
     *
     * @param className name of the class for which additional classes might have got generated
     *
     * @return map of generated class names and bytecodes for those generated classes
     */
    public Map<String, byte[]> getGeneratedClassesFor(final String className) {
        final Map<String, IUnwovenClassFile> generated = this.generatedClasses;
        final Map<String, byte[]> result = new HashMap<>();

        for (Map.Entry<String, IUnwovenClassFile> entry : generated.entrySet()) {
            final String name = entry.getKey();
            final IUnwovenClassFile unwovenClass = entry.getValue();
            if (!name.equals(className) && name.equals(unwovenClass.getClassName())) {
              result.put(name, unwovenClass.getBytes());
            }
        }

        flushGeneratedClasses();
        return result;
    }

    /**
     * @see org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptor#getNamespace()
     */
    @Override
    public String getNamespace() {
        return namespace;
    }

    /**
     * initialize the weaving adaptor
     */
    public void initialize() {
        synchronized (initializeLock) {
            if (!initialized) {
                super.initialize(classLoader, weavingContext);
                this.generatedClassHandler = new OSGiGeneratedClassHandler(classLoader);
                initialized = true;
                if (AspectJWeavingStarter.verbose) {
                    System.err.println(
                        "[org.eclipse.equinox.weaving.aspectj] info " + (isEnabled() ? "" : "not ") +
                        "weaving bundle '" + weavingContext.getClassLoaderName() + "'"
                    );
                }
            }
        }
    }

    /**
     * @see org.aspectj.weaver.tools.WeavingAdaptor#weaveClass(java.lang.String,
     *      byte[], boolean)
     */
    @Override
    public byte[] weaveClass(final String name, byte[] bytes, final boolean mustWeave) throws IOException {

        /* Avoid recursion during adaptor initialization */
        synchronized (initializeLock) {
            if (!initialized) {
                super.initialize(classLoader, weavingContext);
                this.generatedClassHandler = new OSGiGeneratedClassHandler(classLoader);
                initialized = true;
            }
        }

        synchronized (this) {
            byte[] wovenBytes = super.weaveClass(name, bytes, mustWeave);
            // Since 1.9.21.2, the AspectJ weaver, like other canonical Java instrumentation agents, returns null,
            // if nothing was woven. We accommodate to that change here.
            if (wovenBytes != null)
                bytes = wovenBytes;
        }

        ((OSGiGeneratedClassHandler) this.generatedClassHandler).defineGeneratedClasses();

        return bytes;
    }

}
