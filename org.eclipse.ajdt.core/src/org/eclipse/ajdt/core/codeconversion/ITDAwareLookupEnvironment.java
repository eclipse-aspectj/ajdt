package org.eclipse.ajdt.core.codeconversion;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.parserbridge.ITDInserter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;

public class ITDAwareLookupEnvironment extends LookupEnvironment {
    
    private List cusToRevert;
    
    private boolean insertITDs = true;
    
    public ITDAwareLookupEnvironment(LookupEnvironment wrapper, INameEnvironment nameEnvironment) {
        super(wrapper.typeRequestor, wrapper.globalOptions, wrapper.problemReporter, nameEnvironment);
    }
    
    
    public void completeTypeBindings() {
        // before completing type bindings, add ITD info to each type
        // only insert ITDs for the source types that we are parsing, 
        // not the types grabbed by the LookupEnvironment
        if (insertITDs) {
            cusToRevert = new LinkedList();
            CompilationUnitDeclaration[] units = getUnits();
            for (int i = 0; i < units.length; i++) {
                if (units[i] != null) {
                  ITDInserter visitor = new ITDInserter(findCU(units[i]), this.problemReporter);
                  units[i].traverse(visitor, units[i].scope);
                  cusToRevert.add(visitor);
                }
            }
        }
        // only insert ITDs for the units we are compiling directly
        // all others will have ITDs inserted by the ITDAwareCancelableNameEnvironment
        // don't want to insert ITDs twice.
        insertITDs = false;
        super.completeTypeBindings();
    }
    
    // not controlled externally any more
    public void setInsertITDs(boolean insertITDs) {
//        this.insertITDs = insertITDs;
    }
    
    
    private ICompilationUnit findCU(CompilationUnitDeclaration unit) {
        String fileName = new String(unit.getFileName());
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fileName));
        if (fileName.endsWith(".aj")) {
            return AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(file);
        } else {
            return (ICompilationUnit) JavaCore.create(file);
        }
    }


    static Field unitsField;
    private CompilationUnitDeclaration[] getUnits() {
        try {
            if (unitsField == null) {
                unitsField = LookupEnvironment.class.getDeclaredField("units");
                unitsField.setAccessible(true);
            }
            return (CompilationUnitDeclaration[]) unitsField.get(this);
        } catch (SecurityException e) {
        } catch (IllegalArgumentException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }
    
    /**
     * remove the inserted ITDs from these compilation units
     */
    public void revertCompilationUnits() {
        if (cusToRevert != null) {
            for (Iterator visitorIter = cusToRevert.iterator(); visitorIter.hasNext();) {
                ITDInserter visitor = (ITDInserter) visitorIter.next();
                visitor.revert();
            }
        }
    }
}
