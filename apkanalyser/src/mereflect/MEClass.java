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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import mereflect.info.AttributeInfo;
import mereflect.info.CiClass;
import mereflect.info.CiUtf8;
import mereflect.info.ClassInfo;

public class MEClass implements Type, Comparable<Object>
{
    public static final int ACC_PUBLIC = 0x0001;
    public static final int ACC_FINAL = 0x0010;
    public static final int ACC_SUPER = 0x0020;
    public static final int ACC_INTERFACE = 0x0200;
    public static final int ACC_ABSTRACT = 0x0400;

    protected int m_majorVersion;
    protected int m_minorVersion;

    protected ClassInfo[] m_constantPool;

    protected int m_accessFlags;
    protected int m_thisClassIndex;
    protected int m_superClassIndex;
    protected int[] m_interfaceIndices;

    protected MEMethod[] m_methods;
    protected MEField[] m_fields;
    protected AttributeInfo[] m_attributes;

    protected MEClassResource m_resource;

    protected MEClass m_superClass = null;
    protected MEClass[] m_interfaces = null;

    // Logic

    /**
     * Returns the class name as appearing in constant pool
     */
    public String getRawName()
    {
        CiClass classInfo = (CiClass) getConstantPool()[m_thisClassIndex];
        return ((CiUtf8) getConstantPool()[classInfo.getNameIndex()]).getUtf8();
    }

    /**
     * Returns full name of class
     * @return class name including package
     */
    @Override
    public String getName()
    {
        return getRawName().replace('/', '.');
    }

    /**
     * Returns class name, excluding package declaration
     * @return class name excluding package
     */
    public String getClassName()
    {
        String clName = getName();
        int dIndex = clName.lastIndexOf('.');
        if (dIndex >= 0)
        {
            clName = clName.substring(dIndex + 1);
        }
        return clName;
    }

    /**
     * Returns superclass
     * @return the superclass of this type
     */
    public MEClass getSuperClass()
    {
        if (m_superClass == null)
        {
            if (m_superClassIndex == 0) {
                return null;
            }
            CiClass classInfo = (CiClass) getConstantPool()[m_superClassIndex];
            CiUtf8 classDef = (CiUtf8) getConstantPool()[classInfo.getNameIndex()];
            String classname = classDef.getUtf8().replace('/', '.');
            try
            {
                m_superClass = getResource().getContext().getMEClass(classname);
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e)
            {
                m_superClass = new UnknownClass(classname, getResource());
            }
        }
        return m_superClass;
    }

    public MEClass[] getInterfaces()
    {
        if (m_interfaces == null)
        {
            m_interfaces = new MEClass[m_interfaceIndices.length];
            for (int i = 0; i < m_interfaces.length; i++)
            {
                int ifcIdx = m_interfaceIndices[i];
                CiClass ifcInfo = (CiClass) getConstantPool()[ifcIdx];
                CiUtf8 ifcDef = (CiUtf8) getConstantPool()[ifcInfo.getNameIndex()];
                String ifcName = ifcDef.getUtf8().replace('/', '.');
                try
                {
                    m_interfaces[i] = getResource().getContext().getMEClass(ifcName);
                } catch (IOException e)
                {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e)
                {
                    m_interfaces[i] = new UnknownClass(ifcName, getResource());
                }
            }
        }
        return m_interfaces;
    }

    /**
     * Returns true if this class has public access
     * @return if this class has public access
     */
    public boolean isPublic()
    {
        return (m_accessFlags & ACC_PUBLIC) > 0;
    }

    /**
     * Returns true if this class is declared final
     * @return if this class is final
     */
    public boolean isFinal()
    {
        return (m_accessFlags & ACC_FINAL) > 0;
    }

    /**
     * Returns true if this class is super - excerpt from VM Spec:<br>
     * Treat superclass methods specially when invoked by the invokespecial instruction
     * @return if this class is super
     */
    public boolean isSuper()
    {
        return (m_accessFlags & ACC_SUPER) > 0;
    }

    /**
     * Returns true if this class is an interface
     * @return if this class is an interface
     */
    public boolean isInterface()
    {
        return (m_accessFlags & ACC_INTERFACE) > 0;
    }

    public int getFlags()
    {
        return m_accessFlags;
    }

    /**
     * Returns true if this class is declared abstract
     * @return if this class is abstract
     */
    public boolean isAbstract()
    {
        return (m_accessFlags & ACC_ABSTRACT) > 0;
    }

    @Override
    public boolean isPrimitive()
    {
        return false;
    }

    @Override
    public boolean isArray()
    {
        return false;
    }

    /**
     * Returns all class references found in constantpool
     * @return String array of all class references
     */
    public String[] getDependencies()
    {
        TreeSet<String> refClasses = new TreeSet<String>();
        for (int i = 1; i < m_constantPool.length; i++)
        {
            ClassInfo ci = m_constantPool[i];
            if (ci != null && ci.getTag() == ClassInfo.CONSTANT_Class)
            {
                int classNameIndex = ((CiClass) ci).getNameIndex();
                if (classNameIndex > 0 && classNameIndex < m_constantPool.length &&
                        m_constantPool[classNameIndex] != null &&
                        m_constantPool[classNameIndex].getTag() == ClassInfo.CONSTANT_Utf8)
                {
                    String cname = ((CiUtf8) m_constantPool[classNameIndex]).getUtf8();
                    int arrayIx = cname.lastIndexOf('[');
                    int clEndIx = cname.indexOf(';');
                    if (arrayIx < 0)
                    {
                        refClasses.add(cname);
                    }
                    else if (arrayIx >= 0 && clEndIx > 0)
                    {
                        cname = cname.substring(arrayIx + 2, cname.length() - 1); // e.g. [[Ljava/lang/Object;
                        refClasses.add(cname);
                    }
                }
            }
        }
        return refClasses.toArray(new String[refClasses.size()]);
    }

    /**
     * Returns all methods whose methodname matches paraemter.
     * Only searches this class, not in superclasses nor implemented interfaces.
     * @param methodName the matching methodname to search for
     * @return array of methods, or empty array of no matching methods were found
     */
    public MEMethod[] getMethods(String methodName)
    {
        List<MEMethod> res = new ArrayList<MEMethod>();
        MEMethod[] meths = getMethods();
        for (int i = 0; i < meths.length; i++)
        {
            if (meths[i].getName().equals(methodName)) {
                res.add(meths[i]);
            }
        }
        return res.toArray(new MEMethod[res.size()]);
    }

    public String getUnknownSuperClassName()
    {
        MEClass clazz = this;
        String className = null;
        while (clazz != null
                && (!(clazz instanceof UnknownClass))) {
            clazz = clazz.getSuperClass();
        }
        if (clazz != null
                && clazz instanceof UnknownClass) {
            className = clazz.getName();
        }
        return className;
    }

    public MEField getField(String fieldName, String descriptor)
    {
        MEField res = null;
        MEField[] fields = getFields();
        if (fields != null) {
            for (int i = 0; i < fields.length; i++)
            {
                if (fields[i].getName().equals(fieldName) &&
                        (descriptor == null ||
                        fields[i].getDescriptor().equals(descriptor)))
                {
                    res = fields[i];
                    break;
                }
            }
        }
        if (res == null)
        {
            MEClass superClass = getSuperClass();
            if (superClass != null && !(superClass instanceof UnknownClass))
            {
                res = superClass.getField(fieldName, descriptor);
            }
        }
        return res;
    }

    /**
     * Returns method matching methodname and descriptor. The format
     * of the descriptor is according to the constant pool definition.
     * This method also searches superclasses and implemented interfaces.
     * @param methodName		matching name of method
     * @param descriptor		matching descriptor, or null for wildcard.
     *                        If descriptor is null, arbitrary matching method
     *                        will be returned.
     * @return Matching method or null
     */
    public MEMethod getMethod(String methodName, String descriptor)
    {

        MEMethod res = null;
        MEMethod[] meths = getMethods();
        if (meths != null) {
            for (int i = 0; i < meths.length; i++)
            {
                if (meths[i].getName().equals(methodName) &&
                        (descriptor == null ||
                        meths[i].getDescriptor().equals(descriptor)))
                {
                    res = meths[i];
                    break;
                }
            }
        }
        if (res == null)
        {
            MEClass superClass = getSuperClass();
            if (superClass != null && !(superClass instanceof UnknownClass))
            {
                res = superClass.getMethod(methodName, descriptor);
            }
        }
        if (res == null)
        {
            MEClass[] ifcs = getInterfaces();
            for (int i = 0; i < ifcs.length && res == null; i++)
            {
                if (!(ifcs[i] instanceof UnknownClass))
                {
                    res = ifcs[i].getMethod(methodName, descriptor);
                }
            }
        }
        return res;
    }

    /**
     * Returns method matching methodname and descriptor. The format
     * of the descriptor is according to the constant pool definition.
     * This method does not search superclasses and implemented interfaces.
     * @param methodName    matching name of method
     * @param descriptor    matching descriptor, or null for wildcard.
     *                      If descriptor is null, no method will be returned.
     * @return Matching method or null
     */
    public MEMethod getMethodIsolated(String methodName, String descriptor)
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

    /**
     * Recursively goes through superclasses and interfaces of this
     * class to find a match against specified class.
     * @param p   The class to search for
     * @return    True if found in this or supertree, false if not.
     */
    public boolean isInstanceOf(MEClass p)
    {
        if (equals(p))
        {
            return true;
        }
        boolean res = false;
        MEClass superClass = getSuperClass();
        if (superClass != null && !(superClass instanceof UnknownClass))
        {
            res = superClass.isInstanceOf(p);
        }
        if (!res)
        {
            MEClass[] ifcs = getInterfaces();
            for (int i = 0; ifcs != null && !res && i < ifcs.length; i++)
            {
                res = ifcs[i].isInstanceOf(p);
            }
        }
        return res;
    }

    // Setters

    public void setAccessFlags(int accessFlags)
    {
        m_accessFlags = accessFlags;
    }

    public void setAttributes(AttributeInfo[] attributes)
    {
        m_attributes = attributes;
    }

    public void setConstantPool(ClassInfo[] constantPool)
    {
        m_constantPool = constantPool;
    }

    public void setFields(MEField[] fields)
    {
        m_fields = fields;
    }

    public void setInterfaceIndices(int[] interfaces)
    {
        m_interfaceIndices = interfaces;
    }

    public void setMajorVersion(int majorVersion)
    {
        m_majorVersion = majorVersion;
    }

    public void setMethods(MEMethod[] methods)
    {
        m_methods = methods;
    }

    public void setMinorVersion(int minorVersion)
    {
        m_minorVersion = minorVersion;
    }

    public void setSuperClassIndex(int superClass)
    {
        m_superClassIndex = superClass;
    }

    public void setThisClassIndex(int thisClass)
    {
        m_thisClassIndex = thisClass;
    }

    public void setResource(MEClassResource rsc)
    {
        m_resource = rsc;
    }

    // Getters

    public int getAccessFlags()
    {
        return m_accessFlags;
    }

    public AttributeInfo[] getAttributes()
    {
        return m_attributes;
    }

    public ClassInfo[] getConstantPool()
    {
        return m_constantPool;
    }

    public MEField[] getFields()
    {
        return m_fields;
    }

    public int[] getInterfaceIndices()
    {
        return m_interfaceIndices;
    }

    public int getMajorVersion()
    {
        return m_majorVersion;
    }

    /**
     * Returns all methods in this class/interface.
     * Does not return methods of superclass nor implemented interfaces.
     * @return Array of methods.
     */
    public MEMethod[] getMethods()
    {
        return m_methods;
    }

    public int getMinorVersion()
    {
        return m_minorVersion;
    }

    public int getSuperClassIndex()
    {
        return m_superClassIndex;
    }

    public int getThisClassIndex()
    {
        return m_thisClassIndex;
    }

    public MEClassResource getResource()
    {
        return m_resource;
    }

    @Override
    public int hashCode()
    {
        int hashCode = getName().hashCode();
        hashCode = 31 * hashCode + getResource().getContextualSpecification().hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object o)
    {
        return (o != null && o instanceof MEClass && !(o instanceof UnknownClass)
                && ((MEClass) o).getResource().getContextualSpecification().equals(getResource().getContextualSpecification())
                && ((MEClass) o).getName().equals(getName()));
    }

    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public int compareTo(Object o)
    {
        if (o instanceof MEClass) {
            return getName().compareTo(((MEClass) o).getName());
        } else {
            return getName().compareTo(o.toString());
        }
    }
}
