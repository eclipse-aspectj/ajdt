/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *  
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.internal.compiler.parser;

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/
public interface ParserBasicInformation {

	public final static int

    ERROR_SYMBOL      = 138,
    MAX_NAME_LENGTH   = 41,
    NUM_STATES        = 1341,

    NT_OFFSET         = 138,
    SCOPE_UBOUND      = 380,
    SCOPE_SIZE        = 381,
    LA_STATE_OFFSET   = 20011,
    MAX_LA            = 1,
    NUM_RULES         = 1067,
    NUM_TERMINALS     = 138,
    NUM_NON_TERMINALS = 462,
    NUM_SYMBOLS       = 600,
    START_STATE       = 1330,
    EOFT_SYMBOL       = 72,
    EOLT_SYMBOL       = 72,
    ACCEPT_ACTION     = 20010,
    ERROR_ACTION      = 20011;
	
}
