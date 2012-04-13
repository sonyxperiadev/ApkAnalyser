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
import java.util.Set;
import java.util.TreeSet;

import mereflect.MEField;

public class RefField extends AbstractReference {
    protected MEField m_field;

    protected Set<Reference> m_children = new TreeSet<Reference>();

    public RefField(MEField m)
    {
        m_field = m;
    }

    @Override
    public String getName()
    {
        return m_field.getName() + " : " + m_field.getType();
    }

    public MEField getField()
    {
        return m_field;
    }

    @Override
    public Object getReferred()
    {
        return getField();
    }

    @Override
    public Collection<Reference> getChildren() {
        return m_children;
    }

    public RefFieldAccess registerAccess(RefFieldAccess ref)
    {
        m_children.add(ref);
        ref.m_parent = this;
        addCount(1);

        return ref;
    }

    @Override
    public int compareTo(Reference o)
    {
        if (o instanceof RefMethod)
        {
            return -1;
        }
        else
        {
            return super.compareTo(o);
        }
    }
}
