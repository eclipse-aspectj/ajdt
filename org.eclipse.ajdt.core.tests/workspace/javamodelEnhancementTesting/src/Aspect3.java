/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors
 *     Andrew Eisenberg - initial implementation
 *******************************************************************************/

 
import org.aspectj.lang.annotation.Aspect;
 
aspect Aspect3 {
 	@Aspect static class AtAspect1 { }
 	@org.aspectj.lang.annotation.Aspect static class AtAspect2 { }
    class Class1 { }
 	static aspect Aspect4 { }
}