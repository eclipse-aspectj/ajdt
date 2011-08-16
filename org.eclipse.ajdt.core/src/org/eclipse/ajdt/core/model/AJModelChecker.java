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

package org.eclipse.ajdt.core.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.ajdt.internal.core.builder.AjState;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationshipMap;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.javaelements.DeclareElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.jdt.core.IJavaElement;

/**
 * @author Andrew Eisenberg
 * @created Feb 3, 2009
 *
 * Performs sanity checking on the crosscutting model.
 * 
 * The following rules are checked:
 * 
 * 1. Advice relationships target only methods, fields, and AJCodeElements
 * 2. Aspect Declarations (ITDs) target only types
 * 
 */
public class AJModelChecker {
    private AJModelChecker() {
        // not instantiable
    }
    
    public static void doModelCheckIfRequired(AJProjectModelFacade model) {
        if (shouldCheckModel()) {
            AJLog.logStart("Model sanity check for: " + model.getProject().getName());
            List<String> problems = internalCheckModel(model);
            logProblems(problems);
            AJLog.logEnd(AJLog.MODEL, "Model sanity check for: " + model.getProject().getName());
        }
    }
    
    public static boolean shouldCheckModel() {
        // if the state listener is not null, this means that debug tracing is enabled.
        return AjState.stateListener != null;
    }
    
    private static void logProblems(List<String> problems) {
        if (problems.size() == 0) {
            AJLog.log(AJLog.MODEL, "Crosscutting model sanity checked with no problems");
            return;
        }
        
        AJLog.log(AJLog.MODEL, "Crosscutting model sanity checked.  The following problems found:");
        for (Iterator<String> probIter = problems.iterator(); probIter.hasNext();) {
            String problem = probIter.next();
            AJLog.log(AJLog.MODEL, problem);
        }
        AJLog.log(AJLog.MODEL, "");
    }
    
    /**
     * iterates through all relationships and checks to see that it passes all rules
     * 
     * returns list of questionable relationships as strings.  An empty list is returned if the
     * model is OK. 
     */
    private static List<String> internalCheckModel(AJProjectModelFacade model) {
        IRelationshipMap relationships = model.getAllRelationships();
        List<String> problems = new ArrayList<String>();
        if (relationships != null) {
            for (Iterator<String> relIter = relationships.getEntries().iterator(); relIter.hasNext();) {
                String handle = (String) relIter.next();
                List<IRelationship> relsForHandle = relationships.get(handle);
                for (Iterator<IRelationship> relIter2 = relsForHandle.iterator(); relIter2
                        .hasNext();) {
                    IRelationship rel = (IRelationship) relIter2.next();
                    List<String> res = invalidAdviceRelationsip(rel, model);
                    problems.addAll(res);
                    res = itdsNotOnType(rel, model);
                    problems.addAll(res);
                }
            }
        } else {
            problems.add("No relationshipes found");
        }
        
        return problems;
    }
    
    private static List<String> invalidAdviceRelationsip(IRelationship rel, AJProjectModelFacade model) {
        List<String> problems = new ArrayList<String>();
        if (rel.getKind() == IRelationship.Kind.ADVICE ||
                rel.getKind() == IRelationship.Kind.ADVICE_AFTER ||
                rel.getKind() == IRelationship.Kind.ADVICE_AFTERRETURNING ||
                rel.getKind() == IRelationship.Kind.ADVICE_AFTERTHROWING ||
                rel.getKind() == IRelationship.Kind.ADVICE_BEFORE ||
                rel.getKind() == IRelationship.Kind.ADVICE_AROUND) {
            
            IJavaElement elt = model.programElementToJavaElement(rel.getSourceHandle());
            if (!elt.exists()) {
                problems.add("Java Element does not exist: " + rel.getSourceHandle() + 
                        "\n\tIt is the source relationship of " + toRelString(rel) +
                        "\n\tThis may not actually be a problem if compiling broken code or advising static initializers.");
            }
            if (    elt.getElementType() == IJavaElement.COMPILATION_UNIT || 
                    elt.getElementType() == IJavaElement.CLASS_FILE) {
                problems.add("Java Element is wrong type (advice relationships should not contain any types or compilation units): " + 
                        rel.getSourceHandle() + 
                        "\n\tIt is the source relationship of " + toRelString(rel));
            }
            
            for (Iterator<String> targetIter = rel.getTargets().iterator(); targetIter.hasNext();) {
                String target = targetIter.next();
                elt = model.programElementToJavaElement(target);
                if (!elt.exists()) {
                    problems.add("Java Element does not exist: " + target + 
                            "\n\tIt is the source relationship of " + toRelString(rel) +
                            "\n\tThis may not actually be a problem if compiling broken code or advising static initializers.");
                }
                if (elt != AJProjectModelFacade.ERROR_JAVA_ELEMENT && 
                       (elt.getElementType() == IJavaElement.COMPILATION_UNIT || 
                        elt.getElementType() == IJavaElement.CLASS_FILE)) {
                    problems.add("Java Element is wrong type (advice relationships should not contain any types or compilation units): " + 
                            target + 
                            "\n\tIt is the source relationship of " + toRelString(rel));
                }
            }
                
        }
        return problems;
    }
    private static List<String> itdsNotOnType(IRelationship rel, AJProjectModelFacade model) {
        List<String> problems = new ArrayList<String>();
        if (rel.getKind() == IRelationship.Kind.DECLARE_INTER_TYPE) {
            
            IJavaElement elt = model.programElementToJavaElement(rel.getSourceHandle());
            if (!elt.exists()) {
                problems.add("Java Element does not exist: " + rel.getSourceHandle() + 
                        "\n\tIt is the source relationship of " + toRelString(rel) +
                        "\n\tThis may not actually be a problem if compiling broken code.");
            }
            if (elt != AJProjectModelFacade.ERROR_JAVA_ELEMENT &&
                   (elt.getElementType() == IJavaElement.FIELD || 
                    elt.getElementType() == IJavaElement.METHOD || 
                    elt.getElementType() == IJavaElement.LOCAL_VARIABLE || 
                    elt.getElementType() == IJavaElement.INITIALIZER || 
                    elt.getElementType() == IJavaElement.COMPILATION_UNIT || 
                    elt.getElementType() == IJavaElement.CLASS_FILE) &&
                    !(elt instanceof IntertypeElement || elt instanceof DeclareElement)) {
                problems.add("Java Element is wrong type (ITD relationships should only contain types and intertype elements): " + 
                        rel.getSourceHandle() + 
                        "\n\tIt is the source relationship of " + toRelString(rel));
            }
            
            for (Iterator<String> targetIter = rel.getTargets().iterator(); targetIter.hasNext();) {
                String target = targetIter.next();
                elt = model.programElementToJavaElement(target);
                if (!elt.exists()) {
                    problems.add("Java Element does not exist: " + target + 
                            "\n\tIt is the source relationship of " + toRelString(rel) +
                            "\n\tThis may not actually be a problem if compiling broken code.");
                }
                if (elt != AJProjectModelFacade.ERROR_JAVA_ELEMENT &&
                       (elt.getElementType() == IJavaElement.FIELD || 
                        elt.getElementType() == IJavaElement.METHOD || 
                        elt.getElementType() == IJavaElement.LOCAL_VARIABLE || 
                        elt.getElementType() == IJavaElement.INITIALIZER || 
                        elt.getElementType() == IJavaElement.COMPILATION_UNIT || 
                        elt.getElementType() == IJavaElement.CLASS_FILE) &&
                        !(elt instanceof IntertypeElement || elt instanceof DeclareElement)) {
                    problems.add("Java Element is wrong type (ITD relationships should only contain types and intertype elements): " + 
                            target + 
                            "\n\tIt is the source relationship of " + toRelString(rel));
                }
            }
                
        }
        return problems;
    }

    private static String toRelString(IRelationship rel) {
        StringBuffer sb = new StringBuffer();
        sb.append(rel.getSourceHandle());
        sb.append(" --");
        sb.append(rel.getName());
        sb.append("--> ");
        for (Iterator<String> targetIter = rel.getTargets().iterator(); targetIter.hasNext();) {
            String target = targetIter.next();
            sb.append(target);
            if (targetIter.hasNext()) {
                sb.append(",   ");
            }
        }
        return sb.toString();
    }
}
