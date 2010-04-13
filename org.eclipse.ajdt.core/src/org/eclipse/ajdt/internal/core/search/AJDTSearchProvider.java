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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.core.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.core.codeconversion.ConversionOptions;
import org.eclipse.ajdt.core.codeconversion.ITDAwareLookupEnvironment;
import org.eclipse.ajdt.core.codeconversion.ITDAwareNameEnvironment;
import org.eclipse.ajdt.core.codeconversion.AspectsConvertingParser.Replacement;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElementInfo;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.contribution.jdt.itdawareness.ISearchProvider;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;

/**
 * 
 * @author Andrew Eisenberg
 * @created Mar 8, 2010
 */
public class AJDTSearchProvider implements ISearchProvider {
//    private ArrayList<Replacement> replacements;
    
    public char[] translateForMatchProcessing(char[] original, CompilationUnit unit) {
        if (unit instanceof AJCompilationUnit ) {
            return unit.getContents();
        } else {
            return original;
        }
    }

    public int translateLocationToOriginal(int translatedLocation) {
        return translatedLocation;
//        return AspectsConvertingParser.translatePositionToBeforeChanges(translatedLocation, replacements);
    }

    public IJavaElement convertJavaElement(IJavaElement origElement) {
        if (origElement instanceof IntertypeElement) {
            try {
                char[] targetTypeName = ((IntertypeElementInfo) ((IntertypeElement) origElement).getElementInfo()).getTargetType();
                if (targetTypeName != null) {
                    AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(origElement);
                    List<IJavaElement> rels = model.getRelationshipsForElement(origElement, AJRelationshipManager.DECLARED_ON);
                    if (rels.size() > 0 && rels.get(0) instanceof IType) {
                        IType targetType = (IType) rels.get(0);
                        IJavaElement newElement = ((IntertypeElement) origElement).createMockDeclaration(targetType);
                        if (newElement != null) {
                            return newElement;
                        }
                    }
                }
            } catch (JavaModelException e) {
            }
        }
        return origElement;
    }

    public LookupEnvironment createLookupEnvironment(
            LookupEnvironment orig,
            ICompilationUnit[] workingCopies, JavaProject project) {
        ITDAwareNameEnvironment env;
        try {
            env = new ITDAwareNameEnvironment(project, workingCopies);
            return new ITDAwareLookupEnvironment(orig, env);
        } catch (JavaModelException e) {
        }
        return orig;
    }

}
