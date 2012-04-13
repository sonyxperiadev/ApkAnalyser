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

package mereflect.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import mereflect.MEClass;
import mereflect.MEField;
import mereflect.MEMethod;
import mereflect.info.AiCode;
import mereflect.info.AiConstantValue;
import mereflect.info.AiExceptions;
import mereflect.info.AiInnerClasses;
import mereflect.info.AiLineNumberTable;
import mereflect.info.AiSourceFile;
import mereflect.info.AttributeInfo;
import mereflect.info.CiClass;
import mereflect.info.CiDouble;
import mereflect.info.CiFieldRef;
import mereflect.info.CiFloat;
import mereflect.info.CiInteger;
import mereflect.info.CiInterfaceMethodRef;
import mereflect.info.CiLong;
import mereflect.info.CiMethodRef;
import mereflect.info.CiNameAndType;
import mereflect.info.CiString;
import mereflect.info.CiUtf8;
import mereflect.info.ClassInfo;

public class DefaultClassReader implements ClassReader
{
    @Override
    public MEClass readClassFile(DataInputStream dis) throws IOException
    {
        MEClass c = new MEClass();

        int magic = dis.readInt();
        if (magic != 0xcafebabe) {
            throw new IOException("Not a class file");
        }

        // Version
        int majMin = dis.readInt();
        c.setMajorVersion((majMin & 0xff00) >> 8);
        c.setMinorVersion(majMin & 0xff);

        // Constant pool
        boolean corruptPool = false;
        int prereadByte = 0;
        int cpLen = dis.readUnsignedShort();
        ClassInfo[] infos = new ClassInfo[cpLen];
        for (int i = 1; i < cpLen; i++)
        {
            try
            {
                infos[i] = readClassInfo(c, dis);
                if (infos[i].getTag() == ClassInfo.CONSTANT_Long ||
                        infos[i].getTag() == ClassInfo.CONSTANT_Double)
                {
                    i++;
                }
            } catch (MissingClassInfoException mcie)
            {
                corruptPool = true;
                prereadByte = mcie.getTag();
                break;
            }
        }
        c.setConstantPool(infos);

        // Class data
        int accessFlags = 0;
        if (corruptPool) {
            accessFlags = (prereadByte << 8) | dis.readUnsignedByte(); // Pool corrupt, we've already read a zero byte
        } else {
            accessFlags = dis.readUnsignedShort();
        }

        int thisClass = dis.readUnsignedShort();
        int superClass = dis.readUnsignedShort();
        c.setAccessFlags(accessFlags);
        c.setThisClassIndex(thisClass);
        c.setSuperClassIndex(superClass);

        // Interfaces
        int ifcLen = dis.readUnsignedShort();
        int[] ifcs = new int[ifcLen];
        for (int i = 0; i < ifcLen; i++)
        {
            ifcs[i] = dis.readUnsignedShort();
        }
        c.setInterfaceIndices(ifcs);

        // Fields
        int fLen = dis.readUnsignedShort();
        MEField[] fields = new MEField[fLen];
        for (int i = 0; i < fLen; i++)
        {
            fields[i] = readFieldInfo(c, dis);
        }
        c.setFields(fields);

        // Methods
        int mLen = dis.readUnsignedShort();
        MEMethod[] methods = new MEMethod[mLen];
        for (int i = 0; i < mLen; i++)
        {
            methods[i] = readMethodInfo(c, dis);
        }
        c.setMethods(methods);

        // Attributes
        AttributeInfo[] attrs = readAttributeInfos(c, dis);
        c.setAttributes(attrs);
        //System.out.println("[ClassParser] class:"+c.getName());
        return c;
    }

    protected ClassInfo readClassInfo(MEClass c, DataInputStream dis) throws IOException
    {
        ClassInfo res = null;
        int tag = dis.readUnsignedByte();
        if (tag == 0 || tag == 2)
        {
            throw new MissingClassInfoException(tag);
        }
        else
        {
            switch (tag)
            {
            case ClassInfo.CONSTANT_Class:
                int nameIndex = dis.readUnsignedShort();
                res = new CiClass(nameIndex);
                break;
            case ClassInfo.CONSTANT_Fieldref:
                int classIndex = dis.readUnsignedShort();
                int nameAndTypeIndex = dis.readUnsignedShort();
                res = new CiFieldRef(classIndex, nameAndTypeIndex);
                break;
            case ClassInfo.CONSTANT_Methodref:
                classIndex = dis.readUnsignedShort();
                nameAndTypeIndex = dis.readUnsignedShort();
                res = new CiMethodRef(classIndex, nameAndTypeIndex);
                break;
            case ClassInfo.CONSTANT_InterfaceMethodref:
                classIndex = dis.readUnsignedShort();
                nameAndTypeIndex = dis.readUnsignedShort();
                res = new CiInterfaceMethodRef(classIndex, nameAndTypeIndex);
                break;
            case ClassInfo.CONSTANT_String:
                int stringIndex = dis.readUnsignedShort();
                res = new CiString(stringIndex);
                break;
            case ClassInfo.CONSTANT_Integer:
                int i = dis.readInt();
                res = new CiInteger(i);
                break;
            case ClassInfo.CONSTANT_Float:
                float f = dis.readFloat();
                res = new CiFloat(f);
                break;
            case ClassInfo.CONSTANT_Long:
                long l = dis.readLong();
                res = new CiLong(l);
                break;
            case ClassInfo.CONSTANT_Double:
                double d = dis.readDouble();
                res = new CiDouble(d);
                break;
            case ClassInfo.CONSTANT_NameAndType:
                nameIndex = dis.readUnsignedShort();
                int descIndex = dis.readUnsignedShort();
                res = new CiNameAndType(nameIndex, descIndex);
                break;
            case ClassInfo.CONSTANT_Utf8:
                String utf8 = dis.readUTF();
                res = new CiUtf8(utf8);
                break;
            default:
                throw new ClassReaderException("Unknown class info tag [" + tag + "]");
            }
        }
        return res;
    }

    protected MEMethod readMethodInfo(MEClass c, DataInputStream dis) throws IOException
    {
        MEMethod res = null;

        int accessFlags = dis.readUnsignedShort();
        int nameIndex = dis.readUnsignedShort();
        int descIndex = dis.readUnsignedShort();
        AttributeInfo[] attrs = readAttributeInfos(c, dis);
        res = new MEMethod(c);
        res.setAccessFlags(accessFlags);
        res.setNameIndex(nameIndex);
        res.setDescriptorIndex(descIndex);
        res.setAttributes(attrs);
        //System.out.println("[ClassParser] method:"+res.getName());
        return res;
    }

    protected MEField readFieldInfo(MEClass c, DataInputStream dis) throws IOException
    {
        MEField res = null;

        int accessFlags = dis.readUnsignedShort();
        int nameIndex = dis.readUnsignedShort();
        int descIndex = dis.readUnsignedShort();
        AttributeInfo[] attrs = readAttributeInfos(c, dis);
        res = new MEField(c);
        res.setAccessFlags(accessFlags);
        res.setNameIndex(nameIndex);
        res.setDescriptorIndex(descIndex);
        res.setAttributes(attrs);
        //System.out.println("[ClassParser] field:"+res.getName());
        return res;
    }

    protected AttributeInfo[] readAttributeInfos(MEClass c, DataInputStream dis) throws IOException
    {
        int attrLen = dis.readUnsignedShort();
        AttributeInfo[] attrs = new AttributeInfo[attrLen];
        for (int i = 0; i < attrLen; i++)
        {
            attrs[i] = readAttributeInfo(c, dis);
        }
        return attrs;
    }

    protected AttributeInfo readAttributeInfo(MEClass c, DataInputStream dis) throws IOException
    {
        AttributeInfo res = null;

        int nameIndex = dis.readUnsignedShort();
        long length = readUnsigned4(dis);
        long read = 0;

        boolean def = true;

        if (nameIndex <= 0 || nameIndex >= c.getConstantPool().length)
        {
            throw new ClassReaderException("Attribute name index out of range [0 <= " +
                    nameIndex + " <= " + c.getConstantPool().length + "]");
        }
        ClassInfo ci = c.getConstantPool()[nameIndex];
        if (ci != null && ci.getTag() == ClassInfo.CONSTANT_Utf8)
        {
            String name = ((CiUtf8) ci).getUtf8();
            if (name.equals(AttributeInfo.EXCEPTIONS))
            {
                res = new AiExceptions();
                int exLen = dis.readUnsignedShort();
                read += 2;
                int[] exs = new int[exLen];
                for (int i = 0; i < exLen; i++)
                {
                    exs[i] = dis.readUnsignedShort();
                    read += 2;
                }
                ((AiExceptions) res).setExceptionIndices(exs);
                def = false;
            }
            else if (name.equals(AttributeInfo.CONSTANT_VALUE))
            {
                res = new AiConstantValue();
                int cvIndex = dis.readUnsignedShort();
                read += 2;
                ((AiConstantValue) res).setConstantValueIndex(cvIndex);
                def = false;
            }
            else if (name.equals(AttributeInfo.SOURCE_FILE))
            {
                res = new AiSourceFile();
                int srcFileIndex = dis.readUnsignedShort();
                read += 2;
                ((AiSourceFile) res).setSourceFileIndex(srcFileIndex);
                def = false;
            }
            else if (name.equals(AttributeInfo.INNER_CLASSES))
            {
                res = new AiInnerClasses();
                int icLen = dis.readUnsignedShort();
                read += 2;
                AiInnerClasses.InnerClass[] inner = new AiInnerClasses.InnerClass[icLen];
                for (int i = 0; i < icLen; i++)
                {
                    int iciIdx = dis.readUnsignedShort();
                    read += 2;
                    int ociIdx = dis.readUnsignedShort();
                    read += 2;
                    int inIdx = dis.readUnsignedShort();
                    read += 2;
                    int accessFlags = dis.readUnsignedShort();
                    read += 2;
                    inner[i] = new AiInnerClasses.InnerClass();
                    inner[i].setInnerClassInfoIndex(iciIdx);
                    inner[i].setOuterClassInfoIndex(ociIdx);
                    inner[i].setInnerNameIndex(inIdx);
                    inner[i].setAccessFlags(accessFlags);
                }
                ((AiInnerClasses) res).setInnerClasses(inner);
                def = false;
            }
            else if (name.equals(AttributeInfo.CODE))
            {
                res = new AiCode();
                int maxStack = dis.readUnsignedShort();
                read += 2;
                int maxLocals = dis.readUnsignedShort();
                read += 2;
                long codeLength = readUnsigned4(dis);
                read += 4;
                byte[] code = new byte[(int) codeLength];
                for (long i = 0; i < codeLength; i++)
                {
                    code[(int) i] = dis.readByte();
                    read++;
                }
                int exceptionTableLen = dis.readUnsignedShort();
                read += 2;
                AiCode.ExceptionSpec[] exs = new AiCode.ExceptionSpec[exceptionTableLen];
                for (int i = 0; i < exceptionTableLen; i++)
                {
                    int startPc = dis.readUnsignedShort();
                    read += 2;
                    int endPc = dis.readUnsignedShort();
                    read += 2;
                    int handlerPc = dis.readUnsignedShort();
                    read += 2;
                    int catchType = dis.readUnsignedShort();
                    read += 2;
                    exs[i] = new AiCode.ExceptionSpec();
                    exs[i].setStartPc(startPc);
                    exs[i].setEndPc(endPc);
                    exs[i].setHandlerPc(handlerPc);
                    exs[i].setCatchType(catchType);
                }

                AttributeInfo attrs[] = readAttributeInfos(c, dis);
                read += 2; // attr len
                for (int i = 0; i < attrs.length; i++)
                {
                    read += 6 + // initial 6 bytes of attribute
                    attrs[i].getLength(); // rest of attribute
                }

                ((AiCode) res).setMaxStack(maxStack);
                ((AiCode) res).setMaxLocals(maxLocals);
                ((AiCode) res).setCode(code);
                ((AiCode) res).setExceptions(exs);
                ((AiCode) res).setAttributes(attrs);
                def = false;
            }
            else if (name.equals(AttributeInfo.LINE_NUMBER_TABLE))
            {
                res = new AiLineNumberTable();
                int lnbrLen = dis.readUnsignedShort();
                read += 2;
                int[][] lookup = new int[lnbrLen][2];
                for (int i = 0; i < lnbrLen; i++)
                {
                    lookup[i][0] = dis.readUnsignedShort();
                    read += 2;
                    lookup[i][1] = dis.readUnsignedShort();
                    read += 2;
                }
                ((AiLineNumberTable) res).set(lookup);
                def = false;
            }
        }

        if (def)
        {
            byte[] info = new byte[(int) length];
            for (long i = 0; i < length; i++)
            {
                info[(int) i] = dis.readByte();
            }
            res = new AttributeInfo();
            res.setInfo(info);
        }
        else
        {
            long left = length - read;
            if (left > 0)
            {
                while (left-- > 0)
                {
                    dis.read();
                }
            }
        }

        res.setNameIndex(nameIndex);
        res.setLength(length);

        return res;
    }

    protected long readUnsigned4(InputStream is) throws IOException
    {
        long l = 0;
        for (int i = 0; i < 4; i++)
        {
            l |= (is.read() & 0xff);
            if (i < 3) {
                l <<= 8;
            }
        }
        return l;
    }
}
