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
package org.eclipse.ajdt.internal.core.search;

import org.eclipse.contribution.jdt.itdawareness.ISearchProvider;
import org.eclipse.core.runtime.IAdapterFactory;

public class SearchAdapterFactory implements IAdapterFactory {
  @Override
  public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
    if (adapterType == ISearchProvider.class)
      return adapterType.cast(new AJDTSearchProvider());
    return null;
  }

  @Override
  public Class<?>[] getAdapterList() {
    return new Class<?>[] { ISearchProvider.class };
  }
}
