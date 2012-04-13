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

import gui.AppException;

public class CorruptBytecodeException extends AppException
{
    private static final long serialVersionUID = 5841596489809482262L;
    int m_pc;
    int m_len;
    int m_bytecode;

    public CorruptBytecodeException()
    {
        super();
    }

    public CorruptBytecodeException(int pc, int len, int bytecode)
    {
        m_pc = pc;
        m_len = len;
        m_bytecode = bytecode;
    }

    public int getLen()
    {
        return m_len;
    }

    public int getPc()
    {
        return m_pc;
    }

    public int getBytecode()
    {
        return m_bytecode;
    }
}
