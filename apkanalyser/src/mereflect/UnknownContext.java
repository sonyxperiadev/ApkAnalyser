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
import java.util.Map;
import java.util.TreeMap;

public class UnknownContext extends AbstractClassContext
{
    protected Map<String, MEClass> m_classes = new TreeMap<String, MEClass>();

    @Override
    public MEClassResource[] getClassResourcesImpl() throws IOException
    {
        return null;
    }

    @Override
    public String getContextName()
    {
        return "Unknown Code Reference";
    }

    @Override
    public String getContextDescription()
    {
        return "Unknown Code Reference";
    }

    @Override
    public MEClass getMEClass(String classname) throws IOException, ClassNotFoundException
    {
        MEClass c = m_classes.get(classname);
        if (c == null)
        {
            throw new ClassNotFoundException(classname);
        }
        return c;
    }

    public void defineClass(UnknownClass clazz)
    {
        m_classes.put(clazz.getName(), clazz);
    }

}
