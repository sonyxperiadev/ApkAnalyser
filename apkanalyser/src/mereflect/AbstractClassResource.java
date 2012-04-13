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

public abstract class AbstractClassResource implements MEClassResource
{
    protected MEClassContext m_ctx;

    public void setContext(MEClassContext ctx)
    {
        m_ctx = ctx;
    }

    @Override
    public MEClassContext getContext()
    {
        return m_ctx;
    }

    @Override
    public String getName()
    {
        String name = getClassName();
        int idx = name.lastIndexOf('.');
        if (idx >= 0)
        {
            name = name.substring(idx + 1, name.length());
        }
        return name;
    }

    @Override
    public String getPackage()
    {
        String name = getClassName();
        int idx = name.lastIndexOf('.');
        if (idx >= 0)
        {
            name = name.substring(0, idx);
        }
        else
        {
            name = "";
        }
        return name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof MEClassResource)
        {
            return ((MEClassResource) o).getClassName().equals(getClassName());
        }
        else
        {
            return false;
        }
    }
}
