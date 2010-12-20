package org.eclipse.ajdt.core.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IProgramElement.Kind;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.DeclareElement;
import org.eclipse.ajdt.core.javaelements.FieldIntertypeElement;
import org.eclipse.ajdt.core.javaelements.IAspectJElement;
import org.eclipse.ajdt.core.javaelements.MethodIntertypeElement;
import org.eclipse.ajdt.core.javaelements.PointcutElement;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.tests.model.AJModelTest4;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ImportContainer;

import static junit.framework.Assert.*;
import static org.aspectj.asm.IProgramElement.Kind.*;

public class HandleTestUtils {

	private static Map<Kind, Class<?>> kindMap = new HashMap<IProgramElement.Kind, Class<?>>();

	public static void checkAJHandle(String origAjHandle, AJProjectModelFacade model) throws JavaModelException {
	    
	    if (origAjHandle.endsWith("binaries")) {
	        return;
	    }
	    
	    IJavaElement origJavaElement = model.programElementToJavaElement(origAjHandle);
	    boolean checkExistence = !(origJavaElement instanceof ImportContainer || 
	    		                   origJavaElement instanceof IInitializer);
	    String origJavaHandle = origJavaElement.getHandleIdentifier();
		
		if (origJavaElement.getJavaProject().getProject().equals(model.getProject())) {

		    if (checkExistence) {
		        assertTrue("Element for AJHandle doesn't exist: "+origAjHandle, 
		                origJavaElement.exists());
		    }
		    checkType(origJavaElement);
		
			IProgramElement recreatedAjElement = model.javaElementToProgramElement(origJavaElement);
			String recreatedAjHandle = recreatedAjElement.getHandleIdentifier();
			
			IJavaElement recreatedJavaElement = model.programElementToJavaElement(recreatedAjHandle);
		    if (checkExistence) {
		        assertTrue("Doesn't exist: " + recreatedJavaElement.getHandleIdentifier(), recreatedJavaElement.exists());
		    }
		    checkType(recreatedJavaElement);
		    
			String recreatedJavaHandle = recreatedJavaElement.getHandleIdentifier();
	        
	        assertEquals("Handle identifier of JavaElements should be equal",  //$NON-NLS-1$
	                origJavaHandle, recreatedJavaHandle);
	        
	        assertEquals("Handle identifier of ProgramElements should be equal",  //$NON-NLS-1$
	                origAjHandle, recreatedAjHandle);
	        
	        assertEquals("JavaElements should be equal",  //$NON-NLS-1$
	                origJavaElement, recreatedJavaElement);
	        
	        assertEquals("JavaElement names should be equal",  //$NON-NLS-1$
	                origJavaElement.getElementName(), recreatedJavaElement.getElementName());
	        
	        assertEquals("JavaElement types should be equal",  //$NON-NLS-1$
	                origJavaElement.getElementType(), recreatedJavaElement.getElementType());
	        
	        assertEquals("JavaElement parents should be equal",  //$NON-NLS-1$
	                origJavaElement.getParent(), recreatedJavaElement.getParent());
	        
	        assertEquals("JavaElement parents should be equal",  //$NON-NLS-1$
	                origJavaElement.getJavaProject(), recreatedJavaElement.getJavaProject());
	        
	        assertEquals("JavaElement resources should be the same",  //$NON-NLS-1$
	                origJavaElement.getResource(), recreatedJavaElement.getResource());
		} else {
		    // reference to another project
		    assertTrue("Program Element in other project should exist, but doesn't: " + origJavaHandle, //$NON-NLS-1$
		            origJavaElement.exists());
		    
		    // check to make sure that this element is in the other model
		    AJProjectModelFacade otherModel = AJProjectModelFactory.getInstance().getModelForProject(origJavaElement.getJavaProject().getProject());
		    IProgramElement ipe = otherModel.javaElementToProgramElement(origJavaElement);
		    checkAJHandle(ipe.getHandleIdentifier(), otherModel);
		}
	}

	/** 
	 * Check whether the class of the object agrees with its AJKind. (Only doing this for select AJKinds and types now,
	 * but maybe we should do this for more kinds.
	 * 
	 * @throws JavaModelException 
	 */
	private static void checkType(IJavaElement javaElement) throws JavaModelException {
		Kind kind = getAjKind(javaElement);
		if (kind != null) {
			assertEquals("The JavaElement "+javaElement+" is an instance of the wrong class", getClassForKind(kind), javaElement.getClass());
		}
	}

	private static Class<?> getClassForKind(Kind kind) {
		return kindMap.get(kind);
	}

	static {
		//Specify what type of object is expected for a given AspectJ Kind below...
		kindMap.put(ADVICE, 						AdviceElement.class);
		kindMap.put(ASPECT, 						AspectElement.class);
		
		kindMap.put(DECLARE_ERROR, 					DeclareElement.class);
		kindMap.put(DECLARE_PARENTS, 				DeclareElement.class);
		kindMap.put(DECLARE_SOFT,					DeclareElement.class);
		kindMap.put(DECLARE_ANNOTATION_AT_FIELD,	DeclareElement.class);
		kindMap.put(DECLARE_ANNOTATION_AT_METHOD,	DeclareElement.class);
		kindMap.put(DECLARE_ANNOTATION_AT_TYPE,		DeclareElement.class);
		kindMap.put(DECLARE_WARNING, 				DeclareElement.class);
		
		kindMap.put(INTER_TYPE_FIELD, 				FieldIntertypeElement.class);
		kindMap.put(INTER_TYPE_METHOD, 				MethodIntertypeElement.class);
		kindMap.put(INTER_TYPE_CONSTRUCTOR, 		MethodIntertypeElement.class);
		
		kindMap.put(POINTCUT, 						PointcutElement.class);
		
// TODO: specify expected class for other AJKind? 
//       but they are not currently covered by the tests (or we would
//       be getting a test failure when they are not specified!)
//		
//		PROJECT,  PACKAGE, FILE, FILE_JAVA, FILE_ASPECTJ, FILE_LST, IMPORT_REFERENCE, CLASS,
//			INTERFACE, ENUM, ENUM_VALUE, ANNOTATION, INITIALIZER,,
//			INTER_TYPE_PARENT, CONSTRUCTOR, METHOD, FIELD, DECLARE_PARENTS,
//			CODE, ERROR, DECLARE_ANNOTATION_AT_CONSTRUCTOR,
//			SOURCE_FOLDER,
//			PACKAGE_DECLARATION	}
	}
	
	/**
	 * Get the AJKind for given javeElement if it can be determined.
	 * @param javaElement
	 * @return null if this is not an AspectJ element, or if its kind can not be determined.
	 */
	private static Kind getAjKind(IJavaElement javaElement) {
		if (javaElement instanceof IAspectJElement) {
			try {
				IAspectJElement ajElement = (IAspectJElement) javaElement;
				return ajElement.getAJKind();
			}
			catch (JavaModelException e) {
				return null;
			}
		}
		return null;
	}

	public static List<String> checkJavaHandle(String origJavaHandle, AJProjectModelFacade model) {
	    List<String> accumulatedErrors = new ArrayList<String>();
	    
	    try {
	        
	        IJavaElement origJavaElement = JavaCore.create(origJavaHandle);
	        IProgramElement origAjElement = model.javaElementToProgramElement(origJavaElement);
	        String origAjHandle = origAjElement.getHandleIdentifier();
	        
	        // AspectJ adds the import container always even when there are no imports
	        if (!origJavaElement.exists() && !(origJavaElement instanceof ImportContainer)
	        && !(origJavaElement instanceof IInitializer) ) { // Bug 263310
	            accumulatedErrors.add("Java element " + origJavaElement.getHandleIdentifier() + " does not exist");
	        }
	        
	        if (origJavaElement.getJavaProject().getProject().equals(model.getProject())) {
	        
	            IProgramElement recreatedAjElement = model.javaElementToProgramElement(origJavaElement);
	            String recreatedAjHandle = recreatedAjElement.getHandleIdentifier();
	            
	            IJavaElement recreatedJavaElement = model.programElementToJavaElement(recreatedAjHandle);
	            String recreatedJavaHandle = recreatedJavaElement.getHandleIdentifier();
	            
	            
	            if (!origJavaHandle.equals(recreatedJavaHandle)) {
	                accumulatedErrors.add("Handle identifier of JavaElements should be equal:\n\t" + origJavaHandle + "\n\t" + recreatedJavaHandle);
	            }
	            
	            if (!origAjHandle.equals(recreatedAjHandle)) {
	                accumulatedErrors.add("Handle identifier of ProgramElements should be equal:\n\t" + origAjHandle + "\n\t" + recreatedAjHandle);
	            }
	            
	            if (!origJavaElement.equals(recreatedJavaElement)) {
	                accumulatedErrors.add("JavaElements should be equal:\n\t" + origJavaElement + "\n\t" + recreatedJavaElement);
	            }
	            
	            if (!origJavaElement.getElementName().equals(recreatedJavaElement.getElementName())) {
	                accumulatedErrors.add("JavaElement names should be equal:\n\t" + origJavaElement.getElementName() + "\n\t" + recreatedJavaElement.getElementName());
	            }
	            
	            if (origJavaElement.getElementType()!= recreatedJavaElement.getElementType()) {
	                accumulatedErrors.add("JavaElement types should be equal:\n\t" + origJavaElement.getElementType() + "\n\t" + recreatedJavaElement.getElementType());
	            }
	            
	            if (!origJavaElement.getParent().equals(recreatedJavaElement.getParent())) {
	                accumulatedErrors.add("JavaElement parents should be equal:\n\t" + origJavaElement.getParent() + "\n\t" + recreatedJavaElement.getParent());
	            }
	            
	            if (!origJavaElement.getJavaProject().equals(recreatedJavaElement.getJavaProject())) {
	                accumulatedErrors.add("JavaElement projects should be equal:\n\t" + origJavaElement.getJavaProject() + "\n\t" + recreatedJavaElement.getJavaProject());
	            }
	        } else {
	            // reference to another project
	            if (!origJavaElement.exists()) {
	                accumulatedErrors.add("Program Element in other project should exist, but doesn't:\n\t" + origJavaHandle );
	            }
	
	            
	            // check to make sure that this element is in the other model
	            AJProjectModelFacade otherModel = AJProjectModelFactory.getInstance().getModelForProject(origJavaElement.getJavaProject().getProject());
	            IProgramElement ipe = otherModel.javaElementToProgramElement(origJavaElement);
	            HandleTestUtils.checkAJHandle(ipe.getHandleIdentifier(), otherModel);
	        }
	    } catch (Exception e) {
	        accumulatedErrors.add("Error thrown on: " + origJavaHandle);
	        accumulatedErrors.add(e.toString());
	        for (int i = 0; i < e.getStackTrace().length; i++) {
	            accumulatedErrors.add("\t" + e.getStackTrace()[i].toString());
	        }
	    }
	    return accumulatedErrors;
	}
	
}
