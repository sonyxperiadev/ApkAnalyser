/*
 * Copyright (C) 2012 Sony Mobile Communications AB
 *
 * This file is part of ApkAnalyser.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mereflect;

import java.io.IOException;

import mereflect.info.AiConstantValue;
import mereflect.info.AttributeInfo;
import mereflect.info.CiDouble;
import mereflect.info.CiFloat;
import mereflect.info.CiInteger;
import mereflect.info.CiLong;
import mereflect.info.CiString;
import mereflect.info.CiUtf8;
import mereflect.info.ClassInfo;
import mereflect.io.DescriptorParser;

public class MEField
{
    public static final int ACC_PUBLIC = 0x0001;
    public static final int ACC_PRIVATE = 0x0002;
    public static final int ACC_PROTECTED = 0x0004;
    public static final int ACC_STATIC = 0x0008;
    public static final int ACC_FINAL = 0x0010;
    public static final int ACC_VOLATILE = 0x0040;
    public static final int ACC_TRANSIENT = 0x0080;

    protected int m_accFlags;
    protected int m_nameIndex;
    protected int m_descIndex;
    protected AttributeInfo[] m_attributes;
    protected MEClass m_class;
    protected Type m_type = null;

    public MEField(MEClass clazz)
    {
        m_class = clazz;
    }

    public MEClass getMEClass() {
        return m_class;
    }

    /**
     * Returns the name of this field
     * @return field name
     */
    public String getName()
    {
        return ((CiUtf8) m_class.getConstantPool()[getNameIndex()]).getUtf8();
    }

    /**
     * Returns constant pool descriptor of this field
     * @return constant pool descriptor
     */
    public String getDescriptor()
    {
        return ((CiUtf8) m_class.getConstantPool()[getDescriptorIndex()]).getUtf8();
    }

    /**
     * Returns the type that this field is declared as
     * @return the field type
     */
    public Type getType()
    {
        if (m_type == null)
        {
            try
            {
                m_type = DescriptorParser.processTypeDescriptor(m_class,
                        new StringBuffer(getDescriptor()));
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        return m_type;
    }

    /**
     * Returns true if this field is declared public
     * @return if field is public
     */
    public boolean isPublic()
    {
        return (m_accFlags & ACC_PUBLIC) > 0;
    }

    /**
     * Returns true if this field is declared protected
     * @return if field is protected
     */
    public boolean isProtected()
    {
        return (m_accFlags & ACC_PROTECTED) > 0;
    }

    /**
     * Returns true if this field is declared private
     * @return if field is private
     */
    public boolean isPrivate()
    {
        return (m_accFlags & ACC_PRIVATE) > 0;
    }

    /**
     * Returns true if this field is declared static
     * @return if field is static
     */
    public boolean isStatic()
    {
        return (m_accFlags & ACC_STATIC) > 0;
    }

    /**
     * Returns true if this field is declared final
     * @return if field is final
     */
    public boolean isFinal()
    {
        return (m_accFlags & ACC_FINAL) > 0;
    }

    /**
     * Returns true if this field is declared volatile
     * @return if field is volatile
     */
    public boolean isVolatile()
    {
        return (m_accFlags & ACC_VOLATILE) > 0;
    }

    /**
     * Returns true if this field is declared transient
     * @return if field is transient
     */
    public boolean isTransient()
    {
        return (m_accFlags & ACC_TRANSIENT) > 0;
    }

    /**
     * Returns the constant value declared by this field. Constant values
     * are only declared by final primitives and strings.
     * @return constant value or null if no constant value is declared
     */
    public Object getConstantValue()
    {
        Object res = null;
        AiConstantValue cval = null;
        AttributeInfo[] attrs = getAttributes();
        for (int i = 0; cval == null && i < attrs.length; i++)
        {
            if (attrs[i] instanceof AiConstantValue) {
                cval = (AiConstantValue) attrs[i];
            }
        }
        if (cval != null)
        {
            ClassInfo valInfo = m_class.getConstantPool()[cval.getConstantValueIndex()];
            Type t = getType();
            if (t.equals(Type.BOOLEAN)) {
                res = new Boolean(((CiInteger) valInfo).getInteger() > 0);
            } else if (t.equals(Type.BYTE)) {
                res = new Long((byte) ((CiInteger) valInfo).getInteger());
            } else if (t.equals(Type.CHAR)) {
                res = new Character((char) ((CiInteger) valInfo).getInteger());
            } else if (t.equals(Type.DOUBLE)) {
                res = new Double(((CiDouble) valInfo).getDouble());
            } else if (t.equals(Type.FLOAT)) {
                res = new Float(((CiFloat) valInfo).getFloat());
            } else if (t.equals(Type.INT)) {
                res = new Long(((CiInteger) valInfo).getInteger());
            } else if (t.equals(Type.LONG)) {
                res = new Long(((CiLong) valInfo).getLong());
            } else if (t.equals(Type.SHORT)) {
                res = new Long((short) ((CiInteger) valInfo).getInteger());
            } else if (t.getName().equals(Type.STR_STRING)) {
                res = ((CiUtf8) m_class.getConstantPool()[(((CiString) valInfo).getStringIndex())]).getUtf8();
            }
        }
        return res;
    }

    /**
     * Returns a string representation of the constantvalue declared by this field.
     * @return String representation of constant value or null
     *          if no constant value is declared
     */
    public String getConstantValueString()
    {
        Object cval = getConstantValue();
        if (cval != null)
        {
            StringBuffer sb = new StringBuffer();
            Type t = getType();
            boolean tFloat = t.equals(Type.FLOAT);
            boolean tLong = t.equals(Type.LONG);
            boolean tChar = t.equals(Type.CHAR) && Character.isLetterOrDigit(((Character) cval).charValue());
            boolean tCharSpec = t.equals(Type.CHAR) && !Character.isLetterOrDigit(((Character) cval).charValue());
            boolean tString = t.getName().equals(Type.STR_STRING);
            boolean tNumeric = t.equals(Type.INT) || t.equals(Type.LONG) || t.equals(Type.SHORT);
            if (tChar) {
                sb.append('\'');
            }
            if (tString) {
                sb.append('\"');
            }
            if (tNumeric && (((Long) cval).longValue() > 255))
            {
                sb.append("0x");
                sb.append(Long.toHexString(((Long) cval).longValue()));
            }
            else if (tCharSpec)
            {
                sb.append(Integer.toString(((Character) cval).charValue()));
            }
            else
            {
                sb.append(cval);
            }
            if (tFloat) {
                sb.append('f');
            }
            if (tLong) {
                sb.append('l');
            }
            if (tChar) {
                sb.append('\'');
            }
            if (tString) {
                sb.append('\"');
            }
            return sb.toString();
        }
        else
        {
            return null;
        }
    }

    // Getters

    /**
     * Returns accessflags as declared in constant pool
     * @return constant pool access flags
     */
    public int getAccessFlags()
    {
        return m_accFlags;
    }

    /**
     * Returns nameindex as declared in constant pool
     * @return constant pool nameindex
     */
    public int getNameIndex()
    {
        return m_nameIndex;
    }

    /**
     * Returns descriptor index as declared in constant pool
     * @return constant pool descriptor index
     */
    public int getDescriptorIndex()
    {
        return m_descIndex;
    }

    /**
     * Returns constant pool attributes belonging to this field
     * @return constant pool attributes
     */
    public AttributeInfo[] getAttributes()
    {
        return m_attributes;
    }

    // Setters

    public void setAccessFlags(int accFlags)
    {
        m_accFlags = accFlags;
    }

    public void setAttributes(AttributeInfo[] attributes)
    {
        m_attributes = attributes;
    }

    public void setDescriptorIndex(int descIndex)
    {
        m_descIndex = descIndex;
    }

    public void setNameIndex(int nameIndex)
    {
        m_nameIndex = nameIndex;
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        if (isPrivate()) {
            sb.append("private ");
        } else if (isProtected()) {
            sb.append("protected ");
        } else if (isPublic()) {
            sb.append("public ");
        }
        if (isStatic()) {
            sb.append("static ");
        }
        if (isFinal()) {
            sb.append("final ");
        }
        if (isVolatile()) {
            sb.append("volatile ");
        }
        if (isTransient()) {
            sb.append("transient ");
        }
        sb.append(getType());
        sb.append(' ');
        sb.append(getName());
        String cval = getConstantValueString();
        if (cval != null)
        {
            sb.append(" = ");
            sb.append(cval);
        }
        sb.append(';');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        //return getName().hashCode();
        int hashCode = m_class.getName().hashCode();
        hashCode = 31 * hashCode + getName().hashCode();
        hashCode = 31 * hashCode + getDescriptor().hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof MEField) {
            MEField m = (MEField) o;
            boolean match = m.getMEClass().equals(getMEClass())
                    && m.getName().equals(getName())
                    && m.getDescriptor().equals(getDescriptor());
            return match;
        } else {
            return false;
        }
    }
}
