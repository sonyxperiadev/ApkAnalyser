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

package andreflect;

import java.io.IOException;
import java.util.List;

import mereflect.MEClass;
import mereflect.UnknownClass;

import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;

import andreflect.definition.DexClassDefinition;

public class DexClass extends MEClass {

    ClassDefItem m_item;
    DexClassDefinition m_def = null;

    public DexClass(ClassDefItem classItem) {
        m_item = classItem;
    }

    public DexClassDefinition getDefinition() {
        if (m_def == null) {
            m_def = new DexClassDefinition(m_item);
        }
        return m_def;
    }

    public ClassDefItem getClassDefItem() {
        return m_item;
    }

    @Override
    public String getRawName()
    {
        return Util.getClassRawName(m_item);
    }

    @Override
    public MEClass getSuperClass() {
        if (m_superClass == null)
        {
            if (m_item.getSuperclass() == null) {
                return null;
            }
            String classname = Util.getClassName(m_item.getSuperclass());
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

    @Override
    public MEClass[] getInterfaces() {
        if (m_interfaces == null)
        {
            TypeListItem interfaceList = m_item.getInterfaces();
            if (interfaceList == null) {
                return new MEClass[0];
            }

            List<TypeIdItem> interfaces = interfaceList.getTypes();
            if (interfaces == null || interfaces.size() == 0) {
                return new MEClass[0];
            }

            m_interfaces = new MEClass[interfaceList.getTypeCount()];

            int i = 0;
            for (TypeIdItem typeIdItem : interfaceList.getTypes()) {

                MEClass interfaceImpl = null;
                try {
                    interfaceImpl = getResource().getContext().getMEClass(Util.getClassName(typeIdItem));
                } catch (IOException e)
                {
                    interfaceImpl = new UnknownClass(Util.getClassName(typeIdItem), getResource());
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e)
                {
                    interfaceImpl = new UnknownClass(Util.getClassName(typeIdItem), getResource());
                }

                m_interfaces[i++] = interfaceImpl;

            }
        }
        return m_interfaces;
    }

    @Override
    public String[] getDependencies()
    {
        //TODO only command line?
        return null;
    }
}
