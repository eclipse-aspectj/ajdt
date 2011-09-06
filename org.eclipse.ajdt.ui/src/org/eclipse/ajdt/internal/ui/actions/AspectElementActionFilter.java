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

package org.eclipse.ajdt.internal.ui.actions;

import org.eclipse.ajdt.core.javaelements.AspectJMemberElement;
import org.eclipse.jdt.internal.debug.ui.MemberActionFilter;
import org.eclipse.ui.IActionFilter;

/**
 * @author Andrew Eisenberg
 * @created Jul 3, 2009
 *
 * Action filter for Aspect Elements
 */
public class AspectElementActionFilter extends MemberActionFilter implements IActionFilter {

    /**
     * Returns true when the for the name "aspectElement" and the value "true"
     * if the target is an AspectJMemberElement
     */
    public boolean testAttribute(Object target, String name, String value) {
        if (target instanceof AspectJMemberElement && name.equals("aspectElement") && value.equals("true")) {
            return true;
        }
        return super.testAttribute(target, name, value);
    }

}
