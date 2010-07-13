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

package org.eclipse.ajdt.ui.tests;

import java.io.StringReader;

/**
 * @author Andrew Eisenberg
 * @created May 26, 2010
 *
 */
public class StringInputStream extends ReaderInputStream {

    public StringInputStream(String s) {
        super(new StringReader(s));
    }

}
