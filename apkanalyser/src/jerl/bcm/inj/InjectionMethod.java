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

public abstract class InjectionMethod extends Injection implements Comparable<Object> {
    private final String methodSignature;

    /**
     * 
     * @param signature.
     *            examples: <code><init>(Ljava/io/OutputStream;)V, a(IZ)V</code>
     */
    public InjectionMethod(String signature) {
        methodSignature = signature;
    }

    /**
     * Preform injection.
     * 
     * @param cv
     */
    public abstract void inject(MethodVisitor mv);

    /**
     * Returns the method signature
     * @return
     */
    public String getMethodSignature() {
        return methodSignature;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof InjectionMethod) {
            return getMethodSignature().compareTo(
                    ((InjectionMethod) o).getMethodSignature());
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof InjectionMethod) {
            if (getMethodSignature().equals(((InjectionMethod) o).getMethodSignature()) &&
                    getClass().equals(o.getClass())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "[" + methodSignature + ", type=" + getInjectionType() + "]";
    }

    @Override
    public List<String> getInstanceData() {
        Vector<String> v = new Vector<String>();
        v.add(getMethodSignature());
        return v;
    }
}
