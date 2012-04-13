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

public class UnknownResource extends AbstractClassResource
{
    protected String m_fullClassName;

    public UnknownResource(String fullClassName, MEClassContext uCtx)
    {
        m_fullClassName = fullClassName;
        setContext(uCtx);
    }

    @Override
    public String getContextualSpecification()
    {
        return "unknown/" + getClassName();
    }

    @Override
    public String getClassName()
    {
        return m_fullClassName;
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        throw new IOException("No class resource inputstream for unknown contexts");
    }
}
