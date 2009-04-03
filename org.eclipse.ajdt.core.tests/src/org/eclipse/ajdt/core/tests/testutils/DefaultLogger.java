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

package org.eclipse.ajdt.core.tests.testutils;

import org.eclipse.ajdt.core.IAJLogger;

/**
 * @author Andrew Eisenberg
 * @created Apr 3, 2009
 *
 * Logs to stdout
 */
public class DefaultLogger implements IAJLogger {

    public void log(String msg) {
        System.out.println(msg);
    }

    public void log(int category, String msg) {
        System.out.println(msg);
    }

}
