package org.eclipse.contribution.jdt;

import org.eclipse.jdt.core.ToolFactory;

/**
 * This aspect tests to see if the weaving service is properly installed.
 * 
 * @author andrew
 * @created Dec 3, 2008
 *
 */
public aspect IsWovenTester {

    interface WeavingMarker { }
    
    /**
     * add a marker interface to an arbitrary class in JDT
     * later, we can see if the marker has been added.
     */
    declare parents : ToolFactory implements WeavingMarker;
    
    private static boolean weavingActive = new ToolFactory() instanceof WeavingMarker;
    
    public static boolean isWeavingActive() {
        return weavingActive;
    }
    
}
