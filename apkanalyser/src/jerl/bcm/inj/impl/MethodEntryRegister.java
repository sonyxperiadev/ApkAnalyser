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

import jerl.bcm.inj.InjectionMethodEntry;

import org.objectweb.asm.MethodVisitor;

public class MethodEntryRegister extends InjectionMethodEntry {
    private final int id;

    public MethodEntryRegister(String signature, int id) {
        super(signature);
        this.id = id;
    }

    public int getID() {
        return id;
    }

    @Override
    public void inject(MethodVisitor mv) {
        new InjectCollection().injectRegister(mv, id);
    }

    @Override
    public List<String> getInstanceData() {
        Vector<String> v = new Vector<String>();
        v.add(getMethodSignature());
        v.add(Integer.toString(id));
        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MethodEntryRegister mer = (MethodEntryRegister) o;
            return getID() == mer.getID();
        } else {
            return false;
        }
    }
}
