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

import mereflect.MEClass;
import mereflect.MEField;
import mereflect.MEMethod;
import mereflect.info.AbstractClassInfo;
import mereflect.info.AttributeInfo;
import mereflect.info.CiUtf8;
import mereflect.info.ClassInfo;

public class DebugClassReader extends DefaultClassReader
{
    boolean DBG = true;
    IndexedInputStream iis;

    @Override
    public MEClass readClassFile(DataInputStream dis) throws IOException
    {
        iis = new IndexedInputStream(dis);
        dis = new DataInputStream(iis);

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
        if (DBG) {
            System.out.println("\tconstant pool " + cpLen);
        }
        ClassInfo[] infos = new ClassInfo[cpLen];
        for (int i = 1; i < cpLen; i++)
        {
            try
            {
                int pre = iis.getIndex();
                infos[i] = readClassInfo(c, dis);
                int post = iis.getIndex() - 1;
                if (DBG) {
                    System.out.print("\t" + i + ": " +
                            AbstractClassInfo.ciTagToString(infos[i].getTag()) +
                            "\t[" + infos[i].getTag() + "]  \t" +
                            Integer.toHexString(pre) + " - " +
                            Integer.toHexString(post) + " : " + (post - pre + 1));
                }
                if (DBG)
                {
                    if (infos[i].getTag() == ClassInfo.CONSTANT_Utf8)
                    {
                        int strlen = ((CiUtf8) infos[i]).getUtf8().length();
                        System.out.println("  strlen:" + strlen +
                                (((strlen + 3) != (post - pre + 1)) ? " MISMATCH" : ""));
                    } else {
                        System.out.println();
                    }
                }
                if (infos[i].getTag() == ClassInfo.CONSTANT_Long ||
                        infos[i].getTag() == ClassInfo.CONSTANT_Double)
                {
                    i++;
                }
            } catch (MissingClassInfoException mcie)
            {
                if (DBG) {
                    System.out.println(">>>\t" + c.getMajorVersion() + "/" + c.getMinorVersion() +
                            " Got corrupt pool, tag " + mcie.getTag() + " on " + i);
                }
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
        int pre = iis.getIndex();
        int ifcLen = dis.readUnsignedShort();
        int[] ifcs = new int[ifcLen];
        for (int i = 0; i < ifcLen; i++)
        {
            ifcs[i] = dis.readUnsignedShort();
        }
        int post = iis.getIndex() - 1;
        c.setInterfaceIndices(ifcs);
        if (DBG) {
            System.out.println("\tInterfaces  \t" +
                    ifcLen + "\t" +
                    Integer.toHexString(pre) + " - " +
                    Integer.toHexString(post) + " : " + (post - pre + 1));
        }

        // Fields
        pre = iis.getIndex();
        int fLen = dis.readUnsignedShort();
        MEField[] fields = new MEField[fLen];
        for (int i = 0; i < fLen; i++)
        {
            int pre2 = iis.getIndex();
            fields[i] = readFieldInfo(c, dis);
            int post2 = iis.getIndex() - 1;
            if (DBG) {
                System.out.println("\tField \t\t" + i + "\t" +
                        Integer.toHexString(pre2) + " - " +
                        Integer.toHexString(post2) + " : " + (post2 - pre2 + 1));
            }
        }
        post = iis.getIndex() - 1;
        c.setFields(fields);
        if (DBG) {
            System.out.println("\tFields \t\t" +
                    fLen + "\t" +
                    Integer.toHexString(pre) + " - " +
                    Integer.toHexString(post) + " : " + (post - pre + 1));
        }

        // Methods
        pre = iis.getIndex();
        int mLen = dis.readUnsignedShort();
        MEMethod[] methods = new MEMethod[mLen];
        for (int i = 0; i < mLen; i++)
        {
            int pre2 = iis.getIndex();
            methods[i] = readMethodInfo(c, dis);
            int post2 = iis.getIndex() - 1;
            if (DBG) {
                System.out.println("\tMethod \t\t" + i + "\t" +
                        Integer.toHexString(pre2) + " - " +
                        Integer.toHexString(post2) + " : " + (post2 - pre2 + 1));
            }
        }
        post = iis.getIndex() - 1;
        c.setMethods(methods);
        if (DBG) {
            System.out.println("\tMethods \t\t" +
                    mLen + "\t" +
                    Integer.toHexString(pre) + " - " +
                    Integer.toHexString(post) + " : " + (post - pre + 1));
        }

        // Attributes
        AttributeInfo[] attrs = readAttributeInfos(c, dis);
        c.setAttributes(attrs);

        return c;
    }

    @Override
    protected AttributeInfo[] readAttributeInfos(MEClass c, DataInputStream dis) throws IOException
    {
        int attrLen = dis.readUnsignedShort();
        AttributeInfo[] attrs = new AttributeInfo[attrLen];
        int pre = iis.getIndex();
        for (int i = 0; i < attrLen; i++)
        {
            int pre2 = iis.getIndex();
            attrs[i] = readAttributeInfo(c, dis);
            int post2 = iis.getIndex() - 1;
            if (DBG) {
                System.out.println("\t\tAttr" + attrs[i].getNameIndex() + "\t" +
                        i + "\t" +
                        Integer.toHexString(pre2) + " - " +
                        Integer.toHexString(post2) + " : " + (post2 - pre2 + 1));
            }
        }
        int post = iis.getIndex() - 1;
        if (attrLen > 0) {
            if (DBG) {
                System.out.println("\t\tAttrs \t" +
                        attrLen + "\t" +
                        Integer.toHexString(pre) + " - " +
                        Integer.toHexString(post) + " : " + (post - pre + 1));
            }
        }
        return attrs;
    }
}
