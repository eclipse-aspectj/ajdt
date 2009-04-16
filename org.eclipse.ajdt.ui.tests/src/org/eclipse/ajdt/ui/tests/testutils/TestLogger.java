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
    	if (log == null) {
    		return false;
    	}
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
        if (log == null) {
            return null;
        }
    	for (int i = log.size()-1; i >= 0; i--) {
           String logEntry = (String)log.get(i);
            if (logEntry.indexOf(msg) != -1) {
                return logEntry;
            }
        }
        return null;
    }
    
    public int numberOfEntriesForMessage(String msg) {
        if (log == null) {
            return 0;
        }
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
    
    /**
     * Clears the log
     */
    public void clearLog() {
    	if (log != null) {
        	log.clear();			
		}
    }
    
    /**
     * Prints the contents of the log to the screen - useful
     * in testcase development
     */
    public void printLog() {
    	System.out.println(""); //$NON-NLS-1$
    	System.out.println("Printing log begin ------------------------------------"); //$NON-NLS-1$
    	if (log == null) {
    	    System.out.println("Empty log"); //$NON-NLS-1$
    	}
    	for (Iterator iter = log.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			System.out.println("LOG: " + element); //$NON-NLS-1$
		}
    	System.out.println("-------------------------------------- Printing log end"); //$NON-NLS-1$
    }

    public String getLogMessages() {
        StringBuffer sb = new StringBuffer();
        if (log == null) {
            return "";
        }
        for (Iterator logIter = log.iterator(); logIter.hasNext();) {
            String msg = (String) logIter.next();
            sb.append(msg + "\n");
        }
        return sb.toString();
    }
}
