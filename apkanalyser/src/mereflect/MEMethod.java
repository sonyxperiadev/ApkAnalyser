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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mereflect.bytecode.Bytecode;
import mereflect.bytecode.Bytecodes;
import mereflect.info.AbstractRefInfo;
import mereflect.info.AiCode;
import mereflect.info.AiExceptions;
import mereflect.info.AiLineNumberTable;
import mereflect.info.AttributeInfo;
import mereflect.info.CiClass;
import mereflect.info.CiNameAndType;
import mereflect.info.CiUtf8;
import mereflect.info.ClassInfo;
import mereflect.io.DescriptorParser;

import org.jf.dexlib.Code.Instruction;

import andreflect.Util;

public class MEMethod {
    public static final int ACC_PUBLIC = 0x0001;
    public static final int ACC_PRIVATE = 0x0002;
    public static final int ACC_PROTECTED = 0x0004;
    public static final int ACC_STATIC = 0x0008;
    public static final int ACC_FINAL = 0x0010;
    public static final int ACC_SYNCHRONIZED = 0x0020;
    public static final int ACC_NATIVE = 0x0100;
    public static final int ACC_ABSTRACT = 0x0400;
    public static final int ACC_STRICT = 0x0800;

    public static final String CTOR_METHOD_NAME = "<init>";

    protected MEClass m_class;
    protected int m_accFlags;
    protected int m_nameIndex;
    protected int m_descIndex;
    protected AttributeInfo[] m_attributes;
    protected Type m_returnType = null;
    protected Type[] m_arguments = null;
    protected MEClass[] m_exceptions = null;
    protected AiLineNumberTable m_lineNumberTable = null;

    public MEMethod(MEClass clazz) {
        m_class = clazz;
    }

    // Logic
    public boolean isPublic() {
        return (m_accFlags & ACC_PUBLIC) > 0;
    }

    public boolean isPrivate() {
        return (m_accFlags & ACC_PRIVATE) > 0;
    }

    public boolean isProtected() {
        return (m_accFlags & ACC_PROTECTED) > 0;
    }

    public boolean isStatic() {
        return (m_accFlags & ACC_STATIC) > 0;
    }

    public boolean isFinal() {
        return (m_accFlags & ACC_FINAL) > 0;
    }

    public boolean isSynchronized() {
        return (m_accFlags & ACC_SYNCHRONIZED) > 0;
    }

    public boolean isNative() {
        return (m_accFlags & ACC_NATIVE) > 0;
    }

    public boolean isAbstract() {
        return (m_accFlags & ACC_ABSTRACT) > 0;
    }

    public boolean isStrict() {
        return (m_accFlags & ACC_STRICT) > 0;
    }

    public int getFlags() {
        return m_accFlags;
    }

    /**
     * Returns method name from constant pool.
     * 
     * @return name from constant pool.
     */
    public String getName() {
        return ((CiUtf8) m_class.getConstantPool()[getNameIndex()]).getUtf8();
    }

    public boolean isConstructor() {
        return getName().equals(CTOR_METHOD_NAME);
    }

    /**
     * Returns a formatted name for this method, e.g. classname for constructor
     * etc.
     * 
     * @return a formatted name for this method.
     */
    public String getFormattedName() {
        String methodStr = getName();

        if (isConstructor()) {
            methodStr = getMEClass().getClassName();
        }

        return methodStr;
    }

    /**
     * Returns method descriptor from constant pool.
     * 
     * @return method descriptor from constant pool.
     */
    public String getDescriptor() {
        return ((CiUtf8) m_class.getConstantPool()[getDescriptorIndex()]).getUtf8();
    }

    /**
     * Returns type of return value for this method.
     * 
     * @return type of return value.
     * @throws IOException
     */
    public Type getReturnClass() throws IOException {
        if (m_returnType == null) {
            m_returnType = getReturnClass(new StringBuffer(getDescriptor()));
        }
        return m_returnType;
    }

    public Type getReturnClass(StringBuffer descriptor) throws IOException {
        descriptor.delete(0, descriptor.indexOf(")") + 1);
        return DescriptorParser.processTypeDescriptor(m_class, descriptor);
    }

    /**
     * Returns a type array of this method's arguments.
     * 
     * @return a type array of arguments.
     * @throws IOException
     */
    public Type[] getArguments() throws IOException {
        if (m_arguments == null) {
            m_arguments = getArguments(new StringBuffer(getDescriptor()));
        }
        return m_arguments;
    }

    public Type[] getArguments(StringBuffer sb) throws IOException {
        List<Type> res = new ArrayList<Type>();
        sb.deleteCharAt(0);
        sb.delete(sb.indexOf(")"), sb.length());
        Type t = null;
        do {
            t = DescriptorParser.processTypeDescriptor(m_class, sb);
            if (t != null) {
                res.add(t);
            }
        } while (t != null);
        return res.toArray(new Type[res.size()]);
    }

    /**
     * Returns an exception array of declared exceptions of this method.
     * 
     * @return an exception array of declared exceptions.
     * @throws IOException
     */
    public MEClass[] getExceptions() throws IOException {
        if (m_exceptions == null) {
            AiExceptions excs = null;
            for (int i = 0; i < m_attributes.length; i++) {
                if (m_attributes[i] instanceof AiExceptions) {
                    excs = (AiExceptions) m_attributes[i];
                    break;
                }
            }
            if (excs != null) {
                int[] exIndices = excs.getExceptionIndices();
                m_exceptions = new MEClass[exIndices.length];
                for (int i = 0; i < exIndices.length; i++) {
                    CiClass classInfo = (CiClass) m_class.getConstantPool()[exIndices[i]];
                    String classname = ((CiUtf8) m_class.getConstantPool()[classInfo
                            .getNameIndex()]).getUtf8();
                    classname = classname.replace('/', '.');
                    try {
                        m_exceptions[i] = m_class.getResource().getContext().getMEClass(
                                classname);
                    } catch (ClassNotFoundException cnfe) {
                        m_exceptions[i] = new UnknownClass(classname, m_class.getResource());
                    }
                }
            } else {
                m_exceptions = new MEClass[0];
            }
        }
        return m_exceptions;
    }

    public byte[] getByteCodes() {
        AiCode code = (AiCode) getAttributeInfo(m_attributes, AiCode.class);
        if (code == null) {
            return null;
        } else {
            return code.getCode();
        }
    }

    public AiLineNumberTable getLineNumberTable() {
        if (m_lineNumberTable == null) {
            AiCode code = (AiCode) getAttributeInfo(m_attributes, AiCode.class);
            if (code != null) {
                AiLineNumberTable table = (AiLineNumberTable) getAttributeInfo(code
                        .getAttributes(), AiLineNumberTable.class);
                if (table != null) {
                    m_lineNumberTable = table;
                }
            }
        }
        return m_lineNumberTable;
    }

    /**
     * Returns a List of the invokations called from within this method. The list contains
     * a formatted representation of the invokation MEMethod.Invokation.
     * 
     * @return a list of the invokations called from within this method.
     * @throws CorruptBytecodeException
     */
    public List<MEMethod.Invokation> getInvokations() throws CorruptBytecodeException {
        final List<MEMethod.Invokation> res = new ArrayList<MEMethod.Invokation>();
        final ClassInfo[] cp = m_class.getConstantPool();
        traverseBytecodes(new BytecodeVisitor() {
            int index = 0;

            @Override
            public void visitInvokation(int pc, int bytecode, int len, int cpIndex) throws CorruptBytecodeException {
                if (cpIndex > cp.length) {
                    throw new CorruptBytecodeException(pc, len, bytecode);
                }
                if (cp[cpIndex] instanceof AbstractRefInfo) // safeguard for evil obfuscators
                {
                    AbstractRefInfo methodRef = (AbstractRefInfo) cp[cpIndex];
                    if (methodRef == null) {
                        throw new CorruptBytecodeException(pc, len, bytecode);
                    }
                    CiClass ciClass = (CiClass) cp[methodRef.getClassIndex()];
                    String classname = ((CiUtf8) cp[ciClass.getNameIndex()]).getUtf8().replace('/', '.');
                    CiNameAndType ciNameType = (CiNameAndType) cp[methodRef.getNameAndTypeIndex()];
                    String methname = ((CiUtf8) cp[ciNameType.getNameIndex()]).getUtf8();
                    String descriptor = ((CiUtf8) cp[ciNameType.getDescriptorIndex()]).getUtf8();
                    boolean isVirtual = bytecode == 182; //invokevirtual
                    boolean isInterface = bytecode == 185; //invokeinterface
                    MEMethod.Invokation inv = new MEMethod.Invokation(classname, methname, descriptor, pc, index, isVirtual, isInterface, null);
                    //System.out.println("[MEMethod] invoke:" + classname + "->" +methname +"->" +descriptor);
                    res.add(inv);
                }
                index++;
            }

            @Override
            public void visitNewBytecode(int pc, int bytecode) {
            }

            @Override
            public void visit(int pc, int bytecode, int len) {
                index++;
            }

            @Override
            public void visitConstantPool(int pc, int bytecode, int len, int cpIndex) {
                index++;
            }

            @Override
            public void visitJump(int pc, int bytecode, int len, short relJump) {
                index++;
            }

            @Override
            public void visitLocalFieldName(int pc, int bytecode, int len, int cpIndex) {
                index++;
            }

            @Override
            public void visitLookupSwitch(int pc, int bytecode, int len, Map<Object, Object> switch1) {
                index++;
            }

            @Override
            public void visitTableSwitch(int pc, int bytecode, int len, Map<Object, Object> switch1) {
                index++;
            }
        });
        return res;
    }

    /**
     * Returns a list integers of pc-offsets where specified bytecode is defined.
     * 
     * @return a list of pc's where specified bytecode is defined.
     * @throws CorruptBytecodeException
     */
    public List<Integer> getBytecode(final int bytecodeSearch) throws CorruptBytecodeException {
        final List<Integer> res = new ArrayList<Integer>();
        traverseBytecodes(new SimpleVisitor() {
            @Override
            public void visitNewBytecode(int pc, int bytecode) {
                if (bytecode == bytecodeSearch) {
                    res.add(new Integer(pc));
                }
            }
        });
        return res;
    }

    public int countBytecodeBytes() {
        return getByteCodes() != null ? getByteCodes().length : 0;
    }

    public void traverseBytecodes(BytecodeVisitor bv) throws CorruptBytecodeException {
        byte[] code = getByteCodes();
        int pc = 0;
        if (code != null) {
            while (pc < code.length) {
                int bytecode = (code[pc] & 0xff);
                bv.visitNewBytecode(pc, bytecode);

                int len = Bytecodes.BC_LENGTHS[bytecode];
                if (bytecode == 170) // tableswitch
                {
                    try {
                        Map<Object, Object> tSwitch = Bytecode.getTableSwitch(code, pc);
                        len = ((Integer) tSwitch.get(Bytecode.SWITCH_OP_LENGTH)).intValue();
                        bv.visitTableSwitch(pc, bytecode, len, tSwitch);
                    } catch (Exception e) {
                        throw new CorruptBytecodeException();
                    }
                } else if (bytecode == 171) // lookupswitch
                {
                    try {
                        Map<Object, Object> lSwitch = Bytecode.getLookupSwitch(code, pc);
                        len = ((Integer) lSwitch.get(Bytecode.SWITCH_OP_LENGTH)).intValue();
                        bv.visitLookupSwitch(pc, bytecode, len, lSwitch);
                    } catch (Exception e) {
                        throw new CorruptBytecodeException();
                    }
                } else if (len > 1) {
                    // invokation name lookup
                    if (bytecode == 182 || // invokevirtual
                            bytecode == 183 || // invokespecial
                            bytecode == 184 || // invokestatic
                            bytecode == 185) // invokeinterface
                    {
                        int cpIndex = ((code[pc + 1] & 0xff) << 8)
                                | ((code[pc + 2]) & 0xff);
                        bv.visitInvokation(pc, bytecode, len, cpIndex);
                    }
                    // local field name lookup
                    else if (bytecode == 178 // getstatic
                            || bytecode == 179 // putstatic
                            /*|| (pc > 0 && code[pc - 1] == 42 // aload_0 (this)
                            &&*/|| (bytecode == 180 // getfield
                            || bytecode == 181)) // putfield
                    {
                        int cpIndex = ((code[pc + 1] & 0xff) << 8)
                                | ((code[pc + 2]) & 0xff);
                        bv.visitLocalFieldName(pc, bytecode, len, cpIndex);
                    }
                    // constant pool lookup
                    else if (bytecode == 18 || bytecode == 19 || bytecode == 20) // ldc, ldc_w (integer, float or string), ldc2_w
                    {
                        int cpIndex =
                                (bytecode == 19 || bytecode == 20) ? ((code[pc + 1] & 0xff) << 8) | ((code[pc + 2]) & 0xff) : (code[pc + 1] & 0xff);
                        bv.visitConstantPool(pc, bytecode, len, cpIndex);
                    }
                    // label registration
                    else if (bytecode == 167 || (bytecode >= 153 && bytecode <= 166) || bytecode == 198 || bytecode == 199) // goto, if*
                    {
                        short relJump = (short) (((code[pc + 1] & 0xff) << 8) | ((code[pc + 2]) & 0xff));
                        bv.visitJump(pc, bytecode, len, relJump);
                    } else
                    // unparsed data
                    {
                        bv.visit(pc, bytecode, len);
                    }
                }

                if (len < 0 || pc + len > code.length) {
                    throw new CorruptBytecodeException(pc, len, bytecode);
                }
                pc += len;
            }
        }
    }

    // Getters

    public int getAccessFlags() {
        return m_accFlags;
    }

    public int getNameIndex() {
        return m_nameIndex;
    }

    public int getDescriptorIndex() {
        return m_descIndex;
    }

    public AttributeInfo[] getAttributes() {
        return m_attributes;
    }

    public MEClass getMEClass() {
        return m_class;
    }

    // Setters

    public void setAccessFlags(int accFlags) {
        m_accFlags = accFlags;
    }

    public void setAttributes(AttributeInfo[] attributes) {
        m_attributes = attributes;
    }

    public void setDescriptorIndex(int descIndex) {
        m_descIndex = descIndex;
    }

    public void setNameIndex(int nameIndex) {
        m_nameIndex = nameIndex;
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
        if (o != null && o instanceof MEMethod) {
            MEMethod m = (MEMethod) o;
            boolean match = m.getMEClass().equals(getMEClass())
                    && m.getName().equals(getName());
            if (match) {
                try {
                    Type[] ma = m.getArguments();
                    Type[] a = getArguments();
                    match = ma.length == a.length;
                    for (int i = 0; match && i < ma.length; i++) {
                        match = ma[i].equals(a[i]);
                    }
                    if (match) {
                        match = getReturnClass().equals(m.getReturnClass());
                    }
                } catch (IOException e) {
                    return false;
                }
            }
            return match;
        } else {
            return false;
        }
    }

    public String getArgumentsStringUml() {
        try {
            Type[] args = getArguments();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < args.length; i++) {
                sb.append(Util.shortenClassName(args[i].toString()));
                if (i < args.length - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();

        } catch (Exception e) {
            return "?";
        }
    }

    public String getArgumentsString() {
        try {
            return getArgumentsString(getArguments());
        } catch (Exception e) {
            return "?";
        }
    }

    public String getArgumentsString(Type[] args) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i < args.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String getExceptionsString() {
        StringBuffer sb = new StringBuffer();
        try {
            Type[] ex = getExceptions();
            for (int i = 0; i < ex.length; i++) {
                sb.append(ex[i]);
                if (i < ex.length - 1) {
                    sb.append(", ");
                }
            }
        } catch (Exception e) {
            sb.append("?");
        }
        return sb.toString();

    }

    public boolean declaresExceptions() {
        try {
            return getExceptions().length > 0;
        } catch (IOException e) {
            return false;
        }
    }

    public String getReturnClassString() {
        try {
            return getReturnClass().toString();
        } catch (Exception e) {
            return "?";
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (isStatic()) {
            sb.append("static ");
        }
        if (isSynchronized()) {
            sb.append("synchronized ");
        }
        if (isNative()) {
            sb.append("native ");
        }
        if (isStrict()) {
            sb.append("strict ");
        }
        if (isPrivate()) {
            sb.append("private ");
        } else if (isProtected()) {
            sb.append("protected ");
        } else if (isPublic()) {
            sb.append("public ");
        }
        if (isFinal()) {
            sb.append("final ");
        }
        if (isAbstract()) {
            sb.append("abstract ");
        }
        if (!isConstructor()) {
            sb.append(getReturnClassString());
            sb.append(' ');
        }
        sb.append(getFormattedName());
        sb.append('(');
        sb.append(getArgumentsString());
        sb.append(")");
        if (declaresExceptions()) {
            sb.append(" throws ");
            sb.append(getExceptionsString());
        }

        return sb.toString();
    }

    public AttributeInfo getAttributeInfo(AttributeInfo[] infos, Class<?> infoType) {
        AttributeInfo res = null;
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].getClass().equals(infoType)) {
                res = infos[i];
                break;
            }
        }
        return res;
    }

    public static class Invokation {
        public String invClassname;
        public String invMethodname;
        public String invDescriptor;
        public int bytecodePc;
        public int bytecodeIndex;
        public boolean isVirtual;
        public boolean isInterface;
        public Instruction offsetIns;

        public MEMethod method;
        public MEClass clazz;

        public Invokation(String classname, String methodName, String descriptor, int pc, int index,
                boolean isVirtual, boolean isInterface, Instruction offsetIns) {
            invClassname = classname;
            invMethodname = methodName;
            invDescriptor = descriptor;
            bytecodePc = pc;
            bytecodeIndex = index;
            this.isVirtual = isVirtual;
            this.isInterface = isInterface;
            this.offsetIns = offsetIns;
            method = null;
            clazz = null;
        }

        public void setMethod(MEMethod method, MEClass clazz) {
            this.method = method;
            this.clazz = clazz;
        }

        @Override
        public String toString() {
            return invClassname + "." + invMethodname + ":" + invDescriptor;
        }

        @Override
        public int hashCode() {
            return (invClassname.hashCode() & 0x0000ffff) +
                    ((invMethodname.hashCode() + invDescriptor.hashCode()) & 0xffff0000);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof MEMethod.Invokation) {
                MEMethod.Invokation inv = (MEMethod.Invokation) o;
                return inv.invClassname.equals(invClassname) &&
                        inv.invMethodname.equals(invMethodname) &&
                        inv.invDescriptor.equals(invDescriptor) &&
                        inv.bytecodePc == bytecodePc;
            }
            return false;
        }

    }

}
