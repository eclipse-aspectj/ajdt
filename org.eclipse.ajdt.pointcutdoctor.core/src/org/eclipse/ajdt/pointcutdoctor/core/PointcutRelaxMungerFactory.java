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
package org.eclipse.ajdt.pointcutdoctor.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aspectj.weaver.Advice;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.CustomMungerFactory;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.World;
import org.eclipse.ajdt.pointcutdoctor.core.virtual.VirtualShadowUtil;


public class PointcutRelaxMungerFactory implements CustomMungerFactory {

	private Map<String, List<ShadowMunger>> mungerMap = new HashMap<String, List<ShadowMunger>>();
	
	public Collection<ShadowMunger> createCustomShadowMungers(
			ResolvedType aspectType) {
		List<ShadowMunger> newm = new ArrayList<ShadowMunger>();
		//for named pointcuts
		ResolvedMember[] pointcuts = aspectType.getDeclaredPointcuts();
		for (ResolvedMember ptc:pointcuts)
			if (ptc instanceof ResolvedPointcutDefinition) {//TODO why "instanceof" is needed here?
				newm.add(PointcutMunger.createPointcutMunger((ResolvedPointcutDefinition)ptc));
			}
		// for advice: anonymous pointcuts and others
		Iterator<ShadowMunger> adviceIter = aspectType.getDeclaredAdvice().iterator();
		while (adviceIter.hasNext()) {
			Object o = adviceIter.next();
			if (o instanceof Advice) 
				newm.add(PointcutMunger.createPointcutMunger((Advice)o));
		}

		mungerMap.put(aspectType.getErasureSignature(), newm);
		VirtualShadowUtil.createAndMatchVirtualShadow(aspectType.getWorld(), newm);
		return newm;
	}

	public Collection<ConcreteTypeMunger> createCustomTypeMungers(
			ResolvedType aspectType) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ShadowMunger> getAllPointcutMungers() {
		//TODO: We should not create a list, but an iterator that doesn't require the creation of 
		// large data structure, just to iterate over it.
		List<ShadowMunger> allShadowMungers = new LinkedList<ShadowMunger>();
		for (List<ShadowMunger> someMungers : mungerMap.values()) {
			for (ShadowMunger munger : someMungers) {
				allShadowMungers.add(munger);
			}
		}
		return allShadowMungers;
	}

	public Collection<Shadow> createCustomShadows(World world) {
		return VirtualShadowUtil.createVirtualShadows(world);
	}

	public Collection<ShadowMunger> getAllCreatedCustomShadowMungers() {
		return getAllPointcutMungers();
	}

	public Collection<ConcreteTypeMunger> getAllCreatedCustomTypeMungers() {
		return Collections.emptyList();
	}
	
//	private PointcutRelaxMungerFactory() {}
//	
//	private static PointcutRelaxMungerFactory instance;
//	
//	public static PointcutRelaxMungerFactory getInstance() {
//		if (instance==null)
//			instance = new PointcutRelaxMungerFactory();
//		return instance;
//	}

}
