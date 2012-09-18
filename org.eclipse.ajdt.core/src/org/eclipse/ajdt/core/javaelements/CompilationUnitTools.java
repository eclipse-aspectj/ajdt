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

import java.util.ArrayList;
import java.util.List;

import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IProgramElement.Modifiers;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.PackageFragment;

/**
 * @author Luzius Meisser
 */
public class CompilationUnitTools {
	
	public static String convertAJToJavaFileName(String ajfile){
		return ajfile.substring(0, ajfile.lastIndexOf('.')).concat(".java"); //$NON-NLS-1$
	}
	
	
	public static PackageFragment getParentPackage(IFile ajFile){
		IJavaProject jp = JavaCore.create(ajFile.getProject());
		IJavaElement elem = JavaModelManager.determineIfOnClasspath(ajFile, jp);
		if (elem == null){
			//not on classpath -> default package
			IPackageFragmentRoot root = jp.getPackageFragmentRoot(ajFile.getParent());
			elem = root.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);
		}
		if (elem instanceof PackageFragment){
			return (PackageFragment)elem;
		}
		//should never happen
		
		return null;
	}
	
	public static int getModifierCode(IProgramElement elem){
		IProgramElement.Accessibility acc = elem.getAccessibility();
		List<Modifiers> others = elem.getModifiers();
		int modifiers = 0;
		if (acc == IProgramElement.Accessibility.PUBLIC){
			modifiers |= ClassFileConstants.AccPublic;
		} else if (acc ==  IProgramElement.Accessibility.PROTECTED) {
			modifiers |= ClassFileConstants.AccProtected;
		} else if (acc == IProgramElement.Accessibility.PRIVATE) {
			modifiers |= ClassFileConstants.AccPrivate;
		}

		if (others != null) {
    		if (others.contains(IProgramElement.Modifiers.ABSTRACT)) {
    			modifiers |= ClassFileConstants.AccAbstract;
    		}
    		if (others.contains(IProgramElement.Modifiers.FINAL)) {
    			modifiers |= ClassFileConstants.AccFinal;
    		}
    		if (others.contains(IProgramElement.Modifiers.NATIVE)) {
    			modifiers |= ClassFileConstants.AccNative;
    		}
    		if (others.contains(IProgramElement.Modifiers.STATIC)) {
    			modifiers |= ClassFileConstants.AccStatic;
    		}
    		if (others.contains(IProgramElement.Modifiers.SYNCHRONIZED)) {
    			modifiers |= ClassFileConstants.AccSynchronized;
    		}
    		if (others.contains(IProgramElement.Modifiers.TRANSIENT)) {
    			modifiers |= ClassFileConstants.AccTransient;
    		}
    		if (others.contains(IProgramElement.Modifiers.VOLATILE)) {
    			modifiers |= ClassFileConstants.AccVolatile;
    		}
		}		
		return modifiers;
	}
	
	
	/**
	 * returns the modifiers of this element as if this element were declared
	 * public
	 */
	public static int getPublicModifierCode(IAspectJElementInfo info){
        int modifiers = ClassFileConstants.AccPublic;

        List<Modifiers> others = info.getAJModifiers();
        if (others == null) {
            return modifiers;
        }
        if (others.contains(IProgramElement.Modifiers.ABSTRACT)) {
            modifiers |= ClassFileConstants.AccAbstract;
        }
        if (others.contains(IProgramElement.Modifiers.FINAL)) {
            modifiers |= ClassFileConstants.AccFinal;
        }
        if (others.contains(IProgramElement.Modifiers.NATIVE)) {
            modifiers |= ClassFileConstants.AccNative;
        }
        if (others.contains(IProgramElement.Modifiers.STATIC)) {
            modifiers |= ClassFileConstants.AccStatic;
        }
        if (others.contains(IProgramElement.Modifiers.SYNCHRONIZED)) {
            modifiers |= ClassFileConstants.AccSynchronized;
        }
        if (others.contains(IProgramElement.Modifiers.TRANSIENT)) {
            modifiers |= ClassFileConstants.AccTransient;
        }
        if (others.contains(IProgramElement.Modifiers.VOLATILE)) {
            modifiers |= ClassFileConstants.AccVolatile;
        }
        
        return modifiers;
    }
	
	public static IProgramElement.Accessibility getAccessibilityFromModifierCode(int code){
		IProgramElement.Accessibility acc = null;
		if ((code & ClassFileConstants.AccPublic) != 0){
			acc = IProgramElement.Accessibility.PUBLIC;
		} else if ((code & ClassFileConstants.AccProtected) != 0) {
			acc = IProgramElement.Accessibility.PROTECTED;
		} else if ((code & ClassFileConstants.AccPrivate) != 0) {
			acc = IProgramElement.Accessibility.PRIVATE;
		} else {
			acc = IProgramElement.Accessibility.PACKAGE;
		}
		return acc;
	}
	
	public static List<Modifiers> getModifiersFromModifierCode(int code){
		List<Modifiers> mods = new ArrayList<Modifiers>(2);
		if ((code & ClassFileConstants.AccAbstract) != 0){
			mods.add(IProgramElement.Modifiers.ABSTRACT);
		}
		if ((code & ClassFileConstants.AccFinal) != 0) {
			mods.add(IProgramElement.Modifiers.FINAL);
		}
		if ((code & ClassFileConstants.AccStatic) != 0) {
			mods.add(IProgramElement.Modifiers.STATIC);
		}
		if ((code & ClassFileConstants.AccVolatile) != 0){
			mods.add(IProgramElement.Modifiers.VOLATILE);
		}
		if ((code & ClassFileConstants.AccTransient) != 0){
			mods.add(IProgramElement.Modifiers.TRANSIENT);
		}
		if ((code & ClassFileConstants.AccSynchronized) != 0){
			mods.add(IProgramElement.Modifiers.SYNCHRONIZED);
		}
		if ((code & ClassFileConstants.AccNative) != 0){
			mods.add(IProgramElement.Modifiers.NATIVE);
		}
		return mods;
	}
	
	/**
	 * Attempts to convert the compilation unit into an AJCompilation unit.
	 * Returns null if not possible.
	 */
	public static AJCompilationUnit convertToAJCompilationUnit(ICompilationUnit unit) {
	    if (unit instanceof AJCompilationUnit) {
            return (AJCompilationUnit) unit;
        } else if (unit instanceof CompilationUnit) {
            AJCompilationUnit maybeAJUnit = AJCompilationUnitManager.INSTANCE.getAJCompilationUnit((IFile) unit.getResource());
            return maybeAJUnit;
        } else {
            return null;
        }
	}
	
	/**
	 * Returns the type with the given simple name in the given compilation unit.
	 * 
	 * If an inner type, then this name includes parent types.
	 * 
	 * The separator may either be source ('.') or binary ('$')
	 */
	public static IType findType(ICompilationUnit unit, String name, boolean isBinarySeparator) {
	    String[] names = name.split("\\" + (isBinarySeparator ? '$' : '.'));
	    IType candidate = unit.getType(names[0]); 
	    if (names.length > 0) {
	        for (int i = 1; i < names.length; i++) {
                candidate = candidate.getType(names[i]);
            }
	    }
	    return candidate.exists() ? candidate : null;
	}
	

	
//	
//	private static IType internalFindType(IParent parent, String name) {
//	    try {
//            IJavaElement[] children = parent.getChildren();
//            for (int i = 0; i < children.length; i++) {
//                if (children[i].getElementType() == IJavaElement.TYPE && children[i].getElementName().equals(name)) {
//                    return (IType) children[i];
//                } else if (children[i] instanceof IParent){
//                    IType type = internalFindType((IParent) children[i], name);
//                    if (type != null) {
//                        return type;
//                    }
//                }
//            }
//        } catch (JavaModelException e) {
//        }
//	    return null;
//	}
}
