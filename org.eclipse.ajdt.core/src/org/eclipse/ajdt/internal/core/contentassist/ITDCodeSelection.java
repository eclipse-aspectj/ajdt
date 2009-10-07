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

package org.eclipse.ajdt.internal.core.contentassist;

import java.util.ArrayList;

import org.eclipse.ajdt.core.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.core.codeconversion.ConversionOptions;
import org.eclipse.ajdt.core.codeconversion.ITDAwareNameEnvironment;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.text.ITDAwareSelectionRequestor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.codeassist.SelectionEngine;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jface.text.IRegion;

/**
 * @author Andrew Eisenberg
 * @created Jun 6, 2009
 * Provides ITD Aware code selection
 */
public class ITDCodeSelection {
    private final ICompilationUnit unit;
    public ITDCodeSelection(ICompilationUnit unit) {
        this.unit = unit;
    }
    
    public IJavaElement[] findJavaElement(IRegion wordRegion)
            throws JavaModelException {
        JavaProject javaProject = (JavaProject) unit.getJavaProject();
        SearchableEnvironment environment = new ITDAwareNameEnvironment(javaProject, unit.getOwner(), null);

        ITDAwareSelectionRequestor requestor = new ITDAwareSelectionRequestor(AJProjectModelFactory.getInstance().getModelForJavaElement(javaProject), unit);
        /* AJDT 1.7 */
        SelectionEngine engine = new SelectionEngine(environment, requestor, javaProject.getOptions(true), unit.getOwner());
        
        final AspectsConvertingParser converter = new AspectsConvertingParser(((CompilationUnit) unit).getContents());
        converter.setUnit(unit);
        ArrayList replacements = converter.convert(ConversionOptions.CODE_COMPLETION);
        
        org.eclipse.jdt.internal.compiler.env.ICompilationUnit wrappedUnit = 
                new CompilationUnit((PackageFragment) unit.getParent(), unit.getElementName(), unit.getOwner()){
            public char[] getContents() {
                return converter.content;
            }
        };
        int transformedStart = AspectsConvertingParser.translatePositionToAfterChanges(wordRegion.getOffset(), replacements);
        int transformedEnd = AspectsConvertingParser.translatePositionToAfterChanges(wordRegion.getOffset() + wordRegion.getLength(), replacements)-1;
        
        engine.select(wrappedUnit, transformedStart, transformedEnd);
        IJavaElement[] elements = requestor.getElements();
        return elements;
    }


}
