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

package analyser.logic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import mereflect.MEClass;
import mereflect.MEField;
import mereflect.MEMethod;

public class RefClass extends AbstractReference
{
    protected MEClass m_class;
    protected Map<Object, Reference> m_children = new HashMap<Object, Reference>();

    public RefClass(MEClass c)
    {
        m_class = c;
    }

    @Override
    public Object getReferred()
    {
        return getMEClass();
    }

    public MEClass getMEClass()
    {
        return m_class;
    }

    @Override
    public String getName()
    {
        return m_class.getClassName();
    }

    @Override
    public Collection<Reference> getChildren()
    {
        return m_children.values();
    }

    public RefMethod registerMethod(MEMethod key)
    {
        RefMethod ref;
        if (!m_children.keySet().contains(key))
        {
            ref = new RefMethod(key);
            m_children.put(key, ref);
            ref.m_parent = this;
        }
        else
        {
            ref = (RefMethod) m_children.get(key);
        }
        return ref;
    }

    public RefField registerField(MEField key)
    {
        RefField ref;
        if (!m_children.keySet().contains(key))
        {
            ref = new RefField(key);
            m_children.put(key, ref);
            ref.m_parent = this;
        }
        else
        {
            ref = (RefField) m_children.get(key);
        }
        return ref;
    }
}
