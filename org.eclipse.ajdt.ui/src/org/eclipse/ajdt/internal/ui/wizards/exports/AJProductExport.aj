///*******************************************************************************
// * Copyright (c) 2006 IBM Corporation and others.
// * All rights reserved. This program and the accompanying materials 
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// * 
// * Contributors:
// *     IBM Corporation - initial API and implementation
// *     Matt Chapman - initial version
// *******************************************************************************/
//package org.eclipse.ajdt.internal.ui.wizards.exports;
//
//import org.eclipse.ajdt.core.exports.AJBuildScriptGenerator;
//import org.eclipse.ajdt.internal.core.exports.FeatureExportOperation;
//import org.eclipse.pde.internal.build.BuildScriptGenerator;
//
///**
// * Make the feature export operation use our build script generator.
// */
//public aspect AJProductExport {
//
//	BuildScriptGenerator around() : call(BuildScriptGenerator.new(..))
//		&& within(FeatureExportOperation) {
//		return new AJBuildScriptGenerator();
//	}
//	
//}
