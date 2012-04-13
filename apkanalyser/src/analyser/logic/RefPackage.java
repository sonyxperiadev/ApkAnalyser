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
import java.util.Map;
import java.util.TreeMap;

import mereflect.MEClass;

public class RefPackage extends AbstractReference
{
    protected String m_name;
    protected Map<MEClass, Reference> m_children = new TreeMap<MEClass, Reference>();

    public RefPackage(String name)
    {
        if (name == null || name.length() == 0) {
            m_name = "(default)";
        } else {
            m_name = name;
        }
    }

    @Override
    public String getName()
    {
        return m_name;
    }

    @Override
    public Collection<Reference> getChildren()
    {
        return m_children.values();
    }

    public RefClass registerClass(MEClass key)
    {
        RefClass ref;
        if (!m_children.keySet().contains(key))
        {
            ref = new RefClass(key);
            m_children.put(key, ref);
            ref.m_parent = this;
        }
        else
        {
            ref = (RefClass) m_children.get(key);
        }
        return ref;
    }

    @Override
    public Object getReferred()
    {
        return m_name;
    }
}
