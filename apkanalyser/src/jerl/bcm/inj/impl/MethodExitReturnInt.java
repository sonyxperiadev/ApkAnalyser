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

import jerl.bcm.inj.InjectionMethodExit;

import org.objectweb.asm.MethodVisitor;

public class MethodExitReturnInt extends InjectionMethodExit {
    private final int i;

    public MethodExitReturnInt(String signature, int i) {
        super(signature);
        this.i = i;
    }

    public int getInt() {
        return i;
    }

    @Override
    public void inject(MethodVisitor mv) {
        new InjectCollection().injectReplaceStackInt(mv, i);
    }

    @Override
    public List<String> getInstanceData() {
        Vector<String> v = new Vector<String>();
        v.add(getMethodSignature());
        v.add(Integer.toString(i));
        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MethodExitReturnInt med = (MethodExitReturnInt) o;
            return getInt() == med.getInt();
        } else {
            return false;
        }
    }
}
