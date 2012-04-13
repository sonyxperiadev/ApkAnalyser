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

package mereflect.info;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractClassInfo implements ClassInfo
{
    protected int m_tag;

    protected Map<Integer, Object> m_values = new HashMap<Integer, Object>();

    @Override
    public int getTag()
    {
        return m_tag;
    }

    public void setTag(int tag)
    {
        m_tag = tag;
    }

    protected void setValue(int id, Object value)
    {
        m_values.put(new Integer(id), value);
    }

    protected Object getValue(int id)
    {
        return m_values.get(new Integer(id));
    }

    public static String ciTagToString(int tag)
    {
        switch (tag)
        {
        case ClassInfo.CONSTANT_Class:
            return "Class";
        case ClassInfo.CONSTANT_Fieldref:
            return "Fieldref";
        case ClassInfo.CONSTANT_Methodref:
            return "Methodref";
        case ClassInfo.CONSTANT_InterfaceMethodref:
            return "IfcMethodref";
        case ClassInfo.CONSTANT_String:
            return "String";
        case ClassInfo.CONSTANT_Integer:
            return "Integer";
        case ClassInfo.CONSTANT_Float:
            return "Float";
        case ClassInfo.CONSTANT_Long:
            return "Long";
        case ClassInfo.CONSTANT_Double:
            return "Double";
        case ClassInfo.CONSTANT_NameAndType:
            return "NameAndType";
        case ClassInfo.CONSTANT_Utf8:
            return "Utf8";
        default:
            return "<N/A>";
        }
    }
}
