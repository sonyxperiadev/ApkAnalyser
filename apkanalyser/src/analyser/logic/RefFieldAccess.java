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

import andreflect.DexReferenceCache;

public class RefFieldAccess extends AbstractReference implements ReferredReference {
    DexReferenceCache.FieldAccess m_access;
    RefMethod m_refMethod;

    public RefFieldAccess(DexReferenceCache.FieldAccess access, RefMethod refMethod) {
        setCount(1);
        m_access = access;
        m_refMethod = refMethod;
    }

    @Override
    public Collection<Reference> getChildren() {
        return new ArrayList<Reference>();
    }

    @Override
    public String getName() {
        return m_access.toString();
    }

    @Override
    public Object getReferred() {
        return m_access;
    }

    public DexReferenceCache.FieldAccess getAccess() {
        return m_access;
    }

    @Override
    public int compareTo(Reference o)
    {
        if (o instanceof RefFieldAccess)
        {
            RefFieldAccess aco = (RefFieldAccess) o;
            if (aco.m_access.isRead == false
                    && m_access.isRead == true) {
                return -1;
            } else if (aco.m_access.isRead == true
                    && m_access.isRead == false) {
                return 1;
            } else {
                return getName().compareTo(aco.getName());
            }
        }
        else
        {
            return super.compareTo(o);
        }
    }

    public RefMethod getReferredMethod()
    {
        return m_refMethod;
    }

    @Override
    public Reference getLocalReferredReference() {
        return m_refMethod;
    }

    @Override
    public Reference getExternalReferredReference() {
        return null;
    }

}
