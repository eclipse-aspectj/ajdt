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

import org.eclipse.contribution.jdt.itdawareness.IJavaContentAssistProvider;
import org.eclipse.core.runtime.IAdapterFactory;

public class ContentAssistProviderAdapterFactory implements IAdapterFactory {
  @Override
  public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
    if (adapterType == IJavaContentAssistProvider.class)
      return adapterType.cast(new ContentAssistProvider());
    return null;
  }

  @Override
  public Class<?>[] getAdapterList() {
    return new Class<?>[] { IJavaContentAssistProvider.class };
  }
}
