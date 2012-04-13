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
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import andreflect.ApkClassContext;

public class CollaborateClassContext extends AbstractClassContext
{
    protected static final String DESCRIPTION = "Collaborate";
    protected static int ID = 1;
    protected int m_id = ID++;

    public void setClasspath(String classpath)
    {
        StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);
        while (st.hasMoreTokens())
        {
            String path = st.nextToken();
            if (path.toLowerCase().endsWith(ApkClassContext.APK_SUFFIX))
            {
                //special case for framework-res.apk
                try {
                    ZipFile zipFile = new ZipFile(path);
                    ZipEntry zipEntry = zipFile.getEntry("classes.dex");
                    if (zipEntry == null) {
                        addJarClasspath(path);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (path.toLowerCase().endsWith(JarClassContext.JAR_SUFFIX))
            {
                try {
                    ZipFile zipFile = new ZipFile(path);
                    ZipEntry zipEntry = zipFile.getEntry("classes.dex");
                    if (zipEntry != null) {
                        addDexClasspath(path);
                    } else {
                        addJarClasspath(path);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (path.toLowerCase().endsWith(JarClassContext.ZIP_SUFFIX))
            {
                try {
                    ZipFile zipFile = new ZipFile(path);
                    ZipEntry zipEntry = zipFile.getEntry("classes.dex");
                    if (zipEntry != null) {
                        addDexClasspath(path);
                    } else {
                        addJarClasspath(path);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (path.toLowerCase().endsWith(".odex")) {
                addDexClasspath(path);
            } else if (path.toLowerCase().endsWith(".dex")) {
                addDexClasspath(path);
            }
            else
            {
                addFileClasspath(path);
            }
        }
    }

    public String getClasspath()
    {
        StringBuffer sb = new StringBuffer();
        MEClassContext[] ctxs = getContexts();
        appendContexts(ctxs, sb);
        if (sb.length() >= 1)
        {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private void appendContexts(MEClassContext[] ctxs, StringBuffer sb)
    {
        if (ctxs == null) {
            return;
        }
        for (int i = 0; i < ctxs.length; i++)
        {
            MEClassContext ctx = ctxs[i];
            if (ctx.getContextDescription().equals(FileClassContext.DESCRIPTION))
            {
                sb.append(((FileClassContext) ctx).getDir().getAbsolutePath());
            }
            else if (ctx.getContextDescription().equals(JarClassContext.DESCRIPTION))
            {
                sb.append(((JarClassContext) ctx).getFile().getAbsolutePath());
            }

            sb.append(File.pathSeparatorChar);
        }
        for (int i = 0; i < ctxs.length; i++)
        {
            appendContexts(ctxs[i].getContexts(), sb);
        }
    }

    public void addDexClasspath(String classpath)
    {
        addContext(new ApkClassContext(classpath, false));
    }

    public void addFileClasspath(String classpath)
    {
        addContext(new FileClassContext(classpath, false));
    }

    public void addJarClasspath(String classpath)
    {
        addContext(new JarClassContext(classpath, false));
    }

    @Override
    public MEClassResource[] getClassResourcesImpl() throws IOException
    {
        return null;
    }

    @Override
    public String getContextName()
    {
        return "Collaborate" + m_id;
    }

    @Override
    public String getContextDescription()
    {
        return DESCRIPTION;
    }

    @Override
    public boolean isMidlet()
    {
        throw new RuntimeException("Cannot resolve if context belongs to midlet classpath");
    }
}
