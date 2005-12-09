/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.buildconfig;

import java.util.List;

import org.eclipse.ajdt.core.CoreUtils.FilenameFilter;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

public interface IBuildConfiguration {

	public static final String EXTENSION = "ajproperties"; //$NON-NLS-1$

	public static final String STANDARD_BUILD_CONFIGURATION_NAME = UIMessages.buildConfig_standardFileName;

	public static final String STANDARD_BUILD_CONFIGURATION_FILE = STANDARD_BUILD_CONFIGURATION_NAME
		+ "." + EXTENSION; //$NON-NLS-1$

	public String getName();

	public IFile getFile();

	public List getIncludedJavaFiles(FilenameFilter filter) ;

	public boolean isIncluded(IResource correspondingResource);

	public List getIncludedJavaFileNames(FilenameFilter aspectj_source_filter);

	public void update(boolean b);
}
