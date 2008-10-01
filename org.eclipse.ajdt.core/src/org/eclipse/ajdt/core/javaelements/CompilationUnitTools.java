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
import org.eclipse.ajdt.internal.core.AJWorkingCopyOwner;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
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
		List others = elem.getModifiers();
		int modifiers = 0;
		if (acc == IProgramElement.Accessibility.PUBLIC){
			modifiers |= ClassFileConstants.AccPublic;
		} else if (acc ==  IProgramElement.Accessibility.PROTECTED) {
			modifiers |= ClassFileConstants.AccProtected;
		} else if (acc == IProgramElement.Accessibility.PRIVATE) {
			modifiers |= ClassFileConstants.AccPrivate;
		}

		if (others.contains(IProgramElement.Modifiers.ABSTRACT))
			modifiers |= ClassFileConstants.AccAbstract;
		if (others.contains(IProgramElement.Modifiers.FINAL))
			modifiers |= ClassFileConstants.AccFinal;
		if (others.contains(IProgramElement.Modifiers.NATIVE))
			modifiers |= ClassFileConstants.AccNative;
		if (others.contains(IProgramElement.Modifiers.STATIC))
			modifiers |= ClassFileConstants.AccStatic;
		if (others.contains(IProgramElement.Modifiers.SYNCHRONIZED))
			modifiers |= ClassFileConstants.AccSynchronized;
		if (others.contains(IProgramElement.Modifiers.TRANSIENT))
			modifiers |= ClassFileConstants.AccTransient;
		if (others.contains(IProgramElement.Modifiers.VOLATILE))
			modifiers |= ClassFileConstants.AccVolatile;
		
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
	
	public static List getModifiersFromModifierCode(int code){
		List mods = new ArrayList(2);
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
            return new AJCompilationUnit((PackageFragment) unit.getParent(), unit.getElementName(), AJWorkingCopyOwner.INSTANCE);
        } else {
            return null;
        }
	}
}
