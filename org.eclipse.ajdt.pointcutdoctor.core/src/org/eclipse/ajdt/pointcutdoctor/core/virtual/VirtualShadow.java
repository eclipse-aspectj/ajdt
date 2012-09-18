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

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelShadow;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.bcel.LazyClassGen;
import org.aspectj.weaver.bcel.LazyMethodGen;

public class VirtualShadow extends BcelShadow {

	public VirtualShadow(World world, Kind kind, Member signature, LazyMethodGen enclosingMethod, BcelShadow enclosingShadow) {
		super((BcelWorld) world, kind, signature, enclosingMethod, enclosingShadow);
	}

//	public static Shadow createVirtualShadow(LazyMethodGen mg, BcelWorld world) {
//		Member sig = world.makeJoinPointSignature(mg);
//		VirtualShadow shadow = createVirtualShadow(sig, world);
//		return shadow;
//	}

	
	public String toString() {
		return "(virtual) "+super.toString();
	}

	public static VirtualShadow createVirtualShadow(Member sig, World world) {
		VirtualShadow shadow;
		if (sig.getName().equals("<init>"))
			shadow = new VirtualShadow(world, Shadow.ConstructorCall, sig, null, null);
		else 
			shadow = new VirtualShadow(world, Shadow.MethodCall, sig, null, null);
		return shadow;
	}

	@Override
	public ISourceLocation getSourceLocation() {
		return ISourceLocation.EMPTY;
	}

	@Override
	public LazyMethodGen getEnclosingMethod() {
		return null;
	}
	
	@Override
	public LazyClassGen getEnclosingClass() {
		return null;
	}
	
	@Override
	public Member getEnclosingCodeSignature() {
		return null;
	}
	
	@Override
	public ResolvedType getEnclosingType() {
		return ReferenceType.fromTypeX(UnresolvedType.OBJECT, getWorld()); //TODO how to represent UnresolvedType.EMPTY?
	}
}
