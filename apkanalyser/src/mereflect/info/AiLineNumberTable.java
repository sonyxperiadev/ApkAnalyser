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

public class AiLineNumberTable extends AttributeInfo
{
    protected int[][] m_lookup;

    public void set(int[][] lookup)
    {
        m_lookup = lookup;
    }

    public int[][] getLookup()
    {
        return m_lookup;
    }

    public int getBytecodeIndex(int linenumber)
    {
        int index = -1;
        for (int i = 0; i < m_lookup.length; i++)
        {
            if (linenumber >= m_lookup[i][1])
            {
                index = i;
                break;
            }
        }
        if (index >= 0)
        {
            return m_lookup[index][0];
        }
        else
        {
            return -1;
        }
    }

    public int getLinenumber(int bcIndex)
    {
        int index = -1;
        for (int i = 0; i < m_lookup.length; i++)
        {
            if (bcIndex >= m_lookup[i][0])
            {
                index = i;
                break;
            }
        }
        if (index >= 0)
        {
            return m_lookup[index][1];
        }
        else
        {
            return -1;
        }
    }
}
