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
import org.aspectj.weaver.patterns.HandlerPointcut;
import org.aspectj.weaver.patterns.TypePattern;
import org.eclipse.ajdt.pointcutdoctor.core.utils.PointcutUtil;
import org.eclipse.ajdt.pointcutdoctor.core.utils.PointcutUtil.Op;



public class HandlerRelaxer extends AbstractRelaxer {

	@Override
	protected void doRelax(World world, List<RelaxData> results) {
		HandlerPointcut hptc = (HandlerPointcut)affectingPointcut;
		TypePattern type = PointcutUtil.getExceptionType(hptc);
		
		// add +
		if (!type.isIncludeSubtypes()) {
			TypePattern newDt = PointcutUtil.cloneTypePatternAndModify(type, Op.IncludeSubtypes);
			results.add(new RelaxData(RelaxOp.Replace, newDt, this, affectingPointcut));
		}
	}
	
//	@Override
//	public List<RelaxedPointcut> relax(RelaxedPointcut rptc, World world) {
//		List<RelaxedPointcut> results = new ArrayList<RelaxedPointcut>();
//		HandlerPointcut hptc = (HandlerPointcut)rptc.getCorrespondingPointcut(this);
//		TypePattern tp = hptc.getExceptionType();
//		
//		// add +
//		TypePattern newTp = PointcutUtil.cloneTypePatternAndModify(tp, PointcutUtil.Op.IncludeSubtypes);
//		HandlerPointcut newPtc = new HandlerPointcut(newTp);
//		results.add(createRelaxedPointcutByReplacingOldPtc(rptc,hptc, newPtc));
//		
//		return results;
//	}
	
	
	
}
