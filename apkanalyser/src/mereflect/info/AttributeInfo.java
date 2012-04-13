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

package mereflect.info;

public class AttributeInfo
{
    public static final String CONSTANT_VALUE = "ConstantValue";
    public static final String CODE = "Code";
    public static final String EXCEPTIONS = "Exceptions";
    public static final String INNER_CLASSES = "InnerClasses";
    public static final String SYNTHETIC = "Synthetic";
    public static final String SOURCE_FILE = "SourceFile";
    public static final String LINE_NUMBER_TABLE = "LineNumberTable";
    public static final String LOCAL_VARIABLE_TABLE = "LocalVariableTable";
    public static final String DEPRECATED = "Deprecated";

    protected int m_nameIndex;
    protected long m_length;
    protected byte[] m_info;

    public int getNameIndex()
    {
        return m_nameIndex;
    }

    public long getLength()
    {
        return m_length;
    }

    public byte[] getInfo()
    {
        return m_info;
    }

    public void setInfo(byte[] info)
    {
        m_info = info;
    }

    public void setLength(long length)
    {
        m_length = length;
    }

    public void setNameIndex(int nameIndex)
    {
        m_nameIndex = nameIndex;
    }
}
