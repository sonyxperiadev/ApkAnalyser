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
import java.io.InputStream;
import java.util.jar.JarEntry;

public class JarClassResource extends AbstractClassResource
{
    protected JarEntry m_entry;

    public JarClassResource(MEClassContext ctx, JarEntry j)
    {
        setContext(ctx);
        m_entry = j;
    }

    @Override
    public String getContextualSpecification()
    {
        return m_entry.getName() + "@" + ((JarClassContext) getContext()).getFile().getAbsolutePath();
    }

    @Override
    public String getClassName()
    {
        String full = m_entry.getName();
        full = full.replace('/', '.');
        full = full.substring(0, full.length() - ".class".length());
        return full;
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        return ((JarClassContext) getContext()).getJar().getInputStream(m_entry);
    }

}
