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

package mereflect.bytecode;

import java.util.HashMap;
import java.util.Map;

import mereflect.CorruptBytecodeException;
import mereflect.MEMethod;

public class Bytecode
{
    protected int m_bytecode;
    protected int m_index;
    protected int m_bcIndex;
    protected int m_len;
    protected String m_bcString;

    /**
     * Returns index of the bytecode in bytecode-array
     * @return
     */
    public int getBytecodeIndex()
    {
        return m_bcIndex;
    }

    /**
     * Returns textual description of bytecode
     * @return
     */
    public String getBytecodeOpString()
    {
        return m_bcString;
    }

    /**
     * Returns the bytecode
     * @return
     */
    public int getBytecode()
    {
        return m_bytecode;
    }

    /**
     * Returns index of the bytecode in a byte-array
     * @return
     */
    public int getIndex()
    {
        return m_index;
    }

    /**
     * Returns length of bytecode in bytes
     * @return
     */
    public int getLength()
    {
        return m_len;
    }

    public static int getBytecodeIndexForOffset(MEMethod method, int offset)
            throws CorruptBytecodeException {
        int pc = 0;
        int index = 0;
        byte[] code = method.getByteCodes();
        if (code != null)
        {
            while (pc < code.length && pc < offset)
            {
                int bytecode = (code[pc] & 0xff);
                int len = Bytecodes.BC_LENGTHS[bytecode];
                if (bytecode == 170) // tableswitch
                {
                    len = ((Integer) Bytecode.getTableSwitch(code, pc).get(Bytecode.SWITCH_OP_LENGTH)).intValue();
                }
                else if (bytecode == 171) // lookupswitch
                {
                    len = ((Integer) Bytecode.getLookupSwitch(code, pc).get(Bytecode.SWITCH_OP_LENGTH)).intValue();
                }

                if (len < 0 || pc + len > code.length)
                {
                    throw new CorruptBytecodeException(pc, len, bytecode);
                }

                pc += len;
                index++;
            }
        }
        return index;
    }

    public static final String SWITCH_OP_LENGTH = "switch_len";
    public static final String SWITCH_DEFAULT = "default";
    public static final String SWITCH_LOW = "low";
    public static final String SWITCH_HIGH = "high";
    public static final String SWITCH_PAIRS = "pairs";

    public static class Pair
    {
        public int vCase;
        public int vJump;

        public Pair(int vCase, int vJump)
        {
            this.vCase = vCase;
            this.vJump = vJump;
        }
    }

    public static Map<Object, Object> getTableSwitch(byte[] code, int pc)
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        int oldPc = pc;
        pc++;
        int padding = 4 - (pc & 3);
        if (padding == 4) {
            padding = 0;
        }
        pc += padding;
        int vDefault = read32(code, pc);
        pc += 4;
        map.put(SWITCH_DEFAULT, new Integer(vDefault));
        int vLow = read32(code, pc);
        pc += 4;
        map.put(SWITCH_LOW, new Integer(vLow));
        int vHigh = read32(code, pc);
        pc += 4;
        map.put(SWITCH_HIGH, new Integer(vHigh));

        for (int i = vLow; i <= vHigh; i++)
        {
            int vJump = read32(code, pc);
            pc += 4;
            map.put(new Integer(i), new Integer(vJump));
        }

        map.put(SWITCH_OP_LENGTH, new Integer(pc - oldPc));
        return map;
    }

    public static Map<Object, Object> getLookupSwitch(byte[] code, int pc)
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        int oldPc = pc;
        pc++;
        int padding = 4 - (pc & 3);
        if (padding == 4) {
            padding = 0;
        }
        pc += padding;
        int vDefault = read32(code, pc);
        map.put(SWITCH_DEFAULT, new Integer(vDefault));
        pc += 4;
        int nPairs = read32(code, pc);
        pc += 4;
        map.put(SWITCH_PAIRS, new Integer(nPairs));

        for (int i = 0; i < nPairs; i++)
        {
            int vData = read32(code, pc);
            pc += 4;
            int vJump = read32(code, pc);
            pc += 4;
            map.put(new Integer(i), new Pair(vData, vJump));
        }

        map.put(SWITCH_OP_LENGTH, new Integer(pc - oldPc));
        return map;
    }

    protected static int read32(byte[] data, int offset)
    {
        int v = 0;
        v = ((data[offset] & 0xff) << 24) | ((data[offset + 1] & 0xff) << 16)
                | ((data[offset + 2] & 0xff) << 8) | ((data[offset + 3] & 0xff));
        return v;
    }
}
