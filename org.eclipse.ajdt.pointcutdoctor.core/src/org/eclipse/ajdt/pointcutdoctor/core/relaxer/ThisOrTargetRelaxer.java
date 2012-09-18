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
package org.eclipse.ajdt.pointcutdoctor.core.relaxer;

import java.util.List;

import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.ThisOrTargetPointcut;


public class ThisOrTargetRelaxer extends AbstractRelaxer {
	@Override
	protected void doRelax(World world, List<RelaxData> results) {
//		ThisOrTargetPointcut tptc = (ThisOrTargetPointcut)affectingPointcut;
//		ResolvedType type = ((ExactTypePattern) tptc.getType()).getType().resolve(world);
//		
//		Iterator iter = type.getDirectSupertypes(); //TODO do we consider indirect super types?
//		while(iter.hasNext()) {
//			ResolvedType stype = (ResolvedType) iter.next();
//			if (!PointcutUtil.isObject(stype)) {
//				TypePattern newTp = new ExactTypePattern(stype, false, false);
//				results.add(new RelaxData(RelaxOp.Replace, newTp, this, affectingPointcut));
//			}
//		}		
//		
//		if (results.size()==0)
			// simply drop it
		results.add(new RelaxData(RelaxOp.Remove,this, affectingPointcut));
	}
	
	public boolean isThis() {
		return ((ThisOrTargetPointcut)affectingPointcut).isThis();
	}

}
