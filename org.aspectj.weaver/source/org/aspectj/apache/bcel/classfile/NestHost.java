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
 * https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html#jvms-4.7.28
 *
 * @see Attribute
 */
public final class NestHost extends Attribute {
	private int hostClassIndex;

	public NestHost(NestHost c) {
		this(c.getNameIndex(), c.getLength(), c.getHostClassIndex(), c.getConstantPool());
	}

	public NestHost(int nameIndex, int length, int hostClassIndex, ConstantPool cp) {
		super(Constants.ATTR_NEST_MEMBERS, nameIndex, length, cp);
		this.hostClassIndex = hostClassIndex;
	}

	NestHost(int nameIndex, int length, DataInputStream file, ConstantPool constant_pool) throws IOException {
		this(nameIndex, length, 0, constant_pool);
		hostClassIndex = file.readUnsignedShort();
	}

	@Override
	public void accept(ClassVisitor v) {
		v.visitNestHost(this);
	}

	@Override
	public final void dump(DataOutputStream file) throws IOException {
		super.dump(file);
		file.writeShort(hostClassIndex);
	}

	public final int getHostClassIndex() {
		return hostClassIndex;
	}

	public final void setHostClassIndex(int hostClassIndex) {
		this.hostClassIndex = hostClassIndex;
	}

	public final String getHostClassName() {
		ConstantClass constantClass = (ConstantClass)cpool.getConstant(hostClassIndex,Constants.CONSTANT_Class);
		return constantClass.getClassname(cpool);
	}

	@Override
	public final String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("NestHost(");
		ConstantClass constantClass = (ConstantClass)cpool.getConstant(hostClassIndex,Constants.CONSTANT_Class);
		buf.append(constantClass.getClassname(cpool));
		buf.append(")");
		return buf.toString();
	}
}
