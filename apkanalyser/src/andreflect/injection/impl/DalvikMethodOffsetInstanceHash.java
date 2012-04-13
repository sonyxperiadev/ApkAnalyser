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

import org.jf.dexlib.Code.Instruction;

import andreflect.DexMethod;
import andreflect.Util;
import andreflect.injection.DalvikInjectCollection;
import andreflect.injection.abs.DalvikInjectionMethodExit;

public class DalvikMethodOffsetInstanceHash extends DalvikInjectionMethodExit {

    private final String str;
    private short register;
    private final boolean isConstructor;

    public DalvikMethodOffsetInstanceHash(String signature, String str, boolean isConstructor) {
        super(signature);
        this.str = str;
        this.isConstructor = isConstructor;
    }

    public String getMessage() {
        return str;
    }

    public void setRegister(short register) {
        this.register = register;
    }

    @Override
    public ArrayList<Instruction> injectDalvik(DalvikInjectCollection dic, DexMethod method, Instruction instruction) {
        StringBuffer sb = new StringBuffer();
        if (isConstructor) {
            sb.append("@   Construct ");
            sb.append(Util.appendCodeAddressAndLineNum(str, instruction));
        } else {
            sb.append("@   Finalize ");
            sb.append(str);
        }
        sb.append(" @ ");
        String prompt = sb.toString();
        return dic.injectInstanceHash(method, register, prompt, instruction, true);
    }

    @Override
    public List<String> getInstanceData() {
        Vector<String> v = new Vector<String>();
        v.add(getMethodSignature());
        v.add(str);
        v.add(Boolean.toString(isConstructor));
        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            DalvikMethodOffsetInstanceHash meo = (DalvikMethodOffsetInstanceHash) o;
            return getMessage().equals(meo.getMessage())
                    && isConstructor == meo.isConstructor;
        } else {
            return false;
        }
    }

}