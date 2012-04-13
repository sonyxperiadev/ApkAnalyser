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

import org.jf.dexlib.Code.Instruction;

import analyser.gui.MainFrame;
import analyser.gui.actions.bytecodemod.AbstractTreeBytecodeModAction;
import analyser.logic.BytecodeModificationMediator;
import analyser.logic.Reference;
import andreflect.DexMethod;
import andreflect.injection.impl.DalvikMethodOffsetExThrow;

public class DalvikMethodOffsetExThrowAction extends AbstractTreeBytecodeModAction {

    private static final long serialVersionUID = -4840576452152709135L;
    protected static DalvikMethodOffsetExThrowAction m_inst = null;

    public static DalvikMethodOffsetExThrowAction getInstance(MainFrame mainFrame)
    {
        if (m_inst == null)
        {
            m_inst = new DalvikMethodOffsetExThrowAction("Print exception throw", null);
            m_inst.setMainFrame(mainFrame);
        }
        return m_inst;
    }

    protected DalvikMethodOffsetExThrowAction(String arg0, Icon arg1)
    {
        super(arg0, arg1);
    }

    @Override
    protected void modify(MEMethod method) throws Throwable {
        if (method.isAbstract()) {
            return;
        }
        DexMethod dexMethod = (DexMethod) method;
        if (hasThrow(dexMethod)) {
            ((MainFrame) getMainFrame()).getMidletTree().findAndMarkNode(method, Reference.MODIFIED);
        }
    }

    @Override
    protected Injection getInjection(String className, String methodSignature) {
        //not used
        return null;
    }

    public boolean hasThrow(DexMethod method) {
        if (method.getEncodedMethod().codeItem == null) {
            return false;
        }
        boolean ret = false;
        Instruction[] instructions = method.getEncodedMethod().codeItem.getInstructions();
        for (Instruction instruction : instructions) {
            switch (instruction.deodexedInstruction.opcode) {
            case THROW:
                DalvikMethodOffsetExThrow throwInjection = new DalvikMethodOffsetExThrow(getMethodSignature(method),
                        instruction,
                        method.getMEClass().getName() + ":" + getMethodSignature(method));
                BytecodeModificationMediator.getInstance().registerModification(
                        method.getMEClass().getResource().getContext(),
                        method.getMEClass(),
                        throwInjection,
                        method);
                ret = true;
                break;
            }
        }
        return ret;
    }

}