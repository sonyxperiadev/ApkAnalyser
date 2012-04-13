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

package jerl.bcm.inj.impl;

import java.util.List;
import java.util.Vector;

import jerl.bcm.inj.InjectionMethodCall;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodCallReplace extends InjectionMethodCall {
    private final String new_owner;
    private final String new_name;
    private final String new_desc;

    public MethodCallReplace(String signature, String callSignature, boolean pre, String new_owner, String new_name, String new_desc) {
        super(signature, callSignature, pre, false);
        if (!pre) {
            throw new IllegalArgumentException("Only pre call injection available");
        }
        this.new_owner = new_owner;
        this.new_name = new_name;
        this.new_desc = new_desc;
    }

    @Override
    public void inject(MethodVisitor mv) {
        // TODO Auto-generated method stub
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, new_owner, new_name, new_desc);
    }

    @Override
    public List<String> getInstanceData() {
        Vector<String> v = new Vector<String>();
        v.add(getMethodSignature());
        v.add(getCallSignature());
        v.add(Boolean.toString(isPreInjection()));
        v.add(new_owner);
        v.add(new_name);
        v.add(new_desc);
        return v;
    }
}
