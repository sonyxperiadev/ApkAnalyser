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

public abstract class AbstractReference implements Reference
{
    protected int m_count = 0;
    protected int m_flags;
    protected Reference m_parent;
    protected String m_mirrorName = null;

    @Override
    public Reference getParent()
    {
        return m_parent;
    }

    @Override
    public int getFlags()
    {
        return m_flags;
    }

    @Override
    public void setFlags(int flags)
    {
        m_flags = flags;
    }

    @Override
    public int getCount()
    {
        return m_count;
    }

    @Override
    public void setCount(int count)
    {
        m_count = count;
    }

    @Override
    public void addCount(int count)
    {
        m_count += count;
        if (m_parent != null) {
            m_parent.addCount(count);
        }
    }

    @Override
    public void rename(String name)
    {
        m_mirrorName = name;
    }

    @Override
    public void removeRename()
    {
        m_mirrorName = null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Reference)
        {
            boolean eq = true;
            if (m_parent != null) {
                eq = m_parent.equals(((Reference) o).getParent());
            }
            return eq && getName().equals(((Reference) o).getName());
        }
        else
        {
            return false;
        }
    }

    @Override
    public int compareTo(Reference o)
    {
        if (o instanceof Reference)
        {
            return getName().toLowerCase().compareTo((o).getName().toLowerCase());
        }
        else
        {
            return getName().toLowerCase().compareTo(o.toString().toLowerCase());
        }
    }

    @Override
    public String toString()
    {
        if (m_mirrorName == null) {
            return getName();
        } else {
            return m_mirrorName;
        }
    }
}
