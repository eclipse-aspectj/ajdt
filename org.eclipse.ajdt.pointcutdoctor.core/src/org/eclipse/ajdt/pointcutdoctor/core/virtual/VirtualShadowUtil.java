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
package org.eclipse.ajdt.pointcutdoctor.core.virtual;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.MemberImpl;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.World;
import org.aspectj.weaver.World.TypeMap;

public class VirtualShadowUtil {

//	public static void matchVirtualShadows(PointcutRelaxMungerFactory factory) {
//		List<PointcutMunger> pointcutMungers = factory.getAllPointcutMungers();
//
//		if (pointcutMungers.size()>0) {
//			//match against virtual shadows
//			World world = ((PointcutMunger)pointcutMungers.iterator().next()).getWorld();
//			List<Shadow> vshadows = createVirtualShadows(world);
//
//			for (PointcutMunger munger:pointcutMungers)
//				for (Shadow shadow:vshadows)
//					try {
//						munger.match(shadow, world);
//					}catch(Exception e) {
//						e.printStackTrace();
//					}
//		}
//	}


	public static List<Shadow> createVirtualShadows(World world) {
		List<Shadow> shadows = new LinkedList<Shadow>();
		// create virtual calls
		if (world!=null) {
			ResolvedType[] allTypes = getAllType(world);
			if (allTypes!=null)
				for(ResolvedType type:allTypes) {
					if (type.isClass() && !type.isAnonymous() && !isAspectJClass(type)) {
						try {
							ResolvedMember[] methods = type.getDeclaredMethods();// type.getDeclaredJavaMethods();
							for (ResolvedMember method:methods) {
								String methodName = method.getName();
								if (!(isAbstractClassConstructor(type, method) ||//methodName.contains("$") || 
										methodName.equals("<clinit>") || 
										methodName.trim().length()==0 || Modifier.isPrivate(method.getModifiers()) ||
										Modifier.isProtected(method.getModifiers()) || 
										method.isAjSynthetic() || method.isBridgeMethod() || method.isSynthetic())) {
									Member sig = MemberImpl.method(type, method.getModifiers(), methodName, method.getSignature());
									shadows.add(VirtualShadow.createVirtualShadow(sig, world));
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
			}
		}
		//TODO other?
		return shadows;
	}

	private static ResolvedType[] getAllType(World world) {
		TypeMap map = world.getTypeMap();
		Collection<ResolvedType> types = map.getMainMap().values();
		return types.toArray(new ResolvedType[types.size()]);
	}

	private static boolean isAspectJClass(ResolvedType type) {
		return type.getName().startsWith("org.aspectj");
	}
	
	private static boolean isAbstractClassConstructor(ResolvedType type, Member method) {
		return type.isAbstract() && (method.getName().equals("<init>"));
	}


	public static void createAndMatchVirtualShadow(World world,
			List<ShadowMunger> pointcutMungers) {
		if (pointcutMungers.size()>0) {
			//match against virtual shadows
			List<Shadow> vshadows = createVirtualShadows(world);

			for (ShadowMunger munger:pointcutMungers)
				for (Shadow shadow:vshadows)
					try {
						munger.match(shadow, world);
					}catch(Exception e) {
						e.printStackTrace();
					}
		}
	}


}
