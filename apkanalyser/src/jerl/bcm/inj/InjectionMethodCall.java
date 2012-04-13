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

public abstract class InjectionMethodCall extends InjectionMethod {
    private final String callSignature;

    private final boolean isPreModification;

    protected boolean keepExistingCall;

    /**
     * 
     * @param signature
     * @param callSignature
     * @param pre
     *            if <code>true</code> make injection before method call
     *            otherwise after method call
     */
    public InjectionMethodCall(String signature, String callSignature,
            boolean pre) {
        this(signature, callSignature, pre, true);
    }

    public InjectionMethodCall(String signature, String callSignature,
            boolean pre, boolean keepExisting) {
        super(signature);
        this.callSignature = callSignature;
        isPreModification = pre;
        keepExistingCall = keepExisting;
    }

    public String getCallSignature() {
        return callSignature;
    }

    public boolean isPreInjection() {
        return isPreModification;
    }

    public boolean keepExistingCall() {
        return keepExistingCall;
    }

    @Override
    public int getInjectionType() {
        return Injection.METHOD_CALL_INJECTION;
    }

    @Override
    public abstract void inject(MethodVisitor mv);

    /**
     * This method is called prior to any call to
     * <code>inject(MethodVisitor)V</code>.
     * 
     * @param opcode
     * @param owner
     * @param name
     * @param desc
     */
    public void visitMethodInsn(int opcode, String owner, String name,
            String desc) {

    }

    @Override
    public String toString() {
        return "[" + getMethodSignature() + ", type=" + getInjectionType()
                + ", pre=" + isPreModification + ", keepExisting="
                + keepExistingCall + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            // match up to method signature
            InjectionMethodCall imc = (InjectionMethodCall) o;
            if (getCallSignature().equals(imc.getCallSignature()) &&
                    isPreInjection() == imc.isPreInjection() &&
                    keepExistingCall() == imc.keepExistingCall()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public List<String> getInstanceData() {
        Vector<String> v = new Vector<String>();
        v.add(getMethodSignature());
        v.add(getCallSignature());
        v.add(Boolean.toString(isPreInjection()));
        v.add(Boolean.toString(keepExistingCall()));
        return v;
    }
}
