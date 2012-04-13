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

package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public abstract class JarFileModifier
{
    static SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    ZipFile m_origJar;
    String m_destJarName;

    public JarFileModifier(ZipFile originalJar)
    {
        this(originalJar, null);
    }

    public JarFileModifier(JarFile originalJar)
    {
        this(originalJar, null);
    }

    public JarFileModifier(ZipFile originalJar, String destinationJar)
    {
        if (destinationJar == null || destinationJar.trim().length() == 0)
        {
            String name = originalJar.getName();
            if (name.toLowerCase().endsWith(".apk"))
            {
                name = name.substring(0, name.length() - 4);
            }
            String dateString = m_dateFormat.format(new Date());
            destinationJar = name + "_" + dateString + ".ap_";
        }
        m_origJar = originalJar;
        m_destJarName = destinationJar;
    }

    public JarFileModifier(JarFile originalJar, String destinationJar)
    {
        if (destinationJar == null || destinationJar.trim().length() == 0)
        {
            String name = originalJar.getName();
            if (name.toLowerCase().endsWith(".jar"))
            {
                name = name.substring(0, name.length() - 4);
            }
            String dateString = m_dateFormat.format(new Date());
            destinationJar = name + "_" + dateString + ".jar";
        }
        m_origJar = originalJar;
        m_destJarName = destinationJar;
    }

    /**
     * Implement to return a list of Strings denoting what entries should be excluded in
     * modified jar file
     * @return List of Strings with exclude names, or null of no excluded entries
     */
    public abstract List<String> excludeEntries();

    /**
     * Implement to return a list of Strings denoting names of new entries which should
     * be included modified jar file. <code>getNewEntry(theName, false)</code> will then
     * be called to get the content of the new entry.
     * @return List of Strings with new names, or null of no new entries
     */
    public abstract List<String> newEntries();

    /**
     * Implement this to return a inputstream to data to a modified or a new entry.
     * @param entryName   The name of the new or modified entry
     * @param modified    True if requested entry is modified, false if requested entry is new.
     * @return inputstream to new or modified entry.
     */
    public abstract InputStream getNewEntry(String entryName, boolean modified);

    public File createModifiedJar() throws IOException {
        File f = new File(m_destJarName);
        if (f.exists()) {
            f.delete();
        }

        ZipOutputStream zout = null;
        FileOutputStream fos = null;
        InputStream is = null;
        try
        {
            fos = new FileOutputStream(m_destJarName);
            zout = new ZipOutputStream(fos);
            zout.setLevel(9);

            // Fill in all new entries
            List<String> newEntries = newEntries();
            for (int i = 0; newEntries != null && i < newEntries.size(); i++) {
                String newEntryName = newEntries.get(i);
                is = getNewEntry(newEntryName, false);
                if (is != null) {
                    zout.putNextEntry(new ZipEntry(newEntryName));
                    transfer(is, zout);
                    zout.closeEntry();
                    is.close();
                }
            }

            List<String> excludeEntries = excludeEntries();

            // Fill in existing and modified, exclude specified
            Enumeration<? extends ZipEntry> entries = m_origJar.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // Exclude entry
                if (excludeEntries != null && excludeEntries.contains(entryName)) {
                    continue;
                }

                // Try getting a modified entry
                is = getNewEntry(entryName, true);

                // Otherwise, get original entry
                if (is == null) {
                    is = m_origJar.getInputStream(entry);
                }

                zout.putNextEntry(entry.getMethod() == ZipEntry.STORED ? new ZipEntry(entry) : new ZipEntry(entryName));
                transfer(is, zout);
                zout.closeEntry();
                is.close();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable ignore) {
                }
            }
            if (zout != null) {
                try {
                    zout.closeEntry();
                } catch (Throwable ignore) {
                }
                zout.close();
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Throwable ignore) {
                }
            }
        }
        return f;
    }

    void transfer(InputStream is, OutputStream os) throws IOException
    {
        int len;
        byte[] buffer = new byte[1024];
        synchronized (buffer)
        {
            while ((len = is.read(buffer)) > 0)
            {
                os.write(buffer, 0, len);
            }
            os.flush();
        }
    }
}
