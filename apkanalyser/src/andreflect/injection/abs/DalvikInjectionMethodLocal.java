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

package andreflect.injection.abs;

import java.util.List;
import java.util.Vector;

import jerl.bcm.inj.Injection;

import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;

public abstract class DalvikInjectionMethodLocal extends DalvikInjectionMethod {
    public static final int INVALID_REG = -1;
    public Instruction beginIns = null;
    public Instruction endIns = null;
    public short reg = INVALID_REG;
    public boolean isRead = false;
    public StringIdItem var = null;
    public TypeIdItem type = null;

    public DalvikInjectionMethodLocal(String signature, Instruction begin, Instruction end, short reg, boolean isRead, StringIdItem str, TypeIdItem type) {
        super(signature);
        beginIns = begin;
        endIns = end;
        this.reg = reg;
        this.isRead = isRead;
        var = str;
        this.type = type;
    }

    public void setEndIns(Instruction end) {
        endIns = end;
    }

    @Override
    public int getInjectionType() {
        return Injection.METHOD_LOCAL_ACCESS_INJECTION;
    }

    @Override
    public List<String> getInstanceData() {
        Vector<String> v = new Vector<String>();
        v.add(getMethodSignature());
        v.add(beginIns.opcode.name);
        if (endIns != null) {
            v.add(endIns.opcode.name);
        } else {
            v.add("null");
        }
        v.add(Integer.toString(reg));
        v.add(Boolean.toString(isRead));
        v.add(var.getStringValue());
        v.add(type.getTypeDescriptor());

        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            DalvikInjectionMethodLocal meo = (DalvikInjectionMethodLocal) o;
            return beginIns == meo.beginIns
                    && endIns == meo.endIns
                    && reg == meo.reg
                    && isRead == meo.isRead
                    && var == meo.var
                    && type == meo.type;
        } else {
            return false;
        }
    }
}
