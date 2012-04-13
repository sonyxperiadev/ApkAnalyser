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

package andreflect;

import java.io.File;
import java.io.IOException;

import mereflect.AbstractClassContext;
import mereflect.MEClassResource;

import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;

import andreflect.xml.XmlParser;

//todo:
//3. clean up show bytecode in mainframe and action
//11. fix locating array list resource id in show bytecode
//12. intent and broadcost dependency check.

public class ApkClassContext extends AbstractClassContext {
    public static final String DESCRIPTION = "Apk";
    public static final String APK_SUFFIX = ".apk";
    protected File m_file;
    protected DexFile m_dex;
    protected String m_contextDescriptionName = null;
    private XmlParser m_xmlParser = null;
    private DexReferenceCache m_refCache = null;

    public ApkClassContext(String apkPath, boolean isApk) {
        this(new File(apkPath), isApk);
    }

    public ApkClassContext(File apkFile, boolean isApk) {
        m_file = apkFile;
        m_isMidlet = isApk;
        m_refCache = new DexReferenceCache(m_file, m_isMidlet);
        m_xmlParser = new XmlParser(m_file, m_refCache.getResTable());

    }

    @Override
    public DexReferenceCache getDexReferenceCache() {
        return m_refCache;
    }

    @Override
    public MEClassResource[] getClassResourcesImpl() throws IOException {
        if (m_dex == null) {
            m_dex = new DexFile(m_file);
        }

        DexResource[] rscs = new DexResource[m_dex.ClassDefsSection.getItems().size()];
        //System.out.println("[ApkClassContext] " + m_file.getName()+ " is loaded with "+ m_dex.ClassDefsSection.getItems().size()+" classes");
        int i = 0;
        for (ClassDefItem classDefItem : m_dex.ClassDefsSection.getItems()) {
            rscs[i++] = new DexResource(this, classDefItem);
        }

        return rscs;
    }

    @Override
    public String getContextName() {
        return m_file.getAbsolutePath();
    }

    @Override
    public String getContextDescription() {
        return DESCRIPTION;
    }

    public XmlParser getXmlParser() {
        return m_xmlParser;
    }

    public String getContextDescriptionName() {
        if (m_contextDescriptionName == null) {
            if (m_isMidlet
                    && getXmlParser().getManifest() != null) {
                m_contextDescriptionName = getXmlParser().getManifest().getContextDescriptionName()
                        + " (" + m_file.getName() + ")";
            } else {
                m_contextDescriptionName = m_file.getName();
            }
        }
        return m_contextDescriptionName;
    }

    public File getFile() {
        return m_file;
    }

    public DexFile getDex() {
        try {
            if (m_dex == null) {
                m_dex = new DexFile(getFile());
            }
        } catch (IOException ioe) {
        }
        return m_dex;
    }

}
