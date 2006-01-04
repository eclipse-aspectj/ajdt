/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/

package org.eclipse.ajdt.internal.ui;

import java.util.ArrayList;

import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.DeclareElement;
import org.eclipse.ajdt.core.javaelements.IAspectJElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.internal.ui.resources.AJDTIcon;
import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.internal.ui.viewsupport.TreeHierarchyLayoutProblemsDecorator;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorManager;

/**
 * @see ILabelDecorator
 */
public class ImageDecorator implements ILabelDecorator {
	
	private ArrayList listeners;
	private ImageDescriptorRegistry fRegistry;
	private boolean preventRecursion = false;
	private TreeHierarchyLayoutProblemsDecorator problemsDecorator;
	private DecoratorManager decman;
	private ImageDescriptor halfFilledPackageID;
	private ImageDescriptor activeConfigFileImage;
	
	private AspectJImages iconRegistry = AspectJImages.instance();
	
	/**
	 *
	 */
	public ImageDecorator() {
		listeners = new ArrayList(2);
		problemsDecorator = new TreeHierarchyLayoutProblemsDecorator();
		decman = WorkbenchPlugin.getDefault().getDecoratorManager();
		halfFilledPackageID = AspectJImages.BC_HALF_FILLED_PACKAGE.getImageDescriptor();
		activeConfigFileImage = AspectJImages.BC_SELECTED_FILE.getImageDescriptor();
	}

	public void addListener(ILabelProviderListener listener)  {
		listeners.add(listener);
	}

	public void dispose()  {
	}

	public boolean isLabelProperty(Object element, String property)  {
		return false;
	}

	public void removeListener(ILabelProviderListener listener)  {
		listeners.remove(listener);
	}

	/**
	 * @see ILabelDecorator#decorateImage
	 */
	public Image decorateImage(Image image, Object element)  {
		if (preventRecursion)
			return null;
		
		if (element instanceof ICompilationUnit){
			ICompilationUnit comp = (ICompilationUnit)element;
			try {
				element = comp.getCorrespondingResource();
			} catch (JavaModelException e) {
				element = null;
			}
		}
		
		Image img = null;
		//hook for AspectJElements (unrelated to buidconfigurator)
		//-> TODO: refactor
		if (element instanceof AJCodeElement) {
			img = getImageLabel(AspectJImages.AJ_CODE.getImageDescriptor());
		} else if (element instanceof IAspectJElement) {
			try {
				IAspectJElement ajElem = (IAspectJElement)element;
				if(ajElem.getJavaProject().getProject().exists()) {
					IProgramElement.Accessibility acceb = ajElem.getAJAccessibility();
					AJDTIcon icon = null;
					if (acceb == null){
						if (ajElem instanceof AdviceElement) {						
							icon = (AJDTIcon)iconRegistry.getAdviceIcon(ajElem.getAJExtraInformation(), AJModel.getInstance().hasRuntimeTest(ajElem));
						} else if (ajElem instanceof IntertypeElement) {
							icon = (AJDTIcon)iconRegistry.getStructureIcon(ajElem.getAJKind(), ajElem.getAJAccessibility());
						} else if (ajElem instanceof DeclareElement) {
							icon = (AJDTIcon)iconRegistry.getStructureIcon(ajElem.getAJKind(), ajElem.getAJAccessibility());
						} else {
							icon = (AJDTIcon)iconRegistry.getIcon(ajElem.getAJKind());
						}
					} else {
						icon = (AJDTIcon)iconRegistry.getStructureIcon(ajElem.getAJKind(), ajElem.getAJAccessibility());
					}
					if (icon != null){
						img = getImageLabel(getJavaImageDescriptor(icon.getImageDescriptor(), image.getBounds(), computeJavaAdornmentFlags(ajElem)));
					}
				}
			} catch (JavaModelException e) {
			}
		}

		if (img != null){			
			preventRecursion = true;
			
			//the Java ProblemsDecorator is not registered in the official
			//decorator list of eclipse, so we need it to call ourself.
			//problem: if jdt includes more decorators, we won't know it.
			img = problemsDecorator.decorateImage(img, element);
			
			//apply standard decorators (eg cvs)
			img = decman.decorateImage(img, element);
			preventRecursion = false;
			return img;
		}
		return null;
	}
	
	private Image getImageLabel(ImageDescriptor descriptor){
		if (descriptor == null) 
			return null;	
		return getRegistry().get(descriptor);
	}
	
	private ImageDescriptorRegistry getRegistry() {
		if (fRegistry == null) {
			fRegistry= JavaPlugin.getImageDescriptorRegistry();
		}
		return fRegistry;
	}
	
	public static ImageDescriptor getJavaImageDescriptor(ImageDescriptor descriptor, Rectangle rect, int adorflags) {
		int flags = (rect.width == 16)?JavaElementImageProvider.SMALL_ICONS:0;
		Point size= useSmallSize(flags) ? JavaElementImageProvider.SMALL_SIZE : JavaElementImageProvider.BIG_SIZE;
		return new JavaElementImageDescriptor(descriptor, adorflags, size);
	}
	
	private static boolean useSmallSize(int flags) {
		return (flags & JavaElementImageProvider.SMALL_ICONS) != 0;
	}

	/**
	 * @see ILabelDecorator#decorateText
	 */
	public String decorateText(String text, Object element)  {
		if (element instanceof AJCompilationUnit){
			return text.replaceFirst(".java", ".aj");  //$NON-NLS-1$  //$NON-NLS-2$
		} else {
			if (element instanceof IAspectJElement){
				try {
					if(((IAspectJElement)element).getJavaProject().getProject().exists()) {
						IProgramElement.Kind kind = ((IAspectJElement)element).getAJKind();
						if (!((kind == IProgramElement.Kind.ASPECT) || (kind == IProgramElement.Kind.ADVICE) || (kind == IProgramElement.Kind.POINTCUT) || (kind == IProgramElement.Kind.INTER_TYPE_METHOD) || (kind == IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR))){
							return text.substring(0, text.length() - 2);
						}
					}
				} catch (JavaModelException e) {
				}
			}
		}
		return null;
	}
	
	private int computeJavaAdornmentFlags(IJavaElement element) {
		int flags= 0;
		if (true && element instanceof IMember) {
			try {
				IMember member= (IMember) element;
				
				if (element.getElementType() == IJavaElement.METHOD && ((IMethod)element).isConstructor())
					flags |= JavaElementImageDescriptor.CONSTRUCTOR;
					
				int modifiers= member.getFlags();
				if (Flags.isAbstract(modifiers) && confirmAbstract(member))
					flags |= JavaElementImageDescriptor.ABSTRACT;
				if (Flags.isFinal(modifiers) || isInterfaceField(member))
					flags |= JavaElementImageDescriptor.FINAL;
				if (Flags.isSynchronized(modifiers) && confirmSynchronized(member))
					flags |= JavaElementImageDescriptor.SYNCHRONIZED;
				if (Flags.isStatic(modifiers) || isInterfaceField(member))
					flags |= JavaElementImageDescriptor.STATIC;
				
				if (Flags.isDeprecated(modifiers))
					flags |= JavaElementImageDescriptor.DEPRECATED;
				
				if (member.getElementType() == IJavaElement.TYPE) {
					if (JavaModelUtil.hasMainMethod((IType) member)) {
						flags |= JavaElementImageDescriptor.RUNNABLE;
					}
				}
			} catch (JavaModelException e) {
				// do nothing. Can't compute runnable adornment or get flags
				// can be ignored
			}
		}
		return flags;
	}
	
	private static boolean confirmAbstract(IMember element) throws JavaModelException {
		// never show the abstract symbol on interfaces or members in interfaces
		if (element.getElementType() == IJavaElement.TYPE) {
			return ((IType) element).isClass();
		}
		return element.getDeclaringType().isClass();
	}
	
	private static boolean isInterfaceField(IMember element) throws JavaModelException {
		// always show the final && static symbol on interface fields
		if (element.getElementType() == IJavaElement.FIELD) {
			return element.getDeclaringType().isInterface();
		}
		return false;
	}	
	
	private static boolean confirmSynchronized(IJavaElement member) {
		// Synchronized types are allowed but meaningless.
		return member.getElementType() != IJavaElement.TYPE;
	}
}
