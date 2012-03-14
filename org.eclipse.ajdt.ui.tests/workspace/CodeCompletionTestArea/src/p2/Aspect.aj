/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Luzius Meisser  - initial version
 *******************************************************************************/
package p2;

public aspect Aspect {
	
	int x37;
	
	public int Foo.bar;
	
	public int meth(){
		meth()/*completion test pos A*/
		
		return 7;
	}
	
	public int Class.interMethod(){
		int localInt;
		/*completion test pos B*/
		return 7;
	}
	
	after(): execution(* *(..)){
		/*completion test pos E*/
	}
	before(): execution(* *(..)){
	    d/*completion test pos F*/
	}
	
}
