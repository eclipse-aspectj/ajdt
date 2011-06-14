/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *      Matthew Webster - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.ras;

import org.aspectj.lang.JoinPoint;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * FFDC aspect the reports caught exceptions to Eclipse Error Log
 */
public abstract aspect PluginFFDC extends FFDC {

    /** 
     * Template method to obtain plug-in ID for IStatus objects
     * 
     * @return String e.g. "org.eclipse.ajdt.core"
     */ 
    protected abstract String getPluginId ();
    
    /** 
     * Template method to consume FFDC
     */ 
    protected abstract void log (IStatus status);
    
    protected void processStaticFFDC (Throwable th, String sourceId) {
        logException(th,sourceId,null);
    }

    protected void processNonStaticFFDC (Throwable th, Object obj, String sourceId) {
        logException(th,sourceId,obj);
    }
    
    /**
     * Build IStatus messages contain details of caught exception, where it was
     * caught and the object (if any) that caught it
     */
    public void logException (Throwable th, String sourceId, Object obj) {
        IStatus status = null;
        if (th instanceof JavaModelException && ((JavaModelException)th).isDoesNotExist()) {
            // Ignore ".. does not exist" Exceptions
            return;
        } else if (th instanceof ResourceException && (((ResourceException)th).getStatus().getCode() == IResourceStatus.RESOURCE_NOT_FOUND || ((ResourceException)th).getStatus().getCode() == IResourceStatus.PROJECT_NOT_OPEN)) {
            // Ignore "resource does not exist" and "resource is not open" Exceptions
            return;
        } else if (th instanceof AbortCompilation) {
            //ignore abort compilation
            return;
        } else if (th instanceof CoreException) {
            status = ((CoreException) th).getStatus();
        } else {
            try {
                IStatus sourceChild = new Status(IStatus.ERROR,getPluginId(),IStatus.OK,sourceId,null);
                String message = th.getMessage();
                if (message == null) {
                    message = th.toString();
                }

                IStatus[] children;
                if (obj == null) {
                    children = new IStatus[] { sourceChild };
                }
                else {
                    IStatus objectChild = introspect(obj);
                    children = new IStatus[] { sourceChild, objectChild };
                }
                
                status = new MultiStatus(getPluginId(),IStatus.OK,children,message,th);
            }
            catch (Exception ex) {
                status = new Status(IStatus.ERROR,getClass().getName(),IStatus.OK,sourceId,th);
            }
        }
        log(status);
    }

    /**
     * Obtain object identity and class as well as field values
     * 
     * @param obj Object to be introspected
     * @return MultiStatus object
     */
    private IStatus introspect (Object obj) {
        final Class sjpClass = JoinPoint.StaticPart.class;
        String message;
        List fieldValues = new LinkedList();
        IStatus[] fieldValuesArray;

        /* Handle any NullPointerExceptions */
        try {
            message = obj.toString();
        }
        catch (Exception ex) {
            message = safeToString(obj);
        }
        
        /* Handle any SecurityExceptions */
        try {
            Field[] fields = obj.getClass().getDeclaredFields();
            
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                /* Omit static join point fields */
                if (!sjpClass.isAssignableFrom(field.getType())) {
                    Object value = field.get(obj);
                    String fieldMessage = field.getName() + "=" + safeToString(value); //$NON-NLS-1$
                    fieldValues.add(new Status(IStatus.INFO,getPluginId(),IStatus.OK,fieldMessage,null));
                }
            }

            fieldValuesArray = new IStatus[fieldValues.size()];
            fieldValues.toArray(fieldValuesArray);
        }
        catch (Exception ex) {
            fieldValuesArray = new IStatus[] {};
        }

        IStatus result = new MultiStatus(getPluginId(),IStatus.INFO,fieldValuesArray,message,null);
        return result;
    }
    
    private static String safeToString (Object obj) {
        if (obj == null) return "null"; //$NON-NLS-1$
        else if (obj instanceof String) return "\"" + obj.toString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
        else return (obj.getClass() + "@" + obj.hashCode()); //$NON-NLS-1$
    }

}
