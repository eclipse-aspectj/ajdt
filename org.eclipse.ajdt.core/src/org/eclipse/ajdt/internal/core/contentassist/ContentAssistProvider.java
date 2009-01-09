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

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.core.codeconversion.ConversionOptions;
import org.eclipse.ajdt.core.codeconversion.ITDAwareNameEnvironment;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.contribution.jdt.itdawareness.IJavaContentAssistProvider;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.PackageFragment;

/**
 * @author Andrew Eisenberg
 * @created Jan 2, 2009
 *
 */
public class ContentAssistProvider implements IJavaContentAssistProvider {
    
    private class MockCompilationUnit extends CompilationUnit {
        
        char[] transformedContents;
        CompilationUnit orig;
        
        ArrayList insertionTable;
        
        public MockCompilationUnit(CompilationUnit orig) {
            super((PackageFragment) orig.getParent(), orig.getElementName(), orig.owner);
            this.orig = orig;
            transformedContents = initContents();
        }

        private char[] initContents() {
            AspectsConvertingParser parser = new AspectsConvertingParser(orig.getContents());
            parser.setUnit(orig);
            insertionTable = parser.convert(ConversionOptions.CODE_COMPLETION);
            
            return parser.content;
        }
        
        public char[] getContents() {
            return transformedContents;
        }
        
        int translatePositionToReal(int pos) {
            return AspectsConvertingParser.translatePositionToBeforeChanges(pos, insertionTable);
        }
        
        int translatePositionToFake(int pos) {
            return AspectsConvertingParser.translatePositionToAfterChanges(pos, insertionTable);
        }
    }

    public boolean doContentAssist(ICompilationUnit cu,
            ICompilationUnit unitToSkip, int position,
            CompletionRequestor requestor, WorkingCopyOwner owner,
            ITypeRoot typeRoot, Openable target) throws Exception {
        JavaProject project = (JavaProject) target.getJavaProject();
        if (! AspectJPlugin.isAJProject(project.getProject())) {
            return false;
        }
        if (target instanceof AJCompilationUnit) {
            // already handled by the compilation unit
            return false;
        }
        if (! (target instanceof CompilationUnit)) {
            return false;
        }
        IBuffer buffer = target.getBuffer();
        if (buffer == null) {
            return false;
        }

        if (requestor == null) {
            throw new IllegalArgumentException("Completion requestor cannot be null"); //$NON-NLS-1$
        }

        MockCompilationUnit mcu = new MockCompilationUnit((CompilationUnit) target);
        requestor = new ProposalRequestorWrapper(requestor, mcu.insertionTable);
        int transformedPos = mcu.translatePositionToFake(position);
        if (transformedPos < -1 || transformedPos > mcu.getContents().length) {
            throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INDEX_OUT_OF_BOUNDS));
        }

        ITDAwareNameEnvironment environment = new ITDAwareNameEnvironment(project, owner, null);
        environment.setUnitToSkip(unitToSkip);

        // code complete
        CompletionEngine engine = new CompletionEngine(environment, requestor, project.getOptions(true), project, owner);
        engine.complete(mcu, transformedPos, 0, typeRoot);
        
        return true;
    }
}
