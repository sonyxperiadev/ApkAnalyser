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

import org.jf.dexlib.Code.Instruction;

public abstract class DalvikInjectionMethodOffset extends DalvikInjectionMethod {
    private Instruction instruction = null;

    public DalvikInjectionMethodOffset(String signature, Instruction instruction) {
        super(signature);
        this.instruction = instruction;
    }

    public Instruction getOffsetInstruction() {
        return instruction;
    }

    public void setOffsetInstruction(Instruction instruction) {
        this.instruction = instruction;
    }

    @Override
    public int getInjectionType() {
        return Injection.METHOD_OFFSET_INJECTION;
    }

    @Override
    public List<String> getInstanceData() {
        Vector<String> v = new Vector<String>();
        v.add(getMethodSignature());
        v.add(instruction.opcode.name);
        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            // match up to method signature
            DalvikInjectionMethodOffset imo = (DalvikInjectionMethodOffset) o;
            return getOffsetInstruction() == imo.getOffsetInstruction();
        } else {
            return false;
        }
    }

}
