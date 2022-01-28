/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;

/**
 * @author Alexandre Vasseur (alex AT gnilux DOT com)
 */
public class DefaultMessageHandler implements IMessageHandler {

    boolean isVerbose = false;
    boolean isDebug = false;
    boolean showWeaveInfo = false;
    boolean showWarn = true;

    public boolean handleMessage(IMessage message) throws AbortException {
        if (isIgnoring(message.getKind())) {
    		return false;
    	} else {
    		/*
    		 * TODO maw We ship this class but don't use or document it. Changed
    		 * to use stderr instead of stdout to allow improvements to LTW tests.
    		 * Currently many pass whether or not LTW occurs because they are
    		 * already woven. Some changed to check for appropriate weaving messages
    		 * as well as absence of warnings or errors.
    		 */
    		return SYSTEM_ERR.handleMessage(message);
//            if (message.getKind().isSameOrLessThan(IMessage.INFO)) {
//                return SYSTEM_OUT.handleMessage(message);
//            } else {
//                return SYSTEM_ERR.handleMessage(message);
//            }
        }
    }

    public boolean isIgnoring(IMessage.Kind kind) {
        if (kind.equals(IMessage.WEAVEINFO)) {
            return !showWeaveInfo;
        }
        if (kind.isSameOrLessThan(IMessage.INFO)) {
            return !isVerbose;
        }
        if (kind.isSameOrLessThan(IMessage.DEBUG)) {
            return !isDebug;
        }
        return !showWarn;
    }

    public void dontIgnore(IMessage.Kind kind) {
        if (kind.equals(IMessage.WEAVEINFO)) {
            showWeaveInfo = true;
        } else if (kind.equals(IMessage.DEBUG)) {
            isVerbose = true;
        } else if (kind.equals(IMessage.WARNING)) {
            showWarn = false;
        }
    }

	public void ignore(IMessage.Kind kind) {
        if (kind.equals(IMessage.WEAVEINFO)) {
            showWeaveInfo = false;
        } else if (kind.equals(IMessage.DEBUG)) {
            isVerbose = false;
        } else if (kind.equals(IMessage.WARNING)) {
            showWarn = true;
        }
	}

}
