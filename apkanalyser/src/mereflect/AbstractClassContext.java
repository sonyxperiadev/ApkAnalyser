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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mereflect.io.ClassReaderFactory;
import andreflect.ApkClassContext;
import andreflect.DexReader;
import andreflect.DexReferenceCache;
import andreflect.DexResource;

public abstract class AbstractClassContext implements MEClassContext, Comparable<Object>
{
    protected MEClassContext m_parent;
    protected List<MEClassContext> m_children = new ArrayList<MEClassContext>();
    protected Map<String, MEClass> m_classCache = new TreeMap<String, MEClass>();
    protected MEClassResource[] m_resourceCache = null;
    protected boolean m_isMidlet;

    @Override
    public MEClassContext getParentContext()
    {
        return m_parent;
    }

    @Override
    public void setParentContext(MEClassContext ctx)
    {
        m_parent = ctx;
    }

    @Override
    public synchronized MEClassContext[] getContexts()
    {
        return (m_children.toArray(new MEClassContext[m_children.size()]));
    }

    @Override
    public synchronized void addContext(MEClassContext ctx)
    {
        if (!m_children.contains(ctx))
        {
            ctx.setParentContext(this);
            m_children.add(ctx);
            m_resourceCache = null;
        }
    }

    @Override
    public synchronized void removeContext(MEClassContext ctx)
    {
        if (m_children.contains(ctx))
        {
            ctx.setParentContext(null);
            m_children.remove(ctx);
            m_resourceCache = null;
        }
    }

    @Override
    public synchronized MEClassResource[] getClassResources() throws IOException
    {
        if (m_resourceCache == null)
        {
            ArrayList<MEClassResource> rscs = new ArrayList<MEClassResource>();
            addToList(rscs, getClassResourcesImpl());
            MEClassContext[] ctxs = getContexts();
            for (int i = 0; i < ctxs.length; i++)
            {
                addToList(rscs, ctxs[i].getClassResources());
            }
            m_resourceCache = rscs.toArray(new MEClassResource[rscs.size()]);
        }
        return m_resourceCache;
    }

    public abstract MEClassResource[] getClassResourcesImpl() throws IOException;

    @Override
    public synchronized MEClassResource getClassResource(String name) throws IOException
    {
        MEClassResource[] rscs = getClassResources();
        MEClassResource rsc = null;
        for (int i = 0; i < rscs.length; i++)
        {
            if (rscs[i].getClassName().equals(name))
            {
                rsc = rscs[i];
                break;
            }
        }
        return rsc;
    }

    @Override
    public String[] getClassnames() throws IOException
    {
        MEClassResource[] rscs = getClassResources();
        String[] cnames = new String[rscs.length];
        for (int i = 0; i < cnames.length; i++)
        {
            cnames[i] = rscs[i].getClassName();
        }
        return cnames;
    }

    @Override
    public MEClass getMEClass(String classname) throws IOException, ClassNotFoundException
    {
        MEClass c = null;
        c = m_classCache.get(classname);
        if (c == null && getParentContext() != null)
        {
            try
            {
                c = getParentContext().getMEClass(classname);
            } catch (ClassNotFoundException cnfe)
            {
                c = null;
            }
        }

        if (c == null)
        {
            MEClassResource[] rscs = getClassResources();
            for (int i = 0; i < rscs.length; i++)
            {
                if (rscs[i].getClassName().equals(classname)) {

                    if (rscs[i] instanceof DexResource) {
                        try
                        {
                            c = DexReader.readClassFile((DexResource) rscs[i], ((ApkClassContext) (rscs[i].getContext())).getDex().isOdex());
                            m_classCache.put(classname, c);
                            c.setResource(rscs[i]);
                            break;
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    } else {
                        DataInputStream dis = null;
                        try
                        {
                            dis = new DataInputStream(rscs[i].getInputStream());
                            c = ClassReaderFactory.getClassReader().readClassFile(dis);
                            m_classCache.put(classname, c);
                            c.setResource(rscs[i]);
                            break;
                        } finally
                        {
                            if (dis != null)
                            {
                                dis.close();
                            }
                        }
                    }
                }
            }
        }
        if (c == null)
        {
            throw new ClassNotFoundException(classname);
        }
        return c;
    }

    private void addToList(ArrayList<MEClassResource> list, MEClassResource[] rscs)
    {
        if (rscs != null)
        {
            for (int i = 0; i < rscs.length; i++)
            {
                if (!list.contains(rscs[i])) {
                    list.add(rscs[i]);
                }
            }
        }
    }

    @Override
    public int compareTo(Object o)
    {
        if (o instanceof MEClassContext)
        {
            return getContextName().compareTo(((MEClassContext) o).getContextName());
        }
        else
        {
            return getContextName().compareTo(o.toString());
        }
    }

    @Override
    public boolean isMidlet()
    {
        return m_isMidlet;
    }

    @Override
    public DexReferenceCache getDexReferenceCache() {
        return null;
    }
}
