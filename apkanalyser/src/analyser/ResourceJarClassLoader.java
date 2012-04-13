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

package analyser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ResourceJarClassLoader extends URLClassLoader
{

    ClassLoader sibling;

    List<String> classpaths = new ArrayList<String>();
    List<File> tempFiles = new ArrayList<File>();
    boolean initiated = false;

    public ResourceJarClassLoader(URL[] urls, ClassLoader parent, ClassLoader sibling)
    {
        super(urls, parent);
        this.sibling = sibling;
    }

    public void addClasspath(String resourceClasspath)
    {
        classpaths.add(resourceClasspath);
    }

    public void arm()
    {
        initiate();
    }

    @Override
    public InputStream getResourceAsStream(String resource)
    {
        InputStream is = null;

        is = super.getResourceAsStream(resource);

        if (is == null)
        {
            is = getParent().getResourceAsStream(resource);
        }
        if (is == null && sibling != null)
        {
            is = sibling.getResourceAsStream(resource);
        }
        return is;

    }

    @Override
    public URL getResource(String resource)
    {
        URL url = null;

        url = super.getResource(resource);

        if (url == null)
        {
            url = getParent().getResource(resource);
        }
        if (url == null && sibling != null)
        {
            url = sibling.getResource(resource);
        }
        return url;

    }

    @Override
    @SuppressWarnings("deprecation")
    public Class<?> findClass(String name) throws ClassNotFoundException
    {
        Class<?> c = null;

        try
        {
            c = super.findClass(name);
        } catch (NoClassDefFoundError ncdfe) {
        } catch (ClassNotFoundException cnfe) {
        }

        if (c == null)
        {
            byte[] b = loadClassData(name);
            if (b != null)
            {
                c = defineClass(b, 0, b.length);
            }
        }

        if (c == null)
        {
            try
            {
                c = getParent().loadClass(name);
            } catch (NoClassDefFoundError ncdfe) {
            } catch (ClassNotFoundException cnfe) {
            }
        }

        if (c == null && sibling != null)
        {
            try
            {
                c = sibling.loadClass(name);
            } catch (NoClassDefFoundError ncdfe) {
            } catch (ClassNotFoundException cnfe) {
            }
        }

        if (c == null)
        {
            throw new ClassNotFoundException(name);
        }

        return c;
    }

    byte[] loadClassData(String name)
    {
        initiate();
        byte[] b = null;
        for (int i = 0; b == null && i < tempFiles.size(); i++)
        {
            File tmpf = tempFiles.get(i);
            b = loadClassDataFromJar(name, tmpf);
        } // per tempfile

        return b;
    }

    byte[] loadClassDataFromJar(String name, File tmpf)
    {
        byte[] b = null;
        JarFile jar = null;
        InputStream is = null;
        try
        {
            jar = new JarFile(tmpf);
            String entryName = name.replace('.', '/') + ".class";
            ZipEntry entry = jar.getEntry(entryName);
            if (entry != null)
            {
                is = jar.getInputStream(entry);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int data;
                while ((data = is.read()) != -1)
                {
                    baos.write(data);
                }
                b = baos.toByteArray();
            }
            else
            {
            }
        } catch (IOException ignore) {
        } finally
        {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {
                }
            }
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException ignore) {
                }
            }
        }
        return b;
    }

    synchronized void initiate()
    {
        if (!initiated)
        {
            // create temporary lib files
            byte[] buf = new byte[1024];
            for (int i = 0; i < classpaths.size(); i++)
            {
                String cp = classpaths.get(i);

                InputStream is = null;
                FileOutputStream fos = null;

                try
                {
                    is = Thread.currentThread().getContextClassLoader().getResourceAsStream(cp);

                    if (is != null)
                    {
                        int sepIx = cp.indexOf("/");
                        String fname = sepIx < 0 ? cp : cp.substring(sepIx);
                        File f = new File("." + File.separator + fname);
                        if (f.exists()) {
                            f.delete();
                        }
                        f.createNewFile();
                        f.deleteOnExit();

                        fos = new FileOutputStream(f);
                        int len;
                        while ((len = is.read(buf)) > 0)
                        {
                            fos.write(buf, 0, len);
                        }
                        fos.flush();

                        tempFiles.add(f);
                    }
                    else
                    {
                        //System.err.println("Warning: " + cp + " not found");
                    }
                } catch (Throwable t)
                {
                    t.printStackTrace();
                } finally
                {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ignore) {
                        }
                    }
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
            } // per classpath

            initiated = true;
        }
    }
}
