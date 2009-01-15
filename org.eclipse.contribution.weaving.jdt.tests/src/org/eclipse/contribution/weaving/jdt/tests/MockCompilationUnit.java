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

package org.eclipse.contribution.weaving.jdt.tests;

import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.PackageFragment;

/**
 * @author Andrew Eisenberg
 * @created Jan 5, 2009
 *
 */
public class MockCompilationUnit extends CompilationUnit {

    public MockCompilationUnit(PackageFragment parent, String name,
            WorkingCopyOwner owner) {
        super(parent, name, owner);
    }

    @Override
    protected char getHandleMementoDelimiter() {
        return '|';
    }
}
