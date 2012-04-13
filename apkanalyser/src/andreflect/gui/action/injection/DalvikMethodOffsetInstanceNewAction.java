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

import javax.swing.Icon;

import jerl.bcm.inj.Injection;
import mereflect.MEMethod;

import org.jf.dexlib.ItemType;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.Code.FiveRegisterInstruction;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.InstructionWithReference;
import org.jf.dexlib.Code.RegisterRangeInstruction;
import org.jf.dexlib.Util.AccessFlags;

import analyser.gui.MainFrame;
import analyser.gui.actions.bytecodemod.AbstractTreeBytecodeModAction;
import analyser.logic.BytecodeModificationMediator;
import analyser.logic.Reference;
import andreflect.DexMethod;
import andreflect.Util;
import andreflect.injection.impl.DalvikMethodOffsetInstanceNew;

public class DalvikMethodOffsetInstanceNewAction extends AbstractTreeBytecodeModAction {

    private static final long serialVersionUID = 4159169784080111449L;
    protected static DalvikMethodOffsetInstanceNewAction m_inst = null;

    public static DalvikMethodOffsetInstanceNewAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new DalvikMethodOffsetInstanceNewAction("Print new instance", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected DalvikMethodOffsetInstanceNewAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    protected void modify(MEMethod method) throws Throwable {
        if (method.isAbstract()) {
            return;
        }
        DexMethod dexMethod = (DexMethod) method;
        if (hasNewInstance(dexMethod)) {
            ((MainFrame) getMainFrame()).getMidletTree().findAndMarkNode(method, Reference.MODIFIED);
        }
    }

    @Override
    protected Injection getInjection(String className, String methodSignature) {
        //not used
        return null;
    }

    public boolean hasNewInstance(DexMethod method) {
        if (method.getEncodedMethod().codeItem == null) {
            return false;
        }
        boolean ret = false;
        Instruction[] instructions = method.getEncodedMethod().codeItem.getInstructions();
        for (Instruction instruction : instructions) {
            short register = -1;
            InstructionWithReference refInstruction = null;
            switch (instruction.deodexedInstruction.opcode) {
            case INVOKE_DIRECT:
                refInstruction = (InstructionWithReference) instruction;
                register = ((FiveRegisterInstruction) instruction).getRegisterD();
                break;
            case INVOKE_DIRECT_RANGE:
                refInstruction = (InstructionWithReference) instruction;
                register = (short) ((RegisterRangeInstruction) instruction).getStartRegister();
                break;
            /*
            case INVOKE_DIRECT_EMPTY:
            refInstruction = (InstructionWithReference)method.getDeodexedInstruction(instruction);
            register = ((FiveRegisterInstruction)refInstruction).getRegisterD();
            break;
            case EXECUTE_INLINE:
            refInstruction = (InstructionWithReference)method.getDeodexedInstruction(instruction);
            if (refInstruction.opcode == Opcode.INVOKE_DIRECT){
            	register = ((FiveRegisterInstruction)refInstruction).getRegisterD();
            }
            break;
            case EXECUTE_INLINE_RANGE:
            refInstruction = (InstructionWithReference)method.getDeodexedInstruction(instruction);
            if (refInstruction.opcode == Opcode.INVOKE_DIRECT_RANGE){
            	register = (short)((RegisterRangeInstruction)refInstruction).getStartRegister();
            }
            break;
             */
            }

            if (register != -1
                    && refInstruction.getReferencedItem().getItemType() == ItemType.TYPE_METHOD_ID_ITEM) {
                MethodIdItem methodIdItem = (MethodIdItem) refInstruction.getReferencedItem();

                if (methodIdItem.getMethodName().getStringValue().equals("<init>")) {
                    DalvikMethodOffsetInstanceNew newInstanceInjection = new DalvikMethodOffsetInstanceNew(getMethodSignature(method),
                            method.getNextInstruction(instruction),
                            method.getMEClass().getName() + ":" + getMethodSignature(method));

                    int totalRegisters = method.getEncodedMethod().codeItem.getRegisterCount();
                    int parameterRegisters = method.getEncodedMethod().method.getPrototype().getParameterRegisterCount();
                    int thisRegister = totalRegisters - parameterRegisters - 1;

                    if (totalRegisters != 0
                            && (method.getEncodedMethod().accessFlags & AccessFlags.STATIC.getValue()) == 0) {
                        newInstanceInjection.setIsThis(thisRegister == register);
                    } else {
                        newInstanceInjection.setIsThis(false);
                    }

                    newInstanceInjection.setRegister(register);
                    newInstanceInjection.setClassName(Util.getClassName(methodIdItem.getContainingClass().getTypeDescriptor()));

                    BytecodeModificationMediator.getInstance().registerModification(
                            method.getMEClass().getResource().getContext(),
                            method.getMEClass(),
                            newInstanceInjection,
                            method);
                    ret = true;
                }
            }
        }
        return ret;
    }

}