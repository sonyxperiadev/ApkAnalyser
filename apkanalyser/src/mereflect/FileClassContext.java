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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileClassContext extends AbstractClassContext
{
    protected static final String DESCRIPTION = "Classpath";
    protected static final String SUFFIX = ".class";
    protected File m_dir;

    public FileClassContext(String directory, boolean isMidlet)
    {
        this(new File(directory));
        m_isMidlet = isMidlet;
    }

    public FileClassContext(File directory)
    {
        m_dir = directory;
    }

    @Override
    public MEClassResource[] getClassResourcesImpl() throws IOException
    {
        ArrayList<File> files = new ArrayList<File>();
        recurseFiles(files, m_dir.listFiles());
        MEClassResource[] rscs = new MEClassResource[files.size()];
        for (int i = 0; i < files.size(); i++)
        {
            rscs[i] = new FileClassResource(this, files.get(i));
        }
        return rscs;
    }

    private void recurseFiles(List<File> list, File[] files)
    {
        for (int i = 0; files != null && i < files.length; i++)
        {
            if (files[i].isFile() && files[i].getAbsolutePath().endsWith(SUFFIX))
            {
                list.add(files[i]);
            }
            else if (files[i].isDirectory())
            {
                recurseFiles(list, files[i].listFiles());
            }
        }
    }

    @Override
    public String getContextName()
    {
        return m_dir.getAbsolutePath();
    }

    @Override
    public String getContextDescription()
    {
        return DESCRIPTION;
    }

    /**
     * Returns the directory of this file class context
     * @return the classpath
     */
    public File getDir()
    {
        return m_dir;
    }
}
