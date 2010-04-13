/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.ui;

import org.eclipse.contribution.jdt.itdawareness.INameEnvironmentProvider;
import org.eclipse.core.runtime.IAdapterFactory;

public class AJDTNameEnvironmentAdapterFactory implements IAdapterFactory {

    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adapterType == INameEnvironmentProvider.class) {
            return new AJDTNameEnvironmentProvider();
        }
        return null;
    }

    public Class[] getAdapterList() {
        return new Class[] { AJDTNameEnvironmentProvider.class };
    }

}
