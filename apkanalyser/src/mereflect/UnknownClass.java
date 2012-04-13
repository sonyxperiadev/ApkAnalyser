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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import mereflect.info.AttributeInfo;
import mereflect.info.ClassInfo;

public class UnknownClass extends MEClass
{
    protected String m_classname;
    protected Map<String, MEMethod> m_methodMap = new TreeMap<String, MEMethod>();
    protected Map<String, MEMethod> m_fieldMap = new TreeMap<String, MEMethod>();

    public UnknownClass(String classname, MEClassResource rsc)
    {
        m_classname = classname;
        setResource(rsc);
    }

    @Override
    public String getName()
    {
        return m_classname;
    }

    /**
     * Always returns null
     * @return null
     */
    @Override
    public MEClass getSuperClass()
    {
        return null;
    }

    /**
     * Always returns null
     * @return null
     */
    @Override
    public MEClass[] getInterfaces()
    {
        return null;
    }

    /**
     * Always returns null.
     * @return null
     */
    @Override
    public String[] getDependencies()
    {
        return null;
    }

    /**
     * Returns method matching methodname and descriptor. The format
     * of the descriptor is according to the constant pool definition.
     * In UnknownClass, only methods in this class are searched since
     * any interface or superclass is unknown
     * @param methodName		matching name of method
     * @param descriptor		matching descriptor
     * @return Matching method or null
     */
    @Override
    public MEMethod getMethod(String methodName, String descriptor)
    {

        MEMethod res = null;
        MEMethod[] meths = getMethods();
        for (int i = 0; i < meths.length; i++)
        {
            if (meths[i].getName().equals(methodName) &&
                    meths[i].getDescriptor().equals(descriptor))
            {
                res = meths[i];
                break;
            }
        }
        return res;
    }

    @Override
    public MEField getField(String fieldName, String descriptor)
    {

        MEField res = null;
        MEField[] fields = getFields();
        for (int i = 0; i < fields.length; i++)
        {
            if (fields[i].getName().equals(fieldName) &&
                    fields[i].getDescriptor().equals(descriptor))
            {
                res = fields[i];
                break;
            }
        }
        return res;
    }

    @Override
    public MEField[] getFields()
    {
        Collection<MEMethod> ms = m_fieldMap.values();
        return ms.toArray(new MEField[ms.size()]);
    }

    public void defineMethod(UnknownMethod uMethod)
    {
        m_methodMap.put(uMethod.getName() + uMethod.getDescriptor(), uMethod);
    }

    // Getters
    @Override
    public MEMethod[] getMethods()
    {
        Collection<MEMethod> ms = m_methodMap.values();
        return ms.toArray(new MEMethod[ms.size()]);
    }

    /**
     * Always returns null
     * @return null
     */
    @Override
    public AttributeInfo[] getAttributes()
    {
        return null;
    }

    /**
     * Always returns null
     * @return null
     */
    @Override
    public ClassInfo[] getConstantPool()
    {
        return null;
    }

    /**
     * Always returns null
     * @return null
     */
    @Override
    public int[] getInterfaceIndices()
    {
        return null;
    }

    /**
     * Always returns -1
     * @return -1
     */
    @Override
    public int getMajorVersion()
    {
        return -1;
    }

    /**
     * Always returns -1
     * @return -1
     */
    @Override
    public int getMinorVersion()
    {
        return -1;
    }

    /**
     * Always returns -1
     * @return -1
     */
    @Override
    public int getSuperClassIndex()
    {
        return -1;
    }

    /**
     * Always returns -1
     * @return -1
     */
    @Override
    public int getThisClassIndex()
    {
        return -1;
    }

    @Override
    public int hashCode()
    {
        return m_classname.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        return (o != null && o instanceof UnknownClass && ((UnknownClass) o).getName().equals(getName()));
    }
}
