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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import andreflect.ApkClassContext;
import andreflect.DexResSpec;

public class RefResSpec extends AbstractReference implements ReverseReference {

    protected Set<Reference> m_children = new TreeSet<Reference>();

    protected DexResSpec m_spec;
    protected ApkClassContext m_refCtx;
    ArrayList<ReferredReference> m_reverseLocal = new ArrayList<ReferredReference>();
    ArrayList<ReferredReference> m_reverseExternal = new ArrayList<ReferredReference>();

    public RefResSpec(DexResSpec spec, ApkClassContext ctx) {
        m_spec = spec;
        m_refCtx = ctx;
    }

    public RefResReference registerResReference(Object reference, int type, Reference refReference) {
        RefResReference ref = new RefResReference(reference, type, m_refCtx, refReference);
        m_children.add(ref);
        ref.m_parent = this;
        addCount(1);
        return ref;
    }

    public DexResSpec getDexSpec() {
        return m_spec;
    }

    public ApkClassContext getRefContext() {
        return m_refCtx;
    }

    @Override
    public Collection<Reference> getChildren() {
        return m_children;
    }

    @Override
    public String getName() {
        return m_spec.getResSpec().getName();
    }

    @Override
    public Object getReferred() {
        return getDexSpec();
    }

    @Override
    public List<ReferredReference> getReferredReference(boolean isLocal) {
        if (isLocal) {
            return m_reverseLocal;
        } else {
            return m_reverseExternal;
        }
    }

    @Override
    public void addReferredReference(ReferredReference reference, boolean isLocal) {
        ArrayList<ReferredReference> reverse;
        if (isLocal) {
            reverse = m_reverseLocal;
        } else {
            reverse = m_reverseExternal;
        }

        if (!reverse.contains(reference)) {
            reverse.add(reference);
        }
    }
}
