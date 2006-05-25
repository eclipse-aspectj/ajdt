/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.examples.progressmon;

/**
 * Codes to identify the different violations detected
 */
public enum RuleViolations {
	CALL_BEGIN_TWICE, CALL_BEGIN_AFTER_DONE, CALL_DONE_TWICE,
	CALL_DONE_WITHOUT_BEGIN, CALL_WORKED_WITHOUT_BEGIN,
	CALL_WORKED_AFTER_DONE, OVER_REPORTING, SUBPROGRESS_WITHOUT_BEGIN,
	SUBPROGRESS_AFTER_DONE;
}
