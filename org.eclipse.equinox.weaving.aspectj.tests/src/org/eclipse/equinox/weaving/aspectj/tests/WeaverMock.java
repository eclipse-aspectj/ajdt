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

import org.aspectj.weaver.bcel.BcelWeaver;
import org.aspectj.weaver.bcel.BcelWorld;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.aspectj.weaver.IClassFileProvider;
import org.aspectj.weaver.IUnwovenClassFile;
import org.aspectj.weaver.bcel.BcelObjectType;
import org.aspectj.weaver.bcel.UnwovenClassFile;

/**
 * Mock of the BcelWeaver that does not actually weave, but only causes the effect that weaving a class
 * has on the generatedClasses map.
 * 
 * See https://github.com/eclipse-aspectj/ajdt/issues/45 for the explanation.
 * 
 * @author Stefan Winkler <stefan@winklerweb.net>
 */
public class WeaverMock extends BcelWeaver {
    
    /**
     * Constructor. We use an uninitialized BcelWorld as parameter, which is sufficient for our test case.
     */
    public WeaverMock() {
        super(new BcelWorld());
    }

    @Override
    public Collection<String> weave(IClassFileProvider input) throws IOException {
        // We simulate what happens to the generatedClass field when we are weaving a class with 
        // and aspects applied to it. (I.e., we simulate the generation of two aspect closures)

        IUnwovenClassFile targetClass = input.getClassFileIterator().next();
        // first the target class is offered to the requestor (see WeavingAdaptor:898)
        input.getRequestor().acceptResult(targetClass);
        
        // then the generated closure classes of the target class are offered to the requestor (see WeavingAdaptor:905)
        String baseName = targetClass.getClassName();
        input.getRequestor().acceptResult(new UnwovenClassFile(targetClass.getFilename(), baseName + "$AjcClosure1", new byte[0]));
        input.getRequestor().acceptResult(new UnwovenClassFile(targetClass.getFilename(), baseName + "$AjcClosure3", new byte[0]));

        // the caller does not care about the return value, so we just return the empty list
        return Collections.emptyList();
    }
}
