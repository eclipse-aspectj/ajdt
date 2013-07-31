/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.internal.compiler.parser;

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/
public interface ParserBasicInformation {

    int 

    ERROR_SYMBOL      = 125,
    MAX_NAME_LENGTH   = 41,
    NUM_STATES        = 1265,

    NT_OFFSET         = 125,
    SCOPE_UBOUND      = 366,
    SCOPE_SIZE        = 367,
    LA_STATE_OFFSET   = 19170,
    MAX_LA            = 1,
    NUM_RULES         = 986,
    NUM_TERMINALS     = 125,
    NUM_NON_TERMINALS = 420,
    NUM_SYMBOLS       = 545,
    START_STATE       = 1226,
    EOFT_SYMBOL       = 70,
    EOLT_SYMBOL       = 70,
    ACCEPT_ACTION     = 19169,
    ERROR_ACTION      = 19170;
}
