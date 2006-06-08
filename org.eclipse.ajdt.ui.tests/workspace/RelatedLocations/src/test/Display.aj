/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package test;

/**
 * @author mchapman
 */
public aspect Display {
	public int Game.foo;

	pointcut pc(String mode) : call(Game+.new(String)) && (args(mode)
			|| args(mode));
	
	after(String mode) returning(Game game) : pc(mode) {
		System.out.println("hello");
	}
	
	declare warning : execution(* Game.run(..)) : "Declare warning";
}
