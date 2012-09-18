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
package org.eclipse.ajdt.pointcutdoctor.core.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.ReferencePointcut;

public class PointcutData {

	private Map<IProgramElement, Pointcut> pepctMap = new HashMap<IProgramElement, Pointcut>();
	private Map<IProgramElement, List<ReferencePointcut>> referencePointcutMap = new HashMap<IProgramElement, List<ReferencePointcut>>();
//	private Map<IProgramElement, ShadowSignature> shadowSignatureMap = new HashMap<IProgramElement, ShadowSignature>();
	
	public void addItem(IProgramElement pe, Pointcut pointcut) {
		pepctMap.put(pe, pointcut);
	}
	
	public Set<IProgramElement> pointcutPESet() {
		return pepctMap.keySet();
	}
	
	public Pointcut getCorrespondingPointcut(IProgramElement pe) {
		return pepctMap.get(pe);
	}

	public Pointcut findNamedPointcut(UnresolvedType type, String name) {
		Iterator<IProgramElement> peIter = pointcutPESet().iterator();
		while(peIter.hasNext()) {
			IProgramElement pe = peIter.next();
			String packageName = pe.getPackageName();
			String typeName = pe.getParent().getName();
			if (packageName!=null && !"".equals(packageName)) typeName = packageName+"."+typeName;
			if ((type==null || typeName.equals(type.getName())) && pe.getKind().equals(IProgramElement.Kind.POINTCUT)&&
					pe.getName().equals(name))
				return pepctMap.get(pe);
		}
		return null;
	}
	
	//TODO we should provide a method to remove redundancy
	public List<IProgramElement> getAdvisedProgramElements(IProgramElement ptcProgramElement) {
		AsmManager asmManager = ptcProgramElement.getModel();
		IRelationship.Kind kind = IRelationship.Kind.ADVICE;
		IRelationship rs = asmManager.getRelationshipMap().get(ptcProgramElement, kind, "advises");
		List<String> advisedHandles = rs.getTargets();
		List<IProgramElement> result = new ArrayList<IProgramElement>();
		for(int i=0; i<advisedHandles.size(); i++) {
			IProgramElement pe = asmManager.getHierarchy().findElementForHandle(
					advisedHandles.get(i));
			result.add(pe);
		}
		return result;
	}

	public void putReferencePointcut(IProgramElement pe, ReferencePointcut pointcut) {
		List<ReferencePointcut> referencePtcList = referencePointcutMap .get(pe);
		if (referencePtcList==null) {
			referencePtcList = new ArrayList<ReferencePointcut>();
			referencePointcutMap.put(pe, referencePtcList);
		}
		referencePtcList.add(pointcut); //TODO do we need to remove duplication?
	}

	public IProgramElement getCorrespondingProgramElement(Pointcut ptc) {
		for(IProgramElement pe:pepctMap.keySet())
			if (ptc==pepctMap.get(pe))
				return pe;
		return null;
	}

//	public ShadowSignature getShadowSignature(IProgramElement shadowPE) {
//		return shadowSignatureMap.get(shadowPE);
//	}
//
//	public void setShadowSignature(IProgramElement shadowPE, ShadowSignature shadowSig) {
//		shadowSignatureMap.put(shadowPE, shadowSig);
//	}

	public Pointcut getPointcutByLocation(File file, int offset) {
		//TODO any problem with referenced pointcuts?
		for (IProgramElement pe:pepctMap.keySet()) {
			ISourceLocation sl = pe.getSourceLocation();
			if (file.equals(sl.getSourceFile())&& offset==sl.getOffset())
				return pepctMap.get(pe);
		}
		return null;
	}

}
