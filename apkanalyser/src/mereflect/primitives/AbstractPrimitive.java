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

public abstract class AbstractPrimitive implements Type
{
    @Override
    public boolean isArray()
    {
        return false;
    }

    @Override
    public boolean isPrimitive()
    {
        return true;
    }

    public abstract char getDescriptorChar();

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        return (o != null && o.getClass().equals(this.getClass()));
    }

    @Override
    public String toString()
    {
        return getName();
    }

    public static Class<?> convertJavaPrimitiveToEnclosingClass(Class<?> c) {
        if (c.isPrimitive()) {
            if (c.equals(int.class)) {
                c = Integer.class;
            } else if (c.equals(boolean.class)) {
                c = Boolean.class;
            } else if (c.equals(float.class)) {
                c = Float.class;
            } else if (c.equals(double.class)) {
                c = Double.class;
            } else if (c.equals(short.class)) {
                c = Short.class;
            } else if (c.equals(char.class)) {
                c = Character.class;
            } else if (c.equals(byte.class)) {
                c = Byte.class;
            } else if (c.equals(double.class)) {
                c = Double.class;
            } else if (c.equals(long.class)) {
                c = Long.class;
            }
        }
        return c;
    }
}
