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

import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;

import andreflect.ApkClassContext;
import andreflect.xml.XmlParser;

public class RefAndroidManifest extends RefXml {

    protected ApkClassContext m_ctx;

    public RefAndroidManifest(ZipEntry xml, ApkClassContext
            ctx)
    {
        super(xml, ctx);
        m_ctx = ctx;
    }

    public ApkClassContext getContext()
    {
        return m_ctx;
    }

    @Override
    public Collection<Reference> getChildren() {
        return new ArrayList<Reference>();
    }

    @Override
    public String getName() {
        return XmlParser.MANIFEST;
    }

    @Override
    public Object getReferred() {
        return null;
    }

    @Override
    public int compareTo(Reference o)
    {
        return 1;
    }
}
