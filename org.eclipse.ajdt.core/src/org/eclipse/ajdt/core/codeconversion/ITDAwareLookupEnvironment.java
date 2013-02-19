package org.eclipse.ajdt.core.codeconversion;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.ajdt.core.ReflectionUtils;
import org.eclipse.ajdt.core.parserbridge.ITDInserter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;

public class ITDAwareLookupEnvironment extends LookupEnvironment {
    
    private List<ITDInserter> cusToRevert;
    
    private boolean insertITDs = true;
    
    public ITDAwareLookupEnvironment(LookupEnvironment wrapper, INameEnvironment nameEnvironment) {
        super(wrapper.typeRequestor, wrapper.globalOptions, wrapper.problemReporter, nameEnvironment);
    }
    
    
    public void completeTypeBindings() {
        // before completing type bindings, add ITD info to each type
        // only insert ITDs for the source types that we are parsing, 
        // not the types grabbed by the LookupEnvironment
        if (insertITDs) {
            cusToRevert = new LinkedList<ITDInserter>();
            CompilationUnitDeclaration[] units = getUnits();
            for (int i = 0; i < units.length; i++) {
                if (units[i] != null) {
                  ICompilationUnit cunit = findCU(units[i]);
                  if (cunit != null) {
                      ITDInserter visitor = new ITDInserter(cunit, this, this.problemReporter);
                      units[i].traverse(visitor, units[i].scope);
                      cusToRevert.add(visitor);
                  }
                }
            }
        }
        // only insert ITDs for the units we are compiling directly
        // all others will have ITDs inserted by the ITDAwareCancelableNameEnvironment
        // don't want to insert ITDs twice.
        insertITDs = false;
        super.completeTypeBindings();
    }
    
    private ICompilationUnit findCU(CompilationUnitDeclaration unit) {
        String fileName = new String(unit.getFileName());
        IPath path = new Path(fileName);
        if (path.segmentCount() > 1) {
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            return (ICompilationUnit) JavaCore.create(file);
        } else {
            // we might be part of a PossibleMatch, which doesn't include the path part of the file name
            // get the full file name a different way.
            if (unit.compilationResult.compilationUnit instanceof PossibleMatch && 
                    ((PossibleMatch) unit.compilationResult.compilationUnit).openable instanceof ICompilationUnit) {
                return (ICompilationUnit) ((PossibleMatch) unit.compilationResult.compilationUnit).openable;
            }
        } 
        return null;
    }


    private CompilationUnitDeclaration[] getUnits() {
        return (CompilationUnitDeclaration[]) ReflectionUtils.getPrivateField(LookupEnvironment.class, "units", this);
    }
    
    /**
     * remove the inserted ITDs from these compilation units
     */
    public void revertCompilationUnits() {
        if (cusToRevert != null) {
            for (ITDInserter visitor : cusToRevert) {
                visitor.revert();
            }
        }
    }
}
