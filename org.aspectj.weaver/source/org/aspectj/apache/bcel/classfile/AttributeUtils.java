package org.aspectj.apache.bcel.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;

public class AttributeUtils {

	public static Attribute[] readAttributes(DataInputStream dataInputstream, ConstantPool cpool) {
		try {
			int length = dataInputstream.readUnsignedShort();
			if (length == 0) {
				return Attribute.NoAttributes;
			}
			Attribute[] attrs = new Attribute[length];
			for (int i = 0; i < length; i++) {
				attrs[i] = Attribute.readAttribute(dataInputstream, cpool);
			}
			return attrs;
		} catch (IOException e) {
			throw new ClassFormatException("IOException whilst reading set of attributes: " + e.toString());
		}
	}

	/** Write (serialize) a set of attributes into a specified output stream */
	public static void writeAttributes(Attribute[] attributes, DataOutputStream file) throws IOException {
		if (attributes == null) {
			file.writeShort(0);
		} else {
			file.writeShort(attributes.length);
			for (Attribute attribute : attributes) {
				attribute.dump(file);
			}
		}
	}

	public static Signature getSignatureAttribute(Attribute[] attributes) {
		for (Attribute attribute : attributes) {
			if (attribute.tag == Constants.ATTR_SIGNATURE) {
				return (Signature) attribute;
			}
		}
		return null;
	}

	public static Code getCodeAttribute(Attribute[] attributes) {
		for (Attribute attribute : attributes) {
			if (attribute.tag == Constants.ATTR_CODE) {
				return (Code) attribute;
			}
		}
		return null;
	}

	public static ExceptionTable getExceptionTableAttribute(Attribute[] attributes) {
		for (Attribute attribute : attributes) {
			if (attribute.tag == Constants.ATTR_EXCEPTIONS) {
				return (ExceptionTable) attribute;
			}
		}
		return null;
	}

	public static ConstantValue getConstantValueAttribute(Attribute[] attributes) {
		for (Attribute attribute : attributes) {
			if (attribute.getTag() == Constants.ATTR_CONSTANT_VALUE) {
				return (ConstantValue) attribute;
			}
		}
		return null;
	}

	public static void accept(Attribute[] attributes, ClassVisitor visitor) {
		for (Attribute attribute : attributes) {
			attribute.accept(visitor);
		}
	}

	public static boolean hasSyntheticAttribute(Attribute[] attributes) {
		for (Attribute attribute : attributes) {
			if (attribute.tag == Constants.ATTR_SYNTHETIC) {
				return true;
			}
		}
		return false;
	}

	public static SourceFile getSourceFileAttribute(Attribute[] attributes) {
		for (Attribute attribute : attributes) {
			if (attribute.tag == Constants.ATTR_SOURCE_FILE) {
				return (SourceFile) attribute;
			}
		}
		return null;
	}

}
