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

import java.util.Vector;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class InjectionMethodAdapter extends MethodAdapter {
    private int byteCodeCounter = 0;

    private final Vector<Injection> offsetInjections = new Vector<Injection>();
    private final Vector<Injection> callInjections = new Vector<Injection>();
    private final Vector<Injection> entryInjections = new Vector<Injection>();
    private final Vector<Injection> exitInjections = new Vector<Injection>();
    private final Vector<Injection> exHandlerInjections = new Vector<Injection>();

    private final Vector<Label> handlerList = new Vector<Label>();

    private boolean isDebugMode = false;

    public InjectionMethodAdapter(MethodVisitor mv, InjectionMethod[] injections) {
        super(mv);

        for (int i = 0; i < injections.length; i++) {
            switch (injections[i].getInjectionType()) {
            case Injection.METHOD_CALL_INJECTION:
                callInjections.add(injections[i]);
                break;
            case Injection.METHOD_ENTRY_INJECTION:
                entryInjections.add(injections[i]);
                break;
            case Injection.METHOD_OFFSET_INJECTION:
                offsetInjections.add(injections[i]);
                break;
            case Injection.METHOD_EXIT_INJECTION:
                exitInjections.add(injections[i]);
                break;
            case Injection.METHOD_EXCEPTION_HANDLER_INJECTION:
                exHandlerInjections.add(injections[i]);
                break;
            }
        }
    }

    public InjectionMethodAdapter(MethodVisitor mv, InjectionMethod[] injections, boolean debugMode) {
        this(mv, injections);
        setDebugMode(debugMode);
    }

    /**
     * @param on
     */
    public void setDebugMode(boolean on) {
        isDebugMode = on;
    }

    @Override
    public void visitCode() {
        // inject method entry
        for (int i = 0; i < entryInjections.size(); i++) {
            InjectionMethodEntry ime = (InjectionMethodEntry) entryInjections.elementAt(i);
            ime.inject(mv);
            if (isDebugMode) {
                System.out.println("\t**Inject: " + ime);
            }
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
            String desc) {
        incByteCodeCounter();
        String methodID = owner + "." + name + desc;
        boolean keepExistingCall = true;
        // inject pre method call
        for (int i = 0; i < callInjections.size(); i++) {
            InjectionMethodCall imc = (InjectionMethodCall) callInjections.elementAt(i);
            if (imc.getCallSignature().equals(methodID) && imc.isPreInjection()) {
                imc.visitMethodInsn(opcode, owner, name, desc);
                imc.inject(mv);
                keepExistingCall = imc.keepExistingCall();
                if (isDebugMode) {
                    System.out.println("\t**Inject: " + imc);
                }
            }
        }
        if (keepExistingCall) {
            mv.visitMethodInsn(opcode, owner, name, desc);
        }
        // inject post method call
        for (int i = 0; i < callInjections.size(); i++) {
            InjectionMethodCall imc = (InjectionMethodCall) callInjections.elementAt(i);
            if (imc.getCallSignature().equals(methodID) && !imc.isPreInjection()) {
                imc.visitMethodInsn(opcode, owner, name, desc);
                imc.inject(mv);
                if (isDebugMode) {
                    System.out.println("\t**Inject: " + imc);
                }
            }
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        incByteCodeCounter();
        mv.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        incByteCodeCounter();
        mv.visitIincInsn(var, increment);
    }

    @Override
    public void visitInsn(int opcode) {
        incByteCodeCounter();
        if (opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN
                || opcode == Opcodes.FRETURN || opcode == Opcodes.DRETURN
                || opcode == Opcodes.ARETURN || opcode == Opcodes.RETURN) {
            for (int i = 0; i < exitInjections.size(); i++) {
                InjectionMethodExit ime = (InjectionMethodExit) exitInjections.elementAt(i);
                ime.inject(mv);
                if (isDebugMode) {
                    System.out.println("\t**Inject: " + ime);
                }
            }
        }
        mv.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        incByteCodeCounter();
        mv.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        incByteCodeCounter();
        mv.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(Object cst) {
        incByteCodeCounter();
        mv.visitLdcInsn(cst);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        incByteCodeCounter();
        mv.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        incByteCodeCounter();
        mv.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitTypeInsn(int opcode, String desc) {
        incByteCodeCounter();
        mv.visitTypeInsn(opcode, desc);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        incByteCodeCounter();
        mv.visitVarInsn(opcode, var);
    }

    private void incByteCodeCounter() {
        for (int i = 0; i < offsetInjections.size(); i++) {
            // check if injection at this offset
            InjectionMethodOffset imo = (InjectionMethodOffset) offsetInjections.elementAt(i);
            if (imo.getByteCodeOffset() == byteCodeCounter) {
                imo.inject(mv);
                if (isDebugMode) {
                    System.out.println("\t**Inject: " + imo);
                }
            }
        }
        byteCodeCounter++;
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack,
            Object[] stack) {
        mv.visitFrame(type, nLocal, local, nStack, stack);
    }

    @Override
    public void visitLabel(Label label) {
        mv.visitLabel(label);
        if (handlerList.remove(label)) {
            for (int i = 0; i < exHandlerInjections.size(); i++) {
                InjectionMethodExceptionHandler ime = (InjectionMethodExceptionHandler) exHandlerInjections.elementAt(i);
                ime.inject(mv);
                if (isDebugMode) {
                    System.out.println("\t**Inject: " + ime);
                }
            }
        }
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler,
            String type) {
        handlerList.add(handler);
        mv.visitTryCatchBlock(start, end, handler, type);
    }
}
