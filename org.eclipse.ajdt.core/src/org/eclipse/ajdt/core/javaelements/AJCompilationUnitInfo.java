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

import java.lang.reflect.Field;
import java.util.HashMap;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.ASTHolderCUInfo;

/**
 * @author Luzius Meisser
 */
public class AJCompilationUnitInfo extends ASTHolderCUInfo {

	public void setTimestamp(long stamp){
		this.timestamp = stamp;
	}
	
	// get super's fields by reflection
	private static Field astLevelField;
	public int getASTLevel() {
	    try {
            if (astLevelField == null) {
                astLevelField = ASTHolderCUInfo.class.getDeclaredField("astLevel");
                astLevelField.setAccessible(true);
            }
            return astLevelField.getInt(this);
        } catch (SecurityException e) {
        } catch (IllegalArgumentException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
        return ICompilationUnit.NO_AST;
	}
	
	
	private static Field resolveBindingsField;
    public boolean doResolveBindings() {
        try {
            if (resolveBindingsField == null) {
                resolveBindingsField = ASTHolderCUInfo.class.getDeclaredField("resolveBindings");
                resolveBindingsField.setAccessible(true);
            }
            return resolveBindingsField.getBoolean(this);
        } catch (SecurityException e) {
        } catch (IllegalArgumentException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
        return false;
	}
	
    private static Field reconcileFlagsField;
    public int getReconcileFlags() {
        try {
            if (reconcileFlagsField == null) {
                reconcileFlagsField = ASTHolderCUInfo.class.getDeclaredField("reconcileFlags");
                reconcileFlagsField.setAccessible(true);
            }
            return reconcileFlagsField.getInt(this);
        } catch (SecurityException e) {
        } catch (IllegalArgumentException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
        return ICompilationUnit.NO_AST;
	}
	
    
    private static Field problemsField;
    public HashMap getProblems() {
        try {
            if (problemsField == null) {
                problemsField = ASTHolderCUInfo.class.getDeclaredField("problems");
                problemsField.setAccessible(true);
            }
            return (HashMap) problemsField.get(this);
        } catch (SecurityException e) {
        } catch (IllegalArgumentException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }
    
    private static Field astField;
    public void setAST(CompilationUnit cu) {
        try {
            if (astField == null) {
                astField = ASTHolderCUInfo.class.getDeclaredField("ast");
                astField.setAccessible(true);
            }
            astField.set(this, cu);
        } catch (SecurityException e) {
        } catch (IllegalArgumentException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }

    }

}
