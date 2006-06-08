/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.ui.tests.testutils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.IAJLogger;
import org.eclipse.ajdt.core.builder.AJBuilder;

/**
 * Logger to help with builder tests
 */
public class TestLogger implements IAJLogger {

    private List log;
    
    public TestLogger() {
    	// need to register state listener to get feedback about builds
    	AJBuilder.addStateListener();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ajdt.core.IAJLogger#log(java.lang.String)
     */
    public void log(String msg) {
        if (log == null) {
            log = new ArrayList();
        }
        log.add(msg);
    }

    public void log(int category, String msg) {
    	log(msg);
    }
    
    public boolean containsMessage(String msg) {
        for (Iterator iter = log.iterator(); iter.hasNext();) {
            String logEntry = (String) iter.next();
            if (logEntry.indexOf(msg) != -1) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Return the first line in the log that contains the given string
     * @param msg
     * @return
     */
    public String getMostRecentMatchingMessage(String msg) {
    	for (int i = log.size()-1; i >= 0; i--) {
           String logEntry = (String)log.get(i);
            if (logEntry.indexOf(msg) != -1) {
                return logEntry;
            }
        }
        return null;
    }
    
    public int numberOfEntriesForMessage(String msg) {
        int occurances = 0;
        for (Iterator iter = log.iterator(); iter.hasNext();) {
            String logEntry = (String) iter.next();
            StringBuffer sb = new StringBuffer(logEntry);
            if (sb.indexOf(msg) != -1) {
                occurances++;
            }
        }
        return occurances;
    }
    
}
