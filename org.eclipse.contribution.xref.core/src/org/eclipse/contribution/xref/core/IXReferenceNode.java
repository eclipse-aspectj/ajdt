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

import org.eclipse.jdt.core.IJavaElement;

/**
 * A cross reference node - could be an associate of an IXReference.
 * By implementing this interface, contributors are able to navigate to
 * the required place if the information isn't easily represented as
 * an IJavaElement or an IResource.
 */
public interface IXReferenceNode {
    
    public IJavaElement getJavaElement();
    
}
