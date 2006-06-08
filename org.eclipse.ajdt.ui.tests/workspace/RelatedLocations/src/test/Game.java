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
 * This file should contain one error and one warning.
 * @author mchapman
 */
public class Game {
    public static void main(String[] args) {
        if ( args.length == 0 )
            new Game("1").run();
        //new Game(args[0]).run();
    }

    public Game(String mode) {}
    
    public void run() {
    	System.out.println("run");
    	int gg = 5;
    }
}

















