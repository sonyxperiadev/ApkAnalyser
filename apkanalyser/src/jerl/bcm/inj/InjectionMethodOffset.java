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

package jerl.bcm.inj;

import java.util.List;
import java.util.Vector;

import org.objectweb.asm.MethodVisitor;

public abstract class InjectionMethodOffset extends InjectionMethod {
    private final int offset;

    /**
     * 
     * @param signature
     *            method signature
     * @param offset
     *            bytecode offset, inject after the given offset.
     */
    public InjectionMethodOffset(String signature, int offset) {
        super(signature);
        this.offset = offset;
    }

    /**
     * Returns the bytecode offset where this injection should be injected.
     * 
     * @return bytecode offset
     */
    public int getByteCodeOffset() {
        return offset;
    }

    @Override
    public int getInjectionType() {
        return Injection.METHOD_OFFSET_INJECTION;
    }

    @Override
    public abstract void inject(MethodVisitor mv);

    @Override
    public List<String> getInstanceData() {
        Vector<String> v = new Vector<String>();
        v.add(getMethodSignature());
        v.add(Integer.toString(getByteCodeOffset()));
        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            // match up to method signature
            InjectionMethodOffset imo = (InjectionMethodOffset) o;
            return getByteCodeOffset() == imo.getByteCodeOffset();
        } else {
            return false;
        }
    }
}
