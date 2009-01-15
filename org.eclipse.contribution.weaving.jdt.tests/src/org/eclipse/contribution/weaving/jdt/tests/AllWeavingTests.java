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

import org.eclipse.contribution.weaving.jdt.tests.cuprovider.CompilationUnitProviderTests;
import org.eclipse.contribution.weaving.jdt.tests.imagedescriptor.ImageDescriptorSelectorTests;
import org.eclipse.contribution.weaving.jdt.tests.sourceprovider.SourceTransformerTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Andrew Eisenberg
 * @created Jan 5, 2009
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ CompilationUnitProviderTests.class, SourceTransformerTests.class, ImageDescriptorSelectorTests.class })
public class AllWeavingTests {
}
