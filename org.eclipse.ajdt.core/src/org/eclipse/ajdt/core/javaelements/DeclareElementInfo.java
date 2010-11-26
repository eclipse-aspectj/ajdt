/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

public class DeclareElementInfo extends AspectJMemberElementInfo {
    private boolean extendz = false;
    private boolean implementz = false;
    private boolean annotationRemover = false;
    
    private char[][] types;
    
    /**
     * @return true if this is an extends declare parent
     */
    public boolean isExtends() {
        return extendz;
    }
    
    public void setExtends(boolean extendz) {
        this.extendz = extendz;
    }
    
    /**
     * @return true if this is an extends declare parent
     */
    public boolean isImplements() {
        return implementz;
    }
    
    public void setImplements(boolean implementz) {
        this.implementz = implementz;
    }
    
	public void setTypes(String[] types) {
	    this.types = new char[types.length][];
	    for (int i = 0; i < types.length; i++) {
	        this.types[i] = types[i].toCharArray();
        }
    }
	
	public char[][] getTypes() {
        return types;
    }
	
	/**
	 * convenience method for extends declare parents
	 * @param type
	 */
    public void setType(String type) {
        this.types = new char[][] { type.toCharArray() };
    }
    
    /**
     * convenience method for extends declare parents
     * @param type
     */
    public char[] getType() {
        return types[0];
    }
    
    /**
     * @return true iff this declare element removes the target annotation
     */
    public boolean isAnnotationRemover() {
        return annotationRemover;
    }
    
    public void setAnnotationRemover(boolean annotationRemover) {
        this.annotationRemover = annotationRemover;
    }
}
