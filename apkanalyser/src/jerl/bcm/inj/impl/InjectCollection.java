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

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class InjectCollection {

    public void injectIDRegistration(MethodVisitor mv, int id) {
        // TODO: change to correct call
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
                "Ljava/io/PrintStream;");
        mv.visitLdcInsn("entry: " + id);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
                "println", "(Ljava/lang/String;)V");
    }

    public void injectSystemOut(MethodVisitor mv, String str) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
                "Ljava/io/PrintStream;");
        mv.visitLdcInsn(str);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
                "println", "(Ljava/lang/String;)V");
    }

    public void injectMemberFieldSystemOut(MethodVisitor mv, String clazz, String field, String type, String printType, String prefix, boolean isStatic) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn(prefix);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V");

        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        if (isStatic) {
            mv.visitFieldInsn(Opcodes.GETSTATIC, clazz, field, type);
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, 0); // this
            mv.visitFieldInsn(Opcodes.GETFIELD, clazz, field, type);
        }
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(" + printType + ")V");
    }

    public void injectMemberFieldArraySystemOut(MethodVisitor mv, String clazz, String field, String type, String printType, String prefix, boolean isStatic, int index) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn(prefix);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V");

        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        if (isStatic) {
            mv.visitFieldInsn(Opcodes.GETSTATIC, clazz, field, type);
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, 0); // this
            mv.visitFieldInsn(Opcodes.GETFIELD, clazz, field, type);
        }
        mv.visitLdcInsn(new Integer(index));

        if (type.endsWith("[Z") || type.endsWith("[B")) {
            mv.visitInsn(Opcodes.BALOAD);
        } else if (type.endsWith("[C")) {
            mv.visitInsn(Opcodes.CALOAD);
        } else if (type.endsWith("[D")) {
            mv.visitInsn(Opcodes.DALOAD);
        } else if (type.endsWith("[F")) {
            mv.visitInsn(Opcodes.FALOAD);
        } else if (type.endsWith("[I")) {
            mv.visitInsn(Opcodes.IALOAD);
        } else if (type.endsWith("[J")) {
            mv.visitInsn(Opcodes.LALOAD);
        } else if (type.endsWith("[S")) {
            mv.visitInsn(Opcodes.SALOAD);
        } else {
            mv.visitInsn(Opcodes.AALOAD);
        }
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(" + printType + ")V");
    }

    public void injectSystemOutCurThread(MethodVisitor mv, String str) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
                "Ljava/io/PrintStream;");
        mv.visitLdcInsn(str);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
                "print", "(Ljava/lang/String;)V");

        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
                "Ljava/io/PrintStream;");
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread",
                "currentThread", "()Ljava/lang/Thread;");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
                "println", "(Ljava/lang/Object;)V");
    }

    public void injectGC(MethodVisitor mv) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "gc", "()V");
    }

    public void injectCrash(MethodVisitor mv) {
        // TODO: change to correct call
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "crash", "()V");
    }

    public void injectRegister(MethodVisitor mv, int id) {
        mv.visitLdcInsn(new Integer(id));
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "jerl/bcm/util/Register", "register", "(I)V");
    }

    public void injectReplaceStackInt(MethodVisitor mv, int i) {
        mv.visitInsn(Opcodes.POP);
        mv.visitLdcInsn(new Integer(i));
    }

    public void injectReturn(MethodVisitor mv) {
        mv.visitInsn(Opcodes.RETURN);
    }

    public void injectPrintRegs(MethodVisitor mv) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "jerl/bcm/util/Register", "printRegistrations", "()V");
    }
}
