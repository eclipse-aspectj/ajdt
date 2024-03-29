/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ajdt.core.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AJInjarElement;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.BinaryAspectElement;
import org.eclipse.ajdt.internal.core.AJWorkingCopyOwner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.NamedMember;
import org.eclipse.jdt.internal.core.PackageFragment;

public class AspectJCore {

	public static IJavaElement create(IFile file) {
		if ("aj".equals(file.getFileExtension())) {
			return AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(file);
		}
		return JavaModelManager.create(file, null/*unknown java project*/);
	}

	/**
	 * Returns the Java model element corresponding to the given handle
	 * identifier generated by <code>IJavaElement.getHandleIdentifier()</code>,
	 * or <code>null</code> if unable to create the associated element.
	 *
	 * @param handleIdentifier
	 *            the given handle identifier
	 * @return the Java element corresponding to the handle identifier
	 */
	public static IJavaElement create(String handleIdentifier) {
		return create(handleIdentifier, AJWorkingCopyOwner.INSTANCE);
	}

	private static int indexOfIgnoringEscapes(String str, char ch) {
		boolean prevEscape = false;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == JavaElement.JEM_ESCAPE) {
				prevEscape = true;
			} else {
				if ((c == ch) && (!prevEscape)) {
					return i;
				}
				prevEscape = false;
			}
		}
		return -1;
	}

	private static AJCodeElement getCodeElement(String codeElementHandle, JavaElement parent) {
		int li = indexOfIgnoringEscapes(codeElementHandle, JavaElement.JEM_COUNT);
		if (li != -1) {
			int occurrenceIndex = codeElementHandle.lastIndexOf(JavaElement.JEM_COUNT);
			if (Character.isDigit(codeElementHandle.charAt(occurrenceIndex + 1))) {
				int occurrence = Integer.parseInt(codeElementHandle.substring(occurrenceIndex + 1));
				String cname = codeElementHandle.substring(0, li);
				return new AJCodeElement(parent, cname, occurrence);
			}
			codeElementHandle = codeElementHandle.substring(0, li);
		}
		// no occurrance count
		return new AJCodeElement(parent, codeElementHandle);
	}

	public static IJavaElement create(String handleIdentifier, WorkingCopyOwner owner) {

		if (handleIdentifier == null) {
			return null;
		}
		Map<IOpenable, List<NamedMember>> aspectsInJavaFiles = new HashMap<>();

		boolean isCodeElement = false;
		String codeElementHandle = ""; //$NON-NLS-1$

		int codeElementDelimPos = indexOfIgnoringEscapes(handleIdentifier, AspectElement.JEM_CODEELEMENT);
		if (codeElementDelimPos != -1) {
			isCodeElement = true;
			codeElementHandle = handleIdentifier.substring(codeElementDelimPos + 1);
			handleIdentifier = handleIdentifier.substring(0, codeElementDelimPos);
		}

		AJMementoTokenizer memento = new AJMementoTokenizer(handleIdentifier);
		while (memento.hasMoreTokens()) {
			String token = memento.nextToken();
			final char firstChar = token.charAt(0);
			if (
				firstChar == AspectElement.JEM_ASPECT_CU ||
				firstChar == JavaElement.JEM_COMPILATIONUNIT ||
				firstChar == JavaElement.JEM_CLASSFILE
			) {

				int index;
				if (firstChar == AspectElement.JEM_ASPECT_CU)
					index = handleIdentifier.indexOf(AspectElement.JEM_ASPECT_CU);
				else if (firstChar == JavaElement.JEM_COMPILATIONUNIT)
					index = handleIdentifier.indexOf(JavaElement.JEM_COMPILATIONUNIT);
				else
					index = handleIdentifier.indexOf(JavaElement.JEM_CLASSFILE);

				if (index != -1) {
					IJavaElement je = JavaCore.create(handleIdentifier.substring(0, index));
					if (je instanceof PackageFragment) {
						PackageFragment pf = (PackageFragment) je;
						String cuName = handleIdentifier.substring(index + 1);
						int ind1 = cuName.indexOf(JavaElement.JEM_TYPE);
						if (ind1 != -1)
							cuName = cuName.substring(0, ind1);
						int ind2 = cuName.indexOf(AspectElement.JEM_ASPECT_TYPE);
						if (ind2 != -1)
							cuName = cuName.substring(0, ind2);
						int ind3 = cuName.indexOf(AspectElement.JEM_ITD_METHOD);
						if (ind3 != -1)
							cuName = cuName.substring(0, ind3);
						ind3 = cuName.indexOf(AspectElement.JEM_ITD_FIELD);
						if (ind3 != -1)
							cuName = cuName.substring(0, ind3);
						int ind4 = cuName.indexOf(AspectElement.JEM_DECLARE);
						if (ind4 != -1)
							cuName = cuName.substring(0, ind4);
						int ind5 = cuName.indexOf(AspectElement.JEM_IMPORTDECLARATION);
						if (ind5 != -1)
							cuName = cuName.substring(0, ind5);
						int ind6 = cuName.indexOf(AspectElement.JEM_PACKAGEDECLARATION);
						if (ind6 != -1)
							cuName = cuName.substring(0, ind6);
						if (CoreUtils.ASPECTJ_SOURCE_ONLY_FILTER.accept(cuName)) {
							// no need to use a cuprovider because we know this
							// is an AJCompilationUnit
							JavaElement cu = new AJCompilationUnit(pf, cuName, owner);
							memento.nextToken();
							if (!memento.hasMoreTokens())
								return cu;
							IJavaElement restEl = cu.getHandleFromMemento(memento.nextToken(), memento, owner);
							if (restEl != null) {
								if (isCodeElement) {
									// there was an AJCodeElement at the end of
									// the handle
									AJCodeElement codeEl = getCodeElement(codeElementHandle,(JavaElement) restEl);
									if (codeEl != null)
										return codeEl;
								}
								return restEl;
							}
						}
						else {
							// Use the default working copy owner for Java elements
							IJavaElement restEl = pf.getHandleFromMemento(token, memento, DefaultWorkingCopyOwner.PRIMARY);
							if (restEl != null) {
								if (isCodeElement) {
									// there was an AJCodeElement at the end of
									// the handle
									AJCodeElement codeEl = getCodeElement(codeElementHandle,(JavaElement) restEl);
									if (codeEl != null)
										return codeEl;
								}
								return restEl;
							}
							else if (ind2 != -1) { // An aspect in a .java file...
								int index3 = handleIdentifier.indexOf(AspectElement.JEM_ASPECT_TYPE);
								String aspectName = handleIdentifier.substring(index3 + 1);
								boolean identifierIsAspect = true;
								int ind7 = aspectName.indexOf(AspectElement.JEM_DECLARE);
								if (ind7 != -1) {
									aspectName = aspectName.substring(0, ind7);
									identifierIsAspect = false;
								}
								int ind8 = aspectName.indexOf(AspectElement.JEM_ADVICE);
								if (ind8 != -1) {
									aspectName = aspectName.substring(0, ind8);
									identifierIsAspect = false;
								}
								int ind9 = aspectName.indexOf(AspectElement.JEM_ITD_METHOD);
								if (ind9 != -1) {
									aspectName = aspectName.substring(0, ind9);
									identifierIsAspect = false;
								}
								ind9 = aspectName.indexOf(AspectElement.JEM_ITD_FIELD);
								if (ind9 != -1) {
									aspectName = aspectName.substring(0, ind9);
									identifierIsAspect = false;
								}
								int ind10 = aspectName.indexOf(AspectElement.JEM_ASPECT_TYPE);
								if (ind10 != -1) {
									aspectName = aspectName.substring(0, ind10);
									identifierIsAspect = false;
								}
								int ind11 = aspectName.indexOf(AspectElement.JEM_TYPE);
								if (ind11 != -1) {
									aspectName = aspectName.substring(0, ind11);
									identifierIsAspect = false;
								}
								int ind12 = aspectName.indexOf(AspectElement.JEM_FIELD);
								if (ind12 != -1) {
									aspectName = aspectName.substring(0, ind12);
									identifierIsAspect = false;
								}
								int ind13 = aspectName.indexOf(AspectElement.JEM_METHOD);
								if (ind13 != -1) {
									aspectName = aspectName.substring(0, ind13);
									identifierIsAspect = false;
								}
								int ind14 = aspectName.indexOf(AspectElement.JEM_POINTCUT);
								if (ind14 != -1) {
									aspectName = aspectName.substring(0, ind14);
									identifierIsAspect = false;
								}
								IOpenable openable = cuName.endsWith(".class")
									? pf.getClassFile(cuName)
									: pf.getCompilationUnit(cuName);
								List<NamedMember> namedMembers = aspectsInJavaFiles.computeIfAbsent(openable, k -> new ArrayList<>());
								NamedMember aspectElement = null;
								for (NamedMember namedMember : namedMembers) {
									if (namedMember.getElementName().equals(aspectName)) {
										aspectElement = namedMember;
									}
								}
								if(aspectElement == null) {
									if (openable instanceof ClassFile) {
										ClassFile cOpenable = (ClassFile) openable;
										aspectElement = new BinaryAspectElement(cOpenable, aspectName);
									}
									else {
										aspectElement = new AspectElement((JavaElement) openable, aspectName);
									}
									namedMembers.add(aspectElement);
								}
								int afterAspectIndex = index3 + aspectName.length() + 1;

								if (identifierIsAspect) {
									return aspectElement;
								}
								else {
									memento.setIndexTo(afterAspectIndex);
									return aspectElement.getHandleFromMemento(memento.nextToken(), memento, owner);
								}
							}
						}
					}
				}
			}
		}
		// XXX can we get here???
		if (isCodeElement) {
			// an injar aspect with no parent
			return new AJInjarElement(codeElementHandle);
		}
		return JavaCore.create(handleIdentifier);
	}

	/**
	 * Converts a handle signifying Java class to a handle signifying an
	 * aspect element.
	 * <p>
	 * This method is necessary because JavaCore does not create
	 * AspectElements when it is building structure using the {@link AspectsConvertingParser}
	 * <p>
	 * Note that this changes the top level class to being an aspect and keeps
	 * all others the same.  This may not work in all situations (eg- an inner aspect)
	 *
	 * @param classHandle
	 * @return converts the handle to using {@link AspectElement#JEM_ASPECT_CU} and
	 * {@link AspectElement#JEM_ASPECT_TYPE}
	 */
	public static String convertToAspectHandle(String classHandle, IJavaElement elt) {
		String aspectHandle = classHandle.replaceFirst(
			"\\" + JavaElement.JEM_TYPE,
			Character.toString(AspectElement.JEM_ASPECT_TYPE)
		);

		if (CoreUtils.ASPECTJ_SOURCE_ONLY_FILTER.accept(elt.getResource().getName())) {
			aspectHandle = aspectHandle.replace(
				JavaElement.JEM_COMPILATIONUNIT,
				AspectElement.JEM_ASPECT_CU
			);
		}
		return aspectHandle;
	}

	public static String convertToJavaCUHandle(String aspectHandle, IJavaElement elt) {
		String javaHandle = aspectHandle;
		if (elt != null) {
			IResource resource = elt.getResource();
			if (resource != null) {
				if (CoreUtils.ASPECTJ_SOURCE_ONLY_FILTER.accept(resource.getName())) {
					javaHandle = javaHandle.replaceFirst(
						"\\" + AspectElement.JEM_ASPECT_CU,
						Character.toString(JavaElement.JEM_COMPILATIONUNIT)
					);
				}
			}
		}
		return javaHandle;
	}

}
