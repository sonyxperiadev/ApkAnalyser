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

package andreflect.injection.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.InstructionWithReference;

import andreflect.DexMethod;
import andreflect.Util;
import andreflect.injection.DalvikInjectCollection;
import andreflect.injection.abs.DalvikInjectionMethodOffset;

public class DalvikMethodOffsetInvokeParam extends DalvikInjectionMethodOffset {
    private final String str;

    public DalvikMethodOffsetInvokeParam(String signature, Instruction ins, String str) {
        super(signature, ins);
        this.str = str;
    }

    public String getMessage() {
        return str;
    }

    @Override
    public ArrayList<Instruction> injectDalvik(DalvikInjectCollection dic, DexMethod method, Instruction ins) {
        //this is always deodexed
        MethodIdItem methodIdItem = (MethodIdItem) ((InstructionWithReference) ins.deodexedInstruction).getReferencedItem();
        return dic.injectInvokeParams(method, Util.appendCodeAddressAndLineNum(str, ins), ins, methodIdItem);
    }

    @Override
    public List<String> getInstanceData() {
        Vector<String> v = new Vector<String>();
        v.add(getMethodSignature());
        v.add(getOffsetInstruction().opcode.name);
        v.add(str);
        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            DalvikMethodOffsetInvokeParam moo = (DalvikMethodOffsetInvokeParam) o;
            return getMessage().equals(moo.getMessage());
        } else {
            return false;
        }
    }
}
