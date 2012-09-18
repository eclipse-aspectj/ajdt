/*******************************************************************************
 * Copyright (c) 2007 Linton Ye.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Linton Ye - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.core.explain;

public enum ExplainMessage {
	MSG0 ("The [partType] of the given join point \"[partValue]\" cannot be matched by the pattern \"[pattern]\""),
	M_MSG0 (""),
	MSGArgs0 ("[args-type] doesn't match join points with [param-num] arguments"),
	MSGArgs1 ("[param-type] cannot be matched by pattern [args-type]."),
	M_MSGArgs0 ("[param-type] is a subtype of [args-type], and the runtime type of the argument will always be of type [args-type]."),
	M_MSGArgs1 ("[args-type] is a subtype of [param-type].  Runtime test is needed because the runtime type of the argument could, but not necessarily, be [args-type]."),
	MSGDT0 ("Calls to constructors of [dtp]'s subtypes (e.g. [dt]) won't be matched.  Use [dtp]+ to include calls to constructors of its subtypes."),
	MSGDT1 ("Constructors of [dtp]'s subtypes (e.g. [dt]) won't be matched.  Use [dtp]+ to include constructors of its subtypes."),
	MSGDT2 ("the declaring type of [private/static] methods has to be matched exactly by the pattern \"[dtp]\", i.e. methods with the same signatures in subtypes are not matched."),
	MSGDT3 ("method [methodSig] is not applicable to the type [dtp], i.e. it is declared in [dt] but not in [dtp] or any of [dtp]'s super types.  Use [dtp]+ to include all qualifying methods declared in its subtypes."),
	M_MSGDT0 ("Matched because: 1) The static target type of the call ([dt]) is a subtype of [dtp]; 2) method [methodSig] is [declared/inherited] in [dtp]"),
	MSGDT4 ("Call pointcut only matches against the static target type, but the static target type of this call ([dt]) is neither [dtp] nor subtype of [dtp].  Use [call(* set*(..))&&target(SubFoo)] to include the join point with the runtime target type being [dtp]."),
	MSGDT5 ("[dt] cannot be matched by pattern \"[dtp]\""),
	MSGDT6 ("A \"[dt]\" is not a \"[dtp]\". Use [execution(* set*(..))&&this(SubFoo)] to include the join point arising here with the runtime type being [dtp]."),
	MSGDT7 ("not match because field [fieldName] is re-declared in class [dt]. Use [dt] to pick this join point only, and [dtp]+ to pick fields declared in [dtp]'s subtypes ."),
	MSGParam0 ("Unlike args(...), the parameter pattern here only matches against the STATIC types of the join point's parameters, so the parameter types of this join point ([paramT]) cannot be matched by the pattern [paramPattern]"),
	M_MSGParam0 ("match because: [paramT] is a subtype of [paramPattern]"),
	MSGAnno0 ("Annotations are not inherited by default, though the method [methodName] is overriden in [dt]"),
	MSGThrow0 ("the method doesn't declare \"throws [throwsPattern]\".  By Java convention, a method doesn't need to explicitly declare unchecked exceptions, though any method could throw such exceptions."),
	MSGThis0 ("There is no \"this\" in a static context"),
	MSGTarget0 ("[Constructor calls] don't have \"target\". Use the advice after(...) returning(...) to capture the object being created."),
	MSGTarget1 ("[Calls to static methods] don't have \"target\"."),
	MSGTarget2 ("[MSG] The \"target\" could never be of type Foo"),
	M_MSGWithincode0 ("withincode matches method overrides, and [SubFoo.bar()] overrides method [Foo.bar()]."),
	MSGWithincode0 ("not matched because method bar() is declared in SubFoo but not in its super type Foo"),
	MSGWithin0 ("not matched because within only cares about static lexical scope.  Use Foo+ to include join points within subtypes of Foo."),
	MSGRef0 ("[refName] doesn't match this join point, see the definition of [refName] for detailed reason."),
	MSGHandler0 ("handler pointcut doesn't implicitly match subtypes.  To include this join point, use handler([etp]+) instead."),
	M_MSGHandler0 ("matched because SubE is a subtype of E");

	private String message = "";
	ExplainMessage(String msg) {
		message = msg;
	}
	public String getMessage() {
		String regex = "\\[[^\\[\\]]*\\]";
		return message.replaceAll(regex, "%s");
	}
}
