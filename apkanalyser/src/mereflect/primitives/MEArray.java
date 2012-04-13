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

package mereflect.primitives;

import mereflect.Type;

public final class MEArray extends AbstractPrimitive
{
    protected Type m_arrayType;

    public MEArray(Type arrayType)
    {
        m_arrayType = arrayType;
    }

    @Override
    public char getDescriptorChar()
    {
        return Type.CH_ARRAY;
    }

    @Override
    public String getName()
    {
        return Character.toString(Type.CH_ARRAY);
    }

    @Override
    public boolean isArray()
    {
        return true;
    }

    public Type getArrayType()
    {
        return m_arrayType;
    }

    public int getArrayDepth()
    {
        int depth = 0;
        Type t = this;
        do
        {
            depth++;
            t = ((MEArray) t).getArrayType();
        } while (t.isArray());
        return depth;
    }

    public Type getFinalArrayType()
    {
        Type t = this;
        do
        {
            t = ((MEArray) t).getArrayType();
        } while (t.isArray());
        return t;
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        return (o != null &&
                o instanceof MEArray &&
                ((MEArray) o).getArrayDepth() == getArrayDepth() && ((MEArray) o).getFinalArrayType().equals(getFinalArrayType()));
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(getFinalArrayType().toString());
        for (int i = getArrayDepth(); i > 0; i--)
        {
            sb.append("[]");
        }
        return sb.toString();
    }
}
