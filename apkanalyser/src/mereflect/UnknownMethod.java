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

import analyser.gui.LineBuilder;


public class UnknownMethod extends MEMethod
{
    protected String m_name;
    protected String m_descriptor;

    public UnknownMethod(String name, String descriptor, MEClass clazz)
    {
        super(clazz);
        m_name = name;
        m_descriptor = descriptor;
    }

    @Override
    public String getName()
    {
        return m_name;
    }

    @Override
    public byte[] getByteCodes()
    {
        return new byte[0];
    }

    public LineBuilder getByteCodeAssembler(String prefix)
            throws CorruptBytecodeException
    {
        LineBuilder lb = new LineBuilder();
        lb.newLine();
        lb.append("[This method was not found]");
        return lb;
    }

    @Override
    public String getDescriptor()
    {
        return m_descriptor;
    }

    @Override
    public MEClass[] getExceptions() throws IOException
    {
        return new MEClass[0];
    }

    @Override
    public String getExceptionsString()
    {
        return "";
    }

    @Override
    public List<Invokation> getInvokations() throws CorruptBytecodeException
    {
        return new ArrayList<Invokation>();
    }

    @Override
    public boolean isAbstract()
    {
        return false;
    }

    @Override
    public boolean isFinal()
    {
        return false;
    }

    @Override
    public boolean isNative()
    {
        return false;
    }

    @Override
    public boolean isPrivate()
    {
        return false;
    }

    @Override
    public boolean isProtected()
    {
        return false;
    }

    @Override
    public boolean isPublic()
    {
        return true;
    }

    @Override
    public boolean isStatic()
    {
        return false;
    }

    @Override
    public boolean isStrict()
    {
        return false;
    }

    @Override
    public boolean isSynchronized()
    {
        return false;
    }
}
