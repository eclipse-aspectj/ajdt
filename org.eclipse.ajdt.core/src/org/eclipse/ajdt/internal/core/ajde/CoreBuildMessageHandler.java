/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.eclipse.ajdt.internal.core.ajde;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.ajde.core.IBuildMessageHandler;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessage.Kind;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.core.runtime.Platform;

/**
 * IBuildMessageHandler which handles messages by calling AJLog.log.
 * By default ignores INFO and WEAVEINFO messages
 */
public class CoreBuildMessageHandler implements IBuildMessageHandler {

    // if AJDT is being run headless, then this message handler is active.  Otherwise, ignore any messages from the compiler.
    private static final boolean USE_LOG = Boolean.parseBoolean(System.getProperty("ajdt.showMessagesInLog", Boolean.FALSE.toString()));
    
	private List<Kind> ignoring;
	
	public CoreBuildMessageHandler() {
        ignoring = new ArrayList<Kind>();
        ignore(IMessage.INFO);
        ignore(IMessage.WEAVEINFO);
	}	

	public boolean handleMessage(IMessage message) {
	    if (!USE_LOG) {
	        return true;
	    }
        IMessage.Kind kind = message.getKind(); 
        if (kind.equals(IMessage.ABORT)) {
        	AJLog.log(AJLog.COMPILER,"AJC: Compiler error: "+message.getMessage()); //$NON-NLS-1$
    		message.getThrown().printStackTrace();
        }
        if (isIgnoring(kind)) {
            return true;
        }
        AJLog.log(AJLog.COMPILER,"AJC: "+message); //$NON-NLS-1$
		return true;
	}
	
	public void dontIgnore(Kind kind) {
	    if (null != kind) {
	        ignoring.remove(kind);
	    }
	}

	public boolean isIgnoring(Kind kind) {
		return ((null != kind) && (ignoring.contains(kind)));
	}
	
	public void ignore(Kind kind) {
	    if ((null != kind) && (!ignoring.contains(kind))) {
	        ignoring.add(kind);
	    }	
	}
}
