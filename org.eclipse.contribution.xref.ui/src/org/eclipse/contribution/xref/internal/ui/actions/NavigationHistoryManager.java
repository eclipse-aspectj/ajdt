/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.internal.ui.actions;

import java.util.Stack;

/**
 * This class keeps track of navigations from a view, and provides
 * forward and backward navigation history
 *
 */
public class NavigationHistoryManager {

	private Stack back,forward;

	public NavigationHistoryManager() {
		back = new Stack();
		forward = new Stack();
	}
	
	/**
	 * true if there are any elements available for backward navigation
	 */
	public boolean hasBack() {
		return !back.isEmpty();
	}
	
	/**
	 * true if there are any elements available for forward navigation
	 */
	public boolean hasForward() {
		return !forward.isEmpty();
	}
	
	/**
	 * Inform the history manager that we want to go back, passing the
	 * currently displayed object.
	 * @return the object that the view should display as the result
	 * of a navigate back action
	 */
	public Object goBack(Object o) {
		if ((o != null) && !o.equals(peekForward())) {
			forward.push(o);
		}
		return hasBack() ? back.pop() : null;
	}
	
	/**
	 * What object if any is at the top of the back list?
	 */
	public Object peekBack() {
		return hasBack() ? back.peek() : null;
	}
	
	/**
	 * Inform the history manager that we want to go forward, passing the
	 * currently displayed object
	 * @return the object that the view should display as the result
	 * of a navigate forward action
	 */
	public Object goForward(Object o) {
		if ((o != null) && !o.equals(peekBack())) {
			back.push(o);
		}
		return hasForward() ? forward.pop() : null;
	}
	
	/**
	 * What object if any is at the top of the forward list?
	 */
	public Object peekForward() {
		return hasForward() ? forward.peek() : null;
	}

	/**
	 * Inform the history manager that we are about to navigate from
	 * the given object, and may wish to come back to it at some point.
	 */
	public void nowLeaving(Object o) {
		if ((o != null) && !o.equals(peekBack())) {
			back.push(o);
		}
	}
	
	
}
