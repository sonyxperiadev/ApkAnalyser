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

package andreflect.gui.action.injection;

import java.util.ArrayList;

import javax.swing.Icon;

import jerl.bcm.inj.Injection;
import mereflect.MEMethod;

import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DebugInfoItem;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Debug.DebugInstructionIterator;

import analyser.gui.MainFrame;
import analyser.gui.actions.bytecodemod.AbstractTreeBytecodeModAction;
import analyser.logic.BytecodeModificationMediator;
import analyser.logic.Reference;
import andreflect.DexMethod;
import andreflect.injection.impl.DalvikMethodLocal;

public class DalvikMethodLocalWriteAction extends AbstractTreeBytecodeModAction {

    private static final long serialVersionUID = -5286241535443961497L;
    protected static DalvikMethodLocalWriteAction m_inst = null;

    public static DalvikMethodLocalWriteAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new DalvikMethodLocalWriteAction("Print writing local variables", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected DalvikMethodLocalWriteAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    protected void modify(final MEMethod method) throws Throwable {
        if (method.isAbstract()) {
            return;
        }
        final DexMethod dexMethod = (DexMethod) method;
        ClassDataItem.EncodedMethod encodedMethod = dexMethod.getEncodedMethod();
        CodeItem codeItem = encodedMethod.codeItem;
        DebugInfoItem debugInfoItem = null;
        if (codeItem != null) {
            debugInfoItem = codeItem.getDebugInfo();
        }
        if (debugInfoItem == null) {
            return;
        }

        final ArrayList<DalvikMethodLocal> startedLocal = new ArrayList<DalvikMethodLocal>();
        DebugInstructionIterator.DecodeInstructions(debugInfoItem, codeItem.getRegisterCount(),
                new DebugInstructionIterator.ProcessDecodedDebugInstructionDelegate() {
                    @Override
                    public void ProcessStartLocal(final int codeAddress, final int length, final int registerNum,
                            final StringIdItem name, final TypeIdItem type) {
                        Instruction begin = dexMethod.getPreviousInstructionAtCodeAddress(codeAddress);
                        if (begin != null && type != null) {
                            DalvikMethodLocal injection = new DalvikMethodLocal(getMethodSignature(method),
                                    method.getMEClass().getName() + ":" + getMethodSignature(method),
                                    begin,
                                    (short) registerNum,
                                    name,
                                    type,
                                    false);
                            startedLocal.add(injection);
                            BytecodeModificationMediator.getInstance().registerModification(
                                    method.getMEClass().getResource().getContext(),
                                    method.getMEClass(),
                                    injection,
                                    method);
                        }
                    }

                    @Override
                    public void ProcessStartLocalExtended(final int codeAddress, final int length,
                            final int registerNum, final StringIdItem name,
                            final TypeIdItem type, final StringIdItem signature) {
                        Instruction begin = dexMethod.getPreviousInstructionAtCodeAddress(codeAddress);
                        if (begin != null && type != null) {
                            DalvikMethodLocal injection = new DalvikMethodLocal(getMethodSignature(method),
                                    method.getMEClass().getName() + ":" + getMethodSignature(method),
                                    begin,
                                    (short) registerNum,
                                    name,
                                    type,
                                    false);
                            startedLocal.add(injection);
                            BytecodeModificationMediator.getInstance().registerModification(
                                    method.getMEClass().getResource().getContext(),
                                    method.getMEClass(),
                                    injection,
                                    method);
                        }
                    }

                    @Override
                    public void ProcessRestartLocal(final int codeAddress, final int length, final int registerNum,
                            final StringIdItem name, final TypeIdItem type,
                            final StringIdItem signature) {
                        Instruction begin = dexMethod.getPreviousInstructionAtCodeAddress(codeAddress);
                        if (begin != null && type != null) {
                            DalvikMethodLocal injection = new DalvikMethodLocal(getMethodSignature(method),
                                    method.getMEClass().getName() + ":" + getMethodSignature(method),
                                    begin,
                                    (short) registerNum,
                                    name,
                                    type,
                                    false);
                            startedLocal.add(injection);
                            BytecodeModificationMediator.getInstance().registerModification(
                                    method.getMEClass().getResource().getContext(),
                                    method.getMEClass(),
                                    injection,
                                    method);
                        }
                    }

                    @Override
                    public void ProcessEndLocal(final int codeAddress, final int length, final int registerNum,
                            final StringIdItem name, final TypeIdItem type,
                            final StringIdItem signature) {
                        Instruction end = dexMethod.getInstructionAtCodeAddress(codeAddress);
                        if (end != null) {
                            for (int i = startedLocal.size() - 1; i >= 0; i--) {
                                DalvikMethodLocal injection = startedLocal.get(i);
                                if (injection.reg == registerNum
                                        && injection.var == name
                                        && injection.type == type) {
                                    injection.endIns = end;
                                    break;
                                }
                            }
                        }
                    }
                });

        if (startedLocal.size() != 0) {
            ((MainFrame) getMainFrame()).getMidletTree().findAndMarkNode(method, Reference.MODIFIED);
        }
    }

    @Override
    protected Injection getInjection(String className, String methodSignature) {
        //not used
        return null;
    }

}