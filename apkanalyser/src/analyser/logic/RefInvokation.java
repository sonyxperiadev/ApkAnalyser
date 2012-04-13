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

import analyser.Analyser;

import mereflect.MEClass;
import mereflect.MEClassContext;
import mereflect.MEMethod;

public class RefInvokation extends AbstractReference implements ReferredReference
{
    protected String m_name;
    protected RefContext m_refContext;
    protected RefPackage m_refPackage;
    protected RefClass m_refClass;
    protected RefMethod m_refMethod;
    protected boolean m_isLocal = false;
    protected MEMethod.Invokation m_invokation;

    protected RefInvokation m_oppositeInvokation;

    public RefInvokation(String invName, RefContext refContext, RefPackage refPackage,
            RefClass refClass, RefMethod refMethod, boolean isLocal, MEMethod.Invokation inv)
    {
        setCount(1);
        m_name = invName;
        m_refContext = refContext;
        m_refPackage = refPackage;
        m_refClass = refClass;
        m_refMethod = refMethod;
        m_isLocal = isLocal;
        m_invokation = inv;
    }

    @Override
    public String getName()
    {
        return m_name;
    }

    public String getResourceName()
    {
        return Analyser.getContextName(getContext());
    }

    @Override
    public Collection<Reference> getChildren()
    {
        return new ArrayList<Reference>();
        //throw new RuntimeException("Logic error");
    }

    public MEClass getMEClass()
    {
        return m_refClass.getMEClass();
    }

    public String getPackage()
    {
        return getRefPackage().getName();
    }

    public MEClassContext getContext()
    {
        return getMEClass().getResource().getContext();
    }

    public MEMethod getMethod()
    {
        return getRefMethod().getMethod();
    }

    public RefClass getRefClass()
    {
        return m_refClass;
    }

    public RefMethod getRefMethod()
    {
        return m_refMethod;
    }

    public RefPackage getRefPackage()
    {
        return m_refPackage;
    }

    public RefContext getRefResource()
    {
        return m_refContext;
    }

    public MEMethod.Invokation getInvokation()
    {
        return m_invokation;
    }

    @Override
    public Object getReferred()
    {
        return getInvokation();
    }

    public RefInvokation getOppositeInvokation()
    {
        return m_oppositeInvokation;
    }

    public void setOppositeInvokation(RefInvokation oppositeInvokation)
    {
        m_oppositeInvokation = oppositeInvokation;
    }

    public boolean isLocal()
    {
        return m_isLocal;
    }

    @Override
    public int compareTo(Reference o)
    {
        if (o instanceof RefInvokation)
        {
            return getCompareName().compareTo(((RefInvokation) o).getCompareName());
        }
        else
        {
            return getCompareName().compareTo(o.toString());
        }
    }

    private String getCompareName()
    {
        if (m_oppositeInvokation != null)
        {
            return m_oppositeInvokation.getResourceName() + getName();
        }
        else
        {
            return m_name;
        }
    }

    @Override
    public Reference getLocalReferredReference() {
        return getRefResource().getContext().isMidlet() ? this : getOppositeInvokation();
    }

    @Override
    public Reference getExternalReferredReference() {
        return getRefResource().getContext().isMidlet() ? getOppositeInvokation() : this;
    }
}
