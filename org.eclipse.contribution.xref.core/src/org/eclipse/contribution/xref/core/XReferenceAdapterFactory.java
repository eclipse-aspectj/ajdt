/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.core;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * Adapter factory to support cross-references
 */
public class XReferenceAdapterFactory implements IAdapterFactory {
  public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
    if (!adapterType.equals(IXReferenceAdapter.class))
      return null;
    else if (adaptableObject instanceof XReferenceAdapter)
      return adapterType.cast(adaptableObject);
    else if (adaptableObject instanceof IAdaptable)
      return adapterType.cast(new XReferenceAdapter((IAdaptable) adaptableObject));
    return null;
  }

  @Override
  public Class<?>[] getAdapterList() {
    return new Class<?>[] { IXReferenceAdapter.class };
  }

}
