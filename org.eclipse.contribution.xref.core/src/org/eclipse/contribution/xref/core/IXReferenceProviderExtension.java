/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.contribution.xref.core;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Extension to provide type safe access to xreferences
 * @author Andrew Eisenberg
 * @created Dec 6, 2010
 */
public interface IXReferenceProviderExtension extends IXReferenceProvider {
  /**
  * Get the collection of {@link IXReference}s for the Object o. "o" is 
  * guaranteed to be non-null and of a type returned by getClasses.
  * This method will be called in "user time" and should have a 
  * sub-second response time. To contribute cross references that cannot 
  * guarantee to be computed in that timeframe, return an 
  * <code>IDeferredXReference</code>. See the 
  * <code>org.eclipse.contributions.xref.internal.providers.ProjectReferencesProvider</code> 
  * for an example of a provider that uses this technique.
  * @param o the object to get cross references for
  * @return IXReference collection of cross references for "o". If there
  * are no cross references to be contributed, either an empty collection or
  * null is an acceptable return value.
  */
 public Collection<IXReference> getXReferences(IAdaptable o, List<String> l);

}
