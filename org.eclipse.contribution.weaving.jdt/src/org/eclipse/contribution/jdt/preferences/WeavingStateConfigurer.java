/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.contribution.jdt.preferences;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

import org.eclipse.contribution.jdt.IsWovenTester;
import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 * @author Andrew Eisenberg
 * @created Jan 19, 2009
 *
 * Object that controls the state of the Equinox aspects weaver
 */
public class WeavingStateConfigurer {
    
    private final static Version MIN_WEAVER_VERSION = new Version(1, 6, 1);

    private final static boolean IS_WEAVING = IsWovenTester.isWeavingActive();

    
    public String getWeaverVersionInfo() {
        BundleDescription weaver = 
            Platform.getPlatformAdmin().getState(false).
            getBundle("org.aspectj.weaver", null);
        
        if (weaver != null) {
            if (MIN_WEAVER_VERSION.compareTo(weaver.getVersion()) <= 0) {
                return "";
//                return "AspectJ weaver version " + weaver.getVersion().toString() + " OK!";
            } else {
                return "No compatible version of org.aspectj.weaver found.  " +
                "JDT Weaving requires 1.6.1 or higher.  Found version " +
                weaver.getVersion();
            }
        } else {
            return "org.aspectj.weaver not installed.  JDT Weaving requires 1.6.3 or higher.";
        }
    }
    
    public IStatus changeWeavingState(boolean becomeEnabled) {
        // now, weaving service is controlled by 
        // starting and stopping weaving.aspectj bundle
        // this method is used only to ensure the hook exists
        IStatus success;
        boolean isCurrentlyWeaving = false;
        try {
            isCurrentlyWeaving = currentConfigStateIsWeaving();
        } catch (Exception e) {
            JDTWeavingPlugin.logException(e);
        }
        if (becomeEnabled && !isCurrentlyWeaving) {
            success = changeConfigDotIni(becomeEnabled);
        } else {
            // do nothing
            success = Status.OK_STATUS;
        }
        
        
        IStatus success2 = changeAutoStartupAspectsBundle(becomeEnabled);
        if (success != Status.OK_STATUS) {
            return success;
        } else if (success2 != Status.OK_STATUS) {
            return success2;
        } else {
            return new Status(IStatus.OK, JDTWeavingPlugin.ID,
                    "Weaving service successfully "
                    + (becomeEnabled ? "ENABLED" : "DISABLED"));
        } 
    }

    private IStatus changeAutoStartupAspectsBundle(boolean becomeEnabled) {
        Bundle b = Platform.getBundle("org.eclipse.equinox.weaving.aspectj"); //$NON-NLS-1$
        if (b == null) {
            return new Status(IStatus.ERROR, JDTWeavingPlugin.ID, "Could not find org.eclipse.equinox.weaving.aspectj" +
            		" so weaving service cannot be " + 
            		(becomeEnabled ? "enabled" : "disabled") + ".");
        }
        try {
            if (becomeEnabled) {
                b.start();
            } else {
                b.stop();
            }
            
            return Status.OK_STATUS;
        } catch (BundleException e) {
            return new Status(IStatus.ERROR, JDTWeavingPlugin.ID, "Error occurred in setting org.eclipse.equinox.weaving.aspectj to autostart" +
                    " so weaving service cannot be " + 
                    (becomeEnabled ? "enabled" : "disabled") + ".", e);
        }
    }

    private IStatus changeConfigDotIni(boolean becomeEnabled) {
        
        // a little crude find the config.ini go through each 
        // line and filter out the osgi.framework.extensions line
        IStatus success;
        try {
            String configArea = getConfigArea();
            File f = new File(new URI(configArea));
            BufferedReader br = new BufferedReader(new FileReader(f));
            
            String newConfig = internalChangeWeavingState(becomeEnabled, br);
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(newConfig);
            bw.close();

            
            if (becomeEnabled == currentConfigStateIsWeaving()) {
                success = Status.OK_STATUS;
            } else {
                success = new Status(IStatus.ERROR, JDTWeavingPlugin.ID, "Could not add or remove org.eclipse.equinox.weaving.hook as a framework adaptor.");
            }
            
        } catch (Exception e) {
            success = new Status(IStatus.ERROR, JDTWeavingPlugin.ID, e
                    .getMessage(), e);
        }
        return success;
    }

    protected String internalChangeWeavingState(boolean becomeEnabled, 
            BufferedReader br) throws IOException {
        StringBuffer sb = new StringBuffer();
        String line = null;
        boolean hookAdded = false;
        while ((line = br.readLine()) != null) {
            if (line.trim().startsWith("osgi.framework.extensions=")) {
                String[] split = line.split("=");
                if (split.length > 1) {
                    String[] extNames = split[1].split(",");
                    boolean shouldAddLine = false;
                    StringBuffer sb2 = new StringBuffer();
                    sb2.append("osgi.framework.extensions=");
                    for (int i = 0; i < extNames.length; i++) {
                        String extName = extNames[i].trim();
                        // don't add hook, we add it later in needed
                        if (!extName.equals("org.eclipse.equinox.weaving.hook")) {
                            sb2.append(extName + ",");
                            shouldAddLine = true;
                        }
                    }
                    if (shouldAddLine) {
                        if (becomeEnabled) {
                            sb2.append("org.eclipse.equinox.weaving.hook\n");
                            hookAdded = true;
                        } else {
                            // replace last comma
                            sb2.replace(sb2.length() - 1, sb2.length(), "\n");
                        }
                        sb.append(sb2);
                    }
                }
            } else {
                sb.append(line + "\n");
            }
        }

        // if line didn't exist before
        if (becomeEnabled && !hookAdded) {
            sb.append("osgi.framework.extensions=org.eclipse.equinox.weaving.hook\n");
        }
        try {
            br.close();
        } catch (IOException e) {
            JDTWeavingPlugin.logException(e);
        }
        return sb.toString();
    }

    private String getConfigArea() {
        String configArea = FrameworkProperties.getProperty("osgi.configuration.area") + "config.ini";
        configArea = configArea.replaceAll(" ", "%20");
        return configArea;
    }
    
    
    public boolean currentConfigStateIsWeaving() throws Exception {
        String configArea = getConfigArea();
        
        File f = new File(new URI(configArea));
        if (! f.exists()) {
            throw new FileNotFoundException("Could not find config file: " + f.getAbsolutePath());
        }
        if (! f.canWrite()) {
            throw new IOException("Could not write to config file: " + f.getAbsolutePath());
        }
        BufferedReader br = new BufferedReader(new FileReader(f));
        return internalCurrentConfigStateIsWeaving(br);
    }

    protected boolean internalCurrentConfigStateIsWeaving(BufferedReader br)
            throws IOException {
        String line = null;
        while ((line = br.readLine()) != null) {
            if (line.trim().startsWith("osgi.framework.extensions=") &&
                    line.contains("org.eclipse.equinox.weaving.hook")) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isWeaving() {
        return IS_WEAVING;
    }
}
