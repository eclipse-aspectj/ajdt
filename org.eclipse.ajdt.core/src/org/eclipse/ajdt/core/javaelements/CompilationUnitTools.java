/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.asm.IProgramElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
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
			modifiers |= FieldDeclaration.AccPublic;
		} else if (acc ==  IProgramElement.Accessibility.PROTECTED) {
			modifiers |= FieldDeclaration.AccProtected;
		} else if (acc == IProgramElement.Accessibility.PRIVATE) {
			modifiers |= FieldDeclaration.AccPrivate;
		}

		if (others.contains(IProgramElement.Modifiers.ABSTRACT))
			modifiers |= FieldDeclaration.AccAbstract;
		if (others.contains(IProgramElement.Modifiers.FINAL))
			modifiers |= FieldDeclaration.AccFinal;
		if (others.contains(IProgramElement.Modifiers.NATIVE))
			modifiers |= FieldDeclaration.AccNative;
		if (others.contains(IProgramElement.Modifiers.STATIC))
			modifiers |= FieldDeclaration.AccStatic;
		if (others.contains(IProgramElement.Modifiers.SYNCHRONIZED))
			modifiers |= FieldDeclaration.AccSynchronized;
		if (others.contains(IProgramElement.Modifiers.TRANSIENT))
			modifiers |= FieldDeclaration.AccTransient;
		if (others.contains(IProgramElement.Modifiers.VOLATILE))
			modifiers |= FieldDeclaration.AccVolatile;
		
		return modifiers;
	}
	
	public static IProgramElement.Accessibility getAccessibilityFromModifierCode(int code){
		IProgramElement.Accessibility acc = null;
		if ((code & FieldDeclaration.AccPublic) != 0){
			acc = IProgramElement.Accessibility.PUBLIC;
		} else if ((code & FieldDeclaration.AccProtected) != 0) {
			acc = IProgramElement.Accessibility.PROTECTED;
		} else if ((code & FieldDeclaration.AccPrivate) != 0) {
			acc = IProgramElement.Accessibility.PRIVATE;
		} else {
			acc = IProgramElement.Accessibility.PACKAGE;
		}
		return acc;
	}
	
	public static List getModifiersFromModifierCode(int code){
		List mods = new ArrayList(2);
		if ((code & FieldDeclaration.AccAbstract) != 0){
			mods.add(IProgramElement.Modifiers.ABSTRACT);
		}
		if ((code & FieldDeclaration.AccFinal) != 0) {
			mods.add(IProgramElement.Modifiers.FINAL);
		}
		if ((code & FieldDeclaration.AccStatic) != 0) {
			mods.add(IProgramElement.Modifiers.STATIC);
		}
		if ((code & FieldDeclaration.AccVolatile) != 0){
			mods.add(IProgramElement.Modifiers.VOLATILE);
		}
		if ((code & FieldDeclaration.AccTransient) != 0){
			mods.add(IProgramElement.Modifiers.TRANSIENT);
		}
		if ((code & FieldDeclaration.AccSynchronized) != 0){
			mods.add(IProgramElement.Modifiers.SYNCHRONIZED);
		}
		if ((code & FieldDeclaration.AccNative) != 0){
			mods.add(IProgramElement.Modifiers.NATIVE);
		}
		return mods;
	}
}
