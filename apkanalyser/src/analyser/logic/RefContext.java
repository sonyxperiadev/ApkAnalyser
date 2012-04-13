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

package analyser.logic;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;

import mereflect.MEClassContext;
import mereflect.UnknownContext;
import mereflect.UnknownResContext;
import analyser.Analyser;
import andreflect.ApkClassContext;
import andreflect.xml.XmlParser;

public class RefContext extends AbstractReference
{
    protected MEClassContext m_ctx;
    protected Map<String, Reference> m_children = new TreeMap<String, Reference>();

    public RefContext(MEClassContext ctx)
    {
        m_ctx = ctx;
    }

    @Override
    public Object getReferred()
    {
        return getContext();
    }

    public MEClassContext getContext()
    {
        return m_ctx;
    }

    @Override
    public String getName()
    {
        return Analyser.getContextName(m_ctx);
    }

    @Override
    public Collection<Reference> getChildren()
    {
        return m_children.values();
    }

    public RefFolder registerSubFolder(String name, int type) {
        RefFolder refFolder;
        if (!m_children.keySet().contains(name)) {
            refFolder = new RefFolder(name, type);
            m_children.put(name, refFolder);
            refFolder.m_parent = this;
        } else {
            refFolder = (RefFolder) m_children.get(name);
        }

        return refFolder;
    }

    public RefFolder registerResSubFolder(String name) {
        RefFolder refFolder;
        if (!m_children.keySet().contains("res")) {
            refFolder = new RefFolder("res", RefFolder.RES);
            m_children.put("res", refFolder);
            refFolder.m_parent = this;
        } else {
            refFolder = (RefFolder) m_children.get("res");
        }

        return (RefFolder) refFolder.registerChild(name, new RefFolder(name, RefFolder.RESTYPE));
    }

    public RefAndroidManifest registerManifest(ZipEntry xml) {
        RefAndroidManifest ref;
        if (!m_children.keySet().contains(XmlParser.MANIFEST)
                && m_ctx instanceof ApkClassContext)
        {
            ref = new RefAndroidManifest(xml, (ApkClassContext) m_ctx);
            m_children.put(XmlParser.MANIFEST, ref);
            ref.m_parent = this;
        }
        else
        {
            ref = (RefAndroidManifest) m_children.get(XmlParser.MANIFEST);
        }
        return ref;
    }

    public RefPackage registerPackage(String key)
    {
        if (m_ctx.getDexReferenceCache() != null
                && m_ctx.getDexReferenceCache().hasSpec()) {
            RefFolder refFolder;
            if (!m_children.keySet().contains("src")) {
                refFolder = new RefFolder("src", RefFolder.SRC);
                m_children.put("src", refFolder);
                refFolder.m_parent = this;
            } else {
                refFolder = (RefFolder) m_children.get("src");
            }
            return (RefPackage) refFolder.registerChild(key, new RefPackage(key));
        }

        RefPackage ref;
        if (!m_children.keySet().contains(key))
        {
            ref = new RefPackage(key);
            m_children.put(key, ref);
            ref.m_parent = this;
        }
        else
        {
            ref = (RefPackage) m_children.get(key);
        }
        return ref;
    }

    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public int compareTo(Reference o)
    {
        if (m_ctx instanceof UnknownContext) {
            return -1;
        }

        if (m_ctx instanceof UnknownResContext) {
            if (o instanceof RefContext) {
                RefContext refContext = (RefContext) o;
                if (refContext.getContext() instanceof UnknownContext) {
                    return 1;
                }
            }
            return -1;
        }
        return super.compareTo(o);
    }

}
