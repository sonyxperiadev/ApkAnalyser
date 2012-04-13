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

public class UnknownField extends MEField {
    protected String m_name;
    protected String m_descriptor;

    public UnknownField(String name, String descriptor, MEClass clazz)
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
    public String getDescriptor()
    {
        return m_descriptor;
    }

    /**
     * Returns true if this field is declared public
     * @return if field is public
     */
    @Override
    public boolean isPublic()
    {
        return true;
    }

    /**
     * Returns true if this field is declared protected
     * @return if field is protected
     */
    @Override
    public boolean isProtected()
    {
        return false;
    }

    /**
     * Returns true if this field is declared private
     * @return if field is private
     */
    @Override
    public boolean isPrivate()
    {
        return false;
    }

    /**
     * Returns true if this field is declared static
     * @return if field is static
     */
    @Override
    public boolean isStatic()
    {
        return false;
    }

    /**
     * Returns true if this field is declared final
     * @return if field is final
     */
    @Override
    public boolean isFinal()
    {
        return false;
    }

    /**
     * Returns true if this field is declared volatile
     * @return if field is volatile
     */
    @Override
    public boolean isVolatile()
    {
        return false;
    }

    /**
     * Returns true if this field is declared transient
     * @return if field is transient
     */
    @Override
    public boolean isTransient()
    {
        return false;
    }
}
