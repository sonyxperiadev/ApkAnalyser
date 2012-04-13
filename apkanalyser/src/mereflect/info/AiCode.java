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

public class AiCode extends AttributeInfo
{
    protected int m_maxStack;
    protected int m_maxLocals;
    protected byte[] m_code;
    protected int m_codeIndexStart;
    protected int m_codeIndexStop;
    protected ExceptionSpec[] m_exceptions;
    protected AttributeInfo[] m_attributes;

    public AttributeInfo[] getAttributes()
    {
        return m_attributes;
    }

    public byte[] getCode()
    {
        return m_code;
    }

    public int getCodeIndexStart()
    {
        return m_codeIndexStart;
    }

    public int getCodeIndexStop()
    {
        return m_codeIndexStop;
    }

    public ExceptionSpec[] getExceptions()
    {
        return m_exceptions;
    }

    public int getMaxLocals()
    {
        return m_maxLocals;
    }

    public int getMaxStack()
    {
        return m_maxStack;
    }

    public void setAttributes(AttributeInfo[] attributes)
    {
        m_attributes = attributes;
    }

    public void setCode(byte[] code)
    {
        m_code = code;
    }

    public void setCodeIndexStart(int codeIndexStart)
    {
        m_codeIndexStart = codeIndexStart;
    }

    public void setCodeIndexStop(int codeIndexStop)
    {
        m_codeIndexStop = codeIndexStop;
    }

    public void setExceptions(ExceptionSpec[] exceptions)
    {
        m_exceptions = exceptions;
    }

    public void setMaxLocals(int maxLocals)
    {
        m_maxLocals = maxLocals;
    }

    public void setMaxStack(int maxStack)
    {
        m_maxStack = maxStack;
    }

    public static class ExceptionSpec
    {
        protected int m_startPc;
        protected int m_endPc;
        protected int m_handlerPc;
        protected int m_catchType;

        public int getCatchType()
        {
            return m_catchType;
        }

        public int getEndPc()
        {
            return m_endPc;
        }

        public int getHandlerPc()
        {
            return m_handlerPc;
        }

        public int getStartPc()
        {
            return m_startPc;
        }

        public void setCatchType(int catchType)
        {
            m_catchType = catchType;
        }

        public void setEndPc(int endPc)
        {
            m_endPc = endPc;
        }

        public void setHandlerPc(int handlerPc)
        {
            m_handlerPc = handlerPc;
        }

        public void setStartPc(int startPc)
        {
            m_startPc = startPc;
        }
    }
}
