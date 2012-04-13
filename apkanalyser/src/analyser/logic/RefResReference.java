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

import andreflect.ApkClassContext;
import andreflect.DexReferenceCache;
import andreflect.DexResSpec;
import andreflect.xml.XmlParser.XmlLine;
import brut.androlib.res.data.ResResSpec;

public class RefResReference extends AbstractReference implements ReferredReference {

    public static int RESREF = 0;
    public static int XML = 1;
    public static int CODE = 2;

    private final int m_type;

    private final Object m_reference;
    protected ApkClassContext m_refCtx;

    private final Reference m_refReference;

    public RefResReference(Object reference, int type, ApkClassContext ctx, Reference ref) {
        setCount(1);
        m_type = type;
        m_reference = reference;
        m_refCtx = ctx;
        m_refReference = ref;
    }

    public ApkClassContext getRefContext() {
        return m_refCtx;
    }

    public boolean isXml() {
        return m_type == XML;
    }

    public boolean isCode() {
        return m_type == CODE;
    }

    public boolean isRes() {
        return m_type == RESREF;
    }

    @Override
    public Collection<Reference> getChildren() {
        return new ArrayList<Reference>();
    }

    @Override
    public String getName() {
        if (m_type == RESREF) {
            return DexResSpec.getRefName((ResResSpec) m_reference);
        } else if (m_type == XML) {
            return DexResSpec.getRefName((XmlLine) m_reference);
        } else if (m_type == CODE) {
            return DexResSpec.getRefName((DexReferenceCache.LoadConstRes) m_reference);
        }
        return null;
    }

    @Override
    public Object getReferred() {
        return m_reference;
    }

    @Override
    public int compareTo(Reference o)
    {
        if (o instanceof RefResReference)
        {
            int otype = ((RefResReference) o).m_type;

            if (otype != m_type) {
                return m_type - otype;
            } else {
                return super.compareTo(o);
            }
        }
        else
        {
            return super.compareTo(o);
        }
    }

    @Override
    public Reference getExternalReferredReference() {
        return null;
    }

    @Override
    public Reference getLocalReferredReference() {
        return m_refReference;
    }

}
