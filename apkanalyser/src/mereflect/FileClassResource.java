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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileClassResource extends AbstractClassResource
{
    protected File m_file;

    public FileClassResource(MEClassContext ctx, File f)
    {
        setContext(ctx);
        m_file = f;
    }

    @Override
    public String getContextualSpecification()
    {
        return m_file.getAbsolutePath();
    }

    @Override
    public String getClassName()
    {
        String full = m_file.getAbsolutePath();
        String dir = ((FileClassContext) getContext()).getDir().getAbsolutePath();
        String c = null;
        if (!dir.endsWith(File.separator))
        {
            c = full.substring(dir.length() + 1);
        }
        else
        {
            c = full.substring(dir.length());
        }
        c = c.replace(File.separatorChar, '.');
        c = c.substring(0, c.length() - ".class".length());
        return c;
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        return new FileInputStream(m_file);
    }

}
