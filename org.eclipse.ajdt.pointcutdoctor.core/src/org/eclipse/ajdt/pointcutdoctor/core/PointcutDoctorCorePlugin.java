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

import org.eclipse.ajdt.core.builder.AJBuilder;
import org.eclipse.ajdt.core.builder.IAJBuildListener;
import org.eclipse.ajdt.pointcutdoctor.core.almost.AlmostJPSPluginFacade;
import org.eclipse.ajdt.pointcutdoctor.core.almost.PointcutDoctorBuildListener;
import org.eclipse.ajdt.pointcutdoctor.core.explain.Explainer;
import org.eclipse.core.runtime.Assert;


/**
 * This is the "main" class for the Core plugin. All functionality and state of the core
 * plugin should reside or be referenced through an instance of this class. 
 * <p>
 * Note that this is not actually a plugin instance / activator class. It is instantiated by
 * the UI plugin when pointcut doctor is enabled. It is disposed off by the the UI plugin when
 * PointcutDoctor is disabled.
 * 
 * @author kdvolder
 */
public class PointcutDoctorCorePlugin {

	private Explainer explainer;
	private IAJBuildListener ajBuildListener;
	private AlmostJPSPluginFacade almostJPSPluginFacade;

	public PointcutDoctorCorePlugin() {
		initAspectJWeaverExtension();
	}
	
	private void deregisterAspectJWeaverExtension() {
		Assert.isTrue(ajBuildListener!=null);
		AJBuilder.removeAJBuildListener(ajBuildListener);
		ajBuildListener = null;
	}

	private void initAspectJWeaverExtension() {
		Assert.isTrue(ajBuildListener==null);
		ajBuildListener = new PointcutDoctorBuildListener(this);
		AJBuilder.addAJBuildListener(ajBuildListener);
	}
	
	public Explainer getExplainer() {
		if (explainer==null) {
			explainer = new Explainer();
		}
		return explainer;
	}
	
	public AlmostJPSPluginFacade getAlmostJPSPluginFacade() {
		Assert.isTrue(isEnabled(), "This instance of PointcutDoctorCorePlugin is disposed");
		if (almostJPSPluginFacade==null) {
			almostJPSPluginFacade = new AlmostJPSPluginFacade(this);
		}
		return almostJPSPluginFacade;
	}

	private boolean isEnabled() {
		return ajBuildListener!=null;
	}

	public void dispose() {
		explainer = null;
		deregisterAspectJWeaverExtension();
	}

}
