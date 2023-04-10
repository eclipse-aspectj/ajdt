/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.core.tests;

import org.eclipse.core.runtime.IAdaptable;

/**
 *
 * @author Andrew Eisenberg
 * @created Dec 5, 2010
 */
public class AdaptableString implements IAdaptable {

    private final String value;

    public AdaptableString() {
        this.value = "";
    }
    public AdaptableString(String value) {
        this.value = value;
    }

    public Object getAdapter(Class adapter) {
        if (String.class == adapter)
            return value;
        return null;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AdaptableString other = (AdaptableString) obj;
        if (value == null) {
          return other.value == null;
        } else
          return value.equals(other.value);
    }

    @Override
    public String toString() {
      String builder = "AdaptableString [val=" +
              value +
                       "]";
        return builder;
    }
}
