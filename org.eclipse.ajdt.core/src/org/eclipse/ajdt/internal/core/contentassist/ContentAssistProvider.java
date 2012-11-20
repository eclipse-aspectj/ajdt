/*******************************************************************************
 * Copyright (c) 2009, 2010 SpringSource and others.
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

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.core.codeconversion.AspectsConvertingParser.Replacement;
import org.eclipse.ajdt.core.codeconversion.ConversionOptions;
import org.eclipse.ajdt.core.codeconversion.ITDAwareLookupEnvironment;
import org.eclipse.ajdt.core.codeconversion.ITDAwareNameEnvironment;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.text.ITDCodeSelection;
import org.eclipse.contribution.jdt.itdawareness.IJavaContentAssistProvider;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IType;
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
import org.eclipse.jface.text.Region;

/**
 * @author Andrew Eisenberg
 * @created Jan 2, 2009
 * Provides ITD Aware content assist and code selection
 */
public class ContentAssistProvider implements IJavaContentAssistProvider {
    
    private static class MockCompilationUnit extends CompilationUnit {
        
        char[] transformedContents;
        CompilationUnit orig;
        
        ArrayList<Replacement> insertionTable;
        
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
        
        int translatePositionToFake(int pos) {
            return AspectsConvertingParser.translatePositionToAfterChanges(pos, insertionTable);
        }
    }

    public boolean doContentAssist(ICompilationUnit cu,
            ICompilationUnit unitToSkip, int position,
            CompletionRequestor requestor, WorkingCopyOwner owner,
            /* AJDT 1.7 */
            ITypeRoot typeRoot, Openable target, IProgressMonitor monitor) throws Exception {
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
        ProposalRequestorWrapper wrapped = new ProposalRequestorWrapper(requestor, mcu, mcu.insertionTable);
        int transformedPos = mcu.translatePositionToFake(position);
        if (transformedPos < -1 || transformedPos > mcu.getContents().length) {
            throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INDEX_OUT_OF_BOUNDS));
        }

		/* AJDT 1.7 */
        ITDAwareNameEnvironment environment = new ITDAwareNameEnvironment(project, owner, monitor);
        environment.setUnitToSkip(unitToSkip);

        // code complete
        /* AJDT 1.7 */
        CompletionEngine engine = new CompletionEngine(environment, wrapped, project.getOptions(true), project, owner, monitor);
        engine.lookupEnvironment = new ITDAwareLookupEnvironment(engine.lookupEnvironment, environment);
        engine.complete(mcu, transformedPos, 0, typeRoot);
        
        return true;
    }

    /**
     * performs ITD-aware code select if necessary.
     * The check to see if necessary does the following:
     * 1. if prevResults is null or has length 0, then does it
     * 2. (a) check to see if the prevResults has length 1 and is IType
     *    (b) get the text covered by the selection and expand (or contract) to get the full word
     *    (c) if the full word is not the same as the element name, then do it
     */
    public IJavaElement[] doCodeSelect(
            org.eclipse.jdt.core.ICompilationUnit unit,
            int offset, int length, IJavaElement[] prevResults)
            throws JavaModelException {

        AJLog.log("===Code Select.  Unit: " + unit.getElementName() + " [ " + offset + ", " + length + " ]");
        
        // see if we should shortcut other processing and we can
        // quickly find a selection that we know is only valid inside of
        // AspectJ
        Region wordRegion = new Region(offset, length);
        ITDCodeSelection itdCodeSelection = new ITDCodeSelection(unit);
        IJavaElement[] maybeResult = itdCodeSelection.shortCutCodeSelection(wordRegion);
        if (maybeResult != null && maybeResult.length > 0) {
            return maybeResult;
        }
        
        if (prevResults != null && prevResults.length > 1) {
            return prevResults;
        }
        if (prevResults.length == 1 && prevResults[0] instanceof IType) {
            // get the expanded text region and see if it matches the type name
            String expandedRegion = getExpandedRegion(offset, length, ((CompilationUnit) unit).getContents()).replace('$', '.');
            if (expandedRegion.equals(prevResults[0].getElementName()) || 
                    expandedRegion.equals(((IType) prevResults[0]).getFullyQualifiedName())) {
                // we really are looking for the type
                return prevResults;
            }
        }
        // we want to do ITD Aware code select
        IJavaElement[] newResults = itdCodeSelection.findJavaElement(wordRegion);
        return newResults != null && newResults.length > 0 ?
                newResults : prevResults;
    }

    /**
     * Expands the region of the selection to include a full word.
     * After finding this word, whitespace from the ends are removed
     * Also, if the word contains a '.' or any whitespace in the center,
     * then only the last segment is returned
     */
    protected String getExpandedRegion(int offset, int length, char[] contents) {
        int start = offset;
        int end = offset+length;
        
        start--;
        while (start >= 0 && Character.isJavaIdentifierPart(contents[start])) {
            start--;
        }
        start++;
        
        while (end < contents.length && Character.isJavaIdentifierPart(contents[end])) {
            end++;
        }
        
        // include paren or bracket because this would be the start of a constructor call
        // TODO handle situation where there are spaces after the name and before the '(' or '<'
        if (end < contents.length && (contents[end] == '(' || contents[end] == '<')) {
            end++;
        }
        String candidate = String.valueOf(contents, start, end-start);
        candidate = candidate.trim();
        String split[] = candidate.split("\\.|\\s");
        if (split.length > 1) {
            candidate = split[split.length-1];
        }
        return candidate;
    }

}
