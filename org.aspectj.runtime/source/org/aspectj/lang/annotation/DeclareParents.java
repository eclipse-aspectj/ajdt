/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 * initial implementation              Adrian Colyer
 *******************************************************************************/
package org.aspectj.lang.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Declare parents mixin annotation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DeclareParents {

    /**
     * @return the target types expression
     */
    String value();

    /**
     * Optional class defining default implementation
     * of interface members (equivalent to defining
     * a set of interface member ITDs for the
     * public methods of the interface).
     *
     * @return define the default implementation of interface members (should always be specified)
     */
    Class defaultImpl() default DeclareParents.class;

    // note - a default of "null" is not allowed,
    // hence the strange default given above.
}
