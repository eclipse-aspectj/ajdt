/*******************************************************************************
 * Copyright (c) 2007 Linton Ye.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Linton Ye - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.ui;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.MemberKind;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.Pointcut;
import org.eclipse.ajdt.pointcutdoctor.core.ShadowWrapper;
import org.eclipse.ajdt.pointcutdoctor.core.virtual.VirtualShadow;
import org.eclipse.contribution.xref.core.IXReferenceNode;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;


public class ShadowNode implements IXReferenceNode, IAdaptable {

	private IJavaElement javaElement;
	private Pointcut pointcut;
	private ShadowWrapper shadowWrapper;
	private IProject project;

	public ShadowNode(IJavaElement je, Pointcut ptc, ShadowWrapper sh, IProject project) {
		javaElement = je;
		pointcut = ptc;
		shadowWrapper = sh;
		this.project = project;
	}

	public IJavaElement getJavaElement() {
		return javaElement;
	}

	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return ShadowNodeAdapter.instance;
		} 
		return null;
	}

	public IProject getProject() {
		return project;
	}
	
	public Pointcut getMainPointcut() {
		return pointcut;
	}

	public ShadowWrapper getShadowWrapper() {
		return shadowWrapper;
	}

	public String getLabel() {
		FuzzyBoolean matchResult = shadowWrapper.getMatchResult();
		return getLabel(shadowWrapper.getShadow(), matchResult.maybeTrue()&&!matchResult.alwaysTrue());
	}

	// make it static so that it's easier for testing
	public static String getLabel(Shadow shadow, boolean maybe) {
		//TODO maybe different from AJDT
		String enclosing = "";
		// XXX what if this shadow has more than one signature??? 
		String sig = signature2String(shadow, true);
		String label;
		if (isCodeShadow(shadow)) {
			enclosing = (shadow instanceof VirtualShadow) ? "(virtual) "
					: shadow.getEnclosingType().getClassName() + ": ";
			String kind = shadow.getKind().getName();
			label = enclosing+kind+"("+sig+")";
		} else label = sig;
		
		return (maybe? "? ":"") + label;
	}

	private static String signature2String(Shadow shadow, boolean fullyQualifiedNames) {
		Member signature = shadow.getSignature();
		String returnType = fullyQualifiedNames ? signature.getReturnType().getName()
				: signature.getReturnType().getClassName();
		String declaringType = fullyQualifiedNames ? signature.getDeclaringType().getName()
				: signature.getDeclaringType().getClassName();
		String name = signature.getName(); 
		String args = "";
		for(UnresolvedType t:signature.getParameterTypes()) {
			if (args.length()>0) args += ", ";
			args += fullyQualifiedNames ? t.getName()
					: t.getClassName();
		}
		if (shadow.getKind()==Shadow.ExceptionHandler)
			return args;
		else {
			if (hasArgs(signature))
				return returnType+" "+declaringType+"."+name+"("+args+")";
			else return returnType+" "+declaringType+"."+name;
		}
	}

	private static boolean hasArgs(Member signature) {
		MemberKind[] argKinds = {Member.ADVICE, Member.CONSTRUCTOR, Member.METHOD};
		for(MemberKind k:argKinds)
			if (signature.getKind()==k) return true;
		return false;
	}

	private static boolean isCodeShadow(Shadow shadow) {
		Shadow.Kind[] codekinds = {Shadow.MethodCall, Shadow.ConstructorCall,
				Shadow.ExceptionHandler, Shadow.FieldGet, Shadow.FieldSet
				//TODO more code kinds?
		};
		for (Shadow.Kind k:codekinds)
			if (shadow.getKind()==k) return true;
		return false;
	}

	static class ShadowNodeAdapter implements IWorkbenchAdapter {
	    protected ShadowNodeAdapter() { 
	    }

	    public static final ShadowNodeAdapter instance = new ShadowNodeAdapter();
		
		public String getLabel(Object o) {
			if ( o instanceof ShadowNode ) {
				ShadowNode node = (ShadowNode) o;	
				return node.getLabel();
			}
	        return null;
		}
		public Object[] getChildren(Object o) {
			// TODO Auto-generated method stub
			return null;
		}

		public ImageDescriptor getImageDescriptor(Object object) {
			// TODO Auto-generated method stub
			return null;
		}


		public Object getParent(Object o) {
			// TODO Auto-generated method stub
			return null;
		}

	}

}
