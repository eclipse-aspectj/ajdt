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
package org.eclipse.ajdt.internal.core.contentassist;

import org.eclipse.contribution.jdt.itdawareness.ContentAssistAdapter;
import org.eclipse.contribution.jdt.itdawareness.IJavaContentAssistProvider;
import org.eclipse.core.runtime.IAdapterFactory;

@SuppressWarnings("unchecked")
public class ContentAssistProviderAdapterFactory implements IAdapterFactory {

    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adapterType == IJavaContentAssistProvider.class) {
            return new ContentAssistProvider();
        }
        return null;
    }

    public Class[] getAdapterList() {
        return new Class[] { IJavaContentAssistProvider.class };
    }

}
