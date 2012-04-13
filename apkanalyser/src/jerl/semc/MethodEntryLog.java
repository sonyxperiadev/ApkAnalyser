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

package jerl.semc;

import java.util.List;
import java.util.Vector;

import jerl.bcm.inj.InjectionMethodEntry;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodEntryLog extends InjectionMethodEntry {
    private final String logMsg;
    private final String className;

    public MethodEntryLog(String signature, String str, String className) {
        super(signature);
        logMsg = str;
        this.className = className;
    }

    @Override
    public void inject(MethodVisitor mv) {
        injectSEMCLogging(mv, logMsg, className);
    }

    public static void injectSEMCLogging(MethodVisitor mv, String logMsg, String className) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, className, FieldAddSEMCLog.LOG_FIELD_NAME, "L" + FieldAddSEMCLog.LOG_CLASS_NAME + ";");
        mv.visitLdcInsn(logMsg);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, FieldAddSEMCLog.LOG_CLASS_NAME, "trace", "(Ljava/lang/String;)V");
    }

    @Override
    public List<String> getInstanceData() {
        Vector<String> v = new Vector<String>();
        v.add(getMethodSignature());
        v.add(logMsg);
        v.add(className);
        return v;
    }
}
