/*******************************************************************************
 * Copyright (c) 2008 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      SpringSource
 *      Andrew Eisenberg (initial implementation)
 *******************************************************************************/
package org.eclipse.ajdt.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajde.core.AjCompiler;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.HierarchyWalker;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.asm.internal.Relationship;
import org.aspectj.asm.internal.RelationshipMap;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.jdt.internal.core.ImportContainer;
import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.core.builder.AJBuilder;
import org.eclipse.ajdt.core.builder.IAJBuildListener;
import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.AspectJMemberElement;
import org.eclipse.ajdt.core.javaelements.AspectJMemberElementInfo;
import org.eclipse.ajdt.core.javaelements.CompilationUnitTools;
import org.eclipse.ajdt.core.lazystart.IAdviceChangedListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ImportDeclaration;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * 
 * @created Sep 8, 2008
 *
 * This class is a facade for the AspectJ compiler structure model and relationship
 * map.
 * <p> 
 * One object of this class exists for each AspectJ project.  
 * It is created during a full build and lasts until a clean build or
 * another full build is performed.  
 * <p>
 * Objects of this class should be instantiated using the 
 * {@link AJProjectModelFactory} class.
 */
public class AJProjectModelFacade {
    
    public final static IJavaElement ERROR_JAVA_ELEMENT = new CompilationUnit(null, "ERROR_JAVA_ELEMENT", null);
    
    private static class ProjectModelBuildListener implements IAJBuildListener {
        private Set/*IProject*/ beingBuilt = new HashSet();
        private Set/*IProject*/ beingCleaned = new HashSet();
        
        public synchronized void postAJBuild(int kind, IProject project,
                boolean noSourceChanges) {
            beingBuilt.remove(project);
        }

        public synchronized void postAJClean(IProject project) {
            beingCleaned.remove(project);
        }

        public synchronized void preAJBuild(int kind, IProject project,
                IProject[] requiredProjects) {
            if (kind == IncrementalProjectBuilder.CLEAN_BUILD) {
                beingCleaned.add(project);
            } else {
                beingBuilt.add(project);
            }
        }

        public void addAdviceListener(IAdviceChangedListener adviceListener) { }
        public void removeAdviceListener(IAdviceChangedListener adviceListener) { }
        
        
        synchronized boolean isCurrentlyBuilding(IProject project) {
            return beingBuilt.contains(project) || beingCleaned.contains(project);
        }
    }
    

    /**
     * The aspectj program hierarchy
     */
    IHierarchy structureModel;
    
    /**
     * stores crosscutting relationships between structure elements
     */
    IRelationshipMap relationshipMap;
    
    /**
     * the java project that this project model is associated with
     */
    private final IProject project;
    
    boolean isInitialized;
    
    boolean disposed;
    
    private static ProjectModelBuildListener buildListener = new ProjectModelBuildListener();
    
    /**
     * creates a new project model facade for this project
     */
    AJProjectModelFacade(IProject project) {
        this.project = project;
        this.disposed = false;
        this.isInitialized = false;
    }
    
    public static void installListener() {
        AJBuilder.addAJBuildListener(buildListener);
    }
    

    /**
     * grabs the structure and relationships for this project
     * <p> 
     * called by the before advice in EnsureInitialized aspect
     */
    synchronized void init() {
        if (!buildListener.isCurrentlyBuilding(project)) {
            AjCompiler compiler = AspectJPlugin.getDefault().getCompilerFactory().getCompilerForProject(project);
            AsmManager existingState = compiler.getModel();
            if (existingState != null) {
                relationshipMap = existingState.getRelationshipMap();
                structureModel = existingState.getHierarchy();
                if (relationshipMap != null && structureModel != null) {
                    isInitialized = true;
                }
            }
        } else {
            // can't initialize...building
        }
    }    

    /**
     * @return true if the AspectJ project model has been found.  false otherwise.
     */
    public boolean hasModel() {
        return isInitialized && !disposed && 
                !buildListener.isCurrentlyBuilding(project);
    }
    
    /**
     * @param handle an AspectJ program element handle
     * @return a program element for the handle, or an empty element
     * if the program element is not found
     */
    public IProgramElement getProgramElement(String handle) {
        IProgramElement ipe = structureModel.findElementForHandleOrCreate(handle, false);
        if (ipe != null) {
            return ipe;
        } else {
            // occurs when the handles are not working properly
//            AspectJPlugin.getDefault().getLog().log(new Status(IStatus.WARNING, AspectJPlugin.PLUGIN_ID, 
//                    "Could not find the AspectJ program element for handle: " + 
//                    handle, new RuntimeException()));
            return IHierarchy.NO_STRUCTURE;
        }
    }
    
    /**
     * @return the 1-based line number for the given java element
     */
    public int getJavaElementLineNumber(IJavaElement je) {
        IProgramElement ipe = javaElementToProgramElement(je);
        return ipe.getSourceLocation() != null ? ipe.getSourceLocation().getLine() : 1;
    }

    /**
     * @return a human readable name for the given Java element that is
     * meant to be displayed on menus and labels.
     */
    public String getJavaElementLinkName(IJavaElement je) {
        IProgramElement ipe = javaElementToProgramElement(je);
        if (ipe != IHierarchy.NO_STRUCTURE) {  // null if model isn't initialized
            String name = ipe.toLinkLabelString(false);
            if ((name != null) && (name.length() > 0)) {
                return name;
            }
        }
        // use element name instead, qualified with parent
        if (je.getParent() != null) {
            return je.getParent().getElementName() + '.' + je.getElementName();
        }
        return je.getElementName();
    }

    /**
     * @return a program element that corresponds to the given java element.
     */
    public IProgramElement javaElementToProgramElement(IJavaElement je) {
        if (!isInitialized) {
            return IHierarchy.NO_STRUCTURE;
        }
        String ajHandle = je.getHandleIdentifier();
        
        if (isBinaryHandle(ajHandle)) {
            ajHandle = convertToAspectJBinaryHandle(ajHandle);
        }
        
        // check to see if we need to replace { (compilation unit) with * (aj compilation unit)
        // if using cuprovider, then aj compilation units have {, but needs to change to *
        ICompilationUnit cu =  null;
        if (je instanceof IMember) {
            cu = ((IMember) je).getCompilationUnit();
        } else if (je instanceof IPackageDeclaration) {
            IJavaElement parent = ((IPackageDeclaration) je).getParent();
            if (parent instanceof ICompilationUnit) {
                cu = (ICompilationUnit) parent;
            }
        } else if (je instanceof AJCodeElement) {
            cu = ((AJCodeElement) je).getCompilationUnit();
            // get the occurence count 
            int count = ((AJCodeElement) je).occurrenceCount;
            // need the first bang after the last close paren
            int lastParen = ajHandle.lastIndexOf(')');
            int firstBang = ajHandle.indexOf(JavaElement.JEM_COUNT, lastParen);
            if (firstBang > -1) {
                ajHandle = ajHandle.substring(0, firstBang);
                if (count > 1) {
                    // there is more than one element
                    // with this name
                    ajHandle += "!" + count;
                }
            }
            
        } else if (je instanceof ILocalVariable) {
            IOpenable openable = ((ILocalVariable) je).getOpenable();
            cu = openable != null && openable instanceof ICompilationUnit ?
                    (ICompilationUnit) openable : null;
        } else if (je instanceof ImportDeclaration) {
            cu = ((ImportDeclaration) je).getCompilationUnit();
        } else if (je instanceof ImportContainer) {
            cu = ((ImportContainer) je).getCompilationUnit();  
        } else if (je instanceof ICompilationUnit) {
            cu = (ICompilationUnit) je;
        }
        if (cu != null &&
                CoreUtils.ASPECTJ_SOURCE_ONLY_FILTER.accept(cu.getResource().getName())) {
            ajHandle = ajHandle.replaceFirst("\\" + JavaElement.JEM_COMPILATIONUNIT, 
                    Character.toString(AspectElement.JEM_ASPECT_CU));
        }
        
        ajHandle = ajHandle.replaceFirst("declare \\\\@", "declare @");

        IProgramElement ipe = structureModel.findElementForHandleOrCreate(ajHandle, false);
        if (ipe == null) {
            // occurs when the handles are not working properly
//            AspectJPlugin.getDefault().getLog().log(new Status(IStatus.WARNING, AspectJPlugin.PLUGIN_ID, 
//                    "Could not find the AspectJ program element for handle: " + 
//                    ajHandle, new RuntimeException()));
            return IHierarchy.NO_STRUCTURE;
        }
        return ipe;
    }

    /**
     * This will return false in the cases where a java elt exists, but
     * a program elt does not.  This may happen when there are some kinds
     * of errors in the file that prevent the aspectj compiler from running, but
     * the Java compiler can still run.
     * @param je
     * @return
     */
    public boolean hasProgramElement(IJavaElement je) {
        return IHierarchy.NO_STRUCTURE != javaElementToProgramElement(je);
    }
    
    
    private String convertToAspectJBinaryHandle(String ajHandle) {
        int packageRootIndex = ajHandle.indexOf(JavaElement.JEM_PACKAGEFRAGMENTROOT);
        int packageIndex = ajHandle.indexOf(JavaElement.JEM_PACKAGEFRAGMENT, packageRootIndex);
        String newHandle = ajHandle.substring(0, packageRootIndex+1) + "binaries" + ajHandle.substring(packageIndex);
        return newHandle;
    }

    private boolean isBinaryHandle(String ajHandle) {
        int jemClassIndex = ajHandle.indexOf(JavaElement.JEM_CLASSFILE);
        if (jemClassIndex != -1) {
            int classFileIndex = ajHandle.indexOf(".class", jemClassIndex);
            if (classFileIndex != -1) {
                return true;
            }
        }
        return false;
    }

    public IJavaElement programElementToJavaElement(IProgramElement ipe) {
        return programElementToJavaElement(ipe.getHandleIdentifier());
    }
    
    public IJavaElement programElementToJavaElement(String ajHandle) {
        // check to see if this is a spurious handle. 
        // For ITDs, the aspectj compiler generates program elements before the
        // rest of the program is in place, and they therfore have no parent.
        // They should not exist and we can ignore them.
        if (ajHandle.charAt(0) != '=') {
            return ERROR_JAVA_ELEMENT;
        }
        
        String jHandle = ajHandle;
        
        
        // are we dealing with something inside of a classfile?
        // if so, then we have to handle it specially
        // because we want to convert this into a source reference if possible
        int classFileIndex = jHandle.indexOf(JavaElement.JEM_CLASSFILE);
        if (classFileIndex != -1) {
            // now make sure this isn't a code element
            int dotClassIndex = jHandle.indexOf(".class");
            if (dotClassIndex != -1) {
                char typeChar = jHandle.charAt(dotClassIndex + ".class".length());
                if (typeChar == AspectElement.JEM_ASPECT_TYPE ||
                        typeChar == JavaElement.JEM_TYPE) {
                    return getElementFromClassFile(jHandle);
                }
            }
        }

        // if using cuprovider, then we don not use the '*' for Aspect compilation units,
        // it uses the '{' of Java Compilation Units
        if (AspectJPlugin.USING_CU_PROVIDER) {
            jHandle = jHandle.replaceFirst("\\" + AspectElement.JEM_ASPECT_CU, 
                    Character.toString(JavaElement.JEM_COMPILATIONUNIT));
        }

        int codeEltIndex = jHandle.indexOf(AspectElement.JEM_CODEELEMENT);
        if (codeEltIndex != -1) {
            // because code elements are sub classes of local variables
            // must make the code element's handle look like a local
            // variable's handle
            int countIndex = jHandle.lastIndexOf('!');
            int count = 0;
            if (countIndex > codeEltIndex) {
                try {
                    count = Integer.parseInt(jHandle.substring(countIndex+1));
                    jHandle = jHandle.substring(0, countIndex);
                } catch (NumberFormatException e) {
                    // if the count is not from the code element, but from one of its parents
                    count = 0;
                }
            }
            jHandle += "!0!0!0!0!I";
            if (count > 1) {
                jHandle += "!" + count;
            }
        }
        
        // add escapes to various sundries
        jHandle = jHandle.replaceFirst("declare @", "declare \\\\@");
        jHandle = jHandle.replaceFirst("\\.\\*", ".\\\\*");
        
        IJavaElement je = AspectJCore.create(jHandle);
        if (je == null) {
            // occurs when the handles are not working properly
//            AspectJPlugin.getDefault().getLog().log(new Status(IStatus.WARNING, AspectJPlugin.PLUGIN_ID, 
//                    "Could not find the Java program element for handle: " + 
//                    jHandle, new RuntimeException()));
            return ERROR_JAVA_ELEMENT;
        }
        return je;
    }

    
    private IJavaElement getElementFromClassFile(String jHandle) {
        IProgramElement ipe = structureModel.findElementForHandleOrCreate(jHandle, false);

        String packageName = ipe.getPackageName();
        // need to find the top level type
        IProgramElement candidate = ipe;
        while (candidate != null && candidate.getKind() != IProgramElement.Kind.FILE) {
            candidate = candidate.getParent();
        }
        String typeName;
        if (candidate != null) {
            int typeIndex = 0;
            while (typeIndex < candidate.getChildren().size() &&
                    ((IProgramElement) candidate.getChildren().get(typeIndex)).getKind() 
                    == IProgramElement.Kind.IMPORT_REFERENCE) {
                typeIndex++;
            }
            if (typeIndex < candidate.getChildren().size()) {
                typeName = ((IProgramElement) candidate.getChildren().get(typeIndex)).getName();
            } else {
                typeName = "";
            }
        } else {
            typeName = "";
        }        
        String qualifiedName = (packageName.length() > 0 ? packageName + "." : "")
            + typeName;
        try {
            // this gives us the type in the current project,
            // but we don't want this if the type exists as source in 
            // some other project in the workspace.
            ITypeRoot unit = getTypeFromQualifiedName(qualifiedName);
            
            
            if (unit instanceof ICompilationUnit) {
                // we're in luck...
                // all this work has taken us straight to the compilaiton unit
                if (unit instanceof ICompilationUnit) {
                    AJCompilationUnit newUnit = CompilationUnitTools.convertToAJCompilationUnit((ICompilationUnit) unit);
                    unit = newUnit != null ? newUnit : unit;
                }
                return unit.getElementAt(offsetFromLine(unit, ipe.getSourceLocation()));
                
            } else {
                // we have a class file.
                // search the rest of the workspace for this type
                IResource file = unit.getResource();
                
                // try to find the source
                if (file != null && !file.getFileExtension().equals("jar")) {
                    // we have a class file that is not in a jar.
                    // can we find this as a source file in some project?
                    IPath path = unit.getPath();
                    IJavaProject otherProject = JavaCore.create(project).getJavaModel().getJavaProject(path.segment(0));
                    if (otherProject.exists()) {
                        IType type = otherProject.findType(qualifiedName);
                        unit = type.getTypeRoot();
                        if (unit instanceof ICompilationUnit) {
                            AJCompilationUnit newUnit = CompilationUnitTools.convertToAJCompilationUnit((ICompilationUnit) unit);
                            unit = newUnit != null ? newUnit : unit;
                        }
                    }
                    return unit.getElementAt(offsetFromLine(unit, ipe.getSourceLocation()));
    
                } else {
                    // we have a class file in a jar
                    // try finding the source by creating a handle identifier
                    // if the source is not found, this will bring up a class file editor
                    int classIndex = jHandle.indexOf(".class");
                    String newHandle = unit.getHandleIdentifier() + 
                            jHandle.substring(classIndex+".class".length());
                    
                    IJavaElement newElt = (IJavaElement) AspectJCore.create(newHandle);
                    if (newElt instanceof AspectJMemberElement) {
                        AspectJMemberElement ajElt = (AspectJMemberElement) newElt;
                        Object info = ajElt.getElementInfo();
                        if (info instanceof AspectJMemberElementInfo) {
                            AspectJMemberElementInfo ajInfo = (AspectJMemberElementInfo) info;
                            ajInfo.setSourceRangeStart(offsetFromLine(unit, ipe.getSourceLocation()));
                        }
                    }
                    return newElt;
                }
            }
        } catch (JavaModelException e) {
            AspectJPlugin.getDefault().getLog().log(new Status(IStatus.WARNING, 
                    AspectJPlugin.PLUGIN_ID, "Could not find type root for " + jHandle, e));
            return ERROR_JAVA_ELEMENT;
        } catch (NullPointerException e) {
            AspectJPlugin.getDefault().getLog().log(new Status(IStatus.WARNING, 
                    AspectJPlugin.PLUGIN_ID, "Could not find type root for " + jHandle, e));
            return ERROR_JAVA_ELEMENT;
        }
    }

    private ITypeRoot getTypeFromQualifiedName(String qualifiedName)
            throws JavaModelException {
        IJavaProject jproj = JavaCore.create(project);
        IType type = jproj.findType(qualifiedName);
        // won't work if type is in a .aj file
        if (type != null) {
            return type.getTypeRoot();
        }
        
        // try by looking for the package instead
        // will not work for inner types
        // but that's ok, because we are only working
        // top-level types
        int dotIndex = qualifiedName.lastIndexOf('.');
        String typeName = qualifiedName.substring(dotIndex+1);
        String packageName = qualifiedName.substring(0,dotIndex);
        IPackageFragmentRoot[] pkgRoots = jproj.getAllPackageFragmentRoots();
        IPackageFragment pkg = null;
        for (int i = 0; i < pkgRoots.length; i++) {
            IPackageFragment candidate = pkgRoots[i].getPackageFragment(packageName);
            if (candidate.exists()) {
                pkg = candidate;
                break;
            }
        }
        if (pkg == null) {
            return (ICompilationUnit) ERROR_JAVA_ELEMENT;
        }
        ICompilationUnit[] cus = pkg.getCompilationUnits();
        int dollarIndex = typeName.lastIndexOf('$');
        
        // for compilation units, use the type name
        // without the top level type
        String typeNameNoParent;
        if (dollarIndex > -1) {
            typeNameNoParent = typeName.substring(dollarIndex+1);
        } else {
            typeNameNoParent = typeName;
        }
        // XXX uh-oh, will not find types declared in
        // methods.  Worry about this later
        for (int i = 0; i < cus.length; i++) {
            IType[] types = cus[i].getAllTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].getElementName().equals(typeNameNoParent)) {
                    return cus[i];
                }
            }
        }
        IClassFile[] cfs = pkg.getClassFiles();
        for (int i = 0; i < cfs.length; i++) {
            IType cType = cfs[i].getType();
            if (cType.getElementName().equals(typeName)) {
                    return cfs[i];
            }
        }
        return (ICompilationUnit) ERROR_JAVA_ELEMENT;
    }
    
    private int offsetFromLine(ITypeRoot unit, ISourceLocation sloc) throws JavaModelException {
        if (sloc.getOffset() > 0) {
            return sloc.getOffset();
        }
        
        if (unit instanceof AJCompilationUnit) {
            AJCompilationUnit ajUnit = (AJCompilationUnit) unit;
            ajUnit.requestOriginalContentMode();
        }
        IBuffer buf = unit.getBuffer();
        if (unit instanceof AJCompilationUnit) {
            AJCompilationUnit ajUnit = (AJCompilationUnit) unit;
            ajUnit.discardOriginalContentMode();
        }
        if (buf != null) {
            int requestedLine = sloc.getLine();
            int currentLine = 1;
            int offset = 0;
            while (offset < buf.getLength() && currentLine < requestedLine) {
                if (buf.getChar(offset++) == '\n') {
                    currentLine++;
                }
            }
            while (offset < buf.getLength() && Character.isWhitespace(buf.getChar(offset))) {
                offset++;
            }
            return offset;
        } 
        // no source code
        return 0;
    }


    public boolean hasRuntimeTest(IJavaElement je) {
        if (!isInitialized) {
            return false;
        }
        IProgramElement ipe = javaElementToProgramElement(je);
        List relationships = relationshipMap.get(ipe);
        if (relationships != null) {
            for (Iterator relIter = relationships.iterator(); relIter
                    .hasNext();) {
                IRelationship rel = (IRelationship) relIter.next();
                if (rel.hasRuntimeTest()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * A hierarchy walker that trim off branches and cut its walk
     * short
     *
     */
    class CancellableHierarchyWalker extends HierarchyWalker {
        private boolean cancelled = false;
        public IProgramElement process(IProgramElement node) {
            preProcess(node);
            if (!cancelled) {
                node.walk(this);
            } else {
                cancelled = false;
            }
            postProcess(node);
            return node;
        }

        protected void cancel() {
            cancelled = true;
        }
    }

    
    /**
     * find out what java elements are on a particular line
     */
    public List/*IJavaElement*/ getJavaElementsForLine(ICompilationUnit icu, final int line) {
        IProgramElement ipe = javaElementToProgramElement(icu);
        final List/*IProgramElement*/ elementsOnLine = new LinkedList();
        
        // walk the program element to get all ipes on the source line
        ipe.walk(new CancellableHierarchyWalker() {
            protected void preProcess(IProgramElement node) {
                ISourceLocation sourceLocation = node.getSourceLocation();
                if (sourceLocation != null) {
                    if (sourceLocation.getEndLine() < line) {
                        // we don't need to explore the rest of this branch
                        cancel();
                    } else if (sourceLocation.getLine() == line) {
                        elementsOnLine.add(node);
                    }
                }
            }
        });
        // now convert to IJavaElements
        List /*IJavaElement*/ javaElements = new ArrayList(elementsOnLine.size());
        for (Iterator ipeIter = elementsOnLine.iterator(); ipeIter.hasNext();) {
            IProgramElement ipeOnLine = (IProgramElement) ipeIter.next();
            javaElements.add(programElementToJavaElement(ipeOnLine));
        }
        return javaElements;
    }
    
    /**
     * find the relationships of a particular kind for a java element
     */
    public List/*IJavaElement*/ getRelationshipsForElement(IJavaElement je, AJRelationshipType relType) {
        if (!isInitialized) {
            return Collections.EMPTY_LIST;
        }
        IProgramElement ipe = javaElementToProgramElement(je);
        List/*Relationship*/ relationships = relationshipMap.get(ipe);
        if (relationships != null) {
            List/*IJavaElement*/ relatedJavaElements = new ArrayList(relationships.size());
            if (relationships != null) {
                for (Iterator iterator = relationships.iterator(); iterator.hasNext();) {
                    Relationship rel = (Relationship) iterator.next();
                    if (relType.getDisplayName().equals(rel.getName())) {
                        for (Iterator targetIter = rel.getTargets().iterator(); targetIter
                                .hasNext();) {
                            String handle = (String) targetIter.next();
                            IJavaElement targetJe = programElementToJavaElement(handle);
                            if (targetJe != null && targetJe != ERROR_JAVA_ELEMENT) {
                                relatedJavaElements.add(targetJe);
                            } else {
                                // ignore handles that start with *
                                // these are handles from ITDs that are created earlyvoi
                                if (! handle.startsWith("*")) {
                                    AspectJPlugin.getDefault().getLog().log(new Status(IStatus.WARNING, 
                                            AspectJPlugin.PLUGIN_ID, "Could not create a Java element " +
                                            "with handle:\n" + handle, new RuntimeException()));
                                }
                            }
                        }
                    }
                }
            }
            return relatedJavaElements;
        } else {
            // means something was wrong.
            // something not initialized or the
            // java handles don't mesh with the aspectj handles
            return Collections.EMPTY_LIST;
        }
    }
    
    /**
     * walks the file and grabs all relationships for it
     * could cache this to go faster
     */
    public Map/*Integer,List<IRelationship>*/ getRelationshipsForFile(ICompilationUnit icu) {
        // walk the hierarchy and get relationships for each node
        final Map/*Integer, List<IRelationship>*/ allRelationshipsMap = new HashMap();
        IProgramElement ipe = javaElementToProgramElement(icu);
        ipe.walk(new HierarchyWalker() {
            protected void preProcess(IProgramElement node) {
                List/*IRelationship*/ nodeRels = relationshipMap.get(node);
                if (nodeRels != null && nodeRels.size() > 0) {
                    List/*IRelationship*/ allRelsForLine;
                    Integer line = new Integer(node.getSourceLocation().getLine());
                    if (allRelationshipsMap.containsKey(line)) {
                        allRelsForLine = (List) allRelationshipsMap.get(line);
                    } else {
                        allRelsForLine = new LinkedList();
                        allRelationshipsMap.put(line, allRelsForLine);
                    }
                    allRelsForLine.addAll(nodeRels);
                }                
            }
        });
        return allRelationshipsMap;
    }
    
    /**
     * I don't like how the 3 methods getRelationshipsForXXX return very different things.
     * I am trying to be efficient and not do too much processing on my end, but this leads
     * to having different return types.  Maybe return each as an iterator.  That would be nice.
     */
    public List/*IRelationship*/ getRelationshipsForProject(AJRelationshipType[] relType) {
        Set interesting = new HashSet();
        for (int i = 0; i < relType.length; i++) {
            interesting.add(relType[i].getDisplayName());
        }
        if (relationshipMap instanceof RelationshipMap) {
            RelationshipMap map = (RelationshipMap) relationshipMap;
            // flatten and filter the map
            List allRels = new LinkedList();
            for (Iterator relListIter = map.values().iterator(); relListIter.hasNext();) {
                List/*IRelationship*/ relList = (List) relListIter.next();
                for (Iterator relIter = relList.iterator(); relIter.hasNext();) {
                    IRelationship rel = (IRelationship) relIter.next();
                    if (interesting.contains(rel.getName())) {
                        allRels.add(rel);
                    }
                }
            }
            return allRels;
        } else {
            // shouldn't happen
            return Collections.EMPTY_LIST;
        }
    }
    
    public boolean isAdvised(IJavaElement elt) {
        if (!isInitialized) {
            return false;
        }
        
        IProgramElement ipe = javaElementToProgramElement(elt);
        if (ipe != IHierarchy.NO_STRUCTURE) {
            List/*IRelationship*/ rels = relationshipMap.get(ipe);
            if (rels != null && rels.size() > 0) {
                for (Iterator relIter = rels.iterator(); relIter.hasNext();) {
                    IRelationship rel = (IRelationship) relIter.next();
                    if (!rel.isAffects()) {
                        return true;
                    }
                }
            }
            // check children if the children would not otherwise be in 
            // outline view (ie- code elements)
            if (ipe.getKind() != IProgramElement.Kind.CLASS && ipe.getKind() != IProgramElement.Kind.ASPECT) {
                List /*IProgramElement*/ ipeChildren = ipe.getChildren();
                if (ipeChildren != null) {
                    for (Iterator childIter = ipeChildren.iterator(); childIter
                            .hasNext();) {
                        IProgramElement child = (IProgramElement) childIter.next();
                        if (child.getKind() == IProgramElement.Kind.CODE) {
                            rels = relationshipMap.get(child);
                            for (Iterator relIter = rels.iterator(); relIter.hasNext();) {
                                IRelationship rel = (IRelationship) relIter.next();
                                if (!rel.isAffects()) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public Set /* IJavaElement */ aspectsForFile(ICompilationUnit cu) {
        IProgramElement ipe = javaElementToProgramElement(cu);
        // compiler should be able to do this for us, but functionality is
        // not exposed. so let's do it ourselves
        final Set /* IJavaElement */ aspects = new HashSet();
        ipe.walk(new HierarchyWalker() {
            protected void preProcess(IProgramElement node) {
                if (node.getKind() == IProgramElement.Kind.ASPECT) {
                    aspects.add(programElementToJavaElement(node));
                }
            }
        });
        return aspects;
    }
    
    void dispose() {
        structureModel = null;
        relationshipMap = null;
        isInitialized = false;
        disposed = true;
    }

    public boolean isDisposed() {
        return disposed;
    }
    
    public IProject getProject() {
        return project;
    }
    
    public static String printHierarchy(IHierarchy h) {
        final StringBuffer sb = new StringBuffer();
        HierarchyWalker walker = new HierarchyWalker() {
            int depth = 0;
            int MAX = 200;
            int curr = 0;
            
            protected void preProcess(IProgramElement node) {
                if (curr < MAX) {
                    sb.append(spaces(depth));
                    sb.append(node.getHandleIdentifier());
                    sb.append("\n");
                } if (curr == MAX) {
                    sb.append("...");
                }
                curr++;
                depth+=2;
            }
            protected void postProcess(IProgramElement node) {
                depth-=2;
            }
            
            String spaces(int depth) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < depth; i++) {
                    sb.append(" ");
                }
                return sb.toString();
            }
        };
        h.getRoot().walk(walker);
        return sb.toString();
    }
    
    IRelationshipMap getAllRelationships() {
        return relationshipMap;
    }
}