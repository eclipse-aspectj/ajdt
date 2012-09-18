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

import java.util.Iterator;
import java.util.List;

import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.ExactTypePattern;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.SignaturePattern;
import org.aspectj.weaver.patterns.TypePattern;
import org.eclipse.ajdt.pointcutdoctor.core.utils.PointcutUtil;
import org.eclipse.ajdt.pointcutdoctor.core.utils.PointcutUtil.Op;



public class DeclaringTypeRelaxer extends SignatureRelaxer {
	
	
	private boolean matchTypesInAllPackages(TypePattern type) {
		//TODO what else?
		return (type.isStar() || type.toString().contains(".."));
	}


	@Override
	protected void doRelax(World world, List<RelaxData> results) {
		KindedPointcut kptc = (KindedPointcut)affectingPointcut;
		SignaturePattern sig = kptc.getSignature();
		TypePattern declaringType = sig.getDeclaringType();
		// add +
		if (!declaringType.isIncludeSubtypes()) {
			TypePattern newDt = PointcutUtil.cloneTypePatternAndModify(declaringType, Op.IncludeSubtypes);
			results.add(new RelaxData(RelaxOp.Replace, newDt, this, affectingPointcut));
		}
		// promote to super type
		if (declaringType instanceof ExactTypePattern && !inConstructorPointcut()) {
			//TODO do we promote to all super types?
			ResolvedType type = ((ExactTypePattern)declaringType).getType().resolve(world);
			
			Iterator<ResolvedType> iter = type.getDirectSupertypes();

			while(iter.hasNext()) {
				ResolvedType superclass = iter.next();
				if (superclass!=null && !PointcutUtil.isObject(superclass)) {
					TypePattern newDt = new ExactTypePattern(superclass, declaringType.isIncludeSubtypes(), 
							declaringType.isVarArgs());
					results.add(new RelaxData(RelaxOp.Replace, newDt, this, affectingPointcut));
				}
			}
		}
		// add package *..
		if (!(declaringType instanceof ExactTypePattern)) {
			if (!matchTypesInAllPackages(declaringType)) {
//				List names = new ArrayList();
//				TypePattern newType = new WildTypePattern(names, false, 0);
				//XXX not done yet!
			}
		}
	}


	private boolean inConstructorPointcut() {
		return this.affectingPointcut.toString().contains("new("); //TODO ugly way!
	}


}
