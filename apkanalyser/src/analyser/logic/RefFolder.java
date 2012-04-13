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

public class RefFolder extends AbstractReference {
    protected Map<String, Reference> m_children = new TreeMap<String, Reference>();
    protected int m_type;
    protected String m_name;

    public static final int UNKNOWN = 0;
    public static final int SRC = 1;
    public static final int RES = 2;
    public static final int RESTYPE = 3;
    public static final int XML = 4;

    public RefFolder(String name, int type)
    {
        m_name = name;
        m_type = type;
    }

    @Override
    public String getName()
    {
        return m_name;
    }

    public int getType() {
        return m_type;
    }

    @Override
    public Collection<Reference> getChildren()
    {
        return m_children.values();
    }

    public AbstractReference registerChild(String key, AbstractReference ref)
    {
        if (!m_children.keySet().contains(key))
        {
            m_children.put(key, ref);
            ref.m_parent = this;
        }
        else
        {
            ref = (AbstractReference) m_children.get(key);
        }
        return ref;
    }

    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public int compareTo(Reference o)
    {
        if (o instanceof RefFolder) {
            return m_type - ((RefFolder) o).getType();
        } else {
            return -1;
        }
    }

    @Override
    public Object getReferred() {
        return null;
    }
}
