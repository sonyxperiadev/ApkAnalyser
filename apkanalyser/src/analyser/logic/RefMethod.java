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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import mereflect.MEMethod;
import mereflect.primitives.MEVoid;

public class RefMethod extends AbstractReference implements ReverseReference
{
    protected MEMethod m_method;
    protected Set<Reference> m_children = new TreeSet<Reference>();
    ArrayList<ReferredReference> m_reverseLocal = new ArrayList<ReferredReference>();
    ArrayList<ReferredReference> m_reverseExternal = new ArrayList<ReferredReference>();

    public RefMethod(MEMethod m)
    {
        m_method = m;
    }

    @Override
    public String getName()
    {
        try {
            if (m_method.getReturnClass() instanceof MEVoid) {
                return m_method.getFormattedName() + "(" + m_method.getArgumentsString() + ")";
            } else {
                return m_method.getFormattedName() + "(" + m_method.getArgumentsString() + ") " + m_method.getReturnClassString();
            }
        } catch (IOException e) {
            return m_method.getFormattedName() + "(" + m_method.getArgumentsString() + ") " + m_method.getReturnClassString();
        }
    }

    @Override
    public Collection<Reference> getChildren()
    {
        return m_children;
    }

    public RefInvokation registerInvokation(RefInvokation ref)
    {
        if (m_children.contains(ref))
        {
            Iterator<Reference> i = m_children.iterator();
            while (i.hasNext())
            {
                RefInvokation inv = (RefInvokation) i.next();
                if (inv.equals(ref))
                {
                    inv.addCount(1);
                    break;
                }
            }
        }
        else
        {
            m_children.add(ref);
            ref.m_parent = this;
            ref.setCount(1);
            addCount(1);
        }

        return ref;
    }

    public MEMethod getMethod()
    {
        return m_method;
    }

    @Override
    public Object getReferred()
    {
        return getMethod();
    }

    @Override
    public int compareTo(Reference o)
    {
        if (o instanceof RefField)
        {
            return 1;
        }
        else
        {
            return super.compareTo(o);
        }
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
