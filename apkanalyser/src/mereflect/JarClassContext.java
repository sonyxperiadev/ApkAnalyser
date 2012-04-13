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
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import andreflect.DexReferenceCache;

public class JarClassContext extends AbstractClassContext {
    public static final String DESCRIPTION = "Jar";
    public static final String JAR_SUFFIX = ".jar";
    public static final String ZIP_SUFFIX = ".zip";
    protected static final String SUFFIX = ".class";
    protected File m_file;
    protected JarFile m_jar;
    private DexReferenceCache m_refCache = null;

    public static final String RESOURCE = "resources.arsc";

    public JarClassContext(String jarPath, boolean isMidlet) {
        this(new File(jarPath), isMidlet);
    }

    public JarClassContext(File jarFile, boolean isMidlet) {
        m_file = jarFile;
        m_isMidlet = isMidlet;
        m_refCache = new DexReferenceCache(m_file, m_isMidlet);
    }

    @Override
    public DexReferenceCache getDexReferenceCache() {
        return m_refCache;
    }

    @Override
    public MEClassResource[] getClassResourcesImpl() throws IOException {
        ArrayList<JarEntry> classFiles = new ArrayList<JarEntry>();
        if (m_jar == null) {
            m_jar = new JarFile(m_file);
        }
        Enumeration<JarEntry> e = m_jar.entries();
        while (e.hasMoreElements()) {
            visit(classFiles, e.nextElement());
        }
        MEClassResource[] rscs = new MEClassResource[classFiles.size()];
        for (int i = 0; i < classFiles.size(); i++) {
            rscs[i] = new JarClassResource(this, classFiles.get(i));
        }
        return rscs;
    }

    private void visit(List<JarEntry> classFiles, JarEntry entry) {
        // String name = entry.getName();
        if (!entry.isDirectory() && entry.getName().endsWith(SUFFIX)) {
            classFiles.add(entry);
        }
    }

    @Override
    public String getContextName() {
        return m_file.getAbsolutePath();
    }

    @Override
    public String getContextDescription() {
        return DESCRIPTION;
    }

    /**
     * Returns file that points out the jar
     * 
     * @return the file
     */
    public File getFile() {
        return m_file;
    }

    /**
     * Returns jar file
     * 
     * @return the jar file
     */
    public JarFile getJar() {
        try {
            if (m_jar == null) {
                m_jar = new JarFile(getFile());
            }
        } catch (IOException ioe) {
        }
        return m_jar;
    }
}
