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

import java.util.ArrayList;

import jerl.bcm.inj.InjectionMethod;

import org.jf.dexlib.Code.Instruction;
import org.objectweb.asm.MethodVisitor;

import andreflect.DexMethod;
import andreflect.injection.DalvikInjectCollection;

public abstract class DalvikInjectionMethod extends InjectionMethod {
    private DexMethod dexMethod = null;

    public DexMethod getMethod() {
        return dexMethod;
    }

    public void setMethod(DexMethod method) {
        dexMethod = method;
    }

    public abstract ArrayList<Instruction> injectDalvik(DalvikInjectCollection dic, DexMethod method, Instruction instruction);

    public DalvikInjectionMethod(String signature) {
        super(signature);
    }

    @Override
    public void inject(MethodVisitor mv) {
        //not used
    }
}
