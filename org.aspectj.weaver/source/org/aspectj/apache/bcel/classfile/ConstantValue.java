package org.aspectj.apache.bcel.classfile;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;

/**
 * This class is derived from <em>Attribute</em> and represents a constant value, i.e., a default value for initializing a class
 * field. This class is instantiated by the <em>Attribute.readAttribute()</em> method.
 *
 * @version $Id: ConstantValue.java,v 1.6 2009/09/16 00:43:49 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @see Attribute
 */
public final class ConstantValue extends Attribute {
	private int constantvalue_index;

	ConstantValue(int name_index, int length, DataInputStream file, ConstantPool constant_pool) throws IOException {
		this(name_index, length, file.readUnsignedShort(), constant_pool);
	}

	public ConstantValue(int name_index, int length, int constantvalue_index, ConstantPool constant_pool) {
		super(Constants.ATTR_CONSTANT_VALUE, name_index, length, constant_pool);
		this.constantvalue_index = constantvalue_index;
	}

	@Override
	public void accept(ClassVisitor v) {
		v.visitConstantValue(this);
	}

	@Override
	public final void dump(DataOutputStream file) throws IOException {
		super.dump(file);
		file.writeShort(constantvalue_index);
	}

	public final int getConstantValueIndex() {
		return constantvalue_index;
	}

	@Override
	public final String toString() {
		Constant c = cpool.getConstant(constantvalue_index);

		String buf;
		int i;

		// Print constant to string depending on its type
		switch (c.getTag()) {
		case Constants.CONSTANT_Long:
			buf = "" + ((ConstantLong) c).getValue();
			break;
		case Constants.CONSTANT_Float:
			buf = "" + ((ConstantFloat) c).getValue();
			break;
		case Constants.CONSTANT_Double:
			buf = "" + ((ConstantDouble) c).getValue();
			break;
		case Constants.CONSTANT_Integer:
			buf = "" + ((ConstantInteger) c).getValue();
			break;
		case Constants.CONSTANT_String:
			i = ((ConstantString) c).getStringIndex();
			c = cpool.getConstant(i, Constants.CONSTANT_Utf8);
			buf = "\"" + Utility.convertString(((ConstantUtf8) c).getValue()) + "\"";
			break;

		default:
			throw new IllegalStateException("Type of ConstValue invalid: " + c);
		}

		return buf;
	}
}
