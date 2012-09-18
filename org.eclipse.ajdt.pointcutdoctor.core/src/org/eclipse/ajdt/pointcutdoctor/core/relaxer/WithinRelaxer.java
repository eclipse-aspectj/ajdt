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
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.patterns.WithinPointcut;
import org.eclipse.ajdt.pointcutdoctor.core.utils.PointcutUtil;
import org.eclipse.ajdt.pointcutdoctor.core.utils.PointcutUtil.Op;



public class WithinRelaxer extends AbstractRelaxer {

	@Override
	protected void doRelax(World world, List<RelaxData> results) {
		// simply drop it
//		results.add(new RelaxData(RelaxOp.Remove,this, affectingPointcut));

		// add +
		WithinPointcut wptc = (WithinPointcut)affectingPointcut;
		TypePattern type = wptc.getTypePattern();
		if (!type.isIncludeSubtypes()) {
			TypePattern newDt = PointcutUtil.cloneTypePatternAndModify(type, Op.IncludeSubtypes);
			results.add(new RelaxData(RelaxOp.Replace, newDt, this, affectingPointcut));
		}
	}
}
