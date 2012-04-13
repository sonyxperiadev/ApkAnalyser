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

public class AiInnerClasses extends AttributeInfo
{
    protected InnerClass[] m_innerClasses;

    public InnerClass[] getInnerClasses()
    {
        return m_innerClasses;
    }

    public void setInnerClasses(InnerClass[] innerClasses)
    {
        m_innerClasses = innerClasses;
    }

    public static class InnerClass
    {
        protected int m_innerClassInfoIndex;
        protected int m_outerClassInfoIndex;
        protected int m_innerNameIndex;
        protected int m_accessFlags;

        public int getAccessFlags()
        {
            return m_accessFlags;
        }

        public int getInnerClassInfoIndex()
        {
            return m_innerClassInfoIndex;
        }

        public int getInnerNameIndex()
        {
            return m_innerNameIndex;
        }

        public int getOuterClassInfoIndex()
        {
            return m_outerClassInfoIndex;
        }

        public void setAccessFlags(int accessFlags)
        {
            m_accessFlags = accessFlags;
        }

        public void setInnerClassInfoIndex(int innerClassInfoIndex)
        {
            m_innerClassInfoIndex = innerClassInfoIndex;
        }

        public void setInnerNameIndex(int innerNameIndex)
        {
            m_innerNameIndex = innerNameIndex;
        }

        public void setOuterClassInfoIndex(int outerClassInfoIndex)
        {
            m_outerClassInfoIndex = outerClassInfoIndex;
        }
    }
}
