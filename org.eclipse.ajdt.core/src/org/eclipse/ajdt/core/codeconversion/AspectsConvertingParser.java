/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.codeconversion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.asm.IProgramElement;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.core.compiler.InvalidInputException;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.internal.core.ras.NoFFDC;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * The purpose of this parser is to convert AspectJ code into similar Java code
 * which allows us to reuse for example for jdt formatting or comment
 * generation.
 * 
 * Depending on the ConversionOptions it gets called with, it does: - replace
 * the keyword "aspect" by "class " - replace all the '.'s in intertype
 * declarations by an '$'s to make them look like an ordinary declarations. -
 * erase the keywords "returning", "throwing", "privileged", "issingleton" e.g.:
 * "after() throwing (Exception e)" -> "after( Exception e)" - erase pointcut
 * designators (includes "percflow" & co.) - add dummy references to all erased
 * class references to end of buffer to make "organize imports" work correctly -
 * add a reference to the target class inside intertype method declarations to
 * simulate the context switch necessary to get proper code completion.
 * (A detailed description of how code completion works in AJDT can be found in
 * bug 74419.)
 * 
 * Restrictions: - class names inside pointcut designators must begin with a
 * capital letter to be recognised as such
 * 
 * 
 * @author Luzius Meisser
 */
public class AspectsConvertingParser implements TerminalTokens, NoFFDC {

	private static final String IMPLEMENTS = "implements";

    private static final String EXTENDS = "extends";

    private static final char[] throwing = "throwing".toCharArray(); //$NON-NLS-1$

    private static final char[] returning = "returning".toCharArray(); //$NON-NLS-1$

    private static final char[] percflow = "percflow".toCharArray(); //$NON-NLS-1$

    private static final char[] percflowbelow = "percflowbelow".toCharArray(); //$NON-NLS-1$

    private static final char[] perthis = "perthis".toCharArray(); //$NON-NLS-1$

    private static final char[] pertarget = "pertarget".toCharArray(); //$NON-NLS-1$

    private static final char[] issingleton = "issingleton".toCharArray(); //$NON-NLS-1$

    private static final char[] pertypewithin = "pertypewithin".toCharArray(); //$NON-NLS-1$

    private static final char[] classs = "class ".toCharArray(); //$NON-NLS-1$

    private static final char[] privileged = "          ".toCharArray(); //$NON-NLS-1$

    private static final String thizString = "thiz"; //$NON-NLS-1$

    private final static char[] tjpRefs2 = "org.aspectj.lang.JoinPoint thisJoinPoint; org.aspectj.lang.JoinPoint.StaticPart thisJoinPointStaticPart; org.aspectj.lang.JoinPoint.StaticPart thisEnclosingJoinPointStaticPart;" //$NON-NLS-1$
    .toCharArray();

    private final static char[] endThrow = new char[] { '(', ':' };

    
    public class Replacement {
    	//the position in the original char[]
    	public int posBefore;
    
    	//the position in the new char[], or -1 if not yet applied
    	public int posAfter;
    
    	//the number of chars that get replaced
    	public int length;
    
    	//the content to be inserted
    	public char[] text;
    
    	//the number of additional chars (lengthAdded == text.length - length)
    	public int lengthAdded;
    
    	public Replacement(int pos, int length, char[] text) {
    		this.posBefore = pos;
    		this.posAfter = -1;
    		this.length = length;
    		this.text = text;
    		lengthAdded = text.length - length;
    	}
    
    }

    public AspectsConvertingParser(char[] content) {
    	this.content = content;
    	this.typeReferences = new HashSet();
    	this.usedIdentifiers = new HashSet();
    	replacements = new ArrayList(5);
    }

    public char[] content;

	private Set typeReferences;

	private Set usedIdentifiers;

	private ConversionOptions options;
	
	/**
	 * can be null.  used for determining ITDs
	 */
	private ICompilationUnit unit;

	//list of replacements
	//by convetion: sorted by posBefore in ascending order
	private ArrayList replacements;

	protected Scanner scanner;

	private boolean inPointcutDesignator;

	private boolean inAspect;

	private boolean inAspectDeclaration;
	
    private boolean inClassDeclaration;
    
    private boolean inInterfaceDeclaration;
	
	private int posColon;

    public void setUnit(ICompilationUnit unit) {
        this.unit = unit;
    }


    //returns a list of Insertions to let the client now what has been inserted
	//into the buffer so he can translate positions from the old into the new
	//buffer
	public ArrayList convert(ConversionOptions options) {
		this.options = options;
		boolean insertIntertypeDeclarations = options
				.isThisJoinPointReferencesEnabled();
		boolean addReferencesForOrganizeImports = options
				.isDummyTypeReferencesForOrganizeImportsEnabled();
		boolean isSimulateContextSwitchNecessary = (options.getTargetType() != null);

		scanner = new Scanner();
		scanner.setSource(content);

		inPointcutDesignator = false;
		inAspect = false;
		inAspectDeclaration = false;
		inClassDeclaration = false;
		inInterfaceDeclaration = false;
		
		// Bug 93248: Count question marks so as to ignore colons that are part of conditional statements		
		int questionMarkCount = 0; 
		
		// count paren levels so that we know if we are in a intertype declaration name or not
		int parenLevel = 0;
		
		// Bug 110751: Ignore colons that are part of enhanced "for" loop in Java 5
		boolean insideFor = false;
		
		char[] currentTypeName = null;
		
		replacements.clear();
		typeReferences.clear();
		usedIdentifiers.clear();

		int tok;
		int pos;
		int typeDeclStart = 0;
		while (true) {

			try {
				tok = scanner.getNextToken();
			} catch (InvalidInputException e) {
				continue;
			}
			if (tok == TokenNameEOF)
				break;

			switch (tok) {
			case TokenNameIdentifier:
				if (!inAspect && !inTypeDeclaration())
					break;

				char[] name = scanner.getCurrentIdentifierSource();

				if (inTypeDeclaration() && !inPointcutDesignator) {
				    
				    // only do this if we are not adding ITDs
				    if (inAspectDeclaration && !insertIntertypeDeclarations) {
    					if (CharOperation.equals(percflow, name)) {
    						startPointcutDesignator();
    					} else if (CharOperation.equals(percflowbelow, name)) {
    						startPointcutDesignator();
    					} else if (CharOperation.equals(perthis, name)) {
    						startPointcutDesignator();
    					} else if (CharOperation.equals(pertarget, name)) {
    						startPointcutDesignator();
    					} else if (CharOperation.equals(issingleton, name)) {
    						startPointcutDesignator();
    					} else if (CharOperation.equals(pertypewithin, name)) {
    						startPointcutDesignator();
    					}
				    } 
				    // store the type name if not already found
				    if (currentTypeName == null) {
				        currentTypeName = name;
				    }
				}

				if (CharOperation.equals(throwing, name))
					consumeRetOrThrow();
				else if (CharOperation.equals(returning, name))
					consumeRetOrThrow();
				else if (inPointcutDesignator
						&& Character.isUpperCase(name[0])
						&& (content[scanner.getCurrentTokenStartPosition()-1]!='.')) {
					typeReferences.add(new String(name));
				}

				if (isSimulateContextSwitchNecessary) {
					usedIdentifiers.add(new String(name));
				}
				break;
			case TokenNamefor:
				insideFor=true;
				break;
            case TokenNameLPAREN:
                parenLevel++;
                break;
 			case TokenNameRPAREN:
				insideFor=false;
				parenLevel--;
				break;
			case TokenNameCOLON:
				if (!inAspect)
					break;
				if (insideFor)
					break;
				if (questionMarkCount > 0) {
					questionMarkCount--;
					break;
				}
				startPointcutDesignator();
				break;
				
			case TokenNameQUESTION:
				questionMarkCount++;
				break;
				
			case TokenNameSEMICOLON:
				if (inPointcutDesignator)
					endPointcutDesignator();
				break;
				
			    

			case TokenNameDOT:
				if (!inAspect) {
					break;
				} else if (inPointcutDesignator) {
					break;
				} else if (parenLevel > 0) {
				    // don't want to convert '.' to '$' inside the parameter declaration
				    break;
				}
				processPotentialIntertypeDeclaration();
				break;

			case TokenNameLBRACE:
				if (inPointcutDesignator) {
					endPointcutDesignator();
					//must be start of advice body -> insert tjp reference
					if (insertIntertypeDeclarations && !inTypeDeclaration()) {
						addReplacement(scanner.getCurrentTokenStartPosition() + 1, 0,
								tjpRefs2);
					}
				}
				
				// determine if we should add intertype declarations
				if (insertIntertypeDeclarations && inTypeDeclaration()) {
				    
				    char[] implementsExtends = createImplementExtendsITDs(currentTypeName);
				    if (implementsExtends != null) {
				        addReplacement(typeDeclStart, scanner.getCurrentTokenStartPosition()-typeDeclStart, implementsExtends);
				    } else {
				        // not able to add ITDs since no compilation unit available
				        // may have to replace "aspect" with "class " since we didn't do that before
				        
				        if (hasWordAtPosition("aspect", typeDeclStart)) {
		                    addReplacement(typeDeclStart, classs.length, classs);
				        }
				    }
				    char[] interTypeDecls = getInterTypeDecls(currentTypeName);
				    if (interTypeDecls.length > 0) {
				        addReplacement(scanner.getCurrentTokenStartPosition() + 1, 0, interTypeDecls);
				    }
				}
				
				
				inAspectDeclaration = false;
				inClassDeclaration = false;
				inInterfaceDeclaration = false;
				currentTypeName = null;
				
//				insideBlockCount ++;
				break;
			case TokenNameRBRACE:
//				insideBlockCount ++;
				if (inPointcutDesignator) {
					// bug 129367: if we've hit a } here, we must be
					// in the middle of an unterminated pointcut
					endPointcutDesignator();
				}				
				break;
			case TokenNameaspect:
				inAspect = true;
				inAspectDeclaration = true;
				typeDeclStart = pos = scanner.getCurrentTokenStartPosition();
				
				// only do this if not inserting ITDs
				// if inserting ITDs, this replacement occurs in createImplementExtendsITDs()
				if (!insertIntertypeDeclarations) {  
				    addReplacement(pos, classs.length, classs);
				}
				
				break;

			case TokenNameclass:
			    typeDeclStart = pos = scanner.getCurrentTokenStartPosition();
			    inClassDeclaration = true;
			    break;

			case TokenNameinterface:  // interface and @interface 
			    typeDeclStart = pos = scanner.getCurrentTokenStartPosition();
			    inInterfaceDeclaration = true;
			    break;
			    
			case TokenNameprivileged:
				pos = scanner.getCurrentTokenStartPosition();
				addReplacement(pos, privileged.length, privileged);
				break;

			}

		}

		if (inPointcutDesignator) {
			// bug 129367: if we've hit the end of the buffer, we must
			// be in the middle of an unterminated pointcut
			endPointcutDesignator();
		}

		if (addReferencesForOrganizeImports)
			addReferences();

		if (isSimulateContextSwitchNecessary)
			simulateContextSwitch(options.getCodeCompletePosition(), options
					.getTargetType());

		applyReplacements();

		//System.out.println(new String(content));
		return replacements;
	}
	
	/**
	 * 
	 * @param typeName name of the type
	 * @return new type declaration string to replace the original
	 * that contains all of the types that are declared parents of this one
	 * returns null if could not find the super types and super interfaces
	 */
	protected char[] createImplementExtendsITDs(char[] typeName) {

	    if (unit != null && typeName != null) {
	        IType type = unit.getType(new String(typeName));
	        if (type.exists()) {
        	    try {
        	        StringBuffer sb = new StringBuffer();
                    sb.append(type.isInterface() ? "interface " : "class ");
                    sb.append(typeName);
                    
                    List[] declares = getDeclareExtendsImplements(type);
                    List declareExtends = declares[0];
                    List declareImplements = declares[1];
                    if (! type.isInterface()) {
                        String superClass = type.getSuperclassName();
                        if (declareExtends.size() > 0) {
                            superClass = (String) declareExtends.get(0);
                            superClass = superClass.replace('$', '.');
                        }
                        if (superClass != null) {
                            sb.append(" " + EXTENDS + " " + superClass);
                        }
                    } 
        
                    String[] superInterfaces = type.getSuperInterfaceNames();
                    for (int i = 0; i < superInterfaces.length; i++) {
                        declareImplements.add(superInterfaces[i]);
                    }
                    
                    if (declareImplements.size() > 0) {
                        
                        if (type.isInterface()) {
                            sb.append(" " + EXTENDS + " ");
                        } else {
                            sb.append(" " + IMPLEMENTS +  " ");
                        }
                    
                        for (Iterator interfaceIter = declareImplements.iterator(); interfaceIter
                                .hasNext();) {
                            String interName = (String) interfaceIter.next();
                            interName = interName.replace('$', '.');
                            sb.append(" " + interName);
                            if (interfaceIter.hasNext()) {
                                sb.append(",");
                            }
                        }
                    }
                    return sb.toString().toCharArray();
                } catch (JavaModelException e) {
                    AspectJPlugin.getDefault().getLog().log(new Status(Status.ERROR, AspectJPlugin.PLUGIN_ID, e.getMessage(), e));
                    return null;
                }
    	    }
	    }
	    return null;
	}
	
	

	
	/**
	 * @param type type to look for declare extends on
	 * @return list of all declare extends that apply to this type
	 * in fully qualified strings
	 */
    protected List/*String*/[] getDeclareExtendsImplements(IType type) {
        List declareExtends = new ArrayList();
        List declareImplements = new ArrayList();
        if (type != null  && type.exists()) {
            AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(type);
            if (model.hasModel()) {
                List /*IJavaElement*/ rels = model.getRelationshipsForElement(type, AJRelationshipManager.ASPECT_DECLARATIONS);
                for (Iterator eltIter = rels.iterator(); eltIter.hasNext();) {
                    IJavaElement je = (IJavaElement) eltIter.next();
                    IProgramElement pe = model.javaElementToProgramElement(je);
                    if (pe.getKind() == IProgramElement.Kind.DECLARE_PARENTS) {
                        List/*String*/ parentTypes = pe.getParentTypes();
                        String details = pe.getDetails();
                        if (details.startsWith(EXTENDS)) {
                            declareExtends.addAll(parentTypes);
                        } else if (details.startsWith(IMPLEMENTS)) {
                            declareImplements.addAll(parentTypes);
                        }
                    }
                }
            }
        }
        return new List[] { declareExtends, declareImplements };
    }
	
    protected char[] getInterTypeDecls(char[] currentTypeName) {
        if (unit != null) {
            AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(unit);
            if (model.hasModel()) {
                IType type = unit.getType(new String(currentTypeName));
                if (type.exists()) {
                    List /*IJavaElement*/ rels = model.getRelationshipsForElement(type, AJRelationshipManager.ASPECT_DECLARATIONS);
                    StringBuffer sb = new StringBuffer("\n\t");
                    for (Iterator relIter = rels.iterator(); relIter.hasNext();) {
                        IJavaElement je = (IJavaElement) relIter.next();
                        IProgramElement declareElt = model.javaElementToProgramElement(je);
                        if (declareElt != null && declareElt.getParent() != null && declareElt.getKind().isInterTypeMember()) { // checks to see if this element is valid
                            // should be fully qualified type and simple name
                            
                            int lastDot = declareElt.getName().lastIndexOf('.');
                            String name = declareElt.getName().substring(lastDot+1);
    
                            if (declareElt.getKind() == IProgramElement.Kind.INTER_TYPE_FIELD) {
                                List modifiers = declareElt.getModifiers();
                                for (Iterator iterator = modifiers.iterator(); iterator
                                        .hasNext();) {
                                    sb.append(iterator.next() + " ");
                                }
                                sb.append(declareElt.getCorrespondingType(true) + " " + name + ";\n");
                            } else if (declareElt.getKind() == IProgramElement.Kind.INTER_TYPE_METHOD || 
                                       declareElt.getKind() == IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR) {
                                
                                sb.append(declareElt.getAccessibility() + " ");
                                List modifiers = declareElt.getModifiers();
                                for (Iterator iterator = modifiers.iterator(); iterator
                                        .hasNext();) {
                                    sb.append(iterator.next() + " ");
                                }
                                // need to add a return statement?
                                if (declareElt.getKind() == IProgramElement.Kind.INTER_TYPE_METHOD) {
                                    sb.append(declareElt.getCorrespondingType(true) + " " + name);
                                } else {
                                    sb.append(currentTypeName);
                                }
                                sb.append("(");
                                List/*String*/ names = declareElt.getParameterNames();
                                List/*String*/ types = declareElt.getParameterTypes();
                                if (types != null && names != null) {
                                    for (Iterator typeIter = types.iterator(), nameIter = names.iterator(); 
                                         typeIter.hasNext();) {
                                        String paramType = new String((char[]) typeIter.next());
                                        String paramName = (String) nameIter.next();
                                        sb.append(paramType + " " + paramName);
                                        if (typeIter.hasNext()) {
                                            sb.append(", ");
                                        }
                                    }
                                }
                                sb.append(") { }\n");
                            }
                        }
                    }
                    return sb.toString().toCharArray();
                }
            }
        }
        
        return new char[0];
    }

    private boolean inTypeDeclaration() {
        return (inAspectDeclaration || 
                 inClassDeclaration || 
                 inInterfaceDeclaration);
    }

	/**
	 * Inserts a reference to targetType at the given position. Thanks to this,
	 * we can simulate the context switch necessary in intertype method
	 * declarations.
	 * 
	 * Transformations: - Insertion of local variable 'TargetType thiz' (or, if
	 * thiz is already used, a number is added to thiz to make it unique) at
	 * start of method mody
	 *  - if code completion on code like 'this.methodcall().more...', 'this
	 * gets replaced by thiz
	 *  - if code completion on code like 'methodcall().more...', 'thiz.' gets
	 * added in front.
	 * 
	 * How the correct place for insertion is found: -
	 * 
	 *  
	 */
	private void simulateContextSwitch(int position, char[] targetType) {
		int pos = findInsertionPosition(position - 1) + 1;
		//if code completion on 'this' -> overwrite the this keyword
		int len = 0;
		boolean dotRequired = true;
		if ((content[pos] == 't') && (content[pos + 1] == 'h')
				&& (content[pos + 2] == 'i') && (content[pos + 3] == 's')
				&& !Character.isJavaIdentifierPart(content[pos + 4])) {
		    len = 4;
		    dotRequired = false;
		}

		String ident = findFreeIdentifier();
		char[] toInsert = (new String(targetType) + ' ' + ident + "; " + ident +
		        (dotRequired ? "." : "")).toCharArray();
		addReplacement(pos, len, toInsert);

	}

	/**
	 * @return An unused identifier
	 */
	private String findFreeIdentifier() {
		int i = 0;
		String ident = thizString + i;
		while (usedIdentifiers.contains(ident)) {
			i++;
			ident = thizString + i;
		}
		return ident;
	}

	/**
	 * @param pos -
	 *            a code position
	 * @return the position that defines the context of the current one at the
	 *         highest level
	 * 
	 * e.g. ' this.doSomthing().get' with pos on the last 't' returns the
	 * position of the char before the first 't'
	 */
	private int findInsertionPosition(int pos) {
		char ch = content[pos];
		int currentPos = pos;

		if (Character.isWhitespace(ch)) {
			currentPos = findPreviousNonSpace(pos);
			if (currentPos == -1)
				return pos;

			ch = content[currentPos];
			if (ch == '.')
				return findInsertionPosition(--currentPos);
			return pos;
		}

		if (Character.isJavaIdentifierPart(ch)) {
			while (Character.isJavaIdentifierPart(ch)) {
				currentPos--;
				ch = content[currentPos];
			}
			return findInsertionPosition(currentPos);
		}

		if (ch == '.') {
			return findInsertionPosition(--pos);
		}

		if (ch == ')') {
			currentPos--;
			int bracketCounter = 1;
			while (currentPos >= 0) {
				ch = content[currentPos];
				if (bracketCounter == 0)
					break;
				if (ch == ')')
					bracketCounter++;
				if (ch == '(') {
					bracketCounter--;
					if (bracketCounter < 0)
						return -1;
				}
				currentPos--;
			}
			return findInsertionPosition(currentPos);
		}

		return pos;
	}

	private void applyReplacements() {
		Iterator iter = replacements.listIterator();
		int offset = 0;
		while (iter.hasNext()) {
			Replacement ins = (Replacement) iter.next();
			ins.posAfter = ins.posBefore + offset;
			replace(ins.posAfter, ins.length, ins.text);
			offset += ins.lengthAdded;
		}
	}

	private void replace(int pos, int origLength, char[] text) {
		if (origLength != text.length) {
			int lengthDiff = text.length - origLength;
            int oldEnd = pos + origLength;
            int newEnd = pos + text.length;
			char[] temp = new char[content.length + lengthDiff];
			
			System.arraycopy(content, 0, temp, 0, pos);
			System.arraycopy(content, oldEnd, temp, newEnd, content.length
					- oldEnd);
			content = temp;
		}
		System.arraycopy(text, 0, content, pos, text.length);
	}

	private void startPointcutDesignator() {
		if (inPointcutDesignator)
			return;
		inPointcutDesignator = true;
		posColon = scanner.getCurrentTokenStartPosition();
	}

	/**
	 *  
	 */
	private void endPointcutDesignator() {
		inPointcutDesignator = false;
		int posSemi = scanner.getCurrentTokenStartPosition();
		int len = posSemi - posColon;
		char[] empty = new char[len];
		for (int i = 0; i < empty.length; i++) {
			empty[i] = ' ';
		}
		addReplacement(posColon, len, empty);
	}

	//identifies intertype declaration of form 'type qualifier.membername'
	//and replaces every '.' by '$'.
	//e.g. "int tracing.Circle.x;" -> "int tracing$Circle$x;"
	private void processPotentialIntertypeDeclaration() {

		//pos points to the '.'
		int pos = scanner.getCurrentTokenStartPosition();

		//check if valid identifier char on left side of dot
		//(to sort out construct like '(new Object()).' )
		int nonspace1 = findPreviousNonSpace(pos - 1);
		if (nonspace1 == -1)
			return;
		if (!Character.isJavaIdentifierPart(content[nonspace1]))
			return;

		//check if there is another java identifier before qualifier,
		//if no, return (to sort out method calls and the like)
		int space = findPreviousSpace(nonspace1);
		if (space == -1) {
			return;
		}
		int nonspace2 = findPreviousNonSpace(space);
		if (nonspace2 == -1) {
			return;
		}
		if (!Character.isJavaIdentifierPart(content[nonspace2]) &&
		        content[nonspace2] != '>') { // could be a parameterized type
		    // but still could be part of a "new" ITD
		    int nextNonSpace = findNextNonSpace(pos+1);
		    if (!hasWordAtPosition("new", nextNonSpace)) {
		        // XXX problem here is that this doesn't account for comments
		        return;
		    }
		}
		//check if rightmost part of qualifier starts with Capital letter,
		//and if yes, assume it is a Class name -> intertype declaration
		int spaceordot = findPreviousWhitespaceOr(',', nonspace1);
		if (spaceordot == -1)
			return;
		if (Character.isUpperCase(content[spaceordot + 1])) {

			//assume intertype declaration and replace all '.' by '$'
			char[] rep = new char[] { '$' };
			addReplacement(pos, 1, rep);

			if (content[spaceordot] == ' ') {
				String type = new String(content, space + 1, pos - space - 1);
				boolean validIdentifier = true;
				for (int i = 0; validIdentifier && (i < type.length()); i++) {
					char c = type.charAt(i);
					if (i==0) {
						if (!Character.isJavaIdentifierStart(c)) {
							validIdentifier = false;
						}
					} else if (!Character.isJavaIdentifierPart(c)) {
						validIdentifier = false;
					}
				}
				if (validIdentifier) {
					typeReferences.add(type);
				}
			} else {
				do {
					addReplacement(spaceordot, 1, rep);
					spaceordot = findPreviousWhitespaceOr(',', --spaceordot);
				} while (content[spaceordot] == '.');
			}

			//if requested, add ajc$ in front of intertype declaration
			//e.g. "public int Circle$x;" -> "public int ajc$Circle$x;"
			if (options.isAddAjcTagToIntertypesEnabled()) {
				addReplacement(spaceordot + 1, 0, "ajc$".toCharArray()); //$NON-NLS-1$
			}

		}
	}

	private boolean hasWordAtPosition(String string, int pos) {
	    char[] word = string.toCharArray();
	    for (int i = 0; i < word.length; i++) {
            if (word[i] != content[pos+i] ) {
                return false;
            }
        }
        return true;
    }


    public int findPrevious(char ch, int pos) {
		while (pos >= 0) {
			if (content[pos] == ch)
				return pos;
			pos--;
		}
		return -1;
	}

    public int findPreviousWhitespaceOr(char ch, int pos) {
        while (pos >= 0) {
            if (content[pos] == ch || Character.isWhitespace(content[pos])) {
                    return pos;
            }
            pos--;
        }
        return -1;
    }

    
    
	public int findPrevious(char[] chs, int pos) {
		while (pos >= 0) {
			for (int i = 0; i < chs.length; i++) {
				if (content[pos] == chs[i])
					return pos;
			}
			pos--;
		}
		return -1;
	}

	public int findPreviousSpace(int pos) {
		while (pos >= 0) {
			if (Character.isWhitespace(content[pos]))
				return pos;
			pos--;
		}
		return -1;
	}

	public int findPreviousNonSpace(int pos) {
		while (pos >= 0) {
			if (!Character.isWhitespace(content[pos]))
				return pos;
			pos--;
		}
		return -1;
	}

   public int findNextNonSpace(int pos) {
        while (pos < content.length) {
            if (!Character.isWhitespace(content[pos]))
                return pos;
            pos++;
        }
        return -1;
    }

	public int findNext(char[] chs, int pos) {
		while (pos < content.length) {
			for (int i = 0; i < chs.length; i++) {
				if (content[pos] == chs[i])
					return pos;
			}
			pos++;
		}
		return -1;
	}

	private void consumeRetOrThrow() {
		int pos = scanner.getCurrentTokenStartPosition();
		char[] content = scanner.source;

		int end = findNext(endThrow, pos);
		if (end == -1)
			return;

		char[] temp = null;
		if (content[end] == endThrow[0]) {
			pos = findPrevious(')', pos);
			if (pos == -1)
				return;
			int advicebracket = findPrevious('(', pos);
			if (advicebracket == -1)
				return;
			temp = new char[end - pos + 1];
			if (bracketsContainSomething(advicebracket)
					&& bracketsContainSomething(end))
				temp[0] = ',';
			else
				temp[0] = ' ';
			for (int i = 1; i < temp.length; i++) {
				temp[i] = ' ';
			}
		} else {
			temp = new char[end - pos];
			for (int i = 0; i < temp.length; i++) {
				temp[i] = ' ';
			}
		}
		addReplacement(pos, temp.length, temp);
	}

	/**
	 * @param end
	 * @return
	 */
	private boolean bracketsContainSomething(int start) {
		while (++start < content.length) {
			if (content[start] == ')')
				return false;
			if (Character.isJavaIdentifierPart(content[start]))
				return true;
		}
		return false;
	}

	private int findLast(char ch) {
		int pos = content.length;
		while (--pos >= 0) {
			if (content[pos] == ch)
				break;
		}
		return pos;
	}

	//adds references to all used type -> organize imports will work
	private void addReferences() {
		if (typeReferences == null)
			return;

		//char[] decl = new char[] { ' ', 'x', ';' };
		int pos = findLast('}');
		if (pos < 0)
			return;
		StringBuffer temp = new StringBuffer(typeReferences.size() * 10);
		Iterator iter = typeReferences.iterator();
		int varCount=1;
		while (iter.hasNext()) {
			String ref = (String) iter.next();
			temp.append(ref);
			temp.append(" x"); //$NON-NLS-1$
			temp.append(varCount++);
			temp.append(';');
		}
		char[] decls = new char[temp.length()];
		temp.getChars(0, decls.length, decls, 0);
		addReplacement(pos, 0, decls);
	}

	//adds a replacement to list
	//pre: list sorted, post: list sorted
	private void addReplacement(int pos, int length, char[] text) {
		int last = replacements.size() - 1;
		while (last >= 0) {
			if (((Replacement) replacements.get(last)).posBefore < pos)
				break;
			last--;
		}
		replacements.add(last + 1, new Replacement(pos, length, text));
	}

	public static boolean conflictsWithAJEdit(int offset, int length,
			ArrayList replacements) {
		Replacement ins;
		for (int i = 0; i < replacements.size(); i++) {
			ins = (Replacement) replacements.get(i);
			if ((offset >= ins.posAfter) && (offset < ins.posAfter + ins.length)) {
				return true;
			}
			if ((offset < ins.posAfter) && (offset + length > ins.posAfter)) {
				return true;
			}
		}
		return false;
	}
	
	//translates a position from after to before changes
	//if the char at that position did not exist before, it returns the
	// position before the inserted area
	public static int translatePositionToBeforeChanges(int posAfter,
			ArrayList replacements) {
		Replacement ins;
		int offset = 0, i;

		for (i = 0; i < replacements.size(); i++) {
			ins = (Replacement) replacements.get(i);
			if (ins.posAfter > posAfter)
				break;
			offset += ins.lengthAdded;
		}
		if (i > 0) {
			ins = (Replacement) replacements.get(i - 1);
			if (ins.posAfter + ins.text.length > posAfter) {
				//diff must be > 0
				int diff = posAfter - ins.posAfter;
				if (diff > ins.length)
					//we are in inserted area -> return pos directly before
					// that area
					offset += diff - ins.length;
			}
		}

		return posAfter - offset;
	}

	//translates a position from before to after changes
	public static int translatePositionToAfterChanges(int posBefore,
			ArrayList replacements) {
		for (int i = 0; i < replacements.size(); i++) {
			Replacement ins = (AspectsConvertingParser.Replacement) replacements
					.get(i);
			if (ins.posAfter <= posBefore)
				posBefore += ins.lengthAdded;
			else
				return posBefore;
		}
		return posBefore;
	}

}