/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.ant;

import org.aspectj.ajdt.ajc.AjdtCommand;
import org.aspectj.bridge.IMessageHandler;

/**
 * 
 * @author Andrew Eisenberg
 * @created Apr 15, 2010
 */
public class AjdtCommandForAnt extends AjdtCommand {
    
    private boolean failed;
    
    @Override
    protected boolean doCommand(IMessageHandler handler, boolean repeat) {
        return failed = super.doCommand(handler, repeat);
    }
    
    public boolean isFailed() {
        return failed;
    }

}
