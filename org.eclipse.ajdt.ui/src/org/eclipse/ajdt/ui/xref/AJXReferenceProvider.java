/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.ui.xref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.asm.internal.Relationship;
import org.eclipse.ajdt.core.javaelements.AspectJMemberElement;
import org.eclipse.ajdt.internal.builder.AJModel;
import org.eclipse.ajdt.internal.builder.AJNode;
import org.eclipse.contribution.xref.core.IXReference;
import org.eclipse.contribution.xref.core.IXReferenceProvider;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.Member;

/**
 * @author hawkinsh
 *  
 */
public class AJXReferenceProvider implements IXReferenceProvider {

    private static final Class[] myClasses = new Class[] { IJavaElement.class };

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getClasses()
     */
    public Class[] getClasses() {
        return myClasses;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getXReferences(java.lang.Object)
     */
    public Collection getXReferences(Object o) {
        if (!(o instanceof IJavaElement))
            return Collections.EMPTY_SET;

        List xrefs = new ArrayList();
        IJavaElement je = (IJavaElement) o;
                
        int lineNumber = 0;
        int endLineNumber = 0;
        
        if (je instanceof AspectJMemberElement) {
            AspectJMemberElement ae = (AspectJMemberElement)je;
            try {
                int offset = ae.getSourceRange().getOffset();
                lineNumber = getLineNumFromOffset(ae,offset);
                endLineNumber = getLineNumFromOffset(ae,(offset + ae.getSourceRange().getLength()));
            } catch (JavaModelException e) {
            }
        } else if (je instanceof Member) {
            Member m = (Member)je;
            try {
                int offset = m.getSourceRange().getOffset();
                lineNumber = getLineNumFromOffset(m,offset);
                endLineNumber = getLineNumFromOffset(m,(offset + m.getSourceRange().getLength()));
            } catch (JavaModelException e) {
            }
        }
        
        IRelationshipMap xrefMap = AsmManager.getDefault().getRelationshipMap();
        Set sources = xrefMap.getEntries();

        for (Iterator iter = sources.iterator(); iter.hasNext();) {
            String sourceOfRelationship = (String) iter.next();
            
            IProgramElement ipe = AsmManager.getDefault().getHierarchy()
                    .findElementForHandle(sourceOfRelationship);

            if (je.getResource().getRawLocation().toOSString().equals(
                    ipe.getSourceLocation().getSourceFile().getPath())
                    	&& (lineNumber <= ipe.getSourceLocation().getLine() 
                    	                && ipe.getSourceLocation().getLine() <= endLineNumber)) {

                List relationships = xrefMap.get(ipe);
                for (Iterator iterator = relationships.iterator(); iterator.hasNext();) {
                    Relationship rel = (Relationship) iterator.next();
 
                    if (rel.getKind().equals(IRelationship.Kind.USES_POINTCUT)) {
                        continue;
                    }
                    
                    List associates = new ArrayList();
                    List targets = rel.getTargets();
                    for (Iterator iterator2 = targets.iterator(); iterator2.hasNext();) {
                        String t = (String) iterator2.next();
                        IProgramElement link = AsmManager.getDefault()
                                .getHierarchy().findElementForHandle(t);
                        IJavaElement javaElement = AJModel.getInstance().getCorrespondingJavaElement(link);
                        AJNode associate = new AJNode(javaElement,link.toLinkLabelString()); 
                        
                        if (associate != null) {
                            associates.add(associate);
                        }
                    }
                    XRef xref = new XRef(rel.getName(), associates);
                    xrefs.add(xref);
                }
            }
        }
        return xrefs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getProviderDescription()
     */
    public String getProviderDescription() {
        return "Provides AspectJ cross-cutting structure references";
    }

    private static class XRef implements IXReference {

        private String name;

        private List associates;

        public XRef(String name, List associates) {
            this.name = name;
            this.associates = associates;
        }

        public String getName() {
            return name;
        }

        public Iterator getAssociates() {
            return associates.iterator();
        }
    }
    
	/**
	 * Get the line number for the given offset in the given AspectJMemberElement
	 */
	private int getLineNumFromOffset(AspectJMemberElement ajelement, int offSet){
		try {
		    IJavaElement je = ajelement.getParent().getParent();
		    ICompilationUnit cu = null;
		    if (je instanceof ICompilationUnit) {
                cu = (ICompilationUnit)je;
            }
		    if (cu != null) {
                return getLineFromOffset(cu.getSource(),ajelement.getDeclaringType(),offSet);
            }			    
		} catch (JavaModelException jme) {
		}
		return 0;		
	}

	/**
	 * Get the line number for the given offset in the given Member
	 */
	private int getLineNumFromOffset(Member m, int offSet){
		try {
		    IJavaElement je = m.getParent();
		    ICompilationUnit cu = null;
		    if (je instanceof ICompilationUnit) {
                cu = (ICompilationUnit)je;
            } else {
                IJavaElement j = je.getParent();
                if (j instanceof ICompilationUnit) {
                    cu = (ICompilationUnit)j;
                }
            }
		    if (cu != null) {
                return getLineFromOffset(cu.getSource(),m.getDeclaringType(),offSet);
            }		    
		} catch (JavaModelException jme) {
		}
		return 0;		
	}
	
	/**
	 * Get the line number for the given offset in the given Source and type
	 */
	private int getLineFromOffset(String source, IType type, int offSet) {
		if(type != null) {
			String sourcetodeclaration = source.substring(0, offSet);
			int lines = 0;
			char[] chars = new char[sourcetodeclaration.length()];
			sourcetodeclaration.getChars(
				0,
				sourcetodeclaration.length(),
				chars,
				0);
			for (int i = 0; i < chars.length; i++) {
				if (chars[i] == '\n') {
					lines++;
				}
			}
			return lines + 1;
		}
		return 0;	    
	}

}